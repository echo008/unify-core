@file:OptIn(ExperimentalMaterial3Api::class)

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
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import kotlin.coroutines.resume

/**
 * iOS平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    val device = UIDevice.currentDevice
    val processInfo = NSProcessInfo.processInfo
    
    return PlatformInfo(
        name = "iOS",
        version = device.systemVersion,
        architecture = processInfo.processorCount.toString() + " cores",
        deviceModel = device.model,
        screenSize = "unknown", // 需要在Composable中获取
        capabilities = listOf(
            "Camera", "Microphone", "GPS", "Bluetooth", "NFC", 
            "Biometric", "Push Notifications", "Background Processing",
            "File System", "Network", "Sensors", "Vibration"
        )
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> true
        PlatformCapability.MICROPHONE -> true
        PlatformCapability.GPS -> true
        PlatformCapability.BLUETOOTH -> true
        PlatformCapability.NFC -> true // iOS 11+
        PlatformCapability.BIOMETRIC -> true // Touch ID/Face ID
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> true
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> true
        PlatformCapability.VIBRATION -> true
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> true
        PlatformCapability.SCREEN_RECORDING -> true // iOS 11+
        PlatformCapability.CONTACTS -> true
        PlatformCapability.CALENDAR -> true
        PlatformCapability.PHOTOS -> true
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> true
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        // 在实际实现中，这里会使用iOS的权限请求API
        // 现在返回模拟结果
        continuation.resume(true)
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return IOSPlatformUI()
}

/**
 * iOS平台UI实现
 */
class IOSPlatformUI : PlatformSpecificUI {
    
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
                containerColor = Color(0xFF007AFF), // iOS蓝色
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF007AFF).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(10.dp), // iOS风格圆角
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp, // iOS按钮通常无阴影
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
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
                    color = Color(0xFF8E8E93) // iOS占位符颜色
                ) 
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007AFF),
                unfocusedBorderColor = Color(0xFFD1D1D6),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color(0xFF007AFF)
            ),
            shape = RoundedCornerShape(10.dp)
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
                checkedTrackColor = Color(0xFF34C759), // iOS绿色
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5EA),
                checkedBorderColor = Color(0xFF34C759),
                uncheckedBorderColor = Color(0xFFE5E5EA)
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
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF007AFF),
                inactiveTrackColor = Color(0xFFE5E5EA)
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
            color = Color(0xFF007AFF),
            trackColor = Color(0xFFE5E5EA)
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
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF007AFF)
                    )
                ) {
                    Text(
                        confirmText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF007AFF)
                    )
                ) {
                    Text(dismissText)
                }
            },
            containerColor = Color(0xFFF2F2F7), // iOS背景色
            shape = RoundedCornerShape(14.dp)
        )
    }
    
    @Composable
    override fun NativeToast(
        message: String,
        duration: Long,
        onDismiss: () -> Unit
    ) {
        // iOS风格的Toast (类似HUD)
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(duration)
            onDismiss()
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
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
            containerColor = Color(0xFFF2F2F7),
            contentColor = Color.Black,
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // 操作项
                actions.forEachIndexed { index, action ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                action.action()
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = when {
                            actions.size == 1 -> RoundedCornerShape(14.dp)
                            index == 0 -> RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                            index == actions.size - 1 -> RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                            else -> RoundedCornerShape(0.dp)
                        },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            action.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = action.title,
                                    tint = if (action.isDestructive) {
                                        Color(0xFFFF3B30) // iOS红色
                                    } else {
                                        Color(0xFF007AFF)
                                    },
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                            
                            Text(
                                text = action.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (action.isDestructive) {
                                    Color(0xFFFF3B30)
                                } else {
                                    Color(0xFF007AFF)
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (index < actions.size - 1) {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 取消按钮
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "取消",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.SemiBold
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
        
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF007AFF)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF007AFF)
                    )
                ) {
                    Text("取消")
                }
            },
            containerColor = Color(0xFFF2F2F7),
            shape = RoundedCornerShape(14.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFFF2F2F7),
                    selectedDayContainerColor = Color(0xFF007AFF),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color(0xFF007AFF)
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
                        contentColor = Color(0xFF007AFF)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF007AFF)
                    )
                ) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = Color(0xFF007AFF),
                        containerColor = Color(0xFFF2F2F7)
                    )
                )
            },
            containerColor = Color(0xFFF2F2F7),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

/**
 * iOS特定功能
 */
object IOSSpecificFeatures {
    /**
     * 获取iOS版本信息
     */
    fun getIOSVersionInfo(): Map<String, String> {
        val device = UIDevice.currentDevice
        val bundle = NSBundle.mainBundle
        
        return mapOf(
            "SYSTEM_VERSION" to device.systemVersion,
            "SYSTEM_NAME" to device.systemName,
            "MODEL" to device.model,
            "LOCALIZED_MODEL" to device.localizedModel,
            "BUNDLE_VERSION" to (bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "unknown"),
            "BUNDLE_SHORT_VERSION" to (bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "unknown")
        )
    }
    
    /**
     * 获取设备硬件信息
     */
    fun getDeviceHardwareInfo(): Map<String, String> {
        val device = UIDevice.currentDevice
        val processInfo = NSProcessInfo.processInfo
        
        return mapOf(
            "NAME" to device.name,
            "MODEL" to device.model,
            "LOCALIZED_MODEL" to device.localizedModel,
            "IDENTIFIER_FOR_VENDOR" to (device.identifierForVendor?.UUIDString ?: "unknown"),
            "PROCESSOR_COUNT" to processInfo.processorCount.toString(),
            "PHYSICAL_MEMORY" to processInfo.physicalMemory.toString(),
            "SYSTEM_UPTIME" to processInfo.systemUptime.toString()
        )
    }
    
    /**
     * 检查是否为iPad
     */
    fun isIPad(): Boolean {
        val device = UIDevice.currentDevice
        return device.userInterfaceIdiom == platform.UIKit.UIUserInterfaceIdiomPad
    }
    
    /**
     * 检查是否支持Face ID
     */
    fun supportsFaceID(): Boolean {
        // 在实际实现中会检查LAContext.biometryType
        return true // 简化实现
    }
    
    /**
     * 检查是否支持Touch ID
     */
    fun supportsTouchID(): Boolean {
        // 在实际实现中会检查LAContext.biometryType
        return true // 简化实现
    }
}
