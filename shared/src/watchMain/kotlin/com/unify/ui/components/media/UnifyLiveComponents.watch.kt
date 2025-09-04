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
 * Watch平台实时媒体组件
 * 针对小屏幕和触摸操作优化
 */
object WatchLiveComponents {
    
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
                    .aspectRatio(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌚ Watch相机",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Watch优化的紧凑按钮布局
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { 
                        onImageCaptured("watch_photo_${System.currentTimeMillis()}.jpg")
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "拍照",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = { isRecording = !isRecording },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (isRecording) "停止" else "录制",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
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
        
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Watch优化的小进度条
                LinearProgressIndicator(
                    progress = { (currentTime / duration).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 紧凑的时间显示
                Text(
                    text = "${formatTime(currentTime)} / ${formatTime(duration)}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Watch优化的播放控制
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            onPlaybackStateChanged(isPlaying)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { /* 重播 */ },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = "重播",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    fun HeartRateMonitor(
        modifier: Modifier = Modifier,
        onHeartRateChanged: (Int) -> Unit = {}
    ) {
        var heartRate by remember { mutableStateOf(72) }
        var isMonitoring by remember { mutableStateOf(false) }
        
        LaunchedEffect(isMonitoring) {
            if (isMonitoring) {
                while (isMonitoring) {
                    delay(1000)
                    heartRate = (65..85).random()
                    onHeartRateChanged(heartRate)
                }
            }
        }
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE91E63).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "心率",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$heartRate",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE91E63)
                )
                
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE91E63)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { isMonitoring = !isMonitoring },
                    modifier = Modifier.size(80.dp, 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoring) MaterialTheme.colorScheme.error else Color(0xFFE91E63)
                    )
                ) {
                    Text(
                        if (isMonitoring) "停止" else "开始",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    
    @Composable
    fun WorkoutRecorder(
        modifier: Modifier = Modifier,
        onWorkoutStateChanged: (Boolean) -> Unit = {}
    ) {
        var isRecording by remember { mutableStateOf(false) }
        var workoutTime by remember { mutableStateOf(0) }
        var calories by remember { mutableStateOf(0) }
        
        LaunchedEffect(isRecording) {
            if (isRecording) {
                while (isRecording) {
                    delay(1000)
                    workoutTime++
                    calories += (1..3).random()
                }
            } else {
                workoutTime = 0
                calories = 0
            }
        }
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRecording) "运动中" else "准备运动",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isRecording) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatTime(workoutTime.toDouble()),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "时间",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$calories",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = "卡路里",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Button(
                    onClick = {
                        isRecording = !isRecording
                        onWorkoutStateChanged(isRecording)
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        if (isRecording) "结束运动" else "开始运动",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    
    @Composable
    fun VoiceRecorder(
        modifier: Modifier = Modifier,
        onRecordingComplete: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isRecording by remember { mutableStateOf(false) }
        var recordingTime by remember { mutableStateOf(0) }
        
        LaunchedEffect(isRecording) {
            if (isRecording) {
                while (isRecording) {
                    delay(1000)
                    recordingTime++
                }
            } else {
                recordingTime = 0
            }
        }
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "语音录制",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isRecording) formatTime(recordingTime.toDouble()) else "点击录制",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2196F3)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        if (isRecording) {
                            onRecordingComplete("watch_voice_${System.currentTimeMillis()}.wav")
                            isRecording = false
                        } else {
                            isRecording = true
                        }
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            if (isRecording) MaterialTheme.colorScheme.error else Color(0xFF2196F3),
                            CircleShape
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = if (isRecording) "停止" else "录制",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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
                .height(60.dp)
        ) {
            val barWidth = size.width / audioLevels.size
            audioLevels.forEachIndexed { index, level ->
                val barHeight = size.height * level
                drawRect(
                    color = Color(0xFF9C27B0), // Watch紫色
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

/**
 * Watch平台特定的媒体工具
 */
object WatchMediaUtils {
    
    /**
     * 检查Watch是否支持心率监测
     */
    fun hasHeartRateSupport(): Boolean = true
    
    /**
     * 检查Watch是否支持GPS
     */
    fun hasGPSSupport(): Boolean = true
    
    /**
     * 检查Watch是否支持蜂窝网络
     */
    fun hasCellularSupport(): Boolean = false
    
    /**
     * 获取Watch电池电量
     */
    fun getBatteryLevel(): Int = 85
    
    /**
     * 检查Watch是否支持NFC支付
     */
    fun hasNFCPaymentSupport(): Boolean = true
    
    /**
     * 获取Watch支持的运动类型
     */
    fun getSupportedWorkoutTypes(): List<String> {
        return listOf("跑步", "步行", "骑行", "游泳", "瑜伽", "力量训练")
    }
    
    /**
     * 检查Watch是否支持语音助手
     */
    fun hasVoiceAssistantSupport(): Boolean = true
    
    /**
     * 获取Watch屏幕尺寸信息
     */
    fun getScreenInfo(): Pair<Int, Int> = Pair(368, 448) // Apple Watch Series 7
}
