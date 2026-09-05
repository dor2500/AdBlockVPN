package com.adblocker.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background

// Core Cyber Palette
val DeepSpaceBlue = Color(0xFF060B19)
val CyberPurple = Color(0xFF1E1442)
val CyberCyan = Color(0xFF00E5FF)
val CyberGreen = Color(0xFF00FF87)
val CyberRed = Color(0xFFFF2A55)

// Legacy colors to maintain compilation
val NeonCyan = CyberCyan
val NeonGreen = CyberGreen
val DarkBackground = DeepSpaceBlue

val SurfaceDark = Color(0x05FFFFFF) // Barely visible glass
val SurfaceVariantDark = Color(0x15FFFFFF) // Glassmorphism base

val CyberBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        DeepSpaceBlue,
        Color(0xFF0A1128),
        Color(0xFF0D0B24),
        DeepSpaceBlue
    )
)

fun Modifier.cyberBackground(): Modifier = this.background(CyberBackgroundGradient)

private val PremiumDarkScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    secondary = CyberGreen,
    onSecondary = Color.Black,
    background = Color.Transparent, // We use the gradient modifier instead
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFA0A5B5)
)

val ModernTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 28.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun AdBlockerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkScheme,
        typography = ModernTypography,
        content = content
    )
}
