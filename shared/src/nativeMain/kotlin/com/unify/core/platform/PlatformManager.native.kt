package com.unify.core.platform

import kotlin.native.Platform
import com.unify.core.types.PlatformType
import com.unify.core.types.DeviceInfo

/**
 * Native平台管理器实现
 */
actual fun getCurrentPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createAndroidPlatformManager(): PlatformManager {
    return NativePlatformManager() // 在Native平台返回通用实现
}

actual fun createIOSPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createWebPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createDesktopPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createHarmonyOSPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createMiniProgramPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createWatchPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

actual fun createTVPlatformManager(): PlatformManager {
    return NativePlatformManager()
}

/**
 * Native平台管理器实现
 */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
private class NativePlatformManager : PlatformManager {
    private val config = mutableMapOf<String, String>()
    
    override fun getPlatformType(): PlatformType = PlatformType.NATIVE
    override fun getPlatformName(): String = "Native (${Platform.osFamily})"
    override fun getPlatformVersion(): String = "1.0.0"
    
    override suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = "Kotlin/Native",
            model = "Native",
            systemName = Platform.osFamily.name,
            systemVersion = "1.0.0",
            deviceId = "native-device",
            isEmulator = false
        )
    }
    
    override fun hasCapability(capability: String): Boolean = false
    override fun getSupportedCapabilities(): List<String> = emptyList()
    
    override suspend fun initialize(): Boolean {
        // Native平台初始化
        return true
    }
    
    override suspend fun cleanup() {
        // Native平台清理
    }
    
    override fun getPlatformConfig(): Map<String, String> = config.toMap()
    
    override fun setPlatformConfig(key: String, value: String) {
        config[key] = value
    }
}
