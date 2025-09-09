package com.unify.ui.components.wearable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.sp
import com.unify.core.types.HealthMetric
import com.unify.core.types.WorkoutType
import com.unify.ui.components.system.NotificationAction
import platform.Foundation.*
import platform.HealthKit.*
import kotlin.math.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * iOS平台可穿戴设备组件实现 (适用于Apple Watch)
 */

@Composable
actual fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(healthMetrics.take(3)) { metric ->
                    Card(
                        modifier = Modifier.size(60.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = metric.score.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = metric.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// 健康监控组件
@Composable
actual fun UnifyHealthMonitor(
    metrics: List<HealthMetric>,
    onMetricSelected: (HealthMetric) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(metrics) { metric ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    onMetricSelected(metric)
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = metric.details,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Score: ${metric.score}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Status: ${metric.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNotificationAction(notification, NotificationAction.OPEN)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.content,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                    Text(
                        text = "At ${formatTimestamp(notification.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Workout: ${workoutType.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Duration: ${duration / 60} min",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            metrics.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onWorkoutAction(WorkoutAction.START) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }
                Button(
                    onClick = { onWorkoutAction(WorkoutAction.PAUSE) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pause")
                }
                Button(
                    onClick = { onWorkoutAction(WorkoutAction.STOP) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

private fun getIconForAction(iconName: String): ImageVector {
    return when (iconName) {
        "call" -> Icons.Default.Call
        "message" -> Icons.Default.Email
        "music" -> Icons.Default.PlayArrow
        "weather" -> Icons.Default.Info
        "timer" -> Icons.Default.DateRange
        "workout" -> Icons.Default.FavoriteBorder
        else -> Icons.Default.Settings
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return "${timestamp / 1000}s ago"
}

private fun formatDuration(duration: Long): String {
    val minutes = duration / 60000
    val seconds = (duration % 60000) / 1000
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}

// 快捷操作组件
@Composable
actual fun UnifyWatchQuickActions(
    actions: List<QuickAction>,
    onActionSelected: (QuickAction) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(actions) { action ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .clickable { onActionSelected(action) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(action.title)
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 圆形表盘组件
@Composable
fun CircularWatchFace(
    time: String,
    date: String,
    batteryLevel: Float,
    heartRate: Int?,
    steps: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // 绘制表盘刻度
            drawWatchFaceMarks(batteryLevel)
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = time,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = date,
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                heartRate?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart Rate",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$it",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
                
                steps?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Steps",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$it",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        
        // 电池指示器
        LinearProgressIndicator(
            progress = batteryLevel / 100f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .width(60.dp)
                .height(4.dp),
            color = if (batteryLevel > 20) Color.Green else Color.Red
        )
    }
}

private fun DrawScope.drawWatchFaceMarks(
    batteryLevel: Float
) {
    val center = this.center
    val radius = this.size.minDimension / 2f - 20.dp.toPx()
    
    // 绘制12个小时刻度
    for (i in 0..11) {
        val angle = i * 30f - 90f // 从12点开始
        val startRadius = radius - 10.dp.toPx()
        val endRadius = radius
        
        val startX = center.x + cos(angle * PI / 180f).toFloat() * startRadius
        val startY = center.y + sin(angle * PI / 180f).toFloat() * startRadius
        val endX = center.x + cos(angle * PI / 180f).toFloat() * endRadius
        val endY = center.y + sin(angle * PI / 180f).toFloat() * endRadius
        
        this.drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(startX, startY),
            end = androidx.compose.ui.geometry.Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
