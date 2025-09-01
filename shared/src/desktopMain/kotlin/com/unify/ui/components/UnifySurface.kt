package com.unify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop平台的表面实现
 */
actual class UnifyPlatformSurface {
    companion object {
        fun getDesktopElevation(level: Int): Dp {
            return when (level) {
                0 -> 0.dp
                1 -> 2.dp
                2 -> 4.dp
                3 -> 6.dp
                4 -> 8.dp
                5 -> 12.dp
                else -> 16.dp
            }
        }
        
        fun getSystemCornerRadius(): Dp {
            val osName = System.getProperty("os.name").lowercase()
            return when {
                osName.contains("windows") -> 4.dp  // Windows 11 style
                osName.contains("mac") -> 8.dp      // macOS style
                osName.contains("linux") -> 6.dp    // Linux style
                else -> 8.dp
            }
        }
        
        fun supportsAcrylic(): Boolean {
            // Windows 10/11 Acrylic effect
            return System.getProperty("os.name").lowercase().contains("windows")
        }
        
        fun supportsMica(): Boolean {
            // Windows 11 Mica effect
            return System.getProperty("os.name").lowercase().contains("windows")
        }
        
        fun supportsVibrancy(): Boolean {
            // macOS Vibrancy effect
            return System.getProperty("os.name").lowercase().contains("mac")
        }
        
        fun getSystemTheme(): String {
            // 获取系统主题
            return "light" // 实际实现需要调用系统API
        }
        
        fun isHighDpiDisplay(): Boolean {
            return System.getProperty("sun.java2d.uiScale")?.toFloatOrNull()?.let { it > 1.0f } ?: false
        }
        
        fun getDisplayScale(): Float {
            return System.getProperty("sun.java2d.uiScale")?.toFloatOrNull() ?: 1.0f
        }
        
        fun getMaxElevation(): Dp {
            return 24.dp
        }
    }
}

/**
 * Desktop平台的原生表面组件适配器
 */
@Composable
actual fun UnifyNativeSurface(
    modifier: Modifier,
    shape: Shape,
    color: Color,
    contentColor: Color,
    elevation: Dp,
    border: BorderStroke?,
    content: @Composable () -> Unit
) {
    val desktopElevation = when {
        elevation <= 2.dp -> UnifyPlatformSurface.getDesktopElevation(1)
        elevation <= 4.dp -> UnifyPlatformSurface.getDesktopElevation(2)
        elevation <= 6.dp -> UnifyPlatformSurface.getDesktopElevation(3)
        elevation <= 8.dp -> UnifyPlatformSurface.getDesktopElevation(4)
        elevation <= 12.dp -> UnifyPlatformSurface.getDesktopElevation(5)
        else -> UnifyPlatformSurface.getDesktopElevation(6)
    }
    
    val maxElevation = UnifyPlatformSurface.getMaxElevation()
    val clampedElevation = if (desktopElevation > maxElevation) maxElevation else desktopElevation
    
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = clampedElevation,
        shadowElevation = clampedElevation,
        border = border,
        content = content
    )
}
