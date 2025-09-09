package com.unify.core.ui

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.awt.Toolkit

/**
 * Desktop平台UI管理器actual实现
 */

// Desktop UI管理器实现
class DesktopUnifyUIManager : UnifyUIManager {
    private val _theme =
        MutableStateFlow(
            UnifyTheme(
                name = "default",
                isDark = false,
                primaryColor = Color.Blue,
                secondaryColor = Color.Green,
                backgroundColor = Color.White,
                surfaceColor = Color.White,
                errorColor = Color.Red,
                onPrimaryColor = Color.White,
                onSecondaryColor = Color.White,
                onBackgroundColor = Color.Black,
                onSurfaceColor = Color.Black,
                onErrorColor = Color.White,
            ),
        )

    private val _fontScale = MutableStateFlow(1.0f)
    private var animationsEnabled = true
    private var accessibilityEnabled = false

    override fun setTheme(theme: UnifyTheme) {
        _theme.value = theme
    }

    override fun getTheme(): UnifyTheme {
        return _theme.value
    }

    override fun observeTheme(): StateFlow<UnifyTheme> {
        return _theme
    }

    override fun toggleDarkMode() {
        val current = _theme.value
        _theme.value = current.copy(isDark = !current.isDark)
    }

    override fun isDarkMode(): Boolean {
        return _theme.value.isDark
    }

    override fun getPrimaryColor(): Color = _theme.value.primaryColor

    override fun getSecondaryColor(): Color = _theme.value.secondaryColor

    override fun getBackgroundColor(): Color = _theme.value.backgroundColor

    override fun getSurfaceColor(): Color = _theme.value.surfaceColor

    override fun getErrorColor(): Color = _theme.value.errorColor

    override fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    override fun getFontScale(): Float {
        return _fontScale.value
    }

    override fun observeFontScale(): StateFlow<Float> {
        return _fontScale
    }

    override fun getScreenWidth(): Int {
        return Toolkit.getDefaultToolkit().screenSize.width
    }

    override fun getScreenHeight(): Int {
        return Toolkit.getDefaultToolkit().screenSize.height
    }

    override fun getScreenDensity(): Float {
        return 1.0f
    }

    override fun isTablet(): Boolean {
        return false
    }

    override fun isLandscape(): Boolean {
        return getScreenWidth() > getScreenHeight()
    }

    override fun setAnimationsEnabled(enabled: Boolean) {
        animationsEnabled = enabled
    }

    override fun areAnimationsEnabled(): Boolean {
        return animationsEnabled
    }

    override fun getAnimationDuration(): Long {
        return 300L
    }

    override fun setAccessibilityEnabled(enabled: Boolean) {
        accessibilityEnabled = enabled
    }

    override fun isAccessibilityEnabled(): Boolean {
        return accessibilityEnabled
    }

    override fun announceForAccessibility(message: String) {
        println("Accessibility: $message")
    }
}

actual object UnifyUIManagerFactory {
    actual fun create(): UnifyUIManager {
        return DesktopUnifyUIManager()
    }
}
