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
import androidx.wear.compose.navigation.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
            text = "基于Kotlin Multiplatform",
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
    var stepCount by remember { mutableStateOf(8500) }
    var heartRate by remember { mutableStateOf(72) }
    
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
            onClick = { },
            modifier = Modifier.fillMaxWidth()
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
            onClick = { },
            modifier = Modifier.fillMaxWidth()
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
        
        Text(
            text = "平台: Wear OS",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "架构: Kotlin Multiplatform",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "基于KuiklyUI架构",
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
        content = content
    )
}

private val wearColorPalette = Colors(
    primary = Color(0xFF6200EE),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1C),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)
