package com.unify.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue as MaterialDrawerValue

/**
 * Desktop平台抽屉导航组件actual实现
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    scrimColor: Color,
    content: @Composable () -> Unit,
) {
    val materialDrawerState =
        rememberDrawerState(
            initialValue = if (drawerState == DrawerValue.Open) MaterialDrawerValue.Open else MaterialDrawerValue.Closed,
        )

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                drawerContent()
            }
        },
        modifier = modifier,
        drawerState = materialDrawerState,
        gesturesEnabled = gesturesEnabled,
        scrimColor = scrimColor,
        content = content,
    )
}

@Composable
actual fun UnifyDrawerItem(
    item: DrawerItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    colors: DrawerItemColors,
) {
    NavigationDrawerItem(
        label = { Text(item.title) },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = item.icon,
        badge =
            if (item.badge != null) {
                { Text(item.badge) }
            } else {
                null
            },
        colors = NavigationDrawerItemDefaults.colors(),
    )
}

@Composable
actual fun UnifyDrawerHeader(
    title: String,
    subtitle: String?,
    avatar: (@Composable () -> Unit)?,
    background: (@Composable () -> Unit)?,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onClick() },
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box {
            background?.invoke()

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                avatar?.let {
                    it()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                subtitle?.let { sub ->
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

// 添加缺失的其他Drawer函数
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyModalDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    scrimColor: Color,
    drawerWidth: Dp,
    content: @Composable () -> Unit,
) {
    val materialDrawerState =
        rememberDrawerState(
            initialValue = if (drawerState == DrawerValue.Open) MaterialDrawerValue.Open else MaterialDrawerValue.Closed,
        )

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
            ) {
                drawerContent()
            }
        },
        modifier = modifier,
        drawerState = materialDrawerState,
        gesturesEnabled = gesturesEnabled,
        scrimColor = scrimColor,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyPermanentDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerWidth: Dp,
    content: @Composable () -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(drawerWidth),
            ) {
                drawerContent()
            }
        },
        modifier = modifier,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyDismissibleDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier,
    drawerState: DrawerValue,
    onDrawerStateChange: (DrawerValue) -> Unit,
    gesturesEnabled: Boolean,
    drawerWidth: Dp,
    content: @Composable () -> Unit,
) {
    val materialDrawerState =
        rememberDrawerState(
            initialValue = if (drawerState == DrawerValue.Open) MaterialDrawerValue.Open else MaterialDrawerValue.Closed,
        )

    DismissibleNavigationDrawer(
        drawerContent = {
            DismissibleDrawerSheet(
                modifier = Modifier.width(drawerWidth),
            ) {
                drawerContent()
            }
        },
        modifier = modifier,
        drawerState = materialDrawerState,
        gesturesEnabled = gesturesEnabled,
        content = content,
    )
}
