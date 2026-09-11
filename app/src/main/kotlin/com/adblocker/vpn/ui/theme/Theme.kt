package com.adblocker.vpn.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// --- Apple Minimal Theme ---
val AppleSurface = Color(0xFFFFFFFF)
val AppleBackground = Color(0xFFF2F2F7)
val ApplePrimary = Color(0xFF007AFF)
val AppleText = Color(0xFF000000)
val AppleTextSecondary = Color(0xFF8E8E93)
val AppleCard = Color(0xFFFFFFFF)

val AppleMinimalScheme = lightColorScheme(
    primary = ApplePrimary,
    onPrimary = Color.White,
    secondary = ApplePrimary,
    onSecondary = Color.White,
    background = AppleBackground,
    surface = AppleSurface,
    onBackground = AppleText,
    onSurface = AppleText,
    onSurfaceVariant = AppleTextSecondary
)

// --- Neo Brutalism Theme ---
val BrutalBackground = Color(0xFFFFF9E6) // Warm yellowish white
val BrutalSurface = Color(0xFFFFFFFF)
val BrutalPrimary = Color(0xFFFF5E5B) // Punchy Red
val BrutalSecondary = Color(0xFF00E5FF) // Cyan
val BrutalText = Color(0xFF000000)

val NeoBrutalismScheme = lightColorScheme(
    primary = BrutalPrimary,
    onPrimary = Color.White,
    secondary = BrutalSecondary,
    onSecondary = Color.Black,
    background = BrutalBackground,
    surface = BrutalSurface,
    onBackground = BrutalText,
    onSurface = BrutalText,
    onSurfaceVariant = Color.Black
)

// --- Cyberpunk Theme ---
val CyberBackground = Color(0xFF0F0F1A) // Deep space black
val CyberSurface = Color(0xFF1B1B2F) // Dark blue-grey
val CyberPrimary = Color(0xFFE94560) // Neon pink/red
val CyberSecondary = Color(0xFF00FFCC) // Neon cyan
val CyberText = Color(0xFFFFFFFF)
val CyberTextSecondary = Color(0xFFA0A0B5)

val CyberpunkScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color.Black,
    secondary = CyberSecondary,
    onSecondary = Color.Black,
    background = CyberBackground,
    surface = CyberSurface,
    onBackground = CyberText,
    onSurface = CyberText,
    onSurfaceVariant = CyberTextSecondary
)

// Standard Typography
val ModernTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 36.sp,
        letterSpacing = (-1).sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        letterSpacing = (-0.2).sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)

// Legacy variables to not break compilation in untouched files while transitioning
val PremiumCyan = BrutalSecondary
val NeonGreen = Color(0xFF00FF87)
val GlassBackground = Color.Transparent
val GlassBorder = Color.Transparent
val Blue500 = ApplePrimary

@Composable
fun AdBlockerTheme(
    themeName: String = "cyberpunk", // Default to Cyberpunk
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "apple" -> AppleMinimalScheme
        "brutal" -> NeoBrutalismScheme
        "cyberpunk" -> CyberpunkScheme
        else -> CyberpunkScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModernTypography,
        content = content
    )
}

@Composable
fun Modifier.cyberBackground(): Modifier {
    return this.background(MaterialTheme.colorScheme.background)
}
