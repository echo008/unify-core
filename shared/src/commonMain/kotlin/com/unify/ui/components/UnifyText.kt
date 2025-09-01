package com.unify.ui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.theme.UnifyTypography

/**
 * 统一文本组件
 * 跨平台一致的文本显示组件，支持多种样式和排版
 */
@Composable
fun UnifyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    variant: UnifyTextVariant = UnifyTextVariant.Body1,
    selectable: Boolean = false,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    val textStyle = getTextStyleForVariant(variant, theme.typography)
    val finalStyle = textStyle.merge(style).copy(
        color = if (color != Color.Unspecified) color else textStyle.color,
        fontSize = if (fontSize != TextUnit.Unspecified) fontSize else textStyle.fontSize,
        fontStyle = fontStyle ?: textStyle.fontStyle,
        fontWeight = fontWeight ?: textStyle.fontWeight,
        fontFamily = fontFamily ?: textStyle.fontFamily,
        letterSpacing = if (letterSpacing != TextUnit.Unspecified) letterSpacing else textStyle.letterSpacing,
        textDecoration = textDecoration ?: textStyle.textDecoration,
        textAlign = textAlign ?: textStyle.textAlign,
        lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight else textStyle.lineHeight
    )
    
    val textContent = @Composable {
        Text(
            text = text,
            modifier = modifier,
            style = finalStyle,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout
        )
    }
    
    if (selectable) {
        SelectionContainer {
            textContent()
        }
    } else {
        textContent()
    }
}

/**
 * 文本变体枚举
 */
enum class UnifyTextVariant {
    DisplayLarge,     // 显示文本 - 大
    DisplayMedium,    // 显示文本 - 中
    DisplaySmall,     // 显示文本 - 小
    HeadlineLarge,    // 标题 - 大
    HeadlineMedium,   // 标题 - 中
    HeadlineSmall,    // 标题 - 小
    TitleLarge,       // 题目 - 大
    TitleMedium,      // 题目 - 中
    TitleSmall,       // 题目 - 小
    LabelLarge,       // 标签 - 大
    LabelMedium,      // 标签 - 中
    LabelSmall,       // 标签 - 小
    Body1,            // 正文 - 大
    Body2,            // 正文 - 小
    Caption           // 说明文字
}

/**
 * 获取文本变体对应的样式
 */
@Composable
private fun getTextStyleForVariant(
    variant: UnifyTextVariant,
    typography: UnifyTypography
): TextStyle {
    return when (variant) {
        UnifyTextVariant.DisplayLarge -> typography.displayLarge
        UnifyTextVariant.DisplayMedium -> typography.displayMedium
        UnifyTextVariant.DisplaySmall -> typography.displaySmall
        UnifyTextVariant.HeadlineLarge -> typography.headlineLarge
        UnifyTextVariant.HeadlineMedium -> typography.headlineMedium
        UnifyTextVariant.HeadlineSmall -> typography.headlineSmall
        UnifyTextVariant.TitleLarge -> typography.titleLarge
        UnifyTextVariant.TitleMedium -> typography.titleMedium
        UnifyTextVariant.TitleSmall -> typography.titleSmall
        UnifyTextVariant.LabelLarge -> typography.labelLarge
        UnifyTextVariant.LabelMedium -> typography.labelMedium
        UnifyTextVariant.LabelSmall -> typography.labelSmall
        UnifyTextVariant.Body1 -> typography.bodyLarge
        UnifyTextVariant.Body2 -> typography.bodyMedium
        UnifyTextVariant.Caption -> typography.bodySmall
    }
}

/**
 * 便捷的文本组件变体
 */
@Composable
fun UnifyDisplayText(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyDisplaySize = UnifyDisplaySize.Medium,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val variant = when (size) {
        UnifyDisplaySize.Large -> UnifyTextVariant.DisplayLarge
        UnifyDisplaySize.Medium -> UnifyTextVariant.DisplayMedium
        UnifyDisplaySize.Small -> UnifyTextVariant.DisplaySmall
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun UnifyHeadlineText(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyHeadlineSize = UnifyHeadlineSize.Medium,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val variant = when (size) {
        UnifyHeadlineSize.Large -> UnifyTextVariant.HeadlineLarge
        UnifyHeadlineSize.Medium -> UnifyTextVariant.HeadlineMedium
        UnifyHeadlineSize.Small -> UnifyTextVariant.HeadlineSmall
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun UnifyTitleText(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyTitleSize = UnifyTitleSize.Medium,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val variant = when (size) {
        UnifyTitleSize.Large -> UnifyTextVariant.TitleLarge
        UnifyTitleSize.Medium -> UnifyTextVariant.TitleMedium
        UnifyTitleSize.Small -> UnifyTextVariant.TitleSmall
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun UnifyBodyText(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyBodySize = UnifyBodySize.Large,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    selectable: Boolean = false
) {
    val variant = when (size) {
        UnifyBodySize.Large -> UnifyTextVariant.Body1
        UnifyBodySize.Medium -> UnifyTextVariant.Body2
        UnifyBodySize.Small -> UnifyTextVariant.Caption
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        selectable = selectable
    )
}

@Composable
fun UnifyLabelText(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyLabelSize = UnifyLabelSize.Medium,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val variant = when (size) {
        UnifyLabelSize.Large -> UnifyTextVariant.LabelLarge
        UnifyLabelSize.Medium -> UnifyTextVariant.LabelMedium
        UnifyLabelSize.Small -> UnifyTextVariant.LabelSmall
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * 文本尺寸枚举
 */
enum class UnifyDisplaySize { Large, Medium, Small }
enum class UnifyHeadlineSize { Large, Medium, Small }
enum class UnifyTitleSize { Large, Medium, Small }
enum class UnifyBodySize { Large, Medium, Small }
enum class UnifyLabelSize { Large, Medium, Small }

/**
 * 平台特定的文本实现
 */
expect class UnifyPlatformText

/**
 * 文本组件的平台适配器
 */
@Composable
expect fun UnifyNativeText(
    text: String,
    modifier: Modifier = Modifier,
    variant: UnifyTextVariant = UnifyTextVariant.Body1,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
)
