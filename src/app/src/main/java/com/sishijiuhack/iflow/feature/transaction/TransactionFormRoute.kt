package com.sishijiuhack.iflow.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.domain.model.TransactionType

@Composable
fun TransactionFormRoute(
    onClose: () -> Unit,
    viewModel: TransactionFormViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveFinished) {
        if (uiState.saveFinished) {
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (uiState.isEdit) "编辑流水" else "记一笔",
            style = MaterialTheme.typography.headlineSmall,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.form.type == TransactionType.Expense,
                onClick = { viewModel.setType(TransactionType.Expense) },
                label = { Text("支出") },
            )
            FilterChip(
                selected = uiState.form.type == TransactionType.Income,
                onClick = { viewModel.setType(TransactionType.Income) },
                label = { Text("收入") },
            )
        }

        OutlinedTextField(
            value = uiState.form.amountInput,
            onValueChange = viewModel::setAmount,
            label = { Text("金额") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = uiState.amountError != null,
            supportingText = uiState.amountError?.let { error ->
                { Text(error) }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("分类", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.categories.forEach { category ->
                FilterChip(
                    selected = uiState.form.categoryId == category.id,
                    onClick = { viewModel.setCategory(category.id) },
                    label = { Text(category.name) },
                )
            }
        }

        Text("账户", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.accounts.forEach { account ->
                FilterChip(
                    selected = uiState.form.accountId == account.id,
                    onClick = { viewModel.setAccount(account.id) },
                    label = { Text(account.name) },
                )
            }
        }

        OutlinedTextField(
            value = uiState.form.merchant,
            onValueChange = viewModel::setMerchant,
            label = { Text("商户") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.form.note,
            onValueChange = viewModel::setNote,
            label = { Text("备注") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = uiState.form.occurredAtInput,
                onValueChange = viewModel::setOccurredAtInput,
                label = { Text("时间") },
                isError = uiState.timeError != null,
                supportingText = {
                    Text(uiState.timeError ?: "格式：yyyy-MM-dd HH:mm")
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = viewModel::setOccurredAtNow) {
                Text("现在")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = viewModel::save,
                enabled = uiState.canSave,
            ) {
                Text("保存")
            }
            TextButton(onClick = onClose) {
                Text("取消")
            }
        }
    }
}
