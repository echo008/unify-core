package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
    source: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    placeholder: String?,
    error: String?,
    width: Dp,
    height: Dp,
    cornerRadius: Dp
) {
    var painter by remember { mutableStateOf<Painter?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    // 加载图片
    LaunchedEffect(source) {
        isLoading = true
        hasError = false
        
        try {
            val loadedPainter = loadDesktopImage(source)
            painter = loadedPainter
            isLoading = false
        } catch (e: Exception) {
            hasError = true
            isLoading = false
            // 尝试加载错误图片
            error?.let { errorSource ->
                try {
                    painter = loadDesktopImage(errorSource)
                } catch (e: Exception) {
                    // 忽略错误图片加载失败
                }
            }
        }
    }
    
    val imageModifier = modifier
        .let { if (width != Dp.Unspecified) it.size(width = width, height = height) else it }
        .let { if (cornerRadius > 0.dp) it.clip(RoundedCornerShape(cornerRadius)) else it }
    
    Box(
        modifier = imageModifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                // 显示加载占位符
                if (placeholder != null) {
                    LaunchedEffect(placeholder) {
                        try {
                            painter = loadDesktopImage(placeholder)
                        } catch (e: Exception) {
                            // 显示默认加载指示器
                        }
                    }
                    painter?.let { placeholderPainter ->
                        Image(
                            painter = placeholderPainter,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        )
                    } ?: CircularProgressIndicator()
                } else {
                    CircularProgressIndicator()
                }
            }
            hasError -> {
                // 显示错误状态
                painter?.let { errorPainter ->
                    Image(
                        painter = errorPainter,
                        contentDescription = "Error loading image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
                    )
                }
            }
            painter != null -> {
                // 显示加载成功的图片
                Image(
                    painter = painter!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }
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
