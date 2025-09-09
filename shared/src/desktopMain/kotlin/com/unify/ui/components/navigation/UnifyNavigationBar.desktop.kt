package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop平台导航栏组件actual实现
 */

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
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
                titleContentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
            ),
    )
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
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.badge != null) {
                        BadgedBox(
                            badge = { Badge { Text(item.badge) } },
                        ) {
                            item.icon?.invoke()
                        }
                    } else {
                        item.icon?.invoke()
                    }
                },
                label = { Text(item.title) },
                selected = item.id == selectedItemId,
                onClick = { onItemSelected(item.id) },
                enabled = item.enabled,
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
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
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        header = header,
    ) {
        items.forEach { item ->
            NavigationRailItem(
                icon = {
                    if (item.badge != null) {
                        BadgedBox(
                            badge = { Badge { Text(item.badge) } },
                        ) {
                            item.icon?.invoke()
                        }
                    } else {
                        item.icon?.invoke()
                    }
                },
                label = { Text(item.title) },
                selected = item.id == selectedItemId,
                onClick = { onItemSelected(item.id) },
                enabled = item.enabled,
                colors =
                    NavigationRailItemDefaults.colors(
                        selectedIconColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
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
    // Desktop平台使用简化的导航栏项实现
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp),
        ) {
            if (selected && selectedIcon != null) {
                selectedIcon()
            } else {
                icon()
            }

            if (alwaysShowLabel && label != null) {
                label()
            }

            badge?.invoke()
        }
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
        selectedContentColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.primary,
        unselectedContentColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
