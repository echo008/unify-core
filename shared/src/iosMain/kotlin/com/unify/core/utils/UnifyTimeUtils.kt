package com.unify.core.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS平台时间工具实现
 */
actual object UnifyTimeUtils {
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun nanoTime(): Long {
        return (NSDate().timeIntervalSince1970 * 1_000_000_000).toLong()
    }
}
