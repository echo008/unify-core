package com.unify.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify视图容器组件
 * 提供统一的容器布局和样式管理
 */
@Composable
fun UnifyViewContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 8.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    elevation: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(cornerRadius),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                    .padding(padding),
            content = content,
        )
    }
}

/**
 * 垂直布局容器
 */
@Composable
fun UnifyVerticalContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/**
 * 水平布局容器
 */
@Composable
fun UnifyHorizontalContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = verticalAlignment,
        content = content,
    )
}

/**
 * 网格容器
 */
@Composable
fun UnifyGridContainer(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: Dp = 8.dp,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/**
 * 分割线容器
 */
@Composable
fun UnifyDividerContainer(
    modifier: Modifier = Modifier,
    dividerColor: Color = MaterialTheme.colorScheme.outline,
    dividerThickness: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        content()
        HorizontalDivider(
            thickness = dividerThickness,
            color = dividerColor,
        )
    }
}

/**
 * 可滚动容器
 */
@Composable
fun UnifyScrollableContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(content = content)
        }
    }
}

/**
 * 响应式容器
 */
@Composable
fun UnifyResponsiveContainer(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        when {
            maxWidth < 600.dp -> compactContent()
            maxWidth < 840.dp -> mediumContent()
            else -> expandedContent()
        }
    }
}

/**
 * 带标题的容器
 */
@Composable
fun UnifyTitledContainer(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    UnifyViewContainer(modifier = modifier) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = titleColor,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            content()
        }
    }
}

/**
 * 可折叠容器
 */
@Composable
fun UnifyCollapsibleContainer(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    UnifyViewContainer(modifier = modifier) {
        Column {
            UnifyHorizontalContainer(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (isExpanded) "−" else "+",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

/**
 * 带阴影的容器
 */
@Composable
fun UnifyShadowContainer(
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

// 扩展函数
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(Modifier.padding(4.dp))
}
