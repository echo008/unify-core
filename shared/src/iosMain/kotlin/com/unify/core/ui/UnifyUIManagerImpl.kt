package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.UIKit.UIApplication
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import java.util.UUID

/**
 * iOS平台的UI管理器实现
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
    
    init {
        updateScreenInfo()
        observeOrientationChanges()
    }
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
        updateSystemAppearance()
    }
    
    override fun toggleDarkMode() {
        val current = _currentTheme.value
        val newTheme = current.copy(isDark = !current.isDark)
        setTheme(newTheme)
    }
    
    override suspend fun showToast(message: String, duration: ToastDuration) {
        dispatch_async(dispatch_get_main_queue()) {
            // iOS没有原生Toast，使用UIAlertController模拟
            val alert = UIAlertController.alertControllerWithTitle(
                title = null,
                message = message,
                preferredStyle = UIAlertControllerStyleAlert
            )
            
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                alert, animated = true, completion = null
            )
            
            // 自动消失
            val delayTime = when (duration) {
                ToastDuration.SHORT -> 2.0
                ToastDuration.LONG -> 4.0
            }
            
            dispatch_async(dispatch_get_main_queue()) {
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay((delayTime * 1000).toLong())
                    alert.dismissViewControllerAnimated(true, completion = null)
                }
            }
        }
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        
        dispatch_async(dispatch_get_main_queue()) {
            val alert = UIAlertController.alertControllerWithTitle(
                title = message ?: "加载中...",
                message = null,
                preferredStyle = UIAlertControllerStyleAlert
            )
            
            // 添加加载指示器
            val indicator = UIActivityIndicatorView(UIActivityIndicatorViewStyleMedium)
            indicator.startAnimating()
            alert.setValue(indicator, forKey = "accessoryView")
            
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                alert, animated = true, completion = null
            )
        }
        
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
        
        dispatch_async(dispatch_get_main_queue()) {
            UIApplication.sharedApplication.keyWindow?.rootViewController?.dismissViewControllerAnimated(
                true, completion = null
            )
        }
    }
    
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        
        // iOS底部弹窗实现（使用UISheetPresentationController）
        
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        val window = UIApplication.sharedApplication.keyWindow
        val safeAreaInsets = window?.safeAreaInsets
        
        return SafeAreaInsets(
            top = (safeAreaInsets?.top?.toFloat() ?: 0f).dp,
            bottom = (safeAreaInsets?.bottom?.toFloat() ?: 0f).dp,
            left = (safeAreaInsets?.left?.toFloat() ?: 0f).dp,
            right = (safeAreaInsets?.right?.toFloat() ?: 0f).dp
        )
    }
    
    override fun getStatusBarHeight(): Dp {
        val statusBarFrame = UIApplication.sharedApplication.statusBarFrame
        return statusBarFrame.useContents { size.height.toFloat().dp }
    }
    
    override fun getNavigationBarHeight(): Dp {
        // iOS导航栏标准高度
        return 44.dp
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        dispatch_async(dispatch_get_main_queue()) {
            val statusBarStyle = when (style) {
                StatusBarStyle.LIGHT_CONTENT -> UIStatusBarStyleLightContent
                StatusBarStyle.DARK_CONTENT -> UIStatusBarStyleDarkContent
                StatusBarStyle.AUTO -> {
                    if (_currentTheme.value.isDark) {
                        UIStatusBarStyleLightContent
                    } else {
                        UIStatusBarStyleDarkContent
                    }
                }
            }
            
            UIApplication.sharedApplication.setStatusBarStyle(statusBarStyle, animated = true)
        }
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        dispatch_async(dispatch_get_main_queue()) {
            val navigationController = UIApplication.sharedApplication.keyWindow?.rootViewController as? UINavigationController
            val navigationBar = navigationController?.navigationBar
            
            when (style) {
                NavigationBarStyle.LIGHT -> {
                    navigationBar?.barStyle = UIBarStyleDefault
                }
                NavigationBarStyle.DARK -> {
                    navigationBar?.barStyle = UIBarStyleBlack
                }
                NavigationBarStyle.AUTO -> {
                    if (_currentTheme.value.isDark) {
                        setNavigationBarStyle(NavigationBarStyle.DARK)
                    } else {
                        setNavigationBarStyle(NavigationBarStyle.LIGHT)
                    }
                }
            }
        }
    }
    
    override fun requestFullscreen(enable: Boolean) {
        dispatch_async(dispatch_get_main_queue()) {
            UIApplication.sharedApplication.setStatusBarHidden(enable, withAnimation = UIStatusBarAnimationSlide)
        }
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        dispatch_async(dispatch_get_main_queue()) {
            val deviceOrientation = when (orientation) {
                OrientationLock.PORTRAIT -> UIDeviceOrientationPortrait
                OrientationLock.LANDSCAPE -> UIDeviceOrientationLandscapeLeft
                OrientationLock.AUTO -> UIDeviceOrientationUnknown
            }
            
            UIDevice.currentDevice.setValue(deviceOrientation, forKey = "orientation")
        }
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        val device = UIDevice.currentDevice
        val screen = UIScreen.mainScreen
        
        return mapOf(
            "platform" to "iOS",
            "ui_framework" to "SwiftUI + Compose Multiplatform",
            "ios_version" to device.systemVersion,
            "device_model" to device.model,
            "screen_scale" to screen.scale,
            "supports_dark_mode" to true,
            "supports_dynamic_type" to true,
            "supports_haptic_feedback" to true,
            "safe_area_supported" to true,
            "human_interface_guidelines" to "iOS 17"
        )
    }
    
    private fun updateScreenInfo() {
        val screen = UIScreen.mainScreen
        val bounds = screen.bounds
        val scale = screen.scale
        
        val widthPoints = bounds.useContents { size.width }
        val heightPoints = bounds.useContents { size.height }
        
        // 计算屏幕尺寸类别
        val minDimension = minOf(widthPoints, heightPoints) * scale
        _screenSizeClass.value = when {
            minDimension < 600 -> ScreenSizeClass.COMPACT
            minDimension < 840 -> ScreenSizeClass.MEDIUM
            minDimension < 1200 -> ScreenSizeClass.EXPANDED
            minDimension < 1600 -> ScreenSizeClass.LARGE
            else -> ScreenSizeClass.EXTRA_LARGE
        }
        
        // 更新方向
        val orientation = UIApplication.sharedApplication.statusBarOrientation
        _orientation.value = when (orientation) {
            UIInterfaceOrientationLandscapeLeft,
            UIInterfaceOrientationLandscapeRight -> Orientation.LANDSCAPE
            else -> Orientation.PORTRAIT
        }
    }
    
    private fun observeOrientationChanges() {
        // 监听设备方向变化
        platform.Foundation.NSNotificationCenter.defaultCenter.addObserverForName(
            UIDeviceOrientationDidChangeNotification,
            `object` = null,
            queue = platform.Foundation.NSOperationQueue.mainQueue
        ) { _ ->
            updateScreenInfo()
        }
    }
    
    private fun updateSystemAppearance() {
        dispatch_async(dispatch_get_main_queue()) {
            val window = UIApplication.sharedApplication.keyWindow
            val isDark = _currentTheme.value.isDark
            
            if (@Suppress("CAST_NEVER_SUCCEEDS") (platform.Foundation.NSProcessInfo.processInfo.operatingSystemVersion.majorVersion >= 13)) {
                window?.overrideUserInterfaceStyle = if (isDark) {
                    UIUserInterfaceStyleDark
                } else {
                    UIUserInterfaceStyleLight
                }
            }
        }
    }
}
