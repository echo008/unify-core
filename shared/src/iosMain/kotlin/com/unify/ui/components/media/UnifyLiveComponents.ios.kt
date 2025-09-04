package com.unify.ui.components.media

import androidx.compose.foundation.Canvas
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
import platform.AVFoundation.*
import platform.CoreMedia.*
import platform.Foundation.*
import platform.UIKit.*

/**
 * iOS平台实时媒体组件
 */
object IOSLiveComponents {
    
    /**
     * 相机预览组件
     */
    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
        onImageCaptured: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isRecording by remember { mutableStateOf(false) }
        var captureSession by remember { mutableStateOf<AVCaptureSession?>(null) }
        var photoOutput by remember { mutableStateOf<AVCapturePhotoOutput?>(null) }
        
        LaunchedEffect(Unit) {
            try {
                val session = AVCaptureSession()
                session.sessionPreset = AVCaptureSessionPresetHigh
                
                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                if (device != null) {
                    val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                    if (input != null && session.canAddInput(input)) {
                        session.addInput(input)
                    }
                    
                    val output = AVCapturePhotoOutput()
                    if (session.canAddOutput(output)) {
                        session.addOutput(output)
                        photoOutput = output
                    }
                    
                    session.startRunning()
                    captureSession = session
                }
            } catch (e: Exception) {
                onError("相机初始化失败: ${e.message}")
            }
        }
        
        DisposableEffect(Unit) {
            onDispose {
                captureSession?.stopRunning()
            }
        }
        
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷 iOS相机预览",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        capturePhoto(photoOutput, onImageCaptured, onError)
                    }
                ) {
                    Text("拍照")
                }
                
                Button(
                    onClick = {
                        toggleFlash(onError)
                    }
                ) {
                    Text("闪光灯")
                }
                
                Button(
                    onClick = {
                        switchCamera(captureSession, onError)
                    }
                ) {
                    Text("切换")
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
        var audioPlayer by remember { mutableStateOf<AVAudioPlayer?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentTime by remember { mutableStateOf(0.0) }
        var duration by remember { mutableStateOf(0.0) }
        
        LaunchedEffect(audioUrl) {
            try {
                val url = NSURL.URLWithString(audioUrl)
                if (url != null) {
                    val data = NSData.dataWithContentsOfURL(url)
                    if (data != null) {
                        val player = AVAudioPlayer(data, null)
                        player?.prepareToPlay()
                        duration = player?.duration ?: 0.0
                        audioPlayer = player
                    }
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
        
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying && audioPlayer?.playing == true) {
                    currentTime = audioPlayer?.currentTime ?: 0.0
                    delay(100)
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
                    progress = { if (duration > 0) (currentTime / duration).toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatTime(currentTime))
                    
                    IconButton(
                        onClick = {
                            audioPlayer?.let { player ->
                                if (isPlaying) {
                                    player.pause()
                                    isPlaying = false
                                } else {
                                    player.play()
                                    isPlaying = true
                                }
                                onPlaybackStateChanged(isPlaying)
                            }
                        }
                    ) {
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
    
    /**
     * 视频播放器组件
     */
    @Composable
    fun VideoPlayer(
        videoUrl: String,
        modifier: Modifier = Modifier,
        onPlaybackStateChanged: (Boolean) -> Unit = {}
    ) {
        var player by remember { mutableStateOf<AVPlayer?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        
        LaunchedEffect(videoUrl) {
            try {
                val url = NSURL.URLWithString(videoUrl)
                if (url != null) {
                    val playerItem = AVPlayerItem.playerItemWithURL(url)
                    val avPlayer = AVPlayer.playerWithPlayerItem(playerItem)
                    player = avPlayer
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📹 iOS视频播放器",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = {
                        player?.let { p ->
                            if (isPlaying) {
                                p.pause()
                                isPlaying = false
                            } else {
                                p.play()
                                isPlaying = true
                            }
                            onPlaybackStateChanged(isPlaying)
                        }
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = {
                        player?.seekToTime(CMTimeMake(0, 1))
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "重播",
                        tint = Color.White
                    )
                }
            }
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
        var audioRecorder by remember { mutableStateOf<AVAudioRecorder?>(null) }
        
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
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRecording) "录制中: ${formatTime(recordingTime.toDouble())}" else "准备录制",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (isRecording) {
                            stopRecording(audioRecorder, onRecordingComplete, onError)
                            isRecording = false
                        } else {
                            audioRecorder = startRecording(onError)
                            if (audioRecorder != null) {
                                isRecording = true
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
                    color = Color(0xFF007AFF), // iOS蓝色
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
    
    /**
     * 直播推流组件
     */
    @Composable
    fun LiveStreaming(
        streamUrl: String,
        modifier: Modifier = Modifier,
        onStreamingStateChanged: (Boolean) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isStreaming by remember { mutableStateOf(false) }
        var viewerCount by remember { mutableStateOf(0) }
        
        LaunchedEffect(isStreaming) {
            if (isStreaming) {
                // 模拟观众数量变化
                while (isStreaming) {
                    delay(5000)
                    viewerCount += (1..10).random()
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
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isStreaming) "🔴 直播中" else "📱 准备直播",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                if (isStreaming) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = "👥 $viewerCount",
                            modifier = Modifier.padding(8.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
                        if (isStreaming) {
                            stopStreaming(onError)
                            isStreaming = false
                        } else {
                            startStreaming(streamUrl, onError)
                            isStreaming = true
                        }
                        onStreamingStateChanged(isStreaming)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStreaming) Color(0xFFFF3B30) else Color(0xFF007AFF)
                    )
                ) {
                    Text(if (isStreaming) "结束直播" else "开始直播")
                }
                
                Button(
                    onClick = {
                        // 切换摄像头
                    }
                ) {
                    Text("切换摄像头")
                }
                
                Button(
                    onClick = {
                        // 美颜设置
                    }
                ) {
                    Text("美颜")
                }
            }
        }
    }
    
    private fun capturePhoto(
        photoOutput: AVCapturePhotoOutput?,
        onImageCaptured: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            photoOutput?.let { output ->
                val settings = AVCapturePhotoSettings.photoSettings()
                // 在实际实现中会配置拍照设置并执行拍照
                onImageCaptured("photo_captured_${System.currentTimeMillis()}.jpg")
            }
        } catch (e: Exception) {
            onError("拍照失败: ${e.message}")
        }
    }
    
    private fun toggleFlash(onError: (String) -> Unit) {
        try {
            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            device?.let { dev ->
                if (dev.hasTorch) {
                    dev.lockForConfiguration(null)
                    dev.torchMode = if (dev.torchMode == AVCaptureTorchModeOn) {
                        AVCaptureTorchModeOff
                    } else {
                        AVCaptureTorchModeOn
                    }
                    dev.unlockForConfiguration()
                }
            }
        } catch (e: Exception) {
            onError("闪光灯切换失败: ${e.message}")
        }
    }
    
    private fun switchCamera(
        captureSession: AVCaptureSession?,
        onError: (String) -> Unit
    ) {
        try {
            // 在实际实现中会切换前后摄像头
            onError("摄像头切换功能需要完整实现")
        } catch (e: Exception) {
            onError("摄像头切换失败: ${e.message}")
        }
    }
    
    private fun startRecording(onError: (String) -> Unit): AVAudioRecorder? {
        return try {
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory, 
                NSUserDomainMask, 
                true
            ).firstOrNull() as? String
            
            if (documentsPath != null) {
                val filePath = "$documentsPath/recording_${System.currentTimeMillis()}.m4a"
                val url = NSURL.fileURLWithPath(filePath)
                
                val settings = mapOf<Any?, Any?>(
                    AVFormatIDKey to kAudioFormatMPEG4AAC,
                    AVSampleRateKey to 44100.0,
                    AVNumberOfChannelsKey to 2,
                    AVEncoderAudioQualityKey to AVAudioQualityHigh
                )
                
                val recorder = AVAudioRecorder(url, settings, null)
                recorder?.prepareToRecord()
                recorder?.record()
                recorder
            } else {
                onError("无法创建录音文件")
                null
            }
        } catch (e: Exception) {
            onError("录音启动失败: ${e.message}")
            null
        }
    }
    
    private fun stopRecording(
        audioRecorder: AVAudioRecorder?,
        onRecordingComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            audioRecorder?.stop()
            onRecordingComplete("录音完成")
        } catch (e: Exception) {
            onError("录音停止失败: ${e.message}")
        }
    }
    
    private fun startStreaming(streamUrl: String, onError: (String) -> Unit) {
        try {
            // 在实际实现中会配置RTMP推流
            // 这里只是模拟
        } catch (e: Exception) {
            onError("直播启动失败: ${e.message}")
        }
    }
    
    private fun stopStreaming(onError: (String) -> Unit) {
        try {
            // 在实际实现中会停止RTMP推流
        } catch (e: Exception) {
            onError("直播停止失败: ${e.message}")
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
 * iOS平台特定的媒体工具
 */
object IOSMediaUtils {
    
    /**
     * 检查相机权限
     */
    fun hasCameraPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }
    
    /**
     * 请求相机权限
     */
    fun requestCameraPermission(completion: (Boolean) -> Unit) {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            completion(granted)
        }
    }
    
    /**
     * 检查麦克风权限
     */
    fun hasMicrophonePermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)
        return status == AVAuthorizationStatusAuthorized
    }
    
    /**
     * 请求麦克风权限
     */
    fun requestMicrophonePermission(completion: (Boolean) -> Unit) {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { granted ->
            completion(granted)
        }
    }
    
    /**
     * 获取可用的相机设备
     */
    fun getAvailableCameraDevices(): List<String> {
        val devices = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
        return devices.map { device ->
            (device as AVCaptureDevice).localizedName
        }
    }
    
    /**
     * 检查设备是否支持闪光灯
     */
    fun hasFlashSupport(): Boolean {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        return device?.hasFlash ?: false
    }
    
    /**
     * 检查设备是否支持手电筒
     */
    fun hasTorchSupport(): Boolean {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        return device?.hasTorch ?: false
    }
}
