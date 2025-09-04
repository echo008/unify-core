package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.MediaQueryList
import org.w3c.dom.get

/**
 * Web/JS平台UnifyUIManager实现
 */
class UnifyUIManagerImpl : UnifyUIManager {
    // 主题状态管理
    private val _currentTheme = MutableStateFlow(createDefaultTheme())
    private val _fontScale = MutableStateFlow(1.0f)
    
    // 动画和无障碍设置
    private var animationsEnabled = true
    private var accessibilityEnabled = false
    
    // 媒体查询监听器
    private val darkModeMediaQuery: MediaQueryList = window.matchMedia("(prefers-color-scheme: dark)")
    
    init {
        // 监听系统主题变化
        darkModeMediaQuery.addListener { 
            if (_currentTheme.value.name == "Auto") {
                _currentTheme.value = createDefaultTheme()
            }
        }
    }
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
        applyThemeToDocument(theme)
    }
    
    override fun getTheme(): UnifyTheme = _currentTheme.value
    
    override fun observeTheme(): StateFlow<UnifyTheme> = _currentTheme.asStateFlow()
    
    override fun toggleDarkMode() {
        val currentTheme = _currentTheme.value
        val newTheme = if (currentTheme.isDark) {
            createLightTheme()
        } else {
            createDarkTheme()
        }
        setTheme(newTheme)
    }
    
    override fun isDarkMode(): Boolean = _currentTheme.value.isDark
    
    override fun getPrimaryColor(): Color = _currentTheme.value.primaryColor
    
    override fun getSecondaryColor(): Color = _currentTheme.value.secondaryColor
    
    override fun getBackgroundColor(): Color = _currentTheme.value.backgroundColor
    
    override fun getSurfaceColor(): Color = _currentTheme.value.surfaceColor
    
    override fun getErrorColor(): Color = _currentTheme.value.errorColor
    
    override fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.5f, 3.0f)
        applyFontScaleToDocument(scale)
    }
    
    override fun getFontScale(): Float = _fontScale.value
    
    override fun observeFontScale(): StateFlow<Float> = _fontScale.asStateFlow()
    
    override fun getScreenWidth(): Int = window.innerWidth
    
    override fun getScreenHeight(): Int = window.innerHeight
    
    override fun getScreenDensity(): Float = window.devicePixelRatio.toFloat()
    
    override fun isTablet(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        val isTabletUserAgent = userAgent.contains("tablet") || 
                               userAgent.contains("ipad") ||
                               (userAgent.contains("android") && !userAgent.contains("mobile"))
        
        // 也可以根据屏幕尺寸判断
        val isLargeScreen = window.innerWidth >= 768 && window.innerHeight >= 1024
        
        return isTabletUserAgent || isLargeScreen
    }
    
    override fun isLandscape(): Boolean = window.innerWidth > window.innerHeight
    
    override fun setAnimationsEnabled(enabled: Boolean) {
        animationsEnabled = enabled
        
        // 设置CSS动画
        val style = document.createElement("style")
        style.textContent = if (enabled) {
            "* { transition: all 0.3s ease !important; }"
        } else {
            "* { transition: none !important; animation: none !important; }"
        }
        document.head?.appendChild(style)
    }
    
    override fun areAnimationsEnabled(): Boolean = animationsEnabled
    
    override fun getAnimationDuration(): Long {
        return if (animationsEnabled) 300L else 0L
    }
    
    override fun setAccessibilityEnabled(enabled: Boolean) {
        accessibilityEnabled = enabled
    }
    
    override fun isAccessibilityEnabled(): Boolean {
        return accessibilityEnabled || checkSystemAccessibilitySettings()
    }
    
    override fun announceForAccessibility(message: String) {
        if (isAccessibilityEnabled()) {
            // 创建一个隐藏的aria-live区域来宣布消息
            val liveRegion = document.createElement("div").apply {
                setAttribute("aria-live", "polite")
                setAttribute("aria-atomic", "true")
                setAttribute("style", "position: absolute; left: -10000px; width: 1px; height: 1px; overflow: hidden;")
                textContent = message
            }
            
            document.body?.appendChild(liveRegion)
            
            // 短暂延迟后移除元素
            window.setTimeout({
                document.body?.removeChild(liveRegion)
            }, 1000)
        }
    }
    
    private fun createDefaultTheme(): UnifyTheme {
        val isDarkMode = isSystemInDarkMode()
        return if (isDarkMode) createDarkTheme() else createLightTheme()
    }
    
    private fun createLightTheme(): UnifyTheme {
        return UnifyTheme(
            name = "Light",
            isDark = false,
            primaryColor = Color(0xFF1976D2),
            secondaryColor = Color(0xFF388E3C),
            backgroundColor = Color(0xFFFAFAFA),
            surfaceColor = Color.White,
            errorColor = Color(0xFFD32F2F),
            onPrimaryColor = Color.White,
            onSecondaryColor = Color.White,
            onBackgroundColor = Color(0xFF212121),
            onSurfaceColor = Color(0xFF212121),
            onErrorColor = Color.White
        )
    }
    
    private fun createDarkTheme(): UnifyTheme {
        return UnifyTheme(
            name = "Dark",
            isDark = true,
            primaryColor = Color(0xFF90CAF9),
            secondaryColor = Color(0xFF81C784),
            backgroundColor = Color(0xFF121212),
            surfaceColor = Color(0xFF1E1E1E),
            errorColor = Color(0xFFEF5350),
            onPrimaryColor = Color.Black,
            onSecondaryColor = Color.Black,
            onBackgroundColor = Color(0xFFE0E0E0),
            onSurfaceColor = Color(0xFFE0E0E0),
            onErrorColor = Color.Black
        )
    }
    
    private fun isSystemInDarkMode(): Boolean {
        return darkModeMediaQuery.matches
    }
    
    private fun checkSystemAccessibilitySettings(): Boolean {
        // 检查浏览器的无障碍设置
        return window.matchMedia("(prefers-reduced-motion: reduce)").matches ||
               window.matchMedia("(prefers-contrast: high)").matches
    }
    
    private fun applyThemeToDocument(theme: UnifyTheme) {
        val root = document.documentElement
        root?.style?.apply {
            setProperty("--primary-color", colorToHex(theme.primaryColor))
            setProperty("--secondary-color", colorToHex(theme.secondaryColor))
            setProperty("--background-color", colorToHex(theme.backgroundColor))
            setProperty("--surface-color", colorToHex(theme.surfaceColor))
            setProperty("--error-color", colorToHex(theme.errorColor))
            setProperty("--on-primary-color", colorToHex(theme.onPrimaryColor))
            setProperty("--on-secondary-color", colorToHex(theme.onSecondaryColor))
            setProperty("--on-background-color", colorToHex(theme.onBackgroundColor))
            setProperty("--on-surface-color", colorToHex(theme.onSurfaceColor))
            setProperty("--on-error-color", colorToHex(theme.onErrorColor))
        }
        
        // 设置body背景色
        document.body?.style?.backgroundColor = colorToHex(theme.backgroundColor)
        document.body?.style?.color = colorToHex(theme.onBackgroundColor)
    }
    
    private fun applyFontScaleToDocument(scale: Float) {
        val root = document.documentElement
        root?.style?.fontSize = "${scale * 16}px" // 16px是默认字体大小
    }
    
    private fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return "#${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
    }
}

actual object UnifyUIManagerFactory {
    actual fun create(): UnifyUIManager {
        return UnifyUIManagerImpl()
    }
}
