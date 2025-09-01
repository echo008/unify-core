package com.unify.core.dynamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSFileManager
import platform.darwin.NSObject

/**
 * iOS平台存储适配器
 * 使用UserDefaults和文件系统进行数据持久化
 */
class IOSStorageAdapter : StorageAdapter {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val documentsPath = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true
    ).firstOrNull() as? String ?: ""
    private val dynamicDir = "$documentsPath/unify_dynamic"
    
    init {
        // 确保目录存在
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dynamicDir)) {
            fileManager.createDirectoryAtPath(
                dynamicDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
    }
    
    override suspend fun save(key: String, value: String) {
        withContext(Dispatchers.Main) {
            try {
                if (value.length > MAX_USER_DEFAULTS_SIZE) {
                    // 大数据存储到文件
                    val filePath = "$dynamicDir/$key"
                    val nsString = NSString.create(string = value)
                    val success = nsString.writeToFile(
                        filePath,
                        atomically = true,
                        encoding = NSUTF8StringEncoding,
                        error = null
                    )
                    
                    if (success) {
                        // 在UserDefaults中记录文件路径
                        userDefaults.setObject("file:$filePath", forKey = key)
                        userDefaults.setDouble(
                            NSDate().timeIntervalSince1970,
                            forKey = "${key}_timestamp"
                        )
                        userDefaults.synchronize()
                    } else {
                        throw StorageException("iOS文件写入失败: $key")
                    }
                } else {
                    // 小数据直接存储到UserDefaults
                    userDefaults.setObject(value, forKey = key)
                    userDefaults.setDouble(
                        NSDate().timeIntervalSince1970,
                        forKey = "${key}_timestamp"
                    )
                    userDefaults.synchronize()
                }
                
                UnifyPerformanceMonitor.recordMetric("ios_storage_save", 1.0, "count",
                    mapOf("key" to key, "size" to value.length.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("ios_storage_save_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                throw StorageException("iOS存储保存失败: $key", e)
            }
        }
    }
    
    override suspend fun load(key: String): String? {
        return withContext(Dispatchers.Main) {
            try {
                val value = userDefaults.stringForKey(key)
                
                if (value?.startsWith("file:") == true) {
                    // 从文件读取
                    val filePath = value.removePrefix("file:")
                    val fileManager = NSFileManager.defaultManager
                    
                    if (fileManager.fileExistsAtPath(filePath)) {
                        val nsString = NSString.stringWithContentsOfFile(
                            filePath,
                            encoding = NSUTF8StringEncoding,
                            error = null
                        )
                        nsString
                    } else {
                        null
                    }
                } else {
                    value
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("ios_storage_load_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    override suspend fun delete(key: String) {
        withContext(Dispatchers.Main) {
            try {
                val value = userDefaults.stringForKey(key)
                
                if (value?.startsWith("file:") == true) {
                    // 删除文件
                    val filePath = value.removePrefix("file:")
                    val fileManager = NSFileManager.defaultManager
                    if (fileManager.fileExistsAtPath(filePath)) {
                        fileManager.removeItemAtPath(filePath, error = null)
                    }
                }
                
                // 删除UserDefaults中的记录
                userDefaults.removeObjectForKey(key)
                userDefaults.removeObjectForKey("${key}_timestamp")
                userDefaults.synchronize()
                
                UnifyPerformanceMonitor.recordMetric("ios_storage_delete", 1.0, "count",
                    mapOf("key" to key))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("ios_storage_delete_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.Main) {
            try {
                val dictionary = userDefaults.dictionaryRepresentation()
                val keys = mutableListOf<String>()
                
                dictionary.keys.forEach { key ->
                    val keyString = key.toString()
                    if (!keyString.endsWith("_timestamp")) {
                        keys.add(keyString)
                    }
                }
                
                keys
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("ios_storage_get_keys_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                emptyList()
            }
        }
    }
    
    override suspend fun getLastModified(key: String): Long? {
        return withContext(Dispatchers.Main) {
            try {
                val timestamp = userDefaults.doubleForKey("${key}_timestamp")
                if (timestamp > 0) (timestamp * 1000).toLong() else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getTotalSize(): Long {
        return withContext(Dispatchers.Main) {
            try {
                var totalSize = 0L
                
                // 计算UserDefaults大小（估算）
                val dictionary = userDefaults.dictionaryRepresentation()
                dictionary.values.forEach { value ->
                    when (value) {
                        is NSString -> totalSize += value.length * 2 // UTF-16编码
                        is NSNumber -> totalSize += 8
                    }
                }
                
                // 计算文件大小
                val fileManager = NSFileManager.defaultManager
                if (fileManager.fileExistsAtPath(dynamicDir)) {
                    val enumerator = fileManager.enumeratorAtPath(dynamicDir)
                    while (true) {
                        val fileName = enumerator?.nextObject() as? String ?: break
                        val filePath = "$dynamicDir/$fileName"
                        val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
                        val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
                        totalSize += fileSize
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
        withContext(Dispatchers.Main) {
            try {
                val currentTime = NSDate().timeIntervalSince1970 * 1000
                var cleanedCount = 0
                
                val fileManager = NSFileManager.defaultManager
                if (fileManager.fileExistsAtPath(dynamicDir)) {
                    val enumerator = fileManager.enumeratorAtPath(dynamicDir)
                    while (true) {
                        val fileName = enumerator?.nextObject() as? String ?: break
                        val filePath = "$dynamicDir/$fileName"
                        val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
                        val modificationDate = attributes?.get(NSFileModificationDate) as? NSDate
                        
                        if (modificationDate != null) {
                            val fileAge = currentTime - (modificationDate.timeIntervalSince1970 * 1000)
                            if (fileAge > maxAge) {
                                if (fileManager.removeItemAtPath(filePath, error = null)) {
                                    cleanedCount++
                                }
                            }
                        }
                    }
                }
                
                UnifyPerformanceMonitor.recordMetric("ios_storage_cleanup", 1.0, "count",
                    mapOf("cleaned_files" to cleanedCount.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("ios_storage_cleanup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
            }
        }
    }
    
    companion object {
        private const val MAX_USER_DEFAULTS_SIZE = 1024 * 1024 // 1MB
    }
}

/**
 * 创建iOS平台存储适配器
 */
actual fun createPlatformStorageAdapter(): StorageAdapter {
    return IOSStorageAdapter()
}
