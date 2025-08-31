package com.unify.ui.components.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformType

/**
 * Unify Image 组件
 * 支持多平台适配的统一图片组件，参考 KuiklyUI 设计规范
 */

/**
 * 图片变体枚举
 */
enum class UnifyImageVariant {
    STANDARD,       // 标准图片
    AVATAR,         // 头像图片
    THUMBNAIL,      // 缩略图
    BANNER,         // 横幅图片
    ICON,           // 图标图片
    BACKGROUND      // 背景图片
}

/**
 * 图片尺寸枚举
 */
enum class UnifyImageSize {
    EXTRA_SMALL,    // 16dp
    SMALL,          // 24dp
    MEDIUM,         // 48dp
    LARGE,          // 72dp
    EXTRA_LARGE,    // 96dp
    CUSTOM          // 自定义尺寸
}

/**
 * 图片状态枚举
 */
enum class UnifyImageState {
    LOADING,        // 加载中
    SUCCESS,        // 加载成功
    ERROR,          // 加载失败
    PLACEHOLDER     // 占位符
}

/**
 * 主要 Unify Image 组件
 */
@Composable
fun UnifyImage(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: UnifyImageVariant = UnifyImageVariant.STANDARD,
    size: UnifyImageSize = UnifyImageSize.MEDIUM,
    customSize: Dp? = null,
    state: UnifyImageState = UnifyImageState.SUCCESS,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
    onImageLoad: (() -> Unit)? = null,
    onImageError: (() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取图片尺寸
    val imageSize = customSize ?: getImageSize(size, theme)
    
    // 获取图片形状
    val imageShape = getImageShape(variant, theme)
    
    // 构建修饰符
    val imageModifier = modifier
        .size(imageSize)
        .clip(imageShape)
        .semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    
    Box(
        modifier = imageModifier,
        contentAlignment = alignment
    ) {
        when (state) {
            UnifyImageState.LOADING -> {
                loading?.invoke() ?: DefaultLoadingContent()
            }
            UnifyImageState.ERROR -> {
                error?.invoke() ?: DefaultErrorContent(variant, theme)
            }
            UnifyImageState.PLACEHOLDER -> {
                placeholder?.invoke() ?: DefaultPlaceholderContent(variant, theme)
            }
            UnifyImageState.SUCCESS -> {
                painter?.let { p ->
                    Image(
                        painter = p,
                        contentDescription = contentDescription,
                        modifier = Modifier.fillMaxSize(),
                        alignment = alignment,
                        contentScale = contentScale,
                        alpha = alpha,
                        colorFilter = colorFilter
                    )
                    
                    LaunchedEffect(painter) {
                        onImageLoad?.invoke()
                    }
                } ?: run {
                    placeholder?.invoke() ?: DefaultPlaceholderContent(variant, theme)
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
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: UnifyImageVariant = UnifyImageVariant.STANDARD,
    size: UnifyImageSize = UnifyImageSize.MEDIUM,
    customSize: Dp? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
    onLoading: ((String) -> Unit)? = null,
    onSuccess: ((Painter) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
) {
    var imageState by remember { mutableStateOf(UnifyImageState.LOADING) }
    var imagePainter by remember { mutableStateOf<Painter?>(null) }
    
    // 这里应该集成实际的图片加载库，如 Coil
    // 现在使用简化的实现
    LaunchedEffect(model) {
        imageState = UnifyImageState.LOADING
        onLoading?.invoke(model.toString())
        
        try {
            // 模拟异步加载
            kotlinx.coroutines.delay(1000)
            // 实际实现中这里应该加载图片
            imageState = UnifyImageState.SUCCESS
            onSuccess?.invoke(imagePainter!!)
        } catch (e: Exception) {
            imageState = UnifyImageState.ERROR
            onError?.invoke(e)
        }
    }
    
    UnifyImage(
        painter = imagePainter,
        contentDescription = contentDescription,
        modifier = modifier,
        variant = variant,
        size = size,
        customSize = customSize,
        state = imageState,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        placeholder = placeholder,
        error = error,
        loading = loading
    )
}

/**
 * 头像组件
 */
@Composable
fun UnifyAvatar(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: UnifyImageSize = UnifyImageSize.MEDIUM,
    customSize: Dp? = null,
    initials: String? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    val avatarSize = customSize ?: getImageSize(size, theme)
    val bgColor = backgroundColor ?: theme.colors.primary
    val txtColor = textColor ?: theme.colors.onPrimary
    
    Box(
        modifier = modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(bgColor)
            .let { mod ->
                onClick?.let { 
                    mod.clickable { it() }
                } ?: mod
            },
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (initials != null) {
            UnifyText(
                text = initials,
                variant = when (size) {
                    UnifyImageSize.EXTRA_SMALL -> UnifyTextVariant.LABEL_SMALL
                    UnifyImageSize.SMALL -> UnifyTextVariant.LABEL_MEDIUM
                    UnifyImageSize.MEDIUM -> UnifyTextVariant.TITLE_MEDIUM
                    UnifyImageSize.LARGE -> UnifyTextVariant.TITLE_LARGE
                    UnifyImageSize.EXTRA_LARGE -> UnifyTextVariant.HEADLINE_SMALL
                    UnifyImageSize.CUSTOM -> UnifyTextVariant.TITLE_MEDIUM
                },
                color = txtColor
            )
        } else {
            // 默认头像图标
            Icon(
                imageVector = getDefaultAvatarIcon(),
                contentDescription = contentDescription,
                tint = txtColor,
                modifier = Modifier.size(avatarSize * 0.6f)
            )
        }
    }
}

/**
 * 缩略图组件
 */
@Composable
fun UnifyThumbnail(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: UnifyImageSize = UnifyImageSize.SMALL,
    customSize: Dp? = null,
    contentScale: ContentScale = ContentScale.Crop,
    overlay: @Composable (BoxScope.() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    val thumbnailSize = customSize ?: getImageSize(size, theme)
    
    Box(
        modifier = modifier
            .size(thumbnailSize)
            .clip(theme.shapes.small)
    ) {
        UnifyImage(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            variant = UnifyImageVariant.THUMBNAIL,
            size = UnifyImageSize.CUSTOM,
            customSize = thumbnailSize,
            contentScale = contentScale
        )
        
        overlay?.invoke(this)
    }
}

/**
 * 横幅图片组件
 */
@Composable
fun UnifyBanner(
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f,
    contentScale: ContentScale = ContentScale.Crop,
    overlay: @Composable (BoxScope.() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(theme.shapes.medium)
    ) {
        UnifyImage(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            variant = UnifyImageVariant.BANNER,
            size = UnifyImageSize.CUSTOM,
            contentScale = contentScale
        )
        
        overlay?.invoke(this)
    }
}

/**
 * 默认加载内容
 */
@Composable
private fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

/**
 * 默认错误内容
 */
@Composable
private fun DefaultErrorContent(
    variant: UnifyImageVariant,
    theme: com.unify.ui.theme.UnifyTheme
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getErrorIcon(),
            contentDescription = "加载失败",
            tint = theme.colors.error,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 默认占位符内容
 */
@Composable
private fun DefaultPlaceholderContent(
    variant: UnifyImageVariant,
    theme: com.unify.ui.theme.UnifyTheme
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getPlaceholderIcon(variant),
            contentDescription = "占位符",
            tint = theme.colors.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 获取图片尺寸
 */
@Composable
private fun getImageSize(
    size: UnifyImageSize,
    theme: com.unify.ui.theme.UnifyTheme
): Dp {
    return when (size) {
        UnifyImageSize.EXTRA_SMALL -> 16.dp
        UnifyImageSize.SMALL -> 24.dp
        UnifyImageSize.MEDIUM -> 48.dp
        UnifyImageSize.LARGE -> 72.dp
        UnifyImageSize.EXTRA_LARGE -> 96.dp
        UnifyImageSize.CUSTOM -> 48.dp // 默认值
    }
}

/**
 * 获取图片形状
 */
@Composable
private fun getImageShape(
    variant: UnifyImageVariant,
    theme: com.unify.ui.theme.UnifyTheme
): Shape {
    return when (variant) {
        UnifyImageVariant.AVATAR -> CircleShape
        UnifyImageVariant.THUMBNAIL -> theme.shapes.small
        UnifyImageVariant.BANNER -> theme.shapes.medium
        UnifyImageVariant.ICON -> theme.shapes.none
        UnifyImageVariant.BACKGROUND -> theme.shapes.none
        UnifyImageVariant.STANDARD -> theme.shapes.small
    }
}

/**
 * 获取默认头像图标
 */
private fun getDefaultAvatarIcon(): ImageVector {
    // 这里应该返回实际的头像图标
    // 现在返回一个占位符
    return androidx.compose.material.icons.Icons.Default.Person
}

/**
 * 获取错误图标
 */
private fun getErrorIcon(): ImageVector {
    return androidx.compose.material.icons.Icons.Default.Error
}

/**
 * 获取占位符图标
 */
private fun getPlaceholderIcon(variant: UnifyImageVariant): ImageVector {
    return when (variant) {
        UnifyImageVariant.AVATAR -> androidx.compose.material.icons.Icons.Default.Person
        UnifyImageVariant.THUMBNAIL -> androidx.compose.material.icons.Icons.Default.Image
        UnifyImageVariant.BANNER -> androidx.compose.material.icons.Icons.Default.Landscape
        UnifyImageVariant.ICON -> androidx.compose.material.icons.Icons.Default.Category
        else -> androidx.compose.material.icons.Icons.Default.Image
    }
}
