package com.unify.ui.components.tv

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * iOS平台TV组件实现 (适用于Apple TV)
 */

@Composable
actual fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TV Remote",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Direction pad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onKeyPressed(TVKey.UP) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("↑")
                }
                
                Row {
                    Button(
                        onClick = { onKeyPressed(TVKey.LEFT) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("←")
                    }
                    
                    Button(
                        onClick = { onKeyPressed(TVKey.CENTER) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("OK")
                    }
                    
                    Button(
                        onClick = { onKeyPressed(TVKey.RIGHT) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("→")
                    }
                }
                
                Button(
                    onClick = { onKeyPressed(TVKey.DOWN) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("↓")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onKeyPressed(TVKey.BACK) }
                ) {
                    Text("Back")
                }
                
                Button(
                    onClick = { onKeyPressed(TVKey.HOME) }
                ) {
                    Text("Home")
                }
                
                Button(
                    onClick = { onKeyPressed(TVKey.MENU) }
                ) {
                    Text("Menu")
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
    autoPlay: Boolean
) {
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var currentPosition by remember { mutableStateOf(0L) }
    
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Media: $mediaUrl",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (showControls) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isPlaying = !isPlaying
                            onPlaybackStateChange(
                                if (isPlaying) TVPlaybackState.PLAYING else TVPlaybackState.PAUSED
                            )
                        }
                    ) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                    
                    Button(
                        onClick = {
                            isPlaying = false
                            currentPosition = 0L
                            onPlaybackStateChange(TVPlaybackState.STOPPED)
                        }
                    ) {
                        Text("Stop")
                    }
                    
                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(300000L)}",
                        style = MaterialTheme.typography.bodySmall
                    )
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.padding(16.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onItemSelected(item) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = getIconForMenuItem(item),
                        contentDescription = item,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
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
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Volume: ${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (showMute) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVolumeChange(if (volume > 0) 0f else 0.5f) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (volume > 0) "Mute" else "Unmute")
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
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { channel ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChannelSelected(channel) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = channel.number.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = channel.currentProgram ?: "No program info",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    AssistChip(
                        onClick = { },
                        label = { Text("Ch ${channel.number}") }
                    )
                }
            }
        }
    }
}

private fun getIconForMenuItem(item: String): ImageVector {
    return when (item.lowercase()) {
        "home" -> Icons.Default.Home
        "settings" -> Icons.Default.Settings
        "media" -> Icons.Default.PlayArrow
        "apps" -> Icons.Default.Apps
        "search" -> Icons.Default.Search
        "favorites" -> Icons.Default.Favorite
        "music" -> Icons.Default.MusicNote
        "photos" -> Icons.Default.Photo
        else -> Icons.Default.Tv
    }
}

private fun formatTime(timeMs: Long): String {
    val minutes = timeMs / 60000
    val seconds = (timeMs % 60000) / 1000
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}
