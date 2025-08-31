package com.unify.core.dynamic

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import java.io.File

/**
 * Android平台存储适配器
 * 使用SharedPreferences和文件系统进行数据持久化
 */
class AndroidStorageAdapter(private val context: Context) : StorageAdapter {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "unify_dynamic_storage", Context.MODE_PRIVATE
    )
    private val filesDir = File(context.filesDir, "unify_dynamic")
    
    init {
        // 确保目录存在
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
    }
    
    override suspend fun save(key: String, value: String) {
        withContext(Dispatchers.IO) {
            try {
                if (value.length > MAX_SHARED_PREFS_SIZE) {
                    // 大数据存储到文件
                    val file = File(filesDir, key)
                    file.writeText(value)
                    
                    // 在SharedPreferences中记录文件路径
                    sharedPrefs.edit()
                        .putString(key, "file:${file.absolutePath}")
                        .putLong("${key}_timestamp", System.currentTimeMillis())
                        .apply()
                } else {
                    // 小数据直接存储到SharedPreferences
                    sharedPrefs.edit()
                        .putString(key, value)
                        .putLong("${key}_timestamp", System.currentTimeMillis())
                        .apply()
                }
                
                UnifyPerformanceMonitor.recordMetric("android_storage_save", 1.0, "count",
                    mapOf("key" to key, "size" to value.length.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("android_storage_save_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                throw StorageException("Android存储保存失败: $key", e)
            }
        }
    }
    
    override suspend fun load(key: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val value = sharedPrefs.getString(key, null)
                
                if (value?.startsWith("file:") == true) {
                    // 从文件读取
                    val filePath = value.removePrefix("file:")
                    val file = File(filePath)
                    if (file.exists()) {
                        file.readText()
                    } else {
                        null
                    }
                } else {
                    value
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("android_storage_load_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            try {
                val value = sharedPrefs.getString(key, null)
                
                if (value?.startsWith("file:") == true) {
                    // 删除文件
                    val filePath = value.removePrefix("file:")
                    val file = File(filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                
                // 删除SharedPreferences中的记录
                sharedPrefs.edit()
                    .remove(key)
                    .remove("${key}_timestamp")
                    .apply()
                
                UnifyPerformanceMonitor.recordMetric("android_storage_delete", 1.0, "count",
                    mapOf("key" to key))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("android_storage_delete_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                sharedPrefs.all.keys
                    .filter { !it.endsWith("_timestamp") }
                    .toList()
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("android_storage_get_keys_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                emptyList()
            }
        }
    }
    
    override suspend fun getLastModified(key: String): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = sharedPrefs.getLong("${key}_timestamp", -1L)
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
                
                // 计算SharedPreferences大小（估算）
                sharedPrefs.all.values.forEach { value ->
                    when (value) {
                        is String -> totalSize += value.length * 2 // UTF-16编码
                        is Int -> totalSize += 4
                        is Long -> totalSize += 8
                        is Float -> totalSize += 4
                        is Boolean -> totalSize += 1
                    }
                }
                
                // 计算文件大小
                if (filesDir.exists()) {
                    filesDir.walkTopDown().forEach { file ->
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
                
                if (filesDir.exists()) {
                    filesDir.listFiles()?.forEach { file ->
                        if (file.isFile && (currentTime - file.lastModified()) > maxAge) {
                            if (file.delete()) {
                                cleanedCount++
                            }
                        }
                    }
                }
                
                UnifyPerformanceMonitor.recordMetric("android_storage_cleanup", 1.0, "count",
                    mapOf("cleaned_files" to cleanedCount.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("android_storage_cleanup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
            }
        }
    }
    
    companion object {
        private const val MAX_SHARED_PREFS_SIZE = 1024 * 1024 // 1MB
    }
}

/**
 * 创建Android平台存储适配器
 */
actual fun createPlatformStorageAdapter(): StorageAdapter {
    val context = PlatformManager.getApplicationContext() as Context
    return AndroidStorageAdapter(context)
}
