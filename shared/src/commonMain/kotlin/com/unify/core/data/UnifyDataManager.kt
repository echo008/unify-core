package com.unify.core.data

import kotlinx.coroutines.flow.Flow

/**
 * Unify跨平台数据管理器接口
 * 统一管理本地存储、缓存和状态
 */
interface UnifyDataManager {
    // 键值存储
    suspend fun getString(key: String, defaultValue: String? = null): String?
    suspend fun setString(key: String, value: String)
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    suspend fun setInt(key: String, value: Int)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    suspend fun setBoolean(key: String, value: Boolean)
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long
    suspend fun setLong(key: String, value: Long)
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float
    suspend fun setFloat(key: String, value: Float)
    
    // 对象存储
    suspend fun <T : Any> getObject(key: String, clazz: kotlin.reflect.KClass<T>): T?
    suspend fun <T> setObject(key: String, value: T)
    
    // 批量操作
    suspend fun clear()
    suspend fun remove(key: String)
    suspend fun contains(key: String): Boolean
    suspend fun getAllKeys(): Set<String>
    
    // 响应式数据流
    fun <T : Any> observeKey(key: String, clazz: kotlin.reflect.KClass<T>): Flow<T?>
    fun observeStringKey(key: String): Flow<String?>
    fun observeIntKey(key: String): Flow<Int>
    fun observeBooleanKey(key: String): Flow<Boolean>
    
    // 缓存管理
    suspend fun setCacheExpiry(key: String, expiryMillis: Long)
    suspend fun isCacheExpired(key: String): Boolean
    suspend fun clearExpiredCache()
    
    // 数据同步
    suspend fun syncToCloud()
    suspend fun syncFromCloud()
    fun isSyncEnabled(): Boolean
    fun setSyncEnabled(enabled: Boolean)
}

/**
 * 数据管理器工厂
 */
expect object UnifyDataManagerFactory {
    fun create(): UnifyDataManager
}

/**
 * 数据管理器expect声明
 */
expect class UnifyDataManagerImpl() : UnifyDataManager
