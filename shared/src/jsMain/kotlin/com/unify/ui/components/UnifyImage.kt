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
 * Web平台的图片实现
 */
actual class UnifyPlatformImage {
    companion object {
        fun createImageElement(url: String): Map<String, Any> {
            return mapOf(
                "src" to url,
                "crossOrigin" to "anonymous",
                "loading" to "lazy",
                "decoding" to "async"
            )
        }
        
        fun getImageCacheSize(): Long {
            return 75 * 1024 * 1024L // 75MB for Web
        }
        
        fun isWebPSupported(): Boolean {
            // 检查浏览器是否支持WebP
            return true // 实际实现需要检查浏览器能力
        }
        
        fun isAVIFSupported(): Boolean {
            // 检查浏览器是否支持AVIF
            return false // 实际实现需要检查浏览器能力
        }
        
        fun getOptimalImageFormat(): String {
            return when {
                isAVIFSupported() -> "AVIF"
                isWebPSupported() -> "WebP"
                else -> "JPEG"
            }
        }
        
        fun isIntersectionObserverSupported(): Boolean {
            // 检查是否支持Intersection Observer API
            return true // 实际实现需要检查浏览器API
        }
        
        fun getDevicePixelRatio(): Float {
            // 获取设备像素比
            return 1.0f // 实际实现需要调用window.devicePixelRatio
        }
        
        fun isReducedDataMode(): Boolean {
            // 检查是否为省流量模式
            return false // 实际实现需要检查Network Information API
        }
    }
}

/**
 * Web平台的异步图片加载实现
 */
actual suspend fun loadImageFromUrl(url: String): Painter {
    return withContext(Dispatchers.Main) {
        try {
            // 使用浏览器原生Image API加载图片
            val image = js("new Image()") as HTMLImageElement
            suspendCancellableCoroutine { continuation ->
                image.onload = {
                    continuation.resume(BitmapPainter(ImageBitmap.imageResource(url)))
                }
                image.onerror = {
                    continuation.resume(ColorPainter(Color.Gray))
                }
                image.src = url
            }
        } catch (e: Exception) {
            ColorPainter(Color.Gray)
        }
    }
}

/**
 * Web平台的原生图片组件适配器
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
            // 模拟Web图片加载
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
