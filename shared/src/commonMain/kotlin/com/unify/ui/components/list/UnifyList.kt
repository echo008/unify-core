package com.unify.ui.components.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unify列表组件 - 跨平台统一列表系统
 * 支持8大平台的高性能列表渲染和交互
 */

/**
 * 统一列表项数据类
 */
data class UnifyListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val badge: String? = null,
    val isSelected: Boolean = false,
    val isEnabled: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 统一列表配置
 */
data class UnifyListConfig(
    val itemSpacing: Dp = 8.dp,
    val contentPadding: PaddingValues = PaddingValues(16.dp),
    val showDividers: Boolean = false,
    val dividerColor: Color = Color.Gray.copy(alpha = 0.3f),
    val dividerThickness: Dp = 1.dp,
    val itemCornerRadius: Dp = 8.dp,
    val itemElevation: Dp = 2.dp,
    val itemBackgroundColor: Color = Color.White,
    val selectedItemColor: Color = Color.Blue.copy(alpha = 0.1f),
    val disabledItemColor: Color = Color.Gray.copy(alpha = 0.3f)
)

/**
 * 统一垂直列表
 */
@Composable
fun <T> UnifyVerticalList(
    items: List<T>,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyListState = rememberLazyListState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    onItemLongClick: ((T, Int) -> Unit)? = null,
    itemContent: @Composable LazyItemScope.(T, Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        verticalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(config.itemCornerRadius))
                    .background(config.itemBackgroundColor)
                    .then(
                        if (onItemClick != null) {
                            Modifier.clickable { onItemClick(item, index) }
                        } else Modifier
                    )
                    .padding(12.dp)
            ) {
                itemContent(item, index)
            }
            
            if (config.showDividers && index < items.size - 1) {
                HorizontalDivider(
                    thickness = config.dividerThickness,
                    color = config.dividerColor
                )
            }
        }
    }
}

/**
 * 统一水平列表
 */
@Composable
fun <T> UnifyHorizontalList(
    items: List<T>,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyListState = rememberLazyListState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    itemContent: @Composable LazyItemScope.(T, Int) -> Unit
) {
    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        horizontalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(config.itemCornerRadius))
                    .background(config.itemBackgroundColor)
                    .then(
                        if (onItemClick != null) {
                            Modifier.clickable { onItemClick(item, index) }
                        } else Modifier
                    )
                    .padding(12.dp)
            ) {
                itemContent(item, index)
            }
        }
    }
}

/**
 * 统一网格列表
 */
@Composable
fun <T> UnifyGridList(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyGridState = rememberLazyGridState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    itemContent: @Composable LazyGridItemScope.(T, Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        verticalArrangement = Arrangement.spacedBy(config.itemSpacing),
        horizontalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(config.itemCornerRadius))
                    .background(config.itemBackgroundColor)
                    .then(
                        if (onItemClick != null) {
                            Modifier.clickable { onItemClick(item, index) }
                        } else Modifier
                    )
                    .padding(12.dp)
            ) {
                itemContent(item, index)
            }
        }
    }
}

/**
 * 统一自适应网格列表
 */
@Composable
fun <T> UnifyAdaptiveGridList(
    items: List<T>,
    minItemWidth: Dp = 200.dp,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyGridState = rememberLazyGridState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    itemContent: @Composable LazyGridItemScope.(T, Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minItemWidth),
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        verticalArrangement = Arrangement.spacedBy(config.itemSpacing),
        horizontalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(config.itemCornerRadius))
                    .background(config.itemBackgroundColor)
                    .then(
                        if (onItemClick != null) {
                            Modifier.clickable { onItemClick(item, index) }
                        } else Modifier
                    )
                    .padding(12.dp)
            ) {
                itemContent(item, index)
            }
        }
    }
}

/**
 * 统一瀑布流列表
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> UnifyStaggeredList(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    itemContent: @Composable LazyStaggeredGridItemScope.(T, Int) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        verticalItemSpacing = config.itemSpacing,
        horizontalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(config.itemCornerRadius))
                    .background(config.itemBackgroundColor)
                    .then(
                        if (onItemClick != null) {
                            Modifier.clickable { onItemClick(item, index) }
                        } else Modifier
                    )
                    .padding(12.dp)
            ) {
                itemContent(item, index)
            }
        }
    }
}

/**
 * 统一列表项组件
 */
@Composable
fun UnifyListItemComponent(
    item: UnifyListItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 前置内容
            leadingContent?.invoke()
            
            // 主要内容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (leadingContent != null) 12.dp else 0.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                
                item.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                item.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // 徽章
            item.badge?.let { badge ->
                Badge(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp
                    )
                }
            }
            
            // 后置内容
            trailingContent?.invoke()
        }
    }
}

/**
 * 统一分组列表
 */
@Composable
fun <T, K> UnifyGroupedList(
    items: Map<K, List<T>>,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyListState = rememberLazyListState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    headerContent: @Composable (K) -> Unit,
    itemContent: @Composable LazyItemScope.(T, Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        verticalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        items.forEach { (key, groupItems) ->
            // 分组标题
            item {
                headerContent(key)
            }
            
            // 分组项目
            itemsIndexed(groupItems) { index, item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(config.itemCornerRadius))
                        .background(config.itemBackgroundColor)
                        .then(
                            if (onItemClick != null) {
                                Modifier.clickable { onItemClick(item, index) }
                            } else Modifier
                        )
                        .padding(12.dp)
                ) {
                    itemContent(item, index)
                }
            }
        }
    }
}

/**
 * 统一可展开列表
 */
@Composable
fun <T> UnifyExpandableList(
    items: List<T>,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyListState = rememberLazyListState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    isExpanded: (T) -> Boolean = { false },
    onExpandToggle: ((T, Int) -> Unit)? = null,
    headerContent: @Composable LazyItemScope.(T, Int) -> Unit,
    expandedContent: @Composable LazyItemScope.(T, Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = config.contentPadding,
        verticalArrangement = Arrangement.spacedBy(config.itemSpacing)
    ) {
        itemsIndexed(items) { index, item ->
            Column {
                // 标题内容
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(config.itemCornerRadius))
                        .background(config.itemBackgroundColor)
                        .clickable { 
                            onExpandToggle?.invoke(item, index)
                            onItemClick?.invoke(item, index)
                        }
                        .padding(12.dp)
                ) {
                    headerContent(item, index)
                }
                
                // 展开内容
                if (isExpanded(item)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(config.itemCornerRadius))
                            .background(config.itemBackgroundColor.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        expandedContent(item, index)
                    }
                }
            }
        }
    }
}

/**
 * 统一搜索列表
 */
@Composable
fun <T> UnifySearchableList(
    items: List<T>,
    searchQuery: String,
    modifier: Modifier = Modifier,
    config: UnifyListConfig = UnifyListConfig(),
    state: LazyListState = rememberLazyListState(),
    onItemClick: ((T, Int) -> Unit)? = null,
    searchFilter: (T, String) -> Boolean,
    itemContent: @Composable LazyItemScope.(T, Int) -> Unit
) {
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { searchFilter(it, searchQuery) }
        }
    }
    
    UnifyVerticalList(
        items = filteredItems,
        modifier = modifier,
        config = config,
        state = state,
        onItemClick = onItemClick,
        itemContent = itemContent
    )
}

/**
 * 默认列表配置
 */
val DefaultListConfig = UnifyListConfig()
