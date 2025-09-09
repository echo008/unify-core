package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * HarmonyOS平台UI适配器实现
 */
@Composable
actual fun UnifyPlatformButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: String,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
    ) {
        Text(text = text)
    }
}

@Composable
actual fun UnifyPlatformTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        enabled = enabled,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

@Composable
actual fun UnifyPlatformSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

@Composable
actual fun UnifyPlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        enabled = enabled,
        colors =
            SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

@Composable
actual fun UnifyPlatformProgressBar(
    progress: Float,
    modifier: Modifier,
    color: Color,
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
    )
}

@Composable
actual fun UnifyPlatformDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    )
}

@Composable
actual fun UnifyPlatformCard(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        content()
    }
}

@Composable
actual fun UnifyPlatformChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean,
) {
    FilterChip(
        onClick = onClick,
        label = { Text(text) },
        selected = selected,
        modifier = modifier,
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

/**
 * HarmonyOS平台特有的UI组件和交互方式
 */
object HarmonyUnifyPlatformAdapters {
    /**
     * HarmonyOS分布式设备选择器
     */
    @Composable
    fun HarmonyDeviceSelector(
        devices: List<HarmonyDevice>,
        selectedDevice: HarmonyDevice?,
        onDeviceSelected: (HarmonyDevice) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "选择设备",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                devices.forEach { device ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedDevice == device,
                            onClick = { onDeviceSelected(device) },
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = device.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // 连接状态指示器
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (device.isConnected) Color.Green else Color.Gray,
                            modifier = Modifier.size(8.dp),
                        ) {}
                    }
                }
            }
        }
    }

    /**
     * HarmonyOS多屏协同面板
     */
    @Composable
    fun HarmonyMultiScreenPanel(
        screens: List<HarmonyScreen>,
        onScreenSelected: (HarmonyScreen) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(screens.size) { index ->
                val screen = screens[index]
                Card(
                    modifier =
                        Modifier
                            .width(120.dp)
                            .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (screen.isActive) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                        ),
                    onClick = { onScreenSelected(screen) },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.name,
                                tint =
                                    if (screen.isActive) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = screen.name,
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    if (screen.isActive) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * HarmonyOS原子化服务卡片
     */
    @Composable
    fun HarmonyAtomicServiceCard(
        service: AtomicService,
        onServiceClick: (AtomicService) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            onClick = { onServiceClick(service) },
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = service.iconColor,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = service.icon,
                                contentDescription = service.name,
                                tint = Color.White,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }

                // 服务状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "状态: ${service.status}",
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color =
                            when (service.status) {
                                "运行中" -> Color.Green
                                "已停止" -> Color.Gray
                                "错误" -> Color.Red
                                else -> Color.Gray
                            },
                        modifier = Modifier.size(8.dp),
                    ) {}
                }
            }
        }
    }

    /**
     * HarmonyOS分布式任务面板
     */
    @Composable
    fun HarmonyDistributedTaskPanel(
        tasks: List<DistributedTask>,
        onTaskAction: (DistributedTask, String) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(tasks.size) { index ->
                val task = tasks[index]
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Text(
                                text = task.progress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Text(
                            text = "设备: ${task.deviceName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )

                        LinearProgressIndicator(
                            progress = task.progressValue,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = { onTaskAction(task, "pause") },
                            ) {
                                Text("暂停")
                            }

                            TextButton(
                                onClick = { onTaskAction(task, "cancel") },
                            ) {
                                Text("取消")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * HarmonyOS智能推荐卡片
     */
    @Composable
    fun HarmonySmartRecommendationCard(
        recommendations: List<SmartRecommendation>,
        onRecommendationClick: (SmartRecommendation) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "智能推荐",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                recommendations.forEach { recommendation ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = recommendation.icon,
                            contentDescription = recommendation.title,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = recommendation.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = recommendation.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                        }

                        TextButton(
                            onClick = { onRecommendationClick(recommendation) },
                        ) {
                            Text(
                                text = "查看",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * HarmonyOS设备信息
 */
data class HarmonyDevice(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean,
)

/**
 * HarmonyOS屏幕信息
 */
data class HarmonyScreen(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isActive: Boolean,
)

/**
 * 原子化服务
 */
data class AtomicService(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val status: String,
)

/**
 * 分布式任务
 */
data class DistributedTask(
    val id: String,
    val name: String,
    val deviceName: String,
    val progress: String,
    val progressValue: Float,
)

/**
 * 智能推荐
 */
data class SmartRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
