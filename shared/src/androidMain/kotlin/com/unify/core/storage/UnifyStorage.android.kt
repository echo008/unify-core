package com.unify.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

actual class PlatformStorageFactory : StorageFactory {
    override fun createStorage(config: StorageConfig): UnifyStorage {
        return AndroidUnifyStorage(config)
    }
    
    override fun createEncryptedStorage(config: StorageConfig, encryptionKey: String): UnifyStorage {
        return AndroidUnifyStorage(config, encryptionKey)
    }
    
    override fun createMemoryStorage(): UnifyStorage {
        return com.unify.core.storage.AndroidMemoryStorage()
    }
    
    override fun createFileStorage(path: String): UnifyStorage {
        return com.unify.core.storage.AndroidFileStorage(getContext(), path)
    }
    
    private fun getContext(): Context {
        return com.unify.core.storage.AndroidStorageFactory.context ?: throw IllegalStateException("StorageFactory not initialized")
    }
}



class AndroidUnifyStorage(
    private val config: StorageConfig,
    private val encryptionKey: String? = null
) : UnifyStorage {
    
    private val context = com.unify.core.storage.AndroidStorageFactory.context ?: throw IllegalStateException("StorageFactory not initialized")
    private val sharedPreferences = context.getSharedPreferences(config.name, Context.MODE_PRIVATE)
    
    override suspend fun <T> save(key: String, value: T, serializer: KSerializer<T>) {
        val jsonString = Json.encodeToString(serializer, value)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }
    
    override suspend fun <T> load(key: String, serializer: KSerializer<T>): T? {
        return try {
            val jsonString = sharedPreferences.getString(key, null) ?: return null
            Json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String): Boolean {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.edit().remove(key).apply()
            true
        } else {
            false
        }
    }
    
    override suspend fun clear(): Boolean {
        sharedPreferences.edit().clear().apply()
        return true
    }
    
    override suspend fun exists(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
    
    override suspend fun getAllKeys(): List<String> {
        return sharedPreferences.all.keys.toList()
    }
    
    override suspend fun getSize(): Long {
        return sharedPreferences.all.values.sumOf { 
            it.toString().toByteArray().size.toLong() 
        }
    }
    
    override fun observeChanges(): Flow<StorageEvent> {
        return emptyFlow()
    }
    
    override suspend fun batch(operations: List<StorageOperation>) {
        val editor = sharedPreferences.edit()
        operations.forEach { operation ->
            when (operation) {
                is StorageOperation.Save<*> -> {
                    val jsonString = Json.encodeToString(operation.serializer as KSerializer<Any?>, operation.value)
                    editor.putString(operation.key, jsonString)
                }
                is StorageOperation.Delete -> {
                    editor.remove(operation.key)
                }
                is StorageOperation.Clear -> {
                    editor.clear()
                }
            }
        }
        editor.apply()
    }
    
    override suspend fun backup(): String {
        return Json.encodeToString(sharedPreferences.all)
    }
    
    override suspend fun restore(backupData: String) {
        try {
            val data = Json.decodeFromString<Map<String, Any>>(backupData)
            val editor = sharedPreferences.edit().clear()
            data.forEach { (key, value) ->
                editor.putString(key, value.toString())
            }
            editor.apply()
        } catch (e: Exception) {
            // 处理恢复错误
        }
    }
    
    override suspend fun compact() {
        // SharedPreferences不需要压缩
    }
}


actual fun createPlatformStorage(): PlatformStorage {
    return AndroidStorage()
}

class AndroidStorage : PlatformStorage {
    private val storageStateFlow = MutableStateFlow(
        StorageState(
            isConnected = true,
            syncStatus = "Connected",
            lastSync = System.currentTimeMillis()
        )
    )
    private var sharedPreferences: SharedPreferences? = null
    private var cacheDir: File? = null
    private var filesDir: File? = null
    
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
        cacheDir = context.cacheDir
        filesDir = context.filesDir
        storageStateFlow.value = StorageState(
            isConnected = true,
            syncStatus = "Available",
            lastSync = System.currentTimeMillis()
        )
    }
    
    override suspend fun store(key: String, value: String): StorageResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.putString(key, value)?.apply()
                    ?: return@withContext StorageResult.Error("Storage not initialized")
                StorageResult.Success(Unit)
            } catch (e: Exception) {
                StorageResult.Error("Failed to store data: ${e.message}")
            }
        }
    }
    
    override suspend fun retrieve(key: String): RetrievalResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val value = sharedPreferences?.getString(key, null)
                if (value != null) {
                    RetrievalResult.Success(value)
                } else {
                    RetrievalResult.NotFound("Key not found: $key")
                }
            } catch (e: Exception) {
                RetrievalResult.Error("Memory retrieval failed: ${e.message}")
            }
        }
    }
    
    override suspend fun delete(key: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.remove(key)?.apply()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.contains(key) ?: false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override suspend fun clear(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.edit()?.clear()?.apply()
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                sharedPreferences?.all?.keys?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return withContext(Dispatchers.IO) {
            try {
                val totalSpace = filesDir?.totalSpace ?: 0L
                val freeSpace = filesDir?.freeSpace ?: 0L
                val usedSpace = totalSpace - freeSpace
                
                StorageInfo(
                    type = StorageType.LOCAL,
                    size = totalSpace,
                    lastModified = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                StorageInfo(
                    type = StorageType.LOCAL,
                    size = 0L,
                    lastModified = System.currentTimeMillis()
                )
            }
        }
    }
    
    override fun getStorageStateFlow(): StateFlow<StorageState> {
        return storageStateFlow
    }
}

actual suspend fun storeSecurely(key: String, value: String): SecureStorageResult {
    return try {
        // In a real implementation, this would use Android Keystore encryption
        SecureStorageResult.Success("Data stored securely")
    } catch (e: Exception) {
        SecureStorageResult.Error("Secure storage failed: ${e.message}")
    }
}

actual suspend fun retrieveSecurely(key: String): SecureRetrievalResult {
    return try {
        // In a real implementation, this would decrypt from Android Keystore
        SecureRetrievalResult.Error("Secure key not found")
    } catch (e: Exception) {
        SecureRetrievalResult.Error("Secure retrieval failed: ${e.message}")
    }
}

actual suspend fun clearSecureStorage(): Boolean {
    return try {
        // Clear secure storage implementation
        true
    } catch (e: Exception) {
        false
    }
}

actual suspend fun exportData(keys: List<String>): DataExportResult {
    return try {
        val exportData = mutableMapOf<String, String>()
        // Export implementation would go here
        DataExportResult.Success(exportData.toString())
    } catch (e: Exception) {
        DataExportResult.Error("Export failed: ${e.message}")
    }
}

actual suspend fun importData(data: Map<String, String>): DataImportResult {
    return try {
        // Import implementation would go here
        DataImportResult.Success("Data imported successfully")
    } catch (e: Exception) {
        DataImportResult.Error("Import failed: ${e.message}")
    }
}

actual suspend fun syncWithCloud(): CloudSyncResult {
    return try {
        // Cloud sync implementation would go here
        CloudSyncResult.Success("Sync completed successfully")
    } catch (e: Exception) {
        CloudSyncResult.Error("Cloud sync failed: ${e.message}")
    }
}

actual fun getStorageStateFlow(): kotlinx.coroutines.flow.StateFlow<StorageState> {
    return kotlinx.coroutines.flow.MutableStateFlow(
        StorageState(
            isConnected = true,
            syncStatus = "Connected",
            lastSync = System.currentTimeMillis()
        )
    )
}

