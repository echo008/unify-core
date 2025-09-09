package com.unify.core.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Native平台存储工厂实现
 */
actual fun createStorage(config: StorageConfig): UnifyStorage {
    return NativeUnifyStorage(config)
}

actual fun createEncryptedStorage(config: StorageConfig, encryptionKey: String): UnifyStorage {
    return NativeEncryptedStorage(config, encryptionKey)
}

actual fun createMemoryStorage(): UnifyStorage {
    return NativeMemoryStorage()
}

actual fun createFileStorage(path: String): UnifyStorage {
    return NativeFileStorage(path)
}

/**
 * Native平台存储实现
 */
private class NativeUnifyStorage(private val config: StorageConfig) : UnifyStorage {
    private val data = mutableMapOf<String, String>()
    private val _changes = MutableStateFlow<StorageEvent>(StorageEvent.Cleared)
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        // 简化实现：将值转换为字符串存储
        data[key] = value.toString()
        _changes.value = StorageEvent.KeyAdded(key)
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        // 简化实现：返回null
        return null
    }
    
    override suspend fun delete(key: String): Boolean {
        val removed = data.remove(key) != null
        if (removed) _changes.value = StorageEvent.KeyDeleted(key)
        return removed
    }
    
    override suspend fun clear(): Boolean {
        data.clear()
        _changes.value = StorageEvent.Cleared
        return true
    }
    
    override suspend fun exists(key: String): Boolean = data.containsKey(key)
    override suspend fun getAllKeys(): List<String> = data.keys.toList()
    override suspend fun getSize(): Long = data.size.toLong()
    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> = _changes
    override suspend fun batch(operations: List<StorageOperation>) { /* 简化实现 */ }
    override suspend fun backup(): String = ""
    override suspend fun restore(backupData: String) { /* 简化实现 */ }
    override suspend fun compact() { /* 简化实现 */ }
}

private class NativeEncryptedStorage(
    private val config: StorageConfig, 
    private val encryptionKey: String
) : UnifyStorage {
    private val data = mutableMapOf<String, String>()
    private val _changes = MutableStateFlow<StorageEvent>(StorageEvent.Cleared)
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        data[key] = encrypt(value.toString())
        _changes.value = StorageEvent.KeyAdded(key)
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? = null
    override suspend fun delete(key: String): Boolean = data.remove(key) != null
    override suspend fun clear(): Boolean { data.clear(); return true }
    override suspend fun exists(key: String): Boolean = data.containsKey(key)
    override suspend fun getAllKeys(): List<String> = data.keys.toList()
    override suspend fun getSize(): Long = data.size.toLong()
    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> = _changes
    override suspend fun batch(operations: List<StorageOperation>) { /* 简化实现 */ }
    override suspend fun backup(): String = ""
    override suspend fun restore(backupData: String) { /* 简化实现 */ }
    override suspend fun compact() { /* 简化实现 */ }
    
    private fun encrypt(value: String): String = value // 简化实现
    private fun decrypt(value: String): String = value // 简化实现
}

private class NativeMemoryStorage : UnifyStorage {
    private val data = mutableMapOf<String, String>()
    private val _changes = MutableStateFlow<StorageEvent>(StorageEvent.Cleared)
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        data[key] = value.toString()
        _changes.value = StorageEvent.KeyAdded(key)
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? = null
    override suspend fun delete(key: String): Boolean = data.remove(key) != null
    override suspend fun clear(): Boolean { data.clear(); return true }
    override suspend fun exists(key: String): Boolean = data.containsKey(key)
    override suspend fun getAllKeys(): List<String> = data.keys.toList()
    override suspend fun getSize(): Long = data.size.toLong()
    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> = _changes
    override suspend fun batch(operations: List<StorageOperation>) { /* 简化实现 */ }
    override suspend fun backup(): String = ""
    override suspend fun restore(backupData: String) { /* 简化实现 */ }
    override suspend fun compact() { /* 简化实现 */ }
}

private class NativeFileStorage(private val path: String) : UnifyStorage {
    private val data = mutableMapOf<String, String>()
    private val _changes = MutableStateFlow<StorageEvent>(StorageEvent.Cleared)
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        data[key] = value.toString()
        _changes.value = StorageEvent.KeyAdded(key)
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? = null
    override suspend fun delete(key: String): Boolean = data.remove(key) != null
    override suspend fun clear(): Boolean { data.clear(); return true }
    override suspend fun exists(key: String): Boolean = data.containsKey(key)
    override suspend fun getAllKeys(): List<String> = data.keys.toList()
    override suspend fun getSize(): Long = data.size.toLong()
    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> = _changes
    override suspend fun batch(operations: List<StorageOperation>) { /* 简化实现 */ }
    override suspend fun backup(): String = ""
    override suspend fun restore(backupData: String) { /* 简化实现 */ }
    override suspend fun compact() { /* 简化实现 */ }
}
