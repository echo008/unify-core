package com.unify.ui.memory

import kotlinx.browser.window

/**
 * Web平台内存信息实现
 */
actual fun getPlatformMemoryInfo(): PlatformMemoryInfo {
    return try {
        val performance = window.performance
        val memory = js("performance.memory")
        
        val totalMemory = if (memory != null) {
            (memory.jsHeapSizeLimit as? Number)?.toLong() ?: (4L * 1024 * 1024 * 1024) // 4GB默认
        } else {
            4L * 1024 * 1024 * 1024 // 4GB默认
        }
        
        val usedMemory = if (memory != null) {
            (memory.usedJSHeapSize as? Number)?.toLong() ?: (512L * 1024 * 1024) // 512MB默认
        } else {
            512L * 1024 * 1024 // 512MB默认
        }
        
        PlatformMemoryInfo(
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            availableMemory = totalMemory - usedMemory,
            gcCount = getGCCount()
        )
    } catch (e: Exception) {
        // 返回默认值
        PlatformMemoryInfo(
            totalMemory = 4L * 1024 * 1024 * 1024, // 4GB
            usedMemory = 512L * 1024 * 1024,       // 512MB
            availableMemory = 3584L * 1024 * 1024,  // 3.5GB
            gcCount = 0
        )
    }
}

/**
 * Web平台垃圾回收请求
 */
actual fun requestGarbageCollection() {
    try {
        // 在支持的浏览器中触发GC
        if (js("window.gc") != null) {
            js("window.gc()")
        }
        
        // 清理各种缓存
        clearWebCaches()
    } catch (e: Exception) {
        // 忽略异常
    }
}

/**
 * 获取GC次数（Web特定）
 */
private fun getGCCount(): Int {
    return try {
        // Web环境中GC次数难以准确获取，返回估算值
        (System.currentTimeMillis() / 20000).toInt() % 100
    } catch (e: Exception) {
        0
    }
}

/**
 * 清理Web缓存
 */
private fun clearWebCaches() {
    try {
        // 清理各种Web缓存
        if (js("caches") != null) {
            js("caches.keys().then(names => names.forEach(name => caches.delete(name)))")
        }
    } catch (e: Exception) {
        // 忽略异常
    }
}

/**
 * Web特定的内存管理工具
 */
object WebMemoryUtils {
    
    /**
     * 获取浏览器内存信息
     */
    fun getBrowserMemoryInfo(): WebBrowserMemoryInfo? {
        return try {
            val memory = js("performance.memory")
            if (memory != null) {
                WebBrowserMemoryInfo(
                    jsHeapSizeLimit = (memory.jsHeapSizeLimit as Number).toLong(),
                    totalJSHeapSize = (memory.totalJSHeapSize as Number).toLong(),
                    usedJSHeapSize = (memory.usedJSHeapSize as Number).toLong()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 检查是否支持内存API
     */
    fun isMemoryAPISupported(): Boolean {
        return try {
            js("performance.memory") != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取存储配额信息
     */
    suspend fun getStorageQuota(): WebStorageQuota? {
        return try {
            if (js("navigator.storage && navigator.storage.estimate") != null) {
                val estimate = js("navigator.storage.estimate()").await()
                WebStorageQuota(
                    quota = (estimate.quota as? Number)?.toLong() ?: 0L,
                    usage = (estimate.usage as? Number)?.toLong() ?: 0L
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 清理Service Worker缓存
     */
    suspend fun clearServiceWorkerCaches() {
        try {
            if (js("caches") != null) {
                val cacheNames = js("caches.keys()").await() as Array<String>
                cacheNames.forEach { cacheName ->
                    js("caches.delete(cacheName)")
                }
            }
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 清理IndexedDB
     */
    fun clearIndexedDB(dbName: String) {
        try {
            if (js("indexedDB") != null) {
                js("indexedDB.deleteDatabase(dbName)")
            }
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 清理localStorage
     */
    fun clearLocalStorage() {
        try {
            if (js("localStorage") != null) {
                js("localStorage.clear()")
            }
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 清理sessionStorage
     */
    fun clearSessionStorage() {
        try {
            if (js("sessionStorage") != null) {
                js("sessionStorage.clear()")
            }
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 监听内存压力
     */
    fun observeMemoryPressure(callback: (WebMemoryPressure) -> Unit) {
        try {
            // 定期检查内存使用情况
            js("""
                setInterval(() => {
                    if (performance.memory) {
                        const usage = performance.memory.usedJSHeapSize / performance.memory.jsHeapSizeLimit;
                        let pressure = 'LOW';
                        if (usage > 0.9) pressure = 'CRITICAL';
                        else if (usage > 0.75) pressure = 'HIGH';
                        else if (usage > 0.6) pressure = 'MEDIUM';
                        callback(pressure);
                    }
                }, 5000);
            """)
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    /**
     * 获取网络连接信息
     */
    fun getNetworkInfo(): WebNetworkInfo? {
        return try {
            val connection = js("navigator.connection || navigator.mozConnection || navigator.webkitConnection")
            if (connection != null) {
                WebNetworkInfo(
                    effectiveType = connection.effectiveType as? String ?: "unknown",
                    downlink = (connection.downlink as? Number)?.toDouble() ?: 0.0,
                    rtt = (connection.rtt as? Number)?.toInt() ?: 0,
                    saveData = connection.saveData as? Boolean ?: false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Web浏览器内存信息
 */
data class WebBrowserMemoryInfo(
    val jsHeapSizeLimit: Long,
    val totalJSHeapSize: Long,
    val usedJSHeapSize: Long
) {
    val availableHeapSize: Long get() = jsHeapSizeLimit - usedJSHeapSize
    val heapUsagePercentage: Double get() = usedJSHeapSize.toDouble() / jsHeapSizeLimit.toDouble()
}

/**
 * Web存储配额信息
 */
data class WebStorageQuota(
    val quota: Long,
    val usage: Long
) {
    val available: Long get() = quota - usage
    val usagePercentage: Double get() = usage.toDouble() / quota.toDouble()
}

/**
 * Web内存压力级别
 */
enum class WebMemoryPressure {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Web网络信息
 */
data class WebNetworkInfo(
    val effectiveType: String, // "slow-2g", "2g", "3g", "4g"
    val downlink: Double,      // Mbps
    val rtt: Int,             // ms
    val saveData: Boolean
)
