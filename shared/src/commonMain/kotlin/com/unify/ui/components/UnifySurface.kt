package com.unify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台表面组件
 * 支持8大平台的统一表面容器
 */
@Composable
expect fun UnifySurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 0.dp,
    border: UnifyBorder? = null,
    content: @Composable () -> Unit
)

/**
 * Unify卡片组件
 */
@Composable
expect fun UnifyCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    border: UnifyBorder? = null,
    content: @Composable () -> Unit
)

/**
 * Unify容器组件
 */
@Composable
expect fun UnifyContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    content: @Composable () -> Unit
)

/**
 * 边框数据类
 */
data class UnifyBorder(
    val width: Dp,
    val color: Color,
    val style: BorderStyle = BorderStyle.SOLID
)

/**
 * 边框样式枚举
 */
enum class BorderStyle {
    SOLID,
    DASHED,
    DOTTED
}
