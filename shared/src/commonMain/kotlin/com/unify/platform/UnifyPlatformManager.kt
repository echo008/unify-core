package com.unify.platform

import com.unify.network.NetworkConfig
import com.unify.network.UnifyNetworkService
import com.unify.network.NetworkFactory
import com.unify.storage.StorageType
import com.unify.storage.StorageConfig
import com.unify.storage.StorageFactory
import com.unify.storage.UnifyStorage

/**
 * 统一平台管理器
 * 基于文档第三部分平台适配实现要求
 */

/**
 * 平台类型枚举
 */
enum class PlatformType {
    ANDROID,
    IOS,
    WEB,
    DESKTOP,
    HARMONY_OS
}

/**
 * 平台信息接口
 */
interface PlatformInfo {
    val platformType: PlatformType
    val platformVersion: String
    val deviceModel: String
    val isDebug: Boolean
    
    fun getScreenSize(): Pair<Int, Int>
    fun getDeviceId(): String?
    fun isMobile(): Boolean
    fun isTablet(): Boolean
}

/**
 * 平台能力接口
 */
interface PlatformCapabilities {
    val supportsFileSystem: Boolean
    val supportsCamera: Boolean
    val supportsLocation: Boolean
    val supportsPushNotifications: Boolean
    val supportsBackgroundTasks: Boolean
    val supportsBiometrics: Boolean
    val supportsNFC: Boolean
    val supportsVibration: Boolean
}

/**
 * 平台配置
 */
data class PlatformConfig(
    val networkConfig: NetworkConfig = NetworkConfig(),
    val storageConfig: StorageConfig = StorageConfig(),
    val enableLogging: Boolean = true,
    val enableCrashReporting: Boolean = true,
    val enableAnalytics: Boolean = false
)

/**
 * 统一平台管理器
 */
class UnifyPlatformManager(
    private val config: PlatformConfig = PlatformConfig()
) {
    private lateinit var _platformInfo: PlatformInfo
    private lateinit var _platformCapabilities: PlatformCapabilities
    private lateinit var _networkService: UnifyNetworkService
    private val _storageServices = mutableMapOf<StorageType, UnifyStorage>()
    
    val platformInfo: PlatformInfo get() = _platformInfo
    val platformCapabilities: PlatformCapabilities get() = _platformCapabilities
    val networkService: UnifyNetworkService get() = _networkService
    
    fun initialize() {
        _platformInfo = createPlatformInfo()
        _platformCapabilities = createPlatformCapabilities()
        _networkService = NetworkFactory.createNetworkService(config.networkConfig)
        initializeStorageServices()
    }
    
    fun getStorage(type: StorageType): UnifyStorage {
        return _storageServices.getOrPut(type) {
            StorageFactory.createStorage(type, config.storageConfig)
        }
    }
    
    private fun createPlatformInfo(): PlatformInfo {
        return when (getCurrentPlatform()) {
            PlatformType.ANDROID -> AndroidPlatformInfoImpl()
            PlatformType.IOS -> IOSPlatformInfoImpl()
            PlatformType.WEB -> WebPlatformInfoImpl()
            PlatformType.DESKTOP -> DesktopPlatformInfoImpl()
            PlatformType.HARMONY_OS -> HarmonyOSPlatformInfoImpl()
        }
    }
    
    private fun createPlatformCapabilities(): PlatformCapabilities {
        return when (getCurrentPlatform()) {
            PlatformType.ANDROID -> AndroidPlatformCapabilitiesImpl()
            PlatformType.IOS -> IOSPlatformCapabilitiesImpl()
            PlatformType.WEB -> WebPlatformCapabilitiesImpl()
            PlatformType.DESKTOP -> DesktopPlatformCapabilitiesImpl()
            PlatformType.HARMONY_OS -> HarmonyOSPlatformCapabilitiesImpl()
        }
    }
    
    private fun initializeStorageServices() {
        // 预初始化常用存储服务
        StorageType.values().forEach { type ->
            try {
                _storageServices[type] = StorageFactory.createStorage(type, config.storageConfig)
            } catch (e: Exception) {
                if (config.enableLogging) {
                    println("Failed to initialize storage type $type: ${e.message}")
                }
            }
        }
    }
    
    private fun getCurrentPlatform(): PlatformType {
        return getPlatformType()
    }
}

/**
 * 平台特定实现（各平台模块中实现）
 */
expect fun getPlatformType(): PlatformType

expect class AndroidPlatformInfoImpl() : PlatformInfo
expect class IOSPlatformInfoImpl() : PlatformInfo
expect class WebPlatformInfoImpl() : PlatformInfo
expect class DesktopPlatformInfoImpl() : PlatformInfo
expect class HarmonyOSPlatformInfoImpl() : PlatformInfo

expect class AndroidPlatformCapabilitiesImpl() : PlatformCapabilities
expect class IOSPlatformCapabilitiesImpl() : PlatformCapabilities
expect class WebPlatformCapabilitiesImpl() : PlatformCapabilities
expect class DesktopPlatformCapabilitiesImpl() : PlatformCapabilities
expect class HarmonyOSPlatformCapabilitiesImpl() : PlatformCapabilities

/**
 * 平台工具类
 */
object PlatformUtils {
    fun isAndroid(): Boolean = getPlatformType() == PlatformType.ANDROID
    fun isIOS(): Boolean = getPlatformType() == PlatformType.IOS
    fun isWeb(): Boolean = getPlatformType() == PlatformType.WEB
    fun isDesktop(): Boolean = getPlatformType() == PlatformType.DESKTOP
    fun isHarmonyOS(): Boolean = getPlatformType() == PlatformType.HARMONY_OS
    
    fun isMobilePlatform(): Boolean = isAndroid() || isIOS()
    fun isDesktopPlatform(): Boolean = isDesktop()
    fun isWebPlatform(): Boolean = isWeb()
    
    fun requiresPlatformSpecificImplementation(feature: String): Boolean {
        return when (feature) {
            "camera", "location", "biometrics", "nfc" -> isMobilePlatform()
            "file_system" -> !isWeb()
            "push_notifications" -> !isDesktop()
            else -> false
        }
    }
}

/**
 * 平台适配装饰器
 */
abstract class PlatformAdapter<T> {
    abstract fun adapt(input: T): T
    abstract fun isSupported(): Boolean
    
    fun adaptIfSupported(input: T): T {
        return if (isSupported()) adapt(input) else input
    }
}

/**
 * 网络适配器
 */
class NetworkPlatformAdapter : PlatformAdapter<NetworkConfig>() {
    override fun adapt(input: NetworkConfig): NetworkConfig {
        return when (getPlatformType()) {
            PlatformType.WEB -> input.copy(
                timeout = minOf(input.timeout, 30000L), // Web平台限制超时时间
                retryCount = minOf(input.retryCount, 2) // 减少重试次数
            )
            PlatformType.ANDROID, PlatformType.IOS -> input.copy(
                enableCache = true, // 移动平台启用缓存
                cacheMaxAge = 600000L // 10分钟缓存
            )
            else -> input
        }
    }
    
    override fun isSupported(): Boolean = true
}

/**
 * 存储适配器
 */
class StoragePlatformAdapter : PlatformAdapter<StorageConfig>() {
    override fun adapt(input: StorageConfig): StorageConfig {
        return when (getPlatformType()) {
            PlatformType.WEB -> input.copy(
                enableEncryption = false, // Web平台暂不支持加密
                enableBackup = false // Web平台不支持备份
            )
            PlatformType.ANDROID, PlatformType.IOS -> input.copy(
                enableEncryption = true, // 移动平台启用加密
                enableBackup = true // 移动平台支持备份
            )
            else -> input
        }
    }
    
    override fun isSupported(): Boolean = true
}

/**
 * 全局平台管理器实例
 */
object GlobalPlatformManager {
    private var _instance: UnifyPlatformManager? = null
    
    fun getInstance(config: PlatformConfig = PlatformConfig()): UnifyPlatformManager {
        return _instance ?: synchronized(this) {
            _instance ?: UnifyPlatformManager(config).also { 
                it.initialize()
                _instance = it 
            }
        }
    }
    
    fun isInitialized(): Boolean = _instance != null
    
    fun reset() {
        _instance = null
    }
}
