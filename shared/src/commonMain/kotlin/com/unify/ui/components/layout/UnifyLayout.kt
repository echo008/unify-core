package com.unify.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.core.platform.PlatformManager

/**
 * Unify Layout 组件
 * 支持多平台适配的统一布局组件，参考 KuiklyUI 设计规范
 */

/**
 * 布局间距枚举
 */
enum class UnifySpacing {
    NONE,           // 0dp
    EXTRA_SMALL,    // 4dp
    SMALL,          // 8dp
    MEDIUM,         // 16dp
    LARGE,          // 24dp
    EXTRA_LARGE,    // 32dp
    CUSTOM          // 自定义间距
}

/**
 * 布局对齐方式
 */
enum class UnifyAlignment {
    START,
    CENTER,
    END,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY
}

/**
 * 响应式断点
 */
enum class UnifyBreakpoint {
    COMPACT,        // < 600dp
    MEDIUM,         // 600dp - 840dp
    EXPANDED        // > 840dp
}

/**
 * 增强的 Row 组件
 */
@Composable
fun UnifyRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    spacing: UnifySpacing = UnifySpacing.NONE,
    customSpacing: Dp? = null,
    scrollable: Boolean = false,
    reverseLayout: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualSpacing = customSpacing ?: getSpacingValue(spacing, theme)
    
    val arrangement = if (actualSpacing > 0.dp) {
        Arrangement.spacedBy(actualSpacing)
    } else {
        horizontalArrangement
    }
    
    val rowModifier = if (scrollable) {
        modifier.horizontalScroll(rememberScrollState(), reverseScrolling = reverseLayout)
    } else {
        modifier
    }
    
    Row(
        modifier = rowModifier,
        horizontalArrangement = arrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * 增强的 Column 组件
 */
@Composable
fun UnifyColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    spacing: UnifySpacing = UnifySpacing.NONE,
    customSpacing: Dp? = null,
    scrollable: Boolean = false,
    reverseLayout: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualSpacing = customSpacing ?: getSpacingValue(spacing, theme)
    
    val arrangement = if (actualSpacing > 0.dp) {
        Arrangement.spacedBy(actualSpacing)
    } else {
        verticalArrangement
    }
    
    val columnModifier = if (scrollable) {
        modifier.verticalScroll(rememberScrollState(), reverseScrolling = reverseLayout)
    } else {
        modifier
    }
    
    Column(
        modifier = columnModifier,
        verticalArrangement = arrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/**
 * 增强的 Box 组件
 */
@Composable
fun UnifyBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content
    )
}

/**
 * 堆叠布局组件
 */
@Composable
fun UnifyStack(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    spacing: UnifySpacing = UnifySpacing.NONE,
    customSpacing: Dp? = null,
    direction: UnifyStackDirection = UnifyStackDirection.VERTICAL,
    content: @Composable UnifyStackScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualSpacing = customSpacing ?: getSpacingValue(spacing, theme)
    
    val scope = UnifyStackScopeImpl(actualSpacing, direction)
    
    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        scope.content()
    }
}

/**
 * 堆叠方向
 */
enum class UnifyStackDirection {
    VERTICAL,
    HORIZONTAL,
    OVERLAY
}

/**
 * 堆叠作用域
 */
interface UnifyStackScope {
    @Composable
    fun Item(
        modifier: Modifier = Modifier,
        alignment: Alignment? = null,
        content: @Composable () -> Unit
    )
}

/**
 * 堆叠作用域实现
 */
private class UnifyStackScopeImpl(
    private val spacing: Dp,
    private val direction: UnifyStackDirection
) : UnifyStackScope {
    
    @Composable
    override fun Item(
        modifier: Modifier,
        alignment: Alignment?,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier,
            contentAlignment = alignment ?: Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * 网格布局组件
 */
@Composable
fun UnifyGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    spacing: UnifySpacing = UnifySpacing.MEDIUM,
    customSpacing: Dp? = null,
    aspectRatio: Float? = null,
    adaptive: Boolean = false,
    minItemWidth: Dp = 120.dp,
    content: @Composable UnifyGridScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualSpacing = customSpacing ?: getSpacingValue(spacing, theme)
    
    val gridColumns = if (adaptive) {
        GridCells.Adaptive(minItemWidth)
    } else {
        GridCells.Fixed(columns)
    }
    
    LazyVerticalGrid(
        columns = gridColumns,
        modifier = modifier,
        contentPadding = PaddingValues(actualSpacing),
        horizontalArrangement = Arrangement.spacedBy(actualSpacing),
        verticalArrangement = Arrangement.spacedBy(actualSpacing)
    ) {
        val scope = UnifyGridScopeImpl(this)
        scope.content()
    }
}

/**
 * 网格作用域
 */
interface UnifyGridScope {
    fun item(
        key: Any? = null,
        span: GridItemSpan? = null,
        content: @Composable () -> Unit
    )
    
    fun items(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        span: ((index: Int) -> GridItemSpan)? = null,
        itemContent: @Composable (index: Int) -> Unit
    )
}

/**
 * 网格作用域实现
 */
private class UnifyGridScopeImpl(
    private val lazyGridScope: LazyGridScope
) : UnifyGridScope {
    
    override fun item(
        key: Any?,
        span: GridItemSpan?,
        content: @Composable () -> Unit
    ) {
        lazyGridScope.item(key = key, span = span, content = { content() })
    }
    
    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        span: ((index: Int) -> GridItemSpan)?,
        itemContent: @Composable (index: Int) -> Unit
    ) {
        lazyGridScope.items(count = count, key = key, span = span) { index ->
            itemContent(index)
        }
    }
}

/**
 * 响应式容器组件
 */
@Composable
fun UnifyResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1200.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.(UnifyBreakpoint) -> Unit
) {
    val screenWidth = with(LocalDensity.current) {
        PlatformManager.getScreenInfo().width.dp
    }
    
    val breakpoint = when {
        screenWidth < 600.dp -> UnifyBreakpoint.COMPACT
        screenWidth < 840.dp -> UnifyBreakpoint.MEDIUM
        else -> UnifyBreakpoint.EXPANDED
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = maxWidth)
            .padding(padding)
    ) {
        content(breakpoint)
    }
}

/**
 * 自适应布局组件
 */
@Composable
fun UnifyAdaptiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: (@Composable () -> Unit)? = null,
    expandedContent: (@Composable () -> Unit)? = null
) {
    UnifyResponsiveContainer(modifier = modifier) { breakpoint ->
        when (breakpoint) {
            UnifyBreakpoint.COMPACT -> compactContent()
            UnifyBreakpoint.MEDIUM -> mediumContent?.invoke() ?: compactContent()
            UnifyBreakpoint.EXPANDED -> expandedContent?.invoke() ?: mediumContent?.invoke() ?: compactContent()
        }
    }
}

/**
 * 分割线组件
 */
@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: androidx.compose.ui.graphics.Color? = null,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp
) {
    val theme = LocalUnifyTheme.current
    val dividerColor = color ?: theme.colors.divider
    
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = dividerColor
    )
}

/**
 * 垂直分割线组件
 */
@Composable
fun UnifyVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: androidx.compose.ui.graphics.Color? = null
) {
    val theme = LocalUnifyTheme.current
    val dividerColor = color ?: theme.colors.divider
    
    androidx.compose.material3.VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = dividerColor
    )
}

/**
 * 间距组件
 */
@Composable
fun UnifySpacer(
    spacing: UnifySpacing = UnifySpacing.MEDIUM,
    customSize: Dp? = null,
    direction: UnifySpacerDirection = UnifySpacerDirection.BOTH
) {
    val theme = LocalUnifyTheme.current
    val size = customSize ?: getSpacingValue(spacing, theme)
    
    when (direction) {
        UnifySpacerDirection.HORIZONTAL -> Spacer(modifier = Modifier.width(size))
        UnifySpacerDirection.VERTICAL -> Spacer(modifier = Modifier.height(size))
        UnifySpacerDirection.BOTH -> Spacer(modifier = Modifier.size(size))
    }
}

/**
 * 间距方向
 */
enum class UnifySpacerDirection {
    HORIZONTAL,
    VERTICAL,
    BOTH
}

/**
 * 流式布局组件
 */
@Composable
fun UnifyFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    spacing: UnifySpacing = UnifySpacing.SMALL,
    customSpacing: Dp? = null,
    content: @Composable UnifyFlowRowScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val actualSpacing = customSpacing ?: getSpacingValue(spacing, theme)
    
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(actualSpacing),
        verticalArrangement = Arrangement.spacedBy(actualSpacing),
        maxItemsInEachRow = maxItemsInEachRow
    ) {
        val scope = UnifyFlowRowScopeImpl()
        scope.content()
    }
}

/**
 * 流式布局作用域
 */
interface UnifyFlowRowScope {
    @Composable
    fun Item(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    )
}

/**
 * 流式布局作用域实现
 */
private class UnifyFlowRowScopeImpl : UnifyFlowRowScope {
    @Composable
    override fun Item(
        modifier: Modifier,
        content: @Composable () -> Unit
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * 获取间距值
 */
@Composable
private fun getSpacingValue(
    spacing: UnifySpacing,
    theme: com.unify.ui.theme.UnifyTheme
): Dp {
    return when (spacing) {
        UnifySpacing.NONE -> 0.dp
        UnifySpacing.EXTRA_SMALL -> theme.dimensions.spaceXs
        UnifySpacing.SMALL -> theme.dimensions.spaceSm
        UnifySpacing.MEDIUM -> theme.dimensions.spaceMd
        UnifySpacing.LARGE -> theme.dimensions.spaceLg
        UnifySpacing.EXTRA_LARGE -> theme.dimensions.spaceXl
        UnifySpacing.CUSTOM -> theme.dimensions.spaceMd // 默认值
    }
}
