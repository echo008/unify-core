package com.unify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme

/**
 * 统一表面组件
 * 跨平台一致的表面容器组件，支持阴影、边框、点击等功能
 */
@Composable
fun UnifySurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content
    )
}

/**
 * 可点击的表面组件
 */
@Composable
fun UnifyClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * 卡片表面组件
 */
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    elevation: UnifyCardElevation = UnifyCardElevation.Level1,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val (tonalElevation, shadowElevation) = elevation.toElevationValues()
    
    UnifySurface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content
    )
}

/**
 * 可点击的卡片表面组件
 */
@Composable
fun UnifyClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    elevation: UnifyCardElevation = UnifyCardElevation.Level1,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val (tonalElevation, shadowElevation) = elevation.toElevationValues()
    
    UnifyClickableSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * 容器表面组件
 */
@Composable
fun UnifyContainer(
    modifier: Modifier = Modifier,
    variant: UnifyContainerVariant = UnifyContainerVariant.Surface,
    shape: Shape = RectangleShape,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val (backgroundColor, contentColor, elevation) = when (variant) {
        UnifyContainerVariant.Surface -> Triple(
            theme.colors.surface,
            theme.colors.onSurface,
            0.dp
        )
        UnifyContainerVariant.SurfaceVariant -> Triple(
            theme.colors.surfaceVariant,
            theme.colors.onSurfaceVariant,
            0.dp
        )
        UnifyContainerVariant.Primary -> Triple(
            theme.colors.primary,
            theme.colors.onPrimary,
            0.dp
        )
        UnifyContainerVariant.Secondary -> Triple(
            theme.colors.secondary,
            theme.colors.onSecondary,
            0.dp
        )
        UnifyContainerVariant.Tertiary -> Triple(
            theme.colors.tertiary,
            theme.colors.onTertiary,
            0.dp
        )
        UnifyContainerVariant.Error -> Triple(
            theme.colors.error,
            theme.colors.onError,
            0.dp
        )
        UnifyContainerVariant.Background -> Triple(
            theme.colors.background,
            theme.colors.onBackground,
            0.dp
        )
    }
    
    UnifySurface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = elevation,
        border = border,
        content = content
    )
}

/**
 * 分隔线表面组件
 */
@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    startIndent: Dp = 0.dp
) {
    val theme = LocalUnifyTheme.current
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

/**
 * 垂直分隔线表面组件
 */
@Composable
fun UnifyVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
) {
    androidx.compose.material3.VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

/**
 * 卡片高度枚举
 */
enum class UnifyCardElevation {
    Level0,  // 0dp
    Level1,  // 1dp
    Level2,  // 3dp
    Level3,  // 6dp
    Level4,  // 8dp
    Level5   // 12dp
}

/**
 * 容器变体枚举
 */
enum class UnifyContainerVariant {
    Surface,
    SurfaceVariant,
    Primary,
    Secondary,
    Tertiary,
    Error,
    Background
}

/**
 * 将卡片高度转换为具体的高度值
 */
private fun UnifyCardElevation.toElevationValues(): Pair<Dp, Dp> {
    return when (this) {
        UnifyCardElevation.Level0 -> Pair(0.dp, 0.dp)
        UnifyCardElevation.Level1 -> Pair(1.dp, 1.dp)
        UnifyCardElevation.Level2 -> Pair(3.dp, 3.dp)
        UnifyCardElevation.Level3 -> Pair(6.dp, 6.dp)
        UnifyCardElevation.Level4 -> Pair(8.dp, 8.dp)
        UnifyCardElevation.Level5 -> Pair(12.dp, 12.dp)
    }
}

/**
 * 平台特定的表面实现
 */
expect class UnifyPlatformSurface

/**
 * 表面组件的平台适配器
 */
@Composable
expect fun UnifyNativeSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = Color.Transparent,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
)
