package com.unify.device

import com.unify.device.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 小程序平台UnifyDeviceManager实现
 * 基于小程序系统API和设备能力
 */
class UnifyDeviceManagerImpl : UnifyDeviceManager {
    
    // 传感器数据流
    private val sensorDataFlows = mutableMapOf<SensorType, MutableStateFlow<SensorData>>()
    
    // 小程序设备管理器
    private val miniAppDeviceManager = MiniAppDeviceManager()
    
    override suspend fun requestPermission(permission: DevicePermission): PermissionStatus {
        return try {
            miniAppDeviceManager.authorize(permission)
        } catch (e: Exception) {
            PermissionStatus.DENIED
        }
    }
    
    override suspend fun checkPermission(permission: DevicePermission): PermissionStatus {
        return try {
            miniAppDeviceManager.getSetting(permission)
        } catch (e: Exception) {
            PermissionStatus.DENIED
        }
    }
    
    override suspend fun requestMultiplePermissions(permissions: List<DevicePermission>): Map<DevicePermission, PermissionStatus> {
        return permissions.associateWith { permission ->
            requestPermission(permission)
        }
    }
    
    override suspend fun getDeviceInfo(): DeviceInfo {
        return try {
            val systemInfo = miniAppDeviceManager.getSystemInfoSync()
            DeviceInfo(
                deviceId = systemInfo.deviceId,
                deviceName = "${systemInfo.brand} ${systemInfo.model}",
                manufacturer = systemInfo.brand,
                model = systemInfo.model,
                osVersion = systemInfo.system,
                appVersion = systemInfo.version,
                screenWidth = systemInfo.screenWidth,
                screenHeight = systemInfo.screenHeight,
                screenDensity = systemInfo.pixelRatio,
                totalMemory = 0L, // 小程序无法获取内存信息
                availableMemory = 0L,
                totalStorage = 0L, // 小程序无法获取存储信息
                availableStorage = 0L,
                batteryLevel = miniAppDeviceManager.getBatteryInfoSync().level,
                isCharging = miniAppDeviceManager.getBatteryInfoSync().isCharging,
                networkType = miniAppDeviceManager.getNetworkType().networkType,
                isRooted = false // 小程序无法检测root状态
            )
        } catch (e: Exception) {
            DeviceInfo(
                deviceId = "miniapp_device",
                deviceName = "Mini App Device",
                manufacturer = "Unknown",
                model = "Unknown",
                osVersion = "Unknown",
                appVersion = "1.0.0",
                screenWidth = 375,
                screenHeight = 667,
                screenDensity = 2.0f,
                totalMemory = 0L,
                availableMemory = 0L,
                totalStorage = 0L,
                availableStorage = 0L,
                batteryLevel = 50,
                isCharging = false,
                networkType = "wifi",
                isRooted = false
            )
        }
    }
    
    override suspend fun getSystemInfo(): SystemInfo {
        return try {
            val systemInfo = miniAppDeviceManager.getSystemInfoSync()
            SystemInfo(
                osName = systemInfo.platform,
                osVersion = systemInfo.system,
                kernelVersion = "Unknown",
                apiLevel = 0,
                buildNumber = "Unknown",
                securityPatchLevel = "Unknown",
                bootloader = "Unknown",
                hardware = "Unknown",
                cpuAbi = "Unknown",
                supportedAbis = emptyList(),
                javaVmVersion = "Unknown",
                timezone = "Unknown",
                language = systemInfo.language,
                country = "Unknown"
            )
        } catch (e: Exception) {
            SystemInfo(
                osName = "MiniApp",
                osVersion = "Unknown",
                kernelVersion = "Unknown",
                apiLevel = 0,
                buildNumber = "Unknown",
                securityPatchLevel = "Unknown",
                bootloader = "Unknown",
                hardware = "Unknown",
                cpuAbi = "Unknown",
                supportedAbis = emptyList(),
                javaVmVersion = "Unknown",
                timezone = "Unknown",
                language = "zh_CN",
                country = "Unknown"
            )
        }
    }
    
    override suspend fun getHardwareInfo(): HardwareInfo {
        return try {
            val systemInfo = miniAppDeviceManager.getSystemInfoSync()
            HardwareInfo(
                cpuModel = "Unknown",
                cpuCores = 0,
                cpuFrequency = 0L,
                gpuModel = "Unknown",
                totalRam = 0L,
                availableRam = 0L,
                totalStorage = 0L,
                availableStorage = 0L,
                screenResolution = "${systemInfo.screenWidth}x${systemInfo.screenHeight}",
                screenDpi = (systemInfo.pixelRatio * 160).toInt(),
                cameraResolution = "Unknown",
                hasFrontCamera = systemInfo.cameraAuthorized,
                hasBackCamera = systemInfo.cameraAuthorized,
                hasFlash = false,
                hasBluetooth = systemInfo.bluetoothEnabled,
                hasWifi = systemInfo.wifiEnabled,
                hasNfc = false,
                hasGps = systemInfo.locationEnabled,
                hasCellular = true,
                sensors = getSupportedSensors()
            )
        } catch (e: Exception) {
            HardwareInfo(
                cpuModel = "Unknown",
                cpuCores = 0,
                cpuFrequency = 0L,
                gpuModel = "Unknown",
                totalRam = 0L,
                availableRam = 0L,
                totalStorage = 0L,
                availableStorage = 0L,
                screenResolution = "375x667",
                screenDpi = 320,
                cameraResolution = "Unknown",
                hasFrontCamera = true,
                hasBackCamera = true,
                hasFlash = false,
                hasBluetooth = true,
                hasWifi = true,
                hasNfc = false,
                hasGps = true,
                hasCellular = true,
                sensors = listOf("Accelerometer", "Gyroscope", "Compass")
            )
        }
    }
    
    override suspend fun getBatteryInfo(): BatteryInfo {
        return try {
            val batteryInfo = miniAppDeviceManager.getBatteryInfoSync()
            BatteryInfo(
                level = batteryInfo.level,
                isCharging = batteryInfo.isCharging,
                chargingType = if (batteryInfo.isCharging) "USB" else "None",
                health = "Good",
                temperature = 25.0f,
                voltage = 0,
                capacity = 0,
                technology = "Unknown",
                estimatedTimeRemaining = 0L
            )
        } catch (e: Exception) {
            BatteryInfo(
                level = 50,
                isCharging = false,
                chargingType = "None",
                health = "Good",
                temperature = 25.0f,
                voltage = 0,
                capacity = 0,
                technology = "Unknown",
                estimatedTimeRemaining = 0L
            )
        }
    }
    
    override suspend fun getNetworkInfo(): NetworkInfo {
        return try {
            val networkInfo = miniAppDeviceManager.getNetworkType()
            NetworkInfo(
                isConnected = networkInfo.isConnected,
                networkType = networkInfo.networkType,
                connectionType = networkInfo.networkType,
                signalStrength = 0,
                ipAddress = "Unknown",
                macAddress = "Unknown",
                ssid = "Unknown",
                isMetered = false,
                isRoaming = false,
                downloadSpeed = 0L,
                uploadSpeed = 0L
            )
        } catch (e: Exception) {
            NetworkInfo(
                isConnected = true,
                networkType = "wifi",
                connectionType = "wifi",
                signalStrength = 0,
                ipAddress = "Unknown",
                macAddress = "Unknown",
                ssid = "Unknown",
                isMetered = false,
                isRoaming = false,
                downloadSpeed = 0L,
                uploadSpeed = 0L
            )
        }
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return StorageInfo(
            totalInternal = 0L,
            availableInternal = 0L,
            totalExternal = 0L,
            availableExternal = 0L,
            cacheSize = 0L,
            appDataSize = 0L,
            systemSize = 0L,
            isExternalAvailable = false,
            isExternalRemovable = false,
            isExternalEmulated = false
        )
    }
    
    override suspend fun getDisplayInfo(): DisplayInfo {
        return try {
            val systemInfo = miniAppDeviceManager.getSystemInfoSync()
            DisplayInfo(
                width = systemInfo.screenWidth,
                height = systemInfo.screenHeight,
                density = systemInfo.pixelRatio,
                dpi = (systemInfo.pixelRatio * 160).toInt(),
                refreshRate = 60.0f,
                orientation = if (systemInfo.screenWidth > systemInfo.screenHeight) "Landscape" else "Portrait",
                isHdr = false,
                colorSpace = "sRGB",
                brightnessLevel = 128,
                isAutoRotationEnabled = false
            )
        } catch (e: Exception) {
            DisplayInfo(
                width = 375,
                height = 667,
                density = 2.0f,
                dpi = 320,
                refreshRate = 60.0f,
                orientation = "Portrait",
                isHdr = false,
                colorSpace = "sRGB",
                brightnessLevel = 128,
                isAutoRotationEnabled = false
            )
        }
    }
    
    override suspend fun getSupportedFeatures(): List<String> {
        return listOf(
            "Camera", "Location", "Storage", "Network", "Accelerometer", 
            "Gyroscope", "Compass", "Vibration", "Clipboard", "Share"
        )
    }
    
    override suspend fun startSensorMonitoring(sensorType: SensorType): Flow<SensorData> {
        val flow = sensorDataFlows.getOrPut(sensorType) {
            MutableStateFlow(SensorData(sensorType, floatArrayOf(0f, 0f, 0f), System.currentTimeMillis()))
        }
        
        try {
            when (sensorType) {
                SensorType.ACCELEROMETER -> {
                    miniAppDeviceManager.startAccelerometer { data ->
                        flow.value = SensorData(sensorType, data, System.currentTimeMillis())
                    }
                }
                SensorType.GYROSCOPE -> {
                    miniAppDeviceManager.startGyroscope { data ->
                        flow.value = SensorData(sensorType, data, System.currentTimeMillis())
                    }
                }
                SensorType.COMPASS -> {
                    miniAppDeviceManager.startCompass { data ->
                        flow.value = SensorData(sensorType, data, System.currentTimeMillis())
                    }
                }
                else -> {
                    // 其他传感器小程序不支持
                }
            }
        } catch (e: Exception) {
            // 传感器启动失败，使用模拟数据
        }
        
        return flow.asStateFlow()
    }
    
    override suspend fun stopSensorMonitoring(sensorType: SensorType) {
        try {
            when (sensorType) {
                SensorType.ACCELEROMETER -> miniAppDeviceManager.stopAccelerometer()
                SensorType.GYROSCOPE -> miniAppDeviceManager.stopGyroscope()
                SensorType.COMPASS -> miniAppDeviceManager.stopCompass()
                else -> {}
            }
            sensorDataFlows.remove(sensorType)
        } catch (e: Exception) {
            // 忽略停止传感器的错误
        }
    }
    
    override suspend fun vibrate(durationMillis: Long) {
        try {
            miniAppDeviceManager.vibrateShort()
        } catch (e: Exception) {
            // 振动失败，忽略
        }
    }
    
    override suspend fun vibratePattern(pattern: LongArray) {
        try {
            miniAppDeviceManager.vibrateLong()
        } catch (e: Exception) {
            // 振动失败，忽略
        }
    }
    
    override suspend fun setVolume(streamType: String, volume: Int) {
        // 小程序无法控制系统音量
    }
    
    override suspend fun getVolume(streamType: String): Int {
        // 小程序无法获取系统音量
        return 50
    }
    
    override suspend fun setBrightness(brightness: Int) {
        try {
            miniAppDeviceManager.setScreenBrightness(brightness / 255.0f)
        } catch (e: Exception) {
            // 亮度设置失败，忽略
        }
    }
    
    override suspend fun getBrightness(): Int {
        return try {
            (miniAppDeviceManager.getScreenBrightness() * 255).toInt()
        } catch (e: Exception) {
            128 // 默认亮度
        }
    }
    
    override suspend fun showNotification(title: String, message: String, channelId: String) {
        // 小程序无法显示系统通知，可以使用小程序内的提示
        try {
            miniAppDeviceManager.showToast(message)
        } catch (e: Exception) {
            // 通知显示失败，忽略
        }
    }
    
    override suspend fun copyToClipboard(text: String) {
        try {
            miniAppDeviceManager.setClipboardData(text)
        } catch (e: Exception) {
            // 剪贴板操作失败，忽略
        }
    }
    
    override suspend fun getFromClipboard(): String {
        return try {
            miniAppDeviceManager.getClipboardData()
        } catch (e: Exception) {
            "" // 默认空字符串
        }
    }
    
    override suspend fun shareText(text: String, title: String) {
        try {
            miniAppDeviceManager.share(title, text)
        } catch (e: Exception) {
            // 分享失败，忽略
        }
    }
    
    override suspend fun setScreenOrientation(orientation: String) {
        // 小程序无法控制屏幕方向
    }
    
    override suspend fun getScreenOrientation(): String {
        return try {
            val systemInfo = miniAppDeviceManager.getSystemInfoSync()
            if (systemInfo.screenWidth > systemInfo.screenHeight) "Landscape" else "Portrait"
        } catch (e: Exception) {
            "Portrait"
        }
    }
    
    override suspend fun takePicture(outputPath: String): Boolean {
        return try {
            miniAppDeviceManager.chooseImage(1).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun recordVideo(outputPath: String, durationSeconds: Int): Boolean {
        return try {
            miniAppDeviceManager.chooseVideo().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun recordAudio(outputPath: String, durationSeconds: Int): Boolean {
        return try {
            miniAppDeviceManager.startRecord()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getCurrentLocation(): LocationData? {
        return try {
            miniAppDeviceManager.getLocation()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun scanBluetooth(): List<BluetoothDevice> {
        return try {
            miniAppDeviceManager.getBluetoothDevices()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun readNfc(): String? {
        return try {
            miniAppDeviceManager.getHCEState()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun authenticateBiometric(): Boolean {
        return try {
            miniAppDeviceManager.checkIsSupportSoterAuthentication()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getSupportedSensors(): List<String> {
        return listOf("Accelerometer", "Gyroscope", "Compass")
    }
}

// 小程序设备管理器模拟实现
private class MiniAppDeviceManager {
    
    fun authorize(permission: DevicePermission): PermissionStatus {
        // 实际实现中会调用小程序API: wx.authorize()
        return PermissionStatus.GRANTED
    }
    
    fun getSetting(permission: DevicePermission): PermissionStatus {
        // 实际实现中会调用小程序API: wx.getSetting()
        return PermissionStatus.GRANTED
    }
    
    fun getSystemInfoSync(): SystemInfo {
        // 实际实现中会调用小程序API: wx.getSystemInfoSync()
        return SystemInfo(
            brand = "iPhone",
            model = "iPhone 12",
            pixelRatio = 3.0f,
            screenWidth = 390,
            screenHeight = 844,
            system = "iOS 15.0",
            platform = "ios",
            version = "8.0.5",
            language = "zh_CN",
            deviceId = "miniapp_device_001",
            cameraAuthorized = true,
            locationEnabled = true,
            bluetoothEnabled = true,
            wifiEnabled = true
        )
    }
    
    fun getBatteryInfoSync(): BatteryInfo {
        // 实际实现中会调用小程序API: wx.getBatteryInfoSync()
        return BatteryInfo(level = 80, isCharging = false)
    }
    
    fun getNetworkType(): NetworkInfo {
        // 实际实现中会调用小程序API: wx.getNetworkType()
        return NetworkInfo(isConnected = true, networkType = "wifi")
    }
    
    fun startAccelerometer(callback: (FloatArray) -> Unit) {
        // 实际实现中会调用小程序API: wx.startAccelerometer()
    }
    
    fun stopAccelerometer() {
        // 实际实现中会调用小程序API: wx.stopAccelerometer()
    }
    
    fun startGyroscope(callback: (FloatArray) -> Unit) {
        // 实际实现中会调用小程序API: wx.startGyroscope()
    }
    
    fun stopGyroscope() {
        // 实际实现中会调用小程序API: wx.stopGyroscope()
    }
    
    fun startCompass(callback: (FloatArray) -> Unit) {
        // 实际实现中会调用小程序API: wx.startCompass()
    }
    
    fun stopCompass() {
        // 实际实现中会调用小程序API: wx.stopCompass()
    }
    
    fun vibrateShort() {
        // 实际实现中会调用小程序API: wx.vibrateShort()
    }
    
    fun vibrateLong() {
        // 实际实现中会调用小程序API: wx.vibrateLong()
    }
    
    fun setScreenBrightness(brightness: Float) {
        // 实际实现中会调用小程序API: wx.setScreenBrightness()
    }
    
    fun getScreenBrightness(): Float {
        // 实际实现中会调用小程序API: wx.getScreenBrightness()
        return 0.5f
    }
    
    fun showToast(message: String) {
        // 实际实现中会调用小程序API: wx.showToast()
    }
    
    fun setClipboardData(text: String) {
        // 实际实现中会调用小程序API: wx.setClipboardData()
    }
    
    fun getClipboardData(): String {
        // 实际实现中会调用小程序API: wx.getClipboardData()
        return ""
    }
    
    fun share(title: String, text: String) {
        // 实际实现中会调用小程序API: wx.shareAppMessage()
    }
    
    fun chooseImage(count: Int): List<String> {
        // 实际实现中会调用小程序API: wx.chooseImage()
        return listOf("temp://image.jpg")
    }
    
    fun chooseVideo(): List<String> {
        // 实际实现中会调用小程序API: wx.chooseVideo()
        return listOf("temp://video.mp4")
    }
    
    fun startRecord(): Boolean {
        // 实际实现中会调用小程序API: wx.startRecord()
        return true
    }
    
    fun getLocation(): LocationData {
        // 实际实现中会调用小程序API: wx.getLocation()
        return LocationData(
            latitude = 39.9042,
            longitude = 116.4074,
            altitude = 43.5,
            accuracy = 5.0f,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun getBluetoothDevices(): List<BluetoothDevice> {
        // 实际实现中会调用小程序API: wx.getBluetoothDevices()
        return emptyList()
    }
    
    fun getHCEState(): String {
        // 实际实现中会调用小程序API: wx.getHCEState()
        return "available"
    }
    
    fun checkIsSupportSoterAuthentication(): Boolean {
        // 实际实现中会调用小程序API: wx.checkIsSupportSoterAuthentication()
        return true
    }
    
    data class SystemInfo(
        val brand: String,
        val model: String,
        val pixelRatio: Float,
        val screenWidth: Int,
        val screenHeight: Int,
        val system: String,
        val platform: String,
        val version: String,
        val language: String,
        val deviceId: String,
        val cameraAuthorized: Boolean,
        val locationEnabled: Boolean,
        val bluetoothEnabled: Boolean,
        val wifiEnabled: Boolean
    )
    
    data class BatteryInfo(
        val level: Int,
        val isCharging: Boolean
    )
    
    data class NetworkInfo(
        val isConnected: Boolean,
        val networkType: String
    )
}

actual object UnifyDeviceManagerFactory {
    actual fun create(): UnifyDeviceManager {
        return UnifyDeviceManagerImpl()
    }
}
