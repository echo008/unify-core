package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun UnifyCamera(
    config: CameraConfig,
    onCaptureResult: (CaptureResult) -> Unit,
    modifier: Modifier,
    showControls: Boolean,
    enableGestures: Boolean,
    onPermissionDenied: () -> Unit
) {
    Column(modifier = modifier) {
        Text("iOS Camera")
        Button(
            onClick = { 
                onCaptureResult(CaptureResult(
                    success = true,
                    filePath = "captured_image_path"
                ))
            }
        ) {
            Text("Capture")
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
    Box(modifier = modifier) {
        Text("iOS Camera Preview")
        LaunchedEffect(Unit) {
            onCameraReady()
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
        Text("iOS Image Capture")
        Button(
            onClick = { 
                // Create a placeholder ImageBitmap - in real implementation would capture actual image
                // onImageCaptured(capturedBitmap)
            }
        ) {
            Text("Capture Image")
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
    Column(modifier = modifier) {
        Text("iOS Video Capture")
        Button(
            onClick = { onVideoRecorded("recorded_video") }
        ) {
            Text("Record Video")
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
    Column(modifier = modifier) {
        Text("iOS QR Scanner")
        Button(
            onClick = { onQRCodeDetected("scanned_qr_code") }
        ) {
            Text("Scan QR")
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
    Column(modifier = modifier) {
        Text("iOS Barcode Scanner")
        Button(
            onClick = { onBarcodeDetected("scanned_barcode", "CODE_128") }
        ) {
            Text("Scan Barcode")
        }
    }
}
