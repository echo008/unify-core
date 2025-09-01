package com.unify.device

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * 统一设备功能管理器核心接口
 * 提供跨平台的权限管理、设备信息获取和传感器访问功能
 */
interface UnifyDeviceManager {
    
    // 权限管理
    val permissions: UnifyPermissionManager
    
    // 设备信息
    val deviceInfo: UnifyDeviceInfo
    
    // 传感器管理
    val sensors: UnifySensorManager
    
    // 系统功能
    val systemFeatures: UnifySystemFeatures
    
    // 硬件访问
    val hardware: UnifyHardwareManager
    
    /**
     * 初始化设备管理器
     */
    suspend fun initialize()
    
    /**
     * 清理资源
     */
    suspend fun cleanup()
}

/**
 * 统一权限管理接口
 */
interface UnifyPermissionManager {
    
    /**
     * 检查权限状态
     */
    suspend fun checkPermission(permission: UnifyPermission): UnifyPermissionStatus
    
    /**
     * 批量检查权限状态
     */
    suspend fun checkPermissions(permissions: List<UnifyPermission>): Map<UnifyPermission, UnifyPermissionStatus>
    
    /**
     * 请求权限
     */
    suspend fun requestPermission(permission: UnifyPermission): UnifyPermissionResult
    
    /**
     * 批量请求权限
     */
    suspend fun requestPermissions(permissions: List<UnifyPermission>): Map<UnifyPermission, UnifyPermissionResult>
    
    /**
     * 打开应用设置页面
     */
    suspend fun openAppSettings()
    
    /**
     * 检查是否应该显示权限说明
     */
    suspend fun shouldShowPermissionRationale(permission: UnifyPermission): Boolean
    
    /**
     * 观察权限状态变化
     */
    fun observePermissionChanges(): Flow<UnifyPermissionChange>
}

/**
 * 统一设备信息接口
 */
interface UnifyDeviceInfo {
    
    /**
     * 获取设备基本信息
     */
    suspend fun getDeviceInfo(): UnifyDeviceDetails
    
    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): UnifySystemInfo
    
    /**
     * 获取硬件信息
     */
    suspend fun getHardwareInfo(): UnifyHardwareInfo
    
    /**
     * 获取网络信息
     */
    suspend fun getNetworkInfo(): UnifyNetworkInfo
    
    /**
     * 获取存储信息
     */
    suspend fun getStorageInfo(): UnifyStorageInfo
    
    /**
     * 获取电池信息
     */
    suspend fun getBatteryInfo(): UnifyBatteryInfo
    
    /**
     * 获取显示信息
     */
    suspend fun getDisplayInfo(): UnifyDisplayInfo
    
    /**
     * 检查设备功能支持
     */
    suspend fun isFeatureSupported(feature: UnifyDeviceFeature): Boolean
    
    /**
     * 观察设备状态变化
     */
    fun observeDeviceChanges(): Flow<UnifyDeviceChange>
}

/**
 * 统一传感器管理接口
 */
interface UnifySensorManager {
    
    /**
     * 获取可用传感器列表
     */
    suspend fun getAvailableSensors(): List<UnifySensorInfo>
    
    /**
     * 检查传感器是否可用
     */
    suspend fun isSensorAvailable(sensorType: UnifySensorType): Boolean
    
    /**
     * 开始监听传感器数据
     */
    suspend fun startSensorListening(
        sensorType: UnifySensorType,
        samplingRate: UnifySensorSamplingRate = UnifySensorSamplingRate.NORMAL
    ): Flow<UnifySensorData>
    
    /**
     * 停止监听传感器数据
     */
    suspend fun stopSensorListening(sensorType: UnifySensorType)
    
    /**
     * 获取传感器详细信息
     */
    suspend fun getSensorDetails(sensorType: UnifySensorType): UnifySensorInfo?
    
    /**
     * 设置传感器监听配置
     */
    suspend fun configureSensor(sensorType: UnifySensorType, config: UnifySensorConfig)
}

/**
 * 统一系统功能接口
 */
interface UnifySystemFeatures {
    
    /**
     * 振动功能
     */
    suspend fun vibrate(duration: Long)
    suspend fun vibratePattern(pattern: LongArray, repeat: Int = -1)
    suspend fun cancelVibration()
    
    /**
     * 音频功能
     */
    suspend fun playSystemSound(soundType: UnifySystemSound)
    suspend fun setVolume(streamType: UnifyAudioStream, volume: Float)
    suspend fun getVolume(streamType: UnifyAudioStream): Float
    
    /**
     * 屏幕功能
     */
    suspend fun setBrightness(brightness: Float) // 0.0 - 1.0
    suspend fun getBrightness(): Float
    suspend fun keepScreenOn(keepOn: Boolean)
    suspend fun setScreenOrientation(orientation: UnifyScreenOrientation)
    
    /**
     * 通知功能
     */
    suspend fun showNotification(notification: UnifyNotification)
    suspend fun cancelNotification(id: String)
    suspend fun clearAllNotifications()
    
    /**
     * 剪贴板功能
     */
    suspend fun copyToClipboard(text: String)
    suspend fun getFromClipboard(): String?
    
    /**
     * 分享功能
     */
    suspend fun shareText(text: String, title: String? = null)
    suspend fun shareFile(filePath: String, mimeType: String, title: String? = null)
}

/**
 * 统一硬件管理接口
 */
interface UnifyHardwareManager {
    
    /**
     * 相机功能
     */
    suspend fun isCameraAvailable(): Boolean
    suspend fun takePicture(): UnifyCameraResult
    suspend fun recordVideo(maxDuration: Long): UnifyCameraResult
    
    /**
     * 麦克风功能
     */
    suspend fun isMicrophoneAvailable(): Boolean
    suspend fun startRecording(config: UnifyAudioConfig): Flow<UnifyAudioData>
    suspend fun stopRecording()
    
    /**
     * 位置服务
     */
    suspend fun isLocationAvailable(): Boolean
    suspend fun getCurrentLocation(accuracy: UnifyLocationAccuracy): UnifyLocationResult
    suspend fun startLocationUpdates(config: UnifyLocationConfig): Flow<UnifyLocationData>
    suspend fun stopLocationUpdates()
    
    /**
     * 蓝牙功能
     */
    suspend fun isBluetoothAvailable(): Boolean
    suspend fun isBluetoothEnabled(): Boolean
    suspend fun enableBluetooth()
    suspend fun scanBluetoothDevices(): Flow<UnifyBluetoothDevice>
    
    /**
     * NFC功能
     */
    suspend fun isNFCAvailable(): Boolean
    suspend fun isNFCEnabled(): Boolean
    suspend fun readNFCTag(): Flow<UnifyNFCData>
    
    /**
     * 生物识别
     */
    suspend fun isBiometricAvailable(): Boolean
    suspend fun authenticateWithBiometric(config: UnifyBiometricConfig): UnifyBiometricResult
}

// 枚举定义

/**
 * 权限类型枚举
 */
enum class UnifyPermission {
    CAMERA,
    MICROPHONE,
    LOCATION_FINE,
    LOCATION_COARSE,
    STORAGE_READ,
    STORAGE_WRITE,
    CONTACTS_READ,
    CONTACTS_WRITE,
    PHONE_CALL,
    SMS_SEND,
    SMS_READ,
    CALENDAR_READ,
    CALENDAR_WRITE,
    BLUETOOTH,
    NFC,
    BIOMETRIC,
    NOTIFICATIONS,
    VIBRATE,
    INTERNET,
    NETWORK_STATE
}

/**
 * 权限状态枚举
 */
enum class UnifyPermissionStatus {
    GRANTED,        // 已授权
    DENIED,         // 已拒绝
    NOT_DETERMINED, // 未确定
    RESTRICTED      // 受限制
}

/**
 * 权限请求结果枚举
 */
enum class UnifyPermissionResult {
    GRANTED,                // 用户授权
    DENIED,                 // 用户拒绝
    DENIED_DONT_ASK_AGAIN, // 用户拒绝且不再询问
    ERROR                   // 请求出错
}

/**
 * 传感器类型枚举
 */
enum class UnifySensorType {
    ACCELEROMETER,      // 加速度计
    GYROSCOPE,          // 陀螺仪
    MAGNETOMETER,       // 磁力计
    GRAVITY,            // 重力传感器
    LINEAR_ACCELERATION,// 线性加速度
    ROTATION_VECTOR,    // 旋转矢量
    ORIENTATION,        // 方向传感器
    PROXIMITY,          // 接近传感器
    LIGHT,              // 光线传感器
    PRESSURE,           // 气压传感器
    TEMPERATURE,        // 温度传感器
    HUMIDITY,           // 湿度传感器
    HEART_RATE,         // 心率传感器
    STEP_COUNTER,       // 计步器
    STEP_DETECTOR       // 步行检测器
}

/**
 * 传感器采样率枚举
 */
enum class UnifySensorSamplingRate {
    FASTEST,    // 最快
    GAME,       // 游戏
    UI,         // UI
    NORMAL      // 正常
}

/**
 * 设备功能枚举
 */
enum class UnifyDeviceFeature {
    CAMERA,
    CAMERA_FRONT,
    MICROPHONE,
    GPS,
    BLUETOOTH,
    BLUETOOTH_LE,
    NFC,
    WIFI,
    CELLULAR,
    FINGERPRINT,
    FACE_ID,
    ACCELEROMETER,
    GYROSCOPE,
    COMPASS,
    BAROMETER,
    TELEPHONY,
    SMS,
    VIBRATOR,
    TOUCHSCREEN,
    MULTITOUCH
}

/**
 * 系统声音类型枚举
 */
enum class UnifySystemSound {
    CLICK,
    BEEP,
    ERROR,
    SUCCESS,
    WARNING,
    NOTIFICATION
}

/**
 * 音频流类型枚举
 */
enum class UnifyAudioStream {
    MUSIC,
    RING,
    NOTIFICATION,
    ALARM,
    VOICE_CALL,
    SYSTEM
}

/**
 * 屏幕方向枚举
 */
enum class UnifyScreenOrientation {
    PORTRAIT,
    LANDSCAPE,
    PORTRAIT_REVERSE,
    LANDSCAPE_REVERSE,
    AUTO
}

/**
 * 位置精度枚举
 */
enum class UnifyLocationAccuracy {
    HIGH,       // 高精度 (GPS)
    MEDIUM,     // 中等精度 (网络)
    LOW,        // 低精度 (被动)
    BALANCED    // 平衡模式
}

// 数据类定义

/**
 * 权限变化数据类
 */
@Serializable
data class UnifyPermissionChange(
    val permission: UnifyPermission,
    val oldStatus: UnifyPermissionStatus,
    val newStatus: UnifyPermissionStatus,
    val timestamp: Long
)

/**
 * 设备详细信息数据类
 */
@Serializable
data class UnifyDeviceDetails(
    val deviceId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val product: String,
    val board: String,
    val hardware: String,
    val serial: String?,
    val fingerprint: String,
    val isEmulator: Boolean,
    val isRooted: Boolean
)

/**
 * 系统信息数据类
 */
@Serializable
data class UnifySystemInfo(
    val osName: String,
    val osVersion: String,
    val osApiLevel: Int,
    val kernelVersion: String?,
    val buildId: String,
    val buildTime: Long,
    val locale: String,
    val timezone: String,
    val uptime: Long
)

/**
 * 硬件信息数据类
 */
@Serializable
data class UnifyHardwareInfo(
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuFrequency: Long,
    val totalMemory: Long,
    val availableMemory: Long,
    val totalStorage: Long,
    val availableStorage: Long,
    val screenWidth: Int,
    val screenHeight: Int,
    val screenDensity: Float,
    val screenRefreshRate: Float
)

/**
 * 网络信息数据类
 */
@Serializable
data class UnifyNetworkInfo(
    val isConnected: Boolean,
    val connectionType: String,
    val isWifi: Boolean,
    val isCellular: Boolean,
    val isEthernet: Boolean,
    val networkOperator: String?,
    val ipAddress: String?,
    val macAddress: String?
)

/**
 * 存储信息数据类
 */
@Serializable
data class UnifyStorageInfo(
    val internalTotal: Long,
    val internalAvailable: Long,
    val internalUsed: Long,
    val externalTotal: Long?,
    val externalAvailable: Long?,
    val externalUsed: Long?,
    val cacheSize: Long,
    val dataSize: Long
)

/**
 * 电池信息数据类
 */
@Serializable
data class UnifyBatteryInfo(
    val level: Int,          // 0-100
    val isCharging: Boolean,
    val chargingType: String, // USB, AC, WIRELESS
    val health: String,       // GOOD, OVERHEAT, DEAD, etc.
    val technology: String,   // Li-ion, Li-poly, etc.
    val temperature: Float,   // 摄氏度
    val voltage: Float,       // 伏特
    val capacity: Int         // mAh
)

/**
 * 显示信息数据类
 */
@Serializable
data class UnifyDisplayInfo(
    val width: Int,
    val height: Int,
    val density: Float,
    val densityDpi: Int,
    val refreshRate: Float,
    val orientation: Int,
    val brightness: Float,
    val isHdr: Boolean,
    val colorSpace: String,
    val cutoutInfo: String?
)

/**
 * 设备变化数据类
 */
@Serializable
data class UnifyDeviceChange(
    val changeType: String,
    val oldValue: String?,
    val newValue: String?,
    val timestamp: Long
)

/**
 * 传感器信息数据类
 */
@Serializable
data class UnifySensorInfo(
    val type: UnifySensorType,
    val name: String,
    val vendor: String,
    val version: Int,
    val maxRange: Float,
    val resolution: Float,
    val power: Float,
    val minDelay: Int,
    val maxDelay: Int,
    val isWakeUp: Boolean
)

/**
 * 传感器数据类
 */
@Serializable
data class UnifySensorData(
    val sensorType: UnifySensorType,
    val values: FloatArray,
    val accuracy: Int,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as UnifySensorData
        
        if (sensorType != other.sensorType) return false
        if (!values.contentEquals(other.values)) return false
        if (accuracy != other.accuracy) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = sensorType.hashCode()
        result = 31 * result + values.contentHashCode()
        result = 31 * result + accuracy
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

/**
 * 传感器配置数据类
 */
@Serializable
data class UnifySensorConfig(
    val samplingRate: UnifySensorSamplingRate,
    val batchSize: Int = 1,
    val maxLatency: Long = 0,
    val enableWakeUp: Boolean = false
)

/**
 * 通知数据类
 */
@Serializable
data class UnifyNotification(
    val id: String,
    val title: String,
    val content: String,
    val iconPath: String? = null,
    val priority: Int = 0,
    val autoCancel: Boolean = true,
    val vibrate: Boolean = false,
    val sound: Boolean = true,
    val lights: Boolean = false,
    val actions: List<UnifyNotificationAction> = emptyList()
)

/**
 * 通知操作数据类
 */
@Serializable
data class UnifyNotificationAction(
    val id: String,
    val title: String,
    val iconPath: String? = null
)

/**
 * 相机结果数据类
 */
@Serializable
data class UnifyCameraResult(
    val isSuccess: Boolean,
    val filePath: String?,
    val error: String?
)

/**
 * 音频配置数据类
 */
@Serializable
data class UnifyAudioConfig(
    val sampleRate: Int = 44100,
    val channels: Int = 1,
    val bitRate: Int = 128000,
    val format: String = "AAC"
)

/**
 * 音频数据类
 */
@Serializable
data class UnifyAudioData(
    val data: ByteArray,
    val timestamp: Long,
    val sampleRate: Int,
    val channels: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as UnifyAudioData
        
        if (!data.contentEquals(other.data)) return false
        if (timestamp != other.timestamp) return false
        if (sampleRate != other.sampleRate) return false
        if (channels != other.channels) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channels
        return result
    }
}

/**
 * 位置配置数据类
 */
@Serializable
data class UnifyLocationConfig(
    val accuracy: UnifyLocationAccuracy,
    val updateInterval: Long = 10000,
    val minDistance: Float = 0f,
    val timeout: Long = 30000
)

/**
 * 位置结果数据类
 */
@Serializable
data class UnifyLocationResult(
    val isSuccess: Boolean,
    val location: UnifyLocationData?,
    val error: String?
)

/**
 * 位置数据类
 */
@Serializable
data class UnifyLocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float,
    val bearing: Float?,
    val speed: Float?,
    val timestamp: Long,
    val provider: String
)

/**
 * 蓝牙设备数据类
 */
@Serializable
data class UnifyBluetoothDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val deviceType: String,
    val bondState: String,
    val services: List<String>
)

/**
 * NFC数据类
 */
@Serializable
data class UnifyNFCData(
    val id: ByteArray,
    val type: String,
    val data: ByteArray,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        
        other as UnifyNFCData
        
        if (!id.contentEquals(other.id)) return false
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = id.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

/**
 * 生物识别配置数据类
 */
@Serializable
data class UnifyBiometricConfig(
    val title: String,
    val subtitle: String,
    val description: String,
    val negativeButtonText: String,
    val allowDeviceCredential: Boolean = false
)

/**
 * 生物识别结果数据类
 */
@Serializable
data class UnifyBiometricResult(
    val isSuccess: Boolean,
    val error: String?,
    val errorCode: Int = 0
)

/**
 * 设备管理器配置数据类
 */
@Serializable
data class UnifyDeviceConfig(
    val enablePermissionMonitoring: Boolean = true,
    val enableSensorMonitoring: Boolean = true,
    val enableBatteryMonitoring: Boolean = true,
    val enableLocationServices: Boolean = true,
    val sensorUpdateInterval: Long = 100,
    val locationUpdateInterval: Long = 10000,
    val batteryUpdateInterval: Long = 30000
)

/**
 * 设备管理器工厂
 */
expect object UnifyDeviceManagerFactory {
    fun create(config: UnifyDeviceConfig = UnifyDeviceConfig()): UnifyDeviceManager
}
