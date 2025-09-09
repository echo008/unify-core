package com.unify.ui.components.tv

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * TV平台组件接口定义
 * 支持大屏幕和遥控器操作
 */

enum class TVKey(val displayName: String) {
    UP("↑"),
    DOWN("↓"),
    LEFT("←"),
    RIGHT("→"),
    CENTER("OK"),
    BACK("返回"),
    HOME("主页"),
    MENU("菜单"),
    VOLUME_UP("音量+"),
    VOLUME_DOWN("音量-"),
    MUTE("静音"),
    CHANNEL_UP("频道+"),
    CHANNEL_DOWN("频道-"),
    POWER("电源"),
    INPUT("输入源"),
    SETTINGS("设置"),
    NUMBER_0("0"),
    NUMBER_1("1"),
    NUMBER_2("2"),
    NUMBER_3("3"),
    NUMBER_4("4"),
    NUMBER_5("5"),
    NUMBER_6("6"),
    NUMBER_7("7"),
    NUMBER_8("8"),
    NUMBER_9("9"),
}

enum class TVPlaybackState {
    PLAYING,
    PAUSED,
    STOPPED,
    BUFFERING,
    ERROR,
}

data class TVChannel(
    val id: String,
    val number: Int,
    val name: String,
    val currentProgram: String? = null,
)

@Composable
expect fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun UnifyTVMediaPlayer(
    mediaUrl: String,
    onPlaybackStateChange: (TVPlaybackState) -> Unit,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    autoPlay: Boolean = false,
)

@Composable
expect fun UnifyTVGridMenu(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
)

@Composable
expect fun UnifyTVVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showMute: Boolean = true,
)

@Composable
expect fun UnifyTVChannelList(
    channels: List<TVChannel>,
    currentChannel: String,
    onChannelSelected: (TVChannel) -> Unit,
    modifier: Modifier = Modifier,
)
