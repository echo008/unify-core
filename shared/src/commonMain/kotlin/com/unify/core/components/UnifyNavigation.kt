package com.unify.core.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.unit.dp

/**
 * Unify统一导航组件
 * 100% Kotlin Compose语法实现
 */
data class UnifyNavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badge: String? = null,
    val badgeCount: Int? = null,
    val enabled: Boolean = true
)

@Composable
fun UnifyBottomNavigation(
    items: List<UnifyNavigationItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: androidx.compose.ui.unit.Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets
    ) {
        items.forEach { item ->
            UnifyNavigationBarItem(
                item = item,
                selected = selectedItemId == item.id,
                onClick = { onItemSelected(item.id) }
            )
        }
    }
}

@Composable
private fun RowScope.UnifyNavigationBarItem(
    item: UnifyNavigationItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            NavigationItemIcon(
                item = item,
                selected = selected
            )
        },
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        enabled = item.enabled,
        colors = NavigationBarItemDefaults.colors()
    )
}

@Composable
fun UnifyNavigationRail(
    items: List<UnifyNavigationItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationRailDefaults.ContainerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    header: (@Composable () -> Unit)? = null
) {
    NavigationRail(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        header = header?.let { { it() } }
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = selectedItemId == item.id,
                onClick = { onItemSelected(item.id) },
                icon = {
                    NavigationItemIcon(
                        item = item,
                        selected = selectedItemId == item.id
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                enabled = item.enabled,
                colors = NavigationRailItemDefaults.colors()
            )
        }
    }
}

@Composable
fun UnifyNavigationDrawerItem(
    item: UnifyNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors()
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = {
            NavigationItemIcon(
                item = item,
                selected = selected
            )
        },
        badge = if (item.badge != null || item.badgeCount != null) {
            {
                NavigationItemBadge(
                    badge = item.badge,
                    badgeCount = item.badgeCount
                )
            }
        } else null,
        colors = colors
    )
}

@Composable
private fun NavigationItemIcon(
    item: UnifyNavigationItem,
    selected: Boolean
) {
    val iconVector = if (selected && item.selectedIcon != null) {
        item.selectedIcon
    } else {
        item.icon
    }
    
    if (item.badge != null || item.badgeCount != null) {
        BadgedBox(
            badge = {
                NavigationItemBadge(
                    badge = item.badge,
                    badgeCount = item.badgeCount
                )
            }
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = item.label,
                modifier = Modifier.size(24.dp)
            )
        }
    } else {
        Icon(
            imageVector = iconVector,
            contentDescription = item.label,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun NavigationItemBadge(
    badge: String? = null,
    badgeCount: Int? = null
) {
    when {
        badge != null -> {
            Badge {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        badgeCount != null && badgeCount > 0 -> {
            Badge {
                Text(
                    text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        else -> {
            Badge()
        }
    }
}
