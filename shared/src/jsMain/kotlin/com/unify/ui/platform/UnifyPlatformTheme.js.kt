package com.unify.ui.platform

actual class UnifyPlatformThemeAdapter actual constructor() {
    actual fun getSystemColorScheme(isDark: Boolean): UnifyColorScheme {
        return UnifyColorScheme(
            primary = 0xFF2196F3L,
            onPrimary = 0xFFFFFFFFL,
            primaryContainer = 0xFFBBDEFBL,
            onPrimaryContainer = 0xFF0D47A1L,
            secondary = 0xFF757575L,
            onSecondary = 0xFFFFFFFFL,
            secondaryContainer = 0xFFE0E0E0L,
            onSecondaryContainer = 0xFF424242L,
            tertiary = 0xFF4CAF50L,
            onTertiary = 0xFFFFFFFFL,
            tertiaryContainer = 0xFFC8E6C9L,
            onTertiaryContainer = 0xFF1B5E20L,
            error = 0xFFF44336L,
            onError = 0xFFFFFFFFL,
            errorContainer = 0xFFFFCDD2L,
            onErrorContainer = 0xFFB71C1CL,
            background = if (isDark) 0xFF121212L else 0xFFFFFFFFL,
            onBackground = if (isDark) 0xFFFFFFFFL else 0xFF000000L,
            surface = if (isDark) 0xFF1E1E1EL else 0xFFFAFAFAL,
            onSurface = if (isDark) 0xFFFFFFFFL else 0xFF000000L,
            surfaceVariant = if (isDark) 0xFF2E2E2EL else 0xFFF5F5F5L,
            onSurfaceVariant = if (isDark) 0xFFE0E0E0L else 0xFF616161L,
            outline = 0xFF9E9E9EL,
            outlineVariant = 0xFFBDBDBDL,
            scrim = 0xFF000000L,
            inverseSurface = if (isDark) 0xFFFFFFFFL else 0xFF121212L,
            inverseOnSurface = if (isDark) 0xFF000000L else 0xFFFFFFFFL,
            inversePrimary = 0xFF1976D2L,
        )
    }

    actual fun getSystemTypography(): UnifyTypography {
        return UnifyTypography(
            displayLarge = UnifyTextStyle(57f, 64f, 400, 0f),
            displayMedium = UnifyTextStyle(45f, 52f, 400, 0f),
            displaySmall = UnifyTextStyle(36f, 44f, 400, 0f),
            headlineLarge = UnifyTextStyle(32f, 40f, 400, 0f),
            headlineMedium = UnifyTextStyle(28f, 36f, 400, 0f),
            headlineSmall = UnifyTextStyle(24f, 32f, 400, 0f),
            titleLarge = UnifyTextStyle(22f, 28f, 500, 0f),
            titleMedium = UnifyTextStyle(16f, 24f, 500, 0.15f),
            titleSmall = UnifyTextStyle(14f, 20f, 500, 0.1f),
            bodyLarge = UnifyTextStyle(16f, 24f, 400, 0.5f),
            bodyMedium = UnifyTextStyle(14f, 20f, 400, 0.25f),
            bodySmall = UnifyTextStyle(12f, 16f, 400, 0.4f),
            labelLarge = UnifyTextStyle(14f, 20f, 500, 0.1f),
            labelMedium = UnifyTextStyle(12f, 16f, 500, 0.5f),
            labelSmall = UnifyTextStyle(11f, 16f, 500, 0.5f),
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
        return theme
    }
}
