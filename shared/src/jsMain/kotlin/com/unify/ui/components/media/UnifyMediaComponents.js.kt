package com.unify.ui.components.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    Box(modifier = modifier) {
        Text("Image Viewer - JS Implementation")
        Button(
            onClick = { onImageClick(currentIndex) }
        ) {
            Text("View Image ${currentIndex + 1}/${images.size}")
        }
    }
}

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
    Box(modifier = modifier) {
        Text("Video Player - JS Implementation")
        Button(
            onClick = { onPlaybackStateChange(PlaybackState.PLAYING) }
        ) {
            Text("Play ${mediaItem.title}")
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
    onError: (String) -> Unit
) {
    Box(modifier = modifier) {
        Text("Audio Player - JS Implementation")
        Button(
            onClick = { onPlaybackStateChange(PlaybackState.PLAYING) }
        ) {
            Text("Play ${mediaItem.title}")
        }
    }
}

@Composable
actual fun UnifyMediaThumbnail(
    mediaItem: MediaItem,
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    showDuration: Boolean,
    showPlayIcon: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = modifier.size(size)) {
        Text("${mediaItem.title} Thumbnail")
        Button(onClick = onClick) {
            Text("View")
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
    showPreview: Boolean
) {
    Box(modifier = modifier) {
        Button(
            onClick = { 
                onMediaSelected(listOf(
                    MediaItem(
                        id = "mock1",
                        title = "Mock Media",
                        url = "mock_media.jpg",
                        type = MediaType.IMAGE
                    )
                ))
            }
        ) {
            Text("Upload Media")
        }
    }
}

@Composable
actual fun UnifyMediaGallery(
    mediaItems: List<MediaItem>,
    onItemSelected: (MediaItem) -> Unit,
    modifier: Modifier,
    columns: Int,
    spacing: androidx.compose.ui.unit.Dp,
    showPlayIcon: Boolean,
    enableSelection: Boolean,
    selectedItems: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(mediaItems) { item ->
            Button(
                onClick = { onItemSelected(item) }
            ) {
                Text(item.title)
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
    onStreamStateChange: (PlaybackState) -> Unit
) {
    Box(modifier = modifier) {
        Text("Live Stream - JS Implementation")
        Button(
            onClick = { onStreamStateChange(PlaybackState.PLAYING) }
        ) {
            Text("Start Stream")
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
    enablePause: Boolean
) {
    Box(modifier = modifier) {
        Button(
            onClick = { onRecordingComplete("mock_recording.mp4") }
        ) {
            Text("Record ${mediaType.name}")
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
    onSpeedChange: (Float) -> Unit
) {
    Row(modifier = modifier) {
        Button(onClick = if (playbackState == PlaybackState.PLAYING) onPause else onPlay) {
            Text(if (playbackState == PlaybackState.PLAYING) "Pause" else "Play")
        }
        if (showFullscreen) {
            Button(onClick = onFullscreen) {
                Text("Fullscreen")
            }
        }
    }
}
