package com.unify.core.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android平台UnifyUIManager实现
 */
class UnifyUIManagerImpl(private val context: Context) : UnifyUIManager {
    private val resources: Resources = context.resources
    private val displayMetrics: DisplayMetrics = resources.displayMetrics
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

    // 主题状态管理
    private val _currentTheme = MutableStateFlow(createDefaultTheme())
    private val _fontScale = MutableStateFlow(1.0f)

    // 动画设置
    private var animationsEnabled = true
    private var accessibilityEnabled = false

    override fun setTheme(theme: UnifyTheme) {
        _currentTheme.value = theme
    }

    override fun getTheme(): UnifyTheme = _currentTheme.value

    override fun observeTheme(): StateFlow<UnifyTheme> = _currentTheme.asStateFlow()

    override fun toggleDarkMode() {
        val currentTheme = _currentTheme.value
        val newTheme =
            if (currentTheme.isDark) {
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
        _fontScale.value = scale.coerceIn(0.5f, 2.0f)
    }

    override fun getFontScale(): Float = _fontScale.value

    override fun observeFontScale(): StateFlow<Float> = _fontScale.asStateFlow()

    override fun getScreenWidth(): Int = displayMetrics.widthPixels

    override fun getScreenHeight(): Int = displayMetrics.heightPixels

    override fun getScreenDensity(): Float = displayMetrics.density

    override fun isTablet(): Boolean {
        val configuration = resources.configuration
        val screenLayout = configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        return screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    override fun isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
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
        return accessibilityEnabled || accessibilityManager.isEnabled
    }

    override fun announceForAccessibility(message: String) {
        if (isAccessibilityEnabled()) {
            // 在Android中，可以通过View.announceForAccessibility()实现
            // 这里提供基础实现，实际使用时需要传入具体的View
        }
    }

    private fun createDefaultTheme(): UnifyTheme {
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return if (isDarkMode) createDarkTheme() else createLightTheme()
    }

    private fun createLightTheme(): UnifyTheme {
        return UnifyTheme(
            name = "Light",
            isDark = false,
            primaryColor = Color(0xFF6200EE),
            secondaryColor = Color(0xFF03DAC6),
            backgroundColor = Color(0xFFFFFBFE),
            surfaceColor = Color(0xFFFFFBFE),
            errorColor = Color(0xFFB00020),
            onPrimaryColor = Color.White,
            onSecondaryColor = Color.Black,
            onBackgroundColor = Color(0xFF1C1B1F),
            onSurfaceColor = Color(0xFF1C1B1F),
            onErrorColor = Color.White,
        )
    }

    private fun createDarkTheme(): UnifyTheme {
        return UnifyTheme(
            name = "Dark",
            isDark = true,
            primaryColor = Color(0xFFBB86FC),
            secondaryColor = Color(0xFF03DAC6),
            backgroundColor = Color(0xFF121212),
            surfaceColor = Color(0xFF121212),
            errorColor = Color(0xFFCF6679),
            onPrimaryColor = Color.Black,
            onSecondaryColor = Color.Black,
            onBackgroundColor = Color(0xFFE1E2E1),
            onSurfaceColor = Color(0xFFE1E2E1),
            onErrorColor = Color.Black,
        )
    }
}

actual object UnifyUIManagerFactory {
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    actual fun create(): UnifyUIManager {
        return UnifyUIManagerImpl(
            context ?: throw IllegalStateException("UnifyUIManagerFactory not initialized. Call initialize(context) first."),
        )
    }
}
