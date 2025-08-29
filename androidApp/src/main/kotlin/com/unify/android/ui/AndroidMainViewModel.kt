package com.unify.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unify.android.di.AndroidDI
import com.unify.ui.state.UnifyViewModel
import com.unify.data.UnifyDataFlow
import com.unify.data.DataState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Android主ViewModel
 * 集成Unify KMP框架的状态管理
 */
class AndroidMainViewModel : ViewModel() {
    
    private val module = AndroidDI.module
    
    private val _uiState = MutableStateFlow(AndroidMainUiState())
    val uiState: StateFlow<AndroidMainUiState> = _uiState.asStateFlow()
    
    // 用户数据流
    private val userDataFlow = UnifyDataFlow<List<User>>(viewModelScope)
    val userData: StateFlow<DataState<List<User>>> = userDataFlow.data
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        loadUsers()
    }
    
    fun loadUsers() {
        userDataFlow.fetch {
            module.databaseRepository.getAllUsers()
        }
    }
    
    fun refreshUsers() {
        userDataFlow.refresh {
            module.databaseRepository.getAllUsers()
        }
    }
    
    fun updateConnectionStatus(isConnected: Boolean) {
        _uiState.value = _uiState.value.copy(isNetworkConnected = isConnected)
    }
    
    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            showError = true
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showError = false
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        userDataFlow.clear()
    }
}

/**
 * Android主UI状态
 */
data class AndroidMainUiState(
    val isNetworkConnected: Boolean = true,
    val errorMessage: String? = null,
    val showError: Boolean = false
)

/**
 * 用户数据类（临时定义，应该从共享模块导入）
 */
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null
)

/**
 * ViewModel工厂
 */
class AndroidMainViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AndroidMainViewModel::class.java)) {
            return AndroidMainViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
