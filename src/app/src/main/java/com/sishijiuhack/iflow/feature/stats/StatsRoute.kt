package com.sishijiuhack.iflow.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.data.repository.CategoryExpense
import com.sishijiuhack.iflow.data.repository.DailyExpense

@Composable
fun StatsRoute(
    viewModel: StatsViewModel = viewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    var selectedRange by remember { mutableStateOf("月") }
    val monthlyMax = maxOf(stats.summary.incomeCents, stats.summary.expenseCents, 1L)
    val dailyMax = stats.dailyExpenses.maxOfOrNull { it.amountCents } ?: 1L
    val categoryMax = stats.categoryExpenses.maxOfOrNull { it.amountCents } ?: 1L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatsHeader()
        RangeSelector(
            selectedRange = selectedRange,
            onRangeSelected = { selectedRange = it },
        )
        IncomeExpenseCard(
            incomeCents = stats.summary.incomeCents,
            expenseCents = stats.summary.expenseCents,
            balanceCents = stats.summary.balanceCents,
            monthlyMax = monthlyMax,
        )
        QuickStatsGrid(
            todayExpenseCents = stats.todayExpenseCents,
            last7DaysExpenseCents = stats.last7DaysExpenseCents,
        )
        DetailSectionCard(
            title = "报销统计",
            items = listOf(
                "待报销" to 0L,
                "已报销" to 0L,
                "报销入账" to 0L,
                "报销收入" to 0L,
            ),
        )
        DetailSectionCard(
            title = "流转统计",
            items = listOf(
                "还款" to 0L,
                "收款" to 0L,
                "转账" to 0L,
                "充值" to 0L,
            ),
        )
        CategoryDetailCard(
            totalExpenseCents = stats.summary.expenseCents,
            categoryExpenses = stats.categoryExpenses,
            categoryMax = categoryMax,
        )
        DailyTrendCard(
            dailyExpenses = stats.dailyExpenses,
            dailyMax = dailyMax,
        )
        FooterActions()
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun StatsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "默认账本",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundIconButton(
                icon = Icons.Outlined.FilterList,
                contentDescription = "筛选账本",
            )
            RoundIconButton(
                icon = Icons.Outlined.MoreHoriz,
                contentDescription = "更多统计操作",
            )
        }
    }
}

@Composable
private fun RangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEDEEF0),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf("周", "月", "年", "全部").forEach { range ->
                val selected = selectedRange == range
                Surface(
                    modifier = Modifier.weight(1f),
                    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                    onClick = { onRangeSelected(range) },
                ) {
                    Text(
                        text = range,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeExpenseCard(
    incomeCents: Long,
    expenseCents: Long,
    balanceCents: Long,
    monthlyMax: Long,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "收支统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatMetric(
                    label = "支出",
                    amountCents = expenseCents,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                StatMetric(
                    label = "收入",
                    amountCents = incomeCents,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                StatMetric(
                    label = "结余",
                    amountCents = balanceCents,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
            StatBar(
                label = "收入",
                amountCents = incomeCents,
                progress = incomeCents.toFloat() / monthlyMax.toFloat(),
                color = MaterialTheme.colorScheme.secondary,
            )
            StatBar(
                label = "支出",
                amountCents = expenseCents,
                progress = expenseCents.toFloat() / monthlyMax.toFloat(),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun QuickStatsGrid(
    todayExpenseCents: Long,
    last7DaysExpenseCents: Long,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SmallStatCard(
            label = "今日支出",
            amountCents = todayExpenseCents,
            modifier = Modifier.weight(1f),
        )
        SmallStatCard(
            label = "近7天支出",
            amountCents = last7DaysExpenseCents,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CategoryDetailCard(
    totalExpenseCents: Long,
    categoryExpenses: List<CategoryExpense>,
    categoryMax: Long,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "分类详情",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "支出 · 一级",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            CategoryDonut(
                totalExpenseCents = totalExpenseCents,
                categoryExpenses = categoryExpenses,
            )
            if (categoryExpenses.isEmpty()) {
                Text(
                    text = "本月还没有支出。",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                categoryExpenses.take(5).forEachIndexed { index, item ->
                    CategoryExpenseBar(
                        index = index,
                        item = item,
                        progress = item.amountCents.toFloat() / categoryMax.toFloat(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDonut(
    totalExpenseCents: Long,
    categoryExpenses: List<CategoryExpense>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val colors = listOf(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            Color(0xFFFF9500),
            Color(0xFF8E8E93),
        )
        Canvas(modifier = Modifier.size(168.dp)) {
            val strokeWidth = 24.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)
            if (categoryExpenses.isEmpty() || totalExpenseCents <= 0L) {
                drawArc(
                    color = surfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            } else {
                var startAngle = -90f
                categoryExpenses.take(5).forEachIndexed { index, item ->
                    val sweep = (item.amountCents.toFloat() / totalExpenseCents.toFloat()) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep.coerceAtLeast(3f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    startAngle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "总支出",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = MoneyCents(totalExpenseCents).format(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    items: List<Pair<String, Long>>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item ->
                        CompactMetric(
                            label = item.first,
                            amountCents = item.second,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(2 - rowItems.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTrendCard(
    dailyExpenses: List<DailyExpense>,
    dailyMax: Long,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "近 7 天每日支出",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            dailyExpenses.forEach { item ->
                DailyExpenseBar(
                    item = item,
                    progress = item.amountCents.toFloat() / dailyMax.toFloat(),
                )
            }
        }
    }
}

@Composable
private fun FooterActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FooterAction(
            icon = Icons.Outlined.Share,
            label = "分享页面",
            modifier = Modifier.weight(1f),
        )
        FooterAction(
            icon = Icons.Outlined.SwapVert,
            label = "图表排序",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatMetric(
    label: String,
    amountCents: Long,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = MoneyCents(amountCents).format(),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SmallStatCard(
    label: String,
    amountCents: Long,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = MoneyCents(amountCents).format(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CompactMetric(
    label: String,
    amountCents: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = MoneyCents(amountCents).format(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatBar(
    label: String,
    amountCents: Long,
    progress: Float,
    color: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            Text(MoneyCents(amountCents).format())
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = Color(0xFFEDEEF0),
        )
    }
}

@Composable
private fun DailyExpenseBar(
    item: DailyExpense,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(item.label)
            Text(MoneyCents(item.amountCents).format())
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error,
            trackColor = Color(0xFFEDEEF0),
        )
    }
}

@Composable
private fun CategoryExpenseBar(
    index: Int,
    item: CategoryExpense,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${index + 1}. ${item.categoryName}",
                fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = MoneyCents(item.amountCents).format(),
                fontWeight = FontWeight.SemiBold,
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error,
            trackColor = Color(0xFFEDEEF0),
        )
    }
}

@Composable
private fun FooterAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
