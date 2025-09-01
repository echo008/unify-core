package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * Watch平台的UnifyCore实现
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
            "platform" to "Watch",
            "version" to getVersion(),
            "capabilities" to listOf(
                "touch", "crown_input", "haptic_feedback", "heart_rate", 
                "accelerometer", "gyroscope", "gps", "cellular", "notifications"
            ),
            "ui_framework" to "WatchOS + Compose Multiplatform",
            "watch_features" to listOf(
                "health_monitoring", "fitness_tracking", "complications",
                "digital_crown", "force_touch", "always_on_display",
                "workout_detection", "fall_detection", "ecg"
            ),
            "supported_devices" to listOf(
                "Apple Watch Series 4+", "Wear OS 3.0+", 
                "Samsung Galaxy Watch", "Huawei Watch GT"
            ),
            "screen_sizes" to listOf("38mm", "40mm", "41mm", "42mm", "44mm", "45mm", "49mm"),
            "battery_optimization" to true,
            "always_on_support" to true,
            "cellular_support" to true
        )
    }
}
