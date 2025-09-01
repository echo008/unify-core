package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.StateFlow

/**
 * 统一UI管理器接口
 * 提供跨平台一致的UI管理功能
 */
interface UnifyUIManager {
    
    /**
     * 获取当前主题
     */
    val currentTheme: StateFlow<UnifyTheme>
    
    /**
     * 获取屏幕尺寸类别
     */
    val screenSizeClass: StateFlow<ScreenSizeClass>
    
    /**
     * 获取当前方向
     */
    val orientation: StateFlow<Orientation>
    
    /**
     * 设置主题
     */
    fun setTheme(theme: UnifyTheme)
    
    /**
     * 切换深色/浅色模式
     */
    fun toggleDarkMode()
    
    /**
     * 显示Toast消息
     */
    suspend fun showToast(message: String, duration: ToastDuration = ToastDuration.SHORT)
    
    /**
     * 显示加载指示器
     */
    suspend fun showLoading(message: String? = null): LoadingHandle
    
    /**
     * 隐藏加载指示器
     */
    suspend fun hideLoading(handle: LoadingHandle)
    
    /**
     * 显示底部弹窗
     */
    suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle
    
    /**
     * 获取安全区域内边距
     */
    fun getSafeAreaInsets(): SafeAreaInsets
    
    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(): Dp
    
    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(): Dp
    
    /**
     * 设置状态栏样式
     */
    fun setStatusBarStyle(style: StatusBarStyle)
    
    /**
     * 设置导航栏样式
     */
    fun setNavigationBarStyle(style: NavigationBarStyle)
    
    /**
     * 请求全屏模式
     */
    fun requestFullscreen(enable: Boolean)
    
    /**
     * 设置屏幕方向
     */
    fun setOrientation(orientation: OrientationLock)
    
    /**
     * 获取平台特定的UI配置
     */
    fun getPlatformUIConfig(): Map<String, Any>
}

/**
 * 统一主题定义
 */
data class UnifyTheme(
    val isDark: Boolean = false,
    val primaryColor: Color = Color(0xFF1976D2),
    val secondaryColor: Color = Color(0xFF03DAC6),
    val backgroundColor: Color = Color.White,
    val surfaceColor: Color = Color.White,
    val errorColor: Color = Color(0xFFB00020),
    val onPrimary: Color = Color.White,
    val onSecondary: Color = Color.Black,
    val onBackground: Color = Color.Black,
    val onSurface: Color = Color.Black,
    val onError: Color = Color.White
)

/**
 * 屏幕尺寸类别
 */
enum class ScreenSizeClass {
    COMPACT,    // 手机竖屏
    MEDIUM,     // 手机横屏/小平板
    EXPANDED,   // 平板
    LARGE,      // 桌面
    EXTRA_LARGE // 大屏幕
}

/**
 * 屏幕方向
 */
enum class Orientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * Toast持续时间
 */
enum class ToastDuration {
    SHORT,
    LONG
}

/**
 * 加载指示器句柄
 */
data class LoadingHandle(
    val id: String,
    val message: String?
)

/**
 * 底部弹窗句柄
 */
data class BottomSheetHandle(
    val id: String
)

/**
 * 安全区域内边距
 */
data class SafeAreaInsets(
    val top: Dp,
    val bottom: Dp,
    val left: Dp,
    val right: Dp
)

/**
 * 状态栏样式
 */
enum class StatusBarStyle {
    LIGHT_CONTENT,  // 浅色内容（深色背景）
    DARK_CONTENT,   // 深色内容（浅色背景）
    AUTO            // 自动根据主题调整
}

/**
 * 导航栏样式
 */
enum class NavigationBarStyle {
    LIGHT,
    DARK,
    AUTO
}

/**
 * 屏幕方向锁定
 */
enum class OrientationLock {
    PORTRAIT,
    LANDSCAPE,
    AUTO
}

/**
 * UI管理器实现类
 * 使用expect/actual机制实现跨平台功能
 */
expect class UnifyUIManagerImpl : UnifyUIManager
