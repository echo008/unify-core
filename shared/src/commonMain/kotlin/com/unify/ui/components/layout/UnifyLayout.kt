@file:OptIn(ExperimentalLayoutApi::class)

package com.unify.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify布局组件 - 跨平台统一布局系统
 * 支持8大平台的响应式布局和自适应设计
 */

/**
 * 统一容器布局
 */
@Composable
fun UnifyContainer(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(padding),
        content = content,
    )
}

// Layout components moved to core/components/UnifyLayout.kt to avoid duplicate declarations

/**
 * 统一网格布局
 */
@Composable
fun UnifyGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: @Composable () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
    ) {
        item {
            content()
        }
    }
}

/**
 * 统一自适应网格布局
 */
@Composable
fun UnifyAdaptiveGrid(
    minItemWidth: Dp = 200.dp,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: @Composable () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minItemWidth),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
    ) {
        item {
            content()
        }
    }
}

/**
 * 统一瀑布流布局
 */
@Composable
fun UnifyStaggeredGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalItemSpacing: Dp = 8.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: @Composable () -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalItemSpacing = verticalItemSpacing,
        horizontalArrangement = horizontalArrangement,
    ) {
        item {
            content()
        }
    }
}

/**
 * 统一水平滚动布局
 */
@Composable
fun UnifyHorizontalScroll(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        item {
            content()
        }
    }
}

/**
 * 统一垂直滚动布局
 */
@Composable
fun UnifyVerticalScroll(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        item {
            content()
        }
    }
}

/**
 * 统一间距组件
 */
@Composable
fun UnifySpacer(
    width: Dp = 0.dp,
    height: Dp = 0.dp,
) {
    Spacer(
        modifier = Modifier.size(width = width, height = height),
    )
}

/**
 * 统一分割线
 */
@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f),
) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

/**
 * 统一垂直分割线
 */
@Composable
fun UnifyVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.3f),
) {
    androidx.compose.material3.VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

/**
 * 统一流式布局
 */
@Composable
fun UnifyFlowLayout(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    // 使用FlowRow实现流式布局
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        content()
    }
}

/**
 * 统一约束布局辅助函数
 */
object UnifyConstraints {
    fun createHorizontalChain(vararg ids: Any) = ids.toList()

    fun createVerticalChain(vararg ids: Any) = ids.toList()

    fun createBarrier(
        vararg ids: Any,
        direction: BarrierDirection,
    ) = BarrierInfo(ids.toList(), direction)
}

enum class BarrierDirection {
    Start,
    End,
    Top,
    Bottom,
}

data class BarrierInfo(
    val referencedIds: List<Any>,
    val direction: BarrierDirection,
)

/**
 * 统一响应式布局断点
 */
object UnifyBreakpoints {
    const val COMPACT_WIDTH = 600
    const val MEDIUM_WIDTH = 840
    const val EXPANDED_WIDTH = 1200

    const val COMPACT_HEIGHT = 480
    const val MEDIUM_HEIGHT = 900
    const val EXPANDED_HEIGHT = 1200
}

/**
 * 统一布局配置
 */
data class UnifyLayoutConfig(
    val padding: PaddingValues = PaddingValues(16.dp),
    val spacing: Dp = 8.dp,
    val cornerRadius: Dp = 8.dp,
    val elevation: Dp = 4.dp,
    val isResponsive: Boolean = true,
    val maxWidth: Dp = Dp.Unspecified,
    val minHeight: Dp = 48.dp,
)

/**
 * 默认布局配置
 */
val DefaultLayoutConfig = UnifyLayoutConfig()
