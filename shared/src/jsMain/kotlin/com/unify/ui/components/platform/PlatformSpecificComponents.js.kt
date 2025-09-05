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
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Navigator
import kotlin.coroutines.resume

/**
 * Web平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    val navigator = window.navigator
    
    return PlatformInfo(
        name = "Web",
        version = navigator.appVersion,
        architecture = navigator.platform,
        deviceModel = navigator.userAgent,
        screenSize = "${window.screen.width}x${window.screen.height}",
        capabilities = listOf(
            "Camera", "Microphone", "GPS", "Network", "File System",
            "Push Notifications", "Sensors", "Storage"
        )
    )
}

actual fun checkPlatformCapability(capability: PlatformCapability): Boolean {
    return when (capability) {
        PlatformCapability.CAMERA -> js("navigator.mediaDevices && navigator.mediaDevices.getUserMedia") != null
        PlatformCapability.MICROPHONE -> js("navigator.mediaDevices && navigator.mediaDevices.getUserMedia") != null
        PlatformCapability.GPS -> js("navigator.geolocation") != null
        PlatformCapability.BLUETOOTH -> js("navigator.bluetooth") != null
        PlatformCapability.NFC -> js("navigator.nfc") != null
        PlatformCapability.BIOMETRIC -> js("navigator.credentials && navigator.credentials.create") != null
        PlatformCapability.PUSH_NOTIFICATIONS -> js("'Notification' in window") as Boolean
        PlatformCapability.BACKGROUND_PROCESSING -> js("'serviceWorker' in navigator") as Boolean
        PlatformCapability.FILE_SYSTEM -> js("'showOpenFilePicker' in window") as Boolean
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> js("'DeviceMotionEvent' in window") as Boolean
        PlatformCapability.VIBRATION -> js("navigator.vibrate") != null
        PlatformCapability.AUDIO_RECORDING -> js("navigator.mediaDevices && navigator.mediaDevices.getUserMedia") != null
        PlatformCapability.VIDEO_RECORDING -> js("navigator.mediaDevices && navigator.mediaDevices.getUserMedia") != null
        PlatformCapability.SCREEN_RECORDING -> js("navigator.mediaDevices && navigator.mediaDevices.getDisplayMedia") != null
        PlatformCapability.CONTACTS -> js("navigator.contacts") != null
        PlatformCapability.CALENDAR -> false // Web平台通常不支持
        PlatformCapability.PHOTOS -> js("'showOpenFilePicker' in window") as Boolean
        PlatformCapability.STORAGE -> js("'localStorage' in window") as Boolean
        PlatformCapability.TELEPHONY -> false // Web平台不支持
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        when (capability) {
            PlatformCapability.CAMERA, PlatformCapability.MICROPHONE -> {
                // 使用getUserMedia请求权限
                js("""
                    navigator.mediaDevices.getUserMedia({
                        video: capability === 'CAMERA',
                        audio: capability === 'MICROPHONE'
                    }).then(function(stream) {
                        stream.getTracks().forEach(track => track.stop());
                        continuation.resume(true);
                    }).catch(function(error) {
                        continuation.resume(false);
                    });
                """)
            }
            PlatformCapability.GPS -> {
                js("""
                    navigator.geolocation.getCurrentPosition(
                        function(position) { continuation.resume(true); },
                        function(error) { continuation.resume(false); }
                    );
                """)
            }
            PlatformCapability.PUSH_NOTIFICATIONS -> {
                js("""
                    Notification.requestPermission().then(function(permission) {
                        continuation.resume(permission === 'granted');
                    });
                """)
            }
            else -> continuation.resume(true)
        }
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return WebPlatformUI()
}

/**
 * Web平台UI实现
 */
class WebPlatformUI : PlatformSpecificUI {
    
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
                containerColor = Color(0xFF0066CC), // Web蓝色
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF0066CC).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(6.dp), // Web风格圆角
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
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
                    color = Color(0xFF999999)
                ) 
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0066CC),
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color(0xFF0066CC)
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
                checkedTrackColor = Color(0xFF0066CC),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCCCCCC),
                checkedBorderColor = Color(0xFF0066CC),
                uncheckedBorderColor = Color(0xFFCCCCCC)
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
                thumbColor = Color(0xFF0066CC),
                activeTrackColor = Color(0xFF0066CC),
                inactiveTrackColor = Color(0xFFCCCCCC)
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
            color = Color(0xFF0066CC),
            trackColor = Color(0xFFE6E6E6)
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
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF0066CC)
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
                        contentColor = Color(0xFF666666)
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
        // Web风格的Toast通知
        LaunchedEffect(message) {
            // 使用浏览器原生通知API
            js("""
                if ('Notification' in window && Notification.permission === 'granted') {
                    new Notification(message);
                }
            """)
            
            kotlinx.coroutines.delay(duration)
            onDismiss()
        }
        
        // 同时显示页面内Toast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
        // Web平台使用模态对话框实现ActionSheet
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column {
                    actions.forEach { action ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = action.title,
                                    color = if (action.isDestructive) {
                                        Color(0xFFDC3545) // Web红色
                                    } else {
                                        Color(0xFF0066CC)
                                    }
                                )
                            },
                            leadingContent = action.icon?.let { icon ->
                                {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = action.title,
                                        tint = if (action.isDestructive) {
                                            Color(0xFFDC3545)
                                        } else {
                                            Color(0xFF0066CC)
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
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF666666)
                    )
                ) {
                    Text("取消")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
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
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF0066CC)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF666666)
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
                    selectedDayContainerColor = Color(0xFF0066CC),
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = Color(0xFF0066CC)
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
                        contentColor = Color(0xFF0066CC)
                    )
                ) {
                    Text("确定", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF666666)
                    )
                ) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = Color(0xFF0066CC),
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
 * Web特定功能
 */
object WebSpecificFeatures {
    /**
     * 获取浏览器信息
     */
    fun getBrowserInfo(): Map<String, String> {
        val navigator = window.navigator
        
        return mapOf(
            "USER_AGENT" to navigator.userAgent,
            "APP_NAME" to navigator.appName,
            "APP_VERSION" to navigator.appVersion,
            "PLATFORM" to navigator.platform,
            "LANGUAGE" to navigator.language,
            "LANGUAGES" to navigator.languages.joinToString(", "),
            "COOKIE_ENABLED" to navigator.cookieEnabled.toString(),
            "ONLINE" to navigator.onLine.toString()
        )
    }
    
    /**
     * 获取屏幕信息
     */
    fun getScreenInfo(): Map<String, String> {
        val screen = window.screen
        
        return mapOf(
            "WIDTH" to screen.width.toString(),
            "HEIGHT" to screen.height.toString(),
            "AVAILABLE_WIDTH" to screen.availWidth.toString(),
            "AVAILABLE_HEIGHT" to screen.availHeight.toString(),
            "COLOR_DEPTH" to screen.colorDepth.toString(),
            "PIXEL_DEPTH" to screen.pixelDepth.toString(),
            "DEVICE_PIXEL_RATIO" to window.devicePixelRatio.toString()
        )
    }
    
    /**
     * 检查是否为移动设备
     */
    fun isMobileDevice(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("mobile") || 
               userAgent.contains("android") || 
               userAgent.contains("iphone") || 
               userAgent.contains("ipad")
    }
    
    /**
     * 检查是否支持PWA
     */
    fun supportsPWA(): Boolean {
        return js("'serviceWorker' in navigator") as Boolean
    }
    
    /**
     * 检查是否支持WebAssembly
     */
    fun supportsWebAssembly(): Boolean {
        return js("typeof WebAssembly === 'object'") as Boolean
    }
    
    /**
     * 获取网络连接信息
     */
    fun getNetworkInfo(): Map<String, String> {
        val connection = js("navigator.connection || navigator.mozConnection || navigator.webkitConnection")
        
        return if (connection != null) {
            mapOf(
                "EFFECTIVE_TYPE" to (js("connection.effectiveType") as? String ?: "unknown"),
                "DOWNLINK" to (js("connection.downlink") as? String ?: "unknown"),
                "RTT" to (js("connection.rtt") as? String ?: "unknown"),
                "SAVE_DATA" to (js("connection.saveData") as? String ?: "unknown")
            )
        } else {
            mapOf("STATUS" to "Connection API not supported")
        }
    }
    
    /**
     * 检查本地存储支持
     */
    fun checkStorageSupport(): Map<String, Boolean> {
        return mapOf(
            "LOCAL_STORAGE" to (js("typeof Storage !== 'undefined'") as Boolean),
            "SESSION_STORAGE" to (js("typeof sessionStorage !== 'undefined'") as Boolean),
            "INDEXED_DB" to (js("'indexedDB' in window") as Boolean),
            "WEB_SQL" to (js("'openDatabase' in window") as Boolean)
        )
    }
}
