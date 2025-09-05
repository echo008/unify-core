package com.unify.core.platform

import platform.Foundation.*
import platform.darwin.NSEC_PER_MSEC

/**
 * iOS平台时间实现
 */
actual fun getCurrentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getNanoTime(): Long {
    return (NSDate().timeIntervalSince1970 * 1_000_000_000).toLong()
}

actual fun formatTimestamp(timestamp: Long, pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble() / 1000.0)
    val formatter = NSDateFormatter()
    
    val nsPattern = when (pattern) {
        "yyyy-MM-dd HH:mm:ss" -> "yyyy-MM-dd HH:mm:ss"
        else -> "yyyy-MM-dd HH:mm:ss"
    }
    
    formatter.dateFormat = nsPattern
    return formatter.stringFromDate(date)
}

actual fun getTimezoneOffset(): Int {
    return (NSTimeZone.localTimeZone.secondsFromGMT / 60).toInt()
}
