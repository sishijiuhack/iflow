package com.sishijiuhack.iflow.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sishijiuhack.iflow.feature.assets.AssetsRoute
import com.sishijiuhack.iflow.feature.home.HomeRoute
import com.sishijiuhack.iflow.feature.ledger.LedgerRoute
import com.sishijiuhack.iflow.feature.pending.PendingRoute
import com.sishijiuhack.iflow.feature.settings.SettingsRoute
import com.sishijiuhack.iflow.feature.stats.StatsRoute
import com.sishijiuhack.iflow.feature.transaction.TransactionFormRoute
import com.sishijiuhack.iflow.ui.navigation.IFlowDestination

@Composable
fun IFlowApp() {
    val navController = rememberNavController()
    val destinations = IFlowDestination.entries
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: IFlowDestination.Ledger.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    val selected = when (destination) {
                        IFlowDestination.New -> currentRoute.startsWith("transaction/")
                        else -> currentRoute == destination.route
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = destination != IFlowDestination.New
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = destination != IFlowDestination.New
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = IFlowDestination.Ledger.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeRoute(
                    onAddTransaction = { navController.navigate("transaction/new") },
                    onOpenPending = { navController.navigate("pending") },
                )
            }
            composable(IFlowDestination.Ledger.route) {
                LedgerRoute(
                    onAddTransaction = { navController.navigate("transaction/new") },
                    onEditTransaction = { id -> navController.navigate("transaction/$id") },
                )
            }
            composable(IFlowDestination.Assets.route) {
                AssetsRoute(
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable(IFlowDestination.Stats.route) { StatsRoute() }
            composable("settings") { SettingsRoute() }
            composable("pending") {
                PendingRoute(
                    onEditTransaction = { id -> navController.navigate("transaction/$id") },
                    onBack = { navController.popBackStack() },
                )
            }
            composable("transaction/new") {
                TransactionFormRoute(
                    onClose = { navController.popBackStack() },
                )
            }
            composable("transaction/{transactionId}") {
                TransactionFormRoute(
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}
