package com.unify.ui.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel {
    private val job = SupervisorJob()
    protected val viewModelScope: CoroutineScope = CoroutineScope(job + Dispatchers.Default)

    protected val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    protected val _uiEffect = MutableSharedFlow<UiEffect>()
    val uiEffect: SharedFlow<UiEffect> = _uiEffect.asSharedFlow()

    abstract fun createInitialState(): UiState

    protected fun updateState(reducer: UiState.() -> UiState) {
        _uiState.value = _uiState.value.reducer()
    }

    protected fun sendEffect(effect: UiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }

    abstract fun handleIntent(intent: UiIntent)

    protected fun handleError(error: Throwable) {
        viewModelScope.launch {
            val errorEffect = when (error) {
                is NetworkException -> UiEffect.ShowError("网络连接失败")
                is ValidationException -> UiEffect.ShowError(error.message ?: "数据验证失败")
                else -> UiEffect.ShowError("未知错误")
            }
            _uiEffect.emit(errorEffect)
        }
    }

    protected fun launchWithErrorHandling(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    open fun onCleared() {
        viewModelScope.cancel()
    }
}

interface UiState
interface UiIntent
interface UiEffect {
    data class ShowError(val message: String) : UiEffect
    data class ShowToast(val message: String) : UiEffect
    data class Navigate(val route: String) : UiEffect
    object Loading : UiEffect
    object Idle : UiEffect
}

sealed class UnifyException(message: String) : Exception(message)
class NetworkException(message: String) : UnifyException(message)
class ValidationException(message: String) : UnifyException(message)
class PlatformException(message: String) : UnifyException(message)
