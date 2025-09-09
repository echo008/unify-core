package com.unify.ui.components.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
actual object UnifyLiveComponents {
    /**
     * 实时相机预览组件
     */
    @Composable
    actual fun LiveCameraPreview(
        modifier: Modifier,
        onCameraReady: () -> Unit,
        onError: (String) -> Unit,
    ) {
        var isInitialized by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                // 模拟相机初始化
                kotlinx.coroutines.delay(1000)
                isInitialized = true
                onCameraReady()
            } catch (e: Exception) {
                errorMessage = "相机初始化失败: ${e.message}"
                onError(errorMessage!!)
            }
        }

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .aspectRatio(16f / 9f),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = "📷 $errorMessage",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    } else if (isInitialized) {
                        Text(
                            text = "📹 Android相机预览已就绪",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(
                            text = "📷 相机初始化中...",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    /**
     * 实时音频波形显示组件
     */
    @Composable
    actual fun LiveAudioWaveform(
        modifier: Modifier,
        isRecording: Boolean,
        onRecordingToggle: (Boolean) -> Unit,
    ) {
        var amplitude by remember { mutableStateOf(0f) }
        var recordingTime by remember { mutableStateOf(0) }

        LaunchedEffect(isRecording) {
            if (isRecording) {
                recordingTime = 0
                while (isRecording) {
                    // 模拟音频波形数据
                    amplitude = (0..100).random() / 100f
                    recordingTime++
                    kotlinx.coroutines.delay(100)
                }
            } else {
                amplitude = 0f
                recordingTime = 0
            }
        }

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 音频波形可视化区域
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (isRecording) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "🎵 Android录音中...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                        )
                        Text(
                            text = "音量: ${(amplitude * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                        Text(
                            text = "时间: ${recordingTime / 10}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                } else {
                    Text(
                        text = "🎤 点击开始录音",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 录音控制按钮
            Button(
                onClick = {
                    onRecordingToggle(!isRecording)
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isRecording) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    ),
            ) {
                Text(if (isRecording) "停止录制" else "开始录制")
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
        onPlaybackStateChanged: (Boolean) -> Unit = {},
    ) {
        val context = LocalContext.current
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentPosition by remember { mutableStateOf(0) }
        var duration by remember { mutableStateOf(0) }

        LaunchedEffect(audioUrl) {
            mediaPlayer?.release()
            mediaPlayer =
                MediaPlayer().apply {
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                LinearProgressIndicator(
                    progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
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
                        },
                    ) {
                        Icon(
                            imageVector =
                                if (isPlaying) {
                                    androidx.compose.material.icons.Icons.Default.Pause
                                } else {
                                    androidx.compose.material.icons.Icons.Default.PlayArrow
                                },
                            contentDescription = if (isPlaying) "暂停" else "播放",
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
        onPlaybackStateChanged: (Boolean) -> Unit = {},
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
            modifier = modifier,
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
        onError: (String) -> Unit = {},
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isRecording) "录制中: ${formatTime(recordingTime * 1000)}" else "准备录制",
                    style = MaterialTheme.typography.titleMedium,
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
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isRecording) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        ),
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
        modifier: Modifier = Modifier,
    ) {
        Canvas(
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(100.dp),
        ) {
            val barWidth = size.width / audioLevels.size
            audioLevels.forEachIndexed { index, level ->
                val barHeight = size.height * level
                drawRect(
                    color = androidx.compose.ui.graphics.Color.Blue,
                    topLeft =
                        androidx.compose.ui.geometry.Offset(
                            x = index * barWidth,
                            y = size.height - barHeight,
                        ),
                    size =
                        androidx.compose.ui.geometry.Size(
                            width = barWidth * 0.8f,
                            height = barHeight,
                        ),
                )
            }
        }
    }

    private fun startRecording(
        context: Context,
        onError: (String) -> Unit,
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
        onError: (String) -> Unit,
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
            android.Manifest.permission.CAMERA,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查录音权限
     */
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查存储权限
     */
    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
