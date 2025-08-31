package com.unify.core.platform

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

/**
 * Android平台管理器生产级实现
 * 支持Android API 24+ (Android 7.0+)
 */
actual object PlatformManager {
    
    private lateinit var context: Context
    private var currentActivity: Activity? = null
    
    actual fun initialize() {
        // Android初始化逻辑在setContext中完成
    }
    
    /**
     * 设置应用上下文和当前Activity
     */
    fun setContext(context: Context, activity: Activity? = null) {
        this.context = context.applicationContext
        this.currentActivity = activity
    }
    
    actual fun getPlatformType(): PlatformType = PlatformType.ANDROID
    
    actual fun getPlatformName(): String = "Android"
    
    actual fun getPlatformVersion(): String = Build.VERSION.RELEASE
    
    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            systemName = "Android",
            systemVersion = Build.VERSION.RELEASE,
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            isEmulator = isEmulator()
        )
    }
    
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }
    
    actual fun getScreenInfo(): ScreenInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            displayMetrics.widthPixels = bounds.width()
            displayMetrics.heightPixels = bounds.height()
            displayMetrics.density = context.resources.displayMetrics.density
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        
        val orientation = when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
            else -> Orientation.UNKNOWN
        }
        
        // 获取安全区域（刘海屏适配）
        val safeAreaInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            currentActivity?.window?.decorView?.rootWindowInsets?.let { insets ->
                val displayCutout = insets.displayCutout
                SafeAreaInsets(
                    top = displayCutout?.safeInsetTop ?: 0,
                    bottom = displayCutout?.safeInsetBottom ?: 0,
                    left = displayCutout?.safeInsetLeft ?: 0,
                    right = displayCutout?.safeInsetRight ?: 0
                )
            } ?: SafeAreaInsets()
        } else {
            SafeAreaInsets()
        }
        
        return ScreenInfo(
            width = displayMetrics.widthPixels,
            height = displayMetrics.heightPixels,
            density = displayMetrics.density,
            orientation = orientation,
            refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.defaultDisplay.refreshRate
            } else {
                60f
            },
            safeAreaInsets = safeAreaInsets
        )
    }
    
    actual fun getSystemCapabilities(): SystemCapabilities {
        val packageManager = context.packageManager
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        return SystemCapabilities(
            isTouchSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN),
            isKeyboardSupported = context.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS,
            isMouseSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH),
            isCameraSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
            isMicrophoneSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE),
            isLocationSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION),
            isNotificationSupported = true,
            isFileSystemSupported = true,
            isBiometricSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
            } else false,
            isNFCSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC),
            isBluetoothSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            supportedSensors = getSupportedSensors(sensorManager)
        )
    }
    
    private fun getSupportedSensors(sensorManager: SensorManager): List<SensorType> {
        val supportedSensors = mutableListOf<SensorType>()
        
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            supportedSensors.add(SensorType.ACCELEROMETER)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            supportedSensors.add(SensorType.GYROSCOPE)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            supportedSensors.add(SensorType.MAGNETOMETER)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            supportedSensors.add(SensorType.PROXIMITY)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            supportedSensors.add(SensorType.LIGHT)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            supportedSensors.add(SensorType.PRESSURE)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            supportedSensors.add(SensorType.TEMPERATURE)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            supportedSensors.add(SensorType.HUMIDITY)
        }
        
        return supportedSensors
    }
    
    actual fun getNetworkStatus(): NetworkStatus {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkStatus.CONNECTED_WIFI
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkStatus.CONNECTED_CELLULAR
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkStatus.CONNECTED_ETHERNET
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true -> NetworkStatus.CONNECTED_UNKNOWN
                else -> NetworkStatus.DISCONNECTED
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo?.isConnected == true) {
                NetworkStatus.CONNECTED_UNKNOWN
            } else {
                NetworkStatus.DISCONNECTED
            }
        }
    }
    
    actual fun observeNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getNetworkStatus())
            }
            
            override fun onLost(network: Network) {
                trySend(NetworkStatus.DISCONNECTED)
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getNetworkStatus())
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(request, callback)
        }
        
        // 发送初始状态
        trySend(getNetworkStatus())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    
    actual fun getStorageInfo(): StorageInfo {
        val internalDir = context.filesDir
        val internalStat = StatFs(internalDir.path)
        
        val blockSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            internalStat.blockSizeLong
        } else {
            @Suppress("DEPRECATION")
            internalStat.blockSize.toLong()
        }
        
        val totalBlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            internalStat.blockCountLong
        } else {
            @Suppress("DEPRECATION")
            internalStat.blockCount.toLong()
        }
        
        val availableBlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            internalStat.availableBlocksLong
        } else {
            @Suppress("DEPRECATION")
            internalStat.availableBlocks.toLong()
        }
        
        val totalSpace = totalBlocks * blockSize
        val availableSpace = availableBlocks * blockSize
        val usedSpace = totalSpace - availableSpace
        
        return StorageInfo(
            totalSpace = totalSpace,
            availableSpace = availableSpace,
            usedSpace = usedSpace,
            isExternalStorageAvailable = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        )
    }
    
    actual fun getPerformanceInfo(): PerformanceInfo {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        val memoryUsage = MemoryUsage(
            totalMemory = maxMemory,
            availableMemory = freeMemory,
            usedMemory = usedMemory,
            appMemoryUsage = usedMemory
        )
        
        return PerformanceInfo(
            cpuUsage = 0f, // CPU使用率需要更复杂的实现
            memoryUsage = memoryUsage,
            batteryLevel = getBatteryLevel(),
            thermalState = ThermalState.NORMAL // 热状态需要更复杂的实现
        )
    }
    
    private fun getBatteryLevel(): Float {
        return try {
            val batteryIntent = context.registerReceiver(null, 
                android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            
            if (level != -1 && scale != -1) {
                level.toFloat() / scale.toFloat() * 100f
            } else {
                -1f
            }
        } catch (e: Exception) {
            -1f
        }
    }
    
    actual suspend fun showNativeDialog(config: DialogConfig): DialogResult = suspendCancellableCoroutine { continuation ->
        val activity = currentActivity ?: run {
            continuation.resume(DialogResult(buttonIndex = -1, cancelled = true))
            return@suspendCancellableCoroutine
        }
        
        val builder = AlertDialog.Builder(activity)
            .setTitle(config.title)
            .setMessage(config.message)
        
        config.buttons.forEachIndexed { index, button ->
            when (index) {
                0 -> builder.setPositiveButton(button.text) { _, _ ->
                    button.action()
                    continuation.resume(DialogResult(buttonIndex = index))
                }
                1 -> builder.setNegativeButton(button.text) { _, _ ->
                    button.action()
                    continuation.resume(DialogResult(buttonIndex = index))
                }
                2 -> builder.setNeutralButton(button.text) { _, _ ->
                    button.action()
                    continuation.resume(DialogResult(buttonIndex = index))
                }
            }
        }
        
        builder.setOnCancelListener {
            continuation.resume(DialogResult(buttonIndex = -1, cancelled = true))
        }
        
        val dialog = builder.create()
        dialog.show()
        
        continuation.invokeOnCancellation {
            dialog.dismiss()
        }
    }
    
    actual suspend fun invokePlatformFeature(feature: PlatformFeature): PlatformResult {
        return try {
            when (feature) {
                is PlatformFeature.Vibrate -> {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                    PlatformResult(success = true)
                }
                
                is PlatformFeature.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(feature.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    PlatformResult(success = true)
                }
                
                is PlatformFeature.ShareContent -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = feature.type
                        putExtra(Intent.EXTRA_TEXT, feature.content)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val chooser = Intent.createChooser(shareIntent, "分享")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                    PlatformResult(success = true)
                }
                
                is PlatformFeature.RequestPermission -> {
                    val hasPermission = ContextCompat.checkSelfPermission(context, feature.permission) == PackageManager.PERMISSION_GRANTED
                    PlatformResult(success = hasPermission, data = hasPermission)
                }
                
                else -> PlatformResult(success = false, error = "不支持的功能: ${feature::class.simpleName}")
            }
        } catch (e: Exception) {
            PlatformResult(success = false, error = e.message)
        }
    }
    
    actual fun getPlatformConfig(): PlatformConfig {
        return PlatformConfig(
            platformType = PlatformType.ANDROID,
            supportedFeatures = setOf(
                "vibration", "camera", "location", "notifications", 
                "biometric", "nfc", "bluetooth", "file_system",
                "share", "deep_links", "background_tasks"
            ),
            limitations = setOf(
                "no_file_system_root_access",
                "permission_based_features"
            ),
            optimizations = mapOf(
                "material_design" to true,
                "adaptive_icons" to true,
                "edge_to_edge" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q),
                "dynamic_colors" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            )
        )
    }
}
