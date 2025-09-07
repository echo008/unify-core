package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue as MaterialDrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.components.navigation.DrawerValue

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
    val state = rememberDrawerState(initialValue = when(drawerState) {
        DrawerValue.Open -> MaterialDrawerValue.Open
        DrawerValue.Closed -> MaterialDrawerValue.Closed
    })
    ModalNavigationDrawer(
        drawerContent = { ModalDrawerSheet { drawerContent() } },
        modifier = modifier,
        drawerState = state,
        gesturesEnabled = gesturesEnabled,
        scrimColor = scrimColor,
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
    val state = rememberDrawerState(initialValue = when(drawerState) {
        DrawerValue.Open -> MaterialDrawerValue.Open
        DrawerValue.Closed -> MaterialDrawerValue.Closed
    })
    ModalNavigationDrawer(
        drawerContent = { ModalDrawerSheet { drawerContent() } },
        modifier = modifier,
        drawerState = state,
        gesturesEnabled = gesturesEnabled,
        scrimColor = scrimColor,
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
        drawerContent = { PermanentDrawerSheet { drawerContent() } },
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
    val state = rememberDrawerState(initialValue = when(drawerState) {
        DrawerValue.Open -> MaterialDrawerValue.Open
        DrawerValue.Closed -> MaterialDrawerValue.Closed
    })
    ModalNavigationDrawer(
        drawerContent = { ModalDrawerSheet { drawerContent() } },
        modifier = modifier,
        drawerState = state,
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
        { Text(item.title) },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = item.icon,
        badge = if (item.badge != null) { { Text(item.badge!!) } } else null
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
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        avatar?.invoke()
        Text(
            title,
            style = MaterialTheme.typography.titleMedium
        )
        subtitle?.let { subtitleText ->
            Text(
                subtitleText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        background?.invoke()
    }
}
