package com.unify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * 统一数据管理器接口
 * 提供跨平台一致的数据管理功能
 */
interface UnifyDataManager {
    
    /**
     * 获取存储管理器
     */
    val storage: UnifyStorage
    
    /**
     * 获取安全存储管理器
     */
    val secureStorage: UnifySecureStorage
    
    /**
     * 获取缓存管理器
     */
    val cache: UnifyCacheManager
    
    /**
     * 获取状态管理器
     */
    val state: UnifyStateManager
    
    /**
     * 获取数据库管理器
     */
    val database: UnifyDatabaseManager
    
    /**
     * 初始化数据管理器
     */
    suspend fun initialize()
    
    /**
     * 清理所有数据
     */
    suspend fun clearAllData()
    
    /**
     * 备份数据
     */
    suspend fun backupData(): BackupResult
    
    /**
     * 恢复数据
     */
    suspend fun restoreData(backupData: ByteArray): RestoreResult
    
    /**
     * 获取数据使用统计
     */
    suspend fun getDataUsageStats(): DataUsageStats
}

/**
 * 统一存储接口
 */
interface UnifyStorage {
    
    /**
     * 存储字符串
     */
    suspend fun putString(key: String, value: String)
    
    /**
     * 获取字符串
     */
    suspend fun getString(key: String): String?
    
    /**
     * 存储整数
     */
    suspend fun putInt(key: String, value: Int)
    
    /**
     * 获取整数
     */
    suspend fun getInt(key: String): Int?
    
    /**
     * 存储长整数
     */
    suspend fun putLong(key: String, value: Long)
    
    /**
     * 获取长整数
     */
    suspend fun getLong(key: String): Long?
    
    /**
     * 存储浮点数
     */
    suspend fun putFloat(key: String, value: Float)
    
    /**
     * 获取浮点数
     */
    suspend fun getFloat(key: String): Float?
    
    /**
     * 存储布尔值
     */
    suspend fun putBoolean(key: String, value: Boolean)
    
    /**
     * 获取布尔值
     */
    suspend fun getBoolean(key: String): Boolean?
    
    /**
     * 存储字节数组
     */
    suspend fun putByteArray(key: String, value: ByteArray)
    
    /**
     * 获取字节数组
     */
    suspend fun getByteArray(key: String): ByteArray?
    
    /**
     * 删除键值对
     */
    suspend fun remove(key: String)
    
    /**
     * 清空所有数据
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
}

/**
 * 安全存储接口
 */
interface UnifySecureStorage : UnifyStorage {
    
    /**
     * 安全存储敏感字符串
     */
    suspend fun putSecureString(key: String, value: String)
    
    /**
     * 安全获取敏感字符串
     */
    suspend fun getSecureString(key: String): String?
    
    /**
     * 存储加密数据
     */
    suspend fun putEncrypted(key: String, value: ByteArray)
    
    /**
     * 获取解密数据
     */
    suspend fun getDecrypted(key: String): ByteArray?
    
    /**
     * 设置加密密钥
     */
    suspend fun setEncryptionKey(key: String)
    
    /**
     * 验证生物识别
     */
    suspend fun authenticateWithBiometric(): Boolean
}

/**
 * 缓存管理器接口
 */
interface UnifyCacheManager {
    
    /**
     * 设置缓存
     */
    suspend fun <T> put(key: String, value: T, ttl: Long? = null)
    
    /**
     * 获取缓存
     */
    suspend fun <T> get(key: String, type: Class<T>): T?
    
    /**
     * 删除缓存
     */
    suspend fun remove(key: String)
    
    /**
     * 清空所有缓存
     */
    suspend fun clear()
    
    /**
     * 检查缓存是否存在且未过期
     */
    suspend fun isValid(key: String): Boolean
    
    /**
     * 获取缓存大小
     */
    suspend fun getSize(): Long
    
    /**
     * 设置最大缓存大小
     */
    suspend fun setMaxSize(maxSize: Long)
    
    /**
     * 清理过期缓存
     */
    suspend fun cleanupExpired()
}

/**
 * 状态管理器接口
 */
interface UnifyStateManager {
    
    /**
     * 设置全局状态
     */
    fun <T> setState(key: String, value: T)
    
    /**
     * 获取全局状态
     */
    fun <T> getState(key: String, type: Class<T>): T?
    
    /**
     * 观察状态变化
     */
    fun <T> observeState(key: String, type: Class<T>): Flow<T?>
    
    /**
     * 删除状态
     */
    fun removeState(key: String)
    
    /**
     * 清空所有状态
     */
    fun clearAllStates()
    
    /**
     * 持久化状态到存储
     */
    suspend fun persistState()
    
    /**
     * 从存储恢复状态
     */
    suspend fun restoreState()
}

/**
 * 数据库管理器接口
 */
interface UnifyDatabaseManager {
    
    /**
     * 初始化数据库
     */
    suspend fun initialize(databaseName: String, version: Int)
    
    /**
     * 执行SQL查询
     */
    suspend fun query(sql: String, args: Array<Any>? = null): List<Map<String, Any?>>
    
    /**
     * 执行SQL更新
     */
    suspend fun execute(sql: String, args: Array<Any>? = null): Int
    
    /**
     * 开始事务
     */
    suspend fun beginTransaction(): TransactionHandle
    
    /**
     * 提交事务
     */
    suspend fun commitTransaction(handle: TransactionHandle)
    
    /**
     * 回滚事务
     */
    suspend fun rollbackTransaction(handle: TransactionHandle)
    
    /**
     * 关闭数据库
     */
    suspend fun close()
}

/**
 * 事务句柄
 */
data class TransactionHandle(
    val id: String,
    val timestamp: Long
)

/**
 * 备份结果
 */
@Serializable
data class BackupResult(
    val success: Boolean,
    val data: ByteArray? = null,
    val size: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val error: String? = null
)

/**
 * 恢复结果
 */
@Serializable
data class RestoreResult(
    val success: Boolean,
    val restoredItems: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val error: String? = null
)

/**
 * 数据使用统计
 */
@Serializable
data class DataUsageStats(
    val totalStorageUsed: Long,
    val cacheSize: Long,
    val databaseSize: Long,
    val secureStorageSize: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 数据管理器实现类
 * 使用expect/actual机制实现跨平台功能
 */
expect class UnifyDataManagerImpl : UnifyDataManager
