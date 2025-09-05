package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * TV平台UI适配器实现
 */
@Composable
actual fun UnifyPlatformButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(width = 200.dp, height = 56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
actual fun UnifyPlatformTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(64.dp),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
actual fun UnifyPlatformSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    enabled: Boolean
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
actual fun UnifyPlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(48.dp),
        valueRange = valueRange,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
actual fun UnifyPlatformProgressBar(
    progress: Float,
    modifier: Modifier,
    color: Color
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier.height(8.dp),
        color = color
    )
}

@Composable
actual fun UnifyPlatformDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        modifier = Modifier.size(width = 400.dp, height = 300.dp)
    )
}

@Composable
actual fun UnifyPlatformCard(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
actual fun UnifyPlatformChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean
) {
    FilterChip(
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        selected = selected,
        modifier = modifier.height(48.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary
        )
    )
}

/**
 * TV平台特有的UI组件和交互方式
 */
object TVUnifyPlatformAdapters {
    
    /**
     * TV遥控器导航面板
     */
    @Composable
    fun TVRemoteNavigationPanel(
        onDirectionClick: (Direction) -> Unit,
        onCenterClick: () -> Unit,
        onBackClick: () -> Unit,
        onHomeClick: () -> Unit,
        onMenuClick: () -> Unit,
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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 方向键
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 上
                    IconButton(
                        onClick = { onDirectionClick(Direction.UP) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowUp,
                            contentDescription = "上",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左
                        IconButton(
                            onClick = { onDirectionClick(Direction.LEFT) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowLeft,
                                contentDescription = "左",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // 确定键
                        Button(
                            onClick = onCenterClick,
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("OK")
                        }
                        
                        // 右
                        IconButton(
                            onClick = { onDirectionClick(Direction.RIGHT) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                                contentDescription = "右",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    // 下
                    IconButton(
                        onClick = { onDirectionClick(Direction.DOWN) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                            contentDescription = "下",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // 功能按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                    
                    IconButton(
                        onClick = onHomeClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Home,
                            contentDescription = "主页"
                        )
                    }
                    
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "菜单"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * TV媒体播放控制面板
     */
    @Composable
    fun TVMediaControlPanel(
        isPlaying: Boolean,
        currentTime: String,
        totalTime: String,
        progress: Float,
        volume: Float,
        onPlayPause: () -> Unit,
        onStop: () -> Unit,
        onPrevious: () -> Unit,
        onNext: () -> Unit,
        onSeek: (Float) -> Unit,
        onVolumeChange: (Float) -> Unit,
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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 进度条
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentTime,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = totalTime,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Slider(
                        value = progress,
                        onValueChange = onSeek,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 播放控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.SkipPrevious,
                            contentDescription = "上一个",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Button(
                        onClick = onPlayPause,
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                androidx.compose.material.icons.Icons.Default.Pause
                            } else {
                                androidx.compose.material.icons.Icons.Default.PlayArrow
                            },
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Stop,
                            contentDescription = "停止",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.SkipNext,
                            contentDescription = "下一个",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // 音量控制
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.VolumeDown,
                        contentDescription = "音量"
                    )
                    
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )
                    
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.VolumeUp,
                        contentDescription = "音量"
                    )
                }
            }
        }
    }
    
    /**
     * TV频道选择器
     */
    @Composable
    fun TVChannelSelector(
        channels: List<TVChannel>,
        currentChannel: TVChannel?,
        onChannelSelected: (TVChannel) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(channels.size) { index ->
                val channel = channels[index]
                val isSelected = currentChannel == channel
                
                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    onClick = { onChannelSelected(channel) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = channel.number.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
    
    /**
     * TV设置面板
     */
    @Composable
    fun TVSettingsPanel(
        settings: List<TVSetting>,
        onSettingChanged: (TVSetting, Any) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(settings.size) { index ->
                val setting = settings[index]
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = setting.icon,
                            contentDescription = setting.title,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = setting.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (setting.description.isNotEmpty()) {
                                Text(
                                    text = setting.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // 设置控件
                        when (setting.type) {
                            SettingType.SWITCH -> {
                                Switch(
                                    checked = setting.value as Boolean,
                                    onCheckedChange = { onSettingChanged(setting, it) }
                                )
                            }
                            SettingType.SLIDER -> {
                                Slider(
                                    value = setting.value as Float,
                                    onValueChange = { onSettingChanged(setting, it) },
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                            SettingType.SELECTION -> {
                                TextButton(
                                    onClick = { /* 打开选择对话框 */ }
                                ) {
                                    Text(setting.value.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * TV信息显示面板
     */
    @Composable
    fun TVInfoPanel(
        title: String,
        description: String,
        details: Map<String, String>,
        onClose: () -> Unit,
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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                details.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 方向枚举
 */
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

/**
 * TV频道
 */
data class TVChannel(
    val number: Int,
    val name: String,
    val category: String
)

/**
 * TV设置
 */
data class TVSetting(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val type: SettingType,
    val value: Any
)

/**
 * 设置类型
 */
enum class SettingType {
    SWITCH, SLIDER, SELECTION
}
