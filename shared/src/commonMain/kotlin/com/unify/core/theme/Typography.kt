package com.unify.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Unify跨平台字体排版系统
 * 支持8大平台的统一字体管理
 */

// 基础字体系列定义
val UnifyFontFamily = FontFamily.Default

// Unify字体排版定义
val UnifyTypography = Typography(
    // 显示级别字体
    displayLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    
    // 标题级别字体
    headlineLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    
    // 标题级别字体
    titleLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    
    // 正文级别字体
    bodyLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    
    // 标签级别字体
    labelLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * 扩展字体样式定义
 */
object UnifyTextStyles {
    // 超大显示字体
    val displayExtraLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 72.sp,
        lineHeight = 80.sp,
        letterSpacing = (-0.5).sp,
    )
    
    // 超小字体
    val labelExtraSmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    )
    
    // 代码字体
    val codeSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    )
    
    val codeMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    )
    
    val codeLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    )
    
    // 特殊用途字体
    val caption = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    )
    
    val overline = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp,
    )
    
    // 按钮字体
    val buttonLarge = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    )
    
    val buttonMedium = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )
    
    val buttonSmall = TextStyle(
        fontFamily = UnifyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
    )
}

/**
 * 响应式字体大小计算
 */
object UnifyResponsiveTypography {
    /**
     * 根据屏幕尺寸调整字体大小
     */
    fun getScaledTextStyle(baseStyle: TextStyle, scaleFactor: Float): TextStyle {
        return baseStyle.copy(
            fontSize = baseStyle.fontSize * scaleFactor,
            lineHeight = baseStyle.lineHeight * scaleFactor
        )
    }
    
    /**
     * 获取平台特定的字体缩放因子
     */
    fun getPlatformFontScale(): Float = 1.0f
    
    /**
     * 获取响应式字体排版
     */
    @Composable
    fun getResponsiveTypography(): Typography {
        val scaleFactor = getPlatformFontScale()
        return Typography(
            displayLarge = getScaledTextStyle(UnifyTypography.displayLarge, scaleFactor),
            displayMedium = getScaledTextStyle(UnifyTypography.displayMedium, scaleFactor),
            displaySmall = getScaledTextStyle(UnifyTypography.displaySmall, scaleFactor),
            headlineLarge = getScaledTextStyle(UnifyTypography.headlineLarge, scaleFactor),
            headlineMedium = getScaledTextStyle(UnifyTypography.headlineMedium, scaleFactor),
            headlineSmall = getScaledTextStyle(UnifyTypography.headlineSmall, scaleFactor),
            titleLarge = getScaledTextStyle(UnifyTypography.titleLarge, scaleFactor),
            titleMedium = getScaledTextStyle(UnifyTypography.titleMedium, scaleFactor),
            titleSmall = getScaledTextStyle(UnifyTypography.titleSmall, scaleFactor),
            bodyLarge = getScaledTextStyle(UnifyTypography.bodyLarge, scaleFactor),
            bodyMedium = getScaledTextStyle(UnifyTypography.bodyMedium, scaleFactor),
            bodySmall = getScaledTextStyle(UnifyTypography.bodySmall, scaleFactor),
            labelLarge = getScaledTextStyle(UnifyTypography.labelLarge, scaleFactor),
            labelMedium = getScaledTextStyle(UnifyTypography.labelMedium, scaleFactor),
            labelSmall = getScaledTextStyle(UnifyTypography.labelSmall, scaleFactor),
        )
    }
}
