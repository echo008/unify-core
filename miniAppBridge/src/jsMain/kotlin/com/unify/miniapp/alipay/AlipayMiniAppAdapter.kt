package com.unify.miniapp.alipay

import com.unify.miniapp.MiniAppAdapter
import com.unify.miniapp.SystemInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 支付宝小程序适配器实现
 * 严格按照fix-plan.md技术方案实现支付宝小程序API桥接
 */
class AlipayMiniAppAdapter : MiniAppAdapter {
    
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized
    
    private val _systemInfo = MutableStateFlow<SystemInfo?>(null)
    override val systemInfo: StateFlow<SystemInfo?> = _systemInfo
    
    /**
     * 初始化支付宝小程序环境
     */
    override suspend fun initialize(): Boolean {
        return try {
            val success = js("""
                (function() {
                    if (typeof my !== 'undefined') {
                        console.log('支付宝小程序环境初始化成功');
                        return true;
                    } else {
                        console.warn('支付宝小程序环境不可用');
                        return false;
                    }
                })()
            """).unsafeCast<Boolean>()
            
            if (success) {
                _isInitialized.value = true
                loadSystemInfo()
            }
            success
        } catch (e: Exception) {
            console.error("支付宝小程序初始化异常:", e)
            false
        }
    }
    
    /**
     * 页面导航 - 按照fix-plan.md方案实现
     */
    override fun navigateTo(page: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.navigateTo({
                    url: arguments[0],
                    success: function(res) {
                        console.log('导航成功:', res);
                    },
                    fail: function(err) {
                        console.error('导航失败:', err);
                    }
                });
            }
        """)
    }
    
    /**
     * 返回上一页
     */
    override fun navigateBack() {
        js("""
            if (typeof my !== 'undefined') {
                my.navigateBack({
                    delta: 1
                });
            }
        """)
    }
    
    /**
     * 重定向到页面
     */
    override fun redirectTo(page: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.redirectTo({
                    url: arguments[0]
                });
            }
        """)
    }
    
    /**
     * 切换Tab页面
     */
    override fun switchTab(page: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.switchTab({
                    url: arguments[0]
                });
            }
        """)
    }
    
    /**
     * Toast显示 - 按照fix-plan.md方案实现（注意支付宝使用content而非title）
     */
    override fun showToast(message: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.showToast({
                    content: arguments[0],
                    type: 'none',
                    duration: 2000
                });
            }
        """)
    }
    
    /**
     * 显示加载提示
     */
    override fun showLoading(title: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.showLoading({
                    content: arguments[0],
                    delay: 0
                });
            }
        """)
    }
    
    /**
     * 隐藏加载提示
     */
    override fun hideLoading() {
        js("""
            if (typeof my !== 'undefined') {
                my.hideLoading();
            }
        """)
    }
    
    /**
     * 显示模态对话框
     */
    override fun showModal(title: String, content: String, callback: (Boolean) -> Unit) {
        js("""
            if (typeof my !== 'undefined') {
                my.confirm({
                    title: arguments[0],
                    content: arguments[1],
                    success: function(res) {
                        arguments[2](res.confirm);
                    }
                });
            }
        """)
    }
    
    /**
     * 获取系统信息 - 支付宝小程序API适配
     */
    override fun getSystemInfo(): SystemInfo {
        val info = js("""
            if (typeof my !== 'undefined') {
                return my.getSystemInfoSync();
            }
            return {
                platform: 'unknown',
                version: '1.0.0',
                screenWidth: 375,
                screenHeight: 667,
                pixelRatio: 2,
                statusBarHeight: 20,
                safeArea: {
                    top: 20,
                    bottom: 667,
                    left: 0,
                    right: 375,
                    width: 375,
                    height: 647
                }
            };
        """)
        
        return SystemInfo(
            platform = "alipay-miniapp",
            version = info.asDynamic().version?.toString() ?: "1.0.0",
            screenWidth = info.asDynamic().screenWidth?.toString()?.toIntOrNull() ?: 375,
            screenHeight = info.asDynamic().screenHeight?.toString()?.toIntOrNull() ?: 667,
            pixelRatio = info.asDynamic().pixelRatio?.toString()?.toFloatOrNull() ?: 2.0f,
            statusBarHeight = info.asDynamic().statusBarHeight?.toString()?.toIntOrNull() ?: 20,
            safeAreaTop = info.asDynamic().safeArea?.top?.toString()?.toIntOrNull() ?: 20,
            safeAreaBottom = info.asDynamic().safeArea?.bottom?.toString()?.toIntOrNull() ?: 667
        )
    }
    
    /**
     * 设置导航栏标题
     */
    override fun setNavigationBarTitle(title: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.setNavigationBar({
                    title: arguments[0]
                });
            }
        """)
    }
    
    /**
     * 设置导航栏颜色
     */
    override fun setNavigationBarColor(frontColor: String, backgroundColor: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.setNavigationBar({
                    frontColor: arguments[0],
                    backgroundColor: arguments[1]
                });
            }
        """)
    }
    
    /**
     * 获取用户信息
     */
    override fun getUserInfo(callback: (String?) -> Unit) {
        js("""
            if (typeof my !== 'undefined') {
                my.getAuthUserInfo({
                    success: function(res) {
                        arguments[0](JSON.stringify(res));
                    },
                    fail: function(err) {
                        console.error('获取用户信息失败:', err);
                        arguments[0](null);
                    }
                });
            } else {
                arguments[0](null);
            }
        """)
    }
    
    /**
     * 获取位置信息
     */
    override fun getLocation(callback: (Double?, Double?) -> Unit) {
        js("""
            if (typeof my !== 'undefined') {
                my.getLocation({
                    type: 1, // 支付宝使用数字类型
                    success: function(res) {
                        arguments[0](res.latitude, res.longitude);
                    },
                    fail: function(err) {
                        console.error('获取位置失败:', err);
                        arguments[0](null, null);
                    }
                });
            } else {
                arguments[0](null, null);
            }
        """)
    }
    
    /**
     * 选择图片
     */
    override fun chooseImage(count: Int, callback: (List<String>) -> Unit) {
        js("""
            if (typeof my !== 'undefined') {
                my.chooseImage({
                    count: arguments[0],
                    success: function(res) {
                        arguments[1](res.apFilePaths || res.tempFilePaths || []);
                    },
                    fail: function(err) {
                        console.error('选择图片失败:', err);
                        arguments[1]([]);
                    }
                });
            } else {
                arguments[1]([]);
            }
        """)
    }
    
    /**
     * 预览图片
     */
    override fun previewImage(urls: List<String>, current: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.previewImage({
                    urls: arguments[0],
                    current: arguments[1] || 0
                });
            }
        """)
    }
    
    /**
     * 设置剪贴板内容
     */
    override fun setClipboardData(data: String) {
        js("""
            if (typeof my !== 'undefined') {
                my.setClipboard({
                    text: arguments[0],
                    success: function() {
                        console.log('复制成功');
                    }
                });
            }
        """)
    }
    
    /**
     * 获取剪贴板内容
     */
    override fun getClipboardData(callback: (String?) -> Unit) {
        js("""
            if (typeof my !== 'undefined') {
                my.getClipboard({
                    success: function(res) {
                        arguments[0](res.text);
                    },
                    fail: function(err) {
                        console.error('获取剪贴板失败:', err);
                        arguments[0](null);
                    }
                });
            } else {
                arguments[0](null);
            }
        """)
    }
    
    /**
     * 震动反馈
     */
    override fun vibrateShort() {
        js("""
            if (typeof my !== 'undefined') {
                my.vibrate({
                    type: 'short'
                });
            }
        """)
    }
    
    /**
     * 长震动反馈
     */
    override fun vibrateLong() {
        js("""
            if (typeof my !== 'undefined') {
                my.vibrate({
                    type: 'long'
                });
            }
        """)
    }
    
    /**
     * 加载系统信息到状态流
     */
    private fun loadSystemInfo() {
        try {
            val info = getSystemInfo()
            _systemInfo.value = info
        } catch (e: Exception) {
            console.error("加载系统信息失败:", e)
        }
    }
    
    /**
     * 页面生命周期：页面显示
     */
    override fun onShow() {
        js("""
            console.log('支付宝小程序页面显示');
        """)
    }
    
    /**
     * 页面生命周期：页面隐藏
     */
    override fun onHide() {
        js("""
            console.log('支付宝小程序页面隐藏');
        """)
    }
    
    /**
     * 页面生命周期：页面卸载
     */
    override fun onUnload() {
        js("""
            console.log('支付宝小程序页面卸载');
        """)
        _isInitialized.value = false
        _systemInfo.value = null
    }
}
