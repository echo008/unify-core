package com.unify.web.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlinx.coroutines.launch
import com.unify.web.viewmodel.WebMainViewModel
import com.unify.data.DataState

/**
 * Web应用主组件
 * 使用Compose for Web实现的跨平台UI
 */
@Composable
fun WebApp() {
    val viewModel = remember { WebMainViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val userData by viewModel.userData.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }
    
    Style(WebAppStyles)
    
    Div(attrs = {
        classes("app-container")
    }) {
        // 标题栏
        Header(attrs = {
            classes("app-header")
        }) {
            H1 { Text("Unify KMP Demo") }
            Button(attrs = {
                classes("refresh-button")
                onClick { viewModel.refreshUsers() }
            }) {
                Text("刷新")
            }
        }
        
        // 主内容区
        Main(attrs = {
            classes("app-main")
        }) {
            // 网络状态指示器
            NetworkStatusSection(isConnected = uiState.isNetworkConnected)
            
            // 用户列表
            UserListSection(userData = userData, onRefresh = { viewModel.refreshUsers() })
            
            // 错误提示
            if (uiState.showError) {
                ErrorMessage(message = uiState.errorMessage ?: "未知错误")
            }
        }
    }
}

@Composable
fun NetworkStatusSection(isConnected: Boolean) {
    Div(attrs = {
        classes("network-status", if (isConnected) "connected" else "disconnected")
    }) {
        Span(attrs = {
            classes("status-icon")
        }) {
            Text(if (isConnected) "🟢" else "🔴")
        }
        Text(if (isConnected) "网络已连接" else "网络未连接")
    }
}

@Composable
fun UserListSection(userData: DataState<List<WebUser>>, onRefresh: () -> Unit) {
    Section(attrs = {
        classes("user-list-section")
    }) {
        H2 { Text("用户列表") }
        
        when (userData) {
            is DataState.Loading -> {
                Div(attrs = {
                    classes("loading-container")
                }) {
                    Text("加载中...")
                }
            }
            
            is DataState.Success -> {
                if (userData.data.isEmpty()) {
                    EmptyUserList(onRefresh = onRefresh)
                } else {
                    UserList(users = userData.data)
                }
            }
            
            is DataState.Error -> {
                ErrorState(message = userData.message, onRetry = onRefresh)
            }
        }
    }
}

@Composable
fun UserList(users: List<WebUser>) {
    Div(attrs = {
        classes("user-list")
    }) {
        users.forEach { user ->
            UserItem(user = user)
        }
    }
}

@Composable
fun UserItem(user: WebUser) {
    Div(attrs = {
        classes("user-item")
    }) {
        Div(attrs = {
            classes("user-info")
        }) {
            H3(attrs = {
                classes("user-name")
            }) {
                Text(user.displayName)
            }
            P(attrs = {
                classes("user-email")
            }) {
                Text(user.email)
            }
        }
    }
}

@Composable
fun EmptyUserList(onRefresh: () -> Unit) {
    Div(attrs = {
        classes("empty-state")
    }) {
        P { Text("暂无用户数据") }
        Button(attrs = {
            classes("retry-button")
            onClick { onRefresh() }
        }) {
            Text("刷新")
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Div(attrs = {
        classes("error-state")
    }) {
        P(attrs = {
            classes("error-message")
        }) {
            Text("加载失败: $message")
        }
        Button(attrs = {
            classes("retry-button")
            onClick { onRetry() }
        }) {
            Text("重试")
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Div(attrs = {
        classes("error-banner")
    }) {
        Text(message)
    }
}

/**
 * Web用户数据模型
 */
data class WebUser(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null
)

/**
 * Web应用样式
 */
object WebAppStyles : StyleSheet() {
    val appContainer by style {
        maxWidth(1200.px)
        margin(0.px, auto)
        padding(20.px)
        fontFamily("system-ui", "-apple-system", "sans-serif")
    }
    
    val appHeader by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        marginBottom(20.px)
        paddingBottom(20.px)
        borderBottom(1.px, LineStyle.Solid, Color("#e0e0e0"))
    }
    
    val refreshButton by style {
        padding(8.px, 16.px)
        backgroundColor(Color("#007bff"))
        color(Color.white)
        border(0.px)
        borderRadius(4.px)
        cursor("pointer")
        
        hover(self) style {
            backgroundColor(Color("#0056b3"))
        }
    }
    
    val appMain by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(20.px)
    }
    
    val networkStatus by style {
        padding(12.px, 16.px)
        borderRadius(8.px)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(8.px)
        
        className("connected") style {
            backgroundColor(Color("#d4edda"))
            color(Color("#155724"))
        }
        
        className("disconnected") style {
            backgroundColor(Color("#f8d7da"))
            color(Color("#721c24"))
        }
    }
    
    val userListSection by style {
        backgroundColor(Color.white)
        padding(20.px)
        borderRadius(8.px)
        boxShadow(0.px, 2.px, 4.px, Color("rgba(0,0,0,0.1)"))
    }
    
    val userList by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(12.px)
    }
    
    val userItem by style {
        padding(16.px)
        backgroundColor(Color("#f8f9fa"))
        borderRadius(6.px)
        border(1.px, LineStyle.Solid, Color("#e9ecef"))
    }
    
    val userName by style {
        margin(0.px)
        fontSize(18.px)
        fontWeight("600")
        color(Color("#212529"))
    }
    
    val userEmail by style {
        margin(4.px, 0.px, 0.px, 0.px)
        fontSize(14.px)
        color(Color("#6c757d"))
    }
    
    val emptyState by style {
        textAlign("center")
        padding(40.px)
        color(Color("#6c757d"))
    }
    
    val errorState by style {
        textAlign("center")
        padding(40.px)
    }
    
    val errorMessage by style {
        color(Color("#dc3545"))
        marginBottom(16.px)
    }
    
    val retryButton by style {
        padding(8.px, 16.px)
        backgroundColor(Color("#28a745"))
        color(Color.white)
        border(0.px)
        borderRadius(4.px)
        cursor("pointer")
        
        hover(self) style {
            backgroundColor(Color("#218838"))
        }
    }
    
    val loadingContainer by style {
        textAlign("center")
        padding(40.px)
        color(Color("#6c757d"))
    }
    
    val errorBanner by style {
        padding(12.px, 16.px)
        backgroundColor(Color("#f8d7da"))
        color(Color("#721c24"))
        borderRadius(4.px)
        border(1.px, LineStyle.Solid, Color("#f5c6cb"))
    }
}
