package com.unify.ui.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import com.unify.ui.components.advanced.*
import com.unify.ui.responsive.*
import com.unify.ui.accessibility.*
import kotlinx.datetime.*

/**
 * Unify UI 高级功能综合演示
 * 展示高级组件、响应式设计和无障碍支持的完整示例
 */

/**
 * 完整的高级演示应用
 */
@Composable
fun UnifyAdvancedDemo() {
    UnifyThemeProvider {
        UnifyResponsiveProvider {
            UnifyAccessibilityProvider {
                var selectedTab by remember { mutableStateOf(0) }
                
                val tabs = listOf(
                    "高级组件" to { AdvancedComponentsDemo() },
                    "响应式设计" to { ResponsiveDesignDemo() },
                    "无障碍支持" to { AccessibilityDemo() },
                    "综合示例" to { ComprehensiveDemo() }
                )
                
                Column(modifier = Modifier.fillMaxSize()) {
                    // 顶部导航栏
                    UnifyTopNavigationBar(
                        title = "Unify UI 高级演示",
                        navigationIcon = Icons.Default.ArrowBack,
                        onNavigationClick = { /* 返回 */ }
                    )
                    
                    // 标签页
                    UnifyTabBar(
                        tabs = tabs.map { it.first },
                        selectedTabIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
                        variant = UnifyTabBarVariant.PRIMARY
                    )
                    
                    // 内容区域
                    Box(modifier = Modifier.weight(1f)) {
                        tabs[selectedTab].second()
                    }
                }
            }
        }
    }
}

/**
 * 高级组件演示
 */
@Composable
private fun AdvancedComponentsDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "图表组件演示",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 折线图
                    val lineChartData = listOf(
                        UnifyChartDataSeries(
                            name = "销售额",
                            data = listOf(
                                UnifyChartDataPoint(1f, 100f, "1月"),
                                UnifyChartDataPoint(2f, 150f, "2月"),
                                UnifyChartDataPoint(3f, 120f, "3月"),
                                UnifyChartDataPoint(4f, 200f, "4月"),
                                UnifyChartDataPoint(5f, 180f, "5月"),
                                UnifyChartDataPoint(6f, 220f, "6月")
                            )
                        )
                    )
                    
                    UnifyChart(
                        data = lineChartData,
                        type = UnifyChartType.LINE,
                        title = "月度销售趋势",
                        modifier = Modifier.height(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 饼图
                    val pieChartData = listOf(
                        UnifyChartDataSeries(
                            name = "销售分布",
                            data = listOf(
                                UnifyChartDataPoint(0f, 30f, "产品A"),
                                UnifyChartDataPoint(0f, 25f, "产品B"),
                                UnifyChartDataPoint(0f, 20f, "产品C"),
                                UnifyChartDataPoint(0f, 15f, "产品D"),
                                UnifyChartDataPoint(0f, 10f, "其他")
                            )
                        )
                    )
                    
                    UnifyChart(
                        data = pieChartData,
                        type = UnifyChartType.PIE,
                        title = "产品销售分布",
                        modifier = Modifier.height(200.dp)
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "日历组件演示",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var selectedDate by remember { 
                        mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
                    }
                    var viewType by remember { mutableStateOf(UnifyCalendarViewType.MONTH) }
                    
                    // 视图切换按钮
                    UnifyRow(spacing = UnifySpacing.SMALL) {
                        UnifyButton(
                            text = "月视图",
                            onClick = { viewType = UnifyCalendarViewType.MONTH },
                            variant = if (viewType == UnifyCalendarViewType.MONTH) {
                                UnifyButtonVariant.FILLED
                            } else {
                                UnifyButtonVariant.OUTLINED
                            },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "周视图",
                            onClick = { viewType = UnifyCalendarViewType.WEEK },
                            variant = if (viewType == UnifyCalendarViewType.WEEK) {
                                UnifyButtonVariant.FILLED
                            } else {
                                UnifyButtonVariant.OUTLINED
                            },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "议程",
                            onClick = { viewType = UnifyCalendarViewType.AGENDA },
                            variant = if (viewType == UnifyCalendarViewType.AGENDA) {
                                UnifyButtonVariant.FILLED
                            } else {
                                UnifyButtonVariant.OUTLINED
                            },
                            size = UnifyButtonSize.SMALL
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UnifyCalendar(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        viewType = viewType,
                        events = listOf(
                            UnifyCalendarEvent(
                                id = "1",
                                title = "重要会议",
                                startDate = selectedDate,
                                color = Color.Red
                            ),
                            UnifyCalendarEvent(
                                id = "2",
                                title = "项目评审",
                                startDate = selectedDate.plusDays(1),
                                color = Color.Blue
                            ),
                            UnifyCalendarEvent(
                                id = "3",
                                title = "团队建设",
                                startDate = selectedDate.plusDays(3),
                                color = Color.Green
                            )
                        ),
                        modifier = Modifier.height(350.dp)
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "选择器组件演示",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 下拉选择器
                    val dropdownItems = listOf(
                        UnifyPickerItem("beijing", "北京"),
                        UnifyPickerItem("shanghai", "上海"),
                        UnifyPickerItem("guangzhou", "广州"),
                        UnifyPickerItem("shenzhen", "深圳")
                    )
                    
                    var selectedCity by remember { mutableStateOf<UnifyPickerItem<String>?>(null) }
                    
                    UnifyPicker(
                        items = dropdownItems,
                        selectedItem = selectedCity,
                        onItemSelected = { selectedCity = it },
                        label = "选择城市",
                        placeholder = "请选择城市"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 滚轮选择器
                    val wheelItems = (1..100).map { 
                        UnifyPickerItem(it, "${it}岁") 
                    }
                    
                    var selectedAge by remember { mutableStateOf<UnifyPickerItem<Int>?>(null) }
                    
                    UnifyText(text = "年龄选择（滚轮）", variant = UnifyTextVariant.BODY_MEDIUM)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnifyPicker(
                        items = wheelItems,
                        selectedItem = selectedAge,
                        onItemSelected = { selectedAge = it },
                        type = UnifyPickerType.WHEEL,
                        modifier = Modifier.height(120.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 日期时间选择器
                    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
                    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
                    
                    UnifyRow(spacing = UnifySpacing.MEDIUM) {
                        UnifyDatePicker(
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            label = "选择日期",
                            modifier = Modifier.weight(1f)
                        )
                        
                        UnifyTimePicker(
                            selectedTime = selectedTime,
                            onTimeSelected = { selectedTime = it },
                            label = "选择时间",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "相机组件演示",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var cameraState by remember { mutableStateOf(UnifyCameraState.PREVIEW) }
                    var cameraMode by remember { mutableStateOf(UnifyCameraMode.PHOTO) }
                    
                    // 模式切换
                    UnifyRow(spacing = UnifySpacing.SMALL) {
                        UnifyButton(
                            text = "拍照",
                            onClick = { cameraMode = UnifyCameraMode.PHOTO },
                            variant = if (cameraMode == UnifyCameraMode.PHOTO) {
                                UnifyButtonVariant.FILLED
                            } else {
                                UnifyButtonVariant.OUTLINED
                            },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "录像",
                            onClick = { cameraMode = UnifyCameraMode.VIDEO },
                            variant = if (cameraMode == UnifyCameraMode.VIDEO) {
                                UnifyButtonVariant.FILLED
                            } else {
                                UnifyButtonVariant.OUTLINED
                            },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "人像",
                            onClick = { cameraMode = UnifyCameraMode.PORTRAIT },
                            variant = if (cameraMode == UnifyCameraMode.PORTRAIT) {
                                UnifyButtonVariant.FILLED
                            } else {
                                UnifyButtonVariant.OUTLINED
                            },
                            size = UnifyButtonSize.SMALL
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UnifyCamera(
                        state = cameraState,
                        onStateChanged = { cameraState = it },
                        mode = cameraMode,
                        modifier = Modifier.height(250.dp),
                        onCapturePhoto = {
                            cameraState = UnifyCameraState.CAPTURING
                            // 模拟拍照处理
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(1000)
                                cameraState = UnifyCameraState.PREVIEW
                            }
                        },
                        onStartRecording = {
                            cameraState = UnifyCameraState.RECORDING
                        },
                        onStopRecording = {
                            cameraState = UnifyCameraState.PREVIEW
                        }
                    )
                }
            }
        }
    }
}

/**
 * 响应式设计演示
 */
@Composable
private fun ResponsiveDesignDemo() {
    val responsive = LocalUnifyResponsive.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "设备信息",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyText(text = "屏幕尺寸: ${responsive.screenSize}")
                    UnifyText(text = "设备类型: ${responsive.deviceType}")
                    UnifyText(text = "屏幕方向: ${responsive.orientation}")
                    UnifyText(text = "屏幕宽度: ${responsive.screenWidth}")
                    UnifyText(text = "屏幕高度: ${responsive.screenHeight}")
                    UnifyText(text = "密度: ${responsive.density}")
                    UnifyText(text = "是否平板: ${responsive.isTablet}")
                    UnifyText(text = "是否横屏: ${responsive.isLandscape}")
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "响应式容器",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyResponsiveContainer {
                        UnifySurface(
                            variant = UnifySurfaceVariant.TONAL,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            UnifyText(
                                text = "这是一个响应式容器，会根据屏幕尺寸自动调整内边距和最大宽度。在不同设备上查看效果。"
                            )
                        }
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "响应式网格",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyResponsiveCardGrid(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(12) { index ->
                            UnifyCard(
                                modifier = Modifier.height(80.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    UnifyText(text = "卡片 ${index + 1}")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "响应式可见性",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyResponsiveVisibility(
                        visibleOn = setOf(UnifyScreenSize.COMPACT)
                    ) {
                        UnifySurface(
                            variant = UnifySurfaceVariant.OUTLINED,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            UnifyText(
                                text = "这段内容只在紧凑屏幕（手机）上显示",
                                color = Color.Red
                            )
                        }
                    }
                    
                    UnifyResponsiveVisibility(
                        visibleOn = setOf(UnifyScreenSize.MEDIUM)
                    ) {
                        UnifySurface(
                            variant = UnifySurfaceVariant.OUTLINED,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            UnifyText(
                                text = "这段内容只在中等屏幕（小平板）上显示",
                                color = Color.Blue
                            )
                        }
                    }
                    
                    UnifyResponsiveVisibility(
                        visibleOn = setOf(UnifyScreenSize.EXPANDED)
                    ) {
                        UnifySurface(
                            variant = UnifySurfaceVariant.OUTLINED,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            UnifyText(
                                text = "这段内容只在扩展屏幕（大平板/桌面）上显示",
                                color = Color.Green
                            )
                        }
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "响应式布局",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyResponsiveRow {
                        repeat(3) { index ->
                            UnifyCard(
                                modifier = Modifier
                                    .weight(rememberResponsiveWeight(
                                        compactWeight = 1f,
                                        mediumWeight = 1f,
                                        expandedWeight = 0.8f
                                    ))
                                    .height(60.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    UnifyText(text = "项目 ${index + 1}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 无障碍支持演示
 */
@Composable
private fun AccessibilityDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "无障碍文本",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyAccessibleText(
                        text = "这是一个支持无障碍的文本组件，具有适当的语义标记。",
                        contentDescription = "无障碍文本示例"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnifyAccessibleText(
                        text = "这是一个标题",
                        heading = true,
                        fontWeight = FontWeight.Bold,
                        contentDescription = "无障碍标题示例"
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "无障碍按钮",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyAccessibleButton(
                        onClick = { /* 处理点击 */ },
                        contentDescription = "这是一个无障碍按钮，支持屏幕阅读器和键盘导航",
                        stateDescription = "可点击状态"
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Accessibility,
                            size = UnifyIconSize.SMALL
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        UnifyText(text = "无障碍按钮")
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "无障碍输入框",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var username by remember { mutableStateOf("") }
                    var email by remember { mutableStateOf("") }
                    var hasError by remember { mutableStateOf(false) }
                    
                    UnifyAccessibleTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "用户名",
                        placeholder = "请输入用户名",
                        helperText = "用户名长度应在3-20个字符之间",
                        contentDescription = "用户名输入框"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UnifyAccessibleTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            hasError = !it.contains("@")
                        },
                        label = "邮箱地址",
                        placeholder = "请输入邮箱地址",
                        errorText = if (hasError && email.isNotEmpty()) "请输入有效的邮箱地址" else null,
                        contentDescription = "邮箱地址输入框"
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "无障碍选择控件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var agreeTerms by remember { mutableStateOf(false) }
                    var enableNotifications by remember { mutableStateOf(true) }
                    
                    UnifyAccessibleCheckbox(
                        checked = agreeTerms,
                        onCheckedChange = { agreeTerms = it },
                        label = "我同意用户协议和隐私政策"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UnifyAccessibleSwitch(
                        checked = enableNotifications,
                        onCheckedChange = { enableNotifications = it },
                        label = "启用推送通知"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val themeOptions = listOf("浅色主题", "深色主题", "跟随系统")
                    var selectedTheme by remember { mutableStateOf<String?>(null) }
                    
                    UnifyAccessibleRadioGroup(
                        options = themeOptions,
                        selectedOption = selectedTheme,
                        onOptionSelected = { selectedTheme = it },
                        groupLabel = "选择主题"
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "无障碍滑块",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var volume by remember { mutableStateOf(0.7f) }
                    var brightness by remember { mutableStateOf(0.5f) }
                    
                    UnifyAccessibleSlider(
                        value = volume,
                        onValueChange = { volume = it },
                        label = "音量控制",
                        valueDescription = { "音量: ${(it * 100).toInt()}%" }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UnifyAccessibleSlider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        label = "亮度控制",
                        valueDescription = { "亮度: ${(it * 100).toInt()}%" }
                    )
                }
            }
        }
    }
}

/**
 * 综合示例演示
 */
@Composable
private fun ComprehensiveDemo() {
    UnifyResponsiveSidebar(
        showSidebar = !isCompactScreen(),
        sidebarContent = {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    UnifyText(
                        text = "功能菜单",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                }
                
                val menuItems = listOf(
                    "仪表板" to Icons.Default.Dashboard,
                    "数据分析" to Icons.Default.Analytics,
                    "用户管理" to Icons.Default.People,
                    "系统设置" to Icons.Default.Settings
                )
                
                items(menuItems.size) { index ->
                    val (title, icon) = menuItems[index]
                    
                    UnifyAccessibleListItem(
                        onClick = { /* 处理菜单点击 */ },
                        contentDescription = "菜单项：$title"
                    ) {
                        UnifyIcon(
                            icon = icon,
                            size = UnifyIconSize.SMALL
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        UnifyText(text = title)
                    }
                }
            }
        },
        mainContent = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    UnifyCard {
                        Column {
                            UnifyText(
                                text = "综合功能展示",
                                variant = UnifyTextVariant.TITLE_LARGE
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            UnifyText(
                                text = "这是一个集成了响应式设计、无障碍支持和高级组件的综合示例。",
                                variant = UnifyTextVariant.BODY_MEDIUM
                            )
                        }
                    }
                }
                
                item {
                    UnifyResponsiveCardGrid {
                        items(6) { index ->
                            UnifyCard(
                                modifier = Modifier.height(120.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    UnifyIcon(
                                        icon = when (index % 3) {
                                            0 -> Icons.Default.TrendingUp
                                            1 -> Icons.Default.People
                                            else -> Icons.Default.Notifications
                                        },
                                        size = UnifyIconSize.MEDIUM
                                    )
                                    
                                    UnifyText(
                                        text = when (index % 3) {
                                            0 -> "销售增长"
                                            1 -> "用户活跃"
                                            else -> "系统通知"
                                        },
                                        variant = UnifyTextVariant.BODY_MEDIUM
                                    )
                                    
                                    UnifyText(
                                        text = "${(index + 1) * 1234}",
                                        variant = UnifyTextVariant.TITLE_MEDIUM,
                                        color = LocalUnifyTheme.current.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    UnifyCard {
                        Column {
                            UnifyText(
                                text = "实时数据图表",
                                variant = UnifyTextVariant.TITLE_MEDIUM
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val realtimeData = listOf(
                                UnifyChartDataSeries(
                                    name = "实时数据",
                                    data = (1..10).map { 
                                        UnifyChartDataPoint(
                                            it.toFloat(), 
                                            (50..200).random().toFloat()
                                        ) 
                                    }
                                )
                            )
                            
                            UnifyChart(
                                data = realtimeData,
                                type = UnifyChartType.AREA,
                                title = "系统性能监控",
                                modifier = Modifier.height(200.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
