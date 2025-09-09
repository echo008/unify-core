package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.awt.Toolkit

/**
 * Desktop平台特定组件actual实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    val toolkit = Toolkit.getDefaultToolkit()
    val screenSize = toolkit.screenSize

    return PlatformInfo(
        name = "Desktop",
        version = System.getProperty("os.version"),
        architecture = System.getProperty("os.arch"),
        deviceModel = "Desktop Computer",
        screenSize = "${screenSize.width}x${screenSize.height}",
        capabilities = listOf("MICROPHONE", "BLUETOOTH", "PUSH_NOTIFICATIONS", "FILE_SYSTEM", "NETWORK"),
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> false
        PlatformCapability.MICROPHONE -> true
        PlatformCapability.GPS -> false
        PlatformCapability.BLUETOOTH -> true
        PlatformCapability.NFC -> false
        PlatformCapability.BIOMETRIC -> false
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> true
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> false
        PlatformCapability.VIBRATION -> false
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> false
        PlatformCapability.SCREEN_RECORDING -> false
        PlatformCapability.CONTACTS -> false
        PlatformCapability.CALENDAR -> false
        PlatformCapability.PHOTOS -> false
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> false
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    // Desktop平台权限模拟
    delay(500) // 模拟权限请求延迟

    return when (capability) {
        PlatformCapability.CAMERA -> false
        PlatformCapability.MICROPHONE -> true
        PlatformCapability.GPS -> false
        PlatformCapability.BLUETOOTH -> true
        PlatformCapability.NFC -> false
        PlatformCapability.BIOMETRIC -> false
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> true
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> false
        PlatformCapability.VIBRATION -> false
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> false
        PlatformCapability.SCREEN_RECORDING -> false
        PlatformCapability.CONTACTS -> false
        PlatformCapability.CALENDAR -> false
        PlatformCapability.PHOTOS -> false
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> false
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return DesktopPlatformUI()
}

/**
 * Desktop平台UI实现
 */
class DesktopPlatformUI : PlatformSpecificUI {
    @Composable
    override fun NativeButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text)
        }
    }

    @Composable
    override fun NativeTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier,
        placeholder: String,
        enabled: Boolean,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = modifier,
            enabled = enabled,
        )
    }

    @Composable
    override fun NativeSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    }

    @Composable
    override fun NativeSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier,
        valueRange: ClosedFloatingPointRange<Float>,
        enabled: Boolean,
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = modifier,
            enabled = enabled,
        )
    }

    @Composable
    override fun NativeProgressBar(
        progress: Float,
        modifier: Modifier,
        color: Color,
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = color,
        )
    }

    @Composable
    override fun NativeDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String,
        dismissText: String,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            },
        )
    }

    @Composable
    override fun NativeToast(
        message: String,
        duration: Long,
        onDismiss: () -> Unit,
    ) {
        // Desktop平台Toast模拟实现
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                ),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    @Composable
    override fun NativeDatePicker(
        selectedDate: Long?,
        onDateSelected: (Long) -> Unit,
        onDismiss: () -> Unit,
    ) {
        // Desktop平台DatePicker模拟实现
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("选择日期") },
            text = {
                Text("当前选择: ${selectedDate?.let { java.util.Date(it) } ?: "未选择"}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(System.currentTimeMillis())
                        onDismiss()
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
        )
    }

    @Composable
    override fun NativeTimePicker(
        selectedTime: Pair<Int, Int>?,
        onTimeSelected: (Int, Int) -> Unit,
        onDismiss: () -> Unit,
    ) {
        // Desktop平台TimePicker模拟实现
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("选择时间") },
            text = {
                Text("当前选择: ${selectedTime?.let { "${it.first}:${it.second}" } ?: "未选择"}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(12, 0)
                        onDismiss()
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
        )
    }

    @Composable
    override fun NativeActionSheet(
        title: String,
        actions: List<ActionSheetItem>,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Column {
                    actions.forEach { action ->
                        TextButton(
                            onClick = {
                                action.action()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = action.title,
                                color =
                                    if (action.isDestructive) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
        )
    }
}
