package com.unify.core.ui.tv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.core.platform.tv.*

/**
 * TV应用示例
 * 展示高性能10-foot UI和焦点管理优化
 * 基于Material Design for TV和Android TV最佳实践
 */

/**
 * TV应用主屏幕
 * 性能优化：使用高效的状态管理和焦点导航
 */
@Composable
fun TVAppHomeScreen(
    tvManager: UnifyTVManager,
    modifier: Modifier = Modifier
) {
    val displayInfo by tvManager.displayInfo.collectAsState()
    // 简化示例，使用模拟数据
    val mediaItems = remember {
        listOf(
            TVMediaItem(
                id = "1",
                title = "示例视频",
                description = "这是一个示例视频文件",
                uri = "",
                mediaType = TVMediaType.Video,
                duration = 120000L
            ),
            TVMediaItem(
                id = "2",
                title = "示例音频",
                description = "这是一个示例音频文件",
                uri = "",
                mediaType = TVMediaType.Audio,
                duration = 180000L
            )
        )
    }
    val playbackState = remember {
        TVPlaybackState(
            isPlaying = false,
            position = 0L,
            duration = 0L,
            volume = 0.5f
        )
    }
    
    // 性能优化：使用remember缓存导航状态
    var selectedSection by remember { mutableStateOf(0) }
    var selectedMediaIndex by remember { mutableStateOf(0) }
    
    // 性能优化：缓存导航项目
    val navigationItems = remember {
        listOf(
            TVNavigationItem("首页", "🏠"),
            TVNavigationItem("视频", "📹"),
            TVNavigationItem("音乐", "🎵"),
            TVNavigationItem("图片", "🖼️"),
            TVNavigationItem("设置", "⚙️")
        )
    }
    
    // 性能优化：缓存推荐的网格列数
    val gridColumns = remember(displayInfo.width) {
        TVPlatformUtils.getRecommendedGridColumns(displayInfo.width)
    }
    
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // 左侧导航面板
        TVNavigationPanel(
            items = navigationItems,
            selectedIndex = selectedSection,
            onSelectionChanged = { selectedSection = it },
            modifier = Modifier
                .width(TVPlatformUtils.Constants.NAVIGATION_PANEL_WIDTH.dp)
                .fillMaxHeight()
        )
        
        // 主内容区域
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            when (selectedSection) {
                0 -> TVHomeContent(
                    displayInfo = displayInfo,
                    mediaItems = mediaItems.take(8), // 显示前8个项目
                    onMediaClick = { mediaItem ->
                        // TODO: 实现媒体播放逻辑
                    },
                    gridColumns = gridColumns
                )
                
                1 -> TVVideoSection(
                    mediaItems = mediaItems.filter { mediaItem -> mediaItem.mediaType == TVMediaType.Video },
                    onMediaClick = { mediaItem ->
                        // TODO: 实现视频播放逻辑
                    },
                    gridColumns = gridColumns
                )
                
                2 -> TVAudioSection(
                    mediaItems = mediaItems.filter { mediaItem -> mediaItem.mediaType == TVMediaType.Audio },
                    playbackState = playbackState,
                    onMediaClick = { mediaItem ->
                        // TODO: 实现音频播放逻辑
                    },
                    onPlayPause = { /* TODO: 实现播放/暂停 */ },
                    onStop = { /* TODO: 实现停止播放 */ },
                    onSeek = { position -> /* TODO: 实现进度跳转 */ }
                )
                
                3 -> TVImageSection(
                    mediaItems = mediaItems.filter { mediaItem -> mediaItem.mediaType == TVMediaType.Image },
                    onMediaClick = { mediaItem ->
                        // TODO: 实现图片查看逻辑
                    },
                    gridColumns = gridColumns
                )
                
                4 -> TVSettingsSection(
                    displayInfo = displayInfo,
                    tvManager = tvManager
                )
            }
        }
    }
}

/**
 * TV首页内容
 * 性能优化：使用高效的网格布局和缓存
 */
@Composable
private fun TVHomeContent(
    displayInfo: TVDisplayInfo,
    mediaItems: List<TVMediaItem>,
    onMediaClick: (TVMediaItem) -> Unit,
    gridColumns: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // 欢迎信息
            TVWelcomeCard(displayInfo = displayInfo)
        }
        
        item {
            // 推荐内容标题
            Text(
                text = "推荐内容",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            // 媒体网格
            TVMediaGrid(
                mediaItems = mediaItems,
                onItemClick = onMediaClick,
                columns = gridColumns,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * TV视频区域
 */
@Composable
private fun TVVideoSection(
    mediaItems: List<TVMediaItem>,
    onMediaClick: (TVMediaItem) -> Unit,
    gridColumns: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "视频库",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TVMediaGrid(
            mediaItems = mediaItems,
            onItemClick = onMediaClick,
            columns = gridColumns,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * TV音频区域
 */
@Composable
private fun TVAudioSection(
    mediaItems: List<TVMediaItem>,
    playbackState: TVPlaybackState,
    onMediaClick: (TVMediaItem) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "音乐库",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 媒体播放器控制面板
        TVMediaPlayerControls(
            playbackState = playbackState,
            onPlay = onPlayPause,
            onPause = onPlayPause,
            onStop = onStop,
            onSeek = onSeek,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        
        // 音频列表
        TVMediaGrid(
            mediaItems = mediaItems,
            onItemClick = onMediaClick,
            columns = 3, // 音频使用较少的列数
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * TV图片区域
 */
@Composable
private fun TVImageSection(
    mediaItems: List<TVMediaItem>,
    onMediaClick: (TVMediaItem) -> Unit,
    gridColumns: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "图片库",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TVMediaGrid(
            mediaItems = mediaItems,
            onItemClick = onMediaClick,
            columns = gridColumns + 1, // 图片使用更多列数
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * TV设置区域
 */
@Composable
private fun TVSettingsSection(
    displayInfo: TVDisplayInfo,
    tvManager: UnifyTVManager,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            TVInfoPanel(
                displayInfo = displayInfo,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            TVSettingsCard(
                title = "显示设置",
                description = "调整分辨率、刷新率等显示参数",
                onClick = { /* 打开显示设置 */ }
            )
        }
        
        item {
            TVSettingsCard(
                title = "音频设置",
                description = "配置音频输出和音效选项",
                onClick = { /* 打开音频设置 */ }
            )
        }
        
        item {
            TVSettingsCard(
                title = "网络设置",
                description = "配置网络连接和流媒体选项",
                onClick = { /* 打开网络设置 */ }
            )
        }
    }
}

/**
 * TV欢迎卡片
 */
@Composable
private fun TVWelcomeCard(
    displayInfo: TVDisplayInfo,
    modifier: Modifier = Modifier
) {
    TVFocusableCard(
        onClick = { /* 欢迎卡片点击事件 */ },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "欢迎使用 Unify TV",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "享受${displayInfo.width}×${displayInfo.height}${if (displayInfo.is4K) " 4K" else ""}高清体验",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * TV设置卡片
 */
@Composable
private fun TVSettingsCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TVFocusableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
