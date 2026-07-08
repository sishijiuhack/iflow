package com.sishijiuhack.iflow.feature.pending

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.repository.TransactionListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository

    val transactions: StateFlow<List<TransactionListItem>> = repository.observePendingTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun confirm(id: Long) {
        viewModelScope.launch {
            repository.confirmPendingTransaction(id)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.softDeleteTransaction(id)
        }
    }
}
