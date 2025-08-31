package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Watch和TV平台扩展支持
 * 为可穿戴设备和智能电视提供专门的平台适配
 */

/**
 * Watch平台管理器接口
 */
interface WatchPlatformManager : PlatformManager {
    
    /**
     * 获取手表特定信息
     */
    suspend fun getWatchInfo(): WatchInfo
    
    /**
     * 获取健康数据
     */
    suspend fun getHealthData(): WatchHealthData
    
    /**
     * 监听健康数据变化
     */
    fun observeHealthData(): Flow<WatchHealthData>
    
    /**
     * 获取运动数据
     */
    suspend fun getWorkoutData(): WatchWorkoutData
    
    /**
     * 开始运动追踪
     */
    suspend fun startWorkoutTracking(type: WorkoutType): Boolean
    
    /**
     * 停止运动追踪
     */
    suspend fun stopWorkoutTracking(): Boolean
    
    /**
     * 发送触觉反馈
     */
    suspend fun sendHapticFeedback(type: HapticFeedbackType): Boolean
    
    /**
     * 显示手表通知
     */
    suspend fun showWatchNotification(notification: WatchNotification): Boolean
    
    /**
     * 获取表冠旋转事件
     */
    fun observeCrownRotation(): Flow<CrownRotationEvent>
    
    /**
     * 控制手表屏幕亮度
     */
    suspend fun setScreenBrightness(brightness: Float): Boolean
    
    /**
     * 获取配对手机信息
     */
    suspend fun getPairedPhoneInfo(): PhoneInfo?
}

/**
 * TV平台管理器接口
 */
interface TVPlatformManager : PlatformManager {
    
    /**
     * 获取TV特定信息
     */
    suspend fun getTVInfo(): TVInfo
    
    /**
     * 获取遥控器状态
     */
    suspend fun getRemoteControlStatus(): RemoteControlStatus
    
    /**
     * 监听遥控器事件
     */
    fun observeRemoteControlEvents(): Flow<RemoteControlEvent>
    
    /**
     * 控制TV音量
     */
    suspend fun setVolume(volume: Int): Boolean
    
    /**
     * 获取当前音量
     */
    suspend fun getVolume(): Int
    
    /**
     * 切换频道
     */
    suspend fun changeChannel(channel: Int): Boolean
    
    /**
     * 获取频道列表
     */
    suspend fun getChannelList(): List<TVChannel>
    
    /**
     * 显示TV通知
     */
    suspend fun showTVNotification(notification: TVNotification): Boolean
    
    /**
     * 控制TV电源
     */
    suspend fun setPowerState(on: Boolean): Boolean
    
    /**
     * 获取HDMI输入状态
     */
    suspend fun getHDMIInputs(): List<HDMIInput>
    
    /**
     * 切换HDMI输入
     */
    suspend fun switchHDMIInput(inputId: String): Boolean
    
    /**
     * 启动应用
     */
    suspend fun launchApp(appId: String): Boolean
    
    /**
     * 获取已安装应用列表
     */
    suspend fun getInstalledApps(): List<TVApp>
}

/**
 * 手表信息
 */
@Serializable
data class WatchInfo(
    val watchModel: String,
    val watchOS: String,
    val watchOSVersion: String,
    val batteryLevel: Float,
    val isCharging: Boolean,
    val crownAvailable: Boolean,
    val hapticEngineAvailable: Boolean,
    val heartRateSensorAvailable: Boolean,
    val gpsAvailable: Boolean,
    val cellularAvailable: Boolean,
    val pairedPhoneId: String?
)

/**
 * 健康数据
 */
@Serializable
data class WatchHealthData(
    val heartRate: Int? = null,
    val stepCount: Int = 0,
    val caloriesBurned: Float = 0f,
    val distanceWalked: Float = 0f,
    val activeMinutes: Int = 0,
    val standHours: Int = 0,
    val sleepData: SleepData? = null,
    val bloodOxygen: Float? = null,
    val stressLevel: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 睡眠数据
 */
@Serializable
data class SleepData(
    val totalSleepTime: Int, // 分钟
    val deepSleepTime: Int,
    val lightSleepTime: Int,
    val remSleepTime: Int,
    val awakeTime: Int,
    val sleepQuality: Float // 0-100
)

/**
 * 运动数据
 */
@Serializable
data class WatchWorkoutData(
    val workoutType: WorkoutType,
    val duration: Int, // 秒
    val caloriesBurned: Float,
    val averageHeartRate: Int,
    val maxHeartRate: Int,
    val distance: Float,
    val pace: Float,
    val isActive: Boolean
)

/**
 * 运动类型
 */
enum class WorkoutType {
    WALKING,
    RUNNING,
    CYCLING,
    SWIMMING,
    YOGA,
    STRENGTH_TRAINING,
    CARDIO,
    HIKING,
    DANCING,
    OTHER
}

/**
 * 触觉反馈类型
 */
enum class HapticFeedbackType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    WARNING,
    ERROR,
    SELECTION
}

/**
 * 手表通知
 */
@Serializable
data class WatchNotification(
    val title: String,
    val message: String,
    val category: String = "default",
    val actionButtons: List<NotificationAction> = emptyList(),
    val hapticFeedback: HapticFeedbackType = HapticFeedbackType.LIGHT
)

/**
 * 通知动作
 */
@Serializable
data class NotificationAction(
    val id: String,
    val title: String,
    val isDestructive: Boolean = false
)

/**
 * 表冠旋转事件
 */
@Serializable
data class CrownRotationEvent(
    val delta: Float,
    val velocity: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 配对手机信息
 */
@Serializable
data class PhoneInfo(
    val phoneModel: String,
    val phoneOS: String,
    val phoneOSVersion: String,
    val isConnected: Boolean,
    val connectionType: String // bluetooth, wifi
)

/**
 * TV信息
 */
@Serializable
data class TVInfo(
    val tvModel: String,
    val tvOS: String,
    val tvOSVersion: String,
    val screenSize: String,
    val resolution: String,
    val hdrSupport: Boolean,
    val dolbyVisionSupport: Boolean,
    val hdmiInputCount: Int,
    val usbPortCount: Int,
    val wirelessConnectivity: List<String>
)

/**
 * 遥控器状态
 */
@Serializable
data class RemoteControlStatus(
    val isConnected: Boolean,
    val batteryLevel: Float?,
    val remoteType: String,
    val supportedFeatures: List<String>
)

/**
 * 遥控器事件
 */
sealed class RemoteControlEvent {
    data class KeyPressed(val keyCode: Int, val keyName: String) : RemoteControlEvent()
    data class KeyReleased(val keyCode: Int, val keyName: String) : RemoteControlEvent()
    data class TouchpadEvent(val x: Float, val y: Float, val type: TouchType) : RemoteControlEvent()
    data class VoiceCommand(val command: String, val confidence: Float) : RemoteControlEvent()
}

/**
 * 触摸类型
 */
enum class TouchType {
    DOWN,
    MOVE,
    UP,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP,
    SWIPE_DOWN
}

/**
 * TV频道
 */
@Serializable
data class TVChannel(
    val channelNumber: Int,
    val channelName: String,
    val logoUrl: String?,
    val isHD: Boolean,
    val category: String
)

/**
 * TV通知
 */
@Serializable
data class TVNotification(
    val title: String,
    val message: String,
    val duration: Int = 5000, // 毫秒
    val position: NotificationPosition = NotificationPosition.TOP_RIGHT,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

/**
 * 通知位置
 */
enum class NotificationPosition {
    TOP_LEFT,
    TOP_RIGHT,
    TOP_CENTER,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    BOTTOM_CENTER,
    CENTER
}

/**
 * 通知优先级
 */
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * HDMI输入
 */
@Serializable
data class HDMIInput(
    val inputId: String,
    val inputName: String,
    val isActive: Boolean,
    val isConnected: Boolean,
    val deviceName: String?
)

/**
 * TV应用
 */
@Serializable
data class TVApp(
    val appId: String,
    val appName: String,
    val version: String,
    val iconUrl: String?,
    val isInstalled: Boolean,
    val canLaunch: Boolean
)
