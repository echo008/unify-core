package com.unify.core.security

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Unify安全管理器
 * 提供跨平台的安全功能，包括加密、认证、权限管理等
 */
expect class UnifySecurityManager {
    /**
     * 初始化安全管理器
     */
    suspend fun initialize(config: SecurityConfig): SecurityInitResult
    
    /**
     * 加密数据
     */
    suspend fun encrypt(data: String, key: String? = null): EncryptionResult
    
    /**
     * 解密数据
     */
    suspend fun decrypt(encryptedData: String, key: String? = null): DecryptionResult
    
    /**
     * 生成安全密钥
     */
    suspend fun generateKey(keyType: KeyType = KeyType.AES256): KeyGenerationResult
    
    /**
     * 哈希数据
     */
    suspend fun hash(data: String, algorithm: HashAlgorithm = HashAlgorithm.SHA256): HashResult
    
    /**
     * 验证哈希
     */
    suspend fun verifyHash(data: String, hash: String, algorithm: HashAlgorithm = HashAlgorithm.SHA256): Boolean
    
    /**
     * 生成数字签名
     */
    suspend fun sign(data: String, privateKey: String): SignatureResult
    
    /**
     * 验证数字签名
     */
    suspend fun verifySignature(data: String, signature: String, publicKey: String): Boolean
    
    /**
     * 生成随机数
     */
    suspend fun generateSecureRandom(length: Int = 32): String
    
    /**
     * 生成UUID
     */
    suspend fun generateUUID(): String
    
    /**
     * 密码强度检查
     */
    fun checkPasswordStrength(password: String): PasswordStrengthResult
    
    /**
     * 生成安全密码
     */
    suspend fun generateSecurePassword(
        length: Int = 12,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String
    
    /**
     * 获取设备指纹
     */
    suspend fun getDeviceFingerprint(): DeviceFingerprintResult
    
    /**
     * 检查应用完整性
     */
    suspend fun checkAppIntegrity(): AppIntegrityResult
    
    /**
     * 检测Root/越狱
     */
    suspend fun detectRootJailbreak(): RootJailbreakResult
    
    /**
     * 安全存储数据
     */
    suspend fun secureStore(key: String, value: String): SecureStorageResult
    
    /**
     * 安全读取数据
     */
    suspend fun secureRetrieve(key: String): SecureRetrievalResult
    
    /**
     * 删除安全存储的数据
     */
    suspend fun secureDelete(key: String): Boolean
    
    /**
     * 清除所有安全存储
     */
    suspend fun secureClear(): Boolean
    
    /**
     * 生物识别认证
     */
    suspend fun authenticateWithBiometric(
        title: String = "生物识别认证",
        subtitle: String = "请使用指纹或面部识别进行认证"
    ): BiometricAuthResult
    
    /**
     * 检查生物识别可用性
     */
    suspend fun isBiometricAvailable(): BiometricAvailabilityResult
    
    /**
     * 网络安全检查
     */
    suspend fun checkNetworkSecurity(url: String): NetworkSecurityResult
    
    /**
     * SSL证书验证
     */
    suspend fun validateSSLCertificate(url: String): SSLValidationResult
    
    /**
     * 获取安全事件流
     */
    fun getSecurityEvents(): Flow<SecurityEvent>
    
    /**
     * 报告安全事件
     */
    suspend fun reportSecurityEvent(event: SecurityEvent)
    
    /**
     * 获取安全状态
     */
    fun getSecurityStatus(): StateFlow<SecurityStatus>
    
    /**
     * 执行安全扫描
     */
    suspend fun performSecurityScan(): SecurityScanResult
}

/**
 * 安全配置
 */
@Serializable
data class SecurityConfig(
    val enableEncryption: Boolean = true,
    val encryptionAlgorithm: EncryptionAlgorithm = EncryptionAlgorithm.AES256,
    val keyDerivationIterations: Int = 10000,
    val enableBiometric: Boolean = true,
    val enableRootDetection: Boolean = true,
    val enableAppIntegrityCheck: Boolean = true,
    val enableNetworkSecurityCheck: Boolean = true,
    val secureStorageEnabled: Boolean = true,
    val logSecurityEvents: Boolean = true,
    val autoLockTimeout: Long = 300000, // 5分钟
    val maxFailedAttempts: Int = 5
)

/**
 * 加密算法
 */
enum class EncryptionAlgorithm {
    AES128,
    AES192,
    AES256,
    RSA2048,
    RSA4096,
    CHACHA20_POLY1305
}

/**
 * 密钥类型
 */
enum class KeyType {
    AES128,
    AES192,
    AES256,
    RSA2048,
    RSA4096,
    ECDSA_P256,
    ECDSA_P384,
    ECDSA_P521
}

/**
 * 哈希算法
 */
enum class HashAlgorithm {
    MD5,
    SHA1,
    SHA256,
    SHA384,
    SHA512,
    BLAKE2B,
    ARGON2
}

/**
 * 安全初始化结果
 */
sealed class SecurityInitResult {
    object Success : SecurityInitResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : SecurityInitResult()
}

/**
 * 加密结果
 */
sealed class EncryptionResult {
    data class Success(val encryptedData: String, val iv: String? = null) : EncryptionResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : EncryptionResult()
}

/**
 * 解密结果
 */
sealed class DecryptionResult {
    data class Success(val decryptedData: String) : DecryptionResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : DecryptionResult()
}

/**
 * 密钥生成结果
 */
sealed class KeyGenerationResult {
    data class Success(val key: String, val publicKey: String? = null) : KeyGenerationResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : KeyGenerationResult()
}

/**
 * 哈希结果
 */
sealed class HashResult {
    data class Success(val hash: String, val salt: String? = null) : HashResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : HashResult()
}

/**
 * 签名结果
 */
sealed class SignatureResult {
    data class Success(val signature: String) : SignatureResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : SignatureResult()
}

/**
 * 密码强度结果
 */
@Serializable
data class PasswordStrengthResult(
    val score: Int, // 0-100
    val strength: PasswordStrength,
    val feedback: List<String>,
    val estimatedCrackTime: String,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasNumbers: Boolean,
    val hasSymbols: Boolean,
    val length: Int,
    val commonPassword: Boolean
)

/**
 * 密码强度等级
 */
enum class PasswordStrength {
    VERY_WEAK,
    WEAK,
    FAIR,
    GOOD,
    STRONG,
    VERY_STRONG
}

/**
 * 设备指纹结果
 */
sealed class DeviceFingerprintResult {
    data class Success(val fingerprint: DeviceFingerprint) : DeviceFingerprintResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : DeviceFingerprintResult()
}

/**
 * 设备指纹
 */
@Serializable
data class DeviceFingerprint(
    val deviceId: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String,
    val screenResolution: String,
    val timezone: String,
    val language: String,
    val hardwareInfo: String,
    val installedApps: List<String> = emptyList(),
    val networkInfo: String,
    val timestamp: Long
)

/**
 * 应用完整性结果
 */
sealed class AppIntegrityResult {
    data class Success(val isIntact: Boolean, val details: AppIntegrityDetails) : AppIntegrityResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : AppIntegrityResult()
}

/**
 * 应用完整性详情
 */
@Serializable
data class AppIntegrityDetails(
    val signatureValid: Boolean,
    val checksumValid: Boolean,
    val debuggingDetected: Boolean,
    val tamperingDetected: Boolean,
    val installerPackage: String?,
    val installSource: String?
)

/**
 * Root/越狱检测结果
 */
sealed class RootJailbreakResult {
    data class Success(val isRootedJailbroken: Boolean, val details: RootJailbreakDetails) : RootJailbreakResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : RootJailbreakResult()
}

/**
 * Root/越狱详情
 */
@Serializable
data class RootJailbreakDetails(
    val rootDetected: Boolean,
    val jailbreakDetected: Boolean,
    val suspiciousApps: List<String>,
    val suspiciousFiles: List<String>,
    val suspiciousBehavior: List<String>
)

/**
 * 安全存储结果
 */
sealed class SecureStorageResult {
    object Success : SecureStorageResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : SecureStorageResult()
}

/**
 * 安全检索结果
 */
sealed class SecureRetrievalResult {
    data class Success(val value: String) : SecureRetrievalResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : SecureRetrievalResult()
    object NotFound : SecureRetrievalResult()
}

/**
 * 生物识别认证结果
 */
sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    object UserCancel : BiometricAuthResult()
    object AuthenticationFailed : BiometricAuthResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : BiometricAuthResult()
}

/**
 * 生物识别可用性结果
 */
sealed class BiometricAvailabilityResult {
    object Available : BiometricAvailabilityResult()
    object NotAvailable : BiometricAvailabilityResult()
    object NotEnrolled : BiometricAvailabilityResult()
    object HardwareNotAvailable : BiometricAvailabilityResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : BiometricAvailabilityResult()
}

/**
 * 网络安全结果
 */
sealed class NetworkSecurityResult {
    data class Success(val isSecure: Boolean, val details: NetworkSecurityDetails) : NetworkSecurityResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : NetworkSecurityResult()
}

/**
 * 网络安全详情
 */
@Serializable
data class NetworkSecurityDetails(
    val httpsEnabled: Boolean,
    val certificateValid: Boolean,
    val tlsVersion: String?,
    val cipherSuite: String?,
    val certificateChain: List<String>,
    val pinningEnabled: Boolean,
    val mitm: Boolean
)

/**
 * SSL验证结果
 */
sealed class SSLValidationResult {
    data class Success(val isValid: Boolean, val certificate: SSLCertificateInfo) : SSLValidationResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : SSLValidationResult()
}

/**
 * SSL证书信息
 */
@Serializable
data class SSLCertificateInfo(
    val subject: String,
    val issuer: String,
    val serialNumber: String,
    val notBefore: Long,
    val notAfter: Long,
    val fingerprint: String,
    val algorithm: String,
    val keySize: Int
)

/**
 * 安全事件
 */
@Serializable
data class SecurityEvent(
    val id: String,
    val type: SecurityEventType,
    val severity: SecurityEventSeverity,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val timestamp: Long,
    val source: String
)

/**
 * 安全事件类型
 */
enum class SecurityEventType {
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAILURE,
    ENCRYPTION_SUCCESS,
    ENCRYPTION_FAILURE,
    DECRYPTION_SUCCESS,
    DECRYPTION_FAILURE,
    KEY_GENERATION,
    BIOMETRIC_AUTH_SUCCESS,
    BIOMETRIC_AUTH_FAILURE,
    ROOT_JAILBREAK_DETECTED,
    APP_TAMPERING_DETECTED,
    NETWORK_SECURITY_VIOLATION,
    SUSPICIOUS_ACTIVITY,
    DATA_BREACH_ATTEMPT,
    UNAUTHORIZED_ACCESS_ATTEMPT
}

/**
 * 安全事件严重程度
 */
enum class SecurityEventSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 安全状态
 */
@Serializable
data class SecurityStatus(
    val isSecure: Boolean,
    val threatLevel: ThreatLevel,
    val activeThreats: List<SecurityThreat>,
    val lastScanTime: Long,
    val securityScore: Int, // 0-100
    val recommendations: List<String>
)

/**
 * 威胁等级
 */
enum class ThreatLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 安全威胁
 */
@Serializable
data class SecurityThreat(
    val id: String,
    val type: ThreatType,
    val severity: SecurityEventSeverity,
    val description: String,
    val detectedAt: Long,
    val mitigated: Boolean
)

/**
 * 威胁类型
 */
enum class ThreatType {
    MALWARE,
    PHISHING,
    MAN_IN_THE_MIDDLE,
    DATA_LEAKAGE,
    UNAUTHORIZED_ACCESS,
    TAMPERING,
    ROOT_JAILBREAK,
    NETWORK_ATTACK,
    SOCIAL_ENGINEERING
}

/**
 * 安全扫描结果
 */
@Serializable
data class SecurityScanResult(
    val scanId: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val threatsFound: Int,
    val vulnerabilitiesFound: Int,
    val securityScore: Int,
    val threats: List<SecurityThreat>,
    val vulnerabilities: List<SecurityVulnerability>,
    val recommendations: List<SecurityRecommendation>
)

/**
 * 安全漏洞
 */
@Serializable
data class SecurityVulnerability(
    val id: String,
    val type: VulnerabilityType,
    val severity: SecurityEventSeverity,
    val description: String,
    val location: String,
    val cveId: String? = null,
    val cvssScore: Double? = null,
    val fixAvailable: Boolean,
    val fixDescription: String? = null
)

/**
 * 漏洞类型
 */
enum class VulnerabilityType {
    INJECTION,
    BROKEN_AUTHENTICATION,
    SENSITIVE_DATA_EXPOSURE,
    XML_EXTERNAL_ENTITIES,
    BROKEN_ACCESS_CONTROL,
    SECURITY_MISCONFIGURATION,
    CROSS_SITE_SCRIPTING,
    INSECURE_DESERIALIZATION,
    VULNERABLE_COMPONENTS,
    INSUFFICIENT_LOGGING
}

/**
 * 安全建议
 */
@Serializable
data class SecurityRecommendation(
    val id: String,
    val priority: RecommendationPriority,
    val category: String,
    val title: String,
    val description: String,
    val actionRequired: String,
    val estimatedEffort: String
)

/**
 * 建议优先级
 */
enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 安全错误代码
 */
enum class SecurityErrorCode {
    INITIALIZATION_FAILED,
    ENCRYPTION_FAILED,
    DECRYPTION_FAILED,
    KEY_GENERATION_FAILED,
    HASH_FAILED,
    SIGNATURE_FAILED,
    VERIFICATION_FAILED,
    BIOMETRIC_NOT_AVAILABLE,
    BIOMETRIC_AUTH_FAILED,
    SECURE_STORAGE_FAILED,
    NETWORK_SECURITY_CHECK_FAILED,
    APP_INTEGRITY_CHECK_FAILED,
    ROOT_DETECTION_FAILED,
    INVALID_PARAMETERS,
    PERMISSION_DENIED,
    HARDWARE_NOT_SUPPORTED,
    SERVICE_UNAVAILABLE,
    TIMEOUT,
    UNKNOWN_ERROR
}

/**
 * 安全管理器工厂
 */
object UnifySecurityManagerFactory {
    private var instance: UnifySecurityManager? = null
    
    fun getInstance(): UnifySecurityManager {
        return instance ?: createSecurityManager().also { instance = it }
    }
    
    private fun createSecurityManager(): UnifySecurityManager {
        return UnifySecurityManager()
    }
    
    fun reset() {
        instance = null
    }
}
