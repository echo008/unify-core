package core

import state.MiniAppStateManager
import style.StyleConverter

class EnhancedMiniAppBridge {
    private val componentRegistry = mutableMapOf<String, ComponentRenderer>()
    private val stateManager = MiniAppStateManager()
    private val eventBus = MiniAppEventBus()
    private val styleConverter = StyleConverter()

    init {
        registerDefaultComponents()
    }

// 简单 JSON 序列化（仅支持基础类型与字符串）
private fun Map<String, Any>.toJson(): String {
    fun Any.toJsonValue(): String = when (this) {
        is String -> "\"" + this.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        is Number, is Boolean -> this.toString()
        is Map<*, *> -> (this as Map<String, Any>).toJson()
        is List<*> -> this.joinToString(prefix = "[", postfix = "]") { (it as Any).toJsonValue() }
        else -> "\"${'$'}{this.toString()}\""
    }
    return entries.joinToString(prefix = "{", postfix = "}") { (k, v) ->
        "\"${'$'}k\": ${'$'}{(v as Any).toJsonValue()}"
    }
}

    private fun registerDefaultComponents() {
        componentRegistry["UnifyText"] = TextComponentRenderer()
        componentRegistry["UnifyButton"] = ButtonComponentRenderer()
        componentRegistry["UnifyImage"] = ImageComponentRenderer()
        componentRegistry["UnifyInput"] = InputComponentRenderer()
        componentRegistry["UnifyView"] = ViewComponentRenderer()
        componentRegistry["UnifyLazyColumn"] = LazyColumnComponentRenderer()
        componentRegistry["UnifyCard"] = CardComponentRenderer()
        componentRegistry["UnifyDialog"] = DialogComponentRenderer()
    }

    fun renderPage(pageData: PageRenderData): MiniAppPageResult {
        val wxml = generateWXML(pageData.componentTree)
        val wxss = generateWXSS(pageData.styles)
        val js = generateJS(pageData.logic)
        val json = generateJSON(pageData.config)

        return MiniAppPageResult(wxml = wxml, wxss = wxss, js = js, json = json)
    }

    private fun generateWXML(componentTree: ComponentNode): String = buildString {
        appendLine("<view class='page-container'>")
        append(renderComponentTree(componentTree))
        appendLine("</view>")
    }

    fun renderComponentTree(node: ComponentNode): String {
        val renderer = componentRegistry[node.type] ?: return ""
        return renderer.render(node, this)
    }

    private fun generateWXSS(styles: Map<String, StyleProperties>): String = buildString {
        appendLine(".page-container { padding: 20rpx; }")
        styles.forEach { (selector, properties) ->
            appendLine("$selector {")
            properties.forEach { (key, value) ->
                appendLine("  ${styleConverter.convertProperty(key, value)};")
            }
            appendLine("}")
        }
    }

    private fun generateJS(logic: PageLogic): String = """
            const bridge = require('./bridge/wechat-bridge.js');
            
            Page({
                data: ${'$'}{logic.initialData.toJson()},
                
                onLoad: function(options) {
                    bridge.onLoad(options);
                    this.initializeComponents();
                },
                
                onShow: function() { bridge.onShow(); },
                onHide: function() { bridge.onHide(); },
                onUnload: function() { bridge.onUnload(); },
                
                initializeComponents: function() {
                    ${'$'}{logic.componentInitializers.joinToString("\n                    ")}
                },
                
                handleEvent: function(e) {
                    const { type, detail, currentTarget } = e;
                    bridge.handleComponentEvent(currentTarget.id, type, detail);
                },
                
                updateState: function(newState) { this.setData(newState); }
            });
        """.trimIndent()

    private fun generateJSON(config: PageConfig): String = """
            {
                "navigationBarTitleText": "${'$'}{config.title}",
                "backgroundColor": "${'$'}{config.backgroundColor}",
                "navigationBarBackgroundColor": "${'$'}{config.navigationBarColor}",
                "navigationBarTextStyle": "${'$'}{config.navigationBarTextStyle}",
                "enablePullDownRefresh": ${'$'}{config.enablePullDownRefresh},
                "usingComponents": {
                    ${'$'}{config.customComponents.entries.joinToString(",\n                    ") { "\"${'$'}{it.key}\": \"${'$'}{it.value}\"" }}
                }
            }
        """.trimIndent()
}

// --- 组件渲染器定义与实现 ---
interface ComponentRenderer {
    fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String
}

class TextComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String {
        val props = node.props
        return """
            <text 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}">
                ${'$'}{props["text"] ?: ""}
            </text>
        """.trimIndent()
    }
}

class ButtonComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String {
        return """
            <button 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}"
                disabled="${'$'}{node.props["disabled"] ?: false}"
                bindtap="handleEvent">
                ${'$'}{renderChildren(node.children, bridge)}
            </button>
        """.trimIndent()
    }
    private fun renderChildren(children: List<ComponentNode>, bridge: EnhancedMiniAppBridge) =
        children.joinToString("") { bridge.renderComponentTree(it) }
}

class ImageComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String {
        val props = node.props
        return """
            <image 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}"
                src="${'$'}{props["src"] ?: ""}"
                mode="${'$'}{props["contentScale"] ?: "aspectFit"}"
                lazy-load="true"
                binderror="handleEvent"
                bindload="handleEvent" />
        """.trimIndent()
    }
}

class InputComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String {
        val props = node.props
        return """
            <input 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}"
                value="${'$'}{props["value"] ?: ""}"
                placeholder="${'$'}{props["placeholder"] ?: ""}"
                disabled="${'$'}{props["disabled"] ?: false}"
                bindinput="handleEvent"
                bindfocus="handleEvent"
                bindblur="handleEvent" />
        """.trimIndent()
    }
}

class LazyColumnComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String = """
            <scroll-view 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}"
                scroll-y="true"
                enable-back-to-top="true"
                bindscrolltolower="handleEvent"
                bindscroll="handleEvent">
                ${'$'}{renderChildren(node.children, bridge)}
            </scroll-view>
        """.trimIndent()
    private fun renderChildren(children: List<ComponentNode>, bridge: EnhancedMiniAppBridge) =
        children.joinToString("") { bridge.renderComponentTree(it) }
}

class ViewComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String = """
            <view 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}"
                bindtap="handleEvent">
                ${'$'}{renderChildren(node.children, bridge)}
            </view>
        """.trimIndent()
    private fun renderChildren(children: List<ComponentNode>, bridge: EnhancedMiniAppBridge) =
        children.joinToString("") { bridge.renderComponentTree(it) }
}

class CardComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String = """
            <view 
                class="card ${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}">
                ${'$'}{renderChildren(node.children, bridge)}
            </view>
        """.trimIndent()
    private fun renderChildren(children: List<ComponentNode>, bridge: EnhancedMiniAppBridge) =
        children.joinToString("") { bridge.renderComponentTree(it) }
}

class DialogComponentRenderer : ComponentRenderer {
    override fun render(node: ComponentNode, bridge: EnhancedMiniAppBridge): String {
        val props = node.props
        return """
            <modal 
                class="${'$'}{node.className}" 
                style="${'$'}{node.inlineStyle}"
                data-component-id="${'$'}{node.id}"
                show="${'$'}{props["show"] ?: false}"
                bindcancel="handleEvent"
                bindconfirm="handleEvent">
                <view class="modal-content">
                    ${'$'}{renderChildren(node.children, bridge)}
                </view>
            </modal>
        """.trimIndent()
    }
    private fun renderChildren(children: List<ComponentNode>, bridge: EnhancedMiniAppBridge) =
        children.joinToString("") { bridge.renderComponentTree(it) }
}

// --- 数据类定义 ---
data class ComponentNode(
    val id: String,
    val type: String,
    val props: Map<String, Any>,
    val children: List<ComponentNode>,
    val className: String = "",
    val inlineStyle: String = ""
)

data class PageRenderData(
    val componentTree: ComponentNode,
    val styles: Map<String, StyleProperties>,
    val logic: PageLogic,
    val config: PageConfig
)

data class MiniAppPageResult(
    val wxml: String,
    val wxss: String,
    val js: String,
    val json: String
)

data class PageLogic(
    val initialData: Map<String, Any>,
    val componentInitializers: List<String>
)

data class PageConfig(
    val title: String,
    val backgroundColor: String,
    val navigationBarColor: String,
    val navigationBarTextStyle: String,
    val enablePullDownRefresh: Boolean,
    val customComponents: Map<String, String>
)

typealias StyleProperties = Map<String, String>
