package com.unify.core.utils

/**
 * Desktop平台字符串工具类actual实现
 */

actual object UnifyStringUtils {
    actual fun format(
        format: String,
        vararg args: Any?,
    ): String {
        return try {
            String.format(format, *args)
        } catch (e: Exception) {
            format
        }
    }
}
