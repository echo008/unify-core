package com.unify.core.miniapp

import kotlinx.serialization.Serializable

/**
 * 小程序Compose集成核心类
 * 提供Compose组件与小程序原生组件的双向绑定
 */
class MiniAppComposeIntegration(
    private val runtime: MiniAppRuntime,
    private val bridge: MiniAppComposeBridge
) {
    private val componentRegistry = mutableMapOf<String, MiniAppComposeComponent>()
    private val viewBindings = mutableMapOf<String, String>() // composableId -> viewId

    /**
     * 注册Compose组件
     */
    fun registerComponent(component: MiniAppComposeComponent) {
        componentRegistry[component.id] = component
    }

    /**
     * 渲染Compose组件到小程序
     */
    suspend fun renderComponent(
        componentId: String,
        props: Map<String, Any> = emptyMap()
    ): String? {
        val component = componentRegistry[componentId] ?: return null
        
        val viewResult = bridge.renderCompose(componentId, props)
        viewBindings[componentId] = viewResult.viewId
        
        return viewResult.viewId
    }

    /**
     * 更新组件属性
     */
    suspend fun updateComponent(
        componentId: String,
        props: Map<String, Any>
    ): Boolean {
        val viewId = viewBindings[componentId] ?: return false
        return bridge.updateProps(viewId, props)
    }

    /**
     * 销毁组件
     */
    suspend fun destroyComponent(componentId: String): Boolean {
        val viewId = viewBindings[componentId] ?: return false
        val result = bridge.destroyView(viewId)
        if (result) {
            viewBindings.remove(componentId)
        }
        return result
    }

    /**
     * 获取所有已注册的组件
     */
    fun getRegisteredComponents(): List<MiniAppComposeComponent> {
        return componentRegistry.values.toList()
    }
}

/**
 * 小程序Compose组件定义
 */
@Serializable
data class MiniAppComposeComponent(
    val id: String,
    val name: String,
    val type: MiniAppComponentType,
    val props: Map<String, MiniAppPropDefinition>,
    val events: List<String> = emptyList(),
    val children: List<String> = emptyList()
)

/**
 * 小程序组件类型
 */
enum class MiniAppComponentType {
    VIEW,           // 视图容器
    TEXT,           // 文本
    BUTTON,         // 按钮
    INPUT,          // 输入框
    IMAGE,          // 图片
    LIST,           // 列表
    SCROLL_VIEW,    // 滚动视图
    SWIPER,         // 轮播图
    PICKER,         // 选择器
    SWITCH,         // 开关
    SLIDER,         // 滑块
    PROGRESS,       // 进度条
    CUSTOM          // 自定义组件
}

/**
 * 属性定义
 */
@Serializable
data class MiniAppPropDefinition(
    val name: String,
    val type: MiniAppPropType,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val description: String = ""
)

/**
 * 属性类型
 */
enum class MiniAppPropType {
    STRING,
    NUMBER,
    BOOLEAN,
    OBJECT,
    ARRAY,
    FUNCTION
}

/**
 * 小程序页面管理器
 */
class MiniAppPageManager(private val runtime: MiniAppRuntime) {
    private val pages = mutableMapOf<String, MiniAppPage>()
    private var currentPageId: String? = null

    /**
     * 注册页面
     */
    fun registerPage(page: MiniAppPage) {
        pages[page.id] = page
    }

    /**
     * 导航到页面
     */
    suspend fun navigateTo(pageId: String, params: Map<String, Any> = emptyMap()): Boolean {
        val page = pages[pageId] ?: return false
        
        val result = runtime.callApi("navigateTo", mapOf(
            "url" to page.path,
            "params" to params
        ))
        
        if (result.success) {
            currentPageId = pageId
        }
        
        return result.success
    }

    /**
     * 返回上一页
     */
    suspend fun navigateBack(): Boolean {
        val result = runtime.callApi("navigateBack", emptyMap())
        return result.success
    }

    /**
     * 获取当前页面
     */
    fun getCurrentPage(): MiniAppPage? {
        return currentPageId?.let { pages[it] }
    }

    /**
     * 获取所有页面
     */
    fun getAllPages(): List<MiniAppPage> {
        return pages.values.toList()
    }
}

/**
 * 小程序页面定义
 */
@Serializable
data class MiniAppPage(
    val id: String,
    val path: String,
    val title: String,
    val components: List<String> = emptyList(),
    val config: MiniAppPageConfig = MiniAppPageConfig()
)

/**
 * 页面配置
 */
@Serializable
data class MiniAppPageConfig(
    val navigationBarTitleText: String = "",
    val navigationBarBackgroundColor: String = "#ffffff",
    val navigationBarTextStyle: String = "black",
    val backgroundColor: String = "#ffffff",
    val enablePullDownRefresh: Boolean = false,
    val onReachBottomDistance: Int = 50
)

/**
 * 小程序主题管理器
 */
class MiniAppThemeManager {
    private var currentTheme: MiniAppTheme = MiniAppTheme.DEFAULT
    private val themeListeners = mutableListOf<(MiniAppTheme) -> Unit>()

    /**
     * 设置主题
     */
    fun setTheme(theme: MiniAppTheme) {
        currentTheme = theme
        themeListeners.forEach { it(theme) }
    }

    /**
     * 获取当前主题
     */
    fun getCurrentTheme(): MiniAppTheme = currentTheme

    /**
     * 添加主题变化监听器
     */
    fun addThemeListener(listener: (MiniAppTheme) -> Unit) {
        themeListeners.add(listener)
    }

    /**
     * 移除主题变化监听器
     */
    fun removeThemeListener(listener: (MiniAppTheme) -> Unit) {
        themeListeners.remove(listener)
    }
}

/**
 * 小程序主题
 */
@Serializable
data class MiniAppTheme(
    val name: String,
    val colors: Map<String, String>,
    val fonts: Map<String, String>,
    val spacing: Map<String, Int>
) {
    companion object {
        val DEFAULT = MiniAppTheme(
            name = "default",
            colors = mapOf(
                "primary" to "#007AFF",
                "secondary" to "#5856D6",
                "background" to "#FFFFFF",
                "surface" to "#F2F2F7",
                "text" to "#000000",
                "textSecondary" to "#8E8E93"
            ),
            fonts = mapOf(
                "body" to "system",
                "heading" to "system-bold"
            ),
            spacing = mapOf(
                "xs" to 4,
                "sm" to 8,
                "md" to 16,
                "lg" to 24,
                "xl" to 32
            )
        )

        val DARK = MiniAppTheme(
            name = "dark",
            colors = mapOf(
                "primary" to "#0A84FF",
                "secondary" to "#5E5CE6",
                "background" to "#000000",
                "surface" to "#1C1C1E",
                "text" to "#FFFFFF",
                "textSecondary" to "#8E8E93"
            ),
            fonts = mapOf(
                "body" to "system",
                "heading" to "system-bold"
            ),
            spacing = mapOf(
                "xs" to 4,
                "sm" to 8,
                "md" to 16,
                "lg" to 24,
                "xl" to 32
            )
        )
    }
}

/**
 * 小程序工具类
 */
object MiniAppUtils {
    /**
     * 创建标准组件
     */
    fun createStandardComponents(): List<MiniAppComposeComponent> {
        return listOf(
            // View组件
            MiniAppComposeComponent(
                id = "view",
                name = "View",
                type = MiniAppComponentType.VIEW,
                props = mapOf(
                    "style" to MiniAppPropDefinition("style", MiniAppPropType.OBJECT),
                    "className" to MiniAppPropDefinition("className", MiniAppPropType.STRING)
                ),
                events = listOf("tap", "longpress")
            ),
            
            // Text组件
            MiniAppComposeComponent(
                id = "text",
                name = "Text",
                type = MiniAppComponentType.TEXT,
                props = mapOf(
                    "text" to MiniAppPropDefinition("text", MiniAppPropType.STRING, required = true),
                    "style" to MiniAppPropDefinition("style", MiniAppPropType.OBJECT),
                    "selectable" to MiniAppPropDefinition("selectable", MiniAppPropType.BOOLEAN, defaultValue = "false")
                )
            ),
            
            // Button组件
            MiniAppComposeComponent(
                id = "button",
                name = "Button",
                type = MiniAppComponentType.BUTTON,
                props = mapOf(
                    "text" to MiniAppPropDefinition("text", MiniAppPropType.STRING, required = true),
                    "type" to MiniAppPropDefinition("type", MiniAppPropType.STRING, defaultValue = "default"),
                    "size" to MiniAppPropDefinition("size", MiniAppPropType.STRING, defaultValue = "default"),
                    "disabled" to MiniAppPropDefinition("disabled", MiniAppPropType.BOOLEAN, defaultValue = "false")
                ),
                events = listOf("tap")
            ),
            
            // Input组件
            MiniAppComposeComponent(
                id = "input",
                name = "Input",
                type = MiniAppComponentType.INPUT,
                props = mapOf(
                    "value" to MiniAppPropDefinition("value", MiniAppPropType.STRING),
                    "placeholder" to MiniAppPropDefinition("placeholder", MiniAppPropType.STRING),
                    "type" to MiniAppPropDefinition("type", MiniAppPropType.STRING, defaultValue = "text"),
                    "maxlength" to MiniAppPropDefinition("maxlength", MiniAppPropType.NUMBER, defaultValue = "140")
                ),
                events = listOf("input", "focus", "blur")
            )
        )
    }

    /**
     * 验证组件属性
     */
    fun validateProps(component: MiniAppComposeComponent, props: Map<String, Any>): List<String> {
        val errors = mutableListOf<String>()
        
        // 检查必需属性
        component.props.values.filter { it.required }.forEach { propDef ->
            if (!props.containsKey(propDef.name)) {
                errors.add("Required property '${propDef.name}' is missing")
            }
        }
        
        // 检查属性类型
        props.forEach { (name, value) ->
            val propDef = component.props[name]
            if (propDef != null) {
                val isValidType = when (propDef.type) {
                    MiniAppPropType.STRING -> value is String
                    MiniAppPropType.NUMBER -> value is Number
                    MiniAppPropType.BOOLEAN -> value is Boolean
                    MiniAppPropType.OBJECT -> value is Map<*, *>
                    MiniAppPropType.ARRAY -> value is List<*>
                    MiniAppPropType.FUNCTION -> true // 函数类型暂不验证
                }
                
                if (!isValidType) {
                    errors.add("Property '$name' has invalid type, expected ${propDef.type}")
                }
            }
        }
        
        return errors
    }
}
