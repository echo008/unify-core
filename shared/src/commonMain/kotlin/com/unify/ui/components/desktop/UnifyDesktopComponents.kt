package com.unify.ui.components.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Unify桌面平台特定组件
 * 专为Desktop平台优化的UI组件
 */

@Composable
fun UnifyDesktopMenuBar(
    items: List<UnifyMenuBarItem>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                UnifyDesktopMenuBarItem(
                    item = item,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun UnifyDesktopMenuBarItem(
    item: UnifyMenuBarItem,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        TextButton(
            onClick = { 
                if (item.subItems.isNotEmpty()) {
                    expanded = !expanded
                } else {
                    item.onClick()
                }
            }
        ) {
            Text(item.title)
        }
        
        if (item.subItems.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                item.subItems.forEach { subItem ->
                    DropdownMenuItem(
                        text = { Text(subItem.title) },
                        onClick = {
                            subItem.onClick()
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

data class UnifyMenuBarItem(
    val title: String,
    val onClick: () -> Unit = {},
    val subItems: List<UnifyMenuBarItem> = emptyList()
)

@Composable
fun UnifyDesktopToolbar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon?.invoke()
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (navigationIcon != null) 16.dp else 0.dp)
            )
            
            actions()
        }
    }
}

@Composable
fun UnifyDesktopSidebar(
    items: List<UnifySidebarItem>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 240.dp
) {
    Surface(
        modifier = modifier
            .width(width)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items) { item ->
                UnifyDesktopSidebarItem(
                    item = item,
                    isSelected = item.id == selectedItem,
                    onClick = { onItemSelected(item.id) }
                )
            }
        }
    }
}

@Composable
private fun UnifyDesktopSidebarItem(
    item: UnifySidebarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.icon,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

data class UnifySidebarItem(
    val id: String,
    val title: String,
    val icon: String
)

@Composable
fun UnifyDesktopStatusBar(
    status: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            actions()
        }
    }
}

@Composable
fun UnifyDesktopWindow(
    title: String,
    modifier: Modifier = Modifier,
    menuBar: (@Composable () -> Unit)? = null,
    toolbar: (@Composable () -> Unit)? = null,
    sidebar: (@Composable () -> Unit)? = null,
    statusBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 菜单栏
        menuBar?.invoke()
        
        // 工具栏
        toolbar?.invoke()
        
        // 主内容区域
        Row(
            modifier = Modifier.weight(1f)
        ) {
            // 侧边栏
            sidebar?.invoke()
            
            // 主内容
            Box(
                modifier = Modifier.weight(1f)
            ) {
                content()
            }
        }
        
        // 状态栏
        statusBar?.invoke()
    }
}

@Composable
fun UnifyDesktopDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(content = content)
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        modifier = modifier
    )
}

@Composable
fun UnifyDesktopTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .hoverable(
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            content()
        }
        
        if (showTooltip) {
            Surface(
                modifier = Modifier.padding(8.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun UnifyDesktopSplitPane(
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    splitRatio: Float = 0.5f
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.weight(splitRatio)
        ) {
            leftContent()
        }
        
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        Box(
            modifier = Modifier.weight(1f - splitRatio)
        ) {
            rightContent()
        }
    }
}
