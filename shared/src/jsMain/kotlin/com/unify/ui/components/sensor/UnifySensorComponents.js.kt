package com.unify.ui.components.sensor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.js.Date

@Composable
actual fun UnifySensorMonitor(
    sensorTypes: Set<SensorType>,
    onSensorData: (SensorData) -> Unit,
    modifier: Modifier,
    samplingRate: SensorSamplingRate,
    showRealTimeData: Boolean,
    maxDataPoints: Int
) {
    Column(modifier = modifier) {
        Text("JS Sensor Monitor")
        Text("Sampling rate: $samplingRate")
        Text("Max data points: $maxDataPoints")
        sensorTypes.forEach { sensor ->
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Sensor: $sensor")
                    if (showRealTimeData) {
                        Text("Real-time data: [0.1, 0.2, 0.3]")
                    }
                    Button(onClick = {
                        onSensorData(SensorData(
                            type = sensor,
                            values = floatArrayOf(0.1f, 0.2f, 0.3f),
                            timestamp = Date.now().toLong(),
                            accuracy = 1
                        ))
                    }) {
                        Text("Simulate")
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
    maxDataPoints: Int
) {
    Column(modifier = modifier) {
        Text("JS Accelerometer")
        if (showValues) {
            Text("X: 0.1 m/s²")
            Text("Y: 0.2 m/s²")
            Text("Z: 9.8 m/s²")
        }
        if (showGraph) {
            Text("Graph visualization", color = graphColor)
            Text("Max points: $maxDataPoints")
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
    maxDataPoints: Int
) {
    Column(modifier = modifier) {
        Text("JS Gyroscope")
        if (showValues) {
            Text("X: 0.01 rad/s")
            Text("Y: 0.02 rad/s")
            Text("Z: 0.03 rad/s")
        }
        if (showGraph) {
            Text("Graph visualization", color = graphColor)
            Text("Max points: $maxDataPoints")
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
    maxDataPoints: Int
) {
    Column(modifier = modifier) {
        Text("JS Magnetometer")
        if (showValues) {
            Text("X: 25.0 μT")
            Text("Y: 30.0 μT")
            Text("Z: 45.0 μT")
        }
        if (showCompass) {
            Text("Compass: N 15°")
        }
        if (showGraph) {
            Text("Graph visualization", color = graphColor)
            Text("Max points: $maxDataPoints")
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
    enableRotationDetection: Boolean
) {
    Column(modifier = modifier) {
        Text("JS Motion Detector")
        Text("Sensitivity: $sensitivity")
        if (enableShakeDetection) Text("Shake detection enabled")
        if (enableTiltDetection) Text("Tilt detection enabled")
        if (enableRotationDetection) Text("Rotation detection enabled")
        Button(onClick = {
            onMotionDetected(MotionEvent(
                type = MotionType.SHAKE,
                intensity = 0.8f,
                direction = floatArrayOf(1.0f, 0.0f, 0.0f),
                timestamp = Date.now().toLong()
            ))
        }) {
            Text("Simulate Motion")
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
    updateInterval: Long
) {
    Column(modifier = modifier) {
        Text("JS Environment Sensor")
        if (showTemperature) Text("Temperature: 22.5°C")
        if (showHumidity) Text("Humidity: 65%")
        if (showPressure) Text("Pressure: 1013.25 hPa")
        if (showLight) Text("Light: 300 lux")
        Text("Update: ${updateInterval}ms")
        Button(onClick = {
            val mockData = mapOf(
                SensorType.TEMPERATURE to 22.5f,
                SensorType.HUMIDITY to 65f,
                SensorType.PRESSURE to 1013.25f,
                SensorType.LIGHT to 300f
            ).filterKeys { it in sensorTypes }
            onEnvironmentData(mockData)
        }) {
            Text("Update Data")
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
    showRealTimeChart: Boolean
) {
    Column(modifier = modifier) {
        Text("JS Health Sensor")
        if (enableHeartRate) {
            Text("Heart Rate: 72 BPM")
        }
        if (enableStepCounter) {
            Text("Steps: 8,432")
        }
        if (enableCalories) {
            Text("Calories: 1,850 cal")
        }
        if (showRealTimeChart) {
            Text("Real-time chart enabled")
        }
        Button(onClick = {
            onHealthData(HealthData(
                heartRate = 72,
                stepCount = 8432,
                calories = 1850f,
                distance = 5.2f,
                timestamp = Date.now().toLong()
            ))
        }) {
            Text("Update Health Data")
        }
    }
}

@Composable
actual fun UnifyProximitySensor(
    onProximityChange: (Boolean, Float) -> Unit,
    modifier: Modifier,
    showIndicator: Boolean,
    indicatorColor: Color,
    threshold: Float
) {
    Column(modifier = modifier) {
        Text("JS Proximity Sensor")
        Text("Object detected: No")
        Text("Threshold: ${threshold}cm")
        if (showIndicator) {
            Text("Distance: 5.0 cm", color = indicatorColor)
        }
        Button(onClick = {
            onProximityChange(true, 5.0f)
        }) {
            Text("Simulate Detection")
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
    lightThreshold: Float
) {
    Column(modifier = modifier) {
        Text("JS Light Sensor")
        if (showLux) {
            Text("300 lux")
        }
        if (showBrightness) {
            Text("Brightness: Normal")
        }
        Text("Threshold: ${lightThreshold} lux")
        if (autoAdjustTheme) {
            Text("Auto theme adjustment enabled")
        }
        Button(onClick = {
            onLightChange(300f)
        }) {
            Text("Read Light Level")
        }
    }
}
