package com.unify.sample

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.unify.ui.state.*
import com.unify.ui.components.*
import com.unify.navigation.*
import com.unify.database.*
import com.unify.di.*
import org.koin.compose.koinInject

/**
 * Unify KMP 示例应用
 * 展示框架的核心功能和最佳实践
 */

/**
 * 示例应用状态
 */
data class SampleAppState(
    val users: List<UserEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedUser: UserEntity? = null
) : State

/**
 * 示例应用意图
 */
sealed class SampleAppIntent : Intent {
    object LoadUsers : SampleAppIntent()
    data class SelectUser(val user: UserEntity) : SampleAppIntent()
    data class CreateUser(val username: String, val email: String, val displayName: String) : SampleAppIntent()
    data class UpdateUser(val user: UserEntity) : SampleAppIntent()
    data class DeleteUser(val userId: Long) : SampleAppIntent()
    object ClearError : SampleAppIntent()
}

/**
 * 示例应用副作用
 */
sealed class SampleAppEffect : Effect {
    data class ShowMessage(val message: String) : SampleAppEffect()
    data class NavigateToDetail(val userId: Long) : SampleAppEffect()
    object NavigateBack : SampleAppEffect()
}

/**
 * 示例应用ViewModel
 */
class SampleAppViewModel(
    private val userService: UserService = koinInject(),
    private val database: UnifyDatabase = koinInject()
) : UnifyViewModel<SampleAppState, SampleAppIntent, SampleAppEffect>() {
    
    override fun createInitialState(): SampleAppState = SampleAppState()
    
    override fun createReducer(): StateReducer<SampleAppState, SampleAppIntent> = { state, intent ->
        when (intent) {
            is SampleAppIntent.LoadUsers -> state.copy(isLoading = true, error = null)
            is SampleAppIntent.SelectUser -> state.copy(selectedUser = intent.user)
            is SampleAppIntent.CreateUser -> state.copy(isLoading = true, error = null)
            is SampleAppIntent.UpdateUser -> state.copy(isLoading = true, error = null)
            is SampleAppIntent.DeleteUser -> state.copy(isLoading = true, error = null)
            is SampleAppIntent.ClearError -> state.copy(error = null)
        }
    }
    
    override fun createMiddleware(): List<Middleware<SampleAppState, SampleAppIntent, SampleAppEffect>> {
        return listOf(
            createAsyncMiddleware { state, intent ->
                when (intent) {
                    is SampleAppIntent.LoadUsers -> {
                        try {
                            val users = database.userQueries.selectAllUsers().executeAsList()
                                .map { user ->
                                    UserEntity(
                                        id = user.id,
                                        username = user.username,
                                        email = user.email,
                                        displayName = user.displayName,
                                        avatarUrl = user.avatarUrl,
                                        createdAt = user.createdAt,
                                        updatedAt = user.updatedAt,
                                        isActive = user.isActive == 1L
                                    )
                                }
                            
                            updateState { it.copy(users = users, isLoading = false) }
                        } catch (e: Exception) {
                            updateState { it.copy(isLoading = false, error = "加载用户失败: ${e.message}") }
                        }
                    }
                    
                    is SampleAppIntent.CreateUser -> {
                        try {
                            val user = UserEntity(
                                id = 0, // 自动生成
                                username = intent.username,
                                email = intent.email,
                                displayName = intent.displayName,
                                avatarUrl = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                isActive = true
                            )
                            
                            database.insertUserEntity(user)
                            emitEffect(SampleAppEffect.ShowMessage("用户创建成功"))
                            handleIntent(SampleAppIntent.LoadUsers)
                        } catch (e: Exception) {
                            updateState { it.copy(isLoading = false, error = "创建用户失败: ${e.message}") }
                        }
                    }
                    
                    is SampleAppIntent.UpdateUser -> {
                        try {
                            database.userQueries.updateUser(
                                email = intent.user.email,
                                displayName = intent.user.displayName,
                                avatarUrl = intent.user.avatarUrl,
                                updatedAt = System.currentTimeMillis(),
                                id = intent.user.id
                            )
                            
                            emitEffect(SampleAppEffect.ShowMessage("用户更新成功"))
                            handleIntent(SampleAppIntent.LoadUsers)
                        } catch (e: Exception) {
                            updateState { it.copy(isLoading = false, error = "更新用户失败: ${e.message}") }
                        }
                    }
                    
                    is SampleAppIntent.DeleteUser -> {
                        try {
                            database.userQueries.deleteUser(
                                updatedAt = System.currentTimeMillis(),
                                id = intent.userId
                            )
                            
                            emitEffect(SampleAppEffect.ShowMessage("用户删除成功"))
                            handleIntent(SampleAppIntent.LoadUsers)
                        } catch (e: Exception) {
                            updateState { it.copy(isLoading = false, error = "删除用户失败: ${e.message}") }
                        }
                    }
                    
                    is SampleAppIntent.SelectUser -> {
                        emitEffect(SampleAppEffect.NavigateToDetail(intent.user.id))
                    }
                    
                    else -> {}
                }
            }
        )
    }
}

/**
 * 示例应用主界面
 */
@Composable
fun SampleAppScreen(
    viewModel: SampleAppViewModel = koinInject(),
    navigator: UnifyNavigator = koinInject()
) {
    val state by viewModel.stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 处理副作用
    LaunchedEffect(viewModel) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is SampleAppEffect.ShowMessage -> {
                    // 显示消息（这里简化处理）
                    println("Message: ${effect.message}")
                }
                is SampleAppEffect.NavigateToDetail -> {
                    navigator.navigateTo(
                        UnifyRoute(
                            path = "/user/${effect.userId}",
                            name = "UserDetail"
                        )
                    )
                }
                is SampleAppEffect.NavigateBack -> {
                    navigator.navigateBack()
                }
            }
        }
    }
    
    // 初始化加载
    LaunchedEffect(Unit) {
        viewModel.handleIntent(SampleAppIntent.LoadUsers)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Unify KMP 示例应用",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 创建用户按钮
        Button(
            onClick = {
                scope.launch {
                    viewModel.handleIntent(
                        SampleAppIntent.CreateUser(
                            username = "user${System.currentTimeMillis()}",
                            email = "user${System.currentTimeMillis()}@example.com",
                            displayName = "示例用户 ${System.currentTimeMillis()}"
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("创建示例用户")
        }
        
        // 错误显示
        state.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.handleIntent(SampleAppIntent.ClearError) }
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
        
        // 加载指示器
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // 用户列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.users) { user ->
                UserCard(
                    user = user,
                    onUserClick = { viewModel.handleIntent(SampleAppIntent.SelectUser(user)) },
                    onDeleteClick = { viewModel.handleIntent(SampleAppIntent.DeleteUser(user.id)) }
                )
            }
        }
    }
}

/**
 * 用户卡片组件
 */
@Composable
fun UserCard(
    user: UserEntity,
    onUserClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onUserClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onDeleteClick
            ) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * 用户详情界面
 */
@Composable
fun UserDetailScreen(
    userId: Long,
    viewModel: SampleAppViewModel = koinInject(),
    navigator: UnifyNavigator = koinInject()
) {
    val state by viewModel.stateFlow.collectAsState()
    val user = state.users.find { it.id == userId }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 返回按钮
        Button(
            onClick = { navigator.navigateBack() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("返回")
        }
        
        if (user != null) {
            // 用户信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "用户详情",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    DetailRow("用户名", user.username)
                    DetailRow("邮箱", user.email)
                    DetailRow("显示名称", user.displayName)
                    DetailRow("用户ID", user.id.toString())
                    DetailRow("创建时间", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(java.util.Date(user.createdAt)))
                    DetailRow("更新时间", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(java.util.Date(user.updatedAt)))
                    DetailRow("状态", if (user.isActive) "活跃" else "非活跃")
                }
            }
        } else {
            Text("用户不存在")
        }
    }
}

/**
 * 详情行组件
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 示例应用入口
 */
@Composable
fun UnifySampleApp() {
    MaterialTheme {
        val navigator = koinInject<UnifyNavigator>()
        val navigationState by navigator.navigationState.collectAsState()
        
        when (navigationState.currentRoute?.name) {
            "UserDetail" -> {
                val userId = navigationState.currentRoute?.parameters?.get("userId")?.toLongOrNull() ?: 0L
                UserDetailScreen(userId = userId)
            }
            else -> {
                SampleAppScreen()
            }
        }
    }
}

/**
 * 示例应用初始化
 */
object SampleAppInitializer {
    fun initialize() {
        // 初始化Koin依赖注入
        org.koin.core.context.startKoin {
            modules(allModules)
        }
        
        // 初始化数据库
        val databaseFactory = org.koin.core.context.GlobalContext.get().get<UnifyDatabaseDriverFactory>()
        UnifyDatabaseFactory.createDatabase(databaseFactory)
        
        // 初始化错误处理
        com.unify.error.UnifyErrorHandler.initialize(
            database = org.koin.core.context.GlobalContext.get().get(),
            platformManager = org.koin.core.context.GlobalContext.get().get()
        )
        
        println("Unify KMP Sample App initialized successfully!")
    }
}
