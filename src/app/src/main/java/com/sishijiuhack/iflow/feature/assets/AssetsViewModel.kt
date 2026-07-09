package com.sishijiuhack.iflow.feature.assets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.repository.AccountBalanceSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssetsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository

    val uiState: StateFlow<AccountBalanceSummary> = repository.observeAccountBalances()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountBalanceSummary(),
        )

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }
}
