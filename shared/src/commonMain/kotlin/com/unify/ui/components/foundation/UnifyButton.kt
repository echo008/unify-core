package com.unify.ui.components.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.platform.FeedbackType
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformFeature

/**
 * Unify Button 组件
 * 支持多平台适配的统一按钮组件，参考 KuiklyUI 设计规范
 */

/**
 * 按钮变体枚举
 */
enum class UnifyButtonVariant {
    PRIMARY,      // 主要按钮
    SECONDARY,    // 次要按钮
    TERTIARY,     // 第三级按钮
    OUTLINE,      // 边框按钮
    TEXT,         // 文本按钮
    ICON,         // 图标按钮
    FAB,          // 浮动操作按钮
    DESTRUCTIVE   // 危险操作按钮
}

/**
 * 按钮尺寸枚举
 */
enum class UnifyButtonSize {
    SMALL,        // 小尺寸 32dp
    MEDIUM,       // 中等尺寸 40dp
    LARGE,        // 大尺寸 48dp
    EXTRA_LARGE   // 超大尺寸 56dp
}

/**
 * 按钮状态枚举
 */
enum class UnifyButtonState {
    ENABLED,      // 启用状态
    DISABLED,     // 禁用状态
    LOADING,      // 加载状态
    PRESSED       // 按下状态
}

/**
 * 主要 Unify Button 组件
 */
@Composable
fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyButtonVariant = UnifyButtonVariant.PRIMARY,
    size: UnifyButtonSize = UnifyButtonSize.MEDIUM,
    state: UnifyButtonState = UnifyButtonState.ENABLED,
    enabled: Boolean = state != UnifyButtonState.DISABLED,
    shape: Shape? = null,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    hapticFeedback: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 根据变体和平台适配样式
    val buttonColors = colors ?: getButtonColors(variant, theme, platformTheme)
    val buttonShape = shape ?: getButtonShape(variant, size, theme)
    val buttonElevation = elevation ?: getButtonElevation(variant, theme)
    val buttonBorder = border ?: getButtonBorder(variant, theme)
    val buttonPadding = contentPadding ?: getButtonPadding(size, theme)
    val buttonHeight = getButtonHeight(size, theme)
    
    // 处理点击事件和触觉反馈 - 优化性能，避免每次重组都创建新函数
    val handleClick by remember(variant, hapticFeedback, platformTheme) {
        mutableStateOf({
            if (hapticFeedback && platformTheme.interactionPatterns.feedbackType == FeedbackType.HAPTIC) {
                // 使用remember保存协程引用，避免重复创建
            }
            onClick()
        })
    }
    
    Button(
        onClick = if (state == UnifyButtonState.LOADING) { {} } else handleClick,
        modifier = modifier
            .height(buttonHeight)
            .semantics { role = Role.Button },
        enabled = enabled && state != UnifyButtonState.LOADING,
        shape = buttonShape,
        colors = buttonColors,
        elevation = buttonElevation,
        border = buttonBorder,
        contentPadding = buttonPadding,
        interactionSource = interactionSource
    ) {
        ButtonContent(
            state = state,
            content = content
        )
    }
}

/**
 * 按钮内容组合
 */
@Composable
private fun RowScope.ButtonContent(
    state: UnifyButtonState,
    content: @Composable RowScope.() -> Unit
) {
    when (state) {
        UnifyButtonState.LOADING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        else -> {}
    }
    
    content()
}

/**
 * 文本按钮快捷方式
 */
@Composable
fun UnifyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyButtonVariant = UnifyButtonVariant.TEXT,
    size: UnifyButtonSize = UnifyButtonSize.MEDIUM,
    state: UnifyButtonState = UnifyButtonState.ENABLED,
    enabled: Boolean = true,
    textStyle: TextStyle? = null
) {
    val theme = LocalUnifyTheme.current
    val finalTextStyle = textStyle ?: getButtonTextStyle(size, theme)
    
    UnifyButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        state = state,
        enabled = enabled
    ) {
        UnifyText(
            text = text,
            style = finalTextStyle
        )
    }
}

/**
 * 图标按钮快捷方式
 */
@Composable
fun UnifyIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyButtonVariant = UnifyButtonVariant.ICON,
    size: UnifyButtonSize = UnifyButtonSize.MEDIUM,
    state: UnifyButtonState = UnifyButtonState.ENABLED,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    UnifyButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        variant = variant,
        size = size,
        state = state,
        enabled = enabled,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.size(getIconSize(size)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

/**
 * 浮动操作按钮
 */
@Composable
fun UnifyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: UnifyButtonSize = UnifyButtonSize.LARGE,
    containerColor: Color? = null,
    contentColor: Color? = null,
    elevation: FloatingActionButtonElevation? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(getFabSize(size)),
        containerColor = containerColor ?: theme.colors.primary,
        contentColor = contentColor ?: theme.colors.onPrimary,
        elevation = elevation,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * 获取按钮颜色
 */
@Composable
private fun getButtonColors(
    variant: UnifyButtonVariant,
    theme: com.unify.ui.theme.UnifyTheme,
    platformTheme: com.unify.ui.platform.UnifyPlatformTheme
): ButtonColors {
    return when (variant) {
        UnifyButtonVariant.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = theme.colors.primary,
            contentColor = theme.colors.onPrimary,
            disabledContainerColor = theme.colors.disabled,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
        UnifyButtonVariant.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = theme.colors.secondary,
            contentColor = theme.colors.onSecondary,
            disabledContainerColor = theme.colors.disabled,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
        UnifyButtonVariant.TERTIARY -> ButtonDefaults.buttonColors(
            containerColor = theme.colors.surfaceVariant,
            contentColor = theme.colors.onSurfaceVariant,
            disabledContainerColor = theme.colors.disabled,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
        UnifyButtonVariant.OUTLINE -> ButtonDefaults.outlinedButtonColors(
            contentColor = theme.colors.primary,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
        UnifyButtonVariant.TEXT -> ButtonDefaults.textButtonColors(
            contentColor = theme.colors.primary,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
        UnifyButtonVariant.ICON -> ButtonDefaults.textButtonColors(
            contentColor = theme.colors.onSurface,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
        UnifyButtonVariant.FAB -> ButtonDefaults.buttonColors(
            containerColor = theme.colors.primary,
            contentColor = theme.colors.onPrimary
        )
        UnifyButtonVariant.DESTRUCTIVE -> ButtonDefaults.buttonColors(
            containerColor = theme.colors.error,
            contentColor = theme.colors.onError,
            disabledContainerColor = theme.colors.disabled,
            disabledContentColor = theme.colors.onSurface.copy(alpha = 0.38f)
        )
    }
}

/**
 * 获取按钮形状
 */
@Composable
private fun getButtonShape(
    variant: UnifyButtonVariant,
    size: UnifyButtonSize,
    theme: com.unify.ui.theme.UnifyTheme
): Shape {
    return when (variant) {
        UnifyButtonVariant.FAB -> theme.shapes.large
        UnifyButtonVariant.ICON -> theme.shapes.full
        else -> when (size) {
            UnifyButtonSize.SMALL -> theme.shapes.small
            UnifyButtonSize.MEDIUM -> theme.shapes.medium
            UnifyButtonSize.LARGE -> theme.shapes.medium
            UnifyButtonSize.EXTRA_LARGE -> theme.shapes.large
        }
    }
}

/**
 * 获取按钮高度
 */
@Composable
private fun getButtonHeight(
    size: UnifyButtonSize,
    theme: com.unify.ui.theme.UnifyTheme
): Dp {
    return when (size) {
        UnifyButtonSize.SMALL -> theme.dimensions.buttonHeightSmall
        UnifyButtonSize.MEDIUM -> theme.dimensions.buttonHeight
        UnifyButtonSize.LARGE -> theme.dimensions.buttonHeightLarge
        UnifyButtonSize.EXTRA_LARGE -> 56.dp
    }
}

/**
 * 获取按钮内边距
 */
@Composable
private fun getButtonPadding(
    size: UnifyButtonSize,
    theme: com.unify.ui.theme.UnifyTheme
): PaddingValues {
    return when (size) {
        UnifyButtonSize.SMALL -> PaddingValues(horizontal = theme.dimensions.spaceSm)
        UnifyButtonSize.MEDIUM -> PaddingValues(horizontal = theme.dimensions.spaceMd)
        UnifyButtonSize.LARGE -> PaddingValues(horizontal = theme.dimensions.spaceLg)
        UnifyButtonSize.EXTRA_LARGE -> PaddingValues(horizontal = theme.dimensions.spaceXl)
    }
}

/**
 * 获取按钮文本样式
 */
@Composable
private fun getButtonTextStyle(
    size: UnifyButtonSize,
    theme: com.unify.ui.theme.UnifyTheme
): TextStyle {
    return when (size) {
        UnifyButtonSize.SMALL -> theme.typography.labelMedium
        UnifyButtonSize.MEDIUM -> theme.typography.labelLarge
        UnifyButtonSize.LARGE -> theme.typography.titleMedium
        UnifyButtonSize.EXTRA_LARGE -> theme.typography.titleLarge
    }
}

/**
 * 获取按钮阴影
 */
@Composable
private fun getButtonElevation(
    variant: UnifyButtonVariant,
    theme: com.unify.ui.theme.UnifyTheme
): ButtonElevation? {
    return when (variant) {
        UnifyButtonVariant.PRIMARY,
        UnifyButtonVariant.SECONDARY,
        UnifyButtonVariant.DESTRUCTIVE -> ButtonDefaults.buttonElevation(
            defaultElevation = theme.dimensions.elevationLow
        )
        UnifyButtonVariant.FAB -> ButtonDefaults.buttonElevation(
            defaultElevation = theme.dimensions.elevationMedium
        )
        else -> null
    }
}

/**
 * 获取按钮边框
 */
@Composable
private fun getButtonBorder(
    variant: UnifyButtonVariant,
    theme: com.unify.ui.theme.UnifyTheme
): BorderStroke? {
    return when (variant) {
        UnifyButtonVariant.OUTLINE -> BorderStroke(
            width = theme.dimensions.borderWidth,
            color = theme.colors.outline
        )
        else -> null
    }
}

/**
 * 获取图标尺寸
 */
private fun getIconSize(size: UnifyButtonSize): Dp {
    return when (size) {
        UnifyButtonSize.SMALL -> 16.dp
        UnifyButtonSize.MEDIUM -> 24.dp
        UnifyButtonSize.LARGE -> 28.dp
        UnifyButtonSize.EXTRA_LARGE -> 32.dp
    }
}

/**
 * 获取 FAB 尺寸
 */
private fun getFabSize(size: UnifyButtonSize): Dp {
    return when (size) {
        UnifyButtonSize.SMALL -> 40.dp
        UnifyButtonSize.MEDIUM -> 56.dp
        UnifyButtonSize.LARGE -> 64.dp
        UnifyButtonSize.EXTRA_LARGE -> 72.dp
    }
}
