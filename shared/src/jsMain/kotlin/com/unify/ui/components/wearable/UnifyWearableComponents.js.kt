package com.unify.ui.components.wearable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text("JS Watch Face")
        Text("Time: $time")
        Text("Date: $date")
        Text("Metrics: ${healthMetrics.size}")
    }
}

@Composable
actual fun UnifyHealthMonitor(
    metrics: List<HealthMetric>,
    onMetricSelected: (HealthMetric) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text("JS Health Monitor")
        metrics.take(3).forEach { metric ->
            Button(onClick = { onMetricSelected(metric) }) {
                Text("${metric.type}: ${metric.value} ${metric.unit}")
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
    Column(modifier = modifier) {
        Text("JS Watch Notifications")
        notifications.take(3).forEach { notification ->
            Card(modifier = Modifier.fillMaxWidth().padding(2.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(notification.title)
                    Text(notification.content)
                    Row {
                        Button(onClick = { onNotificationAction(notification, NotificationAction.VIEW) }) {
                            Text("View")
                        }
                        Button(onClick = { onNotificationAction(notification, NotificationAction.DISMISS) }) {
                            Text("Dismiss")
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
    Column(modifier = modifier) {
        Text("JS Watch Workout")
        Text("Type: ${workoutType.name}")
        Text("Duration: ${duration}s")
        Text("Metrics: ${metrics.size}")
        Row {
            Button(onClick = { onWorkoutAction(WorkoutAction.START) }) {
                Text("Start")
            }
            Button(onClick = { onWorkoutAction(WorkoutAction.PAUSE) }) {
                Text("Pause")
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
    Column(modifier = modifier) {
        Text("JS Watch Quick Actions")
        actions.take(4).forEach { action ->
            Button(onClick = { onActionSelected(action) }) {
                Text(action.title)
            }
        }
    }
}
