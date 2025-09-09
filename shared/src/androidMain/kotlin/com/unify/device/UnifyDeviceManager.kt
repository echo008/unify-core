package com.unify.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

/**
 * Android平台UnifyDeviceManager实现
 */
class UnifyDeviceManagerImpl(private val context: Context) : UnifyDeviceManager {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    // 传感器监听器管理
    private val sensorListeners = mutableMapOf<SensorType, SensorEventListener>()

    // 位置更新流
    private val _locationUpdates = MutableStateFlow<LocationInfo?>(null)

    // 网络状态流
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())

    // 电池状态流
    private val _batteryStatus = MutableStateFlow(getCurrentBatteryStatus())

    override fun getDeviceInfo(): DeviceInfo {
        val displayMetrics = context.resources.displayMetrics
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return DeviceInfo(
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            screenWidth = displayMetrics.widthPixels,
            screenHeight = displayMetrics.heightPixels,
            screenDensity = displayMetrics.density,
            totalMemory = memoryInfo.totalMem,
            availableMemory = memoryInfo.availMem,
        )
    }

    override fun getPlatformName(): String = "Android"

    override fun getDeviceModel(): String = Build.MODEL

    override fun getOSVersion(): String = Build.VERSION.RELEASE

    override fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    override suspend fun requestPermission(permission: DevicePermission): PermissionStatus {
        val androidPermission = mapToAndroidPermission(permission)
        return when (ActivityCompat.checkSelfPermission(context, androidPermission)) {
            PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
            PackageManager.PERMISSION_DENIED -> PermissionStatus.DENIED
            else -> PermissionStatus.NOT_DETERMINED
        }
    }

    override suspend fun requestPermissions(permissions: List<DevicePermission>): Map<DevicePermission, PermissionStatus> {
        return permissions.associateWith { requestPermission(it) }
    }

    override fun checkPermission(permission: DevicePermission): PermissionStatus {
        val androidPermission = mapToAndroidPermission(permission)
        return when (ActivityCompat.checkSelfPermission(context, androidPermission)) {
            PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
            PackageManager.PERMISSION_DENIED -> PermissionStatus.DENIED
            else -> PermissionStatus.NOT_DETERMINED
        }
    }

    override fun observePermissionStatus(permission: DevicePermission): Flow<PermissionStatus> {
        // 简化实现，实际应用中可以监听权限变化
        return MutableStateFlow(checkPermission(permission)).asStateFlow()
    }

    override fun getSupportedSensors(): List<SensorType> {
        val supportedSensors = mutableListOf<SensorType>()

        SensorType.values().forEach { sensorType ->
            if (isSensorAvailable(sensorType)) {
                supportedSensors.add(sensorType)
            }
        }

        return supportedSensors
    }

    override fun startSensorMonitoring(
        sensorType: SensorType,
        listener: SensorListener,
    ) {
        val androidSensor = getAndroidSensor(sensorType) ?: return

        val sensorEventListener =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    listener.onSensorChanged(sensorType, event.values, event.timestamp)
                }

                override fun onAccuracyChanged(
                    sensor: Sensor,
                    accuracy: Int,
                ) {
                    listener.onAccuracyChanged(sensorType, accuracy)
                }
            }

        sensorListeners[sensorType] = sensorEventListener
        sensorManager.registerListener(sensorEventListener, androidSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun stopSensorMonitoring(sensorType: SensorType) {
        sensorListeners[sensorType]?.let { listener ->
            sensorManager.unregisterListener(listener)
            sensorListeners.remove(sensorType)
        }
    }

    override fun isSensorAvailable(sensorType: SensorType): Boolean {
        return getAndroidSensor(sensorType) != null
    }

    override fun vibrate(durationMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    override fun setScreenBrightness(brightness: Float) {
        // 需要WRITE_SETTINGS权限，这里提供基础实现
        val brightnessValue = (brightness * 255).toInt().coerceIn(0, 255)
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue)
    }

    override fun getScreenBrightness(): Float {
        return try {
            val brightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            brightness / 255f
        } catch (e: Exception) {
            0.5f
        }
    }

    override fun setVolume(volume: Float) {
        // 音量控制需要AudioManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val volumeLevel = (volume * maxVolume).toInt()
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, volumeLevel, 0)
    }

    override fun getVolume(): Float {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        return currentVolume.toFloat() / maxVolume
    }

    override fun showNotification(
        title: String,
        message: String,
        id: String,
    ) {
        val notification =
            NotificationCompat.Builder(context, "default_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
    }

    override suspend fun takePicture(): String? {
        // 相机功能需要复杂的实现，这里提供基础框架
        return suspendCancellableCoroutine { continuation ->
            // 实际实现需要使用CameraX或Camera2 API
            continuation.resume(null)
        }
    }

    override suspend fun recordAudio(durationMillis: Long): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val outputFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.3gp")
                val recorder =
                    MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        setOutputFile(outputFile.absolutePath)
                        prepare()
                        start()
                    }

                // 简化实现，实际应用中需要处理录音时长
                continuation.resume(outputFile.absolutePath)
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    override suspend fun getCurrentLocation(): LocationInfo? {
        return suspendCancellableCoroutine { continuation ->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val locationListener =
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val locationInfo =
                            LocationInfo(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitude = location.altitude,
                                accuracy = location.accuracy,
                                timestamp = location.time,
                            )
                        continuation.resume(locationInfo)
                        locationManager.removeUpdates(this)
                    }

                    override fun onProviderEnabled(provider: String) {}

                    override fun onProviderDisabled(provider: String) {}
                }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        }
    }

    override fun observeLocationUpdates(): Flow<LocationInfo> = _locationUpdates.asStateFlow().filterNotNull()

    override fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun getNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.UNKNOWN
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.UNKNOWN

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkType.BLUETOOTH
            else -> NetworkType.UNKNOWN
        }
    }

    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()

    override fun getBatteryLevel(): Float {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) / 100f
    }

    override fun isBatteryCharging(): Boolean {
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    override fun observeBatteryStatus(): Flow<BatteryStatus> = _batteryStatus.asStateFlow()

    override fun getAvailableStorage(): Long {
        return context.filesDir.freeSpace
    }

    override fun getTotalStorage(): Long {
        return context.filesDir.totalSpace
    }

    override fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }

    private fun mapToAndroidPermission(permission: DevicePermission): String {
        return when (permission) {
            DevicePermission.CAMERA -> Manifest.permission.CAMERA
            DevicePermission.MICROPHONE -> Manifest.permission.RECORD_AUDIO
            DevicePermission.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
            DevicePermission.STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
            DevicePermission.CONTACTS -> Manifest.permission.READ_CONTACTS
            DevicePermission.CALENDAR -> Manifest.permission.READ_CALENDAR
            DevicePermission.PHONE -> Manifest.permission.CALL_PHONE
            DevicePermission.SMS -> Manifest.permission.SEND_SMS
            DevicePermission.NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
            DevicePermission.BLUETOOTH -> Manifest.permission.BLUETOOTH
            DevicePermission.NFC -> Manifest.permission.NFC
            DevicePermission.BIOMETRIC -> Manifest.permission.USE_BIOMETRIC
        }
    }

    private fun getAndroidSensor(sensorType: SensorType): Sensor? {
        val androidSensorType =
            when (sensorType) {
                SensorType.ACCELEROMETER -> Sensor.TYPE_ACCELEROMETER
                SensorType.GYROSCOPE -> Sensor.TYPE_GYROSCOPE
                SensorType.MAGNETOMETER -> Sensor.TYPE_MAGNETIC_FIELD
                SensorType.GRAVITY -> Sensor.TYPE_GRAVITY
                SensorType.LINEAR_ACCELERATION -> Sensor.TYPE_LINEAR_ACCELERATION
                SensorType.ROTATION_VECTOR -> Sensor.TYPE_ROTATION_VECTOR
                SensorType.ORIENTATION -> Sensor.TYPE_ORIENTATION
                SensorType.PROXIMITY -> Sensor.TYPE_PROXIMITY
                SensorType.LIGHT -> Sensor.TYPE_LIGHT
                SensorType.PRESSURE -> Sensor.TYPE_PRESSURE
                SensorType.TEMPERATURE -> Sensor.TYPE_AMBIENT_TEMPERATURE
                SensorType.HUMIDITY -> Sensor.TYPE_RELATIVE_HUMIDITY
                SensorType.HEART_RATE -> Sensor.TYPE_HEART_RATE
                SensorType.STEP_COUNTER -> Sensor.TYPE_STEP_COUNTER
            }

        return sensorManager.getDefaultSensor(androidSensorType)
    }

    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isNetworkAvailable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }

    private fun getCurrentBatteryStatus(): BatteryStatus {
        val level = getBatteryLevel()
        val isCharging = isBatteryCharging()
        val chargingType = if (isCharging) ChargingType.AC else ChargingType.NONE // 简化实现

        return BatteryStatus(
            level = level,
            isCharging = isCharging,
            chargingType = chargingType,
            temperature = 25.0f, // 简化实现
        )
    }
}

// 扩展函数用于过滤非空值
private fun <T> Flow<T?>.filterNotNull(): Flow<T> =
    kotlinx.coroutines.flow.flow {
        collect { value ->
            if (value != null) emit(value)
        }
    }

actual object UnifyDeviceManagerFactory {
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    actual fun create(): UnifyDeviceManager {
        return UnifyDeviceManagerImpl(
            context ?: throw IllegalStateException("UnifyDeviceManagerFactory not initialized. Call initialize(context) first."),
        )
    }
}
