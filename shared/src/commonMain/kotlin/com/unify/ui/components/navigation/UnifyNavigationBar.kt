package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台导航栏组件
 * 支持顶部导航栏、底部导航栏等功能
 */

data class NavigationItem(
    val id: String,
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
    val selectedIcon: (@Composable () -> Unit)? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
)

@Composable
expect fun UnifyTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 4.dp,
)

@Composable
expect fun UnifyBottomNavigationBar(
    items: List<NavigationItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
)

@Composable
expect fun UnifyNavigationRail(
    items: List<NavigationItem>,
    selectedItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
)

@Composable
expect fun UnifyNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: (@Composable () -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    selectedIcon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
)

@Composable
expect fun UnifyTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit,
)

@Composable
expect fun UnifyTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
)

data class TabPosition(
    val left: Dp,
    val width: Dp,
)
