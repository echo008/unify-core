package com.unify.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Android平台存储实现
 */
actual object StorageFactory {
    private var context: Context? = null
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    actual fun create(name: String): UnifyStorage = AndroidStorage(name)
    
    internal fun getContext(): Context {
        return context ?: throw IllegalStateException("Context not set. Call StorageFactory.setContext() first.")
    }
}

private class AndroidStorage(name: String) : UnifyStorage {
    
    private val sharedPreferences: SharedPreferences = 
        StorageFactory.getContext().getSharedPreferences(name, Context.MODE_PRIVATE)
    
    private val changeFlow = MutableSharedFlow<Pair<String, String?>>()
    
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key != null) {
            val value = sharedPreferences.getString(key, null)
            changeFlow.tryEmit(key to value)
        }
    }
    
    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }
    
    override suspend fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    override suspend fun putByteArray(key: String, value: ByteArray) {
        val encoded = android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT)
        sharedPreferences.edit().putString(key, encoded).apply()
    }
    
    override suspend fun getByteArray(key: String): ByteArray? {
        val encoded = sharedPreferences.getString(key, null) ?: return null
        return try {
            android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    override suspend fun clear() {
        sharedPreferences.edit().clear().apply()
    }
    
    override suspend fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return sharedPreferences.all.keys
    }
    
    override fun observeKey(key: String): Flow<String?> {
        return changeFlow.asSharedFlow()
            .map { (changedKey, value) -> 
                if (changedKey == key) value else null 
            }
            .distinctUntilChanged()
    }
}
