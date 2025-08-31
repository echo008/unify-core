package com.unify.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
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

/**
 * Unify Navigation Bar 组件
 * 支持多平台适配的统一导航栏组件，参考 KuiklyUI 设计规范
 */

/**
 * 导航栏变体枚举
 */
enum class UnifyNavigationBarVariant {
    STANDARD,       // 标准导航栏
    ELEVATED,       // 提升导航栏
    FILLED,         // 填充导航栏
    TRANSPARENT     // 透明导航栏
}

/**
 * 导航栏尺寸枚举
 */
enum class UnifyNavigationBarSize {
    COMPACT,        // 紧凑尺寸 - 56dp
    STANDARD,       // 标准尺寸 - 64dp
    LARGE           // 大尺寸 - 72dp
}

/**
 * 导航项数据类
 */
data class UnifyNavigationItem(
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val label: String,
    val badge: String? = null,
    val enabled: Boolean = true,
    val contentDescription: String? = null
)

/**
 * 主要 Unify Navigation Bar 组件
 */
@Composable
fun UnifyNavigationBar(
    items: List<UnifyNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyNavigationBarVariant = UnifyNavigationBarVariant.STANDARD,
    size: UnifyNavigationBarSize = UnifyNavigationBarSize.STANDARD,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    showLabels: Boolean = true,
    alwaysShowLabels: Boolean = false,
    maxItems: Int = 5,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取导航栏配置
    val navBarConfig = getNavigationBarConfig(variant, size, theme)
    val actualBackgroundColor = backgroundColor ?: navBarConfig.backgroundColor
    val actualContentColor = contentColor ?: navBarConfig.contentColor
    val actualSelectedContentColor = selectedContentColor ?: theme.colors.primary
    
    // 限制项目数量
    val displayItems = items.take(maxItems)
    
    NavigationBar(
        modifier = modifier
            .height(navBarConfig.height)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        containerColor = actualBackgroundColor,
        contentColor = actualContentColor,
        tonalElevation = navBarConfig.elevation,
        windowInsets = WindowInsets.navigationBars
    ) {
        displayItems.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { 
                    if (item.enabled) {
                        onItemSelected(index)
                    }
                },
                icon = {
                    Box {
                        UnifyIcon(
                            imageVector = if (isSelected && item.selectedIcon != null) {
                                item.selectedIcon
                            } else {
                                item.icon
                            },
                            contentDescription = item.contentDescription,
                            size = UnifyIconSize.SMALL,
                            tint = if (isSelected) actualSelectedContentColor else actualContentColor
                        )
                        
                        // 徽章
                        item.badge?.let { badgeText ->
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
                },
                label = if (showLabels && (alwaysShowLabels || isSelected)) {
                    {
                        UnifyText(
                            text = item.label,
                            variant = UnifyTextVariant.CAPTION,
                            color = if (isSelected) actualSelectedContentColor else actualContentColor
                        )
                    }
                } else null,
                enabled = item.enabled,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = actualSelectedContentColor,
                    selectedTextColor = actualSelectedContentColor,
                    unselectedIconColor = actualContentColor,
                    unselectedTextColor = actualContentColor,
                    indicatorColor = actualSelectedContentColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}

/**
 * 底部导航栏组件
 */
@Composable
fun UnifyBottomNavigationBar(
    items: List<UnifyNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyNavigationBarVariant = UnifyNavigationBarVariant.STANDARD,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    showLabels: Boolean = true,
    contentDescription: String? = null
) {
    UnifyNavigationBar(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        variant = variant,
        size = UnifyNavigationBarSize.STANDARD,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        selectedContentColor = selectedContentColor,
        showLabels = showLabels,
        alwaysShowLabels = true,
        contentDescription = contentDescription
    )
}

/**
 * 顶部导航栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyTopNavigationBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<UnifyNavigationAction> = emptyList(),
    variant: UnifyNavigationBarVariant = UnifyNavigationBarVariant.STANDARD,
    size: UnifyNavigationBarSize = UnifyNavigationBarSize.STANDARD,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val navBarConfig = getNavigationBarConfig(variant, size, theme)
    val actualBackgroundColor = backgroundColor ?: navBarConfig.backgroundColor
    val actualContentColor = contentColor ?: navBarConfig.contentColor
    
    when (size) {
        UnifyNavigationBarSize.COMPACT -> {
            TopAppBar(
                title = {
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.TITLE_MEDIUM,
                        color = actualContentColor
                    )
                },
                modifier = modifier.semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
                navigationIcon = {
                    navigationIcon?.let { icon ->
                        UnifyIconButton(
                            imageVector = icon,
                            contentDescription = "导航",
                            onClick = { onNavigationClick?.invoke() },
                            tint = actualContentColor
                        )
                    }
                },
                actions = {
                    actions.forEach { action ->
                        UnifyIconButton(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            onClick = action.onClick,
                            enabled = action.enabled,
                            tint = actualContentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = actualBackgroundColor,
                    titleContentColor = actualContentColor,
                    navigationIconContentColor = actualContentColor,
                    actionIconContentColor = actualContentColor
                ),
                scrollBehavior = scrollBehavior
            )
        }
        
        UnifyNavigationBarSize.STANDARD -> {
            MediumTopAppBar(
                title = {
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.TITLE_LARGE,
                        color = actualContentColor
                    )
                },
                modifier = modifier.semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
                navigationIcon = {
                    navigationIcon?.let { icon ->
                        UnifyIconButton(
                            imageVector = icon,
                            contentDescription = "导航",
                            onClick = { onNavigationClick?.invoke() },
                            tint = actualContentColor
                        )
                    }
                },
                actions = {
                    actions.forEach { action ->
                        UnifyIconButton(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            onClick = action.onClick,
                            enabled = action.enabled,
                            tint = actualContentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = actualBackgroundColor,
                    titleContentColor = actualContentColor,
                    navigationIconContentColor = actualContentColor,
                    actionIconContentColor = actualContentColor
                ),
                scrollBehavior = scrollBehavior
            )
        }
        
        UnifyNavigationBarSize.LARGE -> {
            LargeTopAppBar(
                title = {
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.HEADLINE_MEDIUM,
                        color = actualContentColor
                    )
                },
                modifier = modifier.semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
                navigationIcon = {
                    navigationIcon?.let { icon ->
                        UnifyIconButton(
                            imageVector = icon,
                            contentDescription = "导航",
                            onClick = { onNavigationClick?.invoke() },
                            tint = actualContentColor
                        )
                    }
                },
                actions = {
                    actions.forEach { action ->
                        UnifyIconButton(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            onClick = action.onClick,
                            enabled = action.enabled,
                            tint = actualContentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = actualBackgroundColor,
                    titleContentColor = actualContentColor,
                    navigationIconContentColor = actualContentColor,
                    actionIconContentColor = actualContentColor
                ),
                scrollBehavior = scrollBehavior
            )
        }
    }
}

/**
 * 导航操作数据类
 */
data class UnifyNavigationAction(
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val contentDescription: String? = null
)

/**
 * 自适应导航栏组件
 */
@Composable
fun UnifyAdaptiveNavigationBar(
    items: List<UnifyNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyNavigationBarVariant = UnifyNavigationBarVariant.STANDARD,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    showLabels: Boolean = true,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    // 根据屏幕宽度决定导航栏类型
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 600.dp) {
            // 小屏幕使用底部导航
            UnifyBottomNavigationBar(
                items = items,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                variant = variant,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                selectedContentColor = selectedContentColor,
                showLabels = showLabels,
                contentDescription = contentDescription
            )
        } else {
            // 大屏幕使用侧边导航栏
            UnifyNavigationRail(
                items = items,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                variant = variant,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                selectedContentColor = selectedContentColor,
                showLabels = showLabels,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * 导航栏轨道组件
 */
@Composable
fun UnifyNavigationRail(
    items: List<UnifyNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyNavigationBarVariant = UnifyNavigationBarVariant.STANDARD,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    selectedContentColor: Color? = null,
    showLabels: Boolean = true,
    header: (@Composable () -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val navBarConfig = getNavigationBarConfig(variant, UnifyNavigationBarSize.STANDARD, theme)
    val actualBackgroundColor = backgroundColor ?: navBarConfig.backgroundColor
    val actualContentColor = contentColor ?: navBarConfig.contentColor
    val actualSelectedContentColor = selectedContentColor ?: theme.colors.primary
    
    NavigationRail(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        containerColor = actualBackgroundColor,
        contentColor = actualContentColor,
        header = header
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            
            NavigationRailItem(
                selected = isSelected,
                onClick = { 
                    if (item.enabled) {
                        onItemSelected(index)
                    }
                },
                icon = {
                    Box {
                        UnifyIcon(
                            imageVector = if (isSelected && item.selectedIcon != null) {
                                item.selectedIcon
                            } else {
                                item.icon
                            },
                            contentDescription = item.contentDescription,
                            size = UnifyIconSize.SMALL,
                            tint = if (isSelected) actualSelectedContentColor else actualContentColor
                        )
                        
                        // 徽章
                        item.badge?.let { badgeText ->
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
                },
                label = if (showLabels) {
                    {
                        UnifyText(
                            text = item.label,
                            variant = UnifyTextVariant.CAPTION,
                            color = if (isSelected) actualSelectedContentColor else actualContentColor
                        )
                    }
                } else null,
                enabled = item.enabled,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = actualSelectedContentColor,
                    selectedTextColor = actualSelectedContentColor,
                    unselectedIconColor = actualContentColor,
                    unselectedTextColor = actualContentColor,
                    indicatorColor = actualSelectedContentColor.copy(alpha = 0.12f)
                )
            )
        }
    }
}

/**
 * 获取导航栏配置
 */
@Composable
private fun getNavigationBarConfig(
    variant: UnifyNavigationBarVariant,
    size: UnifyNavigationBarSize,
    theme: com.unify.ui.theme.UnifyTheme
): NavigationBarConfig {
    val height = when (size) {
        UnifyNavigationBarSize.COMPACT -> 56.dp
        UnifyNavigationBarSize.STANDARD -> 64.dp
        UnifyNavigationBarSize.LARGE -> 72.dp
    }
    
    return when (variant) {
        UnifyNavigationBarVariant.STANDARD -> NavigationBarConfig(
            backgroundColor = theme.colors.surface,
            contentColor = theme.colors.onSurface,
            elevation = 3.dp,
            height = height
        )
        UnifyNavigationBarVariant.ELEVATED -> NavigationBarConfig(
            backgroundColor = theme.colors.surfaceVariant,
            contentColor = theme.colors.onSurfaceVariant,
            elevation = 6.dp,
            height = height
        )
        UnifyNavigationBarVariant.FILLED -> NavigationBarConfig(
            backgroundColor = theme.colors.primaryContainer,
            contentColor = theme.colors.onPrimaryContainer,
            elevation = 0.dp,
            height = height
        )
        UnifyNavigationBarVariant.TRANSPARENT -> NavigationBarConfig(
            backgroundColor = Color.Transparent,
            contentColor = theme.colors.onSurface,
            elevation = 0.dp,
            height = height
        )
    }
}

/**
 * 导航栏配置数据类
 */
private data class NavigationBarConfig(
    val backgroundColor: Color,
    val contentColor: Color,
    val elevation: Dp,
    val height: Dp
)
