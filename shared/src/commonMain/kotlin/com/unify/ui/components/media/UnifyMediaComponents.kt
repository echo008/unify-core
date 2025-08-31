package com.unify.ui.components.media

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 直播播放状态
 */
enum class UnifyLivePlayerState {
    IDLE,           // 空闲
    LOADING,        // 加载中
    PLAYING,        // 播放中
    PAUSED,         // 暂停
    ERROR,          // 错误
    BUFFERING,      // 缓冲中
    ENDED           // 结束
}

/**
 * 直播推流状态
 */
enum class UnifyLivePusherState {
    IDLE,           // 空闲
    CONNECTING,     // 连接中
    PUSHING,        // 推流中
    DISCONNECTED,   // 断开连接
    ERROR,          // 错误
    RECONNECTING    // 重连中
}

/**
 * 直播播放模式
 */
enum class UnifyLiveMode {
    LIVE,           // 直播
    RTC             // 实时通话
}

/**
 * 直播播放配置
 */
data class UnifyLivePlayerConfig(
    val src: String,                                    // 直播地址
    val mode: UnifyLiveMode = UnifyLiveMode.LIVE,      // 播放模式
    val autoplay: Boolean = false,                      // 自动播放
    val muted: Boolean = false,                         // 静音
    val orientation: String = "vertical",               // 画面方向
    val objectFit: String = "contain",                 // 填充模式
    val backgroundMute: Boolean = false,                // 后台静音
    val minCache: Float = 1f,                          // 最小缓存时间(秒)
    val maxCache: Float = 3f,                          // 最大缓存时间(秒)
    val soundMode: String = "speaker",                 // 声音输出方式
    val enableProgressGesture: Boolean = false,         // 是否开启进度条手势
    val enablePlayGesture: Boolean = false,            // 是否开启播放手势
    val enableFullScreenGesture: Boolean = false,      // 是否开启全屏手势
    val enableCamera: Boolean = false,                  // 是否显示摄像头
    val enableMic: Boolean = false,                     // 是否显示麦克风
    val enableAgc: Boolean = false,                     // 是否开启音频自动增益
    val enableAns: Boolean = false,                     // 是否开启音频噪声抑制
    val enableEarMonitor: Boolean = false,             // 是否开启耳返
    val enableVolumeEvaluation: Boolean = false,       // 是否开启音量回调
    val volumeEvaluationInterval: Int = 200            // 音量回调间隔(ms)
)

/**
 * 直播推流配置
 */
data class UnifyLivePusherConfig(
    val url: String,                                    // 推流地址
    val mode: UnifyLiveMode = UnifyLiveMode.LIVE,      // 推流模式
    val autopush: Boolean = false,                      // 自动推流
    val muted: Boolean = false,                         // 静音
    val enableCamera: Boolean = true,                   // 开启摄像头
    val autoFocus: Boolean = true,                      // 自动聚焦
    val orientation: String = "vertical",               // 画面方向
    val beauty: Int = 0,                               // 美颜级别
    val whiteness: Int = 0,                            // 美白级别
    val aspect: String = "9:16",                       // 宽高比
    val minBitrate: Int = 200,                         // 最小码率
    val maxBitrate: Int = 1000,                        // 最大码率
    val audioQuality: String = "high",                 // 音频质量
    val devicePosition: String = "front",              // 摄像头位置
    val waitingImage: String = "",                     // 等待画面
    val waitingImageHash: String = "",                 // 等待画面hash
    val zoom: Boolean = false,                          // 调整焦距
    val backgroundMute: Boolean = false,                // 后台静音
    val mirror: Boolean = false,                        // 镜像
    val remoteMirror: Boolean = false,                  // 远端镜像
    val localMirror: String = "auto",                  // 本地镜像
    val audioReverbType: Int = 0,                      // 音频混响类型
    val enableMic: Boolean = true,                      // 开启麦克风
    val enableAgc: Boolean = false,                     // 开启音频自动增益
    val enableAns: Boolean = false,                     // 开启音频噪声抑制
    val audioVolumeType: String = "voicecall",         // 音量类型
    val videoWidth: Int = 360,                         // 视频宽度
    val videoHeight: Int = 640,                        // 视频高度
    val beautyStyle: String = "smooth",                // 美颜风格
    val filter: String = "standard"                    // 滤镜
)

/**
 * WebRTC 配置
 */
data class UnifyWebRTCConfig(
    val roomId: String,                                 // 房间ID
    val userId: String,                                 // 用户ID
    val userSig: String,                               // 用户签名
    val sdkAppId: String,                              // SDK应用ID
    val enableCamera: Boolean = true,                   // 开启摄像头
    val enableMic: Boolean = true,                      // 开启麦克风
    val enableSpeaker: Boolean = true,                  // 开启扬声器
    val enableEarMonitor: Boolean = false,             // 开启耳返
    val enableVolumeEvaluation: Boolean = false,       // 开启音量回调
    val enableCloudRecord: Boolean = false,            // 开启云端录制
    val videoQuality: String = "high",                 // 视频质量
    val audioQuality: String = "high",                 // 音频质量
    val beautyLevel: Int = 0,                          // 美颜级别
    val whiteLevel: Int = 0,                           // 美白级别
    val videoOrientation: String = "portrait"          // 视频方向
)

/**
 * Unify 媒体组件
 * 包含音频、视频、直播推流、直播播放、WebRTC等完整媒体功能
 * 对应微信小程序的 audio、video、live-player、live-pusher、voip-room 等组件
 */

/**
 * 音频播放状态
 */
enum class UnifyAudioState {
    IDLE,       // 空闲
    LOADING,    // 加载中
    PLAYING,    // 播放中
    PAUSED,     // 暂停
    STOPPED,    // 停止
    ERROR       // 错误
}

/**
 * 音频组件配置
 */
data class UnifyAudioConfig(
    val name: String = "",
    val author: String = "",
    val loop: Boolean = false,
    val controls: Boolean = true,
    val poster: String = "",
    val initialTime: Float = 0f,
    val showPlayBtn: Boolean = true,
    val showProgress: Boolean = true,
    val showTime: Boolean = true,
    val showMute: Boolean = true,
    val showVolume: Boolean = true,
    val showSpeed: Boolean = false,
    val playbackRate: Float = 1f,
    val volume: Float = 1f,
    val muted: Boolean = false
)

/**
 * 音频播放器组件
 */
@Composable
fun UnifyAudio(
    src: String,
    modifier: Modifier = Modifier,
    config: UnifyAudioConfig = UnifyAudioConfig(),
    onPlay: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onEnded: (() -> Unit)? = null,
    onTimeUpdate: ((currentTime: Float, duration: Float) -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var audioState by remember { mutableStateOf(UnifyAudioState.IDLE) }
    var currentTime by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }
    var volume by remember { mutableStateOf(config.volume) }
    var isMuted by remember { mutableStateOf(config.muted) }
    var playbackRate by remember { mutableStateOf(config.playbackRate) }
    
    // 模拟音频播放进度
    LaunchedEffect(audioState) {
        if (audioState == UnifyAudioState.PLAYING) {
            while (audioState == UnifyAudioState.PLAYING) {
                delay(1000)
                currentTime += playbackRate
                onTimeUpdate?.invoke(currentTime, duration)
                
                if (currentTime >= duration && duration > 0) {
                    if (config.loop) {
                        currentTime = 0f
                    } else {
                        audioState = UnifyAudioState.STOPPED
                        onEnded?.invoke()
                    }
                }
            }
        }
    }
    
    // 初始化音频时长（模拟）
    LaunchedEffect(src) {
        if (src.isNotEmpty()) {
            audioState = UnifyAudioState.LOADING
            delay(500) // 模拟加载时间
            duration = 180f // 模拟3分钟音频
            currentTime = config.initialTime
            audioState = UnifyAudioState.IDLE
        }
    }
    
    if (config.controls) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = theme.colors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 音频信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 封面图片
                    if (config.poster.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            UnifyIcon(
                                icon = Icons.Default.MusicNote,
                                size = UnifyIconSize.MEDIUM,
                                tint = theme.colors.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    // 音频标题和作者
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (config.name.isNotEmpty()) {
                            UnifyText(
                                text = config.name,
                                variant = UnifyTextVariant.BODY_LARGE,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        if (config.author.isNotEmpty()) {
                            UnifyText(
                                text = config.author,
                                variant = UnifyTextVariant.BODY_SMALL,
                                color = theme.colors.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // 播放状态指示
                    if (audioState == UnifyAudioState.LOADING) {
                        UnifyLoading(
                            variant = UnifyLoadingVariant.CIRCULAR,
                            size = UnifyLoadingSize.SMALL
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 控制栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 播放/暂停按钮
                    if (config.showPlayBtn) {
                        IconButton(
                            onClick = {
                                when (audioState) {
                                    UnifyAudioState.IDLE, UnifyAudioState.STOPPED, UnifyAudioState.PAUSED -> {
                                        audioState = UnifyAudioState.PLAYING
                                        onPlay?.invoke()
                                    }
                                    UnifyAudioState.PLAYING -> {
                                        audioState = UnifyAudioState.PAUSED
                                        onPause?.invoke()
                                    }
                                    else -> {}
                                }
                            }
                        ) {
                            UnifyIcon(
                                icon = when (audioState) {
                                    UnifyAudioState.PLAYING -> Icons.Default.Pause
                                    else -> Icons.Default.PlayArrow
                                },
                                size = UnifyIconSize.LARGE,
                                tint = theme.colors.primary
                            )
                        }
                    }
                    
                    // 时间显示
                    if (config.showTime) {
                        UnifyText(
                            text = formatTime(currentTime),
                            variant = UnifyTextVariant.CAPTION,
                            color = theme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // 进度条
                    if (config.showProgress) {
                        Slider(
                            value = if (duration > 0) currentTime / duration else 0f,
                            onValueChange = { progress ->
                                currentTime = progress * duration
                                onTimeUpdate?.invoke(currentTime, duration)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = theme.colors.primary,
                                activeTrackColor = theme.colors.primary,
                                inactiveTrackColor = theme.colors.outline
                            )
                        )
                    }
                    
                    // 总时长
                    if (config.showTime) {
                        UnifyText(
                            text = formatTime(duration),
                            variant = UnifyTextVariant.CAPTION,
                            color = theme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // 静音按钮
                    if (config.showMute) {
                        IconButton(
                            onClick = { isMuted = !isMuted }
                        ) {
                            UnifyIcon(
                                icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                size = UnifyIconSize.MEDIUM,
                                tint = theme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // 音量控制
                if (config.showVolume && !isMuted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.VolumeDown,
                            size = UnifyIconSize.SMALL,
                            tint = theme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = theme.colors.primary,
                                activeTrackColor = theme.colors.primary,
                                inactiveTrackColor = theme.colors.outline
                            )
                        )
                        
                        UnifyIcon(
                            icon = Icons.Default.VolumeUp,
                            size = UnifyIconSize.SMALL,
                            tint = theme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // 播放速度控制
                if (config.showSpeed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                            FilterChip(
                                onClick = { playbackRate = speed },
                                label = {
                                    UnifyText(
                                        text = "${speed}x",
                                        variant = UnifyTextVariant.CAPTION
                                    )
                                },
                                selected = playbackRate == speed
                            )
                        }
                    }
                }
            }
        }
    } else {
        // 简单的音频播放器（无控制界面）
        Box(
            modifier = modifier.semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
        ) {
            // 音频播放逻辑，但不显示UI
        }
    }
}

/**
 * 视频播放状态
 */
enum class UnifyVideoState {
    IDLE,           // 空闲
    LOADING,        // 加载中
    PLAYING,        // 播放中
    PAUSED,         // 暂停
    STOPPED,        // 停止
    BUFFERING,      // 缓冲中
    ERROR,          // 错误
    ENDED           // 播放结束
}

/**
 * 视频组件配置
 */
data class UnifyVideoConfig(
    val poster: String = "",
    val controls: Boolean = true,
    val autoplay: Boolean = false,
    val loop: Boolean = false,
    val muted: Boolean = false,
    val initialTime: Float = 0f,
    val duration: Float = 0f,
    val danmuList: List<UnifyDanmu> = emptyList(),
    val danmuBtn: Boolean = false,
    val enableDanmu: Boolean = false,
    val pageGesture: Boolean = false,
    val direction: Int = 0, // 0: 竖屏, 90: 横屏
    val showProgress: Boolean = true,
    val showFullscreenBtn: Boolean = true,
    val showPlayBtn: Boolean = true,
    val showCenterPlayBtn: Boolean = true,
    val showLoading: Boolean = true,
    val enableProgressGesture: Boolean = true,
    val objectFit: String = "contain", // contain, fill, cover
    val playBtnPosition: String = "bottom", // bottom, center
    val preRollUnitId: String = "",
    val postRollUnitId: String = "",
    val vslideGesture: Boolean = false,
    val vslideGestureInFullscreen: Boolean = true,
    val adUnitId: String = "",
    val enablePlayGesture: Boolean = false,
    val autoPauseIfNavigate: Boolean = true,
    val autoPauseIfOpenNative: Boolean = true,
    val showMuteBtn: Boolean = false,
    val title: String = "",
    val playStrategy: Int = 0,
    val header: Map<String, String> = emptyMap(),
    val httpCache: Boolean = true,
    val playWithMuteBtn: Boolean = false,
    val pictureInPictureMode: String = "",
    val pictureInPictureShowProgress: Boolean = false,
    val enableAutoRotation: Boolean = false,
    val showScreenLockButton: Boolean = false,
    val showSnapshotButton: Boolean = false,
    val showBackgroundPlaybackButton: Boolean = false,
    val backgroundPoster: String = "",
    val referrerPolicy: String = "origin"
)

/**
 * 弹幕数据
 */
data class UnifyDanmu(
    val text: String,
    val color: Color = Color.White,
    val time: Float
)

/**
 * 视频播放器组件
 */
@Composable
fun UnifyVideo(
    src: String,
    modifier: Modifier = Modifier,
    config: UnifyVideoConfig = UnifyVideoConfig(),
    onPlay: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onEnded: (() -> Unit)? = null,
    onTimeUpdate: ((currentTime: Float, duration: Float) -> Unit)? = null,
    onFullscreenChange: ((fullscreen: Boolean) -> Unit)? = null,
    onWaiting: (() -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    onProgress: ((buffered: Float) -> Unit)? = null,
    onLoadedMetadata: (() -> Unit)? = null,
    onLoadStart: (() -> Unit)? = null,
    onSeeking: (() -> Unit)? = null,
    onSeeked: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var videoState by remember { mutableStateOf(UnifyVideoState.IDLE) }
    var currentTime by remember { mutableStateOf(config.initialTime) }
    var duration by remember { mutableStateOf(config.duration) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(config.controls) }
    var isMuted by remember { mutableStateOf(config.muted) }
    var volume by remember { mutableStateOf(1f) }
    var bufferedProgress by remember { mutableStateOf(0f) }
    var enableDanmu by remember { mutableStateOf(config.enableDanmu) }
    
    // 控制栏自动隐藏
    var controlsVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(controlsVisible) {
        if (controlsVisible && videoState == UnifyVideoState.PLAYING) {
            delay(3000)
            controlsVisible = false
        }
    }
    
    // 模拟视频播放进度
    LaunchedEffect(videoState) {
        if (videoState == UnifyVideoState.PLAYING) {
            while (videoState == UnifyVideoState.PLAYING) {
                delay(1000)
                currentTime += 1f
                onTimeUpdate?.invoke(currentTime, duration)
                
                if (currentTime >= duration && duration > 0) {
                    if (config.loop) {
                        currentTime = 0f
                    } else {
                        videoState = UnifyVideoState.ENDED
                        onEnded?.invoke()
                    }
                }
            }
        }
    }
    
    // 初始化视频
    LaunchedEffect(src) {
        if (src.isNotEmpty()) {
            videoState = UnifyVideoState.LOADING
            onLoadStart?.invoke()
            delay(1000) // 模拟加载时间
            duration = 300f // 模拟5分钟视频
            videoState = if (config.autoplay) UnifyVideoState.PLAYING else UnifyVideoState.IDLE
            onLoadedMetadata?.invoke()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .clickable {
                controlsVisible = !controlsVisible
            }
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        // 视频内容区域
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 封面图片或视频画面
            if (videoState == UnifyVideoState.IDLE && config.poster.isNotEmpty()) {
                // 显示封面
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(theme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyIcon(
                        icon = Icons.Default.PlayArrow,
                        size = UnifyIconSize.EXTRA_LARGE,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                // 模拟视频画面
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyText(
                        text = "视频内容",
                        color = Color.White,
                        variant = UnifyTextVariant.BODY_LARGE
                    )
                }
            }
            
            // 加载指示器
            if (videoState == UnifyVideoState.LOADING || videoState == UnifyVideoState.BUFFERING) {
                if (config.showLoading) {
                    UnifyLoading(
                        variant = UnifyLoadingVariant.CIRCULAR,
                        size = UnifyLoadingSize.LARGE
                    )
                }
            }
            
            // 中央播放按钮
            if (config.showCenterPlayBtn && 
                (videoState == UnifyVideoState.IDLE || videoState == UnifyVideoState.PAUSED || videoState == UnifyVideoState.ENDED)) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(32.dp)
                        )
                        .clickable {
                            when (videoState) {
                                UnifyVideoState.IDLE, UnifyVideoState.PAUSED, UnifyVideoState.ENDED -> {
                                    videoState = UnifyVideoState.PLAYING
                                    onPlay?.invoke()
                                }
                                else -> {}
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    UnifyIcon(
                        icon = Icons.Default.PlayArrow,
                        size = UnifyIconSize.LARGE,
                        tint = Color.White
                    )
                }
            }
        }
        
        // 弹幕层
        if (enableDanmu && config.danmuList.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // 这里应该实现弹幕渲染逻辑
                // 简化实现，显示一些示例弹幕
                config.danmuList.take(3).forEachIndexed { index, danmu ->
                    if (currentTime >= danmu.time && currentTime <= danmu.time + 5) {
                        UnifyText(
                            text = danmu.text,
                            color = danmu.color,
                            variant = UnifyTextVariant.BODY_SMALL,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(y = (50 + index * 30).dp)
                        )
                    }
                }
            }
        }
        
        // 控制栏
        if (showControls && controlsVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // 进度条
                if (config.showProgress) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnifyText(
                            text = formatTime(currentTime),
                            color = Color.White,
                            variant = UnifyTextVariant.CAPTION
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            // 缓冲进度
                            LinearProgressIndicator(
                                progress = { bufferedProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White.copy(alpha = 0.3f),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            
                            // 播放进度
                            Slider(
                                value = if (duration > 0) currentTime / duration else 0f,
                                onValueChange = { progress ->
                                    currentTime = progress * duration
                                    onTimeUpdate?.invoke(currentTime, duration)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.colors.primary,
                                    activeTrackColor = theme.colors.primary,
                                    inactiveTrackColor = Color.Transparent
                                )
                            )
                        }
                        
                        UnifyText(
                            text = formatTime(duration),
                            color = Color.White,
                            variant = UnifyTextVariant.CAPTION
                        )
                    }
                }
                
                // 控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        // 播放/暂停按钮
                        if (config.showPlayBtn) {
                            IconButton(
                                onClick = {
                                    when (videoState) {
                                        UnifyVideoState.IDLE, UnifyVideoState.PAUSED, UnifyVideoState.ENDED -> {
                                            videoState = UnifyVideoState.PLAYING
                                            onPlay?.invoke()
                                        }
                                        UnifyVideoState.PLAYING -> {
                                            videoState = UnifyVideoState.PAUSED
                                            onPause?.invoke()
                                        }
                                        else -> {}
                                    }
                                }
                            ) {
                                UnifyIcon(
                                    icon = when (videoState) {
                                        UnifyVideoState.PLAYING -> Icons.Default.Pause
                                        else -> Icons.Default.PlayArrow
                                    },
                                    size = UnifyIconSize.MEDIUM,
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // 静音按钮
                        if (config.showMuteBtn) {
                            IconButton(
                                onClick = { isMuted = !isMuted }
                            ) {
                                UnifyIcon(
                                    icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    size = UnifyIconSize.MEDIUM,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    Row {
                        // 弹幕按钮
                        if (config.danmuBtn) {
                            IconButton(
                                onClick = { enableDanmu = !enableDanmu }
                            ) {
                                UnifyIcon(
                                    icon = Icons.Default.ChatBubbleOutline,
                                    size = UnifyIconSize.MEDIUM,
                                    tint = if (enableDanmu) theme.colors.primary else Color.White
                                )
                            }
                        }
                        
                        // 全屏按钮
                        if (config.showFullscreenBtn) {
                            IconButton(
                                onClick = {
                                    isFullscreen = !isFullscreen
                                    onFullscreenChange?.invoke(isFullscreen)
                                }
                            ) {
                                UnifyIcon(
                                    icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    size = UnifyIconSize.MEDIUM,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 标题栏（全屏时显示）
        if (isFullscreen && config.title.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                UnifyText(
                    text = config.title,
                    color = Color.White,
                    variant = UnifyTextVariant.TITLE_MEDIUM,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 时间格式化函数
 */
private fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
