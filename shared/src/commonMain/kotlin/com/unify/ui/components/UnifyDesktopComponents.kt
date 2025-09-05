package com.unify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unify桌面端组件
 * 提供桌面应用特有的UI组件和交互
 */

/**
 * 桌面窗口标题栏
 */
@Composable
fun UnifyDesktopTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 标题
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        // 窗口控制按钮
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            UnifyWindowButton(
                text = "−",
                color = Color(0xFFFFBD2E),
                onClick = onMinimize
            )
            UnifyWindowButton(
                text = "□",
                color = Color(0xFF28CA42),
                onClick = onMaximize
            )
            UnifyWindowButton(
                text = "×",
                color = Color(0xFFFF5F57),
                onClick = onClose
            )
        }
    }
}

/**
 * 窗口控制按钮
 */
@Composable
private fun UnifyWindowButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .background(color, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}

/**
 * 桌面菜单栏
 */
@Composable
fun UnifyDesktopMenuBar(
    menus: List<DesktopMenu>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        menus.forEach { menu ->
            var expanded by remember { mutableStateOf(false) }
            
            Box {
                TextButton(
                    onClick = { expanded = true }
                ) {
                    Text(menu.title)
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    menu.items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.title) },
                            onClick = {
                                item.onClick()
                                expanded = false
                            }
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
    tools: List<DesktopTool>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tools.forEach { tool ->
            IconButton(
                onClick = tool.onClick,
                enabled = tool.enabled
            ) {
                Text(
                    text = tool.icon,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 桌面状态栏
 */
@Composable
fun UnifyDesktopStatusBar(
    status: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        actions()
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
    width: Dp = 200.dp
) {
    Column(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedItem
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemSelected(item.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.icon,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
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
    splitRatio: Float = 0.5f
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(splitRatio)
        ) {
            leftContent()
        }
        
        VerticalDivider(
            modifier = Modifier.width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        Box(
            modifier = Modifier.weight(1f - splitRatio)
        ) {
            rightContent()
        }
    }
}

/**
 * 桌面标签页
 */
@Composable
fun UnifyDesktopTabPane(
    tabs: List<DesktopTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标签头部
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = { Text(tab.title) }
                )
            }
        }
        
        // 标签内容
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTabIndex in tabs.indices) {
                tabs[selectedTabIndex].content()
            }
        }
    }
}

/**
 * 桌面对话框
 */
@Composable
fun UnifyDesktopDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    buttons: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(content = content)
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = buttons
            )
        },
        modifier = modifier
    )
}

/**
 * 桌面通知
 */
@Composable
fun UnifyDesktopNotification(
    message: String,
    type: NotificationType = NotificationType.Info,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                NotificationType.Info -> MaterialTheme.colorScheme.primaryContainer
                NotificationType.Success -> Color(0xFF4CAF50)
                NotificationType.Warning -> Color(0xFFFF9800)
                NotificationType.Error -> Color(0xFFF44336)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (type) {
                    NotificationType.Info -> "ℹ️"
                    NotificationType.Success -> "✅"
                    NotificationType.Warning -> "⚠️"
                    NotificationType.Error -> "❌"
                },
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = Color.White
            )
            
            IconButton(onClick = onDismiss) {
                Text("×", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

/**
 * 桌面快捷键提示
 */
@Composable
fun UnifyDesktopShortcutHint(
    shortcuts: List<KeyboardShortcut>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shortcuts) { shortcut ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shortcut.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = shortcut.keys,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

// 数据类
data class DesktopMenu(
    val title: String,
    val items: List<MenuItem>
)

data class MenuItem(
    val title: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

data class DesktopTool(
    val icon: String,
    val tooltip: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

data class SidebarItem(
    val id: String,
    val title: String,
    val icon: String
)

data class DesktopTab(
    val title: String,
    val content: @Composable () -> Unit
)

data class KeyboardShortcut(
    val keys: String,
    val description: String
)

enum class NotificationType {
    Info, Success, Warning, Error
}

// 扩展函数
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(Modifier.padding(2.dp))
}
