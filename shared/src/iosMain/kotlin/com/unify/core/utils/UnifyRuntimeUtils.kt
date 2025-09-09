package com.unify.core.utils

import platform.Foundation.NSProcessInfo

/**
 * iOS平台运行时工具实现
 */
actual object UnifyRuntimeUtils {
    actual fun getAvailableMemory(): Long {
        // iOS平台获取可用内存的简化实现
        return NSProcessInfo.processInfo.physicalMemory.toLong() / 2
    }

    actual fun getTotalMemory(): Long {
        return NSProcessInfo.processInfo.physicalMemory.toLong()
    }

    actual fun gc() {
        // iOS平台没有显式垃圾回收
    }

    actual fun getMaxMemory(): Long {
        return NSProcessInfo.processInfo.physicalMemory.toLong()
    }

    actual fun getThreadCount(): Int {
        return 1 // iOS平台简化实现
    }

    actual fun getSystemLoadAverage(): Double {
        return 0.5 // iOS平台简化实现
    }

    actual fun availableProcessors(): Int {
        return 4 // iOS平台默认值
    }
}
