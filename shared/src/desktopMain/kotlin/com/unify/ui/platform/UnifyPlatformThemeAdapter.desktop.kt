package com.unify.ui.platform

/**
 * Desktop平台主题适配器actual实现
 */
actual class UnifyPlatformThemeAdapter {
    actual fun getSystemColorScheme(isDark: Boolean): UnifyColorScheme {
        return if (isDark) {
            UnifyColorScheme(
                primary = 0xFFBB86FC,
                onPrimary = 0xFF3700B3,
                primaryContainer = 0xFF3700B3,
                onPrimaryContainer = 0xFFE1BEE7,
                secondary = 0xFF03DAC6,
                onSecondary = 0xFF003A3A,
                secondaryContainer = 0xFF005353,
                onSecondaryContainer = 0xFF80F8FF,
                tertiary = 0xFFCF6679,
                onTertiary = 0xFF4A0E4E,
                tertiaryContainer = 0xFF633B48,
                onTertiaryContainer = 0xFFFFD8E4,
                error = 0xFFCF6679,
                onError = 0xFF601410,
                errorContainer = 0xFF8C1D18,
                onErrorContainer = 0xFFFFDAD6,
                background = 0xFF121212,
                onBackground = 0xFFE6E1E5,
                surface = 0xFF1E1E1E,
                onSurface = 0xFFE6E1E5,
                surfaceVariant = 0xFF49454F,
                onSurfaceVariant = 0xFFCAC4D0,
                outline = 0xFF938F99,
                outlineVariant = 0xFF49454F,
                scrim = 0xFF000000,
                inverseSurface = 0xFFE6E1E5,
                inverseOnSurface = 0xFF313033,
                inversePrimary = 0xFF6750A4,
            )
        } else {
            UnifyColorScheme(
                primary = 0xFF1976D2,
                onPrimary = 0xFFFFFFFF,
                primaryContainer = 0xFFE3F2FD,
                onPrimaryContainer = 0xFF0D47A1,
                secondary = 0xFF03DAC6,
                onSecondary = 0xFF000000,
                secondaryContainer = 0xFFE0F2F1,
                onSecondaryContainer = 0xFF004D40,
                tertiary = 0xFF9C27B0,
                onTertiary = 0xFFFFFFFF,
                tertiaryContainer = 0xFFF3E5F5,
                onTertiaryContainer = 0xFF4A148C,
                error = 0xFFB00020,
                onError = 0xFFFFFFFF,
                errorContainer = 0xFFFFDAD6,
                onErrorContainer = 0xFF410002,
                background = 0xFFFFFBFE,
                onBackground = 0xFF1C1B1F,
                surface = 0xFFFFFFFF,
                onSurface = 0xFF1C1B1F,
                surfaceVariant = 0xFFE7E0EC,
                onSurfaceVariant = 0xFF49454F,
                outline = 0xFF79747E,
                outlineVariant = 0xFFCAC4D0,
                scrim = 0xFF000000,
                inverseSurface = 0xFF313033,
                inverseOnSurface = 0xFFF4EFF4,
                inversePrimary = 0xFFBB86FC,
            )
        }
    }

    actual fun getSystemTypography(): UnifyTypography {
        return UnifyTypography(
            displayLarge =
                UnifyTextStyle(
                    fontSize = 57f,
                    lineHeight = 64f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            displayMedium =
                UnifyTextStyle(
                    fontSize = 45f,
                    lineHeight = 52f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            displaySmall =
                UnifyTextStyle(
                    fontSize = 36f,
                    lineHeight = 44f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            headlineLarge =
                UnifyTextStyle(
                    fontSize = 32f,
                    lineHeight = 40f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            headlineMedium =
                UnifyTextStyle(
                    fontSize = 28f,
                    lineHeight = 36f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            headlineSmall =
                UnifyTextStyle(
                    fontSize = 24f,
                    lineHeight = 32f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            titleLarge =
                UnifyTextStyle(
                    fontSize = 22f,
                    lineHeight = 28f,
                    fontWeight = 400,
                    letterSpacing = 0f,
                ),
            titleMedium =
                UnifyTextStyle(
                    fontSize = 16f,
                    lineHeight = 24f,
                    fontWeight = 500,
                    letterSpacing = 0.15f,
                ),
            titleSmall =
                UnifyTextStyle(
                    fontSize = 14f,
                    lineHeight = 20f,
                    fontWeight = 500,
                    letterSpacing = 0.1f,
                ),
            bodyLarge =
                UnifyTextStyle(
                    fontSize = 16f,
                    lineHeight = 24f,
                    fontWeight = 400,
                    letterSpacing = 0.5f,
                ),
            bodyMedium =
                UnifyTextStyle(
                    fontSize = 14f,
                    lineHeight = 20f,
                    fontWeight = 400,
                    letterSpacing = 0.25f,
                ),
            bodySmall =
                UnifyTextStyle(
                    fontSize = 12f,
                    lineHeight = 16f,
                    fontWeight = 400,
                    letterSpacing = 0.4f,
                ),
            labelLarge =
                UnifyTextStyle(
                    fontSize = 14f,
                    lineHeight = 20f,
                    fontWeight = 500,
                    letterSpacing = 0.1f,
                ),
            labelMedium =
                UnifyTextStyle(
                    fontSize = 12f,
                    lineHeight = 16f,
                    fontWeight = 500,
                    letterSpacing = 0.5f,
                ),
            labelSmall =
                UnifyTextStyle(
                    fontSize = 11f,
                    lineHeight = 16f,
                    fontWeight = 500,
                    letterSpacing = 0.5f,
                ),
        )
    }

    actual fun getSystemShapes(): UnifyShapes {
        return UnifyShapes(
            extraSmall = 4f,
            small = 8f,
            medium = 12f,
            large = 16f,
            extraLarge = 28f,
        )
    }

    actual fun applyPlatformSpecificTheme(theme: UnifyThemeConfig): UnifyThemeConfig {
        // Desktop平台特定的主题调整
        return theme.copy(
            // 可以在这里添加Desktop平台特定的主题修改
            // 例如调整字体大小、间距等
        )
    }
}
