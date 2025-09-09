package com.unify.ui.components.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop平台媒体组件actual实现
 */

@Composable
actual fun UnifyVideoPlayer(
    mediaItem: MediaItem,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlaybackStateChange: (PlaybackState) -> Unit,
    onProgressChange: (PlaybackProgress) -> Unit,
    onError: (String) -> Unit,
) {
    var playbackState by remember { mutableStateOf(PlaybackState.IDLE) }

    LaunchedEffect(mediaItem) {
        onPlaybackStateChange(playbackState)
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "视频播放器 - ${mediaItem.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "URL: ${mediaItem.url}",
                style = MaterialTheme.typography.bodySmall,
            )

            if (showControls) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            playbackState = PlaybackState.PLAYING
                            onPlaybackStateChange(playbackState)
                        },
                    ) {
                        Text("播放")
                    }
                    Button(
                        onClick = {
                            playbackState = PlaybackState.PAUSED
                            onPlaybackStateChange(playbackState)
                        },
                    ) {
                        Text("暂停")
                    }
                    Button(
                        onClick = {
                            playbackState = PlaybackState.STOPPED
                            onPlaybackStateChange(playbackState)
                        },
                    ) {
                        Text("停止")
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyAudioPlayer(
    mediaItem: MediaItem,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    showWaveform: Boolean,
    onPlaybackStateChange: (PlaybackState) -> Unit,
    onProgressChange: (PlaybackProgress) -> Unit,
    onError: (String) -> Unit,
) {
    var playbackState by remember { mutableStateOf(PlaybackState.IDLE) }

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "音频播放器 - ${mediaItem.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showWaveform) {
                Text(
                    text = "波形显示 (Desktop模拟)",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (showControls) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { playbackState = PlaybackState.PLAYING }) {
                        Text("播放")
                    }
                    Button(onClick = { playbackState = PlaybackState.PAUSED }) {
                        Text("暂停")
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyImageViewer(
    images: List<String>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier,
    enableZoom: Boolean,
    enableSwipe: Boolean,
    showIndicator: Boolean,
    onImageClick: (Int) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "图片查看器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (images.isNotEmpty()) {
                Text(
                    text = "当前: ${currentIndex + 1}/${images.size}",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = images[currentIndex],
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            if (currentIndex > 0) {
                                onIndexChange(currentIndex - 1)
                            }
                        },
                        enabled = currentIndex > 0,
                    ) {
                        Text("上一张")
                    }
                    Button(
                        onClick = {
                            if (currentIndex < images.size - 1) {
                                onIndexChange(currentIndex + 1)
                            }
                        },
                        enabled = currentIndex < images.size - 1,
                    ) {
                        Text("下一张")
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyMediaGallery(
    mediaItems: List<MediaItem>,
    onItemSelected: (MediaItem) -> Unit,
    modifier: Modifier,
    columns: Int,
    spacing: Dp,
    showPlayIcon: Boolean,
    enableSelection: Boolean,
    selectedItems: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier,
    ) {
        items(mediaItems) { item ->
            Card(
                onClick = { onItemSelected(item) },
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = item.type.name,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifyLiveStream(
    streamUrl: String,
    modifier: Modifier,
    showControls: Boolean,
    enableChat: Boolean,
    onChatMessage: (String) -> Unit,
    onViewerCountChange: (Int) -> Unit,
    onStreamStateChange: (PlaybackState) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "直播流播放器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "流地址: $streamUrl",
                style = MaterialTheme.typography.bodySmall,
            )

            if (enableChat) {
                Text(
                    text = "聊天功能已启用",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
actual fun UnifyMediaRecorder(
    mediaType: MediaType,
    onRecordingComplete: (String) -> Unit,
    modifier: Modifier,
    maxDuration: Long,
    quality: RecordingQuality,
    showTimer: Boolean,
    enablePause: Boolean,
) {
    var isRecording by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${mediaType.name}录制器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (showTimer) {
                Text(
                    text = "最大时长: ${maxDuration}ms",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isRecording = !isRecording
                    if (!isRecording) {
                        onRecordingComplete("/path/to/recorded/file")
                    }
                },
            ) {
                Text(if (isRecording) "停止录制" else "开始录制")
            }
        }
    }
}

@Composable
actual fun UnifyMediaControls(
    playbackState: PlaybackState,
    progress: PlaybackProgress,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier,
    showFullscreen: Boolean,
    onFullscreen: () -> Unit,
    showSpeed: Boolean,
    playbackSpeed: Float,
    onSpeedChange: (Float) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "媒体控制器",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "状态: ${playbackState.name}",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onPlay) { Text("播放") }
                Button(onClick = onPause) { Text("暂停") }
                if (showFullscreen) {
                    Button(onClick = onFullscreen) { Text("全屏") }
                }
            }
        }
    }
}

@Composable
actual fun UnifyMediaThumbnail(
    mediaItem: MediaItem,
    modifier: Modifier,
    size: Dp,
    showDuration: Boolean,
    showPlayIcon: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(size),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = mediaItem.title,
                style = MaterialTheme.typography.labelSmall,
            )
            if (showDuration && mediaItem.duration > 0) {
                Text(
                    text = "${mediaItem.duration}s",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
actual fun UnifyMediaUploader(
    onMediaSelected: (List<MediaItem>) -> Unit,
    modifier: Modifier,
    allowedTypes: Set<MediaType>,
    maxFileSize: Long,
    maxFiles: Int,
    showPreview: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "媒体上传器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "支持类型: ${allowedTypes.joinToString()}",
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = "最大文件: $maxFiles, 大小限制: ${maxFileSize}B",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // 模拟选择媒体文件
                    val mockItems =
                        listOf(
                            MediaItem(
                                id = "1",
                                title = "示例媒体",
                                url = "/path/to/media",
                                type = MediaType.IMAGE,
                            ),
                        )
                    onMediaSelected(mockItems)
                },
            ) {
                Text("选择媒体文件")
            }
        }
    }
}
