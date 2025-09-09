package com.unify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台图片组件
 * 支持8大平台的统一图片显示
 */
@Composable
expect fun UnifyImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.Medium,
)

/**
 * Unify本地资源图片组件
 */
@Composable
expect fun UnifyResourceImage(
    resourcePath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
)

/**
 * Unify圆形头像组件
 */
@Composable
expect fun UnifyAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Gray,
)

/**
 * Unify图片占位符组件
 */
@Composable
expect fun UnifyImagePlaceholder(
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.LightGray,
    cornerRadius: androidx.compose.ui.unit.Dp = 0.dp,
    content: (@Composable () -> Unit)? = null,
)
