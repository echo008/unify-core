package com.unify.core.utils

/**
 * Android平台时间工具实现
 */
actual object UnifyTimeUtils {
    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    actual fun nanoTime(): Long {
        return System.nanoTime()
    }
}
