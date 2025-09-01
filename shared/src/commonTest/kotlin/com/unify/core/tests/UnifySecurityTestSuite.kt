package com.unify.core.tests

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import com.unify.core.security.UnifySecurityManager
import com.unify.core.security.SecurityLevel
import com.unify.core.security.EncryptionType
import com.unify.core.security.AuthenticationMethod
import com.unify.core.security.SecurityPolicy
import com.unify.core.security.SecurityAuditLog
import com.unify.core.security.ThreatDetectionResult
import com.unify.core.security.SecurityConfiguration

/**
 * Unify安全管理系统测试套件
 * 全面测试安全功能、加密、认证和威胁检测
 */
class UnifySecurityTestSuite {

    companion object {
        // 加密相关常量
        private const val AES_256_KEY_SIZE = 256
        private const val AES_128_KEY_SIZE = 128
        private const val RSA_2048_KEY_SIZE = 2048
        
        // 认证相关常量
        private const val TEST_PASSWORD = "SecurePassword123!"
        private const val BIOMETRIC_HASH = "fingerprint_hash_12345"
        private const val TOKEN_PASSWORD = "TokenPassword123!"
        private const val SESSION_PASSWORD = "SessionPassword123!"
        private const val CORRECT_PASSWORD = "CorrectPassword123!"
        private const val AUDIT_IP = "192.168.1.100"
        private const val STRONG_PASSWORD = "StrongPass123!"
        
        // 性能测试常量
        private const val PERFORMANCE_DATA_REPEAT = 1000
        private const val PERFORMANCE_TEST_ITERATIONS = 100
        private const val MAX_ENCRYPTION_TIME_MS = 5000L
        private const val SESSION_TIMEOUT_MS = 3600000L // 1 hour
        private const val MEMORY_TEST_ITERATIONS = 1000
        private const val MAX_MEMORY_INCREASE_MB = 50L
        private const val GC_DELAY_MS = 100L
        
        // 用户ID和权限常量
        private const val TEST_USER_ID = "user123"
        private const val VALID_PASSWORD_PATTERN = "Valid password123!"
    }

    private lateinit var securityManager: UnifySecurityManager

    @BeforeTest
    fun setup() {
        securityManager = UnifySecurityManager()
    }

    @AfterTest
    fun tearDown() {
        securityManager.cleanup()
    }

    // 基础安全功能测试
    @Test
    fun testSecurityManagerInitialization() = runTest {
        assertTrue(securityManager.isInitialized())
        assertEquals(SecurityLevel.HIGH, securityManager.getCurrentSecurityLevel())
        assertNotNull(securityManager.getSecurityConfiguration())
    }

    @Test
    fun testSecurityLevelConfiguration() = runTest {
        // 测试安全级别设置
        securityManager.setSecurityLevel(SecurityLevel.MAXIMUM)
        assertEquals(SecurityLevel.MAXIMUM, securityManager.getCurrentSecurityLevel())

        securityManager.setSecurityLevel(SecurityLevel.MEDIUM)
        assertEquals(SecurityLevel.MEDIUM, securityManager.getCurrentSecurityLevel())

        // 测试无效级别处理
        assertFailsWith<IllegalArgumentException> {
            securityManager.setSecurityLevel(null)
        }
    }

    // 加密功能测试
    @Test
    fun testDataEncryption() = runTest {
        val testData = "Sensitive test data 测试数据 🔐"
        val key = securityManager.generateEncryptionKey(EncryptionType.AES_256)

        // 测试加密
        val encryptedData = securityManager.encrypt(testData, key, EncryptionType.AES_256)
        assertNotNull(encryptedData)
        assertNotEquals(testData, encryptedData)
        assertTrue(encryptedData.length > testData.length)

        // 测试解密
        val decryptedData = securityManager.decrypt(encryptedData, key, EncryptionType.AES_256)
        assertEquals(testData, decryptedData)
    }

    @Test
    fun testMultipleEncryptionTypes() = runTest {
        val testData = "Multi-encryption test data"

        // 测试不同加密类型
        val encryptionTypes = listOf(
            EncryptionType.AES_128,
            EncryptionType.AES_256,
            EncryptionType.RSA_2048,
            EncryptionType.CHACHA20_POLY1305
        )

        encryptionTypes.forEach { type ->
            val key = securityManager.generateEncryptionKey(type)
            val encrypted = securityManager.encrypt(testData, key, type)
            val decrypted = securityManager.decrypt(encrypted, key, type)
            assertEquals(testData, decrypted, "Failed for encryption type: $type")
        }
    }

    @Test
    fun testKeyGeneration() = runTest {
        // 测试密钥生成
        val aesKey = securityManager.generateEncryptionKey(EncryptionType.AES_256)
        assertNotNull(aesKey)
        assertTrue(aesKey.isNotEmpty())

        val rsaKey = securityManager.generateEncryptionKey(EncryptionType.RSA_2048)
        assertNotNull(rsaKey)
        assertTrue(rsaKey.isNotEmpty())

        // 确保每次生成的密钥都不同
        val anotherKey = securityManager.generateEncryptionKey(EncryptionType.AES_256)
        assertNotEquals(aesKey, anotherKey)
    }

    // 认证功能测试
    @Test
    fun testUserAuthentication() = runTest {
        val username = "testuser"
        val password = TEST_PASSWORD
        val biometricData = BIOMETRIC_HASH

        // 测试密码认证
        val passwordAuth = securityManager.authenticate(
            username, password, AuthenticationMethod.PASSWORD
        )
        assertTrue(passwordAuth.isSuccess)
        assertNotNull(passwordAuth.token)

        // 测试生物识别认证
        val biometricAuth = securityManager.authenticate(
            username, biometricData, AuthenticationMethod.BIOMETRIC
        )
        assertTrue(biometricAuth.isSuccess)
        assertNotNull(biometricAuth.token)

        // 测试多因子认证
        val mfaAuth = securityManager.authenticateMultiFactor(
            username, password, biometricData
        )
        assertTrue(mfaAuth.isSuccess)
        assertTrue(mfaAuth.securityLevel >= SecurityLevel.HIGH)
    }

    @Test
    fun testTokenManagement() = runTest {
        val username = "tokenuser"
        val password = TOKEN_PASSWORD

        // 生成令牌
        val authResult = securityManager.authenticate(username, password, AuthenticationMethod.PASSWORD)
        val token = authResult.token
        assertNotNull(token)

        // 验证令牌
        assertTrue(securityManager.validateToken(token))

        // 刷新令牌
        val newToken = securityManager.refreshToken(token)
        assertNotNull(newToken)
        assertNotEquals(token, newToken)

        // 撤销令牌
        securityManager.revokeToken(token)
        assertFalse(securityManager.validateToken(token))
    }

    @Test
    fun testSessionManagement() = runTest {
        val username = "sessionuser"
        val password = SESSION_PASSWORD

        // 创建会话
        val session = securityManager.createSession(username, password)
        assertNotNull(session)
        assertTrue(session.isActive)

        // 验证会话
        assertTrue(securityManager.validateSession(session.sessionId))

        // 更新会话活动时间
        securityManager.updateSessionActivity(session.sessionId)
        assertTrue(session.lastActivity > 0)

        // 结束会话
        securityManager.endSession(session.sessionId)
        assertFalse(securityManager.validateSession(session.sessionId))
    }

    // 权限管理测试
    @Test
    fun testPermissionManagement() = runTest {
        val userId = TEST_USER_ID
        val permissions = listOf("read", "write", "admin", "delete")

        // 授予权限
        permissions.forEach { permission ->
            securityManager.grantPermission(userId, permission)
            assertTrue(securityManager.hasPermission(userId, permission))
        }

        // 检查权限列表
        val userPermissions = securityManager.getUserPermissions(userId)
        assertEquals(permissions.size, userPermissions.size)
        assertTrue(userPermissions.containsAll(permissions))

        // 撤销权限
        securityManager.revokePermission(userId, "delete")
        assertFalse(securityManager.hasPermission(userId, "delete"))
        assertEquals(permissions.size - 1, securityManager.getUserPermissions(userId).size)
    }

    @Test
    fun testRoleBasedAccess() = runTest {
        val userId = "roleuser"
        val adminRole = "admin"
        val userRole = "user"

        // 分配角色
        securityManager.assignRole(userId, userRole)
        assertTrue(securityManager.hasRole(userId, userRole))

        // 角色权限检查
        val rolePermissions = securityManager.getRolePermissions(userRole)
        assertNotNull(rolePermissions)

        // 升级角色
        securityManager.assignRole(userId, adminRole)
        assertTrue(securityManager.hasRole(userId, adminRole))

        // 移除角色
        securityManager.removeRole(userId, userRole)
        assertFalse(securityManager.hasRole(userId, userRole))
    }

    // 威胁检测测试
    @Test
    fun testThreatDetection() = runTest {
        // 测试SQL注入检测
        val sqlInjectionAttempts = listOf(
            "'; DROP TABLE users; --",
            "1' OR '1'='1",
            "admin'--",
            "' UNION SELECT * FROM passwords--"
        )

        sqlInjectionAttempts.forEach { attempt ->
            val result = securityManager.detectThreat(attempt, "sql_injection")
            assertTrue(result.isThreatDetected, "Failed to detect SQL injection: $attempt")
            assertEquals("sql_injection", result.threatType)
        }

        // 测试XSS检测
        val xssAttempts = listOf(
            "<script>alert('xss')</script>",
            "javascript:alert('xss')",
            "<img src=x onerror=alert('xss')>",
            "<svg onload=alert('xss')>"
        )

        xssAttempts.forEach { attempt ->
            val result = securityManager.detectThreat(attempt, "xss")
            assertTrue(result.isThreatDetected, "Failed to detect XSS: $attempt")
            assertEquals("xss", result.threatType)
        }

        // 测试正常输入
        val normalInputs = listOf(
            "normal user input",
            "user@example.com",
            VALID_PASSWORD_PATTERN,
            "正常的中文输入"
        )

        normalInputs.forEach { input ->
            val result = securityManager.detectThreat(input, "general")
            assertFalse(result.isThreatDetected, "False positive for normal input: $input")
        }
    }

    @Test
    fun testBruteForceProtection() = runTest {
        val username = "bruteforceuser"
        val wrongPassword = "wrongpassword"
        val maxAttempts = 5

        // 模拟暴力破解尝试
        repeat(maxAttempts) {
            val result = securityManager.authenticate(username, wrongPassword, AuthenticationMethod.PASSWORD)
            assertFalse(result.isSuccess)
        }

        // 检查账户是否被锁定
        assertTrue(securityManager.isAccountLocked(username))

        // 尝试正确密码也应该失败
        val correctPassword = CORRECT_PASSWORD
        val result = securityManager.authenticate(username, correctPassword, AuthenticationMethod.PASSWORD)
        assertFalse(result.isSuccess)

        // 解锁账户
        securityManager.unlockAccount(username)
        assertFalse(securityManager.isAccountLocked(username))
    }

    // 安全审计测试
    @Test
    fun testSecurityAuditLogging() = runTest {
        val username = "audituser"
        val action = "login_attempt"
        val details = "User login from IP: $AUDIT_IP"

        // 记录安全事件
        securityManager.logSecurityEvent(username, action, details)

        // 获取审计日志
        val auditLogs = securityManager.getAuditLogs(username, limit = 10)
        assertNotNull(auditLogs)
        assertTrue(auditLogs.isNotEmpty())

        val latestLog = auditLogs.first()
        assertEquals(username, latestLog.username)
        assertEquals(action, latestLog.action)
        assertEquals(details, latestLog.details)
        assertTrue(latestLog.timestamp > 0)
    }

    @Test
    fun testSecurityPolicyEnforcement() = runTest {
        val policy = SecurityPolicy(
            minPasswordLength = 8,
            requireUppercase = true,
            requireLowercase = true,
            requireNumbers = true,
            requireSpecialChars = true,
            maxLoginAttempts = 3,
            sessionTimeout = SESSION_TIMEOUT_MS, // 1 hour
            requireMFA = true
        )

        securityManager.setSecurityPolicy(policy)

        // 测试密码策略
        assertFalse(securityManager.validatePassword("weak"))
        assertFalse(securityManager.validatePassword("NoNumbers!"))
        assertFalse(securityManager.validatePassword("nonumbers123"))
        assertTrue(securityManager.validatePassword(STRONG_PASSWORD))

        // 测试会话超时
        val session = securityManager.createSession("policyuser", STRONG_PASSWORD)
        assertTrue(session.isActive)

        // 模拟会话超时
        securityManager.simulateSessionTimeout(session.sessionId)
        assertFalse(securityManager.validateSession(session.sessionId))
    }

    // 数据完整性测试
    @Test
    fun testDataIntegrityVerification() = runTest {
        val originalData = "Important data that must not be tampered with"
        
        // 生成数据哈希
        val hash = securityManager.generateHash(originalData)
        assertNotNull(hash)
        assertTrue(hash.isNotEmpty())

        // 验证数据完整性
        assertTrue(securityManager.verifyIntegrity(originalData, hash))

        // 测试篡改检测
        val tamperedData = "Tampered data that has been modified"
        assertFalse(securityManager.verifyIntegrity(tamperedData, hash))
    }

    @Test
    fun testDigitalSignature() = runTest {
        val document = "Important document content"
        val privateKey = securityManager.generatePrivateKey()
        val publicKey = securityManager.getPublicKey(privateKey)

        // 生成数字签名
        val signature = securityManager.signDocument(document, privateKey)
        assertNotNull(signature)

        // 验证数字签名
        assertTrue(securityManager.verifySignature(document, signature, publicKey))

        // 测试篡改检测
        val tamperedDocument = "Tampered document content"
        assertFalse(securityManager.verifySignature(tamperedDocument, signature, publicKey))
    }

    // 性能和压力测试
    @Test
    fun testEncryptionPerformance() = runTest {
        val testData = "Performance test data ".repeat(PERFORMANCE_DATA_REPEAT) // ~20KB
        val key = securityManager.generateEncryptionKey(EncryptionType.AES_256)

        val startTime = System.currentTimeMillis()
        
        repeat(PERFORMANCE_TEST_ITERATIONS) {
            val encrypted = securityManager.encrypt(testData, key, EncryptionType.AES_256)
            val decrypted = securityManager.decrypt(encrypted, key, EncryptionType.AES_256)
            assertEquals(testData, decrypted)
        }

        val duration = System.currentTimeMillis() - startTime
        assertTrue(duration < MAX_ENCRYPTION_TIME_MS, "Encryption performance test took too long: ${duration}ms")
    }

    @Test
    fun testConcurrentAuthentication() = runTest {
        val userCount = 50
        val users = (1..userCount).map { "user$it" to "Password$it!" }

        val results = users.map { (username, password) ->
            kotlinx.coroutines.async {
                securityManager.authenticate(username, password, AuthenticationMethod.PASSWORD)
            }
        }.map { it.await() }

        // 验证所有认证都成功
        assertTrue(results.all { it.isSuccess })
        
        // 验证所有令牌都是唯一的
        val tokens = results.map { it.token }
        assertEquals(userCount, tokens.distinct().size)
    }

    // 配置和管理测试
    @Test
    fun testSecurityConfiguration() = runTest {
        val config = SecurityConfiguration(
            encryptionEnabled = true,
            auditLoggingEnabled = true,
            threatDetectionEnabled = true,
            bruteForceProtectionEnabled = true,
            sessionManagementEnabled = true,
            defaultSecurityLevel = SecurityLevel.HIGH
        )

        securityManager.updateConfiguration(config)

        val retrievedConfig = securityManager.getSecurityConfiguration()
        assertEquals(config.encryptionEnabled, retrievedConfig.encryptionEnabled)
        assertEquals(config.auditLoggingEnabled, retrievedConfig.auditLoggingEnabled)
        assertEquals(config.threatDetectionEnabled, retrievedConfig.threatDetectionEnabled)
        assertEquals(config.defaultSecurityLevel, retrievedConfig.defaultSecurityLevel)
    }

    @Test
    fun testSecurityMetrics() = runTest {
        // 执行一些安全操作
        securityManager.authenticate("metricsuser", TEST_PASSWORD, AuthenticationMethod.PASSWORD)
        securityManager.encrypt("test data", securityManager.generateEncryptionKey(EncryptionType.AES_256), EncryptionType.AES_256)
        securityManager.detectThreat("normal input", "general")

        // 获取安全指标
        val metrics = securityManager.getSecurityMetrics()
        assertNotNull(metrics)
        assertTrue(metrics.authenticationAttempts > 0)
        assertTrue(metrics.encryptionOperations > 0)
        assertTrue(metrics.threatDetectionScans > 0)
    }

    // 错误处理和边界条件测试
    @Test
    fun testErrorHandling() = runTest {
        // 测试空输入处理
        assertFailsWith<IllegalArgumentException> {
            securityManager.encrypt("", "", EncryptionType.AES_256)
        }

        assertFailsWith<IllegalArgumentException> {
            securityManager.authenticate("", "", AuthenticationMethod.PASSWORD)
        }

        // 测试无效令牌处理
        assertFalse(securityManager.validateToken("invalid_token"))
        assertFalse(securityManager.validateSession("invalid_session"))

        // 测试权限检查边界条件
        assertFalse(securityManager.hasPermission("nonexistent_user", "any_permission"))
    }

    @Test
    fun testMemoryLeakPrevention() = runTest {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        // 执行大量安全操作
        repeat(MEMORY_TEST_ITERATIONS) {
            val key = securityManager.generateEncryptionKey(EncryptionType.AES_256)
            val encrypted = securityManager.encrypt("test data $it", key, EncryptionType.AES_256)
            securityManager.decrypt(encrypted, key, EncryptionType.AES_256)
            
            securityManager.authenticate("user$it", "password$it", AuthenticationMethod.PASSWORD)
            securityManager.detectThreat("input $it", "general")
        }

        // 强制垃圾回收
        System.gc()
        kotlinx.coroutines.delay(GC_DELAY_MS)

        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory

        // 内存增长应该控制在合理范围内
        assertTrue(memoryIncrease < MAX_MEMORY_INCREASE_MB * 1024 * 1024, "Memory leak detected: ${memoryIncrease / 1024 / 1024}MB increase")
    }
}
