package com.unify.core.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

/**
 * 统一存储接口
 * 提供跨平台的数据存储和管理功能
 */
interface UnifyStorage {
    
    /**
     * 保存数据
     */
    suspend fun <T> save(key: String, value: T, serializer: KSerializer<T>)
    
    /**
     * 加载数据
     */
    suspend fun <T> load(key: String, serializer: KSerializer<T>): T?
    
    /**
     * 删除数据
     */
    suspend fun delete(key: String)
    
    /**
     * 清空所有数据
     */
    suspend fun clear()
    
    /**
     * 检查键是否存在
     */
    suspend fun exists(key: String): Boolean
    
    /**
     * 获取所有键
     */
    suspend fun getAllKeys(): Set<String>
    
    /**
     * 获取存储大小
     */
    suspend fun getSize(): Long
    
    /**
     * 观察存储变化
     */
    fun observeChanges(): Flow<StorageEvent>
    
    /**
     * 批量操作
     */
    suspend fun batch(operations: List<StorageOperation>)
    
    /**
     * 备份数据
     */
    suspend fun backup(): String
    
    /**
     * 恢复数据
     */
    suspend fun restore(backupData: String)
    
    /**
     * 压缩存储
     */
    suspend fun compact()
}

/**
 * 存储操作类型
 */
sealed class StorageOperation {
    data class Save<T>(val key: String, val value: T, val serializer: KSerializer<T>) : StorageOperation()
    data class Delete(val key: String) : StorageOperation()
    object Clear : StorageOperation()
}

/**
 * 存储事件
 */
sealed class StorageEvent {
    data class KeyAdded(val key: String) : StorageEvent()
    data class KeyUpdated(val key: String) : StorageEvent()
    data class KeyDeleted(val key: String) : StorageEvent()
    object Cleared : StorageEvent()
}

/**
 * 存储配置
 */
data class StorageConfig(
    val name: String,
    val encrypted: Boolean = false,
    val maxSize: Long = Long.MAX_VALUE,
    val compressionEnabled: Boolean = false,
    val backupEnabled: Boolean = true,
    val syncEnabled: Boolean = false
)

/**
 * 存储统计信息
 */
data class StorageStats(
    val totalKeys: Int,
    val totalSize: Long,
    val lastModified: Long,
    val compressionRatio: Float = 1.0f
)

/**
 * 存储异常
 */
class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 存储工厂接口
 */
interface StorageFactory {
    fun createStorage(config: StorageConfig): UnifyStorage
    fun createEncryptedStorage(config: StorageConfig, encryptionKey: String): UnifyStorage
    fun createMemoryStorage(): UnifyStorage
    fun createFileStorage(path: String): UnifyStorage
}

/**
 * 平台特定的存储工厂
 */
expect class PlatformStorageFactory() : StorageFactory

/**
 * 存储管理器
 */
class StorageManager {
    private val storages = mutableMapOf<String, UnifyStorage>()
    private val factory = PlatformStorageFactory()
    
    /**
     * 获取或创建存储实例
     */
    fun getStorage(name: String, config: StorageConfig? = null): UnifyStorage {
        return storages.getOrPut(name) {
            val storageConfig = config ?: StorageConfig(name)
            factory.createStorage(storageConfig)
        }
    }
    
    /**
     * 获取加密存储实例
     */
    fun getEncryptedStorage(name: String, encryptionKey: String, config: StorageConfig? = null): UnifyStorage {
        val key = "${name}_encrypted"
        return storages.getOrPut(key) {
            val storageConfig = config ?: StorageConfig(name, encrypted = true)
            factory.createEncryptedStorage(storageConfig, encryptionKey)
        }
    }
    
    /**
     * 获取内存存储实例
     */
    fun getMemoryStorage(name: String): UnifyStorage {
        val key = "${name}_memory"
        return storages.getOrPut(key) {
            factory.createMemoryStorage()
        }
    }
    
    /**
     * 移除存储实例
     */
    suspend fun removeStorage(name: String) {
        storages.remove(name)?.clear()
    }
    
    /**
     * 清理所有存储
     */
    suspend fun clearAll() {
        storages.values.forEach { it.clear() }
        storages.clear()
    }
    
    /**
     * 获取所有存储统计
     */
    suspend fun getAllStats(): Map<String, StorageStats> {
        return storages.mapValues { (_, storage) ->
            StorageStats(
                totalKeys = storage.getAllKeys().size,
                totalSize = storage.getSize(),
                lastModified = System.currentTimeMillis()
            )
        }
    }
}

/**
 * 全局存储管理器实例
 */
object GlobalStorageManager {
    private val manager = StorageManager()
    
    fun getStorage(name: String = "default", config: StorageConfig? = null): UnifyStorage {
        return manager.getStorage(name, config)
    }
    
    fun getEncryptedStorage(name: String, encryptionKey: String, config: StorageConfig? = null): UnifyStorage {
        return manager.getEncryptedStorage(name, encryptionKey, config)
    }
    
    fun getMemoryStorage(name: String = "memory"): UnifyStorage {
        return manager.getMemoryStorage(name)
    }
    
    suspend fun clearAll() {
        manager.clearAll()
    }
}
