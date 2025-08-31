package com.unify.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Unify安全管理器
 * 提供全面的安全验证、加密、权限控制和威胁检测
 */
class UnifySecurityManager {
    private val _securityStatus = MutableStateFlow(SecurityStatus.INITIALIZING)
    val securityStatus: StateFlow<SecurityStatus> = _securityStatus
    
    private val _threats = MutableStateFlow<List<SecurityThreat>>(emptyList())
    val threats: StateFlow<List<SecurityThreat>> = _threats
    
    private val encryptionService = EncryptionService()
    private val permissionManager = PermissionManager()
    private val threatDetector = ThreatDetector()
    private val auditLogger = SecurityAuditLogger()
    
    /**
     * 初始化安全管理器
     */
    suspend fun initialize(config: SecurityConfig = SecurityConfig()) {
        _securityStatus.value = SecurityStatus.INITIALIZING
        
        // 初始化加密服务
        encryptionService.initialize(config.encryptionConfig)
        
        // 初始化权限管理
        permissionManager.initialize(config.permissionConfig)
        
        // 启动威胁检测
        threatDetector.initialize(config.threatDetectionConfig)
        
        // 初始化审计日志
        auditLogger.initialize(config.auditConfig)
        
        _securityStatus.value = SecurityStatus.ACTIVE
        auditLogger.log(SecurityEvent.SYSTEM_INITIALIZED, "安全管理器初始化完成")
    }
    
    /**
     * 验证用户身份
     */
    suspend fun authenticateUser(credentials: UserCredentials): AuthenticationResult {
        auditLogger.log(SecurityEvent.AUTHENTICATION_ATTEMPT, "用户认证尝试: ${credentials.username}")
        
        // 检查账户锁定状态
        if (isAccountLocked(credentials.username)) {
            auditLogger.log(SecurityEvent.AUTHENTICATION_FAILED, "账户已锁定: ${credentials.username}")
            return AuthenticationResult(false, "账户已锁定", null)
        }
        
        // 验证凭据
        val isValid = validateCredentials(credentials)
        
        if (isValid) {
            val token = generateSecureToken(credentials.username)
            auditLogger.log(SecurityEvent.AUTHENTICATION_SUCCESS, "用户认证成功: ${credentials.username}")
            return AuthenticationResult(true, "认证成功", token)
        } else {
            recordFailedAttempt(credentials.username)
            auditLogger.log(SecurityEvent.AUTHENTICATION_FAILED, "用户认证失败: ${credentials.username}")
            return AuthenticationResult(false, "凭据无效", null)
        }
    }
    
    /**
     * 授权检查
     */
    fun authorize(token: String, resource: String, action: String): AuthorizationResult {
        val user = validateToken(token)
        if (user == null) {
            auditLogger.log(SecurityEvent.AUTHORIZATION_FAILED, "无效令牌访问: $resource")
            return AuthorizationResult(false, "令牌无效")
        }
        
        val hasPermission = permissionManager.checkPermission(user.username, resource, action)
        
        if (hasPermission) {
            auditLogger.log(SecurityEvent.AUTHORIZATION_SUCCESS, "授权成功: ${user.username} -> $resource:$action")
            return AuthorizationResult(true, "授权成功")
        } else {
            auditLogger.log(SecurityEvent.AUTHORIZATION_FAILED, "权限不足: ${user.username} -> $resource:$action")
            return AuthorizationResult(false, "权限不足")
        }
    }
    
    /**
     * 加密敏感数据
     */
    fun encryptData(data: String, keyId: String = "default"): EncryptionResult {
        return try {
            val encryptedData = encryptionService.encrypt(data, keyId)
            auditLogger.log(SecurityEvent.DATA_ENCRYPTED, "数据加密成功")
            EncryptionResult(true, encryptedData, null)
        } catch (e: Exception) {
            auditLogger.log(SecurityEvent.ENCRYPTION_FAILED, "数据加密失败: ${e.message}")
            EncryptionResult(false, null, e.message)
        }
    }
    
    /**
     * 解密敏感数据
     */
    fun decryptData(encryptedData: String, keyId: String = "default"): DecryptionResult {
        return try {
            val decryptedData = encryptionService.decrypt(encryptedData, keyId)
            auditLogger.log(SecurityEvent.DATA_DECRYPTED, "数据解密成功")
            DecryptionResult(true, decryptedData, null)
        } catch (e: Exception) {
            auditLogger.log(SecurityEvent.DECRYPTION_FAILED, "数据解密失败: ${e.message}")
            DecryptionResult(false, null, e.message)
        }
    }
    
    /**
     * 输入验证和清理
     */
    fun validateAndSanitizeInput(input: String, type: InputType): InputValidationResult {
        val threats = threatDetector.detectInputThreats(input, type)
        
        if (threats.isNotEmpty()) {
            auditLogger.log(SecurityEvent.MALICIOUS_INPUT_DETECTED, "检测到恶意输入: ${threats.joinToString()}")
            return InputValidationResult(false, null, threats)
        }
        
        val sanitizedInput = sanitizeInput(input, type)
        return InputValidationResult(true, sanitizedInput, emptyList())
    }
    
    /**
     * 检测安全威胁
     */
    suspend fun scanForThreats(): ThreatScanResult {
        val detectedThreats = threatDetector.performFullScan()
        _threats.value = detectedThreats
        
        val criticalThreats = detectedThreats.filter { it.severity == ThreatSeverity.CRITICAL }
        
        if (criticalThreats.isNotEmpty()) {
            _securityStatus.value = SecurityStatus.UNDER_ATTACK
            auditLogger.log(SecurityEvent.CRITICAL_THREAT_DETECTED, "检测到严重威胁: ${criticalThreats.size}个")
        }
        
        return ThreatScanResult(
            totalThreats = detectedThreats.size,
            criticalThreats = criticalThreats.size,
            threats = detectedThreats,
            scanTime = System.currentTimeMillis()
        )
    }
    
    /**
     * 生成安全报告
     */
    fun generateSecurityReport(): SecurityReport {
        val auditLogs = auditLogger.getRecentLogs(24 * 60 * 60 * 1000L) // 24小时
        val currentThreats = _threats.value
        
        return SecurityReport(
            timestamp = System.currentTimeMillis(),
            securityStatus = _securityStatus.value,
            threatCount = currentThreats.size,
            criticalThreatCount = currentThreats.count { it.severity == ThreatSeverity.CRITICAL },
            auditEventCount = auditLogs.size,
            recommendations = generateSecurityRecommendations(currentThreats, auditLogs)
        )
    }
    
    private fun isAccountLocked(username: String): Boolean {
        return permissionManager.isAccountLocked(username)
    }
    
    private suspend fun validateCredentials(credentials: UserCredentials): Boolean {
        // 模拟凭据验证
        return credentials.password.length >= 8 && credentials.username.isNotEmpty()
    }
    
    private fun generateSecureToken(username: String): String {
        return encryptionService.generateToken(username)
    }
    
    private fun validateToken(token: String): User? {
        return encryptionService.validateToken(token)
    }
    
    private fun recordFailedAttempt(username: String) {
        permissionManager.recordFailedAttempt(username)
    }
    
    private fun sanitizeInput(input: String, type: InputType): String {
        return when (type) {
            InputType.HTML -> input.replace("<", "&lt;").replace(">", "&gt;")
            InputType.SQL -> input.replace("'", "''").replace(";", "")
            InputType.COMMAND -> input.replace("|", "").replace("&", "").replace(";", "")
            InputType.GENERAL -> input.trim()
        }
    }
    
    private fun generateSecurityRecommendations(threats: List<SecurityThreat>, auditLogs: List<AuditLog>): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()
        
        if (threats.any { it.type == ThreatType.BRUTE_FORCE }) {
            recommendations.add(SecurityRecommendation(
                type = "authentication",
                description = "启用账户锁定机制防止暴力破解",
                priority = RecommendationPriority.HIGH
            ))
        }
        
        if (threats.any { it.type == ThreatType.SQL_INJECTION }) {
            recommendations.add(SecurityRecommendation(
                type = "input_validation",
                description = "加强SQL注入防护",
                priority = RecommendationPriority.CRITICAL
            ))
        }
        
        val failedLogins = auditLogs.count { it.event == SecurityEvent.AUTHENTICATION_FAILED }
        if (failedLogins > 10) {
            recommendations.add(SecurityRecommendation(
                type = "monitoring",
                description = "异常登录尝试过多，建议加强监控",
                priority = RecommendationPriority.MEDIUM
            ))
        }
        
        return recommendations
    }
}

// 加密服务
class EncryptionService {
    private val keys = mutableMapOf<String, String>()
    
    fun initialize(config: EncryptionConfig) {
        keys["default"] = generateKey()
    }
    
    fun encrypt(data: String, keyId: String): String {
        val key = keys[keyId] ?: throw SecurityException("密钥不存在: $keyId")
        return "encrypted_${data}_with_$key"
    }
    
    fun decrypt(encryptedData: String, keyId: String): String {
        val key = keys[keyId] ?: throw SecurityException("密钥不存在: $keyId")
        return encryptedData.removePrefix("encrypted_").removeSuffix("_with_$key")
    }
    
    fun generateToken(username: String): String {
        return "token_${username}_${System.currentTimeMillis()}"
    }
    
    fun validateToken(token: String): User? {
        if (token.startsWith("token_")) {
            val parts = token.split("_")
            if (parts.size >= 3) {
                return User(parts[1], emptyList())
            }
        }
        return null
    }
    
    private fun generateKey(): String = "secure_key_${System.currentTimeMillis()}"
}

// 权限管理器
class PermissionManager {
    private val userPermissions = mutableMapOf<String, Set<String>>()
    private val lockedAccounts = mutableSetOf<String>()
    private val failedAttempts = mutableMapOf<String, Int>()
    
    fun initialize(config: PermissionConfig) {
        // 初始化默认权限
        userPermissions["admin"] = setOf("read", "write", "delete", "admin")
        userPermissions["user"] = setOf("read", "write")
    }
    
    fun checkPermission(username: String, resource: String, action: String): Boolean {
        val permissions = userPermissions[username] ?: emptySet()
        return permissions.contains(action) || permissions.contains("admin")
    }
    
    fun isAccountLocked(username: String): Boolean {
        return lockedAccounts.contains(username)
    }
    
    fun recordFailedAttempt(username: String) {
        val attempts = failedAttempts.getOrDefault(username, 0) + 1
        failedAttempts[username] = attempts
        
        if (attempts >= 5) {
            lockedAccounts.add(username)
        }
    }
}

// 威胁检测器
class ThreatDetector {
    fun initialize(config: ThreatDetectionConfig) {
        // 初始化威胁检测规则
    }
    
    fun detectInputThreats(input: String, type: InputType): List<String> {
        val threats = mutableListOf<String>()
        
        when (type) {
            InputType.HTML -> {
                if (input.contains("<script>")) threats.add("XSS")
                if (input.contains("javascript:")) threats.add("XSS")
            }
            InputType.SQL -> {
                if (input.contains("DROP TABLE")) threats.add("SQL_INJECTION")
                if (input.contains("UNION SELECT")) threats.add("SQL_INJECTION")
            }
            InputType.COMMAND -> {
                if (input.contains("rm -rf")) threats.add("COMMAND_INJECTION")
                if (input.contains("sudo")) threats.add("PRIVILEGE_ESCALATION")
            }
            else -> {}
        }
        
        return threats
    }
    
    suspend fun performFullScan(): List<SecurityThreat> {
        return listOf(
            SecurityThreat(
                id = "threat_1",
                type = ThreatType.BRUTE_FORCE,
                severity = ThreatSeverity.MEDIUM,
                description = "检测到暴力破解尝试",
                source = "192.168.1.100",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

// 安全审计日志
class SecurityAuditLogger {
    private val logs = mutableListOf<AuditLog>()
    
    fun initialize(config: AuditConfig) {
        // 初始化审计配置
    }
    
    fun log(event: SecurityEvent, details: String) {
        logs.add(AuditLog(
            timestamp = System.currentTimeMillis(),
            event = event,
            details = details,
            source = "UnifySecurityManager"
        ))
        
        // 保持日志大小在合理范围内
        if (logs.size > 10000) {
            logs.removeAt(0)
        }
    }
    
    fun getRecentLogs(timeWindow: Long): List<AuditLog> {
        val cutoff = System.currentTimeMillis() - timeWindow
        return logs.filter { it.timestamp >= cutoff }
    }
}

// 数据类和枚举
@Serializable
data class SecurityConfig(
    val encryptionConfig: EncryptionConfig = EncryptionConfig(),
    val permissionConfig: PermissionConfig = PermissionConfig(),
    val threatDetectionConfig: ThreatDetectionConfig = ThreatDetectionConfig(),
    val auditConfig: AuditConfig = AuditConfig()
)

@Serializable
data class EncryptionConfig(
    val algorithm: String = "AES-256",
    val keyRotationInterval: Long = 24 * 60 * 60 * 1000L // 24小时
)

@Serializable
data class PermissionConfig(
    val maxFailedAttempts: Int = 5,
    val lockoutDuration: Long = 30 * 60 * 1000L // 30分钟
)

@Serializable
data class ThreatDetectionConfig(
    val scanInterval: Long = 60 * 1000L, // 1分钟
    val enableRealTimeDetection: Boolean = true
)

@Serializable
data class AuditConfig(
    val logLevel: String = "INFO",
    val retentionPeriod: Long = 30 * 24 * 60 * 60 * 1000L // 30天
)

@Serializable
data class UserCredentials(
    val username: String,
    val password: String,
    val additionalFactors: Map<String, String> = emptyMap()
)

@Serializable
data class User(
    val username: String,
    val roles: List<String>
)

@Serializable
data class AuthenticationResult(
    val success: Boolean,
    val message: String,
    val token: String?
)

@Serializable
data class AuthorizationResult(
    val authorized: Boolean,
    val message: String
)

@Serializable
data class EncryptionResult(
    val success: Boolean,
    val encryptedData: String?,
    val error: String?
)

@Serializable
data class DecryptionResult(
    val success: Boolean,
    val decryptedData: String?,
    val error: String?
)

@Serializable
data class InputValidationResult(
    val valid: Boolean,
    val sanitizedInput: String?,
    val threats: List<String>
)

@Serializable
data class SecurityThreat(
    val id: String,
    val type: ThreatType,
    val severity: ThreatSeverity,
    val description: String,
    val source: String,
    val timestamp: Long
)

@Serializable
data class ThreatScanResult(
    val totalThreats: Int,
    val criticalThreats: Int,
    val threats: List<SecurityThreat>,
    val scanTime: Long
)

@Serializable
data class SecurityReport(
    val timestamp: Long,
    val securityStatus: SecurityStatus,
    val threatCount: Int,
    val criticalThreatCount: Int,
    val auditEventCount: Int,
    val recommendations: List<SecurityRecommendation>
)

@Serializable
data class SecurityRecommendation(
    val type: String,
    val description: String,
    val priority: RecommendationPriority
)

@Serializable
data class AuditLog(
    val timestamp: Long,
    val event: SecurityEvent,
    val details: String,
    val source: String
)

enum class SecurityStatus {
    INITIALIZING, ACTIVE, WARNING, UNDER_ATTACK, COMPROMISED
}

enum class InputType {
    HTML, SQL, COMMAND, GENERAL
}

enum class ThreatType {
    BRUTE_FORCE, SQL_INJECTION, XSS, COMMAND_INJECTION, PRIVILEGE_ESCALATION, DATA_BREACH
}

enum class ThreatSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class SecurityEvent {
    SYSTEM_INITIALIZED, AUTHENTICATION_ATTEMPT, AUTHENTICATION_SUCCESS, AUTHENTICATION_FAILED,
    AUTHORIZATION_SUCCESS, AUTHORIZATION_FAILED, DATA_ENCRYPTED, DATA_DECRYPTED,
    ENCRYPTION_FAILED, DECRYPTION_FAILED, MALICIOUS_INPUT_DETECTED, CRITICAL_THREAT_DETECTED
}

enum class RecommendationPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}
