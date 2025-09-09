package com.unify.ui.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 根据commonMain expect声明，只保留匹配的actual实现

@Composable
actual fun UnifyVideoPlayer(
    mediaItem: MediaItem,
    modifier: Modifier,
    autoPlay: Boolean,
    showControls: Boolean,
    onPlaybackStateChange: (PlaybackState) -> Unit,
    onProgressChange: (PlaybackProgress) -> Unit,
    onError: (String) -> Unit
) {
    // Native平台视频播放器组件实现
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
    onError: (String) -> Unit
) {
    // Native平台音频播放器组件实现
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
    onImageClick: (Int) -> Unit
) {
    // Native平台图片查看器组件实现
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
    onSelectionChange: (Set<String>) -> Unit
) {
    // Native平台媒体画廊组件实现
}

@Composable
actual fun UnifyLiveStream(
    streamUrl: String,
    modifier: Modifier,
    showControls: Boolean,
    enableChat: Boolean,
    onChatMessage: (String) -> Unit,
    onViewerCountChange: (Int) -> Unit,
    onStreamStateChange: (PlaybackState) -> Unit
) {
    // Native平台直播流组件实现
}

@Composable
actual fun UnifyMediaRecorder(
    mediaType: MediaType,
    onRecordingComplete: (String) -> Unit,
    modifier: Modifier,
    maxDuration: Long,
    quality: RecordingQuality,
    showTimer: Boolean,
    enablePause: Boolean
) {
    // Native平台媒体录制组件实现
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
    onSpeedChange: (Float) -> Unit
) {
    // Native平台媒体控制组件实现
}

@Composable
actual fun UnifyMediaThumbnail(
    mediaItem: MediaItem,
    modifier: Modifier,
    size: Dp,
    showDuration: Boolean,
    showPlayIcon: Boolean,
    onClick: () -> Unit
) {
    // Native平台媒体缩略图组件实现
}

@Composable
actual fun UnifyMediaUploader(
    onMediaSelected: (List<MediaItem>) -> Unit,
    modifier: Modifier,
    allowedTypes: Set<MediaType>,
    maxFileSize: Long,
    maxFiles: Int,
    showPreview: Boolean
) {
    // Native平台媒体上传组件实现
}
