@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * Watch平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    return PlatformInfo(
        name = "WatchOS",
        version = "10.0",
        architecture = "ARM64",
        deviceModel = "Apple Watch Series 9",
        screenSize = "396x484",
        capabilities = listOf(
            "Health Sensors", "GPS", "Cellular", "NFC", "Haptic Feedback",
            "Digital Crown", "Side Button", "Always-On Display", "Water Resistant"
        )
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> false // 手表通常无摄像头
        PlatformCapability.MICROPHONE -> true
        PlatformCapability.GPS -> true
        PlatformCapability.BLUETOOTH -> true
        PlatformCapability.NFC -> true
        PlatformCapability.BIOMETRIC -> true // 心率等生物传感器
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> true
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> true // 丰富的健康传感器
        PlatformCapability.VIBRATION -> true // 触觉反馈
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> false
        PlatformCapability.SCREEN_RECORDING -> false
        PlatformCapability.CONTACTS -> true
        PlatformCapability.CALENDAR -> true
        PlatformCapability.PHOTOS -> true
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> true // 蜂窝版本支持
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        // Watch平台权限请求
        continuation.resume(true)
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return WatchPlatformUI()
}

/**
 * Watch平台UI实现 - 针对小屏幕优化
 */
class WatchPlatformUI : PlatformSpecificUI {
    
    @Composable
    override fun NativeButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .size(width = 120.dp, height = 40.dp), // 手表按钮适中大小
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF), // Apple蓝色
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF007AFF).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(20.dp), // 手表圆润风格
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium, // 手表文字适中
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
        // 手表通常使用语音输入或预设选项，文本输入框简化
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            placeholder = { 
                Text(
                    placeholder,
                    color = Color(0xFF8E8E93),
                    fontSize = 14.sp
                ) 
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF007AFF),
                unfocusedBorderColor = Color(0xFFD1D1D6),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF007AFF)
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            singleLine = true
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
            modifier = modifier.size(width = 50.dp, height = 30.dp), // 手表开关较小
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759), // Apple绿色
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF39393D),
                checkedBorderColor = Color(0xFF34C759),
                uncheckedBorderColor = Color(0xFF39393D)
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
            modifier = modifier.height(30.dp), // 手表滑块较小
            enabled = enabled,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF007AFF),
                inactiveTrackColor = Color(0xFF39393D)
            )
        )
    }
    
    @Composable
    override fun NativeProgressBar(
        progress: Float,
        modifier: Modifier,
        color: Color
    ) {
        // 手表使用圆形进度条更合适
        Box(
            modifier = modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(50.dp),
                color = Color(0xFF007AFF),
                trackColor = Color(0xFF39393D),
                strokeWidth = 4.dp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
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
        // 手表对话框更紧凑
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF8E8E93)
                    )
                ) {
                    Text(
                        dismissText,
                        fontSize = 14.sp
                    )
                }
            },
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(16.dp)
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
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
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
        // 手表使用全屏列表
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            
            actions.forEach { action ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            action.action()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1C1E)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        action.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = action.title,
                                tint = if (action.isDestructive) {
                                    Color(0xFFFF453A)
                                } else {
                                    Color(0xFF007AFF)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Text(
                            text = action.title,
                            fontSize = 14.sp,
                            color = if (action.isDestructive) {
                                Color(0xFFFF453A)
                            } else {
                                Color.White
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Text("取消", fontSize = 14.sp)
            }
        }
    }
    
    @Composable
    override fun NativeDatePicker(
        selectedDate: Long?,
        onDateSelected: (Long) -> Unit,
        onDismiss: () -> Unit
    ) {
        // 手表使用简化的日期选择器
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            Text(
                text = "选择日期",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            
            DatePicker(
                state = datePickerState,
                modifier = Modifier.weight(1f),
                colors = DatePickerDefaults.colors(
                    containerColor = Color.Black,
                    selectedDayContainerColor = Color(0xFF007AFF),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color(0xFF007AFF)
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1C1C1E)
                    )
                ) {
                    Text("取消", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    )
                ) {
                    Text("确定", fontSize = 12.sp)
                }
            }
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            Text(
                text = "选择时间",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )
            
            TimePicker(
                state = timePickerState,
                modifier = Modifier.weight(1f),
                colors = TimePickerDefaults.colors(
                    selectorColor = Color(0xFF007AFF),
                    containerColor = Color.Black
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1C1C1E)
                    )
                ) {
                    Text("取消", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF)
                    )
                ) {
                    Text("确定", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Watch特定功能
 */
object WatchSpecificFeatures {
    /**
     * 获取手表系统信息
     */
    fun getWatchSystemInfo(): Map<String, String> {
        return mapOf(
            "WATCH_OS_VERSION" to "10.0",
            "DEVICE_MODEL" to "Apple Watch Series 9",
            "CASE_SIZE" to "45mm",
            "CASE_MATERIAL" to "Aluminum",
            "BAND_TYPE" to "Sport Band",
            "CELLULAR" to "Supported",
            "ALWAYS_ON_DISPLAY" to "Supported",
            "WATER_RESISTANCE" to "50 meters"
        )
    }
    
    /**
     * 获取健康传感器信息
     */
    fun getHealthSensors(): Map<String, Boolean> {
        return mapOf(
            "HEART_RATE" to true,
            "ECG" to true,
            "BLOOD_OXYGEN" to true,
            "TEMPERATURE" to true,
            "ACCELEROMETER" to true,
            "GYROSCOPE" to true,
            "BAROMETER" to true,
            "AMBIENT_LIGHT" to true,
            "FALL_DETECTION" to true,
            "CRASH_DETECTION" to true
        )
    }
    
    /**
     * 获取健康数据
     */
    fun getHealthData(): Map<String, String> {
        return mapOf(
            "HEART_RATE" to "72 BPM",
            "STEPS_TODAY" to "8,542",
            "CALORIES_BURNED" to "420 kcal",
            "DISTANCE_WALKED" to "6.2 km",
            "ACTIVE_MINUTES" to "45 min",
            "STAND_HOURS" to "8/12",
            "EXERCISE_MINUTES" to "30 min",
            "BLOOD_OXYGEN" to "98%"
        )
    }
    
    /**
     * 检查数字表冠功能
     */
    fun getDigitalCrownFeatures(): Map<String, Boolean> {
        return mapOf(
            "SCROLL_SUPPORT" to true,
            "ZOOM_SUPPORT" to true,
            "HAPTIC_FEEDBACK" to true,
            "PRESS_DETECTION" to true,
            "ROTATION_DETECTION" to true
        )
    }
    
    /**
     * 获取电池信息
     */
    fun getBatteryInfo(): Map<String, String> {
        return mapOf(
            "BATTERY_LEVEL" to "85%",
            "CHARGING_STATUS" to "Not Charging",
            "LOW_POWER_MODE" to "Disabled",
            "ESTIMATED_USAGE" to "18 hours",
            "LAST_CHARGE" to "2 hours ago"
        )
    }
    
    /**
     * 检查连接状态
     */
    fun getConnectivityStatus(): Map<String, String> {
        return mapOf(
            "IPHONE_CONNECTION" to "Connected",
            "WIFI_STATUS" to "Connected",
            "CELLULAR_STATUS" to "Available",
            "BLUETOOTH_STATUS" to "Connected",
            "GPS_STATUS" to "Available"
        )
    }
}
