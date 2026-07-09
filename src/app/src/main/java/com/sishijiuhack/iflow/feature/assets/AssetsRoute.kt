package com.sishijiuhack.iflow.feature.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sishijiuhack.iflow.core.model.MoneyCents
import com.sishijiuhack.iflow.data.repository.AccountBalanceItem
import com.sishijiuhack.iflow.domain.model.AccountType

@Composable
fun AssetsRoute(
    onOpenSettings: () -> Unit,
    viewModel: AssetsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hiddenAmounts by remember { mutableStateOf(false) }
    val groupedAccounts = uiState.accounts.groupBy { it.accountType.groupTitle() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AssetsHeader(
            onOpenSettings = onOpenSettings,
            onToggleHidden = { hiddenAmounts = !hiddenAmounts },
            hiddenAmounts = hiddenAmounts,
        )
        NetAssetCard(
            netAssetsCents = uiState.netAssetsCents,
            totalAssetsCents = uiState.totalAssetsCents,
            totalLiabilitiesCents = uiState.totalLiabilitiesCents,
            hiddenAmounts = hiddenAmounts,
        )
        LoanSummaryRow(
            totalBorrowedCents = uiState.totalLiabilitiesCents,
            totalLentCents = 0L,
            hiddenAmounts = hiddenAmounts,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "账户",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            RoundIconButton(
                icon = Icons.Outlined.Add,
                contentDescription = "添加账户",
                onClick = onOpenSettings,
            )
        }
        if (uiState.accounts.isEmpty()) {
            EmptyAccountState()
        } else {
            listOf("资金账户", "其他账户").forEach { groupTitle ->
                val accounts = groupedAccounts[groupTitle].orEmpty()
                if (accounts.isNotEmpty()) {
                    AccountGroup(
                        title = groupTitle,
                        accounts = accounts,
                        hiddenAmounts = hiddenAmounts,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetsHeader(
    onOpenSettings: () -> Unit,
    onToggleHidden: () -> Unit,
    hiddenAmounts: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "资产",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "默认账本",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundIconButton(
                icon = if (hiddenAmounts) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = "隐藏金额",
                onClick = onToggleHidden,
            )
            RoundIconButton(
                icon = Icons.Outlined.MoreHoriz,
                contentDescription = "资产设置",
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun NetAssetCard(
    netAssetsCents: Long,
    totalAssetsCents: Long,
    totalLiabilitiesCents: Long,
    hiddenAmounts: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "净资产",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatAssetAmount(netAssetsCents, hiddenAmounts),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AssetMetric(
                    label = "总资产",
                    amount = formatAssetAmount(totalAssetsCents, hiddenAmounts),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                AssetMetric(
                    label = "总负债",
                    amount = formatAssetAmount(totalLiabilitiesCents, hiddenAmounts),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LoanSummaryRow(
    totalBorrowedCents: Long,
    totalLentCents: Long,
    hiddenAmounts: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoanMetricCard(
            label = "总借入",
            amount = formatAssetAmount(totalBorrowedCents, hiddenAmounts),
            modifier = Modifier.weight(1f),
        )
        LoanMetricCard(
            label = "总借出",
            amount = formatAssetAmount(totalLentCents, hiddenAmounts),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LoanMetricCard(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AssetMetric(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AccountGroup(
    title: String,
    accounts: List<AccountBalanceItem>,
    hiddenAmounts: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        accounts.forEach { account ->
            AccountBalanceRow(
                account = account,
                hiddenAmounts = hiddenAmounts,
            )
        }
    }
}

@Composable
private fun AccountBalanceRow(
    account: AccountBalanceItem,
    hiddenAmounts: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = Color(0xFFF1F2F4),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = account.accountType.iconText(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = account.accountName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = account.accountType.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = formatAssetAmount(account.balanceCents, hiddenAmounts),
                style = MaterialTheme.typography.titleMedium,
                color = if (account.balanceCents < 0L) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EmptyAccountState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
    ) {
        Text(
            text = "暂无账户",
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun formatAssetAmount(
    cents: Long,
    hidden: Boolean,
): String {
    return if (hidden) "••••" else MoneyCents(cents).format()
}

private fun AccountType.displayName(): String {
    return when (this) {
        AccountType.Cash -> "现金账户"
        AccountType.Bank -> "银行卡"
        AccountType.Wechat -> "微信"
        AccountType.Alipay -> "支付宝"
        AccountType.Other -> "其他账户"
    }
}

private fun AccountType.groupTitle(): String {
    return when (this) {
        AccountType.Cash, AccountType.Bank, AccountType.Wechat, AccountType.Alipay -> "资金账户"
        AccountType.Other -> "其他账户"
    }
}

private fun AccountType.iconText(): String {
    return when (this) {
        AccountType.Cash -> "¥"
        AccountType.Bank -> "💳"
        AccountType.Wechat -> "微"
        AccountType.Alipay -> "支"
        AccountType.Other -> "••"
    }
}
