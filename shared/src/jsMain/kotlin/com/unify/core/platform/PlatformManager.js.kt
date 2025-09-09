package com.unify.core.platform

import com.unify.core.types.DeviceInfo
import com.unify.core.types.PlatformType
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Navigator

/**
 * Web平台管理器实现
 */
class WebPlatformManager : BasePlatformManager() {
    private val navigator: Navigator = window.navigator

    override fun getPlatformType(): PlatformType = PlatformType.WEB

    override fun getPlatformName(): String = "Web"

    override fun getPlatformVersion(): String = window.navigator.appVersion

    override suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = getBrowserVendor(),
            model = getBrowserName(),
            systemName = getOperatingSystem(),
            systemVersion = getBrowserVersion(),
            deviceId = generateDeviceId(),
            isEmulator = false, // Web环境不是模拟器
        )
    }

    override fun hasCapability(capability: String): Boolean {
        return when (capability) {
            "camera" -> hasMediaDevices() && js("navigator.mediaDevices.getUserMedia") != null
            "gps" -> js("navigator.geolocation") != null
            "bluetooth" -> js("navigator.bluetooth") != null
            "wifi" -> js("navigator.connection") != null
            "nfc" -> false // Web NFC API支持有限
            "fingerprint" -> js("navigator.credentials") != null && js("window.PublicKeyCredential") != null
            "accelerometer" -> js("window.DeviceMotionEvent") != null
            "gyroscope" -> js("window.DeviceOrientationEvent") != null
            "magnetometer" -> js("window.DeviceOrientationEvent") != null
            "microphone" -> hasMediaDevices() && js("navigator.mediaDevices.getUserMedia") != null
            "telephony" -> false // Web环境不支持电话功能
            "vibration" -> js("navigator.vibrate") != null
            "notifications" -> js("window.Notification") != null
            "fullscreen" -> js("document.fullscreenEnabled") as? Boolean ?: false
            "clipboard" -> js("navigator.clipboard") != null
            "storage" -> js("window.localStorage") != null
            "indexeddb" -> js("window.indexedDB") != null
            "websocket" -> js("window.WebSocket") != null
            "webrtc" -> js("window.RTCPeerConnection") != null
            "webgl" -> hasWebGL()
            "webassembly" -> js("window.WebAssembly") != null
            else -> false
        }
    }

    override fun getSupportedCapabilities(): List<String> {
        val capabilities = mutableListOf<String>()

        if (hasCapability("camera")) capabilities.add("camera")
        if (hasCapability("gps")) capabilities.add("gps")
        if (hasCapability("bluetooth")) capabilities.add("bluetooth")
        if (hasCapability("wifi")) capabilities.add("wifi")
        if (hasCapability("fingerprint")) capabilities.add("fingerprint")
        if (hasCapability("accelerometer")) capabilities.add("accelerometer")
        if (hasCapability("gyroscope")) capabilities.add("gyroscope")
        if (hasCapability("magnetometer")) capabilities.add("magnetometer")
        if (hasCapability("microphone")) capabilities.add("microphone")
        if (hasCapability("vibration")) capabilities.add("vibration")
        if (hasCapability("notifications")) capabilities.add("notifications")
        if (hasCapability("fullscreen")) capabilities.add("fullscreen")
        if (hasCapability("clipboard")) capabilities.add("clipboard")
        if (hasCapability("storage")) capabilities.add("storage")
        if (hasCapability("indexeddb")) capabilities.add("indexeddb")
        if (hasCapability("websocket")) capabilities.add("websocket")
        if (hasCapability("webrtc")) capabilities.add("webrtc")
        if (hasCapability("webgl")) capabilities.add("webgl")
        if (hasCapability("webassembly")) capabilities.add("webassembly")

        return capabilities
    }

    override suspend fun performPlatformInitialization() {
        // Web特定初始化
        config["user_agent"] = navigator.userAgent
        config["language"] = navigator.language
        config["languages"] = navigator.languages.joinToString(",")
        config["platform"] = navigator.platform
        config["cookie_enabled"] = navigator.cookieEnabled.toString()
        config["online"] = navigator.onLine.toString()
        config["java_enabled"] = js("navigator.javaEnabled()").toString()
        config["do_not_track"] = (js("navigator.doNotTrack") as? String) ?: "unknown"

        // 屏幕信息
        config["screen_width"] = js("window.screen.width").toString()
        config["screen_height"] = js("window.screen.height").toString()
        config["screen_color_depth"] = js("window.screen.colorDepth").toString()
        config["screen_pixel_depth"] = js("window.screen.pixelDepth").toString()

        // 窗口信息
        config["window_width"] = js("window.innerWidth").toString()
        config["window_height"] = js("window.innerHeight").toString()
        config["device_pixel_ratio"] = js("window.devicePixelRatio").toString()

        // 时区信息
        config["timezone"] = js("Intl.DateTimeFormat().resolvedOptions().timeZone") as? String ?: "unknown"
        config["timezone_offset"] = js("new Date().getTimezoneOffset()").toString()

        // 连接信息
        val connection = js("navigator.connection")
        if (connection != null) {
            config["connection_type"] = js("navigator.connection.effectiveType") as? String ?: "unknown"
            config["connection_downlink"] = js("navigator.connection.downlink").toString()
            config["connection_rtt"] = js("navigator.connection.rtt").toString()
        }

        // 内存信息
        val memory = js("navigator.deviceMemory")
        if (memory != null) {
            config["device_memory"] = memory.toString()
        }

        // 硬件并发
        val hardwareConcurrency = js("navigator.hardwareConcurrency")
        if (hardwareConcurrency != null) {
            config["hardware_concurrency"] = hardwareConcurrency.toString()
        }
    }

    override suspend fun performPlatformCleanup() {
        // Web特定清理
        config.clear()
    }

    private fun getBrowserName(): String {
        val userAgent = navigator.userAgent
        return when {
            userAgent.contains("Chrome") -> "Chrome"
            userAgent.contains("Firefox") -> "Firefox"
            userAgent.contains("Safari") && !userAgent.contains("Chrome") -> "Safari"
            userAgent.contains("Edge") -> "Edge"
            userAgent.contains("Opera") -> "Opera"
            else -> "Unknown Browser"
        }
    }

    private fun getBrowserVersion(): String {
        val userAgent = navigator.userAgent
        val browserName = getBrowserName()

        return try {
            when (browserName) {
                "Chrome" -> {
                    val match = Regex("Chrome/([0-9.]+)").find(userAgent)
                    match?.groupValues?.get(1) ?: "Unknown"
                }
                "Firefox" -> {
                    val match = Regex("Firefox/([0-9.]+)").find(userAgent)
                    match?.groupValues?.get(1) ?: "Unknown"
                }
                "Safari" -> {
                    val match = Regex("Version/([0-9.]+)").find(userAgent)
                    match?.groupValues?.get(1) ?: "Unknown"
                }
                "Edge" -> {
                    val match = Regex("Edge/([0-9.]+)").find(userAgent)
                    match?.groupValues?.get(1) ?: "Unknown"
                }
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getBrowserVendor(): String {
        return when (getBrowserName()) {
            "Chrome" -> "Google"
            "Firefox" -> "Mozilla"
            "Safari" -> "Apple"
            "Edge" -> "Microsoft"
            "Opera" -> "Opera Software"
            else -> "Unknown"
        }
    }

    private fun getOperatingSystem(): String {
        val platform = navigator.platform
        val userAgent = navigator.userAgent

        return when {
            platform.contains("Win") -> "Windows"
            platform.contains("Mac") -> "macOS"
            platform.contains("Linux") -> "Linux"
            userAgent.contains("Android") -> "Android"
            userAgent.contains("iPhone") || userAgent.contains("iPad") -> "iOS"
            else -> "Unknown OS"
        }
    }

    private fun generateDeviceId(): String {
        // 生成基于浏览器特征的设备ID
        val features =
            listOf(
                navigator.userAgent,
                navigator.language,
                js("window.screen.width").toString(),
                js("window.screen.height").toString(),
                js("window.screen.colorDepth").toString(),
                js("new Date().getTimezoneOffset()").toString(),
            )

        return features.joinToString("|").hashCode().toString()
    }

    private fun hasMediaDevices(): Boolean {
        return js("navigator.mediaDevices") != null
    }

    private fun hasWebGL(): Boolean {
        return try {
            val canvas = document.createElement("canvas")
            val gl = js("canvas.getContext('webgl') || canvas.getContext('experimental-webgl')")
            gl != null
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 平台管理器创建函数实现
 */
actual fun getCurrentPlatformManager(): PlatformManager = WebPlatformManager()

actual fun createAndroidPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Android平台管理器在Web平台不可用")
}

actual fun createIOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("iOS平台管理器在Web平台不可用")
}

actual fun createWebPlatformManager(): PlatformManager = WebPlatformManager()

actual fun createDesktopPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Desktop平台管理器在Web平台不可用")
}

actual fun createHarmonyOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("HarmonyOS平台管理器在Web平台不可用")
}

actual fun createMiniProgramPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("MiniProgram平台管理器在Web平台不可用")
}

actual fun createWatchPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Watch平台管理器在Web平台不可用")
}

actual fun createTVPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("TV平台管理器在Web平台不可用")
}
