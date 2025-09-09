package com.unify.network.cache

import com.unify.core.utils.UnifyTimeUtils
import kotlinx.serialization.json.Json

/**
 * 网络缓存实现
 * 提供内存缓存和持久化缓存功能
 */
class UnifyNetworkCacheImpl {
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 存储缓存数据
     */
    fun put(
        key: String,
        data: String,
        ttl: Long = 300000L,
    ) {
        val entry =
            CacheEntry(
                data = data,
                timestamp = UnifyTimeUtils.currentTimeMillis(),
                ttl = ttl,
            )
        memoryCache[key] = entry
    }

    /**
     * 获取缓存数据
     */
    fun get(key: String): String? {
        val entry = memoryCache[key] ?: return null

        if (isExpired(key)) {
            memoryCache.remove(key)
            return null
        }

        return entry.data
    }

    /**
     * 检查缓存是否过期
     */
    fun isExpired(
        key: String,
        customTtl: Long? = null,
    ): Boolean {
        val entry = memoryCache[key] ?: return true
        val ttl = customTtl ?: entry.ttl
        return UnifyTimeUtils.currentTimeMillis() - entry.timestamp > ttl
    }

    /**
     * 删除指定缓存
     */
    fun remove(key: String) {
        memoryCache.remove(key)
    }

    /**
     * 清空所有缓存
     */
    fun clear() {
        memoryCache.clear()
    }

    /**
     * 获取缓存大小
     */
    fun size(): Int = memoryCache.size

    /**
     * 获取所有缓存键
     */
    fun keys(): Set<String> = memoryCache.keys.toSet()

    /**
     * 清理过期缓存
     */
    fun cleanExpired() {
        val expiredKeys =
            memoryCache.filter { (key, _) ->
                isExpired(key)
            }.keys

        expiredKeys.forEach { key ->
            memoryCache.remove(key)
        }
    }

    /**
     * 获取缓存统计信息
     */
    fun getStats(): CacheStats {
        val totalSize = memoryCache.values.sumOf { it.data.length }
        val expiredCount = memoryCache.count { (key, _) -> isExpired(key) }

        return CacheStats(
            totalEntries = memoryCache.size,
            totalSize = totalSize.toLong(),
            expiredEntries = expiredCount,
            hitRate = 0.0, // 需要实际统计
        )
    }
}

/**
 * 缓存条目
 */
@kotlinx.serialization.Serializable
private data class CacheEntry(
    val data: String,
    val timestamp: Long,
    val ttl: Long,
)

/**
 * 缓存统计信息
 */
@kotlinx.serialization.Serializable
data class CacheStats(
    val totalEntries: Int,
    val totalSize: Long,
    val expiredEntries: Int,
    val hitRate: Double,
)
