package com.sishijiuhack.iflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.sishijiuhack.iflow.feature.transaction.TransactionFormViewModel
import com.sishijiuhack.iflow.ui.navigation.IFlowDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IFlowApp() {
    val navController = rememberNavController()
    val destinations = listOf(
        IFlowDestination.Ledger,
        IFlowDestination.Assets,
        IFlowDestination.Stats,
    )
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: IFlowDestination.Ledger.route
    var showNewTransactionSheet by remember { mutableStateOf(false) }
    var newTransactionSheetKey by remember { mutableIntStateOf(0) }
    val newTransactionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        bottomBar = {
            IFlowBottomBar(
                destinations = destinations,
                currentRoute = currentRoute,
                onDestinationClick = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                },
                onNewTransactionClick = {
                    newTransactionSheetKey += 1
                    showNewTransactionSheet = true
                }
            )
        },
    ) { innerPadding ->
        if (showNewTransactionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNewTransactionSheet = false },
                sheetState = newTransactionSheetState,
                modifier = Modifier.fillMaxHeight(0.88f),
            ) {
                key(newTransactionSheetKey) {
                    TransactionFormRoute(
                        onClose = { showNewTransactionSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        viewModel = viewModel<TransactionFormViewModel>(
                            key = "transaction-new-sheet-$newTransactionSheetKey",
                        ),
                    )
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = IFlowDestination.Ledger.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeRoute(
                    onAddTransaction = {
                        newTransactionSheetKey += 1
                        showNewTransactionSheet = true
                    },
                    onOpenPending = { navController.navigate("pending") },
                )
            }
            composable(IFlowDestination.Ledger.route) {
                LedgerRoute(
                    onAddTransaction = {
                        newTransactionSheetKey += 1
                        showNewTransactionSheet = true
                    },
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

@Composable
private fun IFlowBottomBar(
    destinations: List<IFlowDestination>,
    currentRoute: String,
    onDestinationClick: (IFlowDestination) -> Unit,
    onNewTransactionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(31.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .weight(1f)
                .height(62.dp),
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = currentRoute == destination.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(
                                color = if (selected) Color(0xFFF0F0F2) else Color.Transparent,
                                shape = RoundedCornerShape(25.dp),
                            )
                            .clickable { onDestinationClick(destination) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = destination.label,
                            color = tint,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = CircleShape,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(60.dp)
                .clickable(onClick = onNewTransactionClick),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "新建",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}
