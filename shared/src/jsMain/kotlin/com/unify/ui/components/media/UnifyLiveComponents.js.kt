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
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.url.URL
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Web平台实时媒体组件
 */
object WebLiveComponents {
    
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
        var mediaStream by remember { mutableStateOf<MediaStream?>(null) }
        var videoElement by remember { mutableStateOf<HTMLVideoElement?>(null) }
        
        LaunchedEffect(Unit) {
            try {
                val constraints = js("""({
                    video: { width: 1280, height: 720 },
                    audio: false
                })""").unsafeCast<MediaStreamConstraints>()
                
                val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
                mediaStream = stream
                
                val video = document.createElement("video") as HTMLVideoElement
                video.srcObject = stream
                video.autoplay = true
                video.muted = true
                videoElement = video
            } catch (e: Exception) {
                onError("相机初始化失败: ${e.message}")
            }
        }
        
        DisposableEffect(Unit) {
            onDispose {
                mediaStream?.getTracks()?.forEach { track ->
                    track.stop()
                }
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
                    text = "📷 Web相机预览",
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
                        capturePhoto(videoElement, onImageCaptured, onError)
                    }
                ) {
                    Text("拍照")
                }
                
                Button(
                    onClick = {
                        if (isRecording) {
                            stopVideoRecording(onError)
                            isRecording = false
                        } else {
                            startVideoRecording(mediaStream, onError)
                            isRecording = true
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
                
                Button(
                    onClick = {
                        switchCamera(onError)
                    }
                ) {
                    Text("切换摄像头")
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
        var audioElement by remember { mutableStateOf<HTMLAudioElement?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentTime by remember { mutableStateOf(0.0) }
        var duration by remember { mutableStateOf(0.0) }
        
        LaunchedEffect(audioUrl) {
            val audio = document.createElement("audio") as HTMLAudioElement
            audio.src = audioUrl
            audio.preload = "metadata"
            
            audio.addEventListener("loadedmetadata", { _: Event ->
                duration = audio.duration
            })
            
            audio.addEventListener("timeupdate", { _: Event ->
                currentTime = audio.currentTime
            })
            
            audio.addEventListener("ended", { _: Event ->
                isPlaying = false
                onPlaybackStateChanged(false)
            })
            
            audioElement = audio
        }
        
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying && audioElement?.paused == false) {
                    currentTime = audioElement?.currentTime ?: 0.0
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
                            audioElement?.let { audio ->
                                if (isPlaying) {
                                    audio.pause()
                                    isPlaying = false
                                } else {
                                    audio.play()
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
                    
                    IconButton(
                        onClick = {
                            audioElement?.let { audio ->
                                audio.currentTime = 0.0
                                currentTime = 0.0
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "重播"
                        )
                    }
                    
                    Text(formatTime(duration))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = if (duration > 0) (currentTime / duration).toFloat() else 0f,
                    onValueChange = { value ->
                        audioElement?.let { audio ->
                            val newTime = value * duration
                            audio.currentTime = newTime
                            currentTime = newTime
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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
        var videoElement by remember { mutableStateOf<HTMLVideoElement?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var isFullscreen by remember { mutableStateOf(false) }
        
        LaunchedEffect(videoUrl) {
            val video = document.createElement("video") as HTMLVideoElement
            video.src = videoUrl
            video.controls = false
            video.preload = "metadata"
            
            video.addEventListener("play", { _: Event ->
                isPlaying = true
                onPlaybackStateChanged(true)
            })
            
            video.addEventListener("pause", { _: Event ->
                isPlaying = false
                onPlaybackStateChanged(false)
            })
            
            video.addEventListener("ended", { _: Event ->
                isPlaying = false
                onPlaybackStateChanged(false)
            })
            
            videoElement = video
        }
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📹 Web视频播放器",
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
                        videoElement?.let { video ->
                            if (isPlaying) {
                                video.pause()
                            } else {
                                video.play()
                            }
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
                        videoElement?.let { video ->
                            video.currentTime = 0.0
                        }
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "重播",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = {
                        videoElement?.let { video ->
                            if (isFullscreen) {
                                document.exitFullscreen()
                                isFullscreen = false
                            } else {
                                video.requestFullscreen()
                                isFullscreen = true
                            }
                        }
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "退出全屏" else "全屏",
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
        var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
        var mediaStream by remember { mutableStateOf<MediaStream?>(null) }
        
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
                            stopAudioRecording(mediaRecorder, onRecordingComplete, onError)
                            isRecording = false
                        } else {
                            startAudioRecording(
                                onRecorderReady = { recorder, stream ->
                                    mediaRecorder = recorder
                                    mediaStream = stream
                                    isRecording = true
                                },
                                onError = onError
                            )
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
                    color = Color(0xFF4285F4), // Google蓝色
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
     * 屏幕共享组件
     */
    @Composable
    fun ScreenShare(
        modifier: Modifier = Modifier,
        onSharingStateChanged: (Boolean) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isSharing by remember { mutableStateOf(false) }
        var mediaStream by remember { mutableStateOf<MediaStream?>(null) }
        
        DisposableEffect(Unit) {
            onDispose {
                mediaStream?.getTracks()?.forEach { track ->
                    track.stop()
                }
            }
        }
        
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSharing) "🖥️ 屏幕共享中" else "📺 准备屏幕共享",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (isSharing) {
                            stopScreenShare(mediaStream, onError)
                            isSharing = false
                        } else {
                            startScreenShare(
                                onStreamReady = { stream ->
                                    mediaStream = stream
                                    isSharing = true
                                },
                                onError = onError
                            )
                        }
                        onSharingStateChanged(isSharing)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSharing) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isSharing) "停止共享" else "开始共享")
                }
            }
        }
    }
    
    /**
     * WebRTC视频通话组件
     */
    @Composable
    fun VideoCall(
        remoteStreamUrl: String?,
        modifier: Modifier = Modifier,
        onCallStateChanged: (Boolean) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        var isInCall by remember { mutableStateOf(false) }
        var isMuted by remember { mutableStateOf(false) }
        var isVideoEnabled by remember { mutableStateOf(true) }
        
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                // 远程视频流
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isInCall) "📹 通话中" else "📞 准备通话",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                // 本地视频预览
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(120.dp, 160.dp)
                        .padding(12.dp)
                        .background(Color.Gray)
                ) {
                    Text(
                        text = "📱",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        isMuted = !isMuted
                    },
                    modifier = Modifier.background(
                        if (isMuted) Color.Red else Color.Gray,
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "取消静音" else "静音",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = {
                        if (isInCall) {
                            endCall(onError)
                            isInCall = false
                        } else {
                            startCall(onError)
                            isInCall = true
                        }
                        onCallStateChanged(isInCall)
                    },
                    modifier = Modifier.background(
                        if (isInCall) Color.Red else Color.Green,
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = if (isInCall) Icons.Default.CallEnd else Icons.Default.Call,
                        contentDescription = if (isInCall) "挂断" else "接听",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = {
                        isVideoEnabled = !isVideoEnabled
                    },
                    modifier = Modifier.background(
                        if (!isVideoEnabled) Color.Red else Color.Gray,
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = if (isVideoEnabled) "关闭视频" else "开启视频",
                        tint = Color.White
                    )
                }
            }
        }
    }
    
    private fun capturePhoto(
        videoElement: HTMLVideoElement?,
        onImageCaptured: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            videoElement?.let { video ->
                val canvas = document.createElement("canvas") as HTMLCanvasElement
                canvas.width = video.videoWidth
                canvas.height = video.videoHeight
                
                val context = canvas.getContext("2d") as CanvasRenderingContext2D
                context.drawImage(video, 0.0, 0.0)
                
                val dataUrl = canvas.toDataURL("image/jpeg")
                onImageCaptured(dataUrl)
            }
        } catch (e: Exception) {
            onError("拍照失败: ${e.message}")
        }
    }
    
    private fun startVideoRecording(
        mediaStream: MediaStream?,
        onError: (String) -> Unit
    ) {
        try {
            mediaStream?.let { stream ->
                val recorder = MediaRecorder(stream)
                recorder.start()
            }
        } catch (e: Exception) {
            onError("视频录制启动失败: ${e.message}")
        }
    }
    
    private fun stopVideoRecording(onError: (String) -> Unit) {
        try {
            // 停止视频录制的实现
        } catch (e: Exception) {
            onError("视频录制停止失败: ${e.message}")
        }
    }
    
    private fun switchCamera(onError: (String) -> Unit) {
        try {
            // 切换摄像头的实现
        } catch (e: Exception) {
            onError("摄像头切换失败: ${e.message}")
        }
    }
    
    private fun startAudioRecording(
        onRecorderReady: (MediaRecorder, MediaStream) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val constraints = js("""({
                audio: true,
                video: false
            })""").unsafeCast<MediaStreamConstraints>()
            
            window.navigator.mediaDevices.getUserMedia(constraints).then { stream ->
                val recorder = MediaRecorder(stream)
                onRecorderReady(recorder, stream)
                recorder.start()
            }.catch { error ->
                onError("音频录制启动失败: $error")
            }
        } catch (e: Exception) {
            onError("音频录制启动失败: ${e.message}")
        }
    }
    
    private fun stopAudioRecording(
        mediaRecorder: MediaRecorder?,
        onRecordingComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            mediaRecorder?.stop()
            onRecordingComplete("录音完成")
        } catch (e: Exception) {
            onError("录音停止失败: ${e.message}")
        }
    }
    
    private fun startScreenShare(
        onStreamReady: (MediaStream) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            window.navigator.mediaDevices.getDisplayMedia(js("{}")).then { stream ->
                onStreamReady(stream)
            }.catch { error ->
                onError("屏幕共享启动失败: $error")
            }
        } catch (e: Exception) {
            onError("屏幕共享启动失败: ${e.message}")
        }
    }
    
    private fun stopScreenShare(
        mediaStream: MediaStream?,
        onError: (String) -> Unit
    ) {
        try {
            mediaStream?.getTracks()?.forEach { track ->
                track.stop()
            }
        } catch (e: Exception) {
            onError("屏幕共享停止失败: ${e.message}")
        }
    }
    
    private fun startCall(onError: (String) -> Unit) {
        try {
            // WebRTC通话启动的实现
        } catch (e: Exception) {
            onError("通话启动失败: ${e.message}")
        }
    }
    
    private fun endCall(onError: (String) -> Unit) {
        try {
            // WebRTC通话结束的实现
        } catch (e: Exception) {
            onError("通话结束失败: ${e.message}")
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
 * Web平台特定的媒体工具
 */
object WebMediaUtils {
    
    /**
     * 检查浏览器是否支持getUserMedia
     */
    fun hasGetUserMediaSupport(): Boolean {
        return js("navigator.mediaDevices && navigator.mediaDevices.getUserMedia") != null
    }
    
    /**
     * 检查浏览器是否支持屏幕共享
     */
    fun hasScreenShareSupport(): Boolean {
        return js("navigator.mediaDevices && navigator.mediaDevices.getDisplayMedia") != null
    }
    
    /**
     * 检查浏览器是否支持MediaRecorder
     */
    fun hasMediaRecorderSupport(): Boolean {
        return js("window.MediaRecorder") != null
    }
    
    /**
     * 检查浏览器是否支持WebRTC
     */
    fun hasWebRTCSupport(): Boolean {
        return js("window.RTCPeerConnection") != null
    }
    
    /**
     * 获取支持的媒体格式
     */
    fun getSupportedMimeTypes(): List<String> {
        val types = listOf(
            "video/webm;codecs=vp9",
            "video/webm;codecs=vp8",
            "video/webm",
            "video/mp4;codecs=h264",
            "video/mp4",
            "audio/webm;codecs=opus",
            "audio/webm",
            "audio/mp4",
            "audio/mpeg"
        )
        
        return types.filter { type ->
            js("MediaRecorder.isTypeSupported(type)") as Boolean
        }
    }
    
    /**
     * 请求相机和麦克风权限
     */
    suspend fun requestMediaPermissions(): Boolean {
        return try {
            val constraints = js("""({
                video: true,
                audio: true
            })""").unsafeCast<MediaStreamConstraints>()
            
            val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            stream.getTracks().forEach { track ->
                track.stop()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取可用的摄像头设备
     */
    suspend fun getVideoDevices(): List<String> {
        return try {
            val devices = window.navigator.mediaDevices.enumerateDevices().await()
            devices.filter { device ->
                device.kind == "videoinput"
            }.map { device ->
                device.label.ifEmpty { "摄像头 ${device.deviceId.take(8)}" }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 获取可用的音频设备
     */
    suspend fun getAudioDevices(): List<String> {
        return try {
            val devices = window.navigator.mediaDevices.enumerateDevices().await()
            devices.filter { device ->
                device.kind == "audioinput"
            }.map { device ->
                device.label.ifEmpty { "麦克风 ${device.deviceId.take(8)}" }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
