package com.unify.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyTopAppBar(
    title: String,
    modifier: Modifier,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    elevation: androidx.compose.ui.unit.Dp,
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = backgroundColor,
                titleContentColor = contentColor,
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
        containerColor = backgroundColor,
        contentColor = contentColor,
    ) {
        items.forEach { item ->
            Column(
                modifier =
                    Modifier
                        .clickable { onItemSelected(item.id) }
                        .padding(8.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                item.icon?.invoke()
                Text(
                    text = item.title,
                    color = if (item.id == selectedItemId) selectedContentColor else unselectedContentColor,
                )
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
    unselectedContentColor: Color,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        header = header?.let { { it() } },
    ) {
        items.forEach { item ->
            Column(
                modifier =
                    Modifier
                        .clickable { onItemSelected(item.id) }
                        .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item.icon?.invoke()
                Text(
                    text = item.title,
                    color = if (item.id == selectedItemId) selectedContentColor else unselectedContentColor,
                )
            }
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
    Column(
        modifier =
            modifier
                .clickable(enabled = enabled) { onClick() }
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (selectedIcon != null && selected) {
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
        containerColor = backgroundColor,
        contentColor = contentColor,
        indicator = { tabPositions -> indicator(tabPositions.map { TabPosition(it.left, it.width) }) },
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
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
    )
}
