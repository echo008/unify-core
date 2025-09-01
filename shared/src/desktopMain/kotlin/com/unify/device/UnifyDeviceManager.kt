package com.unify.device

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.lang.management.ManagementFactory
import java.net.NetworkInterface
import java.net.InetAddress
import javax.sound.sampled.AudioSystem
import javax.swing.JOptionPane

// Desktop平台的统一设备管理器实现
actual class UnifyDeviceManagerImpl : UnifyDeviceManager {
    
    actual override val permissions: UnifyPermissionManager = DesktopPermissionManager()
    actual override val deviceInfo: UnifyDeviceInfo = DesktopDeviceInfo()
    actual override val sensors: UnifySensorManager = DesktopSensorManager()
    actual override val systemFeatures: UnifySystemFeatures = DesktopSystemFeatures()
    actual override val hardware: UnifyHardwareManager = DesktopHardwareManager()
    
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

// Desktop权限管理器实现
class DesktopPermissionManager : UnifyPermissionManager {
    
    private val permissionStatusFlow = MutableStateFlow<Map<UnifyPermission, UnifyPermissionStatus>>(emptyMap())
    
    override suspend fun initialize() {
        refreshAllPermissionStatus()
    }
    
    override suspend fun checkPermission(permission: UnifyPermission): UnifyPermissionStatus {
        return when (permission) {
            UnifyPermission.CAMERA -> checkCameraPermission()
            UnifyPermission.MICROPHONE -> checkMicrophonePermission()
            UnifyPermission.STORAGE_READ, UnifyPermission.STORAGE_WRITE -> checkStoragePermission()
            UnifyPermission.INTERNET -> UnifyPermissionStatus.GRANTED
            UnifyPermission.NETWORK_STATE -> UnifyPermissionStatus.GRANTED
            else -> UnifyPermissionStatus.NOT_DETERMINED
        }
    }
    
    override suspend fun requestPermission(permission: UnifyPermission): UnifyPermissionResult {
        return when (permission) {
            UnifyPermission.CAMERA -> requestCameraPermission()
            UnifyPermission.MICROPHONE -> requestMicrophonePermission()
            UnifyPermission.STORAGE_READ, UnifyPermission.STORAGE_WRITE -> requestStoragePermission()
            UnifyPermission.INTERNET, UnifyPermission.NETWORK_STATE -> UnifyPermissionResult.GRANTED
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
        // Desktop权限变化监听实现
    }
    
    override suspend fun shouldShowPermissionRationale(permission: UnifyPermission): Boolean {
        return false // Desktop通常不需要显示权限说明
    }
    
    private fun checkCameraPermission(): UnifyPermissionStatus {
        return try {
            val devices = javax.media.CaptureDeviceManager.getDeviceList(javax.media.format.VideoFormat::class.java)
            if (devices.size > 0) UnifyPermissionStatus.GRANTED else UnifyPermissionStatus.DENIED
        } catch (e: Exception) {
            // JMF不可用，检查其他相机API
            UnifyPermissionStatus.NOT_DETERMINED
        }
    }
    
    private suspend fun requestCameraPermission(): UnifyPermissionResult {
        return try {
            // Desktop相机权限通常由操作系统管理
            val hasCamera = checkCameraPermission() == UnifyPermissionStatus.GRANTED
            if (hasCamera) UnifyPermissionResult.GRANTED else UnifyPermissionResult.DENIED
        } catch (e: Exception) {
            UnifyPermissionResult.DENIED
        }
    }
    
    private fun checkMicrophonePermission(): UnifyPermissionStatus {
        return try {
            val mixers = AudioSystem.getMixerInfo()
            val hasMicrophone = mixers.any { mixer ->
                val mixerInstance = AudioSystem.getMixer(mixer)
                mixerInstance.targetLineInfo.isNotEmpty()
            }
            if (hasMicrophone) UnifyPermissionStatus.GRANTED else UnifyPermissionStatus.DENIED
        } catch (e: Exception) {
            UnifyPermissionStatus.NOT_DETERMINED
        }
    }
    
    private suspend fun requestMicrophonePermission(): UnifyPermissionResult {
        return try {
            val hasMicrophone = checkMicrophonePermission() == UnifyPermissionStatus.GRANTED
            if (hasMicrophone) UnifyPermissionResult.GRANTED else UnifyPermissionResult.DENIED
        } catch (e: Exception) {
            UnifyPermissionResult.DENIED
        }
    }
    
    private fun checkStoragePermission(): UnifyPermissionStatus {
        return try {
            val userHome = System.getProperty("user.home")
            val testFile = File(userHome, ".unify_permission_test")
            val canWrite = testFile.parentFile.canWrite()
            val canRead = testFile.parentFile.canRead()
            
            if (canRead && canWrite) {
                UnifyPermissionStatus.GRANTED
            } else {
                UnifyPermissionStatus.DENIED
            }
        } catch (e: Exception) {
            UnifyPermissionStatus.DENIED
        }
    }
    
    private suspend fun requestStoragePermission(): UnifyPermissionResult {
        return if (checkStoragePermission() == UnifyPermissionStatus.GRANTED) {
            UnifyPermissionResult.GRANTED
        } else {
            UnifyPermissionResult.DENIED
        }
    }
    
    private suspend fun refreshAllPermissionStatus() {
        // 刷新所有权限状态
    }
}

// Desktop设备信息实现
class DesktopDeviceInfo : UnifyDeviceInfo {
    
    override suspend fun getDeviceInfo(): UnifyDeviceDetails {
        return UnifyDeviceDetails(
            deviceId = generateDesktopDeviceId(),
            deviceName = getComputerName(),
            manufacturer = getManufacturer(),
            model = getComputerModel(),
            brand = "Desktop",
            isEmulator = false,
            isRooted = hasAdminRights()
        )
    }
    
    override suspend fun getSystemInfo(): UnifySystemInfo {
        val runtime = Runtime.getRuntime()
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        
        return UnifySystemInfo(
            osName = System.getProperty("os.name"),
            osVersion = System.getProperty("os.version"),
            osApiLevel = 0,
            locale = System.getProperty("user.language") + "_" + System.getProperty("user.country"),
            timezone = System.getProperty("user.timezone"),
            uptime = ManagementFactory.getRuntimeMXBean().uptime
        )
    }
    
    override suspend fun getHardwareInfo(): UnifyHardwareInfo {
        val runtime = Runtime.getRuntime()
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        val toolkit = Toolkit.getDefaultToolkit()
        
        return UnifyHardwareInfo(
            cpuArchitecture = System.getProperty("os.arch"),
            cpuCores = runtime.availableProcessors(),
            totalMemory = runtime.totalMemory(),
            availableMemory = runtime.freeMemory(),
            screenWidth = toolkit.screenSize.width,
            screenHeight = toolkit.screenSize.height,
            screenDensity = toolkit.screenResolution.toFloat() / 96.0f // DPI to scale factor
        )
    }
    
    override suspend fun getBatteryInfo(): UnifyBatteryInfo {
        return try {
            // Desktop电池信息获取（笔记本电脑）
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> getWindowsBatteryInfo()
                osName.contains("mac") -> getMacBatteryInfo()
                osName.contains("linux") -> getLinuxBatteryInfo()
                else -> getDefaultBatteryInfo()
            }
        } catch (e: Exception) {
            getDefaultBatteryInfo()
        }
    }
    
    override suspend fun getNetworkInfo(): UnifyNetworkInfo {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            var isConnected = false
            var connectionType = "none"
            var networkName = ""
            var ipAddress = ""
            var macAddress = ""
            
            for (networkInterface in networkInterfaces) {
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    isConnected = true
                    
                    // 获取网络类型
                    connectionType = when {
                        networkInterface.name.contains("eth", ignoreCase = true) -> "ethernet"
                        networkInterface.name.contains("wlan", ignoreCase = true) || 
                        networkInterface.name.contains("wifi", ignoreCase = true) -> "wifi"
                        else -> "unknown"
                    }
                    
                    // 获取IP地址
                    val addresses = networkInterface.inetAddresses
                    for (address in addresses) {
                        if (!address.isLoopbackAddress && address is InetAddress) {
                            ipAddress = address.hostAddress
                            break
                        }
                    }
                    
                    // 获取MAC地址
                    networkInterface.hardwareAddress?.let { mac ->
                        macAddress = mac.joinToString(":") { "%02x".format(it) }
                    }
                    
                    networkName = networkInterface.displayName
                    break
                }
            }
            
            UnifyNetworkInfo(
                isConnected = isConnected,
                connectionType = connectionType,
                networkName = networkName,
                signalStrength = 0, // Desktop无法获取信号强度
                ipAddress = ipAddress,
                macAddress = macAddress,
                isRoaming = false,
                isMetered = false
            )
        } catch (e: Exception) {
            UnifyNetworkInfo(false, "none", "", 0, "", "", false, false)
        }
    }
    
    override suspend fun getStorageInfo(): UnifyStorageInfo {
        return try {
            val roots = File.listRoots()
            var totalSpace = 0L
            var freeSpace = 0L
            var usedSpace = 0L
            
            for (root in roots) {
                totalSpace += root.totalSpace
                freeSpace += root.freeSpace
            }
            usedSpace = totalSpace - freeSpace
            
            UnifyStorageInfo(
                totalSpace = totalSpace,
                availableSpace = freeSpace,
                usedSpace = usedSpace,
                externalStorageAvailable = roots.size > 1,
                externalTotalSpace = if (roots.size > 1) roots[1].totalSpace else 0,
                externalAvailableSpace = if (roots.size > 1) roots[1].freeSpace else 0
            )
        } catch (e: Exception) {
            UnifyStorageInfo(0, 0, 0, false, 0, 0)
        }
    }
    
    override suspend fun getDisplayInfo(): UnifyDisplayInfo {
        val toolkit = Toolkit.getDefaultToolkit()
        val screenSize = toolkit.screenSize
        val screenResolution = toolkit.screenResolution
        
        return UnifyDisplayInfo(
            width = screenSize.width,
            height = screenSize.height,
            density = screenResolution.toFloat() / 96.0f,
            densityDpi = screenResolution,
            refreshRate = 60.0f, // 默认刷新率
            orientation = if (screenSize.width > screenSize.height) "landscape" else "portrait",
            brightness = 1.0f // Desktop无法获取屏幕亮度
        )
    }
    
    override suspend fun isFeatureSupported(feature: UnifyDeviceFeature): Boolean {
        return when (feature) {
            UnifyDeviceFeature.CAMERA -> hasCameraSupport()
            UnifyDeviceFeature.MICROPHONE -> hasMicrophoneSupport()
            UnifyDeviceFeature.GPS -> false // Desktop通常没有GPS
            UnifyDeviceFeature.BLUETOOTH -> hasBluetoothSupport()
            UnifyDeviceFeature.NFC -> false // Desktop通常没有NFC
            UnifyDeviceFeature.BIOMETRIC -> false // Desktop生物识别支持有限
            UnifyDeviceFeature.ACCELEROMETER -> false // Desktop没有加速度计
            UnifyDeviceFeature.GYROSCOPE -> false // Desktop没有陀螺仪
            UnifyDeviceFeature.MAGNETOMETER -> false // Desktop没有磁力计
            UnifyDeviceFeature.VIBRATION -> false // Desktop没有振动功能
        }
    }
    
    override fun observeDeviceChanges(): Flow<UnifyDeviceChange> = flow {
        // Desktop设备状态变化监听
    }
    
    private fun generateDesktopDeviceId(): String {
        return try {
            val computerName = getComputerName()
            val osName = System.getProperty("os.name")
            val userName = System.getProperty("user.name")
            "desktop_${(computerName + osName + userName).hashCode()}"
        } catch (e: Exception) {
            "desktop_unknown"
        }
    }
    
    private fun getComputerName(): String {
        return try {
            System.getenv("COMPUTERNAME") ?: System.getenv("HOSTNAME") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getManufacturer(): String {
        return try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> "Microsoft"
                osName.contains("mac") -> "Apple"
                osName.contains("linux") -> "Linux"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getComputerModel(): String {
        return try {
            val osName = System.getProperty("os.name")
            val osVersion = System.getProperty("os.version")
            "$osName $osVersion"
        } catch (e: Exception) {
            "Unknown Model"
        }
    }
    
    private fun hasAdminRights(): Boolean {
        return try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Windows管理员权限检查
                    val userName = System.getProperty("user.name")
                    userName.equals("Administrator", ignoreCase = true)
                }
                osName.contains("mac") || osName.contains("linux") -> {
                    // Unix系统root权限检查
                    val userName = System.getProperty("user.name")
                    userName == "root"
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getWindowsBatteryInfo(): UnifyBatteryInfo {
        // Windows电池信息实现（需要WMI或PowerShell）
        return getDefaultBatteryInfo()
    }
    
    private fun getMacBatteryInfo(): UnifyBatteryInfo {
        // macOS电池信息实现（需要系统命令）
        return getDefaultBatteryInfo()
    }
    
    private fun getLinuxBatteryInfo(): UnifyBatteryInfo {
        // Linux电池信息实现（/sys/class/power_supply/）
        return try {
            val batteryPath = File("/sys/class/power_supply/BAT0")
            if (batteryPath.exists()) {
                val capacity = File(batteryPath, "capacity").readText().trim().toIntOrNull() ?: 100
                val status = File(batteryPath, "status").readText().trim()
                val isCharging = status.equals("Charging", ignoreCase = true)
                
                UnifyBatteryInfo(
                    level = capacity,
                    isCharging = isCharging,
                    chargingType = if (isCharging) "ac" else "not_charging",
                    temperature = 0,
                    voltage = 0,
                    capacity = 0,
                    cycleCount = 0
                )
            } else {
                getDefaultBatteryInfo()
            }
        } catch (e: Exception) {
            getDefaultBatteryInfo()
        }
    }
    
    private fun getDefaultBatteryInfo(): UnifyBatteryInfo {
        return UnifyBatteryInfo(
            level = 100, // 假设台式机总是满电
            isCharging = false,
            chargingType = "ac",
            temperature = 0,
            voltage = 0,
            capacity = 0,
            cycleCount = 0
        )
    }
    
    private fun hasCameraSupport(): Boolean {
        return try {
            // 检查是否有可用的摄像头设备
            val devices = javax.media.CaptureDeviceManager.getDeviceList(javax.media.format.VideoFormat::class.java)
            devices.size > 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasMicrophoneSupport(): Boolean {
        return try {
            val mixers = AudioSystem.getMixerInfo()
            mixers.any { mixer ->
                val mixerInstance = AudioSystem.getMixer(mixer)
                mixerInstance.targetLineInfo.isNotEmpty()
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasBluetoothSupport(): Boolean {
        return try {
            // 检查蓝牙支持（需要BlueCove或类似库）
            false // 占位符实现
        } catch (e: Exception) {
            false
        }
    }
}

// Desktop传感器管理器实现
class DesktopSensorManager : UnifySensorManager {
    
    override suspend fun initialize() {
        // Desktop传感器初始化（通常没有传感器）
    }
    
    override suspend fun cleanup() {
        // Desktop传感器清理
    }
    
    override suspend fun getAvailableSensors(): List<UnifySensorInfo> {
        // Desktop通常没有传感器
        return emptyList()
    }
    
    override suspend fun isSensorAvailable(sensorType: UnifySensorType): Boolean {
        // Desktop通常不支持移动设备传感器
        return false
    }
    
    override suspend fun startSensorListening(
        sensorType: UnifySensorType,
        samplingRate: UnifySensorSamplingRate
    ): Flow<UnifySensorData> = flow {
        // Desktop传感器监听（空实现）
    }
    
    override suspend fun stopSensorListening(sensorType: UnifySensorType) {
        // Desktop传感器停止监听
    }
}

// Desktop系统功能实现
class DesktopSystemFeatures : UnifySystemFeatures {
    
    override suspend fun vibrate(duration: Long) {
        // Desktop没有振动功能，可以用系统声音代替
        Toolkit.getDefaultToolkit().beep()
    }
    
    override suspend fun vibratePattern(pattern: LongArray, repeat: Int) {
        // Desktop振动模式（用系统声音代替）
        repeat(pattern.size.coerceAtMost(3)) {
            Toolkit.getDefaultToolkit().beep()
            Thread.sleep(200)
        }
    }
    
    override suspend fun playSystemSound(sound: UnifySystemSound) {
        when (sound) {
            UnifySystemSound.NOTIFICATION -> Toolkit.getDefaultToolkit().beep()
            UnifySystemSound.ALERT -> Toolkit.getDefaultToolkit().beep()
            UnifySystemSound.ERROR -> Toolkit.getDefaultToolkit().beep()
            else -> Toolkit.getDefaultToolkit().beep()
        }
    }
    
    override suspend fun setVolume(streamType: UnifyAudioStream, volume: Float) {
        // Desktop音量控制实现（需要系统API）
    }
    
    override suspend fun getVolume(streamType: UnifyAudioStream): Float {
        return 1.0f // Desktop音量获取实现
    }
    
    override suspend fun setBrightness(brightness: Float) {
        // Desktop屏幕亮度控制（需要系统API）
    }
    
    override suspend fun getBrightness(): Float {
        return 1.0f // Desktop亮度获取实现
    }
    
    override suspend fun setScreenOrientation(orientation: UnifyScreenOrientation) {
        // Desktop屏幕方向设置（通常不支持）
    }
    
    override suspend fun showNotification(notification: UnifyNotification) {
        try {
            // 使用系统托盘通知或对话框
            if (java.awt.SystemTray.isSupported()) {
                // 系统托盘通知实现
                JOptionPane.showMessageDialog(
                    null,
                    notification.content,
                    notification.title,
                    JOptionPane.INFORMATION_MESSAGE
                )
            } else {
                // 降级到对话框
                JOptionPane.showMessageDialog(
                    null,
                    notification.content,
                    notification.title,
                    JOptionPane.INFORMATION_MESSAGE
                )
            }
        } catch (e: Exception) {
            // 通知发送失败，静默处理
        }
    }
    
    override suspend fun copyToClipboard(text: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(text)
            clipboard.setContents(selection, null)
        } catch (e: Exception) {
            // 剪贴板操作失败，静默处理
        }
    }
    
    override suspend fun getClipboardText(): String? {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val data = clipboard.getContents(null)
            if (data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                data.getTransferData(DataFlavor.stringFlavor) as String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun shareText(text: String, title: String?) {
        // Desktop分享功能（复制到剪贴板）
        copyToClipboard(text)
        showNotification(UnifyNotification(
            id = "share_notification",
            title = title ?: "分享",
            content = "文本已复制到剪贴板",
            priority = 1
        ))
    }
    
    override suspend fun shareFile(filePath: String, mimeType: String) {
        try {
            // Desktop文件分享（打开文件管理器）
            val file = File(filePath)
            if (file.exists()) {
                val desktop = java.awt.Desktop.getDesktop()
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(file.parentFile)
                }
            }
        } catch (e: Exception) {
            // 文件分享失败，静默处理
        }
    }
}

// Desktop硬件管理器实现
class DesktopHardwareManager : UnifyHardwareManager {
    
    override suspend fun initialize() {
        // Desktop硬件管理器初始化
    }
    
    override suspend fun cleanup() {
        // Desktop硬件资源清理
    }
    
    override suspend fun isCameraAvailable(): Boolean {
        return try {
            val devices = javax.media.CaptureDeviceManager.getDeviceList(javax.media.format.VideoFormat::class.java)
            devices.size > 0
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun takePicture(): UnifyCameraResult {
        return UnifyCameraResult(
            isSuccess = false,
            filePath = null,
            error = "Camera capture not implemented for desktop"
        )
    }
    
    override suspend fun recordVideo(maxDuration: Long): UnifyCameraResult {
        return UnifyCameraResult(
            isSuccess = false,
            filePath = null,
            error = "Video recording not implemented for desktop"
        )
    }
    
    override suspend fun isMicrophoneAvailable(): Boolean {
        return try {
            val mixers = AudioSystem.getMixerInfo()
            mixers.any { mixer ->
                val mixerInstance = AudioSystem.getMixer(mixer)
                mixerInstance.targetLineInfo.isNotEmpty()
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun startRecording(config: UnifyAudioConfig): Flow<UnifyAudioData> = flow {
        // Desktop录音实现
    }
    
    override suspend fun stopRecording() {
        // 停止录音
    }
    
    override suspend fun isLocationAvailable(): Boolean {
        return false // Desktop通常没有GPS
    }
    
    override suspend fun getCurrentLocation(accuracy: UnifyLocationAccuracy): UnifyLocationResult {
        return UnifyLocationResult(
            location = null,
            error = "Location services not available on desktop"
        )
    }
    
    override suspend fun startLocationUpdates(config: UnifyLocationConfig): Flow<UnifyLocationData> = flow {
        // Desktop位置更新（空实现）
    }
    
    override suspend fun stopLocationUpdates() {
        // 停止位置更新
    }
    
    override suspend fun isBluetoothAvailable(): Boolean {
        return false // Desktop蓝牙支持需要额外库
    }
    
    override suspend fun scanBluetoothDevices(): Flow<UnifyBluetoothDevice> = flow {
        // Desktop蓝牙扫描（空实现）
    }
    
    override suspend fun stopBluetoothScan() {
        // 停止蓝牙扫描
    }
    
    override suspend fun isNFCAvailable(): Boolean {
        return false // Desktop通常没有NFC
    }
    
    override suspend fun readNFCTag(): Flow<UnifyNFCData> = flow {
        // Desktop NFC读取（空实现）
    }
    
    override suspend fun stopNFCReading() {
        // 停止NFC读取
    }
    
    override suspend fun isBiometricAvailable(): Boolean {
        return false // Desktop生物识别支持有限
    }
    
    override suspend fun authenticateWithBiometric(config: UnifyBiometricConfig): UnifyBiometricResult {
        return UnifyBiometricResult(
            isSuccess = false,
            error = "Biometric authentication not supported on desktop",
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
