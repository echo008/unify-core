package com.unify.ui.components.container

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Unify 视图容器组件
 * 对应微信小程序的 scroll-view、swiper、movable-view 等容器组件
 */

/**
 * 滚动视图方向
 */
enum class UnifyScrollDirection {
    VERTICAL,       // 垂直滚动
    HORIZONTAL,     // 水平滚动
    BOTH           // 双向滚动
}

/**
 * 滚动视图配置
 */
data class UnifyScrollViewConfig(
    val direction: UnifyScrollDirection = UnifyScrollDirection.VERTICAL,
    val showScrollbar: Boolean = true,
    val enableRefresh: Boolean = false,
    val enableLoadMore: Boolean = false,
    val scrollToTop: Boolean = false,
    val upperThreshold: Dp = 50.dp,
    val lowerThreshold: Dp = 50.dp,
    val scrollWithAnimation: Boolean = true,
    val enhanced: Boolean = false
)

/**
 * 滚动视图组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifyScrollView(
    modifier: Modifier = Modifier,
    config: UnifyScrollViewConfig = UnifyScrollViewConfig(),
    onScrollToUpper: (() -> Unit)? = null,
    onScrollToLower: (() -> Unit)? = null,
    onScroll: ((scrollTop: Int, scrollLeft: Int) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onLoadMore: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    // 监听滚动事件
    LaunchedEffect(verticalScrollState.value, horizontalScrollState.value) {
        onScroll?.invoke(verticalScrollState.value, horizontalScrollState.value)
        
        // 检查是否到达顶部
        if (verticalScrollState.value <= with(density) { config.upperThreshold.toPx() }) {
            onScrollToUpper?.invoke()
        }
        
        // 检查是否到达底部
        if (verticalScrollState.value >= verticalScrollState.maxValue - with(density) { config.lowerThreshold.toPx() }) {
            onScrollToLower?.invoke()
        }
    }
    
    // 下拉刷新处理
    val refreshState = rememberPullToRefreshState()
    
    Box(
        modifier = modifier
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
            .then(
                when (config.direction) {
                    UnifyScrollDirection.VERTICAL -> {
                        if (config.enableRefresh) {
                            Modifier.nestedScroll(refreshState.nestedScrollConnection)
                        } else {
                            Modifier.verticalScroll(verticalScrollState)
                        }
                    }
                    UnifyScrollDirection.HORIZONTAL -> {
                        Modifier.horizontalScroll(horizontalScrollState)
                    }
                    UnifyScrollDirection.BOTH -> {
                        Modifier
                            .verticalScroll(verticalScrollState)
                            .horizontalScroll(horizontalScrollState)
                    }
                }
            )
    ) {
        content()
        
        // 下拉刷新指示器
        if (config.enableRefresh && refreshState.isRefreshing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                UnifyLoading(
                    variant = UnifyLoadingVariant.CIRCULAR,
                    size = UnifyLoadingSize.SMALL
                )
            }
        }
        
        // 上拉加载更多指示器
        if (config.enableLoadMore && isLoadingMore) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyLoading(
                        variant = UnifyLoadingVariant.CIRCULAR,
                        size = UnifyLoadingSize.SMALL
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(
                        text = "加载中...",
                        variant = UnifyTextVariant.CAPTION
                    )
                }
            }
        }
    }
    
    // 处理刷新逻辑
    LaunchedEffect(refreshState.isRefreshing) {
        if (refreshState.isRefreshing && !isRefreshing) {
            isRefreshing = true
            onRefresh?.invoke()
        }
    }
}

/**
 * 轮播图组件配置
 */
data class UnifySwiperConfig(
    val autoplay: Boolean = false,
    val interval: Long = 5000L,
    val duration: Long = 500L,
    val circular: Boolean = false,
    val vertical: Boolean = false,
    val previousMargin: Dp = 0.dp,
    val nextMargin: Dp = 0.dp,
    val snapToAlignment: Boolean = true,
    val displayMultipleItems: Int = 1,
    val skipHiddenItemLayout: Boolean = false
)

/**
 * 轮播图组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifySwiper(
    itemCount: Int,
    modifier: Modifier = Modifier,
    config: UnifySwiperConfig = UnifySwiperConfig(),
    onCurrentChange: ((current: Int) -> Unit)? = null,
    onTransition: ((dx: Float, dy: Float) -> Unit)? = null,
    onAnimationFinish: ((current: Int) -> Unit)? = null,
    showIndicators: Boolean = true,
    indicatorColor: Color = Color.Gray,
    indicatorActiveColor: Color = LocalUnifyTheme.current.colors.primary,
    contentDescription: String? = null,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { itemCount })
    val coroutineScope = rememberCoroutineScope()
    
    // 自动播放
    LaunchedEffect(config.autoplay, itemCount) {
        if (config.autoplay && itemCount > 1) {
            while (true) {
                kotlinx.coroutines.delay(config.interval)
                val nextPage = if (config.circular) {
                    (pagerState.currentPage + 1) % itemCount
                } else {
                    minOf(pagerState.currentPage + 1, itemCount - 1)
                }
                
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(config.duration.toInt())
                )
            }
        }
    }
    
    // 监听页面变化
    LaunchedEffect(pagerState.currentPage) {
        onCurrentChange?.invoke(pagerState.currentPage)
        onAnimationFinish?.invoke(pagerState.currentPage)
    }
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        if (config.vertical) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    top = config.previousMargin,
                    bottom = config.nextMargin
                ),
                pageSpacing = 8.dp
            ) { page ->
                content(page)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = config.previousMargin,
                    end = config.nextMargin
                ),
                pageSpacing = 8.dp
            ) { page ->
                content(page)
            }
        }
        
        // 指示器
        if (showIndicators && itemCount > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                UnifySwiperIndicators(
                    count = itemCount,
                    currentIndex = pagerState.currentPage,
                    vertical = config.vertical,
                    activeColor = indicatorActiveColor,
                    inactiveColor = indicatorColor
                )
            }
        }
    }
}

/**
 * 轮播图指示器
 */
@Composable
private fun UnifySwiperIndicators(
    count: Int,
    currentIndex: Int,
    vertical: Boolean = false,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    if (vertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(count) { index ->
                Box(
                    modifier = Modifier
                        .size(
                            width = 6.dp,
                            height = if (index == currentIndex) 16.dp else 6.dp
                        )
                        .background(
                            color = if (index == currentIndex) activeColor else inactiveColor,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(count) { index ->
                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentIndex) 16.dp else 6.dp,
                            height = 6.dp
                        )
                        .background(
                            color = if (index == currentIndex) activeColor else inactiveColor,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

/**
 * 可移动视图配置
 */
data class UnifyMovableViewConfig(
    val direction: UnifyMovableDirection = UnifyMovableDirection.ALL,
    val inertia: Boolean = false,
    val outOfBounds: Boolean = false,
    val x: Dp = 0.dp,
    val y: Dp = 0.dp,
    val damping: Float = 20f,
    val friction: Float = 2f,
    val disabled: Boolean = false,
    val scale: Boolean = false,
    val scaleMin: Float = 0.5f,
    val scaleMax: Float = 10f,
    val scaleValue: Float = 1f,
    val animation: Boolean = true
)

/**
 * 可移动方向
 */
enum class UnifyMovableDirection {
    ALL,        // 全方向
    VERTICAL,   // 垂直
    HORIZONTAL, // 水平
    NONE       // 不可移动
}

/**
 * 可移动视图区域
 */
@Composable
fun UnifyMovableArea(
    modifier: Modifier = Modifier,
    scaleArea: Boolean = false,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        content()
    }
}

/**
 * 可移动视图组件
 */
@Composable
fun UnifyMovableView(
    config: UnifyMovableViewConfig,
    modifier: Modifier = Modifier,
    onChange: ((x: Float, y: Float, source: String) -> Unit)? = null,
    onScale: ((scale: Float) -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var offsetX by remember { mutableStateOf(config.x) }
    var offsetY by remember { mutableStateOf(config.y) }
    var scale by remember { mutableStateOf(config.scaleValue) }
    
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .offset { 
                IntOffset(
                    with(density) { offsetX.roundToPx() },
                    with(density) { offsetY.roundToPx() }
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(config.disabled) {
                if (!config.disabled) {
                    detectDragGestures(
                        onDragEnd = {
                            onChange?.invoke(
                                with(density) { offsetX.toPx() },
                                with(density) { offsetY.toPx() },
                                "touch"
                            )
                        }
                    ) { _, dragAmount ->
                        when (config.direction) {
                            UnifyMovableDirection.ALL -> {
                                offsetX += with(density) { dragAmount.x.toDp() }
                                offsetY += with(density) { dragAmount.y.toDp() }
                            }
                            UnifyMovableDirection.HORIZONTAL -> {
                                offsetX += with(density) { dragAmount.x.toDp() }
                            }
                            UnifyMovableDirection.VERTICAL -> {
                                offsetY += with(density) { dragAmount.y.toDp() }
                            }
                            UnifyMovableDirection.NONE -> {
                                // 不移动
                            }
                        }
                    }
                }
            }
            .then(
                if (config.scale) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(config.scaleMin, config.scaleMax)
                            onScale?.invoke(scale)
                        }
                    }
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        content()
    }
}

/**
 * 覆盖视图组件
 */
@Composable
fun UnityCoverView(
    modifier: Modifier = Modifier,
    scrollTop: Boolean = false,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        content()
    }
}

/**
 * 匹配媒体组件
 */
@Composable
fun UnifyMatchMedia(
    minWidth: Dp? = null,
    maxWidth: Dp? = null,
    width: Dp? = null,
    minHeight: Dp? = null,
    maxHeight: Dp? = null,
    height: Dp? = null,
    orientation: String? = null,
    modifier: Modifier = Modifier,
    onResize: ((matches: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // 媒体查询匹配逻辑
    var matches by remember { mutableStateOf(true) }
    
    // 这里应该根据实际屏幕尺寸进行匹配判断
    // 简化实现，实际应用中需要监听屏幕尺寸变化
    
    LaunchedEffect(matches) {
        onResize?.invoke(matches)
    }
    
    if (matches) {
        content()
    }
}

/**
 * 页面容器组件
 */
@Composable
fun UnifyPageContainer(
    modifier: Modifier = Modifier,
    enablePullDownRefresh: Boolean = false,
    onPullDownRefresh: (() -> Unit)? = null,
    backgroundColor: Color = LocalUnifyTheme.current.colors.surface,
    backgroundTextStyle: String = "light",
    enableShareAppMessage: Boolean = false,
    enableShareTimeline: Boolean = false,
    contentDescription: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        content()
    }
}
