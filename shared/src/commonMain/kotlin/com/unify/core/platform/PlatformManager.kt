package com.unify.core.platform

import kotlinx.coroutines.flow.Flow

/**
 * 平台管理器 - 统一平台抽象层
 * 使用expect/actual机制实现跨平台功能
 * 支持 Android、iOS、Web、Desktop、HarmonyOS、小程序、Watch、TV 全平台
 */
expect object PlatformManager {
    
    /**
     * 初始化平台管理器
     */
    fun initialize()
    
    /**
     * 获取平台类型
     */
    fun getPlatformType(): PlatformType
    
    /**
     * 获取平台名称
     */
    fun getPlatformName(): String
    
    /**
     * 获取平台版本
     */
    fun getPlatformVersion(): String
    
    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): DeviceInfo
    
    /**
     * 获取屏幕信息
     */
    fun getScreenInfo(): ScreenInfo
    
    /**
     * 获取系统能力信息
     */
    fun getSystemCapabilities(): SystemCapabilities
    
    /**
     * 获取网络状态
     */
    fun getNetworkStatus(): NetworkStatus
    
    /**
     * 监听网络状态变化
     */
    fun observeNetworkStatus(): Flow<NetworkStatus>
    
    /**
     * 获取存储信息
     */
    fun getStorageInfo(): StorageInfo
    
    /**
     * 获取性能信息
     */
    fun getPerformanceInfo(): PerformanceInfo
    
    /**
     * 显示原生对话框
     */
    suspend fun showNativeDialog(config: DialogConfig): DialogResult
    
    /**
     * 调用平台特定功能
     */
    suspend fun invokePlatformFeature(feature: PlatformFeature): PlatformResult
    
    /**
     * 获取平台特定配置
     */
    fun getPlatformConfig(): PlatformConfig
}

/**
 * 平台类型枚举
 */
enum class PlatformType {
    ANDROID,
    IOS,
    WEB,
    DESKTOP,
    HARMONY_OS,
    MINI_PROGRAM,
    WATCH,
    TV,
    UNKNOWN
}

/**
 * 设备信息数据类
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val systemName: String,
    val systemVersion: String,
    val deviceId: String,
    val isEmulator: Boolean = false
)

/**
 * 屏幕信息数据类
 */
data class ScreenInfo(
    val width: Int,
    val height: Int,
    val density: Float,
    val orientation: Orientation,
    val refreshRate: Float = 60f,
    val colorDepth: Int = 24,
    val safeAreaInsets: SafeAreaInsets = SafeAreaInsets()
)

/**
 * 安全区域内边距
 */
data class SafeAreaInsets(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
)

/**
 * 屏幕方向枚举
 */
enum class Orientation {
    PORTRAIT,
    LANDSCAPE,
    PORTRAIT_UPSIDE_DOWN,
    LANDSCAPE_LEFT,
    LANDSCAPE_RIGHT,
    UNKNOWN
}

/**
 * 系统能力信息
 */
data class SystemCapabilities(
    val isTouchSupported: Boolean,
    val isKeyboardSupported: Boolean,
    val isMouseSupported: Boolean,
    val isCameraSupported: Boolean,
    val isMicrophoneSupported: Boolean,
    val isLocationSupported: Boolean,
    val isNotificationSupported: Boolean,
    val isFileSystemSupported: Boolean,
    val isBiometricSupported: Boolean,
    val isNFCSupported: Boolean,
    val isBluetoothSupported: Boolean,
    val supportedSensors: List<SensorType> = emptyList()
)

/**
 * 传感器类型
 */
enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    PROXIMITY,
    LIGHT,
    PRESSURE,
    TEMPERATURE,
    HUMIDITY
}

/**
 * 网络状态枚举
 */
enum class NetworkStatus {
    CONNECTED_WIFI,
    CONNECTED_CELLULAR,
    CONNECTED_ETHERNET,
    CONNECTED_UNKNOWN,
    DISCONNECTED,
    UNKNOWN
}

/**
 * 存储信息
 */
data class StorageInfo(
    val totalSpace: Long,
    val availableSpace: Long,
    val usedSpace: Long,
    val isExternalStorageAvailable: Boolean = false
)

/**
 * 性能信息
 */
data class PerformanceInfo(
    val cpuUsage: Float,
    val memoryUsage: MemoryUsage,
    val batteryLevel: Float = -1f,
    val thermalState: ThermalState = ThermalState.NORMAL
)

/**
 * 内存使用信息
 */
data class MemoryUsage(
    val totalMemory: Long,
    val availableMemory: Long,
    val usedMemory: Long,
    val appMemoryUsage: Long
)

/**
 * 热状态枚举
 */
enum class ThermalState {
    NORMAL,
    FAIR,
    SERIOUS,
    CRITICAL
}

/**
 * 对话框配置
 */
data class DialogConfig(
    val title: String,
    val message: String,
    val buttons: List<DialogButton>,
    val type: DialogType = DialogType.ALERT
)

/**
 * 对话框按钮
 */
data class DialogButton(
    val text: String,
    val style: ButtonStyle = ButtonStyle.DEFAULT,
    val action: () -> Unit = {}
)

/**
 * 按钮样式
 */
enum class ButtonStyle {
    DEFAULT,
    DESTRUCTIVE,
    CANCEL
}

/**
 * 对话框类型
 */
enum class DialogType {
    ALERT,
    CONFIRMATION,
    INPUT,
    CUSTOM
}

/**
 * 对话框结果
 */
data class DialogResult(
    val buttonIndex: Int,
    val inputText: String? = null,
    val cancelled: Boolean = false,
    val error: String? = null
)

/**
 * 平台特定功能
 */
sealed class PlatformFeature {
    object Vibrate : PlatformFeature()
    data class OpenUrl(val url: String) : PlatformFeature()
    data class ShareContent(val content: String, val type: String = "text/plain") : PlatformFeature()
    data class SaveToGallery(val data: ByteArray, val filename: String) : PlatformFeature()
    data class RequestPermission(val permission: String) : PlatformFeature()
    object GetLocation : PlatformFeature()
    data class ShowNotification(val title: String, val message: String) : PlatformFeature()
}

/**
 * 平台结果
 */
data class PlatformResult(
    val success: Boolean,
    val data: Any? = null,
    val error: String? = null
)

/**
 * 平台配置
 */
data class PlatformConfig(
    val platformType: PlatformType,
    val supportedFeatures: Set<String>,
    val limitations: Set<String>,
    val optimizations: Map<String, Any>
)
