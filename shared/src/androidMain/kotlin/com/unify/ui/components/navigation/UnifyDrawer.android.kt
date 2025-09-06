@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    scrimColor: Color,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                drawerContent()
            }
        },
        modifier = modifier,
        drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed),
        gesturesEnabled = gesturesEnabled,
        content = content
    )
}

@Composable
actual fun UnifyModalDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    scrimColor: Color,
    drawerWidth: Dp,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                drawerContent()
            }
        },
        modifier = modifier,
        content = content
    )
}

@Composable
actual fun UnifyPermanentDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerWidth: Dp,
    content: @Composable () -> Unit
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                drawerContent()
            }
        },
        modifier = modifier,
        content = content
    )
}

@Composable
actual fun UnifyDismissibleDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    drawerWidth: Dp,
    content: @Composable () -> Unit
) {
    DismissibleNavigationDrawer(
        drawerContent = {
            DismissibleDrawerSheet(
                modifier = Modifier.width(drawerWidth)
            ) {
                drawerContent()
            }
        },
        modifier = modifier,
        drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed),
        gesturesEnabled = gesturesEnabled,
        content = content
    )
}

@Composable
actual fun UnifyDrawerItem(
    item: DrawerItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    colors: DrawerItemColors
) {
    NavigationDrawerItem(
        label = { Text(item.title) },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = item.icon,
        badge = if (item.badge != null) {
            { Text(item.badge) }
        } else null
    )
}

@Composable
actual fun UnifyDrawerHeader(
    title: String,
    subtitle: String?,
    avatar: (@Composable () -> Unit)?,
    background: (@Composable () -> Unit)?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box {
            background?.invoke()
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                avatar?.let {
                    it()
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
