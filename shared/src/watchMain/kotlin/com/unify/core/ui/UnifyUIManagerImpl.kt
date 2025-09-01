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
 * Watch平台的UI管理器实现
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
        // 使用Watch平台Toast显示
        // Wear OS: 使用ConfirmationActivity
        // watchOS: 使用本地通知
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        // 使用Watch平台加载指示器
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
    }
    
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        // Watch平台通常使用页面导航而非底部弹窗
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        // Watch设备的安全区域（圆形屏幕考虑）
        return SafeAreaInsets(
            top = 8.dp,
            bottom = 8.dp,
            left = 8.dp,
            right = 8.dp
        )
    }
    
    override fun getStatusBarHeight(): Dp {
        // Watch设备通常没有传统状态栏
        return 0.dp
    }
    
    override fun getNavigationBarHeight(): Dp {
        // Watch设备没有导航栏
        return 0.dp
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        // Watch设备状态栏样式设置
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        // Watch设备导航样式设置
    }
    
    override fun requestFullscreen(enable: Boolean) {
        // Watch设备全屏模式
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        // Watch设备方向锁定（通常固定为竖屏）
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "Watch",
            "compose_version" to "1.7.0",
            "ui_framework" to "Wear Compose",
            "screen_shape" to "round", // 圆形屏幕
            "screen_size" to "small",
            "supports_complications" to true,
            "supports_tiles" to true,
            "supports_always_on" to true,
            "battery_optimization" to true
        )
    }
}
