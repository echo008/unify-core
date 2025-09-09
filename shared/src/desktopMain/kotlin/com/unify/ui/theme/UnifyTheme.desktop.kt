package com.unify.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Desktop平台主题actual实现
 */

@Composable
actual fun supportsDynamicColor(): Boolean {
    // Desktop平台不支持动态颜色
    return false
}

@Composable
actual fun dynamicLightColorScheme(): ColorScheme {
    // Desktop平台返回默认浅色主题
    return lightColorScheme(
        primary = Color(0xFF1976D2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3F2FD),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF03DAC6),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFE0F2F1),
        onSecondaryContainer = Color(0xFF004D40),
        tertiary = Color(0xFF9C27B0),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFF3E5F5),
        onTertiaryContainer = Color(0xFF4A148C),
        error = Color(0xFFB00020),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF313033),
        inverseOnSurface = Color(0xFFF4EFF4),
        inversePrimary = Color(0xFFBB86FC),
    )
}

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    // Desktop平台返回默认深色主题
    return darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color(0xFF3700B3),
        primaryContainer = Color(0xFF3700B3),
        onPrimaryContainer = Color(0xFFE1BEE7),
        secondary = Color(0xFF03DAC6),
        onSecondary = Color(0xFF003A3A),
        secondaryContainer = Color(0xFF005353),
        onSecondaryContainer = Color(0xFF80F8FF),
        tertiary = Color(0xFFCF6679),
        onTertiary = Color(0xFF4A0E4E),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
        error = Color(0xFFCF6679),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF121212),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE6E1E5),
        inverseOnSurface = Color(0xFF313033),
        inversePrimary = Color(0xFF6750A4),
    )
}
