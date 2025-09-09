package com.unify.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

/**
 * Desktop平台Modifier扩展函数actual实现
 */

@Composable
actual fun Modifier.platformClickable(
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier {
    return this.clickable(enabled = enabled, onClick = onClick)
}

@Composable
actual fun Modifier.platformLongClickable(
    enabled: Boolean,
    onLongClick: () -> Unit,
): Modifier {
    return this // Desktop不支持长按，返回原始Modifier
}

@Composable
actual fun Modifier.platformShadow(
    elevation: Dp,
    shape: Shape,
    clip: Boolean,
): Modifier {
    return this.shadow(elevation = elevation, shape = shape, clip = clip)
}

@Composable
actual fun Modifier.platformRoundedCorners(radius: Dp): Modifier {
    return this.clip(RoundedCornerShape(radius))
}

@Composable
actual fun Modifier.platformBorder(
    width: Dp,
    color: Color,
    shape: Shape,
): Modifier {
    return this.border(width = width, color = color, shape = shape)
}

@Composable
actual fun Modifier.platformPadding(
    horizontal: Dp,
    vertical: Dp,
): Modifier {
    return this.padding(horizontal = horizontal, vertical = vertical)
}

@Composable
actual fun Modifier.platformSize(
    width: Dp,
    height: Dp,
): Modifier {
    return this.size(width = width, height = height)
}

@Composable
actual fun Modifier.platformTouchFeedback(enabled: Boolean): Modifier {
    return this // Desktop不需要触摸反馈
}

@Composable
actual fun Modifier.platformAccessibility(
    contentDescription: String?,
    role: String?,
): Modifier {
    return this.semantics {
        contentDescription?.let { this.contentDescription = it }
        // Desktop的role设置简化
    }
}

@Composable
actual fun Modifier.platformScrollable(enabled: Boolean): Modifier {
    return this // Desktop滚动由系统处理
}

@Composable
actual fun Modifier.platformAnimated(enabled: Boolean): Modifier {
    return this // Desktop动画简化实现
}
