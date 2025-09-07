package com.unify.ui.platform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import platform.UIKit.*

/**
 * iOS平台主题适配器实现
 */

actual class UnifyPlatformThemeAdapter {
    actual fun getSystemColorScheme(isDark: Boolean): UnifyColorScheme {
        return if (isDark) {
            getDarkColorScheme()
        } else {
            getLightColorScheme()
        }
    }
    
    actual fun getSystemTypography(): UnifyTypography {
        return getDefaultTypography()
    }
    
    actual fun getSystemShapes(): UnifyShapes {
        return getDefaultShapes()
    }
    
    actual fun applyPlatformSpecificTheme(theme: UnifyThemeConfig): UnifyThemeConfig {
        // Apply iOS-specific theme adjustments
        return theme
    }
}

private fun getLightColorScheme(): UnifyColorScheme {
    return UnifyColorScheme(
        primary = 0xFF007AFF, // iOS蓝色
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFE3F2FD,
        onPrimaryContainer = 0xFF0D47A1,
        secondary = 0xFF5856D6, // iOS紫色
        onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFE8DEF8,
        onSecondaryContainer = 0xFF1D192B,
        tertiary = 0xFF32D74B, // iOS绿色
        onTertiary = 0xFFFFFFFF,
        tertiaryContainer = 0xFFE8F5E8,
        onTertiaryContainer = 0xFF1B5E20,
        error = 0xFFFF3B30, // iOS红色
        onError = 0xFFFFFFFF,
        errorContainer = 0xFFFFEBEE,
        onErrorContainer = 0xFFB71C1C,
        background = 0xFFFFFFFF,
        onBackground = 0xFF000000,
        surface = 0xFFF2F2F7,
        onSurface = 0xFF000000,
        surfaceVariant = 0xFFF5F5F5,
        onSurfaceVariant = 0xFF757575,
        outline = 0xFFBDBDBD,
        outlineVariant = 0xFFE0E0E0,
        scrim = 0xFF000000,
        inverseSurface = 0xFF313033,
        inverseOnSurface = 0xFFF4EFF4,
        inversePrimary = 0xFF64B5F6
    )
}

private fun getDarkColorScheme(): UnifyColorScheme {
    return UnifyColorScheme(
        primary = 0xFF64B5F6,
        onPrimary = 0xFF0D47A1,
        primaryContainer = 0xFF1976D2,
        onPrimaryContainer = 0xFFE3F2FD,
        secondary = 0xFF9C88FF,
        onSecondary = 0xFF1D192B,
        secondaryContainer = 0xFF4A4458,
        onSecondaryContainer = 0xFFE8DEF8,
        tertiary = 0xFF81C784,
        onTertiary = 0xFF1B5E20,
        tertiaryContainer = 0xFF388E3C,
        onTertiaryContainer = 0xFFE8F5E8,
        error = 0xFFEF5350,
        onError = 0xFFB71C1C,
        errorContainer = 0xFFD32F2F,
        onErrorContainer = 0xFFFFEBEE,
        background = 0xFF000000,
        onBackground = 0xFFFFFFFF,
        surface = 0xFF1C1C1E,
        onSurface = 0xFFFFFFFF,
        surfaceVariant = 0xFF2C2C2C,
        onSurfaceVariant = 0xFFBDBDBD,
        outline = 0xFF616161,
        outlineVariant = 0xFF424242,
        scrim = 0xFF000000,
        inverseSurface = 0xFFE6E1E5,
        inverseOnSurface = 0xFF313033,
        inversePrimary = 0xFF007AFF
    )
}

private fun getDefaultTypography(): UnifyTypography {
    return UnifyTypography(
        displayLarge = UnifyTextStyle(57f, 64f, 400, -0.25f),
        displayMedium = UnifyTextStyle(45f, 52f, 400, 0f),
        displaySmall = UnifyTextStyle(36f, 44f, 400, 0f),
        headlineLarge = UnifyTextStyle(32f, 40f, 400, 0f),
        headlineMedium = UnifyTextStyle(28f, 36f, 400, 0f),
        headlineSmall = UnifyTextStyle(24f, 32f, 400, 0f),
        titleLarge = UnifyTextStyle(22f, 28f, 400, 0f),
        titleMedium = UnifyTextStyle(16f, 24f, 500, 0.15f),
        titleSmall = UnifyTextStyle(14f, 20f, 500, 0.1f),
        bodyLarge = UnifyTextStyle(16f, 24f, 400, 0.5f),
        bodyMedium = UnifyTextStyle(14f, 20f, 400, 0.25f),
        bodySmall = UnifyTextStyle(12f, 16f, 400, 0.4f),
        labelLarge = UnifyTextStyle(14f, 20f, 500, 0.1f),
        labelMedium = UnifyTextStyle(12f, 16f, 500, 0.5f),
        labelSmall = UnifyTextStyle(11f, 16f, 500, 0.5f)
    )
}

private fun getDefaultShapes(): UnifyShapes {
    return UnifyShapes(
        extraSmall = 4f,
        small = 8f,
        medium = 12f,
        large = 16f,
        extraLarge = 28f
    )
}
