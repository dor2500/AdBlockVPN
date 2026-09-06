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

// Premium Core Colors
val Blue500 = Color(0xFF0057FF)
val Emerald500 = Color(0xFF00FF87)
val Red500 = Color(0xFFFF0055)

val PremiumCyan = Color(0xFF00F0FF)
val PremiumPurple = Color(0xFF8A2BE2)

// Light Theme Colors
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)

// Slate Theme Colors
val SlateBackground = Color(0xFF0F172A)
val SlateSurface = Color(0xFF1E293B)

// Midnight Theme Colors
val MidnightBackground = Color(0xFF0A0E17)
val MidnightSurface = Color(0xFF131A2A)

// Glassmorphism 
val GlassBackground = Color.White.copy(alpha = 0.03f)
val GlassBorder = Color.White.copy(alpha = 0.1f)

// Premium Gradients
val PrimaryGradient = Brush.linearGradient(listOf(PremiumCyan, Blue500))
val SuccessGradient = Brush.linearGradient(listOf(Emerald500, Color(0xFF00B359)))
val DangerGradient = Brush.linearGradient(listOf(Red500, Color(0xFFCC0044)))

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
    onSurfaceVariant = Color(0xFF8B949E)
)

val ModernTypography = Typography(
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        letterSpacing = 1.2.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp
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
        fontSize = 13.sp
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
