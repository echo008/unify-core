package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyTopAppBar(
    title: String,
    modifier: Modifier,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    elevation: Dp,
) {
    if (navigationIcon != null) {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                ),
        )
    } else {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            actions = actions,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                ),
        )
    }
}

@Composable
actual fun UnifyBottomNavigationBar(
    items: List<NavigationItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.id == selectedItemId,
                onClick = { onItemSelected(item.id) },
                icon = { item.icon?.invoke() ?: Text(item.title) },
                label = { Text(item.title) },
                enabled = item.enabled,
            )
        }
    }
}

@Composable
actual fun UnifyNavigationRail(
    items: List<NavigationItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier,
    header: (@Composable ColumnScope.() -> Unit)?,
    backgroundColor: Color,
    contentColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        header = header,
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = item.id == selectedItemId,
                onClick = { onItemSelected(item.id) },
                icon = { item.icon?.invoke() ?: Text(item.title) },
                label = { Text(item.title) },
                enabled = item.enabled,
            )
        }
    }
}

@Composable
actual fun UnifyNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    label: (@Composable () -> Unit)?,
    alwaysShowLabel: Boolean,
    selectedIcon: (@Composable () -> Unit)?,
    badge: (@Composable () -> Unit)?,
) {
    // iOS简化实现
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        if (selectedIcon != null && selected) {
            selectedIcon()
        } else {
            icon()
        }
        label?.let { it() }
        badge?.let { it() }
    }
}

@Composable
actual fun UnifyTabRow(
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
        containerColor = backgroundColor,
        contentColor = contentColor,
        indicator = { positions ->
            indicator(positions.map { TabPosition(it.left, it.width) })
        },
        divider = divider,
        tabs = tabs,
    )
}

@Composable
actual fun UnifyTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: (@Composable () -> Unit)?,
    icon: (@Composable () -> Unit)?,
    selectedContentColor: Color,
    unselectedContentColor: Color,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = text,
        icon = icon,
    )
}
