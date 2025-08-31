package com.unify.ui.components.scanner

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 扫码类型
 */
enum class UnifyScanType {
    QRCODE,         // 二维码
    BARCODE,        // 条形码
    DATAMATRIX,     // Data Matrix码
    PDF417,         // PDF417码
    AZTEC,          // Aztec码
    ALL             // 所有类型
}

/**
 * 扫码结果
 */
data class UnifyScanResult(
    val type: UnifyScanType,
    val result: String,
    val rawData: ByteArray? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnifyScanResult) return false
        
        if (type != other.type) return false
        if (result != other.result) return false
        if (rawData != null) {
            if (other.rawData == null) return false
            if (!rawData.contentEquals(other.rawData)) return false
        } else if (other.rawData != null) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result1 = type.hashCode()
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + (rawData?.contentHashCode() ?: 0)
        result1 = 31 * result1 + timestamp.hashCode()
        return result1
    }
}

/**
 * 扫码配置
 */
data class UnifyScanConfig(
    val scanType: Set<UnifyScanType> = setOf(UnifyScanType.ALL),
    val enableFlash: Boolean = true,
    val enableZoom: Boolean = true,
    val enableAlbum: Boolean = true,
    val enableSound: Boolean = true,
    val enableVibrate: Boolean = true,
    val autoFocus: Boolean = true,
    val scanArea: Float = 0.7f,              // 扫描区域占屏幕比例
    val maskColor: Color = Color.Black.copy(alpha = 0.5f),
    val borderColor: Color = Color.White,
    val borderWidth: Dp = 2.dp,
    val cornerLength: Dp = 20.dp,
    val cornerWidth: Dp = 4.dp,
    val scanLineColor: Color = Color.Green,
    val scanLineHeight: Dp = 2.dp,
    val animationDuration: Int = 2000,       // 扫描线动画时长(ms)
    val tipText: String = "将二维码/条码放入框内，即可自动扫描",
    val tipTextColor: Color = Color.White,
    val resultDelay: Long = 1000L            // 扫描成功后延迟时间(ms)
)

/**
 * 扫码器状态
 */
enum class UnifyScannerState {
    IDLE,           // 空闲
    SCANNING,       // 扫描中
    SUCCESS,        // 扫描成功
    ERROR,          // 扫描失败
    PERMISSION_DENIED // 权限被拒绝
}

/**
 * 二维码/条码扫描器组件
 */
@Composable
fun UnifyScanner(
    modifier: Modifier = Modifier,
    config: UnifyScanConfig = UnifyScanConfig(),
    onScanResult: ((UnifyScanResult) -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    onPermissionDenied: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var scannerState by remember { mutableStateOf(UnifyScannerState.IDLE) }
    var isFlashOn by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(1f) }
    var scanResult by remember { mutableStateOf<UnifyScanResult?>(null) }
    
    // 扫描线动画
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(config.animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLineOffset"
    )
    
    LaunchedEffect(Unit) {
        // 模拟权限检查和相机初始化
        delay(500)
        scannerState = UnifyScannerState.SCANNING
        
        // 模拟扫描过程
        delay(3000)
        val mockResult = UnifyScanResult(
            type = UnifyScanType.QRCODE,
            result = "https://example.com/qrcode-content"
        )
        scanResult = mockResult
        scannerState = UnifyScannerState.SUCCESS
        onScanResult?.invoke(mockResult)
        
        delay(config.resultDelay)
        scannerState = UnifyScannerState.SCANNING
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role = Role.Button
            }
    ) {
        // 相机预览区域（模拟）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF424242),
                            Color(0xFF212121)
                        )
                    )
                )
        ) {
            UnifyText(
                text = "相机预览",
                color = Color.Gray,
                variant = UnifyTextVariant.H6,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 扫描遮罩和框架
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val scanAreaSize = minOf(canvasWidth, canvasHeight) * config.scanArea
            val left = (canvasWidth - scanAreaSize) / 2
            val top = (canvasHeight - scanAreaSize) / 2
            val right = left + scanAreaSize
            val bottom = top + scanAreaSize
            
            // 绘制遮罩
            drawRect(
                color = config.maskColor,
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(canvasWidth, top)
            )
            drawRect(
                color = config.maskColor,
                topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                size = androidx.compose.ui.geometry.Size(left, scanAreaSize)
            )
            drawRect(
                color = config.maskColor,
                topLeft = androidx.compose.ui.geometry.Offset(right, top),
                size = androidx.compose.ui.geometry.Size(canvasWidth - right, scanAreaSize)
            )
            drawRect(
                color = config.maskColor,
                topLeft = androidx.compose.ui.geometry.Offset(0f, bottom),
                size = androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight - bottom)
            )
            
            // 绘制扫描框边框
            val borderWidthPx = config.borderWidth.toPx()
            val cornerLengthPx = config.cornerLength.toPx()
            val cornerWidthPx = config.cornerWidth.toPx()
            
            // 四个角的线条
            // 左上角
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(cornerLengthPx, cornerWidthPx)
            )
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(cornerWidthPx, cornerLengthPx)
            )
            
            // 右上角
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(right - cornerLengthPx, top),
                size = androidx.compose.ui.geometry.Size(cornerLengthPx, cornerWidthPx)
            )
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(right - cornerWidthPx, top),
                size = androidx.compose.ui.geometry.Size(cornerWidthPx, cornerLengthPx)
            )
            
            // 左下角
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, bottom - cornerWidthPx),
                size = androidx.compose.ui.geometry.Size(cornerLengthPx, cornerWidthPx)
            )
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, bottom - cornerLengthPx),
                size = androidx.compose.ui.geometry.Size(cornerWidthPx, cornerLengthPx)
            )
            
            // 右下角
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(right - cornerLengthPx, bottom - cornerWidthPx),
                size = androidx.compose.ui.geometry.Size(cornerLengthPx, cornerWidthPx)
            )
            drawRect(
                color = config.borderColor,
                topLeft = androidx.compose.ui.geometry.Offset(right - cornerWidthPx, bottom - cornerLengthPx),
                size = androidx.compose.ui.geometry.Size(cornerWidthPx, cornerLengthPx)
            )
            
            // 绘制扫描线
            if (scannerState == UnifyScannerState.SCANNING) {
                val scanLineY = top + (bottom - top) * scanLineOffset
                val scanLineHeightPx = config.scanLineHeight.toPx()
                
                drawRect(
                    color = config.scanLineColor,
                    topLeft = androidx.compose.ui.geometry.Offset(left + cornerWidthPx, scanLineY),
                    size = androidx.compose.ui.geometry.Size(scanAreaSize - 2 * cornerWidthPx, scanLineHeightPx)
                )
            }
        }
        
        // 提示文本
        UnifyText(
            text = config.tipText,
            color = config.tipTextColor,
            variant = UnifyTextVariant.BODY_MEDIUM,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth()
        )
        
        // 顶部控制栏
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 返回按钮
            IconButton(
                onClick = { /* 返回 */ }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            
            Row {
                // 闪光灯控制
                if (config.enableFlash) {
                    IconButton(
                        onClick = { isFlashOn = !isFlashOn }
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (isFlashOn) "关闭闪光灯" else "开启闪光灯",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }
                }
                
                // 相册选择
                if (config.enableAlbum) {
                    IconButton(
                        onClick = { /* 从相册选择 */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "从相册选择",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // 扫描成功提示
        if (scannerState == UnifyScannerState.SUCCESS && scanResult != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        Color.Green.copy(alpha = 0.9f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "扫描成功",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifyText(
                        text = "扫描成功",
                        color = Color.White,
                        variant = UnifyTextVariant.H6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    UnifyText(
                        text = scanResult!!.result,
                        color = Color.White,
                        variant = UnifyTextVariant.BODY_SMALL,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
        
        // 扫描失败提示
        if (scannerState == UnifyScannerState.ERROR) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        Color.Red.copy(alpha = 0.9f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "扫描失败",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifyText(
                        text = "扫描失败",
                        color = Color.White,
                        variant = UnifyTextVariant.H6,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 二维码生成器组件
 */
@Composable
fun UnifyQRCodeGenerator(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    backgroundColor: Color = Color.White,
    foregroundColor: Color = Color.Black,
    errorCorrectionLevel: String = "M", // L, M, Q, H
    margin: Int = 1,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        contentAlignment = Alignment.Center
    ) {
        // 模拟二维码图案
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.toPx() - 32.dp.toPx() // 减去padding
            val blockSize = canvasSize / 25 // 25x25的网格
            
            // 绘制模拟的二维码图案
            for (i in 0 until 25) {
                for (j in 0 until 25) {
                    // 使用内容的哈希值来决定是否绘制方块
                    val shouldDraw = (content.hashCode() + i * 25 + j) % 3 == 0
                    if (shouldDraw) {
                        drawRect(
                            color = foregroundColor,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                i * blockSize,
                                j * blockSize
                            ),
                            size = androidx.compose.ui.geometry.Size(blockSize * 0.8f, blockSize * 0.8f)
                        )
                    }
                }
            }
            
            // 绘制三个定位角
            val cornerSize = blockSize * 7
            val positions = listOf(
                androidx.compose.ui.geometry.Offset(0f, 0f),
                androidx.compose.ui.geometry.Offset(canvasSize - cornerSize, 0f),
                androidx.compose.ui.geometry.Offset(0f, canvasSize - cornerSize)
            )
            
            positions.forEach { pos ->
                // 外框
                drawRect(
                    color = foregroundColor,
                    topLeft = pos,
                    size = androidx.compose.ui.geometry.Size(cornerSize, cornerSize)
                )
                // 内框（白色）
                drawRect(
                    color = backgroundColor,
                    topLeft = androidx.compose.ui.geometry.Offset(pos.x + blockSize, pos.y + blockSize),
                    size = androidx.compose.ui.geometry.Size(cornerSize - 2 * blockSize, cornerSize - 2 * blockSize)
                )
                // 中心点
                drawRect(
                    color = foregroundColor,
                    topLeft = androidx.compose.ui.geometry.Offset(pos.x + 2 * blockSize, pos.y + 2 * blockSize),
                    size = androidx.compose.ui.geometry.Size(3 * blockSize, 3 * blockSize)
                )
            }
        }
    }
}

/**
 * 条形码生成器组件
 */
@Composable
fun UnifyBarcodeGenerator(
    content: String,
    modifier: Modifier = Modifier,
    width: Dp = 300.dp,
    height: Dp = 100.dp,
    backgroundColor: Color = Color.White,
    foregroundColor: Color = Color.Black,
    showText: Boolean = true,
    contentDescription: String? = null
) {
    Column(
        modifier = modifier
            .width(width)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 条形码图案
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = 50 // 条形码条数
            val barWidth = canvasWidth / barCount
            
            // 绘制条形码条纹
            for (i in 0 until barCount) {
                // 使用内容的哈希值来决定条纹宽度和间隔
                val shouldDraw = (content.hashCode() + i) % 3 != 0
                if (shouldDraw) {
                    val barThickness = if ((content.hashCode() + i) % 2 == 0) barWidth * 0.5f else barWidth * 0.8f
                    drawRect(
                        color = foregroundColor,
                        topLeft = androidx.compose.ui.geometry.Offset(i * barWidth, 0f),
                        size = androidx.compose.ui.geometry.Size(barThickness, canvasHeight)
                    )
                }
            }
        }
        
        // 显示文本
        if (showText) {
            Spacer(modifier = Modifier.height(8.dp))
            UnifyText(
                text = content,
                color = foregroundColor,
                variant = UnifyTextVariant.BODY_SMALL,
                textAlign = TextAlign.Center
            )
        }
    }
}
