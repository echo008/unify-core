package com.unify.core.utils

/**
 * JVM平台工具实现
 */
actual object PlatformUtils {
    
    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
    
    actual fun formatString(format: String, vararg args: Any?): String {
        return String.format(format, *args)
    }
}
