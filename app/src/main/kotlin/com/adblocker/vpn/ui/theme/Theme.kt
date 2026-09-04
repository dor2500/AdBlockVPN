package com.adblocker.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonGreen = Color(0xFF00E676)
val DarkBackground = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF141414)
val SurfaceVariantDark = Color(0xFF1E1E1E)

private val PremiumDarkScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    secondary = Color(0xFF2196F3),
    background = DarkBackground,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.LightGray
)

@Composable
fun AdBlockerTheme(
    // We ignore dynamic parameters to force a premium look
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkScheme,
        content = content
    )
}
