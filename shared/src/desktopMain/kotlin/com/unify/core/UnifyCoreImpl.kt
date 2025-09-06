package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerFactory
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerFactory
import com.unify.device.UnifyDeviceManager
import com.unify.device.UnifyDeviceManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Desktop平台UnifyCore实现
 */
class UnifyCoreImpl : UnifyCore {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override val uiManager: UnifyUIManager by lazy { UnifyUIManagerFactory.create() }
    override val dataManager: UnifyDataManager by lazy { UnifyDataManagerFactory.create() }
    // 网络管理器暂时禁用，等待修复编译器内部错误
    // override val networkManager: UnifyNetworkManager by lazy { UnifyNetworkManagerFactory.create() }
    override val deviceManager: UnifyDeviceManager by lazy { UnifyDeviceManagerFactory.create() }
    
    private var initialized = false
    
    override suspend fun initialize() {
        if (!initialized) {
            // 初始化各个管理器
            try {
                // 初始化数据管理器
                // dataManager 已通过 lazy 初始化
                
                // 初始化网络管理器
                networkManager.setBaseUrl("https://api.unify.com")
                networkManager.setDefaultHeaders(mapOf(
                    "User-Agent" to "UnifyCore-Desktop/1.0",
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
                ))
                
                // 初始化UI管理器
                // uiManager 已通过 lazy 初始化
                
                // 初始化设备管理器
                // deviceManager 已通过 lazy 初始化
                
                initialized = true
            } catch (e: Exception) {
                throw RuntimeException("UnifyCore初始化失败: ${e.message}", e)
            }
        }
    }
    
    override suspend fun shutdown() {
        if (initialized) {
            try {
                // 清理网络连接
                networkManager.clearCache()
                
                // 清理数据缓存
                dataManager.clearExpiredCache()
                
                // 取消协程作用域
                coroutineScope.cancel()
                
                initialized = false
            } catch (e: Exception) {
                // 记录错误但不抛出异常，确保shutdown能够完成
                println("UnifyCore关闭时发生错误: ${e.message}")
            }
        }
    }
    
    override fun isInitialized(): Boolean = initialized
    
    override fun getPlatformInfo(): PlatformInfo {
        val osName = System.getProperty("os.name") ?: "Unknown"
        val osVersion = System.getProperty("os.version") ?: "Unknown"
        val javaVersion = System.getProperty("java.version") ?: "Unknown"
        
        return PlatformInfo(
            platformName = "Desktop",
            version = javaVersion,
            deviceModel = getDesktopDeviceModel(),
            osVersion = "$osName $osVersion",
            capabilities = getDesktopCapabilities()
        )
    }
    
    private fun getDesktopDeviceModel(): String {
        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        return when {
            osName.contains("windows") -> "Windows PC"
            osName.contains("mac") -> "Mac"
            osName.contains("linux") -> "Linux PC"
            else -> "Desktop Computer"
        }
    }
    
    private fun getDesktopCapabilities(): List<String> {
        val capabilities = mutableListOf<String>()
        
        // 基础桌面功能
        capabilities.addAll(listOf(
            "FileSystem",
            "Network",
            "Clipboard",
            "SystemTray",
            "WindowManagement"
        ))
        
        // 检查Java AWT功能
        try {
            java.awt.Toolkit.getDefaultToolkit()
            capabilities.add("AWT")
        } catch (e: Exception) {
            // AWT不可用
        }
        
        // 检查系统特定功能
        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        when {
            osName.contains("windows") -> {
                capabilities.addAll(listOf("WindowsRegistry", "WindowsServices"))
            }
            osName.contains("mac") -> {
                capabilities.addAll(listOf("AppleScript", "Keychain"))
            }
            osName.contains("linux") -> {
                capabilities.addAll(listOf("DBus", "SystemD"))
            }
        }
        
        return capabilities
    }
}

actual object UnifyCoreFactory {
    actual fun create(): UnifyCore {
        return UnifyCoreImpl()
    }
}
