package com.unify.core.ui.watch

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Watch平台专用主题系统
 * 针对小屏幕和低功耗场景优化
 * 基于Material Design for Wear OS规范
 */

/**
 * Watch主题配置
 * 性能优化：使用稳定的数据类避免重组
 */
@Stable
data class WatchThemeConfig(
    val isAmbientMode: Boolean = false,
    val isLowPowerMode: Boolean = false,
    val isDarkTheme: Boolean = true
)

/**
 * Watch专用颜色方案
 * 针对OLED屏幕和环境模式优化
 */
private val WatchDarkColorScheme = darkColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFBBDEFB),
    
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color(0xFFA7FFEB),
    
    tertiary = Color(0xFFFF6F00),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFE65100),
    onTertiaryContainer = Color(0xFFFFE0B2),
    
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFE0E0E0),
    
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF2C2C2C)
)

/**
 * 环境模式颜色方案（黑白显示，节省电量）
 */
private val WatchAmbientColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color.White,
    
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color.White,
    
    tertiary = Color.White,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF333333),
    onTertiaryContainer = Color.White,
    
    error = Color.White,
    onError = Color.Black,
    errorContainer = Color(0xFF333333),
    onErrorContainer = Color.White,
    
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color.Black,
    onSurfaceVariant = Color.White,
    
    outline = Color(0xFF666666),
    outlineVariant = Color(0xFF333333)
)

/**
 * Watch专用字体排版
 * 针对小屏幕优化的字体大小
 */
private val WatchTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    ),
    
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    ),
    
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 12.sp
    ),
    
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 8.sp,
        lineHeight = 12.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 6.sp,
        lineHeight = 10.sp
    )
)

/**
 * Watch主题组合器
 * 性能优化：使用CompositionLocal避免prop drilling
 */
val LocalWatchThemeConfig = compositionLocalOf { WatchThemeConfig() }

/**
 * Watch主题提供器
 * 根据环境模式和电源状态自动切换主题
 */
@Composable
fun WatchTheme(
    isAmbientMode: Boolean = false,
    isLowPowerMode: Boolean = false,
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // 性能优化：使用remember缓存主题配置
    val themeConfig = remember(isAmbientMode, isLowPowerMode, isDarkTheme) {
        WatchThemeConfig(
            isAmbientMode = isAmbientMode,
            isLowPowerMode = isLowPowerMode,
            isDarkTheme = isDarkTheme
        )
    }
    
    // 性能优化：根据模式选择最优颜色方案
    val colorScheme = remember(isAmbientMode, isLowPowerMode) {
        when {
            isAmbientMode -> WatchAmbientColorScheme
            else -> WatchDarkColorScheme
        }
    }
    
    CompositionLocalProvider(LocalWatchThemeConfig provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WatchTypography,
            content = content
        )
    }
}

/**
 * Watch主题扩展函数
 * 方便获取当前主题配置
 */
object WatchTheme {
    val config: WatchThemeConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalWatchThemeConfig.current
    
    val isAmbientMode: Boolean
        @Composable
        @ReadOnlyComposable
        get() = config.isAmbientMode
    
    val isLowPowerMode: Boolean
        @Composable
        @ReadOnlyComposable
        get() = config.isLowPowerMode
}
