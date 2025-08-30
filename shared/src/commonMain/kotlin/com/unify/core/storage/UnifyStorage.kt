package com.unify.core.storage

import kotlinx.coroutines.flow.Flow

/**
 * Unify存储服务接口
 * 提供统一的本地存储抽象
 */
interface UnifyStorage {
    
    /**
     * 存储字符串值
     */
    suspend fun putString(key: String, value: String)
    
    /**
     * 获取字符串值
     */
    suspend fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * 存储整数值
     */
    suspend fun putInt(key: String, value: Int)
    
    /**
     * 获取整数值
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * 存储长整数值
     */
    suspend fun putLong(key: String, value: Long)
    
    /**
     * 获取长整数值
     */
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long
    
    /**
     * 存储浮点数值
     */
    suspend fun putFloat(key: String, value: Float)
    
    /**
     * 获取浮点数值
     */
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float
    
    /**
     * 存储布尔值
     */
    suspend fun putBoolean(key: String, value: Boolean)
    
    /**
     * 获取布尔值
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * 存储字节数组
     */
    suspend fun putByteArray(key: String, value: ByteArray)
    
    /**
     * 获取字节数组
     */
    suspend fun getByteArray(key: String): ByteArray?
    
    /**
     * 删除指定键的值
     */
    suspend fun remove(key: String)
    
    /**
     * 清空所有存储
     */
    suspend fun clear()
    
    /**
     * 检查键是否存在
     */
    suspend fun contains(key: String): Boolean
    
    /**
     * 获取所有键
     */
    suspend fun getAllKeys(): Set<String>
    
    /**
     * 监听键值变化
     */
    fun observeKey(key: String): Flow<String?>
}

/**
 * 存储工厂
 */
expect object StorageFactory {
    fun create(name: String = "unify_storage"): UnifyStorage
}
