package com.unify.ui.components.tv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TV平台特定组件实现
 * 针对大屏幕和遥控器操作优化
 */

data class TVMenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: (@Composable () -> Unit)? = null,
    val enabled: Boolean = true
)

@Composable
actual fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.padding(16.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TVKey.values()) { key ->
            Button(
                onClick = { onKeyPressed(key) },
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = key.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
actual fun UnifyTVMediaPlayer(
    mediaUrl: String,
    onPlaybackStateChange: (TVPlaybackState) -> Unit,
    modifier: Modifier,
    showControls: Boolean,
    autoPlay: Boolean
) {
    var playbackState by remember { mutableStateOf(TVPlaybackState.STOPPED) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    
    LaunchedEffect(playbackState) {
        onPlaybackStateChange(playbackState)
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 视频播放区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TV媒体播放器\n$mediaUrl",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
        
        if (showControls) {
            // 播放控制栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        playbackState = when (playbackState) {
                            TVPlaybackState.PLAYING -> TVPlaybackState.PAUSED
                            TVPlaybackState.PAUSED, TVPlaybackState.STOPPED -> TVPlaybackState.PLAYING
                            else -> TVPlaybackState.PLAYING
                        }
                    }
                ) {
                    Text(
                        when (playbackState) {
                            TVPlaybackState.PLAYING -> "暂停"
                            else -> "播放"
                        }
                    )
                }
                
                Button(onClick = { playbackState = TVPlaybackState.STOPPED }) {
                    Text("停止")
                }
                
                Button(onClick = { /* 快退 */ }) {
                    Text("快退")
                }
                
                Button(onClick = { /* 快进 */ }) {
                    Text("快进")
                }
            }
            
            // 进度条
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    onValueChange = { progress ->
                        currentPosition = (progress * duration).toLong()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition))
                    Text(formatTime(duration))
                }
            }
        }
    }
}

@Composable
actual fun UnifyTVGridMenu(
    items: List<TVMenuItem>,
    onItemSelected: (TVMenuItem) -> Unit,
    modifier: Modifier,
    columns: Int
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.padding(16.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            val focusRequester = remember { FocusRequester() }
            
            Card(
                onClick = { onItemSelected(item) },
                modifier = Modifier
                    .aspectRatio(1.2f)
                    .focusRequester(focusRequester)
                    .onKeyEvent { keyEvent ->
                        when (keyEvent.key) {
                            Key.DirectionCenter, Key.Enter -> {
                                if (keyEvent.type == KeyEventType.KeyUp) {
                                    onItemSelected(item)
                                }
                                true
                            }
                            else -> false
                        }
                    },
                enabled = item.enabled,
                colors = CardDefaults.cardColors(
                    containerColor = if (item.enabled) Color(0xFF1976D2) else Color.Gray
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item.icon?.invoke()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    item.subtitle?.let { subtitle ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyTVVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier,
    showMute: Boolean
) {
    var isMuted by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showMute) {
            Button(
                onClick = { isMuted = !isMuted },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMuted) Color.Red else Color(0xFF2196F3)
                )
            ) {
                Text(if (isMuted) "取消静音" else "静音")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
        }
        
        Text("音量", modifier = Modifier.padding(end = 8.dp))
        
        Slider(
            value = if (isMuted) 0f else volume,
            onValueChange = { newVolume ->
                isMuted = false
                onVolumeChange(newVolume)
            },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),
                activeTrackColor = Color(0xFF2196F3)
            )
        )
        
        Text(
            text = "${(if (isMuted) 0f else volume * 100).toInt()}%",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
actual fun UnifyTVChannelList(
    channels: List<TVChannel>,
    currentChannel: String,
    onChannelSelected: (TVChannel) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { channel ->
            val isSelected = channel.id == currentChannel
            val focusRequester = remember { FocusRequester() }
            
            Card(
                onClick = { onChannelSelected(channel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFF424242)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = channel.number.toString(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        channel.currentProgram?.let { program ->
                            Text(
                                text = program,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    if (isSelected) {
                        Text(
                            text = "●",
                            color = Color.Green,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
