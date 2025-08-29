package com.unify.helloworld

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 跨平台Hello World应用
 */

@Composable
fun HelloWorldApp(platformName: String = "Unknown") {
    var count by remember { mutableIntStateOf(0) }
    
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌍 Unify KMP",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "平台: $platformName",
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
                        Button(onClick = { count++ }) {
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
        }
    }
}

// expect/actual 平台信息接口
expect object PlatformInfo {
    fun getPlatformName(): String
    fun getDeviceInfo(): String
}