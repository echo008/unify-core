package com.unify.ui.memory

/**
 * Native平台UI内存管理实现
 */

actual fun getPlatformMemoryInfo(): PlatformMemoryInfo {
    return PlatformMemoryInfo(
        // 8GB
        totalMemory = 8L * 1024 * 1024 * 1024,
        // 2GB
        usedMemory = 2L * 1024 * 1024 * 1024,
        // 6GB
        availableMemory = 6L * 1024 * 1024 * 1024,
    )
}

actual fun requestGarbageCollection() {
    // Native平台垃圾回收请求
}
