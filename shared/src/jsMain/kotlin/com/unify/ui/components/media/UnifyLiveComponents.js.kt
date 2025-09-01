package com.unify.ui.components.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.events.Event
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.url.URL

/**
 * Web 平台直播播放器实现
 */
@Composable
actual fun PlatformLivePlayer(
    config: UnifyLivePlayerConfig,
    onStateChange: ((UnifyLivePlayerState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    var videoElement by remember { mutableStateOf<HTMLVideoElement?>(null) }
    
    DisposableEffect(config.src) {
        val video = document.createElement("video") as HTMLVideoElement
        video.apply {
            src = config.src
            controls = true
            autoplay = config.autoplay
            muted = config.muted
            
            // 设置播放模式
            if (config.mode == UnifyLiveMode.LIVE) {
                setAttribute("playsinline", "true")
                setAttribute("webkit-playsinline", "true")
            }
            
            // 事件监听
            addEventListener("loadstart", {
                onStateChange?.invoke(UnifyLivePlayerState.LOADING)
            })
            
            addEventListener("canplay", {
                onStateChange?.invoke(UnifyLivePlayerState.PLAYING)
            })
            
            addEventListener("pause", {
                onStateChange?.invoke(UnifyLivePlayerState.PAUSED)
            })
            
            addEventListener("ended", {
                onStateChange?.invoke(UnifyLivePlayerState.ENDED)
            })
            
            addEventListener("error", { event ->
                val error = (event.target as HTMLVideoElement).error
                onError?.invoke("Video error: ${error?.message ?: "Unknown error"}")
                onStateChange?.invoke(UnifyLivePlayerState.ERROR)
            })
            
            addEventListener("waiting", {
                onStateChange?.invoke(UnifyLivePlayerState.BUFFERING)
            })
            
            addEventListener("playing", {
                onStateChange?.invoke(UnifyLivePlayerState.PLAYING)
            })
        }
        
        videoElement = video
        
        onDispose {
            video.pause()
            video.src = ""
            videoElement = null
        }
    }
    
    // 使用 HTML5 video 元素进行直播播放
    androidx.compose.ui.platform.LocalDensity.current
}

/**
 * Web 平台直播推流器实现
 */
@Composable
actual fun PlatformLivePusher(
    config: UnifyLivePusherConfig,
    onStateChange: ((UnifyLivePusherState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    var mediaStream by remember { mutableStateOf<MediaStream?>(null) }
    var videoElement by remember { mutableStateOf<HTMLVideoElement?>(null) }
    
    LaunchedEffect(config.url) {
        try {
            onStateChange?.invoke(UnifyLivePusherState.CONNECTING)
            
            // 获取用户媒体流
            val constraints = js("""({
                video: {
                    width: ${config.videoWidth},
                    height: ${config.videoHeight},
                    facingMode: "${if (config.devicePosition == "front") "user" else "environment"}"
                },
                audio: ${config.enableMic}
            })""").unsafeCast<MediaStreamConstraints>()
            
            val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            mediaStream = stream
            
            // 创建视频预览
            val video = document.createElement("video") as HTMLVideoElement
            video.apply {
                srcObject = stream
                autoplay = true
                muted = true
                setAttribute("playsinline", "true")
            }
            videoElement = video
            
            // 这里应该集成 WebRTC 推流到服务器的逻辑
            // 例如使用 WebRTC PeerConnection 推流到 RTMP 服务器
            
            onStateChange?.invoke(UnifyLivePusherState.PUSHING)
            
        } catch (e: Exception) {
            onError?.invoke("Live pusher error: ${e.message}")
            onStateChange?.invoke(UnifyLivePusherState.ERROR)
        }
    }
    
    DisposableEffect(mediaStream) {
        onDispose {
            mediaStream?.getTracks()?.forEach { track ->
                track.stop()
            }
            mediaStream = null
            videoElement = null
        }
    }
}

/**
 * Web 平台 WebRTC 实现
 */
@Composable
actual fun PlatformWebRTC(
    config: UnifyWebRTCConfig,
    onUserJoin: ((String) -> Unit)?,
    onUserLeave: ((String) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    var localStream by remember { mutableStateOf<MediaStream?>(null) }
    var remoteStreams by remember { mutableStateOf<Map<String, MediaStream>>(emptyMap()) }
    
    LaunchedEffect(config.roomId) {
        try {
            // 获取本地媒体流
            val constraints = js("""({
                video: ${config.enableCamera},
                audio: ${config.enableMic}
            })""").unsafeCast<MediaStreamConstraints>()
            
            val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            localStream = stream
            
            // 这里应该集成 WebRTC 信令服务器
            // 例如：Socket.IO、WebSocket 等进行信令交换
            
            // 模拟其他用户加入
            kotlinx.coroutines.delay(2000)
            onUserJoin?.invoke("web_user_1")
            onUserJoin?.invoke("web_user_2")
            
        } catch (e: Exception) {
            onError?.invoke("WebRTC error: ${e.message}")
        }
    }
    
    DisposableEffect(localStream) {
        onDispose {
            localStream?.getTracks()?.forEach { track ->
                track.stop()
            }
            remoteStreams.values.forEach { stream ->
                stream.getTracks().forEach { track ->
                    track.stop()
                }
            }
        }
    }
}
