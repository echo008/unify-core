package com.unify.ui.components.harmonyos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Unify HarmonyOS平台特定组件
 * 专为HarmonyOS平台优化的UI组件，集成分布式特性
 */

@Composable
fun UnifyHarmonyCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: String? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick ?: {},
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (content != null) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
fun UnifyHarmonyServiceCard(
    serviceName: String,
    deviceName: String,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UnifyHarmonyCard(
        title = serviceName,
        subtitle = deviceName,
        icon = if (isConnected) "🔗" else "📱",
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isConnected) "已连接" else "未连接",
                style = MaterialTheme.typography.bodySmall,
                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFF757575),
            )

            Button(
                onClick = if (isConnected) onDisconnect else onConnect,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isConnected) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    ),
            ) {
                Text(if (isConnected) "断开" else "连接")
            }
        }
    }
}

@Composable
fun UnifyHarmonyDeviceList(
    devices: List<UnifyHarmonyDevice>,
    onDeviceClick: (UnifyHarmonyDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(devices) { device ->
            UnifyHarmonyDeviceItem(
                device = device,
                onClick = { onDeviceClick(device) },
            )
        }
    }
}

@Composable
private fun UnifyHarmonyDeviceItem(
    device: UnifyHarmonyDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
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
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 16.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = device.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (device.isOnline) Color(0xFF4CAF50) else Color(0xFF757575),
            ) {
                Text(
                    text = if (device.isOnline) "在线" else "离线",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

data class UnifyHarmonyDevice(
    val id: String,
    val name: String,
    val type: String,
    val icon: String,
    val isOnline: Boolean,
)

@Composable
fun UnifyHarmonyDistributedPanel(
    title: String,
    devices: List<UnifyHarmonyDevice>,
    onDeviceSelect: (UnifyHarmonyDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(devices) { device ->
                    Surface(
                        onClick = { onDeviceSelect(device) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = device.icon,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(end = 12.dp),
                            )

                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )

                            if (device.isOnline) {
                                Text(
                                    text = "✓",
                                    color = Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnifyHarmonyAtomicService(
    serviceName: String,
    description: String,
    icon: String,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
    isInstalled: Boolean = true,
) {
    Card(
        modifier = modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = serviceName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Button(
                onClick = onLaunch,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isInstalled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                    ),
            ) {
                Text(if (isInstalled) "启动" else "安装")
            }
        }
    }
}

@Composable
fun UnifyHarmonyMultiScreenLayout(
    primaryContent: @Composable () -> Unit,
    secondaryContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    isMultiScreen: Boolean = false,
) {
    if (isMultiScreen && secondaryContent != null) {
        Row(
            modifier = modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.weight(1f),
            ) {
                primaryContent()
            }

            Divider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp),
            )

            Box(
                modifier = Modifier.weight(1f),
            ) {
                secondaryContent()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            primaryContent()
        }
    }
}

@Composable
fun UnifyHarmonyFlowLayout(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 简化的流式布局实现
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        var currentRow = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        items.forEach { item ->
            if (currentRow.size < 3) { // 每行最多3个
                currentRow.add(item)
            } else {
                rows.add(currentRow.toList())
                currentRow = mutableListOf(item)
            }
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { item ->
                    FilterChip(
                        onClick = { onItemClick(item) },
                        label = { Text(item) },
                        selected = false,
                        modifier = Modifier.weight(1f),
                    )
                }
                // 填充剩余空间
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
