package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * 小程序平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val MINIAPP_FONT_SCALE_FACTOR = 1.0f
        
        fun getMiniAppFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 40f  // 小程序大标题
                UnifyTextVariant.DisplayMedium -> 32f // 小程序中标题
                UnifyTextVariant.DisplaySmall -> 28f  // 小程序小标题
                UnifyTextVariant.HeadlineLarge -> 24f // 小程序主标题
                UnifyTextVariant.HeadlineMedium -> 20f // 小程序副标题
                UnifyTextVariant.HeadlineSmall -> 18f // 小程序小标题
                UnifyTextVariant.TitleLarge -> 18f
                UnifyTextVariant.TitleMedium -> 16f
                UnifyTextVariant.TitleSmall -> 14f
                UnifyTextVariant.LabelLarge -> 14f
                UnifyTextVariant.LabelMedium -> 12f
                UnifyTextVariant.LabelSmall -> 10f
                UnifyTextVariant.Body1 -> 16f        // 小程序正文
                UnifyTextVariant.Body2 -> 14f        // 小程序辅助文字
                UnifyTextVariant.Caption -> 12f      // 小程序说明文字
            }
        }
        
        fun getMiniAppScaledFontSize(baseSizePx: Float): Float {
            // 小程序字体缩放适配
            return baseSizePx * MINIAPP_FONT_SCALE_FACTOR
        }
        
        fun isMiniAppDarkMode(): Boolean {
            // 检查小程序深色模式
            return false // 实际实现需要调用小程序API
        }
        
        fun getMiniAppPlatform(): String {
            // 获取小程序平台类型
            return "wechat" // 实际实现需要检测具体平台
        }
        
        fun getMiniAppSystemInfo(): Map<String, Any> {
            // 获取小程序系统信息
            return mapOf(
                "platform" to "wechat",
                "version" to "1.0.0",
                "screenWidth" to 375,
                "screenHeight" to 667
            )
        }
        
        fun isMiniAppAccessibilityEnabled(): Boolean {
            // 检查小程序无障碍功能
            return false // 实际实现需要调用小程序API
        }
    }
}

/**
 * 小程序平台的原生文本组件适配器
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
    // 获取小程序平台字体大小
    val miniAppFontSize = UnifyPlatformText.getMiniAppFontSize(variant)
    val scaledFontSize = UnifyPlatformText.getMiniAppScaledFontSize(miniAppFontSize)
    
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
