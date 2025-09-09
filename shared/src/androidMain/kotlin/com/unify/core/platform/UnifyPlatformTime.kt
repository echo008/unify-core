package com.unify.core.platform

import java.text.SimpleDateFormat
import java.util.*

/**
 * Android平台时间实现
 */
actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

actual fun getNanoTime(): Long = System.nanoTime()

actual fun formatTimestamp(
    timestamp: Long,
    pattern: String,
): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(timestamp))
}

actual fun getTimezoneOffset(): Int {
    return TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000 * 60)
}
