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
 * 小程序平台的表面实现
 */
actual class UnifyPlatformSurface {
    companion object {
        fun getMiniAppElevation(level: Int): Dp {
            // 小程序使用较小的阴影值
            return when (level) {
                0 -> 0.dp
                1 -> 0.5.dp
                2 -> 1.dp
                3 -> 2.dp
                4 -> 3.dp
                5 -> 4.dp
                else -> 6.dp
            }
        }
        
        fun getMiniAppPlatform(): String {
            // 获取小程序平台类型
            return "wechat" // 实际实现需要检测具体平台
        }
        
        fun getPlatformCornerRadius(): Dp {
            return when (getMiniAppPlatform()) {
                "wechat" -> 8.dp     // 微信小程序
                "alipay" -> 6.dp     // 支付宝小程序
                "baidu" -> 4.dp      // 百度小程序
                "toutiao" -> 8.dp    // 字节跳动小程序
                else -> 6.dp
            }
        }
        
        fun supportsShadow(): Boolean {
            return when (getMiniAppPlatform()) {
                "wechat" -> true
                "alipay" -> true
                "baidu" -> false     // 百度小程序阴影支持有限
                "toutiao" -> true
                else -> false
            }
        }
        
        fun getMaxElevation(): Dp {
            return when (getMiniAppPlatform()) {
                "wechat" -> 8.dp
                "alipay" -> 6.dp
                "baidu" -> 4.dp
                "toutiao" -> 8.dp
                else -> 4.dp
            }
        }
        
        fun supportsBlur(): Boolean {
            return getMiniAppPlatform() in listOf("wechat", "alipay")
        }
        
        fun getPerformanceLevel(): String {
            // 获取设备性能等级
            return "medium" // 实际实现需要调用小程序API
        }
        
        fun shouldOptimizeForPerformance(): Boolean {
            return getPerformanceLevel() == "low"
        }
    }
}

/**
 * 小程序平台的原生表面组件适配器
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
    val miniAppElevation = when {
        elevation <= 0.5.dp -> UnifyPlatformSurface.getMiniAppElevation(1)
        elevation <= 1.dp -> UnifyPlatformSurface.getMiniAppElevation(2)
        elevation <= 2.dp -> UnifyPlatformSurface.getMiniAppElevation(3)
        elevation <= 3.dp -> UnifyPlatformSurface.getMiniAppElevation(4)
        elevation <= 4.dp -> UnifyPlatformSurface.getMiniAppElevation(5)
        else -> UnifyPlatformSurface.getMiniAppElevation(6)
    }
    
    val maxElevation = UnifyPlatformSurface.getMaxElevation()
    val clampedElevation = if (miniAppElevation > maxElevation) maxElevation else miniAppElevation
    
    // 性能优化：低性能设备禁用阴影
    val finalElevation = if (UnifyPlatformSurface.shouldOptimizeForPerformance()) {
        0.dp
    } else if (UnifyPlatformSurface.supportsShadow()) {
        clampedElevation
    } else {
        0.dp
    }
    
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = finalElevation,
        shadowElevation = finalElevation,
        border = border,
        content = content
    )
}
