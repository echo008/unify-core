package com.unify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.theme.UnifyColors

/**
 * 统一按钮组件
 * 跨平台一致的按钮实现，支持多种样式和状态
 */
@Composable
fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: UnifyButtonVariant = UnifyButtonVariant.Primary,
    size: UnifyButtonSize = UnifyButtonSize.Medium,
    shape: Shape? = null,
    colors: UnifyButtonColors? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val buttonColors = colors ?: UnifyButtonDefaults.colors(variant, theme.colors)
    val buttonShape = shape ?: UnifyButtonDefaults.shape(size)
    val buttonPadding = contentPadding ?: UnifyButtonDefaults.contentPadding(size)
    
    when (variant) {
        UnifyButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = border,
                contentPadding = buttonPadding,
                interactionSource = interactionSource,
                content = content
            )
        }
        UnifyButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = OutlinedButtonDefaults.buttonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = border ?: BorderStroke(1.dp, buttonColors.contentColor),
                contentPadding = buttonPadding,
                interactionSource = interactionSource,
                content = content
            )
        }
        UnifyButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = TextButtonDefaults.textButtonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = border,
                contentPadding = buttonPadding,
                interactionSource = interactionSource,
                content = content
            )
        }
        UnifyButtonVariant.Elevated -> {
            ElevatedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = ElevatedButtonDefaults.elevatedButtonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = border,
                contentPadding = buttonPadding,
                interactionSource = interactionSource,
                content = content
            )
        }
        UnifyButtonVariant.Tonal -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = FilledTonalButtonDefaults.filledTonalButtonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                border = border,
                contentPadding = buttonPadding,
                interactionSource = interactionSource,
                content = content
            )
        }
    }
}

/**
 * 按钮变体枚举
 */
enum class UnifyButtonVariant {
    Primary,    // 主要按钮
    Secondary,  // 次要按钮
    Text,       // 文本按钮
    Elevated,   // 浮动按钮
    Tonal       // 色调按钮
}

/**
 * 按钮尺寸枚举
 */
enum class UnifyButtonSize {
    Small,      // 小尺寸
    Medium,     // 中等尺寸
    Large       // 大尺寸
}

/**
 * 按钮颜色配置
 */
data class UnifyButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
)

/**
 * 按钮默认配置
 */
object UnifyButtonDefaults {
    
    /**
     * 获取按钮颜色配置
     */
    @Composable
    fun colors(
        variant: UnifyButtonVariant,
        colors: UnifyColors
    ): UnifyButtonColors {
        return when (variant) {
            UnifyButtonVariant.Primary -> UnifyButtonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.primary.copy(alpha = 0.12f),
                disabledContentColor = colors.onSurface.copy(alpha = 0.38f)
            )
            UnifyButtonVariant.Secondary -> UnifyButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.primary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.onSurface.copy(alpha = 0.38f)
            )
            UnifyButtonVariant.Text -> UnifyButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.primary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.onSurface.copy(alpha = 0.38f)
            )
            UnifyButtonVariant.Elevated -> UnifyButtonColors(
                containerColor = colors.surface,
                contentColor = colors.primary,
                disabledContainerColor = colors.onSurface.copy(alpha = 0.12f),
                disabledContentColor = colors.onSurface.copy(alpha = 0.38f)
            )
            UnifyButtonVariant.Tonal -> UnifyButtonColors(
                containerColor = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
                disabledContainerColor = colors.onSurface.copy(alpha = 0.12f),
                disabledContentColor = colors.onSurface.copy(alpha = 0.38f)
            )
        }
    }
    
    /**
     * 获取按钮形状
     */
    fun shape(size: UnifyButtonSize): Shape {
        return when (size) {
            UnifyButtonSize.Small -> RoundedCornerShape(16.dp)
            UnifyButtonSize.Medium -> RoundedCornerShape(20.dp)
            UnifyButtonSize.Large -> RoundedCornerShape(24.dp)
        }
    }
    
    /**
     * 获取按钮内边距
     */
    fun contentPadding(size: UnifyButtonSize): PaddingValues {
        return when (size) {
            UnifyButtonSize.Small -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            UnifyButtonSize.Medium -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            UnifyButtonSize.Large -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        }
    }
}

/**
 * 平台特定的按钮实现
 * 使用expect/actual机制适配不同平台的原生体验
 */
expect class UnifyPlatformButton

/**
 * 按钮组件的平台适配器
 */
@Composable
expect fun UnifyNativeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: UnifyButtonVariant = UnifyButtonVariant.Primary,
    size: UnifyButtonSize = UnifyButtonSize.Medium,
    content: @Composable RowScope.() -> Unit
)
