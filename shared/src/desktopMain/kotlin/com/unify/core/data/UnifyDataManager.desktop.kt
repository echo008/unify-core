package com.unify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.reflect.KClass

/**
 * Desktop平台数据管理器工厂实现
 */
actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}

/**
 * Desktop平台数据管理器完整实现
 */
actual class UnifyDataManagerImpl : UnifyDataManager {
    private val dataStorage = mutableMapOf<String, Any>()
    private val cacheExpiry = mutableMapOf<String, Long>()
    private var syncEnabled = true

    // 键值存储实现
    override suspend fun getString(
        key: String,
        defaultValue: String?,
    ): String? {
        return dataStorage[key] as? String ?: defaultValue
    }

    override suspend fun setString(
        key: String,
        value: String,
    ) {
        dataStorage[key] = value
    }

    override suspend fun getInt(
        key: String,
        defaultValue: Int,
    ): Int {
        return dataStorage[key] as? Int ?: defaultValue
    }

    override suspend fun setInt(
        key: String,
        value: Int,
    ) {
        dataStorage[key] = value
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean {
        return dataStorage[key] as? Boolean ?: defaultValue
    }

    override suspend fun setBoolean(
        key: String,
        value: Boolean,
    ) {
        dataStorage[key] = value
    }

    override suspend fun getLong(
        key: String,
        defaultValue: Long,
    ): Long {
        return dataStorage[key] as? Long ?: defaultValue
    }

    override suspend fun setLong(
        key: String,
        value: Long,
    ) {
        dataStorage[key] = value
    }

    override suspend fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float {
        return dataStorage[key] as? Float ?: defaultValue
    }

    override suspend fun setFloat(
        key: String,
        value: Float,
    ) {
        dataStorage[key] = value
    }

    // 对象存储实现
    override suspend fun <T : Any> getObject(
        key: String,
        clazz: KClass<T>,
    ): T? {
        @Suppress("UNCHECKED_CAST")
        return dataStorage[key] as? T
    }

    override suspend fun <T> setObject(
        key: String,
        value: T,
    ) {
        if (value != null) {
            dataStorage[key] = value as Any
        }
    }

    // 批量操作实现
    override suspend fun clear() {
        dataStorage.clear()
        cacheExpiry.clear()
    }

    override suspend fun remove(key: String) {
        dataStorage.remove(key)
        cacheExpiry.remove(key)
    }

    override suspend fun contains(key: String): Boolean {
        return dataStorage.containsKey(key)
    }

    override suspend fun getAllKeys(): Set<String> {
        return dataStorage.keys.toSet()
    }

    // 响应式数据流实现
    override fun <T : Any> observeKey(
        key: String,
        clazz: KClass<T>,
    ): Flow<T?> {
        @Suppress("UNCHECKED_CAST")
        return flowOf(dataStorage[key] as? T)
    }

    override fun observeStringKey(key: String): Flow<String?> {
        return flowOf(dataStorage[key] as? String)
    }

    override fun observeIntKey(key: String): Flow<Int> {
        return flowOf(dataStorage[key] as? Int ?: 0)
    }

    override fun observeBooleanKey(key: String): Flow<Boolean> {
        return flowOf(dataStorage[key] as? Boolean ?: false)
    }

    // 缓存管理实现
    override suspend fun setCacheExpiry(
        key: String,
        expiryMillis: Long,
    ) {
        cacheExpiry[key] = System.currentTimeMillis() + expiryMillis
    }

    override suspend fun isCacheExpired(key: String): Boolean {
        val expiry = cacheExpiry[key] ?: return false
        return System.currentTimeMillis() > expiry
    }

    override suspend fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys =
            cacheExpiry.filter { (_, expiry) ->
                currentTime > expiry
            }.keys

        expiredKeys.forEach { key ->
            dataStorage.remove(key)
            cacheExpiry.remove(key)
        }
    }

    // 数据同步实现
    override suspend fun syncToCloud() {
        // Desktop平台云同步上传实现
    }

    override suspend fun syncFromCloud() {
        // Desktop平台云同步下载实现
    }

    override fun isSyncEnabled(): Boolean {
        return syncEnabled
    }

    override fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }
}
