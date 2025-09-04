package com.unify.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.*
import platform.CoreLocation.*
import platform.CoreMotion.*
import platform.Foundation.*
import platform.UIKit.*
import platform.UserNotifications.*
import kotlin.coroutines.resume

/**
 * iOS平台UnifyDeviceManager实现
 */
class UnifyDeviceManagerImpl : UnifyDeviceManager {
    private val device = UIDevice.currentDevice
    private val screen = UIScreen.mainScreen
    private val motionManager = CMMotionManager()
    private val locationManager = CLLocationManager()
    
    // 状态流管理
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    private val _batteryStatus = MutableStateFlow(getCurrentBatteryStatus())
    private val _locationUpdates = MutableStateFlow<LocationInfo?>(null)
    
    // 传感器监听器管理
    private val sensorListeners = mutableMapOf<SensorType, SensorListener>()
    
    init {
        // 启用电池监控
        device.batteryMonitoringEnabled = true
    }
    
    override fun getDeviceInfo(): DeviceInfo {
        val bounds = screen.bounds.useContents { this }
        val scale = screen.scale
        
        return DeviceInfo(
            deviceId = device.identifierForVendor?.UUIDString ?: "unknown",
            deviceName = device.name,
            model = device.model,
            manufacturer = "Apple",
            osName = device.systemName,
            osVersion = device.systemVersion,
            screenWidth = (bounds.size.width * scale).toInt(),
            screenHeight = (bounds.size.height * scale).toInt(),
            screenDensity = scale.toFloat(),
            totalMemory = NSProcessInfo.processInfo.physicalMemory.toLong(),
            availableMemory = getAvailableMemory()
        )
    }
    
    override fun getPlatformName(): String = "iOS"
    
    override fun getDeviceModel(): String = device.model
    
    override fun getOSVersion(): String = "${device.systemName} ${device.systemVersion}"
    
    override fun getAppVersion(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
    }
    
    override suspend fun requestPermission(permission: DevicePermission): PermissionStatus {
        return when (permission) {
            DevicePermission.CAMERA -> requestCameraPermission()
            DevicePermission.MICROPHONE -> requestMicrophonePermission()
            DevicePermission.LOCATION -> requestLocationPermission()
            DevicePermission.NOTIFICATIONS -> requestNotificationPermission()
            DevicePermission.CONTACTS -> requestContactsPermission()
            DevicePermission.CALENDAR -> requestCalendarPermission()
            DevicePermission.BIOMETRIC -> requestBiometricPermission()
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
            DevicePermission.CONTACTS -> checkContactsPermission()
            DevicePermission.CALENDAR -> checkCalendarPermission()
            DevicePermission.BIOMETRIC -> checkBiometricPermission()
            else -> PermissionStatus.GRANTED
        }
    }
    
    override fun observePermissionStatus(permission: DevicePermission): Flow<PermissionStatus> {
        return MutableStateFlow(checkPermission(permission)).asStateFlow()
    }
    
    override fun getSupportedSensors(): List<SensorType> {
        val supportedSensors = mutableListOf<SensorType>()
        
        if (motionManager.accelerometerAvailable) {
            supportedSensors.add(SensorType.ACCELEROMETER)
        }
        if (motionManager.gyroAvailable) {
            supportedSensors.add(SensorType.GYROSCOPE)
        }
        if (motionManager.magnetometerAvailable) {
            supportedSensors.add(SensorType.MAGNETOMETER)
        }
        if (motionManager.deviceMotionAvailable) {
            supportedSensors.addAll(listOf(
                SensorType.GRAVITY,
                SensorType.LINEAR_ACCELERATION,
                SensorType.ROTATION_VECTOR
            ))
        }
        
        return supportedSensors
    }
    
    override fun startSensorMonitoring(sensorType: SensorType, listener: SensorListener) {
        sensorListeners[sensorType] = listener
        
        when (sensorType) {
            SensorType.ACCELEROMETER -> {
                if (motionManager.accelerometerAvailable) {
                    motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
                        data?.let {
                            val values = floatArrayOf(
                                it.acceleration.x.toFloat(),
                                it.acceleration.y.toFloat(),
                                it.acceleration.z.toFloat()
                            )
                            listener.onSensorChanged(sensorType, values, (NSDate().timeIntervalSince1970 * 1000).toLong())
                        }
                    }
                }
            }
            SensorType.GYROSCOPE -> {
                if (motionManager.gyroAvailable) {
                    motionManager.startGyroUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
                        data?.let {
                            val values = floatArrayOf(
                                it.rotationRate.x.toFloat(),
                                it.rotationRate.y.toFloat(),
                                it.rotationRate.z.toFloat()
                            )
                            listener.onSensorChanged(sensorType, values, (NSDate().timeIntervalSince1970 * 1000).toLong())
                        }
                    }
                }
            }
            SensorType.MAGNETOMETER -> {
                if (motionManager.magnetometerAvailable) {
                    motionManager.startMagnetometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
                        data?.let {
                            val values = floatArrayOf(
                                it.magneticField.x.toFloat(),
                                it.magneticField.y.toFloat(),
                                it.magneticField.z.toFloat()
                            )
                            listener.onSensorChanged(sensorType, values, (NSDate().timeIntervalSince1970 * 1000).toLong())
                        }
                    }
                }
            }
            else -> {
                // 其他传感器的实现
            }
        }
    }
    
    override fun stopSensorMonitoring(sensorType: SensorType) {
        sensorListeners.remove(sensorType)
        
        when (sensorType) {
            SensorType.ACCELEROMETER -> motionManager.stopAccelerometerUpdates()
            SensorType.GYROSCOPE -> motionManager.stopGyroUpdates()
            SensorType.MAGNETOMETER -> motionManager.stopMagnetometerUpdates()
            else -> {
                // 其他传感器的停止逻辑
            }
        }
    }
    
    override fun isSensorAvailable(sensorType: SensorType): Boolean {
        return when (sensorType) {
            SensorType.ACCELEROMETER -> motionManager.accelerometerAvailable
            SensorType.GYROSCOPE -> motionManager.gyroAvailable
            SensorType.MAGNETOMETER -> motionManager.magnetometerAvailable
            SensorType.GRAVITY, SensorType.LINEAR_ACCELERATION, SensorType.ROTATION_VECTOR -> motionManager.deviceMotionAvailable
            else -> false
        }
    }
    
    override fun vibrate(durationMillis: Long) {
        // iOS使用触觉反馈
        val impactFeedback = UIImpactFeedbackGenerator(UIImpactFeedbackStyleMedium)
        impactFeedback.impactOccurred()
    }
    
    override fun setScreenBrightness(brightness: Float) {
        screen.brightness = brightness.toDouble().coerceIn(0.0, 1.0)
    }
    
    override fun getScreenBrightness(): Float {
        return screen.brightness.toFloat()
    }
    
    override fun setVolume(volume: Float) {
        // iOS不允许直接设置系统音量，只能通过AVAudioSession调整
        // 这里提供基础框架
    }
    
    override fun getVolume(): Float {
        // iOS获取音量需要通过AVAudioSession
        return 0.7f // 简化实现
    }
    
    override fun showNotification(title: String, message: String, id: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound)
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = null
        )
        
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            error?.let {
                println("Notification error: ${it.localizedDescription}")
            }
        }
    }
    
    override suspend fun takePicture(): String? {
        return suspendCancellableCoroutine { continuation ->
            // iOS相机实现需要使用UIImagePickerController或AVFoundation
            // 这里提供基础框架
            continuation.resume(null)
        }
    }
    
    override suspend fun recordAudio(durationMillis: Long): String? {
        return suspendCancellableCoroutine { continuation ->
            // iOS音频录制需要使用AVAudioRecorder
            // 这里提供基础框架
            continuation.resume(null)
        }
    }
    
    override suspend fun getCurrentLocation(): LocationInfo? {
        return suspendCancellableCoroutine { continuation ->
            if (checkLocationPermission() != PermissionStatus.GRANTED) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            locationManager.requestLocation()
            // 实际实现需要设置delegate并处理回调
            continuation.resume(null)
        }
    }
    
    override fun observeLocationUpdates(): Flow<LocationInfo> = _locationUpdates.asStateFlow().filterNotNull()
    
    override fun isNetworkAvailable(): Boolean {
        // iOS网络状态检查需要使用Network framework或Reachability
        return true // 简化实现
    }
    
    override fun getNetworkType(): NetworkType {
        // iOS网络类型检查需要使用Network framework
        return NetworkType.WIFI // 简化实现
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun getBatteryLevel(): Float {
        return device.batteryLevel
    }
    
    override fun isBatteryCharging(): Boolean {
        return device.batteryState == UIDeviceBatteryStateCharging || 
               device.batteryState == UIDeviceBatteryStateFull
    }
    
    override fun observeBatteryStatus(): Flow<BatteryStatus> = _batteryStatus.asStateFlow()
    
    override fun getAvailableStorage(): Long {
        val fileManager = NSFileManager.defaultManager
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: return 0L
        
        return try {
            val attributes = fileManager.attributesOfFileSystemForPath(documentsPath, null)
            (attributes?.get(NSFileSystemFreeSize) as? NSNumber)?.longValue ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    override fun getTotalStorage(): Long {
        val fileManager = NSFileManager.defaultManager
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: return 0L
        
        return try {
            val attributes = fileManager.attributesOfFileSystemForPath(documentsPath, null)
            (attributes?.get(NSFileSystemSize) as? NSNumber)?.longValue ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    override fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }
    
    private fun getAvailableMemory(): Long {
        return NSProcessInfo.processInfo.physicalMemory.toLong() / 2 // 简化实现
    }
    
    private suspend fun requestCameraPermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                continuation.resume(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
            }
        }
    }
    
    private suspend fun requestMicrophonePermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { granted ->
                continuation.resume(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
            }
        }
    }
    
    private suspend fun requestLocationPermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            locationManager.requestWhenInUseAuthorization()
            // 实际实现需要设置delegate并处理回调
            continuation.resume(PermissionStatus.NOT_DETERMINED)
        }
    }
    
    private suspend fun requestNotificationPermission(): PermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(options) { granted, error ->
                continuation.resume(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
            }
        }
    }
    
    private suspend fun requestContactsPermission(): PermissionStatus {
        // 需要导入Contacts framework
        return PermissionStatus.NOT_DETERMINED
    }
    
    private suspend fun requestCalendarPermission(): PermissionStatus {
        // 需要导入EventKit framework
        return PermissionStatus.NOT_DETERMINED
    }
    
    private suspend fun requestBiometricPermission(): PermissionStatus {
        // 需要导入LocalAuthentication framework
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkCameraPermission(): PermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> PermissionStatus.DENIED
            else -> PermissionStatus.NOT_DETERMINED
        }
    }
    
    private fun checkMicrophonePermission(): PermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> PermissionStatus.DENIED
            else -> PermissionStatus.NOT_DETERMINED
        }
    }
    
    private fun checkLocationPermission(): PermissionStatus {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.GRANTED
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> PermissionStatus.DENIED
            else -> PermissionStatus.NOT_DETERMINED
        }
    }
    
    private fun checkNotificationPermission(): PermissionStatus {
        // 需要异步检查，这里简化实现
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkContactsPermission(): PermissionStatus {
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkCalendarPermission(): PermissionStatus {
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun checkBiometricPermission(): PermissionStatus {
        return PermissionStatus.NOT_DETERMINED
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isNetworkAvailable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
    
    private fun getCurrentBatteryStatus(): BatteryStatus {
        return BatteryStatus(
            level = getBatteryLevel(),
            isCharging = isBatteryCharging(),
            chargingType = if (isBatteryCharging()) ChargingType.AC else ChargingType.NONE,
            temperature = 25.0f // iOS不提供电池温度信息
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
