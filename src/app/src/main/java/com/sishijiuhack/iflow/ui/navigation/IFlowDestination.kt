package com.sishijiuhack.iflow.ui.navigation

import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.ui.graphics.vector.ImageVector

enum class IFlowDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Ledger("ledger", "账本", Icons.AutoMirrored.Outlined.List),
    Assets("assets", "资产", Icons.Outlined.AccountBalanceWallet),
    Stats("stats", "统计", Icons.AutoMirrored.Outlined.ShowChart),
    New("transaction/new", "新建", Icons.Outlined.AddCircle),
}
