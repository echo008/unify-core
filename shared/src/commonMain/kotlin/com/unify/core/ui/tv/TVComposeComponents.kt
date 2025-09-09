package com.unify.core.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.platform.tv.*
import com.unify.core.utils.TimeFormatter

/**
 * 统一TV平台Compose组件库
 * 基于Android TV和10-foot UI设计原则
 * 确保原生性能和大屏幕体验
 */

/**
 * TV应用根容器
 * 提供统一的主题和焦点管理
 * 性能优化：使用derivedStateOf减少重组
 */
@Composable
fun UnifyTVApp(
    tvManager: UnifyTVManager,
    content: @Composable () -> Unit
) {
    val displayInfo by tvManager.displayInfo.collectAsState()
    val lifecycleState by tvManager.lifecycleState.collectAsState()
    
    // 性能优化：使用derivedStateOf避免不必要的重组
    val isActive by remember {
        derivedStateOf { lifecycleState == TVLifecycleState.Active }
    }
    
    TVTheme(
        displayInfo = displayInfo,
        isActive = isActive
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

// TVTheme已在TVTheme.kt中定义，此处移除重复定义

// TVFocusableCard已在TVFocusComponents.kt中定义，此处移除重复定义

/**
 * TV媒体播放器控制面板
 * 性能优化：使用高性能焦点管理和缓存计算
 */
@Composable
fun TVMediaPlayerControls(
    playbackState: TVPlaybackState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 性能优化：缓存进度计算
    val progress = remember(playbackState.position, playbackState.duration) {
        if (playbackState.duration > 0) {
            playbackState.position.toFloat() / playbackState.duration.toFloat()
        } else 0f
    }
    
    // 性能优化：缓存时间格式化
    val currentTime = remember(playbackState.position) {
        TimeFormatter.formatPlaybackTime(playbackState.position)
    }
    
    val totalTime = remember(playbackState.duration) {
        TimeFormatter.formatPlaybackTime(playbackState.duration)
    }
    
    // 性能优化：缓存FocusRequester
    val focusRequesters = remember {
        List(3) { FocusRequester() }
    }
    
    TVFocusableCard(
        onClick = { /* 媒体播放器点击事件 */ },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // 进度条
            TVProgressBar(
                progress = progress,
                onSeek = { seekProgress ->
                    val position = (seekProgress * playbackState.duration).toLong()
                    onSeek(position)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 时间显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = totalTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TVFocusableButton(
                    onClick = onStop,
                    modifier = Modifier.size(80.dp, 48.dp),
                    focusRequester = focusRequesters[0]
                ) {
                    Text("停止")
                }
                
                TVFocusableButton(
                    onClick = if (playbackState.isPlaying) onPause else onPlay,
                    modifier = Modifier.size(100.dp, 56.dp),
                    focusRequester = focusRequesters[1]
                ) {
                    Text(if (playbackState.isPlaying) "暂停" else "播放")
                }
                
                TVFocusableButton(
                    onClick = { /* TODO: 打开设置 */ },
                    modifier = Modifier.size(80.dp, 48.dp),
                    focusRequester = focusRequesters[2]
                ) {
                    Text("设置")
                }
            }
        }
    }
}

/**
 * TV进度条组件
 */
@Composable
fun TVProgressBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var tempProgress by remember { mutableStateOf(progress) }
    
    Column(modifier = modifier) {
        // 进度条轨道
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // 进度指示器
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isDragging) tempProgress else progress)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

// TVControlButton功能已整合到TVFocusableButton中，此处移除重复定义

/**
 * TV导航面板
 * 性能优化：使用高性能焦点管理和缓存状态
 */
@Composable
fun TVNavigationPanel(
    items: List<TVNavigationItem>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 性能优化：缓存FocusRequester列表
    val focusRequesters = remember(items.size) {
        List(items.size) { FocusRequester() }
    }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isSelected = index == selectedIndex
            val focusRequester = focusRequesters[index]
            
            // 性能优化：使用固定颜色避免@Composable调用问题
            val iconColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
            
            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            
            TVFocusableCard(
                onClick = { onSelectionChanged(index) },
                modifier = Modifier.fillMaxWidth(),
                focusRequester = focusRequester
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.icon != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = iconColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * TV媒体网格
 * 性能优化：使用TVFocusableGrid和高效布局管理
 */
@Composable
fun TVMediaGrid(
    mediaItems: List<TVMediaItem>,
    onItemClick: (TVMediaItem) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    TVFocusableGrid(
        items = mediaItems,
        columns = columns,
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) { mediaItem, focusRequester ->
        TVMediaCard(
            mediaItem = mediaItem,
            onClick = { onItemClick(mediaItem) },
            modifier = Modifier.width(200.dp),
            focusRequester = focusRequester
        )
    }
}

/**
 * TV媒体卡片
 * 性能优化：使用缓存计算和高效渲染
 */
@Composable
fun TVMediaCard(
    mediaItem: TVMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    // 性能优化：缓存媒体类型图标
    val mediaIcon = remember(mediaItem.mediaType) {
        when (mediaItem.mediaType) {
            TVMediaType.Video -> "▶"
            TVMediaType.Audio -> "♪"
            TVMediaType.Image -> "🖼"
            TVMediaType.LiveStream -> "📡"
        }
    }
    
    // 性能优化：缓存时长格式化
    val formattedDuration = remember(mediaItem.duration) {
        if (mediaItem.duration > 0) {
            TimeFormatter.formatPlaybackTime(mediaItem.duration)
        } else null
    }
    
    TVFocusableCard(
        onClick = onClick,
        modifier = modifier,
        focusRequester = focusRequester
    ) {
        Column {
            // 缩略图区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 媒体类型图标
                Text(
                    text = mediaIcon,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 内容信息
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = mediaItem.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (mediaItem.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = mediaItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                formattedDuration?.let { duration ->
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * TV信息面板
 * 性能优化：使用缓存计算和高效布局
 */
@Composable
fun TVInfoPanel(
    displayInfo: TVDisplayInfo,
    modifier: Modifier = Modifier
) {
    // 性能优化：缓存格式化的显示信息
    val displayInfoItems = remember(displayInfo) {
        listOf(
            "分辨率" to "${displayInfo.width} × ${displayInfo.height}",
            "刷新率" to "${displayInfo.refreshRate.toInt()} Hz",
            "密度" to "${(displayInfo.density * 10).toInt() / 10.0}",
            "宽高比" to displayInfo.aspectRatio,
            "4K支持" to if (displayInfo.is4K) "是" else "否",
            "HDR支持" to if (displayInfo.hdrSupported) "是" else "否"
        )
    }
    
    TVFocusableCard(
        onClick = { /* 信息面板点击事件 */ },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "显示信息",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            displayInfoItems.forEach { (label, value) ->
                InfoRow(label, value)
            }
        }
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * TV导航项数据类
 */
data class TVNavigationItem(
    val title: String,
    val icon: String? = null,
    val badge: String? = null
)
