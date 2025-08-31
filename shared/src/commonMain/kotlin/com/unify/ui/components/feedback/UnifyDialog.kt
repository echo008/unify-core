package com.unify.ui.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*

/**
 * Unify Dialog 组件
 * 支持多平台适配的统一对话框组件，参考 KuiklyUI 设计规范
 */

/**
 * 对话框变体枚举
 */
enum class UnifyDialogVariant {
    STANDARD,       // 标准对话框
    ALERT,          // 警告对话框
    CONFIRMATION,   // 确认对话框
    FULLSCREEN,     // 全屏对话框
    BOTTOM_SHEET    // 底部表单
}

/**
 * 对话框尺寸枚举
 */
enum class UnifyDialogSize {
    SMALL,          // 小尺寸 - 280dp
    MEDIUM,         // 中等尺寸 - 400dp
    LARGE,          // 大尺寸 - 560dp
    EXTRA_LARGE     // 超大尺寸 - 720dp
}

/**
 * 对话框按钮数据类
 */
data class UnifyDialogButton(
    val text: String,
    val onClick: () -> Unit,
    val variant: UnifyButtonVariant = UnifyButtonVariant.TEXT,
    val enabled: Boolean = true,
    val loading: Boolean = false
)

/**
 * 主要 Unify Dialog 组件
 */
@Composable
fun UnifyDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UnifyDialogVariant = UnifyDialogVariant.STANDARD,
    size: UnifyDialogSize = UnifyDialogSize.MEDIUM,
    title: String? = null,
    icon: ImageVector? = null,
    dismissible: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    shape: Shape? = null,
    tonalElevation: Dp = 6.dp,
    properties: DialogProperties = DialogProperties(),
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取对话框配置
    val dialogConfig = getDialogConfig(variant, size, theme)
    val actualBackgroundColor = backgroundColor ?: dialogConfig.backgroundColor
    val actualContentColor = contentColor ?: dialogConfig.contentColor
    val actualShape = shape ?: dialogConfig.shape
    
    when (variant) {
        UnifyDialogVariant.FULLSCREEN -> {
            Dialog(
                onDismissRequest = if (dismissible) onDismissRequest else { },
                properties = properties.copy(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription?.let { 
                                this.contentDescription = it 
                            }
                        },
                    color = actualBackgroundColor,
                    contentColor = actualContentColor
                ) {
                    Column {
                        // 标题栏
                        if (title != null || icon != null) {
                            UnifyDialogHeader(
                                title = title,
                                icon = icon,
                                onDismiss = if (dismissible) onDismissRequest else null,
                                backgroundColor = actualBackgroundColor,
                                contentColor = actualContentColor
                            )
                        }
                        
                        // 内容
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
        
        else -> {
            Dialog(
                onDismissRequest = if (dismissible) onDismissRequest else { },
                properties = properties
            ) {
                Surface(
                    modifier = modifier
                        .width(dialogConfig.width)
                        .clip(actualShape)
                        .semantics {
                            contentDescription?.let { 
                                this.contentDescription = it 
                            }
                        },
                    shape = actualShape,
                    color = actualBackgroundColor,
                    contentColor = actualContentColor,
                    tonalElevation = tonalElevation
                ) {
                    Column {
                        // 标题和图标
                        if (title != null || icon != null) {
                            UnifyDialogTitleSection(
                                title = title,
                                icon = icon,
                                contentColor = actualContentColor
                            )
                        }
                        
                        // 内容
                        Box(
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                bottom = 24.dp,
                                top = if (title != null || icon != null) 0.dp else 24.dp
                            )
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 标准对话框组件
 */
@Composable
fun UnifyAlertDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    text: String? = null,
    icon: ImageVector? = null,
    confirmButton: UnifyDialogButton,
    dismissButton: UnifyDialogButton? = null,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    properties: DialogProperties = DialogProperties(),
    contentDescription: String? = null
) {
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        variant = UnifyDialogVariant.ALERT,
        size = UnifyDialogSize.SMALL,
        title = title,
        icon = icon,
        dismissible = dismissible,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        properties = properties,
        contentDescription = contentDescription
    ) {
        Column {
            // 文本内容
            text?.let { textContent ->
                UnifyText(
                    text = textContent,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // 按钮区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                dismissButton?.let { button ->
                    UnifyButton(
                        text = button.text,
                        onClick = button.onClick,
                        variant = button.variant,
                        enabled = button.enabled,
                        loading = button.loading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                UnifyButton(
                    text = confirmButton.text,
                    onClick = confirmButton.onClick,
                    variant = confirmButton.variant,
                    enabled = confirmButton.enabled,
                    loading = confirmButton.loading
                )
            }
        }
    }
}

/**
 * 确认对话框组件
 */
@Composable
fun UnifyConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit = onDismissRequest,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    confirmText: String = "确认",
    cancelText: String = "取消",
    destructive: Boolean = false,
    dismissible: Boolean = true,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    UnifyAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = text,
        icon = icon,
        confirmButton = UnifyDialogButton(
            text = confirmText,
            onClick = onConfirm,
            variant = if (destructive) UnifyButtonVariant.FILLED else UnifyButtonVariant.FILLED
        ),
        dismissButton = UnifyDialogButton(
            text = cancelText,
            onClick = onCancel,
            variant = UnifyButtonVariant.TEXT
        ),
        modifier = modifier,
        dismissible = dismissible,
        contentDescription = contentDescription
    )
}

/**
 * 输入对话框组件
 */
@Composable
fun UnifyInputDialog(
    onDismissRequest: () -> Unit,
    title: String,
    label: String? = null,
    placeholder: String? = null,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit = onDismissRequest,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    confirmText: String = "确认",
    cancelText: String = "取消",
    maxLength: Int? = null,
    singleLine: Boolean = true,
    dismissible: Boolean = true,
    contentDescription: String? = null
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        variant = UnifyDialogVariant.STANDARD,
        size = UnifyDialogSize.MEDIUM,
        title = title,
        icon = icon,
        dismissible = dismissible,
        contentDescription = contentDescription
    ) {
        Column {
            // 输入框
            UnifyTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = label,
                placeholder = placeholder,
                maxLength = maxLength,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // 按钮区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                UnifyButton(
                    text = cancelText,
                    onClick = onCancel,
                    variant = UnifyButtonVariant.TEXT
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyButton(
                    text = confirmText,
                    onClick = { onConfirm(inputValue) },
                    variant = UnifyButtonVariant.FILLED,
                    enabled = inputValue.isNotBlank()
                )
            }
        }
    }
}

/**
 * 列表选择对话框组件
 */
@Composable
fun <T> UnifyListDialog(
    items: List<T>,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    itemContent: @Composable (T) -> Unit,
    dismissible: Boolean = true,
    contentDescription: String? = null
) {
    UnifyDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        variant = UnifyDialogVariant.STANDARD,
        size = UnifyDialogSize.MEDIUM,
        title = title,
        icon = icon,
        dismissible = dismissible,
        contentDescription = contentDescription
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
        ) {
            items.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 8.dp),
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}

/**
 * 底部表单对话框组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyBottomSheetDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets,
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        sheetState = sheetState,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        properties = properties,
        content = content
    )
}

/**
 * 对话框标题区域组件
 */
@Composable
private fun UnifyDialogTitleSection(
    title: String?,
    icon: ImageVector?,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    if (title != null || icon != null) {
        Row(
            modifier = modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { iconVector ->
                UnifyIcon(
                    imageVector = iconVector,
                    contentDescription = null,
                    size = UnifyIconSize.SMALL,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            title?.let { titleText ->
                UnifyText(
                    text = titleText,
                    variant = UnifyTextVariant.TITLE_LARGE,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * 对话框头部组件（全屏对话框用）
 */
@Composable
private fun UnifyDialogHeader(
    title: String?,
    icon: ImageVector?,
    onDismiss: (() -> Unit)?,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { iconVector ->
                UnifyIcon(
                    imageVector = iconVector,
                    contentDescription = null,
                    size = UnifyIconSize.SMALL,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            title?.let { titleText ->
                UnifyText(
                    text = titleText,
                    variant = UnifyTextVariant.TITLE_LARGE,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }
            
            onDismiss?.let { dismissAction ->
                UnifyIconButton(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "关闭",
                    onClick = dismissAction,
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * 获取对话框配置
 */
@Composable
private fun getDialogConfig(
    variant: UnifyDialogVariant,
    size: UnifyDialogSize,
    theme: com.unify.ui.theme.UnifyTheme
): DialogConfig {
    val width = when (size) {
        UnifyDialogSize.SMALL -> 280.dp
        UnifyDialogSize.MEDIUM -> 400.dp
        UnifyDialogSize.LARGE -> 560.dp
        UnifyDialogSize.EXTRA_LARGE -> 720.dp
    }
    
    val shape = when (variant) {
        UnifyDialogVariant.FULLSCREEN -> RoundedCornerShape(0.dp)
        else -> theme.shapes.large
    }
    
    return DialogConfig(
        width = width,
        backgroundColor = theme.colors.surface,
        contentColor = theme.colors.onSurface,
        shape = shape
    )
}

/**
 * 对话框配置数据类
 */
private data class DialogConfig(
    val width: Dp,
    val backgroundColor: Color,
    val contentColor: Color,
    val shape: Shape
)
