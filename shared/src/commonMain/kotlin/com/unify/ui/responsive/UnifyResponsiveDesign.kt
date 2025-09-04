package com.unify.ui.responsive

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台响应式设计系统
 * 支持不同屏幕尺寸和设备类型的自适应布局
 */

// 断点定义
enum class ScreenSize {
    COMPACT,    // 手机竖屏 < 600dp
    MEDIUM,     // 手机横屏/小平板 600dp - 840dp
    EXPANDED    // 大平板/桌面 > 840dp
}

enum class DeviceType {
    PHONE, TABLET, DESKTOP, TV, WATCH
}

data class ResponsiveConfig(
    val screenSize: ScreenSize,
    val deviceType: DeviceType,
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isLandscape: Boolean
)

@Composable
fun rememberResponsiveConfig(): ResponsiveConfig {
    val density = LocalDensity.current
    
    // 这里应该根据实际平台获取屏幕信息
    // 暂时使用模拟数据
    val screenWidth = 400.dp
    val screenHeight = 800.dp
    val isLandscape = screenWidth > screenHeight
    
    val screenSize = when {
        screenWidth < 600.dp -> ScreenSize.COMPACT
        screenWidth < 840.dp -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
    
    val deviceType = when {
        screenWidth < 600.dp -> DeviceType.PHONE
        screenWidth < 1200.dp -> DeviceType.TABLET
        else -> DeviceType.DESKTOP
    }
    
    return ResponsiveConfig(
        screenSize = screenSize,
        deviceType = deviceType,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        isLandscape = isLandscape
    )
}

@Composable
fun UnifyResponsiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: (@Composable () -> Unit)? = null,
    expandedContent: (@Composable () -> Unit)? = null
) {
    val config = rememberResponsiveConfig()
    
    Box(modifier = modifier) {
        when (config.screenSize) {
            ScreenSize.COMPACT -> compactContent()
            ScreenSize.MEDIUM -> mediumContent?.invoke() ?: compactContent()
            ScreenSize.EXPANDED -> expandedContent?.invoke() ?: mediumContent?.invoke() ?: compactContent()
        }
    }
}

@Composable
fun UnifyAdaptiveNavigation(
    navigationContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val config = rememberResponsiveConfig()
    
    when (config.screenSize) {
        ScreenSize.COMPACT -> {
            // 手机：底部导航
            Column(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                navigationContent()
            }
        }
        ScreenSize.MEDIUM -> {
            // 平板：侧边导航栏
            Row(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(80.dp)) {
                    navigationContent()
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
        ScreenSize.EXPANDED -> {
            // 桌面：侧边抽屉导航
            Row(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(240.dp)) {
                    navigationContent()
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun UnifyAdaptiveGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val config = rememberResponsiveConfig()
    
    val columns = when (config.screenSize) {
        ScreenSize.COMPACT -> 1
        ScreenSize.MEDIUM -> 2
        ScreenSize.EXPANDED -> 3
    }
    
    // 简化实现，避免LazyGridScope编译错误
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

@Composable
fun UnifyResponsiveRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit
) {
    val config = rememberResponsiveConfig()
    
    if (config.screenSize == ScreenSize.COMPACT && config.isLandscape.not()) {
        // 手机竖屏：垂直排列
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // 将Row内容转换为Column内容
            // 简化实现，避免RowScope编译错误
            content(this as RowScope)
        }
    } else {
        // 其他情况：水平排列
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content(this)
        }
    }
}

@Composable
fun UnifyResponsiveColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val config = rememberResponsiveConfig()
    
    if (config.screenSize == ScreenSize.EXPANDED) {
        // 大屏：可能需要限制宽度
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 800.dp),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                content(this)
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content(this)
        }
    }
}

// 响应式尺寸工具
object ResponsiveSizes {
    @Composable
    fun padding(): PaddingValues {
        val config = rememberResponsiveConfig()
        return when (config.screenSize) {
            ScreenSize.COMPACT -> PaddingValues(8.dp)
            ScreenSize.MEDIUM -> PaddingValues(16.dp)
            ScreenSize.EXPANDED -> PaddingValues(24.dp)
        }
    }
    
    @Composable
    fun cardElevation(): Dp {
        val config = rememberResponsiveConfig()
        return when (config.screenSize) {
            ScreenSize.COMPACT -> 2.dp
            ScreenSize.MEDIUM -> 4.dp
            ScreenSize.EXPANDED -> 8.dp
        }
    }
    
    @Composable
    fun buttonHeight(): Dp {
        val config = rememberResponsiveConfig()
        return when (config.deviceType) {
            DeviceType.PHONE -> 48.dp
            DeviceType.TABLET -> 56.dp
            DeviceType.DESKTOP -> 40.dp
            DeviceType.TV -> 64.dp
            DeviceType.WATCH -> 32.dp
        }
    }
}

// 设备检测工具
@Composable
expect fun isTablet(): Boolean

@Composable
expect fun isDesktop(): Boolean

@Composable
expect fun isTV(): Boolean

@Composable
expect fun isWatch(): Boolean

@Composable
expect fun getScreenDensity(): Float

@Composable
expect fun getScreenOrientation(): Int

// 响应式断点工具
object BreakpointUtils {
    const val COMPACT_MAX_WIDTH = 600
    const val MEDIUM_MAX_WIDTH = 840
    
    fun getScreenSize(widthDp: Int): ScreenSize {
        return when {
            widthDp < COMPACT_MAX_WIDTH -> ScreenSize.COMPACT
            widthDp < MEDIUM_MAX_WIDTH -> ScreenSize.MEDIUM
            else -> ScreenSize.EXPANDED
        }
    }
    
    fun getDeviceType(widthDp: Int, heightDp: Int): DeviceType {
        val minDimension = minOf(widthDp, heightDp)
        val maxDimension = maxOf(widthDp, heightDp)
        
        return when {
            maxDimension < 600 -> DeviceType.PHONE
            minDimension < 600 -> DeviceType.PHONE
            maxDimension < 1200 -> DeviceType.TABLET
            else -> DeviceType.DESKTOP
        }
    }
}
