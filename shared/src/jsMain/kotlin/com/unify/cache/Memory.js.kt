package com.unify.cache

actual fun getCurrentMemoryInfo(): MemoryInfo {
    // JS 无法稳定获取内存信息，返回占位值
    return MemoryInfo(totalMemory = 0, usedMemory = 0, usagePercentage = 0f)
}

actual fun clearCaches() {
    // JS 平台暂无全局缓存注册，保持空实现
}
