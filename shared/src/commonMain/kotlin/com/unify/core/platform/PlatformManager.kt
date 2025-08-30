package com.unify.core.platform

/**
 * 平台管理器 - 统一平台抽象层
 * 使用expect/actual机制实现跨平台功能
 */
expect object PlatformManager {
    
    /**
     * 初始化平台管理器
     */
    fun initialize()
    
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
    fun getDeviceInfo(): String
    
    /**
     * 获取屏幕信息
     */
    fun getScreenInfo(): ScreenInfo
    
    /**
     * 是否支持触摸
     */
    fun isTouchSupported(): Boolean
    
    /**
     * 是否支持键盘
     */
    fun isKeyboardSupported(): Boolean
    
    /**
     * 获取网络状态
     */
    fun getNetworkStatus(): NetworkStatus
}

/**
 * 屏幕信息数据类
 */
data class ScreenInfo(
    val width: Int,
    val height: Int,
    val density: Float,
    val orientation: Orientation
)

/**
 * 屏幕方向枚举
 */
enum class Orientation {
    PORTRAIT, LANDSCAPE, UNKNOWN
}

/**
 * 网络状态枚举
 */
enum class NetworkStatus {
    CONNECTED, DISCONNECTED, UNKNOWN
}
