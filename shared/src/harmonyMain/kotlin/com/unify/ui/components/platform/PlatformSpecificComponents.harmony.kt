package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * HarmonyOS平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    return PlatformInfo(
        name = "HarmonyOS",
        version = "4.0", // 模拟版本
        architecture = "ARM64",
        deviceModel = "Harmony Device",
        screenSize = "unknown", // 需要在运行时获取
        capabilities = listOf(
            "Camera", "Microphone", "GPS", "Bluetooth", "NFC", 
            "Biometric", "Push Notifications", "Background Processing",
            "File System", "Network", "Sensors", "Vibration",
            "Distributed Computing", "Multi-Screen Collaboration"
        )
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> true
        PlatformCapability.MICROPHONE -> true
        PlatformCapability.GPS -> true
        PlatformCapability.BLUETOOTH -> true
        PlatformCapability.NFC -> true
        PlatformCapability.BIOMETRIC -> true
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> true
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> true
        PlatformCapability.VIBRATION -> true
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> true
        PlatformCapability.SCREEN_RECORDING -> true
        PlatformCapability.CONTACTS -> true
        PlatformCapability.CALENDAR -> true
        PlatformCapability.PHOTOS -> true
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> true
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        // 在实际实现中，这里会使用HarmonyOS的权限请求API
        // 现在返回模拟结果
        continuation.resume(true)
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return HarmonyOSPlatformUI()
}

/**
 * HarmonyOS平台UI实现
 */
class HarmonyOSPlatformUI : PlatformSpecificUI {
    
    @Composable
    override fun NativeButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0A59F7), // HarmonyOS蓝色
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF0A59F7).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(24.dp), // HarmonyOS圆润风格
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    @Composable
    override fun NativeTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier,
        placeholder: String,
        enabled: Boolean
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = { 
                Text(
                    placeholder,
                    color = Color(0xFF99A9BF)
                ) 
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0A59F7),
                unfocusedBorderColor = Color(0xFFE4E6EA),
                focusedTextColor = Color(0xFF182431),
                unfocusedTextColor = Color(0xFF182431),
                cursorColor = Color(0xFF0A59F7)
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    @Composable
    override fun NativeSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        enabled: Boolean
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0A59F7),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE4E6EA),
                checkedBorderColor = Color(0xFF0A59F7),
                uncheckedBorderColor = Color(0xFFE4E6EA)
            )
        )
    }
    
    @Composable
    override fun NativeSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier,
        valueRange: ClosedFloatingPointRange<Float>,
        enabled: Boolean
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF0A59F7),
                activeTrackColor = Color(0xFF0A59F7),
                inactiveTrackColor = Color(0xFFE4E6EA)
            )
        )
    }
    
    @Composable
    override fun NativeProgressBar(
        progress: Float,
        modifier: Modifier,
        color: Color
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = Color(0xFF0A59F7),
            trackColor = Color(0xFFE4E6EA)
        )
    }
    
    @Composable
    override fun NativeDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        confirmText: String,
        dismissText: String
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF182431)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF182431)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF0A59F7)
                    )
                ) {
                    Text(
                        confirmText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF99A9BF)
                    )
                ) {
                    Text(dismissText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
    
    @Composable
    override fun NativeToast(
        message: String,
        duration: Long,
        onDismiss: () -> Unit
    ) {
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(duration)
            onDismiss()
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF182431).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    @Composable
    override fun NativeActionSheet(
        title: String,
        actions: List<ActionSheetItem>,
        onDismiss: () -> Unit
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            contentColor = Color(0xFF182431),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF182431),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                actions.forEach { action ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = action.title,
                                color = if (action.isDestructive) {
                                    Color(0xFFE84026) // HarmonyOS红色
                                } else {
                                    Color(0xFF182431)
                                },
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingContent = action.icon?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = action.title,
                                    tint = if (action.isDestructive) {
                                        Color(0xFFE84026)
                                    } else {
                                        Color(0xFF0A59F7)
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                action.action()
                                onDismiss()
                            }
                            .padding(vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    @Composable
    override fun NativeDatePicker(
        selectedDate: Long?,
        onDateSelected: (Long) -> Unit,
        onDismiss: () -> Unit
    ) {
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
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF0A59F7)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF99A9BF)
                    )
                ) {
                    Text("取消")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF0A59F7),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color(0xFF0A59F7)
                )
            )
        }
    }
    
    @Composable
    override fun NativeTimePicker(
        selectedTime: Pair<Int, Int>?,
        onTimeSelected: (Int, Int) -> Unit,
        onDismiss: () -> Unit
    ) {
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
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF0A59F7)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF99A9BF)
                    )
                ) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = Color(0xFF0A59F7),
                        containerColor = Color.White
                    )
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

/**
 * HarmonyOS特定功能
 */
object HarmonyOSSpecificFeatures {
    /**
     * 获取HarmonyOS版本信息
     */
    fun getHarmonyOSVersionInfo(): Map<String, String> {
        return mapOf(
            "HARMONY_VERSION" to "4.0",
            "API_LEVEL" to "10",
            "BUILD_VERSION" to "4.0.0.100",
            "SECURITY_PATCH" to "2024-01-01",
            "KERNEL_VERSION" to "5.10.0"
        )
    }
    
    /**
     * 获取设备硬件信息
     */
    fun getDeviceHardwareInfo(): Map<String, String> {
        return mapOf(
            "MANUFACTURER" to "HUAWEI",
            "BRAND" to "HUAWEI",
            "MODEL" to "Mate 60 Pro",
            "DEVICE_TYPE" to "Phone",
            "CHIPSET" to "Kirin 9000S",
            "RAM" to "12GB",
            "STORAGE" to "512GB"
        )
    }
    
    /**
     * 检查分布式能力
     */
    fun checkDistributedCapabilities(): Map<String, Boolean> {
        return mapOf(
            "DISTRIBUTED_DATA" to true,
            "DISTRIBUTED_TASK" to true,
            "MULTI_SCREEN_COLLABORATION" to true,
            "DEVICE_DISCOVERY" to true,
            "CROSS_DEVICE_MIGRATION" to true,
            "DISTRIBUTED_NOTIFICATION" to true
        )
    }
    
    /**
     * 检查是否为折叠屏设备
     */
    fun isFoldableDevice(): Boolean {
        // 在实际实现中会检查设备类型
        return false // 简化实现
    }
    
    /**
     * 获取超级终端设备列表
     */
    fun getSuperDeviceList(): List<Map<String, String>> {
        return listOf(
            mapOf(
                "DEVICE_ID" to "device_001",
                "DEVICE_NAME" to "MatePad Pro",
                "DEVICE_TYPE" to "Tablet",
                "CONNECTION_STATUS" to "Connected"
            ),
            mapOf(
                "DEVICE_ID" to "device_002",
                "DEVICE_NAME" to "MateBook X Pro",
                "DEVICE_TYPE" to "Laptop",
                "CONNECTION_STATUS" to "Available"
            )
        )
    }
    
    /**
     * 检查原子化服务支持
     */
    fun checkAtomicServiceSupport(): Map<String, Boolean> {
        return mapOf(
            "ATOMIC_SERVICE_RUNTIME" to true,
            "QUICK_APP_SUPPORT" to true,
            "CROSS_PLATFORM_SHARING" to true,
            "INSTANT_LAUNCH" to true
        )
    }
    
    /**
     * 获取AI能力信息
     */
    fun getAICapabilities(): Map<String, Boolean> {
        return mapOf(
            "NPU_SUPPORT" to true,
            "MINDSPORE_LITE" to true,
            "VOICE_RECOGNITION" to true,
            "IMAGE_RECOGNITION" to true,
            "NATURAL_LANGUAGE_PROCESSING" to true,
            "COMPUTER_VISION" to true
        )
    }
}
