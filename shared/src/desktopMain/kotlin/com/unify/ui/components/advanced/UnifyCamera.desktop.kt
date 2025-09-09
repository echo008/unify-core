package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp

/**
 * Desktop平台Camera组件actual实现
 */

@Composable
actual fun UnifyCamera(
    config: CameraConfig,
    onCaptureResult: (CaptureResult) -> Unit,
    modifier: Modifier,
    showControls: Boolean,
    enableGestures: Boolean,
    onPermissionDenied: () -> Unit,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "相机组件 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "相机预览",
                        modifier = Modifier.size(64.dp),
                    )
                }
            }

            if (showControls) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = {
                            onCaptureResult(
                                CaptureResult(
                                    success = true,
                                    filePath = "mock_photo_${System.currentTimeMillis()}.jpg",
                                ),
                            )
                        },
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "拍照")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拍照")
                    }

                    Button(
                        onClick = {
                            onCaptureResult(
                                CaptureResult(
                                    success = true,
                                    filePath = "mock_video_${System.currentTimeMillis()}.mp4",
                                ),
                            )
                        },
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "录像")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("录像")
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyCameraPreview(
    config: CameraConfig,
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "相机预览",
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = "相机预览 - Desktop模拟",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
actual fun UnifyImageCapture(
    onImageCaptured: (ImageBitmap) -> Unit,
    modifier: Modifier,
    facing: CameraFacing,
    flashMode: FlashMode,
    showPreview: Boolean,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "图像捕获 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            if (showPreview) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "预览",
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // 模拟图像捕获，这里无法创建真实的ImageBitmap
                    // onImageCaptured(mockImageBitmap)
                },
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "拍照")
                Spacer(modifier = Modifier.width(8.dp))
                Text("拍照")
            }
        }
    }
}

@Composable
actual fun UnifyVideoCapture(
    onVideoRecorded: (String) -> Unit,
    modifier: Modifier,
    facing: CameraFacing,
    quality: VideoQuality,
    maxDuration: Long,
    showPreview: Boolean,
) {
    var isRecording by remember { mutableStateOf(false) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "视频捕获 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            if (showPreview) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp),
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
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "视频预览",
                            modifier = Modifier.size(48.dp),
                            tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isRecording) {
                        isRecording = false
                        onVideoRecorded("mock_video_${System.currentTimeMillis()}.mp4")
                    } else {
                        isRecording = true
                    }
                },
                colors =
                    if (isRecording) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                    contentDescription = if (isRecording) "停止录制" else "开始录制",
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRecording) "停止录制" else "开始录制")
            }
        }
    }
}

@Composable
actual fun UnifyQRScanner(
    onQRCodeDetected: (String) -> Unit,
    modifier: Modifier,
    showOverlay: Boolean,
    overlayColor: Color,
    enableFlash: Boolean,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "二维码扫描 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "二维码扫描",
                            modifier = Modifier.size(64.dp),
                        )
                        if (showOverlay) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.size(100.dp),
                                color = overlayColor.copy(alpha = 0.3f),
                            ) {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onQRCodeDetected("https://example.com/mock-qr-code")
                },
            ) {
                Text("模拟扫描二维码")
            }
        }
    }
}

@Composable
actual fun UnifyBarcodeScanner(
    onBarcodeDetected: (String, String) -> Unit,
    modifier: Modifier,
    supportedFormats: List<String>,
    showOverlay: Boolean,
    enableFlash: Boolean,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "条形码扫描 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "支持格式: ${supportedFormats.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "条形码扫描",
                        modifier = Modifier.size(64.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val format = supportedFormats.firstOrNull() ?: "QR_CODE"
                    onBarcodeDetected("1234567890123", format)
                },
            ) {
                Text("模拟扫描条形码")
            }
        }
    }
}
