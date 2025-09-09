package com.unify.ui.components.wearable

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.types.HealthMetric
import com.unify.core.types.WorkoutType
import com.unify.ui.components.system.NotificationAction

/**
 * 可穿戴设备组件接口定义
 * 支持小屏幕和触摸操作
 */

// 数据类定义
// 使用统一的健康和运动相关类型定义，避免重复声明

data class WatchNotification(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = getCurrentTimeMillis(),
)

data class WorkoutSession(
    val id: String,
    val type: String,
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long = 0L,
    val calories: Int = 0,
    val distance: Float = 0f,
    val heartRate: List<Int> = emptyList(),
)

enum class WorkoutAction {
    START,
    PAUSE,
    STOP,
    RESUME,
}

data class QuickAction(
    val id: String,
    val title: String,
    val icon: String,
    val action: () -> Unit,
)

@Composable
expect fun UnifyWatchFace(
    time: String,
    date: String,
    healthMetrics: List<HealthMetric>,
    modifier: Modifier = Modifier,
)

@Composable
expect fun UnifyHealthMonitor(
    metrics: List<HealthMetric>,
    onMetricSelected: (HealthMetric) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun UnifyWatchNotifications(
    notifications: List<WatchNotification>,
    onNotificationAction: (WatchNotification, NotificationAction) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun UnifyWatchWorkout(
    workoutType: WorkoutType,
    duration: Long,
    metrics: Map<String, String>,
    onWorkoutAction: (WorkoutAction) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun UnifyWatchQuickActions(
    actions: List<QuickAction>,
    onActionSelected: (QuickAction) -> Unit,
    modifier: Modifier = Modifier,
)
