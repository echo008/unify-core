package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Desktop平台UI适配器
 * 提供桌面端特有的UI组件和交互方式
 */
object DesktopUnifyPlatformAdapters {
    
    /**
     * Desktop平台对话框
     */
    @Composable
    fun DesktopDialog(
        onDismissRequest: () -> Unit,
        title: String,
        content: @Composable () -> Unit,
        confirmButton: @Composable () -> Unit,
        dismissButton: @Composable (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 标题
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 内容
                    content()
                    
                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButton?.let { dismiss ->
                            dismiss()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        confirmButton()
                    }
                }
            }
        }
    }
    
    /**
     * Desktop平台工具提示
     */
    @Composable
    fun DesktopTooltip(
        text: String,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        // Desktop平台可以使用鼠标悬停显示工具提示
        // 这里是简化实现
        Box(modifier = modifier) {
            content()
        }
    }
    
    /**
     * Desktop平台上下文菜单
     */
    @Composable
    fun DesktopContextMenu(
        items: List<ContextMenuItem>,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.text) },
                        onClick = {
                            item.onClick()
                            onDismiss()
                        },
                        enabled = item.enabled,
                        leadingIcon = item.icon?.let { icon ->
                            { Icon(imageVector = icon, contentDescription = null) }
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Desktop平台窗口控制栏
     */
    @Composable
    fun DesktopWindowControls(
        onMinimize: () -> Unit,
        onMaximize: () -> Unit,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 最小化按钮
            IconButton(
                onClick = onMinimize,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Minimize,
                    contentDescription = "Minimize",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 最大化按钮
            IconButton(
                onClick = onMaximize,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CropFree,
                    contentDescription = "Maximize",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // 关闭按钮
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    /**
     * Desktop平台菜单栏
     */
    @Composable
    fun DesktopMenuBar(
        menus: List<MenuBarItem>,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            menus.forEach { menu ->
                TextButton(
                    onClick = { menu.onClick() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = menu.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    /**
     * Desktop平台状态栏
     */
    @Composable
    fun DesktopStatusBar(
        leftContent: @Composable RowScope.() -> Unit = {},
        centerContent: @Composable RowScope.() -> Unit = {},
        rightContent: @Composable RowScope.() -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧内容
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leftContent()
                }
                
                // 中间内容
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    centerContent()
                }
                
                // 右侧内容
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rightContent()
                }
            }
        }
    }
    
    /**
     * Desktop平台工具栏
     */
    @Composable
    fun DesktopToolbar(
        title: String,
        actions: List<ToolbarAction> = emptyList(),
        navigationIcon: (@Composable () -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 导航图标
                navigationIcon?.let { icon ->
                    icon()
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { action ->
                        IconButton(
                            onClick = action.onClick,
                            enabled = action.enabled
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                                tint = if (action.enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Desktop平台分割面板
     */
    @Composable
    fun DesktopSplitPane(
        leftContent: @Composable () -> Unit,
        rightContent: @Composable () -> Unit,
        splitRatio: Float = 0.5f,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxSize()
        ) {
            // 左侧面板
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(splitRatio)
            ) {
                leftContent()
            }
            
            // 分割线
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline
            )
            
            // 右侧面板
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f - splitRatio)
            ) {
                rightContent()
            }
        }
    }
}

/**
 * 上下文菜单项
 */
data class ContextMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

/**
 * 菜单栏项
 */
data class MenuBarItem(
    val text: String,
    val onClick: () -> Unit
)

/**
 * 工具栏操作
 */
data class ToolbarAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)
