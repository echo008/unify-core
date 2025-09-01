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
 * TV平台的UI管理器实现
 */
actual class UnifyUIManagerImpl : UnifyUIManager {
    
    private val _currentTheme = MutableStateFlow(UnifyTheme())
    override val currentTheme: StateFlow<UnifyTheme> = _currentTheme.asStateFlow()
    
    private val _screenSizeClass = MutableStateFlow(ScreenSizeClass.EXPANDED)
    override val screenSizeClass: StateFlow<ScreenSizeClass> = _screenSizeClass.asStateFlow()
    
    private val _orientation = MutableStateFlow(Orientation.LANDSCAPE)
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
        // 使用TV平台Toast显示
        // Android TV: 使用Toast
        // tvOS: 使用Alert
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        // 使用TV平台加载指示器
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
    }
    
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        // TV平台使用侧边栏或覆盖层
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        // TV设备的安全区域（考虑过扫描）
        return SafeAreaInsets(
            top = 27.dp,
            bottom = 27.dp,
            left = 48.dp,
            right = 48.dp
        )
    }
    
    override fun getStatusBarHeight(): Dp {
        // TV设备没有状态栏
        return 0.dp
    }
    
    override fun getNavigationBarHeight(): Dp {
        // TV设备没有导航栏
        return 0.dp
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        // TV设备状态栏样式设置
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        // TV设备导航样式设置
    }
    
    override fun requestFullscreen(enable: Boolean) {
        // TV设备通常默认全屏
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        // TV设备通常固定为横屏
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "TV",
            "compose_version" to "1.7.0",
            "ui_framework" to "TV Compose",
            "screen_type" to "large",
            "input_method" to "remote_control",
            "supports_10_foot_ui" to true,
            "supports_leanback" to true,
            "focus_navigation" to true,
            "overscan_compensation" to true
        )
    }
}
