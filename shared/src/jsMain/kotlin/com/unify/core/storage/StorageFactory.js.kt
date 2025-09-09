package com.unify.core.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer

actual fun createStorage(config: StorageConfig): UnifyStorage {
    return JSStorageImpl()
}

actual fun createEncryptedStorage(
    config: StorageConfig,
    encryptionKey: String,
): UnifyStorage {
    return JSStorageImpl()
}

actual fun createMemoryStorage(): UnifyStorage {
    return JSStorageImpl()
}

actual fun createFileStorage(path: String): UnifyStorage {
    return JSStorageImpl()
}

// JavaScript specific UnifyStorage implementation
class JSStorageImpl : UnifyStorage {
    private val storage = mutableMapOf<String, String>()

    override suspend fun <T> save(
        key: String,
        value: T,
        serializer: KSerializer<T>,
    ) {
        storage[key] = value.toString()
    }

    override suspend fun <T> load(
        key: String,
        serializer: KSerializer<T>,
    ): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key] as? T
    }

    override suspend fun delete(key: String): Boolean {
        return storage.remove(key) != null
    }

    override suspend fun clear(): Boolean {
        storage.clear()
        return true
    }

    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }

    override suspend fun getAllKeys(): List<String> {
        return storage.keys.toList()
    }

    override suspend fun getSize(): Long {
        return storage.size.toLong()
    }

    override fun observeChanges(): Flow<StorageEvent> {
        return flowOf()
    }

    override suspend fun batch(operations: List<StorageOperation>) {
        // Simplified batch operations
    }

    override suspend fun backup(): String {
        return storage.toString()
    }

    override suspend fun restore(backupData: String) {
        // Simplified restore
    }

    override suspend fun compact() {
        // No-op for JS
    }
}
