package com.unify.core.platform

import com.unify.core.types.DeviceInfo
import com.unify.core.types.PlatformType

/**
 * Desktop平台管理器相关函数实现
 */
actual fun getCurrentPlatformManager(): PlatformManager {
    return DesktopPlatformManager()
}

actual fun createAndroidPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Android platform manager not supported on Desktop")
}

actual fun createIOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("iOS platform manager not supported on Desktop")
}

actual fun createWebPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Web platform manager not supported on Desktop")
}

actual fun createDesktopPlatformManager(): PlatformManager {
    return DesktopPlatformManager()
}

actual fun createHarmonyOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("HarmonyOS platform manager not supported on Desktop")
}

actual fun createMiniProgramPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Mini program platform manager not supported on Desktop")
}

actual fun createWatchPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Watch platform manager not supported on Desktop")
}

actual fun createTVPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("TV platform manager not supported on Desktop")
}

/**
 * Desktop平台管理器实现
 */
class DesktopPlatformManager : BasePlatformManager() {
    override fun getPlatformType(): PlatformType = PlatformType.DESKTOP

    override fun getPlatformName(): String = "Desktop"

    override fun getPlatformVersion(): String = "1.0.0"

    override suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "desktop_device",
            systemName = "Desktop",
            systemVersion = "1.0.0",
            manufacturer = "Desktop",
            model = "Desktop",
            isEmulator = false,
        )
    }

    override fun hasCapability(capability: String): Boolean = false

    override fun getSupportedCapabilities(): List<String> = emptyList()

    override suspend fun performPlatformInitialization() {
        // Desktop平台初始化
    }

    override suspend fun performPlatformCleanup() {
        // Desktop平台清理
    }
}
