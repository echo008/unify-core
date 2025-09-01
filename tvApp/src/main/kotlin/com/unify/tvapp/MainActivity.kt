package com.unify.tvapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.platform.PlatformManager
import com.unify.helloworld.HelloWorldApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化平台管理器
        PlatformManager.initialize()
        
        setContent {
            UnifyTVTheme {
                // 使用统一的HelloWorldApp
                HelloWorldApp()
            }
        }
    }
}

@Composable
fun TVApp() {
    var currentScreen by remember { mutableStateOf("home") }
    
    when (currentScreen) {
        "home" -> TVHomeScreen(
            onNavigateToMedia = { currentScreen = "media" },
            onNavigateToSettings = { currentScreen = "settings" },
            onNavigateToApps = { currentScreen = "apps" }
        )
        "media" -> TVMediaScreen(
            onBack = { currentScreen = "home" }
        )
        "settings" -> TVSettingsScreen(
            onBack = { currentScreen = "home" }
        )
        "apps" -> TVAppsScreen(
            onBack = { currentScreen = "home" }
        )
    }
}

@Composable
fun TVHomeScreen(
    onNavigateToMedia: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToApps: () -> Unit
) {
    val greeting = remember { Greeting().greet() }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp) // TV安全区域
    ) {
        // 标题区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unify TV",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = greeting,
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 主要功能区域
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                TVSection(
                    title = "媒体中心",
                    items = listOf("电影", "电视剧", "音乐", "照片"),
                    onItemClick = { onNavigateToMedia() }
                )
            }
            
            item {
                TVSection(
                    title = "应用程序",
                    items = listOf("Netflix", "YouTube", "Prime Video", "Disney+"),
                    onItemClick = { onNavigateToApps() }
                )
            }
            
            item {
                TVSection(
                    title = "系统设置",
                    items = listOf("显示设置", "音频设置", "网络设置", "关于"),
                    onItemClick = { onNavigateToSettings() }
                )
            }
        }
    }
}

@Composable
fun TVSection(
    title: String,
    items: List<String>,
    onItemClick: () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                TVCard(
                    title = item,
                    onClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun TVCard(
    title: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .size(200.dp, 120.dp)
            .focusable()
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.DirectionCenter && keyEvent.type == KeyEventType.KeyUp -> {
                        onClick()
                        true
                    }
                    else -> false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color(0xFF1EB980) else Color(0xFF2C2C2C)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TVMediaScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "媒体中心",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EB980)
                )
            ) {
                Text("返回", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                TVMediaSection(
                    title = "最近播放",
                    items = listOf("复仇者联盟", "权力的游戏", "星际穿越", "黑镜")
                )
            }
            
            item {
                TVMediaSection(
                    title = "推荐电影",
                    items = listOf("沙丘", "蜘蛛侠", "阿凡达", "盗梦空间")
                )
            }
            
            item {
                TVMediaSection(
                    title = "热门电视剧",
                    items = listOf("怪奇物语", "王冠", "曼达洛人", "女王的棋局")
                )
            }
        }
    }
}

@Composable
fun TVMediaSection(
    title: String,
    items: List<String>
) {
    Column {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                TVMediaCard(title = item)
            }
        }
    }
}

@Composable
fun TVMediaCard(title: String) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .size(280.dp, 160.dp)
            .focusable(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color(0xFF1EB980) else Color(0xFF2C2C2C)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            // 这里可以添加媒体缩略图
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF404040))
            )
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun TVSettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "系统设置",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EB980)
                )
            ) {
                Text("返回", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val platformInfo = remember { 
            "${PlatformManager.getPlatformName()} ${PlatformManager.getPlatformVersion()}"
        }
        val deviceInfo = remember { PlatformManager.getDeviceInfo() }
        val screenInfo = remember { PlatformManager.getScreenInfo() }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TVSettingItem("平台信息", platformInfo)
            }
            item {
                TVSettingItem("设备型号", "${deviceInfo.manufacturer} ${deviceInfo.model}")
            }
            item {
                TVSettingItem("屏幕分辨率", "${screenInfo.width} x ${screenInfo.height}")
            }
            item {
                TVSettingItem("刷新率", "${screenInfo.refreshRate} Hz")
            }
            item {
                TVSettingItem("网络状态", PlatformManager.getNetworkStatus().toString())
            }
        }
    }
}

@Composable
fun TVSettingItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TVAppsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "应用程序",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EB980)
                )
            ) {
                Text("返回", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val apps = listOf(
            "Netflix", "YouTube", "Prime Video", "Disney+",
            "Spotify", "Plex", "Kodi", "VLC",
            "Steam Link", "GeForce Now", "Xbox", "PlayStation"
        )
        
        LazyColumn {
            items(apps.chunked(4)) { rowApps ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(rowApps) { app ->
                        TVAppCard(appName = app)
                    }
                }
            }
        }
    }
}

@Composable
fun TVAppCard(appName: String) {
    var isFocused by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .size(160.dp, 120.dp)
            .focusable(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color(0xFF1EB980) else Color(0xFF2C2C2C)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 这里可以添加应用图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF404040))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = appName,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UnifyTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF1EB980),
            secondary = Color(0xFFFF6859),
            background = Color.Black,
            surface = Color(0xFF1C1C1C),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
