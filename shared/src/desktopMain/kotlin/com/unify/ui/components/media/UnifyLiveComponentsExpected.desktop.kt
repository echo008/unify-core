package com.unify.ui.components.media

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Desktop平台实时媒体组件actual实现
 */

actual object UnifyLiveComponents {
    @Composable
    actual fun LiveCameraPreview(
        modifier: Modifier,
        onCameraReady: () -> Unit,
        onError: (String) -> Unit,
    ) {
        LaunchedEffect(Unit) {
            onCameraReady()
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "实时相机预览",
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = "实时相机预览 (Desktop)",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }

    @Composable
    actual fun LiveAudioWaveform(
        modifier: Modifier,
        isRecording: Boolean,
        onRecordingToggle: (Boolean) -> Unit,
    ) {
        Card(modifier = modifier) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "音频波形",
                    modifier = Modifier.size(48.dp),
                    tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isRecording) "录音中..." else "音频波形显示",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 模拟波形显示
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                    color =
                        if (isRecording) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isRecording) "~~~波形~~~" else "静音",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onRecordingToggle(!isRecording) },
                    colors =
                        if (isRecording) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                ) {
                    Text(if (isRecording) "停止录音" else "开始录音")
                }
            }
        }
    }
}
