package com.unify.ui.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 统一ViewModel基类（基于增强MVI架构）
 * 基于文档第6章要求实现的状态管理与数据流架构
 */
abstract class UnifyViewModel<S : State, I : Intent, E : Effect> {
    private val job = SupervisorJob()
    protected val viewModelScope: CoroutineScope = CoroutineScope(job + Dispatchers.Default)
    
    // MVI状态管理器
    private val mviStateManager by lazy {
        EnhancedMVIStateManager(
            initialState = createInitialState(),
            reducer = createReducer(),
            middleware = createMiddleware()
        )
    }
    
    // 暴露状态和副作用流
    val stateFlow: StateFlow<S> = mviStateManager.stateFlow
    val effectFlow: SharedFlow<E> = mviStateManager.effectFlow
    
    /**
     * 创建初始状态
     */
    abstract fun createInitialState(): S
    
    /**
     * 创建状态归约器
     */
    abstract fun createReducer(): StateReducer<S, I>
    
    /**
     * 创建中间件列表
     */
    protected open fun createMiddleware(): List<Middleware<S, I, E>> {
        return listOf(
            LoggingMiddleware(),
            PerformanceMiddleware(),
            ErrorHandlingMiddleware { error -> createErrorEffect(error) }
        )
    }
    
    /**
     * 创建错误副作用
     */
    protected abstract fun createErrorEffect(error: Throwable): E
    
    /**
     * 处理意图
     */
    fun handleIntent(intent: I) {
        mviStateManager.sendIntent(intent)
    }
    
    /**
     * 发送副作用
     */
    protected fun sendEffect(effect: E) {
        mviStateManager.sendEffect(effect)
    }
    
    /**
     * 获取当前状态
     */
    protected fun getCurrentState(): S = stateFlow.value
    
    /**
     * 异步处理意图（用于复杂的业务逻辑）
     */
    protected fun launchWithErrorHandling(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                sendEffect(createErrorEffect(e))
            }
        }
    }
    
    /**
     * 时间旅行调试
     */
    fun timeTravel(index: Int) {
        mviStateManager.timeTravel(index)
    }
    
    /**
     * 获取状态历史
     */
    fun getStateHistory(): List<StateSnapshot<S>> {
        return mviStateManager.getStateHistory()
    }
    
    /**
     * 清理资源
     */
    open fun onCleared() {
        mviStateManager.dispose()
        viewModelScope.cancel()
    }
}

/**
 * 通用UI状态
 */
sealed class UiState : State {
    object Idle : UiState()
    object Loading : UiState()
    data class Success<T>(val data: T) : UiState()
    data class Error(val message: String, val exception: Throwable? = null) : UiState()
}

/**
 * 通用UI意图
 */
sealed class UiIntent : Intent {
    object LoadData : UiIntent()
    object Refresh : UiIntent()
    object Retry : UiIntent()
    data class Search(val query: String) : UiIntent()
    data class Navigate(val route: String) : UiIntent()
}

/**
 * 通用UI副作用
 */
sealed class UiEffect : Effect {
    data class ShowError(val message: String) : UiEffect()
    data class ShowToast(val message: String) : UiEffect()
    data class Navigate(val route: String) : UiEffect()
    data class ShowLoading(val show: Boolean) : UiEffect()
    object ScrollToTop : UiEffect()
}

/**
 * 示例：用户列表ViewModel
 */
class UserListViewModel : UnifyViewModel<UserListState, UserListIntent, UserListEffect>() {
    
    override fun createInitialState(): UserListState {
        return UserListState(
            users = emptyList(),
            isLoading = false,
            error = null,
            searchQuery = ""
        )
    }
    
    override fun createReducer(): StateReducer<UserListState, UserListIntent> {
        return UserListReducer()
    }
    
    override fun createErrorEffect(error: Throwable): UserListEffect {
        return UserListEffect.ShowError(error.message ?: "Unknown error")
    }
    
    // 业务逻辑方法
    fun loadUsers() {
        handleIntent(UserListIntent.LoadUsers)
    }
    
    fun searchUsers(query: String) {
        handleIntent(UserListIntent.SearchUsers(query))
    }
    
    fun refreshUsers() {
        handleIntent(UserListIntent.RefreshUsers)
    }
    
    fun selectUser(userId: String) {
        handleIntent(UserListIntent.SelectUser(userId))
    }
}

/**
 * 用户列表状态
 */
data class UserListState(
    val users: List<User>,
    val isLoading: Boolean,
    val error: String?,
    val searchQuery: String,
    val selectedUserId: String? = null
) : State

/**
 * 用户列表意图
 */
sealed class UserListIntent : Intent {
    object LoadUsers : UserListIntent()
    object RefreshUsers : UserListIntent()
    data class SearchUsers(val query: String) : UserListIntent()
    data class SelectUser(val userId: String) : UserListIntent()
}

/**
 * 用户列表副作用
 */
sealed class UserListEffect : Effect {
    data class ShowError(val message: String) : UserListEffect()
    data class ShowToast(val message: String) : UserListEffect()
    data class NavigateToUserDetail(val userId: String) : UserListEffect()
    object ScrollToTop : UserListEffect()
}

/**
 * 用户列表状态归约器
 */
class UserListReducer : StateReducer<UserListState, UserListIntent> {
    override fun reduce(currentState: UserListState, intent: UserListIntent): UserListState {
        return when (intent) {
            is UserListIntent.LoadUsers -> {
                currentState.copy(isLoading = true, error = null)
            }
            is UserListIntent.RefreshUsers -> {
                currentState.copy(isLoading = true, error = null)
            }
            is UserListIntent.SearchUsers -> {
                currentState.copy(searchQuery = intent.query)
            }
            is UserListIntent.SelectUser -> {
                currentState.copy(selectedUserId = intent.userId)
            }
        }
    }
}

/**
 * 用户数据类
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String? = null
)

/**
 * 用户列表中间件（业务逻辑处理）
 */
class UserListMiddleware(
    private val userRepository: UserRepository
) : Middleware<UserListState, UserListIntent, UserListEffect> {
    
    override fun postProcess(
        state: UserListState,
        intent: UserListIntent,
        newState: UserListState
    ): MiddlewareResult<UserListState, UserListEffect> {
        return when (intent) {
            is UserListIntent.LoadUsers -> {
                // 这里应该触发异步数据加载
                // 实际实现中会调用 userRepository.getUsers()
                val mockUsers = listOf(
                    User("1", "张三", "zhangsan@example.com"),
                    User("2", "李四", "lisi@example.com")
                )
                MiddlewareResult(
                    state = newState.copy(users = mockUsers, isLoading = false),
                    effects = listOf(UserListEffect.ShowToast("用户列表加载完成"))
                )
            }
            is UserListIntent.SelectUser -> {
                MiddlewareResult(
                    state = newState,
                    effects = listOf(UserListEffect.NavigateToUserDetail(intent.userId))
                )
            }
            else -> MiddlewareResult(newState)
        }
    }
}

/**
 * 用户仓库接口
 */
interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun searchUsers(query: String): List<User>
    suspend fun getUserById(id: String): User?
}
