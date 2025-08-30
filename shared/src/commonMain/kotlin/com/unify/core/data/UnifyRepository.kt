package com.unify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

/**
 * Unify-Core 生产级数据层架构
 * Repository 模式 + 网络/本地数据源
 */

/**
 * 1. 数据结果封装
 */
sealed class UnifyResult<out T> {
    data class Success<T>(val data: T) : UnifyResult<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "未知错误") : UnifyResult<Nothing>()
    object Loading : UnifyResult<Nothing>()
}

/**
 * 2. 网络响应封装
 */
@Serializable
data class UnifyApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val code: Int = 200
)

/**
 * 3. 分页数据封装
 */
@Serializable
data class UnifyPagedData<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val hasMore: Boolean
)

/**
 * 4. 基础 Repository 接口
 */
interface UnifyRepository<T, ID> {
    suspend fun getAll(): UnifyResult<List<T>>
    suspend fun getById(id: ID): UnifyResult<T?>
    suspend fun create(item: T): UnifyResult<T>
    suspend fun update(item: T): UnifyResult<T>
    suspend fun delete(id: ID): UnifyResult<Boolean>
    
    // 响应式数据流
    fun observeAll(): Flow<UnifyResult<List<T>>>
    fun observeById(id: ID): Flow<UnifyResult<T?>>
}

/**
 * 5. 网络数据源接口
 */
interface UnifyNetworkDataSource<T, ID> {
    suspend fun fetchAll(): List<T>
    suspend fun fetchById(id: ID): T?
    suspend fun create(item: T): T
    suspend fun update(item: T): T
    suspend fun delete(id: ID): Boolean
    
    // 分页支持
    suspend fun fetchPaged(page: Int, pageSize: Int): UnifyPagedData<T>
}

/**
 * 6. 本地数据源接口
 */
interface UnifyLocalDataSource<T, ID> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: ID): T?
    suspend fun insert(item: T): T
    suspend fun update(item: T): T
    suspend fun delete(id: ID): Boolean
    suspend fun clear()
    
    // 响应式查询
    fun observeAll(): Flow<List<T>>
    fun observeById(id: ID): Flow<T?>
}

/**
 * 7. 缓存策略
 */
enum class UnifyCacheStrategy {
    CACHE_FIRST,    // 优先使用缓存
    NETWORK_FIRST,  // 优先使用网络
    CACHE_ONLY,     // 仅使用缓存
    NETWORK_ONLY    // 仅使用网络
}

/**
 * 8. 基础 Repository 实现
 */
abstract class UnifyBaseRepository<T, ID>(
    protected val networkDataSource: UnifyNetworkDataSource<T, ID>,
    protected val localDataSource: UnifyLocalDataSource<T, ID>,
    private val cacheStrategy: UnifyCacheStrategy = UnifyCacheStrategy.CACHE_FIRST
) : UnifyRepository<T, ID> {

    override suspend fun getAll(): UnifyResult<List<T>> {
        return try {
            when (cacheStrategy) {
                UnifyCacheStrategy.CACHE_FIRST -> {
                    val cached = localDataSource.getAll()
                    if (cached.isNotEmpty()) {
                        UnifyResult.Success(cached)
                    } else {
                        fetchAndCache()
                    }
                }
                UnifyCacheStrategy.NETWORK_FIRST -> fetchAndCache()
                UnifyCacheStrategy.CACHE_ONLY -> UnifyResult.Success(localDataSource.getAll())
                UnifyCacheStrategy.NETWORK_ONLY -> UnifyResult.Success(networkDataSource.fetchAll())
            }
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }

    override suspend fun getById(id: ID): UnifyResult<T?> {
        return try {
            when (cacheStrategy) {
                UnifyCacheStrategy.CACHE_FIRST -> {
                    val cached = localDataSource.getById(id)
                    if (cached != null) {
                        UnifyResult.Success(cached)
                    } else {
                        val network = networkDataSource.fetchById(id)
                        network?.let { localDataSource.insert(it) }
                        UnifyResult.Success(network)
                    }
                }
                UnifyCacheStrategy.NETWORK_FIRST -> {
                    val network = networkDataSource.fetchById(id)
                    network?.let { localDataSource.insert(it) }
                    UnifyResult.Success(network)
                }
                UnifyCacheStrategy.CACHE_ONLY -> UnifyResult.Success(localDataSource.getById(id))
                UnifyCacheStrategy.NETWORK_ONLY -> UnifyResult.Success(networkDataSource.fetchById(id))
            }
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }

    override suspend fun create(item: T): UnifyResult<T> {
        return try {
            val created = networkDataSource.create(item)
            localDataSource.insert(created)
            UnifyResult.Success(created)
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }

    override suspend fun update(item: T): UnifyResult<T> {
        return try {
            val updated = networkDataSource.update(item)
            localDataSource.update(updated)
            UnifyResult.Success(updated)
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }

    override suspend fun delete(id: ID): UnifyResult<Boolean> {
        return try {
            val success = networkDataSource.delete(id)
            if (success) {
                localDataSource.delete(id)
            }
            UnifyResult.Success(success)
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }

    override fun observeAll(): Flow<UnifyResult<List<T>>> = flow {
        emit(UnifyResult.Loading)
        try {
            localDataSource.observeAll().collect { cached ->
                emit(UnifyResult.Success(cached))
            }
        } catch (e: Exception) {
            emit(UnifyResult.Error(e))
        }
    }

    override fun observeById(id: ID): Flow<UnifyResult<T?>> = flow {
        emit(UnifyResult.Loading)
        try {
            localDataSource.observeById(id).collect { cached ->
                emit(UnifyResult.Success(cached))
            }
        } catch (e: Exception) {
            emit(UnifyResult.Error(e))
        }
    }

    private suspend fun fetchAndCache(): UnifyResult<List<T>> {
        val network = networkDataSource.fetchAll()
        localDataSource.clear()
        network.forEach { localDataSource.insert(it) }
        return UnifyResult.Success(network)
    }
}

/**
 * 9. 同步管理器
 */
class UnifySyncManager<T, ID>(
    private val repository: UnifyRepository<T, ID>
) {
    suspend fun syncAll(): UnifyResult<Boolean> {
        return try {
            when (val result = repository.getAll()) {
                is UnifyResult.Success -> UnifyResult.Success(true)
                is UnifyResult.Error -> result
                UnifyResult.Loading -> UnifyResult.Success(false)
            }
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }
    
    suspend fun syncById(id: ID): UnifyResult<Boolean> {
        return try {
            when (val result = repository.getById(id)) {
                is UnifyResult.Success -> UnifyResult.Success(true)
                is UnifyResult.Error -> result
                UnifyResult.Loading -> UnifyResult.Success(false)
            }
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }
}

/**
 * 10. 数据验证器
 */
interface UnifyDataValidator<T> {
    fun validate(data: T): UnifyValidationResult
}

data class UnifyValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

/**
 * 11. 数据转换器
 */
interface UnifyDataMapper<From, To> {
    fun map(from: From): To
    fun mapList(from: List<From>): List<To> = from.map { map(it) }
}

/**
 * 12. 离线支持
 */
class UnifyOfflineManager<T, ID>(
    private val repository: UnifyRepository<T, ID>
) {
    private val pendingOperations = mutableListOf<UnifyPendingOperation<T, ID>>()
    
    suspend fun executePendingOperations(): List<UnifyResult<*>> {
        val results = mutableListOf<UnifyResult<*>>()
        
        pendingOperations.forEach { operation ->
            val result = when (operation.type) {
                UnifyOperationType.CREATE -> repository.create(operation.data!!)
                UnifyOperationType.UPDATE -> repository.update(operation.data!!)
                UnifyOperationType.DELETE -> repository.delete(operation.id!!)
            }
            results.add(result)
        }
        
        pendingOperations.clear()
        return results
    }
    
    fun addPendingOperation(operation: UnifyPendingOperation<T, ID>) {
        pendingOperations.add(operation)
    }
}

data class UnifyPendingOperation<T, ID>(
    val type: UnifyOperationType,
    val id: ID? = null,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UnifyOperationType {
    CREATE, UPDATE, DELETE
}
