package com.sishijiuhack.iflow.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sishijiuhack.iflow.core.android.appContainer
import com.sishijiuhack.iflow.core.model.formatExportFileTime
import com.sishijiuhack.iflow.data.export.LedgerExporter
import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.appContainer().ledgerRepository
    private val exporter = LedgerExporter()

    private val _exportEvent = MutableStateFlow<ExportEvent?>(null)
    val exportEvent: StateFlow<ExportEvent?> = _exportEvent

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observeSettings(),
        repository.observeAccounts(),
        repository.observeNotificationRules(),
    ) { settings, accounts, notificationRules ->
        SettingsUiState(
            settings = settings,
            accounts = accounts,
            notificationRules = notificationRules,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            val snapshot = repository.exportSnapshot()
            _exportEvent.value = ExportEvent(
                fileName = "iflow-${snapshot.exportedAt.formatExportFileTime()}.json",
                mimeType = "application/json",
                content = exporter.toJson(snapshot),
            )
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val snapshot = repository.exportSnapshot()
            _exportEvent.value = ExportEvent(
                fileName = "iflow-${snapshot.exportedAt.formatExportFileTime()}.csv",
                mimeType = "text/csv",
                content = exporter.toCsv(snapshot),
            )
        }
    }

    fun consumeExportEvent() {
        _exportEvent.value = null
    }

    fun setAutoCaptureEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoCaptureEnabled(enabled)
        }
    }

    fun setAutoConfirmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoConfirmEnabled(enabled)
        }
    }

    fun setDefaultAccount(accountId: Long) {
        viewModelScope.launch {
            repository.setDefaultAccount(accountId)
        }
    }

    fun setNotificationRuleEnabled(ruleId: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationRuleEnabled(ruleId, enabled)
        }
    }
}

data class ExportEvent(
    val fileName: String,
    val mimeType: String,
    val content: String,
)

data class SettingsUiState(
    val settings: AppSettingEntity? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val notificationRules: List<NotificationRuleEntity> = emptyList(),
)
