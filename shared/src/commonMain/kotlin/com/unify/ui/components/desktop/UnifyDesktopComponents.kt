package com.unify.ui.components.desktop

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * 桌面窗口状态
 */
enum class UnifyWindowState {
    NORMAL,     // 正常
    MINIMIZED,  // 最小化
    MAXIMIZED,  // 最大化
    FULLSCREEN  // 全屏
}

/**
 * 桌面窗口配置
 */
data class UnifyWindowConfig(
    val title: String = "Unify App",
    val width: Int = 800,
    val height: Int = 600,
    val minWidth: Int = 400,
    val minHeight: Int = 300,
    val resizable: Boolean = true,
    val alwaysOnTop: Boolean = false,
    val showInTaskbar: Boolean = true,
    val icon: String? = null
)

/**
 * 系统托盘配置
 */
data class UnifySystemTrayConfig(
    val icon: String,
    val tooltip: String = "Unify App",
    val menuItems: List<Pair<String, () -> Unit>> = emptyList()
)

/**
 * 桌面窗口控制组件
 */
@Composable
fun UnifyDesktopWindow(
    config: UnifyWindowConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyWindowState) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    var windowState by remember { mutableStateOf(UnifyWindowState.NORMAL) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        // 自定义标题栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = theme.colors.surfaceVariant
            ),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    config.icon?.let {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = theme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    UnifyText(
                        text = config.title,
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 最小化按钮
                    IconButton(
                        onClick = {
                            windowState = UnifyWindowState.MINIMIZED
                            onStateChange?.invoke(windowState)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Minimize,
                            contentDescription = "最小化",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // 最大化/还原按钮
                    IconButton(
                        onClick = {
                            windowState = if (windowState == UnifyWindowState.MAXIMIZED) {
                                UnifyWindowState.NORMAL
                            } else {
                                UnifyWindowState.MAXIMIZED
                            }
                            onStateChange?.invoke(windowState)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (windowState == UnifyWindowState.MAXIMIZED) {
                                Icons.Default.FilterNone
                            } else {
                                Icons.Default.CropSquare
                            },
                            contentDescription = if (windowState == UnifyWindowState.MAXIMIZED) "还原" else "最大化",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // 关闭按钮
                    IconButton(
                        onClick = { onClose?.invoke() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        // 窗口内容
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.colors.background)
        ) {
            content()
        }
    }
}

/**
 * 系统托盘组件
 */
@Composable
fun UnifySystemTray(
    config: UnifySystemTrayConfig,
    modifier: Modifier = Modifier,
    onTrayClick: (() -> Unit)? = null,
    onMenuItemClick: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var showMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        // 托盘图标
        IconButton(
            onClick = {
                onTrayClick?.invoke()
                showMenu = !showMenu
            }
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = config.tooltip,
                tint = theme.colors.primary
            )
        }
        
        // 托盘菜单
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            config.menuItems.forEach { (itemText, action) ->
                DropdownMenuItem(
                    text = {
                        UnifyText(
                            text = itemText,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    },
                    onClick = {
                        action()
                        onMenuItemClick?.invoke(itemText)
                        showMenu = false
                    }
                )
            }
        }
    }
}

/**
 * 桌面通知组件
 */
@Composable
fun UnifyDesktopNotification(
    title: String,
    message: String,
    type: String = "info", // "info", "success", "warning", "error"
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
    actionText: String? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) // 5秒后自动消失
        visible = false
        onDismiss?.invoke()
    }
    
    if (visible) {
        Card(
            modifier = modifier
                .width(320.dp)
                .semantics {
                    contentDescription?.let { this.contentDescription = it }
                },
            colors = CardDefaults.cardColors(
                containerColor = when (type) {
                    "success" -> Color.Green.copy(alpha = 0.1f)
                    "warning" -> Color.Orange.copy(alpha = 0.1f)
                    "error" -> Color.Red.copy(alpha = 0.1f)
                    else -> theme.colors.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = when (type) {
                        "success" -> Icons.Default.CheckCircle
                        "warning" -> Icons.Default.Warning
                        "error" -> Icons.Default.Error
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (type) {
                        "success" -> Color.Green
                        "warning" -> Color.Orange
                        "error" -> Color.Red
                        else -> theme.colors.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    UnifyText(
                        text = message,
                        variant = UnifyTextVariant.BODY_SMALL,
                        color = theme.colors.onSurfaceVariant
                    )
                    
                    if (actionText != null && onAction != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onAction,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            UnifyText(
                                text = actionText,
                                variant = UnifyTextVariant.BODY_SMALL,
                                color = theme.colors.primary
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = {
                        visible = false
                        onDismiss?.invoke()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 桌面文件拖拽区域组件
 */
@Composable
fun UnifyDesktopDropZone(
    modifier: Modifier = Modifier,
    acceptedTypes: List<String> = listOf("*/*"),
    onFilesDropped: ((List<String>) -> Unit)? = null,
    onDragEnter: (() -> Unit)? = null,
    onDragLeave: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable (isDragging: Boolean) -> Unit
) {
    val theme = LocalUnifyTheme.current
    var isDragging by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(
                width = if (isDragging) 2.dp else 1.dp,
                color = if (isDragging) theme.colors.primary else theme.colors.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isDragging) 
                    theme.colors.primaryContainer.copy(alpha = 0.1f) 
                else 
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        content(isDragging)
        
        if (isDragging) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        theme.colors.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = theme.colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifyText(
                        text = "释放以上传文件",
                        variant = UnifyTextVariant.BODY_LARGE,
                        color = theme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 桌面菜单栏组件
 */
@Composable
fun UnifyDesktopMenuBar(
    menuItems: List<UnifyMenuGroup>,
    modifier: Modifier = Modifier,
    onMenuItemClick: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surfaceVariant
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            menuItems.forEach { group ->
                var expanded by remember { mutableStateOf(false) }
                
                Box {
                    TextButton(
                        onClick = { expanded = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        UnifyText(
                            text = group.title,
                            variant = UnifyTextVariant.BODY_SMALL
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        group.items.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        item.icon?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        UnifyText(
                                            text = item.text,
                                            variant = UnifyTextVariant.BODY_SMALL
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        item.shortcut?.let { shortcut ->
                                            UnifyText(
                                                text = shortcut,
                                                variant = UnifyTextVariant.CAPTION,
                                                color = theme.colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    item.action()
                                    onMenuItemClick?.invoke(item.text)
                                    expanded = false
                                },
                                enabled = item.enabled
                            )
                            
                            if (item.hasDivider) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 菜单组
 */
data class UnifyMenuGroup(
    val title: String,
    val items: List<UnifyMenuItem>
)

/**
 * 菜单项
 */
data class UnifyMenuItem(
    val text: String,
    val action: () -> Unit,
    val icon: ImageVector? = null,
    val shortcut: String? = null,
    val enabled: Boolean = true,
    val hasDivider: Boolean = false
)

/**
 * 桌面工具栏组件
 */
@Composable
fun UnifyDesktopToolbar(
    tools: List<UnifyToolbarItem>,
    modifier: Modifier = Modifier,
    onToolClick: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tools.forEach { tool ->
                when (tool.type) {
                    "button" -> {
                        IconButton(
                            onClick = {
                                tool.action()
                                onToolClick?.invoke(tool.id)
                            },
                            enabled = tool.enabled
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.tooltip,
                                tint = if (tool.enabled) theme.colors.onSurface else Color.Gray
                            )
                        }
                    }
                    
                    "toggle" -> {
                        var isToggled by remember { mutableStateOf(tool.isToggled) }
                        
                        IconToggleButton(
                            checked = isToggled,
                            onCheckedChange = { 
                                isToggled = it
                                tool.action()
                                onToolClick?.invoke(tool.id)
                            },
                            enabled = tool.enabled
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.tooltip,
                                tint = if (isToggled) theme.colors.primary else theme.colors.onSurface
                            )
                        }
                    }
                    
                    "separator" -> {
                        VerticalDivider(
                            modifier = Modifier.height(24.dp),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 工具栏项
 */
data class UnifyToolbarItem(
    val id: String,
    val type: String, // "button", "toggle", "separator"
    val icon: ImageVector,
    val tooltip: String,
    val action: () -> Unit,
    val enabled: Boolean = true,
    val isToggled: Boolean = false
)

/**
 * 桌面状态栏组件
 */
@Composable
fun UnifyDesktopStatusBar(
    statusItems: List<UnifyStatusItem>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surfaceVariant
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧状态项
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusItems.filter { it.position == "left" }.forEach { item ->
                    StatusItemView(item = item)
                }
            }
            
            // 右侧状态项
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusItems.filter { it.position == "right" }.forEach { item ->
                    StatusItemView(item = item)
                }
            }
        }
    }
}

/**
 * 状态项
 */
data class UnifyStatusItem(
    val id: String,
    val text: String,
    val icon: ImageVector? = null,
    val position: String = "left", // "left", "right"
    val clickable: Boolean = false,
    val action: (() -> Unit)? = null
)

/**
 * 状态项视图
 */
@Composable
private fun StatusItemView(
    item: UnifyStatusItem,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    Row(
        modifier = modifier
            .then(
                if (item.clickable) {
                    Modifier.clickable { item.action?.invoke() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = theme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        
        UnifyText(
            text = item.text,
            variant = UnifyTextVariant.CAPTION,
            color = theme.colors.onSurfaceVariant
        )
    }
}

/**
 * 桌面快捷键管理组件
 */
@Composable
fun UnifyDesktopShortcuts(
    shortcuts: Map<String, () -> Unit>,
    modifier: Modifier = Modifier,
    onShortcutTriggered: ((String) -> Unit)? = null
) {
    // 这里可以集成键盘事件监听
    LaunchedEffect(shortcuts) {
        // 注册快捷键监听
        shortcuts.forEach { (shortcut, action) ->
            // 模拟快捷键注册
        }
    }
}

/**
 * 桌面多窗口管理组件
 */
@Composable
fun UnifyDesktopWindowManager(
    windows: List<UnifyWindowInfo>,
    modifier: Modifier = Modifier,
    onWindowSelect: ((String) -> Unit)? = null,
    onWindowClose: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            UnifyText(
                text = "窗口管理",
                variant = UnifyTextVariant.H6,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(windows.size) { index ->
                    val window = windows[index]
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWindowSelect?.invoke(window.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (window.isActive) 
                                theme.colors.primaryContainer 
                            else 
                                theme.colors.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = window.icon,
                                contentDescription = null,
                                tint = if (window.isActive) theme.colors.primary else theme.colors.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                UnifyText(
                                    text = window.title,
                                    variant = UnifyTextVariant.BODY_MEDIUM,
                                    fontWeight = if (window.isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                UnifyText(
                                    text = "${window.state} • ${window.width}x${window.height}",
                                    variant = UnifyTextVariant.CAPTION,
                                    color = theme.colors.onSurfaceVariant
                                )
                            }
                            
                            IconButton(
                                onClick = { onWindowClose?.invoke(window.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭窗口",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 窗口信息
 */
data class UnifyWindowInfo(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val state: String, // "normal", "minimized", "maximized"
    val width: Int,
    val height: Int,
    val isActive: Boolean = false
)
