package com.unify.core.platform

import kotlin.js.Date

/**
 * Web/JS平台时间实现
 */
actual fun getCurrentTimeMillis(): Long = Date.now().toLong()

actual fun getNanoTime(): Long = (Date.now() * 1_000_000).toLong()

actual fun formatTimestamp(timestamp: Long, pattern: String): String {
    val date = Date(timestamp.toDouble())
    return when (pattern) {
        "yyyy-MM-dd HH:mm:ss" -> {
            val year = date.getFullYear()
            val month = (date.getMonth() + 1).toString().padStart(2, '0')
            val day = date.getDate().toString().padStart(2, '0')
            val hours = date.getHours().toString().padStart(2, '0')
            val minutes = date.getMinutes().toString().padStart(2, '0')
            val seconds = date.getSeconds().toString().padStart(2, '0')
            "$year-$month-$day $hours:$minutes:$seconds"
        }
        else -> date.toString()
    }
}

actual fun getTimezoneOffset(): Int = -Date().getTimezoneOffset()
