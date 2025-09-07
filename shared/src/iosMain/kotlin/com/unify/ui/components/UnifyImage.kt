package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp

@Composable
actual fun UnifyImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality
) {
    // iOS平台使用Compose Multiplatform的AsyncImage
    // 这里使用占位符实现，实际项目中需要集成网络图片加载库
    UnifyImagePlaceholder(
        modifier = modifier,
        content = {
            Text(
                text = "🖼️",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    )
}

@Composable
actual fun UnifyResourceImage(
    resourcePath: String,
    contentDescription: String?,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?
) {
    // iOS资源图片实现
    UnifyImagePlaceholder(
        modifier = modifier,
        content = {
            Text(
                text = "📱",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    )
}

@Composable
actual fun UnifyAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier,
    size: Dp,
    backgroundColor: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
    }
}

@Composable
actual fun UnifyImagePlaceholder(
    modifier: Modifier,
    backgroundColor: Color,
    cornerRadius: Dp,
    content: (@Composable () -> Unit)?
) {
    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        content?.invoke() ?: Text(
            text = "📷",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Gray
        )
    }
}
