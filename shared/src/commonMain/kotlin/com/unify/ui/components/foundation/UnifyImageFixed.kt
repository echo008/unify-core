package com.unify.ui.components.foundation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台图片组件 - 修复版本
 * 支持8大平台的统一图片显示
 */
@Composable
fun UnifyImageFixed(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1.0f,
    shape: Shape = RoundedCornerShape(0.dp),
) {
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = alignment,
    ) {
        if (painter != null) {
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
            )
        } else {
            Text(
                text = "图片加载失败",
                color = Color.Gray,
            )
        }
    }
}

/**
 * Unify圆形图片组件
 */
@Composable
fun UnifyCircularImageFixed(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    UnifyImageFixed(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        shape = androidx.compose.foundation.shape.CircleShape,
        contentScale = ContentScale.Crop,
    )
}

/**
 * Unify头像组件
 */
@Composable
fun UnifyAvatarFixed(
    painter: Painter?,
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    isLoading: Boolean = false,
    fallbackText: String = "?",
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            painter != null ->
                UnifyCircularImageFixed(
                    painter = painter,
                    contentDescription = "头像: $name",
                    size = size,
                )
            else ->
                Text(
                    text = fallbackText,
                    color = Color.White,
                )
        }
    }
}
