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
 * HarmonyOS平台的表面实现
 */
actual class UnifyPlatformSurface {
    companion object {
        fun getHarmonyElevation(level: Int): Dp {
            // HarmonyOS设计语言的阴影规范
            return when (level) {
                0 -> 0.dp
                1 -> 1.dp
                2 -> 2.dp
                3 -> 4.dp
                4 -> 6.dp
                5 -> 8.dp
                else -> 12.dp
            }
        }
        
        fun getHarmonyCornerRadius(): Dp {
            return 12.dp // HarmonyOS设计语言圆角
        }
        
        fun supportsDistributedUI(): Boolean {
            // 检查是否支持分布式UI
            return true // HarmonyOS特有功能
        }
        
        fun getDeviceFormFactor(): String {
            // 获取设备形态
            return "phone" // 实际实现需要调用HarmonyOS API
        }
        
        fun adaptElevationForDevice(): Dp {
            return when (getDeviceFormFactor()) {
                "watch" -> 1.dp      // 手表使用较小阴影
                "tv" -> 8.dp         // 电视使用较大阴影
                "car" -> 4.dp        // 车机使用中等阴影
                "tablet" -> 6.dp     // 平板使用中大阴影
                else -> 4.dp         // 手机默认阴影
            }
        }
        
        fun supportsAtomicService(): Boolean {
            // 检查是否支持原子化服务
            return true // HarmonyOS特有功能
        }
        
        fun getHarmonyMaterialColor(): Color {
            // 获取HarmonyOS材质颜色
            return Color(0xFFF1F3F5) // HarmonyOS Light surface
        }
        
        fun isHarmonyDarkMode(): Boolean {
            // 检查HarmonyOS深色模式
            return false // 实际实现需要调用HarmonyOS API
        }
        
        fun getMaxElevation(): Dp {
            return 16.dp // HarmonyOS最大阴影
        }
    }
}

/**
 * HarmonyOS平台的原生表面组件适配器
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
    val harmonyElevation = when {
        elevation <= 1.dp -> UnifyPlatformSurface.getHarmonyElevation(1)
        elevation <= 2.dp -> UnifyPlatformSurface.getHarmonyElevation(2)
        elevation <= 4.dp -> UnifyPlatformSurface.getHarmonyElevation(3)
        elevation <= 6.dp -> UnifyPlatformSurface.getHarmonyElevation(4)
        elevation <= 8.dp -> UnifyPlatformSurface.getHarmonyElevation(5)
        else -> UnifyPlatformSurface.getHarmonyElevation(6)
    }
    
    val adaptedElevation = UnifyPlatformSurface.adaptElevationForDevice()
    val finalElevation = minOf(harmonyElevation, adaptedElevation)
    val maxElevation = UnifyPlatformSurface.getMaxElevation()
    val clampedElevation = if (finalElevation > maxElevation) maxElevation else finalElevation
    
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
