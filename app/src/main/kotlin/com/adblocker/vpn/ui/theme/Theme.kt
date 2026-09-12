package com.adblocker.vpn.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// --- Glass Dark Theme (NovaMind AI) ---
val GlassPrimary = Color(0xFF7C5CFF) // Purple
val GlassSecondary = Color(0xFF00D4FF) // Cyan
val GlassBackground = Color(0xFF0A0A0F) 
val GlassSurface = Color(0xFF0E0E16) 
val GlassSurfaceVariant = Color(0xFF12121C)
val GlassText = Color(0xFFE8E6F0)
val GlassTextSecondary = Color(0xFF9896A8)
val GlassBorder = Color(0x1F7C5CFF)

val GlassScheme = darkColorScheme(
    primary = GlassPrimary,
    onPrimary = Color.White,
    secondary = GlassSecondary,
    onSecondary = Color.Black,
    background = GlassBackground,
    surface = GlassSurface,
    surfaceVariant = GlassSurfaceVariant,
    onBackground = GlassText,
    onSurface = GlassText,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassBorder
)

// --- Aurora Theme ---
val AuroraPrimary = Color(0xFF00E676) // Neon Green
val AuroraSecondary = Color(0xFF00B0FF) // Neon Blue
val AuroraBackground = Color(0xFF05100B) 
val AuroraSurface = Color(0xFF0A1811) 
val AuroraSurfaceVariant = Color(0xFF0F2018)
val AuroraText = Color(0xFFE0F2E9)
val AuroraTextSecondary = Color(0xFF8DAA9B)
val AuroraBorder = Color(0x1F00E676)

val AuroraScheme = darkColorScheme(
    primary = AuroraPrimary,
    onPrimary = Color.Black,
    secondary = AuroraSecondary,
    onSecondary = Color.Black,
    background = AuroraBackground,
    surface = AuroraSurface,
    surfaceVariant = AuroraSurfaceVariant,
    onBackground = AuroraText,
    onSurface = AuroraText,
    onSurfaceVariant = AuroraTextSecondary,
    outline = AuroraBorder
)

// --- Eclipse Theme ---
val EclipsePrimary = Color(0xFFFF3D00) // Crimson/Orange
val EclipseSecondary = Color(0xFFFFB300) // Amber
val EclipseBackground = Color(0xFF000000) // Pure Black (OLED)
val EclipseSurface = Color(0xFF080808) 
val EclipseSurfaceVariant = Color(0xFF101010)
val EclipseText = Color(0xFFFFF0ED)
val EclipseTextSecondary = Color(0xFF998A87)
val EclipseBorder = Color(0x1FFFF3D00)

val EclipseScheme = darkColorScheme(
    primary = EclipsePrimary,
    onPrimary = Color.White,
    secondary = EclipseSecondary,
    onSecondary = Color.Black,
    background = EclipseBackground,
    surface = EclipseSurface,
    surfaceVariant = EclipseSurfaceVariant,
    onBackground = EclipseText,
    onSurface = EclipseText,
    onSurfaceVariant = EclipseTextSecondary,
    outline = EclipseBorder
)

// Professional Typography
val ProfessionalTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        letterSpacing = (-0.1).sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun AdBlockerTheme(
    themeName: String = "glass", 
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName.lowercase()) {
        "aurora" -> AuroraScheme
        "eclipse" -> EclipseScheme
        "glass" -> GlassScheme
        else -> GlassScheme // default to Glass (NovaMind)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ProfessionalTypography,
        content = content
    )
}

@Composable
fun Modifier.cyberBackground(): Modifier {
    return this.background(MaterialTheme.colorScheme.background)
}

