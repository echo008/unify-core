@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 平台特定组件 - 跨平台统一接口
 * 支持8大平台的原生特性和平台专属功能
 */

/**
 * 平台信息数据类
 */
data class PlatformInfo(
    val name: String,
    val version: String,
    val architecture: String,
    val deviceModel: String,
    val screenSize: String,
    val capabilities: List<String>
)

/**
 * 平台能力枚举
 */
enum class PlatformCapability {
    CAMERA,
    MICROPHONE,
    GPS,
    BLUETOOTH,
    NFC,
    BIOMETRIC,
    PUSH_NOTIFICATIONS,
    BACKGROUND_PROCESSING,
    FILE_SYSTEM,
    NETWORK,
    SENSORS,
    VIBRATION,
    AUDIO_RECORDING,
    VIDEO_RECORDING,
    SCREEN_RECORDING,
    CONTACTS,
    CALENDAR,
    PHOTOS,
    STORAGE,
    TELEPHONY
}

/**
 * 获取平台信息的expect函数
 */
expect fun getPlatformInfo(): PlatformInfo

/**
 * 检查平台能力的expect函数
 */
expect fun checkPlatformCapability(capability: PlatformCapability): Boolean

/**
 * 请求平台权限的expect函数
 */
expect suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean

/**
 * 平台特定UI组件接口
 */
interface PlatformSpecificUI {
    @Composable
    fun NativeButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
    )
    
    @Composable
    fun NativeTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        placeholder: String = "",
        enabled: Boolean = true
    )
    
    @Composable
    fun NativeSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
    )
    
    @Composable
    fun NativeSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        enabled: Boolean = true
    )
    
    @Composable
    fun NativeProgressBar(
        progress: Float,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary
    )
    
    @Composable
    fun NativeDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String = "确定",
        dismissText: String = "取消"
    )
    
    @Composable
    fun NativeToast(
        message: String,
        duration: Long = 3000L,
        onDismiss: () -> Unit = {}
    )
    
    @Composable
    fun NativeActionSheet(
        title: String,
        actions: List<ActionSheetItem>,
        onDismiss: () -> Unit
    )
    
    @Composable
    fun NativeDatePicker(
        selectedDate: Long?,
        onDateSelected: (Long) -> Unit,
        onDismiss: () -> Unit
    )
    
    @Composable
    fun NativeTimePicker(
        selectedTime: Pair<Int, Int>?,
        onTimeSelected: (Int, Int) -> Unit,
        onDismiss: () -> Unit
    )
}

/**
 * ActionSheet项目数据类
 */
data class ActionSheetItem(
    val title: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val action: () -> Unit
)

/**
 * 获取平台特定UI实现的expect函数
 */
expect fun getPlatformSpecificUI(): PlatformSpecificUI

/**
 * 统一平台组件包装器
 */
@Composable
fun UnifyPlatformButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            Text(text)
        }
    }
}

/**
 * 统一平台文本输入框
 */
@Composable
fun UnifyPlatformTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = placeholder,
            enabled = enabled
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = { Text(placeholder) },
            enabled = enabled
        )
    }
}

/**
 * 统一平台开关
 */
@Composable
fun UnifyPlatformSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled
        )
    } else {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled
        )
    }
}

/**
 * 统一平台滑块
 */
@Composable
fun UnifyPlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeSlider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            valueRange = valueRange,
            enabled = enabled
        )
    } else {
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            valueRange = valueRange,
            enabled = enabled
        )
    }
}

/**
 * 统一平台进度条
 */
@Composable
fun UnifyPlatformProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeProgressBar(
            progress = progress,
            modifier = modifier,
            color = color
        )
    } else {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = color
        )
    }
}

/**
 * 统一平台对话框
 */
@Composable
fun UnifyPlatformDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "确定",
    dismissText: String = "取消",
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeDialog(
            title = title,
            message = message,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            confirmText = confirmText,
            dismissText = dismissText
        )
    } else {
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
            }
        )
    }
}

/**
 * 统一平台Toast
 */
@Composable
fun UnifyPlatformToast(
    message: String,
    duration: Long = 3000L,
    onDismiss: () -> Unit = {},
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeToast(
            message = message,
            duration = duration,
            onDismiss = onDismiss
        )
    } else {
        // 使用Compose实现的Toast
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(duration)
            onDismiss()
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}

/**
 * 统一平台ActionSheet
 */
@Composable
fun UnifyPlatformActionSheet(
    title: String,
    actions: List<ActionSheetItem>,
    onDismiss: () -> Unit,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeActionSheet(
            title = title,
            actions = actions,
            onDismiss = onDismiss
        )
    } else {
        // 使用Compose实现的ActionSheet
        ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                actions.forEach { action ->
                    ListItem(
                        headlineContent = { Text(action.title) },
                        leadingContent = action.icon?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = action.title,
                                    tint = if (action.isDestructive) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            action.action()
                            onDismiss()
                        },
                        colors = ListItemDefaults.colors(
                            headlineColor = if (action.isDestructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )
                }
            }
        }
    }
}

/**
 * 统一平台日期选择器
 */
@Composable
fun UnifyPlatformDatePicker(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeDatePicker(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            onDismiss = onDismiss
        )
    } else {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 统一平台时间选择器
 */
@Composable
fun UnifyPlatformTimePicker(
    selectedTime: Pair<Int, Int>?,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    useNative: Boolean = false
) {
    if (useNative) {
        val platformUI = remember { getPlatformSpecificUI() }
        platformUI.NativeTimePicker(
            selectedTime = selectedTime,
            onTimeSelected = onTimeSelected,
            onDismiss = onDismiss
        )
    } else {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.first ?: 12,
            initialMinute = selectedTime?.second ?: 0
        )
        
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

/**
 * 平台特定功能管理器
 */
object PlatformSpecificManager {
    /**
     * 获取当前平台信息
     */
    fun getCurrentPlatformInfo(): PlatformInfo {
        return getPlatformInfo()
    }
    
    /**
     * 检查平台能力支持
     */
    fun isCapabilitySupported(capability: PlatformCapability): Boolean {
        return checkPlatformCapability(capability)
    }
    
    /**
     * 请求权限
     */
    suspend fun requestPermission(capability: PlatformCapability): Boolean {
        return requestPlatformPermission(capability)
    }
    
    /**
     * 批量检查能力支持
     */
    fun checkMultipleCapabilities(capabilities: List<PlatformCapability>): Map<PlatformCapability, Boolean> {
        return capabilities.associateWith { checkPlatformCapability(it) }
    }
    
    /**
     * 批量请求权限
     */
    suspend fun requestMultiplePermissions(capabilities: List<PlatformCapability>): Map<PlatformCapability, Boolean> {
        val results = mutableMapOf<PlatformCapability, Boolean>()
        capabilities.forEach { capability ->
            results[capability] = requestPlatformPermission(capability)
        }
        return results
    }
}

/**
 * 平台适配配置
 */
data class PlatformAdaptationConfig(
    val useNativeComponents: Boolean = false,
    val enablePlatformOptimizations: Boolean = true,
    val respectPlatformGuidelines: Boolean = true,
    val enableAccessibility: Boolean = true,
    val enableRTL: Boolean = true,
    val enableDarkMode: Boolean = true
)
