# 小程序Compose集成指南

## 概述

Unify-Core框架提供了完整的小程序运行时支持，能够在Android、iOS、Web、Desktop等平台上运行各种小程序，包括微信、支付宝、字节跳动、百度、快手、小米、华为、QQ等平台的小程序。

## 支持的小程序平台

- ✅ **微信小程序** - 通过WebView/WKWebView运行
- ✅ **支付宝小程序** - 通过WebView/WKWebView运行  
- ✅ **字节跳动小程序** - 通过WebView/WKWebView运行
- ✅ **百度智能小程序** - 通过WebView/WKWebView运行
- ✅ **快手小程序** - 通过WebView/WKWebView运行
- ✅ **小米小程序** - 通过WebView/WKWebView运行
- ✅ **华为快应用** - 通过WebView/WKWebView运行
- ✅ **QQ小程序** - 通过WebView/WKWebView运行

## 核心架构

### 运行时架构
```
┌─────────────────────────────────────────┐
│           MiniApp Runtime               │
├─────────────────────────────────────────┤
│  Platform Specific Implementation      │
│  ┌─────────┬─────────┬─────────┬──────┐ │
│  │ Android │   iOS   │   Web   │ Desk │ │
│  └─────────┴─────────┴─────────┴──────┘ │
├─────────────────────────────────────────┤
│         Common Core Layer               │
│  ┌─────────────┬─────────────────────┐  │
│  │ API Handler │ Event Manager       │  │
│  │ State Mgr   │ Lifecycle Manager   │  │
│  └─────────────┴─────────────────────┘  │
└─────────────────────────────────────────┘
```

### Compose集成架构
```
┌─────────────────────────────────────────┐
│        Compose Components               │
├─────────────────────────────────────────┤
│      MiniApp Compose Bridge            │
├─────────────────────────────────────────┤
│      MiniApp Native Components         │
├─────────────────────────────────────────┤
│         Platform WebView                │
└─────────────────────────────────────────┘
```

## 快速开始

### 1. 初始化小程序运行时

```kotlin
// 创建微信小程序运行时
val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)

// 配置小程序
val config = MiniAppConfig(
    platform = MiniAppPlatform.WECHAT,
    appId = "your_app_id",
    version = "1.0.0",
    enableDebug = true,
    enableCompose = true
)

// 初始化运行时
runtime.initialize(config)
```

### 2. 启动小程序

```kotlin
// 启动小程序
val instance = runtime.launch(
    appId = "your_app_id",
    params = mapOf(
        "scene" to "1001",
        "query" to "param1=value1&param2=value2"
    )
)

println("小程序实例ID: ${instance.instanceId}")
```

### 3. 注册API处理器

```kotlin
// 注册自定义API
runtime.registerApi("getUserInfo") { params ->
    MiniAppApiResult(
        success = true,
        data = mapOf(
            "nickName" to "用户昵称",
            "avatarUrl" to "https://example.com/avatar.jpg"
        )
    )
}

// 调用API
val result = runtime.callApi("getUserInfo", emptyMap())
if (result.success) {
    println("用户信息: ${result.data}")
}
```

### 4. Compose组件集成

```kotlin
// 创建Compose集成
val bridge = object : MiniAppComposeBridge() {
    override suspend fun renderCompose(
        composableId: String,
        props: Map<String, Any>
    ): MiniAppViewResult {
        // 将Compose组件转换为小程序视图
        return MiniAppViewResult(
            viewId = "view_${System.currentTimeMillis()}",
            viewType = "view",
            properties = props
        )
    }
    
    override suspend fun updateProps(
        viewId: String,
        props: Map<String, Any>
    ): Boolean {
        // 更新视图属性
        return true
    }
    
    override suspend fun destroyView(viewId: String): Boolean {
        // 销毁视图
        return true
    }
}

val integration = MiniAppComposeIntegration(runtime, bridge)

// 注册标准组件
MiniAppUtils.createStandardComponents().forEach { component ->
    integration.registerComponent(component)
}

// 渲染组件
val viewId = integration.renderComponent(
    componentId = "button",
    props = mapOf(
        "text" to "点击我",
        "type" to "primary"
    )
)
```

## 平台特定实现

### Android平台

```kotlin
// 在Application中初始化
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MiniAppRuntime.initialize(this)
    }
}

// 在Activity中使用
class MainActivity : ComponentActivity() {
    private lateinit var runtime: MiniAppRuntime
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        
        lifecycleScope.launch {
            runtime.initialize(config)
            runtime.launch("your_app_id")
        }
    }
}
```

### iOS平台

```kotlin
// iOS平台使用WKWebView
val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)

// 监听生命周期
runtime.getLifecycleFlow().collect { state ->
    when (state) {
        MiniAppLifecycleState.LAUNCHED -> {
            NSLog("小程序已启动")
        }
        MiniAppLifecycleState.SHOWN -> {
            NSLog("小程序已显示")
        }
        MiniAppLifecycleState.DESTROYED -> {
            NSLog("小程序已销毁")
        }
    }
}
```

### Web平台

```kotlin
// Web平台直接在浏览器中运行
val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)

// 检测小程序环境
if (js("typeof wx !== 'undefined'")) {
    console.log("运行在微信小程序环境中")
} else {
    console.log("运行在Web环境中")
}
```

### Desktop平台

```kotlin
// Desktop平台通过系统浏览器打开
val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)

// 启动后会在默认浏览器中打开小程序
runtime.launch("your_app_id")
```

## API参考

### 核心API

#### 系统信息
```kotlin
runtime.callApi("getSystemInfo", emptyMap())
// 返回: 平台信息、版本号、设备信息等
```

#### 存储操作
```kotlin
// 设置存储
runtime.callApi("setStorage", mapOf(
    "key" to "user_data",
    "data" to userData
))

// 获取存储
runtime.callApi("getStorage", mapOf("key" to "user_data"))

// 删除存储
runtime.callApi("removeStorage", mapOf("key" to "user_data"))
```

#### 界面交互
```kotlin
// 显示Toast
runtime.callApi("showToast", mapOf(
    "title" to "操作成功",
    "duration" to 2000
))

// 显示对话框
runtime.callApi("showDialog", mapOf(
    "title" to "确认",
    "content" to "是否继续操作？"
))
```

#### 页面导航
```kotlin
// 跳转页面
runtime.callApi("navigateTo", mapOf(
    "url" to "/pages/detail/detail",
    "params" to mapOf("id" to "123")
))

// 返回上一页
runtime.callApi("navigateBack", emptyMap())
```

### 组件API

#### 标准组件

##### View组件
```kotlin
val viewComponent = MiniAppComposeComponent(
    id = "view",
    name = "View",
    type = MiniAppComponentType.VIEW,
    props = mapOf(
        "style" to MiniAppPropDefinition("style", MiniAppPropType.OBJECT),
        "className" to MiniAppPropDefinition("className", MiniAppPropType.STRING)
    ),
    events = listOf("tap", "longpress")
)
```

##### Button组件
```kotlin
integration.renderComponent("button", mapOf(
    "text" to "按钮文字",
    "type" to "primary", // default, primary, warn
    "size" to "default", // mini, default
    "disabled" to false
))
```

##### Input组件
```kotlin
integration.renderComponent("input", mapOf(
    "value" to "输入内容",
    "placeholder" to "请输入内容",
    "type" to "text", // text, number, password
    "maxlength" to 100
))
```

## 事件处理

### 生命周期事件
```kotlin
runtime.getLifecycleFlow().collect { state ->
    when (state) {
        MiniAppLifecycleState.CREATED -> {
            // 小程序已创建
        }
        MiniAppLifecycleState.LAUNCHED -> {
            // 小程序已启动
        }
        MiniAppLifecycleState.SHOWN -> {
            // 小程序已显示
        }
        MiniAppLifecycleState.HIDDEN -> {
            // 小程序已隐藏
        }
        MiniAppLifecycleState.DESTROYED -> {
            // 小程序已销毁
        }
    }
}
```

### 自定义事件
```kotlin
val eventManager = MiniAppEventManager()

// 注册事件监听器
eventManager.addEventListener("custom_event") { event ->
    println("收到自定义事件: ${event.data}")
}

// 触发事件
eventManager.emitEvent(MiniAppEvent(
    type = "custom_event",
    data = mapOf("message" to "Hello World")
))
```

## 主题系统

### 使用预定义主题
```kotlin
val themeManager = MiniAppThemeManager()

// 设置默认主题
themeManager.setTheme(MiniAppTheme.DEFAULT)

// 设置深色主题
themeManager.setTheme(MiniAppTheme.DARK)

// 监听主题变化
themeManager.addThemeListener { theme ->
    println("主题已切换到: ${theme.name}")
}
```

### 自定义主题
```kotlin
val customTheme = MiniAppTheme(
    name = "custom",
    colors = mapOf(
        "primary" to "#FF6B6B",
        "secondary" to "#4ECDC4",
        "background" to "#F7F7F7"
    ),
    fonts = mapOf(
        "body" to "PingFang SC",
        "heading" to "PingFang SC Bold"
    ),
    spacing = mapOf(
        "xs" to 4,
        "sm" to 8,
        "md" to 16
    )
)

themeManager.setTheme(customTheme)
```

## 最佳实践

### 1. 错误处理
```kotlin
try {
    val result = runtime.callApi("someApi", params)
    if (!result.success) {
        println("API调用失败: ${result.error}")
    }
} catch (e: Exception) {
    println("运行时错误: ${e.message}")
}
```

### 2. 内存管理
```kotlin
// 及时销毁不需要的实例
runtime.destroyInstance(instanceId)

// 清理事件监听器
eventManager.removeEventListener("event_type", listener)
```

### 3. 性能优化
```kotlin
// 使用缓存减少重复渲染
val cachedViewId = viewCache[componentId]
if (cachedViewId != null) {
    integration.updateComponent(componentId, newProps)
} else {
    integration.renderComponent(componentId, props)
}
```

### 4. 调试支持
```kotlin
val config = MiniAppConfig(
    platform = MiniAppPlatform.WECHAT,
    appId = "your_app_id",
    version = "1.0.0",
    enableDebug = true, // 启用调试模式
    composeConfig = ComposeConfig(
        enableHotReload = true, // 启用热重载
        enablePreview = true    // 启用预览
    )
)
```

## 故障排除

### 常见问题

1. **小程序无法启动**
   - 检查appId是否正确
   - 确认网络连接正常
   - 验证平台配置

2. **API调用失败**
   - 检查API名称是否正确
   - 验证参数格式
   - 确认权限设置

3. **组件渲染异常**
   - 验证组件属性类型
   - 检查必需属性是否提供
   - 确认组件已正确注册

### 调试工具

```kotlin
// 启用详细日志
runtime.registerApi("enableDebugLog") { params ->
    // 启用调试日志输出
    MiniAppApiResult(success = true)
}

// 获取运行时状态
runtime.registerApi("getRuntimeStatus") { params ->
    MiniAppApiResult(
        success = true,
        data = mapOf(
            "currentInstance" to runtime.getCurrentInstance()?.instanceId,
            "registeredApis" to apiHandlers.keys.toList(),
            "componentCount" to componentRegistry.size
        )
    )
}
```

## 示例项目

完整的示例项目可以在以下目录找到：
- `examples/miniapp-demo/` - 基础小程序示例
- `examples/miniapp-compose/` - Compose集成示例
- `examples/miniapp-multiplatform/` - 多平台小程序示例

## 更新日志

### v1.0.0
- 初始版本发布
- 支持8大小程序平台
- 完整的Compose集成
- 跨平台运行时支持
