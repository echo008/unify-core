package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Desktop平台的图片实现
 */
actual class UnifyPlatformImage {
    companion object {
        fun getImageCacheSize(): Long {
            return 200 * 1024 * 1024L // 200MB for Desktop
        }
        
        fun getSupportedFormats(): List<String> {
            return listOf("JPEG", "PNG", "GIF", "BMP", "WBMP", "WebP")
        }
        
        fun getSystemMemory(): Long {
            return Runtime.getRuntime().maxMemory()
        }
        
        fun getAvailableMemory(): Long {
            return Runtime.getRuntime().freeMemory()
        }
        
        fun isHighDpiDisplay(): Boolean {
            // 检查是否为高DPI显示器
            return System.getProperty("sun.java2d.uiScale")?.toFloatOrNull()?.let { it > 1.0f } ?: false
        }
        
        fun getDisplayScale(): Float {
            return System.getProperty("sun.java2d.uiScale")?.toFloatOrNull() ?: 1.0f
        }
        
        fun isHardwareAccelerated(): Boolean {
            return System.getProperty("sun.java2d.d3d")?.equals("true") ?: false
        }
        
        fun getCacheDirectory(): String {
            return System.getProperty("java.io.tmpdir") + "/unify_image_cache"
        }
    }
}

/**
 * Desktop平台的异步图片加载实现
 */
actual suspend fun loadImageFromUrl(url: String): Painter {
    return withContext(Dispatchers.IO) {
        try {
            // 使用Java ImageIO加载图片
            val inputStream = when {
                url.startsWith("http") -> {
                    java.net.URL(url).openStream()
                }
                url.startsWith("file://") -> {
                    java.io.FileInputStream(url.removePrefix("file://"))
                }
                else -> {
                    // 从resources加载
                    this::class.java.classLoader.getResourceAsStream(url)
                }
            }
            
            inputStream?.use {
                val bufferedImage = javax.imageio.ImageIO.read(it)
                if (bufferedImage != null) {
                    return@withContext BitmapPainter(bufferedImage.toImageBitmap())
                }
            }
            ColorPainter(Color.Gray)
        } catch (e: Exception) {
            ColorPainter(Color.Gray)
        }
    }
}

/**
 * Desktop平台的原生图片组件适配器
 */
@Composable
actual fun UnifyNativeImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    shape: UnifyImageShape,
    placeholder: @Composable (() -> Unit)?,
    error: @Composable (() -> Unit)?,
    loading: @Composable (() -> Unit)?
) {
    var painter by remember { mutableStateOf<Painter?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(url) {
        isLoading = true
        hasError = false
        
        try {
            // 模拟Desktop图片加载
            painter = loadImageFromUrl(url)
        } catch (e: Exception) {
            hasError = true
        } finally {
            isLoading = false
        }
    }
    
    when {
        isLoading -> loading?.invoke()
        hasError -> error?.invoke()
        painter != null -> {
            Image(
                painter = painter!!,
                contentDescription = contentDescription,
                modifier = modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
        else -> placeholder?.invoke()
    }
}
