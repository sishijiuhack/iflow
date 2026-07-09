package com.sishijiuhack.iflow.feature.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.core.model.MoneyExpression
import com.sishijiuhack.iflow.core.model.MoneyParser
import com.sishijiuhack.iflow.core.model.formatEditableTime
import com.sishijiuhack.iflow.core.model.parseEditableTime
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.repository.SaveTransactionInput
import com.sishijiuhack.iflow.data.repository.SaveTransferInput
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class TransactionFormViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository
    private val transactionId: Long? = savedStateHandle.get<String>("transactionId")?.toLongOrNull()

    private val formState = MutableStateFlow(TransactionFormState(id = transactionId))
    private val saveFinished = MutableStateFlow(false)

    val uiState: StateFlow<TransactionFormUiState> = combine(
        formState,
        repository.observeCategories(),
        repository.observeAccounts(),
        repository.observeSettings(),
        saveFinished,
    ) { form, categories, accounts, settings, finished ->
        val categoriesForType = categories.filter { it.type == form.type }
        val normalizedForm = form.withDefaults(
            categories = categoriesForType,
            accounts = accounts,
            defaultAccountId = settings?.defaultAccountId,
        )
        if (normalizedForm != form) {
            formState.value = normalizedForm
        }
        val amountError = normalizedForm.amountInput
            .takeIf { it.isNotBlank() && MoneyExpression.evaluateCents(it) == null }
            ?.let { "请输入有效金额" }
        val baseAmountCents = MoneyExpression.evaluateCents(normalizedForm.amountInput)
        val discountCents = normalizedForm.discountInput
            .takeIf { it.isNotBlank() }
            ?.let { MoneyExpression.evaluateCents(it) }
        val feeCents = normalizedForm.feeInput
            .takeIf { it.isNotBlank() }
            ?.let { MoneyExpression.evaluateCents(it) }
        val discountError = normalizedForm.discountInput.takeIf { it.isNotBlank() }?.let {
            when {
                discountCents == null -> "请输入有效优惠金额"
                baseAmountCents != null && discountCents >= baseAmountCents -> "优惠金额需小于原金额"
                else -> null
            }
        }
        val feeError = normalizedForm.feeInput.takeIf { it.isNotBlank() }?.let {
            if (feeCents == null) "请输入有效手续费" else null
        }
        val timeError = normalizedForm.occurredAtInput
            .takeIf { it.isNotBlank() && parseEditableTime(it) == null }
            ?.let { "请输入有效时间" }
        val parsedTime = parseEditableTime(normalizedForm.occurredAtInput)
        TransactionFormUiState(
            form = normalizedForm,
            categories = categoriesForType,
            accounts = accounts,
            isEdit = transactionId != null,
            amountError = amountError,
            discountError = discountError,
            feeError = feeError,
            timeError = timeError,
            pickerTimeMillis = parsedTime ?: normalizedForm.occurredAt,
            canSave = amountError == null &&
                discountError == null &&
                feeError == null &&
                timeError == null &&
                baseAmountCents != null &&
                parsedTime != null &&
                normalizedForm.categoryId != null &&
                normalizedForm.accountId != null,
            canSaveTransfer = amountError == null &&
                discountError == null &&
                feeError == null &&
                timeError == null &&
                baseAmountCents != null &&
                parsedTime != null &&
                normalizedForm.transferFromAccountId != null &&
                normalizedForm.transferToAccountId != null &&
                normalizedForm.transferFromAccountId != normalizedForm.transferToAccountId,
            saveFinished = finished,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionFormUiState(),
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
            if (transactionId != null) {
                val transaction = repository.getTransaction(transactionId)
                if (transaction != null) {
                    formState.value = TransactionFormState(
                        id = transaction.id,
                        type = transaction.type,
                        amountInput = MoneyParser.formatInput(transaction.amountCents),
                        categoryId = transaction.categoryId,
                        accountId = transaction.accountId,
                        merchant = transaction.merchant.orEmpty(),
                        note = transaction.note.orEmpty(),
                        occurredAt = transaction.occurredAt,
                        occurredAtInput = transaction.occurredAt.formatEditableTime(),
                        status = transaction.status,
                    )
                }
            }
        }
    }

    fun setType(type: TransactionType) {
        formState.update { it.copy(type = type, categoryId = null) }
    }

    fun setAmount(value: String) {
        if (MoneyExpression.isPotential(value)) {
            formState.update { it.copy(amountInput = value) }
        }
    }

    fun setCategory(id: Long) {
        formState.update { it.copy(categoryId = id) }
    }

    fun setAccount(id: Long) {
        formState.update { it.copy(accountId = id) }
    }

    fun setTransferFromAccount(id: Long) {
        formState.update { it.copy(transferFromAccountId = id) }
    }

    fun setTransferToAccount(id: Long) {
        formState.update { it.copy(transferToAccountId = id) }
    }

    fun setMerchant(value: String) {
        formState.update { it.copy(merchant = value) }
    }

    fun setNote(value: String) {
        formState.update { it.copy(note = value) }
    }

    fun setTag(value: String) {
        formState.update { it.copy(tag = value.trim().removePrefix("#")) }
    }

    fun setReimbursable(value: Boolean) {
        formState.update { it.copy(reimbursable = value) }
    }

    fun setMarked(value: Boolean) {
        formState.update { it.copy(marked = value) }
    }

    fun setDiscount(value: String) {
        if (MoneyExpression.isPotential(value)) {
            formState.update { it.copy(discountInput = value) }
        }
    }

    fun setFee(value: String) {
        if (MoneyExpression.isPotential(value)) {
            formState.update { it.copy(feeInput = value) }
        }
    }

    fun setAttachment(value: String) {
        formState.update { it.copy(attachmentLabel = value.trim()) }
    }

    fun setOccurredAtInput(value: String) {
        formState.update { it.copy(occurredAtInput = value) }
    }

    fun setOccurredAtNow() {
        val now = System.currentTimeMillis()
        formState.update {
            it.copy(
                occurredAt = now,
                occurredAtInput = now.formatEditableTime(),
            )
        }
    }

    fun setOccurredAtDate(year: Int, monthIndex: Int, dayOfMonth: Int) {
        val updated = currentEditableDateTime()
            .withYear(year)
            .withMonth(monthIndex + 1)
            .withDayOfMonth(dayOfMonth)
        updateOccurredAt(updated)
    }

    fun setOccurredAtTime(hour: Int, minute: Int) {
        val updated = currentEditableDateTime()
            .withHour(hour)
            .withMinute(minute)
        updateOccurredAt(updated)
    }

    fun save(closeAfterSave: Boolean = true) {
        val form = formState.value
        val baseAmountCents = MoneyExpression.evaluateCents(form.amountInput) ?: return
        val discountCents = form.discountInput.takeIf { it.isNotBlank() }?.let { MoneyExpression.evaluateCents(it) } ?: 0L
        val amountCents = (baseAmountCents - discountCents).takeIf { it > 0L } ?: return
        val occurredAt = parseEditableTime(form.occurredAtInput) ?: return
        val categoryId = form.categoryId ?: return
        val accountId = form.accountId ?: return
        viewModelScope.launch {
            repository.saveManualTransaction(
                SaveTransactionInput(
                    id = form.id,
                    type = form.type,
                    amountCents = amountCents,
                    categoryId = categoryId,
                    accountId = accountId,
                    merchant = form.merchant,
                    note = form.note.withMeta(form),
                    occurredAt = occurredAt,
                    status = form.status,
                ),
            )
            if (closeAfterSave || form.id != null) {
                saveFinished.value = true
            } else {
                val now = System.currentTimeMillis()
                formState.update {
                    it.copy(
                        id = null,
                        amountInput = "",
                        merchant = "",
                        note = "",
                        tag = "",
                        reimbursable = false,
                        marked = false,
                        discountInput = "",
                        feeInput = "",
                        attachmentLabel = "",
                        occurredAt = now,
                        occurredAtInput = now.formatEditableTime(),
                        status = TransactionStatus.Confirmed,
                    )
                }
            }
        }
    }

    fun saveTransfer() {
        val form = formState.value
        val baseAmountCents = MoneyExpression.evaluateCents(form.amountInput) ?: return
        val discountCents = form.discountInput.takeIf { it.isNotBlank() }?.let { MoneyExpression.evaluateCents(it) } ?: 0L
        val amountCents = (baseAmountCents - discountCents).takeIf { it > 0L } ?: return
        val occurredAt = parseEditableTime(form.occurredAtInput) ?: return
        val fromAccountId = form.transferFromAccountId ?: return
        val toAccountId = form.transferToAccountId ?: return
        if (fromAccountId == toAccountId) return
        viewModelScope.launch {
            repository.saveManualTransfer(
                SaveTransferInput(
                    amountCents = amountCents,
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    note = form.note.withMeta(form),
                    occurredAt = occurredAt,
                ),
            )
            saveFinished.value = true
        }
    }

    private fun currentEditableDateTime(): LocalDateTime {
        val form = formState.value
        val millis = parseEditableTime(form.occurredAtInput) ?: form.occurredAt
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private fun updateOccurredAt(dateTime: LocalDateTime) {
        val millis = dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        formState.update {
            it.copy(
                occurredAt = millis,
                occurredAtInput = millis.formatEditableTime(),
            )
        }
    }
}

data class TransactionFormUiState(
    val form: TransactionFormState = TransactionFormState(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val isEdit: Boolean = false,
    val amountError: String? = null,
    val discountError: String? = null,
    val feeError: String? = null,
    val timeError: String? = null,
    val pickerTimeMillis: Long = System.currentTimeMillis(),
    val canSave: Boolean = false,
    val canSaveTransfer: Boolean = false,
    val saveFinished: Boolean = false,
)

data class TransactionFormState(
    val id: Long? = null,
    val type: TransactionType = TransactionType.Expense,
    val amountInput: String = "",
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val transferFromAccountId: Long? = null,
    val transferToAccountId: Long? = null,
    val merchant: String = "",
    val note: String = "",
    val tag: String = "",
    val reimbursable: Boolean = false,
    val marked: Boolean = false,
    val discountInput: String = "",
    val feeInput: String = "",
    val attachmentLabel: String = "",
    val occurredAt: Long = System.currentTimeMillis(),
    val occurredAtInput: String = occurredAt.formatEditableTime(),
    val status: TransactionStatus = TransactionStatus.Confirmed,
) {
    fun withDefaults(
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
        defaultAccountId: Long?,
    ): TransactionFormState {
        val defaultAccount = accounts.firstOrNull { it.id == defaultAccountId } ?: accounts.firstOrNull()
        val nextAccount = accounts.firstOrNull { it.id != defaultAccount?.id } ?: defaultAccount
        return copy(
            categoryId = categoryId ?: categories.firstOrNull()?.id,
            accountId = accountId ?: defaultAccount?.id,
            transferFromAccountId = transferFromAccountId ?: defaultAccount?.id,
            transferToAccountId = transferToAccountId ?: nextAccount?.id,
        )
    }
}

private fun String.withMeta(form: TransactionFormState): String {
    val parts = mutableListOf<String>()
    val trimmedNote = trim()
    if (trimmedNote.isNotBlank()) parts += trimmedNote
    form.tag.trim().removePrefix("#").takeIf { it.isNotBlank() }?.let { parts += "#$it" }
    if (form.reimbursable) parts += "#报销"
    if (form.marked) parts += "#标记"
    form.discountInput.trim().takeIf { it.isNotBlank() }?.let { parts += "优惠¥$it" }
    form.feeInput.trim().takeIf { it.isNotBlank() }?.let { parts += "手续费¥$it" }
    form.attachmentLabel.trim().takeIf { it.isNotBlank() }?.let { parts += "图片:$it" }
    return parts.distinct().joinToString(" ")
}
