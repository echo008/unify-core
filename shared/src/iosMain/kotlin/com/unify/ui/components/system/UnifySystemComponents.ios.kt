package com.unify.ui.components.system

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock

/**
 * iOS平台系统组件实现
 */

@Composable
actual fun UnifySystemInfo(
    systemInfo: SystemInfo,
    modifier: Modifier,
    showBattery: Boolean,
    showMemory: Boolean,
    showStorage: Boolean,
    showNetwork: Boolean,
    refreshInterval: Long
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Device: ${systemInfo.deviceName}")
            Text("OS Version: ${systemInfo.osVersion}")
            Text("App Version: ${systemInfo.appVersion}")
            
            if (showBattery) {
                Text("Battery: ${(systemInfo.batteryLevel * 100).toInt()}%")
            }
            
            if (showMemory) {
                Text("Memory: ${systemInfo.availableMemory / (1024 * 1024)} MB / ${systemInfo.totalMemory / (1024 * 1024)} MB")
            }
            
            if (showStorage) {
                Text("Storage: ${systemInfo.availableStorage / (1024 * 1024 * 1024)} GB / ${systemInfo.totalStorage / (1024 * 1024 * 1024)} GB")
            }
            
            if (showNetwork) {
                Text("Network: ${systemInfo.networkType}")
            }
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
    warningThreshold: Float
) {
    val usagePercentage = usedMemory.toFloat() / totalMemory.toFloat()
    
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Memory Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (showDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Used: ${usedMemory / (1024 * 1024)} MB")
                Text("Total: ${totalMemory / (1024 * 1024)} MB")
                Text("Usage: ${(usagePercentage * 100).toInt()}%")
            }
            
            if (showChart) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = usagePercentage,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (usagePercentage > warningThreshold) Color.Red else MaterialTheme.colorScheme.primary
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
    warningThreshold: Float
) {
    val usagePercentage = usedStorage.toFloat() / totalStorage.toFloat()
    
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Storage Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Used: ${usedStorage / (1024 * 1024 * 1024)} GB")
            Text("Total: ${totalStorage / (1024 * 1024 * 1024)} GB")
            Text("Usage: ${(usagePercentage * 100).toInt()}%")
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = usagePercentage,
                modifier = Modifier.fillMaxWidth(),
                color = if (usagePercentage > warningThreshold) Color.Red else MaterialTheme.colorScheme.primary
            )
            
            if (showBreakdown && categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Breakdown:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                categories.forEach { (category, size) ->
                    Text("$category: ${size / (1024 * 1024 * 1024)} GB")
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
    showDetails: Boolean
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Network Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Type: $networkType")
            Text("Connected: ${if (isConnected) "Yes" else "No"}")
            
            if (showDetails) {
                Text("Signal: ${(signalStrength * 100).toInt()}%")
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = signalStrength,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (showSpeed) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNetworkTest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Speed")
                }
            }
        }
    }
}

@Composable
actual fun UnifyDeviceOrientation(
    onOrientationChange: (DeviceOrientation) -> Unit,
    modifier: Modifier,
    showIndicator: Boolean,
    lockOrientation: DeviceOrientation?
) {
    var currentOrientation by remember { mutableStateOf(DeviceOrientation.PORTRAIT) }
    
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Device Orientation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (showIndicator) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current: ${currentOrientation.name}")
                
                if (lockOrientation != null) {
                    Text("Locked to: ${lockOrientation.name}")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceOrientation.values().forEach { orientation ->
                    Button(
                        onClick = {
                            currentOrientation = orientation
                            onOrientationChange(orientation)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(orientation.name.take(1))
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
    presetPatterns: List<VibrationPattern>
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vibration Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (enableCustomPatterns) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Custom patterns enabled",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val defaultPatterns = if (presetPatterns.isNotEmpty()) presetPatterns else listOf(
                    VibrationPattern("Short", longArrayOf(0, 100), -1),
                    VibrationPattern("Medium", longArrayOf(0, 200), -1),
                    VibrationPattern("Long", longArrayOf(0, 500), -1)
                )
                
                defaultPatterns.forEach { pattern ->
                    Button(
                        onClick = { onVibrate(pattern) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(pattern.name)
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
    showSlider: Boolean
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Brightness Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (showSlider) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Brightness: ${(brightness * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (enableAutoAdjust) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Handle auto adjust */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Auto Adjust")
                }
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
    enableMute: Boolean
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Volume Control (${volumeType.name})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (showSlider) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Volume: ${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (enableMute) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVolumeChange(if (volume > 0) 0f else 0.5f) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (volume > 0) "Mute" else "Unmute")
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
    enableAutoDetect: Boolean
) {
    var clipboardText by remember { mutableStateOf("") }
    val clipboardHistory = remember { mutableStateListOf<String>() }
    
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Clipboard Manager",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = clipboardText,
                onValueChange = { 
                    clipboardText = it
                    onClipboardChange(it)
                },
                label = { Text("Clipboard Content") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        // Copy to system clipboard
                        if (clipboardText.isNotEmpty() && clipboardHistory.size < maxHistorySize) {
                            clipboardHistory.add(0, clipboardText)
                        }
                    }
                ) {
                    Text("Copy")
                }
                Button(
                    onClick = { 
                        // Paste from system clipboard
                        clipboardText = "Pasted content"
                        onClipboardChange(clipboardText)
                    }
                ) {
                    Text("Paste")
                }
            }
            
            if (showHistory && clipboardHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                clipboardHistory.take(5).forEach { item ->
                    Text(
                        text = item.take(50) + if (item.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable { 
                                clipboardText = item
                                onClipboardChange(item)
                            }
                    )
                }
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
    showBadges: Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        if (enableGrouping) {
            val groupedNotifications = notifications.groupBy { it.category }
            groupedNotifications.forEach { (category, categoryNotifications) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp, 8.dp)
                    )
                }
                items(categoryNotifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onAction = onNotificationAction,
                        showBadge = showBadges
                    )
                }
            }
        } else {
            items(notifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onAction = onNotificationAction,
                    showBadge = showBadges
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onAction: (String, NotificationAction) -> Unit,
    showBadge: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = notification.content,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (notification.timestamp > 0) {
                        Text(
                            text = formatNotificationTime(notification.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                if (showBadge && !notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
            }
            
            if (notification.actions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notification.actions.forEach { action ->
                        TextButton(
                            onClick = { onAction(notification.id, action) }
                        ) {
                            Text(action.title)
                        }
                    }
                }
            }
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}

@Composable
actual fun UnifyPerformanceMonitor(
    metrics: Set<PerformanceMetric>,
    onMetricsUpdate: (Map<PerformanceMetric, Float>) -> Unit,
    modifier: Modifier,
    showRealTimeChart: Boolean,
    maxDataPoints: Int,
    updateInterval: Long
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance Monitor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            metrics.forEach { metric ->
                Text("${metric.name}: Monitoring...")
            }
            
            if (showRealTimeChart) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Real-time chart enabled")
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
    chargingColor: Color
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Battery Indicator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (showPercentage) {
                Text("Level: ${(batteryLevel * 100).toInt()}%")
            }
            
            Text("Charging: ${if (isCharging) "Yes" else "No"}")
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = batteryLevel,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    isCharging -> chargingColor
                    batteryLevel < lowBatteryThreshold -> warningColor
                    else -> normalColor
                }
            )
        }
    }
}

@Composable
actual fun UnifyBatteryStatus(
    modifier: Modifier,
    showPercentage: Boolean
) {
    val batteryLevel = remember { mutableStateOf(0.75f) }
    val isCharging = remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Battery Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (showPercentage) {
                Text("Battery: ${(batteryLevel.value * 100).toInt()}%")
            }
            
            Text("Status: ${if (isCharging.value) "Charging" else "Not Charging"}")
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = batteryLevel.value,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
actual fun UnifyCPUUsage(
    modifier: Modifier,
    refreshInterval: Long
) {
    val cpuUsage = remember { mutableStateOf(25.0f) }
    val coreCount = remember { 8 }
    
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "CPU Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Usage: ${cpuUsage.value.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Cores: $coreCount",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = cpuUsage.value / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
