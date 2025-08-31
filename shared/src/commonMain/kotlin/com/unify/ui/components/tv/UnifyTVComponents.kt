package com.unify.ui.components.tv

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay

/**
 * TV遥控器按键
 */
enum class UnifyTVKey {
    UP, DOWN, LEFT, RIGHT,      // 方向键
    OK, BACK, HOME, MENU,       // 功能键
    VOLUME_UP, VOLUME_DOWN,     // 音量键
    CHANNEL_UP, CHANNEL_DOWN,   // 频道键
    POWER, MUTE,               // 电源和静音
    NUMBER_0, NUMBER_1, NUMBER_2, NUMBER_3, NUMBER_4,
    NUMBER_5, NUMBER_6, NUMBER_7, NUMBER_8, NUMBER_9,  // 数字键
    RED, GREEN, YELLOW, BLUE,   // 彩色功能键
    PLAY, PAUSE, STOP,         // 播放控制
    FAST_FORWARD, REWIND,      // 快进快退
    RECORD, INFO, GUIDE        // 录制、信息、节目指南
}

/**
 * TV焦点状态
 */
data class UnifyTVFocusState(
    val isFocused: Boolean = false,
    val isSelected: Boolean = false,
    val focusScale: Float = 1f,
    val focusColor: Color = Color.White
)

/**
 * TV遥控器配置
 */
data class UnifyTVRemoteConfig(
    val enableVoiceControl: Boolean = true,
    val enableGesture: Boolean = true,
    val enableKeyboard: Boolean = true,
    val focusAnimationDuration: Int = 200,
    val focusScale: Float = 1.1f,
    val focusElevation: Dp = 8.dp,
    val autoFocus: Boolean = true,
    val keyRepeatDelay: Long = 500L,
    val keyRepeatInterval: Long = 100L
)

/**
 * TV可焦点组件包装器
 */
@Composable
fun UnifyTVFocusable(
    modifier: Modifier = Modifier,
    focusState: UnifyTVFocusState = UnifyTVFocusState(),
    onFocusChange: ((Boolean) -> Unit)? = null,
    onKeyEvent: ((KeyEvent) -> Boolean)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable (UnifyTVFocusState) -> Unit
) {
    val theme = LocalUnifyTheme.current
    val focusRequester = remember { FocusRequester() }
    var currentFocusState by remember { mutableStateOf(focusState) }
    
    val scale by animateFloatAsState(
        targetValue = if (currentFocusState.isFocused) 1.1f else 1f,
        animationSpec = tween(200),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusInfo ->
                currentFocusState = currentFocusState.copy(isFocused = focusInfo.isFocused)
                onFocusChange?.invoke(focusInfo.isFocused)
            }
            .onKeyEvent { keyEvent ->
                onKeyEvent?.invoke(keyEvent) ?: false
            }
            .clickable { onClick?.invoke() }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (currentFocusState.isFocused) {
                    Modifier.border(
                        width = 2.dp,
                        color = theme.colors.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        content(currentFocusState)
    }
}

/**
 * TV网格菜单组件
 */
@Composable
fun UnifyTVGrid(
    items: List<UnifyTVGridItem>,
    columns: Int = 4,
    modifier: Modifier = Modifier,
    onItemClick: ((UnifyTVGridItem) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var selectedIndex by remember { mutableStateOf(0) }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(items) { index, item ->
            UnifyTVFocusable(
                onKeyEvent = { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionUp -> {
                                if (index >= columns) {
                                    selectedIndex = index - columns
                                    true
                                } else false
                            }
                            Key.DirectionDown -> {
                                if (index + columns < items.size) {
                                    selectedIndex = index + columns
                                    true
                                } else false
                            }
                            Key.DirectionLeft -> {
                                if (index % columns > 0) {
                                    selectedIndex = index - 1
                                    true
                                } else false
                            }
                            Key.DirectionRight -> {
                                if (index % columns < columns - 1 && index + 1 < items.size) {
                                    selectedIndex = index + 1
                                    true
                                } else false
                            }
                            Key.Enter, Key.DirectionCenter -> {
                                onItemClick?.invoke(item)
                                true
                            }
                            else -> false
                        }
                    } else false
                },
                onClick = { onItemClick?.invoke(item) }
            ) { focusState ->
                TVGridItemCard(
                    item = item,
                    focusState = focusState
                )
            }
        }
    }
}

/**
 * TV网格项目数据
 */
data class UnifyTVGridItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector? = null,
    val imageUrl: String = "",
    val badge: String = "",
    val isNew: Boolean = false,
    val isLocked: Boolean = false
)

/**
 * TV网格项目卡片
 */
@Composable
private fun TVGridItemCard(
    item: UnifyTVGridItem,
    focusState: UnifyTVFocusState,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (focusState.isFocused) theme.colors.primaryContainer else theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (focusState.isFocused) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 图标或图片
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (focusState.isFocused) theme.colors.onPrimaryContainer else theme.colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    // 模拟图片
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (focusState.isFocused) theme.colors.onPrimaryContainer else theme.colors.primary,
                                RoundedCornerShape(8.dp)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 标题
                UnifyText(
                    text = item.title,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = if (focusState.isFocused) theme.colors.onPrimaryContainer else theme.colors.onSurface
                )
                
                // 副标题
                if (item.subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    UnifyText(
                        text = item.subtitle,
                        variant = UnifyTextVariant.CAPTION,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        color = if (focusState.isFocused) theme.colors.onPrimaryContainer.copy(alpha = 0.7f) else theme.colors.onSurfaceVariant
                    )
                }
            }
            
            // 徽章和状态指示器
            if (item.badge.isNotEmpty() || item.isNew || item.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    when {
                        item.isLocked -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "已锁定",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        item.isNew -> {
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                UnifyText(
                                    text = "NEW",
                                    variant = UnifyTextVariant.CAPTION,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        item.badge.isNotEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .background(theme.colors.secondary, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                UnifyText(
                                    text = item.badge,
                                    variant = UnifyTextVariant.CAPTION,
                                    color = theme.colors.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TV媒体播放器组件
 */
@Composable
fun UnifyTVMediaPlayer(
    title: String,
    subtitle: String = "",
    duration: Long = 0L,
    modifier: Modifier = Modifier,
    onPlay: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onSeek: ((Long) -> Unit)? = null,
    onVolumeChange: ((Float) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var volume by remember { mutableStateOf(0.5f) }
    var showControls by remember { mutableStateOf(true) }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && currentPosition < duration) {
                delay(1000)
                currentPosition += 1000
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        // 视频播放区域（模拟）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1976D2),
                            Color(0xFF0D47A1)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isPlaying) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "播放",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(120.dp)
                )
            } else {
                UnifyText(
                    text = "正在播放",
                    variant = UnifyTextVariant.H4,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // 控制层
        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                // 顶部信息栏
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(32.dp)
                ) {
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.H5,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        UnifyText(
                            text = subtitle,
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // 中央播放控制
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 快退
                    UnifyTVFocusable(
                        onClick = {
                            currentPosition = maxOf(0L, currentPosition - 10000L)
                            onSeek?.invoke(currentPosition)
                        }
                    ) { focusState ->
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "快退10秒",
                            tint = if (focusState.isFocused) theme.colors.primary else Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    // 播放/暂停
                    UnifyTVFocusable(
                        onClick = {
                            isPlaying = !isPlaying
                            if (isPlaying) onPlay?.invoke() else onPause?.invoke()
                        }
                    ) { focusState ->
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = if (focusState.isFocused) theme.colors.primary else Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    // 快进
                    UnifyTVFocusable(
                        onClick = {
                            currentPosition = minOf(duration, currentPosition + 10000L)
                            onSeek?.invoke(currentPosition)
                        }
                    ) { focusState ->
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "快进10秒",
                            tint = if (focusState.isFocused) theme.colors.primary else Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                // 底部进度和控制栏
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    // 进度条
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UnifyText(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_SMALL
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        LinearProgressIndicator(
                            progress = { if (duration > 0) currentPosition.toFloat() / duration else 0f },
                            modifier = Modifier.weight(1f),
                            color = theme.colors.primary,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        UnifyText(
                            text = formatTime(duration),
                            color = Color.White,
                            variant = UnifyTextVariant.BODY_SMALL
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 控制按钮行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧控制
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            UnifyTVFocusable(
                                onClick = { /* 上一个 */ }
                            ) { focusState ->
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "上一个",
                                    tint = if (focusState.isFocused) theme.colors.primary else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            UnifyTVFocusable(
                                onClick = { /* 下一个 */ }
                            ) { focusState ->
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "下一个",
                                    tint = if (focusState.isFocused) theme.colors.primary else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        // 右侧控制
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 音量控制
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "音量",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Slider(
                                value = volume,
                                onValueChange = { 
                                    volume = it
                                    onVolumeChange?.invoke(it)
                                },
                                modifier = Modifier.width(120.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.colors.primary,
                                    activeTrackColor = theme.colors.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            
                            // 设置
                            UnifyTVFocusable(
                                onClick = { /* 设置 */ }
                            ) { focusState ->
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "设置",
                                    tint = if (focusState.isFocused) theme.colors.primary else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TV遥控器组件
 */
@Composable
fun UnifyTVRemote(
    modifier: Modifier = Modifier,
    config: UnifyTVRemoteConfig = UnifyTVRemoteConfig(),
    onKeyPress: ((UnifyTVKey) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .width(200.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 电源按钮
            TVRemoteButton(
                icon = Icons.Default.Power,
                label = "电源",
                color = Color.Red,
                onClick = { onKeyPress?.invoke(UnifyTVKey.POWER) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 方向键
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TVRemoteButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "上",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.UP) }
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TVRemoteButton(
                        icon = Icons.Default.KeyboardArrowLeft,
                        label = "左",
                        onClick = { onKeyPress?.invoke(UnifyTVKey.LEFT) }
                    )
                    
                    TVRemoteButton(
                        icon = Icons.Default.Circle,
                        label = "确定",
                        color = theme.colors.primary,
                        onClick = { onKeyPress?.invoke(UnifyTVKey.OK) }
                    )
                    
                    TVRemoteButton(
                        icon = Icons.Default.KeyboardArrowRight,
                        label = "右",
                        onClick = { onKeyPress?.invoke(UnifyTVKey.RIGHT) }
                    )
                }
                
                TVRemoteButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "下",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.DOWN) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 功能按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TVRemoteButton(
                    icon = Icons.Default.ArrowBack,
                    label = "返回",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.BACK) }
                )
                
                TVRemoteButton(
                    icon = Icons.Default.Home,
                    label = "主页",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.HOME) }
                )
                
                TVRemoteButton(
                    icon = Icons.Default.Menu,
                    label = "菜单",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.MENU) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 音量控制
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TVRemoteButton(
                    icon = Icons.Default.VolumeDown,
                    label = "音量-",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.VOLUME_DOWN) }
                )
                
                TVRemoteButton(
                    icon = Icons.Default.VolumeOff,
                    label = "静音",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.MUTE) }
                )
                
                TVRemoteButton(
                    icon = Icons.Default.VolumeUp,
                    label = "音量+",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.VOLUME_UP) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 播放控制
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TVRemoteButton(
                    icon = Icons.Default.FastRewind,
                    label = "快退",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.REWIND) }
                )
                
                TVRemoteButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    label = if (isPlaying) "暂停" else "播放",
                    color = theme.colors.primary,
                    onClick = { 
                        isPlaying = !isPlaying
                        onKeyPress?.invoke(if (isPlaying) UnifyTVKey.PLAY else UnifyTVKey.PAUSE)
                    }
                )
                
                TVRemoteButton(
                    icon = Icons.Default.FastForward,
                    label = "快进",
                    onClick = { onKeyPress?.invoke(UnifyTVKey.FAST_FORWARD) }
                )
            }
        }
    }
}

/**
 * TV遥控器按钮
 */
@Composable
private fun TVRemoteButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) color.copy(alpha = 0.3f) else Color.Transparent
            )
            .clickable {
                isPressed = true
                onClick()
                // 模拟按键反馈
                LaunchedEffect(Unit) {
                    delay(100)
                    isPressed = false
                }
            }
            .semantics {
                this.contentDescription = label
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

// 辅助函数
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
