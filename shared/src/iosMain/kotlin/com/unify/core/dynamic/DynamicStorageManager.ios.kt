package com.unify.core.dynamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.darwin.NSObject

/**
 * iOS平台存储适配器实现
 */
actual class PlatformStorageAdapter : StorageAdapter {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val documentsDirectory: NSURL
    
    init {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        )
        documentsDirectory = urls.firstOrNull() as? NSURL 
            ?: throw IllegalStateException("无法获取文档目录")
    }
    
    override suspend fun save(key: String, data: String): Boolean = withContext(Dispatchers.Default) {
        try {
            // 对于大数据使用文件存储，小数据使用UserDefaults
            if (data.length > 8192) { // 8KB阈值
                val fileName = "${key.hashCode()}.dat"
                val fileURL = documentsDirectory.URLByAppendingPathComponent(fileName)
                
                val nsData = (data as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                val success = nsData?.writeToURL(fileURL!!, atomically = true) ?: false
                
                if (success) {
                    userDefaults.setObject("FILE:$fileName", forKey = key)
                    userDefaults.synchronize()
                }
                success
            } else {
                userDefaults.setObject(data, forKey = key)
                userDefaults.synchronize()
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun load(key: String): String? = withContext(Dispatchers.Default) {
        try {
            val value = userDefaults.stringForKey(key) ?: return@withContext null
            
            if (value.startsWith("FILE:")) {
                val fileName = value.removePrefix("FILE:")
                val fileURL = documentsDirectory.URLByAppendingPathComponent(fileName)
                
                val nsData = NSData.dataWithContentsOfURL(fileURL!!)
                nsData?.let {
                    NSString.create(it, NSUTF8StringEncoding)?.toString()
                }
            } else {
                value
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val value = userDefaults.stringForKey(key)
            
            if (value?.startsWith("FILE:") == true) {
                val fileName = value.removePrefix("FILE:")
                val fileURL = documentsDirectory.URLByAppendingPathComponent(fileName)
                
                val fileManager = NSFileManager.defaultManager
                if (fileManager.fileExistsAtPath(fileURL!!.path!!)) {
                    fileManager.removeItemAtURL(fileURL, error = null)
                }
            }
            
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        userDefaults.objectForKey(key) != null
    }
    
    override suspend fun listKeys(prefix: String): List<String> = withContext(Dispatchers.Default) {
        try {
            val allKeys = userDefaults.dictionaryRepresentation().keys
            allKeys.mapNotNull { it as? String }
                .filter { it.startsWith(prefix) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun clear(): Boolean = withContext(Dispatchers.Default) {
        try {
            val fileManager = NSFileManager.defaultManager
            val documentsPath = documentsDirectory.path!!
            
            // 清理所有.dat文件
            val contents = fileManager.contentsOfDirectoryAtPath(documentsPath, error = null)
            contents?.forEach { fileName ->
                val fileNameStr = fileName as String
                if (fileNameStr.endsWith(".dat")) {
                    val filePath = "$documentsPath/$fileNameStr"
                    fileManager.removeItemAtPath(filePath, error = null)
                }
            }
            
            // 清理UserDefaults中的所有键
            val allKeys = userDefaults.dictionaryRepresentation().keys
            allKeys.forEach { key ->
                userDefaults.removeObjectForKey(key as String)
            }
            userDefaults.synchronize()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getSize(key: String): Long = withContext(Dispatchers.Default) {
        try {
            val value = userDefaults.stringForKey(key) ?: return@withContext 0L
            
            if (value.startsWith("FILE:")) {
                val fileName = value.removePrefix("FILE:")
                val fileURL = documentsDirectory.URLByAppendingPathComponent(fileName)
                
                val fileManager = NSFileManager.defaultManager
                if (fileManager.fileExistsAtPath(fileURL!!.path!!)) {
                    val attributes = fileManager.attributesOfItemAtPath(fileURL.path!!, error = null)
                    val fileSize = attributes?.get(NSFileSize) as? NSNumber
                    fileSize?.longValue ?: 0L
                } else {
                    0L
                }
            } else {
                value.toByteArray().size.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    override suspend fun getTotalSize(): Long = withContext(Dispatchers.Default) {
        try {
            var totalSize = 0L
            val fileManager = NSFileManager.defaultManager
            val documentsPath = documentsDirectory.path!!
            
            // 计算文件大小
            val contents = fileManager.contentsOfDirectoryAtPath(documentsPath, error = null)
            contents?.forEach { fileName ->
                val fileNameStr = fileName as String
                if (fileNameStr.endsWith(".dat")) {
                    val filePath = "$documentsPath/$fileNameStr"
                    val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
                    val fileSize = attributes?.get(NSFileSize) as? NSNumber
                    totalSize += fileSize?.longValue ?: 0L
                }
            }
            
            // 计算UserDefaults大小（估算）
            val allValues = userDefaults.dictionaryRepresentation().values
            allValues.forEach { value ->
                if (value is String && !value.startsWith("FILE:")) {
                    totalSize += value.toByteArray().size
                }
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
}
