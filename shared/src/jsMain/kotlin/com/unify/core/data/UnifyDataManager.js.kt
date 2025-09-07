package com.unify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.reflect.KClass

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager = UnifyDataManagerImpl()
}

actual class UnifyDataManagerImpl : UnifyDataManager {
    private val storage = mutableMapOf<String, Any>()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return storage[key] as? String ?: defaultValue
    }
    
    override suspend fun setString(key: String, value: String) {
        storage[key] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return storage[key] as? Int ?: defaultValue
    }
    
    override suspend fun setInt(key: String, value: Int) {
        storage[key] = value
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[key] as? Boolean ?: defaultValue
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) {
        storage[key] = value
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return storage[key] as? Long ?: defaultValue
    }
    
    override suspend fun setLong(key: String, value: Long) {
        storage[key] = value
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return storage[key] as? Float ?: defaultValue
    }
    
    override suspend fun setFloat(key: String, value: Float) {
        storage[key] = value
    }
    
    override suspend fun <T : Any> getObject(key: String, clazz: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key] as? T
    }
    
    override suspend fun <T> setObject(key: String, value: T) {
        if (value != null) {
            storage[key] = value as Any
        }
    }
    
    override suspend fun clear() {
        storage.clear()
    }
    
    override suspend fun remove(key: String) {
        storage.remove(key)
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return storage.keys.toSet()
    }
    
    override fun <T : Any> observeKey(key: String, clazz: KClass<T>): Flow<T?> {
        @Suppress("UNCHECKED_CAST")
        return flowOf(storage[key] as? T)
    }
    
    override fun observeStringKey(key: String): Flow<String?> {
        return flowOf(storage[key] as? String)
    }
    
    override fun observeIntKey(key: String): Flow<Int> {
        return flowOf(storage[key] as? Int ?: 0)
    }
    
    override fun observeBooleanKey(key: String): Flow<Boolean> {
        return flowOf(storage[key] as? Boolean ?: false)
    }
    
    override suspend fun setCacheExpiry(key: String, expiryMillis: Long) {
        // Cache expiry implementation for JS
    }
    
    override suspend fun isCacheExpired(key: String): Boolean {
        return false
    }
    
    override suspend fun clearExpiredCache() {
        // Clear expired cache implementation for JS
    }
    
    override suspend fun syncToCloud() {
        // Sync to cloud implementation for JS
    }
    
    override suspend fun syncFromCloud() {
        // Sync from cloud implementation for JS
    }
    
    override fun isSyncEnabled(): Boolean {
        return false
    }
    
    override fun setSyncEnabled(enabled: Boolean) {
        // Set sync enabled implementation for JS
    }
}
