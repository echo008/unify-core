package com.unify.core.utils

/**
 * Android平台运行时工具实现
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
        return Thread.activeCount()
    }

    actual fun getSystemLoadAverage(): Double {
        return try {
            java.lang.management.ManagementFactory.getOperatingSystemMXBean().systemLoadAverage
        } catch (e: Exception) {
            0.5 // 默认值
        }
    }

    actual fun availableProcessors(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}
