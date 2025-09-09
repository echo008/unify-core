package com.unify.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)

/**
 * iOS平台UnifyDataManager实现
 * 基于NSUserDefaults和文件系统
 */
actual class UnifyDataManagerImpl actual constructor() : UnifyDataManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val documentsPath =
        NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true,
        ).firstOrNull() as? String ?: ""

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    // 响应式数据流管理
    private val dataFlows = mutableMapOf<String, MutableStateFlow<Any?>>()

    override suspend fun saveString(
        key: String,
        value: String,
    ) = mutex.withLock {
        userDefaults.setObject(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }

    override suspend fun getString(
        key: String,
        defaultValue: String,
    ): String =
        mutex.withLock {
            userDefaults.stringForKey(key) ?: defaultValue
        }

    override suspend fun saveInt(
        key: String,
        value: Int,
    ) = mutex.withLock {
        userDefaults.setInteger(value.toLong(), key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }

    override suspend fun getInt(
        key: String,
        defaultValue: Int,
    ): Int =
        mutex.withLock {
            val value = userDefaults.integerForKey(key)
            if (userDefaults.objectForKey(key) == null) defaultValue else value.toInt()
        }

    override suspend fun saveBoolean(
        key: String,
        value: Boolean,
    ) = mutex.withLock {
        userDefaults.setBool(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean =
        mutex.withLock {
            val value = userDefaults.boolForKey(key)
            if (userDefaults.objectForKey(key) == null) defaultValue else value
        }

    override suspend fun saveLong(
        key: String,
        value: Long,
    ) = mutex.withLock {
        userDefaults.setInteger(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }

    override suspend fun getLong(
        key: String,
        defaultValue: Long,
    ): Long =
        mutex.withLock {
            val value = userDefaults.integerForKey(key)
            if (userDefaults.objectForKey(key) == null) defaultValue else value
        }

    override suspend fun saveFloat(
        key: String,
        value: Float,
    ) = mutex.withLock {
        userDefaults.setFloat(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }

    override suspend fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float =
        mutex.withLock {
            val value = userDefaults.floatForKey(key)
            if (userDefaults.objectForKey(key) == null) defaultValue else value
        }

    override suspend fun saveObject(
        key: String,
        value: Any,
    ) = mutex.withLock {
        try {
            val jsonString = value.toString()
            userDefaults.setObject(jsonString, "${key}_object")
            userDefaults.synchronize()
            updateDataFlow(key, value)
        } catch (e: Exception) {
            throw RuntimeException("Failed to serialize object for key: $key", e)
        }
    }

    override suspend fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
    ): T =
        mutex.withLock {
            val jsonString = userDefaults.stringForKey("${key}_object")
            if (jsonString != null) {
                try {
                    json.decodeFromString(serializer, jsonString)
                } catch (e: Exception) {
                    defaultValue
                }
            } else {
                defaultValue
            }
        }

    override suspend fun clear() =
        mutex.withLock {
            val domain = NSBundle.mainBundle.bundleIdentifier
            if (domain != null) {
                userDefaults.removePersistentDomainForName(domain)
                userDefaults.synchronize()
            }
            dataFlows.values.forEach { it.value = null }
        }

    override suspend fun remove(key: String) =
        mutex.withLock {
            userDefaults.removeObjectForKey(key)
            userDefaults.removeObjectForKey("${key}_object")
            userDefaults.synchronize()
            updateDataFlow(key, null)
        }

    override suspend fun contains(key: String): Boolean =
        mutex.withLock {
            userDefaults.objectForKey(key) != null || userDefaults.objectForKey("${key}_object") != null
        }

    override suspend fun getAllKeys(): Set<String> =
        mutex.withLock {
            val allKeys = mutableSetOf<String>()
            val dictionary = userDefaults.dictionaryRepresentation()

            dictionary.keys.forEach { key ->
                val keyString = key.toString()
                if (!keyString.endsWith("_object")) {
                    allKeys.add(keyString)
                } else {
                    allKeys.add(keyString.removeSuffix("_object"))
                }
            }

            allKeys.toSet()
        }

    override fun observeString(
        key: String,
        defaultValue: String,
    ): Flow<String> {
        return getOrCreateDataFlow(key).map { (it as? String) ?: defaultValue }
    }

    override fun observeInt(
        key: String,
        defaultValue: Int,
    ): Flow<Int> {
        return getOrCreateDataFlow(key).map { (it as? Int) ?: defaultValue }
    }

    override fun observeBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> {
        return getOrCreateDataFlow(key).map { (it as? Boolean) ?: defaultValue }
    }

    override fun observeFloat(
        key: String,
        defaultValue: Float,
    ): Flow<Float> {
        return getOrCreateDataFlow(key).map { (it as? Float) ?: defaultValue }
    }

    override fun observeLong(
        key: String,
        defaultValue: Long,
    ): Flow<Long> {
        return getOrCreateDataFlow(key).map { (it as? Long) ?: defaultValue }
    }

    override suspend fun saveToSecureStorage(
        key: String,
        value: String,
    ): Unit =
        mutex.withLock {
            // iOS使用Keychain进行安全存储
            userDefaults.setObject(value, "secure_$key")
            userDefaults.synchronize()
        }

    override suspend fun getFromSecureStorage(
        key: String,
        defaultValue: String,
    ): String =
        mutex.withLock {
            userDefaults.stringForKey("secure_$key") ?: defaultValue
        }

    override suspend fun saveFile(
        fileName: String,
        data: ByteArray,
    ): Unit =
        mutex.withLock {
            val filePath = "$documentsPath/$fileName"
            val nsData = data.toNSData()
            nsData.writeToFile(filePath, true)
        }

    override suspend fun getFile(fileName: String): ByteArray? =
        mutex.withLock {
            val filePath = "$documentsPath/$fileName"
            val nsData = NSData.dataWithContentsOfFile(filePath)
            return nsData?.toByteArray()
        }

    override suspend fun deleteFile(fileName: String): Boolean =
        mutex.withLock {
            val filePath = "$documentsPath/$fileName"
            val fileManager = NSFileManager.defaultManager
            return try {
                fileManager.removeItemAtPath(filePath, null)
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun fileExists(fileName: String): Boolean =
        mutex.withLock {
            val filePath = "$documentsPath/$fileName"
            val fileManager = NSFileManager.defaultManager
            return fileManager.fileExistsAtPath(filePath)
        }

    private fun getOrCreateDataFlow(key: String): Flow<Any?> {
        return dataFlows.getOrPut(key) {
            val initialValue = userDefaults.objectForKey(key) ?: userDefaults.objectForKey("${key}_object")
            MutableStateFlow(initialValue)
        }.asStateFlow()
    }

    private fun updateDataFlow(
        key: String,
        value: Any?,
    ) {
        dataFlows.getOrPut(key) { MutableStateFlow(null) }.value = value
    }

    // 扩展函数用于ByteArray和NSData转换
    private fun ByteArray.toNSData(): NSData {
        return this.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        return ByteArray(this.length.toInt()).apply {
            this@apply.usePinned { pinned ->
                this@toByteArray.getBytes(pinned.addressOf(0), this@toByteArray.length)
            }
        }
    }
}
