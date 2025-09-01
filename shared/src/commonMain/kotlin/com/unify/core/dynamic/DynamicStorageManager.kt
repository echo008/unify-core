package com.unify.core.dynamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * 动态存储管理器
 * 负责本地数据持久化、缓存管理和数据同步
 */
class DynamicStorageManager {
    private val storageAdapter = createPlatformStorageAdapter()
    private val encryptionManager = EncryptionManager()
    private val compressionManager = CompressionManager()
    
    // 存储路径常量
    private companion object {
        const val CONFIG_PREFIX = "unify_config_"
        const val COMPONENT_PREFIX = "unify_component_"
        const val RESOURCE_PREFIX = "unify_resource_"
        const val ROLLBACK_PREFIX = "unify_rollback_"
        const val VERSION_KEY = "unify_current_version"
        const val PENDING_UPDATE_KEY = "unify_pending_update"
    }
    
    /**
     * 保存配置
     */
    suspend fun saveConfig(key: String, value: String) {
        try {
            val encryptedValue = encryptionManager.encrypt(value)
            storageAdapter.save("$CONFIG_PREFIX$key", encryptedValue)
            
            UnifyPerformanceMonitor.recordMetric("storage_config_saved", 1.0, "count",
                mapOf("key" to key))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_config_save_error", 1.0, "count",
                mapOf("key" to key, "error" to e.message.orEmpty()))
            throw StorageException("保存配置失败: $key", e)
        }
    }
    
    /**
     * 获取配置
     */
    suspend fun getConfig(key: String): String? {
        return try {
            val encryptedValue = storageAdapter.load("$CONFIG_PREFIX$key")
            if (encryptedValue != null) {
                encryptionManager.decrypt(encryptedValue)
            } else {
                null
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_config_load_error", 1.0, "count",
                mapOf("key" to key, "error" to e.message.orEmpty()))
            null
        }
    }
    
    /**
     * 获取所有配置
     */
    suspend fun getAllConfigs(): Map<String, String> {
        return try {
            val configs = mutableMapOf<String, String>()
            val keys = storageAdapter.getAllKeys().filter { it.startsWith(CONFIG_PREFIX) }
            
            keys.forEach { fullKey ->
                val key = fullKey.removePrefix(CONFIG_PREFIX)
                val value = getConfig(key)
                if (value != null) {
                    configs[key] = value
                }
            }
            
            configs
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_all_configs_load_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            emptyMap()
        }
    }
    
    /**
     * 清除所有配置
     */
    suspend fun clearAllConfigs() {
        try {
            val keys = storageAdapter.getAllKeys().filter { it.startsWith(CONFIG_PREFIX) }
            keys.forEach { key ->
                storageAdapter.delete(key)
            }
            
            UnifyPerformanceMonitor.recordMetric("storage_all_configs_cleared", 1.0, "count")
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_all_configs_clear_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 保存组件
     */
    suspend fun saveComponent(componentId: String, component: RemoteComponent) {
        try {
            val componentData = Json.encodeToString(component)
            val compressedData = compressionManager.compress(componentData.encodeToByteArray())
            val encryptedData = encryptionManager.encrypt(compressedData.decodeToString())
            
            storageAdapter.save("$COMPONENT_PREFIX$componentId", encryptedData)
            
            UnifyPerformanceMonitor.recordMetric("storage_component_saved", 1.0, "count",
                mapOf("component_id" to componentId))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_component_save_error", 1.0, "count",
                mapOf("component_id" to componentId, "error" to e.message.orEmpty()))
            throw StorageException("保存组件失败: $componentId", e)
        }
    }
    
    /**
     * 获取组件
     */
    suspend fun getComponent(componentId: String): RemoteComponent? {
        return try {
            val encryptedData = storageAdapter.load("$COMPONENT_PREFIX$componentId")
            if (encryptedData != null) {
                val compressedData = encryptionManager.decrypt(encryptedData).encodeToByteArray()
                val componentData = compressionManager.decompress(compressedData).decodeToString()
                Json.decodeFromString<RemoteComponent>(componentData)
            } else {
                null
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_component_load_error", 1.0, "count",
                mapOf("component_id" to componentId, "error" to e.message.orEmpty()))
            null
        }
    }
    
    /**
     * 保存组件代码
     */
    suspend fun saveComponentCode(componentId: String, code: String) {
        try {
            val encryptedCode = encryptionManager.encrypt(code)
            storageAdapter.save("${COMPONENT_PREFIX}${componentId}_code", encryptedCode)
        } catch (e: Exception) {
            throw StorageException("保存组件代码失败: $componentId", e)
        }
    }
    
    /**
     * 获取组件代码
     */
    suspend fun getComponentCode(componentId: String): String? {
        return try {
            val encryptedCode = storageAdapter.load("${COMPONENT_PREFIX}${componentId}_code")
            if (encryptedCode != null) {
                encryptionManager.decrypt(encryptedCode)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 保存组件资源
     */
    suspend fun saveComponentResources(componentId: String, resources: Map<String, String>) {
        try {
            val resourcesJson = Json.encodeToString(resources)
            val encryptedResources = encryptionManager.encrypt(resourcesJson)
            storageAdapter.save("${COMPONENT_PREFIX}${componentId}_resources", encryptedResources)
        } catch (e: Exception) {
            throw StorageException("保存组件资源失败: $componentId", e)
        }
    }
    
    /**
     * 获取组件资源
     */
    suspend fun getComponentResources(componentId: String): Map<String, String>? {
        return try {
            val encryptedResources = storageAdapter.load("${COMPONENT_PREFIX}${componentId}_resources")
            if (encryptedResources != null) {
                val resourcesJson = encryptionManager.decrypt(encryptedResources)
                Json.decodeFromString<Map<String, String>>(resourcesJson)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 保存组件元数据
     */
    suspend fun saveComponentMetadata(componentId: String, metadata: ComponentMetadata) {
        try {
            val metadataJson = Json.encodeToString(metadata)
            val encryptedMetadata = encryptionManager.encrypt(metadataJson)
            storageAdapter.save("${COMPONENT_PREFIX}${componentId}_metadata", encryptedMetadata)
        } catch (e: Exception) {
            throw StorageException("保存组件元数据失败: $componentId", e)
        }
    }
    
    /**
     * 清除所有组件
     */
    suspend fun clearAllComponents() {
        try {
            val keys = storageAdapter.getAllKeys().filter { it.startsWith(COMPONENT_PREFIX) }
            keys.forEach { key ->
                storageAdapter.delete(key)
            }
            
            UnifyPerformanceMonitor.recordMetric("storage_all_components_cleared", 1.0, "count")
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_all_components_clear_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 保存资源
     */
    suspend fun saveResource(path: String, data: ByteArray) {
        try {
            val compressedData = compressionManager.compress(data)
            val encryptedData = encryptionManager.encrypt(compressedData.decodeToString())
            storageAdapter.save("$RESOURCE_PREFIX$path", encryptedData)
            
            UnifyPerformanceMonitor.recordMetric("storage_resource_saved", 1.0, "count",
                mapOf("path" to path, "size" to data.size.toString()))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_resource_save_error", 1.0, "count",
                mapOf("path" to path, "error" to e.message.orEmpty()))
            throw StorageException("保存资源失败: $path", e)
        }
    }
    
    /**
     * 获取资源
     */
    suspend fun getResource(path: String): ByteArray? {
        return try {
            val encryptedData = storageAdapter.load("$RESOURCE_PREFIX$path")
            if (encryptedData != null) {
                val compressedData = encryptionManager.decrypt(encryptedData).encodeToByteArray()
                compressionManager.decompress(compressedData)
            } else {
                null
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_resource_load_error", 1.0, "count",
                mapOf("path" to path, "error" to e.message.orEmpty()))
            null
        }
    }
    
    /**
     * 获取所有资源
     */
    suspend fun getAllResources(): Map<String, ByteArray> {
        return try {
            val resources = mutableMapOf<String, ByteArray>()
            val keys = storageAdapter.getAllKeys().filter { it.startsWith(RESOURCE_PREFIX) }
            
            keys.forEach { fullKey ->
                val path = fullKey.removePrefix(RESOURCE_PREFIX)
                val data = getResource(path)
                if (data != null) {
                    resources[path] = data
                }
            }
            
            resources
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_all_resources_load_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            emptyMap()
        }
    }
    
    /**
     * 清除所有资源
     */
    suspend fun clearAllResources() {
        try {
            val keys = storageAdapter.getAllKeys().filter { it.startsWith(RESOURCE_PREFIX) }
            keys.forEach { key ->
                storageAdapter.delete(key)
            }
            
            UnifyPerformanceMonitor.recordMetric("storage_all_resources_cleared", 1.0, "count")
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_all_resources_clear_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 保存回滚点
     */
    suspend fun saveRollbackPoint(rollbackPoint: RollbackPoint) {
        try {
            val rollbackData = Json.encodeToString(rollbackPoint)
            val compressedData = compressionManager.compress(rollbackData.encodeToByteArray())
            val encryptedData = encryptionManager.encrypt(compressedData.decodeToString())
            
            storageAdapter.save("$ROLLBACK_PREFIX${rollbackPoint.id}", encryptedData)
            
            UnifyPerformanceMonitor.recordMetric("storage_rollback_point_saved", 1.0, "count",
                mapOf("rollback_id" to rollbackPoint.id))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_rollback_point_save_error", 1.0, "count",
                mapOf("rollback_id" to rollbackPoint.id, "error" to e.message.orEmpty()))
            throw StorageException("保存回滚点失败: ${rollbackPoint.id}", e)
        }
    }
    
    /**
     * 获取所有回滚点
     */
    suspend fun getAllRollbackPoints(): List<RollbackPoint> {
        return try {
            val rollbackPoints = mutableListOf<RollbackPoint>()
            val keys = storageAdapter.getAllKeys().filter { it.startsWith(ROLLBACK_PREFIX) }
            
            keys.forEach { key ->
                val encryptedData = storageAdapter.load(key)
                if (encryptedData != null) {
                    try {
                        val compressedData = encryptionManager.decrypt(encryptedData).encodeToByteArray()
                        val rollbackData = compressionManager.decompress(compressedData).decodeToString()
                        val rollbackPoint = Json.decodeFromString<RollbackPoint>(rollbackData)
                        rollbackPoints.add(rollbackPoint)
                    } catch (e: Exception) {
                        // 忽略损坏的回滚点
                    }
                }
            }
            
            rollbackPoints
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_all_rollback_points_load_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            emptyList()
        }
    }
    
    /**
     * 删除回滚点
     */
    suspend fun deleteRollbackPoint(rollbackPointId: String) {
        try {
            storageAdapter.delete("$ROLLBACK_PREFIX$rollbackPointId")
            
            UnifyPerformanceMonitor.recordMetric("storage_rollback_point_deleted", 1.0, "count",
                mapOf("rollback_id" to rollbackPointId))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_rollback_point_delete_error", 1.0, "count",
                mapOf("rollback_id" to rollbackPointId, "error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 保存更新包
     */
    suspend fun saveUpdatePackage(version: String, packageData: ByteArray) {
        try {
            val compressedData = compressionManager.compress(packageData)
            val encryptedData = encryptionManager.encrypt(compressedData.decodeToString())
            storageAdapter.save("update_package_$version", encryptedData)
            
            UnifyPerformanceMonitor.recordMetric("storage_update_package_saved", 1.0, "count",
                mapOf("version" to version, "size" to packageData.size.toString()))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_update_package_save_error", 1.0, "count",
                mapOf("version" to version, "error" to e.message.orEmpty()))
            throw StorageException("保存更新包失败: $version", e)
        }
    }
    
    /**
     * 获取待处理更新
     */
    suspend fun getPendingUpdate(): HotUpdateInfo? {
        return try {
            val encryptedData = storageAdapter.load(PENDING_UPDATE_KEY)
            if (encryptedData != null) {
                val updateData = encryptionManager.decrypt(encryptedData)
                Json.decodeFromString<HotUpdateInfo>(updateData)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 设置当前版本
     */
    suspend fun setCurrentVersion(version: String) {
        try {
            storageAdapter.save(VERSION_KEY, version)
        } catch (e: Exception) {
            throw StorageException("设置当前版本失败: $version", e)
        }
    }
    
    /**
     * 获取当前版本
     */
    suspend fun getCurrentVersion(): String? {
        return try {
            storageAdapter.load(VERSION_KEY)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 清理过期数据
     */
    suspend fun cleanupExpiredData(maxAge: Long = 7 * 24 * 60 * 60 * 1000) { // 7天
        try {
            val currentTime = System.currentTimeMillis()
            val allKeys = storageAdapter.getAllKeys()
            var cleanedCount = 0
            
            allKeys.forEach { key ->
                val lastModified = storageAdapter.getLastModified(key)
                if (lastModified != null && (currentTime - lastModified) > maxAge) {
                    storageAdapter.delete(key)
                    cleanedCount++
                }
            }
            
            UnifyPerformanceMonitor.recordMetric("storage_cleanup_completed", 1.0, "count",
                mapOf("cleaned_count" to cleanedCount.toString()))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("storage_cleanup_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 获取存储统计信息
     */
    suspend fun getStorageStats(): StorageStats {
        return try {
            val allKeys = storageAdapter.getAllKeys()
            val configCount = allKeys.count { it.startsWith(CONFIG_PREFIX) }
            val componentCount = allKeys.count { it.startsWith(COMPONENT_PREFIX) }
            val resourceCount = allKeys.count { it.startsWith(RESOURCE_PREFIX) }
            val rollbackCount = allKeys.count { it.startsWith(ROLLBACK_PREFIX) }
            
            val totalSize = storageAdapter.getTotalSize()
            
            StorageStats(
                totalKeys = allKeys.size,
                configCount = configCount,
                componentCount = componentCount,
                resourceCount = resourceCount,
                rollbackCount = rollbackCount,
                totalSize = totalSize
            )
        } catch (e: Exception) {
            StorageStats()
        }
    }
}

/**
 * 存储适配器接口
 */
interface StorageAdapter {
    suspend fun save(key: String, value: String)
    suspend fun load(key: String): String?
    suspend fun delete(key: String)
    suspend fun getAllKeys(): List<String>
    suspend fun getLastModified(key: String): Long?
    suspend fun getTotalSize(): Long
}

/**
 * 加密管理器
 */
class EncryptionManager {
    fun encrypt(data: String): String {
        // 简化的加密实现，实际应使用AES等强加密算法
        return data.reversed()
    }
    
    fun decrypt(encryptedData: String): String {
        // 简化的解密实现
        return encryptedData.reversed()
    }
}

/**
 * 压缩管理器
 */
class CompressionManager {
    fun compress(data: ByteArray): ByteArray {
        // 简化的压缩实现，实际应使用GZIP等压缩算法
        return data
    }
    
    fun decompress(compressedData: ByteArray): ByteArray {
        // 简化的解压实现
        return compressedData
    }
}

/**
 * 存储统计信息
 */
data class StorageStats(
    val totalKeys: Int = 0,
    val configCount: Int = 0,
    val componentCount: Int = 0,
    val resourceCount: Int = 0,
    val rollbackCount: Int = 0,
    val totalSize: Long = 0L
)

/**
 * 存储异常
 */
class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 创建平台存储适配器
 */
expect fun createPlatformStorageAdapter(): StorageAdapter
