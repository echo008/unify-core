package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Web平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val WEB_BASE_FONT_SIZE = 16f
        private const val WEB_FONT_SCALE_FACTOR = 1.0f
        
        fun getWebFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 3.5f * WEB_BASE_FONT_SIZE  // 56px
                UnifyTextVariant.DisplayMedium -> 2.8f * WEB_BASE_FONT_SIZE // 45px
                UnifyTextVariant.DisplaySmall -> 2.25f * WEB_BASE_FONT_SIZE // 36px
                UnifyTextVariant.HeadlineLarge -> 2.0f * WEB_BASE_FONT_SIZE // 32px
                UnifyTextVariant.HeadlineMedium -> 1.75f * WEB_BASE_FONT_SIZE // 28px
                UnifyTextVariant.HeadlineSmall -> 1.5f * WEB_BASE_FONT_SIZE // 24px
                UnifyTextVariant.TitleLarge -> 1.375f * WEB_BASE_FONT_SIZE // 22px
                UnifyTextVariant.TitleMedium -> 1.0f * WEB_BASE_FONT_SIZE   // 16px
                UnifyTextVariant.TitleSmall -> 0.875f * WEB_BASE_FONT_SIZE  // 14px
                UnifyTextVariant.LabelLarge -> 0.875f * WEB_BASE_FONT_SIZE  // 14px
                UnifyTextVariant.LabelMedium -> 0.75f * WEB_BASE_FONT_SIZE  // 12px
                UnifyTextVariant.LabelSmall -> 0.6875f * WEB_BASE_FONT_SIZE // 11px
                UnifyTextVariant.Body1 -> 1.0f * WEB_BASE_FONT_SIZE         // 16px
                UnifyTextVariant.Body2 -> 0.875f * WEB_BASE_FONT_SIZE       // 14px
                UnifyTextVariant.Caption -> 0.75f * WEB_BASE_FONT_SIZE      // 12px
            }
        }
        
        fun getResponsiveFontSize(baseSizePx: Float): Float {
            // 响应式字体大小调整
            return baseSizePx * WEB_FONT_SCALE_FACTOR
        }
        
        fun isHighContrastMode(): Boolean {
            // 检查是否启用高对比度模式
            return false // 实际实现需要检查CSS媒体查询
        }
        
        fun isPrefersReducedMotion(): Boolean {
            // 检查是否偏好减少动画
            return false // 实际实现需要检查CSS媒体查询
        }
        
        fun getSystemFontStack(): String {
            return "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif"
        }
    }
}

/**
 * Web平台的原生文本组件适配器
 */
@Composable
actual fun UnifyNativeText(
    text: String,
    modifier: Modifier,
    variant: UnifyTextVariant,
    color: Color,
    textAlign: TextAlign?,
    maxLines: Int,
    overflow: TextOverflow
) {
    // 获取Web平台字体大小
    val webFontSize = UnifyPlatformText.getWebFontSize(variant)
    val responsiveFontSize = UnifyPlatformText.getResponsiveFontSize(webFontSize)
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = responsiveFontSize.sp,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
