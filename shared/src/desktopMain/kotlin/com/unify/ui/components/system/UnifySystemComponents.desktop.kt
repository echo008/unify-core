package com.unify.ui.components.system

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.core.types.PerformanceMetric
import com.unify.core.types.SystemInfo
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Desktop平台系统组件actual实现
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
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "系统信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("设备名称: ${systemInfo.deviceName}")
            Text("OS: ${systemInfo.operatingSystem} ${systemInfo.version}")
            Text("Device: ${systemInfo.deviceName} (${systemInfo.deviceModel})")

            if (showMemory) {
                Text("Memory: ${systemInfo.totalMemory / (1024 * 1024 * 1024)}GB / ${systemInfo.availableMemory / (1024 * 1024 * 1024)}GB")
            }
            if (showNetwork) {
                Text("Network: WiFi")
            }
            if (showBattery) {
                Text("Battery: ${systemInfo.batteryLevel?.let { "${(it * 100).toInt()}%" } ?: "N/A"}")
                systemInfo.isCharging?.let { charging ->
                    if (charging) {
                        Text("Charging", color = Color.Green)
                    }
                }
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
    var currentMetrics by remember { mutableStateOf<Map<PerformanceMetric, Float>>(emptyMap()) }
    var isMonitoring by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring, metrics) {
        if (isMonitoring) {
            while (isMonitoring) {
                val newMetrics = mutableMapOf<PerformanceMetric, Float>()
                metrics.forEach { metric ->
                    newMetrics[metric] =
                        when (metric) {
                            PerformanceMetric.CPU_USAGE -> Random.nextFloat() * 100
                            PerformanceMetric.MEMORY_USAGE -> Random.nextFloat() * 100
                            PerformanceMetric.BATTERY_LEVEL -> Random.nextFloat() * 100
                            PerformanceMetric.NETWORK_SPEED -> Random.nextFloat() * 1000
                            PerformanceMetric.FPS -> Random.nextFloat() * 60 + 30
                            PerformanceMetric.TEMPERATURE -> Random.nextFloat() * 40 + 30
                        }
                }
                currentMetrics = newMetrics
                onMetricsUpdate(newMetrics)
                delay(updateInterval)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "性能监控",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isMonitoring = !isMonitoring },
            ) {
                Text(if (isMonitoring) "停止监控" else "开始监控")
            }

            if (currentMetrics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                currentMetrics.forEach { (metric, value) ->
                    Text(
                        text = "${metric.name}: ${String.format("%.1f", value)}${getMetricUnit(metric)}",
                    )
                }

                if (showRealTimeChart) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleChart(
                        data = currentMetrics.values.toList(),
                        modifier = Modifier.height(100.dp).fillMaxWidth(),
                    )
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
    val indicatorColor =
        when {
            isCharging -> chargingColor
            batteryLevel < lowBatteryThreshold -> warningColor
            else -> normalColor
        }

    Card(
        modifier = modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "电池指示器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showPercentage) {
                Text(
                    text = "${(batteryLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = indicatorColor,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { batteryLevel },
                modifier = Modifier.fillMaxWidth(),
                color = indicatorColor,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isCharging) "充电中" else "未充电",
                style = MaterialTheme.typography.bodySmall,
                color = indicatorColor,
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
    val usageRatio = usedMemory.toFloat() / totalMemory.toFloat()
    val isWarning = usageRatio > warningThreshold

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "内存使用情况",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { usageRatio },
                modifier = Modifier.fillMaxWidth(),
                color = if (isWarning) Color.Red else Color.Green,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(usageRatio * 100).toInt()}% 已使用",
                color = if (isWarning) Color.Red else Color.Unspecified,
            )

            if (showDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("已使用: ${usedMemory / 1024 / 1024} MB")
                Text("总内存: ${totalMemory / 1024 / 1024} MB")
                Text("可用: ${(totalMemory - usedMemory) / 1024 / 1024} MB")
            }

            if (showChart) {
                Spacer(modifier = Modifier.height(16.dp))
                SimpleChart(
                    data = listOf(usageRatio * 100),
                    modifier = Modifier.height(80.dp).fillMaxWidth(),
                )
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
    val usageRatio = usedStorage.toFloat() / totalStorage.toFloat()
    val isWarning = usageRatio > warningThreshold

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "存储使用情况",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { usageRatio },
                modifier = Modifier.fillMaxWidth(),
                color = if (isWarning) Color.Red else Color.Blue,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(usageRatio * 100).toInt()}% 已使用",
                color = if (isWarning) Color.Red else Color.Unspecified,
            )

            Text("已使用: ${usedStorage / 1024 / 1024} MB")
            Text("总存储: ${totalStorage / 1024 / 1024} MB")
            Text("可用: ${(totalStorage - usedStorage) / 1024 / 1024} MB")

            if (showBreakdown && categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "分类详情:",
                    style = MaterialTheme.typography.titleSmall,
                )

                categories.forEach { (category, size) ->
                    Text(
                        text = "$category: ${size / 1024 / 1024} MB",
                        style = MaterialTheme.typography.bodySmall,
                    )
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
    var networkSpeed by remember { mutableStateOf(0f) }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            while (isConnected) {
                networkSpeed = Random.nextFloat() * 100 + 10 // 10-110 Mbps
                delay(2000)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "网络状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "状态: ${if (isConnected) "已连接" else "未连接"}",
                    color = if (isConnected) Color.Green else Color.Red,
                )

                Text("类型: $networkType")
            }

            if (isConnected) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("信号强度: ${(signalStrength * 100).toInt()}%")

                LinearProgressIndicator(
                    progress = { signalStrength },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (showSpeed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("网速: ${String.format("%.1f", networkSpeed)} Mbps")
                }

                if (showDetails) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNetworkTest,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("网络测试")
                    }
                }
            }
        }
    }
}

// 辅助函数
private fun getMetricUnit(metric: PerformanceMetric): String {
    return when (metric) {
        PerformanceMetric.CPU_USAGE -> "%"
        PerformanceMetric.MEMORY_USAGE -> "%"
        PerformanceMetric.BATTERY_LEVEL -> "%"
        PerformanceMetric.NETWORK_SPEED -> " Mbps"
        PerformanceMetric.FPS -> " fps"
        PerformanceMetric.TEMPERATURE -> "°C"
    }
}

@Composable
private fun SimpleChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val maxValue = data.maxOrNull() ?: 1f

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * width
            val y = height - (value / maxValue * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color.Blue,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
actual fun UnifyDeviceOrientation(
    onOrientationChange: (DeviceOrientation) -> Unit,
    modifier: Modifier,
    showIndicator: Boolean,
    lockOrientation: DeviceOrientation?,
) {
    var currentOrientation by remember { mutableStateOf(DeviceOrientation.LANDSCAPE) }

    LaunchedEffect(Unit) {
        // Desktop平台默认为横屏，模拟方向检测
        onOrientationChange(currentOrientation)
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "设备方向控制",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showIndicator) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "当前方向: ${currentOrientation.name}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DeviceOrientation.values().forEach { orientation ->
                    Button(
                        onClick = {
                            currentOrientation = orientation
                            onOrientationChange(orientation)
                        },
                        enabled = lockOrientation == null || lockOrientation == orientation,
                    ) {
                        Text(orientation.name)
                    }
                }
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
    val defaultPatterns =
        remember {
            listOf(
                VibrationPattern("短震动", longArrayOf(0, 100), -1),
                VibrationPattern("长震动", longArrayOf(0, 500), -1),
                VibrationPattern("双击", longArrayOf(0, 100, 100, 100), -1),
                VibrationPattern("心跳", longArrayOf(0, 100, 100, 100, 100, 500), -1),
            )
        }

    val patterns = if (presetPatterns.isNotEmpty()) presetPatterns else defaultPatterns

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "震动控制",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Desktop平台不支持震动，仅显示控制界面",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(patterns) { pattern ->
                    Button(
                        onClick = { onVibrate(pattern) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("${pattern.name} (${pattern.pattern.size}ms)")
                    }
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
    var currentBrightness by remember { mutableStateOf(brightness) }
    var autoAdjust by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "亮度控制",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (enableAutoAdjust) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = autoAdjust,
                        onCheckedChange = { autoAdjust = it },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("自动调节")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showSlider) {
                Text(
                    text = "亮度: ${(currentBrightness * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = currentBrightness,
                    onValueChange = {
                        currentBrightness = it
                        onBrightnessChange(it)
                    },
                    enabled = !autoAdjust,
                    valueRange = 0f..1f,
                )
            }

            Text(
                text = "Desktop平台亮度控制由系统管理",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    var currentVolume by remember { mutableStateOf(volume) }
    var isMuted by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "音量控制 - ${volumeType.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (enableMute) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Switch(
                        checked = isMuted,
                        onCheckedChange = {
                            isMuted = it
                            if (it) onVolumeChange(0f) else onVolumeChange(currentVolume)
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("静音")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showSlider) {
                Text(
                    text = "音量: ${if (isMuted) 0 else (currentVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = if (isMuted) 0f else currentVolume,
                    onValueChange = {
                        if (!isMuted) {
                            currentVolume = it
                            onVolumeChange(it)
                        }
                    },
                    enabled = !isMuted,
                    valueRange = 0f..1f,
                )
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
    var clipboardHistory by remember { mutableStateOf(listOf<String>()) }
    var currentClipboard by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "剪贴板管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = currentClipboard,
                onValueChange = {
                    currentClipboard = it
                    onClipboardChange(it)
                },
                label = { Text("当前剪贴板内容") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (currentClipboard.isNotBlank()) {
                        clipboardHistory =
                            (listOf(currentClipboard) + clipboardHistory)
                                .distinct()
                                .take(maxHistorySize)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("添加到历史记录")
            }

            if (showHistory && clipboardHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "历史记录:",
                    style = MaterialTheme.typography.titleSmall,
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(clipboardHistory) { item ->
                        Card(
                            onClick = {
                                currentClipboard = item
                                onClipboardChange(item)
                            },
                        ) {
                            Text(
                                text = item.take(50) + if (item.length > 50) "..." else "",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyBatteryStatus(
    modifier: Modifier,
    showPercentage: Boolean,
) {
    var batteryLevel by remember { mutableStateOf(85) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 模拟电池状态变化
        while (true) {
            delay(5000)
            if (isCharging && batteryLevel < 100) {
                batteryLevel = minOf(100, batteryLevel + 1)
            } else if (!isCharging && batteryLevel > 0) {
                batteryLevel = maxOf(0, batteryLevel - 1)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "电池状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Desktop平台 - 模拟电池状态",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showPercentage) {
                Text(
                    text = "$batteryLevel%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { batteryLevel / 100f },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = isCharging,
                    onCheckedChange = { isCharging = it },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCharging) "充电中" else "未充电")
            }
        }
    }
}

@Composable
actual fun UnifyCPUUsage(
    modifier: Modifier,
    refreshInterval: Long,
) {
    var cpuUsage by remember { mutableStateOf(0f) }
    var memoryUsage by remember { mutableStateOf(0f) }

    LaunchedEffect(refreshInterval) {
        while (true) {
            // 模拟CPU和内存使用率
            cpuUsage = (10..80).random().toFloat()
            memoryUsage = (20..90).random().toFloat()
            delay(refreshInterval)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "系统资源监控",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CPU使用率
            Text(
                text = "CPU使用率: ${cpuUsage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { cpuUsage / 100f },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 内存使用率
            Text(
                text = "内存使用率: ${memoryUsage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { memoryUsage / 100f },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "刷新间隔: ${refreshInterval}ms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// NotificationItem 和 NotificationPriority 已在 commonMain 中定义

// NotificationAction 已在 commonMain 中定义

@Composable
actual fun UnifyNotificationManager(
    notifications: List<NotificationItem>,
    onNotificationAction: (String, NotificationAction) -> Unit,
    modifier: Modifier,
    enableGrouping: Boolean,
    showBadges: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "通知管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                if (showBadges && notifications.isNotEmpty()) {
                    Badge {
                        Text("${notifications.size}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Text(
                    text = "暂无通知",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(notifications) { notification ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = notification.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )

                                    Badge {
                                        Text(notification.priority.name)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notification.content,
                                    style = MaterialTheme.typography.bodySmall,
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    NotificationAction.entries.forEach { action ->
                                        Button(
                                            onClick = { onNotificationAction(notification.id, action) },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text(
                                                text =
                                                    when (action) {
                                                        NotificationAction.DISMISS -> "忽略"
                                                        NotificationAction.MARK_READ -> "已读"
                                                        NotificationAction.REPLY -> "回复"
                                                        NotificationAction.OPEN -> "打开"
                                                    },
                                                style = MaterialTheme.typography.labelSmall,
                                            )
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
}
