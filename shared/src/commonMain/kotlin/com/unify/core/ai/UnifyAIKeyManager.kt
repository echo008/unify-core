package com.unify.core.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * API密钥状态枚举
 */
enum class APIKeyStatus {
    NOT_SET,
    VALID,
    INVALID,
    EXPIRED
}

/**
 * API密钥信息数据类
 */
data class APIKeyInfo(
    val provider: AIProvider,
    val keyHash: String, // 存储密钥的哈希值，不存储明文
    val status: APIKeyStatus,
    val lastValidated: Long,
    val expiresAt: Long? = null
)

/**
 * Unify AI密钥管理器 - 安全管理AI服务API密钥
 */
class UnifyAIKeyManager {
    companion object {
        // 默认API端点配置
        val DEFAULT_ENDPOINTS = mapOf(
            AIProvider.OPENAI to "https://api.openai.com",
            AIProvider.ANTHROPIC to "https://api.anthropic.com",
            AIProvider.GOOGLE to "https://generativelanguage.googleapis.com",
            AIProvider.ALIBABA_CLOUD to "https://dashscope.aliyuncs.com",
            AIProvider.BAIDU to "https://aip.baidubce.com",
            AIProvider.TENCENT to "https://hunyuan.tencentcloudapi.com"
        )
    }
    
    private val _keyInfos = MutableStateFlow<Map<AIProvider, APIKeyInfo>>(emptyMap())
    val keyInfos: StateFlow<Map<AIProvider, APIKeyInfo>> = _keyInfos.asStateFlow()
    
    private val _activeProvider = MutableStateFlow<AIProvider?>(null)
    val activeProvider: StateFlow<AIProvider?> = _activeProvider.asStateFlow()
    
    // 内存中临时存储密钥（实际应用中应使用安全存储）
    private val keyStorage = mutableMapOf<AIProvider, String>()
    
    /**
     * 设置API密钥
     */
    fun setAPIKey(provider: AIProvider, apiKey: String): Boolean {
        return try {
            if (apiKey.isBlank()) {
                removeAPIKey(provider)
                return false
            }
            
            // 基本验证API密钥格式
            if (!validateKeyFormat(provider, apiKey)) {
                updateKeyInfo(provider, APIKeyStatus.INVALID)
                return false
            }
            
            // 存储密钥（实际应用中应加密存储）
            keyStorage[provider] = apiKey
            
            // 更新密钥信息
            updateKeyInfo(provider, APIKeyStatus.VALID)
            
            // 如果没有活跃提供商，设置为当前提供商
            if (_activeProvider.value == null) {
                _activeProvider.value = provider
            }
            
            true
        } catch (e: Exception) {
            updateKeyInfo(provider, APIKeyStatus.INVALID)
            false
        }
    }
    
    /**
     * 获取API密钥
     */
    fun getAPIKey(provider: AIProvider): String? {
        return keyStorage[provider]
    }
    
    /**
     * 移除API密钥
     */
    fun removeAPIKey(provider: AIProvider) {
        keyStorage.remove(provider)
        val currentInfos = _keyInfos.value.toMutableMap()
        currentInfos.remove(provider)
        _keyInfos.value = currentInfos
        
        // 如果移除的是活跃提供商，选择另一个
        if (_activeProvider.value == provider) {
            _activeProvider.value = keyStorage.keys.firstOrNull()
        }
    }
    
    /**
     * 获取当前活跃的提供商
     */
    fun getActiveProvider(): AIProvider? {
        return _activeProvider.value
    }
    
    /**
     * 设置活跃的AI提供商
     */
    fun setActiveProvider(provider: AIProvider): Boolean {
        return if (keyStorage.containsKey(provider)) {
            _activeProvider.value = provider
            true
        } else {
            false
        }
    }
    
    /**
     * 获取活跃提供商的API配置
     */
    fun getActiveAPIConfiguration(): APIConfiguration? {
        val provider = _activeProvider.value ?: return null
        val apiKey = getAPIKey(provider) ?: return null
        val baseUrl = DEFAULT_ENDPOINTS[provider] ?: return null
        
        return APIConfiguration(
            provider = provider,
            apiKey = apiKey,
            baseUrl = baseUrl
        )
    }
    
    /**
     * 验证密钥格式
     */
    private fun validateKeyFormat(provider: AIProvider, apiKey: String): Boolean {
        return when (provider) {
            AIProvider.OPENAI -> apiKey.startsWith("sk-") && apiKey.length > 20
            AIProvider.ANTHROPIC -> apiKey.startsWith("sk-ant-") && apiKey.length > 20
            AIProvider.GOOGLE -> apiKey.length > 10 // Google API密钥格式较灵活
            AIProvider.ALIBABA_CLOUD -> apiKey.length > 10
            AIProvider.BAIDU -> apiKey.length > 10
            AIProvider.TENCENT -> apiKey.length > 10
        }
    }
    
    /**
     * 更新密钥信息
     */
    private fun updateKeyInfo(provider: AIProvider, status: APIKeyStatus) {
        val currentInfos = _keyInfos.value.toMutableMap()
        val keyHash = keyStorage[provider]?.hashCode()?.toString() ?: ""
        
        currentInfos[provider] = APIKeyInfo(
            provider = provider,
            keyHash = keyHash,
            status = status,
            lastValidated = System.currentTimeMillis()
        )
        
        _keyInfos.value = currentInfos
    }
    
    /**
     * 检查是否有可用的API密钥
     */
    fun hasValidAPIKey(): Boolean {
        return keyStorage.isNotEmpty() && _activeProvider.value != null
    }
    
    /**
     * 获取所有支持的提供商列表
     */
    fun getSupportedProviders(): List<AIProvider> {
        return AIProvider.values().toList()
    }
    
    /**
     * 获取提供商显示名称
     */
    fun getProviderDisplayName(provider: AIProvider): String {
        return when (provider) {
            AIProvider.OPENAI -> "OpenAI (GPT-4, GPT-3.5)"
            AIProvider.ANTHROPIC -> "Anthropic (Claude)"
            AIProvider.GOOGLE -> "Google (Gemini)"
            AIProvider.ALIBABA_CLOUD -> "阿里云 (通义千问)"
            AIProvider.BAIDU -> "百度 (文心一言)"
            AIProvider.TENCENT -> "腾讯 (混元)"
        }
    }
    
    /**
     * 清除所有密钥
     */
    fun clearAllKeys() {
        keyStorage.clear()
        _keyInfos.value = emptyMap()
        _activeProvider.value = null
    }
}
