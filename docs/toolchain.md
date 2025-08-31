# Unify KMP 工具链

## 🎯 工具链概述

Unify KMP 提供了一套完整的开发工具链，帮助开发者提高开发效率、确保代码质量，并简化部署流程。

## 🛠️ 核心工具

### 1. 项目模板生成器

#### 快速创建新项目
```bash
# 使用模板创建新项目
npx create-unify-app my-app

# 选择平台和功能
? 选择目标平台 (多选):
❯◉ Android
 ◯ iOS
 ◯ Web
 ◯ Desktop
 ◯ HarmonyOS
 ◯ 小程序

? 选择初始功能:
❯◉ 基础UI组件
 ◯ AI智能组件
 ◯ 地图组件
 ◯ 安全组件
 ◯ 性能监控
```

#### 模板结构
```
my-app/
├── shared/
│   ├── src/commonMain/kotlin/
│   ├── src/androidMain/kotlin/
│   ├── src/iosMain/kotlin/
│   ├── src/jsMain/kotlin/
│   └── src/jvmMain/kotlin/
├── androidApp/
├── iosApp/
├── webApp/
├── desktopApp/
├── docs/
│   ├── API_REFERENCE.md
│   ├── DEVELOPMENT_GUIDE.md
│   └── DEPLOYMENT_GUIDE.md
├── scripts/
│   ├── build-all.sh
│   ├── test-all.sh
│   └── deploy.sh
└── README.md
```

### 2. 代码生成器

#### 组件代码生成
```bash
# 生成新的UI组件
unify generate component MyCustomButton

# 生成完整的组件代码结构
unify generate component MyCustomButton --variant=filled,outlined --size=small,medium,large
```

生成的代码结构：
```kotlin
// MyCustomButton.kt
@Composable
fun MyCustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MyCustomButtonVariant = MyCustomButtonVariant.FILLED,
    size: MyCustomButtonSize = MyCustomButtonSize.MEDIUM,
    enabled: Boolean = true
) {
    // 自动生成的组件实现
}

// MyCustomButtonVariant.kt
enum class MyCustomButtonVariant {
    FILLED,
    OUTLINED,
    TEXT
}

// MyCustomButtonSize.kt
enum class MyCustomButtonSize {
    SMALL,
    MEDIUM,
    LARGE
}
```

#### API接口生成
```bash
# 从OpenAPI规范生成API接口
unify generate api --openapi=api-spec.json --package=com.example.api

# 生成的数据类和接口
// UserApi.kt
interface UserApi {
    suspend fun getUser(id: String): User
    suspend fun createUser(user: CreateUserRequest): User
    suspend fun updateUser(id: String, user: UpdateUserRequest): User
}

// User.kt
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)
```

### 3. 平台适配器生成器

#### 自动生成平台适配代码
```bash
# 生成新的平台功能适配器
unify generate adapter CameraAdapter

# 生成expect/actual代码结构
// commonMain
expect object CameraAdapter {
    fun takePhoto(): String?
    fun recordVideo(): String?
}

// androidMain
actual object CameraAdapter {
    actual fun takePhoto(): String? {
        // Android相机实现
    }

    actual fun recordVideo(): String? {
        // Android录像实现
    }
}

// iosMain
actual object CameraAdapter {
    actual fun takePhoto(): String? {
        // iOS相机实现
    }

    actual fun recordVideo(): String? {
        // iOS录像实现
    }
}
```

## 🔧 质量保证工具

### 1. 代码质量检查

#### 自动代码检查配置
```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

// 配置规则
detekt {
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    autoCorrect = true
}

// 运行检查
./gradlew detekt
./gradlew ktlintCheck
```

#### 自定义检查规则
```yaml
# config/detekt/detekt.yml
style:
  ForbiddenComment:
    active: true
    values: ['TODO:', 'FIXME:', 'STOPSHIP:']
  Naming:
    active: true
    FunctionNaming:
      ignoreAnnotated: ['Composable']

complexity:
  TooManyFunctions:
    active: true
    thresholdInFiles: 15
```

### 2. 性能分析工具

#### 内存泄漏检测
```kotlin
// 启用内存泄漏检测
object MemoryLeakDetector {
    fun detectLeaks() {
        // 检测Compose组件泄漏
        // 检测协程泄漏
        // 检测资源泄漏
    }
}

// 使用
LaunchedEffect(Unit) {
    MemoryLeakDetector.detectLeaks()
}
```

#### 重组分析工具
```kotlin
// 重组计数器
@Composable
fun RecompositionCounter(content: @Composable () -> Unit) {
    var count by remember { mutableIntStateOf(0) }
    count++

    Column {
        Text("重组次数: $count")
        content()
    }
}

// 使用
RecompositionCounter {
    MyComposableComponent()
}
```

## 📊 构建优化工具

### 1. 增量构建配置
```kotlin
// gradle.properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.daemon=true
kotlin.incremental=true
kotlin.compiler.execution.strategy=in-process
```

### 2. 构建缓存优化
```kotlin
// build.gradle.kts
tasks.withType<KotlinCompile> {
    compilerOptions.configure {
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}
```

### 3. 多平台构建脚本
```bash
#!/bin/bash
# scripts/build-all.sh

echo "🏗️  构建所有平台..."

# 并行构建
./gradlew :shared:compileKotlinMetadata &
./gradlew :androidApp:assembleDebug &
./gradlew :iosApp:compileKotlinIosX64 &
./gradlew :webApp:jsBrowserProductionWebpack &
./gradlew :desktopApp:compileKotlinJvm &

wait

echo "✅ 所有平台构建完成"
```

## 🔍 测试工具

### 1. 单元测试模板
```kotlin
// ViewModel测试
class MainViewModelTest {
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        viewModel = MainViewModel()
    }

    @Test
    fun `test initial state`() = runTest {
        assertEquals(MainState.Loading, viewModel.state.value)
    }

    @Test
    fun `test data loading success`() = runTest {
        viewModel.handleIntent(MainIntent.LoadData)
        advanceUntilIdle()
        assertTrue(viewModel.state.value is MainState.Success)
    }
}
```

### 2. UI测试模板
```kotlin
// Compose UI测试
@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test button click`() {
        var clicked = false

        composeTestRule.setContent {
            UnifyButton(
                text = "Click me",
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Click me").performClick()
        assertTrue(clicked)
    }
}
```

## 🚀 部署工具

### 1. 一键部署脚本
```bash
#!/bin/bash
# scripts/deploy.sh

echo "🚀 开始部署..."

# 构建所有平台
./gradlew buildAll

# 部署到各平台
echo "📱 部署Android..."
# Android部署逻辑

echo "🍎 部署iOS..."
# iOS部署逻辑

echo "🌐 部署Web..."
# Web部署逻辑

echo "✅ 部署完成"
```

### 2. 版本管理
```kotlin
// build.gradle.kts
version = project.properties["version"] ?: "1.0.0-SNAPSHOT"

tasks.register("release") {
    dependsOn("build")
    doLast {
        // 打标签
        exec {
            commandLine("git", "tag", "v$version")
        }
        // 发布到Maven Central
        // 更新文档
    }
}
```

## 📚 使用指南

### 安装工具链
```bash
# 安装Unify CLI
npm install -g @unify/cli

# 验证安装
unify --version
```

### 创建新项目
```bash
# 使用交互式创建
unify create my-awesome-app

# 使用模板创建
unify create my-app --template=full-stack
```

### 开发工作流
```bash
# 启动开发服务器
unify dev

# 运行测试
unify test

# 构建生产版本
unify build

# 部署应用
unify deploy
```

## 🎯 最佳实践

### 1. 项目结构规范
- 遵循 `shared/src/commonMain` 结构
- 使用 `expect/actual` 进行平台适配
- 保持业务逻辑与UI分离

### 2. 代码质量保证
- 启用所有静态检查工具
- 编写完整的单元测试
- 使用性能分析工具优化代码

### 3. 构建优化
- 使用增量构建
- 配置构建缓存
- 并行构建多个平台

通过这套完整的工具链，开发者可以专注于业务逻辑的实现，而将重复的开发任务交给自动化工具，大大提升开发效率和代码质量。
