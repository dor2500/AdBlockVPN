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

// Core Colors
val Blue500 = Color(0xFF3B82F6)
val Emerald500 = Color(0xFF10B981)
val Red500 = Color(0xFFEF4444)

// Light Theme Colors
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)

// Slate Theme Colors
val SlateBackground = Color(0xFF0F172A)
val SlateSurface = Color(0xFF1E293B)

// Midnight Theme Colors
val MidnightBackground = Color(0xFF000000)
val MidnightSurface = Color(0xFF111111)

// Legacy aliases for compilation compatibility in other files
val NeonCyan = Blue500
val NeonGreen = Emerald500
val CyberRed = Red500

val LightScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    secondary = Emerald500,
    onSecondary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B)
)

val SlateScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    secondary = Emerald500,
    onSecondary = Color.White,
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8)
)

val MidnightScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    secondary = Emerald500,
    onSecondary = Color.White,
    background = MidnightBackground,
    surface = MidnightSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF6B7280)
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
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

@Composable
fun AdBlockerTheme(
    themeName: String = "system",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = when (themeName) {
        "light" -> LightScheme
        "slate" -> SlateScheme
        "midnight" -> MidnightScheme
        else -> if (isSystemDark) SlateScheme else LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModernTypography,
        content = content
    )
}

// Keep this to not break existing modifiers, but point it to standard background
@Composable
fun Modifier.cyberBackground(): Modifier {
    return this.background(MaterialTheme.colorScheme.background)
}
