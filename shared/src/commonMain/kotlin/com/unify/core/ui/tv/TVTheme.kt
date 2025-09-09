package com.unify.core.ui.tv

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.unify.core.platform.tv.TVDisplayInfo

/**
 * TV平台专用主题系统
 * 针对10-foot UI和大屏幕体验优化
 * 基于Android TV和Material Design TV规范
 */

/**
 * TV主题配置
 * 性能优化：使用稳定的数据类避免重组
 */
@Stable
data class TVThemeConfig(
    val displayInfo: TVDisplayInfo,
    val isActive: Boolean = true,
    val isDarkTheme: Boolean = true,
    val focusScale: Float = 1.1f,
    val animationDuration: Int = 200
)

/**
 * TV专用颜色方案
 * 针对大屏幕和远距离观看优化
 */
private val TVDarkColorScheme = darkColorScheme(
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
    
    background = Color(0xFF0A0A0A),
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFE0E0E0),
    
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF2C2C2C)
)

/**
 * TV专用字体排版
 * 针对10-foot UI优化的字体大小
 */
private val TVTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 20.sp
    ),
    
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 18.sp
    )
)

/**
 * TV焦点颜色定义
 * 针对遥控器导航优化
 */
object TVFocusColors {
    val focused = Color(0xFF1976D2)
    val focusedBorder = Color(0xFF42A5F5)
    val unfocused = Color.Transparent
    val unfocusedBorder = Color(0xFF424242)
    val pressed = Color(0xFF0D47A1)
}

/**
 * TV主题组合器
 * 性能优化：使用CompositionLocal避免prop drilling
 */
val LocalTVThemeConfig = compositionLocalOf { 
    TVThemeConfig(
        displayInfo = TVDisplayInfo(
            width = 1920,
            height = 1080,
            refreshRate = 60.0f,
            density = 1.0f,
            aspectRatio = "16:9",
            is4K = false,
            hdrSupported = false
        )
    ) 
}

/**
 * TV主题提供器
 * 根据显示信息和活动状态自动调整主题
 */
@Composable
fun TVTheme(
    displayInfo: TVDisplayInfo,
    isActive: Boolean = true,
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // 性能优化：使用remember缓存主题配置
    val themeConfig = remember(displayInfo, isActive, isDarkTheme) {
        TVThemeConfig(
            displayInfo = displayInfo,
            isActive = isActive,
            isDarkTheme = isDarkTheme,
            focusScale = if (displayInfo.is4K) 1.05f else 1.1f,
            animationDuration = if (displayInfo.refreshRate >= 120f) 150 else 200
        )
    }
    
    // 性能优化：根据显示信息选择最优颜色方案
    val colorScheme = remember(isDarkTheme) {
        TVDarkColorScheme
    }
    
    CompositionLocalProvider(LocalTVThemeConfig provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TVTypography,
            content = content
        )
    }
}

/**
 * TV主题扩展函数
 * 方便获取当前主题配置
 */
object TVTheme {
    val config: TVThemeConfig
        @Composable
        @ReadOnlyComposable
        get() = LocalTVThemeConfig.current
    
    val displayInfo: TVDisplayInfo
        @Composable
        @ReadOnlyComposable
        get() = config.displayInfo
    
    val isActive: Boolean
        @Composable
        @ReadOnlyComposable
        get() = config.isActive
    
    val focusScale: Float
        @Composable
        @ReadOnlyComposable
        get() = config.focusScale
    
    val animationDuration: Int
        @Composable
        @ReadOnlyComposable
        get() = config.animationDuration
}
