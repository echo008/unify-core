package com.unify.ui.components.media

import androidx.compose.runtime.*
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject

/**
 * iOS 平台直播播放器实现
 */
@Composable
actual fun PlatformLivePlayer(
    config: UnifyLivePlayerConfig,
    onStateChange: ((UnifyLivePlayerState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    var player by remember { mutableStateOf<AVPlayer?>(null) }
    var playerViewController by remember { mutableStateOf<AVPlayerViewController?>(null) }
    
    DisposableEffect(config.src) {
        val url = NSURL.URLWithString(config.src)
        if (url != null) {
            val avPlayer = AVPlayer(uRL = url)
            val controller = AVPlayerViewController().apply {
                this.player = avPlayer
                showsPlaybackControls = true
            }
            
            player = avPlayer
            playerViewController = controller
            
            // 监听播放状态
            avPlayer.addObserver(
                observer = object : NSObject() {},
                forKeyPath = "status",
                options = NSKeyValueObservingOptionNew,
                context = null
            )
            
            if (config.autoplay) {
                avPlayer.play()
                onStateChange?.invoke(UnifyLivePlayerState.PLAYING)
            }
        } else {
            onError?.invoke("Invalid URL: ${config.src}")
            onStateChange?.invoke(UnifyLivePlayerState.ERROR)
        }
        
        onDispose {
            player?.pause()
            player = null
            playerViewController = null
        }
    }
    
    playerViewController?.let { controller ->
        UIKitView(
            factory = { controller.view },
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        )
    }
}

/**
 * iOS 平台直播推流器实现
 */
@Composable
actual fun PlatformLivePusher(
    config: UnifyLivePusherConfig,
    onStateChange: ((UnifyLivePusherState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    var captureSession by remember { mutableStateOf<AVCaptureSession?>(null) }
    var previewLayer by remember { mutableStateOf<AVCaptureVideoPreviewLayer?>(null) }
    
    DisposableEffect(config.url) {
        val session = AVCaptureSession().apply {
            sessionPreset = AVCaptureSessionPresetHigh
        }
        
        // 配置摄像头输入
        val videoDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (videoDevice != null) {
            try {
                val videoInput = AVCaptureDeviceInput.deviceInputWithDevice(videoDevice, error = null)
                if (videoInput != null && session.canAddInput(videoInput)) {
                    session.addInput(videoInput)
                }
                
                // 配置音频输入
                val audioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
                if (audioDevice != null) {
                    val audioInput = AVCaptureDeviceInput.deviceInputWithDevice(audioDevice, error = null)
                    if (audioInput != null && session.canAddInput(audioInput)) {
                        session.addInput(audioInput)
                    }
                }
                
                // 配置输出
                val videoOutput = AVCaptureVideoDataOutput()
                if (session.canAddOutput(videoOutput)) {
                    session.addOutput(videoOutput)
                }
                
                captureSession = session
                
                if (config.autopush) {
                    onStateChange?.invoke(UnifyLivePusherState.CONNECTING)
                    session.startRunning()
                    onStateChange?.invoke(UnifyLivePusherState.PUSHING)
                }
                
            } catch (e: Exception) {
                onError?.invoke("Failed to setup camera: ${e.message}")
                onStateChange?.invoke(UnifyLivePusherState.ERROR)
            }
        } else {
            onError?.invoke("No camera device available")
            onStateChange?.invoke(UnifyLivePusherState.ERROR)
        }
        
        onDispose {
            captureSession?.stopRunning()
            captureSession = null
            previewLayer = null
        }
    }
    
    captureSession?.let { session ->
        UIKitView(
            factory = {
                val view = UIView()
                val preview = AVCaptureVideoPreviewLayer(session = session).apply {
                    videoGravity = AVLayerVideoGravityResizeAspectFill
                    frame = view.bounds
                }
                view.layer.addSublayer(preview)
                previewLayer = preview
                view
            },
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        )
    }
}

/**
 * iOS 平台 WebRTC 实现
 */
@Composable
actual fun PlatformWebRTC(
    config: UnifyWebRTCConfig,
    onUserJoin: ((String) -> Unit)?,
    onUserLeave: ((String) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(config.roomId) {
        try {
            // 集成 WebRTC iOS SDK
            // 例如：腾讯云TRTC iOS SDK、声网Agora iOS SDK等
            
            kotlinx.coroutines.delay(1000)
            onUserJoin?.invoke("ios_user_1")
            onUserJoin?.invoke("ios_user_2")
            
        } catch (e: Exception) {
            onError?.invoke("WebRTC iOS error: ${e.message}")
        }
    }
    
    UIKitView(
        factory = {
            // 创建 WebRTC 视频渲染容器
            UIView().apply {
                backgroundColor = UIColor.blackColor
                // 添加本地和远端视频渲染视图
            }
        },
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    )
}
