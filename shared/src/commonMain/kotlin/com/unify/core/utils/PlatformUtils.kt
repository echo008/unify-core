package com.unify.core.utils

/**
 * 跨平台工具类
 * 提供跨平台兼容的基础功能
 */
expect object PlatformUtils {
    
    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long
    
    /**
     * 格式化字符串
     */
    fun formatString(format: String, vararg args: Any?): String
}

/**
 * 时间格式化工具
 */
object TimeFormatter {
    
    /**
     * 格式化播放时间
     * 将毫秒转换为 HH:MM:SS 或 MM:SS 格式
     */
    fun formatPlaybackTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "${minutes}:${seconds.toString().padStart(2, '0')}"
        }
    }
    
    /**
     * 格式化时间显示
     */
    fun formatTime(timestamp: Long, format: String): String {
        val hours = (timestamp / 3600000) % 24
        val minutes = (timestamp / 60000) % 60
        val seconds = (timestamp / 1000) % 60
        
        return when (format) {
            "HH:mm" -> "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
            "HH:mm:ss" -> "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            else -> "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
        }
    }
}

/**
 * 数值格式化工具
 */
object NumberFormatter {
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        val formattedSize = if (size == size.toLong().toDouble()) {
            size.toLong().toString()
        } else {
            "%.1f".replace("%.1f", "${(size * 10).toLong() / 10.0}")
        }
        
        return "$formattedSize ${units[unitIndex]}"
    }
    
    /**
     * 格式化百分比
     */
    fun formatPercentage(value: Float): String {
        val percentage = (value * 100).toInt()
        return "$percentage%"
    }
    
    /**
     * 格式化健康数据
     */
    fun formatHealthValue(value: Double, unit: String): String {
        val formattedValue = if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.1f".replace("%.1f", "${(value * 10).toLong() / 10.0}")
        }
        return "$formattedValue $unit"
    }
}
