package com.sishijiuhack.iflow.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.repository.MonthSummary
import com.sishijiuhack.iflow.data.repository.TransactionListItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeMonthSummary(),
        repository.observePendingCount(),
        repository.observeRecentTransactions(limit = 5),
    ) { summary, pendingCount, recentTransactions ->
        HomeUiState(
            summary = summary,
            pendingCount = pendingCount,
            recentTransactions = recentTransactions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }
}

data class HomeUiState(
    val summary: MonthSummary = MonthSummary(),
    val pendingCount: Int = 0,
    val recentTransactions: List<TransactionListItem> = emptyList(),
)
