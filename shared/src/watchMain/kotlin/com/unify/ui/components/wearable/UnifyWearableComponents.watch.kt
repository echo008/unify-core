package com.unify.ui.components.wearable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Watch平台特定组件实现
 * 针对小屏幕和触摸操作优化
 */

data class HealthMetric(
    val type: HealthMetricType,
    val value: String,
    val unit: String,
    val trend: HealthTrend = HealthTrend.STABLE
)

enum class HealthMetricType {
    HEART_RATE, STEPS, CALORIES, DISTANCE, SLEEP, BLOOD_OXYGEN
}

enum class HealthTrend {
    UP, DOWN, STABLE
}

data class WatchNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

// NotificationPriority 已在 commonMain 中定义

@Composable
actual fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 时间显示
            Text(
                text = time,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 日期显示
            Text(
                text = date,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 健康指标
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                healthMetrics.take(3).forEach { metric ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = metric.value,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (metric.type) {
                                HealthMetricType.HEART_RATE -> Color.Red
                                HealthMetricType.STEPS -> Color.Green
                                HealthMetricType.CALORIES -> Color.Orange
                                else -> Color.White
                            }
                        )
                        Text(
                            text = metric.unit,
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyHealthMonitor(
    metrics: List<HealthMetric>,
    onMetricSelected: (HealthMetric) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(metrics) { metric ->
            Card(
                onClick = { onMetricSelected(metric) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 指标图标
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (metric.type) {
                                HealthMetricType.HEART_RATE -> "♥"
                                HealthMetricType.STEPS -> "👟"
                                HealthMetricType.CALORIES -> "🔥"
                                HealthMetricType.DISTANCE -> "📏"
                                HealthMetricType.SLEEP -> "😴"
                                HealthMetricType.BLOOD_OXYGEN -> "🫁"
                            },
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${metric.value} ${metric.unit}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = metric.type.name.replace("_", " "),
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // 趋势指示器
                    Text(
                        text = when (metric.trend) {
                            HealthTrend.UP -> "↗"
                            HealthTrend.DOWN -> "↘"
                            HealthTrend.STABLE -> "→"
                        },
                        fontSize = 16.sp,
                        color = when (metric.trend) {
                            HealthTrend.UP -> Color.Green
                            HealthTrend.DOWN -> Color.Red
                            HealthTrend.STABLE -> Color.Gray
                        }
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifyWatchNotifications(
    notifications: List<WatchNotification>,
    onNotificationAction: (WatchNotification, NotificationAction) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(notifications) { notification ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (notification.priority) {
                        NotificationPriority.URGENT -> Color(0xFF8B0000)
                        NotificationPriority.HIGH -> Color(0xFF4A4A4A)
                        else -> Color(0xFF2A2A2A)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = formatTimestamp(notification.timestamp),
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.message,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                onNotificationAction(notification, NotificationAction.DISMISS)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF444444)
                            )
                        ) {
                            Text("忽略", fontSize = 8.sp)
                        }
                        
                        Button(
                            onClick = { 
                                onNotificationAction(notification, NotificationAction.VIEW)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066CC)
                            )
                        ) {
                            Text("查看", fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyWatchWorkout(
    workoutType: WorkoutType,
    duration: Long,
    metrics: Map<String, String>,
    onWorkoutAction: (WorkoutAction) -> Unit,
    modifier: Modifier
) {
    var isActive by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 运动类型
        Text(
            text = workoutType.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 运动时长
        Text(
            text = formatDuration(duration),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 运动指标
        metrics.forEach { (key, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = key,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 控制按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    isActive = !isActive
                    onWorkoutAction(
                        if (isActive) WorkoutAction.START else WorkoutAction.PAUSE
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color.Red else Color.Green
                )
            ) {
                Text(
                    text = if (isActive) "暂停" else "开始",
                    fontSize = 10.sp
                )
            }
            
            Button(
                onClick = { onWorkoutAction(WorkoutAction.STOP) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                )
            ) {
                Text("结束", fontSize = 10.sp)
            }
        }
    }
}

@Composable
actual fun UnifyWatchQuickActions(
    actions: List<QuickAction>,
    onActionSelected: (QuickAction) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(actions) { action ->
            Button(
                onClick = { onActionSelected(action) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066CC)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = action.icon,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = action.title,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

enum class WorkoutType(val displayName: String) {
    RUNNING("跑步"),
    WALKING("步行"),
    CYCLING("骑行"),
    SWIMMING("游泳"),
    YOGA("瑜伽"),
    STRENGTH("力量训练")
}

enum class WorkoutAction {
    START, PAUSE, STOP, RESUME
}

enum class NotificationAction(val title: String) {
    DISMISS("关闭"),
    VIEW("查看"),
    REPLY("回复")
}

data class QuickAction(
    val id: String,
    val title: String,
    val icon: String,
    val action: () -> Unit = {}
)

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        else -> "${minutes / 60}小时前"
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
