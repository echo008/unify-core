package com.unify.core.storage

/**
 * 跨平台存储工厂接口
 */
data class StorageConfig(
    val name: String,
    val version: Int = 1,
    val encrypted: Boolean = false,
)

/**
 * 存储工厂expect声明
 */
expect fun createStorage(config: StorageConfig): UnifyStorage

expect fun createEncryptedStorage(
    config: StorageConfig,
    encryptionKey: String,
): UnifyStorage

expect fun createMemoryStorage(): UnifyStorage

expect fun createFileStorage(path: String): UnifyStorage
