package com.unify.core

import android.content.Context
import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerFactory
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerFactory
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerFactory
import com.unify.device.UnifyDeviceManager
import com.unify.device.UnifyDeviceManagerFactory

/**
 * Android平台UnifyCore实现
 */
class UnifyCoreImpl(private val context: Context) : UnifyCore {
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
            platformName = "Android",
            version = android.os.Build.VERSION.RELEASE,
            deviceModel = android.os.Build.MODEL,
            osVersion = android.os.Build.VERSION.RELEASE,
            capabilities =
                listOf(
                    "Camera",
                    "GPS",
                    "Sensors",
                    "Bluetooth",
                    "NFC",
                    "Biometric",
                ),
        )
    }
}

actual object UnifyCoreFactory {
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    actual fun create(): UnifyCore {
        return UnifyCoreImpl(
            context ?: throw IllegalStateException("UnifyCoreFactory not initialized. Call initialize(context) first."),
        )
    }
}
