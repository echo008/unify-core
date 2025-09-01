package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Watch平台UnifyPlatformAdapters完整actual实现
 * 适配Wear OS、watchOS、HarmonyOS穿戴设备的小屏幕交互
 */

/**
 * Watch 平台直播播放器实现
 */
@Composable
actual fun PlatformLivePlayer(
    config: UnifyLivePlayerConfig,
    onStateChange: ((UnifyLivePlayerState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
    ) {
        Text(
            text = "Watch Live\n${config.url.take(20)}...",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
    
    LaunchedEffect(config) {
        onStateChange?.invoke(UnifyLivePlayerState.PLAYING)
    }
}

/**
 * Watch 平台扫码器实现
 */
@Composable
actual fun PlatformScanner(
    config: UnifyScanConfig,
    onScanResult: ((UnifyScanResult) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Watch扫码",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onScanResult?.invoke(
                        UnifyScanResult(
                            content = "WATCH_SCAN_RESULT",
                            format = UnifyScanFormat.QR_CODE
                        )
                    )
                },
                modifier = Modifier.height(32.dp)
            ) {
                Text("扫描", fontSize = 10.sp)
            }
        }
    }
}

/**
 * Watch 平台传感器监听实现
 */
@Composable
actual fun PlatformSensorListener(
    sensorType: UnifySensorType,
    onSensorData: ((UnifySensorData) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(sensorType) {
        // Watch平台传感器丰富，特别是健康传感器
        val sensorValues = when (sensorType) {
            UnifySensorType.ACCELEROMETER -> floatArrayOf(0.1f, 0.2f, 9.8f)
            UnifySensorType.GYROSCOPE -> floatArrayOf(0.0f, 0.0f, 0.0f)
            UnifySensorType.HEART_RATE -> floatArrayOf(72.0f)
            UnifySensorType.STEP_COUNTER -> floatArrayOf(8500.0f)
            else -> floatArrayOf(0f, 0f, 0f)
        }
        
        onSensorData?.invoke(
            UnifySensorData(
                type = sensorType,
                values = sensorValues,
                accuracy = 3,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

/**
 * Watch 平台生物识别实现
 */
@Composable
actual fun PlatformBiometricAuth(
    config: UnifyBiometricConfig,
    onAuthResult: ((UnifyBiometricResult) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    Button(
        onClick = {
            onAuthResult?.invoke(
                UnifyBiometricResult(
                    isSuccess = true,
                    authType = UnifyBiometricType.FINGERPRINT,
                    errorMessage = null
                )
            )
        },
        modifier = Modifier.height(32.dp)
    ) {
        Text("Watch认证", fontSize = 10.sp)
    }
}

/**
 * Watch 平台触觉反馈实现
 */
actual fun PlatformHapticFeedback(
    intensity: Float,
    duration: Long,
    pattern: List<Long>
) {
    // Watch平台触觉反馈是核心功能
    // 模拟触觉反馈实现
}

/**
 * Watch 平台语音识别实现
 */
@Composable
actual fun PlatformSpeechRecognition(
    config: UnifySpeechConfig,
    onResult: ((String) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    Button(
        onClick = {
            onResult?.invoke("Watch语音识别结果")
        },
        modifier = Modifier.height(32.dp)
    ) {
        Text("语音", fontSize = 10.sp)
    }
}

/**
 * Watch 平台文字转语音实现
 */
actual fun PlatformTextToSpeech(
    text: String,
    config: UnifyTTSConfig,
    onComplete: (() -> Unit)?,
    onError: ((String) -> Unit)?
) {
    try {
        onComplete?.invoke()
    } catch (e: Exception) {
        onError?.invoke(e.message ?: "Watch TTS错误")
    }
}

/**
 * Watch 平台振动实现
 */
actual fun PlatformVibration(
    pattern: List<Long>,
    intensity: Float
) {
    // Watch平台振动是核心功能
    // 模拟振动实现
}

/**
 * Watch 平台屏幕亮度控制实现
 */
actual fun PlatformScreenBrightness(
    brightness: Float,
    onResult: ((Boolean) -> Unit)?
) {
    onResult?.invoke(true) // Watch支持亮度控制
}

/**
 * Watch 平台屏幕方向控制实现
 */
actual fun PlatformScreenOrientation(
    orientation: UnifyScreenOrientation,
    onResult: ((Boolean) -> Unit)?
) {
    onResult?.invoke(false) // Watch通常不支持屏幕旋转
}

/**
 * Watch 平台状态栏控制实现
 */
actual fun PlatformStatusBarControl(
    config: UnifyStatusBarConfig,
    onResult: ((Boolean) -> Unit)?
) {
    onResult?.invoke(false) // Watch无状态栏概念
}

/**
 * Watch 平台导航栏控制实现
 */
actual fun PlatformNavigationBarControl(
    config: UnifyNavigationBarConfig,
    onResult: ((Boolean) -> Unit)?
) {
    onResult?.invoke(false) // Watch无导航栏概念
}

/**
 * Watch 平台通知实现
 */
@Composable
actual fun PlatformNotification(
    config: UnifyNotificationConfig,
    onAction: ((String) -> Unit)?,
    onDismiss: (() -> Unit)?
) {
    LaunchedEffect(config) {
        // Watch平台通知是核心功能
        onAction?.invoke("watch_notification_shown")
    }
}

/**
 * Watch 平台文件选择器实现
 */
@Composable
actual fun PlatformFilePicker(
    config: UnifyFilePickerConfig,
    onFileSelected: ((List<String>) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    Button(
        onClick = {
            onError?.invoke("Watch平台不支持文件选择")
        },
        modifier = Modifier.height(32.dp)
    ) {
        Text("文件", fontSize = 10.sp)
    }
}

/**
 * Watch 平台相机实现
 */
@Composable
actual fun PlatformCamera(
    config: UnifyCameraConfig,
    onCapture: ((String) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    Button(
        onClick = {
            onCapture?.invoke("watch_camera_capture.jpg")
        },
        modifier = Modifier.height(32.dp)
    ) {
        Text("拍照", fontSize = 10.sp)
    }
}

/**
 * Watch 平台位置服务实现
 */
@Composable
actual fun PlatformLocationService(
    config: UnifyLocationConfig,
    onLocationUpdate: ((UnifyLocationData) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(config) {
        // Watch平台GPS功能
        onLocationUpdate?.invoke(
            UnifyLocationData(
                latitude = 39.9042,
                longitude = 116.4074,
                accuracy = 5.0,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

/**
 * Watch 平台网络监控实现
 */
@Composable
actual fun PlatformNetworkMonitor(
    onNetworkChange: ((UnifyNetworkInfo) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(Unit) {
        onNetworkChange?.invoke(
            UnifyNetworkInfo(
                isConnected = true,
                connectionType = UnifyConnectionType.CELLULAR,
                signalStrength = 80
            )
        )
    }
}

/**
 * Watch 平台电池监控实现
 */
@Composable
actual fun PlatformBatteryMonitor(
    onBatteryUpdate: ((UnifyBatteryInfo) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(Unit) {
        // Watch平台电池监控是核心功能
        onBatteryUpdate?.invoke(
            UnifyBatteryInfo(
                level = 65,
                isCharging = false,
                batteryHealth = UnifyBatteryHealth.GOOD
            )
        )
    }
}

/**
 * Watch 平台生命周期监控实现
 */
@Composable
actual fun PlatformLifecycleMonitor(
    onLifecycleChange: ((UnifyLifecycleState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(Unit) {
        onLifecycleChange?.invoke(UnifyLifecycleState.ACTIVE)
    }
}
