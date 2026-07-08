package com.sishijiuhack.iflow.feature.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.core.model.formatLedgerTime
import com.sishijiuhack.iflow.domain.model.TransactionType

@Composable
fun LedgerRoute(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: LedgerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "流水",
            style = MaterialTheme.typography.headlineSmall,
        )
        Button(
            onClick = onAddTransaction,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("新增流水")
        }
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::setQuery,
            label = { Text("搜索") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.typeFilter == LedgerTypeFilter.All,
                onClick = { viewModel.setTypeFilter(LedgerTypeFilter.All) },
                label = { Text("全部") },
            )
            FilterChip(
                selected = uiState.typeFilter == LedgerTypeFilter.Expense,
                onClick = { viewModel.setTypeFilter(LedgerTypeFilter.Expense) },
                label = { Text("支出") },
            )
            FilterChip(
                selected = uiState.typeFilter == LedgerTypeFilter.Income,
                onClick = { viewModel.setTypeFilter(LedgerTypeFilter.Income) },
                label = { Text("收入") },
            )
        }
        if (uiState.transactions.isEmpty()) {
            Text("还没有流水。")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditTransaction(transaction.id) }
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = transaction.categoryName,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            val sign = if (transaction.type == TransactionType.Income) "+" else "-"
                            Text(
                                text = sign + MoneyCents(transaction.amountCents).format(),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            text = "${transaction.accountName} · ${transaction.occurredAt.formatLedgerTime()}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = transaction.merchant ?: transaction.note ?: "无备注",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = { viewModel.deleteTransaction(transaction.id) }) {
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}
