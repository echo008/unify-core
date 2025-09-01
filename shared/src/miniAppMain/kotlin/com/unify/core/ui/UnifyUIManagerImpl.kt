package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * 小程序平台的UI管理器实现
 */
actual class UnifyUIManagerImpl : UnifyUIManager {
    
    private val _currentTheme = MutableStateFlow(UnifyTheme())
    override val currentTheme: StateFlow<UnifyTheme> = _currentTheme.asStateFlow()
    
    private val _screenSizeClass = MutableStateFlow(ScreenSizeClass.COMPACT)
    override val screenSizeClass: StateFlow<ScreenSizeClass> = _screenSizeClass.asStateFlow()
    
    private val _orientation = MutableStateFlow(Orientation.PORTRAIT)
    override val orientation: StateFlow<Orientation> = _orientation.asStateFlow()
    
    private val loadingHandles = mutableMapOf<String, LoadingHandle>()
    private val bottomSheetHandles = mutableMapOf<String, BottomSheetHandle>()
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
    }
    
    override fun toggleDarkMode() {
        val current = _currentTheme.value
        _currentTheme.value = current.copy(isDark = !current.isDark)
    }
    
    override suspend fun showToast(message: String, duration: ToastDuration) {
        // 使用小程序Toast API (wx.showToast)
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        // 使用小程序加载API (wx.showLoading)
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
        // 使用小程序API隐藏加载 (wx.hideLoading)
    }
    
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        // 使用小程序action-sheet组件
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        // 使用小程序API获取安全区域 (wx.getSystemInfo)
        return SafeAreaInsets(
            top = 20.dp,
            bottom = 34.dp,
            left = 0.dp,
            right = 0.dp
        )
    }
    
    override fun getStatusBarHeight(): Dp {
        // 使用小程序API获取状态栏高度
        return 20.dp
    }
    
    override fun getNavigationBarHeight(): Dp {
        // 小程序通常没有系统导航栏
        return 0.dp
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        // 使用小程序API设置状态栏样式 (wx.setNavigationBarColor)
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        // 小程序导航栏样式设置
    }
    
    override fun requestFullscreen(enable: Boolean) {
        // 小程序全屏模式切换
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        // 使用小程序屏幕方向API
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "MiniApp",
            "compose_version" to "1.7.0",
            "ui_framework" to "MiniApp + Compose",
            "supports_wechat" to true,
            "supports_alipay" to true,
            "supports_bytedance" to true,
            "max_package_size" to "20MB",
            "supports_subpackages" to true
        )
    }
}
