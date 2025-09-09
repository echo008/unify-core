package com.unify.ui.components.sensor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.math.*
import kotlin.random.Random

/**
 * Desktop平台传感器组件实现
 * 由于Desktop平台通常没有物理传感器，提供模拟数据和UI展示
 */

@Composable
actual fun UnifySensorMonitor(
    sensorTypes: Set<SensorType>,
    onSensorData: (SensorData) -> Unit,
    modifier: Modifier,
    samplingRate: SensorSamplingRate,
    showRealTimeData: Boolean,
    maxDataPoints: Int,
) {
    var isMonitoring by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring, sensorTypes) {
        if (isMonitoring) {
            while (isMonitoring) {
                sensorTypes.forEach { sensorType ->
                    val mockData = generateMockSensorData(sensorType)
                    onSensorData(mockData)
                }
                delay(getSamplingDelay(samplingRate))
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "传感器监控器 (Desktop模拟)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("监控的传感器: ${sensorTypes.joinToString { it.name }}")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isMonitoring = !isMonitoring },
            ) {
                Text(if (isMonitoring) "停止监控" else "开始监控")
            }

            if (showRealTimeData && isMonitoring) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "状态: 正在监控...",
                    color = Color.Green,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
actual fun UnifyAccelerometerView(
    accelerometerData: Flow<SensorData>,
    modifier: Modifier,
    showGraph: Boolean,
    showValues: Boolean,
    graphColor: Color,
    maxDataPoints: Int,
) {
    var currentData by remember { mutableStateOf<SensorData?>(null) }
    val dataPoints = remember { mutableStateListOf<SensorData>() }

    LaunchedEffect(accelerometerData) {
        accelerometerData.collect { data ->
            currentData = data
            dataPoints.add(data)
            if (dataPoints.size > maxDataPoints) {
                dataPoints.removeAt(0)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "加速度计 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showValues && currentData != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("X: ${String.format("%.2f", currentData!!.values.getOrNull(0) ?: 0f)} m/s²")
                Text("Y: ${String.format("%.2f", currentData!!.values.getOrNull(1) ?: 0f)} m/s²")
                Text("Z: ${String.format("%.2f", currentData!!.values.getOrNull(2) ?: 0f)} m/s²")
            }

            if (showGraph && dataPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SensorGraph(
                    dataPoints = dataPoints,
                    graphColor = graphColor,
                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
actual fun UnifyGyroscopeView(
    gyroscopeData: Flow<SensorData>,
    modifier: Modifier,
    showGraph: Boolean,
    showValues: Boolean,
    graphColor: Color,
    maxDataPoints: Int,
) {
    var currentData by remember { mutableStateOf<SensorData?>(null) }
    val dataPoints = remember { mutableStateListOf<SensorData>() }

    LaunchedEffect(gyroscopeData) {
        gyroscopeData.collect { data ->
            currentData = data
            dataPoints.add(data)
            if (dataPoints.size > maxDataPoints) {
                dataPoints.removeAt(0)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "陀螺仪 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showValues && currentData != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("X: ${String.format("%.2f", currentData!!.values.getOrNull(0) ?: 0f)} rad/s")
                Text("Y: ${String.format("%.2f", currentData!!.values.getOrNull(1) ?: 0f)} rad/s")
                Text("Z: ${String.format("%.2f", currentData!!.values.getOrNull(2) ?: 0f)} rad/s")
            }

            if (showGraph && dataPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SensorGraph(
                    dataPoints = dataPoints,
                    graphColor = graphColor,
                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
actual fun UnifyMagnetometerView(
    magnetometerData: Flow<SensorData>,
    modifier: Modifier,
    showGraph: Boolean,
    showValues: Boolean,
    showCompass: Boolean,
    graphColor: Color,
    maxDataPoints: Int,
) {
    var currentData by remember { mutableStateOf<SensorData?>(null) }
    val dataPoints = remember { mutableStateListOf<SensorData>() }

    LaunchedEffect(magnetometerData) {
        magnetometerData.collect { data ->
            currentData = data
            dataPoints.add(data)
            if (dataPoints.size > maxDataPoints) {
                dataPoints.removeAt(0)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "磁力计 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showValues && currentData != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("X: ${String.format("%.2f", currentData!!.values.getOrNull(0) ?: 0f)} μT")
                Text("Y: ${String.format("%.2f", currentData!!.values.getOrNull(1) ?: 0f)} μT")
                Text("Z: ${String.format("%.2f", currentData!!.values.getOrNull(2) ?: 0f)} μT")
            }

            if (showCompass && currentData != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CompassView(currentData!!)
            }

            if (showGraph && dataPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SensorGraph(
                    dataPoints = dataPoints,
                    graphColor = graphColor,
                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
actual fun UnifyMotionDetector(
    onMotionDetected: (MotionEvent) -> Unit,
    modifier: Modifier,
    sensitivity: Float,
    enableShakeDetection: Boolean,
    enableTiltDetection: Boolean,
    enableRotationDetection: Boolean,
) {
    var isDetecting by remember { mutableStateOf(false) }
    var lastMotion by remember { mutableStateOf<MotionEvent?>(null) }

    LaunchedEffect(isDetecting) {
        if (isDetecting) {
            while (isDetecting) {
                // 模拟运动检测
                if (Random.nextFloat() < 0.1f) { // 10%概率检测到运动
                    val motionTypes = listOf(MotionType.SHAKE, MotionType.TILT, MotionType.ROTATION)
                    val motionType = motionTypes.random()
                    val motion =
                        MotionEvent(
                            type = motionType,
                            intensity = Random.nextFloat() * sensitivity,
                            direction =
                                floatArrayOf(
                                    Random.nextFloat() * 2 - 1,
                                    Random.nextFloat() * 2 - 1,
                                    Random.nextFloat() * 2 - 1,
                                ),
                            timestamp = System.currentTimeMillis(),
                        )
                    lastMotion = motion
                    onMotionDetected(motion)
                }
                delay(100)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "运动检测器 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("摇晃检测: ${if (enableShakeDetection) "开启" else "关闭"}")
                Text("倾斜检测: ${if (enableTiltDetection) "开启" else "关闭"}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("旋转检测: ${if (enableRotationDetection) "开启" else "关闭"}")
            Text("灵敏度: ${String.format("%.1f", sensitivity)}")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isDetecting = !isDetecting },
            ) {
                Text(if (isDetecting) "停止检测" else "开始检测")
            }

            lastMotion?.let { motion ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "最后检测: ${motion.type.name}",
                    color = Color.Green,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
actual fun UnifyEnvironmentSensor(
    sensorTypes: Set<SensorType>,
    onEnvironmentData: (Map<SensorType, Float>) -> Unit,
    modifier: Modifier,
    showTemperature: Boolean,
    showHumidity: Boolean,
    showPressure: Boolean,
    showLight: Boolean,
    updateInterval: Long,
) {
    var currentData by remember { mutableStateOf<Map<SensorType, Float>>(emptyMap()) }
    var isMonitoring by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring, sensorTypes) {
        if (isMonitoring) {
            while (isMonitoring) {
                val data = mutableMapOf<SensorType, Float>()
                sensorTypes.forEach { sensorType ->
                    when (sensorType) {
                        SensorType.TEMPERATURE -> data[sensorType] = Random.nextFloat() * 40 + 10 // 10-50°C
                        SensorType.HUMIDITY -> data[sensorType] = Random.nextFloat() * 100 // 0-100%
                        SensorType.PRESSURE -> data[sensorType] = Random.nextFloat() * 200 + 900 // 900-1100 hPa
                        SensorType.LIGHT -> data[sensorType] = Random.nextFloat() * 1000 // 0-1000 lux
                        else -> data[sensorType] = Random.nextFloat()
                    }
                }
                currentData = data
                onEnvironmentData(data)
                delay(updateInterval)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "环境传感器 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isMonitoring = !isMonitoring },
            ) {
                Text(if (isMonitoring) "停止监控" else "开始监控")
            }

            if (currentData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                currentData.forEach { (sensorType, value) ->
                    when (sensorType) {
                        SensorType.TEMPERATURE ->
                            if (showTemperature) {
                                Text("温度: ${String.format("%.1f", value)}°C")
                            }
                        SensorType.HUMIDITY ->
                            if (showHumidity) {
                                Text("湿度: ${String.format("%.1f", value)}%")
                            }
                        SensorType.PRESSURE ->
                            if (showPressure) {
                                Text("气压: ${String.format("%.1f", value)} hPa")
                            }
                        SensorType.LIGHT ->
                            if (showLight) {
                                Text("光照: ${String.format("%.1f", value)} lux")
                            }
                        else -> Text("${sensorType.name}: ${String.format("%.2f", value)}")
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyHealthSensor(
    onHealthData: (HealthData) -> Unit,
    modifier: Modifier,
    enableHeartRate: Boolean,
    enableStepCounter: Boolean,
    enableCalories: Boolean,
    showRealTimeChart: Boolean,
) {
    var currentHealthData by remember {
        mutableStateOf(HealthData(timestamp = System.currentTimeMillis()))
    }
    var isMonitoring by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            while (isMonitoring) {
                val healthData =
                    HealthData(
                        heartRate = if (enableHeartRate) Random.nextInt(60, 100) else 0,
                        stepCount = if (enableStepCounter) currentHealthData.stepCount + Random.nextInt(0, 5) else 0,
                        calories = if (enableCalories) currentHealthData.calories + Random.nextFloat() * 0.5f else 0f,
                        distance = currentHealthData.distance + Random.nextFloat() * 0.1f,
                        timestamp = System.currentTimeMillis(),
                    )
                currentHealthData = healthData
                onHealthData(healthData)
                delay(1000)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "健康传感器 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isMonitoring = !isMonitoring },
            ) {
                Text(if (isMonitoring) "停止监控" else "开始监控")
            }

            if (isMonitoring) {
                Spacer(modifier = Modifier.height(16.dp))

                if (enableHeartRate) {
                    Text("心率: ${currentHealthData.heartRate} bpm")
                }
                if (enableStepCounter) {
                    Text("步数: ${currentHealthData.stepCount}")
                }
                if (enableCalories) {
                    Text("卡路里: ${String.format("%.1f", currentHealthData.calories)} kcal")
                }
                Text("距离: ${String.format("%.2f", currentHealthData.distance)} km")
            }
        }
    }
}

@Composable
actual fun UnifyProximitySensor(
    onProximityChange: (Boolean, Float) -> Unit,
    modifier: Modifier,
    showIndicator: Boolean,
    indicatorColor: Color,
    threshold: Float,
) {
    var isNear by remember { mutableStateOf(false) }
    var distance by remember { mutableStateOf(10f) }
    var isMonitoring by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            while (isMonitoring) {
                distance = Random.nextFloat() * 20f
                val wasNear = isNear
                isNear = distance < threshold
                if (wasNear != isNear) {
                    onProximityChange(isNear, distance)
                }
                delay(500)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "接近传感器 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isMonitoring = !isMonitoring },
            ) {
                Text(if (isMonitoring) "停止检测" else "开始检测")
            }

            if (isMonitoring) {
                Spacer(modifier = Modifier.height(16.dp))

                Text("距离: ${String.format("%.1f", distance)} cm")
                Text("状态: ${if (isNear) "接近" else "远离"}")

                if (showIndicator) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNear) indicatorColor else Color.Gray),
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifyLightSensor(
    onLightChange: (Float) -> Unit,
    modifier: Modifier,
    showLux: Boolean,
    showBrightness: Boolean,
    autoAdjustTheme: Boolean,
    lightThreshold: Float,
) {
    var lightLevel by remember { mutableStateOf(100f) }
    var isMonitoring by remember { mutableStateOf(false) }

    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            while (isMonitoring) {
                lightLevel = Random.nextFloat() * 1000f
                onLightChange(lightLevel)
                delay(1000)
            }
        }
    }

    Card(
        modifier = modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "光线传感器 (Desktop模拟)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isMonitoring = !isMonitoring },
            ) {
                Text(if (isMonitoring) "停止检测" else "开始检测")
            }

            if (isMonitoring) {
                Spacer(modifier = Modifier.height(16.dp))

                if (showLux) {
                    Text("光照强度: ${String.format("%.1f", lightLevel)} lux")
                }
                if (showBrightness) {
                    val brightness = (lightLevel / 1000f * 100).coerceIn(0f, 100f)
                    Text("亮度: ${String.format("%.1f", brightness)}%")
                }

                Text("环境: ${if (lightLevel > lightThreshold) "明亮" else "昏暗"}")

                if (autoAdjustTheme) {
                    Text(
                        text = "主题: ${if (lightLevel > lightThreshold) "浅色" else "深色"}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}

// 辅助函数和组件

private fun generateMockSensorData(sensorType: SensorType): SensorData {
    val values =
        when (sensorType) {
            SensorType.ACCELEROMETER ->
                floatArrayOf(
                    Random.nextFloat() * 20 - 10,
                    Random.nextFloat() * 20 - 10,
                    Random.nextFloat() * 20 - 10,
                )
            SensorType.GYROSCOPE ->
                floatArrayOf(
                    Random.nextFloat() * 6 - 3,
                    Random.nextFloat() * 6 - 3,
                    Random.nextFloat() * 6 - 3,
                )
            SensorType.MAGNETOMETER ->
                floatArrayOf(
                    Random.nextFloat() * 100 - 50,
                    Random.nextFloat() * 100 - 50,
                    Random.nextFloat() * 100 - 50,
                )
            else -> floatArrayOf(Random.nextFloat())
        }

    return SensorData(
        type = sensorType,
        values = values,
        accuracy = Random.nextInt(0, 4),
        timestamp = System.currentTimeMillis(),
    )
}

private fun getSamplingDelay(samplingRate: SensorSamplingRate): Long {
    return when (samplingRate) {
        SensorSamplingRate.FASTEST -> 20L
        SensorSamplingRate.GAME -> 50L
        SensorSamplingRate.UI -> 100L
        SensorSamplingRate.NORMAL -> 200L
    }
}

@Composable
private fun SensorGraph(
    dataPoints: List<SensorData>,
    graphColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (dataPoints.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val pointSpacing = width / maxOf(1, dataPoints.size - 1)

        val path = Path()
        dataPoints.forEachIndexed { index, data ->
            val x = index * pointSpacing
            val y =
                height - (
                    data.values.firstOrNull()?.let {
                        ((it + 10) / 20 * height).coerceIn(0f, height)
                    } ?: (height / 2)
                )

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = graphColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun CompassView(magnetometerData: SensorData) {
    val angle =
        remember(magnetometerData) {
            val x = magnetometerData.values.getOrNull(0) ?: 0f
            val y = magnetometerData.values.getOrNull(1) ?: 0f
            atan2(y, x) * 180 / PI
        }

    Canvas(
        modifier = Modifier.size(100.dp),
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f

        // 绘制圆圈
        drawCircle(
            color = Color.Gray,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
        )

        // 绘制指针
        val pointerEnd =
            Offset(
                center.x + cos(angle * PI / 180).toFloat() * radius * 0.7f,
                center.y + sin(angle * PI / 180).toFloat() * radius * 0.7f,
            )

        drawLine(
            color = Color.Red,
            start = center,
            end = pointerEnd,
            strokeWidth = 3.dp.toPx(),
        )

        // 绘制中心点
        drawCircle(
            color = Color.Red,
            radius = 4.dp.toPx(),
            center = center,
        )
    }
}
