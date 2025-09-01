package com.unify.ui.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.UnifyButton
import com.unify.ui.components.foundation.UnifyIcon
import com.unify.ui.components.foundation.UnifyImage
import com.unify.ui.components.foundation.UnifySurface
import com.unify.ui.components.foundation.UnifyText
import com.unify.ui.components.layout.UnifyGrid
import com.unify.ui.components.layout.UnifyStack
import com.unify.ui.components.input.UnifyTextField
import com.unify.ui.components.navigation.UnifyBottomNavigation
import com.unify.ui.components.navigation.UnifyTopAppBar
import com.unify.ui.components.feedback.*
import com.unify.ui.components.list.*
import com.unify.ui.components.advanced.*
import com.unify.ui.responsive.*
import com.unify.ui.accessibility.*
import kotlinx.datetime.*

/**
 * Unify UI 组件库综合演示应用
 * 展示所有组件的使用示例和最佳实践
 * 包含基础组件、高级组件、响应式设计和无障碍支持
 */

/**
 * 演示页面数据类
 */
data class DemoPage(
    val title: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

/**
 * 主演示应用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyUIDemo() {
    UnifyThemeProvider {
        var selectedPageIndex by remember { mutableStateOf(0) }
        val toastState = rememberUnifyToastState()
        
        val demoPages = listOf(
            DemoPage("基础组件", Icons.Default.Widgets) { BasicComponentsDemo(toastState) },
            DemoPage("布局组件", Icons.Default.ViewModule) { LayoutComponentsDemo() },
            DemoPage("输入组件", Icons.Default.Input) { InputComponentsDemo() },
            DemoPage("导航组件", Icons.Default.Navigation) { NavigationComponentsDemo() },
            DemoPage("反馈组件", Icons.Default.Feedback) { FeedbackComponentsDemo(toastState) },
            DemoPage("列表组件", Icons.Default.List) { ListComponentsDemo() },
            DemoPage("高级组件", Icons.Default.AutoAwesome) { AdvancedComponentsDemo() },
            DemoPage("响应式设计", Icons.Default.Devices) { ResponsiveDesignDemo() },
            DemoPage("无障碍支持", Icons.Default.Accessibility) { AccessibilityDemo() }
        )
        
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        
        UnifyAdaptiveDrawer(
            groups = listOf(
                UnifyDrawerGroup(
                    title = "组件演示",
                    items = demoPages.mapIndexed { index, page ->
                        UnifyDrawerItem(
                            icon = page.icon,
                            text = page.title,
                            selected = index == selectedPageIndex,
                            onClick = { selectedPageIndex = index }
                        )
                    }
                )
            ),
            selectedItem = demoPages[selectedPageIndex].title,
            onItemClick = { item ->
                val index = demoPages.indexOfFirst { it.title == item.text }
                if (index >= 0) selectedPageIndex = index
            },
            drawerState = drawerState,
            header = {
                UnifyDrawerHeader(
                    title = "Unify UI",
                    subtitle = "跨平台组件库演示"
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                UnifyTopNavigationBar(
                    title = demoPages[selectedPageIndex].title,
                    navigationIcon = Icons.Default.Menu,
                    onNavigationClick = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            drawerState.open()
                        }
                    }
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    demoPages[selectedPageIndex].content()
                }
            }
        }
        
        // Toast 主机
        UnifyToastHost(toastState = toastState)
    }
}

/**
 * 基础组件演示
 */
@Composable
private fun BasicComponentsDemo(toastState: UnifyToastState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "按钮组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyRow(spacing = UnifySpacing.SMALL) {
                        UnifyButton(
                            text = "填充按钮",
                            onClick = { 
                                toastState.showToast("点击了填充按钮", UnifyToastVariant.SUCCESS)
                            },
                            variant = UnifyButtonVariant.FILLED
                        )
                        UnifyButton(
                            text = "轮廓按钮",
                            onClick = { 
                                toastState.showToast("点击了轮廓按钮", UnifyToastVariant.INFO)
                            },
                            variant = UnifyButtonVariant.OUTLINED
                        )
                        UnifyButton(
                            text = "文本按钮",
                            onClick = { 
                                toastState.showToast("点击了文本按钮", UnifyToastVariant.DEFAULT)
                            },
                            variant = UnifyButtonVariant.TEXT
                        )
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "文本组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyText(
                        text = "这是标题大文本",
                        variant = UnifyTextVariant.HEADLINE_LARGE
                    )
                    UnifyText(
                        text = "这是正文中等文本",
                        variant = UnifyTextVariant.BODY_MEDIUM
                    )
                    UnifyText(
                        text = "这是说明小文本",
                        variant = UnifyTextVariant.CAPTION,
                        semantic = UnifyTextSemantic.SECONDARY
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "图标组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyRow(spacing = UnifySpacing.MEDIUM) {
                        UnifyIcon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "首页",
                            size = UnifyIconSize.SMALL
                        )
                        UnifyIcon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "收藏",
                            size = UnifyIconSize.MEDIUM,
                            semantic = UnifyIconSemantic.ERROR
                        )
                        UnifyIconButton(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            onClick = {
                                toastState.showToast("点击了设置图标")
                            }
                        )
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "图片组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyImage(
                        painter = null, // 这里应该提供实际的 Painter
                        contentDescription = "示例图片",
                        variant = UnifyImageVariant.THUMBNAIL,
                        size = UnifyImageSize.MEDIUM,
                        state = UnifyImageState.PLACEHOLDER
                    )
                }
            }
        }
    }
}

/**
 * 布局组件演示
 */
@Composable
private fun LayoutComponentsDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "行布局组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyRow(
                        spacing = UnifySpacing.SMALL,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) { index ->
                            UnifyContainer(
                                modifier = Modifier.size(60.dp),
                                color = androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f)
                            ) {
                                UnifyText(
                                    text = "${index + 1}",
                                    variant = UnifyTextVariant.TITLE_MEDIUM
                                )
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
                        text = "列布局组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyColumn(spacing = UnifySpacing.SMALL) {
                        repeat(3) { index ->
                            UnifyContainer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                color = androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.1f)
                            ) {
                                UnifyText(
                                    text = "项目 ${index + 1}",
                                    variant = UnifyTextVariant.BODY_MEDIUM
                                )
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
                        text = "网格布局组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyGrid(
                        columns = 2,
                        spacing = UnifySpacing.SMALL,
                        modifier = Modifier.height(200.dp)
                    ) {
                        repeat(4) { index ->
                            item {
                                UnifyContainer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f),
                                    color = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)
                                ) {
                                    UnifyText(
                                        text = "${index + 1}",
                                        variant = UnifyTextVariant.TITLE_MEDIUM
                                    )
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
 * 输入组件演示
 */
@Composable
private fun InputComponentsDemo() {
    var textValue by remember { mutableStateOf("") }
    var checkboxValue by remember { mutableStateOf(false) }
    var radioValue by remember { mutableStateOf("选项1") }
    var switchValue by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0.5f) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "文本输入组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        label = "标签",
                        placeholder = "请输入文本",
                        leadingIcon = Icons.Default.Search
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "选择控件组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyCheckboxWithLabel(
                        checked = checkboxValue,
                        onCheckedChange = { checkboxValue = it },
                        label = "复选框选项"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnifyRadioGroup(
                        options = listOf("选项1", "选项2", "选项3"),
                        selectedOption = radioValue,
                        onOptionSelected = { radioValue = it },
                        labelProvider = { it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnifySwitchWithLabel(
                        checked = switchValue,
                        onCheckedChange = { switchValue = it },
                        label = "开关选项"
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "滑块组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifySlider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        showValue = true,
                        valueFormatter = { "${(it * 100).toInt()}%" }
                    )
                }
            }
        }
    }
}

/**
 * 导航组件演示
 */
@Composable
private fun NavigationComponentsDemo() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf(
        UnifyTabItem("首页", Icons.Default.Home),
        UnifyTabItem("搜索", Icons.Default.Search),
        UnifyTabItem("收藏", Icons.Default.Favorite),
        UnifyTabItem("设置", Icons.Default.Settings)
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "选项卡组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyTabBar(
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "芯片式选项卡",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyChipTabBar(
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }
            }
        }
    }
}

/**
 * 反馈组件演示
 */
@Composable
private fun FeedbackComponentsDemo(toastState: UnifyToastState) {
    var showDialog by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.3f) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "Toast 消息",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyRow(spacing = UnifySpacing.SMALL) {
                        UnifyButton(
                            text = "成功",
                            onClick = {
                                toastState.showToast("操作成功！", UnifyToastVariant.SUCCESS)
                            },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "警告",
                            onClick = {
                                toastState.showToast("注意警告！", UnifyToastVariant.WARNING)
                            },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "错误",
                            onClick = {
                                toastState.showToast("发生错误！", UnifyToastVariant.ERROR)
                            },
                            size = UnifyButtonSize.SMALL
                        )
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "对话框组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyButton(
                        text = "显示对话框",
                        onClick = { showDialog = true }
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "加载组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyRow(spacing = UnifySpacing.MEDIUM) {
                        UnifyLoading(
                            variant = UnifyLoadingVariant.CIRCULAR,
                            size = UnifyLoadingSize.SMALL
                        )
                        UnifyLoading(
                            variant = UnifyLoadingVariant.DOTS,
                            size = UnifyLoadingSize.MEDIUM
                        )
                        UnifyButton(
                            text = "全屏加载",
                            onClick = {
                                showLoading = true
                                // 3秒后隐藏
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    kotlinx.coroutines.delay(3000)
                                    showLoading = false
                                }
                            },
                            size = UnifyButtonSize.SMALL
                        )
                    }
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "进度组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyProgress(
                        progress = progress,
                        variant = UnifyProgressVariant.LINEAR,
                        showPercentage = true,
                        label = "下载进度"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyProgress(
                        progress = progress,
                        variant = UnifyProgressVariant.CIRCULAR,
                        size = UnifyProgressSize.MEDIUM,
                        showPercentage = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyRow(spacing = UnifySpacing.SMALL) {
                        UnifyButton(
                            text = "增加",
                            onClick = { progress = (progress + 0.1f).coerceAtMost(1f) },
                            size = UnifyButtonSize.SMALL
                        )
                        UnifyButton(
                            text = "减少",
                            onClick = { progress = (progress - 0.1f).coerceAtLeast(0f) },
                            size = UnifyButtonSize.SMALL
                        )
                    }
                }
            }
        }
    }
    
    // 对话框
    if (showDialog) {
        UnifyConfirmationDialog(
            onDismissRequest = { showDialog = false },
            title = "确认操作",
            text = "您确定要执行此操作吗？",
            onConfirm = {
                showDialog = false
                toastState.showToast("操作已确认", UnifyToastVariant.SUCCESS)
            },
            onCancel = { showDialog = false }
        )
    }
    
    // 加载遮罩
    UnifyLoadingOverlay(
        visible = showLoading,
        text = "正在加载中..."
    )
}

/**
 * 列表组件演示
 */
@Composable
private fun ListComponentsDemo() {
    val sampleItems = remember {
        (1..20).map { index ->
            UnifyListItem(
                id = "$index",
                title = "列表项 $index",
                subtitle = "这是第 $index 个列表项的副标题",
                leadingIcon = when (index % 4) {
                    0 -> Icons.Default.Person
                    1 -> Icons.Default.Email
                    2 -> Icons.Default.Phone
                    else -> Icons.Default.LocationOn
                },
                trailingIcon = Icons.Default.ChevronRight,
                onClick = { /* 处理点击 */ }
            )
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "标准列表",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyList(
                        items = sampleItems.take(5),
                        modifier = Modifier.height(300.dp),
                        variant = UnifyListVariant.STANDARD
                    )
                }
            }
        }
        
        item {
            UnifyCard {
                Column {
                    UnifyText(
                        text = "空状态组件",
                        variant = UnifyTextVariant.TITLE_MEDIUM
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    UnifyEmptyState(
                        title = "暂无内容",
                        description = "这里还没有任何数据",
                        icon = Icons.Default.Inbox,
                        action = {
                            UnifyButton(
                                text = "刷新",
                                onClick = { /* 处理刷新 */ }
                            )
                        }
                    )
                }
            }
        }
    }
}
