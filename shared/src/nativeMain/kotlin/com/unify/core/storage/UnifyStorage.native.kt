package com.unify.core.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.unify.core.platform.getCurrentTimeMillis

/**
 * Native平台存储状态监控实现
 */
actual fun getStorageStateFlow(): StateFlow<StorageState> {
    return MutableStateFlow(StorageState(
        isConnected = true,
        syncStatus = "ready",
        lastSync = getCurrentTimeMillis()
    ))
}

/**
 * Native平台存储创建实现
 */
actual fun createPlatformStorage(): PlatformStorage {
    return NativePlatformStorage()
}

/**
 * Native平台存储实现
 */
private class NativePlatformStorage : PlatformStorage {
    private val data = mutableMapOf<String, String>()
    
    override suspend fun store(key: String, value: String): StorageResult<Unit> {
        data[key] = value
        return StorageResult.Success(Unit)
    }
    
    override suspend fun retrieve(key: String): RetrievalResult<String> {
        return data[key]?.let { RetrievalResult.Success(it) } 
            ?: RetrievalResult.NotFound("Key not found: $key")
    }
    
    override suspend fun delete(key: String): Boolean = data.remove(key) != null
    override suspend fun exists(key: String): Boolean = data.containsKey(key)
    override suspend fun clear(): Boolean { data.clear(); return true }
    override suspend fun getAllKeys(): List<String> = data.keys.toList()
    
    override suspend fun getStorageInfo(): StorageInfo {
        return StorageInfo(
            type = StorageType.MEMORY,
            size = data.size.toLong(),
            lastModified = getCurrentTimeMillis()
        )
    }
    
    override fun getStorageStateFlow(): StateFlow<StorageState> {
        return MutableStateFlow(StorageState(
            isConnected = true,
            syncStatus = "ready",
            lastSync = getCurrentTimeMillis()
        ))
    }
}
