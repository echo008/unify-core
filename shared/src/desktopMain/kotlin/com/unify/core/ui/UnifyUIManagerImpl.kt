package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Dimension
import java.awt.Toolkit

/**
 * Desktop平台UnifyUIManager实现
 */
class UnifyUIManagerImpl : UnifyUIManager {
    private val toolkit = Toolkit.getDefaultToolkit()
    private val screenSize = toolkit.screenSize
    
    // 主题状态管理
    private val _currentTheme = MutableStateFlow(createDefaultTheme())
    private val _fontScale = MutableStateFlow(1.0f)
    
    // 动画和无障碍设置
    private var animationsEnabled = true
    private var accessibilityEnabled = false
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
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
    }
    
    override fun getFontScale(): Float = _fontScale.value
    
    override fun observeFontScale(): StateFlow<Float> = _fontScale.asStateFlow()
    
    override fun getScreenWidth(): Int = screenSize.width
    
    override fun getScreenHeight(): Int = screenSize.height
    
    override fun getScreenDensity(): Float {
        // Desktop通常使用96 DPI作为基准
        val dpi = toolkit.screenResolution
        return dpi / 96f
    }
    
    override fun isTablet(): Boolean {
        // Desktop环境下，根据屏幕尺寸判断是否为大屏设备
        val diagonal = kotlin.math.sqrt(
            (screenSize.width * screenSize.width + screenSize.height * screenSize.height).toDouble()
        )
        return diagonal > 1200 // 简化判断逻辑
    }
    
    override fun isLandscape(): Boolean = screenSize.width > screenSize.height
    
    override fun setAnimationsEnabled(enabled: Boolean) {
        animationsEnabled = enabled
    }
    
    override fun areAnimationsEnabled(): Boolean = animationsEnabled
    
    override fun getAnimationDuration(): Long {
        return if (animationsEnabled) 250L else 0L
    }
    
    override fun setAccessibilityEnabled(enabled: Boolean) {
        accessibilityEnabled = enabled
    }
    
    override fun isAccessibilityEnabled(): Boolean = accessibilityEnabled
    
    override fun announceForAccessibility(message: String) {
        if (isAccessibilityEnabled()) {
            // 在Desktop环境中，可以通过系统通知或日志输出
            println("Accessibility: $message")
        }
    }
    
    private fun createDefaultTheme(): UnifyTheme {
        // 检查系统是否为暗色模式
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
        return try {
            // 在不同操作系统上检查暗色模式
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> isWindowsDarkMode()
                osName.contains("mac") -> isMacDarkMode()
                osName.contains("linux") -> isLinuxDarkMode()
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isWindowsDarkMode(): Boolean {
        return try {
            // Windows注册表检查
            val process = ProcessBuilder(
                "reg", "query", 
                "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme"
            ).start()
            
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            
            output.contains("0x0")
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isMacDarkMode(): Boolean {
        return try {
            // macOS系统偏好检查
            val process = ProcessBuilder(
                "defaults", "read", "-g", "AppleInterfaceStyle"
            ).start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            
            output.equals("Dark", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isLinuxDarkMode(): Boolean {
        return try {
            // GNOME桌面环境检查
            val process = ProcessBuilder(
                "gsettings", "get", "org.gnome.desktop.interface", "gtk-theme"
            ).start()
            
            val output = process.inputStream.bufferedReader().readText().lowercase()
            process.waitFor()
            
            output.contains("dark")
        } catch (e: Exception) {
            // 如果GNOME不可用，尝试检查KDE
            try {
                val kdeProcess = ProcessBuilder(
                    "kreadconfig5", "--file", "kdeglobals", "--group", "Colors:Window", "--key", "BackgroundNormal"
                ).start()
                
                val kdeOutput = kdeProcess.inputStream.bufferedReader().readText()
                kdeProcess.waitFor()
                
                // 简化判断：如果背景色较暗则认为是暗色模式
                kdeOutput.isNotEmpty()
            } catch (e2: Exception) {
                false
            }
        }
    }
}

actual object UnifyUIManagerFactory {
    actual fun create(): UnifyUIManager {
        return UnifyUIManagerImpl()
    }
}
