package com.unify.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify统一布局组件
 * 100% Kotlin Compose语法实现
 */

@Composable
fun UnifyColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    spacing: UnifySpacing = UnifySpacing.SMALL,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.value),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun UnifyRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    spacing: UnifySpacing = UnifySpacing.SMALL,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.value),
        verticalAlignment = verticalAlignment,
        content = content,
    )
}

@Composable
fun UnifyBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content,
    )
}

@Composable
fun UnifyGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

@Composable
fun UnifyStaggeredGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalItemSpacing = verticalItemSpacing,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

@Composable
fun UnifyContainer(
    modifier: Modifier = Modifier,
    padding: UnifySpacing = UnifySpacing.MEDIUM,
    fillMaxSize: Boolean = false,
    content: @Composable () -> Unit,
) {
    val containerModifier =
        if (fillMaxSize) {
            modifier.fillMaxSize()
        } else {
            modifier
        }

    Box(
        modifier = containerModifier.padding(padding.value),
    ) {
        content()
    }
}

@Composable
fun UnifySection(
    title: String? = null,
    modifier: Modifier = Modifier,
    titleContent: @Composable (() -> Unit)? = null,
    spacing: UnifySpacing = UnifySpacing.MEDIUM,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.value),
    ) {
        if (title != null || titleContent != null) {
            if (titleContent != null) {
                titleContent()
            } else if (title != null) {
                androidx.compose.material3.Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        content()
    }
}

@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline,
    isVertical: Boolean = false,
) {
    if (isVertical) {
        VerticalDivider(
            modifier = modifier,
            thickness = thickness,
            color = color,
        )
    } else {
        HorizontalDivider(
            modifier = modifier,
            thickness = thickness,
            color = color,
        )
    }
}

@Composable
fun UnifySpacer(
    size: UnifySpacing = UnifySpacing.MEDIUM,
    modifier: Modifier = Modifier,
    isVertical: Boolean = true,
) {
    if (isVertical) {
        Spacer(modifier = modifier.height(size.value))
    } else {
        Spacer(modifier = modifier.width(size.value))
    }
}

// 响应式布局辅助函数
@Composable
fun UnifyResponsiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable (() -> Unit)? = null,
    expandedContent: @Composable (() -> Unit)? = null,
) {
    // 这里可以根据屏幕尺寸选择不同的布局
    // 目前简化实现，后续可以扩展
    Box(modifier = modifier) {
        compactContent()
    }
}

// 自适应网格
@Composable
fun UnifyAdaptiveGrid(
    modifier: Modifier = Modifier,
    minItemWidth: Dp = 200.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minItemWidth),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}
