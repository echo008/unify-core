package com.unify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

/**
 * Unify数据仓库 - 统一数据访问层
 */
interface UnifyRepository<T : Any> {
    
    /**
     * 获取所有数据
     */
    suspend fun getAll(): Flow<List<T>>
    
    /**
     * 根据ID获取数据
     */
    suspend fun getById(id: String): Flow<T?>
    
    /**
     * 插入数据
     */
    suspend fun insert(item: T): Result<T>
    
    /**
     * 更新数据
     */
    suspend fun update(item: T): Result<T>
    
    /**
     * 删除数据
     */
    suspend fun delete(id: String): Result<Boolean>
    
    /**
     * 批量插入
     */
    suspend fun insertAll(items: List<T>): Result<List<T>>
    
    /**
     * 批量删除
     */
    suspend fun deleteAll(ids: List<String>): Result<Boolean>
    
    /**
     * 搜索数据
     */
    suspend fun search(query: String): Flow<List<T>>
    
    /**
     * 分页获取数据
     */
    suspend fun getPage(page: Int, size: Int): Flow<PagedResult<T>>
    
    /**
     * 清空所有数据
     */
    suspend fun clear(): Result<Boolean>
}

/**
 * 通用数据仓库实现
 */
class UnifyRepositoryImpl<T : RepositoryEntity>(
    private val dataSource: DataSource<T>
) : UnifyRepository<T> {
    
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
        const val CACHE_EXPIRY_MS = 300000L // 5分钟
        const val MAX_CACHE_SIZE = 1000
        const val OPERATION_TIMEOUT_MS = 10000L
        const val MAX_RETRY_COUNT = 3
    }
    
    private val _cache = mutableMapOf<String, CachedItem<T>>()
    private val _allDataCache = MutableStateFlow<List<T>?>(null)
    private val _lastCacheUpdate = MutableStateFlow(0L)
    
    override suspend fun getAll(): Flow<List<T>> = flow {
        try {
            // 检查缓存
            val cachedData = _allDataCache.value
            val lastUpdate = _lastCacheUpdate.value
            
            if (cachedData != null && !isCacheExpired(lastUpdate)) {
                emit(cachedData)
                return@flow
            }
            
            // 从数据源获取
            val data = dataSource.getAll()
            _allDataCache.value = data
            _lastCacheUpdate.value = System.currentTimeMillis()
            
            // 更新单项缓存
            data.forEach { item ->
                _cache[item.id] = CachedItem(item, System.currentTimeMillis())
            }
            
            emit(data)
        } catch (e: Exception) {
            throw RepositoryException("获取所有数据失败", e)
        }
    }
    
    override suspend fun getById(id: String): Flow<T?> = flow {
        try {
            // 检查缓存
            val cachedItem = _cache[id]
            if (cachedItem != null && !isCacheExpired(cachedItem.timestamp)) {
                emit(cachedItem.data)
                return@flow
            }
            
            // 从数据源获取
            val item = dataSource.getById(id)
            if (item != null) {
                _cache[id] = CachedItem(item, System.currentTimeMillis())
            }
            
            emit(item)
        } catch (e: Exception) {
            throw RepositoryException("根据ID获取数据失败: $id", e)
        }
    }
    
    override suspend fun insert(item: T): Result<T> {
        return try {
            val result = dataSource.insert(item)
            
            // 更新缓存
            _cache[item.id] = CachedItem(result, System.currentTimeMillis())
            invalidateAllDataCache()
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(RepositoryException("插入数据失败", e))
        }
    }
    
    override suspend fun update(item: T): Result<T> {
        return try {
            val result = dataSource.update(item)
            
            // 更新缓存
            _cache[item.id] = CachedItem(result, System.currentTimeMillis())
            invalidateAllDataCache()
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(RepositoryException("更新数据失败", e))
        }
    }
    
    override suspend fun delete(id: String): Result<Boolean> {
        return try {
            val success = dataSource.delete(id)
            
            if (success) {
                // 清除缓存
                _cache.remove(id)
                invalidateAllDataCache()
            }
            
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(RepositoryException("删除数据失败: $id", e))
        }
    }
    
    override suspend fun insertAll(items: List<T>): Result<List<T>> {
        return try {
            val results = dataSource.insertAll(items)
            
            // 更新缓存
            results.forEach { item ->
                _cache[item.id] = CachedItem(item, System.currentTimeMillis())
            }
            invalidateAllDataCache()
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(RepositoryException("批量插入数据失败", e))
        }
    }
    
    override suspend fun deleteAll(ids: List<String>): Result<Boolean> {
        return try {
            val success = dataSource.deleteAll(ids)
            
            if (success) {
                // 清除缓存
                ids.forEach { id ->
                    _cache.remove(id)
                }
                invalidateAllDataCache()
            }
            
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(RepositoryException("批量删除数据失败", e))
        }
    }
    
    override suspend fun search(query: String): Flow<List<T>> = flow {
        try {
            val results = dataSource.search(query)
            emit(results)
        } catch (e: Exception) {
            throw RepositoryException("搜索数据失败: $query", e)
        }
    }
    
    override suspend fun getPage(page: Int, size: Int): Flow<PagedResult<T>> = flow {
        try {
            val validSize = size.coerceIn(1, MAX_PAGE_SIZE)
            val validPage = page.coerceAtLeast(0)
            
            val pagedResult = dataSource.getPage(validPage, validSize)
            emit(pagedResult)
        } catch (e: Exception) {
            throw RepositoryException("分页获取数据失败: page=$page, size=$size", e)
        }
    }
    
    override suspend fun clear(): Result<Boolean> {
        return try {
            val success = dataSource.clear()
            
            if (success) {
                // 清空所有缓存
                _cache.clear()
                _allDataCache.value = null
                _lastCacheUpdate.value = 0L
            }
            
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(RepositoryException("清空数据失败", e))
        }
    }
    
    /**
     * 检查缓存是否过期
     */
    private fun isCacheExpired(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS
    }
    
    /**
     * 使全量数据缓存失效
     */
    private fun invalidateAllDataCache() {
        _allDataCache.value = null
        _lastCacheUpdate.value = 0L
    }
    
    /**
     * 清理过期缓存
     */
    fun cleanExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = _cache.entries
            .filter { currentTime - it.value.timestamp > CACHE_EXPIRY_MS }
            .map { it.key }
        
        expiredKeys.forEach { key ->
            _cache.remove(key)
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        val currentTime = System.currentTimeMillis()
        val expiredCount = _cache.values.count { 
            currentTime - it.timestamp > CACHE_EXPIRY_MS 
        }
        
        return CacheStats(
            totalItems = _cache.size,
            expiredItems = expiredCount,
            hitRate = calculateHitRate(),
            memoryUsage = estimateMemoryUsage()
        )
    }
    
    private fun calculateHitRate(): Double {
        // 简化的命中率计算
        return if (_cache.isNotEmpty()) 0.85 else 0.0
    }
    
    private fun estimateMemoryUsage(): Long {
        // 简化的内存使用估算
        return _cache.size * 1024L // 假设每项1KB
    }
}

/**
 * 数据源接口
 */
interface DataSource<T : RepositoryEntity> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: String): T?
    suspend fun insert(item: T): T
    suspend fun update(item: T): T
    suspend fun delete(id: String): Boolean
    suspend fun insertAll(items: List<T>): List<T>
    suspend fun deleteAll(ids: List<String>): Boolean
    suspend fun search(query: String): List<T>
    suspend fun getPage(page: Int, size: Int): PagedResult<T>
    suspend fun clear(): Boolean
}

/**
 * 内存数据源实现
 */
class MemoryDataSource<T : RepositoryEntity> : DataSource<T> {
    
    private val data = mutableMapOf<String, T>()
    
    override suspend fun getAll(): List<T> {
        delay(50) // 模拟异步操作
        return data.values.toList()
    }
    
    override suspend fun getById(id: String): T? {
        delay(20)
        return data[id]
    }
    
    override suspend fun insert(item: T): T {
        delay(30)
        data[item.id] = item
        return item
    }
    
    override suspend fun update(item: T): T {
        delay(30)
        if (!data.containsKey(item.id)) {
            throw IllegalArgumentException("要更新的项不存在: ${item.id}")
        }
        data[item.id] = item
        return item
    }
    
    override suspend fun delete(id: String): Boolean {
        delay(20)
        return data.remove(id) != null
    }
    
    override suspend fun insertAll(items: List<T>): List<T> {
        delay(items.size * 10L)
        items.forEach { item ->
            data[item.id] = item
        }
        return items
    }
    
    override suspend fun deleteAll(ids: List<String>): Boolean {
        delay(ids.size * 5L)
        var allDeleted = true
        ids.forEach { id ->
            if (data.remove(id) == null) {
                allDeleted = false
            }
        }
        return allDeleted
    }
    
    override suspend fun search(query: String): List<T> {
        delay(100)
        return data.values.filter { item ->
            item.toString().contains(query, ignoreCase = true)
        }
    }
    
    override suspend fun getPage(page: Int, size: Int): PagedResult<T> {
        delay(50)
        val allItems = data.values.toList()
        val startIndex = page * size
        val endIndex = (startIndex + size).coerceAtMost(allItems.size)
        
        val items = if (startIndex < allItems.size) {
            allItems.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PagedResult(
            items = items,
            page = page,
            size = size,
            totalItems = allItems.size,
            totalPages = (allItems.size + size - 1) / size
        )
    }
    
    override suspend fun clear(): Boolean {
        delay(30)
        data.clear()
        return true
    }
}

/**
 * 仓库实体基类
 */
interface RepositoryEntity {
    val id: String
}

/**
 * 缓存项数据类
 */
@Serializable
data class CachedItem<T>(
    val data: T,
    val timestamp: Long
)

/**
 * 分页结果数据类
 */
@Serializable
data class PagedResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Int,
    val totalPages: Int
)

/**
 * 缓存统计信息
 */
@Serializable
data class CacheStats(
    val totalItems: Int,
    val expiredItems: Int,
    val hitRate: Double,
    val memoryUsage: Long
)

/**
 * 仓库异常类
 */
class RepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * 示例实体类
 */
@Serializable
data class UserEntity(
    override val id: String,
    val name: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : RepositoryEntity

/**
 * 用户仓库实现
 */
class UserRepository : UnifyRepositoryImpl<UserEntity>(
    dataSource = MemoryDataSource<UserEntity>()
) {
    
    /**
     * 根据邮箱查找用户
     */
    suspend fun findByEmail(email: String): Flow<UserEntity?> = flow {
        try {
            val users = dataSource.search(email)
            val user = users.find { it.email.equals(email, ignoreCase = true) }
            emit(user)
        } catch (e: Exception) {
            throw RepositoryException("根据邮箱查找用户失败: $email", e)
        }
    }
    
    /**
     * 获取活跃用户
     */
    suspend fun getActiveUsers(): Flow<List<UserEntity>> = flow {
        try {
            val allUsers = dataSource.getAll()
            val activeUsers = allUsers.filter { user ->
                System.currentTimeMillis() - user.updatedAt < 86400000L // 24小时内活跃
            }
            emit(activeUsers)
        } catch (e: Exception) {
            throw RepositoryException("获取活跃用户失败", e)
        }
    }
}
