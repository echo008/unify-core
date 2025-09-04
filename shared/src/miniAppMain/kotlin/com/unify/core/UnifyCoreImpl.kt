package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerFactory
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerFactory
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerFactory
import com.unify.device.UnifyDeviceManager
import com.unify.device.UnifyDeviceManagerFactory

/**
 * 小程序平台UnifyCore实现
 */
class UnifyCoreImpl : UnifyCore {
    override val uiManager: UnifyUIManager by lazy { UnifyUIManagerFactory.create() }
    override val dataManager: UnifyDataManager by lazy { UnifyDataManagerFactory.create() }
    override val networkManager: UnifyNetworkManager by lazy { UnifyNetworkManagerFactory.create() }
    override val deviceManager: UnifyDeviceManager by lazy { UnifyDeviceManagerFactory.create() }
    
    private var initialized = false
    
    override suspend fun initialize() {
        if (!initialized) {
            // 初始化各个管理器
            initialized = true
        }
    }
    
    override suspend fun shutdown() {
        if (initialized) {
            // 清理资源
            initialized = false
        }
    }
    
    override fun isInitialized(): Boolean = initialized
    
    override fun getPlatformInfo(): PlatformInfo {
        return PlatformInfo(
            platformName = "MiniApp",
            version = "1.0.0",
            deviceModel = "MiniApp Runtime",
            osVersion = "MiniApp 1.0",
            capabilities = listOf(
                "Camera", "GPS", "Payment", "Share", "Login", "Storage"
            )
        )
    }
}

actual object UnifyCoreFactory {
    actual fun create(): UnifyCore {
        return UnifyCoreImpl()
    }
}
