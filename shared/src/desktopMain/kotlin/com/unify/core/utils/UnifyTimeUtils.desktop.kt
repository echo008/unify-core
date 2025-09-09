package com.unify.core.utils

import java.time.*
import java.util.*

/**
 * Desktop平台时间工具类actual实现
 */

actual object UnifyTimeUtils {
    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    actual fun nanoTime(): Long {
        return System.nanoTime()
    }
}
