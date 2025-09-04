package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台标签栏组件
 * 支持可滚动标签、固定标签等功能
 */

data class TabItem(
    val id: String,
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
    val closeable: Boolean = false
)

enum class TabMode {
    FIXED, SCROLLABLE
}

@Composable
expect fun UnifyTabBar(
    tabs: List<TabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    mode: TabMode = TabMode.FIXED,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
    indicatorColor: Color = Color.Unspecified,
    onTabClosed: ((String) -> Unit)? = null
)

@Composable
expect fun UnifyScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    edgePadding: Dp = 52.dp,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit
)

@Composable
expect fun UnifyFixedTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit
)

@Composable
expect fun UnifyTabBarItem(
    tab: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
    onClose: (() -> Unit)? = null
)

@Composable
expect fun UnifyTabIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    height: Dp = 2.dp
)

@Composable
expect fun UnifyTabBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Red,
    contentColor: Color = Color.White
)
