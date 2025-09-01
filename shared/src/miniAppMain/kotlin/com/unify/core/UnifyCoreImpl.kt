package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * 小程序平台的UnifyCore实现
 */
actual class UnifyCoreImpl : UnifyCore {
    
    override val uiManager: UnifyUIManager = UnifyUIManagerImpl()
    override val dataManager: UnifyDataManager = UnifyDataManagerImpl()
    override val networkManager: UnifyNetworkManager = UnifyNetworkManagerImpl()
    override val platformManager: PlatformManager = PlatformManager
    
    private var initialized = false
    
    override fun initialize() {
        if (initialized) return
        
        // 初始化各个管理器
        platformManager.initialize()
        
        initialized = true
    }
    
    override fun getVersion(): String = UnifyCoreInstance.VERSION
    
    override fun getSupportedPlatforms(): List<String> = UnifyCoreInstance.SUPPORTED_PLATFORMS
    
    override fun isPlatformSupported(platform: String): Boolean {
        return platform in UnifyCoreInstance.SUPPORTED_PLATFORMS
    }
    
    override fun getCurrentPlatformConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "MiniApp",
            "version" to getVersion(),
            "capabilities" to listOf(
                "touch", "camera", "location", "storage", 
                "payment", "share", "login", "canvas"
            ),
            "ui_framework" to "Mini Program Framework + Compose Bridge",
            "supported_platforms" to listOf(
                "wechat", "alipay", "baidu", "toutiao", 
                "qq", "taobao", "kuaishou", "douyin"
            ),
            "miniapp_features" to listOf(
                "native_components", "api_bridge", "lifecycle_management",
                "permission_system", "payment_integration", "social_sharing"
            ),
            "limitations" to listOf(
                "limited_storage", "restricted_api", "size_constraints",
                "performance_limits", "network_restrictions"
            ),
            "max_package_size" to "2MB",
            "api_compatibility" to "Mini Program API 2.0+"
        )
    }
}
