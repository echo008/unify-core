package com.unify.core.platform

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Desktop平台时间工具actual实现
 */
actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun getNanoTime(): Long {
    return System.nanoTime()
}

actual fun formatTimestamp(
    timestamp: Long,
    pattern: String,
): String {
    return try {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern(pattern)
        instant.atOffset(ZoneOffset.UTC).format(formatter)
    } catch (e: Exception) {
        timestamp.toString()
    }
}

actual fun getTimezoneOffset(): Int {
    return ZoneOffset.systemDefault().rules.getOffset(Instant.now()).totalSeconds / 60
}
