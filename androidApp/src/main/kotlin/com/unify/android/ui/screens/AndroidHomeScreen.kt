package com.unify.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unify.android.ui.AndroidMainViewModel
import com.unify.android.ui.AndroidMainViewModelFactory
import com.unify.android.ui.User
import com.unify.data.DataState

/**
 * Android主屏幕
 * 展示Unify KMP框架功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidHomeScreen(
    viewModel: AndroidMainViewModel = viewModel(factory = AndroidMainViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val userData by viewModel.userData.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部应用栏
        TopAppBar(
            title = { Text("Unify KMP Demo") },
            actions = {
                IconButton(onClick = { viewModel.refreshUsers() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 网络状态指示器
        NetworkStatusCard(isConnected = uiState.isNetworkConnected)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 用户列表
        UserListSection(
            userData = userData,
            onRefresh = { viewModel.refreshUsers() }
        )
        
        // 错误提示
        if (uiState.showError) {
            LaunchedEffect(uiState.errorMessage) {
                // 显示Snackbar或其他错误提示
            }
        }
    }
}

@Composable
fun NetworkStatusCard(isConnected: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isConnected) "网络已连接" else "网络未连接",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConnected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun UserListSection(
    userData: DataState<List<User>>,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "用户列表",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            when (userData) {
                is DataState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is DataState.Success -> {
                    if (userData.data.isEmpty()) {
                        EmptyUserList(onRefresh = onRefresh)
                    } else {
                        LazyColumn {
                            items(userData.data) { user ->
                                UserItem(user = user)
                            }
                        }
                    }
                }
                
                is DataState.Error -> {
                    ErrorState(
                        message = userData.message,
                        onRetry = onRefresh
                    )
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
        }
    }
}

@Composable
fun EmptyUserList(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "加载失败: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
