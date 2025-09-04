package com.unify.ui.memory

import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.OperatingSystemMXBean

/**
 * Desktop平台内存信息实现
 */
actual fun getPlatformMemoryInfo(): PlatformMemoryInfo {
    return try {
        val memoryBean = ManagementFactory.getMemoryMXBean()
        val heapMemory = memoryBean.heapMemoryUsage
        val nonHeapMemory = memoryBean.nonHeapMemoryUsage
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        
        val totalMemory = heapMemory.max + nonHeapMemory.max
        val usedMemory = heapMemory.used + nonHeapMemory.used
        
        PlatformMemoryInfo(
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            availableMemory = totalMemory - usedMemory,
            gcCount = getGCCount()
        )
    } catch (e: Exception) {
        // 使用Runtime作为备选方案
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        PlatformMemoryInfo(
            totalMemory = runtime.maxMemory(),
            usedMemory = usedMemory,
            availableMemory = runtime.maxMemory() - usedMemory,
            gcCount = 0
        )
    }
}

/**
 * Desktop平台垃圾回收请求
 */
actual fun requestGarbageCollection() {
    try {
        System.gc()
        Runtime.getRuntime().gc()
        
        // 等待一小段时间让GC完成
        Thread.sleep(100)
        
        // 再次触发finalization
        System.runFinalization()
    } catch (e: Exception) {
        // 忽略异常
    }
}

/**
 * 获取GC次数
 */
private fun getGCCount(): Int {
    return try {
        val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
        gcBeans.sumOf { it.collectionCount }.toInt()
    } catch (e: Exception) {
        0
    }
}

/**
 * Desktop特定的内存管理工具
 */
object DesktopMemoryUtils {
    
    /**
     * 获取详细内存信息
     */
    fun getDetailedMemoryInfo(): DesktopMemoryInfo {
        return try {
            val memoryBean = ManagementFactory.getMemoryMXBean()
            val heapMemory = memoryBean.heapMemoryUsage
            val nonHeapMemory = memoryBean.nonHeapMemoryUsage
            val osBean = ManagementFactory.getOperatingSystemMXBean()
            
            DesktopMemoryInfo(
                heapMemory = HeapMemoryInfo(
                    init = heapMemory.init,
                    used = heapMemory.used,
                    committed = heapMemory.committed,
                    max = heapMemory.max
                ),
                nonHeapMemory = NonHeapMemoryInfo(
                    init = nonHeapMemory.init,
                    used = nonHeapMemory.used,
                    committed = nonHeapMemory.committed,
                    max = nonHeapMemory.max
                ),
                systemMemory = getSystemMemoryInfo(osBean),
                gcInfo = getGCInfo()
            )
        } catch (e: Exception) {
            // 返回基本信息
            val runtime = Runtime.getRuntime()
            DesktopMemoryInfo(
                heapMemory = HeapMemoryInfo(
                    init = runtime.totalMemory(),
                    used = runtime.totalMemory() - runtime.freeMemory(),
                    committed = runtime.totalMemory(),
                    max = runtime.maxMemory()
                ),
                nonHeapMemory = NonHeapMemoryInfo(0, 0, 0, 0),
                systemMemory = SystemMemoryInfo(0, 0),
                gcInfo = emptyList()
            )
        }
    }
    
    /**
     * 获取系统内存信息
     */
    private fun getSystemMemoryInfo(osBean: OperatingSystemMXBean): SystemMemoryInfo {
        return try {
            // 尝试获取系统级内存信息
            val totalPhysicalMemory = when {
                osBean is com.sun.management.OperatingSystemMXBean -> {
                    osBean.totalPhysicalMemorySize
                }
                else -> 0L
            }
            
            val freePhysicalMemory = when {
                osBean is com.sun.management.OperatingSystemMXBean -> {
                    osBean.freePhysicalMemorySize
                }
                else -> 0L
            }
            
            SystemMemoryInfo(
                totalPhysicalMemory = totalPhysicalMemory,
                freePhysicalMemory = freePhysicalMemory
            )
        } catch (e: Exception) {
            SystemMemoryInfo(0, 0)
        }
    }
    
    /**
     * 获取GC信息
     */
    private fun getGCInfo(): List<GCInfo> {
        return try {
            val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
            gcBeans.map { gcBean ->
                GCInfo(
                    name = gcBean.name,
                    collectionCount = gcBean.collectionCount,
                    collectionTime = gcBean.collectionTime,
                    memoryPoolNames = gcBean.memoryPoolNames?.toList() ?: emptyList()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取内存池信息
     */
    fun getMemoryPoolInfo(): List<MemoryPoolInfo> {
        return try {
            val memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans()
            memoryPoolBeans.map { poolBean ->
                val usage = poolBean.usage
                MemoryPoolInfo(
                    name = poolBean.name,
                    type = poolBean.type.toString(),
                    init = usage?.init ?: 0,
                    used = usage?.used ?: 0,
                    committed = usage?.committed ?: 0,
                    max = usage?.max ?: 0,
                    peakUsed = poolBean.peakUsage?.used ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 监控内存使用情况
     */
    fun startMemoryMonitoring(intervalMs: Long = 5000, callback: (DesktopMemoryInfo) -> Unit) {
        Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val memoryInfo = getDetailedMemoryInfo()
                    callback(memoryInfo)
                    Thread.sleep(intervalMs)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    // 继续监控
                }
            }
        }.start()
    }
    
    /**
     * 设置内存使用阈值警告
     */
    fun setMemoryThresholdWarning(thresholdPercentage: Double, callback: () -> Unit) {
        try {
            val memoryBean = ManagementFactory.getMemoryMXBean()
            
            // 在实际实现中会设置内存阈值通知
            Thread {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        val heapUsage = memoryBean.heapMemoryUsage
                        val usagePercentage = heapUsage.used.toDouble() / heapUsage.max.toDouble()
                        
                        if (usagePercentage > thresholdPercentage) {
                            callback()
                        }
                        
                        Thread.sleep(2000)
                    } catch (e: InterruptedException) {
                        break
                    } catch (e: Exception) {
                        // 继续监控
                    }
                }
            }.start()
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 强制执行完整GC
     */
    fun forceFullGC() {
        try {
            // 多次调用GC以确保完整清理
            repeat(3) {
                System.gc()
                Runtime.getRuntime().gc()
                Thread.sleep(100)
            }
            
            System.runFinalization()
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 获取JVM参数
     */
    fun getJVMArguments(): List<String> {
        return try {
            val runtimeBean = ManagementFactory.getRuntimeMXBean()
            runtimeBean.inputArguments
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Desktop内存信息
 */
data class DesktopMemoryInfo(
    val heapMemory: HeapMemoryInfo,
    val nonHeapMemory: NonHeapMemoryInfo,
    val systemMemory: SystemMemoryInfo,
    val gcInfo: List<GCInfo>
)

/**
 * 堆内存信息
 */
data class HeapMemoryInfo(
    val init: Long,
    val used: Long,
    val committed: Long,
    val max: Long
) {
    val usagePercentage: Double get() = if (max > 0) used.toDouble() / max.toDouble() else 0.0
    val available: Long get() = max - used
}

/**
 * 非堆内存信息
 */
data class NonHeapMemoryInfo(
    val init: Long,
    val used: Long,
    val committed: Long,
    val max: Long
)

/**
 * 系统内存信息
 */
data class SystemMemoryInfo(
    val totalPhysicalMemory: Long,
    val freePhysicalMemory: Long
) {
    val usedPhysicalMemory: Long get() = totalPhysicalMemory - freePhysicalMemory
}

/**
 * GC信息
 */
data class GCInfo(
    val name: String,
    val collectionCount: Long,
    val collectionTime: Long,
    val memoryPoolNames: List<String>
)

/**
 * 内存池信息
 */
data class MemoryPoolInfo(
    val name: String,
    val type: String,
    val init: Long,
    val used: Long,
    val committed: Long,
    val max: Long,
    val peakUsed: Long
)
