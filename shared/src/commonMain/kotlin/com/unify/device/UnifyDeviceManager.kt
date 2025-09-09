package com.unify.device

import kotlinx.coroutines.flow.Flow

/**
 * Unify跨平台设备管理器接口
 * 统一管理设备信息、权限、传感器和硬件功能
 */
interface UnifyDeviceManager {
    // 设备信息
    fun getDeviceInfo(): DeviceInfo

    fun getPlatformName(): String

    fun getDeviceModel(): String

    fun getOSVersion(): String

    fun getAppVersion(): String

    // 权限管理
    suspend fun requestPermission(permission: DevicePermission): PermissionStatus

    suspend fun requestPermissions(permissions: List<DevicePermission>): Map<DevicePermission, PermissionStatus>

    fun checkPermission(permission: DevicePermission): PermissionStatus

    fun observePermissionStatus(permission: DevicePermission): Flow<PermissionStatus>

    // 传感器管理
    fun getSupportedSensors(): List<SensorType>

    fun startSensorMonitoring(
        sensorType: SensorType,
        listener: SensorListener,
    )

    fun stopSensorMonitoring(sensorType: SensorType)

    fun isSensorAvailable(sensorType: SensorType): Boolean

    // 系统功能
    fun vibrate(durationMillis: Long)

    fun setScreenBrightness(brightness: Float)

    fun getScreenBrightness(): Float

    fun setVolume(volume: Float)

    fun getVolume(): Float

    fun showNotification(
        title: String,
        message: String,
        id: String,
    )

    // 硬件访问
    suspend fun takePicture(): String? // 返回图片路径

    suspend fun recordAudio(durationMillis: Long): String? // 返回音频文件路径

    suspend fun getCurrentLocation(): LocationInfo?

    fun observeLocationUpdates(): Flow<LocationInfo>

    // 网络状态
    fun isNetworkAvailable(): Boolean

    fun getNetworkType(): NetworkType

    fun observeNetworkStatus(): Flow<NetworkStatus>

    // 电池状态
    fun getBatteryLevel(): Float

    fun isBatteryCharging(): Boolean

    fun observeBatteryStatus(): Flow<BatteryStatus>

    // 存储信息
    fun getAvailableStorage(): Long

    fun getTotalStorage(): Long

    fun getUsedStorage(): Long
}

/**
 * 设备信息数据类
 */
data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val model: String,
    val manufacturer: String,
    val osName: String,
    val osVersion: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val screenDensity: Float,
    val totalMemory: Long,
    val availableMemory: Long,
)

/**
 * 设备权限枚举
 */
enum class DevicePermission {
    CAMERA,
    MICROPHONE,
    LOCATION,
    STORAGE,
    CONTACTS,
    CALENDAR,
    PHONE,
    SMS,
    NOTIFICATIONS,
    BLUETOOTH,
    NFC,
    BIOMETRIC,
}

/**
 * 权限状态枚举
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
    RESTRICTED,
}

/**
 * 传感器类型枚举
 */
enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    GRAVITY,
    LINEAR_ACCELERATION,
    ROTATION_VECTOR,
    ORIENTATION,
    PROXIMITY,
    LIGHT,
    PRESSURE,
    TEMPERATURE,
    HUMIDITY,
    HEART_RATE,
    STEP_COUNTER,
}

/**
 * 传感器监听器接口
 */
interface SensorListener {
    fun onSensorChanged(
        sensorType: SensorType,
        values: FloatArray,
        timestamp: Long,
    )

    fun onAccuracyChanged(
        sensorType: SensorType,
        accuracy: Int,
    )
}

/**
 * 位置信息数据类
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val timestamp: Long,
)

/**
 * 网络类型枚举
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
    UNKNOWN,
}

/**
 * 网络状态枚举
 */
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
}

/**
 * 电池状态数据类
 */
data class BatteryStatus(
    val level: Float,
    val isCharging: Boolean,
    val chargingType: ChargingType,
    val temperature: Float,
)

/**
 * 充电类型枚举
 */
enum class ChargingType {
    NONE,
    AC,
    USB,
    WIRELESS,
}

/**
 * 设备管理器工厂
 */
expect object UnifyDeviceManagerFactory {
    fun create(): UnifyDeviceManager
}
