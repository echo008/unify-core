package com.unify.ui.components.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Desktop平台TV组件actual实现
 * 模拟TV界面和遥控器功能
 */

@Composable
actual fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier,
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "TV遥控器 (Desktop模拟)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 方向键区域
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RemoteButton(
                    text = TVKey.UP.displayName,
                    onClick = { onKeyPressed(TVKey.UP) },
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RemoteButton(
                        text = TVKey.LEFT.displayName,
                        onClick = { onKeyPressed(TVKey.LEFT) },
                    )

                    RemoteButton(
                        text = TVKey.CENTER.displayName,
                        onClick = { onKeyPressed(TVKey.CENTER) },
                        isCenter = true,
                    )

                    RemoteButton(
                        text = TVKey.RIGHT.displayName,
                        onClick = { onKeyPressed(TVKey.RIGHT) },
                    )
                }

                RemoteButton(
                    text = TVKey.DOWN.displayName,
                    onClick = { onKeyPressed(TVKey.DOWN) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 功能按键区域
            Column {
                val functionKeys =
                    listOf(
                        TVKey.BACK, TVKey.HOME, TVKey.MENU,
                        TVKey.VOLUME_UP, TVKey.MUTE, TVKey.VOLUME_DOWN,
                        TVKey.CHANNEL_UP, TVKey.POWER, TVKey.CHANNEL_DOWN,
                        TVKey.INPUT, TVKey.SETTINGS, TVKey.NUMBER_0,
                    )

                functionKeys.chunked(3).forEach { rowKeys ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        rowKeys.forEach { key ->
                            RemoteButton(
                                text = key.displayName,
                                onClick = { onKeyPressed(key) },
                                isSmall = true,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 数字键区域
            Column {
                val numberKeys =
                    listOf(
                        TVKey.NUMBER_1, TVKey.NUMBER_2, TVKey.NUMBER_3,
                        TVKey.NUMBER_4, TVKey.NUMBER_5, TVKey.NUMBER_6,
                        TVKey.NUMBER_7, TVKey.NUMBER_8, TVKey.NUMBER_9,
                    )

                numberKeys.chunked(3).forEach { rowKeys ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        rowKeys.forEach { key ->
                            RemoteButton(
                                text = key.displayName,
                                onClick = { onKeyPressed(key) },
                                isNumber = true,
                            )
                        }
                    }
                }
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
    autoPlay: Boolean,
) {
    var playbackState by remember { mutableStateOf(TVPlaybackState.STOPPED) }
    var currentTime by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(100) }

    LaunchedEffect(autoPlay) {
        if (autoPlay && playbackState == TVPlaybackState.STOPPED) {
            playbackState = TVPlaybackState.PLAYING
            onPlaybackStateChange(playbackState)
        }
    }

    LaunchedEffect(playbackState) {
        if (playbackState == TVPlaybackState.PLAYING) {
            while (playbackState == TVPlaybackState.PLAYING && currentTime < duration) {
                delay(1000)
                currentTime++
            }
            if (currentTime >= duration) {
                playbackState = TVPlaybackState.STOPPED
                currentTime = 0
                onPlaybackStateChange(playbackState)
            }
        }
    }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "TV媒体播放器 (Desktop模拟)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 模拟视频区域
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        when (playbackState) {
                            TVPlaybackState.PLAYING -> "▶ 播放中..."
                            TVPlaybackState.PAUSED -> "⏸ 已暂停"
                            TVPlaybackState.STOPPED -> "⏹ 已停止"
                            TVPlaybackState.BUFFERING -> "⏳ 缓冲中..."
                            TVPlaybackState.ERROR -> "❌ 播放错误"
                        },
                    color = Color.White,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            Column {
                LinearProgressIndicator(
                    progress = { if (duration > 0) currentTime.toFloat() / duration else 0f },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${formatTime(currentTime)}")
                    Text("${formatTime(duration)}")
                }
            }

            if (showControls) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            playbackState =
                                when (playbackState) {
                                    TVPlaybackState.PLAYING -> TVPlaybackState.PAUSED
                                    TVPlaybackState.PAUSED -> TVPlaybackState.PLAYING
                                    TVPlaybackState.STOPPED -> TVPlaybackState.PLAYING
                                    else -> TVPlaybackState.PLAYING
                                }
                            onPlaybackStateChange(playbackState)
                        },
                    ) {
                        Text(
                            when (playbackState) {
                                TVPlaybackState.PLAYING -> "暂停"
                                else -> "播放"
                            },
                        )
                    }

                    Button(
                        onClick = {
                            playbackState = TVPlaybackState.STOPPED
                            currentTime = 0
                            onPlaybackStateChange(playbackState)
                        },
                    ) {
                        Text("停止")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "媒体源: $mediaUrl",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
actual fun UnifyTVGridMenu(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier,
    columns: Int,
) {
    var selectedItem by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "TV网格菜单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items.chunked(columns)) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowItems.forEach { item ->
                            Card(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable {
                                            selectedItem = item
                                            onItemSelected(item)
                                        },
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (selectedItem == item) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                    ),
                                elevation =
                                    CardDefaults.cardElevation(
                                        defaultElevation = if (selectedItem == item) 8.dp else 2.dp,
                                    ),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.titleMedium,
                                        color =
                                            if (selectedItem == item) {
                                                MaterialTheme.colorScheme.onPrimary
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

            selectedItem?.let { item ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "已选择: $item",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
actual fun UnifyTVVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier,
    showMute: Boolean,
) {
    var currentVolume by remember { mutableStateOf(volume) }
    var isMuted by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "TV音量控制",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 音量显示
            Text(
                text = if (isMuted) "静音" else "${(currentVolume * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) Color.Red else MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 音量条
            Slider(
                value = if (isMuted) 0f else currentVolume,
                onValueChange = {
                    if (!isMuted) {
                        currentVolume = it
                        onVolumeChange(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isMuted,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 控制按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = {
                        if (currentVolume > 0.1f) {
                            currentVolume = (currentVolume - 0.1f).coerceAtLeast(0f)
                            if (!isMuted) onVolumeChange(currentVolume)
                        }
                    },
                ) {
                    Text("音量-")
                }

                if (showMute) {
                    Button(
                        onClick = {
                            isMuted = !isMuted
                            onVolumeChange(if (isMuted) 0f else currentVolume)
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = if (isMuted) Color.Red else MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Text(if (isMuted) "取消静音" else "静音")
                    }
                }

                Button(
                    onClick = {
                        if (currentVolume < 0.9f) {
                            currentVolume = (currentVolume + 0.1f).coerceAtMost(1f)
                            if (!isMuted) onVolumeChange(currentVolume)
                        }
                    },
                ) {
                    Text("音量+")
                }
            }
        }
    }
}

@Composable
actual fun UnifyTVChannelList(
    channels: List<TVChannel>,
    currentChannel: String,
    onChannelSelected: (TVChannel) -> Unit,
    modifier: Modifier,
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "TV频道列表",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (channels.isEmpty()) {
                Text(
                    text = "暂无频道",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(channels) { channel ->
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onChannelSelected(channel) },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (channel.id == currentChannel) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                ),
                            elevation =
                                CardDefaults.cardElevation(
                                    defaultElevation = if (channel.id == currentChannel) 4.dp else 1.dp,
                                ),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${channel.number}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(60.dp),
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                    )

                                    channel.currentProgram?.let { program ->
                                        Text(
                                            text = "正在播放: $program",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                if (channel.id == currentChannel) {
                                    Text(
                                        text = "●",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 20.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 辅助组件和函数

@Composable
private fun RemoteButton(
    text: String,
    onClick: () -> Unit,
    isCenter: Boolean = false,
    isSmall: Boolean = false,
    isNumber: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier.size(
                when {
                    isCenter -> 60.dp
                    isSmall -> 50.dp
                    isNumber -> 45.dp
                    else -> 55.dp
                },
            ),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    when {
                        isCenter -> MaterialTheme.colorScheme.primary
                        isNumber -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    },
            ),
    ) {
        Text(
            text = text,
            fontSize =
                when {
                    isCenter -> 16.sp
                    isSmall -> 10.sp
                    isNumber -> 14.sp
                    else -> 12.sp
                },
            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
