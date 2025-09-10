package com.unify.demo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import com.unify.core.utils.UnifyRuntimeUtils
import com.unify.core.utils.UnifyStringUtils
import com.unify.helloworld.PlatformInfo
import com.unify.helloworld.SimplePlatformInfo

/**
 * Unify跨平台演示应用
 * 展示所有核心功能和组件的使用示例
 */
@Composable
fun UnifyDemoApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val platformInfo = remember { SimplePlatformInfo().getCurrentPlatform() }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // 顶部应用栏
        DemoAppBar(platformInfo = platformInfo)

        // 标签栏
        DemoTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        // 内容区域
        when (selectedTab) {
            0 -> PlatformInfoDemo(platformInfo)
            1 -> UIComponentsDemo()
            2 -> DataManagementDemo()
            3 -> NetworkDemo()
            4 -> StorageDemo()
            5 -> PerformanceDemo()
        }
    }
}

/**
 * 演示应用顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoAppBar(platformInfo: PlatformInfo) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Unify Demo",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "运行在 ${platformInfo.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

/**
 * 演示标签栏
 */
@Composable
private fun DemoTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs =
        listOf(
            "平台信息" to Icons.Default.Info,
            "UI组件" to Icons.Default.Widgets,
            "数据管理" to Icons.Default.Storage,
            "网络" to Icons.Default.CloudQueue,
            "存储" to Icons.Default.Save,
            "性能" to Icons.Default.Speed,
        )

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        tabs.forEachIndexed { index, (title, icon) ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(title) },
                icon = { Icon(icon, contentDescription = title) },
            )
        }
    }
}

/**
 * 平台信息演示
 */
@Composable
private fun PlatformInfoDemo(platformInfo: PlatformInfo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DemoCard(
                title = "平台基本信息",
                icon = Icons.Default.PhoneAndroid,
            ) {
                InfoRow("平台名称", platformInfo.name)
                InfoRow("版本", platformInfo.version)
                InfoRow("架构", platformInfo.architecture)
                InfoRow("调试模式", if (platformInfo.isDebug) "是" else "否")
            }
        }

        item {
            DemoCard(
                title = "设备详细信息",
                icon = Icons.Default.DeviceHub,
            ) {
                Text(
                    text = platformInfo.deviceInfo,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        item {
            DemoCard(
                title = "运行时信息",
                icon = Icons.Default.Memory,
            ) {
                InfoRow("当前时间", getCurrentTimeMillis().toString())
                val threadCount = UnifyRuntimeUtils.getThreadCount().toString()
                InfoRow("线程数量", threadCount)
                InfoRow("最大内存", "${UnifyRuntimeUtils.getMaxMemory() / 1024 / 1024} MB")
                InfoRow("已用内存", "${(UnifyRuntimeUtils.getTotalMemory() - UnifyRuntimeUtils.getAvailableMemory()) / 1024 / 1024} MB")
            }
        }
    }
}

/**
 * UI组件演示
 */
@Composable
private fun UIComponentsDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DemoCard(
                title = "按钮组件",
                icon = Icons.Default.TouchApp,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("主要按钮")
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("次要按钮")
                    }

                    TextButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("文本按钮")
                    }
                }
            }
        }

        item {
            DemoCard(
                title = "输入组件",
                icon = Icons.Default.Edit,
            ) {
                var textValue by remember { mutableStateOf("") }
                var switchValue by remember { mutableStateOf(false) }
                var sliderValue by remember { mutableStateOf(0.5f) }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        label = { Text("文本输入") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("开关控件")
                        Switch(
                            checked = switchValue,
                            onCheckedChange = { switchValue = it },
                        )
                    }

                    Column {
                        Text("滑块控件: ${(sliderValue * 100).toInt()}%")
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        item {
            DemoCard(
                title = "图标展示",
                icon = Icons.Default.Star,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Icon(Icons.Default.Home, "首页", tint = Color.Blue)
                    Icon(Icons.Default.Search, "搜索", tint = Color.Green)
                    Icon(Icons.Default.Settings, "设置", tint = Color(0xFFFFA500))
                    Icon(Icons.Default.Person, "用户", tint = Color(0xFF800080))
                    Icon(Icons.Default.Favorite, "收藏", tint = Color.Red)
                }
            }
        }
    }
}

/**
 * 数据管理演示
 */
@Composable
private fun DataManagementDemo() {
    var dataOperationResult by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DemoCard(
                title = "数据操作",
                icon = Icons.Default.DataObject,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            dataOperationResult = "保存数据: 测试数据已保存到本地存储"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存数据")
                    }

                    Button(
                        onClick = {
                            dataOperationResult = "加载数据: 从本地存储加载了测试数据"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("加载数据")
                    }

                    Button(
                        onClick = {
                            dataOperationResult = "同步数据: 数据已同步到云端"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("同步数据")
                    }

                    if (dataOperationResult.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Text(
                                text = dataOperationResult,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 网络演示
 */
@Composable
private fun NetworkDemo() {
    var networkStatus by remember { mutableStateOf("未连接") }
    var networkResult by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DemoCard(
                title = "网络状态",
                icon = Icons.Default.Wifi,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoRow("连接状态", networkStatus)

                    Button(
                        onClick = {
                            networkStatus = "已连接 - WiFi"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("检查网络状态")
                    }
                }
            }
        }

        item {
            DemoCard(
                title = "网络请求",
                icon = Icons.Default.CloudDownload,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            networkResult = "GET请求成功: 获取到用户数据"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("发送GET请求")
                    }

                    Button(
                        onClick = {
                            networkResult = "POST请求成功: 数据已提交到服务器"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("发送POST请求")
                    }

                    if (networkResult.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Text(
                                text = networkResult,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 存储演示
 */
@Composable
private fun StorageDemo() {
    var storageInfo by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DemoCard(
                title = "存储管理",
                icon = Icons.Default.Folder,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            storageInfo = "本地存储: 已使用 15.2MB / 总计 128MB"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("检查存储空间")
                    }

                    Button(
                        onClick = {
                            storageInfo = "缓存清理: 已清理 5.8MB 缓存文件"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("清理缓存")
                    }

                    Button(
                        onClick = {
                            storageInfo = "数据备份: 备份文件已创建 (backup_${getCurrentTimeMillis()}.zip)"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("创建备份")
                    }

                    if (storageInfo.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Text(
                                text = storageInfo,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 性能演示
 */
@Composable
private fun PerformanceDemo() {
    var performanceResult by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DemoCard(
                title = "性能测试",
                icon = Icons.Default.Speed,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            val startTime = getCurrentTimeMillis()
                            // 模拟计算密集型任务
                            var sum = 0L
                            for (i in 1..1000000) {
                                sum += i
                            }
                            val endTime = getCurrentTimeMillis()
                            performanceResult = "CPU测试完成: 耗时 ${endTime - startTime}ms, 结果: $sum"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("CPU性能测试")
                    }

                    Button(
                        onClick = {
                            val totalMemory = UnifyRuntimeUtils.getTotalMemory()
                            val freeMemory = UnifyRuntimeUtils.getAvailableMemory()
                            val usedMemory = totalMemory - freeMemory
                            val memoryUsage = (usedMemory.toDouble() / totalMemory.toDouble()) * 100
                            performanceResult = "内存使用: $usedMemory MB / $totalMemory MB (${com.unify.core.utils.UnifyStringUtils.format("%.2f", memoryUsage.toFloat())}%)"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("内存使用检测")
                    }

                    Button(
                        onClick = {
                            val startTime = getNanoTime()
                            // 模拟IO操作延迟
                            val threadCount = UnifyRuntimeUtils.getThreadCount()
                            val endTime = getNanoTime()
                            val latency = (endTime - startTime) / 1_000_000.0
                            performanceResult = "IO延迟测试: ${com.unify.core.utils.UnifyStringUtils.format("%.2f", latency.toFloat())} ms"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("IO延迟测试")
                    }

                    if (performanceResult.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Text(
                                text = performanceResult,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 演示卡片组件
 */
@Composable
private fun DemoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            content()
        }
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
