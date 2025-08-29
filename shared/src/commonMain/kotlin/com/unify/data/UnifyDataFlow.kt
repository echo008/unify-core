package com.unify.data

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 统一数据流管理器
 * 提供响应式数据流处理和缓存机制
 */
class UnifyDataFlow<T>(
    private val scope: CoroutineScope,
    private val cacheConfig: CacheConfig = CacheConfig()
) {
    private val _data = MutableStateFlow<DataState<T>>(DataState.Loading)
    val data: StateFlow<DataState<T>> = _data.asStateFlow()
    
    private var lastFetchTime: Long = 0
    private var cachedData: T? = null
    
    fun fetch(
        fetcher: suspend () -> T,
        forceRefresh: Boolean = false
    ) {
        scope.launch {
            val now = Clock.System.now().epochSeconds
            
            // 检查缓存是否有效
            if (!forceRefresh && isCacheValid(now) && cachedData != null) {
                _data.value = DataState.Success(cachedData!!)
                return@launch
            }
            
            _data.value = DataState.Loading
            
            try {
                val result = fetcher()
                cachedData = result
                lastFetchTime = now
                _data.value = DataState.Success(result)
            } catch (e: Exception) {
                _data.value = DataState.Error(e.message ?: "Unknown error", e)
            }
        }
    }
    
    fun refresh(fetcher: suspend () -> T) {
        fetch(fetcher, forceRefresh = true)
    }
    
    fun clear() {
        cachedData = null
        lastFetchTime = 0
        _data.value = DataState.Loading
    }
    
    private fun isCacheValid(currentTime: Long): Boolean {
        return (currentTime - lastFetchTime) < cacheConfig.ttlSeconds
    }
}

/**
 * 数据状态封装
 */
sealed class DataState<out T> {
    object Loading : DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : DataState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}

/**
 * 缓存配置
 */
data class CacheConfig(
    val ttlSeconds: Long = 300, // 5分钟默认TTL
    val maxSize: Int = 100
)

/**
 * 分页数据流管理器
 */
class PaginatedDataFlow<T>(
    private val scope: CoroutineScope,
    private val pageSize: Int = 20
) {
    private val _data = MutableStateFlow<PaginatedState<T>>(PaginatedState.initial())
    val data: StateFlow<PaginatedState<T>> = _data.asStateFlow()
    
    fun loadFirstPage(fetcher: suspend (page: Int, size: Int) -> List<T>) {
        scope.launch {
            _data.value = _data.value.copy(isLoading = true, error = null)
            
            try {
                val items = fetcher(0, pageSize)
                _data.value = PaginatedState(
                    items = items,
                    currentPage = 0,
                    hasMore = items.size == pageSize,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _data.value = _data.value.copy(
                    isLoading = false,
                    error = e.message ?: "Load failed"
                )
            }
        }
    }
    
    fun loadNextPage(fetcher: suspend (page: Int, size: Int) -> List<T>) {
        val currentState = _data.value
        if (currentState.isLoading || !currentState.hasMore) return
        
        scope.launch {
            _data.value = currentState.copy(isLoading = true)
            
            try {
                val nextPage = currentState.currentPage + 1
                val newItems = fetcher(nextPage, pageSize)
                
                _data.value = currentState.copy(
                    items = currentState.items + newItems,
                    currentPage = nextPage,
                    hasMore = newItems.size == pageSize,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _data.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "Load failed"
                )
            }
        }
    }
    
    fun refresh(fetcher: suspend (page: Int, size: Int) -> List<T>) {
        loadFirstPage(fetcher)
    }
}

/**
 * 分页状态
 */
data class PaginatedState<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int = -1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        fun <T> initial() = PaginatedState<T>(isLoading = true)
    }
}

/**
 * 实时数据流管理器
 */
class RealtimeDataFlow<T>(
    private val scope: CoroutineScope,
    private val updateInterval: Duration = 30.seconds
) {
    private val _data = MutableStateFlow<DataState<T>>(DataState.Loading)
    val data: StateFlow<DataState<T>> = _data.asStateFlow()
    
    private var isActive = false
    
    fun start(fetcher: suspend () -> T) {
        if (isActive) return
        
        isActive = true
        scope.launch {
            while (isActive) {
                try {
                    val result = fetcher()
                    _data.value = DataState.Success(result)
                } catch (e: Exception) {
                    _data.value = DataState.Error(e.message ?: "Fetch failed", e)
                }
                
                delay(updateInterval)
            }
        }
    }
    
    fun stop() {
        isActive = false
    }
}

/**
 * 数据流组合器
 */
class DataFlowCombiner {
    fun <T1, T2, R> combine(
        flow1: StateFlow<DataState<T1>>,
        flow2: StateFlow<DataState<T2>>,
        combiner: (T1, T2) -> R
    ): Flow<DataState<R>> {
        return kotlinx.coroutines.flow.combine(flow1, flow2) { state1, state2 ->
            when {
                state1 is DataState.Loading || state2 is DataState.Loading -> DataState.Loading
                state1 is DataState.Error -> state1
                state2 is DataState.Error -> state2
                state1 is DataState.Success && state2 is DataState.Success -> {
                    try {
                        DataState.Success(combiner(state1.data, state2.data))
                    } catch (e: Exception) {
                        DataState.Error(e.message ?: "Combine failed", e)
                    }
                }
                else -> DataState.Loading
            }
        }
    }
    
    fun <T1, T2, T3, R> combine(
        flow1: StateFlow<DataState<T1>>,
        flow2: StateFlow<DataState<T2>>,
        flow3: StateFlow<DataState<T3>>,
        combiner: (T1, T2, T3) -> R
    ): Flow<DataState<R>> {
        return kotlinx.coroutines.flow.combine(flow1, flow2, flow3) { state1, state2, state3 ->
            when {
                state1 is DataState.Loading || state2 is DataState.Loading || state3 is DataState.Loading -> 
                    DataState.Loading
                state1 is DataState.Error -> state1
                state2 is DataState.Error -> state2
                state3 is DataState.Error -> state3
                state1 is DataState.Success && state2 is DataState.Success && state3 is DataState.Success -> {
                    try {
                        DataState.Success(combiner(state1.data, state2.data, state3.data))
                    } catch (e: Exception) {
                        DataState.Error(e.message ?: "Combine failed", e)
                    }
                }
                else -> DataState.Loading
            }
        }
    }
}

/**
 * 数据流工厂
 */
object DataFlowFactory {
    fun <T> createSimple(scope: CoroutineScope, cacheConfig: CacheConfig = CacheConfig()): UnifyDataFlow<T> {
        return UnifyDataFlow(scope, cacheConfig)
    }
    
    fun <T> createPaginated(scope: CoroutineScope, pageSize: Int = 20): PaginatedDataFlow<T> {
        return PaginatedDataFlow(scope, pageSize)
    }
    
    fun <T> createRealtime(
        scope: CoroutineScope, 
        updateInterval: Duration = 30.seconds
    ): RealtimeDataFlow<T> {
        return RealtimeDataFlow(scope, updateInterval)
    }
}

/**
 * 数据流扩展函数
 */
fun <T> StateFlow<DataState<T>>.onSuccess(action: (T) -> Unit): StateFlow<DataState<T>> {
    return this.also { flow ->
        flow.value.let { state ->
            if (state is DataState.Success) {
                action(state.data)
            }
        }
    }
}

fun <T> StateFlow<DataState<T>>.onError(action: (String, Throwable?) -> Unit): StateFlow<DataState<T>> {
    return this.also { flow ->
        flow.value.let { state ->
            if (state is DataState.Error) {
                action(state.message, state.exception)
            }
        }
    }
}

fun <T, R> StateFlow<DataState<T>>.mapData(mapper: (T) -> R): Flow<DataState<R>> {
    return this.map { state ->
        when (state) {
            is DataState.Loading -> DataState.Loading
            is DataState.Error -> state
            is DataState.Success -> {
                try {
                    DataState.Success(mapper(state.data))
                } catch (e: Exception) {
                    DataState.Error(e.message ?: "Map failed", e)
                }
            }
        }
    }
}
