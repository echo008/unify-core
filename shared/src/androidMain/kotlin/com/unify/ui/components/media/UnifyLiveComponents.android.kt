package com.unify.ui.components.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File

/**
 * Android平台实时媒体组件
 */
object AndroidLiveComponents {
    
    /**
     * 相机预览组件 (简化实现)
     */
    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        onImageCaptured: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Column(modifier = modifier) {
            // 简化的相机预览占位符
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "相机预览\n(需要CameraX依赖)",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        onImageCaptured("mock_image_${System.currentTimeMillis()}")
                    }
                ) {
                    Text("拍照")
                }
                
                var flashEnabled by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        flashEnabled = !flashEnabled
                    }
                ) {
                    Text(if (flashEnabled) "关闭闪光灯" else "打开闪光灯")
                }
            }
        }
    }
    
    /**
     * 音频播放器组件
     */
    @Composable
    fun AudioPlayer(
        audioUrl: String,
        modifier: Modifier = Modifier,
        onPlaybackStateChanged: (Boolean) -> Unit = {}
    ) {
        val context = LocalContext.current
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentPosition by remember { mutableStateOf(0) }
        var duration by remember { mutableStateOf(0) }
        
        LaunchedEffect(audioUrl) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(context, Uri.parse(audioUrl))
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        duration = mp.duration
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        onPlaybackStateChanged(false)
                    }
                } catch (e: Exception) {
                    // 处理错误
                }
            }
        }
        
        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.release()
            }
        }
        
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying && mediaPlayer?.isPlaying == true) {
                    currentPosition = mediaPlayer?.currentPosition ?: 0
                    kotlinx.coroutines.delay(100)
                }
            }
        }
        
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatTime(currentPosition))
                    
                    IconButton(
                        onClick = {
                            mediaPlayer?.let { mp ->
                                if (isPlaying) {
                                    mp.pause()
                                    isPlaying = false
                                } else {
                                    mp.start()
                                    isPlaying = true
                                }
                                onPlaybackStateChanged(isPlaying)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) 
                                androidx.compose.material.icons.Icons.Default.Pause 
                            else 
                                androidx.compose.material.icons.Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放"
                        )
                    }
                    
                    Text(formatTime(duration))
                }
            }
        }
    }
    
    /**
     * 视频播放器组件
     */
    @Composable
    fun VideoPlayer(
        videoUrl: String,
        modifier: Modifier = Modifier,
        onPlaybackStateChanged: (Boolean) -> Unit = {}
    ) {
        val context = LocalContext.current
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        
        AndroidView(
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    setVideoURI(Uri.parse(videoUrl))
                    setOnPreparedListener { mp ->
                        mediaPlayer = mp
                        mp.setOnCompletionListener {
                            isPlaying = false
                            onPlaybackStateChanged(false)
                        }
                    }
                }
            },
            modifier = modifier
        ) { videoView ->
            // 更新视频视图
        }
    }
    
    /**
     * 音频录制组件
     */
    @Composable
    fun AudioRecorder(
        modifier: Modifier = Modifier,
        onRecordingComplete: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isRecording by remember { mutableStateOf(false) }
        var recordingTime by remember { mutableStateOf(0) }
        var mediaRecorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        
        LaunchedEffect(isRecording) {
            if (isRecording) {
                while (isRecording) {
                    kotlinx.coroutines.delay(1000)
                    recordingTime++
                }
            } else {
                recordingTime = 0
            }
        }
        
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRecording) "录制中: ${formatTime(recordingTime * 1000)}" else "准备录制",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (isRecording) {
                                stopRecording(mediaRecorder, onRecordingComplete, onError)
                                isRecording = false
                                mediaRecorder = null
                            } else {
                                mediaRecorder = startRecording(context, onError)
                                if (mediaRecorder != null) {
                                    isRecording = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isRecording) "停止录制" else "开始录制")
                }
            }
        }
    }
    
    /**
     * 实时音频可视化组件
     */
    @Composable
    fun AudioVisualizer(
        audioLevels: List<Float>,
        modifier: Modifier = Modifier
    ) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val barWidth = size.width / audioLevels.size
            audioLevels.forEachIndexed { index, level ->
                val barHeight = size.height * level
                drawRect(
                    color = androidx.compose.ui.graphics.Color.Blue,
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
    
    private fun startRecording(
        context: Context,
        onError: (String) -> Unit
    ): android.media.MediaRecorder? {
        return try {
            val outputFile = File(context.cacheDir, "recording_${System.currentTimeMillis()}.3gp")
            
            android.media.MediaRecorder().apply {
                setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            onError("录制启动失败: ${e.message}")
            null
        }
    }
    
    private fun stopRecording(
        mediaRecorder: android.media.MediaRecorder?,
        onRecordingComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            onRecordingComplete("录制完成")
        } catch (e: Exception) {
            onError("录制停止失败: ${e.message}")
        }
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

/**
 * Android平台特定的媒体工具
 */
object AndroidMediaUtils {
    
    /**
     * 检查相机权限
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查录音权限
     */
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查存储权限
     */
    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
