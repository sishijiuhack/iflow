package com.sishijiuhack.iflow.ui.navigation

import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class IFlowDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home("home", "首页", Icons.Outlined.Home),
    Ledger("ledger", "流水", Icons.AutoMirrored.Outlined.List),
    Stats("stats", "统计", Icons.AutoMirrored.Outlined.ShowChart),
    Settings("settings", "设置", Icons.Outlined.Settings),
}
