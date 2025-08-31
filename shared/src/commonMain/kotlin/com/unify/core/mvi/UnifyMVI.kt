package com.unify.core.mvi

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * Unify-Core 生产级 MVI 架构系统
 * 提供完整的响应式状态管理和 Compose 集成
 */

/**
 * 1. 核心接口定义
 */
interface UnifyIntent

interface UnifyState

interface UnifyEffect

/**
 * 2. 高级状态管理器
 */
abstract class UnifyStateManager<I : UnifyIntent, S : UnifyState, E : UnifyEffect>(
    initialState: S,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<E>()
    val effect: SharedFlow<E> = _effect.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    protected fun updateState(reducer: (S) -> S) {
        _state.value = reducer(_state.value)
    }
    
    protected fun sendEffect(effect: E) {
        scope.launch {
            _effect.emit(effect)
        }
    }
    
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    abstract fun handleIntent(intent: I)
    
    /**
     * 异步意图处理
     */
    protected fun handleAsyncIntent(
        intent: I,
        block: suspend () -> Unit
    ) {
        scope.launch {
            try {
                setLoading(true)
                block()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                setLoading(false)
            }
        }
    }
    
    protected open fun handleError(error: Exception) {
        // 子类可以重写错误处理逻辑
    }
}

/**
 * 3. ViewModel 基类
 */
abstract class UnifyViewModel<I : UnifyIntent, S : UnifyState, E : UnifyEffect>(
    initialState: S,
    scope: CoroutineScope
) : UnifyStateManager<I, S, E>(initialState, scope) {
    
    /**
     * 生命周期方法
     */
    open fun onStart() {}
    open fun onStop() {}
    open fun onDestroy() {}
}

/**
 * 4. Compose 集成容器
 */
@Composable
fun <I : UnifyIntent, S : UnifyState, E : UnifyEffect> UnifyMVIContainer(
    stateManager: UnifyStateManager<I, S, E>,
    onEffect: (E) -> Unit = {},
    content: @Composable (state: S, isLoading: Boolean, onIntent: (I) -> Unit) -> Unit
) {
    val state by stateManager.state.collectAsState()
    val isLoading by stateManager.isLoading.collectAsState()
    
    LaunchedEffect(stateManager) {
        stateManager.effect.collect { effect ->
            onEffect(effect)
        }
    }
    
    content(state, isLoading) { intent ->
        stateManager.handleIntent(intent)
    }
}

/**
 * 5. 通用状态类型
 */
sealed class UnifyLoadingState {
    object Idle : UnifyLoadingState()
    object Loading : UnifyLoadingState()
    data class Success<T>(val data: T) : UnifyLoadingState()
    data class Error(val message: String, val exception: Throwable? = null) : UnifyLoadingState()
}

/**
 * 6. 通用意图类型
 */
sealed class UnifyCommonIntent : UnifyIntent {
    object Refresh : UnifyCommonIntent()
    object Retry : UnifyCommonIntent()
    object LoadMore : UnifyCommonIntent()
}

/**
 * 7. 通用效果类型
 */
sealed class UnifyCommonEffect : UnifyEffect {
    data class ShowToast(val message: String) : UnifyCommonEffect()
    data class Navigate(val route: String) : UnifyCommonEffect()
    object NavigateBack : UnifyCommonEffect()
    data class ShowError(val error: String) : UnifyCommonEffect()
}

/**
 * 8. 状态组合器
 */
class UnifyStateComposer<S : UnifyState> {
    private val states = mutableMapOf<String, StateFlow<*>>()
    
    fun <T> addState(key: String, state: StateFlow<T>) {
        states[key] = state
    }
    
    @Composable
    fun <T> collectState(key: String): State<T?> {
        val stateFlow = states[key] as? StateFlow<T>
        return stateFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    }
}

/**
 * 9. 中间件系统
 */
interface UnifyMiddleware<I : UnifyIntent, S : UnifyState, E : UnifyEffect> {
    suspend fun process(
        intent: I,
        currentState: S,
        next: suspend (I) -> Unit
    )
}

class UnifyLoggingMiddleware<I : UnifyIntent, S : UnifyState, E : UnifyEffect> : 
    UnifyMiddleware<I, S, E> {
    
    override suspend fun intercept(
        intent: I,
        currentState: S,
        next: suspend (I) -> Unit
    ) {
        UnifyPerformanceMonitor.recordMetric("mvi_intent_processed", 1.0, "count",
            mapOf("intent_type" to intent::class.simpleName.orEmpty()))
        next(intent)
    }
}

/**
 * 10. 状态持久化
 */
interface UnifyStatePersistence<S : UnifyState> {
    suspend fun saveState(state: S)
    suspend fun loadState(): S?
}

/**
 * 11. 测试工具
 */
class UnifyMVITestHelper<I : UnifyIntent, S : UnifyState, E : UnifyEffect>(
    private val stateManager: UnifyStateManager<I, S, E>
) {
    val stateHistory = mutableListOf<S>()
    val effectHistory = mutableListOf<E>()
    
    fun startRecording() {
        // 记录状态和效果变化用于测试
    }
    
    fun assertState(predicate: (S) -> Boolean): Boolean {
        return predicate(stateManager.state.value)
    }
    
    fun assertLastEffect(predicate: (E) -> Boolean): Boolean {
        return effectHistory.lastOrNull()?.let(predicate) ?: false
    }
}
