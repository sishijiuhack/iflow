package com.sishijiuhack.iflow.feature.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val permissionEnabled = remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    val exportEvent by viewModel.exportEvent.collectAsStateWithLifecycle()

    LaunchedEffect(exportEvent) {
        val event = exportEvent ?: return@LaunchedEffect
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = event.mimeType
            putExtra(Intent.EXTRA_TITLE, event.fileName)
            putExtra(Intent.EXTRA_SUBJECT, event.fileName)
            putExtra(Intent.EXTRA_TEXT, event.content)
        }
        context.startActivity(Intent.createChooser(sendIntent, "导出账本"))
        viewModel.consumeExportEvent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        Text("本地导出", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::exportJson) {
                Text("导出 JSON")
            }
            Button(onClick = viewModel::exportCsv) {
                Text("导出 CSV")
            }
        }
        Text("隐私说明", style = MaterialTheme.typography.titleMedium)
        Text("账本、通知解析结果和设置默认仅保存在本机。本应用不上传服务器，不接入第三方统计 SDK，不采集联系人、定位或相册。")
    }
}
