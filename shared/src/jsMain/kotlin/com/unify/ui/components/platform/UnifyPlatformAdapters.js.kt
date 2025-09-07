package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement

/**
 * Web平台特定组件适配器
 * 集成Web原生元素和功能
 */

@Composable
actual fun UnifyPlatformButton(
    onClick: () -> Unit,
    modifier: Modifier,
    text: String,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0066CC) // Web Blue
        )
    ) {
        Text(text)
    }
}

@Composable
actual fun UnifyPlatformTextField(
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
        enabled = enabled,
        placeholder = { Text(placeholder) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0066CC),
            unfocusedBorderColor = Color(0xFFCED4DA)
        )
    )
}

@Composable
actual fun UnifyPlatformSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    enabled: Boolean
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF28A745), // Web Green
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFCED4DA)
        )
    )
}

@Composable
actual fun UnifyPlatformSlider(
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
        valueRange = valueRange,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF0066CC),
            activeTrackColor = Color(0xFF0066CC),
            inactiveTrackColor = Color(0xFFCED4DA)
        )
    )
}

@Composable
actual fun UnifyPlatformProgressBar(
    progress: Float,
    modifier: Modifier,
    showPercentage: Boolean
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0066CC),
            trackColor = Color(0xFFE9ECEF)
        )
        if (showPercentage) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6C757D)
            )
        }
    }
}

@Composable
actual fun UnifyPlatformAlert(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = Color(0xFF0066CC))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消", color = Color(0xFF6C757D))
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color(0xFF6C757D)
    )
}

@Composable
actual fun UnifyPlatformActionSheet(
    title: String,
    actions: List<String>,
    onActionSelected: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    // Web风格的ActionSheet实现
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            actions.forEachIndexed { index, action ->
                TextButton(
                    onClick = { onActionSelected(index) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF0066CC)
                    )
                ) {
                    Text(action)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消", color = Color(0xFF6C757D))
            }
        }
    }
}

@Composable
actual fun UnifyPlatformSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier
) {
    // Web风格的分段控制器
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            OutlinedButton(
                onClick = { onSelectionChanged(index) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color(0xFF0066CC) else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color(0xFF0066CC)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0066CC)).brush
                )
            ) {
                Text(item)
            }
        }
    }
}

@Composable
actual fun UnifyPlatformDatePicker(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier,
    minDate: Long?,
    maxDate: Long?
) {
    // Web风格的日期选择器
    Column(modifier = modifier) {
        Text(
            text = "选择日期",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        Button(
            onClick = {
                // 使用Web原生日期选择器
                val input = document.createElement("input") as HTMLInputElement
                input.type = "date"
                input.onchange = { event ->
                    val dateValue = (event.target as HTMLInputElement).value
                    val timestamp = js("new Date(dateValue).getTime()") as Long
                    onDateSelected(timestamp)
                }
                input.click()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0066CC)
            )
        ) {
            Text("选择日期")
        }
    }
}

// Missing platform adapter functions
actual fun setVolume(volume: Float, streamType: Int) {
    // JavaScript implementation for volume control
    console.log("Setting volume to $volume for stream type $streamType")
}

actual fun observeNetworkStatus(): kotlinx.coroutines.flow.Flow<NetworkType> {
    return kotlinx.coroutines.flow.flowOf(NetworkType.WIFI)
}

actual fun observeBatteryStatus(): kotlinx.coroutines.flow.Flow<BatteryStatus> {
    return kotlinx.coroutines.flow.flowOf(
        BatteryStatus(
            level = 0.85f,
            isCharging = false,
            chargingType = ChargingType.AC,
            temperature = 25.0f,
            voltage = 3,
            health = BatteryHealth.GOOD
        )
    )
}

// Missing platform adapter functions
actual fun shareContent(content: String, title: String, mimeType: String) {
    console.log("Sharing: $title - $content")
}

actual fun showNotification(title: String, content: String, channelId: String, importance: Int) {
    console.log("Notification: $title - $content")
}

actual fun vibrate(duration: Long, amplitude: Int) {
    console.log("Vibrating for ${duration}ms")
}

actual fun toggleFlashlight(enabled: Boolean) {
    console.log("Toggling flashlight: $enabled")
}

actual fun setScreenBrightness(brightness: Float) {
    console.log("Setting brightness to $brightness")
}

// Platform adapter factory
actual object UnifyPlatformAdapterFactory {
    actual fun createAdapter(): UnifyPlatformAdapter {
        return object : UnifyPlatformAdapter {
            override fun getPlatformName(): String = "JavaScript"
            override fun getPlatformVersion(): String = "1.0.0"
            override fun isFeatureSupported(feature: PlatformFeature): Boolean = false
            override fun getDeviceInfo(): DeviceInfo = DeviceInfo(
                deviceId = "js-device",
                deviceName = "Browser",
                manufacturer = "Unknown",
                model = "Web",
                brand = "Browser",
                osVersion = "1.0",
                apiLevel = 1,
                screenWidth = 1920,
                screenHeight = 1080,
                density = 1.0f,
                isTablet = false,
                isEmulator = false,
                totalMemory = 8192L,
                availableMemory = 4096L,
                totalStorage = 1024L,
                availableStorage = 512L
            )
            override fun getSystemInfo(): SystemInfo = SystemInfo(
                platformType = PlatformType.WEB,
                architecture = "x64",
                locale = "en-US",
                timezone = "UTC",
                batteryLevel = 1.0f,
                isCharging = false,
                networkType = NetworkType.WIFI,
                isOnline = true,
                isDarkMode = false,
                systemFeatures = emptyList()
            )
        }
    }
}

actual fun Modifier.platformSpecific(): Modifier {
    return this
}

// Platform controllers as Composable functions
@Composable
actual fun UnifyStatusBarController(
    statusBarColor: androidx.compose.ui.graphics.Color,
    darkIcons: Boolean
) {
    // JS implementation - no-op
}

@Composable
actual fun UnifyNavigationBarController(
    navigationBarColor: androidx.compose.ui.graphics.Color,
    darkIcons: Boolean
) {
    // JS implementation - no-op
}

@Composable
actual fun UnifySystemUIController(
    statusBarColor: androidx.compose.ui.graphics.Color,
    navigationBarColor: androidx.compose.ui.graphics.Color,
    statusBarDarkIcons: Boolean,
    navigationBarDarkIcons: Boolean
) {
    // JS implementation - no-op
}

@Composable
actual fun UnifySafeAreaHandler(
    content: @Composable () -> Unit
) {
    content()
}

@Composable
actual fun UnifyKeyboardHandler(
    onKeyboardVisibilityChanged: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    content()
}

@Composable
actual fun UnifyBackHandler(
    enabled: Boolean,
    onBack: () -> Unit
) {
    // JS back handler implementation
}

@Composable
actual fun UnifyLifecycleHandler(
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDestroy: () -> Unit
) {
    // JS lifecycle handler implementation
}

@Composable
actual fun UnifyPermissionHandler(
    permissions: List<String>,
    onPermissionResult: (Map<String, Boolean>) -> Unit
) {
    LaunchedEffect(permissions) {
        onPermissionResult(permissions.associateWith { true })
    }
}

// Composable components
@Composable
actual fun UnifyFilePicker(
    fileTypes: List<String>,
    multipleSelection: Boolean,
    onFileSelected: (List<String>) -> Unit
) {
    Button(
        onClick = { onFileSelected(listOf("mock_file.txt")) }
    ) {
        Text("Pick File (JS)")
    }
}

@Composable
actual fun UnifyCameraComponent(
    modifier: Modifier,
    onImageCaptured: (ByteArray) -> Unit,
    onError: (String) -> Unit
) {
    Button(
        onClick = { onImageCaptured(byteArrayOf()) },
        modifier = modifier
    ) {
        Text("Capture Image (JS)")
    }
}

@Composable
actual fun UnifyMapComponent(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    onLocationSelected: (Double, Double) -> Unit
) {
    Button(
        onClick = { onLocationSelected(latitude, longitude) },
        modifier = modifier
    ) {
        Text("Map View (JS)")
    }
}

@Composable
actual fun UnifyWebView(
    url: String,
    modifier: Modifier,
    onPageLoaded: (String) -> Unit,
    onError: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text("WebView (JS): $url")
        Button(onClick = { onPageLoaded(url) }) {
            Text("Load")
        }
    }
}

@Composable
actual fun UnifyVideoPlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlaybackStateChanged: (Boolean) -> Unit
) {
    Column(modifier = modifier) {
        Text("Video Player (JS): $url")
        Button(onClick = { onPlaybackStateChanged(true) }) {
            Text("Play")
        }
    }
}

@Composable
actual fun UnifyAudioPlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlaybackStateChanged: (Boolean) -> Unit
) {
    Column(modifier = modifier) {
        Text("Audio Player (JS): $url")
        Button(onClick = { onPlaybackStateChanged(true) }) {
            Text("Play")
        }
    }
}

@Composable
actual fun UnifyQRCodeScanner(
    modifier: Modifier,
    onQRCodeScanned: (String) -> Unit,
    onError: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text("QR Scanner (JS)")
        Button(onClick = { onQRCodeScanned("mock_qr_code") }) {
            Text("Scan")
        }
    }
}

@Composable
actual fun UnifyBiometricAuth(
    title: String,
    subtitle: String,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    onAuthCancel: () -> Unit
) {
    Column {
        Text(title)
        Text(subtitle)
        Button(onClick = onAuthSuccess) {
            Text("Authenticate")
        }
        Button(onClick = onAuthCancel) {
            Text("Cancel")
        }
    }
}
