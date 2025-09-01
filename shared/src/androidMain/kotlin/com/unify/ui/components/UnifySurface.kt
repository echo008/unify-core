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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android平台的表面实现
 */
actual class UnifyPlatformSurface {
    companion object {
        fun getSystemElevation(level: Int): Dp {
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
        
        fun supportsRippleEffect(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
        }
        
        fun getSystemCornerRadius(): Dp {
            return 8.dp // Material Design 3 default
        }
        
        fun isRTL(context: android.content.Context): Boolean {
            return context.resources.configuration.layoutDirection == 
                android.view.View.LAYOUT_DIRECTION_RTL
        }
        
        fun getSystemSurfaceColor(context: android.content.Context): Int {
            val typedArray = context.theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.colorBackground)
            )
            val color = typedArray.getColor(0, 0xFFFFFFFF.toInt())
            typedArray.recycle()
            return color
        }
        
        fun supportsBlur(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
        }
        
        fun getMaxElevation(): Dp {
            return 24.dp // Material Design maximum elevation
        }
    }
}

/**
 * Android平台的原生表面组件适配器
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
    val context = LocalContext.current
    val maxElevation = UnifyPlatformSurface.getMaxElevation()
    val clampedElevation = if (elevation > maxElevation) maxElevation else elevation
    
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
