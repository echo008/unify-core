package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * HarmonyOS平台的UnifyCore实现
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
            "platform" to "HarmonyOS",
            "version" to getVersion(),
            "capabilities" to listOf(
                "touch", "camera", "location", "notifications", 
                "biometric", "nfc", "sensors", "storage", "distributed"
            ),
            "ui_framework" to "ArkUI + Compose Multiplatform",
            "harmony_features" to listOf(
                "distributed_capability", "multi_device_collaboration", 
                "atomic_service", "super_device", "cross_device_migration",
                "distributed_data_management", "distributed_hardware"
            ),
            "device_types" to listOf(
                "smartphone", "tablet", "smart_watch", "smart_tv", 
                "car_machine", "smart_speaker", "smart_glasses"
            ),
            "harmony_version" to "HarmonyOS 4.0+",
            "api_level" to 10
        )
    }
}
