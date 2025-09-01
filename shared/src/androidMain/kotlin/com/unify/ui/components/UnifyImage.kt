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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android平台的图片实现
 */
actual class UnifyPlatformImage {
    companion object {
        fun createImageRequest(context: android.content.Context, url: String): ImageRequest {
            return ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .build()
        }
        
        fun getImageCacheSize(): Long {
            return 50 * 1024 * 1024L // 50MB
        }
        
        fun isNetworkAvailable(context: android.content.Context): Boolean {
            val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork?.isConnectedOrConnecting == true
        }
        
        fun getImageFormat(url: String): String {
            return when {
                url.endsWith(".jpg", true) || url.endsWith(".jpeg", true) -> "JPEG"
                url.endsWith(".png", true) -> "PNG"
                url.endsWith(".webp", true) -> "WebP"
                url.endsWith(".gif", true) -> "GIF"
                url.endsWith(".svg", true) -> "SVG"
                else -> "Unknown"
            }
        }
    }
}

/**
 * Android平台的异步图片加载实现
 */
actual suspend fun loadImageFromUrl(url: String): Painter {
    return withContext(Dispatchers.IO) {
        try {
            // 使用Android原生BitmapFactory加载图片
            val inputStream = when {
                url.startsWith("http") -> {
                    val connection = java.net.URL(url).openConnection()
                    connection.inputStream
                }
                url.startsWith("file://") -> {
                    java.io.FileInputStream(url.removePrefix("file://"))
                }
                else -> {
                    // 从assets或resources加载
                    null
                }
            }
            
            inputStream?.use {
                android.graphics.BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Android平台的原生图片组件适配器
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
    val context = LocalContext.current
    
    // 使用Coil的AsyncImage组件
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale,
        placeholder = if (placeholder != null) {
            null // Coil会处理占位符
        } else {
            rememberAsyncImagePainter(model = android.R.drawable.ic_menu_gallery)
        },
        error = if (error != null) {
            null // Coil会处理错误状态
        } else {
            rememberAsyncImagePainter(model = android.R.drawable.ic_menu_report_image)
        }
    )
}
