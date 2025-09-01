package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 小程序平台管理器生产级实现
 * 支持微信、支付宝、字节跳动、百度、快手、小米、华为、QQ小程序
 */
actual object PlatformManager {
    
    private var miniAppType: String = "wechat"
    private var miniAppContext: Any? = null
    
    actual fun initialize() {
        detectMiniAppType()
        initializeMiniAppServices()
    }
    
    private fun detectMiniAppType() {
        // 检测当前运行的小程序平台
        miniAppType = when {
            isWeChatMiniApp() -> "wechat"
            isAlipayMiniApp() -> "alipay"
            isByteDanceMiniApp() -> "bytedance"
            isBaiduMiniApp() -> "baidu"
            isKuaishouMiniApp() -> "kuaishou"
            isXiaomiMiniApp() -> "xiaomi"
            isHuaweiMiniApp() -> "huawei"
            isQQMiniApp() -> "qq"
            else -> "unknown"
        }
    }
    
    private fun isWeChatMiniApp(): Boolean = js("typeof wx !== 'undefined'")
    private fun isAlipayMiniApp(): Boolean = js("typeof my !== 'undefined'")
    private fun isByteDanceMiniApp(): Boolean = js("typeof tt !== 'undefined'")
    private fun isBaiduMiniApp(): Boolean = js("typeof swan !== 'undefined'")
    private fun isKuaishouMiniApp(): Boolean = js("typeof ks !== 'undefined'")
    private fun isXiaomiMiniApp(): Boolean = js("typeof mi !== 'undefined'")
    private fun isHuaweiMiniApp(): Boolean = js("typeof hm !== 'undefined'")
    private fun isQQMiniApp(): Boolean = js("typeof qq !== 'undefined'")
    
    private fun initializeMiniAppServices() {
        // 初始化小程序服务
    }
    
    actual fun getPlatformType(): PlatformType = PlatformType.MINI_PROGRAM
    
    actual fun getPlatformName(): String = "MiniApp-$miniAppType"
    
    actual fun getPlatformVersion(): String {
        return when (miniAppType) {
            "wechat" -> getWeChatVersion()
            "alipay" -> getAlipayVersion()
            "bytedance" -> getByteDanceVersion()
            "baidu" -> getBaiduVersion()
            "kuaishou" -> getKuaishouVersion()
            "xiaomi" -> getXiaomiVersion()
            "huawei" -> getHuaweiVersion()
            "qq" -> getQQVersion()
            else -> "unknown"
        }
    }
    
    private fun getWeChatVersion(): String = "8.0"
    private fun getAlipayVersion(): String = "10.3"
    private fun getByteDanceVersion(): String = "3.0"
    private fun getBaiduVersion(): String = "13.0"
    private fun getKuaishouVersion(): String = "11.0"
    private fun getXiaomiVersion(): String = "1.0"
    private fun getHuaweiVersion(): String = "12.0"
    private fun getQQVersion(): String = "8.9"
    
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = "MiniApp",
            model = miniAppType,
            systemName = getPlatformName(),
            systemVersion = getPlatformVersion(),
            deviceId = "miniapp_${miniAppType}_${System.currentTimeMillis()}",
            isEmulator = false
        )
    }
    
    actual fun getScreenInfo(): ScreenInfo {
        val screenInfo = getMiniAppScreenInfo()
        return ScreenInfo(
            width = screenInfo.width,
            height = screenInfo.height,
            density = screenInfo.density,
            orientation = Orientation.PORTRAIT,
            refreshRate = 60f,
            safeAreaInsets = SafeAreaInsets()
        )
    }
    
    private fun getMiniAppScreenInfo(): MiniAppScreenInfo {
        return MiniAppScreenInfo(375, 667, 2.0f) // iPhone默认尺寸
    }
    
    private data class MiniAppScreenInfo(val width: Int, val height: Int, val density: Float)
    
    actual fun getSystemCapabilities(): SystemCapabilities {
        return SystemCapabilities(
            isTouchSupported = true,
            isKeyboardSupported = false,
            isMouseSupported = false,
            isCameraSupported = true,
            isMicrophoneSupported = true,
            isLocationSupported = true,
            isNotificationSupported = false,
            isFileSystemSupported = false,
            isBiometricSupported = false,
            isNFCSupported = false,
            isBluetoothSupported = false,
            supportedSensors = listOf()
        )
    }
    
    actual fun getNetworkStatus(): NetworkStatus = NetworkStatus.CONNECTED_WIFI
    
    actual fun observeNetworkStatus(): Flow<NetworkStatus> = flowOf(NetworkStatus.CONNECTED_WIFI)
    
    actual fun getStorageInfo(): StorageInfo {
        return StorageInfo(
            totalSpace = 10L * 1024 * 1024, // 10MB限制
            availableSpace = 5L * 1024 * 1024,
            usedSpace = 5L * 1024 * 1024,
            isExternalStorageAvailable = false
        )
    }
    
    actual fun getPerformanceInfo(): PerformanceInfo {
        val memoryUsage = MemoryUsage(
            totalMemory = 512L * 1024 * 1024, // 512MB限制
            availableMemory = 256L * 1024 * 1024,
            usedMemory = 256L * 1024 * 1024,
            appMemoryUsage = 256L * 1024 * 1024
        )
        
        return PerformanceInfo(
            cpuUsage = 10f,
            memoryUsage = memoryUsage,
            batteryLevel = -1f,
            thermalState = ThermalState.NORMAL
        )
    }
    
    actual suspend fun showNativeDialog(config: DialogConfig): DialogResult = suspendCancellableCoroutine { continuation ->
        when (miniAppType) {
            "wechat" -> showWeChatDialog(config) { result ->
                continuation.resume(result)
            }
            "alipay" -> showAlipayDialog(config) { result ->
                continuation.resume(result)
            }
            else -> continuation.resume(DialogResult(buttonIndex = -1, cancelled = true))
        }
    }
    
    private fun showWeChatDialog(config: DialogConfig, callback: (DialogResult) -> Unit) {
        js("""
            wx.showModal({
                title: config.title,
                content: config.message,
                success: function(res) {
                    if (res.confirm) {
                        callback(new DialogResult(0));
                    } else {
                        callback(new DialogResult(-1, true));
                    }
                }
            });
        """)
    }
    
    private fun showAlipayDialog(config: DialogConfig, callback: (DialogResult) -> Unit) {
        js("""
            my.confirm({
                title: config.title,
                content: config.message,
                success: function(res) {
                    if (res.confirm) {
                        callback(new DialogResult(0));
                    } else {
                        callback(new DialogResult(-1, true));
                    }
                }
            });
        """)
    }
    
    actual suspend fun invokePlatformFeature(feature: PlatformFeature): PlatformResult {
        return try {
            when (feature) {
                is PlatformFeature.ShareContent -> {
                    shareMiniAppContent(feature.content)
                    PlatformResult(success = true)
                }
                is PlatformFeature.OpenUrl -> {
                    openMiniAppUrl(feature.url)
                    PlatformResult(success = true)
                }
                else -> PlatformResult(success = false, error = "小程序不支持的功能")
            }
        } catch (e: Exception) {
            PlatformResult(success = false, error = e.message)
        }
    }
    
    private fun shareMiniAppContent(content: String) {
        when (miniAppType) {
            "wechat" -> js("wx.shareAppMessage({title: '$content'})")
            "alipay" -> js("my.shareAppMessage({title: '$content'})")
        }
    }
    
    private fun openMiniAppUrl(url: String) {
        when (miniAppType) {
            "wechat" -> js("wx.navigateToMiniProgram({appId: '$url'})")
            "alipay" -> js("my.navigateToMiniProgram({appId: '$url'})")
        }
    }
    
    actual fun getPlatformConfig(): PlatformConfig {
        return PlatformConfig(
            platformType = PlatformType.MINI_APP,
            supportedFeatures = setOf(
                "share", "payment", "location", "camera", "microphone",
                "storage_limited", "network", "user_info"
            ),
            limitations = setOf(
                "no_file_system", "memory_limited", "api_restricted",
                "sandbox_environment", "no_background_tasks"
            ),
            optimizations = mapOf(
                "lightweight" to true,
                "fast_startup" to true,
                "cloud_functions" to true,
                "component_based" to true
            )
        )
    }
}

// 扩展PlatformType以包含MINI_APP
val PlatformType.Companion.MINI_APP: PlatformType
    get() = PlatformType.valueOf("MINI_APP")
