package com.unify.miniapp

import com.unify.miniapp.wechat.WeChatMiniAppAdapter
import com.unify.miniapp.alipay.AlipayMiniAppAdapter
import com.unify.miniapp.bytedance.ByteDanceMiniAppAdapter

/**
 * 小程序管理器
 * 按照fix-plan.md技术方案整合所有小程序平台适配器
 */
object MiniAppManager {
    
    private var currentAdapter: MiniAppAdapter? = null
    private var currentPlatform: MiniAppPlatform? = null
    
    /**
     * 自动检测并初始化小程序环境
     */
    suspend fun initialize(): Boolean {
        // 按优先级检测平台环境
        if (checkWeChatEnvironment()) {
            return initializePlatform(MiniAppPlatform.WeChat)
        }
        if (checkAlipayEnvironment()) {
            return initializePlatform(MiniAppPlatform.Alipay)
        }
        if (checkByteDanceEnvironment()) {
            return initializePlatform(MiniAppPlatform.ByteDance)
        }
        
        console.warn("未检测到支持的小程序环境")
        return false
    }
    
    /**
     * 初始化指定平台
     */
    private suspend fun initializePlatform(platform: MiniAppPlatform): Boolean {
        val adapter = when (platform) {
            MiniAppPlatform.WeChat -> WeChatMiniAppAdapter()
            MiniAppPlatform.Alipay -> AlipayMiniAppAdapter()
            MiniAppPlatform.ByteDance -> ByteDanceMiniAppAdapter()
            else -> null
        }
        
        return adapter?.let {
            val success = it.initialize()
            if (success) {
                currentAdapter = it
                currentPlatform = platform
                console.log("${platform}初始化成功")
            }
            success
        } ?: false
    }
    
    /**
     * 获取当前适配器
     */
    fun getCurrentAdapter(): MiniAppAdapter? = currentAdapter
    
    /**
     * 获取当前平台
     */
    fun getCurrentPlatform(): MiniAppPlatform? = currentPlatform
    
    /**
     * 是否已初始化
     */
    fun isInitialized(): Boolean = currentAdapter != null
    
    // === 统一API代理方法 ===
    
    fun navigateTo(page: String) {
        currentAdapter?.navigateTo(page)
    }
    
    fun showToast(message: String) {
        currentAdapter?.showToast(message)
    }
    
    fun getSystemInfo(): SystemInfo? {
        return currentAdapter?.getSystemInfo()
    }
    
    fun showLoading(title: String = "加载中...") {
        currentAdapter?.showLoading(title)
    }
    
    fun hideLoading() {
        currentAdapter?.hideLoading()
    }
    
    fun navigateBack() {
        currentAdapter?.navigateBack()
    }
    
    // === 平台环境检测函数 ===
    
    private fun checkWeChatEnvironment(): Boolean {
        return js("""
            return typeof wx !== 'undefined' && wx.getSystemInfoSync;
        """).unsafeCast<Boolean>()
    }
    
    private fun checkAlipayEnvironment(): Boolean {
        return js("""
            return typeof my !== 'undefined' && my.getSystemInfoSync;
        """).unsafeCast<Boolean>()
    }
    
    private fun checkByteDanceEnvironment(): Boolean {
        return js("""
            return typeof tt !== 'undefined' && tt.getSystemInfoSync;
        """).unsafeCast<Boolean>()
    }
}
