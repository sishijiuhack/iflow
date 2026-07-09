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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.toCategoryEmoji
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.domain.model.AccountType
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
    var showAccountPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(EntryMode.Expense) }

    LaunchedEffect(uiState.saveFinished) {
        if (uiState.saveFinished) {
            onClose()
        }
    }

    LaunchedEffect(uiState.form.type) {
        if (selectedMode != EntryMode.Transfer) {
            selectedMode = uiState.form.type.toEntryMode()
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

    if (showAccountPicker) {
        AccountPickerDialog(
            accounts = uiState.accounts,
            selectedAccountId = uiState.form.accountId,
            onDismiss = { showAccountPicker = false },
            onAccountSelected = { accountId ->
                viewModel.setAccount(accountId)
                showAccountPicker = false
            },
        )
    }

    if (showTagPicker) {
        TagPickerDialog(
            selectedTag = uiState.form.tag,
            onDismiss = { showTagPicker = false },
            onTagSelected = { tag ->
                viewModel.setTag(tag)
                showTagPicker = false
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TransactionSheetHeader(
            isEdit = uiState.isEdit,
            selectedMode = selectedMode,
            onModeSelected = { mode ->
                selectedMode = mode
                when (mode) {
                    EntryMode.Expense -> viewModel.setType(TransactionType.Expense)
                    EntryMode.Income -> viewModel.setType(TransactionType.Income)
                    EntryMode.Transfer -> Unit
                }
            },
            onClose = onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (selectedMode == EntryMode.Transfer) {
                TransferAccountCards(accounts = uiState.accounts)
            } else {
                CategoryGrid(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.form.categoryId,
                    onCategoryClick = viewModel::setCategory,
                )
            }
        }

        Column(
            modifier = Modifier.navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TransactionActionChips(
                mode = selectedMode,
                selectedAccountName = uiState.accounts.firstOrNull { it.id == uiState.form.accountId }?.name,
                selectedTag = uiState.form.tag,
                onAccountClick = { showAccountPicker = true },
                onTagClick = { showTagPicker = true },
            )

            AmountInputCard(
                amountInput = uiState.form.amountInput,
                amountError = uiState.amountError,
                note = uiState.form.note,
                onNoteChange = viewModel::setNote,
                occurredAtInput = uiState.form.occurredAtInput,
                timeError = uiState.timeError,
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true },
                mode = selectedMode,
            )

            NumberKeyboard(
                amountInput = uiState.form.amountInput,
                onAmountChange = viewModel::setAmount,
                onDone = { viewModel.save(closeAfterSave = true) },
                onSaveAndContinue = { viewModel.save(closeAfterSave = false) },
                canSave = uiState.canSave && selectedMode != EntryMode.Transfer,
                canSaveAndContinue = uiState.canSave && !uiState.isEdit && selectedMode != EntryMode.Transfer,
                mode = selectedMode,
            )
        }
    }
}

@Composable
private fun TransactionSheetHeader(
    isEdit: Boolean,
    selectedMode: EntryMode,
    onModeSelected: (EntryMode) -> Unit,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
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
                    selected = selectedMode == EntryMode.Expense,
                    onClick = { onModeSelected(EntryMode.Expense) },
                )
                TypeSegment(
                    text = "收入",
                    selected = selectedMode == EntryMode.Income,
                    onClick = { onModeSelected(EntryMode.Income) },
                )
                TypeSegment(
                    text = "转账",
                    selected = selectedMode == EntryMode.Transfer,
                    onClick = { onModeSelected(EntryMode.Transfer) },
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
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun TransactionActionChips(
    mode: EntryMode,
    selectedAccountName: String?,
    selectedTag: String,
    onAccountClick: () -> Unit,
    onTagClick: () -> Unit,
) {
    val tagText = selectedTag.takeIf { it.isNotBlank() }?.let { "#$it" } ?: "标签"
    val chips = when (mode) {
        EntryMode.Expense -> listOf(
            ActionChipSpec(selectedAccountName ?: "选择账户", "💳", null, onAccountClick),
            ActionChipSpec("报销", "○", null),
            ActionChipSpec("优惠", "🎁", null),
            ActionChipSpec("图片", null, Icons.Outlined.Image),
            ActionChipSpec(tagText, null, Icons.AutoMirrored.Outlined.Label, onTagClick),
        )
        EntryMode.Income -> listOf(
            ActionChipSpec(selectedAccountName ?: "选择账户", "💳", null, onAccountClick),
            ActionChipSpec("图片", null, Icons.Outlined.Image),
            ActionChipSpec(tagText, null, Icons.AutoMirrored.Outlined.Label, onTagClick),
            ActionChipSpec("标记", "★", null),
        )
        EntryMode.Transfer -> listOf(
            ActionChipSpec("优惠", "🎁", null),
            ActionChipSpec("图片", null, Icons.Outlined.Image),
            ActionChipSpec(tagText, null, Icons.AutoMirrored.Outlined.Label, onTagClick),
            ActionChipSpec("手续费", "¥", null),
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            ActionPill(text = chip.text, iconText = chip.iconText, icon = chip.icon, onClick = chip.onClick)
        }
    }
}

@Composable
private fun ActionPill(
    text: String,
    iconText: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (iconText != null) {
                Text(text = iconText, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AccountPickerDialog(
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    onDismiss: () -> Unit,
    onAccountSelected: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择账户",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                accounts.forEach { account ->
                    val selected = account.id == selectedAccountId
                    Surface(
                        color = if (selected) Color(0xFFEAF3FF) else Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(18.dp),
                        onClick = { onAccountSelected(account.id) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = accountIcon(account), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (selected) {
                                Text(
                                    text = "当前",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun TagPickerDialog(
    selectedTag: String,
    onDismiss: () -> Unit,
    onTagSelected: (String) -> Unit,
) {
    var tagInput by remember(selectedTag) { mutableStateOf(selectedTag) }
    val normalizedTag = tagInput.trim().removePrefix("#")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("×", style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    text = "选择标签",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(
                    onClick = { onTagSelected(normalizedTag) },
                    enabled = normalizedTag.isNotBlank(),
                ) {
                    Text("完成")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it.take(16) },
                    placeholder = { Text("搜索或创建标签") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (normalizedTag.isBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "暂无标签",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "试试在搜索框中创建新标签",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFFEAF3FF),
                        shape = RoundedCornerShape(18.dp),
                        onClick = { onTagSelected(normalizedTag) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "#$normalizedTag",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (selectedTag == normalizedTag) "当前" else "创建",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Text(
                    text = "标签管理 >",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun AmountInputCard(
    amountInput: String,
    amountError: String?,
    note: String,
    onNoteChange: (String) -> Unit,
    occurredAtInput: String,
    timeError: String?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    mode: EntryMode,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "¥" + (amountInput.ifBlank { "0.00" }),
                color = mode.tintColor(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (amountError != null) {
                Text(
                    text = amountError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(18.dp),
                    onClick = onDateClick,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(occurredAtInput.takeLast(5))
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    placeholder = { Text("点击填写备注") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onTimeClick) {
                    Text("时间")
                }
            }
            if (timeError != null) {
                Text(
                    text = timeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun NumberKeyboard(
    amountInput: String,
    onAmountChange: (String) -> Unit,
    onDone: () -> Unit,
    onSaveAndContinue: () -> Unit,
    canSave: Boolean,
    canSaveAndContinue: Boolean,
    mode: EntryMode,
) {
    val rows = listOf(
        listOf("1", "2", "3", "⌫"),
        listOf("4", "5", "6", "C"),
        listOf("7", "8", "9", "÷"),
        listOf(".", "0", "保存再记", "完成"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("+", "-", "×", "÷").forEach { operator ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    onClick = { appendAmountKey(amountInput, operator, onAmountChange) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = operator,
                            color = mode.tintColor(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    val isDone = key == "完成"
                    val isSaveAndContinue = key == "保存再记"
                    Surface(
                        color = when {
                            isDone -> mode.tintColor()
                            else -> MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(18.dp),
                        onClick = {
                            when (key) {
                                "完成" -> if (canSave) onDone()
                                "保存再记" -> if (canSaveAndContinue) onSaveAndContinue()
                                "⌫" -> onAmountChange(amountInput.dropLast(1))
                                "C" -> onAmountChange("")
                                "÷" -> Unit
                                else -> appendAmountKey(amountInput, key, onAmountChange)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                color = when {
                                    isDone -> MaterialTheme.colorScheme.onError
                                    isSaveAndContinue && !canSaveAndContinue -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferAccountCards(
    accounts: List<AccountEntity>,
) {
    val fromAccount = accounts.firstOrNull()?.name ?: "选择转出账户"
    val toAccount = accounts.drop(1).firstOrNull()?.name ?: "选择转入账户"
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TransferAccountCard(
            label = "转出账户",
            accountName = fromAccount,
            iconText = "↑",
        )
        TransferAccountCard(
            label = "转入账户",
            accountName = toAccount,
            iconText = "↓",
        )
        Text(
            text = "转账保存流程将在账户模型扩展后启用",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun TransferAccountCard(
    label: String,
    accountName: String,
    iconText: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEAF3FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = iconText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun appendAmountKey(
    current: String,
    key: String,
    onAmountChange: (String) -> Unit,
) {
    val operators = setOf("+", "-", "×", "÷")
    if (key in operators) {
        if (current.isBlank()) return
        val trimmed = current.dropLastWhile { it.isWhitespace() }
        val next = if (trimmed.last().toString() in operators) {
            trimmed.dropLast(1) + key
        } else {
            current + key
        }
        onAmountChange(next)
        return
    }
    val currentPart = current.substringAfterLast('+').substringAfterLast('-').substringAfterLast('×').substringAfterLast('÷')
    if (key == "." && currentPart.contains(".")) return
    val next = if (current == "0" && key != ".") key else current + key
    onAmountChange(next)
}

private data class ActionChipSpec(
    val text: String,
    val iconText: String?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val onClick: (() -> Unit)? = null,
)

private fun accountIcon(account: AccountEntity): String {
    return when (account.type) {
        AccountType.Cash -> "💵"
        AccountType.Bank -> "💳"
        AccountType.Wechat -> "💬"
        AccountType.Alipay -> "🔵"
        AccountType.Other -> "💼"
    }
}

private enum class EntryMode {
    Expense,
    Income,
    Transfer,
}

private fun TransactionType.toEntryMode(): EntryMode {
    return when (this) {
        TransactionType.Expense -> EntryMode.Expense
        TransactionType.Income -> EntryMode.Income
    }
}

@Composable
private fun EntryMode.tintColor(): Color {
    return when (this) {
        EntryMode.Expense -> MaterialTheme.colorScheme.error
        EntryMode.Income -> MaterialTheme.colorScheme.secondary
        EntryMode.Transfer -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(5).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.error else Color(0xFFF0F0F2),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = category.icon.toCategoryEmoji(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
