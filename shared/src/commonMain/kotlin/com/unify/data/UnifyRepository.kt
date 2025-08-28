package com.unify.data

import com.unify.network.NetworkResult
import com.unify.network.UnifyNetworkService
import com.unify.storage.UnifyStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 统一数据仓库系统
 * 集成网络服务和本地存储，实现数据层统一管理
 */

/**
 * 数据源类型
 */
enum class DataSource {
    CACHE,      // 缓存优先
    NETWORK,    // 网络优先
    LOCAL,      // 本地存储优先
    CACHE_FIRST, // 缓存优先，失败时从网络获取
    NETWORK_FIRST // 网络优先，失败时从缓存获取
}

/**
 * 数据结果封装
 */
sealed class DataResult<out T> {
    data class Success<T>(val data: T, val source: DataSource) : DataResult<T>()
    data class Error(val exception: Exception, val source: DataSource) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}

/**
 * 数据策略配置
 */
@Serializable
data class DataStrategy(
    val cacheTimeout: Long = 300000L, // 5分钟
    val enableOfflineMode: Boolean = true,
    val retryCount: Int = 3,
    val retryDelay: Long = 1000L,
    val enablePrefetch: Boolean = false,
    val prefetchDelay: Long = 5000L
)

/**
 * 统一数据仓库接口
 */
interface UnifyRepository<T> {
    suspend fun getData(
        key: String,
        source: DataSource = DataSource.CACHE_FIRST,
        forceRefresh: Boolean = false
    ): DataResult<T>
    
    fun getDataStream(
        key: String,
        source: DataSource = DataSource.CACHE_FIRST
    ): Flow<DataResult<T>>
    
    suspend fun saveData(key: String, data: T)
    suspend fun removeData(key: String)
    suspend fun clearCache()
    suspend fun syncData(keys: List<String> = emptyList())
    suspend fun prefetchData(keys: List<String>)
}

/**
 * 抽象数据仓库基类
 */
abstract class BaseUnifyRepository<T>(
    protected val networkService: UnifyNetworkService,
    protected val localStorage: UnifyStorage,
    protected val strategy: DataStrategy = DataStrategy()
) : UnifyRepository<T> {
    
    protected abstract suspend fun fetchFromNetwork(key: String): NetworkResult<T>
    protected abstract suspend fun saveToLocal(key: String, data: T)
    protected abstract suspend fun loadFromLocal(key: String): T?
    protected abstract fun getCacheKey(key: String): String
    
    override suspend fun getData(
        key: String,
        source: DataSource,
        forceRefresh: Boolean
    ): DataResult<T> {
        return try {
            when (source) {
                DataSource.CACHE -> getFromCache(key)
                DataSource.NETWORK -> getFromNetwork(key)
                DataSource.LOCAL -> getFromLocal(key)
                DataSource.CACHE_FIRST -> getCacheFirst(key, forceRefresh)
                DataSource.NETWORK_FIRST -> getNetworkFirst(key)
            }
        } catch (e: Exception) {
            DataResult.Error(e, source)
        }
    }
    
    override fun getDataStream(
        key: String,
        source: DataSource
    ): Flow<DataResult<T>> = flow {
        emit(DataResult.Loading)
        
        // 首先尝试从本地获取数据
        try {
            val localData = loadFromLocal(key)
            if (localData != null) {
                emit(DataResult.Success(localData, DataSource.LOCAL))
            }
        } catch (e: Exception) {
            // 忽略本地数据错误，继续网络请求
        }
        
        // 然后从网络获取最新数据
        if (source != DataSource.LOCAL) {
            try {
                when (val networkResult = fetchFromNetwork(key)) {
                    is NetworkResult.Success -> {
                        saveToLocal(key, networkResult.data)
                        emit(DataResult.Success(networkResult.data, DataSource.NETWORK))
                    }
                    is NetworkResult.Error -> {
                        emit(DataResult.Error(networkResult.exception, DataSource.NETWORK))
                    }
                    is NetworkResult.Loading -> {
                        // 已经发送了Loading状态
                    }
                }
            } catch (e: Exception) {
                emit(DataResult.Error(e, DataSource.NETWORK))
            }
        }
    }.catch { e ->
        emit(DataResult.Error(Exception(e), source))
    }
    
    private suspend fun getFromCache(key: String): DataResult<T> {
        val cacheKey = getCacheKey(key)
        val cached = loadFromLocal(cacheKey)
        return if (cached != null && !isCacheExpired(cacheKey)) {
            DataResult.Success(cached, DataSource.CACHE)
        } else {
            DataResult.Error(Exception("Cache miss or expired"), DataSource.CACHE)
        }
    }
    
    private suspend fun getFromNetwork(key: String): DataResult<T> {
        return when (val result = fetchFromNetwork(key)) {
            is NetworkResult.Success -> {
                saveToLocal(key, result.data)
                DataResult.Success(result.data, DataSource.NETWORK)
            }
            is NetworkResult.Error -> {
                DataResult.Error(result.exception, DataSource.NETWORK)
            }
            is NetworkResult.Loading -> {
                DataResult.Loading
            }
        }
    }
    
    private suspend fun getFromLocal(key: String): DataResult<T> {
        val data = loadFromLocal(key)
        return if (data != null) {
            DataResult.Success(data, DataSource.LOCAL)
        } else {
            DataResult.Error(Exception("Local data not found"), DataSource.LOCAL)
        }
    }
    
    private suspend fun getCacheFirst(key: String, forceRefresh: Boolean): DataResult<T> {
        if (!forceRefresh) {
            val cacheResult = getFromCache(key)
            if (cacheResult is DataResult.Success) {
                return cacheResult
            }
        }
        
        return getFromNetwork(key)
    }
    
    private suspend fun getNetworkFirst(key: String): DataResult<T> {
        val networkResult = getFromNetwork(key)
        return if (networkResult is DataResult.Success) {
            networkResult
        } else {
            getFromLocal(key)
        }
    }
    
    override suspend fun saveData(key: String, data: T) {
        saveToLocal(key, data)
        setCacheTimestamp(getCacheKey(key))
    }
    
    override suspend fun removeData(key: String) {
        localStorage.remove(key)
        localStorage.remove(getCacheKey(key))
        localStorage.remove(getTimestampKey(key))
    }
    
    override suspend fun clearCache() {
        // 清理所有缓存数据
        val keys = localStorage.getAllKeys()
        keys.filter { it.startsWith("cache_") || it.startsWith("timestamp_") }
            .forEach { localStorage.remove(it) }
    }
    
    override suspend fun syncData(keys: List<String>) {
        keys.forEach { key ->
            try {
                val result = getFromNetwork(key)
                if (result is DataResult.Success) {
                    saveData(key, result.data)
                }
            } catch (e: Exception) {
                // 记录同步错误但继续处理其他数据
                println("Sync error for key $key: ${e.message}")
            }
        }
    }
    
    override suspend fun prefetchData(keys: List<String>) {
        keys.forEach { key ->
            try {
                getData(key, DataSource.NETWORK_FIRST)
            } catch (e: Exception) {
                // 预取错误不影响主流程
                println("Prefetch error for key $key: ${e.message}")
            }
        }
    }
    
    private suspend fun isCacheExpired(cacheKey: String): Boolean {
        val timestampKey = getTimestampKey(cacheKey)
        val timestamp = localStorage.getLong(timestampKey, 0L)
        return System.currentTimeMillis() - timestamp > strategy.cacheTimeout
    }
    
    private suspend fun setCacheTimestamp(cacheKey: String) {
        val timestampKey = getTimestampKey(cacheKey)
        localStorage.putLong(timestampKey, System.currentTimeMillis())
    }
    
    private fun getTimestampKey(key: String): String = "timestamp_$key"
}

/**
 * JSON数据仓库实现
 */
class JsonRepository<T>(
    networkService: UnifyNetworkService,
    localStorage: UnifyStorage,
    private val baseUrl: String,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T?,
    strategy: DataStrategy = DataStrategy()
) : BaseUnifyRepository<T>(networkService, localStorage, strategy) {
    
    override suspend fun fetchFromNetwork(key: String): NetworkResult<T> {
        return networkService.get("$baseUrl/$key")
    }
    
    override suspend fun saveToLocal(key: String, data: T) {
        val json = serializer(data)
        localStorage.putString(getCacheKey(key), json)
    }
    
    override suspend fun loadFromLocal(key: String): T? {
        val json = localStorage.getString(getCacheKey(key)) ?: return null
        return deserializer(json)
    }
    
    override fun getCacheKey(key: String): String = "cache_$key"
}

/**
 * 用户数据仓库示例
 */
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null
)

class UserRepository(
    networkService: UnifyNetworkService,
    localStorage: UnifyStorage,
    strategy: DataStrategy = DataStrategy()
) : BaseUnifyRepository<User>(networkService, localStorage, strategy) {
    
    override suspend fun fetchFromNetwork(key: String): NetworkResult<User> {
        return networkService.get("/api/users/$key")
    }
    
    override suspend fun saveToLocal(key: String, data: User) {
        val json = Json.encodeToString(User.serializer(), data)
        localStorage.putString(getCacheKey(key), json)
    }
    
    override suspend fun loadFromLocal(key: String): User? {
        val json = localStorage.getString(getCacheKey(key)) ?: return null
        return try {
            Json.decodeFromString(User.serializer(), json)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun getCacheKey(key: String): String = "user_cache_$key"
    
    suspend fun getCurrentUser(): DataResult<User> {
        return getData("current", DataSource.CACHE_FIRST)
    }
    
    suspend fun updateUser(user: User): DataResult<User> {
        return try {
            val result: NetworkResult<User> = networkService.put("/api/users/${user.id}", user)
            when (result) {
                is NetworkResult.Success -> {
                    saveData(user.id, result.data)
                    DataResult.Success(result.data, DataSource.NETWORK)
                }
                is NetworkResult.Error -> {
                    DataResult.Error(result.exception, DataSource.NETWORK)
                }
                is NetworkResult.Loading -> {
                    DataResult.Loading
                }
            }
        } catch (e: Exception) {
            DataResult.Error(e, DataSource.NETWORK)
        }
    }
}

/**
 * 数据同步管理器
 */
class DataSyncManager(
    private val repositories: List<UnifyRepository<*>>,
    private val strategy: DataStrategy = DataStrategy()
) {
    private var isSyncing = false
    
    suspend fun syncAll() {
        if (isSyncing) return
        
        isSyncing = true
        try {
            repositories.forEach { repository ->
                try {
                    repository.syncData()
                } catch (e: Exception) {
                    println("Repository sync error: ${e.message}")
                }
            }
        } finally {
            isSyncing = false
        }
    }
    
    suspend fun clearAllCaches() {
        repositories.forEach { repository ->
            try {
                repository.clearCache()
            } catch (e: Exception) {
                println("Cache clear error: ${e.message}")
            }
        }
    }
    
    fun startPeriodicSync(intervalMs: Long = 300000L) {
        // 启动定期同步（具体实现依赖平台）
    }
    
    fun stopPeriodicSync() {
        // 停止定期同步
    }
}

/**
 * 数据仓库工厂
 */
object RepositoryFactory {
    fun <T> createJsonRepository(
        networkService: UnifyNetworkService,
        localStorage: UnifyStorage,
        baseUrl: String,
        serializer: (T) -> String,
        deserializer: (String) -> T?,
        strategy: DataStrategy = DataStrategy()
    ): JsonRepository<T> {
        return JsonRepository(
            networkService,
            localStorage,
            baseUrl,
            serializer,
            deserializer,
            strategy
        )
    }
    
    fun createUserRepository(
        networkService: UnifyNetworkService,
        localStorage: UnifyStorage,
        strategy: DataStrategy = DataStrategy()
    ): UserRepository {
        return UserRepository(networkService, localStorage, strategy)
    }
}

/**
 * 数据管理器
 */
class UnifyDataManager(
    private val networkService: UnifyNetworkService,
    private val localStorage: UnifyStorage,
    private val strategy: DataStrategy = DataStrategy()
) {
    private val repositories = mutableMapOf<String, UnifyRepository<*>>()
    private val syncManager by lazy { DataSyncManager(repositories.values.toList(), strategy) }
    
    fun <T> getRepository(
        key: String,
        factory: () -> UnifyRepository<T>
    ): UnifyRepository<T> {
        @Suppress("UNCHECKED_CAST")
        return repositories.getOrPut(key) { factory() } as UnifyRepository<T>
    }
    
    suspend fun syncAll() {
        syncManager.syncAll()
    }
    
    suspend fun clearAllCaches() {
        syncManager.clearAllCaches()
    }
    
    fun enableOfflineMode() {
        // 启用离线模式
    }
    
    fun disableOfflineMode() {
        // 禁用离线模式
    }
}
