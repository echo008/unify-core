package com.unify.ui.components.sensor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

/**
 * Unify跨平台传感器组件
 * 支持加速度计、陀螺仪、磁力计等传感器数据显示和监控
 */

enum class SensorType {
    ACCELEROMETER,
    GYROSCOPE,
    MAGNETOMETER,
    GRAVITY,
    LINEAR_ACCELERATION,
    ROTATION_VECTOR,
    ORIENTATION,
    PROXIMITY,
    LIGHT,
    PRESSURE,
    TEMPERATURE,
    HUMIDITY,
    HEART_RATE,
    STEP_COUNTER,
    STEP_DETECTOR,
}

data class SensorData(
    val type: SensorType,
    val values: FloatArray,
    val accuracy: Int,
    val timestamp: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SensorData
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

@Composable
expect fun UnifySensorMonitor(
    sensorTypes: Set<SensorType>,
    onSensorData: (SensorData) -> Unit,
    modifier: Modifier = Modifier,
    samplingRate: SensorSamplingRate = SensorSamplingRate.NORMAL,
    showRealTimeData: Boolean = true,
    maxDataPoints: Int = 100,
)

enum class SensorSamplingRate {
    FASTEST,
    GAME,
    UI,
    NORMAL,
}

@Composable
expect fun UnifyAccelerometerView(
    accelerometerData: Flow<SensorData>,
    modifier: Modifier = Modifier,
    showGraph: Boolean = true,
    showValues: Boolean = true,
    graphColor: Color = Color.Blue,
    maxDataPoints: Int = 50,
)

@Composable
expect fun UnifyGyroscopeView(
    gyroscopeData: Flow<SensorData>,
    modifier: Modifier = Modifier,
    showGraph: Boolean = true,
    showValues: Boolean = true,
    graphColor: Color = Color.Green,
    maxDataPoints: Int = 50,
)

@Composable
expect fun UnifyMagnetometerView(
    magnetometerData: Flow<SensorData>,
    modifier: Modifier = Modifier,
    showGraph: Boolean = true,
    showValues: Boolean = true,
    showCompass: Boolean = true,
    graphColor: Color = Color.Red,
    maxDataPoints: Int = 50,
)

@Composable
expect fun UnifyMotionDetector(
    onMotionDetected: (MotionEvent) -> Unit,
    modifier: Modifier = Modifier,
    sensitivity: Float = 0.5f,
    enableShakeDetection: Boolean = true,
    enableTiltDetection: Boolean = true,
    enableRotationDetection: Boolean = true,
)

data class MotionEvent(
    val type: MotionType,
    val intensity: Float,
    val direction: FloatArray,
    val timestamp: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as MotionEvent
        if (type != other.type) return false
        if (intensity != other.intensity) return false
        if (!direction.contentEquals(other.direction)) return false
        if (timestamp != other.timestamp) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + intensity.hashCode()
        result = 31 * result + direction.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

enum class MotionType {
    SHAKE,
    TILT,
    ROTATION,
    TAP,
    DOUBLE_TAP,
}

@Composable
expect fun UnifyEnvironmentSensor(
    sensorTypes: Set<SensorType>,
    onEnvironmentData: (Map<SensorType, Float>) -> Unit,
    modifier: Modifier = Modifier,
    showTemperature: Boolean = true,
    showHumidity: Boolean = true,
    showPressure: Boolean = true,
    showLight: Boolean = true,
    updateInterval: Long = 1000L,
)

@Composable
expect fun UnifyHealthSensor(
    onHealthData: (HealthData) -> Unit,
    modifier: Modifier = Modifier,
    enableHeartRate: Boolean = true,
    enableStepCounter: Boolean = true,
    enableCalories: Boolean = true,
    showRealTimeChart: Boolean = true,
)

data class HealthData(
    val heartRate: Int = 0,
    val stepCount: Int = 0,
    val calories: Float = 0f,
    val distance: Float = 0f,
    val timestamp: Long,
)

@Composable
expect fun UnifyProximitySensor(
    onProximityChange: (Boolean, Float) -> Unit,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = true,
    indicatorColor: Color = Color(0xFFFFA500),
    threshold: Float = 5.0f,
)

@Composable
expect fun UnifyLightSensor(
    onLightChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showLux: Boolean = true,
    showBrightness: Boolean = true,
    autoAdjustTheme: Boolean = false,
    lightThreshold: Float = 100f,
)
