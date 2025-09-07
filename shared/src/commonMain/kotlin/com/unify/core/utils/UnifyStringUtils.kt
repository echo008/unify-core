package com.unify.core.utils

/**
 * 跨平台字符串工具类
 */
expect object UnifyStringUtils {
    /**
     * 格式化字符串
     */
    fun format(format: String, vararg args: Any?): String
}
