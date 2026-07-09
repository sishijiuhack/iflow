package com.sishijiuhack.iflow.feature.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.toCategoryEmoji
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId

@Composable
fun TransactionFormRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionFormViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val pickerDateTime = remember(uiState.pickerTimeMillis) {
        Instant.ofEpochMilli(uiState.pickerTimeMillis)
            .atZone(zoneId)
            .toLocalDateTime()
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveFinished) {
        if (uiState.saveFinished) {
            onClose()
        }
    }

    if (showDatePicker) {
        DisposableEffect(context, pickerDateTime) {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    viewModel.setOccurredAtDate(year, month, dayOfMonth)
                    showDatePicker = false
                },
                pickerDateTime.year,
                pickerDateTime.monthValue - 1,
                pickerDateTime.dayOfMonth,
            )
            dialog.setOnDismissListener { showDatePicker = false }
            dialog.show()
            onDispose { dialog.dismiss() }
        }
    }

    if (showTimePicker) {
        DisposableEffect(context, pickerDateTime) {
            val dialog = TimePickerDialog(
                context,
                { _, hour, minute ->
                    viewModel.setOccurredAtTime(hour, minute)
                    showTimePicker = false
                },
                pickerDateTime.hour,
                pickerDateTime.minute,
                true,
            )
            dialog.setOnDismissListener { showTimePicker = false }
            dialog.show()
            onDispose { dialog.dismiss() }
        }
    }

    Column(
        modifier = modifier
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

        CategoryGrid(
            categories = uiState.categories,
            selectedCategoryId = uiState.form.categoryId,
            onCategoryClick = viewModel::setCategory,
        )

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

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("日期")
                }
                TextButton(onClick = { showTimePicker = true }) {
                    Text("时间")
                }
                TextButton(onClick = viewModel::setOccurredAtNow) {
                    Text("现在")
                }
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

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("分类", style = MaterialTheme.typography.titleSmall)
        categories.chunked(4).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategoryClick(category.id) },
                        label = {
                            Text("${category.icon.toCategoryEmoji()} ${category.name}")
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(4 - rowCategories.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}
