package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Watch平台UI适配器
 * 提供智能手表特有的UI组件和交互方式
 */
object WatchUnifyPlatformAdapters {
    
    /**
     * Watch圆形表盘
     */
    @Composable
    fun WatchFace(
        time: String,
        date: String,
        batteryLevel: Float,
        heartRate: Int?,
        steps: Int?,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.size(200.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 时间显示
                    Text(
                        text = time,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    
                    // 日期显示
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 健康数据
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        heartRate?.let { hr ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                                    contentDescription = "心率",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = hr.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                        
                        steps?.let { stepCount ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.DirectionsWalk,
                                    contentDescription = "步数",
                                    tint = Color.Green,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stepCount.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // 电池指示器
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    LinearProgressIndicator(
                        progress = batteryLevel,
                        modifier = Modifier
                            .width(30.dp)
                            .height(4.dp),
                        color = when {
                            batteryLevel > 0.5f -> Color.Green
                            batteryLevel > 0.2f -> Color.Yellow
                            else -> Color.Red
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Watch健康数据卡片
     */
    @Composable
    fun WatchHealthCard(
        title: String,
        value: String,
        unit: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        iconColor: Color,
        progress: Float? = null,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.size(120.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                progress?.let { prog ->
                    LinearProgressIndicator(
                        progress = prog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = iconColor
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
    
    /**
     * Watch运动控制面板
     */
    @Composable
    fun WatchWorkoutControls(
        isActive: Boolean,
        workoutType: String,
        duration: String,
        calories: Int,
        onStartPause: () -> Unit,
        onStop: () -> Unit,
        onLap: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = workoutType,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = duration,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "${calories} 卡路里",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartPause,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color.Red else Color.Green
                        )
                    ) {
                        Icon(
                            imageVector = if (isActive) {
                                androidx.compose.material.icons.Icons.Default.Pause
                            } else {
                                androidx.compose.material.icons.Icons.Default.PlayArrow
                            },
                            contentDescription = if (isActive) "暂停" else "开始",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onLap,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Flag,
                            contentDescription = "计圈",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onStop,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Stop,
                            contentDescription = "停止",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Watch通知卡片
     */
    @Composable
    fun WatchNotificationCard(
        title: String,
        content: String,
        time: String,
        appIcon: androidx.compose.ui.graphics.vector.ImageVector,
        onDismiss: () -> Unit,
        onAction: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = appIcon,
                            contentDescription = title,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    onAction?.let { action ->
                        TextButton(
                            onClick = action,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "查看",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "关闭",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Watch快捷操作面板
     */
    @Composable
    fun WatchQuickActions(
        actions: List<QuickAction>,
        onActionClick: (QuickAction) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(actions.size) { index ->
                val action = actions[index]
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = action.backgroundColor
                    ),
                    onClick = { onActionClick(action) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = action.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Watch设置滑块
     */
    @Composable
    fun WatchSettingSlider(
        title: String,
        value: Float,
        onValueChange: (Float) -> Unit,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
    
    /**
     * Watch应用启动器
     */
    @Composable
    fun WatchAppLauncher(
        apps: List<WatchApp>,
        onAppClick: (WatchApp) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(apps.size) { index ->
                val app = apps[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Card(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = app.iconColor
                        ),
                        onClick = { onAppClick(app) }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = app.icon,
                                contentDescription = app.name,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 快捷操作
 */
data class QuickAction(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: Color
)

/**
 * Watch应用
 */
data class WatchApp(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color
)
