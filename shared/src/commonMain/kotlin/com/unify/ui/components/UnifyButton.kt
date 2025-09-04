package com.unify.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台按钮组件
 * 支持8大平台的统一按钮实现
 */
@Composable
expect fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(16.dp, 12.dp)
)

/**
 * Unify图标按钮组件
 */
@Composable
expect fun UnifyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
)

/**
 * Unify浮动操作按钮
 */
@Composable
expect fun UnifyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    content: @Composable () -> Unit
)
