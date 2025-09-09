package com.unify.ui.components.sensor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

/**
 * iOS平台传感器组件实现
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
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Sensor Monitor (iOS)")
            Text("Sampling Rate: ${samplingRate.name}")
            Text("Max Data Points: $maxDataPoints")

            sensorTypes.forEach { sensorType ->
                Text("${sensorType.name}: Monitoring...")

                // 模拟传感器数据
                LaunchedEffect(sensorType) {
                    val sensorData =
                        SensorData(
                            type = sensorType,
                            values = floatArrayOf(0.0f, 0.0f, 9.8f),
                            accuracy = 3,
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                        )
                    onSensorData(sensorData)
                }
            }

            if (showRealTimeData) {
                Text("Real-time data enabled")
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
    val currentData by accelerometerData.collectAsState(initial = null)

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Accelerometer (iOS)")
            Text("Max Data Points: $maxDataPoints")

            currentData?.let { data ->
                if (showValues) {
                    Text("X: ${data.values.getOrNull(0) ?: 0.0f} m/s²")
                    Text("Y: ${data.values.getOrNull(1) ?: 0.0f} m/s²")
                    Text("Z: ${data.values.getOrNull(2) ?: 9.8f} m/s²")
                    Text("Accuracy: ${data.accuracy}")
                }
            }

            if (showGraph) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(graphColor.copy(alpha = 0.1f)),
                ) {
                    Text("Graph View", modifier = Modifier.padding(8.dp))
                }
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
    val currentData by gyroscopeData.collectAsState(initial = null)

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Gyroscope (iOS)")
            Text("Max Data Points: $maxDataPoints")

            currentData?.let { data ->
                if (showValues) {
                    Text("X: ${data.values.getOrNull(0) ?: 0.0f} rad/s")
                    Text("Y: ${data.values.getOrNull(1) ?: 0.0f} rad/s")
                    Text("Z: ${data.values.getOrNull(2) ?: 0.0f} rad/s")
                    Text("Accuracy: ${data.accuracy}")
                }
            }

            if (showGraph) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(graphColor.copy(alpha = 0.1f)),
                ) {
                    Text("Graph View", modifier = Modifier.padding(8.dp))
                }
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
    val currentData by magnetometerData.collectAsState(initial = null)

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Magnetometer (iOS)")
            Text("Max Data Points: $maxDataPoints")

            if (showCompass) {
                Text("Compass: 0° N")
            }

            currentData?.let { data ->
                if (showValues) {
                    Text("X: ${data.values.getOrNull(0) ?: 0.0f} μT")
                    Text("Y: ${data.values.getOrNull(1) ?: 0.0f} μT")
                    Text("Z: ${data.values.getOrNull(2) ?: 0.0f} μT")
                    Text("Accuracy: ${data.accuracy}")
                }
            }

            if (showGraph) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(graphColor.copy(alpha = 0.1f)),
                ) {
                    Text("Graph View", modifier = Modifier.padding(8.dp))
                }
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
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Motion Detector (iOS)")
            Text("Sensitivity: $sensitivity")
            Text("Shake Detection: ${if (enableShakeDetection) "On" else "Off"}")
            Text("Tilt Detection: ${if (enableTiltDetection) "On" else "Off"}")
            Text("Rotation Detection: ${if (enableRotationDetection) "On" else "Off"}")

            Button(
                onClick = {
                    val motionEvent =
                        MotionEvent(
                            type = MotionType.SHAKE,
                            intensity = 0.8f,
                            direction = floatArrayOf(1.0f, 0.0f, 0.0f),
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                        )
                    onMotionDetected(motionEvent)
                },
            ) {
                Text("Simulate Motion")
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
    val environmentData =
        remember {
            mapOf(
                SensorType.TEMPERATURE to 22.0f,
                SensorType.HUMIDITY to 45.0f,
                SensorType.PRESSURE to 1013.0f,
                SensorType.LIGHT to 500.0f,
            )
        }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Environment Sensors (iOS)")
            Text("Update Interval: ${updateInterval}ms")

            if (showTemperature && SensorType.TEMPERATURE in sensorTypes) {
                Text("Temperature: ${environmentData[SensorType.TEMPERATURE]}°C")
            }

            if (showHumidity && SensorType.HUMIDITY in sensorTypes) {
                Text("Humidity: ${environmentData[SensorType.HUMIDITY]}%")
            }

            if (showPressure && SensorType.PRESSURE in sensorTypes) {
                Text("Pressure: ${environmentData[SensorType.PRESSURE]} hPa")
            }

            if (showLight && SensorType.LIGHT in sensorTypes) {
                Text("Light: ${environmentData[SensorType.LIGHT]} lux")
            }

            Button(
                onClick = { onEnvironmentData(environmentData) },
            ) {
                Text("Update Data")
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
    val healthData =
        remember {
            HealthData(
                heartRate = 72,
                stepCount = 5432,
                calories = 234.5f,
                distance = 3.2f,
                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            )
        }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Health Sensors (iOS)")

            if (enableHeartRate) {
                Text("Heart Rate: ${healthData.heartRate} bpm")
            }

            if (enableStepCounter) {
                Text("Steps: ${healthData.stepCount}")
            }

            if (enableCalories) {
                Text("Calories: ${healthData.calories}")
            }

            if (showRealTimeChart) {
                Text("Real-time chart enabled")
            }

            Button(
                onClick = { onHealthData(healthData) },
            ) {
                Text("Update Data")
            }
        }
    }
}

@Composable
actual fun UnifyProximitySensor(
    onProximityChange: (Boolean, Float) -> Unit,
    modifier: Modifier,
    showIndicator: Boolean,
    indicatorColor: androidx.compose.ui.graphics.Color,
    threshold: Float,
) {
    var isNear by remember { mutableStateOf(false) }
    val distance = remember { mutableStateOf(10.0f) }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Proximity Sensor")
            Text("Threshold: $threshold cm")
            Text("Distance: ${distance.value} cm")

            if (showIndicator) {
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .background(if (isNear) indicatorColor else androidx.compose.ui.graphics.Color.Gray),
                )
            }

            Button(
                onClick = {
                    isNear = !isNear
                    distance.value = if (isNear) threshold - 1 else threshold + 5
                    onProximityChange(isNear, distance.value)
                },
            ) {
                Text("Toggle Proximity")
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
    var currentLux by remember { mutableStateOf(500f) }
    var brightness by remember { mutableStateOf(0.5f) }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Light Sensor")

            if (showLux) {
                Text("Light: $currentLux lux")
            }

            if (showBrightness) {
                Text("Brightness: ${(brightness * 100).toInt()}%")
            }

            Text("Threshold: $lightThreshold lux")
            Text("Auto Theme: ${if (autoAdjustTheme) "On" else "Off"}")

            Slider(
                value = currentLux,
                onValueChange = {
                    currentLux = it
                    brightness = it / 1000f
                    onLightChange(it)
                },
                valueRange = 0f..1000f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
