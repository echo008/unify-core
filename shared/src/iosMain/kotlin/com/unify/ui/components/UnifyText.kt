package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * iOS平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val IOS_FONT_SCALE_FACTOR = 1.0f
        
        fun getSystemFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 34f  // iOS Large Title
                UnifyTextVariant.DisplayMedium -> 28f // iOS Title 1
                UnifyTextVariant.DisplaySmall -> 22f  // iOS Title 2
                UnifyTextVariant.HeadlineLarge -> 20f // iOS Title 3
                UnifyTextVariant.HeadlineMedium -> 17f // iOS Headline
                UnifyTextVariant.HeadlineSmall -> 16f // iOS Body
                UnifyTextVariant.TitleLarge -> 17f
                UnifyTextVariant.TitleMedium -> 16f
                UnifyTextVariant.TitleSmall -> 15f
                UnifyTextVariant.LabelLarge -> 17f
                UnifyTextVariant.LabelMedium -> 15f
                UnifyTextVariant.LabelSmall -> 13f
                UnifyTextVariant.Body1 -> 17f        // iOS Body
                UnifyTextVariant.Body2 -> 15f        // iOS Callout
                UnifyTextVariant.Caption -> 12f      // iOS Caption 1
            }
        }
        
        fun getAccessibilityFontSize(baseSizePt: Float): Float {
            // iOS动态字体支持
            return baseSizePt * IOS_FONT_SCALE_FACTOR
        }
        
        fun isVoiceOverEnabled(): Boolean {
            // 检查VoiceOver是否启用
            return false // 实际实现需要调用iOS API
        }
        
        fun getBoldTextEnabled(): Boolean {
            // 检查粗体文本是否启用
            return false // 实际实现需要调用iOS API
        }
    }
}

/**
 * iOS平台的原生文本组件适配器
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
    // 获取iOS系统字体大小
    val systemFontSize = UnifyPlatformText.getSystemFontSize(variant)
    val accessibilityFontSize = UnifyPlatformText.getAccessibilityFontSize(systemFontSize)
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = accessibilityFontSize.sp,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
