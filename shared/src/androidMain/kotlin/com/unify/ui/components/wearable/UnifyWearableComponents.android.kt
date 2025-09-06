package com.unify.ui.components.wearable

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp

/**
 * Android可穿戴设备组件实现
 */
@Composable
actual fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Watch Face")
            Text(time, style = MaterialTheme.typography.headlineMedium)
            Text(date, style = MaterialTheme.typography.bodyMedium)
            healthMetrics.take(3).forEach { metric ->
                Text("${metric.type}: ${metric.value} ${metric.unit}")
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Health Monitor")
            LazyColumn {
                items(metrics) { metric ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onMetricSelected(metric) }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("${metric.type}: ${metric.value} ${metric.unit}")
                            Text("Timestamp: ${metric.timestamp}")
                        }
                    }
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Watch Notifications")
            LazyColumn {
                items(notifications) { notification ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(notification.title)
                            Text(notification.content)
                            Row {
                                val defaultActions = listOf(NotificationAction.VIEW, NotificationAction.DISMISS)
                                for (action in defaultActions) {
                                    Button(
                                        onClick = { onNotificationAction(notification, action) }
                                    ) {
                                        Text(action.name)
                                    }
                                }
                            }
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
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Watch Workout")
            Text("Type: ${workoutType.name}")
            Text("Duration: ${duration}ms")
            metrics.forEach { (key, value) ->
                Text("$key: $value")
            }
            Row {
                Button(
                    onClick = { 
                        isActive = !isActive
                        val action = if (isActive) WorkoutAction.START else WorkoutAction.STOP
                        onWorkoutAction(action)
                    }
                ) {
                    Text(if (isActive) "Stop" else "Start")
                }
                Button(
                    onClick = { onWorkoutAction(WorkoutAction.PAUSE) }
                ) {
                    Text("Pause")
                }
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Quick Actions")
            LazyColumn {
                items(actions) { action ->
                    Button(
                        onClick = { onActionSelected(action) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(action.title)
                    }
                }
            }
        }
    }
}
