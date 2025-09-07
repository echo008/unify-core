package com.unify.core.dynamic
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 存储适配器接口
 */
interface StorageAdapter {
    suspend fun save(key: String, data: String): Boolean
    suspend fun load(key: String): String?
    suspend fun delete(key: String): Boolean
    suspend fun exists(key: String): Boolean
    suspend fun listKeys(prefix: String = ""): List<String>
    suspend fun clear(): Boolean
    suspend fun getSize(key: String): Long
    suspend fun getTotalSize(): Long
}

/**
 * 存储配置
 */
@Serializable
data class StorageConfig(
    val enableCompression: Boolean = true,
    val enableEncryption: Boolean = true,
    val maxCacheSize: Long = 100 * 1024 * 1024, // 100MB
    val compressionLevel: Int = 6,
    val encryptionKey: String = "",
    val enableBackup: Boolean = true,
    val backupInterval: Long = 24 * 60 * 60 * 1000L // 24小时
)

/**
 * 存储统计信息
 */
@Serializable
data class StorageStats(
    val totalSize: Long,
    val itemCount: Int,
    val compressionRatio: Double,
    val cacheHitRate: Double,
    val lastBackupTime: Long,
    val availableSpace: Long
)

/**
 * 动态存储管理器接口
 */
interface DynamicStorageManager {
    // 组件存储
    suspend fun storeComponent(component: DynamicComponent): Boolean
    suspend fun loadComponent(componentId: String): DynamicComponent?
    suspend fun removeComponent(componentId: String): Boolean
    suspend fun listComponents(): List<String>
    
    // 配置存储
    suspend fun saveConfig(key: String, value: String): Boolean
    suspend fun getConfig(key: String): String?
    suspend fun removeConfig(key: String): Boolean
    suspend fun listConfigs(prefix: String = ""): List<String>
    
    // 缓存管理
    suspend fun enableCache(maxSize: Long)
    suspend fun disableCache()
    suspend fun clearCache()
    suspend fun getCacheStats(): Map<String, Any>
    
    // 压缩和加密
    suspend fun compress(data: String): String
    suspend fun decompress(data: String): String
    suspend fun encrypt(data: String): String
    suspend fun decrypt(data: String): String
    
    // 备份和恢复
    suspend fun createBackup(): String?
    suspend fun restoreFromBackup(backupData: String): Boolean
    suspend fun listBackups(): List<String>
    
    // 存储统计
    suspend fun getStorageStats(): StorageStats
    suspend fun cleanup(): Int
    
    // 配置管理
    fun updateConfig(config: StorageConfig)
    fun getConfig(): StorageConfig
}

/**
 * 动态存储管理器实现
 */
class DynamicStorageManagerImpl(
    private val adapter: StorageAdapter
) : DynamicStorageManager {
    
    private var config = StorageConfig()
    private val cache = mutableMapOf<String, String>()
    private var cacheEnabled = false
    private var maxCacheSize = 100 * 1024 * 1024L
    private var cacheHits = 0L
    private var cacheMisses = 0L
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val COMPONENT_PREFIX = "component_"
        private const val CONFIG_PREFIX = "config_"
        private const val BACKUP_PREFIX = "backup_"
        private const val STORAGE_CONFIG_KEY = "storage_config"
        private const val COMPRESSION_MARKER = "COMPRESSED:"
        private const val ENCRYPTION_MARKER = "ENCRYPTED:"
    }
    
    override suspend fun storeComponent(component: DynamicComponent): Boolean {
        return try {
            val key = "$COMPONENT_PREFIX${component.id}"
            var data = Json.encodeToString(component)
            
            // 压缩
            if (config.enableCompression) {
                data = compress(data)
            }
            
            // 加密
            if (config.enableEncryption) {
                data = encrypt(data)
            }
            
            val success = adapter.save(key, data)
            
            // 更新缓存
            if (success && cacheEnabled) {
                updateCache(key, data)
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun loadComponent(componentId: String): DynamicComponent? {
        return try {
            val key = "$COMPONENT_PREFIX$componentId"
            
            // 先检查缓存
            var data = if (cacheEnabled) {
                cache[key]?.also { cacheHits++ }
            } else null
            
            // 从存储加载
            if (data == null) {
                data = adapter.load(key)
                if (cacheEnabled) cacheMisses++
                
                if (data != null && cacheEnabled) {
                    updateCache(key, data)
                }
            }
            
            if (data != null) {
                // 解密
                var processedData = data
                if (config.enableEncryption && processedData.startsWith(ENCRYPTION_MARKER)) {
                    processedData = decrypt(processedData)
                }
                
                // 解压缩
                if (config.enableCompression && processedData.startsWith(COMPRESSION_MARKER)) {
                    processedData = decompress(processedData)
                }
                
                Json.decodeFromString<DynamicComponent>(processedData)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun removeComponent(componentId: String): Boolean {
        return try {
            val key = "$COMPONENT_PREFIX$componentId"
            val success = adapter.delete(key)
            
            // 从缓存删除
            if (success && cacheEnabled) {
                cache.remove(key)
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun listComponents(): List<String> {
        return try {
            adapter.listKeys(COMPONENT_PREFIX).map { key ->
                key.removePrefix(COMPONENT_PREFIX)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun saveConfig(key: String, value: String): Boolean {
        return try {
            val storageKey = "$CONFIG_PREFIX$key"
            var data = value
            
            // 压缩
            if (config.enableCompression) {
                data = compress(data)
            }
            
            // 加密
            if (config.enableEncryption) {
                data = encrypt(data)
            }
            
            val success = adapter.save(storageKey, data)
            
            // 更新缓存
            if (success && cacheEnabled) {
                updateCache(storageKey, data)
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getConfig(key: String): String? {
        return try {
            val storageKey = "$CONFIG_PREFIX$key"
            
            // 先检查缓存
            var data = if (cacheEnabled) {
                cache[storageKey]?.also { cacheHits++ }
            } else null
            
            // 从存储加载
            if (data == null) {
                data = adapter.load(storageKey)
                if (cacheEnabled) cacheMisses++
                
                if (data != null && cacheEnabled) {
                    updateCache(storageKey, data)
                }
            }
            
            if (data != null) {
                // 解密
                var processedData = data
                if (config.enableEncryption && processedData.startsWith(ENCRYPTION_MARKER)) {
                    processedData = decrypt(processedData)
                }
                
                // 解压缩
                if (config.enableCompression && processedData.startsWith(COMPRESSION_MARKER)) {
                    processedData = decompress(processedData)
                }
                
                processedData
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun removeConfig(key: String): Boolean {
        return try {
            val storageKey = "$CONFIG_PREFIX$key"
            val success = adapter.delete(storageKey)
            
            // 从缓存删除
            if (success && cacheEnabled) {
                cache.remove(storageKey)
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun listConfigs(prefix: String): List<String> {
        return try {
            val searchPrefix = "$CONFIG_PREFIX$prefix"
            adapter.listKeys(searchPrefix).map { key ->
                key.removePrefix(CONFIG_PREFIX)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun enableCache(maxSize: Long) {
        cacheEnabled = true
        maxCacheSize = maxSize
        manageCacheSize()
    }
    
    override suspend fun disableCache() {
        cacheEnabled = false
        cache.clear()
    }
    
    override suspend fun clearCache() {
        cache.clear()
        cacheHits = 0
        cacheMisses = 0
    }
    
    override suspend fun getCacheStats(): Map<String, Any> {
        val totalRequests = cacheHits + cacheMisses
        val hitRate = if (totalRequests > 0) cacheHits.toDouble() / totalRequests else 0.0
        
        return mapOf(
            "enabled" to cacheEnabled,
            "size" to cache.size,
            "maxSize" to maxCacheSize,
            "hitRate" to hitRate,
            "hits" to cacheHits,
            "misses" to cacheMisses
        )
    }
    
    override suspend fun compress(data: String): String {
        return try {
            // 简化的压缩实现 - 实际应使用真正的压缩算法
            val compressed = data.encodeToByteArray().let { bytes ->
                // 这里应该使用真正的压缩算法如GZIP
                bytes.decodeToString()
            }
            "$COMPRESSION_MARKER$compressed"
        } catch (e: Exception) {
            data
        }
    }
    
    override suspend fun decompress(data: String): String {
        return try {
            if (data.startsWith(COMPRESSION_MARKER)) {
                val compressed = data.removePrefix(COMPRESSION_MARKER)
                // 这里应该使用真正的解压缩算法
                compressed
            } else {
                data
            }
        } catch (e: Exception) {
            data
        }
    }
    
    override suspend fun encrypt(data: String): String {
        return try {
            // 简化的加密实现 - 实际应使用真正的加密算法
            val encrypted = data.reversed() // 简单的字符串反转作为示例
            "$ENCRYPTION_MARKER$encrypted"
        } catch (e: Exception) {
            data
        }
    }
    
    override suspend fun decrypt(data: String): String {
        return try {
            if (data.startsWith(ENCRYPTION_MARKER)) {
                val encrypted = data.removePrefix(ENCRYPTION_MARKER)
                // 简单的字符串反转解密
                encrypted.reversed()
            } else {
                data
            }
        } catch (e: Exception) {
            data
        }
    }
    
    override suspend fun createBackup(): String? {
        return try {
            val backupId = "backup_${getCurrentTimeMillis()}"
            val backupData = mutableMapOf<String, String>()
            
            // 备份所有组件
            val componentKeys = adapter.listKeys(COMPONENT_PREFIX)
            componentKeys.forEach { key ->
                val data = adapter.load(key)
                if (data != null) {
                    backupData[key] = data
                }
            }
            
            // 备份所有配置
            val configKeys = adapter.listKeys(CONFIG_PREFIX)
            configKeys.forEach { key ->
                val data = adapter.load(key)
                if (data != null) {
                    backupData[key] = data
                }
            }
            
            // 保存备份
            val backupJson = Json.encodeToString(backupData)
            val backupKey = "$BACKUP_PREFIX$backupId"
            
            if (adapter.save(backupKey, backupJson)) {
                backupId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun restoreFromBackup(backupData: String): Boolean {
        return try {
            val dataMap: Map<String, String> = Json.decodeFromString(backupData)
            
            var allSuccess = true
            dataMap.forEach { (key, value) ->
                if (!adapter.save(key, value)) {
                    allSuccess = false
                }
            }
            
            // 清理缓存以确保数据一致性
            if (allSuccess && cacheEnabled) {
                clearCache()
            }
            
            allSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun listBackups(): List<String> {
        return try {
            adapter.listKeys(BACKUP_PREFIX).map { key ->
                key.removePrefix(BACKUP_PREFIX)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getStorageStats(): StorageStats {
        return try {
            val totalSize = adapter.getTotalSize()
            val componentKeys = adapter.listKeys(COMPONENT_PREFIX)
            val configKeys = adapter.listKeys(CONFIG_PREFIX)
            val itemCount = componentKeys.size + configKeys.size
            
            val totalRequests = cacheHits + cacheMisses
            val cacheHitRate = if (totalRequests > 0) cacheHits.toDouble() / totalRequests else 0.0
            
            StorageStats(
                totalSize = totalSize,
                itemCount = itemCount,
                compressionRatio = 0.7, // 简化实现
                cacheHitRate = cacheHitRate,
                lastBackupTime = 0L, // 简化实现
                availableSpace = Long.MAX_VALUE // 简化实现
            )
        } catch (e: Exception) {
            StorageStats(
                totalSize = 0L,
                itemCount = 0,
                compressionRatio = 1.0,
                cacheHitRate = 0.0,
                lastBackupTime = 0L,
                availableSpace = 0L
            )
        }
    }
    
    override suspend fun cleanup(): Int {
        var cleanedCount = 0
        
        try {
            // 清理过期的备份
            val backupKeys = adapter.listKeys(BACKUP_PREFIX)
            val cutoffTime = getCurrentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7天前
            
            backupKeys.forEach { key ->
                try {
                    val backupId = key.removePrefix(BACKUP_PREFIX)
                    val timestamp = backupId.removePrefix("backup_").toLongOrNull()
                    
                    if (timestamp != null && timestamp < cutoffTime) {
                        if (adapter.delete(key)) {
                            cleanedCount++
                        }
                    }
                } catch (e: Exception) {
                    // 忽略单个清理错误
                }
            }
            
            // 清理缓存中的过期项
            if (cacheEnabled) {
                manageCacheSize()
            }
            
        } catch (e: Exception) {
            // 忽略清理错误
        }
        
        return cleanedCount
    }
    
    override fun updateConfig(config: StorageConfig) {
        this.config = config
        
        // 保存配置
        scope.launch {
            try {
                val configJson = Json.encodeToString(config)
                adapter.save(STORAGE_CONFIG_KEY, configJson)
            } catch (e: Exception) {
                // 忽略保存错误
            }
        }
    }
    
    override fun getConfig(): StorageConfig = config
    
    // 私有辅助方法
    private fun updateCache(key: String, data: String) {
        if (!cacheEnabled) return
        
        cache[key] = data
        manageCacheSize()
    }
    
    private fun manageCacheSize() {
        if (!cacheEnabled) return
        
        // 简化的缓存大小管理 - 按项目数量限制
        val maxItems = (maxCacheSize / 1024).toInt() // 假设每项平均1KB
        
        while (cache.size > maxItems) {
            // 移除最旧的项（简化实现）
            val oldestKey = cache.keys.first()
            cache.remove(oldestKey)
        }
    }
}

/**
 * 平台特定存储适配器 - 需要在各平台实现
 */
expect class PlatformStorageAdapter() : StorageAdapter
