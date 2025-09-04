package com.unify.ui.components.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.semantics.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Unify无障碍访问组件
 * 支持所有8大平台的统一无障碍体验
 */

@Composable
fun UnifyAccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    role: Role = Role.Button,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
            this.role = role
            if (!enabled) {
                disabled()
            }
        },
        enabled = enabled,
        content = content
    )
}

@Composable
fun UnifyAccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    isHeading: Boolean = false,
    headingLevel: Int = 1,
    isLiveRegion: Boolean = false,
    liveRegionMode: LiveRegionMode = LiveRegionMode.Polite
) {
    Text(
        text = text,
        modifier = modifier.semantics {
            if (isHeading) {
                heading()
                // 设置标题级别
                set(SemanticsProperties.Role, Role.Button) // 使用可用的语义属性
            }
            if (isLiveRegion) {
                liveRegion = liveRegionMode
            }
        }
    )
}

@Composable
fun UnifyAccessibleImage(
    contentDescription: String,
    modifier: Modifier = Modifier,
    isDecorative: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.semantics {
            if (isDecorative) {
                // 装饰性图片，屏幕阅读器应忽略
                invisibleToUser()
            } else {
                this.contentDescription = contentDescription
                role = Role.Image
            }
        }
    ) {
        content()
    }
}

@Composable
fun UnifyAccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    errorMessage: String? = null,
    helpText: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = errorMessage != null,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    if (isRequired) {
                        // 标记为必填字段
                        stateDescription = "必填"
                    }
                    if (errorMessage != null) {
                        error(errorMessage)
                    }
                    if (helpText != null) {
                        // 添加帮助文本
                        contentDescription = "$label. $helpText"
                    }
                }
        )
        
        if (helpText != null && errorMessage == null) {
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                    }
            )
        }
    }
}

@Composable
fun UnifyAccessibleCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Checkbox
                stateDescription = if (checked) "已选中" else "未选中"
                if (description != null) {
                    contentDescription = "$label. $description"
                }
                if (!enabled) {
                    disabled()
                }
            }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UnifyAccessibleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    valueFormatter: (Float) -> String = { "%.1f".format(it) }
) {
    Column(
        modifier = modifier.semantics {
            contentDescription = "$label: ${valueFormatter(value)}"
            if (!enabled) {
                disabled()
            }
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = Modifier.semantics {
                stateDescription = "当前值: ${valueFormatter(value)}"
            }
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = valueFormatter(valueRange.start),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valueFormatter(valueRange.endInclusive),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UnifyAccessibleCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    role: Role = Role.Button,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
            if (onClick != null) {
                this.role = role
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun UnifyAccessibleNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badge: String? = null
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = { Text(label) },
        enabled = enabled,
        modifier = modifier.semantics {
            role = Role.Tab
            stateDescription = if (selected) "已选中" else "未选中"
            contentDescription = if (badge != null) {
                "$label, 有 $badge 个通知"
            } else {
                label
            }
            if (!enabled) {
                disabled()
            }
        }
    )
}

@Composable
fun UnifyAccessibleProgress(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true
) {
    val progressPercentage = (progress * 100).toInt()
    
    Column(
        modifier = modifier.semantics {
            contentDescription = "$label: $progressPercentage%"
            stateDescription = "进度条"
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            if (showPercentage) {
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UnifyAccessibleAlert(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Assertive
                }
            )
        },
        text = {
            Text(
                text = message,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        modifier = modifier.semantics {
            role = Role.Dialog
        }
    )
}

enum class LiveRegionMode {
    Polite, Assertive
}
