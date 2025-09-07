package com.unify.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    // JS implementation - simplified
    Row(modifier = modifier) {
        Column(
            modifier = Modifier.width(280.dp)
        ) {
            drawerContent()
        }
        Box {
            content()
        }
    }
}

@Composable
actual fun UnifyModalDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    scrimColor: Color,
    drawerWidth: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    // JS implementation - simplified
    Box(modifier = modifier) {
        content()
    }
}

@Composable
actual fun UnifyPermanentDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerWidth: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier.width(drawerWidth)
        ) {
            drawerContent()
        }
        Box {
            content()
        }
    }
}

@Composable
actual fun UnifyDismissibleDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    drawerWidth: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier.width(drawerWidth)
        ) {
            drawerContent()
        }
        Box {
            content()
        }
    }
}

@Composable
actual fun UnifyDrawerItem(
    item: DrawerItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    colors: DrawerItemColors
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        if (item.icon != null) {
            item.icon.invoke()
        }
        Text(item.title)
    }
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
        modifier = modifier.clickable { onClick() }
    ) {
        avatar?.invoke()
        Text(title)
        subtitle?.let { Text(it) }
    }
}
