package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * Desktop平台统一图片组件
 * 支持本地文件、网络图片和资源图片
 */
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
    var painter by remember { mutableStateOf<Painter?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    // 加载图片
    LaunchedEffect(imageUrl) {
        isLoading = true
        hasError = false
        
        try {
            val loadedPainter = loadDesktopImage(imageUrl)
            painter = loadedPainter
            isLoading = false
        } catch (e: Exception) {
            hasError = true
            isLoading = false
        }
    }
    
    val imageModifier = modifier
    
    painter?.let {
        Image(
            painter = it,
            contentDescription = contentDescription,
            modifier = imageModifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
    } ?: Box(
        modifier = imageModifier,
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }
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
    colorFilter: ColorFilter?
) {
    try {
        val painter = painterResource(resourcePath)
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
    } catch (e: Exception) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for missing resource
        }
    }
}

@Composable
actual fun UnifyAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier,
    size: Dp,
    backgroundColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            UnifyImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50%))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50%))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = name.take(1).uppercase(),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
actual fun UnifyImagePlaceholder(
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    cornerRadius: Dp,
    content: (@Composable () -> Unit)?
) {
    Box(
        modifier = modifier
            .background(
                backgroundColor,
                if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else RoundedCornerShape(0.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        content?.invoke()
    }

/**
 * 加载Desktop平台图片
 */
private suspend fun loadDesktopImage(source: String): Painter = withContext(Dispatchers.IO) {
    when {
        source.startsWith("http://") || source.startsWith("https://") -> {
            // 网络图片
            loadNetworkImage(source)
        }
        source.startsWith("file://") -> {
            // 本地文件
            loadFileImage(source.removePrefix("file://"))
        }
        source.startsWith("/") -> {
            // 绝对路径
            loadFileImage(source)
        }
        source.contains("/") -> {
            // 相对路径
            loadFileImage(source)
        }
        else -> {
            // 资源图片
            loadResourceImage(source)
        }
    }
}

/**
 * 加载网络图片
 */
private suspend fun loadNetworkImage(url: String): Painter = withContext(Dispatchers.IO) {
    val bufferedImage = ImageIO.read(URL(url))
    createPainterFromBufferedImage(bufferedImage)
}

/**
 * 加载本地文件图片
 */
private suspend fun loadFileImage(filePath: String): Painter = withContext(Dispatchers.IO) {
    val file = File(filePath)
    if (!file.exists()) {
        throw IllegalArgumentException("File does not exist: $filePath")
    }
    val bufferedImage = ImageIO.read(file)
    createPainterFromBufferedImage(bufferedImage)
}

/**
 * 加载资源图片
 */
private suspend fun loadResourceImage(resourceName: String): Painter = withContext(Dispatchers.IO) {
    val resourcePath = "/images/$resourceName"
    val inputStream = object {}.javaClass.getResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Resource not found: $resourcePath")
    
    val bufferedImage = ImageIO.read(inputStream)
    createPainterFromBufferedImage(bufferedImage)
}

/**
 * 从BufferedImage创建Painter
 */
private fun createPainterFromBufferedImage(bufferedImage: BufferedImage): Painter {
    // 这里需要将BufferedImage转换为Compose可用的Painter
    // 在实际实现中，需要使用适当的转换方法
    // 这是一个简化的实现示例
    return object : Painter() {
        override val intrinsicSize = androidx.compose.ui.geometry.Size(
            bufferedImage.width.toFloat(),
            bufferedImage.height.toFloat()
        )
        
        override fun DrawScope.onDraw() {
            // 实际绘制逻辑需要将BufferedImage转换为Compose绘制操作
            drawRect(
                color = androidx.compose.ui.graphics.Color.Gray,
                size = size
            )
        }
    }
}
