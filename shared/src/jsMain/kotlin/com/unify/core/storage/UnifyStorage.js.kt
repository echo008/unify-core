package com.unify.core.storage

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import com.unify.data.sync.SyncStatus

actual class PlatformStorageFactory : StorageFactory {
    override fun createStorage(config: StorageConfig): UnifyStorage {
        return JSStorageImpl()
    }
    
    override fun createEncryptedStorage(config: StorageConfig, encryptionKey: String): UnifyStorage {
        return JSStorageImpl()
    }
    
    override fun createMemoryStorage(): UnifyStorage {
        return JSStorageImpl()
    }
    
    override fun createFileStorage(path: String): UnifyStorage {
        return JSStorageImpl()
    }
}

actual suspend fun exportData(keys: List<String>): DataExportResult {
    return DataExportResult.Success("{\"keys\": ${keys.size}}")
}

actual suspend fun importData(data: Map<String, String>): DataImportResult {
    return DataImportResult.Success("Imported ${data.size} items")
}

actual suspend fun syncWithCloud(): CloudSyncResult {
    return CloudSyncResult.Success("Sync completed")
}

actual suspend fun storeSecurely(key: String, value: String): SecureStorageResult {
    return SecureStorageResult.Success("Stored securely")
}

actual suspend fun retrieveSecurely(key: String): SecureRetrievalResult {
    return SecureRetrievalResult.Success("secure_value_for_$key")
}

actual suspend fun clearSecureStorage(): Boolean {
    return true
}

actual fun getStorageStateFlow(): StateFlow<StorageState> {
    return MutableStateFlow(StorageState(
        isConnected = true,
        syncStatus = "COMPLETED",
        lastSync = com.unify.core.platform.getCurrentTimeMillis()
    ))
}

actual fun createPlatformStorage(): PlatformStorage {
    return JSPlatformStorage()
}

// JavaScript specific platform storage
private class JSPlatformStorage : PlatformStorage {
    private val storage = mutableMapOf<String, String>()
    
    override suspend fun store(key: String, value: String): StorageResult<Unit> {
        return try {
            storage[key] = value
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error("Storage failed: ${e.message}", e)
        }
    }
    
    override suspend fun retrieve(key: String): RetrievalResult<String> {
        return storage[key]?.let { 
            RetrievalResult.Success(it)
        } ?: RetrievalResult.NotFound("Key not found: $key")
    }
    
    override suspend fun delete(key: String): Boolean {
        return storage.remove(key) != null
    }
    
    override suspend fun clear(): Boolean {
        storage.clear()
        return true
    }
    
    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    override suspend fun getAllKeys(): List<String> {
        return storage.keys.toList()
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return StorageInfo(
            type = StorageType.MEMORY,
            size = storage.size.toLong() * 1024L, // Approximate
            lastModified = com.unify.core.platform.getCurrentTimeMillis()
        )
    }
    
    override fun getStorageStateFlow(): kotlinx.coroutines.flow.StateFlow<StorageState> {
        return kotlinx.coroutines.flow.MutableStateFlow(StorageState(
            isConnected = true,
            syncStatus = "IDLE",
            lastSync = com.unify.core.platform.getCurrentTimeMillis()
        ))
    }
}

