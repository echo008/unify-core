package com.unify.core.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Android平台的UI管理器实现
 */
actual class UnifyUIManagerImpl : UnifyUIManager {
    
    private val _currentTheme = MutableStateFlow(UnifyTheme())
    override val currentTheme: StateFlow<UnifyTheme> = _currentTheme.asStateFlow()
    
    private val _screenSizeClass = MutableStateFlow(ScreenSizeClass.COMPACT)
    override val screenSizeClass: StateFlow<ScreenSizeClass> = _screenSizeClass.asStateFlow()
    
    private val _orientation = MutableStateFlow(Orientation.PORTRAIT)
    override val orientation: StateFlow<Orientation> = _orientation.asStateFlow()
    
    private var context: Context? = null
    private val loadingHandles = mutableMapOf<String, LoadingHandle>()
    private val bottomSheetHandles = mutableMapOf<String, BottomSheetHandle>()
    
    fun setContext(context: Context) {
        this.context = context
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
        context?.let { ctx ->
            val androidDuration = when (duration) {
                ToastDuration.SHORT -> Toast.LENGTH_SHORT
                ToastDuration.LONG -> Toast.LENGTH_LONG
            }
            Toast.makeText(ctx, message, androidDuration).show()
        }
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        // Android原生加载对话框实现（使用ProgressDialog）
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
        // 隐藏对应的加载对话框
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        // 底部弹窗显示逻辑（使用BottomSheetScaffold）
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        context?.let { ctx ->
            if (ctx is Activity) {
                val windowInsets = WindowCompat.getInsetsController(ctx.window, ctx.window.decorView)
                val insets = WindowInsetsCompat.toWindowInsetsCompat(
                    ctx.window.decorView.rootWindowInsets
                ).getInsets(WindowInsetsCompat.Type.systemBars())
                
                val density = ctx.resources.displayMetrics.density
                return SafeAreaInsets(
                    top = (insets.top / density).dp,
                    bottom = (insets.bottom / density).dp,
                    left = (insets.left / density).dp,
                    right = (insets.right / density).dp
                )
            }
        }
        return SafeAreaInsets(0.dp, 0.dp, 0.dp, 0.dp)
    }
    
    override fun getStatusBarHeight(): Dp {
        context?.let { ctx ->
            val resourceId = ctx.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                val height = ctx.resources.getDimensionPixelSize(resourceId)
                val density = ctx.resources.displayMetrics.density
                return (height / density).dp
            }
        }
        return 24.dp // 默认值
    }
    
    override fun getNavigationBarHeight(): Dp {
        context?.let { ctx ->
            val resourceId = ctx.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                val height = ctx.resources.getDimensionPixelSize(resourceId)
                val density = ctx.resources.displayMetrics.density
                return (height / density).dp
            }
        }
        return 48.dp // 默认值
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        context?.let { ctx ->
            if (ctx is Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = ctx.window.insetsController
                when (style) {
                    StatusBarStyle.LIGHT_CONTENT -> {
                        controller?.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                    }
                    StatusBarStyle.DARK_CONTENT -> {
                        controller?.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                    }
                    StatusBarStyle.AUTO -> {
                        val isDark = _currentTheme.value.isDark
                        if (isDark) {
                            setStatusBarStyle(StatusBarStyle.LIGHT_CONTENT)
                        } else {
                            setStatusBarStyle(StatusBarStyle.DARK_CONTENT)
                        }
                    }
                }
            }
        }
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        context?.let { ctx ->
            if (ctx is Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = ctx.window.insetsController
                when (style) {
                    NavigationBarStyle.LIGHT -> {
                        controller?.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    }
                    NavigationBarStyle.DARK -> {
                        controller?.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    }
                    NavigationBarStyle.AUTO -> {
                        val isDark = _currentTheme.value.isDark
                        if (isDark) {
                            setNavigationBarStyle(NavigationBarStyle.LIGHT)
                        } else {
                            setNavigationBarStyle(NavigationBarStyle.DARK)
                        }
                    }
                }
            }
        }
    }
    
    override fun requestFullscreen(enable: Boolean) {
        context?.let { ctx ->
            if (ctx is Activity) {
                if (enable) {
                    WindowCompat.setDecorFitsSystemWindows(ctx.window, false)
                } else {
                    WindowCompat.setDecorFitsSystemWindows(ctx.window, true)
                }
            }
        }
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        context?.let { ctx ->
            if (ctx is Activity) {
                ctx.requestedOrientation = when (orientation) {
                    OrientationLock.PORTRAIT -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    OrientationLock.LANDSCAPE -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    OrientationLock.AUTO -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "Android",
            "compose_version" to "1.7.0",
            "material_design" to "Material 3",
            "supports_dynamic_color" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
            "supports_edge_to_edge" to true,
            "min_sdk" to Build.VERSION_CODES.LOLLIPOP,
            "target_sdk" to Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        )
    }
    
    private fun updateScreenInfo() {
        context?.let { ctx ->
            val configuration = ctx.resources.configuration
            
            // 更新屏幕尺寸类别
            val screenWidthDp = configuration.screenWidthDp
            _screenSizeClass.value = when {
                screenWidthDp < 600 -> ScreenSizeClass.COMPACT
                screenWidthDp < 840 -> ScreenSizeClass.MEDIUM
                screenWidthDp < 1200 -> ScreenSizeClass.EXPANDED
                screenWidthDp < 1600 -> ScreenSizeClass.LARGE
                else -> ScreenSizeClass.EXTRA_LARGE
            }
            
            // 更新屏幕方向
            _orientation.value = when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
                Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
                else -> Orientation.PORTRAIT
            }
        }
    }
}
