package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 小程序平台的图片实现
 */
actual class UnifyPlatformImage {
    companion object {
        fun getImageCacheSize(): Long {
            return 30 * 1024 * 1024L // 30MB for MiniApp
        }
        
        fun getSupportedFormats(): List<String> {
            return listOf("JPEG", "PNG", "GIF", "WebP")
        }
        
        fun getMiniAppPlatform(): String {
            // 获取小程序平台类型
            return "wechat" // 实际实现需要检测具体平台
        }
        
        fun getImageUploadLimit(): Long {
            return when (getMiniAppPlatform()) {
                "wechat" -> 10 * 1024 * 1024L // 微信10MB
                "alipay" -> 20 * 1024 * 1024L // 支付宝20MB
                "baidu" -> 5 * 1024 * 1024L   // 百度5MB
                "toutiao" -> 10 * 1024 * 1024L // 字节跳动10MB
                else -> 5 * 1024 * 1024L
            }
        }
        
        fun supportsLazyLoading(): Boolean {
            return true // 大部分小程序平台支持懒加载
        }
        
        fun getNetworkType(): String {
            // 获取网络类型
            return "wifi" // 实际实现需要调用小程序API
        }
        
        fun isLowDataMode(): Boolean {
            // 检查是否为省流量模式
            return getNetworkType() in listOf("2g", "3g")
        }
    }
}

/**
 * 小程序平台的异步图片加载实现
 */
actual suspend fun loadImageFromUrl(url: String): Painter {
    return withContext(Dispatchers.Main) {
        try {
            // 使用小程序原生图片加载API
            val imageData = when {
                url.startsWith("http") -> {
                    // 使用wx.downloadFile下载网络图片
                    downloadMiniAppImage(url)
                }
                url.startsWith("/") -> {
                    // 加载本地资源
                    loadMiniAppResource(url)
                }
                else -> {
                    // 相对路径资源
                    loadMiniAppResource("/" + url)
                }
            }
            
            imageData?.let { BitmapPainter(it) } ?: ColorPainter(Color.Gray)
        } catch (e: Exception) {
            ColorPainter(Color.Gray)
        }
    }
}

/**
 * 小程序平台的原生图片组件适配器
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
            // 模拟小程序图片加载
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
