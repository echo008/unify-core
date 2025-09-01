# Unify-Core 统一API接口规范

## 🎯 API设计原则

### 1. 平台无关性原则
- 所有API接口必须在各目标平台上提供一致的行为
- 平台特定功能通过可选参数或扩展接口提供
- 使用expect/actual机制处理平台差异
- 提供统一的错误处理和异常机制

### 2. 渐进式增强原则
- 核心功能在所有平台上可用
- 高级功能根据平台能力选择性启用
- 提供功能检测API判断平台支持情况
- 实现优雅的功能降级机制

### 3. 类型安全原则
- 使用强类型定义所有API接口
- 利用Kotlin的null安全特性
- 通过密封类处理状态和错误
- 使用泛型提供类型安全的数据操作

### 4. 性能优化原则
- 最小化跨平台抽象层开销
- 支持异步操作和协程
- 提供内存和性能监控接口
- 实现智能缓存和预加载机制

### 5. 扩展性原则
- 支持插件化架构
- 提供自定义组件注册机制
- 允许平台特定功能扩展
- 支持第三方库集成

## 📱 核心API接口定义

### 1. 平台信息API

```kotlin
// 平台信息接口
interface PlatformInfo {
    val platformType: PlatformType
    val platformVersion: String
    val deviceModel: String
    val screenSize: ScreenSize
    val capabilities: Set<PlatformCapability>
}

// 平台类型枚举
enum class PlatformType {
    ANDROID,
    IOS,
    HARMONY_OS,
    WINDOWS,
    MACOS,
    LINUX,
    WEB,
    WECHAT_MINIAPP,
    ALIPAY_MINIAPP,
    BYTEDANCE_MINIAPP,
    BAIDU_MINIAPP,
    KUAISHOU_MINIAPP,
    XIAOMI_MINIAPP,
    HUAWEI_MINIAPP,
    QQ_MINIAPP,
    WATCH_OS,
    WEAR_OS,
    HARMONY_WATCH,
    ANDROID_TV,
    APPLE_TV,
    HARMONY_TV
}

// 平台能力枚举
enum class PlatformCapability {
    // 基础硬件能力
    CAMERA,
    MICROPHONE,
    SPEAKER,
    LOCATION,
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    PROXIMITY_SENSOR,
    LIGHT_SENSOR,
    
    // 连接能力
    WIFI,
    CELLULAR,
    BLUETOOTH,
    NFC,
    USB,
    
    // 系统能力
    PUSH_NOTIFICATION,
    LOCAL_NOTIFICATION,
    FILE_SYSTEM,
    SECURE_STORAGE,
    BIOMETRIC_AUTH,
    HAPTIC_FEEDBACK,
    
    // 媒体能力
    AUDIO_RECORDING,
    VIDEO_RECORDING,
    PHOTO_CAPTURE,
    MEDIA_PLAYBACK,
    
    // 平台特定能力
    DISTRIBUTED_CAPABILITY, // HarmonyOS
    SIRI_SHORTCUTS,        // iOS
    WIDGETS,               // Android/iOS
    LIVE_ACTIVITIES,       // iOS
    DYNAMIC_ISLANDS,       // iOS
    SPLIT_SCREEN,          // Android/Desktop
    PICTURE_IN_PICTURE,    // Android/Web
    
    // 开发调试能力
    HOT_RELOAD,
    PERFORMANCE_MONITORING,
    CRASH_REPORTING,
    
    // 高级功能
    PAYMENT,
    SOCIAL_SHARE,
    VOICE_RECOGNITION,
    BACKGROUND_PROCESSING
}

// 屏幕尺寸信息
data class ScreenSize(
    val width: Int,
    val height: Int,
    val density: Float,
    val sizeClass: ScreenSizeClass,
    val orientation: ScreenOrientation,
    val safeAreaInsets: SafeAreaInsets
)

// 屏幕尺寸分类
enum class ScreenSizeClass {
    COMPACT,    // 手机竖屏
    MEDIUM,     // 手机横屏/小平板
    EXPANDED,   // 平板
    LARGE,      // 桌面
    EXTRA_LARGE // 大屏幕
}

// 屏幕方向
enum class ScreenOrientation {
    PORTRAIT,
    LANDSCAPE,
    PORTRAIT_REVERSE,
    LANDSCAPE_REVERSE
}

// 安全区域边距
data class SafeAreaInsets(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int
)

// 统一结果类型
sealed class UnifyResult<out T> {
    data class Success<T>(val data: T) : UnifyResult<T>()
    data class Error(val exception: UnifyException) : UnifyResult<Nothing>()
    object Loading : UnifyResult<Nothing>()
}

// 统一异常类型
sealed class UnifyException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class PlatformException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class PermissionException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class ValidationException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class ConfigurationException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class UnsupportedOperationException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
}

// 平台能力检测接口
expect object PlatformCapabilityDetector {
    fun hasCapability(capability: PlatformCapability): Boolean
    fun getCapabilityDetails(capability: PlatformCapability): CapabilityDetails?
    suspend fun requestCapability(capability: PlatformCapability): CapabilityRequestResult
}

// 能力详情
data class CapabilityDetails(
    val capability: PlatformCapability,
    val isAvailable: Boolean,
    val version: String?,
    val limitations: List<String>,
    val permissions: List<String>
)

// 能力请求结果
sealed class CapabilityRequestResult {
    object Granted : CapabilityRequestResult()
    object Denied : CapabilityRequestResult()
    data class Restricted(val reason: String) : CapabilityRequestResult()
    data class Error(val exception: UnifyException) : CapabilityRequestResult()
}
```

### 2. 存储API

```kotlin
// 统一存储接口
interface UnifyStorage {
    // 基础数据类型存储
    suspend fun getString(key: String): String?
    suspend fun setString(key: String, value: String)
    suspend fun getInt(key: String): Int?
    suspend fun setInt(key: String, value: Int)
    suspend fun getLong(key: String): Long?
    suspend fun setLong(key: String, value: Long)
    suspend fun getFloat(key: String): Float?
    suspend fun setFloat(key: String, value: Float)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun setBoolean(key: String, value: Boolean)
    
    // 复杂数据类型存储
    suspend fun <T> getObject(key: String, clazz: KClass<T>): T?
    suspend fun <T> setObject(key: String, value: T)
    suspend fun getByteArray(key: String): ByteArray?
    suspend fun setByteArray(key: String, value: ByteArray)
    
    // 批量操作
    suspend fun getAll(): Map<String, Any?>
    suspend fun setAll(data: Map<String, Any?>)
    suspend fun remove(key: String)
    suspend fun removeAll(keys: List<String>)
    suspend fun clear()
    
    // 存储监听
    fun observeKey(key: String): Flow<Any?>
    fun observeAll(): Flow<Map<String, Any?>>
}

// 安全存储接口（用于敏感数据）
interface UnifySecureStorage : UnifyStorage {
    suspend fun setSecure(key: String, value: String, requireAuth: Boolean = false)
    suspend fun getSecure(key: String, requireAuth: Boolean = false): String?
    suspend fun removeSecure(key: String)
    suspend fun clearSecure()
}

// 文件存储接口
interface UnifyFileStorage {
    suspend fun writeFile(path: String, content: ByteArray): UnifyResult<Unit>
    suspend fun readFile(path: String): UnifyResult<ByteArray>
    suspend fun deleteFile(path: String): UnifyResult<Unit>
    suspend fun listFiles(directory: String): UnifyResult<List<FileInfo>>
    suspend fun createDirectory(path: String): UnifyResult<Unit>
    suspend fun deleteDirectory(path: String, recursive: Boolean = false): UnifyResult<Unit>
    suspend fun getFileInfo(path: String): UnifyResult<FileInfo>
    suspend fun copyFile(source: String, destination: String): UnifyResult<Unit>
    suspend fun moveFile(source: String, destination: String): UnifyResult<Unit>
    
    // 流式操作
    suspend fun openInputStream(path: String): UnifyResult<InputStream>
    suspend fun openOutputStream(path: String): UnifyResult<OutputStream>
}

// 文件信息
data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val permissions: FilePermissions
)

// 文件权限
data class FilePermissions(
    val readable: Boolean,
    val writable: Boolean,
    val executable: Boolean
)
```

### 3. 网络通信API

```kotlin
// HTTP客户端接口
interface UnifyHttpClient {
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): UnifyResult<HttpResponse>
    
    suspend fun post(
        url: String,
        body: RequestBody,
        headers: Map<String, String> = emptyMap()
    ): UnifyResult<HttpResponse>
    
    suspend fun put(
        url: String,
        body: RequestBody,
        headers: Map<String, String> = emptyMap()
    ): UnifyResult<HttpResponse>
    
    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): UnifyResult<HttpResponse>
    
    suspend fun patch(
        url: String,
        body: RequestBody,
        headers: Map<String, String> = emptyMap()
    ): UnifyResult<HttpResponse>
    
    // 文件上传下载
    suspend fun uploadFile(
        url: String,
        file: ByteArray,
        fileName: String,
        mimeType: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): UnifyResult<HttpResponse>
    
    suspend fun downloadFile(
        url: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): UnifyResult<ByteArray>
    
    // WebSocket支持
    suspend fun connectWebSocket(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): UnifyResult<WebSocketConnection>
}

// HTTP响应
data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: ByteArray,
    val isSuccessful: Boolean
) {
    fun bodyAsString(): String = body.decodeToString()
    inline fun <reified T> bodyAsJson(): T = Json.decodeFromString(bodyAsString())
}

// 请求体
sealed class RequestBody {
    data class Text(val content: String, val contentType: String = "text/plain") : RequestBody()
    data class Json(val content: String) : RequestBody()
    data class Binary(val content: ByteArray, val contentType: String) : RequestBody()
    data class Form(val fields: Map<String, String>) : RequestBody()
    data class MultiPart(val parts: List<MultiPartData>) : RequestBody()
}

// 多部分数据
sealed class MultiPartData {
    data class Text(val name: String, val value: String) : MultiPartData()
    data class File(val name: String, val fileName: String, val content: ByteArray, val contentType: String) : MultiPartData()
}

// WebSocket连接
interface WebSocketConnection {
    suspend fun send(message: String)
    suspend fun send(data: ByteArray)
    suspend fun close(code: Int = 1000, reason: String = "")
    val messages: Flow<WebSocketMessage>
    val connectionState: StateFlow<WebSocketState>
}

// WebSocket消息
sealed class WebSocketMessage {
    data class Text(val content: String) : WebSocketMessage()
    data class Binary(val content: ByteArray) : WebSocketMessage()
}

// WebSocket状态
enum class WebSocketState {
    CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, ERROR
}

// 网络状态监控
interface UnifyNetworkMonitor {
    val isConnected: StateFlow<Boolean>
    val connectionType: StateFlow<ConnectionType>
    val connectionQuality: StateFlow<ConnectionQuality>
    
    suspend fun ping(host: String, timeout: Long = 5000): UnifyResult<Long>
    suspend fun getNetworkInfo(): UnifyResult<NetworkInfo>
}

// 连接类型
enum class ConnectionType {
    WIFI, CELLULAR_5G, CELLULAR_4G, CELLULAR_3G, CELLULAR_2G, ETHERNET, BLUETOOTH, NONE
}

// 连接质量
enum class ConnectionQuality {
    EXCELLENT, GOOD, FAIR, POOR, UNKNOWN
}

// 网络信息
data class NetworkInfo(
    val connectionType: ConnectionType,
    val connectionQuality: ConnectionQuality,
    val isMetered: Boolean,
    val signalStrength: Int?, // 0-100
    val ipAddress: String?,
    val ssid: String? // WiFi名称
)
```

### 4. 设备管理API

```kotlin
// 设备信息接口
interface UnifyDeviceManager {
    suspend fun getDeviceInfo(): UnifyResult<DeviceInfo>
    suspend fun getBatteryInfo(): UnifyResult<BatteryInfo>
    suspend fun getMemoryInfo(): UnifyResult<MemoryInfo>
    suspend fun getStorageInfo(): UnifyResult<StorageInfo>
    suspend fun getCpuInfo(): UnifyResult<CpuInfo>
    
    // 设备状态监控
    val batteryLevel: StateFlow<Float>
    val isCharging: StateFlow<Boolean>
    val memoryUsage: StateFlow<Float>
    val cpuUsage: StateFlow<Float>
    val thermalState: StateFlow<ThermalState>
    
    // 设备控制
    suspend fun vibrate(pattern: VibrationPattern): UnifyResult<Unit>
    suspend fun setScreenBrightness(level: Float): UnifyResult<Unit>
    suspend fun keepScreenOn(keep: Boolean): UnifyResult<Unit>
    suspend fun setOrientation(orientation: ScreenOrientation): UnifyResult<Unit>
}

// 设备信息
data class DeviceInfo(
    val deviceId: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val systemName: String,
    val systemVersion: String,
    val appVersion: String,
    val buildNumber: String,
    val isPhysicalDevice: Boolean,
    val supportedAbis: List<String>
)

// 电池信息
data class BatteryInfo(
    val level: Float, // 0.0 - 1.0
    val isCharging: Boolean,
    val chargingType: ChargingType,
    val health: BatteryHealth,
    val temperature: Float?, // 摄氏度
    val voltage: Float? // 伏特
)

// 充电类型
enum class ChargingType {
    NONE, AC, USB, WIRELESS, UNKNOWN
}

// 电池健康状态
enum class BatteryHealth {
    GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, UNKNOWN
}

// 内存信息
data class MemoryInfo(
    val totalMemory: Long, // 字节
    val availableMemory: Long, // 字节
    val usedMemory: Long, // 字节
    val memoryUsage: Float // 0.0 - 1.0
)

// 存储信息
data class StorageInfo(
    val totalStorage: Long, // 字节
    val availableStorage: Long, // 字节
    val usedStorage: Long, // 字节
    val storageUsage: Float // 0.0 - 1.0
)

// CPU信息
data class CpuInfo(
    val architecture: String,
    val coreCount: Int,
    val maxFrequency: Long, // Hz
    val currentUsage: Float // 0.0 - 1.0
)

// 热状态
enum class ThermalState {
    NOMINAL, FAIR, SERIOUS, CRITICAL, UNKNOWN
}

// 振动模式
sealed class VibrationPattern {
    data class Duration(val milliseconds: Long) : VibrationPattern()
    data class Pattern(val timings: LongArray, val repeat: Int = -1) : VibrationPattern()
    object Light : VibrationPattern()
    object Medium : VibrationPattern()
    object Heavy : VibrationPattern()
    object Success : VibrationPattern()
    object Warning : VibrationPattern()
    object Error : VibrationPattern()
}
```

### 5. 权限管理API

```kotlin
// 权限管理接口
interface UnifyPermissionManager {
    suspend fun checkPermission(permission: UnifyPermission): PermissionStatus
    suspend fun checkPermissions(permissions: List<UnifyPermission>): Map<UnifyPermission, PermissionStatus>
    suspend fun requestPermission(permission: UnifyPermission): PermissionResult
    suspend fun requestPermissions(permissions: List<UnifyPermission>): Map<UnifyPermission, PermissionResult>
    suspend fun shouldShowRationale(permission: UnifyPermission): Boolean
    suspend fun openAppSettings()
    
    // 权限状态监听
    fun observePermission(permission: UnifyPermission): Flow<PermissionStatus>
}

// 权限类型
enum class UnifyPermission {
    // 基础权限
    CAMERA,
    MICROPHONE,
    LOCATION_COARSE,
    LOCATION_FINE,
    LOCATION_BACKGROUND,
    
    // 存储权限
    READ_EXTERNAL_STORAGE,
    WRITE_EXTERNAL_STORAGE,
    MANAGE_EXTERNAL_STORAGE,
    
    // 通信权限
    PHONE,
    SMS,
    CONTACTS,
    CALL_LOG,
    
    // 传感器权限
    BODY_SENSORS,
    ACTIVITY_RECOGNITION,
    
    // 系统权限
    SYSTEM_ALERT_WINDOW,
    WRITE_SETTINGS,
    INSTALL_PACKAGES,
    
    // 通知权限
    POST_NOTIFICATIONS,
    
    // 生物识别权限
    USE_BIOMETRIC,
    USE_FINGERPRINT,
    
    // 平台特定权限
    BLUETOOTH_SCAN,
    BLUETOOTH_ADVERTISE,
    BLUETOOTH_CONNECT,
    NEARBY_WIFI_DEVICES,
    
    // iOS特定
    PHOTO_LIBRARY,
    PHOTO_LIBRARY_ADD_ONLY,
    MEDIA_LIBRARY,
    APPLE_MUSIC,
    SPEECH_RECOGNITION,
    SIRI,
    
    // HarmonyOS特定
    DISTRIBUTED_DATASYNC,
    DISTRIBUTED_VIRTUALDEVICE
}

// 权限状态
enum class PermissionStatus {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_DETERMINED,
    RESTRICTED
}

// 权限请求结果
sealed class PermissionResult {
    object Granted : PermissionResult()
    object Denied : PermissionResult()
    object PermanentlyDenied : PermissionResult()
    data class ShowRationale(val message: String) : PermissionResult()
    data class Error(val exception: UnifyException) : PermissionResult()
}
```

### 6. 媒体处理API

```kotlin
// 媒体管理接口
interface UnifyMediaManager {
    // 图片处理
    suspend fun pickImage(source: ImageSource): UnifyResult<ImageData>
    suspend fun captureImage(options: CameraOptions): UnifyResult<ImageData>
    suspend fun processImage(image: ImageData, operations: List<ImageOperation>): UnifyResult<ImageData>
    
    // 视频处理
    suspend fun pickVideo(source: VideoSource): UnifyResult<VideoData>
    suspend fun captureVideo(options: VideoOptions): UnifyResult<VideoData>
    suspend fun processVideo(video: VideoData, operations: List<VideoOperation>): UnifyResult<VideoData>
    
    // 音频处理
    suspend fun recordAudio(options: AudioOptions): UnifyResult<AudioData>
    suspend fun playAudio(audio: AudioData, options: PlaybackOptions): UnifyResult<Unit>
    suspend fun processAudio(audio: AudioData, operations: List<AudioOperation>): UnifyResult<AudioData>
}

// 图片数据
data class ImageData(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val metadata: Map<String, Any>
)

// 图片格式
enum class ImageFormat {
    JPEG, PNG, WEBP, HEIC, BMP, GIF
}

// 图片来源
enum class ImageSource {
    CAMERA, GALLERY, FILES
}

// 相机选项
data class CameraOptions(
    val quality: Float = 0.8f,
    val maxWidth: Int? = null,
    val maxHeight: Int? = null,
    val enableEditing: Boolean = false,
    val cameraType: CameraType = CameraType.REAR
)

// 相机类型
enum class CameraType {
    REAR, FRONT
}

// 图片操作
sealed class ImageOperation {
    data class Resize(val width: Int, val height: Int) : ImageOperation()
    data class Crop(val x: Int, val y: Int, val width: Int, val height: Int) : ImageOperation()
    data class Rotate(val degrees: Float) : ImageOperation()
    data class Compress(val quality: Float) : ImageOperation()
    data class Filter(val filterType: FilterType) : ImageOperation()
}

// 滤镜类型
enum class FilterType {
    NONE, GRAYSCALE, SEPIA, BLUR, SHARPEN, BRIGHTNESS, CONTRAST
}
```

### 7. 通知API

```kotlin
// 通知管理接口
interface UnifyNotificationManager {
    // 本地通知
    suspend fun scheduleNotification(notification: LocalNotification): UnifyResult<String>
    suspend fun cancelNotification(id: String): UnifyResult<Unit>
    suspend fun cancelAllNotifications(): UnifyResult<Unit>
    suspend fun getScheduledNotifications(): UnifyResult<List<LocalNotification>>
    
    // 推送通知
    suspend fun registerForPushNotifications(): UnifyResult<String>
    suspend fun unregisterFromPushNotifications(): UnifyResult<Unit>
    suspend fun getNotificationSettings(): UnifyResult<NotificationSettings>
    
    // 通知监听
    val notificationClicks: Flow<NotificationClick>
    val pushTokenUpdates: Flow<String>
}

// 本地通知
data class LocalNotification(
    val id: String,
    val title: String,
    val body: String,
    val scheduledTime: Long,
    val repeatInterval: RepeatInterval? = null,
    val sound: NotificationSound = NotificationSound.Default,
    val priority: NotificationPriority = NotificationPriority.Normal,
    val category: String? = null,
    val data: Map<String, String> = emptyMap()
)

// 重复间隔
enum class RepeatInterval {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

// 通知声音
sealed class NotificationSound {
    object Default : NotificationSound()
    object None : NotificationSound()
    data class Custom(val soundName: String) : NotificationSound()
}

// 通知优先级
enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}

// 通知设置
data class NotificationSettings(
    val isEnabled: Boolean,
    val soundEnabled: Boolean,
    val badgeEnabled: Boolean,
    val alertEnabled: Boolean
)

// 通知点击事件
data class NotificationClick(
    val notificationId: String,
    val data: Map<String, String>
)
```

## 🔧 平台适配实现示例

### Android平台实现

```kotlin
// Android平台信息实现
actual object AndroidPlatformInfo : PlatformInfo {
    actual override val platformType = PlatformType.ANDROID
    actual override val platformVersion = Build.VERSION.RELEASE
    actual override val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
    actual override val screenSize: ScreenSize
        get() = getScreenSize()
    actual override val capabilities: Set<PlatformCapability>
        get() = detectCapabilities()
    
    private fun getScreenSize(): ScreenSize {
        val displayMetrics = Resources.getSystem().displayMetrics
        return ScreenSize(
            width = displayMetrics.widthPixels,
            height = displayMetrics.heightPixels,
            density = displayMetrics.density,
            sizeClass = calculateSizeClass(displayMetrics),
            orientation = getOrientation(),
            safeAreaInsets = getSafeAreaInsets()
        )
    }
    
    private fun detectCapabilities(): Set<PlatformCapability> {
        val capabilities = mutableSetOf<PlatformCapability>()
        val context = getApplicationContext()
        
        // 检测硬件能力
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            capabilities.add(PlatformCapability.CAMERA)
        }
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            capabilities.add(PlatformCapability.MICROPHONE)
        }
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
            capabilities.add(PlatformCapability.LOCATION)
        }
        
        return capabilities
    }
}

// Android存储实现
actual class AndroidStorage(private val context: Context) : UnifyStorage {
    private val sharedPreferences = context.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
    
    actual override suspend fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
    
    actual override suspend fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    // ... 其他方法实现
}
```

### iOS平台实现

```kotlin
// iOS平台信息实现
actual object IOSPlatformInfo : PlatformInfo {
    actual override val platformType = PlatformType.IOS
    actual override val platformVersion = UIDevice.currentDevice.systemVersion
    actual override val deviceModel = UIDevice.currentDevice.model
    actual override val screenSize: ScreenSize
        get() = getScreenSize()
    actual override val capabilities: Set<PlatformCapability>
        get() = detectCapabilities()
    
    private fun getScreenSize(): ScreenSize {
        val screen = UIScreen.mainScreen
        val bounds = screen.bounds
        return ScreenSize(
            width = bounds.size.width.toInt(),
            height = bounds.size.height.toInt(),
            density = screen.scale.toFloat(),
            sizeClass = calculateSizeClass(bounds),
            orientation = getOrientation(),
            safeAreaInsets = getSafeAreaInsets()
        )
    }
    
    private fun detectCapabilities(): Set<PlatformCapability> {
        val capabilities = mutableSetOf<PlatformCapability>()
        
        // 检测iOS特定能力
        if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            capabilities.add(PlatformCapability.CAMERA)
        }
        
        if (AVAudioSession.sharedInstance().isInputAvailable) {
            capabilities.add(PlatformCapability.MICROPHONE)
        }
        
        return capabilities
    }
}
```

### HarmonyOS平台实现

```kotlin
// HarmonyOS平台信息实现
actual object HarmonyPlatformInfo : PlatformInfo {
    actual override val platformType = PlatformType.HARMONY_OS
    actual override val platformVersion = getHarmonyVersion()
    actual override val deviceModel = getDeviceModel()
    actual override val screenSize: ScreenSize
        get() = getScreenSize()
    actual override val capabilities: Set<PlatformCapability>
        get() = detectCapabilities()
    
    private fun detectCapabilities(): Set<PlatformCapability> {
        val capabilities = mutableSetOf<PlatformCapability>()
        
        // HarmonyOS特有能力
        capabilities.add(PlatformCapability.DISTRIBUTED_CAPABILITY)
        
        // 检测其他硬件能力
        // ... 实现检测逻辑
        
        return capabilities
    }
}
```

## 📊 API使用示例

### 基础使用示例

```kotlin
// 获取平台信息
val platformInfo = UnifyCore.platformInfo
println("运行在 ${platformInfo.platformType} ${platformInfo.platformVersion}")

// 检测平台能力
if (PlatformCapabilityDetector.hasCapability(PlatformCapability.CAMERA)) {
    // 使用相机功能
    val imageResult = UnifyCore.mediaManager.captureImage(
        CameraOptions(quality = 0.8f, cameraType = CameraType.REAR)
    )
    
    when (imageResult) {
        is UnifyResult.Success -> {
            // 处理图片
            val processedImage = UnifyCore.mediaManager.processImage(
                imageResult.data,
                listOf(
                    ImageOperation.Resize(800, 600),
                    ImageOperation.Compress(0.7f)
                )
            )
        }
        is UnifyResult.Error -> {
            // 处理错误
            handleError(imageResult.exception)
        }
        is UnifyResult.Loading -> {
            // 显示加载状态
        }
    }
}

// 存储数据
val storage = UnifyCore.storage
storage.setString("user_name", "张三")
storage.setInt("user_age", 25)

// 监听存储变化
storage.observeKey("user_name").collect { value ->
    println("用户名更新为: $value")
}

// 网络请求
val httpClient = UnifyCore.httpClient
val response = httpClient.get(
    url = "https://api.example.com/users",
    headers = mapOf("Authorization" to "Bearer token")
)

when (response) {
    is UnifyResult.Success -> {
        val users = response.data.bodyAsJson<List<User>>()
        // 处理用户数据
    }
    is UnifyResult.Error -> {
        when (response.exception) {
            is UnifyException.NetworkException -> {
                // 处理网络错误
            }
            else -> {
                // 处理其他错误
            }
        }
    }
}

// 权限请求
val permissionManager = UnifyCore.permissionManager
val cameraPermission = permissionManager.requestPermission(UnifyPermission.CAMERA)

when (cameraPermission) {
    is PermissionResult.Granted -> {
        // 权限已授予，可以使用相机
    }
    is PermissionResult.Denied -> {
        // 权限被拒绝
    }
    is PermissionResult.ShowRationale -> {
        // 需要显示权限说明
        showPermissionRationale(cameraPermission.message)
    }
}
```

## 🎯 API设计优势

### 1. 统一性
- 所有平台使用相同的API接口
- 一致的错误处理机制
- 统一的数据类型定义

### 2. 类型安全
- 强类型定义避免运行时错误
- 使用密封类处理状态和结果
- 泛型支持提供类型安全的操作

### 3. 异步支持
- 全面支持Kotlin协程
- 响应式编程模式
- 非阻塞操作设计

### 4. 平台适配
- expect/actual机制处理平台差异
- 优雅的功能降级
- 平台特定功能扩展

### 5. 可扩展性
- 插件化架构支持
- 自定义组件注册
- 第三方库集成

这套统一API接口规范为Unify-Core提供了完整的跨平台开发基础，确保了代码的一致性、可维护性和扩展性。

```kotlin
// 导航控制器
interface UnifyNavigator {
    fun navigate(route: String, args: Map<String, Any> = emptyMap())
    fun navigateBack()
    fun navigateUp()
    fun popToRoot()
    val currentRoute: StateFlow<String?>
}

// 路由定义
sealed class UnifyRoute(val path: String) {
    object Home : UnifyRoute("/home")
    object Profile : UnifyRoute("/profile")
    data class Detail(val id: String) : UnifyRoute("/detail/$id")
}
```

### 5. 权限API

```kotlin
// 权限管理
interface PermissionManager {
    suspend fun requestPermission(permission: UnifyPermission): PermissionResult
    suspend fun requestMultiplePermissions(permissions: List<UnifyPermission>): Map<UnifyPermission, PermissionResult>
    suspend fun checkPermission(permission: UnifyPermission): PermissionResult
    fun openAppSettings()
}

// 统一权限定义
enum class UnifyPermission {
    CAMERA,
    LOCATION,
    MICROPHONE,
    STORAGE,
    CONTACTS,
    CALENDAR,
    PHONE,
    SMS,
    NOTIFICATION
}

// 权限结果
enum class PermissionResult {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    NOT_AVAILABLE
}
```

### 6. 媒体API

```kotlin
// 图片选择器
interface ImagePicker {
    suspend fun pickImage(source: ImageSource = ImageSource.GALLERY): ImageResult?
    suspend fun pickMultipleImages(maxCount: Int = 9): List<ImageResult>
}

enum class ImageSource {
    CAMERA,
    GALLERY,
    BOTH
}

data class ImageResult(
    val uri: String,
    val name: String,
    val size: Long,
    val mimeType: String
)

// 相机控制
interface CameraController {
    suspend fun takePicture(): ImageResult?
    suspend fun startVideoRecording(): VideoResult?
    fun isAvailable(): Boolean
}
```

### 7. 设备功能API

```kotlin
// 振动反馈
interface HapticFeedback {
    fun lightImpact()
    fun mediumImpact()
    fun heavyImpact()
    fun success()
    fun warning()
    fun error()
    fun isAvailable(): Boolean
}

// 生物识别
interface BiometricAuth {
    suspend fun authenticate(title: String, subtitle: String): BiometricResult
    fun isAvailable(): Boolean
    fun getSupportedTypes(): Set<BiometricType>
}

enum class BiometricType {
    FINGERPRINT,
    FACE,
    IRIS,
    VOICE
}

sealed class BiometricResult {
    object Success : BiometricResult()
    object UserCancel : BiometricResult()
    object AuthenticationFailed : BiometricResult()
    object NotAvailable : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}
```

### 8. 通知API

```kotlin
// 本地通知
interface LocalNotification {
    suspend fun show(notification: NotificationData)
    suspend fun schedule(notification: NotificationData, delay: Duration)
    suspend fun cancel(id: String)
    suspend fun cancelAll()
}

data class NotificationData(
    val id: String,
    val title: String,
    val body: String,
    val icon: String? = null,
    val sound: String? = null,
    val vibrate: Boolean = true,
    val data: Map<String, String> = emptyMap()
)

// 推送通知
interface PushNotification {
    suspend fun getToken(): String?
    suspend fun subscribe(topic: String)
    suspend fun unsubscribe(topic: String)
    val onTokenRefresh: Flow<String>
    val onMessageReceived: Flow<PushMessage>
}

data class PushMessage(
    val title: String?,
    val body: String?,
    val data: Map<String, String>
)
```

## 🔧 平台适配层实现

### 1. Android实现示例

```kotlin
// Android平台存储实现
actual object StorageFactory {
    actual fun createLocalStorage(): UnifyStorage = AndroidLocalStorage()
    actual fun createSecureStorage(): UnifyStorage = AndroidSecureStorage()
}

class AndroidLocalStorage : UnifyStorage {
    private val prefs = context.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
    
    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }
    
    override suspend fun setString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }
    
    // ... 其他方法实现
}
```

### 2. iOS实现示例

```kotlin
// iOS平台存储实现
actual object StorageFactory {
    actual fun createLocalStorage(): UnifyStorage = IOSLocalStorage()
    actual fun createSecureStorage(): UnifyStorage = IOSKeychainStorage()
}

class IOSLocalStorage : UnifyStorage {
    override suspend fun getString(key: String): String? = withContext(Dispatchers.Default) {
        NSUserDefaults.standardUserDefaults.stringForKey(key)
    }
    
    override suspend fun setString(key: String, value: String) = withContext(Dispatchers.Default) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
    }
    
    // ... 其他方法实现
}
```

### 3. 小程序实现示例

```kotlin
// 小程序平台存储实现
actual object StorageFactory {
    actual fun createLocalStorage(): UnifyStorage = MiniAppStorage()
    actual fun createSecureStorage(): UnifyStorage? = null // 小程序不支持安全存储
}

class MiniAppStorage : UnifyStorage {
    override suspend fun getString(key: String): String? = suspendCoroutine { cont ->
        wx.getStorage(object {
            val key = key
            val success: (dynamic) -> Unit = { res -> cont.resume(res.data as? String) }
            val fail: (dynamic) -> Unit = { cont.resume(null) }
        })
    }
    
    // ... 其他方法实现
}
```

## 📋 API使用示例

### 1. 跨平台数据存储

```kotlin
class UserRepository {
    private val storage = StorageFactory.createLocalStorage()
    
    suspend fun saveUser(user: User) {
        storage.setString("user_name", user.name)
        storage.setString("user_email", user.email)
    }
    
    suspend fun getUser(): User? {
        val name = storage.getString("user_name") ?: return null
        val email = storage.getString("user_email") ?: return null
        return User(name, email)
    }
}
```

### 2. 跨平台权限请求

```kotlin
@Composable
fun CameraScreen() {
    val permissionManager = remember { PermissionManagerFactory.create() }
    var hasPermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val result = permissionManager.requestPermission(UnifyPermission.CAMERA)
        hasPermission = result == PermissionResult.GRANTED
    }
    
    if (hasPermission) {
        CameraPreview()
    } else {
        PermissionDeniedMessage()
    }
}
```

### 3. 跨平台网络请求

```kotlin
class ApiService {
    private val httpClient = HttpClientFactory.create()
    
    suspend fun fetchUserProfile(userId: String): Result<UserProfile> {
        return try {
            val response = httpClient.get("https://api.example.com/users/$userId")
            if (response.isSuccess) {
                val profile = Json.decodeFromString<UserProfile>(response.body)
                Result.success(profile)
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🎯 API扩展策略

### 1. 平台特定扩展

```kotlin
// 平台特定功能扩展
interface PlatformExtensions {
    fun getAndroidExtensions(): AndroidExtensions?
    fun getIOSExtensions(): IOSExtensions?
    fun getMiniAppExtensions(): MiniAppExtensions?
}

// Android特定扩展
interface AndroidExtensions {
    fun requestIgnoreBatteryOptimization()
    fun openAppInfo()
    fun shareToSpecificApp(packageName: String, content: String)
}

// iOS特定扩展
interface IOSExtensions {
    fun addToSiri(phrase: String)
    fun requestReview()
    fun openSettings()
}
```

### 2. 功能检测API

```kotlin
// 功能可用性检测
object FeatureDetector {
    fun isSupported(feature: PlatformCapability): Boolean
    fun getSupportedFeatures(): Set<PlatformCapability>
    fun getPlatformLimitations(): List<String>
}

// 使用示例
@Composable
fun ConditionalFeature() {
    if (FeatureDetector.isSupported(PlatformCapability.CAMERA)) {
        CameraButton()
    } else {
        Text("相机功能不可用")
    }
}
```

这个统一的API接口规范为所有平台提供了一致的开发体验，同时保持了平台特定功能的灵活性。
