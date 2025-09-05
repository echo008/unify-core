package com.unify.core.platform

/**
 * 跨平台时间获取接口
 * 解决getCurrentTimeMillis()在Web/JS平台不可用的问题
 */
expect fun getCurrentTimeMillis(): Long

/**
 * 获取高精度时间戳 (纳秒)
 */
expect fun getNanoTime(): Long

/**
 * 格式化时间戳
 */
expect fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String

/**
 * 获取当前时区偏移量
 */
expect fun getTimezoneOffset(): Int
