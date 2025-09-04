package com.unify.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Unify对话框组件
 * 支持多种对话框类型和自定义样式
 */
@Composable
fun UnifyDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    type: DialogType = DialogType.STANDARD,
    properties: DialogProperties = DialogProperties(),
    modifier: Modifier = Modifier
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = properties
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 标题
                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = getDialogTitleColor(type)
                        )
                    }
                    
                    // 内容
                    content?.invoke()
                    
                    // 按钮区域
                    if (confirmButton != null || dismissButton != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            dismissButton?.invoke()
                            confirmButton?.invoke()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 确认对话框
 */
@Composable
fun UnifyConfirmDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    type: DialogType = DialogType.WARNING
) {
    UnifyDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = title,
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = getDialogButtonColor(type)
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        type = type
    )
}

/**
 * 信息对话框
 */
@Composable
fun UnifyInfoDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    buttonText: String = "确定",
    type: DialogType = DialogType.INFO
) {
    UnifyDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = title,
        content = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = getDialogButtonColor(type)
                )
            ) {
                Text(buttonText)
            }
        },
        type = type
    )
}

/**
 * 输入对话框
 */
@Composable
fun UnifyInputDialog(
    visible: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    hint: String = "",
    initialValue: String = "",
    confirmText: String = "确认",
    dismissText: String = "取消"
) {
    var inputValue by remember(visible) { mutableStateOf(initialValue) }
    
    UnifyDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = title,
        content = {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                placeholder = { Text(hint) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(inputValue)
                    onDismiss()
                },
                enabled = inputValue.isNotBlank()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/**
 * 列表选择对话框
 */
@Composable
fun UnifyListDialog(
    visible: Boolean,
    onItemSelected: (Int, String) -> Unit,
    onDismiss: () -> Unit,
    title: String,
    items: List<String>,
    selectedIndex: Int = -1
) {
    UnifyDialog(
        visible = visible,
        onDismiss = onDismiss,
        title = title,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = {
                                onItemSelected(index, item)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 自定义对话框
 */
@Composable
fun UnifyCustomDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = properties
        ) {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                content()
            }
        }
    }
}

/**
 * 全屏对话框
 */
@Composable
fun UnifyFullScreenDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    // 顶部栏
                    if (title != null || actions != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                title?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                actions?.invoke(this)
                            }
                        }
                    }
                    
                    // 内容区域
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

// 辅助函数和枚举

enum class DialogType {
    STANDARD,
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

@Composable
private fun getDialogTitleColor(type: DialogType): Color {
    return when (type) {
        DialogType.SUCCESS -> Color(0xFF4CAF50)
        DialogType.WARNING -> Color(0xFFFF9800)
        DialogType.ERROR -> Color(0xFFF44336)
        DialogType.INFO -> Color(0xFF2196F3)
        DialogType.STANDARD -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun getDialogButtonColor(type: DialogType): Color {
    return when (type) {
        DialogType.SUCCESS -> Color(0xFF4CAF50)
        DialogType.WARNING -> Color(0xFFFF9800)
        DialogType.ERROR -> Color(0xFFF44336)
        DialogType.INFO -> Color(0xFF2196F3)
        DialogType.STANDARD -> MaterialTheme.colorScheme.primary
    }
}
