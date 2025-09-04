package com.unify.ui.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

/**
 * Unify跨平台适配器接口
 * 支持8大平台的统一适配
 */
interface UnifyPlatformAdapter {
    fun getPlatformName(): String
    fun getPlatformVersion(): String
    fun isFeatureSupported(feature: PlatformFeature): Boolean
    fun getDeviceInfo(): DeviceInfo
    fun getSystemInfo(): SystemInfo
}

/**
 * 平台特性枚举
 */
@Serializable
enum class PlatformFeature {
    CAMERA,
    MICROPHONE,
    GPS,
    BLUETOOTH,
    NFC,
    BIOMETRIC,
    PUSH_NOTIFICATIONS,
    BACKGROUND_PROCESSING,
    FILE_SYSTEM,
    NETWORK,
    SENSORS,
    VIBRATION,
    FLASHLIGHT,
    CONTACTS,
    CALENDAR,
    PHONE,
    SMS,
    EMAIL,
    MAPS,
    PAYMENTS
}

/**
 * 设备信息
 */
@Serializable
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val osVersion: String,
    val apiLevel: Int,
    val screenWidth: Int,
    val screenHeight: Int,
    val density: Float,
    val isTablet: Boolean,
    val isEmulator: Boolean,
    val totalMemory: Long,
    val availableMemory: Long,
    val totalStorage: Long,
    val availableStorage: Long
)

/**
 * 系统信息
 */
@Serializable
data class SystemInfo(
    val platformType: PlatformType,
    val architecture: String,
    val locale: String,
    val timezone: String,
    val batteryLevel: Float,
    val isCharging: Boolean,
    val networkType: NetworkType,
    val isOnline: Boolean,
    val isDarkMode: Boolean,
    val systemFeatures: List<PlatformFeature>
)

/**
 * 平台类型枚举
 */
@Serializable
enum class PlatformType {
    ANDROID,
    IOS,
    WEB,
    DESKTOP,
    HARMONY_OS,
    MINI_APP,
    WATCH,
    TV
}

/**
 * 网络类型枚举
 */
@Serializable
enum class NetworkType {
    WIFI,
    MOBILE,
    ETHERNET,
    BLUETOOTH,
    UNKNOWN,
    NONE
}

/**
 * 平台适配器工厂
 */
expect object UnifyPlatformAdapterFactory {
    fun createAdapter(): UnifyPlatformAdapter
}

/**
 * 获取当前平台适配器
 */
fun getCurrentPlatformAdapter(): UnifyPlatformAdapter {
    return UnifyPlatformAdapterFactory.createAdapter()
}

/**
 * 平台特定的UI修饰符
 */
expect fun Modifier.platformSpecific(): Modifier

/**
 * 平台特定的状态栏控制
 */
@Composable
expect fun UnifyStatusBarController(
    statusBarColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    darkIcons: Boolean = false
)

/**
 * 平台特定的导航栏控制
 */
@Composable
expect fun UnifyNavigationBarController(
    navigationBarColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    darkIcons: Boolean = false
)

/**
 * 平台特定的系统UI控制
 */
@Composable
expect fun UnifySystemUIController(
    statusBarColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    navigationBarColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    statusBarDarkIcons: Boolean = false,
    navigationBarDarkIcons: Boolean = false
)

/**
 * 平台特定的安全区域处理
 */
@Composable
expect fun UnifySafeAreaHandler(
    content: @Composable () -> Unit
)

/**
 * 平台特定的键盘处理
 */
@Composable
expect fun UnifyKeyboardHandler(
    onKeyboardVisibilityChanged: (Boolean) -> Unit = {},
    content: @Composable () -> Unit
)

/**
 * 平台特定的返回按钮处理
 */
@Composable
expect fun UnifyBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)

/**
 * 平台特定的生命周期处理
 */
@Composable
expect fun UnifyLifecycleHandler(
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onDestroy: () -> Unit = {}
)

/**
 * 平台特定的权限处理
 */
@Composable
expect fun UnifyPermissionHandler(
    permissions: List<String>,
    onPermissionResult: (Map<String, Boolean>) -> Unit
)

/**
 * 平台特定的文件选择器
 */
@Composable
expect fun UnifyFilePicker(
    fileTypes: List<String> = listOf("*/*"),
    multipleSelection: Boolean = false,
    onFileSelected: (List<String>) -> Unit
)

/**
 * 平台特定的相机组件
 */
@Composable
expect fun UnifyCameraComponent(
    modifier: Modifier = Modifier,
    onImageCaptured: (ByteArray) -> Unit,
    onError: (String) -> Unit = {}
)

/**
 * 平台特定的地图组件
 */
@Composable
expect fun UnifyMapComponent(
    modifier: Modifier = Modifier,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    zoom: Float = 10f,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> }
)

/**
 * 平台特定的WebView组件
 */
@Composable
expect fun UnifyWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageLoaded: (String) -> Unit = {},
    onError: (String) -> Unit = {}
)

/**
 * 平台特定的视频播放器
 */
@Composable
expect fun UnifyVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true,
    onPlaybackStateChanged: (Boolean) -> Unit = {}
)

/**
 * 平台特定的音频播放器
 */
@Composable
expect fun UnifyAudioPlayer(
    url: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true,
    onPlaybackStateChanged: (Boolean) -> Unit = {}
)

/**
 * 平台特定的二维码扫描器
 */
@Composable
expect fun UnifyQRCodeScanner(
    modifier: Modifier = Modifier,
    onQRCodeScanned: (String) -> Unit,
    onError: (String) -> Unit = {}
)

/**
 * 平台特定的生物识别认证
 */
@Composable
expect fun UnifyBiometricAuth(
    title: String = "生物识别认证",
    subtitle: String = "请验证您的身份",
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit = {},
    onAuthCancel: () -> Unit = {}
)

/**
 * 平台特定的分享组件
 */
expect fun shareContent(
    content: String,
    title: String = "",
    mimeType: String = "text/plain"
)

/**
 * 平台特定的通知组件
 */
expect fun showNotification(
    title: String,
    content: String,
    channelId: String = "default",
    importance: Int = 3
)

/**
 * 平台特定的振动功能
 */
expect fun vibrate(
    duration: Long = 100L,
    amplitude: Int = 255
)

/**
 * 平台特定的手电筒控制
 */
expect fun toggleFlashlight(enabled: Boolean)

/**
 * 平台特定的屏幕亮度控制
 */
expect fun setScreenBrightness(brightness: Float)

/**
 * 平台特定的音量控制
 */
expect fun setVolume(volume: Float, streamType: Int = 3)

/**
 * 平台特定的网络状态监听
 */
expect fun observeNetworkStatus(): kotlinx.coroutines.flow.Flow<NetworkType>

/**
 * 平台特定的电池状态监听
 */
expect fun observeBatteryStatus(): kotlinx.coroutines.flow.Flow<BatteryStatus>

/**
 * 电池状态数据类
 */
@Serializable
data class BatteryStatus(
    val level: Float,
    val isCharging: Boolean,
    val chargingType: ChargingType,
    val temperature: Float,
    val voltage: Int,
    val health: BatteryHealth
)

/**
 * 充电类型枚举
 */
@Serializable
enum class ChargingType {
    NONE,
    AC,
    USB,
    WIRELESS,
    UNKNOWN
}

/**
 * 电池健康状态枚举
 */
@Serializable
enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}

/**
 * 平台工具类
 */
object UnifyPlatformUtils {
    /**
     * 获取当前平台类型
     */
    fun getCurrentPlatform(): PlatformType {
        return getCurrentPlatformAdapter().getSystemInfo().platformType
    }
    
    /**
     * 检查平台特性支持
     */
    fun isFeatureSupported(feature: PlatformFeature): Boolean {
        return getCurrentPlatformAdapter().isFeatureSupported(feature)
    }
    
    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): DeviceInfo {
        return getCurrentPlatformAdapter().getDeviceInfo()
    }
    
    /**
     * 获取系统信息
     */
    fun getSystemInfo(): SystemInfo {
        return getCurrentPlatformAdapter().getSystemInfo()
    }
    
    /**
     * 判断是否为移动平台
     */
    fun isMobilePlatform(): Boolean {
        val platform = getCurrentPlatform()
        return platform == PlatformType.ANDROID || 
               platform == PlatformType.IOS || 
               platform == PlatformType.HARMONY_OS
    }
    
    /**
     * 判断是否为桌面平台
     */
    fun isDesktopPlatform(): Boolean {
        return getCurrentPlatform() == PlatformType.DESKTOP
    }
    
    /**
     * 判断是否为Web平台
     */
    fun isWebPlatform(): Boolean {
        return getCurrentPlatform() == PlatformType.WEB
    }
    
    /**
     * 判断是否为可穿戴设备
     */
    fun isWearablePlatform(): Boolean {
        return getCurrentPlatform() == PlatformType.WATCH
    }
    
    /**
     * 判断是否为TV平台
     */
    fun isTVPlatform(): Boolean {
        return getCurrentPlatform() == PlatformType.TV
    }
    
    /**
     * 判断是否为小程序平台
     */
    fun isMiniAppPlatform(): Boolean {
        return getCurrentPlatform() == PlatformType.MINI_APP
    }
}
