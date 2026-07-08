package com.sishijiuhack.iflow.feature.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
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

    val uiState: StateFlow<LedgerUiState> = combine(
        repository.observeTransactions(),
        query,
        typeFilter,
    ) { transactions, query, typeFilter ->
        LedgerUiState(
            query = query,
            typeFilter = typeFilter,
            transactions = filterTransactions(transactions, query, typeFilter),
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
}

data class LedgerUiState(
    val query: String = "",
    val typeFilter: LedgerTypeFilter = LedgerTypeFilter.All,
    val transactions: List<TransactionListItem> = emptyList(),
)
