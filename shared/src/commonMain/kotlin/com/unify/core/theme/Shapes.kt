package com.unify.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台形状系统
 * 支持8大平台的统一形状管理
 */

// Unify形状定义
val UnifyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * 扩展形状定义
 */
object UnifyCustomShapes {
    // 基础圆角形状
    val none = RectangleShape
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(50)
    
    // 特殊圆角形状
    val topRounded = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    val bottomRounded = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 12.dp,
        bottomEnd = 12.dp
    )
    
    val leftRounded = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 0.dp,
        bottomStart = 12.dp,
        bottomEnd = 0.dp
    )
    
    val rightRounded = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 12.dp,
        bottomStart = 0.dp,
        bottomEnd = 12.dp
    )
    
    // 对角圆角形状
    val topLeftBottomRight = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 0.dp,
        bottomStart = 0.dp,
        bottomEnd = 12.dp
    )
    
    val topRightBottomLeft = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 0.dp
    )
    
    // 卡片形状
    val cardSmall = RoundedCornerShape(8.dp)
    val cardMedium = RoundedCornerShape(12.dp)
    val cardLarge = RoundedCornerShape(16.dp)
    
    // 按钮形状
    val buttonSmall = RoundedCornerShape(6.dp)
    val buttonMedium = RoundedCornerShape(8.dp)
    val buttonLarge = RoundedCornerShape(12.dp)
    val buttonPill = RoundedCornerShape(50)
    
    // 输入框形状
    val textFieldSmall = RoundedCornerShape(6.dp)
    val textFieldMedium = RoundedCornerShape(8.dp)
    val textFieldLarge = RoundedCornerShape(12.dp)
    
    // 对话框形状
    val dialogSmall = RoundedCornerShape(12.dp)
    val dialogMedium = RoundedCornerShape(16.dp)
    val dialogLarge = RoundedCornerShape(20.dp)
    
    // 底部表单形状
    val bottomSheetTop = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    // 芯片形状
    val chipSmall = RoundedCornerShape(12.dp)
    val chipMedium = RoundedCornerShape(16.dp)
    val chipLarge = RoundedCornerShape(20.dp)
    
    // 徽章形状
    val badgeSmall = RoundedCornerShape(8.dp)
    val badgeMedium = RoundedCornerShape(12.dp)
    val badgeLarge = RoundedCornerShape(16.dp)
    val badgeCircle = RoundedCornerShape(50)
    
    // 进度条形状
    val progressSmall = RoundedCornerShape(2.dp)
    val progressMedium = RoundedCornerShape(4.dp)
    val progressLarge = RoundedCornerShape(6.dp)
    val progressFull = RoundedCornerShape(50)
    
    // 分割线形状
    val dividerThin = RoundedCornerShape(0.5.dp)
    val dividerMedium = RoundedCornerShape(1.dp)
    val dividerThick = RoundedCornerShape(2.dp)
}

/**
 * 响应式形状计算
 */
object UnifyResponsiveShapes {
    /**
     * 根据屏幕尺寸调整圆角大小
     */
    fun getScaledShape(baseRadius: androidx.compose.ui.unit.Dp, scaleFactor: Float): RoundedCornerShape {
        return RoundedCornerShape(baseRadius * scaleFactor)
    }
    
    /**
     * 获取平台特定的形状缩放因子
     */
    fun getPlatformShapeScale(): Float = 1.0f
    
    /**
     * 获取响应式形状
     */
    fun getResponsiveShapes(): Shapes {
        val scaleFactor = getPlatformShapeScale()
        return Shapes(
            extraSmall = getScaledShape(4.dp, scaleFactor),
            small = getScaledShape(8.dp, scaleFactor),
            medium = getScaledShape(12.dp, scaleFactor),
            large = getScaledShape(16.dp, scaleFactor),
            extraLarge = getScaledShape(28.dp, scaleFactor)
        )
    }
}

/**
 * 形状工具函数
 */
object UnifyShapeUtils {
    /**
     * 创建自定义圆角形状
     */
    fun createCustomRoundedShape(
        topStart: androidx.compose.ui.unit.Dp = 0.dp,
        topEnd: androidx.compose.ui.unit.Dp = 0.dp,
        bottomStart: androidx.compose.ui.unit.Dp = 0.dp,
        bottomEnd: androidx.compose.ui.unit.Dp = 0.dp
    ): Shape {
        return RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )
    }
    
    /**
     * 创建统一圆角形状
     */
    fun createUniformRoundedShape(radius: androidx.compose.ui.unit.Dp): Shape {
        return RoundedCornerShape(radius)
    }
    
    /**
     * 创建椭圆形状
     */
    fun createOvalShape(): Shape {
        return RoundedCornerShape(50)
    }
}
