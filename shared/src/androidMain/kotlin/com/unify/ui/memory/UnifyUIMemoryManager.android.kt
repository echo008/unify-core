package com.unify.ui.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

/**
 * Android平台内存信息实现
 */
actual fun getPlatformMemoryInfo(): PlatformMemoryInfo {
    return try {
        val runtime = Runtime.getRuntime()
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)

        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()

        PlatformMemoryInfo(
            totalMemory = maxMemory,
            usedMemory = usedMemory,
            availableMemory = maxMemory - usedMemory,
            gcCount = getGCCount(),
        )
    } catch (e: Exception) {
        // 返回默认值
        PlatformMemoryInfo(
            totalMemory = 512 * 1024 * 1024L, // 512MB
            usedMemory = 256 * 1024 * 1024L, // 256MB
            availableMemory = 256 * 1024 * 1024L,
            gcCount = 0,
        )
    }
}

/**
 * Android平台垃圾回收请求
 */
actual fun requestGarbageCollection() {
    try {
        System.gc()
        Runtime.getRuntime().gc()
    } catch (e: Exception) {
        // 忽略异常
    }
}

/**
 * 获取GC次数（Android特定）
 */
private fun getGCCount(): Int {
    return try {
        // 在实际实现中可以通过JMX或其他方式获取GC统计信息
        // 这里返回一个模拟值
        (System.currentTimeMillis() / 10000).toInt() % 100
    } catch (e: Exception) {
        0
    }
}

/**
 * Android特定的内存管理工具
 */
object AndroidMemoryUtils {
    /**
     * 获取应用内存使用情况
     */
    fun getAppMemoryInfo(context: Context): AndroidAppMemoryInfo? {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val processMemoryInfo = activityManager.getProcessMemoryInfo(intArrayOf(android.os.Process.myPid()))
            val pmi = processMemoryInfo.firstOrNull()

            AndroidAppMemoryInfo(
                totalRAM = memoryInfo.totalMem,
                availableRAM = memoryInfo.availMem,
                isLowMemory = memoryInfo.lowMemory,
                threshold = memoryInfo.threshold,
                pss = pmi?.totalPss?.toLong() ?: 0L,
                privateClean = pmi?.totalPrivateClean?.toLong() ?: 0L,
                privateDirty = pmi?.totalPrivateDirty?.toLong() ?: 0L,
                sharedClean = pmi?.totalSharedClean?.toLong() ?: 0L,
                sharedDirty = pmi?.totalSharedDirty?.toLong() ?: 0L,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查是否处于低内存状态
     */
    fun isLowMemory(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.lowMemory
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取内存类别
     */
    fun getMemoryClass(context: Context): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.memoryClass
        } catch (e: Exception) {
            64 // 默认64MB
        }
    }

    /**
     * 获取大内存类别
     */
    fun getLargeMemoryClass(context: Context): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.largeMemoryClass
        } catch (e: Exception) {
            256 // 默认256MB
        }
    }

    /**
     * 触发内存整理
     */
    fun trimMemory(level: Int = android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
        try {
            System.gc()
            Runtime.getRuntime().gc()
            // 在实际应用中，这里会调用Application的onTrimMemory
        } catch (e: Exception) {
            // 忽略异常
        }
    }
}

/**
 * Android应用内存信息
 */
data class AndroidAppMemoryInfo(
    val totalRAM: Long,
    val availableRAM: Long,
    val isLowMemory: Boolean,
    val threshold: Long,
    val pss: Long, // Proportional Set Size
    val privateClean: Long,
    val privateDirty: Long,
    val sharedClean: Long,
    val sharedDirty: Long,
) {
    val totalUsed: Long get() = pss
    val privateTotal: Long get() = privateClean + privateDirty
    val sharedTotal: Long get() = sharedClean + sharedDirty
}
