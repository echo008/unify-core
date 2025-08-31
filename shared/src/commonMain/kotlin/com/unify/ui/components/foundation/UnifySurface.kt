package com.unify.ui.components.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme

/**
 * Unify Surface 组件
 * 支持多平台适配的统一表面组件，参考 KuiklyUI 设计规范
 */

/**
 * 表面变体枚举
 */
enum class UnifySurfaceVariant {
    STANDARD,       // 标准表面
    ELEVATED,       // 提升表面
    FILLED,         // 填充表面
    OUTLINED,       // 轮廓表面
    TONAL,          // 色调表面
    TRANSPARENT     // 透明表面
}

/**
 * 表面高度枚举
 */
enum class UnifySurfaceElevation {
    NONE,           // 0dp
    LEVEL1,         // 1dp
    LEVEL2,         // 3dp
    LEVEL3,         // 6dp
    LEVEL4,         // 8dp
    LEVEL5,         // 12dp
    CUSTOM          // 自定义高度
}

/**
 * 主要 Unify Surface 组件
 */
@Composable
fun UnifySurface(
    modifier: Modifier = Modifier,
    variant: UnifySurfaceVariant = UnifySurfaceVariant.STANDARD,
    elevation: UnifySurfaceElevation = UnifySurfaceElevation.NONE,
    customElevation: Dp? = null,
    shape: Shape? = null,
    color: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取表面配置
    val surfaceConfig = getSurfaceConfig(variant, theme)
    val actualElevation = customElevation ?: getElevationValue(elevation)
    val actualShape = shape ?: theme.shapes.medium
    val actualColor = color ?: surfaceConfig.color
    val actualContentColor = contentColor ?: surfaceConfig.contentColor
    
    // 构建修饰符
    val surfaceModifier = modifier
        .shadow(
            elevation = actualElevation,
            shape = actualShape,
            clip = false
        )
        .clip(actualShape)
        .background(actualColor, actualShape)
        .let { mod ->
            border?.let { borderStroke ->
                mod.border(borderStroke, actualShape)
            } ?: mod
        }
        .let { mod ->
            onClick?.let { clickAction ->
                mod.clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = clickAction
                )
            } ?: mod
        }
    
    Surface(
        modifier = surfaceModifier,
        shape = actualShape,
        color = actualColor,
        contentColor = actualContentColor,
        tonalElevation = actualElevation,
        shadowElevation = actualElevation,
        border = border,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * 卡片组件
 */
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    variant: UnifySurfaceVariant = UnifySurfaceVariant.ELEVATED,
    elevation: UnifySurfaceElevation = UnifySurfaceElevation.LEVEL1,
    customElevation: Dp? = null,
    shape: Shape? = null,
    color: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualShape = shape ?: theme.shapes.medium
    
    UnifySurface(
        modifier = modifier,
        variant = variant,
        elevation = elevation,
        customElevation = customElevation,
        shape = actualShape,
        color = color,
        contentColor = contentColor,
        border = border,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }
}

/**
 * 容器组件
 */
@Composable
fun UnifyContainer(
    modifier: Modifier = Modifier,
    variant: UnifySurfaceVariant = UnifySurfaceVariant.FILLED,
    shape: Shape? = null,
    color: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualShape = shape ?: theme.shapes.small
    
    UnifySurface(
        modifier = modifier,
        variant = variant,
        elevation = UnifySurfaceElevation.NONE,
        shape = actualShape,
        color = color,
        contentColor = contentColor,
        border = border
    ) {
        Box(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }
}

/**
 * 面板组件
 */
@Composable
fun UnifyPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    variant: UnifySurfaceVariant = UnifySurfaceVariant.OUTLINED,
    elevation: UnifySurfaceElevation = UnifySurfaceElevation.LEVEL1,
    shape: Shape? = null,
    color: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = null,
    headerContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualShape = shape ?: theme.shapes.medium
    
    UnifySurface(
        modifier = modifier,
        variant = variant,
        elevation = elevation,
        shape = actualShape,
        color = color,
        contentColor = contentColor,
        border = border
    ) {
        Column {
            // 头部内容
            if (title != null || subtitle != null || headerContent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (headerContent != null) {
                        headerContent()
                    } else {
                        Column {
                            title?.let { titleText ->
                                UnifyText(
                                    text = titleText,
                                    variant = com.unify.ui.components.foundation.UnifyTextVariant.TITLE_MEDIUM
                                )
                            }
                            subtitle?.let { subtitleText ->
                                UnifyText(
                                    text = subtitleText,
                                    variant = com.unify.ui.components.foundation.UnifyTextVariant.BODY_MEDIUM,
                                    semantic = com.unify.ui.components.foundation.UnifyTextSemantic.SECONDARY
                                )
                            }
                        }
                    }
                }
                
                UnifyDivider()
            }
            
            // 主要内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
            
            // 底部内容
            footerContent?.let { footer ->
                UnifyDivider()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    footer()
                }
            }
        }
    }
}

/**
 * 芯片组件
 */
@Composable
fun UnifyChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: UnifySurfaceVariant = UnifySurfaceVariant.TONAL,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    val chipColor = if (selected) theme.colors.primary else theme.colors.surface
    val chipContentColor = if (selected) theme.colors.onPrimary else theme.colors.onSurface
    
    UnifySurface(
        modifier = modifier,
        variant = variant,
        shape = theme.shapes.small,
        color = chipColor,
        contentColor = chipContentColor,
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            leadingIcon?.let { icon ->
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            UnifyText(
                text = text,
                variant = com.unify.ui.components.foundation.UnifyTextVariant.LABEL_MEDIUM
            )
            
            trailingIcon?.let { icon ->
                Spacer(modifier = Modifier.width(8.dp))
                icon()
            }
            
            onClose?.let { closeAction ->
                Spacer(modifier = Modifier.width(8.dp))
                UnifyIconButton(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "关闭",
                    onClick = closeAction,
                    size = com.unify.ui.components.foundation.UnifyIconSize.SMALL
                )
            }
        }
    }
}

/**
 * 标签组件
 */
@Composable
fun UnifyBadge(
    modifier: Modifier = Modifier,
    variant: UnifySurfaceVariant = UnifySurfaceVariant.FILLED,
    color: Color? = null,
    contentColor: Color? = null,
    content: (@Composable () -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    val badgeColor = color ?: theme.colors.error
    val badgeContentColor = contentColor ?: theme.colors.onError
    
    UnifySurface(
        modifier = modifier,
        variant = variant,
        shape = CircleShape,
        color = badgeColor,
        contentColor = badgeContentColor
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            content?.invoke()
        }
    }
}

/**
 * 分隔符组件
 */
@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color? = null,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp
) {
    val theme = LocalUnifyTheme.current
    val dividerColor = color ?: theme.colors.outline.copy(alpha = 0.12f)
    
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = dividerColor
    )
}

/**
 * 获取表面配置
 */
@Composable
private fun getSurfaceConfig(
    variant: UnifySurfaceVariant,
    theme: com.unify.ui.theme.UnifyTheme
): SurfaceConfig {
    return when (variant) {
        UnifySurfaceVariant.STANDARD -> SurfaceConfig(
            color = theme.colors.surface,
            contentColor = theme.colors.onSurface
        )
        UnifySurfaceVariant.ELEVATED -> SurfaceConfig(
            color = theme.colors.surfaceVariant,
            contentColor = theme.colors.onSurfaceVariant
        )
        UnifySurfaceVariant.FILLED -> SurfaceConfig(
            color = theme.colors.surfaceVariant,
            contentColor = theme.colors.onSurfaceVariant
        )
        UnifySurfaceVariant.OUTLINED -> SurfaceConfig(
            color = theme.colors.surface,
            contentColor = theme.colors.onSurface
        )
        UnifySurfaceVariant.TONAL -> SurfaceConfig(
            color = theme.colors.secondaryContainer,
            contentColor = theme.colors.onSecondaryContainer
        )
        UnifySurfaceVariant.TRANSPARENT -> SurfaceConfig(
            color = Color.Transparent,
            contentColor = theme.colors.onSurface
        )
    }
}

/**
 * 获取高度值
 */
private fun getElevationValue(elevation: UnifySurfaceElevation): Dp {
    return when (elevation) {
        UnifySurfaceElevation.NONE -> 0.dp
        UnifySurfaceElevation.LEVEL1 -> 1.dp
        UnifySurfaceElevation.LEVEL2 -> 3.dp
        UnifySurfaceElevation.LEVEL3 -> 6.dp
        UnifySurfaceElevation.LEVEL4 -> 8.dp
        UnifySurfaceElevation.LEVEL5 -> 12.dp
        UnifySurfaceElevation.CUSTOM -> 0.dp // 默认值
    }
}

/**
 * 表面配置数据类
 */
private data class SurfaceConfig(
    val color: Color,
    val contentColor: Color
)
