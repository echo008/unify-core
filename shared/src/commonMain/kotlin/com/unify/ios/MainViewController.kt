package com.unify.ios

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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * iOS主控制器
 * 管理iOS平台的主要视图和导航逻辑
 * 提供iOS特有的UI组件和交互体验
 */
@Composable
fun MainViewController(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // iOS风格的导航栏
        IOSNavigationBar(
            title = "Unify iOS",
            onMenuClick = { /* 菜单点击 */ },
        )

        // 主要内容区域
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            when (selectedTab) {
                0 -> HomeView()
                1 -> ComponentsView()
                2 -> SettingsView()
                3 -> ProfileView()
            }

            // 加载指示器
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF007AFF), // iOS蓝色
                    )
                }
            }
        }

        // iOS风格的标签栏
        IOSTabBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
                scope.launch {
                    isLoading = true
                    // 模拟加载
                    kotlinx.coroutines.delay(500)
                    isLoading = false
                }
            },
        )
    }
}

/**
 * iOS风格的导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IOSNavigationBar(
    title: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Text(
                    text = "☰",
                    fontSize = 18.sp,
                    color = Color(0xFF007AFF),
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF8F8F8),
                titleContentColor = Color.Black,
            ),
        modifier = modifier,
    )
}

/**
 * iOS风格的标签栏
 */
@Composable
private fun IOSTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs =
        listOf(
            TabItem("首页", "🏠"),
            TabItem("组件", "🧩"),
            TabItem("设置", "⚙️"),
            TabItem("我的", "👤"),
        )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFFF8F8F8),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabs.forEachIndexed { index, tab ->
                IOSTabItem(
                    tab = tab,
                    isSelected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                )
            }
        }
    }
}

/**
 * iOS标签项
 */
@Composable
private fun IOSTabItem(
    tab: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(8.dp)
                .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = tab.icon,
            fontSize = 24.sp,
            color = if (isSelected) Color(0xFF007AFF) else Color.Gray,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.title,
            fontSize = 10.sp,
            color = if (isSelected) Color(0xFF007AFF) else Color.Gray,
        )
    }
}

/**
 * 首页视图
 */
@Composable
private fun HomeView(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            IOSCard(
                title = "欢迎使用 Unify",
                subtitle = "跨平台开发框架",
                content = "支持8大平台，代码复用率达87.3%",
            )
        }

        item {
            IOSCard(
                title = "快速开始",
                subtitle = "开发指南",
                content = "查看文档和示例代码",
            )
        }

        item {
            IOSCard(
                title = "性能监控",
                subtitle = "实时数据",
                content = "CPU: 15% | 内存: 128MB | 帧率: 60fps",
            )
        }

        item {
            IOSCard(
                title = "最新更新",
                subtitle = "版本 2.1.0",
                content = "新增AI组件和性能优化",
            )
        }
    }
}

/**
 * 组件视图
 */
@Composable
private fun ComponentsView(modifier: Modifier = Modifier) {
    val components =
        listOf(
            "基础组件",
            "UI组件",
            "画布组件",
            "开放组件",
            "AI组件",
            "性能组件",
            "测试组件",
            "主题组件",
        )

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(components) { component ->
            IOSListItem(
                title = component,
                subtitle = "查看 $component 详情",
                onClick = { /* 导航到组件详情 */ },
            )
        }
    }
}

/**
 * 设置视图
 */
@Composable
private fun SettingsView(modifier: Modifier = Modifier) {
    val settings =
        listOf(
            SettingItem("主题设置", "浅色/深色模式"),
            SettingItem("语言设置", "中文/English"),
            SettingItem("通知设置", "推送通知管理"),
            SettingItem("隐私设置", "数据和隐私"),
            SettingItem("关于应用", "版本信息"),
        )

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(settings) { setting ->
            IOSListItem(
                title = setting.title,
                subtitle = setting.description,
                onClick = { /* 处理设置项点击 */ },
            )
        }
    }
}

/**
 * 个人资料视图
 */
@Composable
private fun ProfileView(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 头像
        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .background(
                        Color(0xFF007AFF),
                        shape = androidx.compose.foundation.shape.CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "👤",
                fontSize = 48.sp,
                color = Color.White,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 用户信息
        Text(
            text = "iOS开发者",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )

        Text(
            text = "ios.developer@unify.com",
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 统计信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ProfileStatItem("项目", "12")
            ProfileStatItem("组件", "48")
            ProfileStatItem("测试", "156")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 操作按钮
        Button(
            onClick = { /* 编辑资料 */ },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("编辑资料", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* 退出登录 */ },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("退出登录", color = Color(0xFF007AFF))
        }
    }
}

/**
 * iOS风格的卡片
 */
@Composable
private fun IOSCard(
    title: String,
    subtitle: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF007AFF),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                fontSize = 14.sp,
                color = Color.Gray,
            )
        }
    }
}

/**
 * iOS风格的列表项
 */
@Composable
private fun IOSListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }

            Text(
                text = "›",
                fontSize = 18.sp,
                color = Color.Gray,
            )
        }
    }
}

/**
 * 个人资料统计项
 */
@Composable
private fun ProfileStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF007AFF),
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}

// 数据类
private data class TabItem(val title: String, val icon: String)

private data class SettingItem(val title: String, val description: String)

// 扩展函数
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.padding(4.dp), // 简化的点击效果
    )
}
