package com.unify.core.dynamic

import com.unify.core.storage.StorageAdapter
import platform.Foundation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS平台存储适配器实现
 * 基于NSUserDefaults和文件系统
 */
actual class IOSStorageAdapter : StorageAdapter {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val fileManager = NSFileManager.defaultManager
    
    actual override suspend fun save(key: String, data: ByteArray): Boolean = withContext(Dispatchers.Main) {
        try {
            val nsData = data.toNSData()
            userDefaults.setObject(nsData, key)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun load(key: String): ByteArray? = withContext(Dispatchers.Main) {
        try {
            val nsData = userDefaults.objectForKey(key) as? NSData
            nsData?.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun delete(key: String): Boolean = withContext(Dispatchers.Main) {
        try {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Main) {
        userDefaults.objectForKey(key) != null
    }
    
    actual override suspend fun clear(): Boolean = withContext(Dispatchers.Main) {
        try {
            val domain = NSBundle.mainBundle.bundleIdentifier ?: return@withContext false
            userDefaults.removePersistentDomainForName(domain)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun getAllKeys(): List<String> = withContext(Dispatchers.Main) {
        try {
            val domain = NSBundle.mainBundle.bundleIdentifier ?: return@withContext emptyList()
            val dict = userDefaults.persistentDomainForName(domain) ?: return@withContext emptyList()
            dict.allKeys.mapNotNull { it as? String }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    actual override suspend fun saveToFile(fileName: String, data: ByteArray): Boolean = withContext(Dispatchers.Main) {
        try {
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            ).firstOrNull() as? String ?: return@withContext false
            
            val filePath = "$documentsPath/$fileName"
            val nsData = data.toNSData()
            nsData.writeToFile(filePath, true)
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun loadFromFile(fileName: String): ByteArray? = withContext(Dispatchers.Main) {
        try {
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            ).firstOrNull() as? String ?: return@withContext null
            
            val filePath = "$documentsPath/$fileName"
            val nsData = NSData.dataWithContentsOfFile(filePath) ?: return@withContext null
            nsData.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun deleteFile(fileName: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, NSUserDomainMask, true
            ).firstOrNull() as? String ?: return@withContext false
            
            val filePath = "$documentsPath/$fileName"
            fileManager.removeItemAtPath(filePath, null)
        } catch (e: Exception) {
            false
        }
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.refTo(0), length = this.size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    return ByteArray(this.length.toInt()) { index ->
        this.bytes!!.reinterpret<ByteVar>()[index]
    }
}
