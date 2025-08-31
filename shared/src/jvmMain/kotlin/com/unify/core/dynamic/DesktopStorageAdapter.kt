package com.unify.core.dynamic

import kotlinx.coroutines.*
import java.io.File
import java.util.prefs.Preferences
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

/**
 * Desktop平台存储适配器
 * 使用Java Preferences和文件系统进行数据持久化
 */
class DesktopStorageAdapter : StorageAdapter {
    private val preferences = Preferences.userNodeForPackage(DesktopStorageAdapter::class.java)
    private val userHome = System.getProperty("user.home")
    private val dynamicDir = File("$userHome/.unify/dynamic")
    
    init {
        // 确保目录存在
        if (!dynamicDir.exists()) {
            dynamicDir.mkdirs()
        }
    }
    
    override suspend fun save(key: String, value: String) {
        withContext(Dispatchers.IO) {
            try {
                if (value.length > MAX_PREFERENCES_SIZE) {
                    // 大数据存储到文件
                    val file = File(dynamicDir, key)
                    file.writeText(value, Charsets.UTF_8)
                    
                    // 在Preferences中记录文件路径
                    preferences.put(key, "file:${file.absolutePath}")
                    preferences.putLong("${key}_timestamp", System.currentTimeMillis())
                    preferences.flush()
                } else {
                    // 小数据直接存储到Preferences
                    preferences.put(key, value)
                    preferences.putLong("${key}_timestamp", System.currentTimeMillis())
                    preferences.flush()
                }
                
                UnifyPerformanceMonitor.recordMetric("desktop_storage_save", 1.0, "count",
                    mapOf("key" to key, "size" to value.length.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_save_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                throw StorageException("Desktop存储保存失败: $key", e)
            }
        }
    }
    
    override suspend fun load(key: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val value = preferences.get(key, null)
                
                if (value?.startsWith("file:") == true) {
                    // 从文件读取
                    val filePath = value.removePrefix("file:")
                    val file = File(filePath)
                    if (file.exists()) {
                        file.readText(Charsets.UTF_8)
                    } else {
                        null
                    }
                } else {
                    value
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_load_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            try {
                val value = preferences.get(key, null)
                
                if (value?.startsWith("file:") == true) {
                    // 删除文件
                    val filePath = value.removePrefix("file:")
                    val file = File(filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                
                // 删除Preferences中的记录
                preferences.remove(key)
                preferences.remove("${key}_timestamp")
                preferences.flush()
                
                UnifyPerformanceMonitor.recordMetric("desktop_storage_delete", 1.0, "count",
                    mapOf("key" to key))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_delete_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                preferences.keys()
                    .filter { !it.endsWith("_timestamp") }
                    .toList()
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_get_keys_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                emptyList()
            }
        }
    }
    
    override suspend fun getLastModified(key: String): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = preferences.getLong("${key}_timestamp", -1L)
                if (timestamp != -1L) timestamp else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getTotalSize(): Long {
        return withContext(Dispatchers.IO) {
            try {
                var totalSize = 0L
                
                // 计算Preferences大小（估算）
                preferences.keys().forEach { key ->
                    val value = preferences.get(key, "")
                    totalSize += (key.length + value.length) * 2 // UTF-16编码
                }
                
                // 计算文件大小
                if (dynamicDir.exists()) {
                    dynamicDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            totalSize += file.length()
                        }
                    }
                }
                
                totalSize
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    /**
     * 清理过期文件
     */
    suspend fun cleanupExpiredFiles(maxAge: Long = 7 * 24 * 60 * 60 * 1000) {
        withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                var cleanedCount = 0
                
                if (dynamicDir.exists()) {
                    dynamicDir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            val path = Paths.get(file.absolutePath)
                            val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
                            val lastModified = attrs.lastModifiedTime().toMillis()
                            
                            if ((currentTime - lastModified) > maxAge) {
                                if (file.delete()) {
                                    cleanedCount++
                                }
                            }
                        }
                    }
                }
                
                UnifyPerformanceMonitor.recordMetric("desktop_storage_cleanup", 1.0, "count",
                    mapOf("cleaned_files" to cleanedCount.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_cleanup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
            }
        }
    }
    
    /**
     * 备份存储数据
     */
    suspend fun backupData(backupDir: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val backup = File(backupDir)
                if (!backup.exists()) {
                    backup.mkdirs()
                }
                
                // 备份文件数据
                if (dynamicDir.exists()) {
                    dynamicDir.copyRecursively(File(backup, "files"), overwrite = true)
                }
                
                // 备份Preferences数据
                val prefsBackup = File(backup, "preferences.txt")
                prefsBackup.writeText(exportPreferences())
                
                UnifyPerformanceMonitor.recordMetric("desktop_storage_backup", 1.0, "count",
                    mapOf("backup_dir" to backupDir))
                
                true
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_backup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                false
            }
        }
    }
    
    /**
     * 恢复存储数据
     */
    suspend fun restoreData(backupDir: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val backup = File(backupDir)
                if (!backup.exists()) {
                    return@withContext false
                }
                
                // 恢复文件数据
                val filesBackup = File(backup, "files")
                if (filesBackup.exists()) {
                    filesBackup.copyRecursively(dynamicDir, overwrite = true)
                }
                
                // 恢复Preferences数据
                val prefsBackup = File(backup, "preferences.txt")
                if (prefsBackup.exists()) {
                    importPreferences(prefsBackup.readText())
                }
                
                UnifyPerformanceMonitor.recordMetric("desktop_storage_restore", 1.0, "count",
                    mapOf("backup_dir" to backupDir))
                
                true
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("desktop_storage_restore_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                false
            }
        }
    }
    
    /**
     * 导出Preferences数据
     */
    private fun exportPreferences(): String {
        val sb = StringBuilder()
        preferences.keys().forEach { key ->
            val value = preferences.get(key, "")
            sb.appendLine("$key=$value")
        }
        return sb.toString()
    }
    
    /**
     * 导入Preferences数据
     */
    private fun importPreferences(data: String) {
        data.lines().forEach { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                preferences.put(parts[0], parts[1])
            }
        }
        preferences.flush()
    }
    
    companion object {
        private const val MAX_PREFERENCES_SIZE = 8 * 1024 // 8KB (Preferences限制)
    }
}

/**
 * 创建Desktop平台存储适配器
 */
actual fun createPlatformStorageAdapter(): StorageAdapter {
    return DesktopStorageAdapter()
}
