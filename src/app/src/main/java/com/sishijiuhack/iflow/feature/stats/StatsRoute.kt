package com.sishijiuhack.iflow.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@Composable
fun StatsRoute(
    viewModel: StatsViewModel = viewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

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
            text = "分类支出排行",
            style = MaterialTheme.typography.titleMedium,
        )
        if (stats.categoryExpenses.isEmpty()) {
            Text("本月还没有支出。")
        } else {
            stats.categoryExpenses.forEachIndexed { index, item ->
                Text(
                    text = "${index + 1}. ${item.categoryName} ${MoneyCents(item.amountCents).format()}",
                    fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}
