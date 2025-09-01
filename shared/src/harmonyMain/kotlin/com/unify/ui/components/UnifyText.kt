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
        // HarmonyOS字体缩放常量
        private const val HARMONY_FONT_SCALE_FACTOR = 1.0f
        
        // HarmonyOS字体大小常量
        private const val DISPLAY_LARGE_SIZE = 57f
        private const val DISPLAY_MEDIUM_SIZE = 45f
        private const val DISPLAY_SMALL_SIZE = 36f
        private const val HEADLINE_LARGE_SIZE = 32f
        private const val HEADLINE_MEDIUM_SIZE = 28f
        private const val HEADLINE_SMALL_SIZE = 24f
        private const val TITLE_LARGE_SIZE = 22f
        private const val TITLE_MEDIUM_SIZE = 16f
        private const val TITLE_SMALL_SIZE = 14f
        private const val LABEL_LARGE_SIZE = 14f
        private const val LABEL_MEDIUM_SIZE = 12f
        private const val LABEL_SMALL_SIZE = 11f
        private const val BODY_LARGE_SIZE = 16f
        private const val BODY_MEDIUM_SIZE = 14f
        private const val BODY_SMALL_SIZE = 12f
        
        fun getHarmonyFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> DISPLAY_LARGE_SIZE
                UnifyTextVariant.DisplayMedium -> DISPLAY_MEDIUM_SIZE
                UnifyTextVariant.DisplaySmall -> DISPLAY_SMALL_SIZE
                UnifyTextVariant.HeadlineLarge -> HEADLINE_LARGE_SIZE
                UnifyTextVariant.HeadlineMedium -> HEADLINE_MEDIUM_SIZE
                UnifyTextVariant.HeadlineSmall -> HEADLINE_SMALL_SIZE
                UnifyTextVariant.TitleLarge -> TITLE_LARGE_SIZE
                UnifyTextVariant.TitleMedium -> TITLE_MEDIUM_SIZE
                UnifyTextVariant.TitleSmall -> TITLE_SMALL_SIZE
                UnifyTextVariant.LabelLarge -> LABEL_LARGE_SIZE
                UnifyTextVariant.LabelMedium -> LABEL_MEDIUM_SIZE
                UnifyTextVariant.LabelSmall -> LABEL_SMALL_SIZE
                UnifyTextVariant.Body1 -> BODY_LARGE_SIZE
                UnifyTextVariant.Body2 -> BODY_MEDIUM_SIZE
                UnifyTextVariant.Caption -> BODY_SMALL_SIZE
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
