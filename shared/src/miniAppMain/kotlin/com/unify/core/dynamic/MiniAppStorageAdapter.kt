package com.unify.core.dynamic

import kotlinx.coroutines.*

/**
 * 小程序平台存储适配器
 * 使用小程序的Storage API进行数据持久化
 */
class MiniAppStorageAdapter : StorageAdapter {
    private val keyPrefix = "unify_dynamic_"
    private val timestampSuffix = "_timestamp"
    
    override suspend fun save(key: String, value: String) {
        withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                
                if (value.length > MAX_STORAGE_SIZE) {
                    // 大数据分片存储
                    val chunks = value.chunked(MAX_STORAGE_SIZE)
                    
                    // 保存分片信息
                    setStorageSync("${prefixedKey}_chunks", chunks.size.toString())
                    
                    // 保存每个分片
                    chunks.forEachIndexed { index, chunk ->
                        setStorageSync("${prefixedKey}_chunk_$index", chunk)
                    }
                    
                    // 标记为分片存储
                    setStorageSync(prefixedKey, "chunked:${chunks.size}")
                } else {
                    // 小数据直接存储
                    setStorageSync(prefixedKey, value)
                }
                
                // 保存时间戳
                setStorageSync("$prefixedKey$timestampSuffix", System.currentTimeMillis().toString())
                
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_save", 1.0, "count",
                    mapOf("key" to key, "size" to value.length.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_save_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                throw StorageException("小程序存储保存失败: $key", e)
            }
        }
    }
    
    override suspend fun load(key: String): String? {
        return withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                val value = getStorageSync(prefixedKey)
                
                if (value?.startsWith("chunked:") == true) {
                    // 从分片读取
                    val chunkCount = value.removePrefix("chunked:").toIntOrNull() ?: 0
                    val chunks = mutableListOf<String>()
                    
                    for (i in 0 until chunkCount) {
                        val chunk = getStorageSync("${prefixedKey}_chunk_$i")
                        if (chunk != null) {
                            chunks.add(chunk)
                        } else {
                            return@withContext null // 分片缺失
                        }
                    }
                    
                    chunks.joinToString("")
                } else {
                    value
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_load_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    override suspend fun delete(key: String) {
        withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                val value = getStorageSync(prefixedKey)
                
                if (value?.startsWith("chunked:") == true) {
                    // 删除所有分片
                    val chunkCount = value.removePrefix("chunked:").toIntOrNull() ?: 0
                    
                    for (i in 0 until chunkCount) {
                        removeStorageSync("${prefixedKey}_chunk_$i")
                    }
                    
                    removeStorageSync("${prefixedKey}_chunks")
                }
                
                // 删除主数据和时间戳
                removeStorageSync(prefixedKey)
                removeStorageSync("$prefixedKey$timestampSuffix")
                
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_delete", 1.0, "count",
                    mapOf("key" to key))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_delete_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.Main) {
            try {
                val storageInfo = getStorageInfoSync()
                val keys = storageInfo["keys"] as? Array<String> ?: emptyArray()
                
                keys.filter { key ->
                    key.startsWith(keyPrefix) && 
                    !key.endsWith(timestampSuffix) &&
                    !key.contains("_chunk_") &&
                    !key.endsWith("_chunks")
                }.map { it.removePrefix(keyPrefix) }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_get_keys_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                emptyList()
            }
        }
    }
    
    override suspend fun getLastModified(key: String): Long? {
        return withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                val timestamp = getStorageSync("$prefixedKey$timestampSuffix")
                timestamp?.toLongOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getTotalSize(): Long {
        return withContext(Dispatchers.Main) {
            try {
                val storageInfo = getStorageInfoSync()
                val currentSize = storageInfo["currentSize"] as? Number
                currentSize?.toLong() ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    /**
     * 同步设置存储
     */
    private fun setStorageSync(key: String, value: String) {
        js("""
            try {
                if (typeof wx !== 'undefined') {
                    // 微信小程序
                    wx.setStorageSync(key, value);
                } else if (typeof my !== 'undefined') {
                    // 支付宝小程序
                    my.setStorageSync({ key: key, data: value });
                } else if (typeof tt !== 'undefined') {
                    // 字节跳动小程序
                    tt.setStorageSync(key, value);
                } else if (typeof swan !== 'undefined') {
                    // 百度小程序
                    swan.setStorageSync(key, value);
                } else if (typeof qq !== 'undefined') {
                    // QQ小程序
                    qq.setStorageSync(key, value);
                } else {
                    // 通用实现
                    if (typeof localStorage !== 'undefined') {
                        localStorage.setItem(key, value);
                    }
                }
            } catch (e) {
                throw new Error('小程序存储设置失败: ' + e.message);
            }
        """)
    }
    
    /**
     * 同步获取存储
     */
    private fun getStorageSync(key: String): String? {
        return try {
            js("""
                try {
                    if (typeof wx !== 'undefined') {
                        // 微信小程序
                        return wx.getStorageSync(key) || null;
                    } else if (typeof my !== 'undefined') {
                        // 支付宝小程序
                        const result = my.getStorageSync({ key: key });
                        return result.data || null;
                    } else if (typeof tt !== 'undefined') {
                        // 字节跳动小程序
                        return tt.getStorageSync(key) || null;
                    } else if (typeof swan !== 'undefined') {
                        // 百度小程序
                        return swan.getStorageSync(key) || null;
                    } else if (typeof qq !== 'undefined') {
                        // QQ小程序
                        return qq.getStorageSync(key) || null;
                    } else {
                        // 通用实现
                        if (typeof localStorage !== 'undefined') {
                            return localStorage.getItem(key);
                        }
                        return null;
                    }
                } catch (e) {
                    return null;
                }
            """) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 同步删除存储
     */
    private fun removeStorageSync(key: String) {
        js("""
            try {
                if (typeof wx !== 'undefined') {
                    // 微信小程序
                    wx.removeStorageSync(key);
                } else if (typeof my !== 'undefined') {
                    // 支付宝小程序
                    my.removeStorageSync({ key: key });
                } else if (typeof tt !== 'undefined') {
                    // 字节跳动小程序
                    tt.removeStorageSync(key);
                } else if (typeof swan !== 'undefined') {
                    // 百度小程序
                    swan.removeStorageSync(key);
                } else if (typeof qq !== 'undefined') {
                    // QQ小程序
                    qq.removeStorageSync(key);
                } else {
                    // 通用实现
                    if (typeof localStorage !== 'undefined') {
                        localStorage.removeItem(key);
                    }
                }
            } catch (e) {
                console.log('小程序存储删除失败:', e);
            }
        """)
    }
    
    /**
     * 获取存储信息
     */
    private fun getStorageInfoSync(): Map<String, Any> {
        return try {
            val info = js("""
                try {
                    if (typeof wx !== 'undefined') {
                        // 微信小程序
                        return wx.getStorageInfoSync();
                    } else if (typeof my !== 'undefined') {
                        // 支付宝小程序
                        return my.getStorageInfoSync();
                    } else if (typeof tt !== 'undefined') {
                        // 字节跳动小程序
                        return tt.getStorageInfoSync();
                    } else if (typeof swan !== 'undefined') {
                        // 百度小程序
                        return swan.getStorageInfoSync();
                    } else if (typeof qq !== 'undefined') {
                        // QQ小程序
                        return qq.getStorageInfoSync();
                    } else {
                        // 通用实现
                        const keys = [];
                        let currentSize = 0;
                        if (typeof localStorage !== 'undefined') {
                            for (let i = 0; i < localStorage.length; i++) {
                                const key = localStorage.key(i);
                                if (key) {
                                    keys.push(key);
                                    const value = localStorage.getItem(key) || '';
                                    currentSize += key.length + value.length;
                                }
                            }
                        }
                        return { keys: keys, currentSize: currentSize, limitSize: 10485760 };
                    }
                } catch (e) {
                    return { keys: [], currentSize: 0, limitSize: 10485760 };
                }
            """)
            
            @Suppress("UNCHECKED_CAST")
            info as Map<String, Any>
        } catch (e: Exception) {
            mapOf("keys" to emptyArray<String>(), "currentSize" to 0, "limitSize" to 10485760)
        }
    }
    
    /**
     * 清理过期数据
     */
    suspend fun cleanupExpiredData(maxAge: Long = 7 * 24 * 60 * 60 * 1000) {
        withContext(Dispatchers.Main) {
            try {
                val currentTime = System.currentTimeMillis()
                var cleanedCount = 0
                
                val keys = getAllKeys()
                keys.forEach { key ->
                    val timestamp = getLastModified(key)
                    if (timestamp != null && (currentTime - timestamp) > maxAge) {
                        delete(key)
                        cleanedCount++
                    }
                }
                
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_cleanup", 1.0, "count",
                    mapOf("cleaned_items" to cleanedCount.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("miniapp_storage_cleanup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
            }
        }
    }
    
    /**
     * 获取存储使用情况
     */
    suspend fun getStorageUsage(): StorageUsage {
        return withContext(Dispatchers.Main) {
            try {
                val storageInfo = getStorageInfoSync()
                val currentSize = (storageInfo["currentSize"] as? Number)?.toLong() ?: 0L
                val limitSize = (storageInfo["limitSize"] as? Number)?.toLong() ?: 10485760L // 10MB默认限制
                val keys = (storageInfo["keys"] as? Array<String>)?.size ?: 0
                
                StorageUsage(
                    currentSize = currentSize,
                    limitSize = limitSize,
                    usagePercentage = if (limitSize > 0) (currentSize.toDouble() / limitSize * 100) else 0.0,
                    keyCount = keys
                )
            } catch (e: Exception) {
                StorageUsage(0L, 10485760L, 0.0, 0)
            }
        }
    }
    
    companion object {
        private const val MAX_STORAGE_SIZE = 1024 * 1024 // 1MB per item
    }
}

/**
 * 存储使用情况
 */
data class StorageUsage(
    val currentSize: Long,
    val limitSize: Long,
    val usagePercentage: Double,
    val keyCount: Int
)

/**
 * 创建小程序平台存储适配器
 */
actual fun createPlatformStorageAdapter(): StorageAdapter {
    return MiniAppStorageAdapter()
}
