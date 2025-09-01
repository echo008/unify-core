package com.unify.device.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.device.*
import kotlinx.coroutines.launch

/**
 * 统一设备功能演示应用
 * 展示权限管理、设备信息、传感器访问、系统功能和硬件访问
 */
@Composable
fun UnifyDeviceDemo() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("权限管理", "设备信息", "传感器", "系统功能", "硬件访问")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Unify Device 演示",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> PermissionDemo()
            1 -> DeviceInfoDemo()
            2 -> SensorDemo()
            3 -> SystemFeaturesDemo()
            4 -> HardwareDemo()
        }
    }
}

@Composable
fun PermissionDemo() {
    val scope = rememberCoroutineScope()
    var permissionResults by remember { mutableStateOf<Map<UnifyPermission, UnifyPermissionStatus>>(emptyMap()) }
    
    val deviceManager = remember {
        UnifyDeviceManagerFactory.create()
    }
    
    LaunchedEffect(Unit) {
        deviceManager.initialize()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "权限管理",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            val permissions = listOf(
                                UnifyPermission.CAMERA,
                                UnifyPermission.MICROPHONE,
                                UnifyPermission.LOCATION_FINE,
                                UnifyPermission.STORAGE_READ,
                                UnifyPermission.STORAGE_WRITE
                            )
                            permissionResults = deviceManager.permissions.checkPermissions(permissions)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("检查权限状态")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (permissionResults.isNotEmpty()) {
                    Text(
                        text = "权限状态:",
                        fontWeight = FontWeight.Bold
                    )
                    
                    permissionResults.forEach { (permission, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(permission.name)
                            Text(
                                text = status.name,
                                color = when (status) {
                                    UnifyPermissionStatus.GRANTED -> Color.Green
                                    UnifyPermissionStatus.DENIED -> Color.Red
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceInfoDemo() {
    val scope = rememberCoroutineScope()
    var deviceDetails by remember { mutableStateOf<UnifyDeviceDetails?>(null) }
    var systemInfo by remember { mutableStateOf<UnifySystemInfo?>(null) }
    var hardwareInfo by remember { mutableStateOf<UnifyHardwareInfo?>(null) }
    
    val deviceManager = remember {
        UnifyDeviceManagerFactory.create()
    }
    
    LaunchedEffect(Unit) {
        deviceManager.initialize()
        scope.launch {
            deviceDetails = deviceManager.deviceInfo.getDeviceInfo()
            systemInfo = deviceManager.deviceInfo.getSystemInfo()
            hardwareInfo = deviceManager.deviceInfo.getHardwareInfo()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        deviceDetails?.let { details ->
            DeviceInfoCard("设备信息", listOf(
                "设备名称" to details.deviceName,
                "制造商" to details.manufacturer,
                "型号" to details.model,
                "品牌" to details.brand,
                "是否模拟器" to if (details.isEmulator) "是" else "否",
                "是否Root" to if (details.isRooted) "是" else "否"
            ))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        systemInfo?.let { system ->
            DeviceInfoCard("系统信息", listOf(
                "操作系统" to system.osName,
                "系统版本" to system.osVersion,
                "API级别" to system.osApiLevel.toString(),
                "语言环境" to system.locale,
                "时区" to system.timezone
            ))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        hardwareInfo?.let { hardware ->
            DeviceInfoCard("硬件信息", listOf(
                "CPU架构" to hardware.cpuArchitecture,
                "CPU核心数" to hardware.cpuCores.toString(),
                "总内存" to formatBytes(hardware.totalMemory),
                "可用内存" to formatBytes(hardware.availableMemory),
                "屏幕分辨率" to "${hardware.screenWidth}x${hardware.screenHeight}",
                "屏幕密度" to hardware.screenDensity.toString()
            ))
        }
    }
}

@Composable
fun DeviceInfoCard(title: String, items: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            items.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(key, modifier = Modifier.weight(1f))
                    Text(value, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SensorDemo() {
    val scope = rememberCoroutineScope()
    var availableSensors by remember { mutableStateOf<List<UnifySensorInfo>>(emptyList()) }
    var sensorData by remember { mutableStateOf<UnifySensorData?>(null) }
    var isListening by remember { mutableStateOf(false) }
    
    val deviceManager = remember {
        UnifyDeviceManagerFactory.create()
    }
    
    LaunchedEffect(Unit) {
        deviceManager.initialize()
        scope.launch {
            availableSensors = deviceManager.sensors.getAvailableSensors()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "传感器管理",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = {
                        scope.launch {
                            if (!isListening) {
                                deviceManager.sensors.startSensorListening(
                                    UnifySensorType.ACCELEROMETER
                                ).collect { data ->
                                    sensorData = data
                                }
                                isListening = true
                            } else {
                                deviceManager.sensors.stopSensorListening(UnifySensorType.ACCELEROMETER)
                                isListening = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isListening) "停止监听加速度计" else "开始监听加速度计")
                }
                
                sensorData?.let { data ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("加速度计数据:", fontWeight = FontWeight.Bold)
                    Text("X: ${data.values.getOrNull(0) ?: 0f}")
                    Text("Y: ${data.values.getOrNull(1) ?: 0f}")
                    Text("Z: ${data.values.getOrNull(2) ?: 0f}")
                    Text("精度: ${data.accuracy}")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "可用传感器",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(availableSensors) { sensor ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = sensor.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "类型: ${sensor.type.name}",
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "厂商: ${sensor.vendor}",
                                    fontSize = 12.sp
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
fun SystemFeaturesDemo() {
    val scope = rememberCoroutineScope()
    val deviceManager = remember {
        UnifyDeviceManagerFactory.create()
    }
    
    LaunchedEffect(Unit) {
        deviceManager.initialize()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "系统功能",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                deviceManager.systemFeatures.vibrate(500)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("振动")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                deviceManager.systemFeatures.copyToClipboard("测试文本")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("复制文本")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            val clipboardText = deviceManager.systemFeatures.getFromClipboard()
                            // 显示剪贴板内容
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("读取剪贴板")
                }
            }
        }
    }
}

@Composable
fun HardwareDemo() {
    val scope = rememberCoroutineScope()
    var hardwareStatus by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    
    val deviceManager = remember {
        UnifyDeviceManagerFactory.create()
    }
    
    LaunchedEffect(Unit) {
        deviceManager.initialize()
        scope.launch {
            hardwareStatus = mapOf(
                "相机" to deviceManager.hardware.isCameraAvailable(),
                "麦克风" to deviceManager.hardware.isMicrophoneAvailable(),
                "位置服务" to deviceManager.hardware.isLocationAvailable(),
                "蓝牙" to deviceManager.hardware.isBluetoothAvailable(),
                "NFC" to deviceManager.hardware.isNFCAvailable(),
                "生物识别" to deviceManager.hardware.isBiometricAvailable()
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "硬件功能",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                hardwareStatus.forEach { (feature, available) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(feature)
                        Text(
                            text = if (available) "可用" else "不可用",
                            color = if (available) Color.Green else Color.Red
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            val result = deviceManager.hardware.takePicture()
                            // 处理拍照结果
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("拍照")
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "${bytes} B"
    }
}
