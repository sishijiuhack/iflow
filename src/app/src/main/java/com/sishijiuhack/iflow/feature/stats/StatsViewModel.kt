package com.sishijiuhack.iflow.feature.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.repository.StatsSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository

    val stats: StateFlow<StatsSnapshot> = repository.observeStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsSnapshot(),
        )
}
