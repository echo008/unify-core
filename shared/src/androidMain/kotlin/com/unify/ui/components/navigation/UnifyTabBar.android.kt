@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }
    
    if (mode == TabMode.SCROLLABLE) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
            contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTabId == tab.id,
                    onClick = { onTabSelected(tab.id) },
                    text = { Text(tab.title) },
                    icon = tab.icon,
                    enabled = tab.enabled
                )
            }
        }
    } else {
        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
            contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTabId == tab.id,
                    onClick = { onTabSelected(tab.id) },
                    text = { Text(tab.title) },
                    icon = tab.icon,
                    enabled = tab.enabled
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
    tabs: @Composable () -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        edgePadding = edgePadding,
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
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
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
        text = { Text(tab.title) },
        icon = tab.icon,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
actual fun UnifyTabIndicator(
    modifier: Modifier,
    color: Color,
    height: Dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(if (color != Color.Unspecified) color else MaterialTheme.colorScheme.primary)
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

