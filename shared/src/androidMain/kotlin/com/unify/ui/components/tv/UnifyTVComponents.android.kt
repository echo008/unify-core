package com.unify.ui.components.tv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Android TV组件实现
 */
@Composable
actual fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("TV Remote Control")
            Row {
                Button(onClick = { onKeyPressed(TVKey.UP) }) { Text("↑") }
                Button(onClick = { onKeyPressed(TVKey.DOWN) }) { Text("↓") }
                Button(onClick = { onKeyPressed(TVKey.LEFT) }) { Text("←") }
                Button(onClick = { onKeyPressed(TVKey.RIGHT) }) { Text("→") }
            }
            Row {
                Button(onClick = { onKeyPressed(TVKey.CENTER) }) { Text("OK") }
                Button(onClick = { onKeyPressed(TVKey.BACK) }) { Text("Back") }
                Button(onClick = { onKeyPressed(TVKey.HOME) }) { Text("Home") }
                Button(onClick = { onKeyPressed(TVKey.MENU) }) { Text("Menu") }
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
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Media URL: $mediaUrl")
            if (showControls) {
                Row {
                    Button(
                        onClick = { 
                            isPlaying = !isPlaying
                            val state = if (isPlaying) TVPlaybackState.PLAYING else TVPlaybackState.PAUSED
                            onPlaybackStateChange(state)
                        }
                    ) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                }
            }
            if (autoPlay && !isPlaying) {
                LaunchedEffect(Unit) {
                    isPlaying = true
                    onPlaybackStateChange(TVPlaybackState.PLAYING)
                }
            }
        }
    }
}

@Composable
actual fun UnifyTVGridMenu(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier,
    columns: Int
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("TV Grid Menu")
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns)
            ) {
                items(items) { item ->
                    Button(
                        onClick = { onItemSelected(item) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(item)
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("TV Volume Control")
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f
            )
            if (showMute) {
                Button(
                    onClick = { onVolumeChange(0f) }
                ) {
                    Text("Mute")
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
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("TV Channel List")
            LazyColumn {
                items(channels) { channel: TVChannel ->
                    val isSelected = channel.id == currentChannel
                    Button(
                        onClick = { onChannelSelected(channel) },
                        colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("${channel.number} - ${channel.name}")
                    }
                }
            }
        }
    }
}
