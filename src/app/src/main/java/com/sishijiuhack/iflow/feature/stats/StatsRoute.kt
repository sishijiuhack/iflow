package com.sishijiuhack.iflow.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.data.repository.CategoryExpense

@Composable
fun StatsRoute(
    viewModel: StatsViewModel = viewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val monthlyMax = maxOf(stats.summary.incomeCents, stats.summary.expenseCents, 1L)
    val categoryMax = stats.categoryExpenses.maxOfOrNull { it.amountCents } ?: 1L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "统计",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("今日支出 ${MoneyCents(stats.todayExpenseCents).format()}")
        Text("近7天支出 ${MoneyCents(stats.last7DaysExpenseCents).format()}")
        Text("本月收入 ${MoneyCents(stats.summary.incomeCents).format()}")
        Text("本月支出 ${MoneyCents(stats.summary.expenseCents).format()}")
        Text("本月净额 ${MoneyCents(stats.summary.balanceCents).format()}")
        Text(
            text = "本月收支对比",
            style = MaterialTheme.typography.titleMedium,
        )
        StatBar(
            label = "收入",
            amountCents = stats.summary.incomeCents,
            progress = stats.summary.incomeCents.toFloat() / monthlyMax.toFloat(),
        )
        StatBar(
            label = "支出",
            amountCents = stats.summary.expenseCents,
            progress = stats.summary.expenseCents.toFloat() / monthlyMax.toFloat(),
        )
        Text(
            text = "分类支出排行",
            style = MaterialTheme.typography.titleMedium,
        )
        if (stats.categoryExpenses.isEmpty()) {
            Text("本月还没有支出。")
        } else {
            stats.categoryExpenses.forEachIndexed { index, item ->
                CategoryExpenseBar(
                    index = index,
                    item = item,
                    progress = item.amountCents.toFloat() / categoryMax.toFloat(),
                )
            }
        }
    }
}

@Composable
private fun StatBar(
    label: String,
    amountCents: Long,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$label ${MoneyCents(amountCents).format()}")
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CategoryExpenseBar(
    index: Int,
    item: CategoryExpense,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "${index + 1}. ${item.categoryName} ${MoneyCents(item.amountCents).format()}",
            fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
