package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.prefs.Preferences

/**
 * Desktop平台数据管理器实现
 * 基于Java Preferences API和文件系统实现数据持久化
 */
actual class UnifyDataManagerImpl : UnifyDataManager {
    private val preferences = Preferences.userNodeForPackage(UnifyDataManagerImpl::class.java)
    private val dataDirectory = File(System.getProperty("user.home"), ".unify-core/data")

    // 用于观察数据变化的StateFlow
    private val stringObservers = mutableMapOf<String, MutableStateFlow<String>>()
    private val intObservers = mutableMapOf<String, MutableStateFlow<Int>>()
    private val booleanObservers = mutableMapOf<String, MutableStateFlow<Boolean>>()
    private val floatObservers = mutableMapOf<String, MutableStateFlow<Float>>()
    private val longObservers = mutableMapOf<String, MutableStateFlow<Long>>()

    init {
        // 确保数据目录存在
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
    }

    override suspend fun saveString(
        key: String,
        value: String,
    ) {
        preferences.put(key, value)
        preferences.flush()
        stringObservers[key]?.value = value
    }

    override suspend fun getString(
        key: String,
        defaultValue: String,
    ): String {
        return preferences.get(key, defaultValue)
    }

    override suspend fun saveInt(
        key: String,
        value: Int,
    ) {
        preferences.putInt(key, value)
        preferences.flush()
        intObservers[key]?.value = value
    }

    override suspend fun getInt(
        key: String,
        defaultValue: Int,
    ): Int {
        return preferences.getInt(key, defaultValue)
    }

    override suspend fun saveBoolean(
        key: String,
        value: Boolean,
    ) {
        preferences.putBoolean(key, value)
        preferences.flush()
        booleanObservers[key]?.value = value
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override suspend fun saveFloat(
        key: String,
        value: Float,
    ) {
        preferences.putFloat(key, value)
        preferences.flush()
        floatObservers[key]?.value = value
    }

    override suspend fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float {
        return preferences.getFloat(key, defaultValue)
    }

    override suspend fun saveLong(
        key: String,
        value: Long,
    ) {
        preferences.putLong(key, value)
        preferences.flush()
        longObservers[key]?.value = value
    }

    override suspend fun getLong(
        key: String,
        defaultValue: Long,
    ): Long {
        return preferences.getLong(key, defaultValue)
    }

    override suspend fun saveObject(
        key: String,
        value: Any,
    ) {
        // 对于Any类型，我们使用toString()作为简单实现
        // 在实际项目中，应该使用具体的序列化器
        val jsonString = value.toString()
        preferences.put(key, jsonString)
        preferences.flush()
    }

    override suspend fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
    ): T {
        val jsonString = preferences.get(key, null)
        return if (jsonString != null) {
            try {
                Json.decodeFromString(serializer, jsonString)
            } catch (e: Exception) {
                defaultValue
            }
        } else {
            defaultValue
        }
    }

    override suspend fun remove(key: String) {
        preferences.remove(key)
        preferences.flush()
    }

    override suspend fun clear() {
        preferences.clear()
        preferences.flush()
    }

    override suspend fun contains(key: String): Boolean {
        return preferences.get(key, null) != null
    }

    override suspend fun getAllKeys(): Set<String> {
        return try {
            preferences.keys().toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override fun observeString(
        key: String,
        defaultValue: String,
    ): Flow<String> {
        return stringObservers.getOrPut(key) {
            MutableStateFlow(preferences.get(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeInt(
        key: String,
        defaultValue: Int,
    ): Flow<Int> {
        return intObservers.getOrPut(key) {
            MutableStateFlow(preferences.getInt(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> {
        return booleanObservers.getOrPut(key) {
            MutableStateFlow(preferences.getBoolean(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeFloat(
        key: String,
        defaultValue: Float,
    ): Flow<Float> {
        return floatObservers.getOrPut(key) {
            MutableStateFlow(preferences.getFloat(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeLong(
        key: String,
        defaultValue: Long,
    ): Flow<Long> {
        return longObservers.getOrPut(key) {
            MutableStateFlow(preferences.getLong(key, defaultValue))
        }.asStateFlow()
    }

    override suspend fun saveToSecureStorage(
        key: String,
        value: String,
    ) {
        // Desktop平台使用Preferences作为安全存储
        preferences.put("secure_$key", value)
        preferences.flush()
    }

    override suspend fun getFromSecureStorage(
        key: String,
        defaultValue: String,
    ): String {
        return preferences.get("secure_$key", defaultValue)
    }

    override suspend fun saveFile(
        fileName: String,
        data: ByteArray,
    ) {
        val file = File(dataDirectory, fileName)
        file.writeBytes(data)
    }

    override suspend fun getFile(fileName: String): ByteArray? {
        val file = File(dataDirectory, fileName)
        return if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
    }

    override suspend fun deleteFile(fileName: String): Boolean {
        val file = File(dataDirectory, fileName)
        return file.delete()
    }

    override suspend fun fileExists(fileName: String): Boolean {
        val file = File(dataDirectory, fileName)
        return file.exists()
    }
}
