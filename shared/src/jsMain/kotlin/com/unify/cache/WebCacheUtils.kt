package com.unify.cache

import kotlinx.browser.window
import kotlin.js.Date

data class WebMemoryInfo(
    override val totalMemory: Long,
    override val availableMemory: Long,
    override val usedMemory: Long,
    val jsHeapSizeLimit: Long,
    val totalJSHeapSize: Long,
    val usedJSHeapSize: Long
) : MemoryInfo

actual fun getCurrentMemoryInfo(): MemoryInfo {
    val performance = window.performance
    val memory = performance.asDynamic().memory
    
    val jsHeapSizeLimit = (memory?.jsHeapSizeLimit as? Double)?.toLong() ?: 0L
    val totalJSHeapSize = (memory?.totalJSHeapSize as? Double)?.toLong() ?: 0L
    val usedJSHeapSize = (memory?.usedJSHeapSize as? Double)?.toLong() ?: 0L
    
    // Estimate total system memory (not directly available in web)
    val estimatedTotalMemory = jsHeapSizeLimit * 4 // Rough estimate
    val availableMemory = jsHeapSizeLimit - usedJSHeapSize
    
    return WebMemoryInfo(
        totalMemory = estimatedTotalMemory,
        availableMemory = availableMemory,
        usedMemory = usedJSHeapSize,
        jsHeapSizeLimit = jsHeapSizeLimit,
        totalJSHeapSize = totalJSHeapSize,
        usedJSHeapSize = usedJSHeapSize
    )
}

actual fun clearCaches() {
    try {
        // Clear localStorage
        window.localStorage.clear()
        
        // Clear sessionStorage
        window.sessionStorage.clear()
        
        // Clear IndexedDB (basic approach)
        val indexedDB = window.indexedDB
        if (indexedDB != null) {
            // This is a simplified approach - in practice you'd want to enumerate and clear specific databases
            console.log("IndexedDB cache clearing would be implemented here")
        }
        
        // Clear browser caches (limited in web environment)
        if (window.caches != undefined) {
            val caches = window.caches
            // Note: This requires HTTPS and service worker context
            console.log("Cache API clearing would be implemented here")
        }
        
        // Suggest garbage collection (browser-dependent)
        if (window.gc != undefined) {
            window.gc()
        }
        
    } catch (e: Exception) {
        // Handle cache clearing errors gracefully
        console.error("Error clearing caches: ${e.message}")
    }
}
