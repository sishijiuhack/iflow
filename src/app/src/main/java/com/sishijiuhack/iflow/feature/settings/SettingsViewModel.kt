package com.sishijiuhack.iflow.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.data.export.LedgerExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository
    private val exporter = LedgerExporter()

    private val _exportEvent = MutableStateFlow<ExportEvent?>(null)
    val exportEvent: StateFlow<ExportEvent?> = _exportEvent

    fun exportJson() {
        viewModelScope.launch {
            val snapshot = repository.exportSnapshot()
            _exportEvent.value = ExportEvent(
                fileName = "iflow-${snapshot.exportedAt}.json",
                mimeType = "application/json",
                content = exporter.toJson(snapshot),
            )
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val snapshot = repository.exportSnapshot()
            _exportEvent.value = ExportEvent(
                fileName = "iflow-${snapshot.exportedAt}.csv",
                mimeType = "text/csv",
                content = exporter.toCsv(snapshot),
            )
        }
    }

    fun consumeExportEvent() {
        _exportEvent.value = null
    }
}

data class ExportEvent(
    val fileName: String,
    val mimeType: String,
    val content: String,
)
