package com.sishijiuhack.iflow.feature.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.repository.TransactionListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository
    private val query = MutableStateFlow("")
    private val typeFilter = MutableStateFlow(LedgerTypeFilter.All)
    private val dateFilter = MutableStateFlow(LedgerDateFilter.All)
    private val accountFilter = MutableStateFlow<Long?>(null)

    private val filters = combine(
        query,
        typeFilter,
        dateFilter,
        accountFilter,
    ) { query, typeFilter, dateFilter, accountId ->
        LedgerFilters(
            query = query,
            typeFilter = typeFilter,
            dateFilter = dateFilter,
            accountId = accountId,
        )
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.observeTransactions(),
        repository.observeAccounts(),
        filters,
    ) { transactions, accounts, filters ->
        LedgerUiState(
            query = filters.query,
            typeFilter = filters.typeFilter,
            dateFilter = filters.dateFilter,
            accountFilter = filters.accountId,
            accounts = accounts,
            transactions = filterTransactions(
                transactions = transactions,
                query = filters.query,
                typeFilter = filters.typeFilter,
                dateFilter = filters.dateFilter,
                accountId = filters.accountId,
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
}

data class LedgerUiState(
    val query: String = "",
    val typeFilter: LedgerTypeFilter = LedgerTypeFilter.All,
    val dateFilter: LedgerDateFilter = LedgerDateFilter.All,
    val accountFilter: Long? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionListItem> = emptyList(),
)

private data class LedgerFilters(
    val query: String,
    val typeFilter: LedgerTypeFilter,
    val dateFilter: LedgerDateFilter,
    val accountId: Long?,
)
