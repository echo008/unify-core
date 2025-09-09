package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerFactory
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerFactory
import com.unify.device.UnifyDeviceManager
import com.unify.device.UnifyDeviceManagerFactory

/**
 * Web平台UnifyCore实现
 */
class UnifyCoreImpl : UnifyCore {
    override val uiManager: UnifyUIManager by lazy { UnifyUIManagerFactory.create() }
    override val dataManager: UnifyDataManager by lazy { UnifyDataManagerFactory.create() }

    // 网络管理器暂时禁用，等待修复编译器内部错误
    // override val networkManager: UnifyNetworkManager by lazy { UnifyNetworkManagerFactory.create() }
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
            platformName = "Web",
            version = "1.0.0",
            deviceModel = "Browser",
            osVersion = js("navigator.userAgent").toString(),
            capabilities =
                listOf(
                    "Camera",
                    "GPS",
                    "Sensors",
                    "Bluetooth",
                    "WebRTC",
                    "PWA",
                ),
        )
    }
}

actual object UnifyCoreFactory {
    actual fun create(): UnifyCore {
        return UnifyCoreImpl()
    }
}
