package com.unify.device.demo

import androidx.compose.foundation.layout.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.foundation.lazy.LazyColumn
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.foundation.lazy.items
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.material.icons.Icons
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.material.icons.filled.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.material3.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.runtime.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.Alignment
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.Modifier
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.graphics.Color
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.graphics.vector.ImageVector
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.text.font.FontWeight
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.unit.dp
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.delay
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.launch
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

/**
 * 设备管理演示应用
 * 展示跨平台设备信息获取、权限管理和传感器访问功能
 */
@Composable
fun UnifyDeviceDemo() {
    var deviceState by remember { mutableStateOf(DeviceDemoState()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            // 模拟设备信息加载
            deviceState = deviceState.copy(isLoading = true)
            delay(1500)
            deviceState = deviceState.copy(
                isLoading = false,
                deviceInfo = generateDeviceInfo(),
                permissions = generatePermissions(),
                sensors = generateSensors(),
                systemFeatures = generateSystemFeatures()
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "设备管理演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (deviceState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正在获取设备信息...")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DeviceInfoCard(deviceInfo = deviceState.deviceInfo)
                }
                
                item {
                    PermissionsCard(
                        permissions = deviceState.permissions,
                        onPermissionRequest = { permission ->
                            scope.launch {
                                val updatedPermissions = deviceState.permissions.map {
                                    if (it.name == permission.name) {
                                        it.copy(status = PermissionStatus.GRANTED)
                                    } else it
                                }
                                deviceState = deviceState.copy(permissions = updatedPermissions)
                            }
                        }
                    )
                }
                
                item {
                    SensorsCard(
                        sensors = deviceState.sensors,
                        onSensorToggle = { sensor, enabled ->
                            val updatedSensors = deviceState.sensors.map {
                                if (it.name == sensor.name) {
                                    it.copy(isActive = enabled)
                                } else it
                            }
                            deviceState = deviceState.copy(sensors = updatedSensors)
                        }
                    )
                }
                
                item {
                    SystemFeaturesCard(features = deviceState.systemFeatures)
                }
                
                item {
                    DeviceActionsCard(
                        onVibrate = {
                            scope.launch {
                                // 模拟振动
                                deviceState = deviceState.copy(lastAction = "设备振动")
                            }
                        },
                        onTakePhoto = {
                            scope.launch {
                                deviceState = deviceState.copy(lastAction = "拍照完成")
                            }
                        },
                        onGetLocation = {
                            scope.launch {
                                deviceState = deviceState.copy(lastAction = "获取位置: 北京市朝阳区")
                            }
                        }
                    )
                }
                
                if (deviceState.lastAction.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "最后操作: ${deviceState.lastAction}",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(deviceInfo: DeviceInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "设备信息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            DeviceInfoItem("设备型号", deviceInfo.model)
            DeviceInfoItem("操作系统", deviceInfo.platform)
            DeviceInfoItem("系统版本", deviceInfo.version)
            DeviceInfoItem("设备ID", deviceInfo.deviceId)
            DeviceInfoItem("屏幕分辨率", "${deviceInfo.screenWidth}x${deviceInfo.screenHeight}")
            DeviceInfoItem("内存", "${deviceInfo.totalMemory}GB")
            DeviceInfoItem("存储空间", "${deviceInfo.totalStorage}GB")
            DeviceInfoItem("电池电量", "${deviceInfo.batteryLevel}%")
        }
    }
}

@Composable
private fun DeviceInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PermissionsCard(
    permissions: List<DevicePermission>,
    onPermissionRequest: (DevicePermission) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "权限管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            permissions.forEach { permission ->
                PermissionItem(
                    permission = permission,
                    onRequest = { onPermissionRequest(permission) }
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    permission: DevicePermission,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = permission.icon,
                contentDescription = permission.name,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = permission.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        when (permission.status) {
            PermissionStatus.GRANTED -> {
                Surface(
                    color = Color.Green,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "已授权",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            PermissionStatus.DENIED -> {
                TextButton(onClick = onRequest) {
                    Text("请求权限")
                }
            }
            PermissionStatus.NOT_DETERMINED -> {
                Surface(
                    color = Color.Gray,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "未确定",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorsCard(
    sensors: List<DeviceSensor>,
    onSensorToggle: (DeviceSensor, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "传感器管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            sensors.forEach { sensor ->
                SensorItem(
                    sensor = sensor,
                    onToggle = { enabled -> onSensorToggle(sensor, enabled) }
                )
            }
        }
    }
}

@Composable
private fun SensorItem(
    sensor: DeviceSensor,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = sensor.icon,
                contentDescription = sensor.name,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (sensor.isActive) "当前值: ${sensor.currentValue}" else "未激活",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = sensor.isActive,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun SystemFeaturesCard(features: List<SystemFeature>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "系统功能",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            features.chunked(2).forEach { rowFeatures ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowFeatures.forEach { feature ->
                        SystemFeatureChip(
                            feature = feature,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowFeatures.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SystemFeatureChip(
    feature: SystemFeature,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (feature.isSupported) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.name,
                modifier = Modifier.size(16.dp),
                tint = if (feature.isSupported) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = feature.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (feature.isSupported) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeviceActionsCard(
    onVibrate: () -> Unit,
    onTakePhoto: () -> Unit,
    onGetLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "设备操作",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVibrate,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "振动",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("振动")
                }
                
                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "拍照",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("拍照")
                }
                
                Button(
                    onClick = onGetLocation,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "定位",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("定位")
                }
            }
        }
    }
}

// 数据生成函数
private fun generateDeviceInfo(): DeviceInfo {
    return DeviceInfo(
        model = "Unify Device Pro",
        platform = "Unify OS",
        version = "1.0.0",
        deviceId = "unify-${getCurrentTimeMillis().hashCode()}",
        screenWidth = 1080,
        screenHeight = 2340,
        totalMemory = 8,
        totalStorage = 128,
        batteryLevel = (60..100).random()
    )
}

private fun generatePermissions(): List<DevicePermission> {
    return listOf(
        DevicePermission(
            name = "相机",
            description = "访问设备相机",
            icon = Icons.Default.CameraAlt,
            status = PermissionStatus.GRANTED
        ),
        DevicePermission(
            name = "麦克风",
            description = "录制音频",
            icon = Icons.Default.Mic,
            status = PermissionStatus.DENIED
        ),
        DevicePermission(
            name = "位置",
            description = "获取设备位置",
            icon = Icons.Default.LocationOn,
            status = PermissionStatus.GRANTED
        ),
        DevicePermission(
            name = "存储",
            description = "读写文件",
            icon = Icons.Default.Storage,
            status = PermissionStatus.NOT_DETERMINED
        ),
        DevicePermission(
            name = "通知",
            description = "发送推送通知",
            icon = Icons.Default.Notifications,
            status = PermissionStatus.GRANTED
        )
    )
}

private fun generateSensors(): List<DeviceSensor> {
    return listOf(
        DeviceSensor(
            name = "加速度计",
            icon = Icons.Default.Speed,
            isActive = true,
            currentValue = "X: 0.2, Y: 9.8, Z: 0.1"
        ),
        DeviceSensor(
            name = "陀螺仪",
            icon = Icons.Default.RotateRight,
            isActive = false,
            currentValue = ""
        ),
        DeviceSensor(
            name = "磁力计",
            icon = Icons.Default.Explore,
            isActive = true,
            currentValue = "45.2°"
        ),
        DeviceSensor(
            name = "光线传感器",
            icon = Icons.Default.LightMode,
            isActive = true,
            currentValue = "350 lux"
        ),
        DeviceSensor(
            name = "距离传感器",
            icon = Icons.Default.Straighten,
            isActive = false,
            currentValue = ""
        )
    )
}

private fun generateSystemFeatures(): List<SystemFeature> {
    return listOf(
        SystemFeature("蓝牙", Icons.Default.Bluetooth, true),
        SystemFeature("WiFi", Icons.Default.Wifi, true),
        SystemFeature("NFC", Icons.Default.Nfc, true),
        SystemFeature("GPS", Icons.Default.GpsFixed, true),
        SystemFeature("指纹", Icons.Default.Fingerprint, false),
        SystemFeature("面部识别", Icons.Default.Face, true),
        SystemFeature("双卡", Icons.Default.SimCard, false),
        SystemFeature("无线充电", Icons.Default.BatteryChargingFull, true)
    )
}

// 数据类定义
data class DeviceDemoState(
    val isLoading: Boolean = false,
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val permissions: List<DevicePermission> = emptyList(),
    val sensors: List<DeviceSensor> = emptyList(),
    val systemFeatures: List<SystemFeature> = emptyList(),
    val lastAction: String = ""
)

data class DeviceInfo(
    val model: String = "",
    val platform: String = "",
    val version: String = "",
    val deviceId: String = "",
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val totalMemory: Int = 0,
    val totalStorage: Int = 0,
    val batteryLevel: Int = 0
)

data class DevicePermission(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val status: PermissionStatus
)

enum class PermissionStatus {
    GRANTED, DENIED, NOT_DETERMINED
}

data class DeviceSensor(
    val name: String,
    val icon: ImageVector,
    val isActive: Boolean,
    val currentValue: String
)

data class SystemFeature(
    val name: String,
    val icon: ImageVector,
    val isSupported: Boolean
)
