package com.unify.core.utils

import java.util.*

/**
 * Desktop平台工具类实现
 * 提供Desktop平台特定的系统信息和工具函数
 */
actual object PlatformUtils {
    
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
    
    actual fun formatString(format: String, vararg args: Any?): String {
        return String.format(Locale.getDefault(), format, *args)
    }
}
