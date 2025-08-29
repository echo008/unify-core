package com.unify.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import app.cash.sqldelight.db.SqlDriver

/**
 * 统一数据库仓库
 * 基于SQLDelight实现的数据访问层
 */
class UnifyDatabaseRepository(
    private val database: UnifyDatabase
) {
    
    /**
     * 用户相关操作
     */
    suspend fun getAllUsers(): List<User> {
        return database.unifyDatabaseQueries.selectAllUsers().executeAsList()
    }
    
    suspend fun getUserById(id: Long): User? {
        return database.unifyDatabaseQueries.selectUserById(id).executeAsOneOrNull()
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return database.unifyDatabaseQueries.selectUserByUsername(username).executeAsOneOrNull()
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return database.unifyDatabaseQueries.selectUserByEmail(email).executeAsOneOrNull()
    }
    
    suspend fun createUser(
        username: String,
        email: String,
        displayName: String,
        avatarUrl: String? = null
    ): Long {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.insertUser(
            username = username,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = now,
            updatedAt = now
        )
        return database.unifyDatabaseQueries.selectLastInsertRowId().executeAsOne()
    }
    
    suspend fun updateUser(
        id: Long,
        email: String,
        displayName: String,
        avatarUrl: String?
    ) {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.updateUser(
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl,
            updatedAt = now,
            id = id
        )
    }
    
    suspend fun deleteUser(id: Long) {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.deleteUser(updatedAt = now, id = id)
    }
    
    /**
     * 配置相关操作
     */
    suspend fun getAllConfigs(): List<AppConfig> {
        return database.unifyDatabaseQueries.selectAllConfigs().executeAsList()
    }
    
    suspend fun getConfigByKey(key: String): AppConfig? {
        return database.unifyDatabaseQueries.selectConfigByKey(key).executeAsOneOrNull()
    }
    
    suspend fun setConfig(key: String, value: String, type: String = "STRING") {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.insertOrReplaceConfig(
            key = key,
            value = value,
            type = type,
            updatedAt = now
        )
    }
    
    suspend fun deleteConfig(key: String) {
        database.unifyDatabaseQueries.deleteConfig(key)
    }
    
    /**
     * 缓存相关操作
     */
    suspend fun getCacheByKey(key: String): CacheData? {
        val now = Clock.System.now().epochSeconds
        return database.unifyDatabaseQueries.selectCacheByKey(key, now).executeAsOneOrNull()
    }
    
    suspend fun setCache(key: String, value: String, expiresAt: Long? = null) {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.insertOrReplaceCache(
            key = key,
            value = value,
            expiresAt = expiresAt,
            createdAt = now
        )
    }
    
    suspend fun deleteCacheByKey(key: String) {
        database.unifyDatabaseQueries.deleteCacheByKey(key)
    }
    
    suspend fun clearAllCache() {
        database.unifyDatabaseQueries.clearAllCache()
    }
    
    suspend fun deleteExpiredCache() {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.deleteExpiredCache(now)
    }
    
    /**
     * 性能监控相关操作
     */
    suspend fun getPerformanceMetrics(componentId: String, since: Long): List<PerformanceMetrics> {
        return database.unifyDatabaseQueries.selectPerformanceMetrics(componentId, since).executeAsList()
    }
    
    suspend fun insertPerformanceMetric(
        componentId: String,
        metricType: String,
        value: Double,
        platform: String
    ) {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.insertPerformanceMetric(
            componentId = componentId,
            metricType = metricType,
            value = value,
            timestamp = now,
            platform = platform
        )
    }
    
    suspend fun deleteOldPerformanceMetrics(before: Long) {
        database.unifyDatabaseQueries.deleteOldPerformanceMetrics(before)
    }
    
    /**
     * 错误日志相关操作
     */
    suspend fun getErrorLogs(since: Long, limit: Long): List<ErrorLog> {
        return database.unifyDatabaseQueries.selectErrorLogs(since, limit).executeAsList()
    }
    
    suspend fun getErrorLogsByType(errorType: String, since: Long): List<ErrorLog> {
        return database.unifyDatabaseQueries.selectErrorLogsByType(errorType, since).executeAsList()
    }
    
    suspend fun insertErrorLog(
        errorType: String,
        message: String,
        stackTrace: String?,
        platform: String,
        userId: Long? = null
    ) {
        val now = Clock.System.now().epochSeconds
        database.unifyDatabaseQueries.insertErrorLog(
            errorType = errorType,
            message = message,
            stackTrace = stackTrace,
            timestamp = now,
            platform = platform,
            userId = userId
        )
    }
    
    suspend fun deleteOldErrorLogs(before: Long) {
        database.unifyDatabaseQueries.deleteOldErrorLogs(before)
    }
    
    /**
     * 数据库维护操作
     */
    suspend fun cleanupOldData(retentionDays: Int = 30) {
        val cutoffTime = Clock.System.now().epochSeconds - (retentionDays * 24 * 60 * 60)
        
        // 清理过期缓存
        deleteExpiredCache()
        
        // 清理旧的性能监控数据
        deleteOldPerformanceMetrics(cutoffTime)
        
        // 清理旧的错误日志
        deleteOldErrorLogs(cutoffTime)
    }
}

/**
 * 数据库工厂
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

/**
 * 数据库提供者
 */
object DatabaseProvider {
    fun createDatabase(driverFactory: DatabaseDriverFactory): UnifyDatabase {
        val driver = driverFactory.createDriver()
        return UnifyDatabase(driver)
    }
}
