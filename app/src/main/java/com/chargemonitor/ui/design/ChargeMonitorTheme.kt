package com.chargemonitor.ui.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val colors = darkColorScheme(
    primary = Lime,
    onPrimary = Slate,
    background = Slate,
    onBackground = Ink,
    surface = SlateSurface,
    onSurface = Ink,
    onSurfaceVariant = Muted,
    outline = Divider,
)

@Composable
fun ChargeMonitorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
