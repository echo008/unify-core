package com.unify.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Unify主题系统
 * 提供跨平台一致的Material3主题
 */

// 主色调定义
private val UnifyPrimary = Color(0xFF6750A4)
private val UnifyOnPrimary = Color(0xFFFFFFFF)
private val UnifyPrimaryContainer = Color(0xFFEADDFF)
private val UnifyOnPrimaryContainer = Color(0xFF21005D)

private val UnifySecondary = Color(0xFF625B71)
private val UnifyOnSecondary = Color(0xFFFFFFFF)
private val UnifySecondaryContainer = Color(0xFFE8DEF8)
private val UnifyOnSecondaryContainer = Color(0xFF1D192B)

private val UnifyTertiary = Color(0xFF7D5260)
private val UnifyOnTertiary = Color(0xFFFFFFFF)
private val UnifyTertiaryContainer = Color(0xFFFFD8E4)
private val UnifyOnTertiaryContainer = Color(0xFF31111D)

private val UnifyError = Color(0xFFBA1A1A)
private val UnifyOnError = Color(0xFFFFFFFF)
private val UnifyErrorContainer = Color(0xFFFFDAD6)
private val UnifyOnErrorContainer = Color(0xFF410002)

private val UnifyBackground = Color(0xFFFFFBFE)
private val UnifyOnBackground = Color(0xFF1C1B1F)
private val UnifySurface = Color(0xFFFFFBFE)
private val UnifyOnSurface = Color(0xFF1C1B1F)

// 浅色主题
private val UnifyLightColorScheme = lightColorScheme(
    primary = UnifyPrimary,
    onPrimary = UnifyOnPrimary,
    primaryContainer = UnifyPrimaryContainer,
    onPrimaryContainer = UnifyOnPrimaryContainer,
    secondary = UnifySecondary,
    onSecondary = UnifyOnSecondary,
    secondaryContainer = UnifySecondaryContainer,
    onSecondaryContainer = UnifyOnSecondaryContainer,
    tertiary = UnifyTertiary,
    onTertiary = UnifyOnTertiary,
    tertiaryContainer = UnifyTertiaryContainer,
    onTertiaryContainer = UnifyOnTertiaryContainer,
    error = UnifyError,
    onError = UnifyOnError,
    errorContainer = UnifyErrorContainer,
    onErrorContainer = UnifyOnErrorContainer,
    background = UnifyBackground,
    onBackground = UnifyOnBackground,
    surface = UnifySurface,
    onSurface = UnifyOnSurface,
)

// 深色主题
private val UnifyDarkColorScheme = darkColorScheme(
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
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
)

/**
 * Unify主题配置
 */
data class UnifyThemeConfig(
    val isDarkTheme: Boolean = false,
    val dynamicColor: Boolean = false
)

/**
 * 主题配置的CompositionLocal
 */
val LocalUnifyThemeConfig = staticCompositionLocalOf { UnifyThemeConfig() }

/**
 * Unify主题组件
 */
@Composable
fun UnifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> UnifyDarkColorScheme
        else -> UnifyLightColorScheme
    }
    
    val themeConfig = UnifyThemeConfig(
        isDarkTheme = darkTheme,
        dynamicColor = dynamicColor
    )
    
    CompositionLocalProvider(
        LocalUnifyThemeConfig provides themeConfig
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = UnifyTypography,
            shapes = UnifyShapes,
            content = content
        )
    }
}
