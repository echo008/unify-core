package com.unify.core.utils

/**
 * Android平台字符串工具实现
 */
actual object UnifyStringUtils {
    actual fun format(format: String, vararg args: Any?): String {
        return String.format(format, *args)
    }
}
