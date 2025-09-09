package com.unify.miniapp

import kotlinx.coroutines.flow.StateFlow

/**
 * 小程序适配器接口
 * 定义跨平台小程序功能的统一接口
 */
interface MiniAppAdapter {
    
    /**
     * 初始化状态
     */
    val isInitialized: StateFlow<Boolean>
    
    /**
     * 系统信息状态
     */
    val systemInfo: StateFlow<SystemInfo?>
    
    /**
     * 初始化小程序环境
     */
    suspend fun initialize(): Boolean
    
    // === 导航相关 ===
    
    /**
     * 导航到指定页面
     */
    fun navigateTo(page: String)
    
    /**
     * 返回上一页
     */
    fun navigateBack()
    
    /**
     * 重定向到页面
     */
    fun redirectTo(page: String)
    
    /**
     * 切换Tab页面
     */
    fun switchTab(page: String)
    
    // === UI交互相关 ===
    
    /**
     * 显示Toast提示
     */
    fun showToast(message: String)
    
    /**
     * 显示加载提示
     */
    fun showLoading(title: String = "加载中...")
    
    /**
     * 隐藏加载提示
     */
    fun hideLoading()
    
    /**
     * 显示模态对话框
     */
    fun showModal(title: String, content: String, callback: (Boolean) -> Unit)
    
    // === 系统信息相关 ===
    
    /**
     * 获取系统信息
     */
    fun getSystemInfo(): SystemInfo
    
    /**
     * 设置导航栏标题
     */
    fun setNavigationBarTitle(title: String)
    
    /**
     * 设置导航栏颜色
     */
    fun setNavigationBarColor(frontColor: String, backgroundColor: String)
    
    // === 用户相关 ===
    
    /**
     * 获取用户信息
     */
    fun getUserInfo(callback: (String?) -> Unit)
    
    /**
     * 获取位置信息
     */
    fun getLocation(callback: (Double?, Double?) -> Unit)
    
    // === 媒体相关 ===
    
    /**
     * 选择图片
     */
    fun chooseImage(count: Int = 1, callback: (List<String>) -> Unit)
    
    /**
     * 预览图片
     */
    fun previewImage(urls: List<String>, current: String = "")
    
    // === 设备功能相关 ===
    
    /**
     * 设置剪贴板内容
     */
    fun setClipboardData(data: String)
    
    /**
     * 获取剪贴板内容
     */
    fun getClipboardData(callback: (String?) -> Unit)
    
    /**
     * 短震动反馈
     */
    fun vibrateShort()
    
    /**
     * 长震动反馈
     */
    fun vibrateLong()
    
    // === 生命周期相关 ===
    
    /**
     * 页面显示
     */
    fun onShow()
    
    /**
     * 页面隐藏
     */
    fun onHide()
    
    /**
     * 页面卸载
     */
    fun onUnload()
}

/**
 * 系统信息数据类
 */
data class SystemInfo(
    val platform: String,                    // 平台标识
    val version: String,                     // 版本号
    val screenWidth: Int,                    // 屏幕宽度
    val screenHeight: Int,                   // 屏幕高度
    val pixelRatio: Float,                   // 设备像素比
    val statusBarHeight: Int,                // 状态栏高度
    val safeAreaTop: Int,                    // 安全区域顶部
    val safeAreaBottom: Int                  // 安全区域底部
)

