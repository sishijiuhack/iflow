package com.sishijiuhack.iflow.feature.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.core.model.formatLedgerTime
import com.sishijiuhack.iflow.core.model.toCategoryEmoji
import com.sishijiuhack.iflow.data.repository.MonthSummary
import com.sishijiuhack.iflow.data.repository.TransactionListItem
import com.sishijiuhack.iflow.domain.model.TransactionType
import com.sishijiuhack.iflow.ui.component.IFlowTextField

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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LedgerHeader()
        IFlowTextField(
            value = uiState.query,
            onValueChange = viewModel::setQuery,
            placeholder = { Text("输入关键词") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        MonthSwitcher(
            monthLabel = uiState.selectedMonth.toLedgerMonthLabel(),
            onPrevious = viewModel::showPreviousMonth,
            onNext = viewModel::showNextMonth,
        )
        MonthSummaryCard(summary = uiState.monthSummary)
        if (uiState.transactions.isEmpty()) {
            EmptyLedgerMessage()
        } else {
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
private fun LedgerHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Outlined.CompareArrows,
                contentDescription = "切换账本",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleIconButton(
                    icon = Icons.Outlined.MoreHoriz,
                    contentDescription = "更多",
                )
                CircleIconButton(
                    icon = Icons.Outlined.AccountCircle,
                    contentDescription = "账户",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = "默认账本",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape,
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
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
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onPrevious) {
                Text("<")
            }
            TextButton(onClick = onNext) {
                Text(">")
            }
        }
        Text(
            text = "收支日历",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .background(
                    color = Color(0xFFF0F0F2),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun EmptyLedgerMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = "本月暂无数据",
            color = Color(0xFFC7C7CC),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun SummaryDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFFFFC02E), CircleShape),
        )
        repeat(2) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFE0E0E2), CircleShape),
            )
        }
    }
}

@Composable
private fun MonthSummaryCard(summary: MonthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "总支出 ⇄",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = MoneyCents(summary.expenseCents).format(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SummaryMetric(
                    label = "总收入",
                    amountCents = summary.incomeCents,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SummaryMetric(
                    label = "月结余",
                    amountCents = summary.balanceCents,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                SummaryDots()
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    amountCents: Long,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = MoneyCents(amountCents).format(),
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
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
                    text = "${transaction.categoryIcon.toCategoryEmoji()} ${transaction.categoryName}",
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
