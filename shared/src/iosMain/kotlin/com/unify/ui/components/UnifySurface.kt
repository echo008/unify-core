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
 * iOS平台的表面实现
 */
actual class UnifyPlatformSurface {
    companion object {
        fun getIOSElevation(level: Int): Dp {
            // iOS使用更细微的阴影
            return when (level) {
                0 -> 0.dp
                1 -> 0.5.dp
                2 -> 1.dp
                3 -> 2.dp
                4 -> 4.dp
                5 -> 6.dp
                else -> 8.dp
            }
        }
        
        fun getIOSCornerRadius(): Dp {
            return 10.dp // iOS系统默认圆角
        }
        
        fun supportsVibrancy(): Boolean {
            // 检查是否支持毛玻璃效果
            return true // iOS 7+支持
        }
        
        fun getSystemBlurStyle(): String {
            return "systemMaterial" // iOS系统材质
        }
        
        fun isReduceTransparencyEnabled(): Boolean {
            // 检查是否启用减少透明度
            return false // 实际实现需要调用iOS API
        }
        
        fun getSystemSurfaceColor(): Color {
            // 获取iOS系统表面颜色
            return Color(0xFFF2F2F7) // iOS Light Mode surface
        }
        
        fun getSystemBackgroundColor(): Color {
            // 获取iOS系统背景颜色
            return Color(0xFFFFFFFF) // iOS Light Mode background
        }
        
        fun isDarkModeEnabled(): Boolean {
            // 检查是否启用深色模式
            return false // 实际实现需要调用iOS API
        }
        
        fun getMaxBlurRadius(): Dp {
            return 20.dp // iOS最大模糊半径
        }
    }
}

/**
 * iOS平台的原生表面组件适配器
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
    val iosElevation = when {
        elevation <= 1.dp -> UnifyPlatformSurface.getIOSElevation(1)
        elevation <= 2.dp -> UnifyPlatformSurface.getIOSElevation(2)
        elevation <= 4.dp -> UnifyPlatformSurface.getIOSElevation(3)
        elevation <= 6.dp -> UnifyPlatformSurface.getIOSElevation(4)
        elevation <= 8.dp -> UnifyPlatformSurface.getIOSElevation(5)
        else -> UnifyPlatformSurface.getIOSElevation(6)
    }
    
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = iosElevation,
        shadowElevation = iosElevation,
        border = border,
        content = content
    )
}
