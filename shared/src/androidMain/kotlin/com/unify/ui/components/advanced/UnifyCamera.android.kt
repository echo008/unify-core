@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyCamera(
    config: CameraConfig,
    onCaptureResult: (CaptureResult) -> Unit,
    modifier: Modifier,
    showControls: Boolean,
    enableGestures: Boolean,
    onPermissionDenied: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview placeholder
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Preview",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Mode: ${config.mode}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Facing: ${config.facing}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        if (showControls) {
            CameraControls(
                config = config,
                onCaptureResult = onCaptureResult,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
actual fun UnifyCameraPreview(
    config: CameraConfig,
    modifier: Modifier,
    onCameraReady: () -> Unit,
    onError: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        onCameraReady()
    }
    
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Camera Preview",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Camera Preview",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
actual fun UnifyImageCapture(
    onImageCaptured: (ImageBitmap) -> Unit,
    modifier: Modifier,
    facing: CameraFacing,
    flashMode: FlashMode,
    showPreview: Boolean
) {
    Column(modifier = modifier) {
        if (showPreview) {
            UnifyCameraPreview(
                config = CameraConfig(facing = facing, flashMode = flashMode),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FloatingActionButton(
                onClick = {
                    // Simulate image capture
                    // onImageCaptured would be called with actual ImageBitmap
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Capture Photo",
                    modifier = Modifier.size(32.dp)
                )
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
    showPreview: Boolean
) {
    var isRecording by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        if (showPreview) {
            UnifyCameraPreview(
                config = CameraConfig(
                    facing = facing,
                    mode = CameraMode.VIDEO,
                    videoQuality = quality
                ),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FloatingActionButton(
                onClick = {
                    isRecording = !isRecording
                    if (!isRecording) {
                        // Simulate video recording completion
                        onVideoRecorded("mock_video_path.mp4")
                    }
                },
                modifier = Modifier.size(64.dp),
                containerColor = if (isRecording) 
                    MaterialTheme.colorScheme.error 
                else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        if (isRecording) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
actual fun UnifyQRScanner(
    onQRCodeDetected: (String) -> Unit,
    modifier: Modifier,
    showOverlay: Boolean,
    overlayColor: Color,
    enableFlash: Boolean
) {
    Box(modifier = modifier.fillMaxSize()) {
        UnifyCameraPreview(
            config = CameraConfig(mode = CameraMode.SCAN),
            modifier = Modifier.fillMaxSize()
        )
        
        if (showOverlay) {
            QRScannerOverlay(
                overlayColor = overlayColor,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        if (enableFlash) {
            FloatingActionButton(
                onClick = { /* Toggle flash */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Toggle Flash"
                )
            }
        }
        
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = "Point camera at QR code",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
actual fun UnifyBarcodeScanner(
    onBarcodeDetected: (String, String) -> Unit,
    modifier: Modifier,
    supportedFormats: List<String>,
    showOverlay: Boolean,
    enableFlash: Boolean
) {
    Box(modifier = modifier.fillMaxSize()) {
        UnifyCameraPreview(
            config = CameraConfig(mode = CameraMode.SCAN),
            modifier = Modifier.fillMaxSize()
        )
        
        if (showOverlay) {
            BarcodeScannerOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }
        
        if (enableFlash) {
            FloatingActionButton(
                onClick = { /* Toggle flash */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Toggle Flash"
                )
            }
        }
        
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Point camera at barcode",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Supported: ${supportedFormats.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CameraControls(
    config: CameraConfig,
    onCaptureResult: (CaptureResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash toggle
        IconButton(
            onClick = { /* Toggle flash */ }
        ) {
            Icon(
                imageVector = when (config.flashMode) {
                    FlashMode.OFF -> Icons.Default.FlashOff
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.TORCH -> Icons.Default.FlashOn
                },
                contentDescription = "Flash",
                tint = Color.White
            )
        }
        
        // Capture button
        FloatingActionButton(
            onClick = {
                val result = CaptureResult(
                    success = true,
                    filePath = "mock_capture_${System.currentTimeMillis()}.jpg"
                )
                onCaptureResult(result)
            },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = when (config.mode) {
                    CameraMode.PHOTO -> Icons.Default.CameraAlt
                    CameraMode.VIDEO -> Icons.Default.Videocam
                    CameraMode.SCAN -> Icons.Default.QrCodeScanner
                },
                contentDescription = "Capture",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Switch camera
        IconButton(
            onClick = { /* Switch camera */ }
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Switch Camera",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun QRScannerOverlay(
    overlayColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = overlayColor.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
        ) {
            // Corner indicators
            repeat(4) { index ->
                val alignment = when (index) {
                    0 -> Alignment.TopStart
                    1 -> Alignment.TopEnd
                    2 -> Alignment.BottomStart
                    else -> Alignment.BottomEnd
                }
                Box(
                    modifier = Modifier
                        .align(alignment)
                        .size(20.dp)
                        .background(
                            color = overlayColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun BarcodeScannerOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(100.dp)
                .background(
                    color = Color.Red.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
        )
    }
}
