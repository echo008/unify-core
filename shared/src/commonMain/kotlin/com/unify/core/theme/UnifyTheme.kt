package com.unify.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Unify跨平台主题系统
 * 支持8大平台的统一主题管理
 */

// 浅色主题颜色定义
private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        tertiary = Color(0xFF7D5260),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF79747E),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        inverseSurface = Color(0xFF313033),
        inverseOnSurface = Color(0xFFF4EFF4),
        inversePrimary = Color(0xFFD0BCFF),
        surfaceTint = Color(0xFF6750A4),
        outlineVariant = Color(0xFFCAC4D0),
        scrim = Color(0xFF000000),
    )

// 深色主题颜色定义
private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF938F99),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        inverseSurface = Color(0xFFE6E1E5),
        inverseOnSurface = Color(0xFF313033),
        inversePrimary = Color(0xFF6750A4),
        surfaceTint = Color(0xFFD0BCFF),
        outlineVariant = Color(0xFF49454F),
        scrim = Color(0xFF000000),
    )

/**
 * Unify主题组件
 */
@Composable
fun UnifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnifyTypography,
        shapes = UnifyShapes,
        content = content,
    )
}

/**
 * 自定义颜色扩展
 */
data class UnifyCustomColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

val LightCustomColors =
    UnifyCustomColors(
        success = Color(0xFF4CAF50),
        onSuccess = Color(0xFFFFFFFF),
        successContainer = Color(0xFFE8F5E8),
        onSuccessContainer = Color(0xFF1B5E20),
        warning = Color(0xFFFF9800),
        onWarning = Color(0xFFFFFFFF),
        warningContainer = Color(0xFFFFF3E0),
        onWarningContainer = Color(0xFFE65100),
        info = Color(0xFF2196F3),
        onInfo = Color(0xFFFFFFFF),
        infoContainer = Color(0xFFE3F2FD),
        onInfoContainer = Color(0xFF0D47A1),
    )

val DarkCustomColors =
    UnifyCustomColors(
        success = Color(0xFF81C784),
        onSuccess = Color(0xFF1B5E20),
        successContainer = Color(0xFF2E7D32),
        onSuccessContainer = Color(0xFFC8E6C9),
        warning = Color(0xFFFFB74D),
        onWarning = Color(0xFFE65100),
        warningContainer = Color(0xFFF57C00),
        onWarningContainer = Color(0xFFFFF3E0),
        info = Color(0xFF64B5F6),
        onInfo = Color(0xFF0D47A1),
        infoContainer = Color(0xFF1976D2),
        onInfoContainer = Color(0xFFE3F2FD),
    )

/**
 * 自定义颜色CompositionLocal
 */
val LocalUnifyCustomColors = androidx.compose.runtime.staticCompositionLocalOf { LightCustomColors }

/**
 * 带自定义颜色的Unify主题
 */
@Composable
fun UnifyThemeWithCustomColors(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    CompositionLocalProvider(LocalUnifyCustomColors provides customColors) {
        UnifyTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            content = content,
        )
    }
}

/**
 * 获取当前自定义颜色
 */
object UnifyThemeColors {
    val current: UnifyCustomColors
        @Composable
        get() = LocalUnifyCustomColors.current
}
