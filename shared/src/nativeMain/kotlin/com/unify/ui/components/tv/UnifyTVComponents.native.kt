package com.unify.ui.components.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Native平台TV组件实现
 */

@Composable
actual fun UnifyTVRemoteControl(
    onKeyPressed: (TVKey) -> Unit,
    modifier: Modifier
) {
    // Native平台TV遥控器组件实现
}

@Composable
actual fun UnifyTVMediaPlayer(
    mediaUrl: String,
    onPlaybackStateChange: (TVPlaybackState) -> Unit,
    modifier: Modifier,
    showControls: Boolean,
    autoPlay: Boolean
) {
    // Native平台TV媒体播放器组件实现
}

@Composable
actual fun UnifyTVGridMenu(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier,
    columns: Int
) {
    // Native平台TV网格菜单组件实现
}

@Composable
actual fun UnifyTVVolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier,
    showMute: Boolean
) {
    // Native平台TV音量控制组件实现
}

@Composable
actual fun UnifyTVChannelList(
    channels: List<TVChannel>,
    currentChannel: String,
    onChannelSelected: (TVChannel) -> Unit,
    modifier: Modifier
) {
    // Native平台TV频道列表组件实现
}
