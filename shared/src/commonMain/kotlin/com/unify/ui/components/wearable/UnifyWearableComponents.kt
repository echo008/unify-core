package com.unify.ui.components.wearable

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import com.unify.ui.components.sensor.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 可穿戴设备类型
 */
enum class UnifyWearableType {
    SMARTWATCH,        // 智能手表
    FITNESS_BAND,      // 健身手环
    SMART_RING,        // 智能戒指
    SMART_GLASSES,     // 智能眼镜
    HEARABLES,         // 智能耳机
    SMART_CLOTHING     // 智能服装
}

/**
 * 健康数据类型
 */
enum class UnifyHealthDataType {
    HEART_RATE,        // 心率
    BLOOD_PRESSURE,    // 血压
    BLOOD_OXYGEN,      // 血氧
    BODY_TEMPERATURE,  // 体温
    SLEEP_QUALITY,     // 睡眠质量
    STRESS_LEVEL,      // 压力水平
    CALORIES_BURNED,   // 消耗卡路里
    DISTANCE_WALKED,   // 行走距离
    FLOORS_CLIMBED,    // 爬楼层数
    ACTIVE_MINUTES     // 活跃分钟数
}

/**
 * 健康数据
 */
data class UnifyHealthData(
    val type: UnifyHealthDataType,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float = 1f,
    val source: String = "Unify Wearable"
)

/**
 * 可穿戴设备配置
 */
data class UnifyWearableConfig(
    val deviceType: UnifyWearableType = UnifyWearableType.SMARTWATCH,
    val enableHeartRate: Boolean = true,
    val enableStepCount: Boolean = true,
    val enableSleepTracking: Boolean = true,
    val enableNotifications: Boolean = true,
    val enableHapticFeedback: Boolean = true,
    val screenTimeout: Long = 10000L,      // 屏幕超时时间(ms)
    val batteryOptimization: Boolean = true,
    val alwaysOnDisplay: Boolean = false,
    val crownRotation: Boolean = true,      // 表冠旋转支持
    val touchSensitivity: Float = 1f       // 触摸灵敏度
)

/**
 * 智能手表表盘组件
 */
@Composable
fun UnifyWatchFace(
    modifier: Modifier = Modifier,
    config: UnifyWearableConfig = UnifyWearableConfig(),
    healthData: List<UnifyHealthData> = emptyList(),
    onTap: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var heartRate by remember { mutableStateOf(72) }
    var stepCount by remember { mutableStateOf(5432) }
    var batteryLevel by remember { mutableStateOf(85) }
    
    // 更新时间
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }
    
    // 模拟健康数据更新
    LaunchedEffect(Unit) {
        while (true) {
            heartRate = (65..85).random()
            stepCount += (0..3).random()
            delay(5000)
        }
    }
    
    Box(
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role = Role.Button
            }
            .clickable {
                onTap?.invoke("watch_face")
            },
        contentAlignment = Alignment.Center
    ) {
        // 表盘背景
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            
            // 绘制表盘圆环
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius * 0.9f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            
            // 绘制时间刻度
            for (i in 0 until 12) {
                val angle = i * 30f - 90f
                val startRadius = radius * 0.85f
                val endRadius = radius * 0.9f
                val startX = center.x + startRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val startY = center.y + startRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                val endX = center.x + endRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val endY = center.y + endRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.dp.toPx()
                )
            }
        }
        
        // 数字时间显示
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(currentTime))
            val date = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
                .format(java.util.Date(currentTime))
            
            UnifyText(
                text = time,
                color = Color.White,
                variant = UnifyTextVariant.H4,
                fontWeight = FontWeight.Bold
            )
            
            UnifyText(
                text = date,
                color = Color.White.copy(alpha = 0.7f),
                variant = UnifyTextVariant.BODY_SMALL
            )
        }
        
        // 健康数据指示器
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 心率
            if (config.enableHeartRate) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "心率",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    UnifyText(
                        text = "$heartRate",
                        color = Color.White,
                        variant = UnifyTextVariant.CAPTION
                    )
                }
            }
            
            // 步数
            if (config.enableStepCount) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "步数",
                        tint = Color.Green,
                        modifier = Modifier.size(16.dp)
                    )
                    UnifyText(
                        text = "$stepCount",
                        color = Color.White,
                        variant = UnifyTextVariant.CAPTION
                    )
                }
            }
        }
        
        // 电池电量指示器
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        batteryLevel > 80 -> Icons.Default.BatteryFull
                        batteryLevel > 60 -> Icons.Default.Battery6Bar
                        batteryLevel > 40 -> Icons.Default.Battery4Bar
                        batteryLevel > 20 -> Icons.Default.Battery2Bar
                        else -> Icons.Default.BatteryAlert
                    },
                    contentDescription = "电池",
                    tint = when {
                        batteryLevel > 20 -> Color.White
                        else -> Color.Red
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                UnifyText(
                    text = "$batteryLevel%",
                    color = Color.White,
                    variant = UnifyTextVariant.CAPTION
                )
            }
        }
    }
}

/**
 * 健康监测组件
 */
@Composable
fun UnifyHealthMonitor(
    modifier: Modifier = Modifier,
    config: UnifyWearableConfig = UnifyWearableConfig(),
    onHealthDataUpdate: ((UnifyHealthData) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isMonitoring by remember { mutableStateOf(false) }
    var healthData by remember { mutableStateOf<Map<UnifyHealthDataType, UnifyHealthData>>(emptyMap()) }
    
    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            while (isMonitoring) {
                // 模拟健康数据更新
                val newData = listOf(
                    UnifyHealthData(UnifyHealthDataType.HEART_RATE, (60..100).random().toFloat(), "bpm"),
                    UnifyHealthData(UnifyHealthDataType.BLOOD_OXYGEN, (95..99).random().toFloat(), "%"),
                    UnifyHealthData(UnifyHealthDataType.STRESS_LEVEL, (1..10).random().toFloat(), "level"),
                    UnifyHealthData(UnifyHealthDataType.CALORIES_BURNED, (200..500).random().toFloat(), "cal")
                )
                
                newData.forEach { data ->
                    healthData = healthData + (data.type to data)
                    onHealthDataUpdate?.invoke(data)
                }
                
                delay(5000) // 每5秒更新一次
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
                    text = "健康监测",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isMonitoring,
                    onCheckedChange = { isMonitoring = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 健康数据网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                healthData.forEach { (type, data) ->
                    item {
                        HealthDataCard(
                            healthData = data,
                            isActive = isMonitoring
                        )
                    }
                }
            }
            
            if (healthData.isEmpty() && !isMonitoring) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(
                            text = "开启监测以查看健康数据",
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 健康数据卡片
 */
@Composable
private fun HealthDataCard(
    healthData: UnifyHealthData,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) theme.colors.primaryContainer else theme.colors.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getHealthDataIcon(healthData.type),
                    contentDescription = null,
                    tint = getHealthDataColor(healthData.type),
                    modifier = Modifier.size(20.dp)
                )
                
                if (isActive) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
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
                            .size(8.dp)
                            .background(
                                Color.Green.copy(alpha = alpha),
                                CircleShape
                            )
                    )
                }
            }
            
            Column {
                UnifyText(
                    text = "${healthData.value.toInt()} ${healthData.unit}",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) theme.colors.onPrimaryContainer else theme.colors.onSurfaceVariant
                )
                UnifyText(
                    text = getHealthDataName(healthData.type),
                    variant = UnifyTextVariant.CAPTION,
                    color = if (isActive) theme.colors.onPrimaryContainer.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}

/**
 * 智能手表通知组件
 */
@Composable
fun UnifyWatchNotification(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList(),
    onDismiss: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isVisible by remember { mutableStateOf(true) }
    
    if (isVisible) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription?.let { this.contentDescription = it }
                },
            colors = CardDefaults.cardColors(
                containerColor = theme.colors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ) {
                        icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = theme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        Column {
                            UnifyText(
                                text = title,
                                variant = UnifyTextVariant.BODY_MEDIUM,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            UnifyText(
                                text = content,
                                variant = UnifyTextVariant.BODY_SMALL,
                                color = theme.colors.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            isVisible = false
                            onDismiss?.invoke()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // 操作按钮
                if (actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        actions.forEach { (text, action) ->
                            TextButton(
                                onClick = {
                                    action()
                                    isVisible = false
                                }
                            ) {
                                UnifyText(
                                    text = text,
                                    variant = UnifyTextVariant.BUTTON,
                                    color = theme.colors.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 触觉反馈组件
 */
@Composable
fun UnifyHapticFeedback(
    intensity: Float = 1f,
    duration: Long = 100L,
    pattern: List<Long> = listOf(0L, 100L),
    modifier: Modifier = Modifier,
    onTrigger: (() -> Unit)? = null
) {
    val theme = LocalUnifyTheme.current
    var isTriggered by remember { mutableStateOf(false) }
    
    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            // 触发触觉反馈
            onTrigger?.invoke()
            delay(duration)
            isTriggered = false
        }
    }
    
    Button(
        onClick = { isTriggered = true },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTriggered) theme.colors.primary else theme.colors.secondary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Vibration,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            UnifyText(
                text = "触觉反馈",
                variant = UnifyTextVariant.BUTTON
            )
        }
    }
}

// 辅助函数
private fun getHealthDataIcon(type: UnifyHealthDataType): ImageVector {
    return when (type) {
        UnifyHealthDataType.HEART_RATE -> Icons.Default.Favorite
        UnifyHealthDataType.BLOOD_PRESSURE -> Icons.Default.MonitorHeart
        UnifyHealthDataType.BLOOD_OXYGEN -> Icons.Default.Air
        UnifyHealthDataType.BODY_TEMPERATURE -> Icons.Default.Thermostat
        UnifyHealthDataType.SLEEP_QUALITY -> Icons.Default.Bedtime
        UnifyHealthDataType.STRESS_LEVEL -> Icons.Default.Psychology
        UnifyHealthDataType.CALORIES_BURNED -> Icons.Default.LocalFireDepartment
        UnifyHealthDataType.DISTANCE_WALKED -> Icons.Default.DirectionsWalk
        UnifyHealthDataType.FLOORS_CLIMBED -> Icons.Default.Stairs
        UnifyHealthDataType.ACTIVE_MINUTES -> Icons.Default.Timer
    }
}

private fun getHealthDataColor(type: UnifyHealthDataType): Color {
    return when (type) {
        UnifyHealthDataType.HEART_RATE -> Color.Red
        UnifyHealthDataType.BLOOD_PRESSURE -> Color.Blue
        UnifyHealthDataType.BLOOD_OXYGEN -> Color.Cyan
        UnifyHealthDataType.BODY_TEMPERATURE -> Color.Orange
        UnifyHealthDataType.SLEEP_QUALITY -> Color.Purple
        UnifyHealthDataType.STRESS_LEVEL -> Color.Yellow
        UnifyHealthDataType.CALORIES_BURNED -> Color.Red
        UnifyHealthDataType.DISTANCE_WALKED -> Color.Green
        UnifyHealthDataType.FLOORS_CLIMBED -> Color.Brown
        UnifyHealthDataType.ACTIVE_MINUTES -> Color.Magenta
    }
}

private fun getHealthDataName(type: UnifyHealthDataType): String {
    return when (type) {
        UnifyHealthDataType.HEART_RATE -> "心率"
        UnifyHealthDataType.BLOOD_PRESSURE -> "血压"
        UnifyHealthDataType.BLOOD_OXYGEN -> "血氧"
        UnifyHealthDataType.BODY_TEMPERATURE -> "体温"
        UnifyHealthDataType.SLEEP_QUALITY -> "睡眠"
        UnifyHealthDataType.STRESS_LEVEL -> "压力"
        UnifyHealthDataType.CALORIES_BURNED -> "卡路里"
        UnifyHealthDataType.DISTANCE_WALKED -> "距离"
        UnifyHealthDataType.FLOORS_CLIMBED -> "楼层"
        UnifyHealthDataType.ACTIVE_MINUTES -> "活跃时间"
    }
}
