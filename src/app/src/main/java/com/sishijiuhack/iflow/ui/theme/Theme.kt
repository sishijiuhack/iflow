package com.sishijiuhack.iflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = IFlowPrimary,
    secondary = IFlowSecondary,
    tertiary = IFlowTertiary,
    background = IFlowBackground,
    surface = IFlowSurface,
)

@Composable
fun IFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
