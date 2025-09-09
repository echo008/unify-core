package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
) {
    // Android平台使用占位符实现，实际项目中应集成Coil或Glide
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Image: ${imageUrl.takeLast(20)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
actual fun UnifyResourceImage(
    resourcePath: String,
    contentDescription: String?,
    modifier: Modifier,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
) {
    // Android资源图片实现
    val resourceId =
        LocalContext.current.resources.getIdentifier(
            resourcePath.substringBeforeLast("."),
            "drawable",
            LocalContext.current.packageName,
        )

    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    } else {
        UnifyImagePlaceholder(modifier = modifier)
    }
}

@Composable
actual fun UnifyAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier,
    size: Dp,
    backgroundColor: Color,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            // Android平台头像占位符实现
            Box(
                modifier =
                    Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "IMG",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
            }
        } else {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
        }
    }
}

@Composable
actual fun UnifyImagePlaceholder(
    modifier: Modifier,
    backgroundColor: Color,
    cornerRadius: Dp,
    content: (@Composable () -> Unit)?,
) {
    Box(
        modifier =
            modifier
                .background(
                    backgroundColor,
                    if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else RectangleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        content?.invoke() ?: Text(
            text = "📷",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Gray,
        )
    }
}
