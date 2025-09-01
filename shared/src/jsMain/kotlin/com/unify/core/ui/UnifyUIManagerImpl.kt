package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import java.util.UUID

/**
 * Web平台的UI管理器实现
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
        observeWindowResize()
        detectInitialTheme()
    }
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
        updateDocumentTheme(theme)
    }
    
    override fun toggleDarkMode() {
        val current = _currentTheme.value
        val newTheme = current.copy(isDark = !current.isDark)
        setTheme(newTheme)
    }
    
    override suspend fun showToast(message: String, duration: ToastDuration) {
        // 创建Toast元素
        val toast = document.createElement("div") as HTMLElement
        toast.className = "unify-toast"
        toast.textContent = message
        
        // 设置样式
        toast.style.apply {
            position = "fixed"
            top = "20px"
            right = "20px"
            backgroundColor = "rgba(0, 0, 0, 0.8)"
            color = "white"
            padding = "12px 16px"
            borderRadius = "8px"
            zIndex = "10000"
            fontSize = "14px"
            fontFamily = "system-ui, -apple-system, sans-serif"
            boxShadow = "0 4px 12px rgba(0, 0, 0, 0.15)"
            transform = "translateX(100%)"
            transition = "transform 0.3s ease"
        }
        
        document.body?.appendChild(toast)
        
        // 动画显示
        window.setTimeout({
            toast.style.transform = "translateX(0)"
        }, 10)
        
        // 自动隐藏
        val delayTime = when (duration) {
            ToastDuration.SHORT -> 2000
            ToastDuration.LONG -> 4000
        }
        
        window.setTimeout({
            toast.style.transform = "translateX(100%)"
            window.setTimeout({
                document.body?.removeChild(toast)
            }, 300)
        }, delayTime)
    }
    
    override suspend fun showLoading(message: String?): LoadingHandle {
        val handle = LoadingHandle(
            id = UUID.randomUUID().toString(),
            message = message
        )
        loadingHandles[handle.id] = handle
        
        // 创建加载遮罩
        val overlay = document.createElement("div") as HTMLElement
        overlay.id = "loading-${handle.id}"
        overlay.className = "unify-loading-overlay"
        
        overlay.style.apply {
            position = "fixed"
            top = "0"
            left = "0"
            width = "100%"
            height = "100%"
            backgroundColor = "rgba(0, 0, 0, 0.5)"
            display = "flex"
            alignItems = "center"
            justifyContent = "center"
            zIndex = "9999"
            flexDirection = "column"
        }
        
        // 创建加载指示器
        val spinner = document.createElement("div") as HTMLElement
        spinner.className = "unify-spinner"
        spinner.style.apply {
            width = "40px"
            height = "40px"
            border = "4px solid #f3f3f3"
            borderTop = "4px solid #3498db"
            borderRadius = "50%"
            animation = "spin 1s linear infinite"
        }
        
        // 添加CSS动画
        if (document.getElementById("unify-spinner-style") == null) {
            val style = document.createElement("style")
            style.id = "unify-spinner-style"
            style.textContent = """
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            """.trimIndent()
            document.head?.appendChild(style)
        }
        
        overlay.appendChild(spinner)
        
        // 添加消息
        if (message != null) {
            val messageElement = document.createElement("div") as HTMLElement
            messageElement.textContent = message
            messageElement.style.apply {
                color = "white"
                marginTop = "16px"
                fontSize = "16px"
                fontFamily = "system-ui, -apple-system, sans-serif"
            }
            overlay.appendChild(messageElement)
        }
        
        document.body?.appendChild(overlay)
        
        return handle
    }
    
    override suspend fun hideLoading(handle: LoadingHandle) {
        loadingHandles.remove(handle.id)
        val overlay = document.getElementById("loading-${handle.id}")
        overlay?.let { document.body?.removeChild(it) }
    }
    
    override suspend fun showBottomSheet(content: @Composable () -> Unit): BottomSheetHandle {
        val handle = BottomSheetHandle(id = UUID.randomUUID().toString())
        bottomSheetHandles[handle.id] = handle
        
        // Web底部弹窗实现（使用CSS模态框）
        
        return handle
    }
    
    override fun getSafeAreaInsets(): SafeAreaInsets {
        // Web平台通常没有安全区域概念，返回0
        return SafeAreaInsets(0.dp, 0.dp, 0.dp, 0.dp)
    }
    
    override fun getStatusBarHeight(): Dp {
        // Web平台没有状态栏
        return 0.dp
    }
    
    override fun getNavigationBarHeight(): Dp {
        // Web平台没有导航栏
        return 0.dp
    }
    
    override fun setStatusBarStyle(style: StatusBarStyle) {
        // Web平台不支持状态栏样式
    }
    
    override fun setNavigationBarStyle(style: NavigationBarStyle) {
        // Web平台不支持导航栏样式
    }
    
    override fun requestFullscreen(enable: Boolean) {
        if (enable) {
            document.documentElement?.requestFullscreen()
        } else {
            if (document.fullscreenElement != null) {
                document.exitFullscreen()
            }
        }
    }
    
    override fun setOrientation(orientation: OrientationLock) {
        // Web平台的屏幕方向API
        try {
            val screen = window.asDynamic().screen
            when (orientation) {
                OrientationLock.PORTRAIT -> screen.orientation?.lock("portrait")
                OrientationLock.LANDSCAPE -> screen.orientation?.lock("landscape")
                OrientationLock.AUTO -> screen.orientation?.unlock()
            }
        } catch (e: Exception) {
            console.warn("Screen orientation API not supported")
        }
    }
    
    override fun getPlatformUIConfig(): Map<String, Any> {
        val userAgent = window.navigator.userAgent
        val screen = window.screen
        
        return mapOf(
            "platform" to "Web",
            "ui_framework" to "Compose for Web",
            "user_agent" to userAgent,
            "screen_width" to screen.width,
            "screen_height" to screen.height,
            "device_pixel_ratio" to window.devicePixelRatio,
            "supports_touch" to (window.asDynamic().ontouchstart !== undefined),
            "supports_fullscreen" to (document.documentElement?.asDynamic()?.requestFullscreen !== undefined),
            "supports_notifications" to ("Notification" in window),
            "supports_service_worker" to ("serviceWorker" in window.navigator),
            "supports_web_share" to ("share" in window.navigator),
            "color_scheme" to getCurrentColorScheme()
        )
    }
    
    private fun updateScreenInfo() {
        val width = window.innerWidth
        val height = window.innerHeight
        
        // 计算屏幕尺寸类别
        val minDimension = minOf(width, height)
        _screenSizeClass.value = when {
            minDimension < 600 -> ScreenSizeClass.COMPACT
            minDimension < 840 -> ScreenSizeClass.MEDIUM
            minDimension < 1200 -> ScreenSizeClass.EXPANDED
            minDimension < 1600 -> ScreenSizeClass.LARGE
            else -> ScreenSizeClass.EXTRA_LARGE
        }
        
        // 更新方向
        _orientation.value = if (width > height) {
            Orientation.LANDSCAPE
        } else {
            Orientation.PORTRAIT
        }
    }
    
    private fun observeWindowResize() {
        window.addEventListener("resize", { _: Event ->
            updateScreenInfo()
        })
        
        // 监听方向变化
        window.addEventListener("orientationchange", { _: Event ->
            window.setTimeout({
                updateScreenInfo()
            }, 100) // 延迟更新，等待方向变化完成
        })
    }
    
    private fun detectInitialTheme() {
        val prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches
        if (prefersDark) {
            _currentTheme.value = _currentTheme.value.copy(isDark = true)
        }
        
        // 监听系统主题变化
        window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change") { event ->
            val isDark = event.asDynamic().matches as Boolean
            _currentTheme.value = _currentTheme.value.copy(isDark = isDark)
        }
    }
    
    private fun updateDocumentTheme(theme: UnifyTheme) {
        val root = document.documentElement
        root?.style?.setProperty("--unify-primary-color", theme.primaryColor.toString())
        root?.style?.setProperty("--unify-secondary-color", theme.secondaryColor.toString())
        root?.style?.setProperty("--unify-background-color", theme.backgroundColor.toString())
        root?.style?.setProperty("--unify-surface-color", theme.surfaceColor.toString())
        root?.style?.setProperty("--unify-error-color", theme.errorColor.toString())
        
        // 设置颜色方案
        root?.style?.setProperty("color-scheme", if (theme.isDark) "dark" else "light")
        
        // 更新meta标签
        updateThemeColorMeta(theme.primaryColor)
    }
    
    private fun updateThemeColorMeta(color: Color) {
        val metaThemeColor = document.querySelector("meta[name='theme-color']") as? HTMLElement
            ?: document.createElement("meta").also { meta ->
                (meta as HTMLElement).setAttribute("name", "theme-color")
                document.head?.appendChild(meta)
            } as HTMLElement
        
        metaThemeColor.setAttribute("content", color.toString())
    }
    
    private fun getCurrentColorScheme(): String {
        return if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
            "dark"
        } else {
            "light"
        }
    }
}
