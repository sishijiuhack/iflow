package com.sishijiuhack.iflow.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
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
import com.sishijiuhack.iflow.core.model.formatLedgerTime
import com.sishijiuhack.iflow.domain.model.TransactionType

@Composable
fun HomeRoute(
    onAddTransaction: () -> Unit,
    onOpenPending: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "本月概览",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "收入 ${MoneyCents(uiState.summary.incomeCents).format()} · 支出 ${MoneyCents(uiState.summary.expenseCents).format()} · 结余 ${MoneyCents(uiState.summary.balanceCents).format()}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onAddTransaction,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("记一笔")
        }
        TextButton(onClick = onOpenPending) {
            Text("待确认 ${uiState.pendingCount} 笔")
        }
        Text(
            text = "近期流水",
            style = MaterialTheme.typography.titleMedium,
        )
        if (uiState.recentTransactions.isEmpty()) {
            Text("还没有流水。")
        } else {
            uiState.recentTransactions.forEach { transaction ->
                val sign = if (transaction.type == TransactionType.Income) "+" else "-"
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${transaction.categoryName} · ${sign}${MoneyCents(transaction.amountCents).format()}",
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${transaction.accountName} · ${transaction.occurredAt.formatLedgerTime()} · ${transaction.merchant ?: transaction.note.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
