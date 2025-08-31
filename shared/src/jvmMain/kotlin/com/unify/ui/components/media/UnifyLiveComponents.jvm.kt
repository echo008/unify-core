package com.unify.ui.components.media

import androidx.compose.runtime.*

/**
 * JVM (Desktop) 平台直播组件实现
 * 基于JavaFX MediaPlayer或第三方媒体库
 */
@Composable
actual fun UnifyLivePlayer(
    config: UnifyLivePlayerConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyLivePlayerState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    // 使用JavaFX MediaPlayer或VLCJ等第三方库实现直播播放
    // Desktop平台直播播放组件
}

@Composable
actual fun UnifyLivePusher(
    config: UnifyLivePusherConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyLivePusherState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    // 使用JavaFX Camera和推流库实现直播推流
    // Desktop平台直播推流组件
}

@Composable
actual fun UnifyWebRTC(
    config: UnifyWebRTCConfig,
    modifier: Modifier = Modifier,
    onUserJoin: ((String) -> Unit)? = null,
    onUserLeave: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    // 使用WebRTC Java实现库
    // Desktop平台视频通话组件
}
