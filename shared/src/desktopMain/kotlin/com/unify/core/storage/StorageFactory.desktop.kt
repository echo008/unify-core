package com.unify.core.storage

/**
 * Desktop平台存储工厂实现
 */
actual fun createStorage(config: StorageConfig): UnifyStorage {
    return DesktopUnifyStorage()
}

actual fun createEncryptedStorage(
    config: StorageConfig,
    encryptionKey: String,
): UnifyStorage {
    return DesktopEncryptedStorage(encryptionKey)
}

actual fun createMemoryStorage(): UnifyStorage {
    return DesktopMemoryStorage()
}

actual fun createFileStorage(path: String): UnifyStorage {
    return DesktopFileStorage(path)
}

/**
 * Desktop平台加密存储实现
 */
class DesktopEncryptedStorage(private val encryptionKey: String) : UnifyStorage {
    private val storage = mutableMapOf<String, String>()

    override suspend fun <T> save(
        key: String,
        value: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ) {
        val jsonString = kotlinx.serialization.json.Json.encodeToString(serializer, value)
        val encryptedValue = java.util.Base64.getEncoder().encodeToString(jsonString.toByteArray())
        storage[key] = encryptedValue
    }

    override suspend fun <T> load(
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): T? {
        val encryptedValue = storage[key] ?: return null
        return try {
            val jsonString = String(java.util.Base64.getDecoder().decode(encryptedValue))
            kotlinx.serialization.json.Json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(key: String): Boolean {
        storage.remove(key)
        return true
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

    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> {
        return kotlinx.coroutines.flow.flowOf()
    }

    override suspend fun batch(operations: List<StorageOperation>) {
        operations.forEach { operation ->
            when (operation) {
                is StorageOperation.Save<*> -> {
                    val jsonString =
                        kotlinx.serialization.json.Json.encodeToString(
                            operation.serializer as kotlinx.serialization.KSerializer<Any?>,
                            operation.value,
                        )
                    val encryptedValue = java.util.Base64.getEncoder().encodeToString(jsonString.toByteArray())
                    storage[operation.key] = encryptedValue
                }
                is StorageOperation.Delete -> storage.remove(operation.key)
                is StorageOperation.Clear -> storage.clear()
            }
        }
    }

    override suspend fun backup(): String {
        return storage.entries.joinToString(",") { "${it.key}=${it.value}" }
    }

    override suspend fun restore(backupData: String) {
        try {
            storage.clear()
            backupData.split(",").forEach { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size == 2) {
                    storage[parts[0]] = parts[1]
                }
            }
        } catch (e: Exception) {
            // 恢复失败
        }
    }

    override suspend fun compact() {
        // Desktop平台压缩存储
    }
}

/**
 * Desktop平台内存存储实现
 */
class DesktopMemoryStorage : UnifyStorage {
    private val storage = mutableMapOf<String, String>()

    override suspend fun <T> save(
        key: String,
        value: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ) {
        val jsonString = kotlinx.serialization.json.Json.encodeToString(serializer, value)
        storage[key] = jsonString
    }

    override suspend fun <T> load(
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): T? {
        val jsonString = storage[key] ?: return null
        return try {
            kotlinx.serialization.json.Json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(key: String): Boolean {
        storage.remove(key)
        return true
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

    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> {
        return kotlinx.coroutines.flow.flowOf()
    }

    override suspend fun batch(operations: List<StorageOperation>) {
        operations.forEach { operation ->
            when (operation) {
                is StorageOperation.Save<*> -> {
                    val jsonString =
                        kotlinx.serialization.json.Json.encodeToString(
                            operation.serializer as kotlinx.serialization.KSerializer<Any?>,
                            operation.value,
                        )
                    storage[operation.key] = jsonString
                }
                is StorageOperation.Delete -> storage.remove(operation.key)
                is StorageOperation.Clear -> storage.clear()
            }
        }
    }

    override suspend fun backup(): String {
        return storage.entries.joinToString(",") { "${it.key}=${it.value}" }
    }

    override suspend fun restore(backupData: String) {
        try {
            storage.clear()
            backupData.split(",").forEach { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size == 2) {
                    storage[parts[0]] = parts[1]
                }
            }
        } catch (e: Exception) {
            // 恢复失败
        }
    }

    override suspend fun compact() {
        // Desktop平台压缩存储
    }
}

/**
 * Desktop平台文件存储实现
 */
class DesktopFileStorage(private val path: String) : UnifyStorage {
    private val storage = mutableMapOf<String, String>()

    override suspend fun <T> save(
        key: String,
        value: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ) {
        val jsonString = kotlinx.serialization.json.Json.encodeToString(serializer, value)
        storage[key] = jsonString
    }

    override suspend fun <T> load(
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): T? {
        val jsonString = storage[key] ?: return null
        return try {
            kotlinx.serialization.json.Json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(key: String): Boolean {
        storage.remove(key)
        return true
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

    override fun observeChanges(): kotlinx.coroutines.flow.Flow<StorageEvent> {
        return kotlinx.coroutines.flow.flowOf()
    }

    override suspend fun batch(operations: List<StorageOperation>) {
        operations.forEach { operation ->
            when (operation) {
                is StorageOperation.Save<*> -> {
                    val jsonString =
                        kotlinx.serialization.json.Json.encodeToString(
                            operation.serializer as kotlinx.serialization.KSerializer<Any?>,
                            operation.value,
                        )
                    storage[operation.key] = jsonString
                }
                is StorageOperation.Delete -> storage.remove(operation.key)
                is StorageOperation.Clear -> storage.clear()
            }
        }
    }

    override suspend fun backup(): String {
        return storage.entries.joinToString(",") { "${it.key}=${it.value}" }
    }

    override suspend fun restore(backupData: String) {
        try {
            storage.clear()
            backupData.split(",").forEach { entry ->
                val parts = entry.split("=", limit = 2)
                if (parts.size == 2) {
                    storage[parts[0]] = parts[1]
                }
            }
        } catch (e: Exception) {
            // 恢复失败
        }
    }

    override suspend fun compact() {
        // Desktop平台压缩存储
    }
}
