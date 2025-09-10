package com.unify.core.database

import com.unify.database.UnifyDatabase
import kotlinx.coroutines.flow.Flow

/**
 * 统一数据库管理器 - 跨平台数据持久化核心接口
 * 基于SQLDelight实现，支持所有目标平台
 */
expect class UnifyDatabaseManager {
    companion object {
        fun create(): UnifyDatabaseManager
    }

    /**
     * 初始化数据库
     */
    suspend fun initialize()

    /**
     * 获取数据库实例
     */
    fun getDatabase(): UnifyDatabase

    /**
     * 执行事务
     */
    suspend fun <T> transaction(block: suspend () -> T): T

    /**
     * 清空所有数据
     */
    suspend fun clearAllData()

    /**
     * 获取数据库版本
     */
    fun getDatabaseVersion(): Int

    /**
     * 数据库迁移
     */
    suspend fun migrate(fromVersion: Int, toVersion: Int)

    /**
     * 导出数据库
     */
    suspend fun exportDatabase(): ByteArray

    /**
     * 导入数据库
     */
    suspend fun importDatabase(data: ByteArray): Boolean

    /**
     * 获取数据库统计信息
     */
    suspend fun getDatabaseStats(): DatabaseStats
}

/**
 * 数据库统计信息
 */
data class DatabaseStats(
    val totalTables: Int,
    val totalRecords: Long,
    val databaseSize: Long,
    val lastBackupTime: Long?,
    val syncStatus: SyncStatus
)

/**
 * 同步状态
 */
enum class SyncStatus {
    SYNCED,
    PENDING,
    SYNCING,
    ERROR,
    OFFLINE
}

/**
 * 用户数据访问对象
 */
class UserDao(private val database: UnifyDatabase) {
    fun getAllUsers() = database.unifyDatabaseQueries.selectAllUsers()
    
    fun getUserById(id: Long) = database.unifyDatabaseQueries.selectUserById(id)
    
    fun getUserByUsername(username: String) = database.unifyDatabaseQueries.selectUserByUsername(username)
    
    suspend fun insertUser(
        username: String,
        email: String,
        displayName: String,
        avatarUrl: String? = null
    ) {
        val now = com.unify.core.platform.getCurrentTimeMillis()
        database.unifyDatabaseQueries.insertUser(
            username = username,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = now,
            updatedAt = now
        )
    }
    
    suspend fun updateUser(
        id: Long,
        email: String,
        displayName: String,
        avatarUrl: String?
    ) {
        database.unifyDatabaseQueries.updateUser(
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            updatedAt = com.unify.core.platform.getCurrentTimeMillis(),
            id = id
        )
    }
    
    suspend fun deleteUser(id: Long) {
        database.unifyDatabaseQueries.deleteUser(
            updatedAt = com.unify.core.platform.getCurrentTimeMillis(),
            id = id
        )
    }
}

/**
 * 配置数据访问对象
 */
class ConfigDao(private val database: UnifyDatabase) {
    fun getAllConfigs() = database.unifyDatabaseQueries.selectAllConfigs()
    
    fun getConfigByKey(key: String) = database.unifyDatabaseQueries.selectConfigByKey(key)
    
    suspend fun updateConfig(
        key: String,
        value: String,
        type: String = "string",
        description: String? = null
    ) {
        database.unifyDatabaseQueries.insertOrUpdateConfig(
            key = key,
            value_ = value,
            type = type,
            description = description,
            updatedAt = com.unify.core.platform.getCurrentTimeMillis()
        )
    }
    
    suspend fun deleteConfig(key: String) {
        database.unifyDatabaseQueries.deleteConfig(key)
    }
}

/**
 * 缓存数据访问对象
 */
class CacheDao(private val database: UnifyDatabase) {
    fun getCacheByKey(key: String): String? {
        val now = com.unify.core.platform.getCurrentTimeMillis()
        return database.unifyDatabaseQueries.selectCacheByKey(key, now).executeAsOneOrNull()?.value_
    }
    
    suspend fun insertCacheData(key: String, data: String, expiresAt: Long? = null) {
        database.unifyDatabaseQueries.insertCacheData(
            key,
            data,
            expiresAt,
            com.unify.core.platform.getCurrentTimeMillis()
        )
    }
    
    suspend fun clearExpiredCache() {
        database.unifyDatabaseQueries.deleteExpiredCache(com.unify.core.platform.getCurrentTimeMillis())
    }
    
    suspend fun clearAllCache() {
        database.unifyDatabaseQueries.clearAllCache()
    }
}

/**
 * 同步数据访问对象
 */
class SyncDao(private val database: UnifyDatabase) {
    fun getUnsyncedRecords() = database.unifyDatabaseQueries.selectUnsyncedRecords()
    
    suspend fun addSyncRecord(
        tableName: String,
        recordId: String,
        action: String,
        data: String? = null
    ) {
        database.unifyDatabaseQueries.insertSyncRecord(
            tableName = tableName,
            recordId = recordId,
            action = action,
            data_ = data,
            timestamp = com.unify.core.platform.getCurrentTimeMillis()
        )
    }
    
    suspend fun markRecordSynced(id: Long) {
        database.unifyDatabaseQueries.markRecordSynced(id)
    }
    
    suspend fun cleanOldSyncRecords(olderThan: Long = com.unify.core.platform.getCurrentTimeMillis() - 7 * 24 * 60 * 60 * 1000L) {
        database.unifyDatabaseQueries.deleteOldSyncRecords(olderThan)
    }
}
