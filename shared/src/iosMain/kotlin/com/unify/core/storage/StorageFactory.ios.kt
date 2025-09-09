package com.unify.core.storage

/**
 * iOS存储工厂函数实现
 */
actual fun createStorage(config: StorageConfig): UnifyStorage {
    return IOSUnifyStorage(config.name, config.encrypted)
}

actual fun createEncryptedStorage(
    config: StorageConfig,
    encryptionKey: String,
): UnifyStorage {
    return IOSUnifyStorage(config.name, true, encryptionKey)
}

actual fun createMemoryStorage(): UnifyStorage {
    return IOSMemoryStorage()
}

actual fun createFileStorage(path: String): UnifyStorage {
    return IOSFileStorage(path)
}
