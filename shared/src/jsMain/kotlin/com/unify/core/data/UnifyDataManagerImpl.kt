package com.unify.data

import com.unify.core.utils.UnifyStringUtils
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.js.Date

/**
 * JS平台UnifyDataManager实现
 * 基于浏览器localStorage、IndexedDB和Service Worker缓存
 */
actual class UnifyDataManagerImpl actual constructor() : UnifyDataManager {
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()

    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()

    // 使用localStorage作为存储
    private val storage = localStorage

    override suspend fun saveString(
        key: String,
        value: String,
    ) {
        storage.setItem(key, value)
        notifyDataChange(key, value)
    }

    override suspend fun getString(
        key: String,
        defaultValue: String,
    ): String {
        return storage.getItem(key) ?: defaultValue
    }

    override suspend fun saveInt(
        key: String,
        value: Int,
    ) {
        storage.setItem(key, value.toString())
        notifyDataChange(key, value)
    }

    override suspend fun getInt(
        key: String,
        defaultValue: Int,
    ): Int {
        return storage.getItem(key)?.toIntOrNull() ?: defaultValue
    }

    override suspend fun saveFloat(
        key: String,
        value: Float,
    ) {
        storage.setItem(key, value.toString())
        notifyDataChange(key, value)
    }

    override suspend fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float {
        return storage.getItem(key)?.toFloatOrNull() ?: defaultValue
    }

    override suspend fun saveBoolean(
        key: String,
        value: Boolean,
    ) {
        storage.setItem(key, value.toString())
        notifyDataChange(key, value)
    }

    override suspend fun saveLong(
        key: String,
        value: Long,
    ) {
        storage.setItem(key, value.toString())
        notifyDataChange(key, value)
    }

    override suspend fun getLong(
        key: String,
        defaultValue: Long,
    ): Long {
        return storage.getItem(key)?.toLongOrNull() ?: defaultValue
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean {
        return storage.getItem(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    override suspend fun saveObject(
        key: String,
        value: Any,
    ) {
        try {
            val jsonString = JSON.stringify(value)
            storage.setItem(key, jsonString)
            notifyDataChange(key, value)
        } catch (e: Exception) {
            throw Exception("Failed to save data: ${e.message}")
        }
    }

    override suspend fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): T {
        return try {
            val jsonString = storage.getItem(key)
            if (!jsonString.isNullOrEmpty()) {
                Json.decodeFromString(serializer, jsonString)
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun contains(key: String): Boolean {
        return storage.getItem(key) != null
    }

    override suspend fun remove(key: String) {
        storage.removeItem(key)
        notifyDataChange(key, null)
    }

    override suspend fun clear() {
        storage.clear()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
    }

    override suspend fun getAllKeys(): Set<String> {
        val keys = mutableSetOf<String>()
        for (i in 0 until storage.length) {
            storage.key(i)?.let { keys.add(it) }
        }
        return keys
    }

    override fun observeString(
        key: String,
        defaultValue: String,
    ): Flow<String> {
        val flow =
            dataChangeFlows.getOrPut(key) {
                MutableStateFlow(storage.getItem(key) ?: defaultValue)
            }
        return flow.asStateFlow() as Flow<String>
    }

    override fun observeInt(
        key: String,
        defaultValue: Int,
    ): Flow<Int> {
        val flow =
            dataChangeFlows.getOrPut(key) {
                MutableStateFlow(storage.getItem(key)?.toIntOrNull() ?: defaultValue)
            }
        return flow.asStateFlow() as Flow<Int>
    }

    override fun observeBoolean(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> {
        val flow =
            dataChangeFlows.getOrPut(key) {
                MutableStateFlow(storage.getItem(key)?.toBooleanStrictOrNull() ?: defaultValue)
            }
        return flow.asStateFlow() as Flow<Boolean>
    }

    override fun observeFloat(
        key: String,
        defaultValue: Float,
    ): Flow<Float> {
        val flow =
            dataChangeFlows.getOrPut(key) {
                MutableStateFlow(storage.getItem(key)?.toFloatOrNull() ?: defaultValue)
            }
        return flow.asStateFlow() as Flow<Float>
    }

    override fun observeLong(
        key: String,
        defaultValue: Long,
    ): Flow<Long> {
        val flow =
            dataChangeFlows.getOrPut(key) {
                MutableStateFlow(storage.getItem(key)?.toLongOrNull() ?: defaultValue)
            }
        return flow.asStateFlow() as Flow<Long>
    }

    override suspend fun saveToSecureStorage(
        key: String,
        value: String,
    ) {
        // JS平台使用localStorage作为安全存储
        storage.setItem("secure_$key", value)
        notifyDataChange(key, value)
    }

    private fun <T> observeObjectInternal(
        key: String,
        defaultValue: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): Flow<T> {
        val flow =
            dataChangeFlows.getOrPut(key) {
                val storedValue =
                    try {
                        val jsonString = storage.getItem(key)
                        if (!jsonString.isNullOrEmpty()) {
                            Json.decodeFromString(serializer, jsonString)
                        } else {
                            defaultValue
                        }
                    } catch (e: Exception) {
                        defaultValue
                    }
                MutableStateFlow(storedValue)
            }
        return flow.asStateFlow() as Flow<T>
    }

    // 缓存相关方法（简化实现）
    private fun putCache(
        key: String,
        value: Any,
        ttlMillis: Long,
    ) {
        val expiryTime = Date.now() + ttlMillis
        cache[key] = CacheEntry(value, expiryTime)
    }

    private fun <T> getCache(key: String): T? {
        val entry = cache[key]
        return if (entry != null && !isExpired(entry)) {
            entry.value as? T
        } else {
            cache.remove(key)
            null
        }
    }

    private fun removeCache(key: String) {
        cache.remove(key)
    }

    private fun clearCache() {
        cache.clear()
    }

    private fun notifyDataChange(
        key: String,
        value: Any?,
    ) {
        val flow =
            dataChangeFlows.getOrPut(key) {
                MutableStateFlow(value)
            }
        flow.value = value
    }

    private fun isExpired(entry: CacheEntry): Boolean {
        return Date.now() > entry.expiryTime
    }

    /**
     * 缓存条目
     */
    private data class CacheEntry(
        val value: Any,
        val expiryTime: Double,
    )

    /**
     * 清理过期缓存
     */
    private fun cleanupExpiredCache() {
        val currentTime = Date.now()
        val expiredKeys =
            cache.filter { (_, entry) ->
                currentTime > entry.expiryTime
            }.keys
        expiredKeys.forEach { cache.remove(it) }
    }

    override suspend fun getFromSecureStorage(
        key: String,
        defaultValue: String,
    ): String {
        return storage.getItem("secure_$key") ?: defaultValue
    }

    override suspend fun saveFile(
        fileName: String,
        data: ByteArray,
    ) {
        // JS平台文件保存的简化实现
        val base64Data = data.joinToString("") { UnifyStringUtils.format("%02x", it) }
        storage.setItem("file_$fileName", base64Data)
    }

    override suspend fun getFile(fileName: String): ByteArray? {
        // JS平台文件读取的简化实现
        val hexString = storage.getItem("file_$fileName") ?: return null
        return try {
            hexString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteFile(fileName: String): Boolean {
        return try {
            storage.removeItem("file_$fileName")
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun fileExists(fileName: String): Boolean {
        return storage.getItem("file_$fileName") != null
    }
}

/**
 * JS平台UnifyDataManager工厂
 */
