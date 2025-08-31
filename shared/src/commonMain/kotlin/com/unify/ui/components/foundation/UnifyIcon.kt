package com.unify.ui.components.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme

/**
 * Unify Icon 组件
 * 支持多平台适配的统一图标组件，参考 KuiklyUI 设计规范
 */

/**
 * 图标变体枚举
 */
enum class UnifyIconVariant {
    STANDARD,       // 标准图标
    OUTLINED,       // 轮廓图标
    FILLED,         // 填充图标
    ROUNDED,        // 圆角图标
    SHARP,          // 尖锐图标
    TWO_TONE        // 双色图标
}

/**
 * 图标尺寸枚举
 */
enum class UnifyIconSize {
    EXTRA_SMALL,    // 12dp
    SMALL,          // 16dp
    MEDIUM,         // 24dp
    LARGE,          // 32dp
    EXTRA_LARGE,    // 48dp
    CUSTOM          // 自定义尺寸
}

/**
 * 图标语义类型
 */
enum class UnifyIconSemantic {
    DEFAULT,        // 默认
    PRIMARY,        // 主要
    SECONDARY,      // 次要
    SUCCESS,        // 成功
    WARNING,        // 警告
    ERROR,          // 错误
    INFO,           // 信息
    DISABLED        // 禁用
}

/**
 * 主要 Unify Icon 组件 - ImageVector 版本
 */
@Composable
fun UnifyIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: UnifyIconVariant = UnifyIconVariant.STANDARD,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    semantic: UnifyIconSemantic = UnifyIconSemantic.DEFAULT,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    background: Color? = null,
    backgroundShape: androidx.compose.ui.graphics.Shape? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取图标尺寸
    val iconSize = customSize ?: getIconSize(size, theme)
    
    // 获取语义颜色
    val iconTint = tint ?: getSemanticColor(semantic, theme)
    
    // 构建修饰符
    val iconModifier = modifier
        .size(iconSize)
        .let { mod ->
            if (background != null) {
                val shape = backgroundShape ?: CircleShape
                val padding = iconSize * 0.25f
                mod
                    .background(background, shape)
                    .padding(padding)
            } else {
                mod
            }
        }
        .let { mod ->
            onClick?.let { clickAction ->
                mod
                    .clip(backgroundShape ?: CircleShape)
                    .clickable(enabled = enabled) { clickAction() }
                    .semantics { 
                        role = Role.Button
                        contentDescription?.let { 
                            this.contentDescription = it 
                        }
                    }
            } ?: mod.semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
        }
    
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = iconModifier,
        tint = if (enabled) iconTint else theme.colors.disabled
    )
}

/**
 * Unify Icon 组件 - Painter 版本
 */
@Composable
fun UnifyIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: UnifyIconVariant = UnifyIconVariant.STANDARD,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    semantic: UnifyIconSemantic = UnifyIconSemantic.DEFAULT,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    background: Color? = null,
    backgroundShape: androidx.compose.ui.graphics.Shape? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    val iconSize = customSize ?: getIconSize(size, theme)
    val iconTint = tint ?: getSemanticColor(semantic, theme)
    
    val iconModifier = modifier
        .size(iconSize)
        .let { mod ->
            if (background != null) {
                val shape = backgroundShape ?: CircleShape
                val padding = iconSize * 0.25f
                mod
                    .background(background, shape)
                    .padding(padding)
            } else {
                mod
            }
        }
        .let { mod ->
            onClick?.let { clickAction ->
                mod
                    .clip(backgroundShape ?: CircleShape)
                    .clickable(enabled = enabled) { clickAction() }
                    .semantics { 
                        role = Role.Button
                        contentDescription?.let { 
                            this.contentDescription = it 
                        }
                    }
            } ?: mod.semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
        }
    
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = iconModifier,
        tint = if (enabled) iconTint else theme.colors.disabled
    )
}

/**
 * 图标按钮组件
 */
@Composable
fun UnifyIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyIconVariant = UnifyIconVariant.STANDARD,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    semantic: UnifyIconSemantic = UnifyIconSemantic.DEFAULT,
    tint: Color? = null,
    enabled: Boolean = true,
    background: Color? = null,
    backgroundShape: androidx.compose.ui.graphics.Shape? = null,
    rippleRadius: Dp? = null
) {
    val theme = LocalUnifyTheme.current
    val buttonSize = (customSize ?: getIconSize(size, theme)) * 1.5f
    
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(backgroundShape ?: CircleShape)
            .clickable(enabled = enabled) { onClick() }
            .let { mod ->
                background?.let { bg ->
                    mod.background(bg, backgroundShape ?: CircleShape)
                } ?: mod
            }
            .semantics { 
                role = Role.Button
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        contentAlignment = Alignment.Center
    ) {
        UnifyIcon(
            imageVector = imageVector,
            contentDescription = null, // 已在外层设置
            variant = variant,
            size = size,
            customSize = customSize,
            semantic = semantic,
            tint = tint,
            enabled = enabled
        )
    }
}

/**
 * 带标记的图标组件
 */
@Composable
fun UnifyBadgedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    badge: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyIconVariant = UnifyIconVariant.STANDARD,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    semantic: UnifyIconSemantic = UnifyIconSemantic.DEFAULT,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        UnifyIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            variant = variant,
            size = size,
            customSize = customSize,
            semantic = semantic,
            tint = tint,
            onClick = onClick,
            enabled = enabled
        )
        
        Box(
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            badge()
        }
    }
}

/**
 * 图标组合组件
 */
@Composable
fun UnifyIconGroup(
    icons: List<ImageVector>,
    contentDescriptions: List<String?>,
    modifier: Modifier = Modifier,
    variant: UnifyIconVariant = UnifyIconVariant.STANDARD,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    semantic: UnifyIconSemantic = UnifyIconSemantic.DEFAULT,
    tint: Color? = null,
    spacing: Dp = 8.dp,
    arrangement: Arrangement.Horizontal = Arrangement.spacedBy(spacing),
    onIconClick: ((Int) -> Unit)? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icons.forEachIndexed { index, icon ->
            UnifyIcon(
                imageVector = icon,
                contentDescription = contentDescriptions.getOrNull(index),
                variant = variant,
                size = size,
                customSize = customSize,
                semantic = semantic,
                tint = tint,
                onClick = onIconClick?.let { { it(index) } },
                enabled = enabled
            )
        }
    }
}

/**
 * 状态图标组件
 */
@Composable
fun UnifyStatusIcon(
    status: UnifyIconSemantic,
    modifier: Modifier = Modifier,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    showBackground: Boolean = true
) {
    val theme = LocalUnifyTheme.current
    val (icon, color, backgroundColor) = getStatusIconData(status, theme)
    
    UnifyIcon(
        imageVector = icon,
        contentDescription = getStatusDescription(status),
        modifier = modifier,
        size = size,
        customSize = customSize,
        semantic = status,
        tint = color,
        background = if (showBackground) backgroundColor else null,
        backgroundShape = CircleShape
    )
}

/**
 * 动画图标组件
 */
@Composable
fun UnifyAnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: UnifyIconVariant = UnifyIconVariant.STANDARD,
    size: UnifyIconSize = UnifyIconSize.MEDIUM,
    customSize: Dp? = null,
    semantic: UnifyIconSemantic = UnifyIconSemantic.DEFAULT,
    tint: Color? = null,
    isAnimating: Boolean = false,
    animationType: IconAnimationType = IconAnimationType.ROTATE,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val theme = LocalUnifyTheme.current
    
    // 动画逻辑将在后续实现
    UnifyIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        variant = variant,
        size = size,
        customSize = customSize,
        semantic = semantic,
        tint = tint,
        onClick = onClick,
        enabled = enabled
    )
}

/**
 * 图标动画类型
 */
enum class IconAnimationType {
    ROTATE,         // 旋转
    PULSE,          // 脉冲
    BOUNCE,         // 弹跳
    SHAKE,          // 摇摆
    FADE            // 淡入淡出
}

/**
 * 获取图标尺寸
 */
@Composable
private fun getIconSize(
    size: UnifyIconSize,
    theme: com.unify.ui.theme.UnifyTheme
): Dp {
    return when (size) {
        UnifyIconSize.EXTRA_SMALL -> 12.dp
        UnifyIconSize.SMALL -> theme.dimensions.iconSizeSmall
        UnifyIconSize.MEDIUM -> theme.dimensions.iconSize
        UnifyIconSize.LARGE -> theme.dimensions.iconSizeLarge
        UnifyIconSize.EXTRA_LARGE -> 48.dp
        UnifyIconSize.CUSTOM -> theme.dimensions.iconSize // 默认值
    }
}

/**
 * 获取语义颜色
 */
@Composable
private fun getSemanticColor(
    semantic: UnifyIconSemantic,
    theme: com.unify.ui.theme.UnifyTheme
): Color {
    return when (semantic) {
        UnifyIconSemantic.DEFAULT -> LocalContentColor.current
        UnifyIconSemantic.PRIMARY -> theme.colors.primary
        UnifyIconSemantic.SECONDARY -> theme.colors.secondary
        UnifyIconSemantic.SUCCESS -> theme.colors.success
        UnifyIconSemantic.WARNING -> theme.colors.warning
        UnifyIconSemantic.ERROR -> theme.colors.error
        UnifyIconSemantic.INFO -> theme.colors.info
        UnifyIconSemantic.DISABLED -> theme.colors.disabled
    }
}

/**
 * 获取状态图标数据
 */
@Composable
private fun getStatusIconData(
    status: UnifyIconSemantic,
    theme: com.unify.ui.theme.UnifyTheme
): Triple<ImageVector, Color, Color> {
    return when (status) {
        UnifyIconSemantic.SUCCESS -> Triple(
            androidx.compose.material.icons.Icons.Default.CheckCircle,
            theme.colors.onSuccess,
            theme.colors.success
        )
        UnifyIconSemantic.WARNING -> Triple(
            androidx.compose.material.icons.Icons.Default.Warning,
            theme.colors.onWarning,
            theme.colors.warning
        )
        UnifyIconSemantic.ERROR -> Triple(
            androidx.compose.material.icons.Icons.Default.Error,
            theme.colors.onError,
            theme.colors.error
        )
        UnifyIconSemantic.INFO -> Triple(
            androidx.compose.material.icons.Icons.Default.Info,
            theme.colors.onInfo,
            theme.colors.info
        )
        else -> Triple(
            androidx.compose.material.icons.Icons.Default.Circle,
            theme.colors.onSurface,
            theme.colors.surface
        )
    }
}

/**
 * 获取状态描述
 */
private fun getStatusDescription(status: UnifyIconSemantic): String {
    return when (status) {
        UnifyIconSemantic.SUCCESS -> "成功"
        UnifyIconSemantic.WARNING -> "警告"
        UnifyIconSemantic.ERROR -> "错误"
        UnifyIconSemantic.INFO -> "信息"
        else -> "状态"
    }
}
