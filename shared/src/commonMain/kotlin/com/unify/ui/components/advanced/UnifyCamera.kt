package com.unify.ui.components.advanced

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import com.unify.ui.components.feedback.*

/**
 * Unify Camera 组件
 * 支持多平台适配的统一相机组件，参考 KuiklyUI 设计规范
 */

/**
 * 相机状态枚举
 */
enum class UnifyCameraState {
    IDLE,           // 空闲状态
    PREVIEW,        // 预览状态
    CAPTURING,      // 拍摄中
    RECORDING,      // 录制中
    PROCESSING,     // 处理中
    ERROR           // 错误状态
}

/**
 * 相机模式枚举
 */
enum class UnifyCameraMode {
    PHOTO,          // 拍照模式
    VIDEO,          // 录像模式
    PORTRAIT,       // 人像模式
    PANORAMA,       // 全景模式
    NIGHT,          // 夜景模式
    PROFESSIONAL    // 专业模式
}

/**
 * 相机镜头类型
 */
enum class UnifyCameraLens {
    BACK,           // 后置摄像头
    FRONT,          // 前置摄像头
    WIDE,           // 广角镜头
    TELEPHOTO,      // 长焦镜头
    ULTRA_WIDE      // 超广角镜头
}

/**
 * 相机设置数据
 */
data class UnifyCameraSettings(
    val flashMode: UnifyFlashMode = UnifyFlashMode.AUTO,
    val resolution: UnifyResolution = UnifyResolution.HIGH,
    val aspectRatio: UnifyAspectRatio = UnifyAspectRatio.RATIO_16_9,
    val gridLines: Boolean = false,
    val timer: UnifyTimerMode = UnifyTimerMode.OFF,
    val stabilization: Boolean = true
)

/**
 * 闪光灯模式
 */
enum class UnifyFlashMode {
    OFF, ON, AUTO, TORCH
}

/**
 * 分辨率设置
 */
enum class UnifyResolution {
    LOW, MEDIUM, HIGH, ULTRA_HIGH
}

/**
 * 宽高比设置
 */
enum class UnifyAspectRatio {
    RATIO_1_1, RATIO_4_3, RATIO_16_9, RATIO_21_9
}

/**
 * 定时器模式
 */
enum class UnifyTimerMode {
    OFF, TIMER_3S, TIMER_10S
}

/**
 * 主要 Unify Camera 组件
 */
@Composable
fun UnifyCamera(
    state: UnifyCameraState,
    onStateChanged: (UnifyCameraState) -> Unit,
    modifier: Modifier = Modifier,
    mode: UnifyCameraMode = UnifyCameraMode.PHOTO,
    onModeChanged: ((UnifyCameraMode) -> Unit)? = null,
    lens: UnifyCameraLens = UnifyCameraLens.BACK,
    onLensChanged: ((UnifyCameraLens) -> Unit)? = null,
    settings: UnifyCameraSettings = UnifyCameraSettings(),
    onSettingsChanged: ((UnifyCameraSettings) -> Unit)? = null,
    onCapturePhoto: (() -> Unit)? = null,
    onStartRecording: (() -> Unit)? = null,
    onStopRecording: (() -> Unit)? = null,
    showControls: Boolean = true,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        // 相机预览区域
        UnifyCameraPreview(
            state = state,
            settings = settings,
            modifier = Modifier.fillMaxSize()
        )
        
        // 网格线
        if (settings.gridLines) {
            UnifyCameraGridLines(
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 控制界面
        if (showControls) {
            UnifyCameraControls(
                state = state,
                onStateChanged = onStateChanged,
                mode = mode,
                onModeChanged = onModeChanged,
                lens = lens,
                onLensChanged = onLensChanged,
                settings = settings,
                onSettingsChanged = onSettingsChanged,
                onCapturePhoto = onCapturePhoto,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 状态指示器
        UnifyCameraStatusIndicator(
            state = state,
            mode = mode,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
    }
}

/**
 * 相机预览组件
 */
@Composable
private fun UnifyCameraPreview(
    state: UnifyCameraState,
    settings: UnifyCameraSettings,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            UnifyCameraState.IDLE -> {
                UnifyText(
                    text = "相机未启动",
                    variant = UnifyTextVariant.BODY_LARGE,
                    color = Color.White
                )
            }
            UnifyCameraState.PREVIEW -> {
                // 实际相机预览会在平台特定实现中处理
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyText(
                        text = "相机预览",
                        variant = UnifyTextVariant.BODY_LARGE,
                        color = Color.White
                    )
                }
            }
            UnifyCameraState.CAPTURING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f))
                )
            }
            UnifyCameraState.PROCESSING -> {
                UnifyLoading(
                    variant = UnifyLoadingVariant.CIRCULAR,
                    size = UnifyLoadingSize.LARGE,
                    color = Color.White
                )
            }
            UnifyCameraState.ERROR -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UnifyIcon(
                        icon = Icons.Default.Error,
                        size = UnifyIconSize.LARGE,
                        tint = theme.colors.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifyText(
                        text = "相机错误",
                        variant = UnifyTextVariant.BODY_LARGE,
                        color = theme.colors.error
                    )
                }
            }
            else -> {
                // 其他状态的处理
            }
        }
    }
}

/**
 * 相机网格线组件
 */
@Composable
private fun UnifyCameraGridLines(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 1.dp.toPx()
        val color = Color.White.copy(alpha = 0.5f)
        
        // 垂直线
        drawLine(
            color = color,
            start = Offset(width / 3, 0f),
            end = Offset(width / 3, height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(width * 2 / 3, 0f),
            end = Offset(width * 2 / 3, height),
            strokeWidth = strokeWidth
        )
        
        // 水平线
        drawLine(
            color = color,
            start = Offset(0f, height / 3),
            end = Offset(width, height / 3),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(0f, height * 2 / 3),
            end = Offset(width, height * 2 / 3),
            strokeWidth = strokeWidth
        )
    }
}

/**
 * 相机控制界面组件
 */
@Composable
private fun UnifyCameraControls(
    state: UnifyCameraState,
    onStateChanged: (UnifyCameraState) -> Unit,
    mode: UnifyCameraMode,
    onModeChanged: ((UnifyCameraMode) -> Unit)?,
    lens: UnifyCameraLens,
    onLensChanged: ((UnifyCameraLens) -> Unit)?,
    settings: UnifyCameraSettings,
    onSettingsChanged: ((UnifyCameraSettings) -> Unit)?,
    onCapturePhoto: (() -> Unit)?,
    onStartRecording: (() -> Unit)?,
    onStopRecording: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 顶部控制栏
        UnifyCameraTopControls(
            settings = settings,
            onSettingsChanged = onSettingsChanged,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // 底部控制栏
        UnifyCameraBottomControls(
            state = state,
            onStateChanged = onStateChanged,
            mode = mode,
            onModeChanged = onModeChanged,
            lens = lens,
            onLensChanged = onLensChanged,
            onCapturePhoto = onCapturePhoto,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

/**
 * 顶部控制栏组件
 */
@Composable
private fun UnifyCameraTopControls(
    settings: UnifyCameraSettings,
    onSettingsChanged: ((UnifyCameraSettings) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 闪光灯控制
        UnifyCameraControlButton(
            icon = when (settings.flashMode) {
                UnifyFlashMode.OFF -> Icons.Default.FlashOff
                UnifyFlashMode.ON -> Icons.Default.FlashOn
                UnifyFlashMode.AUTO -> Icons.Default.FlashAuto
                UnifyFlashMode.TORCH -> Icons.Default.Flashlight
            },
            onClick = {
                onSettingsChanged?.invoke(
                    settings.copy(
                        flashMode = when (settings.flashMode) {
                            UnifyFlashMode.OFF -> UnifyFlashMode.AUTO
                            UnifyFlashMode.AUTO -> UnifyFlashMode.ON
                            UnifyFlashMode.ON -> UnifyFlashMode.TORCH
                            UnifyFlashMode.TORCH -> UnifyFlashMode.OFF
                        }
                    )
                )
            },
            contentDescription = "闪光灯"
        )
        
        // 定时器控制
        UnifyCameraControlButton(
            icon = when (settings.timer) {
                UnifyTimerMode.OFF -> Icons.Default.Timer
                UnifyTimerMode.TIMER_3S -> Icons.Default.Timer3
                UnifyTimerMode.TIMER_10S -> Icons.Default.Timer10
            },
            onClick = {
                onSettingsChanged?.invoke(
                    settings.copy(
                        timer = when (settings.timer) {
                            UnifyTimerMode.OFF -> UnifyTimerMode.TIMER_3S
                            UnifyTimerMode.TIMER_3S -> UnifyTimerMode.TIMER_10S
                            UnifyTimerMode.TIMER_10S -> UnifyTimerMode.OFF
                        }
                    )
                )
            },
            contentDescription = "定时器"
        )
        
        // 网格线控制
        UnifyCameraControlButton(
            icon = Icons.Default.GridOn,
            isActive = settings.gridLines,
            onClick = {
                onSettingsChanged?.invoke(
                    settings.copy(gridLines = !settings.gridLines)
                )
            },
            contentDescription = "网格线"
        )
        
        // 设置按钮
        UnifyCameraControlButton(
            icon = Icons.Default.Settings,
            onClick = { /* 打开设置面板 */ },
            contentDescription = "设置"
        )
    }
}

/**
 * 底部控制栏组件
 */
@Composable
private fun UnifyCameraBottomControls(
    state: UnifyCameraState,
    onStateChanged: (UnifyCameraState) -> Unit,
    mode: UnifyCameraMode,
    onModeChanged: ((UnifyCameraMode) -> Unit)?,
    lens: UnifyCameraLens,
    onLensChanged: ((UnifyCameraLens) -> Unit)?,
    onCapturePhoto: (() -> Unit)?,
    onStartRecording: (() -> Unit)?,
    onStopRecording: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 模式选择器
        UnifyCameraModeSelector(
            selectedMode = mode,
            onModeChanged = onModeChanged,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相册按钮
            UnifyCameraControlButton(
                icon = Icons.Default.PhotoLibrary,
                onClick = { /* 打开相册 */ },
                contentDescription = "相册"
            )
            
            // 拍摄按钮
            UnifyCameraCaptureButton(
                state = state,
                mode = mode,
                onCapturePhoto = onCapturePhoto,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )
            
            // 切换镜头按钮
            UnifyCameraControlButton(
                icon = Icons.Default.FlipCameraAndroid,
                onClick = {
                    onLensChanged?.invoke(
                        when (lens) {
                            UnifyCameraLens.BACK -> UnifyCameraLens.FRONT
                            UnifyCameraLens.FRONT -> UnifyCameraLens.BACK
                            else -> UnifyCameraLens.BACK
                        }
                    )
                },
                contentDescription = "切换镜头"
            )
        }
    }
}

/**
 * 模式选择器组件
 */
@Composable
private fun UnifyCameraModeSelector(
    selectedMode: UnifyCameraMode,
    onModeChanged: ((UnifyCameraMode) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        UnifyCameraMode.PHOTO to "拍照",
        UnifyCameraMode.VIDEO to "录像",
        UnifyCameraMode.PORTRAIT to "人像",
        UnifyCameraMode.NIGHT to "夜景"
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = mode == selectedMode
            
            UnifyText(
                text = label,
                variant = if (isSelected) UnifyTextVariant.BODY_MEDIUM else UnifyTextVariant.CAPTION,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    onModeChanged?.invoke(mode)
                }
            )
        }
    }
}

/**
 * 拍摄按钮组件
 */
@Composable
private fun UnifyCameraCaptureButton(
    state: UnifyCameraState,
    mode: UnifyCameraMode,
    onCapturePhoto: (() -> Unit)?,
    onStartRecording: (() -> Unit)?,
    onStopRecording: (() -> Unit)?
) {
    val theme = LocalUnifyTheme.current
    val isRecording = state == UnifyCameraState.RECORDING
    
    // 录制时的脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                Color.White.copy(alpha = 0.2f),
                CircleShape
            )
            .clickable {
                when (mode) {
                    UnifyCameraMode.PHOTO -> onCapturePhoto?.invoke()
                    UnifyCameraMode.VIDEO -> {
                        if (isRecording) {
                            onStopRecording?.invoke()
                        } else {
                            onStartRecording?.invoke()
                        }
                    }
                    else -> onCapturePhoto?.invoke()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    if (isRecording) {
                        Color.Red.copy(alpha = pulseAlpha)
                    } else {
                        Color.White
                    },
                    if (mode == UnifyCameraMode.VIDEO && isRecording) {
                        RoundedCornerShape(8.dp)
                    } else {
                        CircleShape
                    }
                )
        )
    }
}

/**
 * 控制按钮组件
 */
@Composable
private fun UnifyCameraControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                if (isActive) {
                    Color.White.copy(alpha = 0.3f)
                } else {
                    Color.Black.copy(alpha = 0.3f)
                },
                CircleShape
            )
            .clickable { onClick() }
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        contentAlignment = Alignment.Center
    ) {
        UnifyIcon(
            icon = icon,
            size = UnifyIconSize.MEDIUM,
            tint = Color.White
        )
    }
}

/**
 * 状态指示器组件
 */
@Composable
private fun UnifyCameraStatusIndicator(
    state: UnifyCameraState,
    mode: UnifyCameraMode,
    modifier: Modifier = Modifier
) {
    when (state) {
        UnifyCameraState.RECORDING -> {
            Row(
                modifier = modifier
                    .background(
                        Color.Red.copy(alpha = 0.8f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "录制中",
                    variant = UnifyTextVariant.CAPTION,
                    color = Color.White
                )
            }
        }
        UnifyCameraState.PROCESSING -> {
            Row(
                modifier = modifier
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyLoading(
                    variant = UnifyLoadingVariant.CIRCULAR,
                    size = UnifyLoadingSize.SMALL,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "处理中",
                    variant = UnifyTextVariant.CAPTION,
                    color = Color.White
                )
            }
        }
        else -> {
            // 其他状态不显示指示器
        }
    }
}

/**
 * 简化的相机组件（用于快速集成）
 */
@Composable
fun UnifySimpleCamera(
    onPhotoTaken: ((ByteArray) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var cameraState by remember { mutableStateOf(UnifyCameraState.PREVIEW) }
    
    UnifyCamera(
        state = cameraState,
        onStateChanged = { cameraState = it },
        modifier = modifier,
        onCapturePhoto = {
            cameraState = UnifyCameraState.CAPTURING
            // 模拟拍照过程
            // 实际实现中会调用平台特定的相机API
            onPhotoTaken?.invoke(ByteArray(0))
            cameraState = UnifyCameraState.PREVIEW
        }
    )
}
