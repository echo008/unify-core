package com.unify.wearapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.unify.core.platform.PlatformManager
import com.unify.helloworld.Greeting

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化平台管理器
        PlatformManager.initialize()
        
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    UnifyWearTheme {
        val navController = rememberSwipeDismissableNavController()
        
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                WearHomeScreen(
                    onNavigateToHealth = { navController.navigate("health") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("health") {
                WearHealthScreen()
            }
            composable("settings") {
                WearSettingsScreen()
            }
        }
    }
}

@Composable
fun WearHomeScreen(
    onNavigateToHealth: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val greeting = remember { Greeting().greet() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unify Wear",
            style = MaterialTheme.typography.title1,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = greeting,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToHealth,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("健康数据")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("设置")
        }
    }
}

@Composable
fun WearHealthScreen() {
    var stepCount by remember { mutableStateOf(0) }
    var heartRate by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        // 模拟获取健康数据
        stepCount = 8500
        heartRate = 72
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "健康数据",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colors.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "步数",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "$stepCount",
                    style = MaterialTheme.typography.title1,
                    color = MaterialTheme.colors.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "心率",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "$heartRate BPM",
                    style = MaterialTheme.typography.title1,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun WearSettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colors.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val platformInfo = remember { 
            "${PlatformManager.getPlatformName()} ${PlatformManager.getPlatformVersion()}"
        }
        
        Text(
            text = "平台: $platformInfo",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val deviceInfo = remember { PlatformManager.getDeviceInfo() }
        
        Text(
            text = "设备: ${deviceInfo.manufacturer} ${deviceInfo.model}",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun UnifyWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography(),
        content = content
    )
}

private val wearColorPalette = darkColors(
    primary = Color(0xFF1EB980),
    primaryVariant = Color(0xFF045D56),
    secondary = Color(0xFFFF6859),
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1C),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)
