package com.unify.core.ui.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.unify.core.utils.NumberFormatter
import com.unify.core.utils.TimeFormatter
import com.unify.core.utils.PlatformUtils
import com.unify.core.platform.watch.*

/**
 * 统一Watch平台Compose组件库
 * 基于Material Design for Wear OS设计原则
 * 确保原生性能和跨平台一致性
 */

/**
 * Watch应用根容器
 * 提供统一的主题和布局基础
 * 优化：使用remember和derivedStateOf减少重组
 */
@Composable
fun UnifyWatchApp(
    watchManager: UnifyWatchManager,
    content: @Composable () -> Unit
) {
    val batteryState by watchManager.batteryState.collectAsState()
    val lifecycleState by watchManager.lifecycleState.collectAsState()
    
    // 性能优化：使用derivedStateOf避免不必要的重组
    val isAmbientMode by remember {
        derivedStateOf { lifecycleState == WatchLifecycleState.Ambient }
    }
    
    val isLowPowerMode by remember {
        derivedStateOf { batteryState.isLowPowerMode }
    }
    
    WatchTheme(
        isAmbientMode = isAmbientMode,
        isLowPowerMode = isLowPowerMode
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * Watch主题组件
 * 根据环境模式和省电模式调整主题
 */
@Composable
fun WatchTheme(
    isAmbientMode: Boolean = false,
    isLowPowerMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAmbientMode) {
        darkColorScheme(
            primary = Color.White,
            secondary = Color.Gray,
            background = Color.Black,
            surface = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        darkColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1C),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Watch健康数据卡片
 * 显示步数、心率等健康信息
 */
@Composable
fun WatchHealthCard(
    healthData: WatchHealthData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "健康数据",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthMetricItem(
                    label = "步数",
                    value = "${healthData.stepCount}",
                    color = Color(0xFF4CAF50)
                )
                
                HealthMetricItem(
                    label = "心率",
                    value = "${healthData.heartRate}",
                    unit = "BPM",
                    color = Color(0xFFE91E63)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthMetricItem(
                    label = "卡路里",
                    value = NumberFormatter.formatHealthValue(healthData.caloriesBurned, "卡"),
                    color = Color(0xFFFF9800)
                )
                
                HealthMetricItem(
                    label = "活动",
                    value = "${healthData.activeTimeMinutes}",
                    unit = "分钟",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

/**
 * 健康指标项组件
 * 性能优化：使用remember缓存样式和颜色计算
 */
@Composable
private fun HealthMetricItem(
    label: String,
    value: String,
    unit: String = "",
    color: Color
) {
    // 性能优化：使用固定颜色避免@Composable调用问题
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val unitColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            fontSize = 10.sp
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = unitColor,
                fontSize = 8.sp
            )
        }
    }
}

/**
 * Watch电池状态指示器
 * 性能优化：使用remember缓存颜色和尺寸计算
 */
@Composable
fun WatchBatteryIndicator(
    batteryState: WatchBatteryState,
    modifier: Modifier = Modifier
) {
    // 性能优化：缓存电池颜色计算
    val batteryColor = remember(batteryState.level) {
        when {
            batteryState.level > 50 -> Color(0xFF4CAF50)
            batteryState.level > 20 -> Color(0xFFFF9800)
            else -> Color(0xFFE91E63)
        }
    }
    
    // 性能优化：缓存进度宽度计算
    val progressWidth = remember(batteryState.level) {
        (batteryState.level / 100f).coerceIn(0f, 1f)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 电池图标
        Box(
            modifier = Modifier
                .size(20.dp, 10.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressWidth)
                    .background(
                        color = batteryColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = "${batteryState.level}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 10.sp
        )
        
        if (batteryState.isCharging) {
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "⚡",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 8.sp
            )
        }
    }
}

/**
 * Watch圆形进度指示器
 * 适合显示健康目标进度
 * 性能优化：使用remember缓存计算结果
 */
@Composable
fun WatchCircularProgress(
    progress: Float,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    // 性能优化：缓存进度计算
    val clampedProgress = remember(progress) {
        progress.coerceIn(0f, 1f)
    }
    
    // 性能优化：使用固定颜色避免@Composable调用问题
    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 背景圆环
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
        )
        
        // 进度圆环 (简化实现，实际需要使用Canvas绘制)
        Box(
            modifier = Modifier
                .fillMaxSize(clampedProgress)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )
        
        // 中心内容
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 8.sp
            )
        }
    }
}

/**
 * Watch快捷操作按钮
 * 适合主屏幕的快捷功能
 */
@Composable
fun WatchQuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

/**
 * Watch通知卡片
 * 显示接收到的通知
 */
@Composable
fun WatchNotificationCard(
    notification: WatchNotification,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "×",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (notification.actions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    notification.actions.take(2).forEach { action ->
                        TextButton(
                            onClick = { onAction(action.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = action.title,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Watch时间显示组件
 * 支持12/24小时制
 */
@Composable
fun WatchTimeDisplay(
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true,
    showSeconds: Boolean = false
) {
    val currentTime by remember {
        mutableStateOf(PlatformUtils.currentTimeMillis())
    }
    
    LaunchedEffect(Unit) {
        // 这里应该实现时间更新逻辑
        // 简化实现，实际需要使用Timer或协程
    }
    
    val timeFormat = if (is24Hour) {
        if (showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (showSeconds) "hh:mm:ss a" else "hh:mm a"
    }
    
    Text(
        text = TimeFormatter.formatTime(currentTime, timeFormat),
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center
    )
}

/**
 * 格式化时间显示
 */
private fun formatTime(timestamp: Long, format: String): String {
    return TimeFormatter.formatTime(timestamp, format)
}
