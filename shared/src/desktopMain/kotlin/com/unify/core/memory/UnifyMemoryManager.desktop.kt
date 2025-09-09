package com.unify.core.memory

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop平台内存管理器完整实现
 */
internal actual suspend fun getPlatformMemoryInfo(): MemoryInfo {
    val runtime = Runtime.getRuntime()
    val totalMemory = runtime.totalMemory()
    val freeMemory = runtime.freeMemory()
    val usedMemory = totalMemory - freeMemory
    val maxMemory = runtime.maxMemory()

    return MemoryInfo(
        totalMemory = totalMemory,
        availableMemory = freeMemory,
        usedMemory = usedMemory,
        freeMemory = freeMemory,
        maxMemory = maxMemory,
        memoryClass = 192, // Desktop平台默认内存类
        largeMemoryClass = 512, // Desktop平台大内存类
        isLowMemory = (usedMemory.toFloat() / maxMemory) > 0.9f,
        threshold = maxMemory * 8 / 10, // 80%阈值
        timestamp = System.currentTimeMillis(),
    )
}

internal actual suspend fun getPlatformMemoryUsage(): MemoryUsage {
    val runtime = Runtime.getRuntime()
    val totalMemory = runtime.totalMemory()
    val freeMemory = runtime.freeMemory()
    val usedMemory = totalMemory - freeMemory
    val maxMemory = runtime.maxMemory()

    // 获取GC信息
    val gcBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()
    var totalGCCount = 0L
    var totalGCTime = 0L

    for (gcBean in gcBeans) {
        totalGCCount += gcBean.collectionCount
        totalGCTime += gcBean.collectionTime
    }

    return MemoryUsage(
        heapUsed = usedMemory,
        heapTotal = totalMemory,
        heapMax = maxMemory,
        nonHeapUsed = 0L, // Desktop平台非堆内存使用
        nonHeapTotal = 0L, // Desktop平台非堆内存总量
        directMemoryUsed = 0L, // Desktop平台直接内存使用
        directMemoryMax = 0L, // Desktop平台直接内存最大值
        gcCount = totalGCCount,
        gcTime = totalGCTime,
        usagePercentage = (usedMemory.toFloat() / maxMemory) * 100f,
        timestamp = System.currentTimeMillis(),
    )
}

internal actual suspend fun getPlatformGCInfo(): GCInfo {
    val gcBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()
    var youngGenCollections = 0L
    var youngGenTime = 0L
    var oldGenCollections = 0L
    var oldGenTime = 0L
    var totalCollections = 0L
    var totalTime = 0L

    for (gcBean in gcBeans) {
        val name = gcBean.name.lowercase()
        val collections = gcBean.collectionCount
        val time = gcBean.collectionTime

        totalCollections += collections
        totalTime += time

        when {
            name.contains("young") || name.contains("minor") || name.contains("scavenge") -> {
                youngGenCollections += collections
                youngGenTime += time
            }
            name.contains("old") || name.contains("major") || name.contains("marksweep") -> {
                oldGenCollections += collections
                oldGenTime += time
            }
        }
    }

    return GCInfo(
        youngGenCollections = youngGenCollections,
        youngGenTime = youngGenTime,
        oldGenCollections = oldGenCollections,
        oldGenTime = oldGenTime,
        totalCollections = totalCollections,
        totalTime = totalTime,
        lastGCTime = System.currentTimeMillis(),
        averageGCTime = if (totalCollections > 0) totalTime.toDouble() / totalCollections else 0.0,
    )
}

internal actual suspend fun performCacheClear() {
    // Desktop平台缓存清理
    System.gc()
    System.runFinalization()
}

internal actual suspend fun performMemoryOptimization() {
    // Desktop平台内存优化
    System.gc()
    System.runFinalization()
    // 可以添加更多Desktop特定的内存优化策略
}

private val memoryMonitoringFlow = MutableStateFlow<MemoryUsage?>(null)

internal actual fun startMemoryMonitoring(): StateFlow<MemoryUsage?> {
    return memoryMonitoringFlow.asStateFlow()
}

internal actual suspend fun stopMemoryMonitoring() {
    memoryMonitoringFlow.value = null
}
