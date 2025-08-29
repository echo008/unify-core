package com.unify.state

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlin.reflect.KClass

/**
 * 统一状态存储
 * 基于Redux模式的全局状态管理
 */
class UnifyStateStore<S : State>(
    initialState: S,
    private val reducer: StateReducer<S>,
    private val middleware: List<StateMiddleware<S>> = emptyList(),
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()
    
    private val _actions = MutableSharedFlow<Action>()
    val actions: SharedFlow<Action> = _actions.asSharedFlow()
    
    private val _effects = MutableSharedFlow<Effect>()
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()
    
    private val stateHistory = mutableListOf<StateSnapshot<S>>()
    private var maxHistorySize = 50
    
    init {
        // 监听action并处理状态更新
        scope.launch {
            _actions.collect { action ->
                processAction(action)
            }
        }
    }
    
    fun dispatch(action: Action) {
        scope.launch {
            _actions.emit(action)
        }
    }
    
    fun dispatchEffect(effect: Effect) {
        scope.launch {
            _effects.emit(effect)
        }
    }
    
    private suspend fun processAction(action: Action) {
        val currentState = _state.value
        
        // 应用中间件
        var processedAction = action
        middleware.forEach { middleware ->
            processedAction = middleware.beforeReduce(currentState, processedAction)
        }
        
        // 应用reducer
        val newState = reducer.reduce(currentState, processedAction)
        
        // 保存状态历史
        saveStateSnapshot(currentState, processedAction)
        
        // 更新状态
        _state.value = newState
        
        // 应用中间件后处理
        middleware.forEach { middleware ->
            middleware.afterReduce(currentState, processedAction, newState)
        }
    }
    
    private fun saveStateSnapshot(state: S, action: Action) {
        val snapshot = StateSnapshot(
            state = state,
            action = action,
            timestamp = Clock.System.now().epochSeconds
        )
        
        stateHistory.add(snapshot)
        
        // 限制历史记录大小
        if (stateHistory.size > maxHistorySize) {
            stateHistory.removeAt(0)
        }
    }
    
    fun getStateHistory(): List<StateSnapshot<S>> = stateHistory.toList()
    
    fun timeTravel(snapshotIndex: Int) {
        if (snapshotIndex in 0 until stateHistory.size) {
            val snapshot = stateHistory[snapshotIndex]
            _state.value = snapshot.state
        }
    }
    
    fun clearHistory() {
        stateHistory.clear()
    }
}

/**
 * 状态基类
 */
interface State

/**
 * 动作基类
 */
interface Action {
    val type: String
    val timestamp: Long get() = Clock.System.now().epochSeconds
}

/**
 * 副作用基类
 */
interface Effect {
    val type: String
}

/**
 * 状态快照
 */
@Serializable
data class StateSnapshot<S : State>(
    val state: S,
    val action: Action,
    val timestamp: Long
)

/**
 * 状态Reducer接口
 */
interface StateReducer<S : State> {
    fun reduce(state: S, action: Action): S
}

/**
 * 状态中间件接口
 */
interface StateMiddleware<S : State> {
    suspend fun beforeReduce(state: S, action: Action): Action = action
    suspend fun afterReduce(oldState: S, action: Action, newState: S) {}
}

/**
 * 日志中间件
 */
class LoggingMiddleware<S : State> : StateMiddleware<S> {
    override suspend fun beforeReduce(state: S, action: Action): Action {
        println("Action dispatched: ${action.type}")
        return action
    }
    
    override suspend fun afterReduce(oldState: S, action: Action, newState: S) {
        println("State updated after: ${action.type}")
    }
}

/**
 * 持久化中间件
 */
class PersistenceMiddleware<S : State>(
    private val storage: StateStorage<S>
) : StateMiddleware<S> {
    
    override suspend fun afterReduce(oldState: S, action: Action, newState: S) {
        storage.saveState(newState)
    }
}

/**
 * 状态存储接口
 */
interface StateStorage<S : State> {
    suspend fun saveState(state: S)
    suspend fun loadState(): S?
    suspend fun clearState()
}

/**
 * 状态选择器
 */
class StateSelector<S : State, T>(
    private val stateFlow: StateFlow<S>,
    private val selector: (S) -> T
) {
    fun select(): Flow<T> = stateFlow.map(selector).distinctUntilChanged()
}

/**
 * 状态存储构建器
 */
class UnifyStateStoreBuilder<S : State> {
    private var initialState: S? = null
    private var reducer: StateReducer<S>? = null
    private val middleware = mutableListOf<StateMiddleware<S>>()
    private var scope: CoroutineScope? = null
    
    fun initialState(state: S) = apply {
        this.initialState = state
    }
    
    fun reducer(reducer: StateReducer<S>) = apply {
        this.reducer = reducer
    }
    
    fun middleware(vararg middleware: StateMiddleware<S>) = apply {
        this.middleware.addAll(middleware)
    }
    
    fun scope(scope: CoroutineScope) = apply {
        this.scope = scope
    }
    
    fun build(): UnifyStateStore<S> {
        requireNotNull(initialState) { "Initial state must be provided" }
        requireNotNull(reducer) { "Reducer must be provided" }
        requireNotNull(scope) { "Coroutine scope must be provided" }
        
        return UnifyStateStore(
            initialState = initialState!!,
            reducer = reducer!!,
            middleware = middleware,
            scope = scope!!
        )
    }
}

/**
 * 组合状态管理器
 */
class CompositeStateManager {
    private val stores = mutableMapOf<KClass<*>, UnifyStateStore<*>>()
    
    fun <S : State> registerStore(stateClass: KClass<S>, store: UnifyStateStore<S>) {
        stores[stateClass] = store
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <S : State> getStore(stateClass: KClass<S>): UnifyStateStore<S>? {
        return stores[stateClass] as? UnifyStateStore<S>
    }
    
    fun <S : State> dispatch(stateClass: KClass<S>, action: Action) {
        getStore(stateClass)?.dispatch(action)
    }
    
    fun <S : State> getState(stateClass: KClass<S>): StateFlow<S>? {
        return getStore(stateClass)?.state
    }
}

/**
 * 状态管理工厂
 */
object StateStoreFactory {
    fun <S : State> create(
        initialState: S,
        reducer: StateReducer<S>,
        scope: CoroutineScope,
        enableLogging: Boolean = false,
        enablePersistence: Boolean = false,
        storage: StateStorage<S>? = null
    ): UnifyStateStore<S> {
        return UnifyStateStoreBuilder<S>()
            .initialState(initialState)
            .reducer(reducer)
            .scope(scope)
            .apply {
                if (enableLogging) {
                    middleware(LoggingMiddleware())
                }
                if (enablePersistence && storage != null) {
                    middleware(PersistenceMiddleware(storage))
                }
            }
            .build()
    }
}
