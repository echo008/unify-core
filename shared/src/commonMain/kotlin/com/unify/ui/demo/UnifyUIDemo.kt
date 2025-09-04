package com.unify.ui.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.ui.components.*
import com.unify.ui.components.advanced.*
import com.unify.ui.components.navigation.*
import com.unify.ui.components.ai.*
import com.unify.ui.components.media.*
import com.unify.ui.theme.UnifyTheme
import com.unify.ui.components.wearable.HealthMetric

/**
 * Unify UI组件演示应用
 * 展示所有UI组件的使用方法和效果
 */

data class DemoSection(
    val title: String,
    val description: String,
    val content: @Composable () -> Unit
)

@Composable
fun UnifyUIDemo() {
    var selectedSection by remember { mutableStateOf(0) }
    
    val demoSections = listOf(
        DemoSection(
            title = "基础组件",
            description = "按钮、文本、图片等基础UI组件"
        ) {
            BasicComponentsDemo()
        },
        DemoSection(
            title = "高级组件",
            description = "图表、日历、选择器等复杂组件"
        ) {
            AdvancedComponentsDemo()
        },
        DemoSection(
            title = "导航组件",
            description = "抽屉、导航栏、标签栏等导航组件"
        ) {
            NavigationComponentsDemo()
        },
        DemoSection(
            title = "平台特定",
            description = "AI、媒体、安全等平台特定组件"
        ) {
            PlatformSpecificDemo()
        }
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部导航
        UnifyTopAppBar(
            title = "Unify UI Demo",
            backgroundColor = Color(0xFF2196F3),
            contentColor = Color.White
        )
        
        Row(modifier = Modifier.fillMaxSize()) {
            // 侧边导航
            NavigationRail(
                modifier = Modifier.width(120.dp)
            ) {
                demoSections.forEachIndexed { index, section ->
                    NavigationRailItem(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        icon = {
                            Text(
                                text = when (index) {
                                    0 -> "🔧"
                                    1 -> "📊"
                                    2 -> "🧭"
                                    3 -> "🤖"
                                    else -> "📱"
                                }
                            )
                        },
                        label = { Text(section.title) }
                    )
                }
            }
            
            // 主内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val currentSection = demoSections[selectedSection]
                
                Text(
                    text = currentSection.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = currentSection.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                currentSection.content()
            }
        }
    }
}

@Composable
private fun BasicComponentsDemo() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DemoCard("按钮组件") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnifyButton(
                        onClick = { },
                        text = "主要按钮",
                        backgroundColor = Color(0xFF2196F3)
                    )
                    
                    UnifyIconButton(
                        onClick = { },
                        content = { Text("🔍") }
                    )
                    
                    UnifyFloatingActionButton(
                        onClick = { },
                        backgroundColor = Color(0xFF4CAF50),
                        content = { Text("+") }
                    )
                }
            }
        }
        
        item {
            DemoCard("文本组件") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnifyText(
                        text = "标题文本",
                        // style = UnifyTextStyle.Heading1 // 暂时注释，避免编译错误
                    )
                    
                    UnifyText(
                        text = "正文内容，支持多种样式和颜色配置",
                        // style = UnifyTextStyle.BODY // 暂时注释，避免编译错误
                    )
                    
                    UnifyText(
                        text = "小号文本",
                        // style = UnifyTextStyle.CAPTION, // 暂时注释，避免编译错误
                        color = Color(0xFFFFA500)
                    )
                }
            }
        }
        
        item {
            DemoCard("输入组件") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var textValue by remember { mutableStateOf("") }
                    
                    // UnifyTextField暂时注释
                    Text("文本输入框: $textValue")
                    
                    var switchValue by remember { mutableStateOf(false) }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("开关组件")
                        // UnifySwitch暂时注释
                        Text("开关状态: $switchValue")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedComponentsDemo() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DemoCard("图表组件") {
                val chartData = listOf(
                    ChartData("一月", 100f, Color.Blue),
                    ChartData("二月", 150f, Color.Green),
                    ChartData("三月", 120f, Color.Red),
                    ChartData("四月", 180f, Color(0xFFFFA500))
                )
                
                UnifyChart(
                    data = chartData,
                    chartType = ChartType.BAR,
                    modifier = Modifier.height(200.dp),
                    title = "月度数据"
                )
            }
        }
        
        item {
            DemoCard("日历组件") {
                UnifyCalendar(
                    modifier = Modifier.height(300.dp),
                    onDateSelected = { date ->
                        println("选择日期: $date")
                    }
                )
            }
        }
        
        item {
            DemoCard("选择器组件") {
                val pickerItems = listOf(
                    PickerItem("1", "选项一", "value1"),
                    PickerItem("2", "选项二", "value2"),
                    PickerItem("3", "选项三", "value3")
                )
                
                UnifyPicker(
                    items = pickerItems,
                    onSelectionChanged = { selected ->
                        println("选择项目: $selected")
                    },
                    title = "请选择选项"
                )
            }
        }
    }
}

@Composable
private fun NavigationComponentsDemo() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DemoCard("标签栏") {
                val tabItems = listOf(
                    TabItem("tab1", "首页"),
                    TabItem("tab2", "发现"),
                    TabItem("tab3", "我的")
                )
                
                var selectedTab by remember { mutableStateOf("tab1") }
                
                UnifyTabBar(
                    tabs = tabItems,
                    selectedTabId = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
        
        item {
            DemoCard("导航栏") {
                val navItems = listOf(
                    NavigationItem("nav1", "首页", icon = { Text("🏠") }),
                    NavigationItem("nav2", "搜索", icon = { Text("🔍") }),
                    NavigationItem("nav3", "设置", icon = { Text("⚙️") })
                )
                
                var selectedNav by remember { mutableStateOf("nav1") }
                
                UnifyBottomNavigationBar(
                    items = navItems,
                    selectedItemId = selectedNav,
                    onItemSelected = { selectedNav = it }
                )
            }
        }
    }
}

@Composable
private fun PlatformSpecificDemo() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DemoCard("AI组件") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("AI聊天组件")
                    
                    // AI聊天组件暂时注释
                    Text("AI聊天组件演示")
                    
                    val messages = listOf(
                        ChatMessage("1", "你好，我是AI助手", false, System.currentTimeMillis()),
                        ChatMessage("2", "你好！", true, System.currentTimeMillis())
                    )
                    
                    UnifyAIChat(
                        messages = messages,
                        onSendMessage = { message ->
                            println("发送消息: $message")
                        },
                        modifier = Modifier.height(200.dp)
                    )
                }
            }
        }
        
        item {
            DemoCard("媒体组件") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("媒体播放器")
                    
                    // 媒体播放器组件暂时注释
                    Text("媒体播放器组件演示")
                    
                    val mediaItem = MediaItem(
                        id = "1",
                        title = "示例视频",
                        url = "https://example.com/video.mp4",
                        type = MediaType.VIDEO
                    )
                    
                    UnifyVideoPlayer(
                        mediaItem = mediaItem,
                        modifier = Modifier.height(200.dp),
                        showControls = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            content()
        }
    }
}
