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

// --- Executive Professional Theme ---
val ExecutiveBackground = Color(0xFF121212) 
val ExecutiveSurface = Color(0xFF1E1E1E) 
val ExecutiveSurfaceVariant = Color(0xFF2C2C2C)
val ExecutivePrimary = Color(0xFF0A84FF) // Professional iOS-like Blue
val ExecutiveSecondary = Color(0xFF30D158) // Success Green
val ExecutiveText = Color(0xFFFFFFFF)
val ExecutiveTextSecondary = Color(0xFF8E8E93)
val ExecutiveBorder = Color(0xFF38383A)

val ExecutiveScheme = darkColorScheme(
    primary = ExecutivePrimary,
    onPrimary = Color.White,
    secondary = ExecutiveSecondary,
    onSecondary = Color.White,
    background = ExecutiveBackground,
    surface = ExecutiveSurface,
    surfaceVariant = ExecutiveSurfaceVariant,
    onBackground = ExecutiveText,
    onSurface = ExecutiveText,
    onSurfaceVariant = ExecutiveTextSecondary,
    outline = ExecutiveBorder
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
val PremiumCyan = ExecutivePrimary
val NeonGreen = ExecutiveSecondary
val Blue500 = ExecutivePrimary

@Composable
fun AdBlockerTheme(
    themeName: String = "executive", 
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ExecutiveScheme,
        typography = ProfessionalTypography,
        content = content
    )
}

@Composable
fun Modifier.cyberBackground(): Modifier {
    return this.background(MaterialTheme.colorScheme.background)
}

