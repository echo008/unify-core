package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

/**
 * Desktop平台标签栏组件actual实现
 */

// 使用commonMain中定义的TabPosition

@Composable
actual fun UnifyTabBar(
    tabs: List<TabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier,
    mode: TabMode,
    backgroundColor: Color,
    contentColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    indicatorColor: Color,
    onTabClosed: ((String) -> Unit)?,
) {
    val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.takeIf { it >= 0 } ?: 0

    if (mode == TabMode.SCROLLABLE) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
            contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty() && selectedIndex < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.offset(x = tabPositions[selectedIndex].left),
                        color = if (indicatorColor != Color.Unspecified) indicatorColor else MaterialTheme.colorScheme.primary,
                    )
                }
            },
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = tab.id == selectedTabId,
                    onClick = { onTabSelected(tab.id) },
                    enabled = tab.enabled,
                    text = { Text(tab.title) },
                    icon = tab.icon,
                    selectedContentColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
            contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                if (tabPositions.isNotEmpty() && selectedIndex < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.offset(x = tabPositions[selectedIndex].left),
                        color = if (indicatorColor != Color.Unspecified) indicatorColor else MaterialTheme.colorScheme.primary,
                    )
                }
            },
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = tab.id == selectedTabId,
                    onClick = { onTabSelected(tab.id) },
                    enabled = tab.enabled,
                    text = { Text(tab.title) },
                    icon = tab.icon,
                    selectedContentColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.primary,
                    unselectedContentColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
actual fun UnifyScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
    edgePadding: Dp,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
    divider: @Composable () -> Unit,
    tabs: @Composable () -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        edgePadding = edgePadding,
        indicator = { tabPositions ->
            indicator(tabPositions.map { TabPosition(it.left, it.width) })
        },
        divider = divider,
        tabs = tabs,
    )
}

@Composable
actual fun UnifyFixedTabRow(
    selectedTabIndex: Int,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
    divider: @Composable () -> Unit,
    tabs: @Composable () -> Unit,
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            indicator(tabPositions.map { TabPosition(it.left, it.width) })
        },
        divider = divider,
        tabs = tabs,
    )
}

@Composable
actual fun UnifyTabBarItem(
    tab: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    onClose: (() -> Unit)?,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            text = { Text(tab.title) },
            icon = tab.icon,
            selectedContentColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.primary,
            unselectedContentColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        if (tab.closeable && onClose != null) {
            IconButton(
                onClick = { onClose() },
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
actual fun UnifyTabIndicator(
    modifier: Modifier,
    color: Color,
    height: Dp,
) {
    TabRowDefaults.Indicator(
        modifier = modifier,
        height = height,
        color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.primary,
    )
}

@Composable
actual fun UnifyTabBadge(
    text: String,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
) {
    Badge(
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.error,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onError,
    ) {
        Text(text = text)
    }
}
