package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Toolkit
import java.util.UUID
import javax.swing.JOptionPane

/**
 * Desktop平台的UI管理器实现
 */
actual class UnifyUIManagerImpl : UnifyUIManager {
    
    private val _currentTheme = MutableStateFlow(UnifyTheme())
    override val currentTheme: StateFlow<UnifyTheme> = _currentTheme.asStateFlow()
    
    private val _screenSizeClass = MutableStateFlow(ScreenSizeClass.LARGE)
    override val screenSizeClass: StateFlow<ScreenSizeClass> = _screenSizeClass.asStateFlow()
    
    private val _orientation = MutableStateFlow(Orientation.LANDSCAPE)
    override val orientation: StateFlow<Orientation> = _orientation.asStateFlow()
    
    private val loadingHandles = mutableMapOf<String, LoadingHandle>()
    private val bottomSheetHandles = mutableMapOf<String, BottomSheetHandle>()
    
    init {
        updateScreenInfo()
    }
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
    }
    
    override fun toggleDarkMode() {
        val current = _currentTheme.value
        _currentTheme.value = current.copy(isDark = !current.isDark)
    }
    
    override suspend fun showToast(message: String, duration: ToastDuration) {
        // 使用系统通知或对话框显示Toast
        JOptionPane.showMessageDialog(null, message, "通知", JOptionPane.INFORMATION_MESSAGE)
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        // Desktop原生加载对话框实现（使用JDialog）
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
        // 隐藏对应的加载对话框
    }
    
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        // Desktop底部弹窗显示逻辑（使用JPanel）
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        // Desktop通常没有安全区域概念
        return SafeAreaInsets(0.dp, 0.dp, 0.dp, 0.dp)
    }
    
    override fun getStatusBarHeight(): Dp {
        // Desktop没有状态栏
        return 0.dp
    }
    
    override fun getNavigationBarHeight(): Dp {
        // Desktop没有导航栏
        return 0.dp
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        // Desktop没有状态栏，无需实现
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        // Desktop没有导航栏，无需实现
    }
    
    override fun requestFullscreen(enable: Boolean) {
        // Desktop全屏模式切换实现
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        // Desktop通常不需要锁定方向
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        val toolkit = Toolkit.getDefaultToolkit()
        val screenSize = toolkit.screenSize
        
        return mapOf(
            "platform" to "Desktop",
            "compose_version" to "1.7.0",
            "ui_framework" to "Compose Desktop",
            "screen_width" to screenSize.width,
            "screen_height" to screenSize.height,
            "supports_window_management" to true,
            "supports_system_tray" to true,
            "supports_file_dialogs" to true
        )
    }
    
    private fun updateScreenInfo() {
        val toolkit = Toolkit.getDefaultToolkit()
        val screenSize = toolkit.screenSize
        
        // 根据屏幕尺寸确定屏幕类别
        _screenSizeClass.value = when {
            screenSize.width < 1200 -> ScreenSizeClass.MEDIUM
            screenSize.width < 1600 -> ScreenSizeClass.LARGE
            else -> ScreenSizeClass.EXTRA_LARGE
        }
        
        // Desktop通常是横屏
        _orientation.value = if (screenSize.width > screenSize.height) {
            Orientation.LANDSCAPE
        } else {
            Orientation.PORTRAIT
        }
    }
}
