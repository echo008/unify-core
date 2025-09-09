package com.unify.core

import androidx.compose.ui.graphics.Color
import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerFactory
import com.unify.core.ui.UnifyTheme
import com.unify.core.ui.UnifyUIManager
import com.unify.device.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Desktop平台UnifyCore实现
 */
class DesktopUnifyCore : UnifyCore {
    private var initialized = false

    override val uiManager: UnifyUIManager by lazy {
        DesktopUIManager()
    }

    override val dataManager: UnifyDataManager by lazy {
        UnifyDataManagerFactory.create()
    }

    override val deviceManager: UnifyDeviceManager by lazy {
        DesktopDeviceManager()
    }

    override suspend fun initialize() {
        initialized = true
    }

    override suspend fun shutdown() {
        initialized = false
    }

    override fun isInitialized(): Boolean = initialized

    override fun getPlatformInfo(): PlatformInfo {
        return PlatformInfo(
            platformName = "Desktop",
            version = "1.0.0",
            deviceModel = System.getProperty("os.name"),
            osVersion = System.getProperty("os.version"),
            capabilities =
                listOf(
                    "network", "storage", "performance", "memory",
                    "platform", "miniapp", "security", "ui", "device",
                ),
        )
    }
}

/**
 * Desktop平台UI管理器实现
 */
class DesktopUIManager : UnifyUIManager {
    private val _theme =
        MutableStateFlow(
            UnifyTheme(
                name = "Desktop Light",
                isDark = false,
                primaryColor = Color(0xFF1976D2),
                secondaryColor = Color(0xFF03DAC6),
                backgroundColor = Color(0xFFFFFFFF),
                surfaceColor = Color(0xFFF5F5F5),
                errorColor = Color(0xFFB00020),
                onPrimaryColor = Color(0xFFFFFFFF),
                onSecondaryColor = Color(0xFF000000),
                onBackgroundColor = Color(0xFF000000),
                onSurfaceColor = Color(0xFF000000),
                onErrorColor = Color(0xFFFFFFFF),
            ),
        )

    private val _fontScale = MutableStateFlow(1.0f)
    private var animationsEnabled = true
    private var accessibilityEnabled = false

    override fun setTheme(theme: UnifyTheme) {
        _theme.value = theme
    }

    override fun getTheme(): UnifyTheme = _theme.value

    override fun observeTheme(): StateFlow<UnifyTheme> = _theme.asStateFlow()

    override fun toggleDarkMode() {
        val current = _theme.value
        _theme.value = current.copy(isDark = !current.isDark)
    }

    override fun isDarkMode(): Boolean = _theme.value.isDark

    override fun getPrimaryColor(): Color = _theme.value.primaryColor

    override fun getSecondaryColor(): Color = _theme.value.secondaryColor

    override fun getBackgroundColor(): Color = _theme.value.backgroundColor

    override fun getSurfaceColor(): Color = _theme.value.surfaceColor

    override fun getErrorColor(): Color = _theme.value.errorColor

    override fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    override fun getFontScale(): Float = _fontScale.value

    override fun observeFontScale(): StateFlow<Float> = _fontScale.asStateFlow()

    override fun getScreenWidth(): Int = 1920

    override fun getScreenHeight(): Int = 1080

    override fun getScreenDensity(): Float = 1.0f

    override fun isTablet(): Boolean = false

    override fun isLandscape(): Boolean = true

    override fun setAnimationsEnabled(enabled: Boolean) {
        animationsEnabled = enabled
    }

    override fun areAnimationsEnabled(): Boolean = animationsEnabled

    override fun getAnimationDuration(): Long = 300L

    override fun setAccessibilityEnabled(enabled: Boolean) {
        accessibilityEnabled = enabled
    }

    override fun isAccessibilityEnabled(): Boolean = accessibilityEnabled

    override fun announceForAccessibility(message: String) {
        // Desktop平台无障碍公告实现
    }
}

/**
 * Desktop平台设备管理器实现
 */
class DesktopDeviceManager : UnifyDeviceManager {
    override fun getDeviceInfo(): DeviceInfo {
        val runtime = Runtime.getRuntime()
        return DeviceInfo(
            deviceId = "desktop-${System.currentTimeMillis()}",
            deviceName = "Desktop Computer",
            model = System.getProperty("os.name"),
            manufacturer = "Generic",
            osName = System.getProperty("os.name"),
            osVersion = System.getProperty("os.version"),
            screenWidth = 1920,
            screenHeight = 1080,
            screenDensity = 1.0f,
            totalMemory = runtime.maxMemory(),
            availableMemory = runtime.freeMemory(),
        )
    }

    override fun getPlatformName(): String = "Desktop"

    override fun getDeviceModel(): String = System.getProperty("os.name")

    override fun getOSVersion(): String = System.getProperty("os.version")

    override fun getAppVersion(): String = "1.0.0"

    override suspend fun requestPermission(permission: DevicePermission): PermissionStatus = PermissionStatus.GRANTED

    override suspend fun requestPermissions(permissions: List<DevicePermission>): Map<DevicePermission, PermissionStatus> =
        permissions.associateWith { PermissionStatus.GRANTED }

    override fun checkPermission(permission: DevicePermission): PermissionStatus = PermissionStatus.GRANTED

    override fun observePermissionStatus(permission: DevicePermission): Flow<PermissionStatus> = flowOf(PermissionStatus.GRANTED)

    override fun getSupportedSensors(): List<SensorType> = emptyList()

    override fun startSensorMonitoring(
        sensorType: SensorType,
        listener: SensorListener,
    ) {}

    override fun stopSensorMonitoring(sensorType: SensorType) {}

    override fun isSensorAvailable(sensorType: SensorType): Boolean = false

    override fun vibrate(durationMillis: Long) {}

    override fun setScreenBrightness(brightness: Float) {}

    override fun getScreenBrightness(): Float = 1.0f

    override fun setVolume(volume: Float) {}

    override fun getVolume(): Float = 0.5f

    override fun showNotification(
        title: String,
        message: String,
        id: String,
    ) {}

    override suspend fun takePicture(): String? = null

    override suspend fun recordAudio(durationMillis: Long): String? = null

    override suspend fun getCurrentLocation(): LocationInfo? = null

    override fun observeLocationUpdates(): Flow<LocationInfo> = flowOf()

    override fun isNetworkAvailable(): Boolean = true

    override fun getNetworkType(): NetworkType = NetworkType.ETHERNET

    override fun observeNetworkStatus(): Flow<NetworkStatus> = flowOf(NetworkStatus.CONNECTED)

    override fun getBatteryLevel(): Float = 100f

    override fun isBatteryCharging(): Boolean = true

    override fun observeBatteryStatus(): Flow<BatteryStatus> = flowOf(BatteryStatus(100f, true, ChargingType.AC, 25.0f))

    override fun getAvailableStorage(): Long = 1024L * 1024L * 1024L * 100L // 100GB

    override fun getTotalStorage(): Long = 1024L * 1024L * 1024L * 500L // 500GB

    override fun getUsedStorage(): Long = getTotalStorage() - getAvailableStorage()
}

/**
 * Desktop平台UnifyCore工厂实现
 */
actual object UnifyCoreFactory {
    actual fun create(): UnifyCore {
        return DesktopUnifyCore()
    }
}
