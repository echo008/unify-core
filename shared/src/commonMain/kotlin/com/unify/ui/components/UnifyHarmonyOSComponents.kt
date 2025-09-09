package com.unify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unify HarmonyOS组件
 * 提供HarmonyOS平台特有的UI组件和交互
 */

/**
 * HarmonyOS原子化服务卡片
 */
@Composable
fun UnifyAtomicServiceCard(
    title: String,
    description: String,
    icon: String,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onLaunch() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 图标
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 启动按钮
            Text(
                text = "启动",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * HarmonyOS分布式任务栏
 */
@Composable
fun UnifyDistributedTaskBar(
    connectedDevices: List<HarmonyDevice>,
    onDeviceSelect: (HarmonyDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "分布式设备",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(connectedDevices) { device ->
                    UnifyDeviceChip(
                        device = device,
                        onClick = { onDeviceSelect(device) },
                    )
                }
            }
        }
    }
}

/**
 * 设备芯片
 */
@Composable
private fun UnifyDeviceChip(
    device: HarmonyDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (device.isConnected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = device.icon,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
            if (device.isConnected) {
                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .background(
                                Color.Green,
                                androidx.compose.foundation.shape.CircleShape,
                            ),
                )
            }
        }
    }
}

/**
 * HarmonyOS超级终端
 */
@Composable
fun UnifySuperDevice(
    mainDevice: HarmonyDevice,
    connectedDevices: List<HarmonyDevice>,
    onDeviceAction: (HarmonyDevice, DeviceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "超级终端",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // 主设备
            UnifyMainDeviceCard(
                device = mainDevice,
                onAction = { action -> onDeviceAction(mainDevice, action) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 连接的设备
            if (connectedDevices.isNotEmpty()) {
                Text(
                    text = "已连接设备",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(connectedDevices) { device ->
                        UnifyConnectedDeviceItem(
                            device = device,
                            onAction = { action -> onDeviceAction(device, action) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 主设备卡片
 */
@Composable
private fun UnifyMainDeviceCard(
    device: HarmonyDevice,
    onAction: (DeviceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = device.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "主设备 • ${device.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onAction(DeviceAction.Share) }) {
                    Text("📤", fontSize = 16.sp)
                }
                IconButton(onClick = { onAction(DeviceAction.Settings) }) {
                    Text("⚙️", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * 连接设备项
 */
@Composable
private fun UnifyConnectedDeviceItem(
    device: HarmonyDevice,
    onAction: (DeviceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = device.icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 8.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = device.type,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = { onAction(DeviceAction.Connect) },
                modifier = Modifier.size(32.dp),
            ) {
                Text("🔗", fontSize = 14.sp)
            }
            IconButton(
                onClick = { onAction(DeviceAction.Disconnect) },
                modifier = Modifier.size(32.dp),
            ) {
                Text("❌", fontSize = 14.sp)
            }
        }
    }
}

/**
 * HarmonyOS流转任务
 */
@Composable
fun UnifyFlowTask(
    task: FlowTask,
    onContinueOnDevice: (HarmonyDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
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
                    text = "任务流转",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = task.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "可继续的设备:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(task.availableDevices) { device ->
                    Button(
                        onClick = { onContinueOnDevice(device) },
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text(
                            text = "${device.icon} ${device.name}",
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * HarmonyOS服务卡片
 */
@Composable
fun UnifyServiceCard(
    service: HarmonyService,
    onServiceAction: (ServiceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onServiceAction(ServiceAction.Open) },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = service.icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (service.isRunning) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .background(
                                    Color.Green,
                                    androidx.compose.foundation.shape.CircleShape,
                                ),
                    )
                }
            }

            if (service.quickActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(service.quickActions) { action ->
                        OutlinedButton(
                            onClick = { onServiceAction(ServiceAction.QuickAction(action)) },
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text(
                                text = action,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * HarmonyOS万能卡片
 */
@Composable
fun UnifyUniversalCard(
    cardData: UniversalCardData,
    onCardAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // 卡片头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cardData.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = cardData.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 卡片内容
            Text(
                text = cardData.content,
                style = MaterialTheme.typography.bodyMedium,
            )

            // 卡片操作
            if (cardData.actions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    cardData.actions.forEach { action ->
                        TextButton(
                            onClick = { onCardAction(action) },
                        ) {
                            Text(action)
                        }
                    }
                }
            }
        }
    }
}

// 数据类
data class HarmonyDevice(
    val id: String,
    val name: String,
    val type: String,
    val icon: String,
    val isConnected: Boolean = false,
)

data class FlowTask(
    val id: String,
    val description: String,
    val status: String,
    val availableDevices: List<HarmonyDevice>,
)

data class HarmonyService(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isRunning: Boolean = false,
    val quickActions: List<String> = emptyList(),
)

data class UniversalCardData(
    val title: String,
    val content: String,
    val timestamp: String,
    val actions: List<String> = emptyList(),
)

// 枚举
enum class DeviceAction {
    Connect,
    Disconnect,
    Share,
    Settings,
}

sealed class ServiceAction {
    object Open : ServiceAction()

    data class QuickAction(val action: String) : ServiceAction()
}

// 扩展函数
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(Modifier.padding(2.dp))
}
