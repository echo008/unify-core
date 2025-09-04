package com.unify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.StateFlow

/**
 * Unify跨平台UI管理器接口
 * 统一管理主题、样式和UI状态
 */
interface UnifyUIManager {
    // 主题管理
    fun setTheme(theme: UnifyTheme)
    fun getTheme(): UnifyTheme
    fun observeTheme(): StateFlow<UnifyTheme>
    fun toggleDarkMode()
    fun isDarkMode(): Boolean
    
    // 颜色管理
    fun getPrimaryColor(): Color
    fun getSecondaryColor(): Color
    fun getBackgroundColor(): Color
    fun getSurfaceColor(): Color
    fun getErrorColor(): Color
    
    // 字体管理
    fun setFontScale(scale: Float)
    fun getFontScale(): Float
    fun observeFontScale(): StateFlow<Float>
    
    // 布局管理
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
    fun getScreenDensity(): Float
    fun isTablet(): Boolean
    fun isLandscape(): Boolean
    
    // 动画管理
    fun setAnimationsEnabled(enabled: Boolean)
    fun areAnimationsEnabled(): Boolean
    fun getAnimationDuration(): Long
    
    // 无障碍支持
    fun setAccessibilityEnabled(enabled: Boolean)
    fun isAccessibilityEnabled(): Boolean
    fun announceForAccessibility(message: String)
}

/**
 * Unify主题数据类
 */
data class UnifyTheme(
    val name: String,
    val isDark: Boolean,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val errorColor: Color,
    val onPrimaryColor: Color,
    val onSecondaryColor: Color,
    val onBackgroundColor: Color,
    val onSurfaceColor: Color,
    val onErrorColor: Color
)

/**
 * UI管理器工厂
 */
expect object UnifyUIManagerFactory {
    fun create(): UnifyUIManager
}
