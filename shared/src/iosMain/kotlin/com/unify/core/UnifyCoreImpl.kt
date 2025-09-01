package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * iOS平台的UnifyCore实现
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
            "platform" to "iOS",
            "version" to getVersion(),
            "capabilities" to listOf(
                "touch", "camera", "location", "notifications", 
                "biometric", "nfc", "sensors", "storage", "siri"
            ),
            "ui_framework" to "SwiftUI + Compose Multiplatform",
            "native_features" to listOf(
                "app_clips", "widgets", "shortcuts", "handoff", 
                "continuity_camera", "airdrop", "carplay"
            ),
            "ios_version_support" to "iOS 12.0+",
            "device_support" to listOf("iPhone", "iPad", "Apple Watch", "Apple TV")
        )
    }
}
