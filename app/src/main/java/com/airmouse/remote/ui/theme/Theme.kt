package com.airmouse.remote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentCyanDim,
    background = BgDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceLightDark,
    onPrimary = BgDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun AirMouseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
