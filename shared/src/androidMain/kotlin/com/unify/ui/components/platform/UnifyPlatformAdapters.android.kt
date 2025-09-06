@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.platform

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Actual implementations for expect functions

@Composable
actual fun UnifyWebView(
    url: String,
    modifier: Modifier,
    onPageLoaded: (String) -> Unit,
    onError: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("WebView: $url")
            Button(
                onClick = { onPageLoaded(url) }
            ) {
                Text("Load Page")
            }
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Video Player: $url")
            Text("Auto Play: $autoPlay")
            Text("Show Controls: $showControls")
            Button(
                onClick = { onPlaybackStateChanged(true) }
            ) {
                Text("Play")
            }
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Audio Player: $url")
            Text("Auto Play: $autoPlay")
            Button(
                onClick = { onPlaybackStateChanged(true) }
            ) {
                Text("Play Audio")
            }
        }
    }
}

@Composable
actual fun UnifyQRCodeScanner(
    modifier: Modifier,
    onQRCodeScanned: (String) -> Unit,
    onError: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("QR Code Scanner")
            Button(
                onClick = { onQRCodeScanned("sample_qr_code") }
            ) {
                Text("Scan QR Code")
            }
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Biometric Auth")
            Text("Title: $title")
            Text("Subtitle: $subtitle")
            Button(
                onClick = onAuthSuccess
            ) {
                Text("Authenticate")
            }
        }
    }
}

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
        enabled = enabled
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
        placeholder = { Text(placeholder) },
        enabled = enabled
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
        enabled = enabled
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
        enabled = enabled
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
            modifier = Modifier.fillMaxWidth()
        )
        if (showPercentage) {
            Text("${(progress * 100).toInt()}%")
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
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        },
        modifier = modifier
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title)
            actions.forEachIndexed { index, action ->
                Button(
                    onClick = { 
                        onActionSelected(index)
                        onCancel()
                    }
                ) {
                    Text(action)
                }
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
    Row(
        modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            Button(
                onClick = { onSelectionChanged(index) },
                colors = if (index == selectedIndex) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Date Picker")
            Text("Selected: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(selectedDate)}")
            Button(
                onClick = { 
                    onDateSelected(System.currentTimeMillis())
                }
            ) {
                Text("Select Date")
            }
        }
    }
}

actual fun shareContent(
    content: String,
    title: String,
    mimeType: String
) {
    // Android share implementation
}

actual fun showNotification(
    title: String,
    content: String,
    channelId: String,
    importance: Int
) {
    // Android notification implementation
}

actual fun vibrate(
    duration: Long,
    amplitude: Int
) {
    // Android vibration implementation
}

actual fun toggleFlashlight(enabled: Boolean) {
    // Android flashlight implementation
}

actual fun setScreenBrightness(brightness: Float) {
    // Android screen brightness implementation
}

actual fun setVolume(volume: Float, streamType: Int) {
    // Android volume control implementation
}

actual fun observeNetworkStatus(): kotlinx.coroutines.flow.Flow<NetworkType> {
    return kotlinx.coroutines.flow.flowOf(NetworkType.WIFI)
}

actual fun observeBatteryStatus(): kotlinx.coroutines.flow.Flow<BatteryStatus> {
    return kotlinx.coroutines.flow.flowOf(
        BatteryStatus(
            level = 85.0f,
            isCharging = false,
            chargingType = ChargingType.NONE,
            temperature = 25.0f,
            voltage = 4000,
            health = BatteryHealth.GOOD
        )
    )
}

// Additional Platform Adapter actual implementations removed to avoid duplication
// UnifyPlatformAdapterFactory is implemented in UnifyPlatformAdapterFactory.android.kt

actual fun Modifier.platformSpecific(): Modifier {
    return this
}

@Composable
actual fun UnifyStatusBarController(
    statusBarColor: androidx.compose.ui.graphics.Color,
    darkIcons: Boolean
) {
    // Android status bar control implementation
}

@Composable
actual fun UnifyNavigationBarController(
    navigationBarColor: androidx.compose.ui.graphics.Color,
    darkIcons: Boolean
) {
    // Android navigation bar control implementation
}

@Composable
actual fun UnifySystemUIController(
    statusBarColor: androidx.compose.ui.graphics.Color,
    navigationBarColor: androidx.compose.ui.graphics.Color,
    statusBarDarkIcons: Boolean,
    navigationBarDarkIcons: Boolean
) {
    // Android system UI control implementation
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
    // Android back handler implementation
}

@Composable
actual fun UnifyLifecycleHandler(
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDestroy: () -> Unit
) {
    // Android lifecycle handler implementation
}

@Composable
actual fun UnifyPermissionHandler(
    permissions: List<String>,
    onPermissionResult: (Map<String, Boolean>) -> Unit
) {
    val resultMap = permissions.associateWith { true }
    androidx.compose.runtime.LaunchedEffect(permissions) {
        onPermissionResult(resultMap)
    }
}

@Composable
actual fun UnifyFilePicker(
    fileTypes: List<String>,
    multipleSelection: Boolean,
    onFileSelected: (List<String>) -> Unit
) {
    Button(
        onClick = { 
            val files = if (multipleSelection) {
                listOf("file1.txt", "file2.txt")
            } else {
                listOf("sample_file.txt")
            }
            onFileSelected(files)
        }
    ) {
        Text("Pick File")
    }
}

@Composable
actual fun UnifyCameraComponent(
    modifier: Modifier,
    onImageCaptured: (ByteArray) -> Unit,
    onError: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Camera Component")
            Button(
                onClick = { onImageCaptured(byteArrayOf(1, 2, 3)) }
            ) {
                Text("Capture Image")
            }
        }
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Map Component")
            Text("Lat: $latitude, Lng: $longitude")
            Text("Zoom: $zoom")
            Button(
                onClick = { onLocationSelected(latitude + 0.1, longitude + 0.1) }
            ) {
                Text("Select Location")
            }
        }
    }
}

/**
 * Android平台特定的UI适配器组件
 */
object AndroidPlatformAdapters {
    
    /**
     * Android Material Design 按钮适配器
     */
    @Composable
    fun MaterialButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: ButtonColors = ButtonDefaults.buttonColors()
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp
            )
        }
    }
    
    /**
     * Android Material Design 卡片适配器
     */
    @Composable
    fun MaterialCard(
        modifier: Modifier = Modifier,
        colors: CardColors = CardDefaults.cardColors(),
        elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier,
            colors = colors,
            elevation = elevation,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
    
    /**
     * Android Material Design 输入框适配器
     */
    @Composable
    fun MaterialTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        isError: Boolean = false,
        supportingText: String? = null
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            shape = RoundedCornerShape(8.dp)
        )
    }
    
    /**
     * Android Material Design 开关适配器
     */
    @Composable
    fun MaterialSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: SwitchColors = SwitchDefaults.colors()
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = colors
        )
    }
    
    /**
     * Android Material Design 进度条适配器
     */
    @Composable
    fun MaterialProgressIndicator(
        progress: Float,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = color,
            trackColor = trackColor
        )
    }
    
    /**
     * Android Material Design 圆形进度条适配器
     */
    @Composable
    fun MaterialCircularProgressIndicator(
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
    ) {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth
        )
    }
    
    /**
     * Android Material Design 芯片适配器
     */
    @Composable
    fun MaterialChip(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        enabled: Boolean = true
    ) {
        FilterChip(
            onClick = onClick,
            label = { Text(text) },
            selected = selected,
            modifier = modifier,
            enabled = enabled
        )
    }
    
    /**
     * Android Material Design 浮动操作按钮适配器
     */
    @Composable
    fun MaterialFloatingActionButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        content: @Composable () -> Unit
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            content = content
        )
    }
    
    /**
     * Android Material Design 底部导航栏适配器
     */
    @Composable
    fun MaterialBottomNavigation(
        selectedIndex: Int,
        onItemSelected: (Int) -> Unit,
        items: List<BottomNavItem>,
        modifier: Modifier = Modifier
    ) {
        NavigationBar(
            modifier = modifier
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
    
    /**
     * Android Material Design 顶部应用栏适配器
     */
    @Composable
    fun MaterialTopAppBar(
        title: String,
        modifier: Modifier = Modifier,
        navigationIcon: (@Composable () -> Unit)? = null,
        actions: @Composable RowScope.() -> Unit = {},
        colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
    ) {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = colors
        )
    }
    
    /**
     * Android Material Design 对话框适配器
     */
    @Composable
    fun MaterialDialog(
        onDismissRequest: () -> Unit,
        title: String,
        content: @Composable () -> Unit,
        confirmButton: @Composable () -> Unit,
        dismissButton: @Composable (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = content,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            modifier = modifier
        )
    }
    
    /**
     * Android Material Design 底部表单适配器
     */
    @Composable
    fun MaterialBottomSheet(
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
        dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
        content: @Composable ColumnScope.() -> Unit
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            dragHandle = dragHandle,
            content = content
        )
    }
    
    /**
     * Android 系统状态栏颜色适配器
     */
    @Composable
    fun SystemBarsColorAdapter(
        statusBarColor: Color = MaterialTheme.colorScheme.surface,
        navigationBarColor: Color = MaterialTheme.colorScheme.surface,
        darkIcons: Boolean = !isSystemInDarkTheme()
    ) {
        val context = LocalContext.current
        
        LaunchedEffect(statusBarColor, navigationBarColor, darkIcons) {
            // 在实际应用中，这里会设置系统状态栏颜色
            // 需要使用 WindowCompat.setDecorFitsSystemWindows() 等API
        }
    }
    
    /**
     * Android 权限请求适配器
     */
    @Composable
    fun PermissionRequestAdapter(
        permission: String,
        onPermissionResult: (Boolean) -> Unit,
        content: @Composable (requestPermission: () -> Unit) -> Unit
    ) {
        val context = LocalContext.current
        
        content { 
            // 在实际应用中，这里会使用 ActivityResultContracts.RequestPermission()
            // 进行权限请求
            onPermissionResult(true) // 简化实现
        }
    }
}

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

/**
 * Android平台特定的主题适配器
 */
object AndroidThemeAdapter {
    
    /**
     * Material Design 3 主题适配器
     */
    @Composable
    fun MaterialTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
    ) {
        val colorScheme = when {
            dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
        
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

/**
 * Android平台特定的工具函数
 */
object AndroidPlatformUtils {
    
    /**
     * 获取屏幕密度
     */
    @Composable
    fun getScreenDensity(): Float {
        val context = LocalContext.current
        return context.resources.displayMetrics.density
    }
    
    /**
     * 获取屏幕尺寸
     */
    @Composable
    fun getScreenSize(): Pair<Int, Int> {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    
    /**
     * 检查是否为平板设备
     */
    @Composable
    fun isTablet(): Boolean {
        val context = LocalContext.current
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
        return screenLayout >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    /**
     * 获取系统语言
     */
    @Composable
    fun getSystemLanguage(): String {
        val context = LocalContext.current
        return context.resources.configuration.locales[0].language
    }
}
