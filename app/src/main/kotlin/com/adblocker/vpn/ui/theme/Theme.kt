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

import androidx.compose.ui.text.font.Font
import com.adblocker.vpn.R

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold)
)

// --- NovaBlue Theme (NovaMind AI Style) ---
val GlassPrimary = Color(0xFF00D4FF) // Glowing Cyan
val GlassSecondary = Color(0xFF7C5CFF) // Deep Neon Purple
val GlassBackground = Color(0xFF07070A) // Very Dark Tech Blue/Black
val GlassSurface = Color(0xFF0C0C14) 
val GlassSurfaceVariant = Color(0xFF13131F)
val GlassText = Color(0xFFFFFFFF)
val GlassTextSecondary = Color(0xFF8A93A6)
val GlassBorder = Color(0x3300D4FF)

val GlassScheme = darkColorScheme(
    primary = GlassPrimary,
    onPrimary = Color.Black,
    secondary = GlassSecondary,
    onSecondary = Color.White,
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
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        letterSpacing = (-0.1).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)

// --- Cyberpunk Theme ---
val CyberpunkPrimary = Color(0xFFFF007F) // Neon Pink
val CyberpunkSecondary = Color(0xFF00F0FF) // Cyan
val CyberpunkBackground = Color(0xFF0D0221) 
val CyberpunkSurface = Color(0xFF140333) 
val CyberpunkSurfaceVariant = Color(0xFF1E054C)
val CyberpunkText = Color(0xFFFFF0F5)
val CyberpunkTextSecondary = Color(0xFF9A8C98)
val CyberpunkBorder = Color(0x33FF007F)

val CyberpunkScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    onPrimary = Color.Black,
    secondary = CyberpunkSecondary,
    onSecondary = Color.Black,
    background = CyberpunkBackground,
    surface = CyberpunkSurface,
    surfaceVariant = CyberpunkSurfaceVariant,
    onBackground = CyberpunkText,
    onSurface = CyberpunkText,
    onSurfaceVariant = CyberpunkTextSecondary,
    outline = CyberpunkBorder
)

// --- Luxury Theme ---
val LuxuryPrimary = Color(0xFFFFD700) // Gold
val LuxurySecondary = Color(0xFFC0C0C0) // Silver
val LuxuryBackground = Color(0xFF121212) 
val LuxurySurface = Color(0xFF1C1C1C) 
val LuxurySurfaceVariant = Color(0xFF282828)
val LuxuryText = Color(0xFFFAFAD2)
val LuxuryTextSecondary = Color(0xFFA9A9A9)
val LuxuryBorder = Color(0x33FFD700)

val LuxuryScheme = darkColorScheme(
    primary = LuxuryPrimary,
    onPrimary = Color.Black,
    secondary = LuxurySecondary,
    onSecondary = Color.Black,
    background = LuxuryBackground,
    surface = LuxurySurface,
    surfaceVariant = LuxurySurfaceVariant,
    onBackground = LuxuryText,
    onSurface = LuxuryText,
    onSurfaceVariant = LuxuryTextSecondary,
    outline = LuxuryBorder
)

// --- Amethyst Theme ---
val AmethystPrimary = Color(0xFF9966CC) // Amethyst Purple
val AmethystSecondary = Color(0xFFFF69B4) // Hot Pink
val AmethystBackground = Color(0xFF0F0A1A) 
val AmethystSurface = Color(0xFF181028) 
val AmethystSurfaceVariant = Color(0xFF221538)
val AmethystText = Color(0xFFF3E8FF)
val AmethystTextSecondary = Color(0xFFA197B0)
val AmethystBorder = Color(0x339966CC)

val AmethystScheme = darkColorScheme(
    primary = AmethystPrimary,
    onPrimary = Color.White,
    secondary = AmethystSecondary,
    onSecondary = Color.Black,
    background = AmethystBackground,
    surface = AmethystSurface,
    surfaceVariant = AmethystSurfaceVariant,
    onBackground = AmethystText,
    onSurface = AmethystText,
    onSurfaceVariant = AmethystTextSecondary,
    outline = AmethystBorder
)

// --- Monochrome Theme ---
val MonochromePrimary = Color(0xFFFFFFFF) // Pure White
val MonochromeSecondary = Color(0xFFCCCCCC) // Light Grey
val MonochromeBackground = Color(0xFF000000) 
val MonochromeSurface = Color(0xFF111111) 
val MonochromeSurfaceVariant = Color(0xFF222222)
val MonochromeText = Color(0xFFFFFFFF)
val MonochromeTextSecondary = Color(0xFF888888)
val MonochromeBorder = Color(0x33FFFFFF)

val MonochromeScheme = darkColorScheme(
    primary = MonochromePrimary,
    onPrimary = Color.Black,
    secondary = MonochromeSecondary,
    onSecondary = Color.Black,
    background = MonochromeBackground,
    surface = MonochromeSurface,
    surfaceVariant = MonochromeSurfaceVariant,
    onBackground = MonochromeText,
    onSurface = MonochromeText,
    onSurfaceVariant = MonochromeTextSecondary,
    outline = MonochromeBorder
)

// --- Ocean Theme ---
val OceanPrimary = Color(0xFF00B4D8)
val OceanSecondary = Color(0xFF03045E)
val OceanBackground = Color(0xFF020E1A)
val OceanSurface = Color(0xFF05192D)
val OceanSurfaceVariant = Color(0xFF0B2A4A)
val OceanText = Color(0xFFE0F7FA)
val OceanTextSecondary = Color(0xFF81D4FA)
val OceanBorder = Color(0x3300B4D8)

val OceanScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.Black,
    secondary = OceanSecondary,
    onSecondary = Color.White,
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = OceanSurfaceVariant,
    onBackground = OceanText,
    onSurface = OceanText,
    onSurfaceVariant = OceanTextSecondary,
    outline = OceanBorder
)

// --- Forest Theme ---
val ForestPrimary = Color(0xFF76C893)
val ForestSecondary = Color(0xFF1A759F)
val ForestBackground = Color(0xFF061409)
val ForestSurface = Color(0xFF0D2513)
val ForestSurfaceVariant = Color(0xFF153A1F)
val ForestText = Color(0xFFD8F3DC)
val ForestTextSecondary = Color(0xFF95D5B2)
val ForestBorder = Color(0x3376C893)

val ForestScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.Black,
    secondary = ForestSecondary,
    onSecondary = Color.White,
    background = ForestBackground,
    surface = ForestSurface,
    surfaceVariant = ForestSurfaceVariant,
    onBackground = ForestText,
    onSurface = ForestText,
    onSurfaceVariant = ForestTextSecondary,
    outline = ForestBorder
)

// --- Sunset Theme ---
val SunsetPrimary = Color(0xFFFF5400)
val SunsetSecondary = Color(0xFFFF0054)
val SunsetBackground = Color(0xFF1A0500)
val SunsetSurface = Color(0xFF2E0900)
val SunsetSurfaceVariant = Color(0xFF4A1000)
val SunsetText = Color(0xFFFFE5D9)
val SunsetTextSecondary = Color(0xFFFFCAD4)
val SunsetBorder = Color(0x33FF5400)

val SunsetScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.Black,
    secondary = SunsetSecondary,
    onSecondary = Color.White,
    background = SunsetBackground,
    surface = SunsetSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onBackground = SunsetText,
    onSurface = SunsetText,
    onSurfaceVariant = SunsetTextSecondary,
    outline = SunsetBorder
)

@Composable
fun AdBlockerTheme(
    themeName: String = "glass", 
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName.lowercase()) {
        "aurora" -> AuroraScheme
        "eclipse" -> EclipseScheme
        "cyberpunk" -> CyberpunkScheme
        "luxury" -> LuxuryScheme
        "amethyst" -> AmethystScheme
        "monochrome" -> MonochromeScheme
        "ocean" -> OceanScheme
        "forest" -> ForestScheme
        "sunset" -> SunsetScheme
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

