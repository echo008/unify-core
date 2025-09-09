package com.unify.platform.harmony

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * HarmonyOS平台特定组件
 * 提供鸿蒙系统专用的UI组件和分布式功能
 */

/**
 * 鸿蒙分布式设备卡片
 */
@Composable
fun UnifyHarmonyDeviceCard(
    device: HarmonyDevice,
    onConnect: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isConnected: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = getDeviceIcon(device.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )

                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = device.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // 连接状态指示器
                Box(
                    modifier =
                        Modifier
                            .size(12.dp)
                            .background(
                                color = if (isConnected) Color.Green else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape,
                            ),
                )
            }

            // 设备信息
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                HarmonyDeviceInfoRow("设备ID", device.deviceId)
                HarmonyDeviceInfoRow("系统版本", device.osVersion)
                HarmonyDeviceInfoRow("网络类型", device.networkType)
                if (device.batteryLevel != null) {
                    HarmonyDeviceInfoRow("电量", "${device.batteryLevel}%")
                }
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isConnected) {
                    Button(
                        onClick = { onDisconnect(device.deviceId) },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("断开连接")
                    }
                } else {
                    Button(
                        onClick = { onConnect(device.deviceId) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("连接设备")
                    }
                }

                OutlinedButton(
                    onClick = { /* 设备详情 */ },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("详情")
                }
            }
        }
    }
}

@Composable
private fun HarmonyDeviceInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * 鸿蒙多屏协同组件
 */
@Composable
fun UnifyHarmonyMultiScreen(
    screens: List<HarmonyScreen>,
    activeScreenId: String,
    onScreenSelected: (String) -> Unit,
    onMirrorScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "多屏协同",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            screens.forEach { screen ->
                HarmonyScreenItem(
                    screen = screen,
                    isActive = screen.screenId == activeScreenId,
                    onSelect = { onScreenSelected(screen.screenId) },
                    onMirror = { onMirrorScreen(screen.screenId) },
                )
            }
        }
    }
}

@Composable
private fun HarmonyScreenItem(
    screen: HarmonyScreen,
    isActive: Boolean,
    onSelect: () -> Unit,
    onMirror: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    RoundedCornerShape(8.dp),
                ),
        onClick = onSelect,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Monitor,
                    contentDescription = null,
                    tint =
                        if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )

                Column {
                    Text(
                        text = screen.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color =
                            if (isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                    Text(
                        text = "${screen.resolution.width}x${screen.resolution.height}",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }

            IconButton(onClick = onMirror) {
                Icon(
                    imageVector = Icons.Default.ScreenShare,
                    contentDescription = "镜像屏幕",
                    tint =
                        if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
            }
        }
    }
}

/**
 * 鸿蒙原子化服务卡片
 */
@Composable
fun UnifyHarmonyAtomicService(
    service: HarmonyAtomicService,
    onLaunch: (String) -> Unit,
    onInstall: (String) -> Unit,
    modifier: Modifier = Modifier,
    isInstalled: Boolean = false,
) {
    Card(
        modifier = modifier.width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 服务图标
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = service.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            // 服务信息
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Text(
                    text = "v${service.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 操作按钮
            if (isInstalled) {
                Button(
                    onClick = { onLaunch(service.serviceId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("启动")
                }
            } else {
                OutlinedButton(
                    onClick = { onInstall(service.serviceId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("安装")
                }
            }
        }
    }
}

/**
 * 鸿蒙分布式数据同步状态
 */
@Composable
fun UnifyHarmonyDataSync(
    syncStatus: HarmonyDataSyncStatus,
    onStartSync: () -> Unit,
    onStopSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "分布式数据同步",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                Surface(
                    color =
                        when (syncStatus.status) {
                            SyncStatus.SYNCING -> Color.Blue
                            SyncStatus.COMPLETED -> Color.Green
                            SyncStatus.ERROR -> Color.Red
                            SyncStatus.IDLE -> Color.Gray
                        },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = syncStatus.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // 同步进度
            if (syncStatus.status == SyncStatus.SYNCING) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LinearProgressIndicator(
                        progress = syncStatus.progress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${(syncStatus.progress * 100).toInt()}% - ${syncStatus.currentTask}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 同步统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                HarmonySyncStatItem("已同步", syncStatus.syncedCount.toString())
                HarmonySyncStatItem("待同步", syncStatus.pendingCount.toString())
                HarmonySyncStatItem("失败", syncStatus.errorCount.toString())
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (syncStatus.status == SyncStatus.SYNCING) {
                    Button(
                        onClick = onStopSync,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("停止同步")
                    }
                } else {
                    Button(
                        onClick = onStartSync,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("开始同步")
                    }
                }

                OutlinedButton(
                    onClick = { /* 同步设置 */ },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("设置")
                }
            }
        }
    }
}

@Composable
private fun HarmonySyncStatItem(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 鸿蒙智能推荐组件
 */
@Composable
fun UnifyHarmonySmartRecommendation(
    recommendations: List<HarmonyRecommendation>,
    onRecommendationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "智能推荐",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            recommendations.forEach { recommendation ->
                HarmonyRecommendationItem(
                    recommendation = recommendation,
                    onClick = { onRecommendationClick(recommendation.id) },
                )
            }
        }
    }
}

@Composable
private fun HarmonyRecommendationItem(
    recommendation: HarmonyRecommendation,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = recommendation.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "${(recommendation.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

// 辅助函数
private fun getDeviceIcon(type: HarmonyDeviceType): ImageVector {
    return when (type) {
        HarmonyDeviceType.PHONE -> Icons.Default.PhoneAndroid
        HarmonyDeviceType.TABLET -> Icons.Default.Tablet
        HarmonyDeviceType.WATCH -> Icons.Default.Watch
        HarmonyDeviceType.TV -> Icons.Default.Tv
        HarmonyDeviceType.CAR -> Icons.Default.DirectionsCar
        HarmonyDeviceType.SPEAKER -> Icons.Default.Speaker
        HarmonyDeviceType.HEADSET -> Icons.Default.Headset
        HarmonyDeviceType.CAMERA -> Icons.Default.Camera
    }
}

// 数据类定义
data class HarmonyDevice(
    val deviceId: String,
    val name: String,
    val type: HarmonyDeviceType,
    val osVersion: String,
    val networkType: String,
    val batteryLevel: Int? = null,
)

enum class HarmonyDeviceType(val displayName: String) {
    PHONE("手机"),
    TABLET("平板"),
    WATCH("手表"),
    TV("电视"),
    CAR("车机"),
    SPEAKER("音箱"),
    HEADSET("耳机"),
    CAMERA("摄像头"),
}

data class HarmonyScreen(
    val screenId: String,
    val name: String,
    val resolution: ScreenResolution,
)

data class ScreenResolution(
    val width: Int,
    val height: Int,
)

data class HarmonyAtomicService(
    val serviceId: String,
    val name: String,
    val description: String,
    val version: String,
    val icon: ImageVector,
)

data class HarmonyDataSyncStatus(
    val status: SyncStatus,
    val progress: Float,
    val currentTask: String,
    val syncedCount: Int,
    val pendingCount: Int,
    val errorCount: Int,
)

enum class SyncStatus(val displayName: String) {
    IDLE("空闲"),
    SYNCING("同步中"),
    COMPLETED("已完成"),
    ERROR("错误"),
}

data class HarmonyRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val confidence: Float,
)
