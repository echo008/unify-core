package com.unify.core.platform

/**
 * 小程序平台管理器接口
 * 提供小程序特有的平台功能
 */
expect class MiniAppPlatformManager {
    
    /**
     * 初始化小程序平台管理器
     */
    fun initialize()
    
    /**
     * 获取小程序平台类型
     */
    fun getMiniAppPlatform(): MiniAppPlatform
    
    /**
     * 获取小程序版本信息
     */
    fun getMiniAppVersion(): String
    
    /**
     * 获取宿主应用信息
     */
    fun getHostAppInfo(): MiniAppHostInfo
    
    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(): MiniAppUserInfo?
    
    /**
     * 调用原生API
     */
    suspend fun callNativeAPI(apiName: String, params: Map<String, Any>): Any?
    
    /**
     * 显示Toast消息
     */
    fun showToast(message: String, duration: ToastDuration = ToastDuration.SHORT)
    
    /**
     * 显示加载中
     */
    fun showLoading(title: String = "加载中...")
    
    /**
     * 隐藏加载中
     */
    fun hideLoading()
    
    /**
     * 导航到其他页面
     */
    fun navigateTo(url: String)
    
    /**
     * 返回上一页
     */
    fun navigateBack()
}

/**
 * 小程序平台类型
 */
enum class MiniAppPlatform {
    WECHAT, ALIPAY, BAIDU, TOUTIAO, QQ, UNKNOWN
}

/**
 * 宿主应用信息
 */
data class MiniAppHostInfo(
    val platform: MiniAppPlatform,
    val version: String,
    val sdkVersion: String
)

/**
 * 小程序用户信息
 */
data class MiniAppUserInfo(
    val openId: String,
    val unionId: String?,
    val nickName: String,
    val avatarUrl: String,
    val gender: Int,
    val city: String,
    val province: String,
    val country: String
)

/**
 * Toast持续时间
 */
enum class ToastDuration {
    SHORT, LONG
}
