package com.unify.cache

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

data class AndroidMemoryInfo(
    override val totalMemory: Long,
    override val availableMemory: Long,
    override val usedMemory: Long,
    val heapSize: Long,
    val heapUsed: Long,
    val heapFree: Long
) : MemoryInfo

private lateinit var applicationContext: Context

fun initializeAndroidCache(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getCurrentMemoryInfo(): MemoryInfo {
    val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    
    val runtime = Runtime.getRuntime()
    val heapSize = runtime.totalMemory()
    val heapFree = runtime.freeMemory()
    val heapUsed = heapSize - heapFree
    
    return AndroidMemoryInfo(
        totalMemory = memoryInfo.totalMem,
        availableMemory = memoryInfo.availMem,
        usedMemory = memoryInfo.totalMem - memoryInfo.availMem,
        heapSize = heapSize,
        heapUsed = heapUsed,
        heapFree = heapFree
    )
}

actual fun clearCaches() {
    try {
        // Clear system caches
        System.gc()
        
        // Clear application cache if possible
        val cacheDir = applicationContext.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    } catch (e: Exception) {
        // Handle cache clearing errors gracefully
    }
}
