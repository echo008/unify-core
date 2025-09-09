@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Unify跨平台对话框组件
 * 支持所有8大平台的统一对话框体验
 */

@Composable
fun UnifyDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            content()
        }
    }
}

@Composable
fun UnifyAlertDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    text: String? = null,
    icon: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    textContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    iconContentColor: Color = MaterialTheme.colorScheme.secondary,
    tonalElevation: androidx.compose.ui.unit.Dp = 6.dp,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title?.let { { Text(it, fontWeight = FontWeight.Bold) } },
        text = text?.let { { Text(it) } },
        icon = icon,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        modifier = modifier,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        tonalElevation = tonalElevation,
    )
}

@Composable
fun UnifyConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    UnifyAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        icon = icon,
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(dismissText)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun UnifyInfoDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    buttonText: String = "确定",
    modifier: Modifier = Modifier,
) {
    UnifyAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        icon = { Text("ℹ️", style = MaterialTheme.typography.headlineMedium) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(buttonText)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun UnifyWarningDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    buttonText: String = "知道了",
    modifier: Modifier = Modifier,
) {
    UnifyAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        icon = { Text("⚠️", style = MaterialTheme.typography.headlineMedium) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(buttonText)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun UnifyErrorDialog(
    onDismissRequest: () -> Unit,
    title: String = "错误",
    message: String,
    buttonText: String = "确定",
    modifier: Modifier = Modifier,
) {
    UnifyAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        icon = { Text("❌", style = MaterialTheme.typography.headlineMedium) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(buttonText)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun UnifyCustomDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit,
) {
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content,
        )
    }
}

@Composable
fun UnifyLoadingDialog(
    onDismissRequest: () -> Unit = {},
    message: String = "加载中...",
    modifier: Modifier = Modifier,
) {
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun UnifyBottomSheetDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dragHandle = dragHandle,
        content = content,
    )
}
