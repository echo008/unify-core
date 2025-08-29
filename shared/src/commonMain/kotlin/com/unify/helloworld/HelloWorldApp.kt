package com.unify.helloworld

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 轻量级Hello World应用
 * 参考KuiklyUI架构，实现跨平台一致性
 */

// 应用状态
data class HelloWorldState(
    val message: String = "Hello, Unify KMP!",
    val counter: Int = 0,
    val platformName: String = ""
)

// 应用逻辑
class HelloWorldLogic {
    private val _state = mutableStateOf(HelloWorldState())
    val state: State<HelloWorldState> = _state
    
    fun updatePlatform(platform: String) {
        _state.value = _state.value.copy(platformName = platform)
    }
    
    fun incrementCounter() {
        _state.value = _state.value.copy(counter = _state.value.counter + 1)
    }
    
    fun resetCounter() {
        _state.value = _state.value.copy(counter = 0)
    }
}

/**
 * 跨平台Hello World主界面
 * 使用Compose Multiplatform实现
 */
@Composable
fun HelloWorldApp(
    platformName: String = "Unknown",
    logic: HelloWorldLogic = remember { HelloWorldLogic() }
) {
    // 初始化平台信息
    LaunchedEffect(platformName) {
        logic.updatePlatform(platformName)
    }
    
    val currentState by logic.state
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                // 标题
                Text(
                    text = "Unify KMP",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 副标题
                Text(
                    text = "跨平台开发框架",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 平台信息卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "当前平台",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentState.platformName.ifEmpty { "Unknown" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 欢迎消息
                Text(
                    text = currentState.message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 计数器卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "点击计数器",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 计数显示
                        Text(
                            text = currentState.counter.toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 按钮行
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 增加按钮
                            Button(
                                onClick = { logic.incrementCounter() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+1", fontSize = 16.sp)
                            }
                            
                            // 重置按钮
                            OutlinedButton(
                                onClick = { logic.resetCounter() },
                                modifier = Modifier.weight(1f),
                                enabled = currentState.counter > 0
                            ) {
                                Text("重置", fontSize = 16.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 框架信息
                Text(
                    text = "基于 Kotlin Multiplatform + Compose 构建",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 平台特定功能接口
 * 使用expect/actual机制实现
 */
expect object PlatformInfo {
    fun getPlatformName(): String
    fun getDeviceInfo(): String
}
