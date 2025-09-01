package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * TV平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val TV_FONT_SCALE_FACTOR = 1.2f
        
        fun getTVFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 72f  // TV大标题
                UnifyTextVariant.DisplayMedium -> 56f // TV中标题
                UnifyTextVariant.DisplaySmall -> 44f  // TV小标题
                UnifyTextVariant.HeadlineLarge -> 40f // TV主标题
                UnifyTextVariant.HeadlineMedium -> 32f // TV副标题
                UnifyTextVariant.HeadlineSmall -> 28f // TV小标题
                UnifyTextVariant.TitleLarge -> 26f
                UnifyTextVariant.TitleMedium -> 20f
                UnifyTextVariant.TitleSmall -> 18f
                UnifyTextVariant.LabelLarge -> 18f
                UnifyTextVariant.LabelMedium -> 16f
                UnifyTextVariant.LabelSmall -> 14f
                UnifyTextVariant.Body1 -> 20f        // TV正文
                UnifyTextVariant.Body2 -> 18f        // TV辅助文字
                UnifyTextVariant.Caption -> 16f      // TV说明文字
            }
        }
        
        fun getTVScaledFontSize(baseSizeSp: Float): Float {
            // TV字体缩放适配大屏幕
            return baseSizeSp * TV_FONT_SCALE_FACTOR
        }
        
        fun isTVFocused(): Boolean {
            // 检查TV是否获得焦点
            return false // 实际实现需要调用TV API
        }
        
        fun getTVScreenSize(): Pair<Int, Int> {
            // 获取TV屏幕尺寸
            return Pair(1920, 1080) // 实际实现需要调用TV API
        }
        
        fun getTVViewingDistance(): Float {
            // 获取TV观看距离
            return 3.0f // 实际实现需要调用TV API或传感器
        }
        
        fun isTVRemoteConnected(): Boolean {
            // 检查TV遥控器是否连接
            return true // 实际实现需要调用TV API
        }
        
        fun getTVDisplayMode(): String {
            // 获取TV显示模式
            return "standard" // 实际实现需要调用TV API
        }
        
        fun isTVHighContrastMode(): Boolean {
            // 检查TV高对比度模式
            return false // 实际实现需要调用TV API
        }
    }
}

/**
 * TV平台的原生文本组件适配器
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
    // 获取TV平台字体大小
    val tvFontSize = UnifyPlatformText.getTVFontSize(variant)
    val scaledFontSize = UnifyPlatformText.getTVScaledFontSize(tvFontSize)
    
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
