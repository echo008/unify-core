package com.unify.core.ui.tv

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TV平台专用焦点管理组件
 * 针对遥控器导航和10-foot UI优化
 * 确保原生级别的焦点性能
 */

/**
 * TV焦点状态
 * 性能优化：使用稳定的数据类
 */
@Stable
data class TVFocusState(
    val isFocused: Boolean = false,
    val isPressed: Boolean = false,
    val scale: Float = 1.0f,
    val borderWidth: Dp = 0.dp,
    val borderColor: Color = Color.Transparent
)

/**
 * 高性能TV可焦点容器
 * 性能优化：使用remember缓存动画和状态计算
 */
@Composable
fun TVFocusableContainer(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    focusScale: Float = TVTheme.focusScale,
    animationDuration: Int = TVTheme.animationDuration,
    onFocusChanged: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    // 性能优化：使用remember缓存焦点状态
    var focusState by remember { mutableStateOf(TVFocusState()) }
    
    // 性能优化：使用remember缓存动画规格
    val animationSpec = remember(animationDuration) {
        tween<Float>(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        )
    }
    
    // 性能优化：使用animateFloatAsState进行高效动画
    val scale by animateFloatAsState(
        targetValue = if (focusState.isFocused) focusScale else 1.0f,
        animationSpec = animationSpec,
        label = "TVFocusScale"
    )
    
    val borderWidth by animateDpAsState(
        targetValue = if (focusState.isFocused) 3.dp else 0.dp,
        animationSpec = tween(animationDuration),
        label = "TVFocusBorder"
    )
    
    // 性能优化：使用remember缓存边框颜色计算
    val borderColor = remember(focusState.isFocused, focusState.isPressed) {
        when {
            focusState.isPressed -> TVFocusColors.pressed
            focusState.isFocused -> TVFocusColors.focusedBorder
            else -> TVFocusColors.unfocused
        }
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            )
            .background(
                color = if (focusState.isFocused) 
                    TVFocusColors.focused.copy(alpha = 0.1f) 
                else Color.Transparent,
                shape = shape
            )
            .focusable()
            .let { mod ->
                focusRequester?.let { mod.focusRequester(it) } ?: mod
            }
            .onFocusChanged { focusInfo ->
                val newFocusState = focusState.copy(isFocused = focusInfo.isFocused)
                focusState = newFocusState
                onFocusChanged(focusInfo.isFocused)
            },
        content = content
    )
}

/**
 * TV可焦点按钮
 * 针对遥控器操作优化的按钮组件
 */
@Composable
fun TVFocusableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    // 性能优化：使用remember缓存按钮状态
    var isFocused by remember { mutableStateOf(false) }
    
    // 性能优化：使用remember缓存颜色计算
    val buttonColors = remember(isFocused, enabled) {
        if (isFocused && enabled) {
            colors.copy(
                containerColor = TVFocusColors.focused,
                contentColor = Color.White
            )
        } else {
            colors
        }
    }
    
    TVFocusableContainer(
        modifier = modifier,
        focusRequester = focusRequester,
        shape = shape,
        onFocusChanged = { focused -> isFocused = focused },
        onClick = if (enabled) onClick else { {} }
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
            shape = shape,
            colors = buttonColors,
            contentPadding = contentPadding
        ) {
            content()
        }
    }
}

/**
 * TV可焦点卡片
 * 适合内容展示的高性能卡片组件
 */
@Composable
fun TVFocusableCard(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    // 性能优化：使用remember缓存焦点状态
    var isFocused by remember { mutableStateOf(false) }
    
    // 性能优化：使用remember缓存卡片颜色
    val cardColors = remember(isFocused) {
        if (isFocused) {
            colors.copy(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor
            )
        } else {
            colors
        }
    }
    
    TVFocusableContainer(
        modifier = modifier,
        focusRequester = focusRequester,
        shape = shape,
        onFocusChanged = { focused -> isFocused = focused },
        onClick = onClick
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            elevation = elevation,
            colors = cardColors,
            content = content
        )
    }
}

/**
 * TV焦点网格布局
 * 针对遥控器导航优化的网格组件
 * 使用LazyColumn和Row实现网格效果
 */
@Composable
fun <T> TVFocusableGrid(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    itemContent: @Composable (item: T, focusRequester: FocusRequester) -> Unit
) {
    // 性能优化：使用remember缓存FocusRequester列表
    val focusRequesters = remember(items.size) {
        List(items.size) { FocusRequester() }
    }
    
    // 将items分组为行
    val rows = remember(items, columns) {
        items.chunked(columns)
    }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(rows) { rowItems ->
            Row(
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val itemIndex = rows.indexOf(rowItems) * columns + columnIndex
                    val focusRequester = focusRequesters.getOrNull(itemIndex) ?: FocusRequester()
                    
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        itemContent(item, focusRequester)
                    }
                }
                
                // 填充空白位置
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * TV焦点导航辅助函数
 * 提供程序化焦点控制
 */
object TVFocusNavigation {
    
    /**
     * 请求焦点到指定组件
     * 性能优化：使用协程避免阻塞UI线程
     */
    @Composable
    fun requestFocus(focusRequester: FocusRequester) {
        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }
    }
    
    /**
     * 创建焦点请求器网格
     * 性能优化：使用remember缓存创建结果
     */
    @Composable
    fun rememberFocusRequesterGrid(
        rows: Int,
        columns: Int
    ): List<List<FocusRequester>> {
        return remember(rows, columns) {
            List(rows) { List(columns) { FocusRequester() } }
        }
    }
}
