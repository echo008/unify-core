package com.unify.helloworld

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 简化的 HelloWorld 应用，避免编译器内部错误
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = " Unify-Core",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Kotlin Multiplatform Compose 框架",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 计数器
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "计数: $count",
                    style = MaterialTheme.typography.headlineLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { count-- }
                    ) {
                        Text("减少")
                    }
                    
                    Button(
                        onClick = { count++ }
                    ) {
                        Text("增加")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { count = 0 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重置")
                }
            }
        }
        
        // 平台信息
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "平台信息",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("平台: ${getPlatformName()}")
            }
        }
    }
}

expect fun getPlatformName(): String
