package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * Desktop平台的UnifyCore实现
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
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        val javaVersion = System.getProperty("java.version")
        
        return mapOf(
            "platform" to "Desktop",
            "version" to getVersion(),
            "os_name" to osName,
            "os_version" to osVersion,
            "java_version" to javaVersion,
            "capabilities" to listOf(
                "keyboard", "mouse", "file_system", "multi_window", 
                "system_tray", "notifications", "clipboard", "drag_drop"
            ),
            "ui_framework" to "Compose Desktop",
            "desktop_features" to listOf(
                "window_management", "menu_bar", "system_integration", 
                "file_associations", "shortcuts", "auto_updater"
            ),
            "supported_os" to listOf("Windows 10+", "macOS 10.14+", "Linux"),
            "architecture" to System.getProperty("os.arch")
        )
    }
}
