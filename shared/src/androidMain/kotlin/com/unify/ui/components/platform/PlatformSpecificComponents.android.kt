@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.platform

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android平台特定组件实现
 */

actual fun getPlatformInfo(): PlatformInfo {
    return PlatformInfo(
        name = "Android",
        version = Build.VERSION.RELEASE,
        architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
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
        PlatformCapability.NFC -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1
        PlatformCapability.BIOMETRIC -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        PlatformCapability.PUSH_NOTIFICATIONS -> true
        PlatformCapability.BACKGROUND_PROCESSING -> true
        PlatformCapability.FILE_SYSTEM -> true
        PlatformCapability.NETWORK -> true
        PlatformCapability.SENSORS -> true
        PlatformCapability.VIBRATION -> true
        PlatformCapability.AUDIO_RECORDING -> true
        PlatformCapability.VIDEO_RECORDING -> true
        PlatformCapability.SCREEN_RECORDING -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        PlatformCapability.CONTACTS -> true
        PlatformCapability.CALENDAR -> true
        PlatformCapability.PHOTOS -> true
        PlatformCapability.STORAGE -> true
        PlatformCapability.TELEPHONY -> true
    }
}

actual suspend fun requestPlatformPermission(capability: PlatformCapability): Boolean {
    return suspendCancellableCoroutine { continuation ->
        // 在实际实现中，这里会使用Android的权限请求API
        // 现在返回模拟结果
        continuation.resume(true)
    }
}

actual fun getPlatformSpecificUI(): PlatformSpecificUI {
    return AndroidPlatformUI()
}

/**
 * Android平台UI实现
 */
class AndroidPlatformUI : PlatformSpecificUI {
    
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
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
            placeholder = { Text(placeholder) },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
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
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
            color = color,
            trackColor = color.copy(alpha = 0.3f)
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
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(dismissText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
    
    @Composable
    override fun NativeToast(
        message: String,
        duration: Long,
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current
        
        LaunchedEffect(message) {
            // 使用Android原生Toast
            android.widget.Toast.makeText(
                context,
                message,
                if (duration > 3000) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
            ).show()
            
            kotlinx.coroutines.delay(duration)
            onDismiss()
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
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                actions.forEach { action ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = action.title,
                                color = if (action.isDestructive) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                action.action()
                                onDismiss()
                            }
                            .padding(vertical = 4.dp)
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
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
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
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Android特定权限检查
 */
@Composable
fun checkAndroidPermission(permission: String): Boolean {
    val context = LocalContext.current
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Android特定功能
 */
object AndroidSpecificFeatures {
    /**
     * 获取Android版本信息
     */
    fun getAndroidVersionInfo(): Map<String, String> {
        return mapOf(
            "SDK_INT" to Build.VERSION.SDK_INT.toString(),
            "RELEASE" to Build.VERSION.RELEASE,
            "CODENAME" to Build.VERSION.CODENAME,
            "INCREMENTAL" to Build.VERSION.INCREMENTAL,
            "SECURITY_PATCH" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else {
                "N/A"
            }
        )
    }
    
    /**
     * 获取设备硬件信息
     */
    fun getDeviceHardwareInfo(): Map<String, String> {
        return mapOf(
            "MANUFACTURER" to Build.MANUFACTURER,
            "BRAND" to Build.BRAND,
            "MODEL" to Build.MODEL,
            "DEVICE" to Build.DEVICE,
            "PRODUCT" to Build.PRODUCT,
            "HARDWARE" to Build.HARDWARE,
            "BOARD" to Build.BOARD,
            "SUPPORTED_ABIS" to Build.SUPPORTED_ABIS.joinToString(", ")
        )
    }
    
    /**
     * 检查是否为平板设备
     */
    @Composable
    fun isTablet(): Boolean {
        val context = LocalContext.current
        val configuration = context.resources.configuration
        return configuration.screenLayout and 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK >= 
                android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    /**
     * 获取屏幕密度信息
     */
    @Composable
    fun getScreenDensityInfo(): Map<String, String> {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        
        return mapOf(
            "DENSITY" to displayMetrics.density.toString(),
            "DENSITY_DPI" to displayMetrics.densityDpi.toString(),
            "SCALED_DENSITY" to displayMetrics.scaledDensity.toString(),
            "WIDTH_PIXELS" to displayMetrics.widthPixels.toString(),
            "HEIGHT_PIXELS" to displayMetrics.heightPixels.toString()
        )
    }
}
