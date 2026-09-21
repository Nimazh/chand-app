package com.chand.app.widget

import androidx.compose.ui.graphics.Color

/**
 * Customizable visual themes for Android Home Screen Glance Widgets.
 * Defaults to Apple White (matching official iOS Chand app screenshots).
 */
enum class WidgetTheme(
    val id: String,
    val titleFa: String,
    val baseColor: Color,
    val headerColor: Color,
    val cardBg: Color,
    val accentColor: Color,
    val textColor: Color,
    val subTextColor: Color,
    val previewGradient: List<Color>
) {
    APPLE_WHITE(
        id = "apple_white",
        titleFa = "اپل لایت سفید (Apple White)",
        baseColor = Color(0xFFFFFFFF),
        headerColor = Color(0xFF1C1C1E),
        cardBg = Color(0xFFF2F2F7),
        accentColor = Color(0xFF007AFF),
        textColor = Color(0xFF1C1C1E),
        subTextColor = Color(0xFF8E8E93),
        previewGradient = listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F7))
    ),
    DARK_MINIMAL(
        id = "dark_minimal",
        titleFa = "اپل دارک مینیمال (Dark Apple)",
        baseColor = Color(0xFF1C1C1E),
        headerColor = Color(0xFFFFFFFF),
        cardBg = Color(0xFF2C2C2E),
        accentColor = Color(0xFF0A84FF),
        textColor = Color(0xFFFFFFFF),
        subTextColor = Color(0xFF8E8E93),
        previewGradient = listOf(Color(0xFF1C1C1E), Color(0xFF2C2C2E))
    ),
    TRANSPARENT_GLASS(
        id = "transparent_glass",
        titleFa = "شیشه مات خنثی (Frosted Glass)",
        baseColor = Color(0xFFE5E5EA),
        headerColor = Color(0xFF1C1C1E),
        cardBg = Color(0xFFD1D1D6),
        accentColor = Color(0xFF007AFF),
        textColor = Color(0xFF1C1C1E),
        subTextColor = Color(0xFF8E8E93),
        previewGradient = listOf(Color(0xFFE5E5EA), Color(0xFFD1D1D6))
    ),
    MIDNIGHT(
        id = "midnight",
        titleFa = "سورمه‌ای کیهانی (Midnight)",
        baseColor = Color(0xFF0B132B),
        headerColor = Color(0xFF38BDF8),
        cardBg = Color(0xFF1C2541),
        accentColor = Color(0xFF00B4D8),
        textColor = Color(0xFFFFFFFF),
        subTextColor = Color(0xFF94A3B8),
        previewGradient = listOf(Color(0xFF0B132B), Color(0xFF1C2541))
    ),
    EMERALD(
        id = "emerald",
        titleFa = "سبز زمردی (Emerald)",
        baseColor = Color(0xFF06281E),
        headerColor = Color(0xFF34D399),
        cardBg = Color(0xFF0F4D3B),
        accentColor = Color(0xFF10B981),
        textColor = Color(0xFFFFFFFF),
        subTextColor = Color(0xFF6EE7B7),
        previewGradient = listOf(Color(0xFF06281E), Color(0xFF0F5132))
    );

    fun resolveBackgroundColor(opacityPercent: Int): Color {
        val alpha = (opacityPercent.coerceIn(20, 100) / 100f)
        return baseColor.copy(alpha = alpha)
    }

    fun resolveCardBg(opacityPercent: Int): Color {
        val alpha = (opacityPercent.coerceIn(20, 100) / 100f)
        return cardBg.copy(alpha = alpha)
    }

    companion object {
        fun fromId(id: String): WidgetTheme {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: APPLE_WHITE
        }
    }
}
