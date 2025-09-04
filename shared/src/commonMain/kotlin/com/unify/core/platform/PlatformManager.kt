package com.unify.core.platform

import com.unify.core.types.PlatformType
import com.unify.core.types.DeviceInfo
import com.unify.core.exceptions.UnifyPlatformException
import kotlinx.coroutines.flow.StateFlow

/**
 * 平台管理器接口
 * 提供跨平台的统一API
 */
interface PlatformManager {
    
    /**
     * 获取平台类型
     */
    fun getPlatformType(): PlatformType
    
    /**
     * 获取平台名称
     */
    fun getPlatformName(): String
    
    /**
     * 获取平台版本
     */
    fun getPlatformVersion(): String
    
    /**
     * 获取设备信息
     */
    suspend fun getDeviceInfo(): DeviceInfo
    
    /**
     * 检查平台能力
     */
    fun hasCapability(capability: String): Boolean
    
    /**
     * 获取所有支持的能力
     */
    fun getSupportedCapabilities(): List<String>
    
    /**
     * 初始化平台特定功能
     */
    suspend fun initialize(): Boolean
    
    /**
     * 清理平台资源
     */
    suspend fun cleanup()
    
    /**
     * 获取平台特定配置
     */
    fun getPlatformConfig(): Map<String, String>
    
    /**
     * 设置平台特定配置
     */
    fun setPlatformConfig(key: String, value: String)
}

/**
 * 平台管理器实现基类
 */
abstract class BasePlatformManager : PlatformManager {
    
    protected val config = mutableMapOf<String, String>()
    protected var isInitialized = false
    
    override fun getPlatformConfig(): Map<String, String> = config.toMap()
    
    override fun setPlatformConfig(key: String, value: String) {
        config[key] = value
    }
    
    override suspend fun initialize(): Boolean {
        if (isInitialized) return true
        
        try {
            performPlatformInitialization()
            isInitialized = true
            return true
        } catch (e: Exception) {
            throw UnifyPlatformException("平台初始化失败: ${getPlatformName()}", e.message)
        }
    }
    
    override suspend fun cleanup() {
        if (!isInitialized) return
        
        try {
            performPlatformCleanup()
            isInitialized = false
        } catch (e: Exception) {
            throw UnifyPlatformException("平台清理失败: ${getPlatformName()}", e.message)
        }
    }
    
    /**
     * 执行平台特定的初始化
     */
    protected abstract suspend fun performPlatformInitialization()
    
    /**
     * 执行平台特定的清理
     */
    protected abstract suspend fun performPlatformCleanup()
}

/**
 * 平台管理器工厂
 */
object PlatformManagerFactory {
    
    /**
     * 创建当前平台的管理器
     */
    fun createPlatformManager(): PlatformManager {
        return getCurrentPlatformManager()
    }
    
    /**
     * 创建指定平台的管理器
     */
    fun createPlatformManager(platformType: PlatformType): PlatformManager {
        return when (platformType) {
            PlatformType.ANDROID -> createAndroidPlatformManager()
            PlatformType.IOS -> createIOSPlatformManager()
            PlatformType.WEB -> createWebPlatformManager()
            PlatformType.DESKTOP -> createDesktopPlatformManager()
            PlatformType.HARMONY_OS -> createHarmonyOSPlatformManager()
            PlatformType.MINI_PROGRAM -> createMiniProgramPlatformManager()
            PlatformType.WATCH -> createWatchPlatformManager()
            PlatformType.TV -> createTVPlatformManager()
        }
    }
}

/**
 * 平台特定的管理器创建函数
 */
expect fun getCurrentPlatformManager(): PlatformManager
expect fun createAndroidPlatformManager(): PlatformManager
expect fun createIOSPlatformManager(): PlatformManager
expect fun createWebPlatformManager(): PlatformManager
expect fun createDesktopPlatformManager(): PlatformManager
expect fun createHarmonyOSPlatformManager(): PlatformManager
expect fun createMiniProgramPlatformManager(): PlatformManager
expect fun createWatchPlatformManager(): PlatformManager
expect fun createTVPlatformManager(): PlatformManager
