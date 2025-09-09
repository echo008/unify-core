package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun UnifyStatusBarController(
    statusBarColor: Color,
    darkIcons: Boolean,
) {
    // iOS状态栏控制实现
}

@Composable
actual fun UnifyNavigationBarController(
    navigationBarColor: Color,
    darkIcons: Boolean,
) {
    // iOS导航栏控制实现
}

@Composable
actual fun UnifySystemUIController(
    statusBarColor: Color,
    navigationBarColor: Color,
    statusBarDarkIcons: Boolean,
    navigationBarDarkIcons: Boolean,
) {
    // iOS系统UI控制实现
}

@Composable
actual fun UnifySafeAreaHandler(content: @Composable () -> Unit) {
    content()
}

@Composable
actual fun UnifyKeyboardHandler(
    onKeyboardVisibilityChanged: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    content()
}

@Composable
actual fun UnifyBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // iOS返回处理实现
}

@Composable
actual fun UnifyLifecycleHandler(
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDestroy: () -> Unit,
) {
    // iOS生命周期处理实现
}

@Composable
actual fun UnifyPermissionHandler(
    permissions: List<String>,
    onPermissionResult: (Map<String, Boolean>) -> Unit,
) {
    // iOS权限处理实现
}

@Composable
actual fun UnifyFilePicker(
    fileTypes: List<String>,
    multipleSelection: Boolean,
    onFileSelected: (List<String>) -> Unit,
) {
    Button(
        onClick = { onFileSelected(emptyList()) },
    ) {
        Text("iOS File Picker")
    }
}

@Composable
actual fun UnifyCameraComponent(
    modifier: Modifier,
    onImageCaptured: (ByteArray) -> Unit,
    onError: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text("iOS Camera Component")
        Button(onClick = { onImageCaptured(ByteArray(0)) }) {
            Text("Capture Photo")
        }
    }
}

@Composable
actual fun UnifyMapComponent(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    onLocationSelected: (Double, Double) -> Unit,
) {
    Box(modifier = modifier) {
        Text("iOS Map Component")
        Text("Location: $latitude, $longitude")
    }
}

@Composable
actual fun UnifyWebView(
    url: String,
    modifier: Modifier,
    onPageLoaded: (String) -> Unit,
    onError: (String) -> Unit,
) {
    Box(modifier = modifier) {
        Text("iOS WebView: $url")
    }
}
