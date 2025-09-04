package com.unify.core.dynamic

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.collections.mutableListOf
import kotlin.collections.mutableSetOf

/**
 * 安全验证结果
 */
@Serializable
data class SecurityValidationResult(
    val isValid: Boolean,
    val securityLevel: SecurityLevel,
    val violations: List<SecurityViolation> = emptyList(),
    val warnings: List<String> = emptyList(),
    val reason: String = "",
    val checksum: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class SecurityLevel {
    SAFE,        // 安全
    LOW_RISK,    // 低风险
    MEDIUM_RISK, // 中等风险
    HIGH_RISK,   // 高风险
    DANGEROUS    // 危险
}

@Serializable
data class SecurityViolation(
    val type: ViolationType,
    val severity: ViolationSeverity,
    val description: String,
    val location: String = "",
    val suggestion: String = ""
)

@Serializable
enum class ViolationType {
    INVALID_SIGNATURE,     // 无效签名
    UNTRUSTED_SOURCE,      // 不可信来源
    MALICIOUS_CODE,        // 恶意代码
    PERMISSION_ABUSE,      // 权限滥用
    DATA_LEAK,            // 数据泄露
    UNSAFE_OPERATION,     // 不安全操作
    DEPRECATED_API,       // 废弃API
    SECURITY_BYPASS,      // 安全绕过
    INJECTION_ATTACK,     // 注入攻击
    BUFFER_OVERFLOW       // 缓冲区溢出
}

@Serializable
enum class ViolationSeverity {
    INFO,     // 信息
    WARNING,  // 警告
    ERROR,    // 错误
    CRITICAL  // 严重
}

@Serializable
data class SecurityPolicy(
    val allowUnsignedComponents: Boolean = false,
    val trustedSources: Set<String> = emptySet(),
    val blockedSources: Set<String> = emptySet(),
    val maxComponentSize: Long = 50 * 1024 * 1024, // 50MB
    val allowedPermissions: Set<String> = emptySet(),
    val blockedPermissions: Set<String> = emptySet(),
    val enableCodeAnalysis: Boolean = true,
    val enableRuntimeMonitoring: Boolean = true,
    val quarantineMode: Boolean = false
)

@Serializable
data class TrustedCertificate(
    val id: String,
    val name: String,
    val publicKey: String,
    val issuer: String,
    val validFrom: Long,
    val validTo: Long,
    val fingerprint: String
)

/**
 * 热更新安全验证器接口
 */
interface HotUpdateSecurityValidator {
    // 组件验证
    suspend fun validateComponent(component: DynamicComponent): SecurityValidationResult
    suspend fun validateComponentBatch(components: List<DynamicComponent>): List<SecurityValidationResult>
    
    // 签名验证
    suspend fun verifySignature(component: DynamicComponent): Boolean
    suspend fun verifyChecksum(component: DynamicComponent): Boolean
    
    // 代码分析
    suspend fun analyzeCode(content: String, type: DynamicComponentType): List<SecurityViolation>
    suspend fun scanForMaliciousPatterns(content: String): List<SecurityViolation>
    
    // 权限检查
    suspend fun checkPermissions(component: DynamicComponent): List<SecurityViolation>
    suspend fun validateSourceTrust(source: String): Boolean
    
    // 安全策略
    fun updateSecurityPolicy(policy: SecurityPolicy)
    fun getSecurityPolicy(): SecurityPolicy
    
    // 证书管理
    suspend fun addTrustedCertificate(certificate: TrustedCertificate): Boolean
    suspend fun removeTrustedCertificate(certificateId: String): Boolean
    suspend fun getTrustedCertificates(): List<TrustedCertificate>
    
    // 安全监控
    suspend fun startRuntimeMonitoring(componentId: String)
    suspend fun stopRuntimeMonitoring(componentId: String)
    suspend fun getSecurityReport(componentId: String): SecurityValidationResult?
    
    // 隔离和恢复
    suspend fun quarantineComponent(componentId: String): Boolean
    suspend fun releaseFromQuarantine(componentId: String): Boolean
    suspend fun isComponentQuarantined(componentId: String): Boolean
}

/**
 * 热更新安全验证器实现
 */
class HotUpdateSecurityValidatorImpl(
    private val storageManager: DynamicStorageManager
) : HotUpdateSecurityValidator {
    
    private var securityPolicy = SecurityPolicy()
    private val trustedCertificates = mutableMapOf<String, TrustedCertificate>()
    private val quarantinedComponents = mutableSetOf<String>()
    private val runtimeMonitors = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val POLICY_STORAGE_KEY = "security_policy"
        private const val CERTIFICATES_STORAGE_KEY = "trusted_certificates"
        private const val QUARANTINE_STORAGE_KEY = "quarantined_components"
        
        // 恶意代码模式
        private val MALICIOUS_PATTERNS = listOf(
            "eval\\s*\\(",
            "Function\\s*\\(",
            "setTimeout\\s*\\(",
            "setInterval\\s*\\(",
            "document\\.write",
            "innerHTML\\s*=",
            "outerHTML\\s*=",
            "execCommand",
            "crypto\\.subtle",
            "localStorage\\.",
            "sessionStorage\\.",
            "indexedDB\\.",
            "XMLHttpRequest",
            "fetch\\s*\\(",
            "import\\s*\\(",
            "require\\s*\\(",
            "process\\.",
            "global\\.",
            "window\\.",
            "__proto__",
            "constructor\\.",
            "prototype\\."
        )
        
        // 危险权限
        private val DANGEROUS_PERMISSIONS = setOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.CALL_PHONE",
            "android.permission.READ_PHONE_STATE"
        )
    }
    
    init {
        // 加载安全配置
        scope.launch {
            loadSecurityConfiguration()
        }
    }
    
    override suspend fun validateComponent(component: DynamicComponent): SecurityValidationResult {
        val violations = mutableListOf<SecurityViolation>()
        val warnings = mutableListOf<String>()
        
        try {
            // 1. 签名验证
            if (securityPolicy.allowUnsignedComponents) {
                if (component.signature.isNotEmpty() && !verifySignature(component)) {
                    violations.add(SecurityViolation(
                        type = ViolationType.INVALID_SIGNATURE,
                        severity = ViolationSeverity.CRITICAL,
                        description = "组件签名验证失败",
                        suggestion = "请使用有效的数字签名"
                    ))
                }
            } else {
                if (component.signature.isEmpty()) {
                    violations.add(SecurityViolation(
                        type = ViolationType.INVALID_SIGNATURE,
                        severity = ViolationSeverity.CRITICAL,
                        description = "组件缺少数字签名",
                        suggestion = "所有组件必须包含有效的数字签名"
                    ))
                } else if (!verifySignature(component)) {
                    violations.add(SecurityViolation(
                        type = ViolationType.INVALID_SIGNATURE,
                        severity = ViolationSeverity.CRITICAL,
                        description = "组件签名验证失败",
                        suggestion = "请使用有效的数字签名"
                    ))
                }
            }
            
            // 2. 校验和验证
            if (component.checksum.isNotEmpty() && !verifyChecksum(component)) {
                violations.add(SecurityViolation(
                    type = ViolationType.MALICIOUS_CODE,
                    severity = ViolationSeverity.ERROR,
                    description = "组件校验和不匹配",
                    suggestion = "组件可能已被篡改"
                ))
            }
            
            // 3. 来源验证
            val source = component.metadata["source"] ?: ""
            if (!validateSourceTrust(source)) {
                violations.add(SecurityViolation(
                    type = ViolationType.UNTRUSTED_SOURCE,
                    severity = ViolationSeverity.WARNING,
                    description = "组件来源不可信: $source",
                    suggestion = "请从可信来源获取组件"
                ))
            }
            
            // 4. 大小检查
            if (component.content.length > securityPolicy.maxComponentSize) {
                violations.add(SecurityViolation(
                    type = ViolationType.UNSAFE_OPERATION,
                    severity = ViolationSeverity.WARNING,
                    description = "组件大小超过限制",
                    suggestion = "减小组件大小或调整安全策略"
                ))
            }
            
            // 5. 权限检查
            val permissionViolations = checkPermissions(component)
            violations.addAll(permissionViolations)
            
            // 6. 代码分析
            if (securityPolicy.enableCodeAnalysis && component.content.isNotEmpty()) {
                val codeViolations = analyzeCode(component.content, component.type)
                violations.addAll(codeViolations)
                
                val maliciousViolations = scanForMaliciousPatterns(component.content)
                violations.addAll(maliciousViolations)
            }
            
            // 7. 依赖检查
            component.dependencies.forEach { depId ->
                if (isComponentQuarantined(depId)) {
                    violations.add(SecurityViolation(
                        type = ViolationType.UNTRUSTED_SOURCE,
                        severity = ViolationSeverity.ERROR,
                        description = "依赖组件 $depId 已被隔离",
                        suggestion = "移除对隔离组件的依赖"
                    ))
                }
            }
            
            // 计算安全级别
            val securityLevel = calculateSecurityLevel(violations)
            
            // 生成结果
            return SecurityValidationResult(
                isValid = violations.none { it.severity == ViolationSeverity.CRITICAL },
                securityLevel = securityLevel,
                violations = violations,
                warnings = warnings,
                reason = if (violations.isNotEmpty()) "发现 ${violations.size} 个安全问题" else "安全验证通过",
                checksum = generateResultChecksum(component, violations)
            )
            
        } catch (e: Exception) {
            return SecurityValidationResult(
                isValid = false,
                securityLevel = SecurityLevel.DANGEROUS,
                reason = "安全验证异常: ${e.message}",
                violations = listOf(SecurityViolation(
                    type = ViolationType.UNSAFE_OPERATION,
                    severity = ViolationSeverity.CRITICAL,
                    description = "安全验证过程中发生异常",
                    suggestion = "请检查组件格式和内容"
                ))
            )
        }
    }
    
    override suspend fun validateComponentBatch(components: List<DynamicComponent>): List<SecurityValidationResult> {
        return components.map { component ->
            validateComponent(component)
        }
    }
    
    override suspend fun verifySignature(component: DynamicComponent): Boolean {
        if (component.signature.isEmpty()) return false
        
        return try {
            // 简化的签名验证实现
            // 实际实现需要使用加密库进行数字签名验证
            val expectedSignature = generateComponentSignature(component)
            component.signature == expectedSignature
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun verifyChecksum(component: DynamicComponent): Boolean {
        if (component.checksum.isEmpty()) return false
        
        return try {
            val calculatedChecksum = calculateChecksum(component.content)
            component.checksum == calculatedChecksum
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun analyzeCode(content: String, type: DynamicComponentType): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        when (type) {
            DynamicComponentType.COMPOSE_UI -> {
                // 分析Compose代码
                violations.addAll(analyzeComposeCode(content))
            }
            DynamicComponentType.NATIVE_MODULE -> {
                // 分析原生模块
                violations.addAll(analyzeNativeCode(content))
            }
            DynamicComponentType.BUSINESS_LOGIC -> {
                // 分析业务逻辑
                violations.addAll(analyzeBusinessLogic(content))
            }
            else -> {
                // 通用代码分析
                violations.addAll(analyzeGenericCode(content))
            }
        }
        
        return violations
    }
    
    override suspend fun scanForMaliciousPatterns(content: String): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        MALICIOUS_PATTERNS.forEach { pattern ->
            try {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                val matches = regex.findAll(content)
                
                matches.forEach { match ->
                    violations.add(SecurityViolation(
                        type = ViolationType.MALICIOUS_CODE,
                        severity = ViolationSeverity.ERROR,
                        description = "检测到可疑代码模式: ${match.value}",
                        location = "位置: ${match.range}",
                        suggestion = "移除或替换可疑代码"
                    ))
                }
            } catch (e: Exception) {
                // 忽略正则表达式错误
            }
        }
        
        return violations
    }
    
    override suspend fun checkPermissions(component: DynamicComponent): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        // 检查组件请求的权限
        val requestedPermissions = component.metadata["permissions"]?.split(",") ?: emptyList()
        
        requestedPermissions.forEach { permission ->
            val trimmedPermission = permission.trim()
            
            // 检查是否为危险权限
            if (DANGEROUS_PERMISSIONS.contains(trimmedPermission)) {
                violations.add(SecurityViolation(
                    type = ViolationType.PERMISSION_ABUSE,
                    severity = ViolationSeverity.WARNING,
                    description = "请求危险权限: $trimmedPermission",
                    suggestion = "确认是否真的需要此权限"
                ))
            }
            
            // 检查是否为被阻止的权限
            if (securityPolicy.blockedPermissions.contains(trimmedPermission)) {
                violations.add(SecurityViolation(
                    type = ViolationType.PERMISSION_ABUSE,
                    severity = ViolationSeverity.ERROR,
                    description = "请求被阻止的权限: $trimmedPermission",
                    suggestion = "移除对此权限的请求"
                ))
            }
            
            // 检查是否在允许列表中
            if (securityPolicy.allowedPermissions.isNotEmpty() && 
                !securityPolicy.allowedPermissions.contains(trimmedPermission)) {
                violations.add(SecurityViolation(
                    type = ViolationType.PERMISSION_ABUSE,
                    severity = ViolationSeverity.WARNING,
                    description = "请求未授权的权限: $trimmedPermission",
                    suggestion = "只请求必要的权限"
                ))
            }
        }
        
        return violations
    }
    
    override suspend fun validateSourceTrust(source: String): Boolean {
        if (source.isEmpty()) return false
        
        // 检查是否在阻止列表中
        if (securityPolicy.blockedSources.contains(source)) {
            return false
        }
        
        // 检查是否在信任列表中
        if (securityPolicy.trustedSources.isNotEmpty()) {
            return securityPolicy.trustedSources.contains(source)
        }
        
        // 默认允许
        return true
    }
    
    override fun updateSecurityPolicy(policy: SecurityPolicy) {
        securityPolicy = policy
        saveSecurityConfiguration()
    }
    
    override fun getSecurityPolicy(): SecurityPolicy = securityPolicy
    
    override suspend fun addTrustedCertificate(certificate: TrustedCertificate): Boolean {
        return try {
            trustedCertificates[certificate.id] = certificate
            saveTrustedCertificates()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun removeTrustedCertificate(certificateId: String): Boolean {
        return try {
            trustedCertificates.remove(certificateId)
            saveTrustedCertificates()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getTrustedCertificates(): List<TrustedCertificate> {
        return trustedCertificates.values.toList()
    }
    
    override suspend fun startRuntimeMonitoring(componentId: String) {
        if (runtimeMonitors.containsKey(componentId)) return
        
        val monitorJob = scope.launch {
            while (isActive) {
                try {
                    // 执行运行时监控
                    performRuntimeCheck(componentId)
                    delay(5000) // 每5秒检查一次
                } catch (e: Exception) {
                    // 忽略监控错误
                }
            }
        }
        
        runtimeMonitors[componentId] = monitorJob
    }
    
    override suspend fun stopRuntimeMonitoring(componentId: String) {
        runtimeMonitors[componentId]?.cancel()
        runtimeMonitors.remove(componentId)
    }
    
    override suspend fun getSecurityReport(componentId: String): SecurityValidationResult? {
        // 生成组件的安全报告
        return null // 简化实现
    }
    
    override suspend fun quarantineComponent(componentId: String): Boolean {
        return try {
            quarantinedComponents.add(componentId)
            saveQuarantineList()
            
            // 停止运行时监控
            stopRuntimeMonitoring(componentId)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun releaseFromQuarantine(componentId: String): Boolean {
        return try {
            quarantinedComponents.remove(componentId)
            saveQuarantineList()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun isComponentQuarantined(componentId: String): Boolean {
        return quarantinedComponents.contains(componentId)
    }
    
    // 私有辅助方法
    private fun calculateSecurityLevel(violations: List<SecurityViolation>): SecurityLevel {
        val criticalCount = violations.count { it.severity == ViolationSeverity.CRITICAL }
        val errorCount = violations.count { it.severity == ViolationSeverity.ERROR }
        val warningCount = violations.count { it.severity == ViolationSeverity.WARNING }
        
        return when {
            criticalCount > 0 -> SecurityLevel.DANGEROUS
            errorCount > 2 -> SecurityLevel.HIGH_RISK
            errorCount > 0 -> SecurityLevel.MEDIUM_RISK
            warningCount > 3 -> SecurityLevel.LOW_RISK
            else -> SecurityLevel.SAFE
        }
    }
    
    private fun generateComponentSignature(component: DynamicComponent): String {
        // 简化的签名生成实现
        val content = "${component.id}${component.version}${component.content}"
        return calculateChecksum(content)
    }
    
    private fun calculateChecksum(content: String): String {
        // 简化的校验和计算实现
        return content.hashCode().toString()
    }
    
    private fun generateResultChecksum(component: DynamicComponent, violations: List<SecurityViolation>): String {
        val content = "${component.id}${violations.size}${System.currentTimeMillis()}"
        return calculateChecksum(content)
    }
    
    private fun analyzeComposeCode(content: String): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        // 检查不安全的Compose模式
        if (content.contains("LaunchedEffect") && content.contains("while(true)")) {
            violations.add(SecurityViolation(
                type = ViolationType.UNSAFE_OPERATION,
                severity = ViolationSeverity.WARNING,
                description = "检测到可能的无限循环",
                suggestion = "避免在LaunchedEffect中使用无限循环"
            ))
        }
        
        return violations
    }
    
    private fun analyzeNativeCode(content: String): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        // 检查原生代码中的不安全操作
        if (content.contains("System.loadLibrary") || content.contains("System.load")) {
            violations.add(SecurityViolation(
                type = ViolationType.UNSAFE_OPERATION,
                severity = ViolationSeverity.ERROR,
                description = "检测到动态库加载操作",
                suggestion = "避免动态加载未验证的库文件"
            ))
        }
        
        return violations
    }
    
    private fun analyzeBusinessLogic(content: String): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        // 检查业务逻辑中的安全问题
        if (content.contains("password") && content.contains("=")) {
            violations.add(SecurityViolation(
                type = ViolationType.DATA_LEAK,
                severity = ViolationSeverity.WARNING,
                description = "可能存在密码硬编码",
                suggestion = "避免在代码中硬编码敏感信息"
            ))
        }
        
        return violations
    }
    
    private fun analyzeGenericCode(content: String): List<SecurityViolation> {
        val violations = mutableListOf<SecurityViolation>()
        
        // 通用代码安全检查
        if (content.contains("TODO") || content.contains("FIXME")) {
            violations.add(SecurityViolation(
                type = ViolationType.UNSAFE_OPERATION,
                severity = ViolationSeverity.INFO,
                description = "代码包含待完成标记",
                suggestion = "完成所有待办事项后再发布"
            ))
        }
        
        return violations
    }
    
    private suspend fun performRuntimeCheck(componentId: String) {
        // 执行运行时安全检查
        // 简化实现
    }
    
    private suspend fun loadSecurityConfiguration() {
        try {
            // 加载安全策略
            val policyJson = storageManager.getConfig(POLICY_STORAGE_KEY)
            if (policyJson != null) {
                securityPolicy = Json.decodeFromString(policyJson)
            }
            
            // 加载信任证书
            val certificatesJson = storageManager.getConfig(CERTIFICATES_STORAGE_KEY)
            if (certificatesJson != null) {
                val certificates: List<TrustedCertificate> = Json.decodeFromString(certificatesJson)
                certificates.forEach { cert ->
                    trustedCertificates[cert.id] = cert
                }
            }
            
            // 加载隔离列表
            val quarantineJson = storageManager.getConfig(QUARANTINE_STORAGE_KEY)
            if (quarantineJson != null) {
                val quarantineList: List<String> = Json.decodeFromString(quarantineJson)
                quarantinedComponents.addAll(quarantineList)
            }
        } catch (e: Exception) {
            // 使用默认配置
        }
    }
    
    private fun saveSecurityConfiguration() {
        scope.launch {
            try {
                val policyJson = Json.encodeToString(securityPolicy)
                storageManager.saveConfig(POLICY_STORAGE_KEY, policyJson)
            } catch (e: Exception) {
                // 忽略保存错误
            }
        }
    }
    
    private fun saveTrustedCertificates() {
        scope.launch {
            try {
                val certificates = trustedCertificates.values.toList()
                val certificatesJson = Json.encodeToString(certificates)
                storageManager.saveConfig(CERTIFICATES_STORAGE_KEY, certificatesJson)
            } catch (e: Exception) {
                // 忽略保存错误
            }
        }
    }
    
    private fun saveQuarantineList() {
        scope.launch {
            try {
                val quarantineList = quarantinedComponents.toList()
                val quarantineJson = Json.encodeToString(quarantineList)
                storageManager.saveConfig(QUARANTINE_STORAGE_KEY, quarantineJson)
            } catch (e: Exception) {
                // 忽略保存错误
            }
        }
    }
}
