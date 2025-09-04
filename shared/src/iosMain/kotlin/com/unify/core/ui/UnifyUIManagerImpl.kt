package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.UIKit.*
import platform.Foundation.*

/**
 * iOS平台UnifyUIManager实现
 */
class UnifyUIManagerImpl : UnifyUIManager {
    private val screen = UIScreen.mainScreen
    private val device = UIDevice.currentDevice
    
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
    
    override fun getScreenWidth(): Int = (screen.bounds.useContents { size.width } * screen.scale).toInt()
    
    override fun getScreenHeight(): Int = (screen.bounds.useContents { size.height } * screen.scale).toInt()
    
    override fun getScreenDensity(): Float = screen.scale.toFloat()
    
    override fun isTablet(): Boolean {
        return device.userInterfaceIdiom == UIUserInterfaceIdiomPad
    }
    
    override fun isLandscape(): Boolean {
        val orientation = device.orientation
        return orientation == UIDeviceOrientationLandscapeLeft || 
               orientation == UIDeviceOrientationLandscapeRight
    }
    
    override fun setAnimationsEnabled(enabled: Boolean) {
        animationsEnabled = enabled
    }
    
    override fun areAnimationsEnabled(): Boolean = animationsEnabled
    
    override fun getAnimationDuration(): Long {
        return if (animationsEnabled) 300L else 0L
    }
    
    override fun setAccessibilityEnabled(enabled: Boolean) {
        accessibilityEnabled = enabled
    }
    
    override fun isAccessibilityEnabled(): Boolean {
        return accessibilityEnabled || UIAccessibility.isVoiceOverRunning()
    }
    
    override fun announceForAccessibility(message: String) {
        if (isAccessibilityEnabled()) {
            UIAccessibility.post(UIAccessibilityAnnouncementNotification, message)
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
            primaryColor = Color(0xFF007AFF),
            secondaryColor = Color(0xFF34C759),
            backgroundColor = Color(0xFFF2F2F7),
            surfaceColor = Color.White,
            errorColor = Color(0xFFFF3B30),
            onPrimaryColor = Color.White,
            onSecondaryColor = Color.White,
            onBackgroundColor = Color.Black,
            onSurfaceColor = Color.Black,
            onErrorColor = Color.White
        )
    }
    
    private fun createDarkTheme(): UnifyTheme {
        return UnifyTheme(
            name = "Dark",
            isDark = true,
            primaryColor = Color(0xFF0A84FF),
            secondaryColor = Color(0xFF30D158),
            backgroundColor = Color(0xFF000000),
            surfaceColor = Color(0xFF1C1C1E),
            errorColor = Color(0xFFFF453A),
            onPrimaryColor = Color.White,
            onSecondaryColor = Color.Black,
            onBackgroundColor = Color.White,
            onSurfaceColor = Color.White,
            onErrorColor = Color.White
        )
    }
    
    private fun isSystemInDarkMode(): Boolean {
        return if (UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            true
        } else {
            false
        }
    }
}

actual object UnifyUIManagerFactory {
    actual fun create(): UnifyUIManager {
        return UnifyUIManagerImpl()
    }
}
