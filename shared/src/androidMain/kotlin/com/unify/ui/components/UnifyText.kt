package com.unify.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import android.content.Context
import android.util.TypedValue

/**
 * Android平台的文本实现
 */
actual class UnifyPlatformText {
    companion object {
        private const val DESKTOP_FONT_SCALE_FACTOR = 1.0f
        
        fun getScaledTextSize(context: Context, baseSizeSp: Float): Float {
            val fontScale = context.resources.configuration.fontScale
            return baseSizeSp * fontScale * DESKTOP_FONT_SCALE_FACTOR
        }
        
        fun isLargeText(context: Context): Boolean {
            return context.resources.configuration.fontScale > 1.3f
        }
        
        fun getAccessibilityTextSize(context: Context, baseSizeSp: Float): Float {
            val fontScale = context.resources.configuration.fontScale
            return when {
                fontScale >= 2.0f -> baseSizeSp * 2.0f
                fontScale >= 1.5f -> baseSizeSp * 1.5f
                fontScale >= 1.3f -> baseSizeSp * 1.3f
                else -> baseSizeSp
            }
        }
    }
}

/**
 * Android平台的原生文本组件适配器
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
    val context = LocalContext.current
    
    // 根据变体获取基础字体大小
    val baseFontSize = when (variant) {
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
    
    // 应用Android系统字体缩放
    val scaledFontSize = UnifyPlatformText.getAccessibilityTextSize(context, baseFontSize)
    
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
