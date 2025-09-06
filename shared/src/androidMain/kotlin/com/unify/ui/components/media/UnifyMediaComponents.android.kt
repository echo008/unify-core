@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.media

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Video Player: ${mediaItem.title}")
            Row {
                Button(onClick = { onPlaybackStateChange(PlaybackState.PLAYING) }) {
                    Text("Play")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onPlaybackStateChange(PlaybackState.PAUSED) }) {
                    Text("Pause")
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
    onError: (String) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Audio Player: ${mediaItem.title}")
            Row {
                Button(onClick = { onPlaybackStateChange(PlaybackState.PLAYING) }) {
                    Text("Play")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onPlaybackStateChange(PlaybackState.PAUSED) }) {
                    Text("Pause")
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
    onImageClick: (Int) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Image Viewer: ${images.size} images")
            if (images.isNotEmpty()) {
                Text("Current: ${images[currentIndex]}")
                Row {
                    Button(onClick = { if (currentIndex > 0) onIndexChange(currentIndex - 1) }) {
                        Text("Previous")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (currentIndex < images.size - 1) onIndexChange(currentIndex + 1) }) {
                        Text("Next")
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
    onSelectionChange: (Set<String>) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Media Gallery (${mediaItems.size} items)")
            mediaItems.take(3).forEach { item ->
                Button(
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${item.type}: ${item.title}")
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
    onStreamStateChange: (PlaybackState) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Live Stream: $streamUrl")
            Button(onClick = { onStreamStateChange(PlaybackState.PLAYING) }) {
                Text("Start Stream")
            }
            if (enableChat) {
                Button(onClick = { onChatMessage("Hello!") }) {
                    Text("Send Chat")
                }
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
    enablePause: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Media Recorder: $mediaType")
            Text("Quality: $quality, Max: ${maxDuration}ms")
            Row {
                Button(onClick = { onRecordingComplete("recorded_file.mp4") }) {
                    Text("Start Recording")
                }
                if (enablePause) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { }) {
                        Text("Pause")
                    }
                }
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
    onSpeedChange: (Float) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Media Controls - State: $playbackState")
            Row {
                Button(onClick = if (playbackState == PlaybackState.PLAYING) onPause else onPlay) {
                    Text(if (playbackState == PlaybackState.PLAYING) "Pause" else "Play")
                }
                if (showFullscreen) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onFullscreen) {
                        Text("Fullscreen")
                    }
                }
            }
            Text("${progress.currentPosition}ms / ${progress.duration}ms")
            Slider(
                value = if (progress.duration > 0) progress.currentPosition.toFloat() / progress.duration else 0f,
                onValueChange = { onSeek((it * progress.duration).toLong()) }
            )
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
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.size(size)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Thumbnail")
            Text(mediaItem.title, maxLines = 1)
            if (showDuration && mediaItem.duration > 0) {
                Text("${mediaItem.duration}s")
            }
            if (showPlayIcon) {
                Button(onClick = onClick) {
                    Text("▶")
                }
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
    showPreview: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Media Uploader")
            Text("Max size: ${maxFileSize / 1024 / 1024}MB, Max files: $maxFiles")
            Text("Allowed: ${allowedTypes.joinToString()}")
            Button(onClick = { 
                val sampleMedia = MediaItem(
                    id = "1",
                    title = "Sample Media",
                    url = "sample.mp4",
                    type = MediaType.VIDEO
                )
                onMediaSelected(listOf(sampleMedia))
            }) {
                Text("Select Files")
            }
        }
    }
}
