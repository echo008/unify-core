package com.unify.core.storage

/**
 * 跨平台存储适配器接口
 * 提供统一的存储API，支持键值存储和文件存储
 */
interface StorageAdapter {
    /**
     * 保存数据到存储
     */
    suspend fun save(
        key: String,
        data: ByteArray,
    ): Boolean

    /**
     * 从存储加载数据
     */
    suspend fun load(key: String): ByteArray?

    /**
     * 删除存储中的数据
     */
    suspend fun delete(key: String): Boolean

    /**
     * 检查键是否存在
     */
    suspend fun exists(key: String): Boolean

    /**
     * 清空所有存储数据
     */
    suspend fun clear(): Boolean

    /**
     * 获取所有存储的键
     */
    suspend fun getAllKeys(): List<String>

    /**
     * 保存数据到文件
     */
    suspend fun saveToFile(
        fileName: String,
        data: ByteArray,
    ): Boolean

    /**
     * 从文件加载数据
     */
    suspend fun loadFromFile(fileName: String): ByteArray?

    /**
     * 删除文件
     */
    suspend fun deleteFile(fileName: String): Boolean
}

/**
 * 数据异常类
 */
class DataException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 平台存储适配器expect声明
 * 每个平台实现自己的存储适配器
 */
expect class PlatformStorageAdapter() : StorageAdapter
