package com.unify.core.utils

import platform.Foundation.*

/**
 * iOS平台工具实现
 */
actual object PlatformUtils {
    
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
    
    actual fun formatString(format: String, vararg args: Any?): String {
        // iOS/Native平台的简化字符串格式化实现
        var result = format
        args.forEachIndexed { index, arg ->
            val placeholder = when {
                format.contains("%d") -> "%d"
                format.contains("%s") -> "%s"
                format.contains("%f") -> "%f"
                format.contains("%.1f") -> "%.1f"
                format.contains("%02d") -> "%02d"
                else -> "%s"
            }
            
            val replacement = when (arg) {
                is Int -> arg.toString()
                is Long -> arg.toString()
                is Float -> arg.toString()
                is Double -> arg.toString()
                else -> arg.toString()
            }
            
            result = result.replaceFirst(placeholder, replacement)
        }
        return result
    }
}
