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
import kotlin.coroutines.resume

/**
 * 小程序平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    return PlatformInfo(
        name = "MiniApp",
        version = "3.0",
        architecture = "JavaScript",
        deviceModel = "MiniApp Runtime",
        screenSize = "unknown",
        capabilities = listOf(
            "Camera", "GPS", "Network", "Storage", "Payment",
            "Share", "Login", "User Info", "File System"
        )
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> true
        PlatformCapability.MICROPHONE -> true
        PlatformCapability.GPS -> true
        PlatformCapability.BLUETOOTH -> false // 小程序通常不支持
        PlatformCapability.NFC -> false
        PlatformCapability.BIOMETRIC -> false
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> false // 小程序限制
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> true
        PlatformCapability.VIBRATION -> true
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> true
        PlatformCapability.SCREEN_RECORDING -> false
        PlatformCapability.CONTACTS -> true
        PlatformCapability.CALENDAR -> false
        PlatformCapability.PHOTOS -> true
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> false
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        // 模拟小程序权限请求
        continuation.resume(true)
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return MiniAppPlatformUI()
}

/**
 * 小程序平台UI实现
 */
class MiniAppPlatformUI : PlatformSpecificUI {
    
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
                containerColor = Color(0xFF07C160), // 微信绿色
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF07C160).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(4.dp), // 小程序风格
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Normal
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
                    color = Color(0xFF999999)
                ) 
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF07C160),
                unfocusedBorderColor = Color(0xFFE5E5E5),
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                cursorColor = Color(0xFF07C160)
            ),
            shape = RoundedCornerShape(4.dp)
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
                checkedTrackColor = Color(0xFF07C160),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5E5),
                checkedBorderColor = Color(0xFF07C160),
                uncheckedBorderColor = Color(0xFFE5E5E5)
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
                thumbColor = Color(0xFF07C160),
                activeTrackColor = Color(0xFF07C160),
                inactiveTrackColor = Color(0xFFE5E5E5)
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
            color = Color(0xFF07C160),
            trackColor = Color(0xFFE5E5E5)
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
                    color = Color(0xFF333333)
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF07C160)
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
                        contentColor = Color(0xFF999999)
                    )
                ) {
                    Text(dismissText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
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
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
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
            contentColor = Color(0xFF333333),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                actions.forEach { action ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = action.title,
                                color = if (action.isDestructive) {
                                    Color(0xFFFA5151) // 小程序红色
                                } else {
                                    Color(0xFF333333)
                                }
                            )
                        },
                        leadingContent = action.icon?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = action.title,
                                    tint = if (action.isDestructive) {
                                        Color(0xFFFA5151)
                                    } else {
                                        Color(0xFF07C160)
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
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
                        contentColor = Color(0xFF07C160)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF999999)
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
                    selectedDayContainerColor = Color(0xFF07C160),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color(0xFF07C160)
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
                        contentColor = Color(0xFF07C160)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF999999)
                    )
                ) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = Color(0xFF07C160),
                        containerColor = Color.White
                    )
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
 * 小程序特定功能
 */
object MiniAppSpecificFeatures {
    /**
     * 获取小程序环境信息
     */
    fun getMiniAppEnvironment(): Map<String, String> {
        return mapOf(
            "PLATFORM" to "WeChat",
            "VERSION" to "8.0.0",
            "SDK_VERSION" to "3.0.0",
            "BASE_LIBRARY" to "2.32.0",
            "RUNTIME" to "V8"
        )
    }
    
    /**
     * 检查小程序API支持
     */
    fun checkMiniAppAPIs(): Map<String, Boolean> {
        return mapOf(
            "WX_LOGIN" to true,
            "WX_PAY" to true,
            "WX_SHARE" to true,
            "WX_SCAN_CODE" to true,
            "WX_CHOOSE_IMAGE" to true,
            "WX_GET_LOCATION" to true,
            "WX_MAKE_PHONE_CALL" to true,
            "WX_SET_STORAGE" to true,
            "WX_REQUEST" to true,
            "WX_UPLOAD_FILE" to true
        )
    }
    
    /**
     * 获取用户信息
     */
    fun getUserInfo(): Map<String, String> {
        return mapOf(
            "NICKNAME" to "微信用户",
            "AVATAR_URL" to "https://example.com/avatar.jpg",
            "GENDER" to "1",
            "CITY" to "深圳",
            "PROVINCE" to "广东",
            "COUNTRY" to "中国"
        )
    }
    
    /**
     * 检查是否支持分享
     */
    fun canShare(): Boolean {
        return true
    }
    
    /**
     * 检查是否支持支付
     */
    fun canPay(): Boolean {
        return true
    }
    
    /**
     * 获取系统信息
     */
    fun getSystemInfo(): Map<String, String> {
        return mapOf(
            "BRAND" to "iPhone",
            "MODEL" to "iPhone 15 Pro",
            "SYSTEM" to "iOS 17.0",
            "PLATFORM" to "ios",
            "VERSION" to "8.0.0",
            "SCREEN_WIDTH" to "393",
            "SCREEN_HEIGHT" to "852",
            "PIXEL_RATIO" to "3",
            "STATUS_BAR_HEIGHT" to "47",
            "SAFE_AREA" to "{top:47,right:393,bottom:852,left:0}"
        )
    }
}
