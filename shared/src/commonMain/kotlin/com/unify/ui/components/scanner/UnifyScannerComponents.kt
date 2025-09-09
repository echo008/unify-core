package com.unify.ui.components.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.platform.getCurrentTimeMillis

/**
 * 跨平台统一扫描器组件系统
 * 支持二维码、条形码、文档、图像等多种扫描功能
 */

/**
 * 统一扫描器组件
 */
@Composable
fun UnifyScannerView(
    scannerType: ScannerType = ScannerType.QR_CODE,
    onScanResult: (ScanResult) -> Unit = {},
    onScanError: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanHistory by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var currentResult by remember { mutableStateOf<ScanResult?>(null) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            try {
                val result = performScan(scannerType)
                currentResult = result
                scanHistory = scanHistory + result
                onScanResult(result)
                isScanning = false
            } catch (e: Exception) {
                onScanError(e.message ?: "扫描失败")
                isScanning = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 扫描器标题和控制
        ScannerHeader(
            scannerType = scannerType,
            isScanning = isScanning,
            onStartScan = { isScanning = true },
            onStopScan = { isScanning = false },
        )

        // 扫描预览区域
        ScannerPreview(
            scannerType = scannerType,
            isScanning = isScanning,
            currentResult = currentResult,
            modifier = Modifier.weight(1f),
        )

        // 扫描结果显示
        currentResult?.let { result ->
            ScanResultCard(
                result = result,
                onAction = { action ->
                    handleScanResultAction(result, action)
                },
            )
        }

        // 扫描历史
        if (scanHistory.isNotEmpty()) {
            ScanHistorySection(
                history = scanHistory,
                onClearHistory = { scanHistory = emptyList() },
            )
        }
    }
}

/**
 * 扫描器头部组件
 */
@Composable
private fun ScannerHeader(
    scannerType: ScannerType,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = getScannerTypeDisplayName(scannerType),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = getScannerTypeDescription(scannerType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isScanning) {
            Button(
                onClick = onStopScan,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text("停止扫描")
            }
        } else {
            Button(
                onClick = onStartScan,
            ) {
                Text("开始扫描")
            }
        }
    }
}

/**
 * 扫描预览组件
 */
@Composable
private fun ScannerPreview(
    scannerType: ScannerType,
    isScanning: Boolean,
    currentResult: ScanResult?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isScanning -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 6.dp,
                        )
                        Text(
                            text = "正在扫描${getScannerTypeDisplayName(scannerType)}...",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = getScannerInstructions(scannerType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                currentResult != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 48.sp,
                        )
                        Text(
                            text = "扫描成功",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Green,
                        )
                    }
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = getScannerTypeIcon(scannerType),
                            fontSize = 64.sp,
                        )
                        Text(
                            text = "点击开始扫描",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 扫描结果卡片组件
 */
@Composable
private fun ScanResultCard(
    result: ScanResult,
    onAction: (ScanResultAction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "扫描结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = result.type.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = result.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            if (result.metadata.isNotEmpty()) {
                Text(
                    text = "附加信息: ${result.metadata}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onAction(ScanResultAction.Copy) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("复制")
                }

                OutlinedButton(
                    onClick = { onAction(ScanResultAction.Share) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("分享")
                }

                if (result.type == ScanResultType.URL) {
                    OutlinedButton(
                        onClick = { onAction(ScanResultAction.Open) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("打开")
                    }
                }
            }
        }
    }
}

/**
 * 扫描历史区域组件
 */
@Composable
private fun ScanHistorySection(
    history: List<ScanResult>,
    onClearHistory: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "扫描历史 (${history.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            TextButton(
                onClick = onClearHistory,
            ) {
                Text("清空")
            }
        }

        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(history.takeLast(5).reversed()) { result ->
                ScanHistoryItem(result = result)
            }
        }
    }
}

/**
 * 扫描历史项组件
 */
@Composable
private fun ScanHistoryItem(result: ScanResult) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = result.content.take(30) + if (result.content.length > 30) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = result.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = formatTimestamp(result.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 多功能扫描器组件
 */
@Composable
fun UnifyMultiScanner(
    availableTypes: List<ScannerType> = ScannerType.values().toList(),
    onScanResult: (ScanResult) -> Unit = {},
    onScanError: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedType by remember { mutableStateOf(availableTypes.firstOrNull() ?: ScannerType.QR_CODE) }
    var showTypeSelector by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 扫描类型选择器
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "扫描类型",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedButton(
                onClick = { showTypeSelector = true },
            ) {
                Text("${getScannerTypeIcon(selectedType)} ${getScannerTypeDisplayName(selectedType)}")
            }
        }

        // 扫描器视图
        UnifyScannerView(
            scannerType = selectedType,
            onScanResult = onScanResult,
            onScanError = onScanError,
            modifier = Modifier.weight(1f),
        )
    }

    // 类型选择对话框
    if (showTypeSelector) {
        AlertDialog(
            onDismissRequest = { showTypeSelector = false },
            title = { Text("选择扫描类型") },
            text = {
                LazyColumn {
                    items(availableTypes) { type ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    showTypeSelector = false
                                },
                            )
                            Text(getScannerTypeIcon(type))
                            Column {
                                Text(
                                    text = getScannerTypeDisplayName(type),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = getScannerTypeDescription(type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTypeSelector = false },
                ) {
                    Text("取消")
                }
            },
        )
    }
}

/**
 * 批量扫描组件
 */
@Composable
fun UnifyBatchScanner(
    scannerType: ScannerType = ScannerType.QR_CODE,
    maxItems: Int = 10,
    onBatchComplete: (List<ScanResult>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var batchResults by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 批量扫描头部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "批量扫描",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "已扫描 ${batchResults.size}/$maxItems",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (batchResults.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            onBatchComplete(batchResults)
                            batchResults = emptyList()
                        },
                    ) {
                        Text("完成")
                    }
                }

                Button(
                    onClick = { isScanning = !isScanning },
                    enabled = batchResults.size < maxItems,
                ) {
                    Text(if (isScanning) "停止" else "继续扫描")
                }
            }
        }

        // 进度指示器
        LinearProgressIndicator(
            progress = { batchResults.size.toFloat() / maxItems.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )

        // 扫描器
        UnifyScannerView(
            scannerType = scannerType,
            onScanResult = { result ->
                if (batchResults.size < maxItems && !batchResults.any { it.content == result.content }) {
                    batchResults = batchResults + result
                    if (batchResults.size >= maxItems) {
                        isScanning = false
                        onBatchComplete(batchResults)
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )

        // 批量结果列表
        if (batchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(batchResults) { result ->
                    BatchResultItem(
                        result = result,
                        onRemove = {
                            batchResults = batchResults.filter { it != result }
                        },
                    )
                }
            }
        }
    }
}

/**
 * 批量结果项组件
 */
@Composable
private fun BatchResultItem(
    result: ScanResult,
    onRemove: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = result.content.take(40) + if (result.content.length > 40) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = result.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onRemove,
            ) {
                Text("✕")
            }
        }
    }
}

/**
 * 执行扫描操作（平台特定实现）
 */
expect suspend fun performScan(scannerType: ScannerType): ScanResult

/**
 * 处理扫描结果操作（平台特定实现）
 */
expect fun handleScanResultAction(
    result: ScanResult,
    action: ScanResultAction,
)

/**
 * 扫描器类型
 */
enum class ScannerType {
    QR_CODE,
    BARCODE,
    DOCUMENT,
    IMAGE,
    NFC,
    BUSINESS_CARD,
    ID_CARD,
    RECEIPT,
}

/**
 * 扫描结果类型
 */
enum class ScanResultType {
    TEXT,
    URL,
    EMAIL,
    PHONE,
    SMS,
    WIFI,
    CONTACT,
    LOCATION,
    PRODUCT,
    DOCUMENT,
    IMAGE,
}

/**
 * 扫描结果
 */
data class ScanResult(
    val type: ScanResultType,
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = getCurrentTimeMillis(),
    val confidence: Float = 1.0f,
)

/**
 * 扫描结果操作
 */
enum class ScanResultAction {
    Copy,
    Share,
    Open,
    Save,
    Edit,
    Delete,
}

/**
 * 获取扫描器类型显示名称
 */
private fun getScannerTypeDisplayName(type: ScannerType): String {
    return when (type) {
        ScannerType.QR_CODE -> "二维码"
        ScannerType.BARCODE -> "条形码"
        ScannerType.DOCUMENT -> "文档"
        ScannerType.IMAGE -> "图像"
        ScannerType.NFC -> "NFC"
        ScannerType.BUSINESS_CARD -> "名片"
        ScannerType.ID_CARD -> "身份证"
        ScannerType.RECEIPT -> "收据"
    }
}

/**
 * 获取扫描器类型描述
 */
private fun getScannerTypeDescription(type: ScannerType): String {
    return when (type) {
        ScannerType.QR_CODE -> "扫描二维码获取信息"
        ScannerType.BARCODE -> "扫描商品条形码"
        ScannerType.DOCUMENT -> "扫描文档并识别文字"
        ScannerType.IMAGE -> "扫描图像并分析内容"
        ScannerType.NFC -> "读取NFC标签信息"
        ScannerType.BUSINESS_CARD -> "扫描名片提取联系信息"
        ScannerType.ID_CARD -> "扫描身份证件"
        ScannerType.RECEIPT -> "扫描收据提取信息"
    }
}

/**
 * 获取扫描器类型图标
 */
private fun getScannerTypeIcon(type: ScannerType): String {
    return when (type) {
        ScannerType.QR_CODE -> "📱"
        ScannerType.BARCODE -> "📊"
        ScannerType.DOCUMENT -> "📄"
        ScannerType.IMAGE -> "🖼️"
        ScannerType.NFC -> "📡"
        ScannerType.BUSINESS_CARD -> "💼"
        ScannerType.ID_CARD -> "🆔"
        ScannerType.RECEIPT -> "🧾"
    }
}

/**
 * 获取扫描器使用说明
 */
private fun getScannerInstructions(type: ScannerType): String {
    return when (type) {
        ScannerType.QR_CODE -> "将二维码对准扫描框"
        ScannerType.BARCODE -> "将条形码对准扫描框"
        ScannerType.DOCUMENT -> "将文档平放在扫描区域"
        ScannerType.IMAGE -> "将图像对准扫描框"
        ScannerType.NFC -> "将设备靠近NFC标签"
        ScannerType.BUSINESS_CARD -> "将名片平放在扫描区域"
        ScannerType.ID_CARD -> "将身份证件平放在扫描区域"
        ScannerType.RECEIPT -> "将收据平放在扫描区域"
    }
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = getCurrentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}
