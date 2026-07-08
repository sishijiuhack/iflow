package com.sishijiuhack.iflow.feature.settings

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionEnabled = remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    val pendingExport = remember { mutableStateOf<ExportEvent?>(null) }
    val exportEvent by viewModel.exportEvent.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveExport: (android.net.Uri?) -> Unit = { uri ->
        val event = pendingExport.value
        if (uri != null && event != null) {
            runCatching {
                checkNotNull(context.contentResolver.openOutputStream(uri)) {
                    "Cannot open export output stream."
                }.use { output ->
                    output.write(event.content.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                Toast.makeText(context, "导出完成", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
            }
        }
        pendingExport.value = null
        viewModel.consumeExportEvent()
    }
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = saveExport,
    )
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = saveExport,
    )

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionEnabled.value = isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(exportEvent) {
        val event = exportEvent ?: return@LaunchedEffect
        pendingExport.value = event
        when (event.mimeType) {
            "application/json" -> jsonExportLauncher.launch(event.fileName)
            "text/csv" -> csvExportLauncher.launch(event.fileName)
            else -> {
                pendingExport.value = null
                viewModel.consumeExportEvent()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("通知读取：${if (permissionEnabled.value) "已开启" else "未开启"}")
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
        ) {
            Text("前往系统设置开启")
        }
        Text("自动记账依赖通知使用权。未开启时，手动记账仍可完整使用。")
        Text("HyperOS 入口可能随版本变化，请以系统设置页面为准。")
        Text("自动记账", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("捕获支付通知")
            Switch(
                checked = uiState.settings?.autoCaptureEnabled ?: true,
                onCheckedChange = viewModel::setAutoCaptureEnabled,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("自动确认低风险记录")
            Switch(
                checked = uiState.settings?.autoConfirmEnabled ?: false,
                onCheckedChange = viewModel::setAutoConfirmEnabled,
            )
        }
        Text("默认策略是先进入待确认；开启自动确认后，解析成功的通知会直接进入正式流水。")
        Text("默认账户", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            uiState.accounts.forEach { account ->
                FilterChip(
                    selected = uiState.settings?.defaultAccountId == account.id,
                    onClick = { viewModel.setDefaultAccount(account.id) },
                    label = { Text(account.name) },
                )
            }
        }
        Text("本地导出", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::exportJson) {
                Text("导出 JSON")
            }
            Button(onClick = viewModel::exportCsv) {
                Text("导出 CSV")
            }
        }
        Text("通知规则", style = MaterialTheme.typography.titleMedium)
        if (uiState.notificationRules.isEmpty()) {
            Text("暂无通知规则")
        } else {
            uiState.notificationRules.forEach { rule ->
                Text("${rule.appName} · ${if (rule.enabled) "已启用" else "已停用"}")
            }
        }
        Text("隐私说明", style = MaterialTheme.typography.titleMedium)
        Text("账本、通知解析结果和设置默认仅保存在本机。本应用不上传服务器，不接入第三方统计 SDK，不采集联系人、定位或相册。")
    }
}
