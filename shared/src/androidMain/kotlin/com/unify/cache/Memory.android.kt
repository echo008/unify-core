package com.unify.cache

import android.app.ActivityManager
import android.content.Context
import com.unify.platform.AndroidAppContextHolder

private val globalCaches = mutableListOf<LRUCache<*, *>>()

fun registerGlobalCache(cache: LRUCache<*, *>) {
    globalCaches += cache
}

actual fun getCurrentMemoryInfo(): MemoryInfo {
    val ctx: Context = AndroidAppContextHolder.context
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    val total = mi.totalMem
    val avail = mi.availMem
    val used = total - avail
    val percent = if (total > 0) used.toFloat() / total.toFloat() else 0f
    return MemoryInfo(totalMemory = total, usedMemory = used, usagePercentage = percent)
}

actual fun clearCaches() {
    globalCaches.forEach {
        @Suppress("UNCHECKED_CAST")
        (it as LRUCache<Any, Any>).clear()
    }
}
