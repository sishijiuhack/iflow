package com.sishijiuhack.iflow.feature.pending

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun PendingRoute(
    onEditTransaction: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PendingViewModel = viewModel(),
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("待确认", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("返回") }
        }

        if (transactions.isEmpty()) {
            Text("没有待确认记录。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(transactions, key = { it.id }) { transaction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditTransaction(transaction.id) }
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(transaction.categoryName, style = MaterialTheme.typography.titleMedium)
                            val sign = if (transaction.type == TransactionType.Income) "+" else "-"
                            Text(sign + MoneyCents(transaction.amountCents).format(), fontWeight = FontWeight.SemiBold)
                        }
                        Text("${transaction.accountName} · ${transaction.occurredAt.formatLedgerTime()}")
                        Text(transaction.merchant ?: transaction.note ?: "通知解析")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.confirm(transaction.id) }) {
                                Text("确认")
                            }
                            TextButton(onClick = { onEditTransaction(transaction.id) }) {
                                Text("修改")
                            }
                            TextButton(onClick = { viewModel.delete(transaction.id) }) {
                                Text("删除")
                            }
                        }
                    }
                }
            }
        }
    }
}
