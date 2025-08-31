package com.unify.ui.components.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*
import com.unify.ui.components.feedback.UnifyLoading
import com.unify.ui.components.feedback.UnifyLoadingSize
import com.unify.ui.components.feedback.UnifyLoadingVariant

/**
 * Unify List 组件
 * 支持多平台适配的统一列表组件，参考 KuiklyUI 设计规范
 */

/**
 * 列表变体枚举
 */
enum class UnifyListVariant {
    STANDARD,       // 标准列表
    CARD,           // 卡片列表
    INSET,          // 内嵌列表
    DENSE           // 紧凑列表
}

/**
 * 列表项尺寸枚举
 */
enum class UnifyListItemSize {
    COMPACT,        // 紧凑尺寸 - 40dp
    STANDARD,       // 标准尺寸 - 56dp
    LARGE,          // 大尺寸 - 72dp
    EXTRA_LARGE     // 超大尺寸 - 88dp
}

/**
 * 列表项数据类
 */
data class UnifyListItem(
    val id: String = "",
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val avatar: (@Composable () -> Unit)? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val onClick: (() -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
    val contentDescription: String? = null
)

/**
 * 主要 Unify List 组件
 */
@Composable
fun UnifyList(
    items: List<UnifyListItem>,
    modifier: Modifier = Modifier,
    variant: UnifyListVariant = UnifyListVariant.STANDARD,
    size: UnifyListItemSize = UnifyListItemSize.STANDARD,
    showDividers: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    shape: Shape? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val listConfig = getListConfig(variant, theme)
    val actualBackgroundColor = backgroundColor ?: listConfig.backgroundColor
    val actualContentColor = contentColor ?: listConfig.contentColor
    val actualShape = shape ?: listConfig.shape
    
    Surface(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        color = actualBackgroundColor,
        contentColor = actualContentColor,
        shape = actualShape
    ) {
        LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement
        ) {
            itemsIndexed(items) { index, item ->
                UnifyListItemComponent(
                    item = item,
                    size = size,
                    variant = variant,
                    showDivider = showDividers && index < items.size - 1
                )
            }
        }
    }
}

/**
 * 懒加载列表组件
 */
@Composable
fun UnifyLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

/**
 * 懒加载行组件
 */
@Composable
fun UnifyLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

/**
 * 懒加载网格组件
 */
@Composable
fun UnifyLazyGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

/**
 * 瀑布流网格组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifyStaggeredGrid(
    columns: StaggeredGridCells,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = columns,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalItemSpacing = verticalItemSpacing,
        horizontalArrangement = horizontalArrangement,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

/**
 * 分页列表组件
 */
@Composable
fun <T> UnifyPaginatedList(
    items: List<T>,
    isLoading: Boolean,
    hasMoreItems: Boolean,
    onLoadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    loadingContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            UnifyLoading(
                variant = UnifyLoadingVariant.CIRCULAR,
                size = UnifyLoadingSize.MEDIUM
            )
        }
    },
    emptyContent: @Composable () -> Unit = {
        UnifyEmptyState(
            title = "暂无数据",
            description = "列表中没有任何项目"
        )
    }
) {
    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= items.size - 3 && 
                    hasMoreItems && 
                    !isLoading) {
                    onLoadMore()
                }
            }
    }
    
    if (items.isEmpty() && !isLoading) {
        emptyContent()
    } else {
        LazyColumn(
            modifier = modifier,
            state = state,
            contentPadding = contentPadding
        ) {
            items(items) { item ->
                itemContent(item)
            }
            
            if (isLoading) {
                item {
                    loadingContent()
                }
            }
        }
    }
}

/**
 * 列表项组件
 */
@Composable
fun UnifyListItemComponent(
    item: UnifyListItem,
    modifier: Modifier = Modifier,
    size: UnifyListItemSize = UnifyListItemSize.STANDARD,
    variant: UnifyListVariant = UnifyListVariant.STANDARD,
    showDivider: Boolean = false
) {
    val theme = LocalUnifyTheme.current
    // 性能优化：使用remember缓存列表配置，避免重复计算
    val listConfig by remember(variant, size, theme) {
        mutableStateOf(
            UnifyListConfig(
                variant = variant,
                size = size,
                itemHeight = getListItemHeight(size, theme),
                itemPadding = getListItemPadding(size, theme),
                itemSpacing = getListItemSpacing(size, theme)
            )
        )
    }
    
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = listConfig.itemHeight)
                .heightIn(min = itemHeight)
                .let { mod ->
                    if (variant == UnifyListVariant.CARD) {
                        mod
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(theme.shapes.medium)
                            .background(
                                if (item.selected) theme.colors.secondaryContainer 
                                else theme.colors.surface
                            )
                    } else {
                        mod.background(
                            if (item.selected) theme.colors.secondaryContainer.copy(alpha = 0.12f)
                            else Color.Transparent
                        )
                    }
                }
                .clickable(
                    enabled = item.enabled,
                    onClick = { item.onClick?.invoke() },
                    indication = rememberRipple(),
                    interactionSource = interactionSource,
                    role = Role.Button
                )
                .padding(
                    horizontal = if (variant == UnifyListVariant.INSET) 72.dp else 16.dp,
                    vertical = 8.dp
                )
                .semantics {
                    item.contentDescription?.let { 
                        contentDescription = it 
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 前导内容（头像或图标）
            item.avatar?.let { avatarContent ->
                avatarContent()
                Spacer(modifier = Modifier.width(16.dp))
            } ?: item.leadingIcon?.let { icon ->
                UnifyIcon(
                    imageVector = icon,
                    contentDescription = null,
                    size = UnifyIconSize.SMALL,
                    tint = if (item.enabled) Color.Unspecified else theme.colors.disabled
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            // 主要内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                UnifyText(
                    text = item.title,
                    variant = when (size) {
                        UnifyListItemSize.COMPACT -> UnifyTextVariant.BODY_SMALL
                        UnifyListItemSize.STANDARD -> UnifyTextVariant.BODY_MEDIUM
                        UnifyListItemSize.LARGE -> UnifyTextVariant.BODY_LARGE
                        UnifyListItemSize.EXTRA_LARGE -> UnifyTextVariant.TITLE_MEDIUM
                    },
                    color = if (item.enabled) {
                        if (item.selected) theme.colors.onSecondaryContainer else Color.Unspecified
                    } else {
                        theme.colors.disabled
                    }
                )
                
                item.subtitle?.let { subtitleText ->
                    Spacer(modifier = Modifier.height(2.dp))
                    UnifyText(
                        text = subtitleText,
                        variant = UnifyTextVariant.BODY_SMALL,
                        color = if (item.enabled) {
                            theme.colors.onSurfaceVariant
                        } else {
                            theme.colors.disabled
                        }
                    )
                }
                
                item.description?.let { descriptionText ->
                    Spacer(modifier = Modifier.height(2.dp))
                    UnifyText(
                        text = descriptionText,
                        variant = UnifyTextVariant.CAPTION,
                        color = if (item.enabled) {
                            theme.colors.onSurfaceVariant
                        } else {
                            theme.colors.disabled
                        }
                    )
                }
            }
            
            // 尾随内容（徽章或图标）
            item.badge?.let { badgeText ->
                UnifyBadge {
                    UnifyText(
                        text = badgeText,
                        variant = UnifyTextVariant.CAPTION,
                        color = theme.colors.onError
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            item.trailingIcon?.let { icon ->
                UnifyIcon(
                    imageVector = icon,
                    contentDescription = null,
                    size = UnifyIconSize.SMALL,
                    tint = if (item.enabled) Color.Unspecified else theme.colors.disabled
                )
            }
        }
        
        // 分割线
        if (showDivider) {
            UnifyDivider(
                startIndent = if (item.leadingIcon != null || item.avatar != null) 72.dp else 16.dp
            )
        }
    }
}

/**
 * 空状态组件
 */
@Composable
fun UnifyEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    contentDescription: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon?.let { iconVector ->
            UnifyIcon(
                imageVector = iconVector,
                contentDescription = null,
                size = UnifyIconSize.EXTRA_LARGE,
                tint = LocalUnifyTheme.current.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        UnifyText(
            text = title,
            variant = UnifyTextVariant.TITLE_MEDIUM,
            color = LocalUnifyTheme.current.colors.onSurfaceVariant
        )
        
        description?.let { desc ->
            Spacer(modifier = Modifier.height(8.dp))
            UnifyText(
                text = desc,
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = LocalUnifyTheme.current.colors.onSurfaceVariant
            )
        }
        
        action?.let { actionContent ->
            Spacer(modifier = Modifier.height(24.dp))
            actionContent()
        }
    }
}

/**
 * 分组列表组件
 */
@Composable
fun UnifyGroupedList(
    groups: List<Pair<String, List<UnifyListItem>>>,
    modifier: Modifier = Modifier,
    variant: UnifyListVariant = UnifyListVariant.STANDARD,
    size: UnifyListItemSize = UnifyListItemSize.STANDARD,
    showDividers: Boolean = true,
    stickyHeaders: Boolean = true,
    contentDescription: String? = null
) {
    LazyColumn(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        groups.forEach { (groupTitle, groupItems) ->
            if (stickyHeaders) {
                stickyHeader {
                    UnifyListGroupHeader(title = groupTitle)
                }
            } else {
                item {
                    UnifyListGroupHeader(title = groupTitle)
                }
            }
            
            itemsIndexed(groupItems) { index, item ->
                UnifyListItemComponent(
                    item = item,
                    size = size,
                    variant = variant,
                    showDivider = showDividers && index < groupItems.size - 1
                )
            }
        }
    }
}

/**
 * 列表分组头部组件
 */
@Composable
private fun UnifyListGroupHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = theme.colors.surface
    ) {
        UnifyText(
            text = title,
            variant = UnifyTextVariant.TITLE_SMALL,
            color = theme.colors.primary,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )
    }
}

/**
 * 获取列表配置
 */
@Composable
private fun getListConfig(
    variant: UnifyListVariant,
    theme: com.unify.ui.theme.UnifyTheme
): ListConfig {
    return when (variant) {
        UnifyListVariant.STANDARD -> ListConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface,
            shape = RoundedCornerShape(0.dp)
        )
        UnifyListVariant.CARD -> ListConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface,
            shape = theme.shapes.medium
        )
        UnifyListVariant.INSET -> ListConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface,
            shape = RoundedCornerShape(0.dp)
        )
        UnifyListVariant.DENSE -> ListConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface,
            shape = RoundedCornerShape(0.dp)
        )
    }
}

/**
 * 获取列表项高度
 */
private fun getListItemHeight(size: UnifyListItemSize): Dp {
    return when (size) {
        UnifyListItemSize.COMPACT -> 40.dp
        UnifyListItemSize.STANDARD -> 56.dp
        UnifyListItemSize.LARGE -> 72.dp
        UnifyListItemSize.EXTRA_LARGE -> 88.dp
    }
}

/**
 * 列表配置数据类
 */
private data class ListConfig(
    val backgroundColor: Color,
    val contentColor: Color,
    val shape: Shape
)
