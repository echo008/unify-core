package com.unify.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Toolkit
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.coroutines.resume

/**
 * Desktop平台UnifyDeviceManager实现
 */
class UnifyDeviceManagerImpl : UnifyDeviceManager {
    private val toolkit = Toolkit.getDefaultToolkit()
    private val runtime = Runtime.getRuntime()
    private val osBean = ManagementFactory.getOperatingSystemMXBean()
    private val memoryBean = ManagementFactory.getMemoryMXBean()

    // 状态流管理
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    private val _batteryStatus = MutableStateFlow(getCurrentBatteryStatus())
    private val _locationUpdates = MutableStateFlow<LocationInfo?>(null)

    // 传感器监听器管理（Desktop环境下大多数传感器不可用）
    private val sensorListeners = mutableMapOf<SensorType, SensorListener>()

    override fun getDeviceInfo(): DeviceInfo {
        val memoryUsage = memoryBean.heapMemoryUsage
        val screenSize = toolkit.screenSize

        return DeviceInfo(
            deviceId = getDeviceId(),
            deviceName = getComputerName(),
            model = getDeviceModel(),
            manufacturer = getManufacturer(),
            osName = System.getProperty("os.name") ?: "Unknown",
            osVersion = System.getProperty("os.version") ?: "Unknown",
            screenWidth = screenSize.width,
            screenHeight = screenSize.height,
            screenDensity = toolkit.screenResolution / 96f,
            totalMemory = memoryUsage.max,
            availableMemory = memoryUsage.max - memoryUsage.used,
        )
    }

    override fun getPlatformName(): String = "Desktop"

    override fun getDeviceModel(): String {
        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        return when {
            osName.contains("windows") -> "Windows PC"
            osName.contains("mac") -> "Mac"
            osName.contains("linux") -> "Linux PC"
            else -> "Desktop Computer"
        }
    }

    override fun getOSVersion(): String {
        val osName = System.getProperty("os.name") ?: "Unknown"
        val osVersion = System.getProperty("os.version") ?: "Unknown"
        return "$osName $osVersion"
    }

    override fun getAppVersion(): String {
        return try {
            val packageName = this::class.java.`package`?.implementationVersion ?: "1.0.0"
            packageName
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    override suspend fun requestPermission(permission: DevicePermission): PermissionStatus {
        // Desktop环境下大多数权限是自动授予的
        return when (permission) {
            DevicePermission.CAMERA -> checkCameraAvailability()
            DevicePermission.MICROPHONE -> checkMicrophoneAvailability()
            DevicePermission.STORAGE -> PermissionStatus.GRANTED
            DevicePermission.LOCATION -> PermissionStatus.NOT_DETERMINED
            else -> PermissionStatus.GRANTED
        }
    }

    override suspend fun requestPermissions(permissions: List<DevicePermission>): Map<DevicePermission, PermissionStatus> {
        return permissions.associateWith { requestPermission(it) }
    }

    override fun checkPermission(permission: DevicePermission): PermissionStatus {
        return when (permission) {
            DevicePermission.CAMERA -> checkCameraAvailability()
            DevicePermission.MICROPHONE -> checkMicrophoneAvailability()
            DevicePermission.STORAGE -> PermissionStatus.GRANTED
            DevicePermission.LOCATION -> PermissionStatus.NOT_DETERMINED
            else -> PermissionStatus.GRANTED
        }
    }

    override fun observePermissionStatus(permission: DevicePermission): Flow<PermissionStatus> {
        return MutableStateFlow(checkPermission(permission)).asStateFlow()
    }

    override fun getSupportedSensors(): List<SensorType> {
        // Desktop环境下支持的传感器有限
        return listOf(
            // 大多数Desktop设备不支持物理传感器
            // 可以通过软件模拟一些传感器数据
        )
    }

    override fun startSensorMonitoring(
        sensorType: SensorType,
        listener: SensorListener,
    ) {
        sensorListeners[sensorType] = listener
        // Desktop环境下大多数传感器不可用，这里提供模拟实现
        when (sensorType) {
            SensorType.ACCELEROMETER -> {
                // 模拟加速度计数据
                listener.onSensorChanged(sensorType, floatArrayOf(0f, 0f, 9.8f), System.currentTimeMillis())
            }
            else -> {
                // 其他传感器暂不支持
            }
        }
    }

    override fun stopSensorMonitoring(sensorType: SensorType) {
        sensorListeners.remove(sensorType)
    }

    override fun isSensorAvailable(sensorType: SensorType): Boolean {
        // Desktop环境下大多数传感器不可用
        return false
    }

    override fun vibrate(durationMillis: Long) {
        // Desktop设备通常没有振动功能
        // 可以通过系统通知或声音提示替代
        toolkit.beep()
    }

    override fun setScreenBrightness(brightness: Float) {
        // Desktop环境下屏幕亮度通常由系统控制
        // 这里提供基础实现框架
        val brightnessValue = (brightness * 100).toInt().coerceIn(0, 100)

        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        try {
            when {
                osName.contains("windows") -> {
                    // Windows亮度控制
                    ProcessBuilder(
                        "powershell",
                        "-Command",
                        "(Get-WmiObject -Namespace root/WMI -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1,$brightnessValue)",
                    ).start()
                }
                osName.contains("mac") -> {
                    // macOS亮度控制
                    ProcessBuilder(
                        "osascript",
                        "-e",
                        "tell application \"System Events\" to set brightness of display 1 to $brightness",
                    ).start()
                }
                osName.contains("linux") -> {
                    // Linux亮度控制
                    val backlightPath = "/sys/class/backlight/intel_backlight/brightness"
                    if (File(backlightPath).exists()) {
                        ProcessBuilder("sh", "-c", "echo $brightnessValue > $backlightPath").start()
                    }
                }
            }
        } catch (e: Exception) {
            // 亮度控制失败，忽略错误
        }
    }

    override fun getScreenBrightness(): Float {
        // 简化实现，返回默认值
        return 0.8f
    }

    override fun setVolume(volume: Float) {
        // Desktop音量控制
        val volumeValue = (volume * 100).toInt().coerceIn(0, 100)

        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        try {
            when {
                osName.contains("windows") -> {
                    ProcessBuilder(
                        "powershell",
                        "-Command",
                        "[audio]::Volume = $volume",
                    ).start()
                }
                osName.contains("mac") -> {
                    ProcessBuilder(
                        "osascript",
                        "-e",
                        "set volume output volume $volumeValue",
                    ).start()
                }
                osName.contains("linux") -> {
                    ProcessBuilder("amixer", "set", "Master", "$volumeValue%").start()
                }
            }
        } catch (e: Exception) {
            // 音量控制失败，忽略错误
        }
    }

    override fun getVolume(): Float {
        // 简化实现，返回默认值
        return 0.7f
    }

    override fun showNotification(
        title: String,
        message: String,
        id: String,
    ) {
        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        try {
            when {
                osName.contains("windows") -> {
                    ProcessBuilder(
                        "powershell",
                        "-Command",
                        "Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.MessageBox]::Show('$message', '$title')",
                    ).start()
                }
                osName.contains("mac") -> {
                    ProcessBuilder(
                        "osascript",
                        "-e",
                        "display notification \"$message\" with title \"$title\"",
                    ).start()
                }
                osName.contains("linux") -> {
                    ProcessBuilder("notify-send", title, message).start()
                }
            }
        } catch (e: Exception) {
            // 通知显示失败，使用控制台输出
            println("Notification: $title - $message")
        }
    }

    override suspend fun takePicture(): String? {
        // Desktop环境下需要访问摄像头
        return suspendCancellableCoroutine { continuation ->
            try {
                // 这里需要使用Java的摄像头API或第三方库
                // 简化实现，返回null表示不支持
                continuation.resume(null)
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    override suspend fun recordAudio(durationMillis: Long): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                // 这里需要使用Java的音频录制API
                // 简化实现，返回null表示不支持
                continuation.resume(null)
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    override suspend fun getCurrentLocation(): LocationInfo? {
        // Desktop环境下通常通过IP地址获取大概位置
        return suspendCancellableCoroutine { continuation ->
            try {
                // 简化实现，实际应用中可以通过IP地理位置服务获取
                continuation.resume(null)
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    override fun observeLocationUpdates(): Flow<LocationInfo> = _locationUpdates.asStateFlow().filterNotNull()

    override fun isNetworkAvailable(): Boolean {
        return try {
            InetAddress.getByName("google.com").isReachable(5000)
        } catch (e: Exception) {
            false
        }
    }

    override fun getNetworkType(): NetworkType {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    return when {
                        networkInterface.name.contains("eth", ignoreCase = true) -> NetworkType.ETHERNET
                        networkInterface.name.contains("wifi", ignoreCase = true) ||
                            networkInterface.name.contains("wlan", ignoreCase = true) -> NetworkType.WIFI
                        else -> NetworkType.ETHERNET
                    }
                }
            }
            NetworkType.UNKNOWN
        } catch (e: Exception) {
            NetworkType.UNKNOWN
        }
    }

    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()

    override fun getBatteryLevel(): Float {
        // Desktop设备通常连接电源，电池信息获取较复杂
        return 1.0f // 假设始终满电
    }

    override fun isBatteryCharging(): Boolean {
        // Desktop设备通常连接电源
        return true
    }

    override fun observeBatteryStatus(): Flow<BatteryStatus> = _batteryStatus.asStateFlow()

    override fun getAvailableStorage(): Long {
        val userHome = File(System.getProperty("user.home"))
        return userHome.freeSpace
    }

    override fun getTotalStorage(): Long {
        val userHome = File(System.getProperty("user.home"))
        return userHome.totalSpace
    }

    override fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }

    private fun getDeviceId(): String {
        return try {
            val hostName = InetAddress.getLocalHost().hostName
            val mac =
                NetworkInterface.getNetworkInterfaces().asSequence()
                    .firstOrNull { it.hardwareAddress != null }
                    ?.hardwareAddress
                    ?.joinToString(":") { "%02x".format(it) }

            "${hostName}_${mac ?: "unknown"}"
        } catch (e: Exception) {
            "desktop_${System.currentTimeMillis()}"
        }
    }

    private fun getComputerName(): String {
        return try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            System.getProperty("user.name") ?: "Desktop"
        }
    }

    private fun getManufacturer(): String {
        val osName = System.getProperty("os.name")?.lowercase() ?: ""
        return when {
            osName.contains("windows") -> "Microsoft"
            osName.contains("mac") -> "Apple"
            osName.contains("linux") -> "Linux"
            else -> "Unknown"
        }
    }

    private fun checkCameraAvailability(): PermissionStatus {
        return try {
            // 简化检查，实际应用中需要检查摄像头设备
            PermissionStatus.GRANTED
        } catch (e: Exception) {
            PermissionStatus.DENIED
        }
    }

    private fun checkMicrophoneAvailability(): PermissionStatus {
        return try {
            // 简化检查，实际应用中需要检查麦克风设备
            PermissionStatus.GRANTED
        } catch (e: Exception) {
            PermissionStatus.DENIED
        }
    }

    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isNetworkAvailable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }

    private fun getCurrentBatteryStatus(): BatteryStatus {
        return BatteryStatus(
            level = getBatteryLevel(),
            isCharging = isBatteryCharging(),
            chargingType = ChargingType.AC,
            temperature = 25.0f,
        )
    }
}

// 扩展函数用于过滤非空值
private fun <T> Flow<T?>.filterNotNull(): Flow<T> =
    kotlinx.coroutines.flow.flow {
        collect { value ->
            if (value != null) emit(value)
        }
    }

actual object UnifyDeviceManagerFactory {
    actual fun create(): UnifyDeviceManager {
        return UnifyDeviceManagerImpl()
    }
}
