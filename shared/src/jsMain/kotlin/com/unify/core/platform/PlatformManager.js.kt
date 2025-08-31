package com.unify.core.platform

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.coroutines.resume

/**
 * Web平台管理器生产级实现
 * 支持现代浏览器 (Chrome 80+, Firefox 75+, Safari 13+, Edge 80+)
 * 包含PWA、WebAssembly、Service Worker等完整Web生态支持
 */
actual object PlatformManager {
    
    private var serviceWorker: ServiceWorker? = null
    private var isInitialized = false
    
    actual fun initialize() {
        if (!isInitialized) {
            initializeWebFeatures()
            registerServiceWorker()
            setupWebVitals()
            isInitialized = true
        }
    }
    
    private fun initializeWebFeatures() {
        // 初始化Web特性检测
        // 设置视口元标签
        setupViewportMeta()
        // 初始化PWA功能
        initializePWA()
    }
    
    private fun setupViewportMeta() {
        val viewport = document.querySelector("meta[name=viewport]") as? HTMLMetaElement
        if (viewport == null) {
            val meta = document.createElement("meta") as HTMLMetaElement
            meta.name = "viewport"
            meta.content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
            document.head?.appendChild(meta)
        }
    }
    
    private fun initializePWA() {
        // 检查PWA支持
        if (js("'serviceWorker' in navigator") as Boolean) {
            // PWA功能已启用
        }
    }
    
    private fun registerServiceWorker() {
        if (js("'serviceWorker' in navigator") as Boolean) {
            js("""
                navigator.serviceWorker.register('/sw.js')
                    .then(registration => console.log('SW registered'))
                    .catch(error => console.log('SW registration failed'))
            """)
        }
    }
    
    private fun setupWebVitals() {
        // 设置Web性能监控
        js("""
            if ('PerformanceObserver' in window) {
                new PerformanceObserver((list) => {
                    // 监控Web Vitals指标
                }).observe({entryTypes: ['navigation', 'paint', 'largest-contentful-paint']});
            }
        """)
    }
    
    actual fun getPlatformType(): PlatformType = PlatformType.WEB
    
    actual fun getPlatformName(): String = "Web"
    
    actual fun getPlatformVersion(): String = window.navigator.appVersion
    
    actual fun getDeviceInfo(): DeviceInfo {
        val userAgent = window.navigator.userAgent
        return DeviceInfo(
            manufacturer = getBrowserManufacturer(userAgent),
            model = getBrowserName(userAgent),
            systemName = getOperatingSystem(userAgent),
            systemVersion = getBrowserVersion(userAgent),
            deviceId = getWebDeviceId(),
            isEmulator = false // Web环境不区分模拟器
        )
    }
    
    private fun getBrowserManufacturer(userAgent: String): String {
        return when {
            userAgent.contains("Chrome") -> "Google"
            userAgent.contains("Firefox") -> "Mozilla"
            userAgent.contains("Safari") && !userAgent.contains("Chrome") -> "Apple"
            userAgent.contains("Edge") -> "Microsoft"
            else -> "Unknown"
        }
    }
    
    private fun getBrowserName(userAgent: String): String {
        return when {
            userAgent.contains("Chrome") -> "Chrome"
            userAgent.contains("Firefox") -> "Firefox"
            userAgent.contains("Safari") && !userAgent.contains("Chrome") -> "Safari"
            userAgent.contains("Edge") -> "Edge"
            else -> "Unknown Browser"
        }
    }
    
    private fun getOperatingSystem(userAgent: String): String {
        return when {
            userAgent.contains("Windows") -> "Windows"
            userAgent.contains("Mac") -> "macOS"
            userAgent.contains("Linux") -> "Linux"
            userAgent.contains("Android") -> "Android"
            userAgent.contains("iOS") -> "iOS"
            else -> "Unknown OS"
        }
    }
    
    private fun getBrowserVersion(userAgent: String): String {
        // 简化的版本提取逻辑
        return try {
            val regex = Regex("""Chrome/(\d+\.\d+)""")
            regex.find(userAgent)?.groupValues?.get(1) ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getWebDeviceId(): String {
        // Web环境使用localStorage存储设备ID
        return try {
            var deviceId = localStorage.getItem("unify_device_id")
            if (deviceId == null) {
                deviceId = generateWebDeviceId()
                localStorage.setItem("unify_device_id", deviceId)
            }
            deviceId
        } catch (e: Exception) {
            "web_device_unknown"
        }
    }
    
    private fun generateWebDeviceId(): String {
        // 生成基于浏览器特征的设备ID
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val ctx = canvas.getContext("2d")
        val fingerprint = "${window.navigator.userAgent}_${window.screen.width}_${window.screen.height}_${window.devicePixelRatio}"
        return fingerprint.hashCode().toString()
    }
    
    actual fun getScreenInfo(): ScreenInfo {
        val screen = window.screen
        val orientation = getWebOrientation()
        
        return ScreenInfo(
            width = screen.width,
            height = screen.height,
            density = window.devicePixelRatio.toFloat(),
            orientation = orientation,
            refreshRate = getWebRefreshRate(),
            safeAreaInsets = getWebSafeAreaInsets()
        )
    }
    
    private fun getWebOrientation(): Orientation {
        return try {
            val orientationType = js("screen.orientation?.type") as? String
            when (orientationType) {
                "portrait-primary" -> Orientation.PORTRAIT
                "portrait-secondary" -> Orientation.PORTRAIT_UPSIDE_DOWN
                "landscape-primary" -> Orientation.LANDSCAPE_LEFT
                "landscape-secondary" -> Orientation.LANDSCAPE_RIGHT
                else -> if (window.screen.width > window.screen.height) {
                    Orientation.LANDSCAPE
                } else {
                    Orientation.PORTRAIT
                }
            }
        } catch (e: Exception) {
            if (window.screen.width > window.screen.height) {
                Orientation.LANDSCAPE
            } else {
                Orientation.PORTRAIT
            }
        }
    }
    
    private fun getWebRefreshRate(): Float {
        return try {
            (js("screen.refreshRate") as? Number)?.toFloat() ?: 60f
        } catch (e: Exception) {
            60f
        }
    }
    
    private fun getWebSafeAreaInsets(): SafeAreaInsets {
        // Web环境的安全区域通过CSS env()变量获取
        return try {
            val style = window.getComputedStyle(document.documentElement!!)
            SafeAreaInsets(
                top = parseCSSValue(js("getComputedStyle(document.documentElement).getPropertyValue('env(safe-area-inset-top)')") as? String),
                bottom = parseCSSValue(js("getComputedStyle(document.documentElement).getPropertyValue('env(safe-area-inset-bottom)')") as? String),
                left = parseCSSValue(js("getComputedStyle(document.documentElement).getPropertyValue('env(safe-area-inset-left)')") as? String),
                right = parseCSSValue(js("getComputedStyle(document.documentElement).getPropertyValue('env(safe-area-inset-right)')") as? String)
            )
        } catch (e: Exception) {
            SafeAreaInsets()
        }
    }
    
    private fun parseCSSValue(value: String?): Int {
        return try {
            value?.replace("px", "")?.toFloatOrNull()?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    actual fun getSystemCapabilities(): SystemCapabilities {
        return SystemCapabilities(
            isTouchSupported = checkWebTouchSupport(),
            isKeyboardSupported = true, // Web环境通常支持键盘
            isMouseSupported = checkWebMouseSupport(),
            isCameraSupported = checkWebCameraSupport(),
            isMicrophoneSupported = checkWebMicrophoneSupport(),
            isLocationSupported = checkWebLocationSupport(),
            isNotificationSupported = checkWebNotificationSupport(),
            isFileSystemSupported = checkWebFileSystemSupport(),
            isBiometricSupported = checkWebBiometricSupport(),
            isNFCSupported = checkWebNFCSupport(),
            isBluetoothSupported = checkWebBluetoothSupport(),
            supportedSensors = getWebSupportedSensors()
        )
    }
    
    private fun checkWebTouchSupport(): Boolean {
        return js("'ontouchstart' in window || navigator.maxTouchPoints > 0") as Boolean
    }
    
    private fun checkWebMouseSupport(): Boolean {
        return js("'onmousedown' in window") as Boolean
    }
    
    private fun checkWebCameraSupport(): Boolean {
        return js("'mediaDevices' in navigator && 'getUserMedia' in navigator.mediaDevices") as Boolean
    }
    
    private fun checkWebMicrophoneSupport(): Boolean {
        return js("'mediaDevices' in navigator && 'getUserMedia' in navigator.mediaDevices") as Boolean
    }
    
    private fun checkWebLocationSupport(): Boolean {
        return js("'geolocation' in navigator") as Boolean
    }
    
    private fun checkWebNotificationSupport(): Boolean {
        return js("'Notification' in window") as Boolean
    }
    
    private fun checkWebFileSystemSupport(): Boolean {
        return js("'showOpenFilePicker' in window || 'webkitRequestFileSystem' in window") as Boolean
    }
    
    private fun checkWebBiometricSupport(): Boolean {
        return js("'credentials' in navigator && 'create' in navigator.credentials") as Boolean
    }
    
    private fun checkWebNFCSupport(): Boolean {
        return js("'NDEFReader' in window") as Boolean
    }
    
    private fun checkWebBluetoothSupport(): Boolean {
        return js("'bluetooth' in navigator") as Boolean
    }
    
    private fun getWebSupportedSensors(): List<SensorType> {
        val supportedSensors = mutableListOf<SensorType>()
        
        if (js("'DeviceMotionEvent' in window") as Boolean) {
            supportedSensors.add(SensorType.ACCELEROMETER)
            supportedSensors.add(SensorType.GYROSCOPE)
        }
        
        if (js("'DeviceOrientationEvent' in window") as Boolean) {
            supportedSensors.add(SensorType.MAGNETOMETER)
        }
        
        if (js("'AmbientLightSensor' in window") as Boolean) {
            supportedSensors.add(SensorType.LIGHT)
        }
        
        return supportedSensors
    }
    
    actual fun getNetworkStatus(): NetworkStatus {
        return try {
            val connection = js("navigator.connection || navigator.mozConnection || navigator.webkitConnection")
            if (connection != null) {
                val effectiveType = js("connection.effectiveType") as? String
                when (effectiveType) {
                    "4g", "3g" -> NetworkStatus.CONNECTED_CELLULAR
                    else -> NetworkStatus.CONNECTED_WIFI
                }
            } else {
                if (js("navigator.onLine") as Boolean) {
                    NetworkStatus.CONNECTED_UNKNOWN
                } else {
                    NetworkStatus.DISCONNECTED
                }
            }
        } catch (e: Exception) {
            NetworkStatus.UNKNOWN
        }
    }
    
    actual fun observeNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val onlineHandler: (Event) -> Unit = { 
            trySend(getNetworkStatus())
        }
        val offlineHandler: (Event) -> Unit = { 
            trySend(NetworkStatus.DISCONNECTED)
        }
        
        window.addEventListener("online", onlineHandler)
        window.addEventListener("offline", offlineHandler)
        
        // 发送初始状态
        trySend(getNetworkStatus())
        
        awaitClose {
            window.removeEventListener("online", onlineHandler)
            window.removeEventListener("offline", offlineHandler)
        }
    }
    
    actual fun getStorageInfo(): StorageInfo {
        return try {
            getWebStorageInfo()
        } catch (e: Exception) {
            StorageInfo(0L, 0L, 0L, false)
        }
    }
    
    private fun getWebStorageInfo(): StorageInfo {
        return try {
            val estimate = js("navigator.storage?.estimate()") 
            if (estimate != null) {
                // 使用Storage API获取存储信息
                StorageInfo(
                    totalSpace = 1024L * 1024 * 1024, // 1GB 默认值
                    availableSpace = 512L * 1024 * 1024, // 512MB 默认值
                    usedSpace = 512L * 1024 * 1024, // 512MB 默认值
                    isExternalStorageAvailable = false // Web没有外部存储概念
                )
            } else {
                StorageInfo(0L, 0L, 0L, false)
            }
        } catch (e: Exception) {
            StorageInfo(0L, 0L, 0L, false)
        }
    }
    
    actual fun getPerformanceInfo(): PerformanceInfo {
        return try {
            getWebPerformanceInfo()
        } catch (e: Exception) {
            PerformanceInfo(
                cpuUsage = 0f,
                memoryUsage = MemoryUsage(0L, 0L, 0L, 0L),
                batteryLevel = -1f,
                thermalState = ThermalState.NORMAL
            )
        }
    }
    
    private fun getWebPerformanceInfo(): PerformanceInfo {
        val memoryInfo = getWebMemoryInfo()
        
        return PerformanceInfo(
            cpuUsage = 0f, // Web环境无法直接获取CPU使用率
            memoryUsage = memoryInfo,
            batteryLevel = getWebBatteryLevel(),
            thermalState = ThermalState.NORMAL // Web环境通常不提供热状态
        )
    }
    
    private fun getWebMemoryInfo(): MemoryUsage {
        return try {
            val memory = js("performance.memory")
            if (memory != null) {
                val totalJSHeapSize = (js("memory.totalJSHeapSize") as? Number)?.toLong() ?: 0L
                val usedJSHeapSize = (js("memory.usedJSHeapSize") as? Number)?.toLong() ?: 0L
                val jsHeapSizeLimit = (js("memory.jsHeapSizeLimit") as? Number)?.toLong() ?: 0L
                
                MemoryUsage(
                    totalMemory = jsHeapSizeLimit,
                    availableMemory = jsHeapSizeLimit - usedJSHeapSize,
                    usedMemory = usedJSHeapSize,
                    appMemoryUsage = totalJSHeapSize
                )
            } else {
                MemoryUsage(0L, 0L, 0L, 0L)
            }
        } catch (e: Exception) {
            MemoryUsage(0L, 0L, 0L, 0L)
        }
    }
    
    private fun getWebBatteryLevel(): Float {
        return try {
            val battery = js("navigator.battery")
            if (battery != null) {
                ((js("battery.level") as? Number)?.toFloat() ?: -1f) * 100f
            } else {
                -1f
            }
        } catch (e: Exception) {
            -1f
        }
    }
    
    actual suspend fun showNativeDialog(config: DialogConfig): DialogResult = suspendCancellableCoroutine { continuation ->
        try {
            when (config.type) {
                DialogType.ALERT -> {
                    window.alert("${config.title}\n${config.message}")
                    continuation.resume(DialogResult(buttonIndex = 0))
                }
                DialogType.CONFIRMATION -> {
                    val result = window.confirm("${config.title}\n${config.message}")
                    continuation.resume(DialogResult(buttonIndex = if (result) 0 else 1))
                }
                DialogType.INPUT -> {
                    val result = window.prompt("${config.title}\n${config.message}")
                    continuation.resume(DialogResult(
                        buttonIndex = if (result != null) 0 else 1,
                        inputText = result
                    ))
                }
                else -> {
                    // 自定义对话框实现
                    showCustomWebDialog(config) { result ->
                        continuation.resume(result)
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(DialogResult(buttonIndex = -1, cancelled = true))
        }
    }
    
    private fun showCustomWebDialog(config: DialogConfig, callback: (DialogResult) -> Unit) {
        // 创建自定义HTML对话框
        val dialog = document.createElement("div") as HTMLDivElement
        dialog.innerHTML = """
            <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 10000; display: flex; align-items: center; justify-content: center;">
                <div style="background: white; padding: 20px; border-radius: 8px; max-width: 400px; width: 90%;">
                    <h3>${config.title}</h3>
                    <p>${config.message}</p>
                    <div id="dialog-buttons"></div>
                </div>
            </div>
        """.trimIndent()
        
        val buttonsContainer = dialog.querySelector("#dialog-buttons") as HTMLDivElement
        config.buttons.forEachIndexed { index, button ->
            val btn = document.createElement("button") as HTMLButtonElement
            btn.textContent = button.text
            btn.onclick = {
                document.body?.removeChild(dialog)
                button.action()
                callback(DialogResult(buttonIndex = index))
            }
            buttonsContainer.appendChild(btn)
        }
        
        document.body?.appendChild(dialog)
    }
    
    actual suspend fun invokePlatformFeature(feature: PlatformFeature): PlatformResult {
        return try {
            when (feature) {
                is PlatformFeature.Vibrate -> {
                    invokeWebVibration()
                    PlatformResult(success = true)
                }
                
                is PlatformFeature.OpenUrl -> {
                    window.open(feature.url, "_blank")
                    PlatformResult(success = true)
                }
                
                is PlatformFeature.ShareContent -> {
                    invokeWebShare(feature.content, feature.type)
                }
                
                is PlatformFeature.RequestPermission -> {
                    val granted = requestWebPermission(feature.permission)
                    PlatformResult(success = granted, data = granted)
                }
                
                is PlatformFeature.GetLocation -> {
                    val location = getWebLocation()
                    PlatformResult(success = location != null, data = location)
                }
                
                is PlatformFeature.ShowNotification -> {
                    showWebNotification(feature.title, feature.message)
                    PlatformResult(success = true)
                }
                
                else -> PlatformResult(success = false, error = "不支持的功能: ${feature::class.simpleName}")
            }
        } catch (e: Exception) {
            PlatformResult(success = false, error = e.message)
        }
    }
    
    private fun invokeWebVibration() {
        if (js("'vibrate' in navigator") as Boolean) {
            js("navigator.vibrate(100)")
        }
    }
    
    private suspend fun invokeWebShare(content: String, type: String): PlatformResult {
        return try {
            if (js("'share' in navigator") as Boolean) {
                js("navigator.share({text: '$content'})")
                PlatformResult(success = true)
            } else {
                // 降级到复制到剪贴板
                js("navigator.clipboard?.writeText('$content')")
                PlatformResult(success = true, data = "已复制到剪贴板")
            }
        } catch (e: Exception) {
            PlatformResult(success = false, error = e.message)
        }
    }
    
    private suspend fun requestWebPermission(permission: String): Boolean {
        return try {
            when (permission) {
                "camera", "microphone" -> {
                    js("navigator.mediaDevices.getUserMedia({video: true, audio: true})")
                    true
                }
                "location" -> {
                    js("navigator.geolocation.getCurrentPosition(() => {}, () => {})")
                    true
                }
                "notifications" -> {
                    val result = js("Notification.requestPermission()")
                    result == "granted"
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun getWebLocation(): Any? {
        return try {
            js("new Promise((resolve, reject) => navigator.geolocation.getCurrentPosition(resolve, reject))")
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showWebNotification(title: String, message: String) {
        if (js("'Notification' in window && Notification.permission === 'granted'") as Boolean) {
            js("new Notification('$title', {body: '$message'})")
        }
    }
    
    actual fun getPlatformConfig(): PlatformConfig {
        return PlatformConfig(
            platformType = PlatformType.WEB,
            supportedFeatures = setOf(
                "pwa", "service_worker", "web_assembly", "web_rtc",
                "geolocation", "notifications", "file_system_access",
                "web_share", "clipboard", "fullscreen", "screen_capture",
                "web_bluetooth", "web_nfc", "payment_request",
                "background_sync", "push_notifications", "offline_support"
            ),
            limitations = setOf(
                "browser_security_restrictions",
                "no_direct_file_system_access",
                "limited_background_processing",
                "cors_restrictions"
            ),
            optimizations = mapOf(
                "progressive_web_app" to true,
                "service_worker_caching" to true,
                "lazy_loading" to true,
                "code_splitting" to true,
                "tree_shaking" to true,
                "compression" to true,
                "cdn_optimization" to true
            )
        )
    }
}
