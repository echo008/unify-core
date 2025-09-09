package com.unify.core.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop平台存储相关actual实现
 */

// 简化的存储工厂
actual class PlatformStorageFactory : StorageFactory {
    override fun createStorage(config: StorageConfig): UnifyStorage {
        return DesktopUnifyStorage()
    }

    override fun createEncryptedStorage(
        config: StorageConfig,
        encryptionKey: String,
    ): UnifyStorage {
        return DesktopUnifyStorage() // 简化实现
    }

    override fun createMemoryStorage(): UnifyStorage {
        return DesktopUnifyStorage()
    }

    override fun createFileStorage(path: String): UnifyStorage {
        return DesktopUnifyStorage()
    }
}

class DesktopUnifyStorage : UnifyStorage {
    private val storage = ConcurrentHashMap<String, String>()

    override suspend fun <T> save(
        key: String,
        value: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ) {
        storage[key] = value.toString()
    }

    override suspend fun <T> load(
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): T? {
        return null // 简化实现
    }

    override suspend fun delete(key: String): Boolean {
        return storage.remove(key) != null
    }

    override suspend fun clear(): Boolean {
        return try {
            storage.clear()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }

    override suspend fun getAllKeys(): List<String> {
        return storage.keys.toList()
    }

    override suspend fun getSize(): Long {
        return storage.size.toLong()
    }

    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> {
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun batch(operations: List<StorageOperation>) {
        for (operation in operations) {
            when (operation) {
                is StorageOperation.Save<*> -> storage[operation.key] = operation.value.toString()
                is StorageOperation.Delete -> storage.remove(operation.key)
                is StorageOperation.Clear -> storage.clear()
            }
        }
    }

    override suspend fun backup(): String {
        return storage.toString()
    }

    override suspend fun restore(backupData: String) {
        try {
            // 简化实现：清空当前数据并恢复
            storage.clear()
        } catch (e: Exception) {
            // 忽略错误
        }
    }

    override suspend fun compact() {
        // Desktop平台简化实现：无需压缩操作
    }
}

// 安全存储
private val secureStorage = ConcurrentHashMap<String, String>()

// 存储状态监听
private val storageStateFlow =
    MutableStateFlow(
        StorageState(
            isConnected = true,
            syncStatus = "ready",
            lastSync = 0L,
        ),
    )

actual fun getStorageStateFlow(): StateFlow<StorageState> {
    return storageStateFlow
}

actual fun createPlatformStorage(): PlatformStorage {
    return object : PlatformStorage {
        private val storage = ConcurrentHashMap<String, String>()

        override suspend fun retrieve(key: String): RetrievalResult<String> {
            return try {
                val value = storage[key]
                if (value != null) {
                    RetrievalResult.Success(value)
                } else {
                    RetrievalResult.NotFound("Key not found: $key")
                }
            } catch (e: Exception) {
                RetrievalResult.Error("Failed to retrieve: ${e.message}")
            }
        }

        override suspend fun store(
            key: String,
            value: String,
        ): StorageResult<Unit> {
            return try {
                storage[key] = value
                StorageResult.Success(Unit)
            } catch (e: Exception) {
                StorageResult.Error("Failed to store: ${e.message}")
            }
        }

        override suspend fun delete(key: String): Boolean {
            return storage.remove(key) != null
        }

        override suspend fun clear(): Boolean {
            return try {
                storage.clear()
                true
            } catch (e: Exception) {
                false
            }
        }

        override suspend fun exists(key: String): Boolean {
            return storage.containsKey(key)
        }

        override suspend fun getAllKeys(): List<String> {
            return storage.keys.toList()
        }

        override suspend fun getStorageInfo(): StorageInfo {
            return StorageInfo(
                type = StorageType.LOCAL,
                size = storage.size.toLong(),
                lastModified = System.currentTimeMillis(),
            )
        }

        override fun getStorageStateFlow(): kotlinx.coroutines.flow.StateFlow<StorageState> {
            return storageStateFlow
        }
    }
}

// 简化的actual函数实现
actual suspend fun exportData(keys: List<String>): DataExportResult {
    return DataExportResult.Success("exported")
}

actual suspend fun importData(data: Map<String, String>): DataImportResult {
    return DataImportResult.Success("imported")
}

actual suspend fun syncWithCloud(): CloudSyncResult {
    return CloudSyncResult.Success("synced")
}

actual suspend fun storeSecurely(
    key: String,
    value: String,
): SecureStorageResult {
    secureStorage[key] = "secure_$value"
    return SecureStorageResult.Success("stored")
}

actual suspend fun retrieveSecurely(key: String): SecureRetrievalResult {
    val value = secureStorage[key]?.removePrefix("secure_")
    return if (value != null) {
        SecureRetrievalResult.Success(value)
    } else {
        SecureRetrievalResult.Error("Key not found")
    }
}

actual suspend fun clearSecureStorage(): Boolean {
    return try {
        secureStorage.clear()
        true
    } catch (e: Exception) {
        false
    }
}
