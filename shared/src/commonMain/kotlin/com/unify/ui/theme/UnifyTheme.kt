package com.unify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Unify跨平台主题系统
 * 支持亮色/暗色主题，自适应平台风格
 */

// 颜色定义
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF4CAF50),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E8),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = Color(0xFFFF9800),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),
    error = Color(0xFFF44336),
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF757575),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
    scrim = Color(0x80000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF388E3C),
    onSecondaryContainer = Color(0xFFE8F5E8),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFFE65100),
    tertiaryContainer = Color(0xFFF57C00),
    onTertiaryContainer = Color(0xFFFFF3E0),
    error = Color(0xFFEF5350),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242),
    scrim = Color(0x80000000)
)

// 字体定义
private val UnifyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// 主题配置
data class UnifyThemeConfig(
    val useDarkTheme: Boolean = false,
    val useDynamicColor: Boolean = true,
    val platformAdaptive: Boolean = true
)

@Composable
fun UnifyTheme(
    config: UnifyThemeConfig = UnifyThemeConfig(),
    content: @Composable () -> Unit
) {
    val darkTheme = config.useDarkTheme || isSystemInDarkTheme()
    
    val colorScheme = when {
        config.useDynamicColor && supportsDynamicColor() -> {
            if (darkTheme) dynamicDarkColorScheme() else dynamicLightColorScheme()
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnifyTypography,
        content = content
    )
}

@Composable
expect fun supportsDynamicColor(): Boolean

@Composable
expect fun dynamicLightColorScheme(): ColorScheme

@Composable
expect fun dynamicDarkColorScheme(): ColorScheme

// 主题扩展
object UnifyThemeExtensions {
    val ColorScheme.success: Color
        get() = Color(0xFF4CAF50)
    
    val ColorScheme.warning: Color
        get() = Color(0xFFFF9800)
    
    val ColorScheme.info: Color
        get() = Color(0xFF2196F3)
    
    val ColorScheme.onSuccess: Color
        get() = Color.White
    
    val ColorScheme.onWarning: Color
        get() = Color.White
    
    val ColorScheme.onInfo: Color
        get() = Color.White
}

// 平台特定颜色
object PlatformColors {
    // iOS风格颜色
    object iOS {
        val systemBlue = Color(0xFF007AFF)
        val systemGreen = Color(0xFF34C759)
        val systemRed = Color(0xFFFF3B30)
        val systemOrange = Color(0xFFFF9500)
        val systemYellow = Color(0xFFFFCC00)
        val systemPurple = Color(0xFFAF52DE)
        val systemPink = Color(0xFFFF2D92)
        val systemGray = Color(0xFF8E8E93)
    }
    
    // Android风格颜色
    object Android {
        val materialBlue = Color(0xFF2196F3)
        val materialGreen = Color(0xFF4CAF50)
        val materialRed = Color(0xFFF44336)
        val materialOrange = Color(0xFFFF9800)
        val materialYellow = Color(0xFFFFEB3B)
        val materialPurple = Color(0xFF9C27B0)
        val materialPink = Color(0xFFE91E63)
        val materialGray = Color(0xFF9E9E9E)
    }
    
    // Web风格颜色
    object Web {
        val bootstrapPrimary = Color(0xFF0066CC)
        val bootstrapSuccess = Color(0xFF28A745)
        val bootstrapDanger = Color(0xFFDC3545)
        val bootstrapWarning = Color(0xFFFFC107)
        val bootstrapInfo = Color(0xFF17A2B8)
        val bootstrapSecondary = Color(0xFF6C757D)
        val bootstrapLight = Color(0xFFF8F9FA)
        val bootstrapDark = Color(0xFF343A40)
    }
}
