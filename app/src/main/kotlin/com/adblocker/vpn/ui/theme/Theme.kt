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

// --- NovaMind AI Theme ---
val NovaBackground = Color(0xFF0A0A0F) 
val NovaSurface = Color(0xFF0E0E16) 
val NovaSurfaceVariant = Color(0xFF12121C)
val NovaPrimary = Color(0xFF7C5CFF) // Purple
val NovaSecondary = Color(0xFF00D4FF) // Cyan
val NovaText = Color(0xFFE8E6F0)
val NovaTextSecondary = Color(0xFF9896A8)
val NovaBorder = Color(0x1F7C5CFF) // rgba(124, 92, 255, 0.12)

val NovaScheme = darkColorScheme(
    primary = NovaPrimary,
    onPrimary = Color.White,
    secondary = NovaSecondary,
    onSecondary = Color.Black,
    background = NovaBackground,
    surface = NovaSurface,
    surfaceVariant = NovaSurfaceVariant,
    onBackground = NovaText,
    onSurface = NovaText,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaBorder
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

// Legacy compatibility
val PremiumCyan = NovaSecondary
val NeonGreen = NovaSecondary
val Blue500 = NovaPrimary

@Composable
fun AdBlockerTheme(
    themeName: String = "novamind", 
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaScheme,
        typography = ProfessionalTypography,
        content = content
    )
}

@Composable
fun Modifier.cyberBackground(): Modifier {
    return this.background(MaterialTheme.colorScheme.background)
}

