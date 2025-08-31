package com.unify.core.dynamic

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 热更新安全验证器
 * 负责验证更新包的安全性、完整性和合法性
 */
class HotUpdateSecurityValidator {
    private var publicKey: String = ""
    private var allowedDomains: List<String> = emptyList()
    private var isInitialized = false
    
    companion object {
        private const val RSA_KEY_SIZE = 2048
        private const val AES_KEY_SIZE = 256
    }
    
    private val signatureValidator = DigitalSignatureValidator()
    private val checksumValidator = ChecksumValidator()
    private val domainValidator = DomainValidator()
    private val codeAnalyzer = SecurityCodeAnalyzer()
    
    /**
     * 初始化安全验证器
     */
    fun initialize(publicKey: String, allowedDomains: List<String>) {
        this.publicKey = publicKey
        this.allowedDomains = allowedDomains
        
        signatureValidator.initialize(publicKey)
        domainValidator.initialize(allowedDomains)
        
        isInitialized = true
        
        UnifyPerformanceMonitor.recordMetric("security_validator_init", 1.0, "count")
    }
    
    /**
     * 验证更新信息
     */
    suspend fun validateUpdate(updateInfo: HotUpdateInfo): Boolean {
        if (!isInitialized) {
            throw IllegalStateException("安全验证器未初始化")
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            // 1. 验证下载域名
            val domainValid = domainValidator.validateDomain(updateInfo.downloadUrl)
            if (!domainValid) {
                UnifyPerformanceMonitor.recordMetric("security_domain_invalid", 1.0, "count")
                return false
            }
            
            // 2. 验证数字签名
            val signatureValid = signatureValidator.validateSignature(
                data = updateInfo.toString(),
                signature = updateInfo.signature
            )
            if (!signatureValid) {
                UnifyPerformanceMonitor.recordMetric("security_signature_invalid", 1.0, "count")
                return false
            }
            
            // 3. 验证版本合法性
            val versionValid = validateVersion(updateInfo.version)
            if (!versionValid) {
                UnifyPerformanceMonitor.recordMetric("security_version_invalid", 1.0, "count")
                return false
            }
            
            val validationTime = System.currentTimeMillis() - startTime
            UnifyPerformanceMonitor.recordMetric("security_validation_time", 
                validationTime.toDouble(), "ms")
            
            true
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("security_validation_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            false
        }
    }
    
    /**
     * 验证更新包
     */
    suspend fun validatePackage(updatePackage: UpdatePackage): Boolean {
        return try {
            val startTime = System.currentTimeMillis()
            
            // 1. 验证包完整性
            val integrityValid = validatePackageIntegrity(updatePackage)
            if (!integrityValid) {
                UnifyPerformanceMonitor.recordMetric("security_integrity_invalid", 1.0, "count")
                return false
            }
            
            // 2. 验证数字签名
            val signatureValid = signatureValidator.validateSignature(
                data = Json.encodeToString(updatePackage.copy(signature = "")),
                signature = updatePackage.signature
            )
            if (!signatureValid) {
                UnifyPerformanceMonitor.recordMetric("security_package_signature_invalid", 1.0, "count")
                return false
            }
            
            // 3. 验证组件代码安全性
            val codeSecurityValid = validateComponentsSecurity(updatePackage.components)
            if (!codeSecurityValid) {
                UnifyPerformanceMonitor.recordMetric("security_code_invalid", 1.0, "count")
                return false
            }
            
            // 4. 验证配置安全性
            val configSecurityValid = validateConfigurationsSecurity(updatePackage.configurations)
            if (!configSecurityValid) {
                UnifyPerformanceMonitor.recordMetric("security_config_invalid", 1.0, "count")
                return false
            }
            
            val validationTime = System.currentTimeMillis() - startTime
            UnifyPerformanceMonitor.recordMetric("security_package_validation_time", 
                validationTime.toDouble(), "ms")
            
            true
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("security_package_validation_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            false
        }
    }
    
    /**
     * 验证组件安全性
     */
    suspend fun validateComponent(componentData: ComponentData): Boolean {
        return try {
            // 1. 代码安全分析
            val codeAnalysisResult = codeAnalyzer.analyzeCode(componentData.code)
            if (codeAnalysisResult.hasSecurityIssues) {
                UnifyPerformanceMonitor.recordMetric("security_component_code_invalid", 1.0, "count",
                    mapOf("issues" to codeAnalysisResult.issues.joinToString(",")))
                return false
            }
            
            // 2. 依赖安全检查
            val dependencyValid = validateDependencies(componentData.metadata.dependencies)
            if (!dependencyValid) {
                UnifyPerformanceMonitor.recordMetric("security_component_dependency_invalid", 1.0, "count")
                return false
            }
            
            // 3. 权限检查
            val permissionValid = validatePermissions(componentData)
            if (!permissionValid) {
                UnifyPerformanceMonitor.recordMetric("security_component_permission_invalid", 1.0, "count")
                return false
            }
            
            true
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("security_component_validation_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            false
        }
    }
    
    /**
     * 验证包完整性
     */
    private fun validatePackageIntegrity(updatePackage: UpdatePackage): Boolean {
        return try {
            val packageData = Json.encodeToString(updatePackage.copy(checksum = "", signature = ""))
            val calculatedChecksum = checksumValidator.calculateChecksum(packageData.encodeToByteArray())
            
            calculatedChecksum == updatePackage.checksum
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证组件代码安全性
     */
    private suspend fun validateComponentsSecurity(components: Map<String, ComponentData>): Boolean {
        return components.all { (_, componentData) ->
            validateComponent(componentData)
        }
    }
    
    /**
     * 验证配置安全性
     */
    private fun validateConfigurationsSecurity(configurations: Map<String, String>): Boolean {
        return configurations.all { (key, value) ->
            validateConfigurationSecurity(key, value)
        }
    }
    
    /**
     * 验证单个配置安全性
     */
    private fun validateConfigurationSecurity(key: String, value: String): Boolean {
        // 检查敏感配置键
        val sensitiveKeys = listOf("password", "secret", "key", "token", "credential")
        if (sensitiveKeys.any { key.lowercase().contains(it) }) {
            // 敏感配置需要额外验证
            return validateSensitiveConfig(key, value)
        }
        
        // 检查配置值格式
        return validateConfigFormat(key, value)
    }
    
    /**
     * 验证敏感配置
     */
    private fun validateSensitiveConfig(key: String, value: String): Boolean {
        // 敏感配置应该被加密或使用安全格式
        return when {
            value.startsWith("encrypted:") -> true
            value.startsWith("env:") -> true
            value.length < 8 -> false // 太短的敏感值不安全
            else -> false
        }
    }
    
    /**
     * 验证配置格式
     */
    private fun validateConfigFormat(key: String, value: String): Boolean {
        return when {
            key.endsWith("_url") -> validateUrl(value)
            key.endsWith("_email") -> validateEmail(value)
            key.endsWith("_number") -> validateNumber(value)
            else -> true // 其他配置暂不限制格式
        }
    }
    
    /**
     * 验证URL格式
     */
    private fun validateUrl(url: String): Boolean {
        return try {
            url.startsWith("http://") || url.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证邮箱格式
     */
    private fun validateEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
    
    /**
     * 验证数字格式
     */
    private fun validateNumber(number: String): Boolean {
        return number.toDoubleOrNull() != null
    }
    
    /**
     * 验证版本号
     */
    private fun validateVersion(version: String): Boolean {
        // 版本号格式: x.y.z
        val versionRegex = Regex("""^\d+\.\d+\.\d+$""")
        return versionRegex.matches(version)
    }
    
    /**
     * 验证依赖
     */
    private fun validateDependencies(dependencies: List<String>): Boolean {
        return dependencies.all { dependency ->
            validateDependency(dependency)
        }
    }
    
    /**
     * 验证单个依赖
     */
    private fun validateDependency(dependency: String): Boolean {
        // 检查是否为已知安全的依赖
        val trustedDependencies = listOf(
            "androidx.compose",
            "kotlinx.coroutines",
            "kotlinx.serialization",
            "org.jetbrains.kotlin"
        )
        
        return trustedDependencies.any { trusted ->
            dependency.startsWith(trusted)
        }
    }
    
    /**
     * 验证权限
     */
    private fun validatePermissions(componentData: ComponentData): Boolean {
        // 检查组件是否请求了危险权限
        val dangerousPermissions = listOf(
            "WRITE_EXTERNAL_STORAGE",
            "READ_CONTACTS",
            "ACCESS_FINE_LOCATION",
            "CAMERA",
            "RECORD_AUDIO"
        )
        
        // 从组件代码中检测权限使用
        val usedPermissions = extractPermissionsFromCode(componentData.code)
        
        return usedPermissions.none { permission ->
            dangerousPermissions.contains(permission)
        }
    }
    
    /**
     * 从代码中提取权限使用
     */
    private fun extractPermissionsFromCode(code: String): List<String> {
        val permissions = mutableListOf<String>()
        
        // 简单的权限检测（实际实现需要更复杂的代码分析）
        val permissionPatterns = listOf(
            Regex("""checkSelfPermission\("([^"]+)"\)"""),
            Regex("""requestPermissions\(.*"([^"]+)".*\)"""),
            Regex("""uses-permission.*android:name="([^"]+)"""")
        )
        
        permissionPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                match.groupValues.getOrNull(1)?.let { permission ->
                    permissions.add(permission)
                }
            }
        }
        
        return permissions
    }
}

/**
 * 数字签名验证器
 */
class DigitalSignatureValidator {
    private var publicKey: String = ""
    
    fun initialize(publicKey: String) {
        this.publicKey = publicKey
    }
    
    fun validateSignature(data: String, signature: String): Boolean {
        if (publicKey.isEmpty()) return false
        
        return try {
            // 简化的签名验证实现
            // 实际实现需要使用真正的数字签名算法
            val expectedSignature = generateSignature(data, publicKey)
            signature == expectedSignature
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateSignature(data: String, key: String): String {
        // 简化实现，实际需要使用RSA或其他签名算法
        return (data + key).hashCode().toString()
    }
}

/**
 * 校验和验证器
 */
class ChecksumValidator {
    fun calculateChecksum(data: ByteArray): String {
        // 使用简单的哈希算法
        return data.contentHashCode().toString()
    }
    
    fun validateChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        val actualChecksum = calculateChecksum(data)
        return actualChecksum == expectedChecksum
    }
}

/**
 * 域名验证器
 */
class DomainValidator {
    private var allowedDomains: List<String> = emptyList()
    
    fun initialize(allowedDomains: List<String>) {
        this.allowedDomains = allowedDomains
    }
    
    fun validateDomain(url: String): Boolean {
        if (allowedDomains.isEmpty()) return true
        
        return try {
            val domain = extractDomain(url)
            allowedDomains.any { allowedDomain ->
                domain.endsWith(allowedDomain) || domain == allowedDomain
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun extractDomain(url: String): String {
        return url.substringAfter("://").substringBefore("/").lowercase()
    }
}

/**
 * 安全代码分析器
 */
class SecurityCodeAnalyzer {
    suspend fun analyzeCode(code: String): CodeAnalysisResult {
        val issues = mutableListOf<String>()
        
        // 检查危险API调用
        val dangerousApis = listOf(
            "eval(",
            "exec(",
            "Runtime.getRuntime()",
            "System.exit(",
            "ProcessBuilder(",
            "Class.forName(",
            "Method.invoke("
        )
        
        dangerousApis.forEach { api ->
            if (code.contains(api)) {
                issues.add("检测到危险API调用: $api")
            }
        }
        
        // 检查网络请求
        val networkPatterns = listOf(
            Regex("""http://[^"'\s]+"""),
            Regex("""https://[^"'\s]+""")
        )
        
        networkPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                val url = match.value
                if (!isAllowedUrl(url)) {
                    issues.add("检测到不安全的网络请求: $url")
                }
            }
        }
        
        // 检查文件操作
        val fileOperations = listOf(
            "File(",
            "FileInputStream(",
            "FileOutputStream(",
            "RandomAccessFile("
        )
        
        fileOperations.forEach { operation ->
            if (code.contains(operation)) {
                issues.add("检测到文件操作: $operation")
            }
        }
        
        return CodeAnalysisResult(
            hasSecurityIssues = issues.isNotEmpty(),
            issues = issues
        )
    }
    
    private fun isAllowedUrl(url: String): Boolean {
        // 检查URL是否在允许列表中
        val allowedDomains = listOf(
            "api.unify-core.com",
            "cdn.unify-core.com",
            "update.unify-core.com"
        )
        
        return allowedDomains.any { domain ->
            url.contains(domain)
        }
    }
}

/**
 * 代码分析结果
 */
data class CodeAnalysisResult(
    val hasSecurityIssues: Boolean,
    val issues: List<String>
)
