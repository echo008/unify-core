package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.core.ui.components.*
import com.unify.core.mvi.*
import com.unify.core.performance.UnifyComposeOptimizer.PerformanceTracker
import kotlinx.coroutines.CoroutineScope

/**
 * 设置屏幕 - 展示主题切换和偏好设置
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { SettingsViewModel(scope) }
    
    PerformanceTracker("SettingsScreen") {
        UnifyMVIContainer(
            stateManager = viewModel,
            onEffect = { effect ->
                when (effect) {
                    is SettingsEffect.ShowMessage -> {
                        // 显示消息
                    }
                    is SettingsEffect.RestartRequired -> {
                        // 提示需要重启
                    }
                }
            }
        ) { state, isLoading, onIntent ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyButton(
                        onClick = onNavigateBack,
                        text = "← 返回"
                    )
                    Text(
                        text = "应用设置",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(80.dp))
                }
                
                // 外观设置
                UnifyCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🎨 外观设置",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // 主题切换
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "深色主题",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "切换应用主题模式",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isDarkTheme,
                                onCheckedChange = { onIntent(SettingsIntent.ToggleTheme) }
                            )
                        }
                        
                        // 动态颜色
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "动态颜色",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "根据壁纸调整应用颜色",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isDynamicColor,
                                onCheckedChange = { onIntent(SettingsIntent.ToggleDynamicColor) }
                            )
                        }
                    }
                }
                
                // 性能设置
                UnifyCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚡ 性能设置",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "性能监控",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "启用实时性能监控",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isPerformanceMonitorEnabled,
                                onCheckedChange = { onIntent(SettingsIntent.TogglePerformanceMonitor) }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "动画效果",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "启用界面动画效果",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isAnimationEnabled,
                                onCheckedChange = { onIntent(SettingsIntent.ToggleAnimation) }
                            )
                        }
                    }
                }
                
                // 数据设置
                UnifyCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💾 数据设置",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "自动同步",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "自动同步数据到云端",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.isAutoSyncEnabled,
                                onCheckedChange = { onIntent(SettingsIntent.ToggleAutoSync) }
                            )
                        }
                        
                        UnifyButton(
                            onClick = { onIntent(SettingsIntent.ClearCache) },
                            text = "清除缓存",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // 关于信息
                UnifyCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ℹ️ 关于应用",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        InfoRow("应用版本", "1.0.0")
                        InfoRow("构建版本", "2024.08.30")
                        InfoRow("Kotlin 版本", "1.9.22")
                        InfoRow("Compose 版本", "1.5.12")
                        InfoRow("平台支持", "Android, iOS, Desktop, Web")
                    }
                }
            }
        }
    }
}

/**
 * 设置 MVI 实现
 */
data class SettingsState(
    val isDarkTheme: Boolean = false,
    val isDynamicColor: Boolean = true,
    val isPerformanceMonitorEnabled: Boolean = true,
    val isAnimationEnabled: Boolean = true,
    val isAutoSyncEnabled: Boolean = true
) : UnifyState

sealed class SettingsIntent : UnifyIntent {
    object ToggleTheme : SettingsIntent()
    object ToggleDynamicColor : SettingsIntent()
    object TogglePerformanceMonitor : SettingsIntent()
    object ToggleAnimation : SettingsIntent()
    object ToggleAutoSync : SettingsIntent()
    object ClearCache : SettingsIntent()
    object LoadSettings : SettingsIntent()
    object SaveSettings : SettingsIntent()
}

sealed class SettingsEffect : UnifyEffect {
    data class ShowMessage(val message: String) : SettingsEffect()
    object RestartRequired : SettingsEffect()
}

class SettingsViewModel(scope: CoroutineScope) : UnifyViewModel<SettingsIntent, SettingsState, SettingsEffect>(
    initialState = SettingsState(),
    scope = scope
) {
    
    init {
        handleIntent(SettingsIntent.LoadSettings)
    }
    
    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.ToggleTheme -> {
                updateState { state ->
                    state.copy(isDarkTheme = !state.isDarkTheme)
                }
                handleIntent(SettingsIntent.SaveSettings)
                sendEffect(SettingsEffect.ShowMessage("主题已切换"))
            }
            
            SettingsIntent.ToggleDynamicColor -> {
                updateState { state ->
                    state.copy(isDynamicColor = !state.isDynamicColor)
                }
                handleIntent(SettingsIntent.SaveSettings)
                sendEffect(SettingsEffect.ShowMessage("动态颜色设置已更新"))
            }
            
            SettingsIntent.TogglePerformanceMonitor -> {
                updateState { state ->
                    state.copy(isPerformanceMonitorEnabled = !state.isPerformanceMonitorEnabled)
                }
                handleIntent(SettingsIntent.SaveSettings)
                sendEffect(SettingsEffect.ShowMessage("性能监控设置已更新"))
            }
            
            SettingsIntent.ToggleAnimation -> {
                updateState { state ->
                    state.copy(isAnimationEnabled = !state.isAnimationEnabled)
                }
                handleIntent(SettingsIntent.SaveSettings)
                sendEffect(SettingsEffect.ShowMessage("动画设置已更新"))
            }
            
            SettingsIntent.ToggleAutoSync -> {
                updateState { state ->
                    state.copy(isAutoSyncEnabled = !state.isAutoSyncEnabled)
                }
                handleIntent(SettingsIntent.SaveSettings)
                sendEffect(SettingsEffect.ShowMessage("自动同步设置已更新"))
            }
            
            SettingsIntent.ClearCache -> {
                handleAsyncIntent(intent) {
                    kotlinx.coroutines.delay(1000)
                    sendEffect(SettingsEffect.ShowMessage("缓存已清除"))
                }
            }
            
            SettingsIntent.LoadSettings -> {
                handleAsyncIntent(intent) {
                    // 模拟加载设置
                    kotlinx.coroutines.delay(500)
                    // 设置已加载到初始状态
                }
            }
            
            SettingsIntent.SaveSettings -> {
                handleAsyncIntent(intent) {
                    // 模拟保存设置
                    kotlinx.coroutines.delay(200)
                    // 设置已保存
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
