# 🔧 Unify KMP 集成指南

## 📖 概述

Unify KMP 是一个基于 Kotlin Multiplatform + Compose 的跨端架构方案，参考腾讯 KuiklyUI 开源设计，提供高性能、全平台开发框架。

## 🎯 集成方式

### 方式一：直接使用模板

```bash
# 克隆模板项目
git clone <repository-url> my-kmp-project
cd my-kmp-project

# 重命名包名
find . -name "*.kt" -exec sed -i 's/com.unify/com.yourcompany.yourapp/g' {} \;
find . -name "*.kts" -exec sed -i 's/com.unify/com.yourcompany.yourapp/g' {} \;
```

### 方式二：集成到现有项目

#### 1. 添加依赖管理
```kotlin
// gradle/libs.versions.toml
[versions]
kotlin = "2.0.21"
compose = "1.7.0"
ktor = "2.3.7"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.9.1" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

#### 2. 创建共享模块
```kotlin
// shared/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    id("com.android.library")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
    }
}
```

#### 3. 实现跨平台接口
```kotlin
// shared/src/commonMain/kotlin/Platform.kt
expect class Platform {
    companion object {
        fun getName(): String
        fun getInfo(): String
    }
}

// shared/src/androidMain/kotlin/Platform.android.kt
actual class Platform {
    actual companion object {
        actual fun getName(): String = "Android"
        actual fun getInfo(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}
```

## 🏗️ 架构集成

### MVI状态管理集成

```kotlin
// 定义状态
data class AppState(
    val loading: Boolean = false,
    val data: String = "",
    val error: String? = null
)

// 定义意图
sealed class AppIntent {
    object LoadData : AppIntent()
    data class UpdateData(val data: String) : AppIntent()
}

// ViewModel实现
class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    
    fun handleIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.LoadData -> loadData()
            is AppIntent.UpdateData -> updateData(intent.data)
        }
    }
}
```

### 依赖注入集成

```kotlin
// shared/src/commonMain/kotlin/di/AppModule.kt
val appModule = module {
    single<Repository> { RepositoryImpl() }
    factory { AppViewModel(get()) }
}

// 在应用中初始化
fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
```

## 📱 平台特定集成

### Android集成

```kotlin
// androidApp/src/main/kotlin/MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initKoin() // 初始化依赖注入
        
        setContent {
            MaterialTheme {
                App() // 共享UI组件
            }
        }
    }
}
```

### iOS集成

```swift
// iosApp/iosApp/ContentView.swift
struct ContentView: View {
    var body: some View {
        ComposeView()
            .onAppear {
                KoinKt.doInitKoin()
            }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### Web集成

```kotlin
// webApp/src/jsMain/kotlin/Main.kt
fun main() {
    initKoin()
    
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
```

## 🔌 第三方库集成

### 网络请求 (Ktor)

```kotlin
// shared/src/commonMain/kotlin/network/ApiService.kt
class ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    
    suspend fun getData(): ApiResponse {
        return client.get("https://api.example.com/data").body()
    }
}
```

### 数据库 (SQLDelight)

```sql
-- shared/src/commonMain/sqldelight/Database.sq
CREATE TABLE User (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL
);

selectAll:
SELECT * FROM User;

insert:
INSERT INTO User(name, email) VALUES (?, ?);
```

### 图片加载 (Coil/Kamel)

```kotlin
// shared/src/commonMain/kotlin/ui/ImageComponent.kt
@Composable
fun NetworkImage(url: String) {
    KamelImage(
        resource = asyncPainterResource(url),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

## 🧪 测试集成

### 单元测试

```kotlin
// shared/src/commonTest/kotlin/ViewModelTest.kt
class ViewModelTest {
    @Test
    fun testLoadData() {
        val viewModel = AppViewModel()
        viewModel.handleIntent(AppIntent.LoadData)
        
        assertTrue(viewModel.state.value.loading)
    }
}
```

### UI测试

```kotlin
// shared/src/commonTest/kotlin/UITest.kt
@Composable
fun TestApp() {
    ComposeTestRule().setContent {
        App()
    }
}
```

## 📦 构建集成

### CI/CD集成

```yaml
# .github/workflows/build.yml
name: Build All Platforms
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build Android
        run: ./gradlew :androidApp:assembleDebug
      
      - name: Build Web
        run: ./gradlew :webApp:jsBrowserProductionWebpack
      
      - name: Build Desktop
        run: ./gradlew :desktopApp:packageUberJarForCurrentOS
```

### 发布脚本

```bash
#!/bin/bash
# scripts/release.sh

echo "🚀 开始发布流程..."

# 构建所有平台
./gradlew clean
./gradlew :androidApp:assembleRelease
./gradlew :webApp:jsBrowserProductionWebpack
./gradlew :desktopApp:packageUberJarForCurrentOS

echo "✅ 所有平台构建完成！"
```

## 🔧 最佳实践

### 1. 代码组织
```
shared/
├── src/
│   ├── commonMain/kotlin/
│   │   ├── ui/           # UI组件
│   │   ├── data/         # 数据层
│   │   ├── domain/       # 业务逻辑
│   │   └── di/           # 依赖注入
│   ├── androidMain/kotlin/
│   ├── iosMain/kotlin/
│   └── jsMain/kotlin/
```

### 2. 性能优化
- 使用 `@Stable` 和 `@Immutable` 注解
- 合理使用 `remember` 和 `LaunchedEffect`
- 避免在 Composable 中进行重计算

### 3. 错误处理
```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Throwable) : Result<T>()
    class Loading<T> : Result<T>()
}
```

## 📚 迁移指南

### 从原生Android迁移
1. 将UI代码转换为Compose
2. 提取业务逻辑到shared模块
3. 使用expect/actual处理平台差异

### 从React Native迁移
1. 重写UI组件为Compose
2. 迁移状态管理逻辑
3. 适配平台特定功能

### 从Flutter迁移
1. 将Widget转换为Composable
2. 迁移状态管理到MVI模式
3. 重构平台通道为expect/actual

## 🆘 故障排除

### 常见问题

**Q: 编译错误 "expect declaration not found"**
```kotlin
// 确保每个expect声明都有对应的actual实现
// commonMain
expect fun platformSpecificFunction(): String

// androidMain  
actual fun platformSpecificFunction(): String = "Android"

// iosMain
actual fun platformSpecificFunction(): String = "iOS"
```

**Q: iOS构建失败**
```bash
# 清理并重新安装依赖
cd iosApp
rm -rf Pods Podfile.lock
pod install --repo-update
```

**Q: Web应用白屏**
```kotlin
// 检查HTML模板是否包含canvas元素
// webApp/src/jsMain/resources/index.html
<canvas id="ComposeTarget"></canvas>
```

---

**💡 提示**: 遇到问题时，请先查看 [GitHub Issues](https://github.com/your-org/unify-core/issues) 或创建新的问题报告。
