package com.unify.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * 增强MVI状态管理器
 * 基于文档第6章要求实现的状态管理与数据流架构
 */
class EnhancedMVIStateManager<S : State, I : Intent, E : Effect>(
    private val initialState: S,
    private val reducer: StateReducer<S, I>,
    private val middleware: List<Middleware<S, I, E>> = emptyList()
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Default)
    
    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow: StateFlow<S> = _stateFlow.asStateFlow()
    
    private val _intentChannel = Channel<I>(Channel.UNLIMITED)
    private val intentFlow = _intentChannel.receiveAsFlow()
    
    private val _effectFlow = MutableSharedFlow<E>()
    val effectFlow: SharedFlow<E> = _effectFlow.asSharedFlow()
    
    // 时间旅行调试支持
    private val stateHistory = mutableListOf<StateSnapshot<S>>()
    private val maxHistorySize = 100
    
    // 状态持久化
    private val statePersistence = StatePersistenceManager<S>()
    
    init {
        // 启动意图处理
        scope.launch {
            intentFlow.collect { intent ->
                processIntent(intent)
            }
        }
        
        // 恢复持久化状态
        scope.launch {
            statePersistence.restoreState()?.let { restoredState ->
                _stateFlow.value = restoredState
            }
        }
    }
    
    /**
     * 发送意图
     */
    fun sendIntent(intent: I) {
        scope.launch {
            _intentChannel.send(intent)
        }
    }
    
    /**
     * 发送副作用
     */
    fun sendEffect(effect: E) {
        scope.launch {
            _effectFlow.emit(effect)
        }
    }
    
    /**
     * 处理意图
     */
    private suspend fun processIntent(intent: I) {
        val currentState = _stateFlow.value
        
        // 保存状态快照
        saveStateSnapshot(currentState, intent)
        
        // 应用中间件（前处理）
        var processedIntent = intent
        middleware.forEach { middleware ->
            processedIntent = middleware.preProcess(currentState, processedIntent)
        }
        
        // 状态归约
        val newState = reducer.reduce(currentState, processedIntent)
        
        // 应用中间件（后处理）
        var finalState = newState
        val effects = mutableListOf<E>()
        middleware.forEach { middleware ->
            val result = middleware.postProcess(currentState, processedIntent, finalState)
            finalState = result.state
            effects.addAll(result.effects)
        }
        
        // 更新状态
        _stateFlow.value = finalState
        
        // 持久化状态
        statePersistence.persistState(finalState)
        
        // 发送副作用
        effects.forEach { effect ->
            _effectFlow.emit(effect)
        }
    }
    
    /**
     * 保存状态快照
     */
    private fun saveStateSnapshot(state: S, intent: I) {
        val snapshot = StateSnapshot(
            state = state,
            intent = intent,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        
        stateHistory.add(snapshot)
        
        // 限制历史记录大小
        if (stateHistory.size > maxHistorySize) {
            stateHistory.removeAt(0)
        }
    }
    
    /**
     * 时间旅行调试：回到指定状态
     */
    fun timeTravel(index: Int) {
        if (index >= 0 && index < stateHistory.size) {
            val snapshot = stateHistory[index]
            _stateFlow.value = snapshot.state
        }
    }
    
    /**
     * 获取状态历史
     */
    fun getStateHistory(): List<StateSnapshot<S>> = stateHistory.toList()
    
    /**
     * 清理资源
     */
    fun dispose() {
        job.cancel()
        _intentChannel.close()
    }
}

/**
 * 状态接口
 */
interface State

/**
 * 意图接口
 */
interface Intent

/**
 * 副作用接口
 */
interface Effect

/**
 * 状态归约器
 */
interface StateReducer<S : State, I : Intent> {
    fun reduce(currentState: S, intent: I): S
}

/**
 * 中间件接口
 */
interface Middleware<S : State, I : Intent, E : Effect> {
    fun preProcess(state: S, intent: I): I = intent
    fun postProcess(state: S, intent: I, newState: S): MiddlewareResult<S, E>
}

/**
 * 中间件结果
 */
data class MiddlewareResult<S : State, E : Effect>(
    val state: S,
    val effects: List<E> = emptyList()
)

/**
 * 状态快照
 */
data class StateSnapshot<S : State>(
    val state: S,
    val intent: Intent,
    val timestamp: Long
)

/**
 * 状态持久化管理器
 */
class StatePersistenceManager<S : State> {
    private var persistedState: S? = null
    
    suspend fun persistState(state: S) {
        // 实际实现中应该使用平台特定的存储
        persistedState = state
    }
    
    suspend fun restoreState(): S? {
        return persistedState
    }
    
    suspend fun clearPersistedState() {
        persistedState = null
    }
}

/**
 * 日志中间件
 */
class LoggingMiddleware<S : State, I : Intent, E : Effect> : Middleware<S, I, E> {
    override fun preProcess(state: S, intent: I): I {
        println("MVI: Processing intent: ${intent::class.simpleName}")
        return intent
    }
    
    override fun postProcess(state: S, intent: I, newState: S): MiddlewareResult<S, E> {
        println("MVI: State changed from ${state::class.simpleName} to ${newState::class.simpleName}")
        return MiddlewareResult(newState)
    }
}

/**
 * 性能监控中间件
 */
class PerformanceMiddleware<S : State, I : Intent, E : Effect> : Middleware<S, I, E> {
    private val performanceTracker = PerformanceTracker()
    
    override fun preProcess(state: S, intent: I): I {
        performanceTracker.startTracking(intent::class.simpleName ?: "Unknown")
        return intent
    }
    
    override fun postProcess(state: S, intent: I, newState: S): MiddlewareResult<S, E> {
        performanceTracker.endTracking(intent::class.simpleName ?: "Unknown")
        return MiddlewareResult(newState)
    }
}

/**
 * 性能追踪器
 */
class PerformanceTracker {
    private val trackingData = mutableMapOf<String, Long>()
    
    fun startTracking(operation: String) {
        trackingData[operation] = Clock.System.now().toEpochMilliseconds()
    }
    
    fun endTracking(operation: String) {
        val startTime = trackingData[operation]
        if (startTime != null) {
            val duration = Clock.System.now().toEpochMilliseconds() - startTime
            println("MVI Performance: $operation took ${duration}ms")
            trackingData.remove(operation)
        }
    }
}

/**
 * 错误处理中间件
 */
class ErrorHandlingMiddleware<S : State, I : Intent, E : Effect>(
    private val errorEffectFactory: (Throwable) -> E
) : Middleware<S, I, E> {
    
    override fun postProcess(state: S, intent: I, newState: S): MiddlewareResult<S, E> {
        return try {
            MiddlewareResult(newState)
        } catch (e: Exception) {
            val errorEffect = errorEffectFactory(e)
            MiddlewareResult(state, listOf(errorEffect))
        }
    }
}
