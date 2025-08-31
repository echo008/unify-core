# 平台接口 API

## 🌐 平台抽象接口

### PlatformInfo - 平台信息接口

获取当前运行平台的基本信息。

```kotlin
expect class PlatformInfo {
    companion object {
        fun getPlatformName(): String
        fun getDeviceInfo(): String
        fun getSystemVersion(): String
        fun getAppVersion(): String
        fun getBuildNumber(): String
        fun isDebugBuild(): Boolean
    }
}
```

#### 各平台实现

**Android 实现**
```kotlin
actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "Android"
        
        actual fun getDeviceInfo(): String {
            return "${Build.MANUFACTURER} ${Build.MODEL}"
        }
        
        actual fun getSystemVersion(): String {
            return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        }
        
        actual fun getAppVersion(): String {
            val context = getApplicationContext() as Context
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName ?: "Unknown"
        }
        
        actual fun getBuildNumber(): String {
            val context = getApplicationContext() as Context
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.longVersionCode.toString()
        }
        
        actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG
    }
}
```

**iOS 实现**
```kotlin
actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "iOS"
        
        actual fun getDeviceInfo(): String {
            return UIDevice.currentDevice.model
        }
        
        actual fun getSystemVersion(): String {
            return "iOS ${UIDevice.currentDevice.systemVersion}"
        }
        
        actual fun getAppVersion(): String {
            return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "Unknown"
        }
        
        actual fun getBuildNumber(): String {
            return NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "Unknown"
        }
        
        actual fun isDebugBuild(): Boolean {
            #if DEBUG
            return true
            #else
            return false
            #endif
        }
    }
}
```

## 📁 文件系统接口

### FileSystem - 文件操作接口

跨平台的文件系统操作接口。

```kotlin
expect class FileSystem {
    suspend fun readFile(path: String): String?
    suspend fun writeFile(path: String, content: String): Boolean
    suspend fun deleteFile(path: String): Boolean
    suspend fun fileExists(path: String): Boolean
    suspend fun createDirectory(path: String): Boolean
    suspend fun listFiles(directory: String): List<String>
    suspend fun getFileSize(path: String): Long?
    suspend fun getLastModified(path: String): Long?
    suspend fun copyFile(source: String, destination: String): Boolean
    suspend fun moveFile(source: String, destination: String): Boolean
}
```

#### 使用示例
```kotlin
class DocumentManager {
    private val fileSystem = FileSystem()
    
    suspend fun saveDocument(fileName: String, content: String): Boolean {
        val documentsPath = getDocumentsDirectory()
        val filePath = "$documentsPath/$fileName"
        return fileSystem.writeFile(filePath, content)
    }
    
    suspend fun loadDocument(fileName: String): String? {
        val documentsPath = getDocumentsDirectory()
        val filePath = "$documentsPath/$fileName"
        return if (fileSystem.fileExists(filePath)) {
            fileSystem.readFile(filePath)
        } else {
            null
        }
    }
    
    suspend fun listDocuments(): List<String> {
        val documentsPath = getDocumentsDirectory()
        return fileSystem.listFiles(documentsPath)
            .filter { it.endsWith(".txt") || it.endsWith(".md") }
    }
}
```

## 🌐 网络接口

### NetworkManager - 网络状态管理

监控和管理网络连接状态。

```kotlin
expect class NetworkManager {
    suspend fun isNetworkAvailable(): Boolean
    suspend fun getNetworkType(): NetworkType
    fun observeNetworkStatus(callback: (Boolean) -> Unit)
    suspend fun ping(host: String, timeout: Int = 5000): Boolean
}

enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN,
    NONE
}
```

#### 使用示例
```kotlin
@Composable
fun NetworkStatusIndicator() {
    var isOnline by remember { mutableStateOf(true) }
    var networkType by remember { mutableStateOf(NetworkType.UNKNOWN) }
    
    LaunchedEffect(Unit) {
        val networkManager = NetworkManager()
        
        // 初始状态检查
        isOnline = networkManager.isNetworkAvailable()
        networkType = networkManager.getNetworkType()
        
        // 监听网络状态变化
        networkManager.observeNetworkStatus { online ->
            isOnline = online
            if (online) {
                launch {
                    networkType = networkManager.getNetworkType()
                }
            }
        }
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = when {
                !isOnline -> Icons.Default.SignalWifiOff
                networkType == NetworkType.WIFI -> Icons.Default.Wifi
                networkType == NetworkType.CELLULAR -> Icons.Default.SignalCellular4Bar
                else -> Icons.Default.SignalWifiStatusbarNull
            },
            contentDescription = "网络状态",
            tint = if (isOnline) Color.Green else Color.Red
        )
        
        Text(
            text = when {
                !isOnline -> "离线"
                networkType == NetworkType.WIFI -> "WiFi"
                networkType == NetworkType.CELLULAR -> "移动网络"
                else -> "未知网络"
            },
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
```

## 📷 设备能力接口

### CameraManager - 相机管理

跨平台的相机功能接口。

```kotlin
expect class CameraManager {
    suspend fun takePicture(): ByteArray?
    suspend fun selectFromGallery(): ByteArray?
    suspend fun requestCameraPermission(): Boolean
    suspend fun hasCameraPermission(): Boolean
    suspend fun isCameraAvailable(): Boolean
}
```

### LocationManager - 位置服务

获取设备位置信息。

```kotlin
expect class LocationManager {
    suspend fun getCurrentLocation(): Location?
    suspend fun requestLocationPermission(): Boolean
    suspend fun hasLocationPermission(): Boolean
    fun startLocationUpdates(callback: (Location) -> Unit)
    fun stopLocationUpdates()
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

### NotificationManager - 通知管理

发送和管理系统通知。

```kotlin
expect class NotificationManager {
    suspend fun showNotification(
        title: String,
        message: String,
        id: String = UUID.randomUUID().toString()
    )
    suspend fun cancelNotification(id: String)
    suspend fun cancelAllNotifications()
    suspend fun requestNotificationPermission(): Boolean
    suspend fun hasNotificationPermission(): Boolean
}
```

#### 使用示例
```kotlin
class NotificationService {
    private val notificationManager = NotificationManager()
    
    suspend fun sendWelcomeNotification() {
        if (!notificationManager.hasNotificationPermission()) {
            val granted = notificationManager.requestNotificationPermission()
            if (!granted) return
        }
        
        notificationManager.showNotification(
            title = "欢迎使用应用",
            message = "感谢您下载我们的应用！",
            id = "welcome"
        )
    }
    
    suspend fun sendReminderNotification(title: String, message: String) {
        notificationManager.showNotification(
            title = title,
            message = message,
            id = "reminder_${System.currentTimeMillis()}"
        )
    }
}
```

## 🔒 安全接口

### BiometricManager - 生物识别

处理指纹、面部识别等生物识别功能。

```kotlin
expect class BiometricManager {
    suspend fun isBiometricAvailable(): Boolean
    suspend fun authenticate(
        title: String,
        subtitle: String,
        description: String
    ): BiometricResult
}

sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult()
    object Cancelled : BiometricResult()
    object NotAvailable : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}
```

### KeychainManager - 密钥链管理

安全存储敏感数据。

```kotlin
expect class KeychainManager {
    suspend fun store(key: String, value: String): Boolean
    suspend fun retrieve(key: String): String?
    suspend fun delete(key: String): Boolean
    suspend fun clear(): Boolean
}
```

## 📱 平台特定接口

### Android 特定接口

```kotlin
// Android 上下文访问
expect fun getApplicationContext(): Any

// Activity 生命周期
expect class ActivityLifecycleObserver {
    fun onCreate(callback: () -> Unit)
    fun onStart(callback: () -> Unit)
    fun onResume(callback: () -> Unit)
    fun onPause(callback: () -> Unit)
    fun onStop(callback: () -> Unit)
    fun onDestroy(callback: () -> Unit)
}

// 权限管理
expect class PermissionManager {
    suspend fun requestPermission(permission: String): Boolean
    suspend fun requestMultiplePermissions(permissions: List<String>): Map<String, Boolean>
    fun hasPermission(permission: String): Boolean
}
```

### iOS 特定接口

```kotlin
// iOS 视图控制器访问
expect fun getRootViewController(): Any

// iOS 生命周期
expect class ViewControllerLifecycleObserver {
    fun viewDidLoad(callback: () -> Unit)
    fun viewWillAppear(callback: () -> Unit)
    fun viewDidAppear(callback: () -> Unit)
    fun viewWillDisappear(callback: () -> Unit)
    fun viewDidDisappear(callback: () -> Unit)
}

// iOS 设置访问
expect class IOSSettings {
    fun openAppSettings()
    fun canOpenURL(url: String): Boolean
    fun openURL(url: String): Boolean
}
```

### Web 特定接口

```kotlin
// Web 浏览器接口
expect class BrowserManager {
    fun getCurrentURL(): String
    fun navigateTo(url: String)
    fun goBack()
    fun goForward()
    fun reload()
}

// Web 存储接口
expect class WebStorage {
    fun setLocalStorage(key: String, value: String)
    fun getLocalStorage(key: String): String?
    fun removeLocalStorage(key: String)
    fun clearLocalStorage()
    
    fun setSessionStorage(key: String, value: String)
    fun getSessionStorage(key: String): String?
    fun removeSessionStorage(key: String)
    fun clearSessionStorage()
}
```

## 🎵 多媒体接口

### AudioManager - 音频管理

```kotlin
expect class AudioManager {
    suspend fun playSound(resourcePath: String)
    suspend fun playMusic(resourcePath: String)
    suspend fun pauseMusic()
    suspend fun resumeMusic()
    suspend fun stopMusic()
    suspend fun setVolume(volume: Float) // 0.0 - 1.0
    suspend fun getVolume(): Float
}
```

### VideoManager - 视频管理

```kotlin
expect class VideoManager {
    suspend fun playVideo(resourcePath: String)
    suspend fun pauseVideo()
    suspend fun resumeVideo()
    suspend fun stopVideo()
    suspend fun seekTo(position: Long)
    suspend fun getDuration(): Long
    suspend fun getCurrentPosition(): Long
}
```

## 🔄 同步接口

### SyncManager - 数据同步

```kotlin
expect class SyncManager {
    suspend fun syncData(): SyncResult
    suspend fun uploadData(data: Any): Boolean
    suspend fun downloadData(): Any?
    fun observeSyncStatus(callback: (SyncStatus) -> Unit)
}

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class Conflict(val conflicts: List<ConflictItem>) : SyncResult()
}
```

## 📊 分析接口

### AnalyticsManager - 数据分析

```kotlin
expect class AnalyticsManager {
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
    fun trackScreenView(screenName: String, parameters: Map<String, Any> = emptyMap())
    fun setUserProperty(key: String, value: String)
    fun setUserId(userId: String)
    fun logError(error: Throwable, additionalData: Map<String, Any> = emptyMap())
}
```

#### 使用示例
```kotlin
class UserAnalytics {
    private val analytics = AnalyticsManager()
    
    fun trackUserLogin(userId: String, loginMethod: String) {
        analytics.setUserId(userId)
        analytics.trackEvent("user_login", mapOf(
            "method" to loginMethod,
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    fun trackScreenView(screenName: String) {
        analytics.trackScreenView(screenName, mapOf(
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    fun trackError(error: Throwable, context: String) {
        analytics.logError(error, mapOf(
            "context" to context,
            "timestamp" to System.currentTimeMillis()
        ))
    }
}
```

## 🔧 系统集成

### ClipboardManager - 剪贴板管理

```kotlin
expect class ClipboardManager {
    suspend fun copyText(text: String)
    suspend fun pasteText(): String?
    suspend fun hasText(): Boolean
    suspend fun clearClipboard()
}
```

### VibrationManager - 震动管理

```kotlin
expect class VibrationManager {
    suspend fun vibrate(duration: Long = 100)
    suspend fun vibratePattern(pattern: LongArray, repeat: Int = -1)
    suspend fun cancel()
    suspend fun hasVibrator(): Boolean
}
```

### BatteryManager - 电池管理

```kotlin
expect class BatteryManager {
    suspend fun getBatteryLevel(): Float // 0.0 - 1.0
    suspend fun isCharging(): Boolean
    suspend fun getBatteryStatus(): BatteryStatus
    fun observeBatteryStatus(callback: (BatteryInfo) -> Unit)
}

enum class BatteryStatus {
    UNKNOWN,
    CHARGING,
    DISCHARGING,
    NOT_CHARGING,
    FULL
}

data class BatteryInfo(
    val level: Float,
    val isCharging: Boolean,
    val status: BatteryStatus
)
```

---

这些平台接口提供了统一的跨平台API，让您能够在不同平台上访问原生功能，同时保持代码的一致性和可维护性。每个接口都有对应的平台特定实现，确保在各个目标平台上都能正常工作。
