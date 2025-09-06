@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyTopAppBar(
    title: String,
    modifier: Modifier,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    elevation: Dp
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
            titleContentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface
        )
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
    unselectedContentColor: Color
) {
    NavigationBar(
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val isSelected = selectedItemId == item.id
            Button(
                onClick = { onItemSelected(item.id) },
                modifier = Modifier.weight(1f),
                enabled = item.enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Column {
                    item.icon?.invoke()
                    Text(item.title)
                    item.badge?.let { badge ->
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
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
    unselectedContentColor: Color
) {
    NavigationRail(
        modifier = modifier,
        header = header,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = selectedItemId == item.id,
                onClick = { onItemSelected(item.id) },
                icon = { item.icon?.invoke() },
                label = { Text(item.title) },
                enabled = item.enabled,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    badge: (@Composable () -> Unit)?
) {
    // Simplified NavigationBarItem implementation
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
        )
    ) {
        Column {
            if (selected && selectedIcon != null) {
                selectedIcon()
            } else {
                icon()
            }
            if (label != null && (alwaysShowLabel || selected)) {
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
actual fun UnifyTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: (@Composable () -> Unit)?,
    icon: (@Composable () -> Unit)?,
    selectedContentColor: Color,
    unselectedContentColor: Color
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = text,
        icon = icon,
        modifier = modifier,
        enabled = enabled
    )
}

