package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * HarmonyOS平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val HARMONY_FONT_SCALE_FACTOR = 1.0f
        
        fun getHarmonyFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 57f  // HarmonyOS Display Large
                UnifyTextVariant.DisplayMedium -> 45f // HarmonyOS Display Medium
                UnifyTextVariant.DisplaySmall -> 36f  // HarmonyOS Display Small
                UnifyTextVariant.HeadlineLarge -> 32f // HarmonyOS Headline Large
                UnifyTextVariant.HeadlineMedium -> 28f // HarmonyOS Headline Medium
                UnifyTextVariant.HeadlineSmall -> 24f // HarmonyOS Headline Small
                UnifyTextVariant.TitleLarge -> 22f
                UnifyTextVariant.TitleMedium -> 16f
                UnifyTextVariant.TitleSmall -> 14f
                UnifyTextVariant.LabelLarge -> 14f
                UnifyTextVariant.LabelMedium -> 12f
                UnifyTextVariant.LabelSmall -> 11f
                UnifyTextVariant.Body1 -> 16f        // HarmonyOS Body Large
                UnifyTextVariant.Body2 -> 14f        // HarmonyOS Body Medium
                UnifyTextVariant.Caption -> 12f      // HarmonyOS Body Small
            }
        }
        
        fun getAccessibilityScaledFontSize(baseSizeSp: Float): Float {
            // HarmonyOS无障碍字体缩放
            return baseSizeSp * HARMONY_FONT_SCALE_FACTOR
        }
        
        fun isHarmonyDarkMode(): Boolean {
            // 检查HarmonyOS深色模式
            return false // 实际实现需要调用HarmonyOS API
        }
        
        fun getHarmonySystemFontFamily(): String {
            return "HarmonyOS Sans"
        }
        
        fun isHarmonyLargeFont(): Boolean {
            // 检查HarmonyOS大字体设置
            return false // 实际实现需要调用HarmonyOS API
        }
        
        fun getHarmonyDeviceType(): String {
            // 获取HarmonyOS设备类型
            return "phone" // 实际实现需要调用HarmonyOS API
        }
    }
}

/**
 * HarmonyOS平台的原生文本组件适配器
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
    // 获取HarmonyOS平台字体大小
    val harmonyFontSize = UnifyPlatformText.getHarmonyFontSize(variant)
    val accessibilityFontSize = UnifyPlatformText.getAccessibilityScaledFontSize(harmonyFontSize)
    
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
