package com.unify.ui.components.media

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台媒体组件
 * 支持音频、视频、图片等媒体播放和处理
 */

enum class MediaType {
    AUDIO, VIDEO, IMAGE, LIVE_STREAM
}

enum class PlaybackState {
    IDLE, LOADING, PLAYING, PAUSED, STOPPED, ERROR, BUFFERING
}

data class MediaItem(
    val id: String,
    val title: String,
    val url: String,
    val type: MediaType,
    val duration: Long = 0L,
    val thumbnail: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class PlaybackProgress(
    val currentPosition: Long,
    val duration: Long,
    val bufferedPosition: Long = 0L
)

@Composable
expect fun UnifyVideoPlayer(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true,
    onPlaybackStateChange: (PlaybackState) -> Unit = {},
    onProgressChange: (PlaybackProgress) -> Unit = {},
    onError: (String) -> Unit = {}
)

@Composable
expect fun UnifyAudioPlayer(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true,
    showWaveform: Boolean = false,
    onPlaybackStateChange: (PlaybackState) -> Unit = {},
    onProgressChange: (PlaybackProgress) -> Unit = {},
    onError: (String) -> Unit = {}
)

@Composable
expect fun UnifyImageViewer(
    images: List<String>,
    currentIndex: Int = 0,
    onIndexChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    enableZoom: Boolean = true,
    enableSwipe: Boolean = true,
    showIndicator: Boolean = true,
    onImageClick: (Int) -> Unit = {}
)

@Composable
expect fun UnifyMediaGallery(
    mediaItems: List<MediaItem>,
    onItemSelected: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    spacing: Dp = 4.dp,
    showPlayIcon: Boolean = true,
    enableSelection: Boolean = false,
    selectedItems: Set<String> = emptySet(),
    onSelectionChange: (Set<String>) -> Unit = {}
)

@Composable
expect fun UnifyLiveStream(
    streamUrl: String,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    enableChat: Boolean = false,
    onChatMessage: (String) -> Unit = {},
    onViewerCountChange: (Int) -> Unit = {},
    onStreamStateChange: (PlaybackState) -> Unit = {}
)

@Composable
expect fun UnifyMediaRecorder(
    mediaType: MediaType,
    onRecordingComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxDuration: Long = 60000L,
    quality: RecordingQuality = RecordingQuality.MEDIUM,
    showTimer: Boolean = true,
    enablePause: Boolean = true
)

enum class RecordingQuality {
    LOW, MEDIUM, HIGH, ULTRA
}

@Composable
expect fun UnifyMediaControls(
    playbackState: PlaybackState,
    progress: PlaybackProgress,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showFullscreen: Boolean = true,
    onFullscreen: () -> Unit = {},
    showSpeed: Boolean = true,
    playbackSpeed: Float = 1.0f,
    onSpeedChange: (Float) -> Unit = {}
)

@Composable
expect fun UnifyMediaThumbnail(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showDuration: Boolean = true,
    showPlayIcon: Boolean = true,
    onClick: () -> Unit = {}
)

@Composable
expect fun UnifyMediaUploader(
    onMediaSelected: (List<MediaItem>) -> Unit,
    modifier: Modifier = Modifier,
    allowedTypes: Set<MediaType> = setOf(MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO),
    maxFileSize: Long = 100 * 1024 * 1024L, // 100MB
    maxFiles: Int = 10,
    showPreview: Boolean = true
)
