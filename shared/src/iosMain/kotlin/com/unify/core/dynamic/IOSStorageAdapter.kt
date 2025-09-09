@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.unify.core.dynamic

import com.unify.core.storage.StorageAdapter
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*

/**
 * iOS平台存储适配器实现
 * 基于NSUserDefaults和文件系统
 */
class IOSStorageAdapter : StorageAdapter {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val fileManager = NSFileManager.defaultManager

    override suspend fun save(
        key: String,
        data: ByteArray,
    ): Boolean =
        withContext(Dispatchers.Main) {
            try {
                val nsData = convertByteArrayToNSData(data)
                userDefaults.setObject(nsData, key)
                userDefaults.synchronize()
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun load(key: String): ByteArray? =
        withContext(Dispatchers.Main) {
            try {
                val nsData = userDefaults.objectForKey(key) as? NSData
                nsData?.let { convertNSDataToByteArray(it) }
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun delete(key: String): Boolean =
        withContext(Dispatchers.Main) {
            try {
                userDefaults.removeObjectForKey(key)
                userDefaults.synchronize()
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun exists(key: String): Boolean =
        withContext(Dispatchers.Main) {
            userDefaults.objectForKey(key) != null
        }

    override suspend fun clear(): Boolean =
        withContext(Dispatchers.Main) {
            try {
                val domain = NSBundle.mainBundle.bundleIdentifier ?: return@withContext false
                userDefaults.removePersistentDomainForName(domain)
                userDefaults.synchronize()
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun getAllKeys(): List<String> =
        withContext(Dispatchers.Main) {
            try {
                val domain = NSBundle.mainBundle.bundleIdentifier ?: return@withContext emptyList()
                // 简化实现，返回空集合
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun saveToFile(
        fileName: String,
        data: ByteArray,
    ): Boolean =
        withContext(Dispatchers.Main) {
            try {
                val documentsPath =
                    NSSearchPathForDirectoriesInDomains(
                        NSDocumentDirectory, NSUserDomainMask, true,
                    ).firstOrNull() as? String ?: return@withContext false

                val filePath = "$documentsPath/$fileName"
                val nsData = convertByteArrayToNSData(data)
                nsData.writeToFile(filePath, true)
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun loadFromFile(fileName: String): ByteArray? =
        withContext(Dispatchers.Main) {
            try {
                val documentsPath =
                    NSSearchPathForDirectoriesInDomains(
                        NSDocumentDirectory, NSUserDomainMask, true,
                    ).firstOrNull() as? String ?: return@withContext null

                val filePath = "$documentsPath/$fileName"
                val nsData = NSData.dataWithContentsOfFile(filePath) ?: return@withContext null
                nsData?.let { convertNSDataToByteArray(it) }
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun deleteFile(fileName: String): Boolean =
        withContext(Dispatchers.Main) {
            try {
                val documentsPath =
                    NSSearchPathForDirectoriesInDomains(
                        NSDocumentDirectory, NSUserDomainMask, true,
                    ).firstOrNull() as? String ?: return@withContext false

                val filePath = "$documentsPath/$fileName"
                fileManager.removeItemAtPath(filePath, null)
            } catch (e: Exception) {
                false
            }
        }
}
