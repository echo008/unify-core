package com.unify.core.platform

import com.unify.core.types.DeviceInfo
import com.unify.core.types.PlatformType
import platform.AVFoundation.AVCaptureDevice
import platform.CoreLocation.CLLocationManager
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

/**
 * iOS平台管理器实现
 */
class IOSPlatformManager : BasePlatformManager() {
    private val device = UIDevice.currentDevice
    private val processInfo = NSProcessInfo.processInfo
    private val bundle = NSBundle.mainBundle

    override fun getPlatformType(): PlatformType = PlatformType.IOS

    override fun getPlatformName(): String = "iOS"

    override fun getPlatformVersion(): String = device.systemVersion

    override suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = "Apple",
            model = device.model,
            systemName = device.systemName,
            systemVersion = device.systemVersion,
            deviceId = device.identifierForVendor?.UUIDString ?: "unknown",
            isEmulator = isSimulator(),
        )
    }

    override fun hasCapability(capability: String): Boolean {
        return when (capability) {
            "camera" -> AVCaptureDevice.defaultDeviceWithMediaType("vide") != null
            "gps" -> CLLocationManager.locationServicesEnabled()
            "bluetooth" -> true // iOS设备都支持蓝牙
            "wifi" -> true // iOS设备都支持WiFi
            "nfc" -> device.model.contains("iPhone") && !isSimulator() // iPhone支持NFC
            "fingerprint" -> true // 现代iOS设备都支持Touch ID或Face ID
            "accelerometer" -> CMMotionManager().accelerometerAvailable
            "gyroscope" -> CMMotionManager().gyroAvailable
            "magnetometer" -> CMMotionManager().magnetometerAvailable
            "microphone" -> true // iOS设备都有麦克风
            "telephony" -> device.model.contains("iPhone")
            "vibration" -> true // iOS设备都支持震动
            else -> false
        }
    }

    override fun getSupportedCapabilities(): List<String> {
        val capabilities = mutableListOf<String>()

        if (hasCapability("camera")) capabilities.add("camera")
        if (hasCapability("gps")) capabilities.add("gps")
        if (hasCapability("bluetooth")) capabilities.add("bluetooth")
        if (hasCapability("wifi")) capabilities.add("wifi")
        if (hasCapability("nfc")) capabilities.add("nfc")
        if (hasCapability("fingerprint")) capabilities.add("fingerprint")
        if (hasCapability("accelerometer")) capabilities.add("accelerometer")
        if (hasCapability("gyroscope")) capabilities.add("gyroscope")
        if (hasCapability("magnetometer")) capabilities.add("magnetometer")
        if (hasCapability("microphone")) capabilities.add("microphone")
        if (hasCapability("telephony")) capabilities.add("telephony")
        if (hasCapability("vibration")) capabilities.add("vibration")

        return capabilities
    }

    override suspend fun performPlatformInitialization() {
        // iOS特定初始化
        config["system_name"] = device.systemName
        config["system_version"] = device.systemVersion
        config["model"] = device.model
        config["localized_model"] = device.localizedModel
        config["name"] = device.name
        config["user_interface_idiom"] = device.userInterfaceIdiom.toString()
        config["battery_monitoring_enabled"] = device.batteryMonitoringEnabled.toString()
        config["proximity_monitoring_enabled"] = device.proximityMonitoringEnabled.toString()
        config["multitasking_supported"] = device.multitaskingSupported.toString()

        // Bundle信息
        config["bundle_identifier"] = bundle.bundleIdentifier ?: "unknown"
        config["app_version"] = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString")?.toString() ?: "unknown"
        config["build_number"] = bundle.objectForInfoDictionaryKey("CFBundleVersion")?.toString() ?: "unknown"

        // 进程信息
        config["process_name"] = processInfo.processName
        config["host_name"] = processInfo.hostName
        config["operating_system_version"] = processInfo.operatingSystemVersionString
        config["processor_count"] = processInfo.processorCount.toString()
        config["active_processor_count"] = processInfo.activeProcessorCount.toString()
        config["physical_memory"] = processInfo.physicalMemory.toString()
    }

    override suspend fun performPlatformCleanup() {
        // iOS特定清理
        config.clear()
    }

    private fun isSimulator(): Boolean {
        return device.model.contains("Simulator") ||
            processInfo.environment.containsKey("SIMULATOR_DEVICE_NAME")
    }
}

/**
 * 平台管理器创建函数实现
 */
actual fun getCurrentPlatformManager(): PlatformManager = IOSPlatformManager()

actual fun createAndroidPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Android平台管理器在iOS平台不可用")
}

actual fun createIOSPlatformManager(): PlatformManager = IOSPlatformManager()

actual fun createWebPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Web平台管理器在iOS平台不可用")
}

actual fun createDesktopPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Desktop平台管理器在iOS平台不可用")
}

actual fun createHarmonyOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("HarmonyOS平台管理器在iOS平台不可用")
}

actual fun createMiniProgramPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("MiniProgram平台管理器在iOS平台不可用")
}

actual fun createWatchPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Watch平台管理器在iOS平台不可用")
}

actual fun createTVPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("TV平台管理器在iOS平台不可用")
}
