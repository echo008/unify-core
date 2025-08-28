package com.unify.database

import app.cash.sqldelight.db.SqlDriver
import com.unify.database.UnifyDatabase

/**
 * 统一数据库工厂接口
 * 基于文档要求的SQLDelight数据库实现
 */
expect class UnifyDatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

/**
 * 数据库实例管理器
 */
object UnifyDatabaseFactory {
    private var database: UnifyDatabase? = null
    
    fun createDatabase(driverFactory: UnifyDatabaseDriverFactory): UnifyDatabase {
        val driver = driverFactory.createDriver()
        val database = UnifyDatabase(driver)
        UnifyDatabaseFactory.database = database
        return database
    }
    
    fun getDatabase(): UnifyDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call createDatabase() first.")
    }
}

/**
 * 数据库实体类型定义
 */
data class UserEntity(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
)

data class AppConfigEntity(
    val key: String,
    val value: String,
    val type: String,
    val updatedAt: Long
)

data class CacheDataEntity(
    val key: String,
    val value: String,
    val expiresAt: Long?,
    val createdAt: Long
)

data class PerformanceMetricEntity(
    val id: Long,
    val componentId: String,
    val metricType: String,
    val value: Double,
    val timestamp: Long,
    val platform: String
)

data class ErrorLogEntity(
    val id: Long,
    val errorType: String,
    val message: String,
    val stackTrace: String?,
    val timestamp: Long,
    val platform: String,
    val userId: Long?
)

/**
 * 数据库操作扩展函数
 */
fun UnifyDatabase.insertUserEntity(user: UserEntity): Long {
    userQueries.insertUser(
        username = user.username,
        email = user.email,
        displayName = user.displayName,
        avatarUrl = user.avatarUrl,
        createdAt = user.createdAt,
        updatedAt = user.updatedAt
    )
    return userQueries.selectLastInsertRowId().executeAsOne()
}

fun UnifyDatabase.getUserEntity(id: Long): UserEntity? {
    return userQueries.selectUserById(id).executeAsOneOrNull()?.let { user ->
        UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            isActive = user.isActive == 1L
        )
    }
}

fun UnifyDatabase.insertConfigEntity(config: AppConfigEntity) {
    appConfigQueries.insertOrReplaceConfig(
        key = config.key,
        value = config.value,
        type = config.type,
        updatedAt = config.updatedAt
    )
}

fun UnifyDatabase.getConfigEntity(key: String): AppConfigEntity? {
    return appConfigQueries.selectConfigByKey(key).executeAsOneOrNull()?.let { config ->
        AppConfigEntity(
            key = config.key,
            value = config.value,
            type = config.type,
            updatedAt = config.updatedAt
        )
    }
}

fun UnifyDatabase.insertCacheEntity(cache: CacheDataEntity) {
    cacheDataQueries.insertOrReplaceCache(
        key = cache.key,
        value = cache.value,
        expiresAt = cache.expiresAt,
        createdAt = cache.createdAt
    )
}

fun UnifyDatabase.getCacheEntity(key: String): CacheDataEntity? {
    val currentTime = System.currentTimeMillis()
    return cacheDataQueries.selectCacheByKey(key, currentTime).executeAsOneOrNull()?.let { cache ->
        CacheDataEntity(
            key = cache.key,
            value = cache.value,
            expiresAt = cache.expiresAt,
            createdAt = cache.createdAt
        )
    }
}

fun UnifyDatabase.insertPerformanceMetricEntity(metric: PerformanceMetricEntity) {
    performanceMetricsQueries.insertPerformanceMetric(
        componentId = metric.componentId,
        metricType = metric.metricType,
        value = metric.value,
        timestamp = metric.timestamp,
        platform = metric.platform
    )
}

fun UnifyDatabase.insertErrorLogEntity(error: ErrorLogEntity) {
    errorLogQueries.insertErrorLog(
        errorType = error.errorType,
        message = error.message,
        stackTrace = error.stackTrace,
        timestamp = error.timestamp,
        platform = error.platform,
        userId = error.userId
    )
}

fun UnifyDatabase.cleanupOldData(retentionDays: Int = 30) {
    val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
    
    // 清理过期缓存
    cacheDataQueries.deleteExpiredCache(System.currentTimeMillis())
    
    // 清理旧的性能监控数据
    performanceMetricsQueries.deleteOldPerformanceMetrics(cutoffTime)
    
    // 清理旧的错误日志
    errorLogQueries.deleteOldErrorLogs(cutoffTime)
}

/**
 * 高级查询扩展函数
 */

// 用户相关查询
fun UnifyDatabase.getAllActiveUsers(): List<UserEntity> {
    return userQueries.selectActiveUsers().executeAsList().map { user ->
        UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            isActive = user.isActive == 1L
        )
    }
}

fun UnifyDatabase.getUserByUsername(username: String): UserEntity? {
    return userQueries.selectUserByUsername(username).executeAsOneOrNull()?.let { user ->
        UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            isActive = user.isActive == 1L
        )
    }
}

fun UnifyDatabase.getUserByEmail(email: String): UserEntity? {
    return userQueries.selectUserByEmail(email).executeAsOneOrNull()?.let { user ->
        UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            isActive = user.isActive == 1L
        )
    }
}

fun UnifyDatabase.updateUserEntity(user: UserEntity) {
    userQueries.updateUser(
        id = user.id,
        username = user.username,
        email = user.email,
        displayName = user.displayName,
        avatarUrl = user.avatarUrl,
        updatedAt = user.updatedAt
    )
}

fun UnifyDatabase.deactivateUser(userId: Long) {
    userQueries.deactivateUser(userId, System.currentTimeMillis())
}

fun UnifyDatabase.getUserCount(): Long {
    return userQueries.selectUserCount().executeAsOne()
}

// 配置相关查询
fun UnifyDatabase.getAllConfigs(): List<AppConfigEntity> {
    return appConfigQueries.selectAllConfigs().executeAsList().map { config ->
        AppConfigEntity(
            key = config.key,
            value = config.value,
            type = config.type,
            updatedAt = config.updatedAt
        )
    }
}

fun UnifyDatabase.getConfigsByType(type: String): List<AppConfigEntity> {
    return appConfigQueries.selectConfigsByType(type).executeAsList().map { config ->
        AppConfigEntity(
            key = config.key,
            value = config.value,
            type = config.type,
            updatedAt = config.updatedAt
        )
    }
}

fun UnifyDatabase.deleteConfig(key: String) {
    appConfigQueries.deleteConfig(key)
}

// 缓存相关查询
fun UnifyDatabase.getAllValidCache(): List<CacheDataEntity> {
    val currentTime = System.currentTimeMillis()
    return cacheDataQueries.selectAllValidCache(currentTime).executeAsList().map { cache ->
        CacheDataEntity(
            key = cache.key,
            value = cache.value,
            expiresAt = cache.expiresAt,
            createdAt = cache.createdAt
        )
    }
}

fun UnifyDatabase.deleteCacheByKey(key: String) {
    cacheDataQueries.deleteCacheByKey(key)
}

fun UnifyDatabase.clearAllCache() {
    cacheDataQueries.deleteAllCache()
}

fun UnifyDatabase.getCacheSize(): Long {
    return cacheDataQueries.selectCacheCount().executeAsOne()
}

// 性能监控相关查询
fun UnifyDatabase.getPerformanceMetricsByComponent(componentId: String, limit: Long = 100): List<PerformanceMetricEntity> {
    return performanceMetricsQueries.selectMetricsByComponent(componentId, limit).executeAsList().map { metric ->
        PerformanceMetricEntity(
            id = metric.id,
            componentId = metric.componentId,
            metricType = metric.metricType,
            value = metric.value,
            timestamp = metric.timestamp,
            platform = metric.platform
        )
    }
}

fun UnifyDatabase.getPerformanceMetricsByType(metricType: String, limit: Long = 100): List<PerformanceMetricEntity> {
    return performanceMetricsQueries.selectMetricsByType(metricType, limit).executeAsList().map { metric ->
        PerformanceMetricEntity(
            id = metric.id,
            componentId = metric.componentId,
            metricType = metric.metricType,
            value = metric.value,
            timestamp = metric.timestamp,
            platform = metric.platform
        )
    }
}

fun UnifyDatabase.getPerformanceMetricsByPlatform(platform: String, limit: Long = 100): List<PerformanceMetricEntity> {
    return performanceMetricsQueries.selectMetricsByPlatform(platform, limit).executeAsList().map { metric ->
        PerformanceMetricEntity(
            id = metric.id,
            componentId = metric.componentId,
            metricType = metric.metricType,
            value = metric.value,
            timestamp = metric.timestamp,
            platform = metric.platform
        )
    }
}

fun UnifyDatabase.getAveragePerformanceMetric(componentId: String, metricType: String): Double? {
    return performanceMetricsQueries.selectAverageMetric(componentId, metricType).executeAsOneOrNull()
}

// 错误日志相关查询
fun UnifyDatabase.getErrorLogsByType(errorType: String, limit: Long = 100): List<ErrorLogEntity> {
    return errorLogQueries.selectErrorsByType(errorType, limit).executeAsList().map { error ->
        ErrorLogEntity(
            id = error.id,
            errorType = error.errorType,
            message = error.message,
            stackTrace = error.stackTrace,
            timestamp = error.timestamp,
            platform = error.platform,
            userId = error.userId
        )
    }
}

fun UnifyDatabase.getErrorLogsByPlatform(platform: String, limit: Long = 100): List<ErrorLogEntity> {
    return errorLogQueries.selectErrorsByPlatform(platform, limit).executeAsList().map { error ->
        ErrorLogEntity(
            id = error.id,
            errorType = error.errorType,
            message = error.message,
            stackTrace = error.stackTrace,
            timestamp = error.timestamp,
            platform = error.platform,
            userId = error.userId
        )
    }
}

fun UnifyDatabase.getErrorLogsByUser(userId: Long, limit: Long = 100): List<ErrorLogEntity> {
    return errorLogQueries.selectErrorsByUser(userId, limit).executeAsList().map { error ->
        ErrorLogEntity(
            id = error.id,
            errorType = error.errorType,
            message = error.message,
            stackTrace = error.stackTrace,
            timestamp = error.timestamp,
            platform = error.platform,
            userId = error.userId
        )
    }
}

fun UnifyDatabase.getRecentErrors(hours: Int = 24, limit: Long = 100): List<ErrorLogEntity> {
    val cutoffTime = System.currentTimeMillis() - (hours * 60 * 60 * 1000L)
    return errorLogQueries.selectRecentErrors(cutoffTime, limit).executeAsList().map { error ->
        ErrorLogEntity(
            id = error.id,
            errorType = error.errorType,
            message = error.message,
            stackTrace = error.stackTrace,
            timestamp = error.timestamp,
            platform = error.platform,
            userId = error.userId
        )
    }
}

fun UnifyDatabase.getErrorCount(): Long {
    return errorLogQueries.selectErrorCount().executeAsOne()
}

fun UnifyDatabase.getErrorCountByType(errorType: String): Long {
    return errorLogQueries.selectErrorCountByType(errorType).executeAsOne()
}

// 批量操作
fun UnifyDatabase.insertUsersBatch(users: List<UserEntity>): List<Long> {
    val ids = mutableListOf<Long>()
    userQueries.transaction {
        users.forEach { user ->
            userQueries.insertUser(
                username = user.username,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
            ids.add(userQueries.selectLastInsertRowId().executeAsOne())
        }
    }
    return ids
}

fun UnifyDatabase.insertPerformanceMetricsBatch(metrics: List<PerformanceMetricEntity>) {
    performanceMetricsQueries.transaction {
        metrics.forEach { metric ->
            performanceMetricsQueries.insertPerformanceMetric(
                componentId = metric.componentId,
                metricType = metric.metricType,
                value = metric.value,
                timestamp = metric.timestamp,
                platform = metric.platform
            )
        }
    }
}

fun UnifyDatabase.insertErrorLogsBatch(errors: List<ErrorLogEntity>) {
    errorLogQueries.transaction {
        errors.forEach { error ->
            errorLogQueries.insertErrorLog(
                errorType = error.errorType,
                message = error.message,
                stackTrace = error.stackTrace,
                timestamp = error.timestamp,
                platform = error.platform,
                userId = error.userId
            )
        }
    }
}

// 统计和分析查询
fun UnifyDatabase.getDatabaseStats(): DatabaseStats {
    return DatabaseStats(
        userCount = getUserCount(),
        configCount = appConfigQueries.selectConfigCount().executeAsOne(),
        cacheCount = getCacheSize(),
        performanceMetricCount = performanceMetricsQueries.selectMetricCount().executeAsOne(),
        errorLogCount = getErrorCount(),
        databaseSize = calculateDatabaseSize()
    )
}

private fun UnifyDatabase.calculateDatabaseSize(): Long {
    // This is a simplified calculation - in practice you'd query actual database size
    return getUserCount() * 1024 + // Estimate 1KB per user
           appConfigQueries.selectConfigCount().executeAsOne() * 256 + // 256B per config
           getCacheSize() * 2048 + // 2KB per cache entry
           performanceMetricsQueries.selectMetricCount().executeAsOne() * 128 + // 128B per metric
           getErrorCount() * 4096 // 4KB per error log
}

data class DatabaseStats(
    val userCount: Long,
    val configCount: Long,
    val cacheCount: Long,
    val performanceMetricCount: Long,
    val errorLogCount: Long,
    val databaseSize: Long
)
