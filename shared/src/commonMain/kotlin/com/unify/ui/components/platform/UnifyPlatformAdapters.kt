package com.unify.ui.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unify.ui.components.media.*
import com.unify.ui.components.scanner.*
import com.unify.ui.components.sensor.*

/**
 * 平台特定组件适配器
 * 使用 expect/actual 机制处理平台差异
 */

/**
 * 直播播放器平台适配
 */
@Composable
expect fun PlatformLivePlayer(
    config: UnifyLivePlayerConfig,
    onStateChange: ((UnifyLivePlayerState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 直播推流器平台适配
 */
@Composable
expect fun PlatformLivePusher(
    config: UnifyLivePusherConfig,
    onStateChange: ((UnifyLivePusherState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * WebRTC 平台适配
 */
@Composable
expect fun PlatformWebRTC(
    config: UnifyWebRTCConfig,
    onUserJoin: ((String) -> Unit)? = null,
    onUserLeave: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 扫码器平台适配
 */
@Composable
expect fun PlatformScanner(
    config: UnifyScanConfig,
    onScanResult: ((UnifyScanResult) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 传感器平台适配
 */
@Composable
expect fun PlatformSensor(
    config: UnifySensorConfig,
    onDataReceived: ((UnifySensorData) -> Unit)? = null,
    onStateChange: ((UnifySensorState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 生物识别平台适配
 */
@Composable
expect fun PlatformBiometric(
    biometricType: UnifySensorType,
    onSuccess: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
)

/**
 * 触觉反馈平台适配
 */
expect fun PlatformHapticFeedback(
    intensity: Float = 1f,
    duration: Long = 100L,
    pattern: List<Long> = listOf(0L, 100L)
)

/**
 * 语音识别平台适配
 */
@Composable
expect fun PlatformSpeechRecognition(
    language: String = "zh-CN",
    onResult: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 文字转语音平台适配
 */
expect fun PlatformTextToSpeech(
    text: String,
    language: String = "zh-CN",
    rate: Float = 1f,
    pitch: Float = 1f,
    onComplete: (() -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 设备震动平台适配
 */
expect fun PlatformVibration(
    pattern: List<Long> = listOf(0L, 100L),
    repeat: Boolean = false
)

/**
 * 屏幕亮度控制平台适配
 */
expect fun PlatformScreenBrightness(
    brightness: Float // 0.0 - 1.0
)

/**
 * 屏幕方向控制平台适配
 */
expect fun PlatformScreenOrientation(
    orientation: String // "portrait", "landscape", "auto"
)

/**
 * 状态栏控制平台适配
 */
expect fun PlatformStatusBar(
    hidden: Boolean = false,
    style: String = "default", // "default", "light", "dark"
    backgroundColor: androidx.compose.ui.graphics.Color? = null
)

/**
 * 导航栏控制平台适配
 */
expect fun PlatformNavigationBar(
    hidden: Boolean = false,
    style: String = "default",
    backgroundColor: androidx.compose.ui.graphics.Color? = null
)

/**
 * 系统通知平台适配
 */
expect fun PlatformNotification(
    title: String,
    content: String,
    icon: String? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList()
)

/**
 * 文件选择器平台适配
 */
@Composable
expect fun PlatformFilePicker(
    fileTypes: List<String> = listOf("*/*"),
    multiple: Boolean = false,
    onFileSelected: ((List<String>) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 相机平台适配
 */
@Composable
expect fun PlatformCamera(
    facing: String = "back", // "front", "back"
    onPhotoTaken: ((ByteArray) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 位置服务平台适配
 */
expect fun PlatformLocation(
    accuracy: String = "high", // "high", "medium", "low"
    onLocationUpdate: ((Double, Double) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
)

/**
 * 网络状态监听平台适配
 */
@Composable
expect fun PlatformNetworkMonitor(
    onNetworkChange: ((Boolean, String) -> Unit)? = null
)

/**
 * 电池状态监听平台适配
 */
@Composable
expect fun PlatformBatteryMonitor(
    onBatteryChange: ((Int, Boolean) -> Unit)? = null
)

/**
 * 应用生命周期监听平台适配
 */
@Composable
expect fun PlatformLifecycleMonitor(
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null
)
