package com.unify.core.security

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 认证提供者适配器接口
 */
interface AuthProviderAdapter {
    suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult
    suspend fun refreshToken(refreshToken: String): TokenRefreshResult
    suspend fun logout(accessToken: String): Boolean
    suspend fun getUserInfo(accessToken: String): UserInfoResult
    suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult
    suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult
}

/**
 * 本地认证提供者
 */
class LocalAuthProvider : AuthProviderAdapter {
    private val userDatabase = mutableMapOf<String, StoredUser>()
    private val activeTokens = mutableMapOf<String, AuthTokens>()
    
    init {
        // 初始化测试用户
        initializeTestUsers()
    }
    
    override suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult {
        return when (credentials) {
            is AuthCredentials.UsernamePassword -> {
                authenticateWithPassword(credentials.username, credentials.password)
            }
            else -> AuthProviderResult.Error("本地认证不支持此凭据类型")
        }
    }
    
    private suspend fun authenticateWithPassword(username: String, password: String): AuthProviderResult {
        // 模拟网络延迟
        delay(500)
        
        val storedUser = userDatabase[username]
            ?: return AuthProviderResult.Error("用户不存在")
        
        if (!verifyPassword(password, storedUser.passwordHash)) {
            return AuthProviderResult.Error("密码错误")
        }
        
        // 检查是否需要MFA
        if (storedUser.mfaEnabled) {
            return AuthProviderResult.RequiresMFA("请输入多因素认证码")
        }
        
        val tokens = generateTokens(storedUser.id)
        activeTokens[tokens.accessToken] = tokens
        
        val user = AuthUser(
            id = storedUser.id,
            username = storedUser.username,
            email = storedUser.email,
            displayName = storedUser.displayName,
            avatar = storedUser.avatar,
            roles = storedUser.roles,
            permissions = storedUser.permissions,
            metadata = storedUser.metadata,
            createdAt = storedUser.createdAt,
            lastLoginAt = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        return AuthProviderResult.Success(user, tokens)
    }
    
    override suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        delay(200)
        
        // 查找对应的令牌
        val existingTokens = activeTokens.values.find { it.refreshToken == refreshToken }
            ?: return TokenRefreshResult.Error("刷新令牌无效")
        
        // 生成新令牌
        val userId = getUserIdFromToken(existingTokens.accessToken)
        val newTokens = generateTokens(userId)
        
        // 移除旧令牌
        activeTokens.remove(existingTokens.accessToken)
        activeTokens[newTokens.accessToken] = newTokens
        
        return TokenRefreshResult.Success(newTokens)
    }
    
    override suspend fun logout(accessToken: String): Boolean {
        activeTokens.remove(accessToken)
        return true
    }
    
    override suspend fun getUserInfo(accessToken: String): UserInfoResult {
        delay(200)
        
        val tokens = activeTokens[accessToken]
            ?: return UserInfoResult.Error("访问令牌无效")
        
        if (tokens.isExpired()) {
            activeTokens.remove(accessToken)
            return UserInfoResult.Error("访问令牌已过期")
        }
        
        val userId = getUserIdFromToken(accessToken)
        val storedUser = userDatabase.values.find { it.id == userId }
            ?: return UserInfoResult.Error("用户不存在")
        
        val user = AuthUser(
            id = storedUser.id,
            username = storedUser.username,
            email = storedUser.email,
            displayName = storedUser.displayName,
            avatar = storedUser.avatar,
            roles = storedUser.roles,
            permissions = storedUser.permissions,
            metadata = storedUser.metadata,
            createdAt = storedUser.createdAt,
            lastLoginAt = storedUser.lastLoginAt
        )
        
        return UserInfoResult.Success(user)
    }
    
    override suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult {
        delay(300)
        
        val userId = getUserIdFromToken(accessToken)
        val storedUser = userDatabase.values.find { it.id == userId }?.let { user ->
            user.copy(
                displayName = userInfo["displayName"] as? String ?: user.displayName,
                email = userInfo["email"] as? String ?: user.email,
                avatar = userInfo["avatar"] as? String ?: user.avatar,
                metadata = userInfo["metadata"] as? Map<String, String> ?: user.metadata
            )
        } ?: return UpdateUserResult.Error("用户不存在")
        
        userDatabase[storedUser.username] = storedUser
        
        val user = AuthUser(
            id = storedUser.id,
            username = storedUser.username,
            email = storedUser.email,
            displayName = storedUser.displayName,
            avatar = storedUser.avatar,
            roles = storedUser.roles,
            permissions = storedUser.permissions,
            metadata = storedUser.metadata,
            createdAt = storedUser.createdAt,
            lastLoginAt = storedUser.lastLoginAt
        )
        
        return UpdateUserResult.Success(user)
    }
    
    override suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult {
        delay(300)
        
        val userId = getUserIdFromToken(accessToken)
        val storedUser = userDatabase.values.find { it.id == userId }
            ?: return PasswordChangeResult.Error("用户不存在")
        
        if (!verifyPassword(oldPassword, storedUser.passwordHash)) {
            return PasswordChangeResult.Error("原密码错误")
        }
        
        val newPasswordHash = hashPassword(newPassword)
        val updatedUser = storedUser.copy(passwordHash = newPasswordHash)
        userDatabase[storedUser.username] = updatedUser
        
        return PasswordChangeResult.Success
    }
    
    private fun initializeTestUsers() {
        val testUser = StoredUser(
            id = "user_001",
            username = "testuser",
            email = "test@example.com",
            displayName = "测试用户",
            passwordHash = hashPassword("password123"),
            roles = listOf("user"),
            permissions = listOf("read", "write"),
            mfaEnabled = false,
            createdAt = com.unify.core.platform.getCurrentTimeMillis(),
            lastLoginAt = null
        )
        
        val adminUser = StoredUser(
            id = "admin_001",
            username = "admin",
            email = "admin@example.com",
            displayName = "管理员",
            passwordHash = hashPassword("admin123"),
            roles = listOf("admin"),
            permissions = listOf("read", "write", "delete", "manage_users"),
            mfaEnabled = true,
            createdAt = com.unify.core.platform.getCurrentTimeMillis(),
            lastLoginAt = null
        )
        
        userDatabase[testUser.username] = testUser
        userDatabase[adminUser.username] = adminUser
    }
    
    private fun generateTokens(userId: String): AuthTokens {
        val accessToken = "access_${userId}_${com.unify.core.platform.getCurrentTimeMillis()}_${(0..9999).random()}"
        val refreshToken = "refresh_${userId}_${com.unify.core.platform.getCurrentTimeMillis()}_${(0..9999).random()}"
        
        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 3600 // 1小时
        )
    }
    
    private fun getUserIdFromToken(accessToken: String): String {
        // 从令牌中提取用户ID（简化实现）
        return accessToken.split("_").getOrNull(1) ?: ""
    }
    
    private fun hashPassword(password: String): String {
        // 简化的密码哈希（生产环境应使用bcrypt等安全算法）
        return "hash_${password.hashCode()}"
    }
    
    private fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }
}

/**
 * Google OAuth提供者
 */
class GoogleOAuthProvider : AuthProviderAdapter {
    override suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult {
        return when (credentials) {
            is AuthCredentials.OAuth -> {
                authenticateWithOAuth(credentials.authorizationCode)
            }
            else -> AuthProviderResult.Error("Google OAuth不支持此凭据类型")
        }
    }
    
    private suspend fun authenticateWithOAuth(authCode: String): AuthProviderResult {
        delay(1000) // 模拟OAuth流程
        
        // 模拟OAuth验证
        if (authCode.startsWith("google_")) {
            val user = AuthUser(
                id = "google_user_001",
                username = "googleuser",
                email = "user@gmail.com",
                displayName = "Google用户",
                avatar = "https://example.com/avatar.jpg",
                roles = listOf("user"),
                permissions = listOf("read", "write"),
                createdAt = com.unify.core.platform.getCurrentTimeMillis()
            )
            
            val tokens = AuthTokens(
                accessToken = "google_access_${com.unify.core.platform.getCurrentTimeMillis()}",
                refreshToken = "google_refresh_${com.unify.core.platform.getCurrentTimeMillis()}",
                expiresIn = 3600
            )
            
            return AuthProviderResult.Success(user, tokens)
        }
        
        return AuthProviderResult.Error("Google OAuth认证失败")
    }
    
    override suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        delay(500)
        return TokenRefreshResult.Error("Google OAuth令牌刷新未实现")
    }
    
    override suspend fun logout(accessToken: String): Boolean {
        return true
    }
    
    override suspend fun getUserInfo(accessToken: String): UserInfoResult {
        delay(300)
        return UserInfoResult.Error("Google OAuth用户信息获取未实现")
    }
    
    override suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult {
        return UpdateUserResult.Error("Google OAuth不支持更新用户信息")
    }
    
    override suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult {
        return PasswordChangeResult.Error("Google OAuth不支持密码修改")
    }
}

/**
 * GitHub OAuth提供者
 */
class GitHubOAuthProvider : AuthProviderAdapter {
    override suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult {
        return when (credentials) {
            is AuthCredentials.OAuth -> {
                authenticateWithOAuth(credentials.authorizationCode)
            }
            else -> AuthProviderResult.Error("GitHub OAuth不支持此凭据类型")
        }
    }
    
    private suspend fun authenticateWithOAuth(authCode: String): AuthProviderResult {
        delay(1000)
        
        if (authCode.startsWith("github_")) {
            val user = AuthUser(
                id = "github_user_001",
                username = "githubuser",
                email = "user@github.com",
                displayName = "GitHub用户",
                roles = listOf("developer"),
                permissions = listOf("read", "write", "code_access"),
                createdAt = com.unify.core.platform.getCurrentTimeMillis()
            )
            
            val tokens = AuthTokens(
                accessToken = "github_access_${com.unify.core.platform.getCurrentTimeMillis()}",
                refreshToken = "github_refresh_${com.unify.core.platform.getCurrentTimeMillis()}",
                expiresIn = 7200
            )
            
            return AuthProviderResult.Success(user, tokens)
        }
        
        return AuthProviderResult.Error("GitHub OAuth认证失败")
    }
    
    override suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        delay(500)
        return TokenRefreshResult.Error("GitHub OAuth令牌刷新未实现")
    }
    
    override suspend fun logout(accessToken: String): Boolean = true
    
    override suspend fun getUserInfo(accessToken: String): UserInfoResult =
        UserInfoResult.Error("GitHub OAuth用户信息获取未实现")
    
    override suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult =
        UpdateUserResult.Error("GitHub OAuth不支持更新用户信息")
    
    override suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult =
        PasswordChangeResult.Error("GitHub OAuth不支持密码修改")
}

/**
 * Microsoft OAuth提供者
 */
class MicrosoftOAuthProvider : AuthProviderAdapter {
    override suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult {
        return when (credentials) {
            is AuthCredentials.OAuth -> {
                authenticateWithOAuth(credentials.authorizationCode)
            }
            else -> AuthProviderResult.Error("Microsoft OAuth不支持此凭据类型")
        }
    }
    
    private suspend fun authenticateWithOAuth(authCode: String): AuthProviderResult {
        delay(1000)
        
        if (authCode.startsWith("microsoft_")) {
            val user = AuthUser(
                id = "ms_user_001",
                username = "msuser",
                email = "user@outlook.com",
                displayName = "Microsoft用户",
                roles = listOf("user"),
                permissions = listOf("read", "write", "office_access"),
                createdAt = com.unify.core.platform.getCurrentTimeMillis()
            )
            
            val tokens = AuthTokens(
                accessToken = "ms_access_${com.unify.core.platform.getCurrentTimeMillis()}",
                refreshToken = "ms_refresh_${com.unify.core.platform.getCurrentTimeMillis()}",
                expiresIn = 3600
            )
            
            return AuthProviderResult.Success(user, tokens)
        }
        
        return AuthProviderResult.Error("Microsoft OAuth认证失败")
    }
    
    override suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        delay(500)
        return TokenRefreshResult.Error("Microsoft OAuth令牌刷新未实现")
    }
    
    override suspend fun logout(accessToken: String): Boolean = true
    
    override suspend fun getUserInfo(accessToken: String): UserInfoResult =
        UserInfoResult.Error("Microsoft OAuth用户信息获取未实现")
    
    override suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult =
        UpdateUserResult.Error("Microsoft OAuth不支持更新用户信息")
    
    override suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult =
        PasswordChangeResult.Error("Microsoft OAuth不支持密码修改")
}

/**
 * 生物识别认证提供者
 */
class BiometricAuthProvider : AuthProviderAdapter {
    override suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult {
        return when (credentials) {
            is AuthCredentials.Biometric -> {
                authenticateWithBiometric()
            }
            else -> AuthProviderResult.Error("生物识别不支持此凭据类型")
        }
    }
    
    private suspend fun authenticateWithBiometric(): AuthProviderResult {
        delay(800) // 模拟生物识别验证时间
        
        // 模拟生物识别成功
        val user = AuthUser(
            id = "biometric_user_001",
            username = "biometricuser",
            email = "biometric@example.com",
            displayName = "生物识别用户",
            roles = listOf("user"),
            permissions = listOf("read", "write"),
            createdAt = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        val tokens = AuthTokens(
            accessToken = "biometric_access_${com.unify.core.platform.getCurrentTimeMillis()}",
            refreshToken = "biometric_refresh_${com.unify.core.platform.getCurrentTimeMillis()}",
            expiresIn = 1800 // 30分钟
        )
        
        return AuthProviderResult.Success(user, tokens)
    }
    
    override suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        delay(300)
        return TokenRefreshResult.Error("生物识别需要重新验证")
    }
    
    override suspend fun logout(accessToken: String): Boolean = true
    
    override suspend fun getUserInfo(accessToken: String): UserInfoResult =
        UserInfoResult.Error("生物识别用户信息获取未实现")
    
    override suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult =
        UpdateUserResult.Error("生物识别不支持更新用户信息")
    
    override suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult =
        PasswordChangeResult.Error("生物识别不支持密码修改")
}

/**
 * SSO认证提供者
 */
class SSOAuthProvider : AuthProviderAdapter {
    override suspend fun authenticate(credentials: AuthCredentials): AuthProviderResult {
        return when (credentials) {
            is AuthCredentials.Token -> {
                authenticateWithSSOToken(credentials.token)
            }
            else -> AuthProviderResult.Error("SSO不支持此凭据类型")
        }
    }
    
    private suspend fun authenticateWithSSOToken(ssoToken: String): AuthProviderResult {
        delay(600)
        
        if (ssoToken.startsWith("sso_")) {
            val user = AuthUser(
                id = "sso_user_001",
                username = "ssouser",
                email = "sso@company.com",
                displayName = "SSO用户",
                roles = listOf("employee"),
                permissions = listOf("read", "write", "company_access"),
                createdAt = com.unify.core.platform.getCurrentTimeMillis()
            )
            
            val tokens = AuthTokens(
                accessToken = "sso_access_${com.unify.core.platform.getCurrentTimeMillis()}",
                refreshToken = "sso_refresh_${com.unify.core.platform.getCurrentTimeMillis()}",
                expiresIn = 28800 // 8小时
            )
            
            return AuthProviderResult.Success(user, tokens)
        }
        
        return AuthProviderResult.Error("SSO令牌无效")
    }
    
    override suspend fun refreshToken(refreshToken: String): TokenRefreshResult {
        delay(400)
        return TokenRefreshResult.Error("SSO令牌刷新未实现")
    }
    
    override suspend fun logout(accessToken: String): Boolean = true
    
    override suspend fun getUserInfo(accessToken: String): UserInfoResult =
        UserInfoResult.Error("SSO用户信息获取未实现")
    
    override suspend fun updateUserInfo(accessToken: String, userInfo: Map<String, Any>): UpdateUserResult =
        UpdateUserResult.Error("SSO不支持更新用户信息")
    
    override suspend fun changePassword(accessToken: String, oldPassword: String, newPassword: String): PasswordChangeResult =
        PasswordChangeResult.Error("SSO不支持密码修改")
}

/**
 * 存储的用户信息
 */
@Serializable
data class StoredUser(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val avatar: String? = null,
    val roles: List<String>,
    val permissions: List<String>,
    val metadata: Map<String, String> = emptyMap(),
    val mfaEnabled: Boolean = false,
    val createdAt: Long,
    val lastLoginAt: Long? = null
)

/**
 * 令牌存储
 */
class TokenStorage {
    private var storedTokens: AuthTokens? = null
    
    fun storeTokens(tokens: AuthTokens) {
        storedTokens = tokens
        // 在实际实现中，这里应该使用安全的本地存储
    }
    
    fun getStoredTokens(): AuthTokens? {
        return storedTokens
    }
    
    fun getAccessToken(): String? {
        return storedTokens?.accessToken
    }
    
    fun getRefreshToken(): String? {
        return storedTokens?.refreshToken
    }
    
    fun clearTokens() {
        storedTokens = null
    }
    
    fun cleanup() {
        clearTokens()
    }
}

/**
 * 会话管理器
 */
class SessionManager(private val config: AuthConfig) {
    private var currentSession: SessionInfo? = null
    
    fun startSession(session: SessionInfo) {
        currentSession = session
    }
    
    fun updateSession(session: SessionInfo) {
        currentSession = session
    }
    
    fun getCurrentSession(): SessionInfo? {
        return currentSession
    }
    
    fun endSession() {
        currentSession = null
    }
    
    fun isSessionValid(): Boolean {
        val session = currentSession ?: return false
        return com.unify.core.platform.getCurrentTimeMillis() < session.expiresAt
    }
    
    fun cleanup() {
        endSession()
    }
}

/**
 * 多因素认证管理器
 */
class MFAManager {
    private var pendingUser: AuthUser? = null
    private var pendingTokens: AuthTokens? = null
    private val validCodes = mutableSetOf<String>()
    
    init {
        // 添加一些测试MFA码
        validCodes.add("123456")
        validCodes.add("654321")
    }
    
    fun setPendingAuth(user: AuthUser, tokens: AuthTokens) {
        pendingUser = user
        pendingTokens = tokens
    }
    
    fun getPendingUser(): AuthUser? = pendingUser
    
    fun getPendingTokens(): AuthTokens? = pendingTokens
    
    fun verifyCode(code: String, type: MFAType): Boolean {
        return when (type) {
            MFAType.TOTP -> verifyTOTP(code)
            MFAType.SMS -> verifySMS(code)
            MFAType.EMAIL -> verifyEmail(code)
            MFAType.PUSH -> verifyPush(code)
        }
    }
    
    private fun verifyTOTP(code: String): Boolean {
        return validCodes.contains(code)
    }
    
    private fun verifySMS(code: String): Boolean {
        return validCodes.contains(code)
    }
    
    private fun verifyEmail(code: String): Boolean {
        return validCodes.contains(code)
    }
    
    private fun verifyPush(code: String): Boolean {
        return code == "approved"
    }
    
    fun clearPendingAuth() {
        pendingUser = null
        pendingTokens = null
    }
}
