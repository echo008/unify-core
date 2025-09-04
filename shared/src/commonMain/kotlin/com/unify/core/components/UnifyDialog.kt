package com.unify.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * Unify统一对话框组件
 * 100% Kotlin Compose语法实现
 */
enum class UnifyDialogType {
    ALERT,
    CONFIRMATION,
    CUSTOM
}

data class UnifyDialogAction(
    val text: String,
    val onClick: () -> Unit,
    val isPrimary: Boolean = false,
    val enabled: Boolean = true
)

@Composable
fun UnifyDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    message: String? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    actions: List<UnifyDialogAction> = emptyList(),
    type: UnifyDialogType = UnifyDialogType.ALERT,
    dismissible: Boolean = true,
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null
) {
    when (type) {
        UnifyDialogType.ALERT, UnifyDialogType.CONFIRMATION -> {
            AlertDialog(
                onDismissRequest = if (dismissible) onDismissRequest else { },
                title = title?.let {
                    {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = iconTint ?: MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                text = if (message != null || content != null) {
                    {
                        Column {
                            if (message != null) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (content != null) {
                                if (message != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                content()
                            }
                        }
                    }
                } else null,
                confirmButton = {
                    DialogActions(actions = actions)
                },
                properties = properties
            )
        }
        UnifyDialogType.CUSTOM -> {
            CustomDialog(
                onDismissRequest = if (dismissible) onDismissRequest else { },
                title = title,
                message = message,
                icon = icon,
                iconTint = iconTint,
                actions = actions,
                properties = properties,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDialog(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    icon: ImageVector?,
    iconTint: Color?,
    actions: List<UnifyDialogAction>,
    properties: DialogProperties,
    content: (@Composable () -> Unit)?
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        UnifyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with icon and title
                if (title != null || icon != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = iconTint ?: MaterialTheme.colorScheme.primary
                            )
                            if (title != null) {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                        if (title != null) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Message content
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (content != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Custom content
                content?.invoke()
                
                // Actions
                if (actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    DialogActions(
                        actions = actions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogActions(
    actions: List<UnifyDialogAction>,
    modifier: Modifier = Modifier
) {
    if (actions.isEmpty()) return
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEachIndexed { index, action ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            if (action.isPrimary) {
                UnifyButton(
                    onClick = action.onClick,
                    text = action.text,
                    enabled = action.enabled,
                    type = UnifyButtonType.FILLED,
                    size = UnifyButtonSize.MEDIUM
                )
            } else {
                TextButton(
                    onClick = action.onClick,
                    enabled = action.enabled
                ) {
                    Text(action.text)
                }
            }
        }
    }
}

// 便捷方法
@Composable
fun UnifyAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确定",
    onConfirm: () -> Unit = onDismissRequest,
    icon: ImageVector? = null
) {
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        icon = icon,
        type = UnifyDialogType.ALERT,
        actions = listOf(
            UnifyDialogAction(
                text = confirmText,
                onClick = onConfirm,
                isPrimary = true
            )
        )
    )
}

@Composable
fun UnifyConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确定",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    onCancel: () -> Unit = onDismissRequest,
    icon: ImageVector? = null
) {
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        icon = icon,
        type = UnifyDialogType.CONFIRMATION,
        actions = listOf(
            UnifyDialogAction(
                text = cancelText,
                onClick = onCancel
            ),
            UnifyDialogAction(
                text = confirmText,
                onClick = onConfirm,
                isPrimary = true
            )
        )
    )
}
