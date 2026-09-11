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

// --- 2026 Glass Dark Theme (Default) ---
val GlassBackground = Color(0xFF050508) // Absolute black base
val GlassSurface = Color(0xFF14141E) // Semi-transparent look
val GlassPrimary = Color(0xFF8A2BE2) // Deep Purple / Neon Blue accent
val GlassSecondary = Color(0xFF00D4FF) // Cyan accent
val GlassText = Color(0xFFFFFFFF)
val GlassTextSecondary = Color(0xFF9E9EA7)

val GlassDarkScheme = darkColorScheme(
    primary = GlassPrimary,
    onPrimary = Color.White,
    secondary = GlassSecondary,
    onSecondary = Color.Black,
    background = GlassBackground,
    surface = GlassSurface,
    onBackground = GlassText,
    onSurface = GlassText,
    onSurfaceVariant = GlassTextSecondary
)

// --- Aurora Borealis Theme ---
val AuroraBackground = Color(0xFF020907)
val AuroraSurface = Color(0xFF081813)
val AuroraPrimary = Color(0xFF00FF87) // Neon green
val AuroraSecondary = Color(0xFF00E5FF) // Cyan
val AuroraText = Color(0xFFFFFFFF)

val AuroraScheme = darkColorScheme(
    primary = AuroraPrimary,
    onPrimary = Color.Black,
    secondary = AuroraSecondary,
    onSecondary = Color.Black,
    background = AuroraBackground,
    surface = AuroraSurface,
    onBackground = AuroraText,
    onSurface = AuroraText,
    onSurfaceVariant = Color(0xFFA0C0B5)
)

// --- Eclipse Minimal Theme ---
val EclipseBackground = Color(0xFF000000)
val EclipseSurface = Color(0xFF111111)
val EclipsePrimary = Color(0xFFFFFFFF) // Pure white accents
val EclipseSecondary = Color(0xFF888888)
val EclipseText = Color(0xFFFFFFFF)

val EclipseScheme = darkColorScheme(
    primary = EclipsePrimary,
    onPrimary = Color.Black,
    secondary = EclipseSecondary,
    onSecondary = Color.White,
    background = EclipseBackground,
    surface = EclipseSurface,
    onBackground = EclipseText,
    onSurface = EclipseText,
    onSurfaceVariant = Color(0xFF666666)
)

// 2026 Advanced Typography
val ModernTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        letterSpacing = (-2).sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = (-0.3).sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.sp
    )
)

// Legacy compatibility
val PremiumCyan = GlassSecondary
val NeonGreen = AuroraPrimary
val Blue500 = GlassPrimary

@Composable
fun AdBlockerTheme(
    themeName: String = "glass", // Default to Glass Dark
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "glass" -> GlassDarkScheme
        "aurora" -> AuroraScheme
        "eclipse" -> EclipseScheme
        else -> GlassDarkScheme
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
