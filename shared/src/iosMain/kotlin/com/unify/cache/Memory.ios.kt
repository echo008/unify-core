package com.unify.cache

import platform.Foundation.NSProcessInfo

actual fun getCurrentMemoryInfo(): MemoryInfo {
    val total = NSProcessInfo.processInfo.physicalMemory
    return MemoryInfo(totalMemory = total, usedMemory = 0, usagePercentage = 0f)
}

actual fun clearCaches() {
    // iOS 平台：此处仅保留空实现，具体清理逻辑可在全局缓存集中处理
}
