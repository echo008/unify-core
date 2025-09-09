package com.unify.core.utils

/**
 * JS平台运行时工具实现
 */
actual object UnifyRuntimeUtils {
    actual fun getAvailableMemory(): Long {
        // JS平台内存信息的简化实现
        return 512 * 1024 * 1024L // 512MB
    }

    actual fun getTotalMemory(): Long {
        return 1024 * 1024 * 1024L // 1GB
    }

    actual fun gc() {
        // JS平台没有显式垃圾回收
    }

    actual fun getMaxMemory(): Long {
        return 1024L * 1024L * 1024L // 1GB 默认值
    }

    actual fun getThreadCount(): Int {
        return 1 // JS平台单线程
    }

    actual fun getSystemLoadAverage(): Double {
        return 0.3 // JS平台默认值
    }

    actual fun availableProcessors(): Int {
        return 1 // JS平台单线程
    }
}
