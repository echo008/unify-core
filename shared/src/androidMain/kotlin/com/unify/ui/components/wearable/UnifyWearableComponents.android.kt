package com.unify.ui.components.wearable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.core.types.HealthMetric
import com.unify.core.types.WorkoutType
import com.unify.ui.components.system.NotificationAction

/**
 * Android可穿戴设备组件实现
 */
@Composable
actual fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Watch Face")
            Text(time, style = MaterialTheme.typography.headlineMedium)
            Text(date, style = MaterialTheme.typography.bodyMedium)
            healthMetrics.take(3).forEach { metric ->
                Text("Score: ${metric.score} (${metric.status})")
            }
        }
    }
}

@Composable
actual fun UnifyHealthMonitor(
    metrics: List<HealthMetric>,
    onMetricSelected: (HealthMetric) -> Unit,
    modifier: Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Health Monitor")
            LazyColumn {
                items(metrics) { metric ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onMetricSelected(metric) },
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Score: ${metric.score}")
                            Text("Status: ${metric.status}")
                            Text("Details: ${metric.details}")
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
    modifier: Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Watch Notifications")
            LazyColumn {
                items(notifications) { notification ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text(notification.title)
                            Text(notification.content)
                            Row {
                                val defaultActions = listOf(NotificationAction.OPEN, NotificationAction.DISMISS)
                                for (action in defaultActions) {
                                    Button(
                                        onClick = { onNotificationAction(notification, action) },
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
    modifier: Modifier,
) {
    var isActive by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                    },
                ) {
                    Text(if (isActive) "Stop" else "Start")
                }
                Button(
                    onClick = { onWorkoutAction(WorkoutAction.PAUSE) },
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
    modifier: Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Quick Actions")
            LazyColumn {
                items(actions) { action ->
                    Button(
                        onClick = { onActionSelected(action) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    ) {
                        Text(action.title)
                    }
                }
            }
        }
    }
}
