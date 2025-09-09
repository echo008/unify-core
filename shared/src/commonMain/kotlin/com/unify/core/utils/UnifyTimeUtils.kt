package com.unify.core.utils

/**
 * 跨平台时间工具类
 */
expect object UnifyTimeUtils {
    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long

    /**
     * 获取当前时间戳（纳秒）
     */
    fun nanoTime(): Long
}
