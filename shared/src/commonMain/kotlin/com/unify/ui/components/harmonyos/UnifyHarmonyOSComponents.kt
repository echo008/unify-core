package com.unify.ui.components.harmonyos

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * HarmonyOS 分布式设备类型
 */
enum class UnifyDistributedDeviceType {
    PHONE,          // 手机
    TABLET,         // 平板
    WATCH,          // 手表
    TV,             // 电视
    CAR,            // 车机
    SMART_SPEAKER,  // 智能音箱
    IOT_DEVICE,     // IoT设备
    PC              // 电脑
}

/**
 * HarmonyOS 分布式设备信息
 */
data class UnifyDistributedDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: UnifyDistributedDeviceType,
    val isOnline: Boolean = true,
    val capabilities: List<String> = emptyList(),
    val batteryLevel: Int = 100,
    val networkType: String = "WiFi"
)

/**
 * HarmonyOS 分布式能力
 */
enum class UnifyDistributedCapability {
    CONTINUATION,       // 流转
    COLLABORATION,      // 协同
    MIGRATION,         // 迁移
    MULTI_SCREEN,      // 多屏
    CROSS_DEVICE_CALL, // 跨设备调用
    DATA_SYNC,         // 数据同步
    RESOURCE_SHARE     // 资源共享
}

/**
 * HarmonyOS 分布式设备发现组件
 */
@Composable
fun UnifyDistributedDeviceDiscovery(
    modifier: Modifier = Modifier,
    onDeviceFound: ((UnifyDistributedDevice) -> Unit)? = null,
    onDeviceLost: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isScanning by remember { mutableStateOf(false) }
    var discoveredDevices by remember { mutableStateOf<List<UnifyDistributedDevice>>(emptyList()) }
    
    LaunchedEffect(isScanning) {
        if (isScanning) {
            // 模拟设备发现
            val mockDevices = listOf(
                UnifyDistributedDevice(
                    deviceId = "harmony_phone_001",
                    deviceName = "华为 Mate 60",
                    deviceType = UnifyDistributedDeviceType.PHONE,
                    capabilities = listOf("continuation", "collaboration")
                ),
                UnifyDistributedDevice(
                    deviceId = "harmony_tablet_001",
                    deviceName = "华为 MatePad Pro",
                    deviceType = UnifyDistributedDeviceType.TABLET,
                    capabilities = listOf("multi_screen", "migration")
                ),
                UnifyDistributedDevice(
                    deviceId = "harmony_watch_001",
                    deviceName = "华为 Watch GT 4",
                    deviceType = UnifyDistributedDeviceType.WATCH,
                    capabilities = listOf("data_sync", "collaboration")
                )
            )
            
            for (device in mockDevices) {
                kotlinx.coroutines.delay(1000)
                discoveredDevices = discoveredDevices + device
                onDeviceFound?.invoke(device)
            }
        } else {
            discoveredDevices = emptyList()
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyText(
                    text = "分布式设备发现",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isScanning) {
                        val infiniteTransition = rememberInfiniteTransition(label = "scanning")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing)
                            ),
                            label = "rotation"
                        )
                        
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = rotation },
                            tint = theme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Switch(
                        checked = isScanning,
                        onCheckedChange = { isScanning = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 设备列表
            if (discoveredDevices.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(discoveredDevices.size) { index ->
                        val device = discoveredDevices[index]
                        DistributedDeviceCard(
                            device = device,
                            onConnect = { /* 处理连接 */ },
                            onDisconnect = { /* 处理断开 */ }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyText(
                        text = if (isScanning) "正在搜索设备..." else "点击开关开始搜索分布式设备",
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        color = theme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 分布式设备卡片
 */
@Composable
private fun DistributedDeviceCard(
    device: UnifyDistributedDevice,
    onConnect: (() -> Unit)? = null,
    onDisconnect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    var isConnected by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isOnline) 
                theme.colors.surfaceVariant 
            else 
                theme.colors.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getDeviceTypeIcon(device.deviceType),
                contentDescription = null,
                tint = if (device.isOnline) theme.colors.primary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                UnifyText(
                    text = device.deviceName,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Medium
                )
                UnifyText(
                    text = "${getDeviceTypeName(device.deviceType)} • ${device.networkType}",
                    variant = UnifyTextVariant.CAPTION,
                    color = theme.colors.onSurfaceVariant
                )
                
                if (device.capabilities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(device.capabilities.size) { index ->
                            val capability = device.capabilities[index]
                            AssistChip(
                                onClick = { },
                                label = {
                                    UnifyText(
                                        text = capability,
                                        variant = UnifyTextVariant.CAPTION
                                    )
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (device.isOnline) {
                    FilledTonalButton(
                        onClick = {
                            isConnected = !isConnected
                            if (isConnected) {
                                onConnect?.invoke()
                            } else {
                                onDisconnect?.invoke()
                            }
                        },
                        modifier = Modifier.height(32.dp)
                    ) {
                        UnifyText(
                            text = if (isConnected) "断开" else "连接",
                            variant = UnifyTextVariant.CAPTION
                        )
                    }
                } else {
                    UnifyText(
                        text = "离线",
                        variant = UnifyTextVariant.CAPTION,
                        color = Color.Gray
                    )
                }
                
                if (device.deviceType in listOf(
                    UnifyDistributedDeviceType.PHONE,
                    UnifyDistributedDeviceType.TABLET,
                    UnifyDistributedDeviceType.WATCH
                )) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Battery6Bar,
                            contentDescription = null,
                            tint = getBatteryColor(device.batteryLevel),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        UnifyText(
                            text = "${device.batteryLevel}%",
                            variant = UnifyTextVariant.CAPTION,
                            color = getBatteryColor(device.batteryLevel)
                        )
                    }
                }
            }
        }
    }
}

/**
 * HarmonyOS 分布式流转组件
 */
@Composable
fun UnifyDistributedContinuation(
    sourceDevice: UnifyDistributedDevice,
    targetDevices: List<UnifyDistributedDevice>,
    modifier: Modifier = Modifier,
    onContinue: ((UnifyDistributedDevice) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var selectedDevice by remember { mutableStateOf<UnifyDistributedDevice?>(null) }
    var isContinuing by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "应用流转",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 源设备
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getDeviceTypeIcon(sourceDevice.deviceType),
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "从 ${sourceDevice.deviceName}",
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = theme.colors.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 目标设备选择
            UnifyText(
                text = "流转到:",
                variant = UnifyTextVariant.BODY_MEDIUM,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(150.dp)
            ) {
                items(targetDevices.size) { index ->
                    val device = targetDevices[index]
                    val isSelected = selectedDevice?.deviceId == device.deviceId
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDevice = device },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                theme.colors.primaryContainer 
                            else 
                                theme.colors.surface
                        ),
                        border = if (isSelected) 
                            BorderStroke(2.dp, theme.colors.primary) 
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getDeviceTypeIcon(device.deviceType),
                                contentDescription = null,
                                tint = if (isSelected) theme.colors.primary else theme.colors.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                UnifyText(
                                    text = device.deviceName,
                                    variant = UnifyTextVariant.BODY_MEDIUM,
                                    color = if (isSelected) theme.colors.onPrimaryContainer else theme.colors.onSurface
                                )
                                UnifyText(
                                    text = getDeviceTypeName(device.deviceType),
                                    variant = UnifyTextVariant.CAPTION,
                                    color = if (isSelected) 
                                        theme.colors.onPrimaryContainer.copy(alpha = 0.7f) 
                                    else 
                                        theme.colors.onSurfaceVariant
                                )
                            }
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = theme.colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 流转按钮
            Button(
                onClick = {
                    selectedDevice?.let { device ->
                        isContinuing = true
                        onContinue?.invoke(device)
                    }
                },
                enabled = selectedDevice != null && !isContinuing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isContinuing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = theme.colors.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                UnifyText(
                    text = if (isContinuing) "流转中..." else "开始流转",
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    color = theme.colors.onPrimary
                )
            }
        }
    }
}

/**
 * HarmonyOS 多屏协同组件
 */
@Composable
fun UnifyMultiScreenCollaboration(
    connectedDevices: List<UnifyDistributedDevice>,
    modifier: Modifier = Modifier,
    onScreenShare: ((UnifyDistributedDevice) -> Unit)? = null,
    onScreenMirror: ((UnifyDistributedDevice) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var collaborationMode by remember { mutableStateOf("share") } // "share", "mirror", "extend"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ScreenShare,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "多屏协同",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 协同模式选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("share" to "共享", "mirror" to "镜像", "extend" to "扩展").forEach { (mode, label) ->
                    FilterChip(
                        selected = collaborationMode == mode,
                        onClick = { collaborationMode = mode },
                        label = {
                            UnifyText(
                                text = label,
                                variant = UnifyTextVariant.BODY_SMALL
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 设备网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(connectedDevices.size) { index ->
                    val device = connectedDevices[index]
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable {
                                when (collaborationMode) {
                                    "share" -> onScreenShare?.invoke(device)
                                    "mirror" -> onScreenMirror?.invoke(device)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = theme.colors.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = getDeviceTypeIcon(device.deviceType),
                                contentDescription = null,
                                tint = theme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            UnifyText(
                                text = device.deviceName,
                                variant = UnifyTextVariant.CAPTION,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * HarmonyOS 原子化服务卡片
 */
@Composable
fun UnifyAtomicServiceCard(
    serviceName: String,
    serviceIcon: ImageVector,
    serviceDescription: String,
    modifier: Modifier = Modifier,
    onLaunch: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLaunch?.invoke() }
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = serviceIcon,
                        contentDescription = null,
                        tint = theme.colors.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                UnifyText(
                    text = serviceName,
                    variant = UnifyTextVariant.BODY_LARGE,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                UnifyText(
                    text = serviceDescription,
                    variant = UnifyTextVariant.BODY_SMALL,
                    color = theme.colors.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = theme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 辅助函数
private fun getDeviceTypeIcon(deviceType: UnifyDistributedDeviceType): ImageVector {
    return when (deviceType) {
        UnifyDistributedDeviceType.PHONE -> Icons.Default.PhoneAndroid
        UnifyDistributedDeviceType.TABLET -> Icons.Default.Tablet
        UnifyDistributedDeviceType.WATCH -> Icons.Default.Watch
        UnifyDistributedDeviceType.TV -> Icons.Default.Tv
        UnifyDistributedDeviceType.CAR -> Icons.Default.DirectionsCar
        UnifyDistributedDeviceType.SMART_SPEAKER -> Icons.Default.Speaker
        UnifyDistributedDeviceType.IOT_DEVICE -> Icons.Default.DeviceHub
        UnifyDistributedDeviceType.PC -> Icons.Default.Computer
    }
}

private fun getDeviceTypeName(deviceType: UnifyDistributedDeviceType): String {
    return when (deviceType) {
        UnifyDistributedDeviceType.PHONE -> "手机"
        UnifyDistributedDeviceType.TABLET -> "平板"
        UnifyDistributedDeviceType.WATCH -> "手表"
        UnifyDistributedDeviceType.TV -> "电视"
        UnifyDistributedDeviceType.CAR -> "车机"
        UnifyDistributedDeviceType.SMART_SPEAKER -> "音箱"
        UnifyDistributedDeviceType.IOT_DEVICE -> "IoT设备"
        UnifyDistributedDeviceType.PC -> "电脑"
    }
}

private fun getBatteryColor(batteryLevel: Int): Color {
    return when {
        batteryLevel > 50 -> Color.Green
        batteryLevel > 20 -> Color.Orange
        else -> Color.Red
    }
}
