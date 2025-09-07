package com.unify.core.utils

import kotlin.js.Date

/**
 * JS平台时间工具实现
 */
actual object UnifyTimeUtils {
    actual fun currentTimeMillis(): Long {
        return Date.now().toLong()
    }
    
    actual fun nanoTime(): Long {
        return (Date.now() * 1_000_000).toLong()
    }
}
