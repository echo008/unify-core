package com.unify.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.unify.ui.state.UnifyViewModel
import com.unify.ui.theme.UnifyTheme
import com.unify.data.UnifyDataFlow
import com.unify.data.DataState
import com.unify.network.UnifyNetworkService
import com.unify.database.UnifyDatabaseRepository

/**
 * 基础使用示例
 * 展示如何使用 Unify KMP 框架构建一个简单的用户列表应用
 */

// 1. 定义状态、意图和副作用
data class UserListState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : com.unify.ui.state.State

sealed class UserListIntent : com.unify.ui.state.Intent {
    override val type: String get() = this::class.simpleName ?: ""
    
    object LoadUsers : UserListIntent()
    object RefreshUsers : UserListIntent()
    data class DeleteUser(val userId: Long) : UserListIntent()
}

sealed class UserListEffect : com.unify.ui.state.Effect {
    override val type: String get() = this::class.simpleName ?: ""
    
    data class ShowMessage(val message: String) : UserListEffect()
    data class NavigateToDetail(val userId: Long) : UserListEffect()
}

// 2. 实现 Reducer
class UserListReducer(
    private val repository: UnifyDatabaseRepository
) : com.unify.ui.state.StateReducer<UserListState> {
    
    override fun reduce(state: UserListState, action: com.unify.ui.state.Action): UserListState {
        return when (action) {
            is UserListIntent.LoadUsers -> state.copy(isLoading = true, error = null)
            is UserListIntent.RefreshUsers -> state.copy(isLoading = true, error = null)
            is UserListIntent.DeleteUser -> state.copy(isLoading = true)
            else -> state
        }
    }
}

// 3. 实现 ViewModel
class UserListViewModel(
    private val repository: UnifyDatabaseRepository,
    private val networkService: UnifyNetworkService
) : UnifyViewModel<UserListState, UserListIntent, UserListEffect>() {
    
    private val userDataFlow = UnifyDataFlow<List<User>>(viewModelScope)
    
    init {
        // 监听数据流变化
        viewModelScope.launch {
            userDataFlow.data.collect { dataState ->
                when (dataState) {
                    is DataState.Loading -> {
                        // 状态已在 reducer 中处理
                    }
                    is DataState.Success -> {
                        updateState { it.copy(users = dataState.data, isLoading = false, error = null) }
                    }
                    is DataState.Error -> {
                        updateState { it.copy(isLoading = false, error = dataState.message) }
                        sendEffect(UserListEffect.ShowMessage("加载失败: ${dataState.message}"))
                    }
                }
            }
        }
    }
    
    override fun createInitialState() = UserListState()
    
    override fun createReducer() = UserListReducer(repository)
    
    override fun createMiddleware() = listOf(
        UserListMiddleware(repository, networkService)
    )
    
    private fun updateState(update: (UserListState) -> UserListState) {
        // 直接更新状态的辅助方法
        val currentState = stateFlow.value
        val newState = update(currentState)
        // 这里需要通过内部机制更新状态
    }
}

// 4. 实现中间件处理副作用
class UserListMiddleware(
    private val repository: UnifyDatabaseRepository,
    private val networkService: UnifyNetworkService
) : com.unify.ui.state.StateMiddleware<UserListState> {
    
    override suspend fun beforeReduce(
        state: UserListState, 
        action: com.unify.ui.state.Action
    ): com.unify.ui.state.Action {
        when (action) {
            is UserListIntent.LoadUsers -> {
                // 从数据库加载用户
                loadUsersFromDatabase()
            }
            is UserListIntent.RefreshUsers -> {
                // 从网络刷新用户数据
                refreshUsersFromNetwork()
            }
            is UserListIntent.DeleteUser -> {
                // 删除用户
                deleteUser(action.userId)
            }
        }
        return action
    }
    
    private suspend fun loadUsersFromDatabase() {
        try {
            val users = repository.getAllUsers()
            // 通过某种机制更新数据流
        } catch (e: Exception) {
            // 处理错误
        }
    }
    
    private suspend fun refreshUsersFromNetwork() {
        try {
            // 从网络获取最新用户数据
            val result = networkService.get<List<User>>("/api/users")
            when (result) {
                is com.unify.network.NetworkResult.Success -> {
                    // 保存到数据库并更新UI
                    result.data.forEach { user ->
                        repository.createUser(user.username, user.email, user.displayName)
                    }
                }
                is com.unify.network.NetworkResult.Error -> {
                    // 处理网络错误
                }
            }
        } catch (e: Exception) {
            // 处理异常
        }
    }
    
    private suspend fun deleteUser(userId: Long) {
        try {
            repository.deleteUser(userId)
            // 重新加载用户列表
            loadUsersFromDatabase()
        } catch (e: Exception) {
            // 处理删除错误
        }
    }
}

// 5. 实现 UI 组件
@Composable
fun UserListScreen(viewModel: UserListViewModel) {
    val state by viewModel.stateFlow.collectAsState()
    
    // 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is UserListEffect.ShowMessage -> {
                    // 显示消息（通常使用 SnackbarHost）
                    println(effect.message)
                }
                is UserListEffect.NavigateToDetail -> {
                    // 导航到详情页
                    println("Navigate to user ${effect.userId}")
                }
            }
        }
    }
    
    // 初始加载
    LaunchedEffect(Unit) {
        viewModel.sendIntent(UserListIntent.LoadUsers)
    }
    
    UnifyTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题和刷新按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "用户列表",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Button(
                    onClick = { viewModel.sendIntent(UserListIntent.RefreshUsers) },
                    enabled = !state.isLoading
                ) {
                    Text("刷新")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 内容区域
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                state.error != null -> {
                    ErrorMessage(
                        message = state.error,
                        onRetry = { viewModel.sendIntent(UserListIntent.LoadUsers) }
                    )
                }
                
                state.users.isEmpty() -> {
                    EmptyState(
                        onRefresh = { viewModel.sendIntent(UserListIntent.RefreshUsers) }
                    )
                }
                
                else -> {
                    UserList(
                        users = state.users,
                        onDeleteUser = { userId ->
                            viewModel.sendIntent(UserListIntent.DeleteUser(userId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserList(
    users: List<User>,
    onDeleteUser: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users.size) { index ->
            val user = users[index]
            UserItem(
                user = user,
                onDelete = { onDeleteUser(user.id) }
            )
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Text("删除")
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "出错了: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

@Composable
fun EmptyState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "暂无用户数据",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRefresh) {
            Text("刷新")
        }
    }
}

// 6. 数据模型
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true
)
