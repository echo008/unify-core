package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台抽屉导航组件
 * 支持侧滑抽屉、模态抽屉等功能
 */

enum class DrawerValue {
    Closed, Open
}

enum class DrawerType {
    MODAL, PERMANENT, DISMISSIBLE
}

data class DrawerItem(
    val id: String,
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
    val children: List<DrawerItem> = emptyList()
)

@Composable
expect fun UnifyDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerValue = DrawerValue.Closed,
    onDrawerStateChange: (DrawerValue) -> Unit = {},
    gesturesEnabled: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.32f),
    content: @Composable () -> Unit
)

@Composable
expect fun UnifyModalDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerValue = DrawerValue.Closed,
    onDrawerStateChange: (DrawerValue) -> Unit = {},
    gesturesEnabled: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.32f),
    drawerWidth: Dp = 280.dp,
    content: @Composable () -> Unit
)

@Composable
expect fun UnifyPermanentDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 280.dp,
    content: @Composable () -> Unit
)

@Composable
expect fun UnifyDismissibleDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerValue = DrawerValue.Closed,
    onDrawerStateChange: (DrawerValue) -> Unit = {},
    gesturesEnabled: Boolean = true,
    drawerWidth: Dp = 280.dp,
    content: @Composable () -> Unit
)

@Composable
expect fun UnifyDrawerItem(
    item: DrawerItem,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    colors: DrawerItemColors = DrawerItemDefaults.colors()
)

@Composable
expect fun UnifyDrawerHeader(
    title: String,
    subtitle: String? = null,
    avatar: (@Composable () -> Unit)? = null,
    background: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)

object DrawerItemDefaults {
    @Composable
    fun colors(
        selectedContainerColor: Color = Color.Unspecified,
        unselectedContainerColor: Color = Color.Transparent,
        selectedContentColor: Color = Color.Unspecified,
        unselectedContentColor: Color = Color.Unspecified,
        selectedIconColor: Color = Color.Unspecified,
        unselectedIconColor: Color = Color.Unspecified
    ): DrawerItemColors = DrawerItemColors(
        selectedContainerColor = selectedContainerColor,
        unselectedContainerColor = unselectedContainerColor,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
        selectedIconColor = selectedIconColor,
        unselectedIconColor = unselectedIconColor
    )
}

data class DrawerItemColors(
    val selectedContainerColor: Color,
    val unselectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val selectedIconColor: Color,
    val unselectedIconColor: Color
)
