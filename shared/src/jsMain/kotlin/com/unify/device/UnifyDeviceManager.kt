package com.unify.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.browser.window
import kotlinx.browser.document
import org.w3c.dom.Navigator
import org.w3c.dom.Screen
import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.coroutines.resume

/**
 * Web/JS平台UnifyDeviceManager实现
 */
class UnifyDeviceManagerImpl : UnifyDeviceManager {
    private val navigator: Navigator = window.navigator
    private val screen: Screen = window.screen
    
    // 状态流管理
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    private val _batteryStatus = MutableStateFlow(getCurrentBatteryStatus())
    private val _locationUpdates = MutableStateFlow<LocationInfo?>(null)
    
    // 传感器监听器管理（Web环境下支持有限）
    private val sensorListeners = mutableMapOf<SensorType, SensorListener>()
    
    init {
        // 监听网络状态变化
        window.addEventListener("online") { _networkStatus.value = NetworkStatus.CONNECTED }
        window.addEventListener("offline") { _networkStatus.value = NetworkStatus.DISCONNECTED }
        
        // 监听电池状态变化（如果支持）
        setupBatteryMonitoring()
    }
    
    override fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = getDeviceId(),
            deviceName = getDeviceName(),
            model = getDeviceModel(),
            manufacturer = getManufacturer(),
            osName = getOSName(),
            osVersion = getOSVersion(),
            screenWidth = screen.width,
            screenHeight = screen.height,
            screenDensity = window.devicePixelRatio.toFloat(),
            totalMemory = getMemoryInfo().totalJSHeapSize,
            availableMemory = getMemoryInfo().usedJSHeapSize
        )
    }
    
    override fun getPlatformName(): String = "Web"
    
    override fun getDeviceModel(): String {
        val userAgent = navigator.userAgent.lowercase()
        return when {
            userAgent.contains("iphone") -> "iPhone"
            userAgent.contains("ipad") -> "iPad"
            userAgent.contains("android") -> "Android Device"
            userAgent.contains("windows") -> "Windows PC"
            userAgent.contains("macintosh") -> "Mac"
            userAgent.contains("linux") -> "Linux PC"
            else -> "Unknown Device"
        }
    }
    
    override fun getOSVersion(): String = "${getOSName()} ${extractOSVersion()}"
    
    override fun getAppVersion(): String {
        // 从package.json或manifest获取版本信息
        return "1.0.0" // 简化实现
    }
    
    override suspend fun requestPermission(permission: DevicePermission): PermissionStatus {
        return when (permission) {
            DevicePermission.CAMERA -> requestCameraPermission()
            DevicePermission.MICROPHONE -> requestMicrophonePermission()
            DevicePermission.LOCATION -> requestLocationPermission()
            DevicePermission.NOTIFICATIONS -> requestNotificationPermission()
            else -> PermissionStatus.GRANTED
        }
    }
    
    override suspend fun requestPermissions(permissions: List<DevicePermission>): Map<DevicePermission, PermissionStatus> {
        return permissions.associateWith { requestPermission(it) }
    }
    
    override fun checkPermission(permission: DevicePermission): PermissionStatus {
        return when (permission) {
            DevicePermission.CAMERA -> checkCameraPermission()
            DevicePermission.MICROPHONE -> checkMicrophonePermission()
            DevicePermission.LOCATION -> checkLocationPermission()
            DevicePermission.NOTIFICATIONS -> checkNotificationPermission()
            else -> PermissionStatus.GRANTED
        }
    }
    
    override fun observePermissionStatus(permission: DevicePermission): Flow<PermissionStatus> {
        return MutableStateFlow(checkPermission(permission)).asStateFlow()
    }
    
    override fun getSupportedSensors(): List<SensorType> {
        val supportedSensors = mutableListOf<SensorType>()
        
        // 检查设备运动API支持
        if (js("typeof DeviceMotionEvent !== 'undefined'") as Boolean) {
            supportedSensors.addAll(listOf(
                SensorType.ACCELEROMETER,
                SensorType.GYROSCOPE
            ))
        }
        
        // 检查设备方向API支持
        if (js("typeof DeviceOrientationEvent !== 'undefined'") as Boolean) {
            supportedSensors.addAll(listOf(
                SensorType.ORIENTATION,
                SensorType.MAGNETOMETER
            ))
        }
        
        return supportedSensors
    }
    
    override fun startSensorMonitoring(sensorType: SensorType, listener: SensorListener) {
        sensorListeners[sensorType] = listener
        
        when (sensorType) {
            SensorType.ACCELEROMETER, SensorType.GYROSCOPE -> {
                window.addEventListener("devicemotion") { event ->
                    val motionEvent = event.asDynamic()
                    when (sensorType) {
                        SensorType.ACCELEROMETER -> {
                            val acceleration = motionEvent.acceleration
                            if (acceleration != null) {
                                val values = floatArrayOf(
                                    (acceleration.x as? Double)?.toFloat() ?: 0f,
                                    (acceleration.y as? Double)?.toFloat() ?: 0f,
                                    (acceleration.z as? Double)?.toFloat() ?: 0f
                                )
                                listener.onSensorChanged(sensorType, values, js("Date.now()") as Long)
                            }
                        }
                        SensorType.GYROSCOPE -> {
                            val rotationRate = motionEvent.rotationRate
                            if (rotationRate != null) {
                                val values = floatArrayOf(
                                    (rotationRate.alpha as? Double)?.toFloat() ?: 0f,
                                    (rotationRate.beta as? Double)?.toFloat() ?: 0f,
                                    (rotationRate.gamma as? Double)?.toFloat() ?: 0f
                                )
                                listener.onSensorChanged(sensorType, values, js("Date.now()") as Long)
                            }
                        }
                        else -> {}
                    }
                }
            }
            SensorType.ORIENTATION -> {
                window.addEventListener("deviceorientation") { event ->
                    val orientationEvent = event.asDynamic()
                    val values = floatArrayOf(
                        (orientationEvent.alpha as? Double)?.toFloat() ?: 0f,
                        (orientationEvent.beta as? Double)?.toFloat() ?: 0f,
                        (orientationEvent.gamma as? Double)?.toFloat() ?: 0f
                    )
                    listener.onSensorChanged(sensorType, values, js("Date.now()") as Long)
                }
            }
            else -> {
                // 其他传感器暂不支持
            }
        }
    }
    
    override fun stopSensorMonitoring(sensorType: SensorType) {
        sensorListeners.remove(sensorType)
        // 在实际实现中，应该移除对应的事件监听器
    }
    
    override fun isSensorAvailable(sensorType: SensorType): Boolean {
        return when (sensorType) {
            SensorType.ACCELEROMETER, SensorType.GYROSCOPE -> 
                js("typeof DeviceMotionEvent !== 'undefined'") as Boolean
            SensorType.ORIENTATION, SensorType.MAGNETOMETER -> 
                js("typeof DeviceOrientationEvent !== 'undefined'") as Boolean
            else -> false
        }
    }
    
    override fun vibrate(durationMillis: Long) {
        if (js("'vibrate' in navigator") as Boolean) {
            navigator.asDynamic().vibrate(durationMillis.toInt())
        }
    }
    
    override fun setScreenBrightness(brightness: Float) {
        // Web环境下无法直接控制屏幕亮度
        console.warn("Screen brightness control not available in web environment")
    }
    
    override fun getScreenBrightness(): Float {
        // Web环境下无法获取屏幕亮度
        return 0.8f // 返回默认值
    }
    
    override fun setVolume(volume: Float) {
        // Web环境下无法直接控制系统音量
        console.warn("System volume control not available in web environment")
    }
    
    override fun getVolume(): Float {
        // Web环境下无法获取系统音量
        return 0.7f // 返回默认值
    }
    
    override fun showNotification(title: String, message: String, id: String) {
        if (js("'Notification' in window") as Boolean) {
            if (js("Notification.permission === 'granted'") as Boolean) {
                js("new Notification(title, { body: message, tag: id })")
            } else {
                console.warn("Notification permission not granted")
            }
        } else {
            console.warn("Notifications not supported")
        }
    }
    
    override suspend fun takePicture(): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                // 使用getUserMedia API访问摄像头
                val constraints = js("{ video: true }")
                val getUserMedia = navigator.asDynamic().mediaDevices?.getUserMedia
                
                if (getUserMedia != null) {
                    getUserMedia(constraints).then({ stream ->
                        // 创建video元素并捕获图像
                        // 这里需要更复杂的实现来实际捕获图像
                        continuation.resume(null) // 简化实现
                    }).catch({ error ->
                        continuation.resume(null)
                    })
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
    
    override suspend fun recordAudio(durationMillis: Long): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                // 使用MediaRecorder API录制音频
                val constraints = js("{ audio: true }")
                val getUserMedia = navigator.asDynamic().mediaDevices?.getUserMedia
                
                if (getUserMedia != null) {
                    getUserMedia(constraints).then({ stream ->
                        // 创建MediaRecorder并开始录制
                        // 这里需要更复杂的实现来实际录制音频
                        continuation.resume(null) // 简化实现
                    }).catch({ error ->
                        continuation.resume(null)
                    })
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
    
    override suspend fun getCurrentLocation(): LocationInfo? {
        return suspendCancellableCoroutine { continuation ->
            if (js("'geolocation' in navigator") as Boolean) {
                navigator.asDynamic().geolocation.getCurrentPosition(
                    { position ->
                        val coords = position.coords
                        val locationInfo = LocationInfo(
                            latitude = coords.latitude as Double,
                            longitude = coords.longitude as Double,
                            altitude = (coords.altitude as? Double) ?: 0.0,
                            accuracy = coords.accuracy as Float,
                            timestamp = js("Date.now()") as Long
                        )
                        continuation.resume(locationInfo)
                    },
                    { error ->
                        continuation.resume(null)
                    }
                )
            } else {
                continuation.resume(null)
            }
        }
    }
    
    override fun observeLocationUpdates(): Flow<LocationInfo> = _locationUpdates.asStateFlow().filterNotNull()
    
    override fun isNetworkAvailable(): Boolean = navigator.onLine
    
    override fun getNetworkType(): NetworkType {
        val connection = navigator.asDynamic().connection
        return if (connection != null) {
            when (connection.effectiveType as? String) {
                "4g" -> NetworkType.CELLULAR
                "3g" -> NetworkType.CELLULAR
                "2g" -> NetworkType.CELLULAR
                "slow-2g" -> NetworkType.CELLULAR
                else -> NetworkType.WIFI
            }
        } else {
            if (navigator.onLine) NetworkType.WIFI else NetworkType.UNKNOWN
        }
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun getBatteryLevel(): Float {
        val battery = js("navigator.battery") 
        return if (battery != null) {
            (battery.asDynamic().level as? Double)?.toFloat() ?: 1.0f
        } else {
            1.0f // 默认满电
        }
    }
    
    override fun isBatteryCharging(): Boolean {
        val battery = js("navigator.battery")
        return if (battery != null) {
            battery.asDynamic().charging as? Boolean ?: true
        } else {
            true // 默认充电中
        }
    }
    
    override fun observeBatteryStatus(): Flow<BatteryStatus> = _batteryStatus.asStateFlow()
    
    override fun getAvailableStorage(): Long {
        return if (js("'storage' in navigator") as Boolean) {
            // 使用Storage API估算可用存储空间
            val estimate = js("navigator.storage.estimate()")
            // 这是一个Promise，这里简化处理
            1024L * 1024L * 1024L // 1GB 默认值
        } else {
            1024L * 1024L * 1024L // 1GB 默认值
        }
    }
    
    override fun getTotalStorage(): Long {
        return getAvailableStorage() * 2 // 简化实现
    }
    
    override fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }
    
    private fun getDeviceId(): String {
        // Web环境下生成唯一标识符
        val stored = js("localStorage.getItem('unify_device_id')") as? String
        return if (stored != null) {
            stored
        } else {
            val newId = "web_${js("Date.now()")}_${js("Math.random().toString(36).substr(2, 9)")}"
            js("localStorage.setItem('unify_device_id', newId)")
            newId
        }
    }
    
    private fun getDeviceName(): String {
        return "${getOSName()} Browser"
    }
    
    private fun getManufacturer(): String {
        val userAgent = navigator.userAgent.lowercase()
        return when {
            userAgent.contains("chrome") -> "Google"
            userAgent.contains("firefox") -> "Mozilla"
            userAgent.contains("safari") && !userAgent.contains("chrome") -> "Apple"
            userAgent.contains("edge") -> "Microsoft"
            else -> "Unknown"
        }
    }
    
    private fun getOSName(): String {
        val userAgent = navigator.userAgent.lowercase()
        return when {
            userAgent.contains("windows") -> "Windows"
            userAgent.contains("macintosh") || userAgent.contains("mac os") -> "macOS"
            userAgent.contains("linux") -> "Linux"
            userAgent.contains("android") -> "Android"
            userAgent.contains("iphone") || userAgent.contains("ipad") -> "iOS"
            else -> "Unknown OS"
        }
    }
    
    private fun extractOSVersion(): String {
        val userAgent = navigator.userAgent
        // 简化的版本提取逻辑
        return "Unknown Version"
    }
    
    private fun getMemoryInfo(): dynamic {
        return js("performance.memory") ?: js("{ totalJSHeapSize: 0, usedJSHeapSize: 0 }")
    }
    
    private suspend fun requestCameraPermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            if (js("'mediaDevices' in navigator") as Boolean) {
                val getUserMedia = navigator.asDynamic().mediaDevices.getUserMedia
                getUserMedia(js("{ video: true }")).then({
                    continuation.resume(PermissionStatus.GRANTED)
                }).catch({
                    continuation.resume(PermissionStatus.DENIED)
                })
            } else {
                continuation.resume(PermissionStatus.NOT_DETERMINED)
            }
        }
    }
    
    private suspend fun requestMicrophonePermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            if (js("'mediaDevices' in navigator") as Boolean) {
                val getUserMedia = navigator.asDynamic().mediaDevices.getUserMedia
                getUserMedia(js("{ audio: true }")).then({
                    continuation.resume(PermissionStatus.GRANTED)
                }).catch({
                    continuation.resume(PermissionStatus.DENIED)
                })
            } else {
                continuation.resume(PermissionStatus.NOT_DETERMINED)
            }
        }
    }
    
    private suspend fun requestLocationPermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            if (js("'geolocation' in navigator") as Boolean) {
                navigator.asDynamic().geolocation.getCurrentPosition(
                    { continuation.resume(PermissionStatus.GRANTED) },
                    { continuation.resume(PermissionStatus.DENIED) }
                )
            } else {
                continuation.resume(PermissionStatus.NOT_DETERMINED)
            }
        }
    }
    
    private suspend fun requestNotificationPermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            if (js("'Notification' in window") as Boolean) {
                js("Notification.requestPermission()").then({ permission ->
                    val status = when (permission as String) {
                        "granted" -> PermissionStatus.GRANTED
                        "denied" -> PermissionStatus.DENIED
                        else -> PermissionStatus.NOT_DETERMINED
                    }
                    continuation.resume(status)
                })
            } else {
                continuation.resume(PermissionStatus.NOT_DETERMINED)
            }
        }
    }
    
    private fun checkCameraPermission(): PermissionStatus {
        // Web环境下权限检查较复杂，简化实现
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkMicrophonePermission(): PermissionStatus {
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkLocationPermission(): PermissionStatus {
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkNotificationPermission(): PermissionStatus {
        return if (js("'Notification' in window") as Boolean) {
            when (js("Notification.permission") as String) {
                "granted" -> PermissionStatus.GRANTED
                "denied" -> PermissionStatus.DENIED
                else -> PermissionStatus.NOT_DETERMINED
            }
        } else {
            PermissionStatus.NOT_DETERMINED
        }
    }
    
    private fun setupBatteryMonitoring() {
        if (js("'getBattery' in navigator") as Boolean) {
            js("navigator.getBattery()").then({ battery ->
                // 监听电池状态变化
                battery.addEventListener("chargingchange") {
                    _batteryStatus.value = getCurrentBatteryStatus()
                }
                battery.addEventListener("levelchange") {
                    _batteryStatus.value = getCurrentBatteryStatus()
                }
            })
        }
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (navigator.onLine) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
    
    private fun getCurrentBatteryStatus(): BatteryStatus {
        return BatteryStatus(
            level = getBatteryLevel(),
            isCharging = isBatteryCharging(),
            chargingType = if (isBatteryCharging()) ChargingType.AC else ChargingType.NONE,
            temperature = 25.0f // Web环境下无法获取电池温度
        )
    }
}

// 扩展函数用于过滤非空值
private fun <T> Flow<T?>.filterNotNull(): Flow<T> = kotlinx.coroutines.flow.flow {
    collect { value ->
        if (value != null) emit(value)
    }
}

actual object UnifyDeviceManagerFactory {
    actual fun create(): UnifyDeviceManager {
        return UnifyDeviceManagerImpl()
    }
}
