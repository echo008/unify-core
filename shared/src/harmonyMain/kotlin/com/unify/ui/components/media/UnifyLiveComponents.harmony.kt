package com.unify.ui.components.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * HarmonyOS平台实时媒体组件
 */
object HarmonyLiveComponents {
    
    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        onImageCaptured: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isRecording by remember { mutableStateOf(false) }
        
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📱 HarmonyOS相机预览",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onImageCaptured("harmony_photo_${System.currentTimeMillis()}.jpg") }) {
                    Text("拍照")
                }
                Button(
                    onClick = { isRecording = !isRecording },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isRecording) "停止录制" else "开始录制")
                }
            }
        }
    }
    
    @Composable
    fun AudioPlayer(
        audioUrl: String,
        modifier: Modifier = Modifier,
        onPlaybackStateChanged: (Boolean) -> Unit = {}
    ) {
        var isPlaying by remember { mutableStateOf(false) }
        var currentTime by remember { mutableStateOf(0.0) }
        var duration by remember { mutableStateOf(100.0) }
        
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying && currentTime < duration) {
                    delay(100)
                    currentTime += 0.1
                }
                if (currentTime >= duration) {
                    isPlaying = false
                    onPlaybackStateChanged(false)
                }
            }
        }
        
        Card(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                LinearProgressIndicator(
                    progress = { (currentTime / duration).toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(formatTime(currentTime))
                    IconButton(onClick = {
                        isPlaying = !isPlaying
                        onPlaybackStateChanged(isPlaying)
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放"
                        )
                    }
                    Text(formatTime(duration))
                }
            }
        }
    }
    
    @Composable
    fun AudioVisualizer(
        audioLevels: List<Float>,
        modifier: Modifier = Modifier
    ) {
        Canvas(modifier = modifier.fillMaxWidth().height(100.dp)) {
            val barWidth = size.width / audioLevels.size
            audioLevels.forEachIndexed { index, level ->
                val barHeight = size.height * level
                drawRect(
                    color = Color(0xFF007DFF), // HarmonyOS蓝色
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = index * barWidth,
                        y = size.height - barHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = barWidth * 0.8f,
                        height = barHeight
                    )
                )
            }
        }
    }
    
    private fun formatTime(seconds: Double): String {
        val totalSeconds = seconds.toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
