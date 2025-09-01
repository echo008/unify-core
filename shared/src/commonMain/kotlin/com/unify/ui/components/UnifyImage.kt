package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme

/**
 * 统一图片组件
 * 跨平台一致的图片显示组件，支持多种形状、缩放模式和加载状态
 */
@Composable
fun UnifyImage(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    shape: UnifyImageShape = UnifyImageShape.Rectangle(),
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
    onLoading: ((Boolean) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(painter) {
        if (painter != null) {
            isLoading = true
            onLoading?.invoke(true)
            try {
                // 模拟图片加载
                onSuccess?.invoke()
                hasError = false
            } catch (e: Exception) {
                hasError = true
                onError?.invoke()
            } finally {
                isLoading = false
                onLoading?.invoke(false)
            }
        }
    }
    
    Box(
        modifier = modifier.clip(shape.toComposeShape()),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading && loading != null -> {
                loading()
            }
            hasError && error != null -> {
                error()
            }
            painter == null && placeholder != null -> {
                placeholder()
            }
            painter != null -> {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
            else -> {
                // 默认占位符
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(theme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyText(
                        text = "Image",
                        variant = UnifyTextVariant.Caption,
                        color = theme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 异步图片组件
 */
@Composable
fun UnifyAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    shape: UnifyImageShape = UnifyImageShape.Rectangle(),
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
    onLoading: ((Boolean) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    var painter by remember { mutableStateOf<Painter?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(url) {
        isLoading = true
        hasError = false
        onLoading?.invoke(true)
        
        try {
            // 使用平台特定的图片加载器
            painter = loadImageFromUrl(url)
            onSuccess?.invoke()
        } catch (e: Exception) {
            hasError = true
            onError?.invoke()
        } finally {
            isLoading = false
            onLoading?.invoke(false)
        }
    }
    
    Box(
        modifier = modifier.clip(shape.toComposeShape()),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                loading?.invoke() ?: DefaultLoadingIndicator()
            }
            hasError -> {
                error?.invoke() ?: DefaultErrorPlaceholder()
            }
            painter != null -> {
                Image(
                    painter = painter!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
            else -> {
                placeholder?.invoke() ?: DefaultPlaceholder()
            }
        }
    }
}

/**
 * 头像图片组件
 */
@Composable
fun UnifyAvatar(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    fallbackText: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (fallbackText != null) {
            UnifyText(
                text = fallbackText.take(2).uppercase(),
                variant = UnifyTextVariant.LabelMedium,
                color = textColor
            )
        }
    }
}

/**
 * 图片形状定义
 */
sealed class UnifyImageShape {
    data class Rectangle(val cornerRadius: Dp = 0.dp) : UnifyImageShape()
    object Circle : UnifyImageShape()
    data class RoundedCorners(val radius: Dp) : UnifyImageShape()
    data class Custom(val shape: Shape) : UnifyImageShape()
}

/**
 * 将UnifyImageShape转换为Compose Shape
 */
private fun UnifyImageShape.toComposeShape(): Shape {
    return when (this) {
        is UnifyImageShape.Rectangle -> {
            if (cornerRadius > 0.dp) {
                RoundedCornerShape(cornerRadius)
            } else {
                androidx.compose.foundation.shape.RectangleShape
            }
        }
        is UnifyImageShape.Circle -> CircleShape
        is UnifyImageShape.RoundedCorners -> RoundedCornerShape(radius)
        is UnifyImageShape.Custom -> shape
    }
}

/**
 * 默认加载指示器
 */
@Composable
private fun DefaultLoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        strokeWidth = 2.dp
    )
}

/**
 * 默认错误占位符
 */
@Composable
private fun DefaultErrorPlaceholder() {
    val theme = LocalUnifyTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.colors.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        UnifyText(
            text = "Error",
            variant = UnifyTextVariant.Caption,
            color = theme.colors.onErrorContainer
        )
    }
}

/**
 * 默认占位符
 */
@Composable
private fun DefaultPlaceholder() {
    val theme = LocalUnifyTheme.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        UnifyText(
            text = "Image",
            variant = UnifyTextVariant.Caption,
            color = theme.colors.onSurfaceVariant
        )
    }
}

/**
 * 平台特定的图片加载实现
 */
expect suspend fun loadImageFromUrl(url: String): Painter

/**
 * 平台特定的图片实现
 */
expect class UnifyPlatformImage

/**
 * 图片组件的平台适配器
 */
@Composable
expect fun UnifyNativeImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    shape: UnifyImageShape = UnifyImageShape.Rectangle(),
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null
)
