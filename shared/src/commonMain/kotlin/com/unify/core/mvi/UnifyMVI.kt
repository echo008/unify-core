package com.unify.core.mvi

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Unify MVI架构实现 - Model-View-Intent模式
 */
abstract class UnifyMVIViewModel<S : MVIState, I : MVIIntent, E : MVIEffect>(
    initialState: S,
    private val scope: CoroutineScope,
) {
    companion object {
        const val MAX_EFFECT_BUFFER = 64
        const val MAX_INTENT_BUFFER = 128
        const val STATE_REPLAY_CACHE = 1
        const val EFFECT_REPLAY_CACHE = 0
        const val PROCESSING_TIMEOUT_MS = 5000L
        const val MAX_RETRY_COUNT = 3
    }

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects =
        MutableSharedFlow<E>(
            replay = EFFECT_REPLAY_CACHE,
            extraBufferCapacity = MAX_EFFECT_BUFFER,
        )
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    private val _intents =
        MutableSharedFlow<I>(
            replay = 0,
            extraBufferCapacity = MAX_INTENT_BUFFER,
        )

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        scope.launch {
            _intents.collect { intent ->
                processIntent(intent)
            }
        }
    }

    /**
     * 发送Intent
     */
    fun sendIntent(intent: I) {
        _intents.tryEmit(intent)
    }

    /**
     * 处理Intent - 子类需要实现
     */
    protected abstract suspend fun processIntent(intent: I)

    /**
     * 更新状态
     */
    protected fun updateState(reducer: (S) -> S) {
        val currentState = _state.value
        val newState = reducer(currentState)
        _state.value = newState
    }

    /**
     * 发送Effect
     */
    protected fun sendEffect(effect: E) {
        _effects.tryEmit(effect)
    }

    /**
     * 获取当前状态
     */
    protected fun getCurrentState(): S = _state.value

    /**
     * 设置处理状态
     */
    protected fun setProcessing(processing: Boolean) {
        _isProcessing.value = processing
    }

    /**
     * 批量处理Intent
     */
    fun sendIntents(intents: List<I>) {
        intents.forEach { intent ->
            sendIntent(intent)
        }
    }

    /**
     * 清理资源
     */
    open fun clear() {
        // 子类可以重写此方法进行清理
    }
}

/**
 * MVI状态基类
 */
interface MVIState

/**
 * MVI意图基类
 */
interface MVIIntent

/**
 * MVI效果基类
 */
interface MVIEffect

/**
 * 通用MVI状态实现
 */
@Serializable
data class UnifyMVIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = getCurrentTimeMillis(),
) : MVIState

/**
 * 通用MVI意图实现
 */
sealed class UnifyMVIIntent : MVIIntent {
    object LoadData : UnifyMVIIntent()

    object RefreshData : UnifyMVIIntent()

    object ClearError : UnifyMVIIntent()

    data class UpdateData(val key: String, val value: String) : UnifyMVIIntent()

    data class DeleteData(val key: String) : UnifyMVIIntent()

    data class CustomIntent(val action: String, val payload: Map<String, String> = emptyMap()) : UnifyMVIIntent()
}

/**
 * 通用MVI效果实现
 */
sealed class UnifyMVIEffect : MVIEffect {
    data class ShowMessage(val message: String) : UnifyMVIEffect()

    data class NavigateTo(val destination: String) : UnifyMVIEffect()

    data class ShowError(val error: String) : UnifyMVIEffect()

    object ShowLoading : UnifyMVIEffect()

    object HideLoading : UnifyMVIEffect()

    data class CustomEffect(val type: String, val data: Map<String, String> = emptyMap()) : UnifyMVIEffect()
}

/**
 * 通用MVI ViewModel实现
 */
class UnifyMVIViewModelImpl(
    scope: CoroutineScope,
) : UnifyMVIViewModel<UnifyMVIState, UnifyMVIIntent, UnifyMVIEffect>(
        initialState = UnifyMVIState(),
        scope = scope,
    ) {
    override suspend fun processIntent(intent: UnifyMVIIntent) {
        setProcessing(true)

        try {
            when (intent) {
                is UnifyMVIIntent.LoadData -> handleLoadData()
                is UnifyMVIIntent.RefreshData -> handleRefreshData()
                is UnifyMVIIntent.ClearError -> handleClearError()
                is UnifyMVIIntent.UpdateData -> handleUpdateData(intent.key, intent.value)
                is UnifyMVIIntent.DeleteData -> handleDeleteData(intent.key)
                is UnifyMVIIntent.CustomIntent -> handleCustomIntent(intent.action, intent.payload)
            }
        } catch (e: Exception) {
            updateState { it.copy(error = e.message, isLoading = false) }
            sendEffect(UnifyMVIEffect.ShowError(e.message ?: "未知错误"))
        } finally {
            setProcessing(false)
        }
    }

    private suspend fun handleLoadData() {
        updateState { it.copy(isLoading = true, error = null) }
        sendEffect(UnifyMVIEffect.ShowLoading)

        // 模拟数据加载
        kotlinx.coroutines.delay(1000)

        val mockData =
            mapOf(
                "user_id" to "12345",
                "username" to "test_user",
                "email" to "test@example.com",
                "last_login" to getCurrentTimeMillis().toString(),
            )

        updateState {
            it.copy(
                isLoading = false,
                data = mockData,
                timestamp = getCurrentTimeMillis(),
            )
        }
        sendEffect(UnifyMVIEffect.HideLoading)
        sendEffect(UnifyMVIEffect.ShowMessage("数据加载完成"))
    }

    private suspend fun handleRefreshData() {
        updateState { it.copy(isLoading = true, error = null) }

        // 模拟刷新延迟
        kotlinx.coroutines.delay(500)

        val currentData = getCurrentState().data.toMutableMap()
        currentData["last_refresh"] = getCurrentTimeMillis().toString()

        updateState {
            it.copy(
                isLoading = false,
                data = currentData,
                timestamp = getCurrentTimeMillis(),
            )
        }
        sendEffect(UnifyMVIEffect.ShowMessage("数据刷新完成"))
    }

    private fun handleClearError() {
        updateState { it.copy(error = null) }
    }

    private fun handleUpdateData(
        key: String,
        value: String,
    ) {
        val currentData = getCurrentState().data.toMutableMap()
        currentData[key] = value

        updateState {
            it.copy(
                data = currentData,
                timestamp = getCurrentTimeMillis(),
            )
        }
        sendEffect(UnifyMVIEffect.ShowMessage("数据已更新: $key"))
    }

    private fun handleDeleteData(key: String) {
        val currentData = getCurrentState().data.toMutableMap()
        val removed = currentData.remove(key)

        if (removed != null) {
            updateState {
                it.copy(
                    data = currentData,
                    timestamp = getCurrentTimeMillis(),
                )
            }
            sendEffect(UnifyMVIEffect.ShowMessage("数据已删除: $key"))
        } else {
            sendEffect(UnifyMVIEffect.ShowError("要删除的数据不存在: $key"))
        }
    }

    private suspend fun handleCustomIntent(
        action: String,
        payload: Map<String, String>,
    ) {
        when (action) {
            "BATCH_UPDATE" -> {
                val currentData = getCurrentState().data.toMutableMap()
                currentData.putAll(payload)
                updateState {
                    it.copy(
                        data = currentData,
                        timestamp = getCurrentTimeMillis(),
                    )
                }
                sendEffect(UnifyMVIEffect.ShowMessage("批量更新完成"))
            }
            "CLEAR_ALL" -> {
                updateState {
                    it.copy(
                        data = emptyMap(),
                        timestamp = getCurrentTimeMillis(),
                    )
                }
                sendEffect(UnifyMVIEffect.ShowMessage("所有数据已清除"))
            }
            "SIMULATE_ERROR" -> {
                throw RuntimeException("模拟错误: ${payload["message"] ?: "测试错误"}")
            }
            else -> {
                sendEffect(UnifyMVIEffect.CustomEffect(action, payload))
            }
        }
    }
}

/**
 * MVI状态管理器
 */
class MVIStateManager<S : MVIState> {
    private val _stateHistory = mutableListOf<S>()
    private val maxHistorySize = 50

    /**
     * 保存状态到历史记录
     */
    fun saveState(state: S) {
        _stateHistory.add(state)
        if (_stateHistory.size > maxHistorySize) {
            _stateHistory.removeAt(0)
        }
    }

    /**
     * 获取状态历史
     */
    fun getStateHistory(): List<S> = _stateHistory.toList()

    /**
     * 获取上一个状态
     */
    fun getPreviousState(): S? {
        return if (_stateHistory.size >= 2) {
            _stateHistory[_stateHistory.size - 2]
        } else {
            null
        }
    }

    /**
     * 清除历史记录
     */
    fun clearHistory() {
        _stateHistory.clear()
    }

    /**
     * 获取历史记录大小
     */
    fun getHistorySize(): Int = _stateHistory.size
}

/**
 * MVI中间件接口
 */
interface MVIMiddleware<S : MVIState, I : MVIIntent, E : MVIEffect> {
    suspend fun process(
        intent: I,
        currentState: S,
        next: suspend (I) -> Unit,
    )
}

/**
 * 日志中间件实现
 */
class LoggingMiddleware<S : MVIState, I : MVIIntent, E : MVIEffect> : MVIMiddleware<S, I, E> {
    override suspend fun process(
        intent: I,
        currentState: S,
        next: suspend (I) -> Unit,
    ) {
        val startTime = getCurrentTimeMillis()
        println("MVI: 处理Intent: ${intent::class.simpleName}")

        try {
            next(intent)
            val duration = getCurrentTimeMillis() - startTime
            println("MVI: Intent处理完成，耗时: ${duration}ms")
        } catch (e: Exception) {
            val duration = getCurrentTimeMillis() - startTime
            println("MVI: Intent处理失败，耗时: ${duration}ms，错误: ${e.message}")
            throw e
        }
    }
}

/**
 * 性能监控中间件
 */
class PerformanceMiddleware<S : MVIState, I : MVIIntent, E : MVIEffect> : MVIMiddleware<S, I, E> {
    private val performanceMetrics = mutableMapOf<String, MutableList<Long>>()

    override suspend fun process(
        intent: I,
        currentState: S,
        next: suspend (I) -> Unit,
    ) {
        val intentName = intent::class.simpleName ?: "Unknown"
        val startTime = getCurrentTimeMillis()

        try {
            next(intent)
        } finally {
            val duration = getCurrentTimeMillis() - startTime
            recordMetric(intentName, duration)
        }
    }

    private fun recordMetric(
        intentName: String,
        duration: Long,
    ) {
        val metrics = performanceMetrics.getOrPut(intentName) { mutableListOf() }
        metrics.add(duration)

        // 保持最近100次记录
        if (metrics.size > 100) {
            metrics.removeAt(0)
        }
    }

    fun getAverageProcessingTime(intentName: String): Double? {
        val metrics = performanceMetrics[intentName]
        return if (metrics?.isNotEmpty() == true) {
            metrics.average()
        } else {
            null
        }
    }

    fun getAllMetrics(): Map<String, List<Long>> {
        return performanceMetrics.mapValues { it.value.toList() }
    }
}
