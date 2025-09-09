package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Unify跨平台相机组件
 * 支持拍照、录像、扫码等功能
 */

enum class CameraFacing {
    FRONT,
    BACK,
}

enum class CameraMode {
    PHOTO,
    VIDEO,
    SCAN,
}

enum class FlashMode {
    OFF,
    ON,
    AUTO,
    TORCH,
}

data class CameraConfig(
    val facing: CameraFacing = CameraFacing.BACK,
    val mode: CameraMode = CameraMode.PHOTO,
    val flashMode: FlashMode = FlashMode.AUTO,
    val enableZoom: Boolean = true,
    val enableFocus: Boolean = true,
    val maxZoom: Float = 10f,
    val videoQuality: VideoQuality = VideoQuality.HD,
)

enum class VideoQuality {
    LOW,
    MEDIUM,
    HD,
    FULL_HD,
    ULTRA_HD,
}

data class CaptureResult(
    val success: Boolean,
    val filePath: String? = null,
    val bitmap: ImageBitmap? = null,
    val error: String? = null,
)

@Composable
expect fun UnifyCamera(
    config: CameraConfig = CameraConfig(),
    onCaptureResult: (CaptureResult) -> Unit = {},
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    enableGestures: Boolean = true,
    onPermissionDenied: () -> Unit = {},
)

@Composable
expect fun UnifyCameraPreview(
    config: CameraConfig = CameraConfig(),
    modifier: Modifier = Modifier,
    onCameraReady: () -> Unit = {},
    onError: (String) -> Unit = {},
)

@Composable
expect fun UnifyImageCapture(
    onImageCaptured: (ImageBitmap) -> Unit,
    modifier: Modifier = Modifier,
    facing: CameraFacing = CameraFacing.BACK,
    flashMode: FlashMode = FlashMode.AUTO,
    showPreview: Boolean = true,
)

@Composable
expect fun UnifyVideoCapture(
    onVideoRecorded: (String) -> Unit,
    modifier: Modifier = Modifier,
    facing: CameraFacing = CameraFacing.BACK,
    quality: VideoQuality = VideoQuality.HD,
    maxDuration: Long = 60000L, // 60 seconds
    showPreview: Boolean = true,
)

@Composable
expect fun UnifyQRScanner(
    onQRCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showOverlay: Boolean = true,
    overlayColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Red,
    enableFlash: Boolean = true,
)

@Composable
expect fun UnifyBarcodeScanner(
    onBarcodeDetected: (String, String) -> Unit, // value, format
    modifier: Modifier = Modifier,
    supportedFormats: List<String> = listOf("QR_CODE", "CODE_128", "CODE_39", "EAN_13"),
    showOverlay: Boolean = true,
    enableFlash: Boolean = true,
)
