package com.unify.ui.components.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.security.*
import kotlinx.coroutines.launch

/**
 * 统一身份认证组件 - 基于Compose的跨平台UI
 */
@Composable
fun UnifyAuthenticationComponent(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }
    
    val authState by authManager.authState.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 认证状态头部
        AuthHeader(
            authState = authState,
            currentUser = currentUser
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 根据认证状态显示不同内容
        when (authState) {
            AuthState.UNAUTHENTICATED, AuthState.AUTHENTICATION_FAILED -> {
                UnauthenticatedContent(
                    authManager = authManager,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            AuthState.AUTHENTICATING -> {
                AuthenticatingContent()
            }
            AuthState.MFA_REQUIRED -> {
                MFAContent(authManager = authManager)
            }
            AuthState.AUTHENTICATED -> {
                AuthenticatedContent(
                    authManager = authManager,
                    user = currentUser
                )
            }
            AuthState.SESSION_EXPIRED -> {
                SessionExpiredContent(authManager = authManager)
            }
        }
    }
}

/**
 * 认证头部
 */
@Composable
private fun AuthHeader(
    authState: AuthState,
    currentUser: AuthUser?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "身份认证",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = currentUser?.displayName ?: "未登录",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        AuthStateChip(authState = authState)
    }
}

/**
 * 认证状态芯片
 */
@Composable
private fun AuthStateChip(
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (authState) {
        AuthState.UNAUTHENTICATED -> "未认证" to Color.Gray
        AuthState.AUTHENTICATING -> "认证中" to Color.Blue
        AuthState.AUTHENTICATED -> "已认证" to Color.Green
        AuthState.MFA_REQUIRED -> "需要MFA" to Color(0xFFFF9800)
        AuthState.AUTHENTICATION_FAILED -> "认证失败" to Color.Red
        AuthState.SESSION_EXPIRED -> "会话过期" to Color.Red
    }
    
    AssistChip(
        onClick = { },
        label = { Text(statusText, fontSize = 12.sp) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = statusColor.copy(alpha = 0.1f),
            labelColor = statusColor
        )
    )
}

/**
 * 未认证内容
 */
@Composable
private fun UnauthenticatedContent(
    authManager: UnifyAuthenticationManager,
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标签选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthTab.values().forEach { tab ->
                FilterChip(
                    onClick = { onTabSelected(tab) },
                    label = { Text(tab.displayName, fontSize = 12.sp) },
                    selected = selectedTab == tab
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            AuthTab.LOGIN -> LoginContent(authManager = authManager)
            AuthTab.OAUTH -> OAuthContent(authManager = authManager)
            AuthTab.BIOMETRIC -> BiometricContent(authManager = authManager)
        }
    }
}

/**
 * 登录内容
 */
@Composable
private fun LoginContent(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "用户名密码登录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading
            )
            
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = ""
                        
                        val result = authManager.loginWithCredentials(username, password)
                        when (result) {
                            is AuthResult.Success -> {
                                username = ""
                                password = ""
                            }
                            is AuthResult.Error -> {
                                errorMessage = result.message
                            }
                            is AuthResult.RequiresMFA -> {
                                // MFA将由状态变化处理
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("登录")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "测试账号: testuser/password123 或 admin/admin123",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * OAuth内容
 */
@Composable
private fun OAuthContent(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            OAuthProviderCard(
                provider = AuthProvider.OAUTH_GOOGLE,
                providerName = "Google",
                onLogin = {
                    coroutineScope.launch {
                        authManager.loginWithOAuth(AuthProvider.OAUTH_GOOGLE, "google_test_code")
                    }
                }
            )
        }
        
        item {
            OAuthProviderCard(
                provider = AuthProvider.OAUTH_GITHUB,
                providerName = "GitHub",
                onLogin = {
                    coroutineScope.launch {
                        authManager.loginWithOAuth(AuthProvider.OAUTH_GITHUB, "github_test_code")
                    }
                }
            )
        }
        
        item {
            OAuthProviderCard(
                provider = AuthProvider.OAUTH_MICROSOFT,
                providerName = "Microsoft",
                onLogin = {
                    coroutineScope.launch {
                        authManager.loginWithOAuth(AuthProvider.OAUTH_MICROSOFT, "microsoft_test_code")
                    }
                }
            )
        }
    }
}

/**
 * OAuth提供者卡片
 */
@Composable
private fun OAuthProviderCard(
    provider: AuthProvider,
    providerName: String,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "使用 $providerName 登录",
                fontSize = 14.sp
            )
            
            Button(onClick = onLogin) {
                Text("登录", fontSize = 12.sp)
            }
        }
    }
}

/**
 * 生物识别内容
 */
@Composable
private fun BiometricContent(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "生物识别登录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🔐",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "使用指纹、面部识别或其他生物识别方式登录",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        authManager.loginWithBiometric()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("启用生物识别")
            }
        }
    }
}

/**
 * 认证中内容
 */
@Composable
private fun AuthenticatingContent(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "正在认证...",
                fontSize = 16.sp
            )
        }
    }
}

/**
 * MFA内容
 */
@Composable
private fun MFAContent(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    var mfaCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "多因素认证",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "请输入多因素认证码",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = mfaCode,
                onValueChange = { mfaCode = it },
                label = { Text("认证码") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = ""
                        
                        val result = authManager.verifyMFA(mfaCode)
                        when (result) {
                            is AuthResult.Success -> {
                                mfaCode = ""
                            }
                            is AuthResult.Error -> {
                                errorMessage = result.message
                            }
                            else -> {}
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && mfaCode.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("验证")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "测试MFA码: 123456 或 654321",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 已认证内容
 */
@Composable
private fun AuthenticatedContent(
    authManager: UnifyAuthenticationManager,
    user: AuthUser?,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UserInfoCard(user = user)
        }
        
        item {
            AuthStatsCard(authManager = authManager)
        }
        
        item {
            UserActionsCard(
                authManager = authManager,
                onLogout = {
                    coroutineScope.launch {
                        authManager.logout()
                    }
                }
            )
        }
    }
}

/**
 * 用户信息卡片
 */
@Composable
private fun UserInfoCard(
    user: AuthUser?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "用户信息",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            user?.let {
                UserInfoRow("用户名", it.username)
                UserInfoRow("邮箱", it.email)
                UserInfoRow("显示名称", it.displayName)
                UserInfoRow("角色", it.roles.joinToString(", "))
            }
        }
    }
}

/**
 * 用户信息行
 */
@Composable
private fun UserInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

/**
 * 认证统计卡片
 */
@Composable
private fun AuthStatsCard(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    val stats = authManager.getAuthStats()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "认证统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            UserInfoRow("认证状态", if (stats.isAuthenticated) "已认证" else "未认证")
            UserInfoRow("认证提供者", stats.authProvider?.name ?: "无")
            UserInfoRow("会话时长", formatDuration(stats.sessionDuration))
        }
    }
}

/**
 * 用户操作卡片
 */
@Composable
private fun UserActionsCard(
    authManager: UnifyAuthenticationManager,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "用户操作",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* 刷新令牌 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("刷新令牌", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("登出", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 会话过期内容
 */
@Composable
private fun SessionExpiredContent(
    authManager: UnifyAuthenticationManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏰",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "会话已过期",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "请重新登录以继续使用",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        authManager.logout()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重新登录")
            }
        }
    }
}

/**
 * 认证标签枚举
 */
enum class AuthTab(val displayName: String) {
    LOGIN("登录"),
    OAUTH("OAuth"),
    BIOMETRIC("生物识别")
}

/**
 * 格式化持续时间
 */
private fun formatDuration(duration: Long?): String {
    if (duration == null) return "无"
    
    val seconds = duration / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}小时${minutes % 60}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "${seconds}秒"
    }
}
