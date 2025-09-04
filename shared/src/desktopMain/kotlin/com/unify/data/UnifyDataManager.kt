package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.prefs.Preferences

/**
 * Desktop平台数据管理器实现
 * 基于Java Preferences API和文件系统实现数据持久化
 */
class DesktopUnifyDataManager : UnifyDataManager {
    
    private val preferences = Preferences.userNodeForPackage(DesktopUnifyDataManager::class.java)
    private val dataDirectory = File(System.getProperty("user.home"), ".unify-core/data")
    private val _connectionState = MutableStateFlow(DataConnectionState.CONNECTED)
    
    init {
        // 确保数据目录存在
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
    }
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        try {
            val jsonString = Json.encodeToString(serializer, value)
            
            // 小数据存储到Preferences
            if (jsonString.length < 8192) {
                preferences.put(key, jsonString)
                preferences.flush()
            } else {
                // 大数据存储到文件
                val file = File(dataDirectory, "$key.json")
                file.writeText(jsonString)
            }
        } catch (e: Exception) {
            throw DataException("Failed to save data for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        return try {
            // 先尝试从Preferences读取
            val prefValue = preferences.get(key, null)
            if (prefValue != null) {
                Json.decodeFromString(serializer, prefValue)
            } else {
                // 再尝试从文件读取
                val file = File(dataDirectory, "$key.json")
                if (file.exists()) {
                    val jsonString = file.readText()
                    Json.decodeFromString(serializer, jsonString)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        try {
            // 从Preferences删除
            preferences.remove(key)
            preferences.flush()
            
            // 从文件系统删除
            val file = File(dataDirectory, "$key.json")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            throw DataException("Failed to delete data for key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            // 清空Preferences
            preferences.clear()
            preferences.flush()
            
            // 清空数据目录
            dataDirectory.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".json")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to clear all data", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return try {
            preferences.get(key, null) != null || 
            File(dataDirectory, "$key.json").exists()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return try {
            val prefKeys = preferences.keys().toSet()
            val fileKeys = dataDirectory.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".json") }
                ?.map { it.nameWithoutExtension }
                ?.toSet() ?: emptySet()
            
            prefKeys + fileKeys
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override fun observeConnectionState(): Flow<DataConnectionState> {
        return _connectionState.asStateFlow()
    }
    
    override suspend fun sync() {
        // Desktop平台不需要同步，数据直接存储在本地
        _connectionState.value = DataConnectionState.CONNECTED
    }
    
    override suspend fun backup(): String {
        return try {
            val backupDir = File(dataDirectory.parent, "backup-${System.currentTimeMillis()}")
            backupDir.mkdirs()
            
            // 备份Preferences
            val prefsBackup = mutableMapOf<String, String>()
            preferences.keys().forEach { key ->
                prefsBackup[key] = preferences.get(key, "")
            }
            val prefsFile = File(backupDir, "preferences.json")
            prefsFile.writeText(Json.encodeToString(prefsBackup))
            
            // 备份数据文件
            dataDirectory.listFiles()?.forEach { file ->
                if (file.isFile) {
                    Files.copy(file.toPath(), Paths.get(backupDir.absolutePath, file.name))
                }
            }
            
            backupDir.absolutePath
        } catch (e: Exception) {
            throw DataException("Failed to create backup", e)
        }
    }
    
    override suspend fun restore(backupPath: String) {
        try {
            val backupDir = File(backupPath)
            if (!backupDir.exists() || !backupDir.isDirectory) {
                throw DataException("Invalid backup path: $backupPath")
            }
            
            // 恢复Preferences
            val prefsFile = File(backupDir, "preferences.json")
            if (prefsFile.exists()) {
                val prefsBackup = Json.decodeFromString<Map<String, String>>(prefsFile.readText())
                preferences.clear()
                prefsBackup.forEach { (key, value) ->
                    preferences.put(key, value)
                }
                preferences.flush()
            }
            
            // 恢复数据文件
            backupDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name != "preferences.json") {
                    Files.copy(file.toPath(), Paths.get(dataDirectory.absolutePath, file.name))
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to restore from backup: $backupPath", e)
        }
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return try {
            val totalSpace = dataDirectory.totalSpace
            val freeSpace = dataDirectory.freeSpace
            val usedSpace = totalSpace - freeSpace
            
            StorageInfo(
                totalSpace = totalSpace,
                usedSpace = usedSpace,
                freeSpace = freeSpace,
                cacheSize = calculateCacheSize()
            )
        } catch (e: Exception) {
            StorageInfo(0, 0, 0, 0)
        }
    }
    
    private fun calculateCacheSize(): Long {
        return try {
            dataDirectory.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Desktop平台数据管理器工厂
 */
actual fun createUnifyDataManager(): UnifyDataManager {
    return DesktopUnifyDataManager()
}
