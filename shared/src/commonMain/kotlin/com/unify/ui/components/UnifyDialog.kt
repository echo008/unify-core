package com.unify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Unify对话框组件
 * 提供多种类型的对话框和弹窗
 */

/**
 * 基础对话框
 */
@Composable
fun UnifyDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "确定",
    cancelText: String = "取消",
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            }
        },
        dismissButton = {
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text(cancelText)
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * 确认对话框
 */
@Composable
fun UnifyConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "确定",
    cancelText: String = "取消",
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isDestructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    ),
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text(cancelText)
            }
        },
        modifier = modifier,
    )
}

/**
 * 输入对话框
 */
@Composable
fun UnifyInputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    maxLines: Int = 1,
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholder) },
                    maxLines = maxLines,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank(),
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("取消")
            }
        },
        modifier = modifier,
    )
}

/**
 * 选择对话框
 */
@Composable
fun UnifySelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int = -1,
    onSelect: (Int, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedOption by remember { mutableStateOf(selectedIndex) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = index }
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedOption == index,
                            onClick = { selectedOption = index },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedOption >= 0) {
                        onSelect(selectedOption, options[selectedOption])
                    }
                },
                enabled = selectedOption >= 0,
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("取消")
            }
        },
        modifier = modifier,
    )
}

/**
 * 多选对话框
 */
@Composable
fun UnifyMultiSelectionDialog(
    title: String,
    options: List<String>,
    selectedIndices: Set<Int> = emptySet(),
    onConfirm: (Set<Int>, List<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedOptions by remember { mutableStateOf(selectedIndices) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOptions =
                                        if (index in selectedOptions) {
                                            selectedOptions - index
                                        } else {
                                            selectedOptions + index
                                        }
                                }
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = index in selectedOptions,
                            onCheckedChange = { checked ->
                                selectedOptions =
                                    if (checked) {
                                        selectedOptions + index
                                    } else {
                                        selectedOptions - index
                                    }
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedTexts = selectedOptions.map { options[it] }
                    onConfirm(selectedOptions, selectedTexts)
                },
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("取消")
            }
        },
        modifier = modifier,
    )
}

/**
 * 加载对话框
 */
@Composable
fun UnifyLoadingDialog(
    message: String = "加载中...",
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties =
            DialogProperties(
                dismissOnBackPress = onDismiss != null,
                dismissOnClickOutside = false,
            ),
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/**
 * 进度对话框
 */
@Composable
fun UnifyProgressDialog(
    title: String,
    progress: Float,
    message: String = "",
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = { onCancel?.invoke() },
        properties =
            DialogProperties(
                dismissOnBackPress = onCancel != null,
                dismissOnClickOutside = false,
            ),
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (onCancel != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OutlinedButton(onClick = onCancel) {
                            Text("取消")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 自定义对话框
 */
@Composable
fun UnifyCustomDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties,
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            content()
        }
    }
}

/**
 * 全屏对话框
 */
@Composable
fun UnifyFullScreenDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            // 标题栏
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Text("←", fontSize = 20.sp)
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                actions()
            }

            HorizontalDivider()

            // 内容区域
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                content = content,
            )
        }
    }
}

/**
 * 底部弹窗对话框
 */
@Composable
fun UnifyBottomSheetDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    )
                    .padding(16.dp),
        ) {
            // 拖拽指示器
            Box(
                modifier =
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            RoundedCornerShape(2.dp),
                        )
                        .align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

// 扩展函数
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(Modifier.padding(4.dp))
}
