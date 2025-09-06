package com.unify.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Android平台主题适配器实现
 */
actual class UnifyPlatformThemeAdapter {

    actual fun getSystemColorScheme(isDark: Boolean): UnifyColorScheme {
        return if (isDark) {
            UnifyColorScheme(
                primary = 0xFFD0BCFF,
                onPrimary = 0xFF381E72,
                primaryContainer = 0xFF4F378B,
                onPrimaryContainer = 0xFFEADDFF,
                secondary = 0xFFCCC2DC,
                onSecondary = 0xFF332D41,
                secondaryContainer = 0xFF4A4458,
                onSecondaryContainer = 0xFFE8DEF8,
                tertiary = 0xFFEFB8C8,
                onTertiary = 0xFF492532,
                tertiaryContainer = 0xFF633B48,
                onTertiaryContainer = 0xFFFFD8E4,
                error = 0xFFFFB4AB,
                onError = 0xFF690005,
                errorContainer = 0xFF93000A,
                onErrorContainer = 0xFFFFDAD6,
                background = 0xFF1C1B1F,
                onBackground = 0xFFE6E1E5,
                surface = 0xFF1C1B1F,
                onSurface = 0xFFE6E1E5,
                surfaceVariant = 0xFF49454F,
                onSurfaceVariant = 0xFFCAC4D0,
                outline = 0xFF938F99,
                outlineVariant = 0xFF49454F,
                scrim = 0xFF000000,
                inverseSurface = 0xFFE6E1E5,
                inverseOnSurface = 0xFF313033,
                inversePrimary = 0xFF6750A4
            )
        } else {
            UnifyColorScheme(
                primary = 0xFF6750A4,
                onPrimary = 0xFFFFFFFF,
                primaryContainer = 0xFFEADDFF,
                onPrimaryContainer = 0xFF21005D,
                secondary = 0xFF625B71,
                onSecondary = 0xFFFFFFFF,
                secondaryContainer = 0xFFE8DEF8,
                onSecondaryContainer = 0xFF1D192B,
                tertiary = 0xFF7D5260,
                onTertiary = 0xFFFFFFFF,
                tertiaryContainer = 0xFFFFD8E4,
                onTertiaryContainer = 0xFF31111D,
                error = 0xFFBA1A1A,
                onError = 0xFFFFFFFF,
                errorContainer = 0xFFFFDAD6,
                onErrorContainer = 0xFF410002,
                background = 0xFFFFFBFE,
                onBackground = 0xFF1C1B1F,
                surface = 0xFFFFFBFE,
                onSurface = 0xFF1C1B1F,
                surfaceVariant = 0xFFE7E0EC,
                onSurfaceVariant = 0xFF49454F,
                outline = 0xFF79747E,
                outlineVariant = 0xFFCAC4D0,
                scrim = 0xFF000000,
                inverseSurface = 0xFF313033,
                inverseOnSurface = 0xFFF4EFF4,
                inversePrimary = 0xFFD0BCFF
            )
        }
    }

    actual fun getSystemTypography(): UnifyTypography {
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

    actual fun getSystemShapes(): UnifyShapes {
        return UnifyShapes(
            extraSmall = 4f,
            small = 8f,
            medium = 12f,
            large = 16f,
            extraLarge = 28f
        )
    }

    actual fun applyPlatformSpecificTheme(theme: UnifyThemeConfig): UnifyThemeConfig {
        // Apply Android-specific theme modifications
        return theme
    }
}
