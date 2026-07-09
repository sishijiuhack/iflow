package com.sishijiuhack.iflow.feature.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.core.model.formatLedgerTime
import com.sishijiuhack.iflow.data.repository.MonthSummary
import com.sishijiuhack.iflow.data.repository.TransactionListItem
import com.sishijiuhack.iflow.domain.model.TransactionType

@Composable
fun LedgerRoute(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: LedgerViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateGroups = remember(uiState.transactions) {
        groupTransactionsByDate(uiState.transactions)
    }

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
        MonthSummaryCard(summary = uiState.monthSummary)
        MonthSwitcher(
            monthLabel = uiState.selectedMonth.toLedgerMonthLabel(),
            onPrevious = viewModel::showPreviousMonth,
            onNext = viewModel::showNextMonth,
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
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(dateFilterOptions) { option ->
                FilterChip(
                    selected = uiState.dateFilter == option.filter,
                    onClick = { viewModel.setDateFilter(option.filter) },
                    label = { Text(option.label) },
                )
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = uiState.accountFilter == null,
                    onClick = { viewModel.setAccountFilter(null) },
                    label = { Text("全部账户") },
                )
            }
            items(uiState.accounts, key = { it.id }) { account ->
                FilterChip(
                    selected = uiState.accountFilter == account.id,
                    onClick = { viewModel.setAccountFilter(account.id) },
                    label = { Text(account.name) },
                )
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = uiState.categoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("全部分类") },
                )
            }
            items(uiState.categories, key = { it.id }) { category ->
                FilterChip(
                    selected = uiState.categoryFilter == category.id,
                    onClick = { viewModel.setCategoryFilter(category.id) },
                    label = { Text(category.name) },
                )
            }
        }
        if (uiState.transactions.isEmpty()) {
            Text("还没有流水。")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                dateGroups.forEach { group ->
                    item(key = "date-${group.label}") {
                        Text(
                            text = group.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    items(group.transactions, key = { it.id }) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onClick = { onEditTransaction(transaction.id) },
                            onDelete = { viewModel.deleteTransaction(transaction.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSwitcher(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onPrevious) {
            Text("<")
        }
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp),
        )
        TextButton(onClick = onNext) {
            Text(">")
        }
    }
}

@Composable
private fun MonthSummaryCard(summary: MonthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "本月概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryMetric(
                    label = "收入",
                    amountCents = summary.incomeCents,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "支出",
                    amountCents = summary.expenseCents,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                SummaryMetric(
                    label = "结余",
                    amountCents = summary.balanceCents,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    amountCents: Long,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = MoneyCents(amountCents).format(),
                color = color,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionListItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = transaction.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = transaction.merchant ?: transaction.note ?: "无备注",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            val sign = if (transaction.type == TransactionType.Income) "+" else "-"
            val amountColor = if (transaction.type == TransactionType.Income) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.error
            }
            Text(
                text = sign + MoneyCents(transaction.amountCents).format(),
                color = amountColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${transaction.accountName} · ${transaction.occurredAt.formatLedgerTime()}",
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = onDelete) {
                Text("删除")
            }
        }
        HorizontalDivider()
    }
}

private data class DateFilterOption(
    val filter: LedgerDateFilter,
    val label: String,
)

private val dateFilterOptions = listOf(
    DateFilterOption(LedgerDateFilter.All, "全部日期"),
    DateFilterOption(LedgerDateFilter.Today, "今天"),
    DateFilterOption(LedgerDateFilter.Last7Days, "近7天"),
    DateFilterOption(LedgerDateFilter.ThisMonth, "本月"),
)
