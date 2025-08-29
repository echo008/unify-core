package com.unify.storage

import kotlinx.serialization.KSerializer

/**
 * 存储加密器接口
 */
interface StorageEncryptor {
    fun encrypt(data: String): String
    fun decrypt(encryptedData: String): String
}

/**
 * AES存储加密器实现
 */
expect class AESStorageEncryptor() : StorageEncryptor {
    override fun encrypt(data: String): String
    override fun decrypt(encryptedData: String): String
}

/**
 * 加密存储装饰器
 */
class EncryptedStorageDecorator(
    private val storage: UnifyStorage,
    private val encryptor: StorageEncryptor
) : UnifyStorage {
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        val encryptedValue = storage.getString(key, null)
        return if (encryptedValue != null) {
            try {
                encryptor.decrypt(encryptedValue)
            } catch (e: Exception) {
                defaultValue
            }
        } else {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) {
        val encryptedValue = encryptor.encrypt(value)
        storage.putString(key, encryptedValue)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        val stringValue = getString(key, null)
        return stringValue?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        putString(key, value.toString())
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        val stringValue = getString(key, null)
        return stringValue?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        putString(key, value.toString())
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        val stringValue = getString(key, null)
        return stringValue?.toFloatOrNull() ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        putString(key, value.toString())
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val stringValue = getString(key, null)
        return stringValue?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
    }
    
    override suspend fun <T> getObject(key: String, serializer: KSerializer<T>): T? {
        val jsonString = getString(key, null)
        return if (jsonString != null) {
            try {
                kotlinx.serialization.json.Json.decodeFromString(serializer, jsonString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    override suspend fun <T> putObject(key: String, value: T, serializer: KSerializer<T>) {
        val jsonString = kotlinx.serialization.json.Json.encodeToString(serializer,
            value
        )
        putString(key, jsonString)
    }
    
    override suspend fun remove(key: String) {
        storage.remove(key)
    }
    
    override suspend fun clear() {
        storage.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return storage.getAllKeys()
    }
    
    override fun <T> observeKey(key: String): kotlinx.coroutines.flow.Flow<T?> {
        return storage.observeKey(key)
    }
}
