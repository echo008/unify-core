package com.unify.platform.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Desktop平台特定组件
 * 提供桌面应用专用的UI组件和功能
 */

/**
 * 桌面窗口标题栏
 */
@Composable
fun UnifyDesktopTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    onMinimize: (() -> Unit)? = null,
    onMaximize: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    showWindowControls: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )

            // 窗口控制按钮
            if (showWindowControls) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    onMinimize?.let {
                        UnifyWindowControlButton(
                            icon = Icons.Default.Minimize,
                            onClick = it,
                            contentDescription = "最小化",
                        )
                    }

                    onMaximize?.let {
                        UnifyWindowControlButton(
                            icon = Icons.Default.CropSquare,
                            onClick = it,
                            contentDescription = "最大化",
                        )
                    }

                    onClose?.let {
                        UnifyWindowControlButton(
                            icon = Icons.Default.Close,
                            onClick = it,
                            contentDescription = "关闭",
                            isCloseButton = true,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 窗口控制按钮
 */
@Composable
private fun UnifyWindowControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    isCloseButton: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint =
                if (isCloseButton) {
                    Color.Red
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * 桌面菜单栏
 */
@Composable
fun UnifyDesktopMenuBar(
    menus: List<DesktopMenu>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            menus.forEach { menu ->
                UnifyDesktopMenuButton(menu = menu)
            }
        }
    }
}

/**
 * 菜单按钮
 */
@Composable
private fun UnifyDesktopMenuButton(menu: DesktopMenu) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        ) {
            Text(
                text = menu.title,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            menu.items.forEach { item ->
                when (item) {
                    is DesktopMenuItem.Action -> {
                        DropdownMenuItem(
                            text = { Text(item.title) },
                            onClick = {
                                item.onClick()
                                expanded = false
                            },
                            leadingIcon =
                                item.icon?.let { icon ->
                                    { Icon(icon, contentDescription = null) }
                                },
                            enabled = item.enabled,
                        )
                    }
                    is DesktopMenuItem.Separator -> {
                        Divider()
                    }
                    is DesktopMenuItem.Submenu -> {
                        // 子菜单实现
                        DropdownMenuItem(
                            text = { Text(item.title) },
                            onClick = { /* 展开子菜单 */ },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowRight, contentDescription = null)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 桌面工具栏
 */
@Composable
fun UnifyDesktopToolbar(
    actions: List<DesktopToolbarAction>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                when (action) {
                    is DesktopToolbarAction.Button -> {
                        IconButton(
                            onClick = action.onClick,
                            enabled = action.enabled,
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.tooltip,
                            )
                        }
                    }
                    is DesktopToolbarAction.Separator -> {
                        Divider(
                            modifier =
                                Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                        )
                    }
                    is DesktopToolbarAction.Group -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            action.actions.forEach { groupAction ->
                                IconButton(
                                    onClick = groupAction.onClick,
                                    enabled = groupAction.enabled,
                                ) {
                                    Icon(
                                        imageVector = groupAction.icon,
                                        contentDescription = groupAction.tooltip,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 桌面状态栏
 */
@Composable
fun UnifyDesktopStatusBar(
    leftContent: @Composable RowScope.() -> Unit = {},
    rightContent: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = leftContent,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = rightContent,
            )
        }
    }
}

/**
 * 桌面侧边栏
 */
@Composable
fun UnifyDesktopSidebar(
    items: List<SidebarItem>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 240.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Surface(
        modifier =
            modifier
                .width(width)
                .fillMaxHeight(),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { item ->
                when (item) {
                    is SidebarItem.Navigation -> {
                        val isSelected = selectedItem == item.id

                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onItemSelected(item.id) },
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.Transparent
                                },
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )

                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )
                            }
                        }
                    }
                    is SidebarItem.Header -> {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    is SidebarItem.Separator -> {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 桌面分割面板
 */
@Composable
fun UnifyDesktopSplitPane(
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    splitRatio: Float = 0.5f,
    minLeftWidth: androidx.compose.ui.unit.Dp = 200.dp,
    minRightWidth: androidx.compose.ui.unit.Dp = 200.dp,
    resizable: Boolean = true,
) {
    var currentSplitRatio by remember { mutableStateOf(splitRatio) }

    Row(
        modifier = modifier.fillMaxSize(),
    ) {
        // 左侧面板
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(currentSplitRatio),
        ) {
            leftContent()
        }

        // 分割线
        if (resizable) {
            Divider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(MaterialTheme.colorScheme.outline),
            )
        }

        // 右侧面板
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f - currentSplitRatio),
        ) {
            rightContent()
        }
    }
}

/**
 * 桌面标签页
 */
@Composable
fun UnifyDesktopTabBar(
    tabs: List<DesktopTab>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    onTabClosed: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.id == selectedTabId },
        modifier = modifier,
        edgePadding = 0.dp,
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab.id == selectedTabId,
                onClick = { onTabSelected(tab.id) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tab.icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Text(
                            text = tab.title,
                            maxLines = 1,
                        )

                        if (tab.closeable && onTabClosed != null) {
                            IconButton(
                                onClick = { onTabClosed(tab.id) },
                                modifier = Modifier.size(16.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

// 数据类定义

data class DesktopMenu(
    val title: String,
    val items: List<DesktopMenuItem>,
)

sealed class DesktopMenuItem {
    data class Action(
        val title: String,
        val onClick: () -> Unit,
        val icon: ImageVector? = null,
        val enabled: Boolean = true,
        val shortcut: String? = null,
    ) : DesktopMenuItem()

    object Separator : DesktopMenuItem()

    data class Submenu(
        val title: String,
        val items: List<DesktopMenuItem>,
    ) : DesktopMenuItem()
}

sealed class DesktopToolbarAction {
    data class Button(
        val icon: ImageVector,
        val onClick: () -> Unit,
        val tooltip: String,
        val enabled: Boolean = true,
    ) : DesktopToolbarAction()

    object Separator : DesktopToolbarAction()

    data class Group(
        val actions: List<Button>,
    ) : DesktopToolbarAction()
}

sealed class SidebarItem {
    data class Navigation(
        val id: String,
        val title: String,
        val icon: ImageVector,
    ) : SidebarItem()

    data class Header(
        val title: String,
    ) : SidebarItem()

    object Separator : SidebarItem()
}

data class DesktopTab(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val closeable: Boolean = true,
)
