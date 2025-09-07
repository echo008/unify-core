package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

// Type conversion function
private fun androidx.compose.material3.TabPosition.toUnifyTabPosition(): TabPosition {
    return TabPosition(
        left = this.left,
        width = this.width
    )
}

@Composable
actual fun UnifyScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
    edgePadding: androidx.compose.ui.unit.Dp,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
    divider: @Composable () -> Unit,
    tabs: @Composable () -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        indicator = { tabPositions -> indicator(tabPositions.map { it.toUnifyTabPosition() }) },
        divider = divider,
        tabs = tabs
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
    tabs: @Composable () -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        indicator = { tabPositions -> indicator(tabPositions.map { it.toUnifyTabPosition() }) },
        divider = divider,
        tabs = tabs
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
    onClose: (() -> Unit)?
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = { Text(tab.title) },
        icon = tab.icon,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor
    )
}

@Composable
actual fun UnifyTabIndicator(
    modifier: Modifier,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}

@Composable
actual fun UnifyTabBadge(
    text: String,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color
) {
    Badge(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor
    ) {
        Text(text)
    }
}

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
    onTabClosed: ((String) -> Unit)?
) {
    val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.takeIf { it >= 0 } ?: 0
    
    if (mode == TabMode.SCROLLABLE) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = backgroundColor,
            contentColor = contentColor
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab.id == selectedTabId,
                    onClick = { onTabSelected(tab.id) },
                    text = { Text(tab.title) },
                    icon = tab.icon,
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor
                )
            }
        }
    } else {
        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = backgroundColor,
            contentColor = contentColor
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab.id == selectedTabId,
                    onClick = { onTabSelected(tab.id) },
                    text = { Text(tab.title) },
                    icon = tab.icon,
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor
                )
            }
        }
    }
}
