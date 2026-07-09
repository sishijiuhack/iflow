package com.sishijiuhack.iflow.feature.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.repository.MonthSummary
import com.sishijiuhack.iflow.data.repository.TransactionListItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository
    private val zoneId = ZoneId.systemDefault()
    private val query = MutableStateFlow("")
    private val typeFilter = MutableStateFlow(LedgerTypeFilter.All)
    private val dateFilter = MutableStateFlow(LedgerDateFilter.All)
    private val accountFilter = MutableStateFlow<Long?>(null)
    private val categoryFilter = MutableStateFlow<Long?>(null)
    private val selectedMonth = MutableStateFlow(YearMonth.now(zoneId))
    private val monthSummary = selectedMonth.flatMapLatest { month ->
        repository.observeMonthSummary(zoneId = zoneId, month = month)
    }

    private val filters = combine(
        query,
        typeFilter,
        dateFilter,
        accountFilter,
        categoryFilter,
    ) { query, typeFilter, dateFilter, accountId, categoryId ->
        LedgerFilters(
            query = query,
            typeFilter = typeFilter,
            dateFilter = dateFilter,
            accountId = accountId,
            categoryId = categoryId,
        )
    }

    private val ledgerBase = combine(
        repository.observeTransactions(),
        repository.observeAccounts(),
        repository.observeCategories(),
        filters,
    ) { transactions, accounts, categories, filters ->
        LedgerBase(
            transactions = transactions,
            accounts = accounts,
            categories = categories,
            filters = filters,
        )
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        ledgerBase,
        selectedMonth,
        monthSummary,
    ) { base, selectedMonth, monthSummary ->
        val filters = base.filters
        LedgerUiState(
            query = filters.query,
            typeFilter = filters.typeFilter,
            dateFilter = filters.dateFilter,
            accountFilter = filters.accountId,
            categoryFilter = filters.categoryId,
            selectedMonth = selectedMonth,
            monthSummary = monthSummary,
            accounts = base.accounts,
            categories = base.categories,
            transactions = filterTransactions(
                transactions = base.transactions,
                query = filters.query,
                typeFilter = filters.typeFilter,
                dateFilter = filters.dateFilter,
                accountId = filters.accountId,
                categoryId = filters.categoryId,
            ),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LedgerUiState(),
        )

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.softDeleteTransaction(id)
        }
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun setTypeFilter(value: LedgerTypeFilter) {
        typeFilter.update { value }
    }

    fun setDateFilter(value: LedgerDateFilter) {
        dateFilter.update { value }
    }

    fun setAccountFilter(value: Long?) {
        accountFilter.update { value }
    }

    fun setCategoryFilter(value: Long?) {
        categoryFilter.update { value }
    }

    fun showPreviousMonth() {
        selectedMonth.update { it.previousLedgerMonth() }
    }

    fun showNextMonth() {
        selectedMonth.update { it.nextLedgerMonth() }
    }
}

data class LedgerUiState(
    val query: String = "",
    val typeFilter: LedgerTypeFilter = LedgerTypeFilter.All,
    val dateFilter: LedgerDateFilter = LedgerDateFilter.All,
    val accountFilter: Long? = null,
    val categoryFilter: Long? = null,
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthSummary: MonthSummary = MonthSummary(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionListItem> = emptyList(),
)

private data class LedgerFilters(
    val query: String,
    val typeFilter: LedgerTypeFilter,
    val dateFilter: LedgerDateFilter,
    val accountId: Long?,
    val categoryId: Long?,
)

private data class LedgerBase(
    val transactions: List<TransactionListItem>,
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val filters: LedgerFilters,
)
