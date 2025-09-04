package com.unify.ui.components.media

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * TV平台实时媒体组件
 * 针对大屏幕和遥控器操作优化
 */
object TVLiveComponents {
    
    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        onImageCaptured: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isRecording by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf(0) }
        
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📺 TV相机预览",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium
                )
                
                // TV遥控器导航提示
                Text(
                    text = "使用遥控器方向键导航",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
                )
            }
            
            // TV优化的大按钮布局
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { 
                        onImageCaptured("tv_photo_${System.currentTimeMillis()}.jpg")
                    },
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .background(
                            if (selectedOption == 0) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                ) {
                    Text("拍照", style = MaterialTheme.typography.titleLarge)
                }
                
                Button(
                    onClick = { 
                        isRecording = !isRecording 
                    },
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .background(
                            if (selectedOption == 1) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isRecording) "停止" else "录制",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                Button(
                    onClick = { /* 设置 */ },
                    modifier = Modifier
                        .size(120.dp, 60.dp)
                        .background(
                            if (selectedOption == 2) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                ) {
                    Text("设置", style = MaterialTheme.typography.titleLarge)
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
        var selectedControl by remember { mutableStateOf(0) }
        
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
        
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp)
            ) {
                // 大屏幕优化的进度条
                LinearProgressIndicator(
                    progress = { (currentTime / duration).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // TV优化的控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentTime),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            onPlaybackStateChanged(isPlaying)
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (selectedControl == 0) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surface,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
    
    @Composable
    fun VideoPlayer(
        videoUrl: String,
        modifier: Modifier = Modifier,
        onPlaybackStateChanged: (Boolean) -> Unit = {}
    ) {
        var isPlaying by remember { mutableStateOf(false) }
        var isFullscreen by remember { mutableStateOf(false) }
        var selectedControl by remember { mutableStateOf(0) }
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            Text(
                text = "📺 TV视频播放器",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // TV优化的控制界面
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        onPlaybackStateChanged(isPlaying)
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (selectedControl == 0) Color.White.copy(alpha = 0.9f) 
                            else Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        modifier = Modifier.size(40.dp),
                        tint = if (selectedControl == 0) Color.Black else Color.White
                    )
                }
                
                IconButton(
                    onClick = { /* 快退 */ },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (selectedControl == 1) Color.White.copy(alpha = 0.9f) 
                            else Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "快退",
                        modifier = Modifier.size(40.dp),
                        tint = if (selectedControl == 1) Color.Black else Color.White
                    )
                }
                
                IconButton(
                    onClick = { /* 快进 */ },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (selectedControl == 2) Color.White.copy(alpha = 0.9f) 
                            else Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "快进",
                        modifier = Modifier.size(40.dp),
                        tint = if (selectedControl == 2) Color.Black else Color.White
                    )
                }
                
                IconButton(
                    onClick = { isFullscreen = !isFullscreen },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (selectedControl == 3) Color.White.copy(alpha = 0.9f) 
                            else Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "退出全屏" else "全屏",
                        modifier = Modifier.size(40.dp),
                        tint = if (selectedControl == 3) Color.Black else Color.White
                    )
                }
            }
        }
    }
    
    @Composable
    fun LiveStreaming(
        streamUrl: String,
        modifier: Modifier = Modifier,
        onStreamingStateChanged: (Boolean) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isStreaming by remember { mutableStateOf(false) }
        var viewerCount by remember { mutableStateOf(0) }
        var selectedOption by remember { mutableStateOf(0) }
        
        LaunchedEffect(isStreaming) {
            if (isStreaming) {
                while (isStreaming) {
                    delay(3000)
                    viewerCount += (5..20).random()
                }
            } else {
                viewerCount = 0
            }
        }
        
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                Text(
                    text = if (isStreaming) "🔴 TV直播中" else "📺 准备TV直播",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                if (isStreaming) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = "👥 $viewerCount 观众",
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
            
            // TV优化的直播控制
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (isStreaming) {
                            isStreaming = false
                        } else {
                            isStreaming = true
                        }
                        onStreamingStateChanged(isStreaming)
                    },
                    modifier = Modifier
                        .size(140.dp, 70.dp)
                        .background(
                            if (selectedOption == 0) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStreaming) Color(0xFFFF3B30) else Color(0xFF007AFF)
                    )
                ) {
                    Text(
                        if (isStreaming) "结束直播" else "开始直播",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                Button(
                    onClick = { /* 直播设置 */ },
                    modifier = Modifier
                        .size(140.dp, 70.dp)
                        .background(
                            if (selectedOption == 1) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                ) {
                    Text("直播设置", style = MaterialTheme.typography.titleLarge)
                }
                
                Button(
                    onClick = { /* 互动管理 */ },
                    modifier = Modifier
                        .size(140.dp, 70.dp)
                        .background(
                            if (selectedOption == 2) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                ) {
                    Text("互动管理", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
    
    @Composable
    fun AudioVisualizer(
        audioLevels: List<Float>,
        modifier: Modifier = Modifier
    ) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val barWidth = size.width / audioLevels.size
            audioLevels.forEachIndexed { index, level ->
                val barHeight = size.height * level
                drawRect(
                    color = Color(0xFF00C853), // TV绿色
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = index * barWidth,
                        y = size.height - barHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = barWidth * 0.9f,
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

/**
 * TV平台特定的媒体工具
 */
object TVMediaUtils {
    
    /**
     * 检查TV是否支持HDMI输入
     */
    fun hasHDMISupport(): Boolean = true
    
    /**
     * 检查TV是否支持4K播放
     */
    fun has4KSupport(): Boolean = true
    
    /**
     * 检查TV是否支持HDR
     */
    fun hasHDRSupport(): Boolean = true
    
    /**
     * 获取TV支持的分辨率
     */
    fun getSupportedResolutions(): List<String> {
        return listOf("1920x1080", "3840x2160", "1280x720")
    }
    
    /**
     * 检查遥控器连接状态
     */
    fun isRemoteConnected(): Boolean = true
    
    /**
     * 获取音频输出设备
     */
    fun getAudioOutputDevices(): List<String> {
        return listOf("TV扬声器", "HDMI音频", "光纤输出", "蓝牙音响")
    }
}
