package com.unify.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape

/**
 * Unify UI 主题系统
 * 参考 KuiklyUI 设计规范，提供完整的主题定制能力
 */

/**
 * 主题数据类
 */
@Immutable
data class UnifyTheme(
    val colors: UnifyColors,
    val typography: UnifyTypography,
    val shapes: UnifyShapes,
    val dimensions: UnifyDimensions,
    val animations: UnifyAnimations
) {
    companion object {
        fun defaultTheme(): UnifyTheme {
            return UnifyTheme(
                colors = UnifyColors.defaultLight(),
                typography = UnifyTypography.default(),
                shapes = UnifyShapes.default(),
                dimensions = UnifyDimensions.default(),
                animations = UnifyAnimations.default()
            )
        }
        
        fun darkTheme(): UnifyTheme {
            return UnifyTheme(
                colors = UnifyColors.defaultDark(),
                typography = UnifyTypography.default(),
                shapes = UnifyShapes.default(),
                dimensions = UnifyDimensions.default(),
                animations = UnifyAnimations.default()
            )
        }
    }
}

/**
 * 颜色系统
 */
@Immutable
data class UnifyColors(
    // 主色调
    val primary: Color,
    val primaryVariant: Color,
    val onPrimary: Color,
    
    // 次要色调
    val secondary: Color,
    val secondaryVariant: Color,
    val onSecondary: Color,
    
    // 背景色
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    
    // 错误色
    val error: Color,
    val onError: Color,
    
    // 成功色
    val success: Color,
    val onSuccess: Color,
    
    // 警告色
    val warning: Color,
    val onWarning: Color,
    
    // 信息色
    val info: Color,
    val onInfo: Color,
    
    // 中性色
    val outline: Color,
    val outlineVariant: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    
    // 透明度变化
    val scrim: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    
    // 自定义语义色
    val disabled: Color,
    val placeholder: Color,
    val divider: Color,
    val shadow: Color
) {
    companion object {
        fun defaultLight(): UnifyColors {
            return UnifyColors(
                primary = Color(0xFF1976D2),
                primaryVariant = Color(0xFF1565C0),
                onPrimary = Color.White,
                
                secondary = Color(0xFF03DAC6),
                secondaryVariant = Color(0xFF018786),
                onSecondary = Color.Black,
                
                background = Color(0xFFFFFBFE),
                onBackground = Color(0xFF1C1B1F),
                surface = Color(0xFFFFFBFE),
                onSurface = Color(0xFF1C1B1F),
                
                error = Color(0xFFB00020),
                onError = Color.White,
                
                success = Color(0xFF4CAF50),
                onSuccess = Color.White,
                
                warning = Color(0xFFFF9800),
                onWarning = Color.White,
                
                info = Color(0xFF2196F3),
                onInfo = Color.White,
                
                outline = Color(0xFF79747E),
                outlineVariant = Color(0xFFCAC4D0),
                surfaceVariant = Color(0xFFE7E0EC),
                onSurfaceVariant = Color(0xFF49454F),
                
                scrim = Color(0x80000000),
                inverseSurface = Color(0xFF313033),
                inverseOnSurface = Color(0xFFF4EFF4),
                inversePrimary = Color(0xFFBB86FC),
                
                disabled = Color(0x61000000),
                placeholder = Color(0x99000000),
                divider = Color(0x1F000000),
                shadow = Color(0x33000000)
            )
        }
        
        fun defaultDark(): UnifyColors {
            return UnifyColors(
                primary = Color(0xFFBB86FC),
                primaryVariant = Color(0xFF3700B3),
                onPrimary = Color.Black,
                
                secondary = Color(0xFF03DAC6),
                secondaryVariant = Color(0xFF03DAC6),
                onSecondary = Color.Black,
                
                background = Color(0xFF121212),
                onBackground = Color(0xFFE0E0E0),
                surface = Color(0xFF121212),
                onSurface = Color(0xFFE0E0E0),
                
                error = Color(0xFFCF6679),
                onError = Color.Black,
                
                success = Color(0xFF81C784),
                onSuccess = Color.Black,
                
                warning = Color(0xFFFFB74D),
                onWarning = Color.Black,
                
                info = Color(0xFF64B5F6),
                onInfo = Color.Black,
                
                outline = Color(0xFF938F99),
                outlineVariant = Color(0xFF49454F),
                surfaceVariant = Color(0xFF49454F),
                onSurfaceVariant = Color(0xFFCAC4D0),
                
                scrim = Color(0x80000000),
                inverseSurface = Color(0xFFE6E1E5),
                inverseOnSurface = Color(0xFF313033),
                inversePrimary = Color(0xFF6750A4),
                
                disabled = Color(0x61FFFFFF),
                placeholder = Color(0x99FFFFFF),
                divider = Color(0x1FFFFFFF),
                shadow = Color(0x66000000)
            )
        }
    }
}

/**
 * 字体系统
 */
@Immutable
data class UnifyTypography(
    // 显示级别
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    
    // 标题级别
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    
    // 子标题级别
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    
    // 正文级别
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    
    // 标签级别
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle
) {
    companion object {
        fun default(): UnifyTypography {
            return UnifyTypography(
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
                    fontWeight = FontWeight.Normal,
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
        }
    }
}

/**
 * 形状系统
 */
@Immutable
data class UnifyShapes(
    val none: Shape,
    val extraSmall: Shape,
    val small: Shape,
    val medium: Shape,
    val large: Shape,
    val extraLarge: Shape,
    val full: Shape
) {
    companion object {
        fun default(): UnifyShapes {
            return UnifyShapes(
                none = RoundedCornerShape(0.dp),
                extraSmall = RoundedCornerShape(4.dp),
                small = RoundedCornerShape(8.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(16.dp),
                extraLarge = RoundedCornerShape(28.dp),
                full = RoundedCornerShape(50)
            )
        }
    }
}

/**
 * 尺寸系统
 */
@Immutable
data class UnifyDimensions(
    // 间距
    val spaceXs: Dp,
    val spaceSm: Dp,
    val spaceMd: Dp,
    val spaceLg: Dp,
    val spaceXl: Dp,
    val spaceXxl: Dp,
    
    // 组件尺寸
    val buttonHeight: Dp,
    val buttonHeightSmall: Dp,
    val buttonHeightLarge: Dp,
    
    val textFieldHeight: Dp,
    val iconSize: Dp,
    val iconSizeSmall: Dp,
    val iconSizeLarge: Dp,
    
    // 边框
    val borderWidth: Dp,
    val borderWidthThick: Dp,
    
    // 阴影
    val elevationNone: Dp,
    val elevationLow: Dp,
    val elevationMedium: Dp,
    val elevationHigh: Dp
) {
    companion object {
        fun default(): UnifyDimensions {
            return UnifyDimensions(
                spaceXs = 4.dp,
                spaceSm = 8.dp,
                spaceMd = 16.dp,
                spaceLg = 24.dp,
                spaceXl = 32.dp,
                spaceXxl = 48.dp,
                
                buttonHeight = 40.dp,
                buttonHeightSmall = 32.dp,
                buttonHeightLarge = 48.dp,
                
                textFieldHeight = 56.dp,
                iconSize = 24.dp,
                iconSizeSmall = 16.dp,
                iconSizeLarge = 32.dp,
                
                borderWidth = 1.dp,
                borderWidthThick = 2.dp,
                
                elevationNone = 0.dp,
                elevationLow = 2.dp,
                elevationMedium = 4.dp,
                elevationHigh = 8.dp
            )
        }
    }
}

/**
 * 动画系统
 */
@Immutable
data class UnifyAnimations(
    val durationFast: Int,
    val durationMedium: Int,
    val durationSlow: Int,
    val durationExtraSlow: Int
) {
    companion object {
        fun default(): UnifyAnimations {
            return UnifyAnimations(
                durationFast = 150,
                durationMedium = 300,
                durationSlow = 500,
                durationExtraSlow = 1000
            )
        }
    }
}
