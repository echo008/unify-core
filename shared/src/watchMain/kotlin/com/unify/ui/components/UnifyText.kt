package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Watch平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val WATCH_FONT_SCALE_FACTOR = 0.8f
        
        fun getWatchFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 24f  // Watch大标题
                UnifyTextVariant.DisplayMedium -> 20f // Watch中标题
                UnifyTextVariant.DisplaySmall -> 18f  // Watch小标题
                UnifyTextVariant.HeadlineLarge -> 16f // Watch主标题
                UnifyTextVariant.HeadlineMedium -> 14f // Watch副标题
                UnifyTextVariant.HeadlineSmall -> 12f // Watch小标题
                UnifyTextVariant.TitleLarge -> 14f
                UnifyTextVariant.TitleMedium -> 12f
                UnifyTextVariant.TitleSmall -> 10f
                UnifyTextVariant.LabelLarge -> 10f
                UnifyTextVariant.LabelMedium -> 9f
                UnifyTextVariant.LabelSmall -> 8f
                UnifyTextVariant.Body1 -> 12f        // Watch正文
                UnifyTextVariant.Body2 -> 10f        // Watch辅助文字
                UnifyTextVariant.Caption -> 8f       // Watch说明文字
            }
        }
        
        fun getWatchScaledFontSize(baseSizeSp: Float): Float {
            // Watch字体缩放适配小屏幕
            return baseSizeSp * WATCH_FONT_SCALE_FACTOR
        }
        
        fun isWatchAlwaysOn(): Boolean {
            // 检查Watch是否为常亮模式
            return false // 实际实现需要调用Watch API
        }
        
        fun getWatchScreenSize(): Pair<Int, Int> {
            // 获取Watch屏幕尺寸
            return Pair(390, 390) // 实际实现需要调用Watch API
        }
        
        fun isWatchCrownRotating(): Boolean {
            // 检查Watch数字表冠是否在旋转
            return false // 实际实现需要调用Watch API
        }
        
        fun getWatchBatteryLevel(): Float {
            // 获取Watch电量
            return 1.0f // 实际实现需要调用Watch API
        }
        
        fun isWatchInTheaterMode(): Boolean {
            // 检查Watch是否为剧院模式
            return false // 实际实现需要调用Watch API
        }
    }
}

/**
 * Watch平台的原生文本组件适配器
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
    // 获取Watch平台字体大小
    val watchFontSize = UnifyPlatformText.getWatchFontSize(variant)
    val scaledFontSize = UnifyPlatformText.getWatchScaledFontSize(watchFontSize)
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = scaledFontSize.sp,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
