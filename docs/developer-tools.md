# 开发者工具集成指南

## 🎯 工具集成概述

Unify KMP 提供了丰富的开发者工具集成，帮助提升开发效率、改善开发体验，并确保代码质量。

## 🛠️ IDE集成

### IntelliJ IDEA 插件配置

#### 1. Kotlin 插件配置
```xml
<!-- IntelliJ IDEA 配置 -->
<project>
  <component name="KotlinCompilerSettings">
    <option name="jvmTarget" value="17" />
    <option name="incrementalCompilation" value="true" />
    <option name="composeCompilerPluginEnabled" value="true" />
  </component>
</project>
```

#### 2. Compose 插件配置
```kotlin
// build.gradle.kts
plugins {
    id("org.jetbrains.compose") version "1.6.0"
}

compose {
    kotlinCompilerPlugin.set("androidx.compose.compiler:compiler:1.5.8")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=2.0.21")
}
```

#### 3. 代码模板配置
```xml
<!-- File Templates -->
<template name="UnifyComposable" value="@Composable&#10;fun $NAME$(&#10;    modifier: Modifier = Modifier,&#10;    content: @Composable () -&gt; Unit&#10;) {&#10;    Box(modifier = modifier) {&#10;        content()&#10;    }&#10;}" />
```

### Android Studio 配置

#### 1. 设备配置
```xml
<!-- Android Studio 设备配置 -->
<device>
  <name>Unify Test Device</name>
  <target>Android 14.0</target>
  <api>34</api>
  <abi>x86_64</abi>
</device>
```

#### 2. 运行配置模板
```xml
<!-- Run Configuration Template -->
<configuration name="Unify KMP App" type="AndroidRunConfigurationType">
  <module name="unify-core.androidApp.main" />
  <option name="DEPLOY" value="true" />
  <option name="DEPLOY_APK_FROM_BUNDLE" value="false" />
  <option name="DEPLOY_AS_INSTANT" value="false" />
  <option name="ARTIFACT_NAME" value="" />
  <option name="PM_INSTALL_OPTIONS" value="" />
  <option name="CLEAR_APP_STORAGE" value="true" />
</configuration>
```

## 🔧 代码生成器

### 1. 组件生成器

#### 基础组件生成
```kotlin
// 自动生成组件代码
object ComponentGenerator {

    fun generateButton(name: String): String {
        return """
            @Composable
            fun ${name}Button(
                text: String,
                onClick: () -> Unit,
                modifier: Modifier = Modifier,
                enabled: Boolean = true
            ) {
                UnifyButton(
                    text = text,
                    onClick = onClick,
                    modifier = modifier,
                    enabled = enabled
                )
            }
        """.trimIndent()
    }

    fun generateCard(name: String): String {
        return """
            @Composable
            fun ${name}Card(
                modifier: Modifier = Modifier,
                content: @Composable () -> Unit
            ) {
                UnifyCard(modifier = modifier) {
                    content()
                }
            }
        """.trimIndent()
    }
}
```

#### 高级组件生成
```kotlin
// 生成完整的ViewModel
object ViewModelGenerator {

    fun generateViewModel(name: String): String {
        return """
            class ${name}ViewModel : UnifyViewModel<${name}Intent, ${name}State, ${name}Effect>() {

                override fun createInitialState(): ${name}State = ${name}State.Loading

                override fun handleIntent(intent: ${name}Intent) {
                    when (intent) {
                        is ${name}Intent.LoadData -> loadData()
                        is ${name}Intent.Refresh -> refresh()
                    }
                }

                private fun loadData() {
                    // 实现数据加载逻辑
                }

                private fun refresh() {
                    // 实现刷新逻辑
                }
            }
        """.trimIndent()
    }
}
```

### 2. API生成器

#### REST API生成
```kotlin
object ApiGenerator {

    fun generateApiClient(baseUrl: String, endpoints: List<ApiEndpoint>): String {
        val clientCode = """
            class ApiClient(private val baseUrl: String = "$baseUrl") {

                private val httpClient = HttpClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
        """.trimIndent()

        val endpointMethods = endpoints.joinToString("\n\n") { endpoint ->
            generateEndpointMethod(endpoint)
        }

        return """
            $clientCode

            $endpointMethods

                companion object {
                    val instance by lazy { ApiClient() }
                }
            }
        """.trimIndent()
    }

    private fun generateEndpointMethod(endpoint: ApiEndpoint): String {
        return """
                suspend fun ${endpoint.name}(${endpoint.parameters}): ${endpoint.returnType} {
                    return httpClient.${endpoint.method.lowercase()}("$baseUrl${endpoint.path}") {
                        ${endpoint.body ?: ""}
                    }
                }
        """.trimIndent()
    }
}
```

### 3. 测试代码生成器

#### 单元测试生成
```kotlin
object TestGenerator {

    fun generateViewModelTest(className: String): String {
        return """
            @OptIn(ExperimentalCoroutinesApi::class)
            class ${className}Test {

                private lateinit var viewModel: ${className}

                @Before
                fun setup() {
                    viewModel = ${className}()
                }

                @Test
                fun `initial state is loading`() = runTest {
                    assertEquals(${className}State.Loading, viewModel.state.value)
                }

                @Test
                fun `load data success`() = runTest {
                    // Given
                    val expectedData = listOf(TestData())

                    // When
                    viewModel.handleIntent(${className}Intent.LoadData)

                    // Then
                    assertTrue(viewModel.state.value is ${className}State.Success)
                }
            }
        """.trimIndent()
    }
}
```

## 📊 性能监控集成

### 1. 重组监控
```kotlin
@Composable
fun RecompositionTracker(
    content: @Composable () -> Unit
) {
    var recompositionCount by remember { mutableIntStateOf(0) }

    SideEffect {
        recompositionCount++
    }

    Column {
        // 开发模式下显示重组计数
        if (BuildConfig.DEBUG) {
            Text(
                text = "Recompositions: $recompositionCount",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )
        }

        content()
    }
}
```

### 2. 内存监控
```kotlin
object MemoryMonitor {

    fun startMonitoring() {
        // 监控内存使用情况
        Runtime.getRuntime().addShutdownHook(Thread {
            logMemoryUsage()
        })
    }

    private fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024

        println("Memory Usage: $usedMemory MB / $maxMemory MB")
    }
}
```

### 3. 帧率监控
```kotlin
@Composable
fun FrameRateMonitor(
    modifier: Modifier = Modifier
) {
    var frameCount by remember { mutableIntStateOf(0) }
    var lastFrameTime by remember { mutableLongStateOf(System.nanoTime()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000) // 每秒更新一次
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0
            val fps = frameCount / deltaTime

            // 记录FPS数据
            frameCount = 0
            lastFrameTime = currentTime
        }
    }

    SideEffect {
        frameCount++
    }

    // 显示FPS（开发模式）
    if (BuildConfig.DEBUG) {
        Text(
            text = "FPS: ${frameCount}",
            modifier = modifier,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

## 🔍 调试工具

### 1. Compose Inspector
```kotlin
// 启用Compose检查器
@Composable
fun DebugComposition(
    content: @Composable () -> Unit
) {
    if (BuildConfig.DEBUG) {
        Inspectable(
            content = content,
            inspectorInfo = debugInspectorInfo {
                name = "DebugComposition"
                properties["timestamp"] = System.currentTimeMillis()
            }
        )
    } else {
        content()
    }
}
```

### 2. 状态调试器
```kotlin
class StateDebugger<T>(
    private val stateFlow: StateFlow<T>,
    private val tag: String = "StateDebug"
) {
    init {
        // 开发模式下监听状态变化
        if (BuildConfig.DEBUG) {
            GlobalScope.launch {
                stateFlow.collect { state ->
                    println("[$tag] State changed: $state")
                }
            }
        }
    }
}
```

### 3. 网络调试器
```kotlin
class NetworkDebugger(
    private val httpClient: HttpClient
) {
    init {
        if (BuildConfig.DEBUG) {
            // 添加网络日志拦截器
            httpClient.plugin(HttpSend) { request ->
                println("[Network] ${request.method} ${request.url}")
                val startTime = System.currentTimeMillis()

                val response = proceed(request)

                val endTime = System.currentTimeMillis()
                println("[Network] Response: ${response.status} (${endTime - startTime}ms)")

                response
            }
        }
    }
}
```

## 🚀 热重载配置

### 1. 开发服务器配置
```kotlin
// build.gradle.kts
tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserDevelopmentRun") {
    // 配置热重载
    devServer?.apply {
        port = 8080
        open = true
        hot = true
    }
}
```

### 2. 快速重启配置
```kotlin
// gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
kotlin.incremental=true
kotlin.compiler.execution.strategy=in-process
```

### 3. 预编译配置
```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/precompiled")
        }
    }
}

tasks.register("precompileCommon") {
    // 预编译通用代码
    dependsOn(":shared:compileKotlinMetadata")
}
```

## 📱 设备测试集成

### 1. 多设备测试配置
```kotlin
object DeviceTestConfig {

    val testDevices = listOf(
        TestDevice("Pixel 6", "Android 13", "1080x2400"),
        TestDevice("iPhone 14", "iOS 16", "1170x2532"),
        TestDevice("Web Chrome", "Chrome 120", "1920x1080")
    )

    fun runOnAllDevices(test: () -> Unit) {
        testDevices.forEach { device ->
            println("Testing on ${device.name}")
            // 配置设备特定测试环境
            test()
        }
    }
}
```

### 2. 截图测试
```kotlin
object ScreenshotTest {

    @Composable
    fun captureScreenshot(
        name: String,
        content: @Composable () -> Unit
    ) {
        val density = LocalDensity.current

        Box(modifier = Modifier.onGloballyPositioned { coordinates ->
            if (BuildConfig.DEBUG) {
                // 捕获截图用于视觉回归测试
                val bitmap = captureScreenshot(coordinates.size)
                saveScreenshot(name, bitmap)
            }
        }) {
            content()
        }
    }
}
```

## 🎯 最佳实践

### 1. 开发环境配置
```bash
# 配置开发环境
export UNIFY_DEBUG=true
export UNIFY_HOT_RELOAD=true
export UNIFY_PERFORMANCE_MONITOR=true
```

### 2. 调试技巧
- 使用 `RecompositionTracker` 监控重组
- 使用 `FrameRateMonitor` 检查性能
- 使用 `StateDebugger` 跟踪状态变化
- 使用 `NetworkDebugger` 监控网络请求

### 3. 性能优化
- 启用增量编译
- 使用预编译
- 配置构建缓存
- 优化依赖解析

通过这些开发者工具集成，您可以获得更好的开发体验、更高的开发效率和更稳定的代码质量。
