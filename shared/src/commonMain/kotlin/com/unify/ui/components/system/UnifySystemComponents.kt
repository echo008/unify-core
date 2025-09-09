package com.unify.ui.components.system

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unify.core.types.PerformanceMetric
import com.unify.core.types.SystemInfo

/**
 * Unify跨平台系统组件
 * 支持系统信息显示、性能监控等功能
 */

// 使用统一的PerformanceMetric定义，避免重复声明
// 注意：由于PerformanceMetric可能不存在，暂时使用String类型替代

@Composable
expect fun UnifySystemInfo(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier,
    showBattery: Boolean = true,
    showMemory: Boolean = true,
    showStorage: Boolean = true,
    showNetwork: Boolean = true,
    refreshInterval: Long = 5000L,
)

@Composable
expect fun UnifyPerformanceMonitor(
    metrics: Set<PerformanceMetric>,
    onMetricsUpdate: (Map<PerformanceMetric, Float>) -> Unit,
    modifier: Modifier = Modifier,
    showRealTimeChart: Boolean = true,
    maxDataPoints: Int = 100,
    updateInterval: Long = 1000L,
)

@Composable
expect fun UnifyBatteryIndicator(
    batteryLevel: Float,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true,
    lowBatteryThreshold: Float = 0.2f,
    warningColor: Color = Color.Red,
    normalColor: Color = Color.Green,
    chargingColor: Color = Color.Blue,
)

@Composable
expect fun UnifyMemoryUsage(
    totalMemory: Long,
    usedMemory: Long,
    modifier: Modifier = Modifier,
    showChart: Boolean = true,
    showDetails: Boolean = true,
    warningThreshold: Float = 0.8f,
)

@Composable
expect fun UnifyStorageUsage(
    totalStorage: Long,
    usedStorage: Long,
    modifier: Modifier = Modifier,
    showBreakdown: Boolean = true,
    categories: Map<String, Long> = emptyMap(),
    warningThreshold: Float = 0.9f,
)

@Composable
expect fun UnifyNetworkStatus(
    networkType: String,
    isConnected: Boolean,
    signalStrength: Float,
    modifier: Modifier = Modifier,
    showSpeed: Boolean = true,
    onNetworkTest: () -> Unit = {},
    showDetails: Boolean = true,
)

@Composable
expect fun UnifyDeviceOrientation(
    onOrientationChange: (DeviceOrientation) -> Unit,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = true,
    lockOrientation: DeviceOrientation? = null,
)

enum class DeviceOrientation {
    PORTRAIT,
    LANDSCAPE,
    PORTRAIT_REVERSE,
    LANDSCAPE_REVERSE,
}

@Composable
expect fun UnifyVibrationControl(
    onVibrate: (VibrationPattern) -> Unit,
    modifier: Modifier = Modifier,
    enableCustomPatterns: Boolean = true,
    presetPatterns: List<VibrationPattern> = emptyList(),
)

data class VibrationPattern(
    val name: String,
    val pattern: LongArray,
    val repeat: Int = -1,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as VibrationPattern
        if (name != other.name) return false
        if (!pattern.contentEquals(other.pattern)) return false
        if (repeat != other.repeat) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + pattern.contentHashCode()
        result = 31 * result + repeat
        return result
    }
}

@Composable
expect fun UnifyBrightnessControl(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enableAutoAdjust: Boolean = true,
    showSlider: Boolean = true,
)

@Composable
expect fun UnifyVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    volumeType: VolumeType = VolumeType.MEDIA,
    showSlider: Boolean = true,
    enableMute: Boolean = true,
)

enum class VolumeType {
    MEDIA,
    RING,
    NOTIFICATION,
    ALARM,
    CALL,
}

@Composable
expect fun UnifyClipboard(
    onClipboardChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showHistory: Boolean = true,
    maxHistorySize: Int = 10,
    enableAutoDetect: Boolean = true,
)

@Composable
expect fun UnifyBatteryStatus(
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true,
)

@Composable
expect fun UnifyCPUUsage(
    modifier: Modifier = Modifier,
    refreshInterval: Long = 1000L,
)

@Composable
expect fun UnifyNotificationManager(
    notifications: List<NotificationItem>,
    onNotificationAction: (String, NotificationAction) -> Unit,
    modifier: Modifier = Modifier,
    enableGrouping: Boolean = true,
    showBadges: Boolean = true,
)

data class NotificationItem(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val priority: NotificationPriority,
    val category: String,
    val actions: List<NotificationAction> = emptyList(),
    val isRead: Boolean = false,
)

enum class NotificationAction(val title: String) {
    DISMISS("关闭"),
    MARK_READ("标记已读"),
    REPLY("回复"),
    OPEN("打开"),
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT,
}
