package com.unify.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

class LRUCache<K, V>(private val maxSize: Int) {
    private val cache = linkedMapOf<K, V>()

    fun get(key: K): V? {
        val value = cache.remove(key)
        return if (value != null) {
            cache[key] = value
            value
        } else null
    }

    fun put(key: K, value: V) {
        if (cache.containsKey(key)) {
            cache.remove(key)
        } else if (cache.size >= maxSize) {
            val firstKey = cache.keys.first()
            cache.remove(firstKey)
        }
        cache[key] = value
    }

    fun clear() { cache.clear() }
    fun size(): Int = cache.size
}

data class MemoryInfo(
    val totalMemory: Long = 0,
    val usedMemory: Long = 0,
    val usagePercentage: Float = 0f
)

@Composable
fun MemoryMonitor() {
    val memoryInfo = remember { mutableStateOf(MemoryInfo()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            memoryInfo.value = getCurrentMemoryInfo()
        }
    }
    LaunchedEffect(memoryInfo.value) {
        if (memoryInfo.value.usagePercentage > 0.8f) {
            clearCaches()
        }
    }
}

expect fun getCurrentMemoryInfo(): MemoryInfo
expect fun clearCaches()
