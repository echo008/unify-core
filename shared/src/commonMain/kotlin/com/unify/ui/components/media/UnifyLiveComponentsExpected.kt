package com.unify.ui.components.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 跨平台实时媒体组件expect声明
 */
expect object UnifyLiveComponents {
    
    /**
     * 实时相机预览组件
     */
    @Composable
    fun LiveCameraPreview(
        modifier: Modifier = Modifier,
        onCameraReady: () -> Unit = {},
        onError: (String) -> Unit = {}
    )
    
    /**
     * 实时音频波形显示组件
     */
    @Composable
    fun LiveAudioWaveform(
        modifier: Modifier = Modifier,
        isRecording: Boolean = false,
        onRecordingToggle: (Boolean) -> Unit = {}
    )
}
