package com.unify.core.storage

/**
 * Desktop平台存储适配器实现
 */
actual class PlatformStorageAdapter : StorageAdapter {
    private val storage = mutableMapOf<String, ByteArray>()
    private val fileStorage = mutableMapOf<String, ByteArray>()

    override suspend fun save(
        key: String,
        data: ByteArray,
    ): Boolean {
        storage[key] = data
        return true
    }

    override suspend fun load(key: String): ByteArray? {
        return storage[key]
    }

    override suspend fun delete(key: String): Boolean {
        storage.remove(key)
        return true
    }

    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }

    override suspend fun clear(): Boolean {
        storage.clear()
        return true
    }

    override suspend fun getAllKeys(): List<String> {
        return storage.keys.toList()
    }

    override suspend fun saveToFile(
        fileName: String,
        data: ByteArray,
    ): Boolean {
        fileStorage[fileName] = data
        return true
    }

    override suspend fun loadFromFile(fileName: String): ByteArray? {
        return fileStorage[fileName]
    }

    override suspend fun deleteFile(fileName: String): Boolean {
        fileStorage.remove(fileName)
        return true
    }
}
