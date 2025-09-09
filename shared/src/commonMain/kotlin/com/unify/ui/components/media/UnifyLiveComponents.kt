package com.unify.ui.components.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Unify直播组件 - 跨平台统一直播系统
 * 支持8大平台的直播推流、拉流、互动等功能
 */

/**
 * 直播状态枚举
 */
enum class LiveStatus {
    IDLE, // 空闲
    PREPARING, // 准备中
    LIVE, // 直播中
    PAUSED, // 暂停
    ENDED, // 已结束
    ERROR, // 错误
}

/**
 * 直播质量枚举
 */
enum class LiveQuality {
    LOW, // 低质量 480p
    MEDIUM, // 中等质量 720p
    HIGH, // 高质量 1080p
    ULTRA, // 超高质量 4K
}

/**
 * 直播配置
 */
data class LiveConfig(
    val quality: LiveQuality = LiveQuality.HIGH,
    val bitrate: Int = 2000, // kbps
    val frameRate: Int = 30,
    val enableAudio: Boolean = true,
    val enableVideo: Boolean = true,
    val enableChat: Boolean = true,
    val enableGifts: Boolean = true,
    val enableBeauty: Boolean = false,
    val enableFilter: Boolean = false,
    val maxViewers: Int = 10000,
    val recordingEnabled: Boolean = false,
)

/**
 * 直播间信息
 */
data class LiveRoomInfo(
    val roomId: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val category: String,
    val tags: List<String>,
    val hostId: String,
    val hostName: String,
    val hostAvatar: String,
    val viewerCount: Int,
    val likeCount: Int,
    val status: LiveStatus,
    val startTime: Long,
    val duration: Long = 0L,
)

/**
 * 直播消息
 */
data class LiveMessage(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long,
    val level: Int = 1,
    val isVip: Boolean = false,
    val giftInfo: GiftInfo? = null,
)

/**
 * 消息类型
 */
enum class MessageType {
    TEXT, // 文本消息
    GIFT, // 礼物
    LIKE, // 点赞
    FOLLOW, // 关注
    SHARE, // 分享
    SYSTEM, // 系统消息
}

/**
 * 礼物信息
 */
data class GiftInfo(
    val giftId: String,
    val giftName: String,
    val giftIcon: String,
    val giftValue: Int,
    val giftCount: Int = 1,
    val animationUrl: String? = null,
)

/**
 * 直播观众信息
 */
data class LiveViewer(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val level: Int,
    val isVip: Boolean,
    val followTime: Long? = null,
    val giftValue: Int = 0,
)

/**
 * 统一直播播放器
 */
@Composable
fun UnifyLivePlayer(
    roomInfo: LiveRoomInfo,
    modifier: Modifier = Modifier,
    config: LiveConfig = LiveConfig(),
    onStatusChange: (LiveStatus) -> Unit = {},
    onViewerCountChange: (Int) -> Unit = {},
    onError: (String) -> Unit = {},
) {
    var currentStatus by remember { mutableStateOf(roomInfo.status) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(roomInfo.roomId) {
        isLoading = true
        delay(2000) // 模拟加载
        currentStatus = LiveStatus.LIVE
        isLoading = false
        onStatusChange(currentStatus)
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
    ) {
        if (isLoading) {
            // 加载状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "连接中...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            // 视频内容区域
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f)),
            ) {
                // 模拟视频内容
                Text(
                    text = "📺 直播画面",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        // 直播状态指示器
        LiveStatusIndicator(
            status = currentStatus,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
        )

        // 观看人数
        LiveViewerCount(
            count = roomInfo.viewerCount,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
        )

        // 控制按钮
        LivePlayerControls(
            status = currentStatus,
            config = config,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
            onPlayPause = {
                currentStatus =
                    if (currentStatus == LiveStatus.LIVE) {
                        LiveStatus.PAUSED
                    } else {
                        LiveStatus.LIVE
                    }
                onStatusChange(currentStatus)
            },
            onQualityChange = { /* 处理质量切换 */ },
            onFullScreen = { /* 处理全屏 */ },
        )
    }
}

/**
 * 直播状态指示器
 */
@Composable
fun LiveStatusIndicator(
    status: LiveStatus,
    modifier: Modifier = Modifier,
) {
    val (text, color) =
        when (status) {
            LiveStatus.LIVE -> "直播中" to Color(0xFFFF4444)
            LiveStatus.PREPARING -> "准备中" to Color(0xFFFFAA00)
            LiveStatus.PAUSED -> "暂停" to Color(0xFF888888)
            LiveStatus.ENDED -> "已结束" to Color(0xFF666666)
            LiveStatus.ERROR -> "错误" to Color(0xFFFF0000)
            LiveStatus.IDLE -> "空闲" to Color(0xFF999999)
        }

    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.9f),
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status == LiveStatus.LIVE) {
                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * 观看人数显示
 */
@Composable
fun LiveViewerCount(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.6f),
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "观看人数",
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatViewerCount(count),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * 直播播放器控制
 */
@Composable
fun LivePlayerControls(
    status: LiveStatus,
    config: LiveConfig,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onQualityChange: (LiveQuality) -> Unit,
    onFullScreen: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 播放/暂停按钮
        IconButton(
            onClick = onPlayPause,
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp),
        ) {
            Icon(
                imageVector =
                    if (status == LiveStatus.LIVE) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                contentDescription = if (status == LiveStatus.LIVE) "暂停" else "播放",
                tint = Color.White,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 质量选择
        IconButton(
            onClick = { /* 显示质量选择菜单 */ },
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = Color.White,
            )
        }

        // 全屏按钮
        IconButton(
            onClick = onFullScreen,
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "全屏",
                tint = Color.White,
            )
        }
    }
}

/**
 * 直播聊天室
 */
@Composable
fun LiveChatRoom(
    messages: List<LiveMessage>,
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit = {},
    onSendGift: (GiftInfo) -> Unit = {},
) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxHeight(),
    ) {
        // 消息列表
        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(messages.reversed()) { message ->
                LiveMessageItem(message = message)
            }
        }

        // 输入区域
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("说点什么...") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 发送按钮
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = Color.White,
                )
            }

            // 礼物按钮
            IconButton(
                onClick = { /* 显示礼物面板 */ },
                modifier =
                    Modifier
                        .background(Color(0xFFFF6B6B), CircleShape)
                        .size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "礼物",
                    tint = Color.White,
                )
            }
        }
    }
}

/**
 * 直播消息项
 */
@Composable
fun LiveMessageItem(
    message: LiveMessage,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // 用户头像
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .background(Color.Gray, CircleShape)
                    .clip(CircleShape),
        ) {
            Text(
                text = message.userName.take(1),
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 消息内容
        Column(modifier = Modifier.weight(1f)) {
            // 用户名和等级
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.userName,
                    color = if (message.isVip) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )

                if (message.level > 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lv.${message.level}",
                        modifier =
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp),
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 8.sp,
                    )
                }
            }

            // 消息文本
            when (message.type) {
                MessageType.TEXT -> {
                    Text(
                        text = message.content,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                    )
                }
                MessageType.GIFT -> {
                    message.giftInfo?.let { gift ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "送出了 ${gift.giftName}",
                                color = Color(0xFFFF6B6B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            if (gift.giftCount > 1) {
                                Text(
                                    text = " x${gift.giftCount}",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
                MessageType.FOLLOW -> {
                    Text(
                        text = "关注了主播",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                MessageType.LIKE -> {
                    Text(
                        text = "点了个赞 ❤️",
                        color = Color(0xFFFF4444),
                        fontSize = 14.sp,
                    )
                }
                MessageType.SHARE -> {
                    Text(
                        text = "分享了直播间",
                        color = Color(0xFF2196F3),
                        fontSize = 14.sp,
                    )
                }
                MessageType.SYSTEM -> {
                    Text(
                        text = message.content,
                        color = Color(0xFF999999),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
            }
        }
    }
}

/**
 * 直播观众列表
 */
@Composable
fun LiveViewerList(
    viewers: List<LiveViewer>,
    modifier: Modifier = Modifier,
    onViewerClick: (LiveViewer) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(viewers.take(20)) { viewer ->
            LiveViewerItem(
                viewer = viewer,
                onClick = { onViewerClick(viewer) },
            )
        }

        if (viewers.size > 20) {
            item {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                            .clickable { /* 显示更多观众 */ },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+${viewers.size - 20}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * 直播观众项
 */
@Composable
fun LiveViewerItem(
    viewer: LiveViewer,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .background(Color.Gray, CircleShape)
                .clip(CircleShape)
                .clickable { onClick() }
                .then(
                    if (viewer.isVip) {
                        Modifier.border(2.dp, Color(0xFFFFD700), CircleShape)
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = viewer.userName.take(1),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 直播礼物面板
 */
@Composable
fun LiveGiftPanel(
    gifts: List<GiftInfo>,
    modifier: Modifier = Modifier,
    onGiftSelected: (GiftInfo) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "选择礼物",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(gifts) { gift ->
                    GiftItem(
                        gift = gift,
                        onClick = { onGiftSelected(gift) },
                    )
                }
            }
        }
    }
}

/**
 * 礼物项
 */
@Composable
fun GiftItem(
    gift: GiftInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .width(80.dp)
                .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "🎁",
                    fontSize = 20.sp,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = gift.giftName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = "${gift.giftValue}金币",
                fontSize = 8.sp,
                color = Color(0xFFFF6B6B),
            )
        }
    }
}

/**
 * 格式化观看人数
 */
private fun formatViewerCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> "${count / 1000}.${(count % 1000) / 100}K"
        count < 1000000 -> "${count / 10000}.${(count % 10000) / 1000}W"
        else -> "${count / 1000000}.${(count % 1000000) / 100000}M"
    }
}

/**
 * 默认直播配置
 */
val DefaultLiveConfig = LiveConfig()
