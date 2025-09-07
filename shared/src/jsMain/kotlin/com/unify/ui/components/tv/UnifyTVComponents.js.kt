package com.unify.ui.components.tv

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text("JS TV Remote Control")
        Button(onClick = { onKeyPressed(TVKey.POWER) }) {
            Text("Power")
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
    Column(modifier = modifier) {
        Text("JS TV Media Player")
        Text("URL: $mediaUrl")
        if (showControls) {
            Row {
                Button(onClick = { onPlaybackStateChange(TVPlaybackState.PLAYING) }) { Text("Play") }
                Button(onClick = { onPlaybackStateChange(TVPlaybackState.PAUSED) }) { Text("Pause") }
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
    Column(modifier = modifier) {
        Text("JS TV Grid Menu")
        Text("Items: ${items.size}")
        items.take(4).forEach { item ->
            Button(onClick = { onItemSelected(item) }) {
                Text(item)
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
    Column(modifier = modifier) {
        Text("JS TV Volume Control")
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.fillMaxWidth()
        )
        if (showMute) {
            Button(onClick = {}) { Text("Mute") }
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
    Column(modifier = modifier) {
        Text("JS TV Channel List")
        Text("Current: $currentChannel")
        channels.take(3).forEach { channel ->
            Button(onClick = { onChannelSelected(channel) }) {
                Text(channel.name)
            }
        }
    }
}
