package com.unify.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * 桌面端应用入口
 * 独立的Compose Desktop实现，避免shared模块编译器错误
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Unify KMP Desktop",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        MaterialTheme {
            DesktopHelloWorldApp()
        }
    }
}

@Composable
fun DesktopHelloWorldApp() {
    var count by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🖥️ Unify KMP Desktop",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "平台: Desktop (Compose for Desktop)",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "计数器: $count",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { count++ }
                    ) {
                        Text("增加")
                    }
                    
                    Button(
                        onClick = { count = 0 },
                        enabled = count > 0
                    ) {
                        Text("重置")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "✅ 桌面端应用运行正常",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
