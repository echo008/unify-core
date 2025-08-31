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
 * 直播播放器组件
 */
@Composable
fun UnifyLivePlayer(
    config: UnifyLivePlayerConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyLivePlayerState) -> Unit)? = null,
    onFullScreen: (() -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var playerState by remember { mutableStateOf(UnifyLivePlayerState.IDLE) }
    var isFullScreen by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1f) }
    var isMuted by remember { mutableStateOf(config.muted) }
    
    LaunchedEffect(config.src) {
        if (config.autoplay && config.src.isNotEmpty()) {
            playerState = UnifyLivePlayerState.LOADING
            delay(1000) // 模拟加载
            playerState = UnifyLivePlayerState.PLAYING
            onStateChange?.invoke(playerState)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (config.orientation == "horizontal") 16f/9f else 9f/16f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role = Role.Button
            }
    ) {
        // 直播画面区域
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (playerState) {
                UnifyLivePlayerState.IDLE -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(
                            text = "点击播放直播",
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    }
                }
                UnifyLivePlayerState.LOADING, UnifyLivePlayerState.BUFFERING -> {
                    CircularProgressIndicator(
                        color = theme.colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                UnifyLivePlayerState.PLAYING -> {
                    // 直播画面（模拟）
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1976D2),
                                        Color(0xFF42A5F5)
                                    )
                                )
                            )
                    ) {
                        UnifyText(
                            text = "直播画面",
                            color = Color.White,
                            variant = UnifyTextVariant.H6,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        
                        // 直播标识
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .background(
                                    Color.Red,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            UnifyText(
                                text = "LIVE",
                                color = Color.White,
                                variant = UnifyTextVariant.CAPTION,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                UnifyLivePlayerState.ERROR -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "错误",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(
                            text = "直播加载失败",
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    }
                }
                else -> {}
            }
        }
        
        // 控制层
        if (playerState == UnifyLivePlayerState.PLAYING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            ) {
                // 底部控制栏
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 播放/暂停按钮
                        IconButton(
                            onClick = {
                                playerState = if (playerState == UnifyLivePlayerState.PLAYING) {
                                    UnifyLivePlayerState.PAUSED
                                } else {
                                    UnifyLivePlayerState.PLAYING
                                }
                                onStateChange?.invoke(playerState)
                            }
                        ) {
                            Icon(
                                imageVector = if (playerState == UnifyLivePlayerState.PLAYING) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (playerState == UnifyLivePlayerState.PLAYING) "暂停" else "播放",
                                tint = Color.White
                            )
                        }
                        
                        // 音量控制
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                            }
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "取消静音" else "静音",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // 全屏按钮
                    IconButton(
                        onClick = {
                            isFullScreen = !isFullScreen
                            onFullScreen?.invoke()
                        }
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullScreen) "退出全屏" else "全屏",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // 点击播放
        if (playerState == UnifyLivePlayerState.IDLE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        playerState = UnifyLivePlayerState.LOADING
                        onStateChange?.invoke(playerState)
                    }
            )
        }
    }
}

/**
 * 直播推流器组件
 */
@Composable
fun UnifyLivePusher(
    config: UnifyLivePusherConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyLivePusherState) -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var pusherState by remember { mutableStateOf(UnifyLivePusherState.IDLE) }
    var isCameraEnabled by remember { mutableStateOf(config.enableCamera) }
    var isMicEnabled by remember { mutableStateOf(config.enableMic) }
    var beautyLevel by remember { mutableStateOf(config.beauty) }
    var whiteLevel by remember { mutableStateOf(config.whiteness) }
    
    LaunchedEffect(config.url) {
        if (config.autopush && config.url.isNotEmpty()) {
            pusherState = UnifyLivePusherState.CONNECTING
            delay(2000) // 模拟连接
            pusherState = UnifyLivePusherState.PUSHING
            onStateChange?.invoke(pusherState)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(if (config.orientation == "horizontal") 16f/9f else 9f/16f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role = Role.Button
            }
    ) {
        // 推流画面区域
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (pusherState) {
                UnifyLivePusherState.IDLE -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "开始推流",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(
                            text = "点击开始推流",
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    }
                }
                UnifyLivePusherState.CONNECTING, UnifyLivePusherState.RECONNECTING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = theme.colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(
                            text = if (pusherState == UnifyLivePusherState.CONNECTING) "连接中..." else "重连中...",
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    }
                }
                UnifyLivePusherState.PUSHING -> {
                    // 推流画面（模拟摄像头预览）
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF2E7D32)
                                    )
                                )
                            )
                    ) {
                        UnifyText(
                            text = "推流中",
                            color = Color.White,
                            variant = UnifyTextVariant.H6,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        
                        // 推流状态指示
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .background(
                                    Color.Green,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            UnifyText(
                                text = "推流中",
                                color = Color.White,
                                variant = UnifyTextVariant.CAPTION,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                UnifyLivePusherState.ERROR -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "错误",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(
                            text = "推流失败",
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    }
                }
                else -> {}
            }
        }
        
        // 控制层
        if (pusherState == UnifyLivePusherState.PUSHING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                // 顶部控制栏
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 摄像头切换
                    IconButton(
                        onClick = {
                            // 切换前后摄像头
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "切换摄像头",
                            tint = Color.White
                        )
                    }
                    
                    // 美颜控制
                    IconButton(
                        onClick = {
                            // 美颜设置
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "美颜",
                            tint = Color.White
                        )
                    }
                }
                
                // 底部控制栏
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 摄像头开关
                        IconButton(
                            onClick = {
                                isCameraEnabled = !isCameraEnabled
                            }
                        ) {
                            Icon(
                                imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = if (isCameraEnabled) "关闭摄像头" else "开启摄像头",
                                tint = if (isCameraEnabled) Color.White else Color.Red
                            )
                        }
                        
                        // 麦克风开关
                        IconButton(
                            onClick = {
                                isMicEnabled = !isMicEnabled
                            }
                        ) {
                            Icon(
                                imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = if (isMicEnabled) "关闭麦克风" else "开启麦克风",
                                tint = if (isMicEnabled) Color.White else Color.Red
                            )
                        }
                    }
                    
                    // 停止推流按钮
                    IconButton(
                        onClick = {
                            pusherState = UnifyLivePusherState.DISCONNECTED
                            onStateChange?.invoke(pusherState)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止推流",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
        
        // 点击开始推流
        if (pusherState == UnifyLivePusherState.IDLE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        pusherState = UnifyLivePusherState.CONNECTING
                        onStateChange?.invoke(pusherState)
                    }
            )
        }
    }
}

/**
 * WebRTC 视频通话组件
 */
@Composable
fun UnifyWebRTC(
    config: UnifyWebRTCConfig,
    modifier: Modifier = Modifier,
    onUserJoin: ((userId: String) -> Unit)? = null,
    onUserLeave: ((userId: String) -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isConnected by remember { mutableStateOf(false) }
    var remoteUsers by remember { mutableStateOf(listOf<String>()) }
    var isCameraEnabled by remember { mutableStateOf(config.enableCamera) }
    var isMicEnabled by remember { mutableStateOf(config.enableMic) }
    var isSpeakerEnabled by remember { mutableStateOf(config.enableSpeaker) }
    
    LaunchedEffect(config.roomId) {
        if (config.roomId.isNotEmpty()) {
            delay(2000) // 模拟连接
            isConnected = true
            // 模拟其他用户加入
            delay(3000)
            remoteUsers = listOf("user1", "user2")
            onUserJoin?.invoke("user1")
            onUserJoin?.invoke("user2")
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f/9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        if (isConnected) {
            // 多人视频通话界面
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 本地视频
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UnifyText(
                            text = "我",
                            color = Color.White,
                            variant = UnifyTextVariant.H6
                        )
                        
                        // 本地视频状态指示
                        if (!isCameraEnabled) {
                            Icon(
                                imageVector = Icons.Default.VideocamOff,
                                contentDescription = "摄像头已关闭",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            )
                        }
                        
                        if (!isMicEnabled) {
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "麦克风已关闭",
                                tint = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                
                // 远端用户视频
                items(remoteUsers) { userId ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UnifyText(
                            text = userId,
                            color = Color.White,
                            variant = UnifyTextVariant.H6
                        )
                    }
                }
            }
            
            // 控制栏
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.8f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 摄像头控制
                IconButton(
                    onClick = { isCameraEnabled = !isCameraEnabled }
                ) {
                    Icon(
                        imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = if (isCameraEnabled) "关闭摄像头" else "开启摄像头",
                        tint = if (isCameraEnabled) Color.White else Color.Red
                    )
                }
                
                // 麦克风控制
                IconButton(
                    onClick = { isMicEnabled = !isMicEnabled }
                ) {
                    Icon(
                        imageVector = if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = if (isMicEnabled) "关闭麦克风" else "开启麦克风",
                        tint = if (isMicEnabled) Color.White else Color.Red
                    )
                }
                
                // 扬声器控制
                IconButton(
                    onClick = { isSpeakerEnabled = !isSpeakerEnabled }
                ) {
                    Icon(
                        imageVector = if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (isSpeakerEnabled) "关闭扬声器" else "开启扬声器",
                        tint = if (isSpeakerEnabled) Color.White else Color.Red
                    )
                }
                
                // 挂断
                IconButton(
                    onClick = {
                        isConnected = false
                        remoteUsers = emptyList()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "挂断",
                        tint = Color.Red
                    )
                }
            }
        } else {
            // 连接中状态
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = theme.colors.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                UnifyText(
                    text = "正在连接房间...",
                    color = Color.White,
                    variant = UnifyTextVariant.BODY_LARGE
                )
            }
        }
    }
}
