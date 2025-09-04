package com.unify.core.ui

import com.unify.core.ui.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 小程序平台UnifyUIManager实现
 * 基于小程序系统API和组件库
 */
class UnifyUIManagerImpl : UnifyUIManager {
    
    private val _currentTheme = MutableStateFlow(UnifyTheme.LIGHT)
    private val _fontScale = MutableStateFlow(1.0f)
    private val _animationsEnabled = MutableStateFlow(true)
    
    // 小程序系统信息管理器
    private val miniAppSystemManager = MiniAppSystemManager()
    
    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
        applyMiniAppTheme(theme)
    }
    
    override fun getCurrentTheme(): UnifyTheme = _currentTheme.value
    
    override fun observeTheme(): Flow<UnifyTheme> = _currentTheme.asStateFlow()
    
    override fun getColor(colorName: String): Long {
        return when (colorName) {
            "primary" -> when (_currentTheme.value) {
                UnifyTheme.LIGHT -> 0xFF1AAD19 // 微信绿
                UnifyTheme.DARK -> 0xFF2AAD29
                UnifyTheme.AUTO -> if (isSystemDarkMode()) 0xFF2AAD29 else 0xFF1AAD19
            }
            "background" -> when (_currentTheme.value) {
                UnifyTheme.LIGHT -> 0xFFF7F7F7
                UnifyTheme.DARK -> 0xFF191919
                UnifyTheme.AUTO -> if (isSystemDarkMode()) 0xFF191919 else 0xFFF7F7F7
            }
            "surface" -> when (_currentTheme.value) {
                UnifyTheme.LIGHT -> 0xFFFFFFFF
                UnifyTheme.DARK -> 0xFF2C2C2C
                UnifyTheme.AUTO -> if (isSystemDarkMode()) 0xFF2C2C2C else 0xFFFFFFFF
            }
            "onPrimary" -> 0xFFFFFFFF
            "onBackground" -> when (_currentTheme.value) {
                UnifyTheme.LIGHT -> 0xFF000000
                UnifyTheme.DARK -> 0xFFFFFFFF
                UnifyTheme.AUTO -> if (isSystemDarkMode()) 0xFFFFFFFF else 0xFF000000
            }
            "onSurface" -> when (_currentTheme.value) {
                UnifyTheme.LIGHT -> 0xFF000000
                UnifyTheme.DARK -> 0xFFFFFFFF
                UnifyTheme.AUTO -> if (isSystemDarkMode()) 0xFFFFFFFF else 0xFF000000
            }
            else -> 0xFF000000
        }
    }
    
    override fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.8f, 1.5f) // 小程序字体缩放范围限制
        applyMiniAppFontScale(scale)
    }
    
    override fun getFontScale(): Float = _fontScale.value
    
    override fun observeFontScale(): Flow<Float> = _fontScale.asStateFlow()
    
    override fun getFontSize(textStyle: String): Float {
        val baseSize = when (textStyle) {
            "headline1" -> 28f
            "headline2" -> 24f
            "headline3" -> 20f
            "headline4" -> 18f
            "headline5" -> 16f
            "headline6" -> 14f
            "body1" -> 14f
            "body2" -> 12f
            "caption" -> 10f
            "button" -> 14f
            else -> 14f
        }
        return baseSize * _fontScale.value
    }
    
    override fun getScreenSize(): ScreenSize {
        return try {
            val systemInfo = miniAppSystemManager.getSystemInfoSync()
            ScreenSize(
                width = systemInfo.screenWidth,
                height = systemInfo.screenHeight,
                density = systemInfo.pixelRatio
            )
        } catch (e: Exception) {
            ScreenSize(375, 667, 2.0f) // iPhone默认值
        }
    }
    
    override fun getScreenDensity(): Float {
        return try {
            miniAppSystemManager.getSystemInfoSync().pixelRatio
        } catch (e: Exception) {
            2.0f // 默认密度
        }
    }
    
    override fun isTablet(): Boolean {
        val screenSize = getScreenSize()
        val screenInches = kotlin.math.sqrt(
            (screenSize.width / screenSize.density).toDouble().pow(2.0) +
            (screenSize.height / screenSize.density).toDouble().pow(2.0)
        ) / 160.0
        return screenInches >= 7.0
    }
    
    override fun setAnimationsEnabled(enabled: Boolean) {
        _animationsEnabled.value = enabled
        // 小程序中动画通常由CSS控制，这里主要是状态管理
    }
    
    override fun areAnimationsEnabled(): Boolean = _animationsEnabled.value
    
    override fun observeAnimationsEnabled(): Flow<Boolean> = _animationsEnabled.asStateFlow()
    
    override fun getAnimationDuration(animationType: String): Long {
        if (!_animationsEnabled.value) return 0L
        
        return when (animationType) {
            "fast" -> 200L
            "normal" -> 300L
            "slow" -> 500L
            "enter" -> 250L
            "exit" -> 200L
            else -> 300L
        }
    }
    
    override fun enableAccessibility(enabled: Boolean) {
        // 小程序无障碍主要通过aria-label等属性实现
        miniAppSystemManager.setAccessibilityEnabled(enabled)
    }
    
    override fun isAccessibilityEnabled(): Boolean {
        return try {
            miniAppSystemManager.isAccessibilityEnabled()
        } catch (e: Exception) {
            false
        }
    }
    
    override fun setAccessibilityDescription(elementId: String, description: String) {
        try {
            miniAppSystemManager.setAccessibilityDescription(elementId, description)
        } catch (e: Exception) {
            println("Failed to set accessibility description: ${e.message}")
        }
    }
    
    private fun applyMiniAppTheme(theme: UnifyTheme) {
        try {
            // 小程序主题切换通常通过CSS变量实现
            when (theme) {
                UnifyTheme.LIGHT -> {
                    miniAppSystemManager.setThemeVars(lightThemeVars)
                }
                UnifyTheme.DARK -> {
                    miniAppSystemManager.setThemeVars(darkThemeVars)
                }
                UnifyTheme.AUTO -> {
                    val vars = if (isSystemDarkMode()) darkThemeVars else lightThemeVars
                    miniAppSystemManager.setThemeVars(vars)
                }
            }
        } catch (e: Exception) {
            println("Failed to apply mini-app theme: ${e.message}")
        }
    }
    
    private fun applyMiniAppFontScale(scale: Float) {
        try {
            // 小程序字体缩放通过CSS变量实现
            miniAppSystemManager.setFontScale(scale)
        } catch (e: Exception) {
            println("Failed to apply mini-app font scale: ${e.message}")
        }
    }
    
    private fun isSystemDarkMode(): Boolean {
        return try {
            miniAppSystemManager.getSystemInfoSync().theme == "dark"
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        private val lightThemeVars = mapOf(
            "--primary-color" to "#1AAD19",
            "--background-color" to "#F7F7F7",
            "--surface-color" to "#FFFFFF",
            "--text-color" to "#000000",
            "--border-color" to "#E5E5E5"
        )
        
        private val darkThemeVars = mapOf(
            "--primary-color" to "#2AAD29",
            "--background-color" to "#191919",
            "--surface-color" to "#2C2C2C",
            "--text-color" to "#FFFFFF",
            "--border-color" to "#3C3C3C"
        )
    }
}

// 小程序系统信息管理器模拟实现
private class MiniAppSystemManager {
    private var accessibilityEnabled = false
    private val themeVars = mutableMapOf<String, String>()
    private var fontScale = 1.0f
    
    fun getSystemInfoSync(): SystemInfo {
        // 实际实现中会调用小程序API: wx.getSystemInfoSync()
        return SystemInfo(
            brand = "iPhone",
            model = "iPhone 12",
            pixelRatio = 3.0f,
            screenWidth = 390,
            screenHeight = 844,
            windowWidth = 390,
            windowHeight = 844,
            statusBarHeight = 44,
            language = "zh_CN",
            version = "8.0.5",
            system = "iOS 15.0",
            platform = "ios",
            fontSizeSetting = 16,
            SDKVersion = "2.19.4",
            benchmarkLevel = 1,
            albumAuthorized = true,
            cameraAuthorized = true,
            locationAuthorized = true,
            microphoneAuthorized = true,
            notificationAuthorized = true,
            bluetoothEnabled = true,
            locationEnabled = true,
            wifiEnabled = true,
            safeArea = SafeArea(0, 44, 390, 756),
            theme = "light"
        )
    }
    
    fun setThemeVars(vars: Map<String, String>) {
        themeVars.clear()
        themeVars.putAll(vars)
        // 实际实现中会设置CSS变量或调用小程序主题API
    }
    
    fun setFontScale(scale: Float) {
        this.fontScale = scale
        // 实际实现中会设置CSS字体缩放变量
    }
    
    fun setAccessibilityEnabled(enabled: Boolean) {
        this.accessibilityEnabled = enabled
    }
    
    fun isAccessibilityEnabled(): Boolean = accessibilityEnabled
    
    fun setAccessibilityDescription(elementId: String, description: String) {
        // 实际实现中会设置元素的aria-label属性
    }
    
    data class SystemInfo(
        val brand: String,
        val model: String,
        val pixelRatio: Float,
        val screenWidth: Int,
        val screenHeight: Int,
        val windowWidth: Int,
        val windowHeight: Int,
        val statusBarHeight: Int,
        val language: String,
        val version: String,
        val system: String,
        val platform: String,
        val fontSizeSetting: Int,
        val SDKVersion: String,
        val benchmarkLevel: Int,
        val albumAuthorized: Boolean,
        val cameraAuthorized: Boolean,
        val locationAuthorized: Boolean,
        val microphoneAuthorized: Boolean,
        val notificationAuthorized: Boolean,
        val bluetoothEnabled: Boolean,
        val locationEnabled: Boolean,
        val wifiEnabled: Boolean,
        val safeArea: SafeArea,
        val theme: String
    )
    
    data class SafeArea(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}

actual object UnifyUIManagerFactory {
    actual fun create(): UnifyUIManager {
        return UnifyUIManagerImpl()
    }
}
