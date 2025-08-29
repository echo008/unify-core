package com.unify.web.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.unify.data.UnifyDataFlow
import com.unify.data.DataState
import com.unify.database.UnifyDatabaseRepository
import com.unify.database.DatabaseProvider
import com.unify.network.UnifyNetworkServiceImpl
import com.unify.network.NetworkClientFactory
import com.unify.web.di.WebDI
import com.unify.web.ui.WebUser

/**
 * Web主ViewModel
 * 集成Unify KMP框架的状态管理
 */
class WebMainViewModel {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val webDI = WebDI
    
    private val _uiState = MutableStateFlow(WebMainUiState())
    val uiState: StateFlow<WebMainUiState> = _uiState.asStateFlow()
    
    // 用户数据流
    private val userDataFlow = UnifyDataFlow<List<WebUser>>(scope)
    val userData: StateFlow<DataState<List<WebUser>>> = userDataFlow.data
    
    fun loadUsers() {
        userDataFlow.fetch {
            // 模拟从数据库加载用户数据
            val users = webDI.databaseRepository.getAllUsers()
            users.map { user ->
                WebUser(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    displayName = user.displayName,
                    avatarUrl = user.avatarUrl
                )
            }
        }
    }
    
    fun refreshUsers() {
        userDataFlow.refresh {
            val users = webDI.databaseRepository.getAllUsers()
            users.map { user ->
                WebUser(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    displayName = user.displayName,
                    avatarUrl = user.avatarUrl
                )
            }
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
}

/**
 * Web主UI状态
 */
data class WebMainUiState(
    val isNetworkConnected: Boolean = true,
    val errorMessage: String? = null,
    val showError: Boolean = false
)
