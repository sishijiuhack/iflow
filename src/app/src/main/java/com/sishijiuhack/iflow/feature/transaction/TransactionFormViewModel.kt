package com.sishijiuhack.iflow.feature.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.core.model.MoneyParser
import com.sishijiuhack.iflow.core.model.formatEditableTime
import com.sishijiuhack.iflow.core.model.parseEditableTime
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.repository.SaveTransactionInput
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            .takeIf { it.isNotBlank() && MoneyParser.parseCents(it) == null }
            ?.let { "请输入有效金额" }
        val timeError = normalizedForm.occurredAtInput
            .takeIf { it.isNotBlank() && parseEditableTime(it) == null }
            ?.let { "请输入有效时间" }
        TransactionFormUiState(
            form = normalizedForm,
            categories = categoriesForType,
            accounts = accounts,
            isEdit = transactionId != null,
            amountError = amountError,
            timeError = timeError,
            canSave = amountError == null &&
                timeError == null &&
                MoneyParser.parseCents(normalizedForm.amountInput) != null &&
                parseEditableTime(normalizedForm.occurredAtInput) != null &&
                normalizedForm.categoryId != null &&
                normalizedForm.accountId != null,
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
        if (value.isBlank() || MoneyParser.isPotentialAmount(value)) {
            formState.update { it.copy(amountInput = value) }
        }
    }

    fun setCategory(id: Long) {
        formState.update { it.copy(categoryId = id) }
    }

    fun setAccount(id: Long) {
        formState.update { it.copy(accountId = id) }
    }

    fun setMerchant(value: String) {
        formState.update { it.copy(merchant = value) }
    }

    fun setNote(value: String) {
        formState.update { it.copy(note = value) }
    }

    fun setOccurredAtInput(value: String) {
        formState.update { it.copy(occurredAtInput = value) }
    }

    fun save() {
        val form = formState.value
        val amountCents = MoneyParser.parseCents(form.amountInput) ?: return
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
                    note = form.note,
                    occurredAt = occurredAt,
                    status = form.status,
                ),
            )
            saveFinished.value = true
        }
    }
}

data class TransactionFormUiState(
    val form: TransactionFormState = TransactionFormState(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val isEdit: Boolean = false,
    val amountError: String? = null,
    val timeError: String? = null,
    val canSave: Boolean = false,
    val saveFinished: Boolean = false,
)

data class TransactionFormState(
    val id: Long? = null,
    val type: TransactionType = TransactionType.Expense,
    val amountInput: String = "",
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val merchant: String = "",
    val note: String = "",
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
        return copy(
            categoryId = categoryId ?: categories.firstOrNull()?.id,
            accountId = accountId ?: defaultAccount?.id,
        )
    }
}
