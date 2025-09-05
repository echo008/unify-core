package com.unify.ui.components.wearable

import androidx.compose.foundation.layout.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.runtime.Composable
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.Modifier
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

/**
 * 可穿戴设备组件接口定义
 * 支持小屏幕和触摸操作
 */

// 数据类定义
data class HealthMetric(
    val type: String,
    val value: String,
    val unit: String,
    val timestamp: Long = getCurrentTimeMillis()
)

data class WatchNotification(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = getCurrentTimeMillis()
)

enum class NotificationAction {
    DISMISS, REPLY, VIEW
}

enum class WorkoutType {
    RUNNING, WALKING, CYCLING, SWIMMING, YOGA, OTHER
}

enum class WorkoutAction {
    START, PAUSE, STOP, RESUME
}

data class QuickAction(
    val id: String,
    val title: String,
    val icon: String,
    val action: () -> Unit
)

@Composable
expect fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyHealthMonitor(
    metrics: List<HealthMetric>,
    onMetricSelected: (HealthMetric) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyWatchNotifications(
    notifications: List<WatchNotification>,
    onNotificationAction: (WatchNotification, NotificationAction) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyWatchWorkout(
    workoutType: WorkoutType,
    duration: Long,
    metrics: Map<String, String>,
    onWorkoutAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyWatchQuickActions(
    actions: List<QuickAction>,
    onActionSelected: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
)
