package com.unify.ui.components.sensor

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 传感器类型
 */
enum class UnifySensorType {
    ACCELEROMETER,      // 加速度传感器
    GYROSCOPE,         // 陀螺仪
    MAGNETOMETER,      // 磁力计
    PROXIMITY,         // 距离传感器
    LIGHT,             // 光线传感器
    PRESSURE,          // 气压传感器
    TEMPERATURE,       // 温度传感器
    HUMIDITY,          // 湿度传感器
    STEP_COUNTER,      // 计步器
    HEART_RATE,        // 心率传感器
    FINGERPRINT,       // 指纹传感器
    FACE_ID,           // 面部识别
    VOICE_ID,          // 声纹识别
    GESTURE,           // 手势识别
    MOTION,            // 运动检测
    ORIENTATION,       // 方向传感器
    GPS,               // GPS定位
    NFC,               // NFC近场通信
    BLUETOOTH,         // 蓝牙
    WIFI               // WiFi
}

/**
 * 传感器数据
 */
data class UnifySensorData(
    val type: UnifySensorType,
    val values: FloatArray,
    val accuracy: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnifySensorData) return false
        
        if (type != other.type) return false
        if (!values.contentEquals(other.values)) return false
        if (accuracy != other.accuracy) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + values.contentHashCode()
        result = 31 * result + accuracy
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

/**
 * 传感器配置
 */
data class UnifySensorConfig(
    val sensorType: UnifySensorType,
    val samplingRate: Int = 50,           // 采样率(Hz)
    val sensitivity: Float = 1f,          // 灵敏度
    val enableFilter: Boolean = true,     // 启用滤波
    val filterAlpha: Float = 0.8f,       // 滤波系数
    val enableCalibration: Boolean = false, // 启用校准
    val autoStart: Boolean = false,       // 自动开始
    val bufferSize: Int = 100            // 缓冲区大小
)

/**
 * 传感器状态
 */
enum class UnifySensorState {
    IDLE,              // 空闲
    STARTING,          // 启动中
    RUNNING,           // 运行中
    PAUSED,            // 暂停
    STOPPED,           // 停止
    ERROR,             // 错误
    PERMISSION_DENIED, // 权限被拒绝
    NOT_AVAILABLE      // 传感器不可用
}

/**
 * 通用传感器组件
 */
@Composable
fun UnifySensor(
    config: UnifySensorConfig,
    modifier: Modifier = Modifier,
    onDataReceived: ((UnifySensorData) -> Unit)? = null,
    onStateChange: ((UnifySensorState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var sensorState by remember { mutableStateOf(UnifySensorState.IDLE) }
    var sensorData by remember { mutableStateOf<UnifySensorData?>(null) }
    var dataBuffer by remember { mutableStateOf<List<UnifySensorData>>(emptyList()) }
    
    LaunchedEffect(config.sensorType) {
        if (config.autoStart) {
            sensorState = UnifySensorState.STARTING
            onStateChange?.invoke(sensorState)
            
            delay(500) // 模拟传感器初始化
            sensorState = UnifySensorState.RUNNING
            onStateChange?.invoke(sensorState)
            
            // 模拟传感器数据
            while (sensorState == UnifySensorState.RUNNING) {
                val mockData = generateMockSensorData(config.sensorType)
                sensorData = mockData
                onDataReceived?.invoke(mockData)
                
                // 更新缓冲区
                dataBuffer = (dataBuffer + mockData).takeLast(config.bufferSize)
                
                delay(1000L / config.samplingRate)
            }
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 传感器标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getSensorIcon(config.sensorType),
                        contentDescription = null,
                        tint = theme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(
                        text = getSensorDisplayName(config.sensorType),
                        variant = UnifyTextVariant.H6,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 状态指示器
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = when (sensorState) {
                                UnifySensorState.RUNNING -> Color.Green
                                UnifySensorState.ERROR -> Color.Red
                                UnifySensorState.STARTING -> Color.Orange
                                else -> Color.Gray
                            },
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 传感器数据显示
            sensorData?.let { data ->
                Column {
                    UnifyText(
                        text = "当前数据:",
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    data.values.forEachIndexed { index, value ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            UnifyText(
                                text = getAxisName(config.sensorType, index),
                                variant = UnifyTextVariant.BODY_SMALL
                            )
                            UnifyText(
                                text = String.format("%.3f", value),
                                variant = UnifyTextVariant.BODY_SMALL,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 精度显示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UnifyText(
                            text = "精度:",
                            variant = UnifyTextVariant.BODY_SMALL
                        )
                        UnifyText(
                            text = getAccuracyText(data.accuracy),
                            variant = UnifyTextVariant.BODY_SMALL,
                            color = getAccuracyColor(data.accuracy)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        when (sensorState) {
                            UnifySensorState.IDLE, UnifySensorState.STOPPED -> {
                                sensorState = UnifySensorState.STARTING
                                onStateChange?.invoke(sensorState)
                            }
                            UnifySensorState.RUNNING -> {
                                sensorState = UnifySensorState.PAUSED
                                onStateChange?.invoke(sensorState)
                            }
                            UnifySensorState.PAUSED -> {
                                sensorState = UnifySensorState.RUNNING
                                onStateChange?.invoke(sensorState)
                            }
                            else -> {}
                        }
                    },
                    enabled = sensorState != UnifySensorState.STARTING && sensorState != UnifySensorState.ERROR
                ) {
                    UnifyText(
                        text = when (sensorState) {
                            UnifySensorState.IDLE, UnifySensorState.STOPPED -> "开始"
                            UnifySensorState.RUNNING -> "暂停"
                            UnifySensorState.PAUSED -> "继续"
                            else -> "启动中"
                        },
                        variant = UnifyTextVariant.BUTTON
                    )
                }
                
                Button(
                    onClick = {
                        sensorState = UnifySensorState.STOPPED
                        onStateChange?.invoke(sensorState)
                        sensorData = null
                        dataBuffer = emptyList()
                    },
                    enabled = sensorState != UnifySensorState.IDLE && sensorState != UnifySensorState.STOPPED
                ) {
                    UnifyText(
                        text = "停止",
                        variant = UnifyTextVariant.BUTTON
                    )
                }
            }
        }
    }
}

/**
 * 生物识别组件
 */
@Composable
fun UnifyBiometric(
    biometricType: UnifySensorType,
    modifier: Modifier = Modifier,
    onSuccess: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 生物识别图标
            Icon(
                imageVector = when (biometricType) {
                    UnifySensorType.FINGERPRINT -> Icons.Default.Fingerprint
                    UnifySensorType.FACE_ID -> Icons.Default.Face
                    UnifySensorType.VOICE_ID -> Icons.Default.RecordVoiceOver
                    else -> Icons.Default.Security
                },
                contentDescription = null,
                tint = if (isScanning) theme.colors.primary else Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 状态文本
            UnifyText(
                text = when {
                    scanResult != null -> "识别成功"
                    isScanning -> when (biometricType) {
                        UnifySensorType.FINGERPRINT -> "请按压指纹传感器"
                        UnifySensorType.FACE_ID -> "请正视摄像头"
                        UnifySensorType.VOICE_ID -> "请说话进行声纹识别"
                        else -> "正在进行生物识别"
                    }
                    else -> when (biometricType) {
                        UnifySensorType.FINGERPRINT -> "指纹识别"
                        UnifySensorType.FACE_ID -> "面部识别"
                        UnifySensorType.VOICE_ID -> "声纹识别"
                        else -> "生物识别"
                    }
                },
                variant = UnifyTextVariant.H6,
                fontWeight = FontWeight.Medium,
                color = if (scanResult != null) Color.Green else theme.colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            UnifyText(
                text = when {
                    scanResult != null -> "身份验证通过"
                    isScanning -> "正在扫描中，请保持稳定"
                    else -> "点击开始身份验证"
                },
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = theme.colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 控制按钮
            if (scanResult == null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isScanning) {
                        Button(
                            onClick = {
                                isScanning = false
                                onCancel?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            )
                        ) {
                            UnifyText(
                                text = "取消",
                                variant = UnifyTextVariant.BUTTON
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isScanning = true
                                // 模拟生物识别过程
                                LaunchedEffect(Unit) {
                                    delay(2000) // 模拟识别时间
                                    val success = (0..10).random() > 2 // 80%成功率
                                    if (success) {
                                        scanResult = "user_${System.currentTimeMillis()}"
                                        onSuccess?.invoke(scanResult!!)
                                    } else {
                                        onError?.invoke("识别失败，请重试")
                                    }
                                    isScanning = false
                                }
                            }
                        ) {
                            UnifyText(
                                text = "开始识别",
                                variant = UnifyTextVariant.BUTTON
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        scanResult = null
                        isScanning = false
                    }
                ) {
                    UnifyText(
                        text = "重新识别",
                        variant = UnifyTextVariant.BUTTON
                    )
                }
            }
            
            // 扫描动画
            if (isScanning) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val infiniteTransition = rememberInfiniteTransition(label = "biometric")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            theme.colors.primary.copy(alpha = alpha),
                            androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (biometricType) {
                            UnifySensorType.FINGERPRINT -> Icons.Default.Fingerprint
                            UnifySensorType.FACE_ID -> Icons.Default.Face
                            UnifySensorType.VOICE_ID -> Icons.Default.RecordVoiceOver
                            else -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * 运动传感器组件
 */
@Composable
fun UnifyMotionSensor(
    modifier: Modifier = Modifier,
    onMotionDetected: ((String) -> Unit)? = null,
    onStepCount: ((Int) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var stepCount by remember { mutableStateOf(0) }
    var motionType by remember { mutableStateOf("静止") }
    var isActive by remember { mutableStateOf(false) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                delay(1000)
                
                // 模拟运动检测
                val motions = listOf("静止", "走路", "跑步", "骑行", "开车")
                val newMotion = motions.random()
                motionType = newMotion
                onMotionDetected?.invoke(newMotion)
                
                // 模拟计步
                if (newMotion in listOf("走路", "跑步")) {
                    stepCount += (1..5).random()
                    onStepCount?.invoke(stepCount)
                }
            }
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
                    text = "运动传感器",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 运动状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    UnifyText(
                        text = "当前状态",
                        variant = UnifyTextVariant.BODY_SMALL,
                        color = theme.colors.onSurfaceVariant
                    )
                    UnifyText(
                        text = motionType,
                        variant = UnifyTextVariant.H5,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    UnifyText(
                        text = "步数",
                        variant = UnifyTextVariant.BODY_SMALL,
                        color = theme.colors.onSurfaceVariant
                    )
                    UnifyText(
                        text = stepCount.toString(),
                        variant = UnifyTextVariant.H5,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 运动图标指示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val motionIcons = mapOf(
                    "静止" to Icons.Default.Person,
                    "走路" to Icons.Default.DirectionsWalk,
                    "跑步" to Icons.Default.DirectionsRun,
                    "骑行" to Icons.Default.DirectionsBike,
                    "开车" to Icons.Default.DirectionsCar
                )
                
                motionIcons.forEach { (motion, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = motion,
                            tint = if (motionType == motion) theme.colors.primary else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        UnifyText(
                            text = motion,
                            variant = UnifyTextVariant.CAPTION,
                            color = if (motionType == motion) theme.colors.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// 辅助函数
private fun generateMockSensorData(sensorType: UnifySensorType): UnifySensorData {
    return when (sensorType) {
        UnifySensorType.ACCELEROMETER -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf(
                (-10f..10f).random(),
                (-10f..10f).random(),
                (-10f..10f).random()
            ),
            accuracy = (1..3).random()
        )
        UnifySensorType.GYROSCOPE -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf(
                (-5f..5f).random(),
                (-5f..5f).random(),
                (-5f..5f).random()
            ),
            accuracy = (1..3).random()
        )
        UnifySensorType.LIGHT -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf((0f..1000f).random()),
            accuracy = (1..3).random()
        )
        UnifySensorType.PRESSURE -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf((950f..1050f).random()),
            accuracy = (1..3).random()
        )
        UnifySensorType.TEMPERATURE -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf((15f..35f).random()),
            accuracy = (1..3).random()
        )
        UnifySensorType.HEART_RATE -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf((60f..120f).random()),
            accuracy = (1..3).random()
        )
        else -> UnifySensorData(
            type = sensorType,
            values = floatArrayOf(0f),
            accuracy = 1
        )
    }
}

private fun getSensorIcon(sensorType: UnifySensorType): ImageVector {
    return when (sensorType) {
        UnifySensorType.ACCELEROMETER -> Icons.Default.Speed
        UnifySensorType.GYROSCOPE -> Icons.Default.RotateRight
        UnifySensorType.MAGNETOMETER -> Icons.Default.Explore
        UnifySensorType.PROXIMITY -> Icons.Default.Sensors
        UnifySensorType.LIGHT -> Icons.Default.LightMode
        UnifySensorType.PRESSURE -> Icons.Default.Compress
        UnifySensorType.TEMPERATURE -> Icons.Default.Thermostat
        UnifySensorType.HUMIDITY -> Icons.Default.WaterDrop
        UnifySensorType.STEP_COUNTER -> Icons.Default.DirectionsWalk
        UnifySensorType.HEART_RATE -> Icons.Default.Favorite
        UnifySensorType.FINGERPRINT -> Icons.Default.Fingerprint
        UnifySensorType.FACE_ID -> Icons.Default.Face
        UnifySensorType.VOICE_ID -> Icons.Default.RecordVoiceOver
        UnifySensorType.GESTURE -> Icons.Default.PanTool
        UnifySensorType.MOTION -> Icons.Default.DirectionsRun
        UnifySensorType.ORIENTATION -> Icons.Default.ScreenRotation
        UnifySensorType.GPS -> Icons.Default.LocationOn
        UnifySensorType.NFC -> Icons.Default.Nfc
        UnifySensorType.BLUETOOTH -> Icons.Default.Bluetooth
        UnifySensorType.WIFI -> Icons.Default.Wifi
    }
}

private fun getSensorDisplayName(sensorType: UnifySensorType): String {
    return when (sensorType) {
        UnifySensorType.ACCELEROMETER -> "加速度传感器"
        UnifySensorType.GYROSCOPE -> "陀螺仪"
        UnifySensorType.MAGNETOMETER -> "磁力计"
        UnifySensorType.PROXIMITY -> "距离传感器"
        UnifySensorType.LIGHT -> "光线传感器"
        UnifySensorType.PRESSURE -> "气压传感器"
        UnifySensorType.TEMPERATURE -> "温度传感器"
        UnifySensorType.HUMIDITY -> "湿度传感器"
        UnifySensorType.STEP_COUNTER -> "计步器"
        UnifySensorType.HEART_RATE -> "心率传感器"
        UnifySensorType.FINGERPRINT -> "指纹识别"
        UnifySensorType.FACE_ID -> "面部识别"
        UnifySensorType.VOICE_ID -> "声纹识别"
        UnifySensorType.GESTURE -> "手势识别"
        UnifySensorType.MOTION -> "运动检测"
        UnifySensorType.ORIENTATION -> "方向传感器"
        UnifySensorType.GPS -> "GPS定位"
        UnifySensorType.NFC -> "NFC"
        UnifySensorType.BLUETOOTH -> "蓝牙"
        UnifySensorType.WIFI -> "WiFi"
    }
}

private fun getAxisName(sensorType: UnifySensorType, index: Int): String {
    return when (sensorType) {
        UnifySensorType.ACCELEROMETER, UnifySensorType.GYROSCOPE, UnifySensorType.MAGNETOMETER -> {
            when (index) {
                0 -> "X轴"
                1 -> "Y轴"
                2 -> "Z轴"
                else -> "轴$index"
            }
        }
        UnifySensorType.LIGHT -> "亮度(lux)"
        UnifySensorType.PRESSURE -> "气压(hPa)"
        UnifySensorType.TEMPERATURE -> "温度(°C)"
        UnifySensorType.HUMIDITY -> "湿度(%)"
        UnifySensorType.HEART_RATE -> "心率(bpm)"
        else -> "值$index"
    }
}

private fun getAccuracyText(accuracy: Int): String {
    return when (accuracy) {
        0 -> "不可靠"
        1 -> "低精度"
        2 -> "中精度"
        3 -> "高精度"
        else -> "未知"
    }
}

private fun getAccuracyColor(accuracy: Int): Color {
    return when (accuracy) {
        0 -> Color.Red
        1 -> Color.Orange
        2 -> Color.Yellow
        3 -> Color.Green
        else -> Color.Gray
    }
}
