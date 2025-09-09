package com.unify.core.ui.tv

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.utils.TimeFormatter
import com.unify.core.utils.NumberFormatter
import com.unify.core.platform.tv.TVMediaType
import com.unify.core.platform.tv.TVPlaybackState

/**
 * TV平台工具类
 * 提供TV平台相关的实用函数和常量
 */
object TVPlatformUtils {
    
    /**
     * 格式化播放时间
     * 将毫秒转换为 HH:MM:SS 或 MM:SS 格式
     */
    fun formatPlaybackTime(timeMs: Long): String {
        return TimeFormatter.formatPlaybackTime(timeMs)
    }
    
    /**
     * 计算播放进度
     * 返回0.0到1.0之间的进度值
     */
    fun calculatePlaybackProgress(position: Long, duration: Long): Float {
        return if (duration > 0) {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    /**
     * 格式化文件大小
     * 将字节转换为可读的文件大小格式
     */
    fun formatFileSize(bytes: Long): String {
        return NumberFormatter.formatFileSize(bytes)
    }
    
    /**
     * 获取媒体类型显示名称
     */
    fun getMediaTypeDisplayName(mediaType: String): String {
        return when (mediaType.lowercase()) {
            "video" -> "视频"
            "audio" -> "音频"
            "image" -> "图片"
            else -> "未知类型"
        }
    }
    
    /**
     * 获取播放状态显示名称
     */
    fun getPlaybackStateDisplay(state: String): String {
        return when (state.lowercase()) {
            "playing" -> "播放中"
            "paused" -> "已暂停"
            "stopped" -> "已停止"
            "buffering" -> "缓冲中"
            "error" -> "播放错误"
            else -> "未知状态"
        }
    }
    
    /**
     * 判断是否为4K分辨率
     */
    fun is4KResolution(width: Int, height: Int): Boolean {
        return width >= 3840 && height >= 2160
    }
    
    /**
     * 判断是否为超宽屏
     */
    fun isUltraWideScreen(aspectRatio: String): Boolean {
        val ratio = parseAspectRatio(aspectRatio)
        return ratio > 2.0f
    }
    
    /**
     * 解析宽高比字符串
     */
    private fun parseAspectRatio(aspectRatio: String): Float {
        return try {
            val parts = aspectRatio.split(":")
            if (parts.size == 2) {
                val width = parts[0].toFloat()
                val height = parts[1].toFloat()
                width / height
            } else {
                16f / 9f // 默认16:9
            }
        } catch (e: Exception) {
            16f / 9f // 默认16:9
        }
    }
    
    /**
     * 获取推荐的网格列数
     */
    fun getRecommendedGridColumns(width: Int): Int {
        return when {
            width >= 3840 -> 6  // 4K
            width >= 1920 -> 4  // 1080p
            width >= 1280 -> 3  // 720p
            else -> 2
        }
    }
    
    /**
     * 获取推荐的字体大小
     */
    fun getRecommendedFontSize(density: Float): Float {
        return when {
            density >= 3.0f -> 24f  // 高密度
            density >= 2.0f -> 20f  // 中密度
            density >= 1.5f -> 18f  // 低密度
            else -> 16f
        }
    }
    
    /**
     * TV平台常量
     */
    object Constants {
        // 焦点动画持续时间
        const val FOCUS_ANIMATION_DURATION = 200L
        
        // 默认卡片圆角
        const val DEFAULT_CARD_CORNER_RADIUS = 12
        
        // 默认间距
        const val DEFAULT_SPACING = 16
        
        // 大间距
        const val LARGE_SPACING = 24
        
        // 小间距
        const val SMALL_SPACING = 8
        
        // 默认按钮高度
        const val DEFAULT_BUTTON_HEIGHT = 48
        
        // 大按钮高度
        const val LARGE_BUTTON_HEIGHT = 56
        
        // 媒体卡片宽度
        const val MEDIA_CARD_WIDTH = 200
        
        // 媒体卡片高度
        const val MEDIA_CARD_HEIGHT = 300
        
        // 导航面板宽度
        const val NAVIGATION_PANEL_WIDTH = 280
        
        // 最小触摸目标大小
        const val MIN_TOUCH_TARGET_SIZE = 48
    }
    
    /**
     * TV输入事件处理
     */
    object InputHandler {
        
        /**
         * 处理方向键导航
         */
        fun handleDirectionalNavigation(
            keyCode: Int,
            currentFocus: Int,
            itemCount: Int,
            columns: Int
        ): Int {
            return when (keyCode) {
                // 上键
                21 -> {
                    val newFocus = currentFocus - columns
                    if (newFocus >= 0) newFocus else currentFocus
                }
                // 下键
                22 -> {
                    val newFocus = currentFocus + columns
                    if (newFocus < itemCount) newFocus else currentFocus
                }
                // 左键
                19 -> {
                    if (currentFocus % columns > 0) currentFocus - 1 else currentFocus
                }
                // 右键
                20 -> {
                    if (currentFocus % columns < columns - 1 && currentFocus + 1 < itemCount) {
                        currentFocus + 1
                    } else {
                        currentFocus
                    }
                }
                else -> currentFocus
            }
        }
        
        /**
         * 处理媒体控制键
         */
        fun handleMediaControlKey(keyCode: Int): TVMediaAction? {
            return when (keyCode) {
                85 -> TVMediaAction.PLAY_PAUSE  // KEYCODE_MEDIA_PLAY_PAUSE
                86 -> TVMediaAction.STOP        // KEYCODE_MEDIA_STOP
                87 -> TVMediaAction.NEXT        // KEYCODE_MEDIA_NEXT
                88 -> TVMediaAction.PREVIOUS    // KEYCODE_MEDIA_PREVIOUS
                89 -> TVMediaAction.REWIND      // KEYCODE_MEDIA_REWIND
                90 -> TVMediaAction.FAST_FORWARD // KEYCODE_MEDIA_FAST_FORWARD
                else -> null
            }
        }
    }
}

/**
 * TV媒体操作枚举
 */
enum class TVMediaAction {
    PLAY_PAUSE,
    STOP,
    NEXT,
    PREVIOUS,
    REWIND,
    FAST_FORWARD,
    VOLUME_UP,
    VOLUME_DOWN,
    MUTE
}
