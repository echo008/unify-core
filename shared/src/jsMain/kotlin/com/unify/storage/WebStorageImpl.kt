package com.unify.storage

import kotlinx.coroutines.await
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import kotlin.js.Promise

actual class PreferencesStorageImpl : UnifyStorage {
    private val storage: Storage = localStorage
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return storage[key] ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        storage[key] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return storage[key]?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        storage[key] = value.toString()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return storage[key]?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        storage[key] = value.toString()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[key]?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        storage[key] = value.toString()
    }
    
    override suspend fun remove(key: String) {
        storage.removeItem(key)
    }
    
    override suspend fun clear() {
        storage.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage[key] != null
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return (0 until storage.length).mapNotNull { index ->
            storage.key(index)
        }.toSet()
    }
}

actual class DatabaseStorageImpl : UnifyStorage {
    // IndexedDB implementation would go here
    // For now, delegating to localStorage as fallback
    private val prefsImpl = PreferencesStorageImpl()
    
    override suspend fun getString(key: String, defaultValue: String?): String? = 
        prefsImpl.getString(key, defaultValue)
    
    override suspend fun putString(key: String, value: String) = 
        prefsImpl.putString(key, value)
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = 
        prefsImpl.getInt(key, defaultValue)
    
    override suspend fun putInt(key: String, value: Int) = 
        prefsImpl.putInt(key, value)
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = 
        prefsImpl.getLong(key, defaultValue)
    
    override suspend fun putLong(key: String, value: Long) = 
        prefsImpl.putLong(key, value)
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
        prefsImpl.getBoolean(key, defaultValue)
    
    override suspend fun putBoolean(key: String, value: Boolean) = 
        prefsImpl.putBoolean(key, value)
    
    override suspend fun remove(key: String) = prefsImpl.remove(key)
    override suspend fun clear() = prefsImpl.clear()
    override suspend fun contains(key: String): Boolean = prefsImpl.contains(key)
    override suspend fun getAllKeys(): Set<String> = prefsImpl.getAllKeys()
}

actual class FileSystemStorageImpl : UnifyStorage {
    // File System Access API implementation would go here
    // For now, delegating to localStorage as fallback
    private val prefsImpl = PreferencesStorageImpl()
    
    override suspend fun getString(key: String, defaultValue: String?): String? = 
        prefsImpl.getString(key, defaultValue)
    
    override suspend fun putString(key: String, value: String) = 
        prefsImpl.putString(key, value)
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = 
        prefsImpl.getInt(key, defaultValue)
    
    override suspend fun putInt(key: String, value: Int) = 
        prefsImpl.putInt(key, value)
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = 
        prefsImpl.getLong(key, defaultValue)
    
    override suspend fun putLong(key: String, value: Long) = 
        prefsImpl.putLong(key, value)
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
        prefsImpl.getBoolean(key, defaultValue)
    
    override suspend fun putBoolean(key: String, value: Boolean) = 
        prefsImpl.putBoolean(key, value)
    
    override suspend fun remove(key: String) = prefsImpl.remove(key)
    override suspend fun clear() = prefsImpl.clear()
    override suspend fun contains(key: String): Boolean = prefsImpl.contains(key)
    override suspend fun getAllKeys(): Set<String> = prefsImpl.getAllKeys()
}

actual class SecureStorageImpl : UnifyStorage {
    // Web Crypto API implementation for secure storage
    // For now, using localStorage with basic encoding (not truly secure)
    private val storage: Storage = localStorage
    private val prefix = "secure_"
    
    private fun encodeKey(key: String): String = prefix + key
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return storage[encodeKey(key)] ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        storage[encodeKey(key)] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return storage[encodeKey(key)]?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        storage[encodeKey(key)] = value.toString()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return storage[encodeKey(key)]?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        storage[encodeKey(key)] = value.toString()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[encodeKey(key)]?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        storage[encodeKey(key)] = value.toString()
    }
    
    override suspend fun remove(key: String) {
        storage.removeItem(encodeKey(key))
    }
    
    override suspend fun clear() {
        val keysToRemove = getAllKeys()
        keysToRemove.forEach { key ->
            storage.removeItem(encodeKey(key))
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage[encodeKey(key)] != null
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return (0 until storage.length).mapNotNull { index ->
            storage.key(index)?.let { key ->
                if (key.startsWith(prefix)) {
                    key.substring(prefix.length)
                } else null
            }
        }.toSet()
    }
}
