package com.unify.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * iOS平台主题实现
 */

@Composable
actual fun supportsDynamicColor(): Boolean {
    return false // iOS doesn't support dynamic color like Android 12+
}

@Composable
actual fun dynamicLightColorScheme(): ColorScheme {
    // iOS doesn't have dynamic colors, return standard light scheme
    return lightColorScheme(
        // iOS系统蓝色
        primary = Color(0xFF007AFF), // blue
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3F2FD),
        onPrimaryContainer = Color(0xFF001D36),
        // iOS系统紫色
        secondary = Color(0xFF5856D6), // purple
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        // iOS系统绿色
        tertiary = Color(0xFF34C759), // green
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD0F8CE),
        onTertiaryContainer = Color(0xFF002204),
        // iOS系统红色
        error = Color(0xFFFF3B30), // red
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        // iOS浅色模式背景
        background = Color(0xFFF2F2F7),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFF2F2F7), // iOS gray background
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF2F3033),
        inverseOnSurface = Color(0xFFF1F0F4),
        inversePrimary = Color(0xFF9ECAFF),
    )
}

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    // iOS doesn't have dynamic colors, return standard dark scheme
    return darkColorScheme(
        // Lighter iOS blue for dark mode
        primary = Color(0xFF64B5F6),
        onPrimary = Color(0xFF003258),
        primaryContainer = Color(0xFF00497D),
        onPrimaryContainer = Color(0xFFCFE5FF),
        // Lighter iOS purple for dark mode
        secondary = Color(0xFF9C88FF),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        // Lighter iOS green for dark mode
        tertiary = Color(0xFF81C784),
        onTertiary = Color(0xFF0F380A),
        tertiaryContainer = Color(0xFF255420),
        onTertiaryContainer = Color(0xFFBFF2BD),
        // iOS深色模式红色
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        // iOS深色模式纯黑背景
        background = Color(0xFF000000),
        onBackground = Color(0xFFE6E1E5),
        // iOS深色模式表面颜色
        surface = Color(0xFF1C1C1E),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE6E1E5),
        inverseOnSurface = Color(0xFF313033),
        inversePrimary = Color(0xFF005FAF),
    )
}
