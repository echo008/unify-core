package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Desktop平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val DESKTOP_FONT_SCALE_FACTOR = 1.0f
        private const val DESKTOP_BASE_DPI = 96f
        
        fun getDesktopFontSize(variant: UnifyTextVariant): Float {
            return when (variant) {
                UnifyTextVariant.DisplayLarge -> 57f
                UnifyTextVariant.DisplayMedium -> 45f
                UnifyTextVariant.DisplaySmall -> 36f
                UnifyTextVariant.HeadlineLarge -> 32f
                UnifyTextVariant.HeadlineMedium -> 28f
                UnifyTextVariant.HeadlineSmall -> 24f
                UnifyTextVariant.TitleLarge -> 22f
                UnifyTextVariant.TitleMedium -> 16f
                UnifyTextVariant.TitleSmall -> 14f
                UnifyTextVariant.LabelLarge -> 14f
                UnifyTextVariant.LabelMedium -> 12f
                UnifyTextVariant.LabelSmall -> 11f
                UnifyTextVariant.Body1 -> 16f
                UnifyTextVariant.Body2 -> 14f
                UnifyTextVariant.Caption -> 12f
            }
        }
        
        fun getDpiScaledFontSize(baseSizeSp: Float): Float {
            // 根据系统DPI缩放字体大小
            val systemDpi = getSystemDpi()
            val dpiScale = systemDpi / DESKTOP_BASE_DPI
            return baseSizeSp * dpiScale * DESKTOP_FONT_SCALE_FACTOR
        }
        
        private fun getSystemDpi(): Float {
            // 获取系统DPI，实际实现需要调用系统API
            return DESKTOP_BASE_DPI
        }
        
        fun isHighDpiDisplay(): Boolean {
            return getSystemDpi() > 120f
        }
        
        fun getSystemFontFamily(): String {
            val osName = System.getProperty("os.name").lowercase()
            return when {
                osName.contains("windows") -> "Segoe UI"
                osName.contains("mac") -> "SF Pro Display"
                osName.contains("linux") -> "Ubuntu"
                else -> "System UI"
            }
        }
        
        fun isSystemDarkMode(): Boolean {
            // 检查系统是否为深色模式
            return false // 实际实现需要调用系统API
        }
    }
}

/**
 * Desktop平台的原生文本组件适配器
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
    // 获取Desktop平台字体大小
    val desktopFontSize = UnifyPlatformText.getDesktopFontSize(variant)
    val dpiScaledFontSize = UnifyPlatformText.getDpiScaledFontSize(desktopFontSize)
    
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = dpiScaledFontSize.sp,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
