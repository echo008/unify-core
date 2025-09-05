package com.unify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop平台统一表面组件
 * 提供统一的背景、边框、阴影等视觉效果
 */
@Composable
actual fun UnifySurface(
    modifier: Modifier,
    shape: Shape,
    color: Color,
    elevation: Dp,
    border: BorderStroke?,
    content: @Composable () -> Unit
) {
    val surfaceColor = if (color == Color.Unspecified) {
        MaterialTheme.colorScheme.surface
    } else {
        color
    }
    
    val surfaceShape = if (shape == RoundedCornerShape(0.dp)) {
        RoundedCornerShape(8.dp) // Desktop默认圆角
    } else {
        shape
    }
    
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = surfaceShape,
                clip = false
            )
            .clip(surfaceShape)
            .background(surfaceColor)
            .let { mod ->
                border?.let { borderStroke ->
                    mod.border(
                        width = borderStroke.width,
                        color = borderStroke.color,
                        shape = surfaceShape
                    )
                } ?: mod
            }
            .padding(0.dp)
    ) {
        content()
    }
}
