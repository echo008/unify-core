package com.unify.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock

/**
 * 统一组件协议（集成KuiklyUI特性）
 * 基于文档第5章要求实现的核心组件协议
 */
interface UnifyComponentProtocol {
    val componentId: String
    val componentType: ComponentType
    
    // KuiklyUI核心特性：HarmonyOS ArkUI组件映射
    val kuiklyUIMapping: KuiklyUIComponentMapping?
    
    // KuiklyUI性能优化配置
    val performanceConfig: KuiklyPerformanceConfig
    
    @Composable
    fun renderContent(): @Composable () -> Unit
    
    fun getPerformanceMetrics(): ComponentPerformanceMetrics
    fun updateState(newState: Any)
    fun handleEvent(event: ComponentEvent)
    
    // KuiklyUI特有：HarmonyOS原生渲染
    fun renderToArkUI(context: ArkUIContext): ArkUIComponent
    
    // 生命周期管理
    fun onCreate()
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()
    
    // 错误处理
    fun onError(error: Throwable): Boolean
    
    // 平台特定配置
    fun getPlatformConfig(): PlatformComponentConfig?
}

/**
 * KuiklyUI组件映射配置
 */
data class KuiklyUIComponentMapping(
    val arkUIComponent: String,
    val propertyMapping: Map<String, String>,
    val eventMapping: Map<String, String>,
    val styleMapping: Map<String, String>,
    val animationMapping: Map<String, String> = emptyMap()
)

/**
 * KuiklyUI性能配置
 */
data class KuiklyPerformanceConfig(
    val enableLazyLoading: Boolean = true,
    val enableMemoryOptimization: Boolean = true,
    val enableRenderCaching: Boolean = true,
    val enableArkUIAcceleration: Boolean = true,
    val maxCacheSize: Int = 100,
    val preloadThreshold: Int = 3
)

/**
 * 组件类型枚举
 */
enum class ComponentType {
    BUTTON, TEXT, IMAGE, LIST, INPUT, CONTAINER, NAVIGATION, CUSTOM
}

/**
 * 平台组件配置
 */
data class PlatformComponentConfig(
    val androidConfig: AndroidComponentConfig? = null,
    val iosConfig: IosComponentConfig? = null,
    val webConfig: WebComponentConfig? = null,
    val harmonyOSConfig: HarmonyOSComponentConfig? = null
)

data class AndroidComponentConfig(
    val useNativeView: Boolean = false,
    val customAttributes: Map<String, Any> = emptyMap()
)

data class IosComponentConfig(
    val useUIKit: Boolean = false,
    val customProperties: Map<String, Any> = emptyMap()
)

data class WebComponentConfig(
    val useDOMElement: Boolean = false,
    val cssClasses: List<String> = emptyList()
)

data class HarmonyOSComponentConfig(
    val useArkUI: Boolean = true,
    val arkUIProperties: Map<String, Any> = emptyMap()
)

/**
 * 组件事件
 */
sealed class ComponentEvent {
    object Click : ComponentEvent()
    object LongPress : ComponentEvent()
    data class TextChanged(val text: String) : ComponentEvent()
    data class StateChanged(val newState: ComponentState) : ComponentEvent()
    data class CustomEvent(val eventType: String, val data: Any?) : ComponentEvent()
}

/**
 * 组件状态
 */
sealed class ComponentState {
    object Idle : ComponentState()
    object Loading : ComponentState()
    data class Error(val message: String, val exception: Throwable? = null) : ComponentState()
    data class Success(val data: Any?) : ComponentState()
}

/**
 * 性能指标
 */
data class ComponentPerformanceMetrics(
    val renderTime: Long,
    val memoryUsage: Long,
    val recompositionCount: Int,
    val cacheHitRate: Double = 0.0,
    val kuiklyOptimizations: KuiklyOptimizationMetrics? = null
)

/**
 * KuiklyUI优化指标
 */
data class KuiklyOptimizationMetrics(
    val cacheHitRate: Double,
    val memoryPoolUsage: Long,
    val arkUIAccelerationEnabled: Boolean,
    val arkUIPerformance: ArkUIMetrics? = null
)

/**
 * ArkUI性能指标
 */
data class ArkUIMetrics(
    val renderTime: Long,
    val nativeCallCount: Int,
    val memoryFootprint: Long
)

/**
 * ArkUI上下文（HarmonyOS专用）
 */
interface ArkUIContext {
    val deviceInfo: DeviceInfo
    val displayMetrics: DisplayMetrics
    val platformVersion: String
}

/**
 * ArkUI组件（HarmonyOS专用）
 */
interface ArkUIComponent {
    fun mount(): Unit
    fun unmount(): Unit
    fun update(properties: Map<String, Any>): Unit
    fun getMetrics(): ArkUIMetrics
}

/**
 * 设备信息
 */
data class DeviceInfo(
    val deviceModel: String,
    val osVersion: String,
    val screenDensity: Float,
    val availableMemory: Long
)

/**
 * 显示指标
 */
data class DisplayMetrics(
    val widthPixels: Int,
    val heightPixels: Int,
    val density: Float,
    val scaledDensity: Float
)
