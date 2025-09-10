package com.unify.ui.components.open

import androidx.compose.foundation.background
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
import com.unify.core.utils.UnifyStringUtils

/**
 * Unify开放组件库
 * 提供可扩展和自定义的开放式组件
 */

/**
 * 开放式插件容器组件
 */
@Composable
fun UnifyPluginContainer(
    plugins: List<UnifyPlugin>,
    modifier: Modifier = Modifier,
    onPluginAction: (String, String) -> Unit = { _, _ -> },
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = "插件管理器",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        items(plugins) { plugin ->
            UnifyPluginCard(
                plugin = plugin,
                onAction = { action -> onPluginAction(plugin.id, action) },
            )
        }
    }
}

/**
 * 插件卡片组件
 */
@Composable
private fun UnifyPluginCard(
    plugin: UnifyPlugin,
    onAction: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (plugin.enabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = plugin.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Switch(
                    checked = plugin.enabled,
                    onCheckedChange = {
                        onAction(if (it) "enable" else "disable")
                    },
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = { onAction("configure") },
                    label = { Text("配置") },
                )
                AssistChip(
                    onClick = { onAction("info") },
                    label = { Text("详情") },
                )
                if (plugin.hasUpdate) {
                    AssistChip(
                        onClick = { onAction("update") },
                        label = { Text("更新") },
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                    )
                }
            }
        }
    }
}

/**
 * 开放式主题定制器
 */
@Composable
fun UnifyThemeCustomizer(
    currentTheme: UnifyCustomTheme,
    onThemeChange: (UnifyCustomTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    var theme by remember { mutableStateOf(currentTheme) }

    LaunchedEffect(theme) {
        onThemeChange(theme)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = "主题定制器",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "颜色配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )

                    ColorPickerRow(
                        label = "主色调",
                        color = theme.primaryColor,
                        onColorChange = { theme = theme.copy(primaryColor = it) },
                    )

                    ColorPickerRow(
                        label = "次要色调",
                        color = theme.secondaryColor,
                        onColorChange = { theme = theme.copy(secondaryColor = it) },
                    )

                    ColorPickerRow(
                        label = "背景色",
                        color = theme.backgroundColor,
                        onColorChange = { theme = theme.copy(backgroundColor = it) },
                    )
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "字体配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )

                    SliderRow(
                        label = "字体大小",
                        value = theme.fontSize,
                        range = 12f..24f,
                        onValueChange = { theme = theme.copy(fontSize = it) },
                    )

                    SliderRow(
                        label = "行间距",
                        value = theme.lineHeight,
                        range = 1.0f..2.0f,
                        onValueChange = { theme = theme.copy(lineHeight = it) },
                    )
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "布局配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )

                    SliderRow(
                        label = "圆角半径",
                        value = theme.cornerRadius,
                        range = 0f..24f,
                        onValueChange = { theme = theme.copy(cornerRadius = it) },
                    )

                    SliderRow(
                        label = "内边距",
                        value = theme.padding,
                        range = 4f..32f,
                        onValueChange = { theme = theme.copy(padding = it) },
                    )
                }
            }
        }
    }
}

/**
 * 开放式扩展管理器
 */
@Composable
fun UnifyExtensionManager(
    extensions: List<UnifyExtension>,
    modifier: Modifier = Modifier,
    onExtensionAction: (String, ExtensionAction) -> Unit = { _, _ -> },
) {
    var selectedCategory by remember { mutableStateOf("全部") }
    val categories = listOf("全部", "UI组件", "数据处理", "网络", "工具", "其他")

    Column(modifier = modifier.fillMaxSize()) {
        // 分类筛选
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            modifier = Modifier.fillMaxWidth(),
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category) },
                )
            }
        }

        // 扩展列表
        val filteredExtensions =
            if (selectedCategory == "全部") {
                extensions
            } else {
                extensions.filter { it.category == selectedCategory }
            }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(filteredExtensions) { extension ->
                UnifyExtensionCard(
                    extension = extension,
                    onAction = { action -> onExtensionAction(extension.id, action) },
                )
            }
        }
    }
}

/**
 * 扩展卡片组件
 */
@Composable
private fun UnifyExtensionCard(
    extension: UnifyExtension,
    onAction: (ExtensionAction) -> Unit,
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
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = extension.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = extension.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "版本: ${extension.version} | 作者: ${extension.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                ExtensionStatusBadge(extension.status)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (extension.status) {
                    ExtensionStatus.NOT_INSTALLED -> {
                        Button(
                            onClick = { onAction(ExtensionAction.INSTALL) },
                        ) {
                            Text("安装")
                        }
                    }
                    ExtensionStatus.INSTALLED -> {
                        OutlinedButton(
                            onClick = { onAction(ExtensionAction.UNINSTALL) },
                        ) {
                            Text("卸载")
                        }
                        Button(
                            onClick = { onAction(ExtensionAction.CONFIGURE) },
                        ) {
                            Text("配置")
                        }
                    }
                    ExtensionStatus.UPDATE_AVAILABLE -> {
                        Button(
                            onClick = { onAction(ExtensionAction.UPDATE) },
                        ) {
                            Text("更新")
                        }
                    }
                    ExtensionStatus.LOADING -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * 开放式API集成器
 */
@Composable
fun UnifyAPIIntegrator(
    apis: List<UnifyAPIConfig>,
    modifier: Modifier = Modifier,
    onAPIAction: (String, APIAction) -> Unit = { _, _ -> },
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = "API集成管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        items(apis) { api ->
            UnifyAPICard(
                api = api,
                onAction = { action -> onAPIAction(api.id, action) },
            )
        }
    }
}

/**
 * API配置卡片
 */
@Composable
private fun UnifyAPICard(
    api: UnifyAPIConfig,
    onAction: (APIAction) -> Unit,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = api.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = api.endpoint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                APIStatusIndicator(api.status)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onAction(APIAction.TEST) },
                ) {
                    Text("测试")
                }
                Button(
                    onClick = { onAction(APIAction.CONFIGURE) },
                ) {
                    Text("配置")
                }
                if (api.status == APIStatus.CONNECTED) {
                    AssistChip(
                        onClick = { onAction(APIAction.DISCONNECT) },
                        label = { Text("断开") },
                    )
                } else {
                    AssistChip(
                        onClick = { onAction(APIAction.CONNECT) },
                        label = { Text("连接") },
                    )
                }
            }
        }
    }
}

// 辅助组件
@Composable
private fun ColorPickerRow(
    label: String,
    color: Color,
    onColorChange: (Color) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        // 简化的颜色选择器
        Button(
            onClick = { /* 打开颜色选择器 */ },
            colors = ButtonDefaults.buttonColors(containerColor = color),
        ) {
            Text("选择", color = Color.White)
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label)
            Text(text = com.unify.core.utils.UnifyStringUtils.format("%.1f", value))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
        )
    }
}

@Composable
private fun ExtensionStatusBadge(status: ExtensionStatus) {
    val (text, color) =
        when (status) {
            ExtensionStatus.NOT_INSTALLED -> "未安装" to Color.Gray
            ExtensionStatus.INSTALLED -> "已安装" to Color.Green
            ExtensionStatus.UPDATE_AVAILABLE -> "有更新" to Color(0xFFFF9800)
            ExtensionStatus.LOADING -> "加载中" to Color.Blue
        }

    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = color.copy(alpha = 0.2f),
                labelColor = color,
            ),
    )
}

@Composable
private fun APIStatusIndicator(status: APIStatus) {
    val (text, color) =
        when (status) {
            APIStatus.CONNECTED -> "已连接" to Color.Green
            APIStatus.DISCONNECTED -> "未连接" to Color.Gray
            APIStatus.ERROR -> "错误" to Color.Red
            APIStatus.CONNECTING -> "连接中" to Color.Blue
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

// 数据类和枚举
data class UnifyPlugin(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val enabled: Boolean,
    val hasUpdate: Boolean = false,
)

data class UnifyCustomTheme(
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val fontSize: Float,
    val lineHeight: Float,
    val cornerRadius: Float,
    val padding: Float,
)

data class UnifyExtension(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val category: String,
    val status: ExtensionStatus,
)

enum class ExtensionStatus {
    NOT_INSTALLED,
    INSTALLED,
    UPDATE_AVAILABLE,
    LOADING,
}

enum class ExtensionAction {
    INSTALL,
    UNINSTALL,
    UPDATE,
    CONFIGURE,
}

data class UnifyAPIConfig(
    val id: String,
    val name: String,
    val endpoint: String,
    val apiKey: String,
    val status: APIStatus,
)

enum class APIStatus {
    CONNECTED,
    DISCONNECTED,
    ERROR,
    CONNECTING,
}

enum class APIAction {
    CONNECT,
    DISCONNECT,
    TEST,
    CONFIGURE,
}
