package com.unify.core.platform

import kotlinx.datetime.*

/**
 * Native平台时间实现
 */
actual fun getCurrentTimeMillis(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

actual fun getNanoTime(): Long {
    return Clock.System.now().nanosecondsOfSecond.toLong()
}

actual fun formatTimestamp(timestamp: Long, pattern: String): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    
    // 简化的格式化实现
    return when (pattern) {
        "yyyy-MM-dd HH:mm:ss" -> {
            "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')} " +
            "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
        }
        "yyyy-MM-dd" -> {
            "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
        }
        "HH:mm:ss" -> {
            "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
        }
        else -> localDateTime.toString()
    }
}

actual fun getTimezoneOffset(): Int {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    return timeZone.offsetAt(now).totalSeconds / 60 // 返回分钟偏移量
}
