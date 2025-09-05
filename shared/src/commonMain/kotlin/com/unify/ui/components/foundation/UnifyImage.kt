package com.unify.ui.components.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台图片组件
 * 支持8大平台的统一图片显示
 */
@Composable
fun UnifyImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.Medium
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * Unify圆形图片组件
 */
@Composable
fun UnifyCircularImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.clip(androidx.compose.foundation.shape.CircleShape),
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * Unify圆角图片组件
 */
@Composable
fun UnifyRoundedImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * Unify异步图片组件（带加载状态）
 */
@Composable
fun UnifyAsyncImage(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    shape: Shape? = null,
    loadingContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    },
    errorContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            UnifyText("加载失败")
        }
    }
) {
    when {
        isLoading -> loadingContent()
        painter != null -> {
            val imageModifier = if (shape != null) {
                modifier.clip(shape)
            } else {
                modifier
            }
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = imageModifier,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        }
        else -> errorContent()
    }
}

/**
 * Unify头像组件
 */
@Composable
fun UnifyAvatar(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    isLoading: Boolean = false,
    fallbackText: String = "?"
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            painter != null -> UnifyCircularImage(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
            else -> UnifyText(
                text = fallbackText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }
    }
}
