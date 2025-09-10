package com.unify.core.security

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 统一身份认证管理器
 * 提供跨平台的身份认证、会话管理、多因素认证和SSO支持
 */
class UnifyAuthenticationManager(
    private val config: AuthConfig = AuthConfig()
) {
    private val _authState = MutableStateFlow(AuthState.UNAUTHENTICATED)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()
    
    private val _sessionInfo = MutableStateFlow<SessionInfo?>(null)
    val sessionInfo: StateFlow<SessionInfo?> = _sessionInfo.asStateFlow()
    
    // 认证提供者
    private val authProviders = mutableMapOf<AuthProvider, AuthProviderAdapter>()
    private val tokenStorage = TokenStorage()
    private val sessionManager = SessionManager(config)
    
    // 多因素认证
    private val mfaManager = MFAManager()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        // 初始化认证提供者
        initializeAuthProviders()
        
        // 启动会话监控
        startSessionMonitoring()
        
        // 尝试恢复会话
        coroutineScope.launch {
            restoreSession()
        }
    }
    
    /**
     * 用户名密码登录
     */
    suspend fun loginWithCredentials(
        username: String,
        password: String,
        provider: AuthProvider = AuthProvider.LOCAL
    ): AuthResult {
        return try {
            _authState.value = AuthState.AUTHENTICATING
            
            val authProvider = authProviders[provider]
                ?: return AuthResult.Error("不支持的认证提供者: $provider")
            
            val result = authProvider.authenticate(
                AuthCredentials.UsernamePassword(username, password)
            )
            
            when (result) {
                is AuthProviderResult.Success -> {
                    handleSuccessfulAuth(result.user, result.tokens, provider)
                }
                is AuthProviderResult.RequiresMFA -> {
                    _authState.value = AuthState.MFA_REQUIRED
                    AuthResult.RequiresMFA(result.mfaChallenge)
                }
                is AuthProviderResult.Error -> {
                    _authState.value = AuthState.AUTHENTICATION_FAILED
                    AuthResult.Error(result.message)
                }
            }
        } catch (e: Exception) {
            _authState.value = AuthState.AUTHENTICATION_FAILED
            AuthResult.Error("认证失败: ${e.message}")
        }
    }
    
    /**
     * OAuth登录
     */
    suspend fun loginWithOAuth(
        provider: AuthProvider,
        authorizationCode: String
    ): AuthResult {
        return try {
            _authState.value = AuthState.AUTHENTICATING
            
            val authProvider = authProviders[provider]
                ?: return AuthResult.Error("不支持的OAuth提供者: $provider")
            
            val result = authProvider.authenticate(
                AuthCredentials.OAuth(authorizationCode)
            )
            
            when (result) {
                is AuthProviderResult.Success -> {
                    handleSuccessfulAuth(result.user, result.tokens, provider)
                }
                is AuthProviderResult.Error -> {
                    _authState.value = AuthState.AUTHENTICATION_FAILED
                    AuthResult.Error(result.message)
                }
                else -> {
                    _authState.value = AuthState.AUTHENTICATION_FAILED
                    AuthResult.Error("OAuth认证失败")
                }
            }
        } catch (e: Exception) {
            _authState.value = AuthState.AUTHENTICATION_FAILED
            AuthResult.Error("OAuth认证失败: ${e.message}")
        }
    }
    
    /**
     * 生物识别登录
     */
    suspend fun loginWithBiometric(): AuthResult {
        return try {
            _authState.value = AuthState.AUTHENTICATING
            
            val biometricProvider = authProviders[AuthProvider.BIOMETRIC]
                ?: return AuthResult.Error("生物识别不可用")
            
            val result = biometricProvider.authenticate(AuthCredentials.Biometric)
            
            when (result) {
                is AuthProviderResult.Success -> {
                    handleSuccessfulAuth(result.user, result.tokens, AuthProvider.BIOMETRIC)
                }
                is AuthProviderResult.Error -> {
                    _authState.value = AuthState.AUTHENTICATION_FAILED
                    AuthResult.Error(result.message)
                }
                else -> {
                    _authState.value = AuthState.AUTHENTICATION_FAILED
                    AuthResult.Error("生物识别认证失败")
                }
            }
        } catch (e: Exception) {
            _authState.value = AuthState.AUTHENTICATION_FAILED
            AuthResult.Error("生物识别认证失败: ${e.message}")
        }
    }
    
    /**
     * 多因素认证验证
     */
    suspend fun verifyMFA(
        mfaCode: String,
        mfaType: MFAType = MFAType.TOTP
    ): AuthResult {
        return try {
            if (_authState.value != AuthState.MFA_REQUIRED) {
                return AuthResult.Error("当前状态不需要MFA验证")
            }
            
            val isValid = mfaManager.verifyCode(mfaCode, mfaType)
            
            if (isValid) {
                // MFA验证成功，完成认证流程
                val pendingUser = mfaManager.getPendingUser()
                val pendingTokens = mfaManager.getPendingTokens()
                
                if (pendingUser != null && pendingTokens != null) {
                    handleSuccessfulAuth(pendingUser, pendingTokens, AuthProvider.LOCAL)
                } else {
                    AuthResult.Error("MFA验证状态异常")
                }
            } else {
                AuthResult.Error("MFA验证码无效")
            }
        } catch (e: Exception) {
            AuthResult.Error("MFA验证失败: ${e.message}")
        }
    }
    
    /**
     * 刷新令牌
     */
    suspend fun refreshToken(): AuthResult {
        return try {
            val currentSession = _sessionInfo.value
                ?: return AuthResult.Error("无有效会话")
            
            val refreshToken = tokenStorage.getRefreshToken()
                ?: return AuthResult.Error("无刷新令牌")
            
            val provider = authProviders[currentSession.authProvider]
                ?: return AuthResult.Error("认证提供者不可用")
            
            val result = provider.refreshToken(refreshToken)
            
            when (result) {
                is TokenRefreshResult.Success -> {
                    tokenStorage.storeTokens(result.tokens)
                    updateSession(currentSession.copy(
                        accessToken = result.tokens.accessToken,
                        expiresAt = com.unify.core.platform.getCurrentTimeMillis() + result.tokens.expiresIn * 1000
                    ))
                    AuthResult.Success("令牌刷新成功")
                }
                is TokenRefreshResult.Error -> {
                    logout()
                    AuthResult.Error("令牌刷新失败: ${result.message}")
                }
            }
        } catch (e: Exception) {
            logout()
            AuthResult.Error("令牌刷新失败: ${e.message}")
        }
    }
    
    /**
     * 登出
     */
    suspend fun logout(): AuthResult {
        return try {
            val currentSession = _sessionInfo.value
            
            // 通知服务器登出
            currentSession?.let { session ->
                val provider = authProviders[session.authProvider]
                provider?.logout(session.accessToken)
            }
            
            // 清理本地状态
            clearAuthState()
            
            AuthResult.Success("登出成功")
        } catch (e: Exception) {
            // 即使出错也要清理本地状态
            clearAuthState()
            AuthResult.Success("登出完成")
        }
    }
    
    /**
     * 检查认证状态
     */
    fun isAuthenticated(): Boolean {
        return _authState.value == AuthState.AUTHENTICATED
    }
    
    /**
     * 检查权限
     */
    fun hasPermission(permission: String): Boolean {
        val user = _currentUser.value ?: return false
        return user.permissions.contains(permission) || user.roles.any { role ->
            getRolePermissions(role).contains(permission)
        }
    }
    
    /**
     * 检查角色
     */
    fun hasRole(role: String): Boolean {
        val user = _currentUser.value ?: return false
        return user.roles.contains(role)
    }
    
    /**
     * 获取访问令牌
     */
    fun getAccessToken(): String? {
        return _sessionInfo.value?.accessToken
    }
    
    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(): AuthResult {
        return try {
            val accessToken = getAccessToken()
                ?: return AuthResult.Error("无访问令牌")
            
            val currentSession = _sessionInfo.value
                ?: return AuthResult.Error("无有效会话")
            
            val provider = authProviders[currentSession.authProvider]
                ?: return AuthResult.Error("认证提供者不可用")
            
            val userInfo = provider.getUserInfo(accessToken)
            
            when (userInfo) {
                is UserInfoResult.Success -> {
                    _currentUser.value = userInfo.user
                    AuthResult.Success("用户信息获取成功")
                }
                is UserInfoResult.Error -> {
                    AuthResult.Error("获取用户信息失败: ${userInfo.message}")
                }
            }
        } catch (e: Exception) {
            AuthResult.Error("获取用户信息失败: ${e.message}")
        }
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateUserInfo(userInfo: Map<String, Any>): AuthResult {
        return try {
            val accessToken = getAccessToken()
                ?: return AuthResult.Error("无访问令牌")
            
            val currentSession = _sessionInfo.value
                ?: return AuthResult.Error("无有效会话")
            
            val provider = authProviders[currentSession.authProvider]
                ?: return AuthResult.Error("认证提供者不可用")
            
            val result = provider.updateUserInfo(accessToken, userInfo)
            
            when (result) {
                is UpdateUserResult.Success -> {
                    _currentUser.value = result.user
                    AuthResult.Success("用户信息更新成功")
                }
                is UpdateUserResult.Error -> {
                    AuthResult.Error("更新用户信息失败: ${result.message}")
                }
            }
        } catch (e: Exception) {
            AuthResult.Error("更新用户信息失败: ${e.message}")
        }
    }
    
    /**
     * 修改密码
     */
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): AuthResult {
        return try {
            val accessToken = getAccessToken()
                ?: return AuthResult.Error("无访问令牌")
            
            val currentSession = _sessionInfo.value
                ?: return AuthResult.Error("无有效会话")
            
            val provider = authProviders[currentSession.authProvider]
                ?: return AuthResult.Error("认证提供者不可用")
            
            val result = provider.changePassword(accessToken, oldPassword, newPassword)
            
            when (result) {
                is PasswordChangeResult.Success -> {
                    AuthResult.Success("密码修改成功")
                }
                is PasswordChangeResult.Error -> {
                    AuthResult.Error("密码修改失败: ${result.message}")
                }
            }
        } catch (e: Exception) {
            AuthResult.Error("密码修改失败: ${e.message}")
        }
    }
    
    /**
     * 获取认证统计信息
     */
    fun getAuthStats(): AuthStats {
        val session = _sessionInfo.value
        return AuthStats(
            isAuthenticated = isAuthenticated(),
            authProvider = session?.authProvider,
            loginTime = session?.loginTime,
            expiresAt = session?.expiresAt,
            sessionDuration = session?.let { 
                com.unify.core.platform.getCurrentTimeMillis() - it.loginTime 
            },
            userRoles = _currentUser.value?.roles ?: emptyList(),
            userPermissions = _currentUser.value?.permissions ?: emptyList()
        )
    }
    
    /**
     * 处理认证成功
     */
    private suspend fun handleSuccessfulAuth(
        user: AuthUser,
        tokens: AuthTokens,
        provider: AuthProvider
    ): AuthResult {
        // 存储令牌
        tokenStorage.storeTokens(tokens)
        
        // 创建会话
        val session = SessionInfo(
            sessionId = generateSessionId(),
            userId = user.id,
            authProvider = provider,
            accessToken = tokens.accessToken,
            loginTime = com.unify.core.platform.getCurrentTimeMillis(),
            expiresAt = com.unify.core.platform.getCurrentTimeMillis() + tokens.expiresIn * 1000,
            lastActivity = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        // 更新状态
        _currentUser.value = user
        _sessionInfo.value = session
        _authState.value = AuthState.AUTHENTICATED
        
        // 启动会话管理
        sessionManager.startSession(session)
        
        return AuthResult.Success("认证成功")
    }
    
    /**
     * 清理认证状态
     */
    private fun clearAuthState() {
        _currentUser.value = null
        _sessionInfo.value = null
        _authState.value = AuthState.UNAUTHENTICATED
        tokenStorage.clearTokens()
        sessionManager.endSession()
        mfaManager.clearPendingAuth()
    }
    
    /**
     * 恢复会话
     */
    private suspend fun restoreSession() {
        try {
            val storedTokens = tokenStorage.getStoredTokens() ?: return
            
            // 检查令牌是否过期
            if (storedTokens.isExpired()) {
                tokenStorage.clearTokens()
                return
            }
            
            // 尝试获取用户信息验证令牌有效性
            val provider = authProviders[AuthProvider.LOCAL] // 默认使用本地提供者
            if (provider != null) {
                val userInfo = provider.getUserInfo(storedTokens.accessToken)
                if (userInfo is UserInfoResult.Success) {
                    val session = SessionInfo(
                        sessionId = generateSessionId(),
                        userId = userInfo.user.id,
                        authProvider = AuthProvider.LOCAL,
                        accessToken = storedTokens.accessToken,
                        loginTime = com.unify.core.platform.getCurrentTimeMillis(),
                        expiresAt = storedTokens.expiresAt,
                        lastActivity = com.unify.core.platform.getCurrentTimeMillis()
                    )
                    
                    _currentUser.value = userInfo.user
                    _sessionInfo.value = session
                    _authState.value = AuthState.AUTHENTICATED
                    
                    sessionManager.startSession(session)
                }
            }
        } catch (e: Exception) {
            println("恢复会话失败: ${e.message}")
            tokenStorage.clearTokens()
        }
    }
    
    /**
     * 初始化认证提供者
     */
    private fun initializeAuthProviders() {
        // 注册各种认证提供者
        authProviders[AuthProvider.LOCAL] = LocalAuthProvider()
        authProviders[AuthProvider.OAUTH_GOOGLE] = GoogleOAuthProvider()
        authProviders[AuthProvider.OAUTH_GITHUB] = GitHubOAuthProvider()
        authProviders[AuthProvider.OAUTH_MICROSOFT] = MicrosoftOAuthProvider()
        authProviders[AuthProvider.BIOMETRIC] = BiometricAuthProvider()
        authProviders[AuthProvider.SSO] = SSOAuthProvider()
    }
    
    /**
     * 启动会话监控
     */
    private fun startSessionMonitoring() {
        coroutineScope.launch {
            while (isActive) {
                try {
                    val session = _sessionInfo.value
                    if (session != null && isAuthenticated()) {
                        // 检查会话是否过期
                        if (com.unify.core.platform.getCurrentTimeMillis() > session.expiresAt) {
                            // 尝试刷新令牌
                            val refreshResult = refreshToken()
                            if (refreshResult is AuthResult.Error) {
                                logout()
                            }
                        }
                        
                        // 更新最后活动时间
                        updateSession(session.copy(
                            lastActivity = com.unify.core.platform.getCurrentTimeMillis()
                        ))
                    }
                    
                    delay(config.sessionCheckInterval.inWholeMilliseconds)
                } catch (e: Exception) {
                    println("会话监控错误: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 更新会话信息
     */
    private fun updateSession(session: SessionInfo) {
        _sessionInfo.value = session
        sessionManager.updateSession(session)
    }
    
    /**
     * 获取角色权限
     */
    private fun getRolePermissions(role: String): Set<String> {
        // 这里应该从配置或服务器获取角色权限映射
        return when (role) {
            "admin" -> setOf("read", "write", "delete", "manage_users", "manage_system")
            "user" -> setOf("read", "write")
            "guest" -> setOf("read")
            else -> emptySet()
        }
    }
    
    /**
     * 生成会话ID
     */
    private fun generateSessionId(): String {
        return "session_${com.unify.core.platform.getCurrentTimeMillis()}_${(0..9999).random()}"
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        coroutineScope.cancel()
        sessionManager.cleanup()
        tokenStorage.cleanup()
    }
}

/**
 * 认证配置
 */
@Serializable
data class AuthConfig(
    val sessionTimeout: Duration = 24.hours,
    val sessionCheckInterval: Duration = 5.minutes,
    val tokenRefreshThreshold: Duration = 10.minutes,
    val maxLoginAttempts: Int = 5,
    val lockoutDuration: Duration = 30.minutes,
    val enableMFA: Boolean = false,
    val enableBiometric: Boolean = true,
    val enableSSO: Boolean = false
)

/**
 * 认证用户
 */
@Serializable
data class AuthUser(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatar: String? = null,
    val roles: List<String>,
    val permissions: List<String>,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long,
    val lastLoginAt: Long? = null
)

/**
 * 会话信息
 */
@Serializable
data class SessionInfo(
    val sessionId: String,
    val userId: String,
    val authProvider: AuthProvider,
    val accessToken: String,
    val loginTime: Long,
    val expiresAt: Long,
    val lastActivity: Long
)

/**
 * 认证令牌
 */
@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val expiresAt: Long = com.unify.core.platform.getCurrentTimeMillis() + expiresIn * 1000
) {
    fun isExpired(): Boolean {
        return com.unify.core.platform.getCurrentTimeMillis() > expiresAt
    }
}

/**
 * 认证统计
 */
data class AuthStats(
    val isAuthenticated: Boolean,
    val authProvider: AuthProvider?,
    val loginTime: Long?,
    val expiresAt: Long?,
    val sessionDuration: Long?,
    val userRoles: List<String>,
    val userPermissions: List<String>
)

/**
 * 认证状态
 */
enum class AuthState {
    UNAUTHENTICATED,        // 未认证
    AUTHENTICATING,         // 认证中
    AUTHENTICATED,          // 已认证
    MFA_REQUIRED,          // 需要多因素认证
    AUTHENTICATION_FAILED,  // 认证失败
    SESSION_EXPIRED        // 会话过期
}

/**
 * 认证提供者
 */
enum class AuthProvider {
    LOCAL,              // 本地认证
    OAUTH_GOOGLE,       // Google OAuth
    OAUTH_GITHUB,       // GitHub OAuth
    OAUTH_MICROSOFT,    // Microsoft OAuth
    BIOMETRIC,          // 生物识别
    SSO                 // 单点登录
}

/**
 * 多因素认证类型
 */
enum class MFAType {
    TOTP,               // 时间基础一次性密码
    SMS,                // 短信验证码
    EMAIL,              // 邮件验证码
    PUSH                // 推送通知
}

/**
 * 认证凭据
 */
sealed class AuthCredentials {
    data class UsernamePassword(val username: String, val password: String) : AuthCredentials()
    data class OAuth(val authorizationCode: String) : AuthCredentials()
    object Biometric : AuthCredentials()
    data class Token(val token: String) : AuthCredentials()
}

/**
 * 认证结果
 */
sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class RequiresMFA(val challenge: String) : AuthResult()
}

/**
 * 认证提供者结果
 */
sealed class AuthProviderResult {
    data class Success(val user: AuthUser, val tokens: AuthTokens) : AuthProviderResult()
    data class RequiresMFA(val mfaChallenge: String) : AuthProviderResult()
    data class Error(val message: String) : AuthProviderResult()
}

/**
 * 令牌刷新结果
 */
sealed class TokenRefreshResult {
    data class Success(val tokens: AuthTokens) : TokenRefreshResult()
    data class Error(val message: String) : TokenRefreshResult()
}

/**
 * 用户信息结果
 */
sealed class UserInfoResult {
    data class Success(val user: AuthUser) : UserInfoResult()
    data class Error(val message: String) : UserInfoResult()
}

/**
 * 更新用户结果
 */
sealed class UpdateUserResult {
    data class Success(val user: AuthUser) : UpdateUserResult()
    data class Error(val message: String) : UpdateUserResult()
}

/**
 * 密码修改结果
 */
sealed class PasswordChangeResult {
    object Success : PasswordChangeResult()
    data class Error(val message: String) : PasswordChangeResult()
}
