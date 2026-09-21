package com.chand.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color tokens for Apple HIG design in Chand App, supporting Light & Dark Modes.
 */
data class AppleColors(
    val background: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val cardBorderSubtle: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val headerTitle: Color,
    val green: Color = Color(0xFF34C759),
    val greenBg: Color,
    val red: Color = Color(0xFFFF3B30),
    val redBg: Color,
    val blue: Color = Color(0xFF007AFF),
    val orange: Color = Color(0xFFFF9500),
    val yellow: Color = Color(0xFFFFCC00),
    val purple: Color = Color(0xFFAF52DE),
    val segmentBg: Color,
    val searchBg: Color,
    val isDark: Boolean
)

val AppleLightPalette = AppleColors(
    background = Color(0xFFF2F2F7),
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE5E5EA),
    cardBorderSubtle = Color(0xFFEFEFF4),
    divider = Color(0xFFE5E5EA),
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF8E8E93),
    textTertiary = Color(0xFFAEAEB2),
    headerTitle = Color(0xFF1A2536),
    green = Color(0xFF34C759),
    greenBg = Color(0xFFE8F8EE),
    red = Color(0xFFFF3B30),
    redBg = Color(0xFFFFECEB),
    blue = Color(0xFF007AFF),
    orange = Color(0xFFFF9500),
    yellow = Color(0xFFFFCC00),
    purple = Color(0xFFAF52DE),
    segmentBg = Color(0xFFE3E3E8),
    searchBg = Color(0xFFE3E3E8),
    isDark = false
)

val AppleDarkPalette = AppleColors(
    background = Color(0xFF000000), // iOS Pure OLED Black system background
    cardBackground = Color(0xFF1C1C1E), // Apple Dark elevated card surface
    cardBorder = Color(0xFF2C2C2E), // Apple Dark separator
    cardBorderSubtle = Color(0xFF242426),
    divider = Color(0xFF2C2C2E),
    textPrimary = Color(0xFFFFFFFF), // Pure white text
    textSecondary = Color(0xFF8E8E93), // iOS secondary gray
    textTertiary = Color(0xFF636366),
    headerTitle = Color(0xFFFFFFFF),
    green = Color(0xFF34C759),
    greenBg = Color(0xFF133820),
    red = Color(0xFFFF3B30),
    redBg = Color(0xFF381514),
    blue = Color(0xFF0A84FF), // iOS system dark blue
    orange = Color(0xFFFF9F0A),
    yellow = Color(0xFFFFD60A),
    purple = Color(0xFFBF5AF2),
    segmentBg = Color(0xFF2C2C2E),
    searchBg = Color(0xFF1C1C1E),
    isDark = true
)

val LocalAppleColors = staticCompositionLocalOf { AppleLightPalette }

// Dynamic Apple iOS Human Interface Colors (Reactive to Light/Dark Mode)
val AppleBackground: Color @Composable get() = LocalAppleColors.current.background
val AppleCardBackground: Color @Composable get() = LocalAppleColors.current.cardBackground
val AppleCardBorder: Color @Composable get() = LocalAppleColors.current.cardBorder
val AppleCardBorderSubtle: Color @Composable get() = LocalAppleColors.current.cardBorderSubtle
val AppleDivider: Color @Composable get() = LocalAppleColors.current.divider

// Apple iOS Typography & Labels
val AppleTextPrimary: Color @Composable get() = LocalAppleColors.current.textPrimary
val AppleTextSecondary: Color @Composable get() = LocalAppleColors.current.textSecondary
val AppleTextTertiary: Color @Composable get() = LocalAppleColors.current.textTertiary
val AppleHeaderTitle: Color @Composable get() = LocalAppleColors.current.headerTitle

// Apple iOS System Accents
val AppleGreen: Color @Composable get() = LocalAppleColors.current.green
val AppleGreenBg: Color @Composable get() = LocalAppleColors.current.greenBg
val AppleRed: Color @Composable get() = LocalAppleColors.current.red
val AppleRedBg: Color @Composable get() = LocalAppleColors.current.redBg
val AppleBlue: Color @Composable get() = LocalAppleColors.current.blue
val AppleOrange: Color @Composable get() = LocalAppleColors.current.orange
val AppleYellow: Color @Composable get() = LocalAppleColors.current.yellow
val ApplePurple: Color @Composable get() = LocalAppleColors.current.purple
val AppleSegmentBg: Color @Composable get() = LocalAppleColors.current.segmentBg
val AppleSearchBg: Color @Composable get() = LocalAppleColors.current.searchBg

// Compatibility aliases mapping to Apple palette
val IosBlack = Color(0xFF1C1C1E)
val IosDarkBackground: Color @Composable get() = AppleBackground
val IosCardBackground: Color @Composable get() = AppleCardBackground
val IosSecondaryCard: Color @Composable get() = AppleSegmentBg
val IosCardBorder: Color @Composable get() = AppleCardBorder
val IosDivider: Color @Composable get() = AppleDivider

val IosTextPrimary: Color @Composable get() = AppleTextPrimary
val IosTextSecondary: Color @Composable get() = AppleTextSecondary
val IosTextTertiary: Color @Composable get() = AppleTextTertiary
val IosSearchBackground: Color @Composable get() = AppleSearchBg

val IosGreen: Color @Composable get() = AppleGreen
val IosGreenBackground: Color @Composable get() = AppleGreenBg
val IosRed: Color @Composable get() = AppleRed
val IosRedBackground: Color @Composable get() = AppleRedBg
val IosBlue: Color @Composable get() = AppleBlue
val IosOrange: Color @Composable get() = AppleOrange
val IosYellow: Color @Composable get() = AppleYellow
val IosPurple: Color @Composable get() = ApplePurple

// Legacy liquid glass aliases mapped to Apple tones
val LiquidPurple: Color @Composable get() = AppleBlue
val LiquidPurpleLight: Color @Composable get() = AppleBlue.copy(alpha = 0.8f)
val LiquidPurpleDeep: Color @Composable get() = AppleBlue
val LiquidIndigo: Color @Composable get() = AppleBlue
val LiquidBlue: Color @Composable get() = AppleBlue
val LiquidBlueLight: Color @Composable get() = AppleBlue.copy(alpha = 0.5f)
val LiquidBlueDeep: Color @Composable get() = AppleBlue
val LiquidCyan: Color @Composable get() = AppleBlue

val GlassSurfaceTint: Color @Composable get() = AppleCardBackground
val GlassSurfaceDeep: Color @Composable get() = AppleCardBackground
val GlassBorder: Color @Composable get() = AppleCardBorder
val GlassRimHighlight = Color.Transparent
val GlassSpecular = Color.Transparent
val GlassPillBackground: Color @Composable get() = AppleSegmentBg
val GlassPillActive: Color @Composable get() = AppleBlue
