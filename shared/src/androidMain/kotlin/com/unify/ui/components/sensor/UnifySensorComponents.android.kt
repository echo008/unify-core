package com.unify.ui.components.sensor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

/**
 * Android传感器组件实现
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
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Sensor Monitor")
            Text("Sampling Rate: $samplingRate")
            Text("Max Data Points: $maxDataPoints")

            LazyColumn {
                items(sensorTypes.toList()) { sensorType ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Type: $sensorType")
                            if (showRealTimeData) {
                                val sensorData =
                                    SensorData(
                                        type = sensorType,
                                        values = floatArrayOf(0.5f, 0.0f, 0.0f),
                                        accuracy = android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                                        timestamp = System.currentTimeMillis(),
                                    )
                                LaunchedEffect(sensorType) {
                                    onSensorData(sensorData)
                                }
                                Text("Value: ${sensorData.values.joinToString(", ")}")
                            }
                        }
                    }
                }
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Accelerometer")
            Text("Max Data Points: $maxDataPoints")

            val sensorData by accelerometerData.collectAsState(
                initial =
                    SensorData(
                        type = SensorType.ACCELEROMETER,
                        values = floatArrayOf(0f, 0f, 0f),
                        accuracy = android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                        timestamp = System.currentTimeMillis(),
                    ),
            )

            if (showValues) {
                Text("Value: ${sensorData.values.joinToString(", ")}")
                Text("Timestamp: ${sensorData.timestamp}")
            }

            if (showGraph) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(graphColor.copy(alpha = 0.1f)),
                ) {
                    Text("Graph Placeholder", color = graphColor)
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Gyroscope")
            Text("Max Data Points: $maxDataPoints")

            val sensorData by gyroscopeData.collectAsState(
                initial =
                    SensorData(
                        type = SensorType.GYROSCOPE,
                        values = floatArrayOf(0f, 0f, 0f),
                        accuracy = android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                        timestamp = System.currentTimeMillis(),
                    ),
            )

            if (showValues) {
                Text("Value: ${sensorData.values.joinToString(", ")}")
                Text("Timestamp: ${sensorData.timestamp}")
            }

            if (showGraph) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(graphColor.copy(alpha = 0.1f)),
                ) {
                    Text("Graph Placeholder", color = graphColor)
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Magnetometer")
            Text("Max Data Points: $maxDataPoints")

            val sensorData by magnetometerData.collectAsState(
                initial =
                    SensorData(
                        type = SensorType.MAGNETOMETER,
                        values = floatArrayOf(0f, 0f, 0f),
                        accuracy = android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
                        timestamp = System.currentTimeMillis(),
                    ),
            )

            if (showValues) {
                Text("Value: ${sensorData.values.joinToString(", ")}")
                Text("Timestamp: ${sensorData.timestamp}")
            }

            if (showCompass) {
                Text("Compass Direction: N")
            }

            if (showGraph) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(graphColor.copy(alpha = 0.1f)),
                ) {
                    Text("Graph Placeholder", color = graphColor)
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
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Motion Detector")
            Text("Sensitivity: $sensitivity")

            val motionEvent =
                MotionEvent(
                    type = MotionType.SHAKE,
                    intensity = 0.8f,
                    direction = floatArrayOf(0f, 0f, 1f),
                    timestamp = System.currentTimeMillis(),
                )

            if (enableShakeDetection) {
                Button(
                    onClick = { onMotionDetected(motionEvent.copy(type = MotionType.SHAKE)) },
                ) {
                    Text("Shake Detection")
                }
            }
            if (enableTiltDetection) {
                Button(
                    onClick = { onMotionDetected(motionEvent.copy(type = MotionType.TILT)) },
                ) {
                    Text("Tilt Detection")
                }
            }
            if (enableRotationDetection) {
                Button(
                    onClick = { onMotionDetected(motionEvent.copy(type = MotionType.ROTATION)) },
                ) {
                    Text("Rotation Detection")
                }
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Environment Sensors")
            Text("Update Interval: ${updateInterval}ms")

            val sensorData =
                mapOf(
                    SensorType.TEMPERATURE to 23.5f,
                    SensorType.HUMIDITY to 65.0f,
                    SensorType.PRESSURE to 1013.25f,
                    SensorType.LIGHT to 350.0f,
                )

            LaunchedEffect(updateInterval) {
                onEnvironmentData(sensorData.filterKeys { it in sensorTypes })
            }

            if (showTemperature && SensorType.TEMPERATURE in sensorTypes) {
                Text("Temperature: ${sensorData[SensorType.TEMPERATURE]}°C")
            }
            if (showHumidity && SensorType.HUMIDITY in sensorTypes) {
                Text("Humidity: ${sensorData[SensorType.HUMIDITY]}%")
            }
            if (showPressure && SensorType.PRESSURE in sensorTypes) {
                Text("Pressure: ${sensorData[SensorType.PRESSURE]} hPa")
            }
            if (showLight && SensorType.LIGHT in sensorTypes) {
                Text("Light: ${sensorData[SensorType.LIGHT]} lux")
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Health Sensors")

            val healthData =
                HealthData(
                    heartRate = 75,
                    stepCount = 8542,
                    calories = 245.5f,
                    distance = 6.2f,
                    timestamp = System.currentTimeMillis(),
                )

            LaunchedEffect(Unit) {
                onHealthData(healthData)
            }

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
                LinearProgressIndicator(
                    progress = healthData.heartRate / 120f,
                    modifier = Modifier.fillMaxWidth(),
                )
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Proximity Sensor")
            Text("Threshold: ${threshold}cm")

            val distance = 3.2f
            val isNear = distance < threshold

            LaunchedEffect(distance) {
                onProximityChange(isNear, distance)
            }

            Text("Distance: ${distance}cm")
            Text("Status: ${if (isNear) "Near" else "Far"}")

            if (showIndicator) {
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .background(
                                color = if (isNear) indicatorColor else Color.Gray,
                                shape = CircleShape,
                            ),
                )
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Light Sensor")
            Text("Threshold: $lightThreshold lux")

            val lightLevel = 250.0f
            LaunchedEffect(lightLevel) {
                onLightChange(lightLevel)
            }

            if (showLux) {
                Text("Light Level: $lightLevel lux")
            }
            if (showBrightness) {
                val brightness = (lightLevel / 1000f * 100).toInt()
                Text("Brightness: $brightness%")
            }
            if (autoAdjustTheme) {
                Text("Auto-adjust theme: ${if (lightLevel < lightThreshold) "Dark" else "Light"}")
            }

            Slider(
                value = lightLevel,
                onValueChange = onLightChange,
                valueRange = 0f..1000f,
            )
        }
    }
}
