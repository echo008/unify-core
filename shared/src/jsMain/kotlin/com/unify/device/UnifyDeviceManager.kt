package com.unify.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import org.w3c.dom.Window
import org.w3c.dom.Navigator
import org.w3c.dom.Screen
import org.w3c.dom.Document
import org.w3c.dom.events.Event
import kotlinx.browser.document
import kotlinx.browser.window

// Web平台的统一设备管理器实现
actual class UnifyDeviceManagerImpl : UnifyDeviceManager {
    
    actual override val permissions: UnifyPermissionManager = WebPermissionManager()
    actual override val deviceInfo: UnifyDeviceInfo = WebDeviceInfo()
    actual override val sensors: UnifySensorManager = WebSensorManager()
    actual override val systemFeatures: UnifySystemFeatures = WebSystemFeatures()
    actual override val hardware: UnifyHardwareManager = WebHardwareManager()
    
    actual override suspend fun initialize() {
        permissions.initialize()
        sensors.initialize()
        hardware.initialize()
    }
    
    actual override suspend fun cleanup() {
        sensors.cleanup()
        hardware.cleanup()
    }
}

// Web权限管理器实现
class WebPermissionManager : UnifyPermissionManager {
    
    private val permissionStatusFlow = MutableStateFlow<Map<UnifyPermission, UnifyPermissionStatus>>(emptyMap())
    
    override suspend fun initialize() {
        refreshAllPermissionStatus()
    }
    
    override suspend fun checkPermission(permission: UnifyPermission): UnifyPermissionStatus {
        return when (permission) {
            UnifyPermission.CAMERA -> checkWebPermission("camera")
            UnifyPermission.MICROPHONE -> checkWebPermission("microphone")
            UnifyPermission.LOCATION_FINE, UnifyPermission.LOCATION_COARSE -> checkWebPermission("geolocation")
            UnifyPermission.NOTIFICATIONS -> checkWebPermission("notifications")
            UnifyPermission.CLIPBOARD -> checkWebPermission("clipboard-read")
            else -> UnifyPermissionStatus.NOT_DETERMINED
        }
    }
    
    override suspend fun requestPermission(permission: UnifyPermission): UnifyPermissionResult {
        return when (permission) {
            UnifyPermission.CAMERA -> requestWebPermission("camera")
            UnifyPermission.MICROPHONE -> requestWebPermission("microphone")
            UnifyPermission.LOCATION_FINE, UnifyPermission.LOCATION_COARSE -> requestWebPermission("geolocation")
            UnifyPermission.NOTIFICATIONS -> requestNotificationPermission()
            else -> UnifyPermissionResult.DENIED
        }
    }
    
    override suspend fun requestPermissions(permissions: List<UnifyPermission>): Map<UnifyPermission, UnifyPermissionResult> {
        val results = mutableMapOf<UnifyPermission, UnifyPermissionResult>()
        permissions.forEach { permission ->
            results[permission] = requestPermission(permission)
        }
        return results
    }
    
    override fun observePermissionChanges(): Flow<UnifyPermissionChange> = flow {
        // Web权限变化监听实现
    }
    
    override suspend fun shouldShowPermissionRationale(permission: UnifyPermission): Boolean {
        return false // Web不需要显示权限说明
    }
    
    private fun checkWebPermission(permissionName: String): UnifyPermissionStatus {
        return try {
            val navigator = window.navigator.asDynamic()
            if (navigator.permissions != null) {
                // 使用Permissions API检查权限状态
                UnifyPermissionStatus.NOT_DETERMINED // 占位符实现
            } else {
                UnifyPermissionStatus.NOT_DETERMINED
            }
        } catch (e: Exception) {
            UnifyPermissionStatus.NOT_DETERMINED
        }
    }
    
    private suspend fun requestWebPermission(permissionName: String): UnifyPermissionResult {
        return try {
            when (permissionName) {
                "camera", "microphone" -> {
                    // 通过getUserMedia请求媒体权限
                    val constraints = js("{}")
                    if (permissionName == "camera") {
                        constraints.video = true
                    } else {
                        constraints.audio = true
                    }
                    
                    val navigator = window.navigator.asDynamic()
                    if (navigator.mediaDevices?.getUserMedia != null) {
                        // 实际的getUserMedia调用需要在真实环境中实现
                        UnifyPermissionResult.GRANTED
                    } else {
                        UnifyPermissionResult.DENIED
                    }
                }
                "geolocation" -> {
                    // 通过Geolocation API请求位置权限
                    if (window.navigator.asDynamic().geolocation != null) {
                        UnifyPermissionResult.GRANTED
                    } else {
                        UnifyPermissionResult.DENIED
                    }
                }
                else -> UnifyPermissionResult.DENIED
            }
        } catch (e: Exception) {
            UnifyPermissionResult.DENIED
        }
    }
    
    private suspend fun requestNotificationPermission(): UnifyPermissionResult {
        return try {
            val notification = window.asDynamic().Notification
            if (notification != null) {
                // 请求通知权限
                UnifyPermissionResult.GRANTED // 占位符实现
            } else {
                UnifyPermissionResult.DENIED
            }
        } catch (e: Exception) {
            UnifyPermissionResult.DENIED
        }
    }
    
    private suspend fun refreshAllPermissionStatus() {
        // 刷新所有权限状态
    }
}

// Web设备信息实现
class WebDeviceInfo : UnifyDeviceInfo {
    
    override suspend fun getDeviceInfo(): UnifyDeviceDetails {
        val navigator = window.navigator
        return UnifyDeviceDetails(
            deviceId = generateWebDeviceId(),
            deviceName = getBrowserName(),
            manufacturer = "Unknown",
            model = navigator.platform,
            brand = "Web",
            isEmulator = false,
            isRooted = false
        )
    }
    
    override suspend fun getSystemInfo(): UnifySystemInfo {
        val navigator = window.navigator
        return UnifySystemInfo(
            osName = getOperatingSystem(),
            osVersion = navigator.appVersion,
            osApiLevel = 0,
            locale = navigator.language,
            timezone = js("Intl.DateTimeFormat().resolvedOptions().timeZone") as String,
            uptime = 0 // Web无法获取系统运行时间
        )
    }
    
    override suspend fun getHardwareInfo(): UnifyHardwareInfo {
        val navigator = window.navigator.asDynamic()
        return UnifyHardwareInfo(
            cpuArchitecture = navigator.platform ?: "unknown",
            cpuCores = navigator.hardwareConcurrency?.toInt() ?: 1,
            totalMemory = (navigator.deviceMemory?.toLong() ?: 0L) * 1024 * 1024 * 1024, // GB to bytes
            availableMemory = getAvailableMemory(),
            screenWidth = window.screen.width,
            screenHeight = window.screen.height,
            screenDensity = window.devicePixelRatio.toFloat()
        )
    }
    
    override suspend fun getBatteryInfo(): UnifyBatteryInfo {
        return try {
            val navigator = window.navigator.asDynamic()
            if (navigator.getBattery != null) {
                // Battery API实现
                UnifyBatteryInfo(
                    level = 100, // 占位符值
                    isCharging = false,
                    chargingType = "unknown",
                    temperature = 0,
                    voltage = 0,
                    capacity = 0,
                    cycleCount = 0
                )
            } else {
                // 默认电池信息
                UnifyBatteryInfo(100, false, "unknown", 0, 0, 0, 0)
            }
        } catch (e: Exception) {
            UnifyBatteryInfo(100, false, "unknown", 0, 0, 0, 0)
        }
    }
    
    override suspend fun getNetworkInfo(): UnifyNetworkInfo {
        val navigator = window.navigator.asDynamic()
        val connection = navigator.connection
        
        return UnifyNetworkInfo(
            isConnected = navigator.onLine ?: true,
            connectionType = if (connection != null) {
                connection.effectiveType ?: "unknown"
            } else "unknown",
            networkName = "",
            signalStrength = 0,
            ipAddress = "",
            macAddress = "",
            isRoaming = false,
            isMetered = if (connection != null) {
                connection.saveData ?: false
            } else false
        )
    }
    
    override suspend fun getStorageInfo(): UnifyStorageInfo {
        return try {
            val navigator = window.navigator.asDynamic()
            if (navigator.storage?.estimate != null) {
                // Storage API实现
                UnifyStorageInfo(
                    totalSpace = 0, // 占位符值
                    availableSpace = 0,
                    usedSpace = 0,
                    externalStorageAvailable = false,
                    externalTotalSpace = 0,
                    externalAvailableSpace = 0
                )
            } else {
                UnifyStorageInfo(0, 0, 0, false, 0, 0)
            }
        } catch (e: Exception) {
            UnifyStorageInfo(0, 0, 0, false, 0, 0)
        }
    }
    
    override suspend fun getDisplayInfo(): UnifyDisplayInfo {
        val screen = window.screen
        return UnifyDisplayInfo(
            width = screen.width,
            height = screen.height,
            density = window.devicePixelRatio.toFloat(),
            densityDpi = (window.devicePixelRatio * 96).toInt(),
            refreshRate = 60.0f, // 默认刷新率
            orientation = getScreenOrientation(),
            brightness = 1.0f // Web无法获取屏幕亮度
        )
    }
    
    override suspend fun isFeatureSupported(feature: UnifyDeviceFeature): Boolean {
        val navigator = window.navigator.asDynamic()
        return when (feature) {
            UnifyDeviceFeature.CAMERA -> navigator.mediaDevices?.getUserMedia != null
            UnifyDeviceFeature.MICROPHONE -> navigator.mediaDevices?.getUserMedia != null
            UnifyDeviceFeature.GPS -> navigator.geolocation != null
            UnifyDeviceFeature.BLUETOOTH -> navigator.bluetooth != null
            UnifyDeviceFeature.NFC -> false // Web NFC支持有限
            UnifyDeviceFeature.BIOMETRIC -> false // Web不支持生物识别
            UnifyDeviceFeature.ACCELEROMETER -> hasDeviceMotionSupport()
            UnifyDeviceFeature.GYROSCOPE -> hasDeviceMotionSupport()
            UnifyDeviceFeature.MAGNETOMETER -> false // Web不支持磁力计
            UnifyDeviceFeature.VIBRATION -> navigator.vibrate != null
        }
    }
    
    override fun observeDeviceChanges(): Flow<UnifyDeviceChange> = flow {
        // Web设备状态变化监听
    }
    
    private fun generateWebDeviceId(): String {
        // 生成Web设备唯一标识
        return "web_${window.navigator.userAgent.hashCode()}"
    }
    
    private fun getBrowserName(): String {
        val userAgent = window.navigator.userAgent
        return when {
            userAgent.contains("Chrome") -> "Chrome"
            userAgent.contains("Firefox") -> "Firefox"
            userAgent.contains("Safari") -> "Safari"
            userAgent.contains("Edge") -> "Edge"
            else -> "Unknown Browser"
        }
    }
    
    private fun getOperatingSystem(): String {
        val platform = window.navigator.platform
        return when {
            platform.contains("Win") -> "Windows"
            platform.contains("Mac") -> "macOS"
            platform.contains("Linux") -> "Linux"
            platform.contains("Android") -> "Android"
            platform.contains("iPhone") || platform.contains("iPad") -> "iOS"
            else -> "Unknown OS"
        }
    }
    
    private fun getAvailableMemory(): Long {
        val navigator = window.navigator.asDynamic()
        return if (navigator.deviceMemory != null) {
            (navigator.deviceMemory.toLong() * 1024 * 1024 * 1024) / 2 // 估算可用内存
        } else {
            0L
        }
    }
    
    private fun getScreenOrientation(): String {
        val screen = window.screen.asDynamic()
        return if (screen.orientation != null) {
            screen.orientation.type ?: "portrait-primary"
        } else {
            if (window.screen.width > window.screen.height) "landscape-primary" else "portrait-primary"
        }
    }
    
    private fun hasDeviceMotionSupport(): Boolean {
        return window.asDynamic().DeviceMotionEvent != null
    }
}

// Web传感器管理器实现
class WebSensorManager : UnifySensorManager {
    
    private var deviceMotionListener: ((Event) -> Unit)? = null
    private var deviceOrientationListener: ((Event) -> Unit)? = null
    
    override suspend fun initialize() {
        // Web传感器初始化
    }
    
    override suspend fun cleanup() {
        deviceMotionListener?.let { listener ->
            window.removeEventListener("devicemotion", listener)
        }
        deviceOrientationListener?.let { listener ->
            window.removeEventListener("deviceorientation", listener)
        }
    }
    
    override suspend fun getAvailableSensors(): List<UnifySensorInfo> {
        val sensors = mutableListOf<UnifySensorInfo>()
        
        if (window.asDynamic().DeviceMotionEvent != null) {
            sensors.add(UnifySensorInfo(
                type = UnifySensorType.ACCELEROMETER,
                name = "Web Accelerometer",
                vendor = "Browser",
                maxRange = 20.0f,
                resolution = 0.1f,
                power = 0.1f
            ))
            
            sensors.add(UnifySensorInfo(
                type = UnifySensorType.GYROSCOPE,
                name = "Web Gyroscope",
                vendor = "Browser",
                maxRange = 360.0f,
                resolution = 0.1f,
                power = 0.1f
            ))
        }
        
        if (window.asDynamic().DeviceOrientationEvent != null) {
            sensors.add(UnifySensorInfo(
                type = UnifySensorType.ORIENTATION,
                name = "Web Orientation",
                vendor = "Browser",
                maxRange = 360.0f,
                resolution = 0.1f,
                power = 0.1f
            ))
        }
        
        return sensors
    }
    
    override suspend fun isSensorAvailable(sensorType: UnifySensorType): Boolean {
        return when (sensorType) {
            UnifySensorType.ACCELEROMETER, UnifySensorType.GYROSCOPE -> {
                window.asDynamic().DeviceMotionEvent != null
            }
            UnifySensorType.ORIENTATION -> {
                window.asDynamic().DeviceOrientationEvent != null
            }
            else -> false
        }
    }
    
    override suspend fun startSensorListening(
        sensorType: UnifySensorType,
        samplingRate: UnifySensorSamplingRate
    ): Flow<UnifySensorData> = flow {
        when (sensorType) {
            UnifySensorType.ACCELEROMETER, UnifySensorType.GYROSCOPE -> {
                deviceMotionListener = { event ->
                    val motionEvent = event.asDynamic()
                    if (sensorType == UnifySensorType.ACCELEROMETER && motionEvent.accelerationIncludingGravity != null) {
                        val accel = motionEvent.accelerationIncludingGravity
                        // emit加速度计数据
                    } else if (sensorType == UnifySensorType.GYROSCOPE && motionEvent.rotationRate != null) {
                        val rotation = motionEvent.rotationRate
                        // emit陀螺仪数据
                    }
                }
                window.addEventListener("devicemotion", deviceMotionListener!!)
            }
            UnifySensorType.ORIENTATION -> {
                deviceOrientationListener = { event ->
                    val orientationEvent = event.asDynamic()
                    // emit方向传感器数据
                }
                window.addEventListener("deviceorientation", deviceOrientationListener!!)
            }
            else -> {
                // 其他传感器不支持
            }
        }
    }
    
    override suspend fun stopSensorListening(sensorType: UnifySensorType) {
        when (sensorType) {
            UnifySensorType.ACCELEROMETER, UnifySensorType.GYROSCOPE -> {
                deviceMotionListener?.let { listener ->
                    window.removeEventListener("devicemotion", listener)
                    deviceMotionListener = null
                }
            }
            UnifySensorType.ORIENTATION -> {
                deviceOrientationListener?.let { listener ->
                    window.removeEventListener("deviceorientation", listener)
                    deviceOrientationListener = null
                }
            }
            else -> {}
        }
    }
}

// Web系统功能实现
class WebSystemFeatures : UnifySystemFeatures {
    
    override suspend fun vibrate(duration: Long) {
        val navigator = window.navigator.asDynamic()
        if (navigator.vibrate != null) {
            navigator.vibrate(duration.toInt())
        }
    }
    
    override suspend fun vibratePattern(pattern: LongArray, repeat: Int) {
        val navigator = window.navigator.asDynamic()
        if (navigator.vibrate != null) {
            navigator.vibrate(pattern.map { it.toInt() }.toTypedArray())
        }
    }
    
    override suspend fun playSystemSound(sound: UnifySystemSound) {
        // Web系统声音播放实现（有限）
    }
    
    override suspend fun setVolume(streamType: UnifyAudioStream, volume: Float) {
        // Web无法直接控制系统音量
    }
    
    override suspend fun getVolume(streamType: UnifyAudioStream): Float {
        return 1.0f // Web无法获取系统音量
    }
    
    override suspend fun setBrightness(brightness: Float) {
        // Web无法控制屏幕亮度
    }
    
    override suspend fun getBrightness(): Float {
        return 1.0f // Web无法获取屏幕亮度
    }
    
    override suspend fun setScreenOrientation(orientation: UnifyScreenOrientation) {
        val screen = window.screen.asDynamic()
        if (screen.orientation?.lock != null) {
            val orientationString = when (orientation) {
                UnifyScreenOrientation.PORTRAIT -> "portrait-primary"
                UnifyScreenOrientation.LANDSCAPE -> "landscape-primary"
                UnifyScreenOrientation.PORTRAIT_REVERSE -> "portrait-secondary"
                UnifyScreenOrientation.LANDSCAPE_REVERSE -> "landscape-secondary"
                else -> "any"
            }
            // screen.orientation.lock(orientationString)
        }
    }
    
    override suspend fun showNotification(notification: UnifyNotification) {
        try {
            val notificationConstructor = window.asDynamic().Notification
            if (notificationConstructor != null) {
                val options = js("{}")
                options.body = notification.content
                options.icon = notification.iconUrl
                // new Notification(notification.title, options)
            }
        } catch (e: Exception) {
            console.log("Notification not supported: ${e.message}")
        }
    }
    
    override suspend fun copyToClipboard(text: String) {
        try {
            val navigator = window.navigator.asDynamic()
            if (navigator.clipboard?.writeText != null) {
                // navigator.clipboard.writeText(text)
            } else {
                // 降级到传统方法
                val textArea = document.createElement("textarea") as HTMLTextAreaElement
                textArea.value = text
                document.body?.appendChild(textArea)
                textArea.select()
                document.execCommand("copy")
                document.body?.removeChild(textArea)
            }
        } catch (e: Exception) {
            console.log("Clipboard operation failed: ${e.message}")
        }
    }
    
    override suspend fun getClipboardText(): String? {
        return try {
            val navigator = window.navigator.asDynamic()
            if (navigator.clipboard?.readText != null) {
                // navigator.clipboard.readText()
                null // 占位符返回
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun shareText(text: String, title: String?) {
        try {
            val navigator = window.navigator.asDynamic()
            if (navigator.share != null) {
                val shareData = js("{}")
                shareData.title = title ?: ""
                shareData.text = text
                // navigator.share(shareData)
            } else {
                // 降级到复制到剪贴板
                copyToClipboard(text)
            }
        } catch (e: Exception) {
            console.log("Share operation failed: ${e.message}")
        }
    }
    
    override suspend fun shareFile(filePath: String, mimeType: String) {
        // Web文件分享实现（有限）
    }
}

// Web硬件管理器实现
class WebHardwareManager : UnifyHardwareManager {
    
    override suspend fun initialize() {
        // Web硬件管理器初始化
    }
    
    override suspend fun cleanup() {
        // Web硬件资源清理
    }
    
    override suspend fun isCameraAvailable(): Boolean {
        val navigator = window.navigator.asDynamic()
        return navigator.mediaDevices?.getUserMedia != null
    }
    
    override suspend fun takePicture(): UnifyCameraResult {
        return try {
            // Web相机拍照实现
            UnifyCameraResult(
                isSuccess = false,
                filePath = null,
                error = "Camera capture not implemented for web"
            )
        } catch (e: Exception) {
            UnifyCameraResult(
                isSuccess = false,
                filePath = null,
                error = e.message
            )
        }
    }
    
    override suspend fun recordVideo(maxDuration: Long): UnifyCameraResult {
        return UnifyCameraResult(
            isSuccess = false,
            filePath = null,
            error = "Video recording not implemented for web"
        )
    }
    
    override suspend fun isMicrophoneAvailable(): Boolean {
        val navigator = window.navigator.asDynamic()
        return navigator.mediaDevices?.getUserMedia != null
    }
    
    override suspend fun startRecording(config: UnifyAudioConfig): Flow<UnifyAudioData> = flow {
        // Web录音实现
    }
    
    override suspend fun stopRecording() {
        // 停止录音
    }
    
    override suspend fun isLocationAvailable(): Boolean {
        return window.navigator.asDynamic().geolocation != null
    }
    
    override suspend fun getCurrentLocation(accuracy: UnifyLocationAccuracy): UnifyLocationResult {
        return try {
            val geolocation = window.navigator.asDynamic().geolocation
            if (geolocation != null) {
                // Geolocation API实现
                UnifyLocationResult(
                    location = UnifyLocationData(
                        latitude = 0.0,
                        longitude = 0.0,
                        altitude = null,
                        accuracy = 0.0f,
                        timestamp = js("Date.now()") as Long,
                        provider = "Web"
                    ),
                    error = null
                )
            } else {
                UnifyLocationResult(
                    location = null,
                    error = "Geolocation not supported"
                )
            }
        } catch (e: Exception) {
            UnifyLocationResult(
                location = null,
                error = e.message
            )
        }
    }
    
    override suspend fun startLocationUpdates(config: UnifyLocationConfig): Flow<UnifyLocationData> = flow {
        // Web位置更新实现
    }
    
    override suspend fun stopLocationUpdates() {
        // 停止位置更新
    }
    
    override suspend fun isBluetoothAvailable(): Boolean {
        val navigator = window.navigator.asDynamic()
        return navigator.bluetooth != null
    }
    
    override suspend fun scanBluetoothDevices(): Flow<UnifyBluetoothDevice> = flow {
        // Web Bluetooth实现
    }
    
    override suspend fun stopBluetoothScan() {
        // 停止蓝牙扫描
    }
    
    override suspend fun isNFCAvailable(): Boolean {
        return false // Web NFC支持有限
    }
    
    override suspend fun readNFCTag(): Flow<UnifyNFCData> = flow {
        // Web NFC实现（有限）
    }
    
    override suspend fun stopNFCReading() {
        // 停止NFC读取
    }
    
    override suspend fun isBiometricAvailable(): Boolean {
        return false // Web不支持生物识别
    }
    
    override suspend fun authenticateWithBiometric(config: UnifyBiometricConfig): UnifyBiometricResult {
        return UnifyBiometricResult(
            isSuccess = false,
            error = "Biometric authentication not supported on web",
            errorCode = -1
        )
    }
}

// 工厂对象
actual object UnifyDeviceManagerFactory {
    actual fun create(config: UnifyDeviceConfig): UnifyDeviceManager {
        return UnifyDeviceManagerImpl()
    }
}
