package com.unify.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import platform.UIKit.*

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
        primary = Color(0xFF007AFF), // iOS blue
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3F2FD),
        onPrimaryContainer = Color(0xFF001D36),
        
        secondary = Color(0xFF5856D6), // iOS purple
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        
        tertiary = Color(0xFF32D74B), // iOS green
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD0F8CE),
        onTertiaryContainer = Color(0xFF002204),
        
        error = Color(0xFFFF3B30), // iOS red
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        
        background = Color(0xFFFFFFFF),
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
        inversePrimary = Color(0xFF9ECAFF)
    )
}

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    // iOS doesn't have dynamic colors, return standard dark scheme
    return darkColorScheme(
        primary = Color(0xFF64B5F6), // Lighter iOS blue for dark mode
        onPrimary = Color(0xFF003258),
        primaryContainer = Color(0xFF00497D),
        onPrimaryContainer = Color(0xFFCFE5FF),
        
        secondary = Color(0xFF9C88FF), // Lighter iOS purple for dark mode
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        
        tertiary = Color(0xFF81C784), // Lighter iOS green for dark mode
        onTertiary = Color(0xFF0F380A),
        tertiaryContainer = Color(0xFF255420),
        onTertiaryContainer = Color(0xFFBFF2BD),
        
        error = Color(0xFFFFB4AB), // iOS深色模式红色
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        
        background = Color(0xFF000000), // iOS深色模式纯黑背景
        onBackground = Color(0xFFE6E1E5),
        
        surface = Color(0xFF1C1C1E), // iOS深色模式表面颜色
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE6E1E5),
        inverseOnSurface = Color(0xFF313033),
        inversePrimary = Color(0xFF005FAF)
    )
}
