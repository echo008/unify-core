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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
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
 * 从BufferedImage创建Painter
 */
fun createPainterFromBufferedImage(bufferedImage: BufferedImage): Painter {
    return object : Painter() {
        override val intrinsicSize =
            androidx.compose.ui.geometry.Size(
                bufferedImage.width.toFloat(),
                bufferedImage.height.toFloat(),
            )

        override fun DrawScope.onDraw() {
            drawRect(
                color = androidx.compose.ui.graphics.Color.Gray,
                size = size,
            )
        }
    }
}

/**
 * 加载网络图片
 */
suspend fun loadImageFromNetwork(url: String): Painter =
    withContext(Dispatchers.IO) {
        val bufferedImage = ImageIO.read(URL(url))
        createPainterFromBufferedImage(bufferedImage)
    }

/**
 * 加载本地文件图片
 */
suspend fun loadImageFromFile(filePath: String): Painter =
    withContext(Dispatchers.IO) {
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
suspend fun loadImageFromResource(resourceName: String): Painter =
    withContext(Dispatchers.IO) {
        val resourcePath = "/images/$resourceName"
        val inputStream =
            object {}.javaClass.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val bufferedImage = ImageIO.read(inputStream)
        createPainterFromBufferedImage(bufferedImage)
    }

/**
 * 加载Desktop平台图片
 */
suspend fun loadImageFromUrl(source: String): Painter =
    withContext(Dispatchers.IO) {
        when {
            source.startsWith("http://") || source.startsWith("https://") -> {
                loadImageFromNetwork(source)
            }
            source.startsWith("file://") -> {
                loadImageFromFile(source.removePrefix("file://"))
            }
            source.startsWith("/") -> {
                loadImageFromFile(source)
            }
            source.contains("/") -> {
                loadImageFromFile(source)
            }
            else -> {
                loadImageFromResource(source)
            }
        }
    }

/**
 * Desktop平台统一图片组件
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
    filterQuality: FilterQuality,
) {
    var painter by remember { mutableStateOf<Painter?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(imageUrl) {
        isLoading = true
        hasError = false

        try {
            val loadedPainter = loadImageFromUrl(imageUrl)
            painter = loadedPainter
            isLoading = false
        } catch (e: Exception) {
            hasError = true
            isLoading = false
        }
    }

    painter?.let {
        Image(
            painter = it,
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    } ?: Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
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
    colorFilter: ColorFilter?,
) {
    val painter = painterResource(resourcePath)
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

@Composable
actual fun UnifyAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier,
    size: Dp,
    backgroundColor: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            UnifyImage(
                imageUrl = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50.dp)),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50.dp))
                        .background(backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text(
                    text = name.take(1).uppercase(),
                    color = androidx.compose.ui.graphics.Color.White,
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
    content: (@Composable () -> Unit)?,
) {
    Box(
        modifier =
            modifier
                .background(
                    backgroundColor,
                    if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else RoundedCornerShape(0.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        content?.invoke()
    }
}
