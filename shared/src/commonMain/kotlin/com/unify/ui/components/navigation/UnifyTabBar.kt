package com.unify.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.launch

/**
 * Unify Tab Bar 组件
 * 支持多平台适配的统一选项卡组件，参考 KuiklyUI 设计规范
 */

/**
 * 选项卡变体枚举
 */
enum class UnifyTabVariant {
    PRIMARY,        // 主要选项卡
    SECONDARY,      // 次要选项卡
    SCROLLABLE,     // 可滚动选项卡
    FIXED           // 固定选项卡
}

/**
 * 选项卡尺寸枚举
 */
enum class UnifyTabSize {
    SMALL,          // 小尺寸 - 32dp
    MEDIUM,         // 中等尺寸 - 48dp
    LARGE           // 大尺寸 - 64dp
}

/**
 * 选项卡项数据类
 */
data class UnifyTabItem(
    val text: String,
    val icon: ImageVector? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
    val contentDescription: String? = null
)

/**
 * 主要 Unify Tab Bar 组件
 */
@Composable
fun UnifyTabBar(
    tabs: List<UnifyTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyTabVariant = UnifyTabVariant.PRIMARY,
    size: UnifyTabSize = UnifyTabSize.MEDIUM,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    indicatorColor: Color? = null,
    divider: (@Composable () -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取选项卡配置
    val tabConfig = getTabConfig(variant, size, theme)
    val actualBackgroundColor = backgroundColor ?: tabConfig.backgroundColor
    val actualContentColor = contentColor ?: tabConfig.contentColor
    val actualSelectedContentColor = selectedContentColor ?: theme.colors.primary
    val actualIndicatorColor = indicatorColor ?: actualSelectedContentColor
    
    Column(
        modifier = modifier
            .background(actualBackgroundColor)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        when (variant) {
            UnifyTabVariant.SCROLLABLE -> {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = actualBackgroundColor,
                    contentColor = actualContentColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = actualIndicatorColor
                        )
                    },
                    divider = divider ?: {
                        UnifyDivider(color = theme.colors.outline.copy(alpha = 0.12f))
                    }
                ) {
                    tabs.forEachIndexed { index, tab ->
                        UnifyTab(
                            selected = index == selectedTabIndex,
                            onClick = { 
                                if (tab.enabled) {
                                    onTabSelected(index)
                                }
                            },
                            text = tab.text,
                            icon = tab.icon,
                            badge = tab.badge,
                            enabled = tab.enabled,
                            size = size,
                            contentColor = actualContentColor,
                            selectedContentColor = actualSelectedContentColor,
                            contentDescription = tab.contentDescription
                        )
                    }
                }
            }
            
            else -> {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = actualBackgroundColor,
                    contentColor = actualContentColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = actualIndicatorColor
                        )
                    },
                    divider = divider ?: {
                        UnifyDivider(color = theme.colors.outline.copy(alpha = 0.12f))
                    }
                ) {
                    tabs.forEachIndexed { index, tab ->
                        UnifyTab(
                            selected = index == selectedTabIndex,
                            onClick = { 
                                if (tab.enabled) {
                                    onTabSelected(index)
                                }
                            },
                            text = tab.text,
                            icon = tab.icon,
                            badge = tab.badge,
                            enabled = tab.enabled,
                            size = size,
                            contentColor = actualContentColor,
                            selectedContentColor = actualSelectedContentColor,
                            contentDescription = tab.contentDescription
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个选项卡组件
 */
@Composable
private fun UnifyTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badge: String? = null,
    enabled: Boolean = true,
    size: UnifyTabSize = UnifyTabSize.MEDIUM,
    contentColor: Color,
    selectedContentColor: Color,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val tabHeight = getTabHeight(size)
    val actualContentColor = if (selected) selectedContentColor else contentColor
    
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .height(tabHeight)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        enabled = enabled,
        text = {
            UnifyText(
                text = text,
                variant = when (size) {
                    UnifyTabSize.SMALL -> UnifyTextVariant.CAPTION
                    UnifyTabSize.MEDIUM -> UnifyTextVariant.BODY_MEDIUM
                    UnifyTabSize.LARGE -> UnifyTextVariant.BODY_LARGE
                },
                color = actualContentColor
            )
        },
        icon = icon?.let { iconVector ->
            {
                Box {
                    UnifyIcon(
                        imageVector = iconVector,
                        contentDescription = null,
                        size = when (size) {
                            UnifyTabSize.SMALL -> UnifyIconSize.EXTRA_SMALL
                            UnifyTabSize.MEDIUM -> UnifyIconSize.SMALL
                            UnifyTabSize.LARGE -> UnifyIconSize.MEDIUM
                        },
                        tint = actualContentColor
                    )
                    
                    // 徽章
                    badge?.let { badgeText ->
                        UnifyBadge(
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            if (badgeText.isNotEmpty()) {
                                UnifyText(
                                    text = badgeText,
                                    variant = UnifyTextVariant.CAPTION,
                                    color = theme.colors.onError
                                )
                            }
                        }
                    }
                }
            }
        },
        selectedContentColor = selectedContentColor,
        unselectedContentColor = contentColor,
        interactionSource = interactionSource
    )
}

/**
 * 带内容的选项卡组件
 */
@Composable
fun UnifyTabBarWithContent(
    tabs: List<UnifyTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyTabVariant = UnifyTabVariant.PRIMARY,
    size: UnifyTabSize = UnifyTabSize.MEDIUM,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    indicatorColor: Color? = null,
    pagerState: PagerState = rememberPagerState(pageCount = { tabs.size }),
    content: @Composable (tabIndex: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // 同步选项卡选择和页面状态
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }
    
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != selectedTabIndex) {
            onTabSelected(pagerState.currentPage)
        }
    }
    
    Column(modifier = modifier) {
        UnifyTabBar(
            tabs = tabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { index ->
                onTabSelected(index)
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            variant = variant,
            size = size,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            selectedContentColor = selectedContentColor,
            indicatorColor = indicatorColor
        )
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            content(page)
        }
    }
}

/**
 * 垂直选项卡组件
 */
@Composable
fun UnifyVerticalTabBar(
    tabs: List<UnifyTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: UnifyTabSize = UnifyTabSize.MEDIUM,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    indicatorColor: Color? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val tabConfig = getTabConfig(UnifyTabVariant.PRIMARY, size, theme)
    val actualBackgroundColor = backgroundColor ?: tabConfig.backgroundColor
    val actualContentColor = contentColor ?: tabConfig.contentColor
    val actualSelectedContentColor = selectedContentColor ?: theme.colors.primary
    val actualIndicatorColor = indicatorColor ?: actualSelectedContentColor
    
    Column(
        modifier = modifier
            .background(actualBackgroundColor)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex
            val tabHeight = getTabHeight(size)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tabHeight)
                    .clickable(
                        enabled = tab.enabled,
                        onClick = { onTabSelected(index) },
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() },
                        role = Role.Tab
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 指示器
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            color = if (isSelected) actualIndicatorColor else Color.Transparent,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 图标
                tab.icon?.let { iconVector ->
                    Box {
                        UnifyIcon(
                            imageVector = iconVector,
                            contentDescription = null,
                            size = when (size) {
                                UnifyTabSize.SMALL -> UnifyIconSize.EXTRA_SMALL
                                UnifyTabSize.MEDIUM -> UnifyIconSize.SMALL
                                UnifyTabSize.LARGE -> UnifyIconSize.MEDIUM
                            },
                            tint = if (isSelected) actualSelectedContentColor else actualContentColor
                        )
                        
                        // 徽章
                        tab.badge?.let { badgeText ->
                            UnifyBadge(
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                if (badgeText.isNotEmpty()) {
                                    UnifyText(
                                        text = badgeText,
                                        variant = UnifyTextVariant.CAPTION,
                                        color = theme.colors.onError
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // 文本
                UnifyText(
                    text = tab.text,
                    variant = when (size) {
                        UnifyTabSize.SMALL -> UnifyTextVariant.CAPTION
                        UnifyTabSize.MEDIUM -> UnifyTextVariant.BODY_MEDIUM
                        UnifyTabSize.LARGE -> UnifyTextVariant.BODY_LARGE
                    },
                    color = if (isSelected) actualSelectedContentColor else actualContentColor,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (index < tabs.size - 1) {
                UnifyDivider(
                    color = theme.colors.outline.copy(alpha = 0.12f),
                    startIndent = 16.dp
                )
            }
        }
    }
}

/**
 * 芯片式选项卡组件
 */
@Composable
fun UnifyChipTabBar(
    tabs: List<UnifyTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: UnifyTabSize = UnifyTabSize.MEDIUM,
    spacing: Dp = 8.dp,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Row(
        modifier = modifier
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex
            
            UnifyChip(
                text = tab.text,
                selected = isSelected,
                onClick = { 
                    if (tab.enabled) {
                        onTabSelected(index)
                    }
                },
                enabled = tab.enabled,
                leadingIcon = tab.icon?.let { icon ->
                    {
                        UnifyIcon(
                            imageVector = icon,
                            contentDescription = null,
                            size = UnifyIconSize.EXTRA_SMALL
                        )
                    }
                }
            )
        }
    }
}

/**
 * 获取选项卡配置
 */
@Composable
private fun getTabConfig(
    variant: UnifyTabVariant,
    size: UnifyTabSize,
    theme: com.unify.ui.theme.UnifyTheme
): TabConfig {
    return when (variant) {
        UnifyTabVariant.PRIMARY -> TabConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface
        )
        UnifyTabVariant.SECONDARY -> TabConfig(
            backgroundColor = theme.colors.surfaceVariant,
            contentColor = theme.colors.onSurfaceVariant
        )
        UnifyTabVariant.SCROLLABLE, UnifyTabVariant.FIXED -> TabConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface
        )
    }
}

/**
 * 获取选项卡高度
 */
private fun getTabHeight(size: UnifyTabSize): Dp {
    return when (size) {
        UnifyTabSize.SMALL -> 32.dp
        UnifyTabSize.MEDIUM -> 48.dp
        UnifyTabSize.LARGE -> 64.dp
    }
}

/**
 * 选项卡配置数据类
 */
private data class TabConfig(
    val backgroundColor: Color,
    val contentColor: Color
)
