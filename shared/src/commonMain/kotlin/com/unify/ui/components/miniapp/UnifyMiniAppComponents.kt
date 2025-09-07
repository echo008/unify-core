package com.unify.ui.components.miniapp

import androidx.compose.foundation.layout.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.foundation.lazy.LazyColumn
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.foundation.lazy.items
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.material3.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.runtime.*
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.Alignment
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.Modifier
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.graphics.Color
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.text.font.FontWeight
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.unit.dp
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import androidx.compose.ui.unit.sp
import com.unify.core.utils.UnifyReflectionUtils
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

/**
 * 跨平台统一小程序组件系统
 * 支持小程序容器、API桥接、生命周期管理等功能
 */

/**
 * 小程序容器组件
 */
@Composable
fun UnifyMiniAppContainer(
    appId: String,
    appConfig: MiniAppConfig,
    onAppEvent: (MiniAppEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var appState by remember { mutableStateOf(MiniAppState.LOADING) }
    var appData by remember { mutableStateOf<MiniAppData?>(null) }
    
    LaunchedEffect(appId) {
        try {
            appState = MiniAppState.LOADING
            onAppEvent(MiniAppEvent.Loading(appId))
            
            // 加载小程序
            val data = loadMiniApp(appId, appConfig)
            appData = data
            appState = MiniAppState.LOADED
            onAppEvent(MiniAppEvent.Loaded(appId, data))
        } catch (e: Exception) {
            appState = MiniAppState.ERROR
            onAppEvent(MiniAppEvent.Error(appId, e.message ?: "Unknown error"))
        }
    }
    
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when (appState) {
            MiniAppState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "加载小程序中...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            MiniAppState.LOADED -> {
                appData?.let { data ->
                    MiniAppContent(
                        appData = data,
                        onEvent = onAppEvent,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            MiniAppState.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "小程序加载失败",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = {
                                appState = MiniAppState.LOADING
                            }
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 小程序内容组件
 */
@Composable
private fun MiniAppContent(
    appData: MiniAppData,
    onEvent: (MiniAppEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 小程序头部信息
        MiniAppHeader(
            appData = appData,
            onEvent = onEvent
        )
        
        // 小程序主体内容
        MiniAppBody(
            appData = appData,
            onEvent = onEvent,
            modifier = Modifier.weight(1f)
        )
        
        // 小程序底部操作
        MiniAppFooter(
            appData = appData,
            onEvent = onEvent
        )
    }
}

/**
 * 小程序头部组件
 */
@Composable
private fun MiniAppHeader(
    appData: MiniAppData,
    onEvent: (MiniAppEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = appData.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "版本 ${appData.version}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    onEvent(MiniAppEvent.Refresh(appData.appId))
                }
            ) {
                Text("🔄")
            }
            
            IconButton(
                onClick = {
                    onEvent(MiniAppEvent.Close(appData.appId))
                }
            ) {
                Text("✕")
            }
        }
    }
}

/**
 * 小程序主体组件
 */
@Composable
private fun MiniAppBody(
    appData: MiniAppData,
    onEvent: (MiniAppEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 小程序页面列表
        items(appData.pages) { page ->
            MiniAppPageItem(
                page = page,
                onPageClick = { pageId ->
                    onEvent(MiniAppEvent.NavigateTo(appData.appId, pageId))
                }
            )
        }
        
        // 小程序功能列表
        items(appData.features) { feature ->
            MiniAppFeatureItem(
                feature = feature,
                onFeatureClick = { featureId ->
                    onEvent(MiniAppEvent.UseFeature(appData.appId, featureId))
                }
            )
        }
    }
}

/**
 * 小程序页面项组件
 */
@Composable
private fun MiniAppPageItem(
    page: MiniAppPage,
    onPageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = { onPageClick(page.pageId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = page.icon,
                fontSize = 24.sp
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text("▶")
        }
    }
}

/**
 * 小程序功能项组件
 */
@Composable
private fun MiniAppFeatureItem(
    feature: MiniAppFeature,
    onFeatureClick: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onFeatureClick(feature.featureId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = feature.icon,
                fontSize = 20.sp
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (feature.isEnabled) {
                Text("✓", color = Color.Green)
            } else {
                Text("✗", color = Color.Red)
            }
        }
    }
}

/**
 * 小程序底部组件
 */
@Composable
private fun MiniAppFooter(
    appData: MiniAppData,
    onEvent: (MiniAppEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = {
                onEvent(MiniAppEvent.Share(appData.appId))
            }
        ) {
            Text("分享")
        }
        
        OutlinedButton(
            onClick = {
                onEvent(MiniAppEvent.Settings(appData.appId))
            }
        ) {
            Text("设置")
        }
        
        OutlinedButton(
            onClick = {
                onEvent(MiniAppEvent.About(appData.appId))
            }
        ) {
            Text("关于")
        }
    }
}

/**
 * 小程序API桥接组件
 */
@Composable
fun UnifyMiniAppBridge(
    appId: String,
    apiConfig: MiniAppApiConfig,
    onApiCall: (MiniAppApiCall) -> MiniAppApiResult = { MiniAppApiResult.Success("") },
    modifier: Modifier = Modifier
) {
    var apiCalls by remember { mutableStateOf<List<MiniAppApiCall>>(emptyList()) }
    var apiResults by remember { mutableStateOf<Map<String, MiniAppApiResult>>(emptyMap()) }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "小程序API桥接",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // API调用历史
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apiCalls) { apiCall ->
                ApiCallItem(
                    apiCall = apiCall,
                    result = apiResults[apiCall.callId],
                    onRetry = { call ->
                        val result = onApiCall(call)
                        apiResults = apiResults + (call.callId to result)
                    }
                )
            }
        }
        
        // 测试API调用
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val testCall = MiniAppApiCall(
                        callId = "test_${getCurrentTimeMillis()}",
                        appId = appId,
                        method = "getUserInfo",
                        params = mapOf("scope" to "userInfo")
                    )
                    apiCalls = apiCalls + testCall
                    val result = onApiCall(testCall)
                    apiResults = apiResults + (testCall.callId to result)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("测试API")
            }
            
            OutlinedButton(
                onClick = {
                    apiCalls = emptyList()
                    apiResults = emptyMap()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("清空记录")
            }
        }
    }
}

/**
 * API调用项组件
 */
@Composable
private fun ApiCallItem(
    apiCall: MiniAppApiCall,
    result: MiniAppApiResult?,
    onRetry: (MiniAppApiCall) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = apiCall.method,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                when (result) {
                    is MiniAppApiResult.Success -> Text("✓", color = Color.Green)
                    is MiniAppApiResult.Error -> Text("✗", color = Color.Red)
                    null -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Text(
                text = "参数: ${apiCall.params}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            result?.let { res ->
                when (res) {
                    is MiniAppApiResult.Success -> {
                        Text(
                            text = "结果: ${res.data}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                    is MiniAppApiResult.Error -> {
                        Text(
                            text = "错误: ${res.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                        TextButton(
                            onClick = { onRetry(apiCall) }
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 小程序生命周期管理器
 */
@Composable
fun UnifyMiniAppLifecycleManager(
    appId: String,
    onLifecycleEvent: (MiniAppLifecycleEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var lifecycleState by remember { mutableStateOf(MiniAppLifecycleState.CREATED) }
    var lifecycleHistory by remember { mutableStateOf<List<MiniAppLifecycleEvent>>(emptyList()) }
    
    LaunchedEffect(appId) {
        // 模拟生命周期事件
        val events = listOf(
            MiniAppLifecycleEvent.OnCreate(appId),
            MiniAppLifecycleEvent.OnStart(appId),
            MiniAppLifecycleEvent.OnResume(appId)
        )
        
        events.forEach { event ->
            lifecycleHistory = lifecycleHistory + event
            onLifecycleEvent(event)
            lifecycleState = when (event) {
                is MiniAppLifecycleEvent.OnCreate -> MiniAppLifecycleState.CREATED
                is MiniAppLifecycleEvent.OnStart -> MiniAppLifecycleState.STARTED
                is MiniAppLifecycleEvent.OnResume -> MiniAppLifecycleState.RESUMED
                is MiniAppLifecycleEvent.OnPause -> MiniAppLifecycleState.PAUSED
                is MiniAppLifecycleEvent.OnStop -> MiniAppLifecycleState.STOPPED
                is MiniAppLifecycleEvent.OnDestroy -> MiniAppLifecycleState.DESTROYED
            }
        }
    }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "小程序生命周期",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // 当前状态
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前状态",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = lifecycleState.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 生命周期历史
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lifecycleHistory.reversed()) { event ->
                LifecycleEventItem(event = event)
            }
        }
        
        // 生命周期控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val event = MiniAppLifecycleEvent.OnPause(appId)
                    lifecycleHistory = lifecycleHistory + event
                    onLifecycleEvent(event)
                    lifecycleState = MiniAppLifecycleState.PAUSED
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("暂停")
            }
            
            Button(
                onClick = {
                    val event = MiniAppLifecycleEvent.OnResume(appId)
                    lifecycleHistory = lifecycleHistory + event
                    onLifecycleEvent(event)
                    lifecycleState = MiniAppLifecycleState.RESUMED
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("恢复")
            }
            
            OutlinedButton(
                onClick = {
                    val event = MiniAppLifecycleEvent.OnDestroy(appId)
                    lifecycleHistory = lifecycleHistory + event
                    onLifecycleEvent(event)
                    lifecycleState = MiniAppLifecycleState.DESTROYED
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("销毁")
            }
        }
    }
}

/**
 * 生命周期事件项组件
 */
@Composable
private fun LifecycleEventItem(
    event: MiniAppLifecycleEvent
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = UnifyReflectionUtils.getClassName(event),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "AppId: ${event.appId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = getCurrentTimeMillis().toString().takeLast(6),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 加载小程序数据（平台特定实现）
 */
expect suspend fun loadMiniApp(appId: String, config: MiniAppConfig): MiniAppData

/**
 * 小程序配置
 */
data class MiniAppConfig(
    val version: String = "1.0.0",
    val enableDebug: Boolean = false,
    val maxMemoryMB: Int = 512,
    val allowedApis: List<String> = emptyList()
)

/**
 * 小程序数据
 */
data class MiniAppData(
    val appId: String,
    val name: String,
    val version: String,
    val description: String,
    val icon: String,
    val pages: List<MiniAppPage>,
    val features: List<MiniAppFeature>
)

/**
 * 小程序页面
 */
data class MiniAppPage(
    val pageId: String,
    val title: String,
    val description: String,
    val icon: String,
    val path: String
)

/**
 * 小程序功能
 */
data class MiniAppFeature(
    val featureId: String,
    val name: String,
    val description: String,
    val icon: String,
    val isEnabled: Boolean
)

/**
 * 小程序状态
 */
enum class MiniAppState {
    LOADING, LOADED, ERROR
}

/**
 * 小程序事件
 */
sealed class MiniAppEvent {
    data class Loading(val appId: String) : MiniAppEvent()
    data class Loaded(val appId: String, val data: MiniAppData) : MiniAppEvent()
    data class Error(val appId: String, val message: String) : MiniAppEvent()
    data class NavigateTo(val appId: String, val pageId: String) : MiniAppEvent()
    data class UseFeature(val appId: String, val featureId: String) : MiniAppEvent()
    data class Share(val appId: String) : MiniAppEvent()
    data class Settings(val appId: String) : MiniAppEvent()
    data class About(val appId: String) : MiniAppEvent()
    data class Refresh(val appId: String) : MiniAppEvent()
    data class Close(val appId: String) : MiniAppEvent()
}

/**
 * 小程序API配置
 */
data class MiniAppApiConfig(
    val baseUrl: String = "",
    val timeout: Long = 30000,
    val retryCount: Int = 3,
    val enableLogging: Boolean = false
)

/**
 * 小程序API调用
 */
data class MiniAppApiCall(
    val callId: String,
    val appId: String,
    val method: String,
    val params: Map<String, Any>
)

/**
 * 小程序API结果
 */
sealed class MiniAppApiResult {
    data class Success(val data: String) : MiniAppApiResult()
    data class Error(val message: String) : MiniAppApiResult()
}

/**
 * 小程序生命周期状态
 */
enum class MiniAppLifecycleState {
    CREATED, STARTED, RESUMED, PAUSED, STOPPED, DESTROYED
}

/**
 * 小程序生命周期事件
 */
sealed class MiniAppLifecycleEvent {
    abstract val appId: String
    
    data class OnCreate(override val appId: String) : MiniAppLifecycleEvent()
    data class OnStart(override val appId: String) : MiniAppLifecycleEvent()
    data class OnResume(override val appId: String) : MiniAppLifecycleEvent()
    data class OnPause(override val appId: String) : MiniAppLifecycleEvent()
    data class OnStop(override val appId: String) : MiniAppLifecycleEvent()
    data class OnDestroy(override val appId: String) : MiniAppLifecycleEvent()
}
