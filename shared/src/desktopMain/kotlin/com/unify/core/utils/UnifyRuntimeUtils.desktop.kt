package com.unify.core.utils

import java.lang.management.ManagementFactory

/**
 * Desktop平台运行时工具类actual实现
 */

actual object UnifyRuntimeUtils {
    actual fun getAvailableMemory(): Long {
        return Runtime.getRuntime().freeMemory()
    }

    actual fun getTotalMemory(): Long {
        return Runtime.getRuntime().totalMemory()
    }

    actual fun gc() {
        System.gc()
    }

    actual fun getMaxMemory(): Long {
        return Runtime.getRuntime().maxMemory()
    }

    actual fun getThreadCount(): Int {
        return ManagementFactory.getThreadMXBean().threadCount
    }

    actual fun getSystemLoadAverage(): Double {
        return ManagementFactory.getOperatingSystemMXBean().systemLoadAverage
    }

    actual fun availableProcessors(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}
