package com.unify.ui.responsive

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme

/**
 * Unify 响应式设计系统
 * 支持多平台适配的统一响应式布局，参考 KuiklyUI 设计规范
 */

/**
 * 屏幕尺寸断点枚举
 */
enum class UnifyScreenSize {
    COMPACT,        // 紧凑型 (< 600dp)
    MEDIUM,         // 中等型 (600dp - 840dp)
    EXPANDED        // 扩展型 (> 840dp)
}

/**
 * 设备类型枚举
 */
enum class UnifyDeviceType {
    PHONE,          // 手机
    TABLET,         // 平板
    DESKTOP,        // 桌面
    TV,             // 电视
    WATCH,          // 手表
    FOLDABLE        // 折叠屏
}

/**
 * 屏幕方向枚举
 */
enum class UnifyOrientation {
    PORTRAIT,       // 竖屏
    LANDSCAPE       // 横屏
}

/**
 * 响应式断点数据类
 */
data class UnifyBreakpoints(
    val compact: Dp = 0.dp,
    val medium: Dp = 600.dp,
    val expanded: Dp = 840.dp
)

/**
 * 响应式配置数据类
 */
data class UnifyResponsiveConfig(
    val screenSize: UnifyScreenSize,
    val deviceType: UnifyDeviceType,
    val orientation: UnifyOrientation,
    val screenWidth: Dp,
    val screenHeight: Dp,
    val density: Float,
    val isTablet: Boolean,
    val isLandscape: Boolean
)

/**
 * 响应式上下文提供者
 */
val LocalUnifyResponsive = compositionLocalOf<UnifyResponsiveConfig> {
    error("UnifyResponsiveConfig not provided")
}

/**
 * 响应式配置提供者组件
 */
@Composable
fun UnifyResponsiveProvider(
    breakpoints: UnifyBreakpoints = UnifyBreakpoints(),
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidth = with(density) { configuration.screenWidthDp.dp }
    val screenHeight = with(density) { configuration.screenHeightDp.dp }
    val densityValue = density.density
    
    val screenSize = when {
        screenWidth < breakpoints.medium -> UnifyScreenSize.COMPACT
        screenWidth < breakpoints.expanded -> UnifyScreenSize.MEDIUM
        else -> UnifyScreenSize.EXPANDED
    }
    
    val isLandscape = screenWidth > screenHeight
    val orientation = if (isLandscape) UnifyOrientation.LANDSCAPE else UnifyOrientation.PORTRAIT
    
    val deviceType = determineDeviceType(screenWidth, screenHeight, densityValue)
    val isTablet = deviceType == UnifyDeviceType.TABLET
    
    val responsiveConfig = UnifyResponsiveConfig(
        screenSize = screenSize,
        deviceType = deviceType,
        orientation = orientation,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        density = densityValue,
        isTablet = isTablet,
        isLandscape = isLandscape
    )
    
    CompositionLocalProvider(
        LocalUnifyResponsive provides responsiveConfig
    ) {
        content()
    }
}

/**
 * 响应式容器组件
 */
@Composable
fun UnifyResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1200.dp,
    horizontalPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    val containerPadding = when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> horizontalPadding
        UnifyScreenSize.MEDIUM -> horizontalPadding * 1.5f
        UnifyScreenSize.EXPANDED -> horizontalPadding * 2f
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = containerPadding)
            .widthIn(max = maxWidth)
    ) {
        content()
    }
}

/**
 * 响应式网格组件
 */
@Composable
fun UnifyResponsiveGrid(
    modifier: Modifier = Modifier,
    compactColumns: Int = 1,
    mediumColumns: Int = 2,
    expandedColumns: Int = 3,
    spacing: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    val columns = when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactColumns
        UnifyScreenSize.MEDIUM -> mediumColumns
        UnifyScreenSize.EXPANDED -> expandedColumns
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // content 会在实际使用时提供具体的 items
    }
}

/**
 * 响应式行组件
 */
@Composable
fun UnifyResponsiveRow(
    modifier: Modifier = Modifier,
    compactArrangement: Arrangement.Horizontal = Arrangement.Start,
    mediumArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    expandedArrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    val arrangement = when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactArrangement
        UnifyScreenSize.MEDIUM -> mediumArrangement
        UnifyScreenSize.EXPANDED -> expandedArrangement
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * 响应式列组件
 */
@Composable
fun UnifyResponsiveColumn(
    modifier: Modifier = Modifier,
    compactArrangement: Arrangement.Vertical = Arrangement.Top,
    mediumArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    expandedArrangement: Arrangement.Vertical = Arrangement.spacedBy(24.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    val arrangement = when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactArrangement
        UnifyScreenSize.MEDIUM -> mediumArrangement
        UnifyScreenSize.EXPANDED -> expandedArrangement
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = arrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/**
 * 响应式间距组件
 */
@Composable
fun UnifyResponsiveSpacer(
    compactHeight: Dp = 8.dp,
    mediumHeight: Dp = 16.dp,
    expandedHeight: Dp = 24.dp
) {
    val responsive = LocalUnifyResponsive.current
    
    val height = when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactHeight
        UnifyScreenSize.MEDIUM -> mediumHeight
        UnifyScreenSize.EXPANDED -> expandedHeight
    }
    
    Spacer(modifier = Modifier.height(height))
}

/**
 * 响应式导航布局组件
 */
@Composable
fun UnifyResponsiveNavigation(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    Box(modifier = modifier) {
        when (responsive.screenSize) {
            UnifyScreenSize.COMPACT -> compactContent()
            UnifyScreenSize.MEDIUM -> mediumContent()
            UnifyScreenSize.EXPANDED -> expandedContent()
        }
    }
}

/**
 * 响应式侧边栏布局组件
 */
@Composable
fun UnifyResponsiveSidebar(
    modifier: Modifier = Modifier,
    showSidebar: Boolean = true,
    sidebarWidth: Dp = 280.dp,
    sidebarContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> {
            // 紧凑屏幕：不显示侧边栏或使用抽屉
            mainContent()
        }
        UnifyScreenSize.MEDIUM -> {
            // 中等屏幕：可选显示侧边栏
            if (showSidebar) {
                Row(modifier = modifier) {
                    Box(modifier = Modifier.width(sidebarWidth)) {
                        sidebarContent()
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        mainContent()
                    }
                }
            } else {
                mainContent()
            }
        }
        UnifyScreenSize.EXPANDED -> {
            // 扩展屏幕：始终显示侧边栏
            Row(modifier = modifier) {
                Box(modifier = Modifier.width(sidebarWidth)) {
                    sidebarContent()
                }
                Box(modifier = Modifier.weight(1f)) {
                    mainContent()
                }
            }
        }
    }
}

/**
 * 响应式卡片网格组件
 */
@Composable
fun UnifyResponsiveCardGrid(
    modifier: Modifier = Modifier,
    minCardWidth: Dp = 280.dp,
    spacing: Dp = 16.dp,
    content: @Composable LazyGridScope.() -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    val columns = maxOf(1, (responsive.screenWidth / (minCardWidth + spacing)).toInt())
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(spacing),
        content = content
    )
}

/**
 * 响应式文本大小
 */
@Composable
fun rememberResponsiveTextSize(
    compactSize: androidx.compose.ui.unit.TextUnit,
    mediumSize: androidx.compose.ui.unit.TextUnit,
    expandedSize: androidx.compose.ui.unit.TextUnit
): androidx.compose.ui.unit.TextUnit {
    val responsive = LocalUnifyResponsive.current
    
    return when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactSize
        UnifyScreenSize.MEDIUM -> mediumSize
        UnifyScreenSize.EXPANDED -> expandedSize
    }
}

/**
 * 响应式内边距
 */
@Composable
fun rememberResponsivePadding(
    compactPadding: Dp = 8.dp,
    mediumPadding: Dp = 16.dp,
    expandedPadding: Dp = 24.dp
): Dp {
    val responsive = LocalUnifyResponsive.current
    
    return when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactPadding
        UnifyScreenSize.MEDIUM -> mediumPadding
        UnifyScreenSize.EXPANDED -> expandedPadding
    }
}

/**
 * 响应式圆角
 */
@Composable
fun rememberResponsiveCornerRadius(
    compactRadius: Dp = 4.dp,
    mediumRadius: Dp = 8.dp,
    expandedRadius: Dp = 12.dp
): Dp {
    val responsive = LocalUnifyResponsive.current
    
    return when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactRadius
        UnifyScreenSize.MEDIUM -> mediumRadius
        UnifyScreenSize.EXPANDED -> expandedRadius
    }
}

/**
 * 屏幕尺寸判断扩展函数
 */
@Composable
fun isCompactScreen(): Boolean {
    return LocalUnifyResponsive.current.screenSize == UnifyScreenSize.COMPACT
}

@Composable
fun isMediumScreen(): Boolean {
    return LocalUnifyResponsive.current.screenSize == UnifyScreenSize.MEDIUM
}

@Composable
fun isExpandedScreen(): Boolean {
    return LocalUnifyResponsive.current.screenSize == UnifyScreenSize.EXPANDED
}

@Composable
fun isTablet(): Boolean {
    return LocalUnifyResponsive.current.isTablet
}

@Composable
fun isLandscape(): Boolean {
    return LocalUnifyResponsive.current.isLandscape
}

/**
 * 设备类型判断辅助函数
 */
private fun determineDeviceType(
    screenWidth: Dp,
    screenHeight: Dp,
    density: Float
): UnifyDeviceType {
    val minDimension = minOf(screenWidth.value, screenHeight.value)
    val maxDimension = maxOf(screenWidth.value, screenHeight.value)
    
    return when {
        // 手表：小屏幕，通常是正方形或圆形
        maxDimension < 300 -> UnifyDeviceType.WATCH
        
        // 电视：大屏幕，低密度，横屏为主
        maxDimension > 1000 && density < 2.0f -> UnifyDeviceType.TV
        
        // 平板：中大屏幕
        minDimension >= 600 -> UnifyDeviceType.TABLET
        
        // 折叠屏：特殊宽高比
        maxDimension / minDimension > 2.5f -> UnifyDeviceType.FOLDABLE
        
        // 桌面：大屏幕，中等密度
        minDimension >= 800 && density < 3.0f -> UnifyDeviceType.DESKTOP
        
        // 默认为手机
        else -> UnifyDeviceType.PHONE
    }
}

/**
 * 响应式条件渲染组件
 */
@Composable
fun UnifyResponsiveContent(
    compact: (@Composable () -> Unit)? = null,
    medium: (@Composable () -> Unit)? = null,
    expanded: (@Composable () -> Unit)? = null,
    default: @Composable () -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compact?.invoke() ?: default()
        UnifyScreenSize.MEDIUM -> medium?.invoke() ?: default()
        UnifyScreenSize.EXPANDED -> expanded?.invoke() ?: default()
    }
}

/**
 * 响应式可见性组件
 */
@Composable
fun UnifyResponsiveVisibility(
    visibleOn: Set<UnifyScreenSize> = setOf(UnifyScreenSize.COMPACT, UnifyScreenSize.MEDIUM, UnifyScreenSize.EXPANDED),
    content: @Composable () -> Unit
) {
    val responsive = LocalUnifyResponsive.current
    
    if (responsive.screenSize in visibleOn) {
        content()
    }
}

/**
 * 响应式布局权重
 */
@Composable
fun rememberResponsiveWeight(
    compactWeight: Float = 1f,
    mediumWeight: Float = 1f,
    expandedWeight: Float = 1f
): Float {
    val responsive = LocalUnifyResponsive.current
    
    return when (responsive.screenSize) {
        UnifyScreenSize.COMPACT -> compactWeight
        UnifyScreenSize.MEDIUM -> mediumWeight
        UnifyScreenSize.EXPANDED -> expandedWeight
    }
}
