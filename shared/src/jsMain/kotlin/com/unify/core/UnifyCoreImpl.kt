package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * Web平台的UnifyCore实现
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
            "platform" to "Web",
            "version" to getVersion(),
            "capabilities" to listOf(
                "touch", "camera", "location", "notifications", 
                "storage", "webgl", "webassembly", "service_worker"
            ),
            "ui_framework" to "Compose for Web",
            "web_features" to listOf(
                "pwa", "offline_support", "push_notifications", 
                "web_share", "file_system_access", "clipboard_api"
            ),
            "browser_support" to mapOf(
                "chrome" to "90+",
                "firefox" to "88+",
                "safari" to "14+",
                "edge" to "90+"
            ),
            "web_standards" to listOf("ES2020", "WebGL 2.0", "WebAssembly")
        )
    }
}
