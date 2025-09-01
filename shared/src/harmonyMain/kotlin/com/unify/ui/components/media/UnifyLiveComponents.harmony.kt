package com.unify.ui.components.media

import androidx.compose.runtime.Composable

/**
 * HarmonyOS 平台直播组件实现
 * 基于ArkUI Video组件和分布式播放能力
 */
@Composable
actual fun UnifyLivePlayer(
    config: UnifyLivePlayerConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyLivePlayerState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    // 使用ArkUI Video组件实现
    // 集成HarmonyOS分布式播放能力
}

@Composable
actual fun UnifyLivePusher(
    config: UnifyLivePusherConfig,
    modifier: Modifier = Modifier,
    onStateChange: ((UnifyLivePusherState) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    // 使用ArkUI Camera组件实现推流
    // 集成HarmonyOS分布式推流能力
}

@Composable
actual fun UnifyWebRTC(
    config: UnifyWebRTCConfig,
    modifier: Modifier = Modifier,
    onUserJoin: ((String) -> Unit)? = null,
    onUserLeave: ((String) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    // 使用HarmonyOS WebRTC框架
    // 支持分布式视频通话
}
