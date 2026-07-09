package com.sishijiuhack.iflow.feature.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        TransactionSheetHeader(
            isEdit = uiState.isEdit,
            selectedType = uiState.form.type,
            onTypeSelected = viewModel::setType,
            onClose = onClose,
        )

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.form.type == TransactionType.Income) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("完成")
            }
        }
    }
}

@Composable
private fun TransactionSheetHeader(
    isEdit: Boolean,
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Surface(
            color = Color(0xFFF0F0F2),
            shape = RoundedCornerShape(28.dp),
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TypeSegment(
                    text = "支出",
                    selected = selectedType == TransactionType.Expense,
                    onClick = { onTypeSelected(TransactionType.Expense) },
                )
                TypeSegment(
                    text = "收入",
                    selected = selectedType == TransactionType.Income,
                    onClick = { onTypeSelected(TransactionType.Income) },
                )
                TypeSegment(
                    text = "转账",
                    selected = false,
                    enabled = false,
                    onClick = {},
                )
            }
        }
        Text(
            text = if (isEdit) "编辑流水" else "默认账本",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun TypeSegment(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = text,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                selected -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        categories.chunked(5).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowCategories.forEach { category ->
                    CategoryTile(
                        category = category,
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategoryClick(category.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(5 - rowCategories.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: CategoryEntity,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.error else Color(0xFFF0F0F2),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = category.icon.toCategoryEmoji(),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
