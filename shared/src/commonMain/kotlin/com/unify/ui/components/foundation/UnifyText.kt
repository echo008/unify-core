package com.unify.ui.components.foundation

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformType

/**
 * Unify Text 组件
 * 支持多平台适配的统一文本组件，参考 KuiklyUI 设计规范
 */

/**
 * 文本变体枚举
 */
enum class UnifyTextVariant {
    DISPLAY_LARGE,      // 超大显示文本
    DISPLAY_MEDIUM,     // 大显示文本
    DISPLAY_SMALL,      // 小显示文本
    HEADLINE_LARGE,     // 大标题
    HEADLINE_MEDIUM,    // 中标题
    HEADLINE_SMALL,     // 小标题
    TITLE_LARGE,        // 大子标题
    TITLE_MEDIUM,       // 中子标题
    TITLE_SMALL,        // 小子标题
    BODY_LARGE,         // 大正文
    BODY_MEDIUM,        // 中正文
    BODY_SMALL,         // 小正文
    LABEL_LARGE,        // 大标签
    LABEL_MEDIUM,       // 中标签
    LABEL_SMALL,        // 小标签
    CAPTION,            // 说明文字
    OVERLINE            // 上划线文字
}

/**
 * 文本语义类型
 */
enum class UnifyTextSemantic {
    DEFAULT,            // 默认
    PRIMARY,            // 主要文本
    SECONDARY,          // 次要文本
    SUCCESS,            // 成功文本
    WARNING,            // 警告文本
    ERROR,              // 错误文本
    INFO,               // 信息文本
    DISABLED,           // 禁用文本
    PLACEHOLDER         // 占位符文本
}

/**
 * 主要 Unify Text 组件
 */
@Composable
fun UnifyText(
    text: String,
    modifier: Modifier = Modifier,
    variant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    semantic: UnifyTextSemantic = UnifyTextSemantic.DEFAULT,
    color: Color? = null,
    fontSize: TextUnit? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle? = null,
    selectable: Boolean = false,
    accessibilityLabel: String? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取基础样式
    val baseStyle = style ?: getTextStyle(variant, theme)
    
    // 获取语义颜色
    val semanticColor = color ?: getSemanticColor(semantic, theme)
    
    // 优化性能：预计算样式，避免每次重组都计算
    val finalStyle by remember(variant, fontSize, fontWeight, color, theme, platformTheme) {
        mutableStateOf(
            getTextStyle(variant, theme, platformTheme).let { baseStyle ->
                baseStyle.copy(
                    fontSize = fontSize ?: baseStyle.fontSize,
                    fontWeight = fontWeight ?: baseStyle.fontWeight,
                    color = color ?: baseStyle.color
                )
            }
        )
    }
    
    // 构建最终样式
    val adaptedStyle = finalStyle.copy(
        letterSpacing = letterSpacing ?: finalStyle.letterSpacing,
        textDecoration = textDecoration ?: finalStyle.textDecoration,
        textAlign = textAlign ?: finalStyle.textAlign,
        lineHeight = lineHeight ?: finalStyle.lineHeight
    )
    // 平台适配处理
    val adaptedStyle = adaptStyleForPlatform(finalStyle, platformTheme)
    
    val textModifier = modifier.semantics {
        accessibilityLabel?.let { 
            contentDescription = it 
        }
    }
    
    if (selectable) {
        SelectionContainer {
            Text(
                text = text,
                modifier = textModifier,
                style = adaptedStyle,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines
            )
        }
    } else {
        Text(
            text = text,
            modifier = textModifier,
            style = adaptedStyle,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines
        )
    }
}

/**
 * 富文本组件
 */
@Composable
fun UnifyRichText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    variant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    semantic: UnifyTextSemantic = UnifyTextSemantic.DEFAULT,
    color: Color? = null,
    fontSize: TextUnit? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle? = null,
    selectable: Boolean = false,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    accessibilityLabel: String? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    val baseStyle = style ?: getTextStyle(variant, theme)
    val semanticColor = color ?: getSemanticColor(semantic, theme)
    
    val finalStyle = baseStyle.copy(
        color = semanticColor,
        fontSize = fontSize ?: baseStyle.fontSize,
        fontStyle = fontStyle ?: baseStyle.fontStyle,
        fontWeight = fontWeight ?: baseStyle.fontWeight,
        fontFamily = fontFamily ?: baseStyle.fontFamily,
        letterSpacing = letterSpacing ?: baseStyle.letterSpacing,
        textDecoration = textDecoration ?: baseStyle.textDecoration,
        textAlign = textAlign ?: baseStyle.textAlign,
        lineHeight = lineHeight ?: baseStyle.lineHeight
    )
    
    val adaptedStyle = adaptStyleForPlatform(finalStyle, platformTheme)
    
    val textModifier = modifier.semantics {
        accessibilityLabel?.let { 
            contentDescription = it 
        }
    }
    
    if (selectable) {
        SelectionContainer {
            Text(
                text = text,
                modifier = textModifier,
                style = adaptedStyle,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )
        }
    } else {
        Text(
            text = text,
            modifier = textModifier,
            style = adaptedStyle,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout
        )
    }
}

/**
 * 标题文本快捷方式
 */
@Composable
fun UnifyHeadline(
    text: String,
    modifier: Modifier = Modifier,
    level: Int = 1,
    semantic: UnifyTextSemantic = UnifyTextSemantic.PRIMARY,
    color: Color? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    accessibilityLabel: String? = null
) {
    val variant = when (level) {
        1 -> UnifyTextVariant.HEADLINE_LARGE
        2 -> UnifyTextVariant.HEADLINE_MEDIUM
        3 -> UnifyTextVariant.HEADLINE_SMALL
        4 -> UnifyTextVariant.TITLE_LARGE
        5 -> UnifyTextVariant.TITLE_MEDIUM
        else -> UnifyTextVariant.TITLE_SMALL
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        semantic = semantic,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        accessibilityLabel = accessibilityLabel
    )
}

/**
 * 正文文本快捷方式
 */
@Composable
fun UnifyBody(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyTextSize = UnifyTextSize.MEDIUM,
    semantic: UnifyTextSemantic = UnifyTextSemantic.DEFAULT,
    color: Color? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
    accessibilityLabel: String? = null
) {
    val variant = when (size) {
        UnifyTextSize.SMALL -> UnifyTextVariant.BODY_SMALL
        UnifyTextSize.MEDIUM -> UnifyTextVariant.BODY_MEDIUM
        UnifyTextSize.LARGE -> UnifyTextVariant.BODY_LARGE
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        semantic = semantic,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        selectable = selectable,
        accessibilityLabel = accessibilityLabel
    )
}

/**
 * 标签文本快捷方式
 */
@Composable
fun UnifyLabel(
    text: String,
    modifier: Modifier = Modifier,
    size: UnifyTextSize = UnifyTextSize.MEDIUM,
    semantic: UnifyTextSemantic = UnifyTextSemantic.SECONDARY,
    color: Color? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    accessibilityLabel: String? = null
) {
    val variant = when (size) {
        UnifyTextSize.SMALL -> UnifyTextVariant.LABEL_SMALL
        UnifyTextSize.MEDIUM -> UnifyTextVariant.LABEL_MEDIUM
        UnifyTextSize.LARGE -> UnifyTextVariant.LABEL_LARGE
    }
    
    UnifyText(
        text = text,
        modifier = modifier,
        variant = variant,
        semantic = semantic,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        accessibilityLabel = accessibilityLabel
    )
}

/**
 * 说明文字快捷方式
 */
@Composable
fun UnifyCaption(
    text: String,
    modifier: Modifier = Modifier,
    semantic: UnifyTextSemantic = UnifyTextSemantic.SECONDARY,
    color: Color? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    accessibilityLabel: String? = null
) {
    UnifyText(
        text = text,
        modifier = modifier,
        variant = UnifyTextVariant.CAPTION,
        semantic = semantic,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        accessibilityLabel = accessibilityLabel
    )
}

/**
 * 文本尺寸枚举
 */
enum class UnifyTextSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * 获取文本样式
 */
@Composable
private fun getTextStyle(
    variant: UnifyTextVariant,
    theme: com.unify.ui.theme.UnifyTheme
): TextStyle {
    return when (variant) {
        UnifyTextVariant.DISPLAY_LARGE -> theme.typography.displayLarge
        UnifyTextVariant.DISPLAY_MEDIUM -> theme.typography.displayMedium
        UnifyTextVariant.DISPLAY_SMALL -> theme.typography.displaySmall
        UnifyTextVariant.HEADLINE_LARGE -> theme.typography.headlineLarge
        UnifyTextVariant.HEADLINE_MEDIUM -> theme.typography.headlineMedium
        UnifyTextVariant.HEADLINE_SMALL -> theme.typography.headlineSmall
        UnifyTextVariant.TITLE_LARGE -> theme.typography.titleLarge
        UnifyTextVariant.TITLE_MEDIUM -> theme.typography.titleMedium
        UnifyTextVariant.TITLE_SMALL -> theme.typography.titleSmall
        UnifyTextVariant.BODY_LARGE -> theme.typography.bodyLarge
        UnifyTextVariant.BODY_MEDIUM -> theme.typography.bodyMedium
        UnifyTextVariant.BODY_SMALL -> theme.typography.bodySmall
        UnifyTextVariant.LABEL_LARGE -> theme.typography.labelLarge
        UnifyTextVariant.LABEL_MEDIUM -> theme.typography.labelMedium
        UnifyTextVariant.LABEL_SMALL -> theme.typography.labelSmall
        UnifyTextVariant.CAPTION -> theme.typography.bodySmall.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        UnifyTextVariant.OVERLINE -> theme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            textDecoration = TextDecoration.None
        )
    }
}

/**
 * 获取语义颜色
 */
@Composable
private fun getSemanticColor(
    semantic: UnifyTextSemantic,
    theme: com.unify.ui.theme.UnifyTheme
): Color {
    return when (semantic) {
        UnifyTextSemantic.DEFAULT -> theme.colors.onSurface
        UnifyTextSemantic.PRIMARY -> theme.colors.primary
        UnifyTextSemantic.SECONDARY -> theme.colors.onSurface.copy(alpha = 0.7f)
        UnifyTextSemantic.SUCCESS -> theme.colors.success
        UnifyTextSemantic.WARNING -> theme.colors.warning
        UnifyTextSemantic.ERROR -> theme.colors.error
        UnifyTextSemantic.INFO -> theme.colors.info
        UnifyTextSemantic.DISABLED -> theme.colors.disabled
        UnifyTextSemantic.PLACEHOLDER -> theme.colors.placeholder
    }
}

/**
 * 平台样式适配
 */
@Composable
private fun adaptStyleForPlatform(
    style: TextStyle,
    platformTheme: com.unify.ui.platform.UnifyPlatformTheme
): TextStyle {
    return when (platformTheme.platformType) {
        PlatformType.IOS -> {
            // iOS 字体适配
            style.copy(
                fontFamily = FontFamily.Default, // 可以替换为 SF Pro
                letterSpacing = style.letterSpacing * 0.9f // iOS 字间距调整
            )
        }
        PlatformType.ANDROID -> {
            // Android 字体适配
            style.copy(
                fontFamily = FontFamily.Default // 可以替换为 Roboto
            )
        }
        PlatformType.HARMONY -> {
            // HarmonyOS 字体适配
            style.copy(
                fontFamily = FontFamily.Default // 可以替换为 HarmonyOS Sans
            )
        }
        PlatformType.WEB -> {
            // Web 字体适配
            style.copy(
                fontFamily = FontFamily.Default // Web 安全字体
            )
        }
        PlatformType.DESKTOP -> {
            // Desktop 字体适配
            style.copy(
                fontSize = style.fontSize * 0.95f // Desktop 字体稍小
            )
        }
        else -> style
    }
}
