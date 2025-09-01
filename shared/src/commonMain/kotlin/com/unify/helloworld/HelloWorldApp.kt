package com.unify.helloworld

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.ui.components.platform.*

/**
 * Unify-Core Hello World 演示应用
 * 展示跨平台组件和平台特定适配
 */
@Composable
fun HelloWorldApp() {
    MaterialTheme {
        HelloWorldContent()
    }
}

@Composable
fun HelloWorldContent() {
    var count by remember { mutableIntStateOf(0) }
    var textInput by remember { mutableStateOf("") }
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    var switchChecked by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    
    val navigationItems = listOf(
        NavigationItem("首页", "🏠"),
        NavigationItem("组件", "🧩"),
        NavigationItem("设置", "⚙️")
    )
    
    val demoItems = listOf(
        "平台特定按钮演示",
        "输入框交互演示", 
        "列表组件演示",
        "滑块控件演示",
        "开关组件演示",
        "对话框演示"
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主要内容区域
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题卡片
            item {
                PlatformSpecificCard {
                    Text(
                        text = "🚀 Unify-Core",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kotlin Multiplatform Compose 跨平台框架",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "平台: ${getPlatformName()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            when (selectedTab) {
                0 -> {
                    // 首页 - 计数器演示
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "计数器演示",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "当前计数: $count",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PlatformSpecificButton(
                                    text = "减少",
                                    onClick = { count-- },
                                    modifier = Modifier.weight(1f)
                                )
                                PlatformSpecificButton(
                                    text = "增加", 
                                    onClick = { count++ },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            PlatformSpecificButton(
                                text = "重置计数器",
                                onClick = { count = 0 },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                1 -> {
                    // 组件演示页面
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "输入框演示",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            PlatformSpecificTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "输入文本",
                                placeholder = "请输入内容...",
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (textInput.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("您输入的内容: $textInput")
                            }
                        }
                    }
                    
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "滑块演示",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("当前值: ${(sliderValue * 100).toInt()}%")
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            PlatformSpecificSlider(
                                value = sliderValue,
                                onValueChange = { sliderValue = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "开关演示",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("启用功能: ${if (switchChecked) "开启" else "关闭"}")
                                PlatformSpecificSwitch(
                                    checked = switchChecked,
                                    onCheckedChange = { switchChecked = it }
                                )
                            }
                        }
                    }
                    
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "列表演示",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        PlatformSpecificList(
                            items = demoItems,
                            onItemClick = { item ->
                                if (item.contains("对话框")) {
                                    showDialog = true
                                }
                            }
                        )
                    }
                }
                
                2 -> {
                    // 设置页面
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "应用设置",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("这里是设置页面的内容")
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            PlatformSpecificButton(
                                text = "显示对话框",
                                onClick = { showDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    item {
                        PlatformSpecificCard {
                            Text(
                                text = "图片演示",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            PlatformSpecificImage(
                                url = "https://example.com/image.jpg",
                                contentDescription = "演示图片",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // 底部导航栏
        PlatformSpecificNavigationBar(
            items = navigationItems,
            selectedIndex = selectedTab,
            onItemSelected = { selectedTab = it }
        )
    }
    
    // 对话框
    if (showDialog) {
        PlatformSpecificDialog(
            title = "演示对话框",
            content = "这是一个跨平台的对话框组件演示，在不同平台上会有相应的原生样式。",
            onConfirm = { 
                showDialog = false
                count += 10
            },
            onDismiss = { showDialog = false }
        )
    }
    
    // 加载指示器演示
    PlatformSpecificLoadingIndicator(
        isLoading = false // 可以根据需要控制显示
    )
}

expect fun getPlatformName(): String
