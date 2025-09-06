package com.unify.helloworld

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.unify.core.UnifyCore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Android平台HelloWorld应用扩展实现
 */
@Composable
fun AndroidHelloWorldApp() {
    val context = LocalContext.current
    var platformInfo by remember { mutableStateOf("加载中...") }
    
    LaunchedEffect(Unit) {
        try {
            platformInfo = "平台: ${getPlatformName()}\n设备信息: ${getDeviceInfo()}"
        } catch (e: Exception) {
            platformInfo = "错误: ${e.message}"
        }
    }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Hello Unify-Core!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Android平台信息",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = platformInfo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // 演示网络功能
                        kotlinx.coroutines.GlobalScope.launch {
                            try {
                                // 这里可以调用网络演示
                                platformInfo = "网络功能演示已启动"
                            } catch (e: Exception) {
                                platformInfo = "网络演示错误: ${e.message}"
                            }
                        }
                    }
                ) {
                    Text("测试网络功能")
                }
            }
        }
    }
}
