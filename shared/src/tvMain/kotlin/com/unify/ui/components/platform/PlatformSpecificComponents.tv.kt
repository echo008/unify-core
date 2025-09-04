package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * TV平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    return PlatformInfo(
        name = "TV",
        version = "Android TV 13",
        architecture = "ARM64",
        deviceModel = "Smart TV",
        screenSize = "3840x2160",
        capabilities = listOf(
            "HDMI", "Remote Control", "Voice Control", "Network",
            "Media Playback", "Gaming", "Apps", "Casting"
        )
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> false // TV通常无摄像头
        PlatformCapability.MICROPHONE -> true // 遥控器或语音遥控
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
        PlatformCapability.SCREEN_RECORDING -> true
        PlatformCapability.CONTACTS -> false
        PlatformCapability.CALENDAR -> false
        PlatformCapability.PHOTOS -> true
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> false
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        // TV平台权限请求
        continuation.resume(true)
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return TVPlatformUI()
}

/**
 * TV平台UI实现 - 针对大屏和遥控器优化
 */
class TVPlatformUI : PlatformSpecificUI {
    
    @Composable
    override fun NativeButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean
    ) {
        val focusRequester = remember { FocusRequester() }
        
        Button(
            onClick = onClick,
            modifier = modifier
                .focusRequester(focusRequester)
                .focusable()
                .size(width = 200.dp, height = 60.dp), // TV按钮更大
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF1976D2).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
                focusedElevation = 6.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium, // TV文字更大
                fontWeight = FontWeight.Bold
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
            modifier = modifier
                .focusable()
                .defaultMinSize(minWidth = 300.dp, minHeight = 60.dp),
            placeholder = { 
                Text(
                    placeholder,
                    color = Color(0xFF757575),
                    fontSize = 18.sp
                ) 
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1976D2),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF1976D2)
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
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
            modifier = modifier
                .focusable()
                .size(width = 80.dp, height = 40.dp), // TV开关更大
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF1976D2),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF757575),
                checkedBorderColor = Color(0xFF1976D2),
                uncheckedBorderColor = Color(0xFF757575)
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
            modifier = modifier
                .focusable()
                .height(60.dp), // TV滑块更大
            enabled = enabled,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF1976D2),
                activeTrackColor = Color(0xFF1976D2),
                inactiveTrackColor = Color(0xFF757575)
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
            modifier = modifier.height(8.dp), // TV进度条更粗
            color = Color(0xFF1976D2),
            trackColor = Color(0xFF424242)
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
                    style = MaterialTheme.typography.headlineMedium, // TV标题更大
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge, // TV正文更大
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text(
                        confirmText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text(
                        dismissText,
                        fontSize = 16.sp
                    )
                }
            },
            containerColor = Color(0xFF212121),
            shape = RoundedCornerShape(12.dp)
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
                .padding(48.dp), // TV Toast边距更大
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(24.dp), // TV Toast内边距更大
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium, // TV Toast文字更大
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
        // TV使用全屏对话框而不是底部弹窗
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    actions.forEach { action ->
                        Button(
                            onClick = {
                                action.action()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (action.isDestructive) {
                                    Color(0xFFD32F2F)
                                } else {
                                    Color(0xFF1976D2)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                action.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = action.title,
                                        tint = Color.White
                                    )
                                }
                                Text(
                                    text = action.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text("取消", fontSize = 16.sp)
                }
            },
            containerColor = Color(0xFF212121),
            shape = RoundedCornerShape(12.dp)
        )
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
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        onDismiss()
                    },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text("确定", fontSize = 16.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text("取消", fontSize = 16.sp)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF212121)
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF212121),
                    selectedDayContainerColor = Color(0xFF1976D2),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color(0xFF1976D2)
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
                Button(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        onDismiss()
                    },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text("确定", fontSize = 16.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(width = 120.dp, height = 50.dp)
                ) {
                    Text("取消", fontSize = 16.sp)
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = Color(0xFF1976D2),
                        containerColor = Color(0xFF212121)
                    )
                )
            },
            containerColor = Color(0xFF212121),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * TV特定功能
 */
object TVSpecificFeatures {
    /**
     * 获取TV系统信息
     */
    fun getTVSystemInfo(): Map<String, String> {
        return mapOf(
            "PLATFORM" to "Android TV",
            "VERSION" to "13",
            "API_LEVEL" to "33",
            "MANUFACTURER" to "Samsung",
            "MODEL" to "QN85A",
            "SCREEN_SIZE" to "65 inch",
            "RESOLUTION" to "3840x2160",
            "HDR_SUPPORT" to "HDR10+, Dolby Vision",
            "AUDIO_SUPPORT" to "Dolby Atmos"
        )
    }
    
    /**
     * 检查HDMI功能
     */
    fun checkHDMIFeatures(): Map<String, Boolean> {
        return mapOf(
            "HDMI_CEC" to true,
            "HDMI_ARC" to true,
            "HDMI_eARC" to true,
            "HDMI_2_1" to true,
            "VARIABLE_REFRESH_RATE" to true,
            "AUTO_LOW_LATENCY_MODE" to true
        )
    }
    
    /**
     * 获取遥控器信息
     */
    fun getRemoteControlInfo(): Map<String, String> {
        return mapOf(
            "TYPE" to "Smart Remote",
            "VOICE_CONTROL" to "Supported",
            "MOTION_CONTROL" to "Supported",
            "TOUCHPAD" to "Available",
            "BATTERY_LEVEL" to "85%",
            "CONNECTION" to "Bluetooth 5.0"
        )
    }
    
    /**
     * 检查媒体播放能力
     */
    fun getMediaPlaybackCapabilities(): Map<String, Boolean> {
        return mapOf(
            "H264" to true,
            "H265_HEVC" to true,
            "VP9" to true,
            "AV1" to true,
            "DOLBY_VISION" to true,
            "HDR10_PLUS" to true,
            "DOLBY_ATMOS" to true,
            "DTS_X" to true,
            "4K_60FPS" to true,
            "8K_30FPS" to true
        )
    }
    
    /**
     * 检查游戏功能
     */
    fun getGamingFeatures(): Map<String, Boolean> {
        return mapOf(
            "GAME_MODE" to true,
            "LOW_INPUT_LAG" to true,
            "VARIABLE_REFRESH_RATE" to true,
            "AUTO_LOW_LATENCY_MODE" to true,
            "FREESYNC" to true,
            "GSYNC_COMPATIBLE" to true
        )
    }
}
