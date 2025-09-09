package com.unify.ui.components.system

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.unify.core.types.PerformanceMetric
import com.unify.core.types.SystemInfo

/**
 * Android平台系统组件实现
 */
@Composable
actual fun UnifySystemInfo(
    systemInfo: SystemInfo,
    modifier: Modifier,
    showBattery: Boolean,
    showMemory: Boolean,
    showStorage: Boolean,
    showNetwork: Boolean,
    refreshInterval: Long,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("System Information")
            Text("Refresh Interval: ${refreshInterval}ms")

            Text("OS: ${systemInfo.operatingSystem} ${systemInfo.version}")
            Text("Device: ${systemInfo.deviceName} (${systemInfo.deviceModel})")
            Text("Architecture: ${systemInfo.architecture}")
            Text("Memory: ${systemInfo.availableMemory / (1024 * 1024 * 1024)}GB / ${systemInfo.totalMemory / (1024 * 1024 * 1024)}GB")

            if (showBattery) {
                Text("Battery: Available")
            }
            if (showMemory) {
                Text("Memory: Available")
            }
            if (showStorage) {
                Text("Storage: Available")
            }
            if (showNetwork) {
                Text("Network: Available")
            }
        }
    }
}

@Composable
actual fun UnifyPerformanceMonitor(
    metrics: Set<PerformanceMetric>,
    onMetricsUpdate: (Map<PerformanceMetric, Float>) -> Unit,
    modifier: Modifier,
    showRealTimeChart: Boolean,
    maxDataPoints: Int,
    updateInterval: Long,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Performance Monitor")
            Text("Update Interval: ${updateInterval}ms")
            Text("Max Data Points: $maxDataPoints")

            val metricsMap: Map<PerformanceMetric, Float> =
                mapOf(
                    PerformanceMetric.CPU_USAGE to 45.0f,
                    PerformanceMetric.MEMORY_USAGE to 67.0f,
                    PerformanceMetric.NETWORK_SPEED to 23.0f,
                    PerformanceMetric.BATTERY_LEVEL to 12.0f,
                ).filterKeys { it in metrics }

            LaunchedEffect(metrics) {
                onMetricsUpdate(metricsMap)
            }

            metricsMap.forEach { (metric: PerformanceMetric, value: Float) ->
                Text("$metric: $value%")
            }

            if (showRealTimeChart) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Gray.copy(alpha = 0.1f)),
                ) {
                    Text("Real-time Chart Placeholder")
                }
            }
        }
    }
}

@Composable
actual fun UnifyBatteryIndicator(
    batteryLevel: Float,
    isCharging: Boolean,
    modifier: Modifier,
    showPercentage: Boolean,
    lowBatteryThreshold: Float,
    warningColor: Color,
    normalColor: Color,
    chargingColor: Color,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Battery Status")

            val batteryColor =
                when {
                    isCharging -> chargingColor
                    batteryLevel < lowBatteryThreshold -> warningColor
                    else -> normalColor
                }

            if (showPercentage) {
                Text("Level: ${(batteryLevel * 100).toInt()}%")
            }

            LinearProgressIndicator(
                progress = batteryLevel,
                modifier = Modifier.fillMaxWidth(),
                color = batteryColor,
            )

            Text("Charging: ${if (isCharging) "Yes" else "No"}")

            if (batteryLevel < lowBatteryThreshold) {
                Text("Low Battery Warning!", color = warningColor)
            }
        }
    }
}

@Composable
actual fun UnifyBatteryStatus(
    modifier: Modifier,
    showPercentage: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Battery Status")
            Text("85%") // 简化实现
        }
    }
}

@Composable
actual fun UnifyCPUUsage(
    modifier: Modifier,
    refreshInterval: Long,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("CPU Usage")
            LinearProgressIndicator(
                progress = 0.45f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
actual fun UnifyMemoryUsage(
    totalMemory: Long,
    usedMemory: Long,
    modifier: Modifier,
    showChart: Boolean,
    showDetails: Boolean,
    warningThreshold: Float,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Memory Usage")
            val usagePercentage = (usedMemory.toFloat() / totalMemory.toFloat())
            val availableMemory = totalMemory - usedMemory

            LinearProgressIndicator(
                progress = usagePercentage,
                color = if (usagePercentage > warningThreshold) Color.Red else Color.Green,
                modifier = Modifier.fillMaxWidth(),
            )

            if (showDetails) {
                Text("Used: ${usedMemory}MB / ${totalMemory}MB")
                Text("Available: ${availableMemory}MB")
                Text("Usage: ${(usagePercentage * 100).toInt()}%")
            }

            if (showChart) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Gray.copy(alpha = 0.1f)),
                ) {
                    Text("Memory Chart Placeholder")
                }
            }
        }
    }
}

@Composable
actual fun UnifyStorageUsage(
    totalStorage: Long,
    usedStorage: Long,
    modifier: Modifier,
    showBreakdown: Boolean,
    categories: Map<String, Long>,
    warningThreshold: Float,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Storage Usage")
            val usagePercentage = (usedStorage.toFloat() / totalStorage.toFloat())
            val availableStorage = totalStorage - usedStorage

            LinearProgressIndicator(
                progress = usagePercentage,
                color = if (usagePercentage > warningThreshold) Color.Red else Color.Green,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Used: ${usedStorage}MB / ${totalStorage}MB")
            Text("Available: ${availableStorage}MB")
            Text("Usage: ${(usagePercentage * 100).toInt()}%")

            if (showBreakdown && categories.isNotEmpty()) {
                Text("Storage Breakdown:")
                categories.forEach { (category, size) ->
                    Text("$category: ${size}MB")
                }
            }
        }
    }
}

@Composable
actual fun UnifyNetworkStatus(
    networkType: String,
    isConnected: Boolean,
    signalStrength: Float,
    modifier: Modifier,
    showSpeed: Boolean,
    onNetworkTest: () -> Unit,
    showDetails: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Network Status")

            Text("Connected: $isConnected")
            Text("Type: $networkType")
            Text("Signal: ${(signalStrength * 100).toInt()}%")

            LinearProgressIndicator(
                progress = signalStrength,
                modifier = Modifier.fillMaxWidth(),
            )

            if (showSpeed) {
                Text("Speed: 100 Mbps")
            }

            if (showDetails) {
                Text("IP: 192.168.1.100")
                Text("DNS: 8.8.8.8")
            }

            Button(
                onClick = onNetworkTest,
            ) {
                Text("Test Network")
            }
        }
    }
}

@Composable
actual fun UnifyDeviceOrientation(
    onOrientationChange: (DeviceOrientation) -> Unit,
    modifier: Modifier,
    showIndicator: Boolean,
    lockOrientation: DeviceOrientation?,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Device Orientation")
            val currentOrientation = DeviceOrientation.PORTRAIT

            LaunchedEffect(Unit) {
                onOrientationChange(currentOrientation)
            }

            Text("Current: ${currentOrientation.name}")

            if (showIndicator) {
                Box(
                    modifier =
                        Modifier
                            .size(60.dp)
                            .background(Color.Gray.copy(alpha = 0.3f)),
                ) {
                    Text("📱", modifier = Modifier.align(Alignment.Center))
                }
            }

            lockOrientation?.let {
                Text("Locked to: ${it.name}")
            }
        }
    }
}

@Composable
actual fun UnifyVibrationControl(
    onVibrate: (VibrationPattern) -> Unit,
    modifier: Modifier,
    enableCustomPatterns: Boolean,
    presetPatterns: List<VibrationPattern>,
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Vibration Control")

            Button(
                onClick = {
                    val defaultPattern =
                        VibrationPattern(
                            name = "Default",
                            pattern = longArrayOf(0, 250, 250, 250),
                        )
                    onVibrate(defaultPattern)
                },
            ) {
                Text("Vibrate")
            }

            if (enableCustomPatterns) {
                Text("Custom Patterns Available")
            }

            presetPatterns.forEach { pattern ->
                Button(
                    onClick = { onVibrate(pattern) },
                ) {
                    Text(pattern.name)
                }
            }
        }
    }
}

@Composable
actual fun UnifyBrightnessControl(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier,
    enableAutoAdjust: Boolean,
    showSlider: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Brightness Control")
            if (showSlider) {
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                )
            }
            if (enableAutoAdjust) {
                Text("Auto-adjust enabled")
            }
        }
    }
}

@Composable
actual fun UnifyVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier,
    volumeType: VolumeType,
    showSlider: Boolean,
    enableMute: Boolean,
) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Volume Control - ${volumeType.name}")
            if (showSlider) {
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                )
            }
            if (enableMute) {
                Button(
                    onClick = { onVolumeChange(0f) },
                ) {
                    Text("Mute")
                }
            }
        }
    }
}

@Composable
actual fun UnifyClipboard(
    onClipboardChange: (String) -> Unit,
    modifier: Modifier,
    showHistory: Boolean,
    maxHistorySize: Int,
    enableAutoDetect: Boolean,
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var clipboardText by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Clipboard Manager")
            TextField(
                value = clipboardText,
                onValueChange = {
                    clipboardText = it
                    val clip = ClipData.newPlainText("text", it)
                    clipboardManager.setPrimaryClip(clip)
                    onClipboardChange(it)
                },
            )
            if (showHistory) {
                Text("History: $maxHistorySize items")
            }
            if (enableAutoDetect) {
                Text("Auto-detect enabled")
            }
        }
    }
}

@Composable
actual fun UnifyNotificationManager(
    notifications: List<NotificationItem>,
    onNotificationAction: (String, NotificationAction) -> Unit,
    modifier: Modifier,
    enableGrouping: Boolean,
    showBadges: Boolean,
) {
    val context = LocalContext.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Notification Manager")
            if (showBadges) {
                Text("Badges: ${notifications.size}")
            }
            LazyColumn {
                items(notifications) { notification ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text(notification.title)
                            Text(notification.content)
                            Row {
                                notification.actions.forEach { action ->
                                    Button(
                                        onClick = { onNotificationAction(notification.id, action) },
                                    ) {
                                        Text(action.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
