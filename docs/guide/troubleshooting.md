# 故障排除指南

## 🔧 环境配置问题

### JDK 版本问题
**问题**: 编译失败，提示 JDK 版本不兼容
```
> Task :shared:compileKotlinAndroid FAILED
Unsupported class file major version 61
```

**解决方案**:
```bash
# 检查当前 JDK 版本
java -version
javac -version

# 安装 JDK 17
# macOS (使用 Homebrew)
brew install openjdk@17

# Ubuntu
sudo apt install openjdk-17-jdk

# Windows (下载并安装 Oracle JDK 17)

# 设置 JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 验证安装
./gradlew --version
```

### Android SDK 配置问题
**问题**: Android 构建失败，找不到 SDK
```
> SDK location not found. Define location with an ANDROID_SDK_ROOT environment variable
```

**解决方案**:
```bash
# 1. 创建 local.properties 文件
echo "sdk.dir=/path/to/android-sdk" > local.properties

# 2. 或设置环境变量
export ANDROID_SDK_ROOT=/path/to/android-sdk
export ANDROID_HOME=/path/to/android-sdk

# 3. 验证 SDK 安装
$ANDROID_SDK_ROOT/tools/bin/sdkmanager --list
```

### Gradle 构建缓存问题
**问题**: 构建缓存损坏导致编译错误
```
> Could not resolve all files for configuration ':shared:androidDebugCompileClasspath'
```

**解决方案**:
```bash
# 清理 Gradle 缓存
./gradlew clean
./gradlew --stop
rm -rf ~/.gradle/caches
rm -rf .gradle

# 重新构建
./gradlew build
```

## 📱 平台特定问题

### Android 平台问题

#### 1. 多 DEX 问题
**问题**: 方法数超过 65K 限制
```
> The number of method references in a .dex file cannot exceed 64K
```

**解决方案**:
```kotlin
// android/build.gradle.kts
android {
    defaultConfig {
        multiDexEnabled = true
    }
}

dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
}
```

#### 2. 权限问题
**问题**: 运行时权限被拒绝
```kotlin
// 检查和请求权限
private fun checkPermissions() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
        != PackageManager.PERMISSION_GRANTED) {
        
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
        CAMERA_PERMISSION_CODE -> {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
            } else {
                // 权限被拒绝
            }
        }
    }
}
```

### iOS 平台问题

#### 1. 签名问题
**问题**: 代码签名失败
```
Code Sign error: No code signing identities found
```

**解决方案**:
```bash
# 1. 检查证书
security find-identity -v -p codesigning

# 2. 在 Xcode 中配置签名
# Project Settings -> Signing & Capabilities -> Team

# 3. 清理并重新构建
xcodebuild clean -project iosApp.xcodeproj
xcodebuild build -project iosApp.xcodeproj
```

#### 2. Framework 找不到
**问题**: 链接时找不到 shared framework
```
ld: framework not found shared
```

**解决方案**:
```bash
# 1. 重新构建 shared framework
./gradlew :shared:assembleXCFramework

# 2. 检查 Framework Search Paths
# Xcode -> Build Settings -> Framework Search Paths

# 3. 确保 framework 路径正确
# $(PROJECT_DIR)/../shared/build/XCFrameworks/release
```

### Web 平台问题

#### 1. CORS 问题
**问题**: 跨域请求被阻止
```
Access to fetch at 'https://api.example.com' from origin 'http://localhost:8080' 
has been blocked by CORS policy
```

**解决方案**:
```javascript
// 开发环境代理配置
// webpack.config.js
module.exports = {
    devServer: {
        proxy: {
            '/api': {
                target: 'https://api.example.com',
                changeOrigin: true,
                pathRewrite: {
                    '^/api': ''
                }
            }
        }
    }
};
```

#### 2. 内存泄漏
**问题**: 长时间运行后内存占用过高
```kotlin
// 正确的资源清理
@Composable
fun WebComponent() {
    DisposableEffect(Unit) {
        val listener = { event: Event ->
            // 处理事件
        }
        
        window.addEventListener("resize", listener)
        
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }
}
```

## 🔄 编译和构建问题

### Kotlin 编译器错误

#### 1. 内部编译器错误
**问题**: Kotlin 编译器内部错误
```
Internal error in Kotlin compiler: java.lang.IllegalStateException
```

**解决方案**:
```bash
# 1. 清理项目
./gradlew clean

# 2. 删除 Kotlin 缓存
rm -rf ~/.kotlin

# 3. 更新 Kotlin 版本
# gradle/libs.versions.toml
[versions]
kotlin = "2.0.21"

# 4. 如果问题持续，创建独立模块
# 参考桌面端解决方案，避免复杂依赖
```

#### 2. 依赖冲突
**问题**: 版本冲突导致编译失败
```
> Duplicate class found in modules
```

**解决方案**:
```kotlin
// build.gradle.kts
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
        
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("2.0.21")
            }
        }
    }
}
```

### 内存和性能问题

#### 1. Gradle 内存不足
**问题**: 构建过程中内存溢出
```
> Expiring Daemon because JVM heap space is exhausted
```

**解决方案**:
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# 或设置环境变量
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=512m"
```

#### 2. 应用启动慢
**问题**: 应用启动时间过长
```kotlin
// 启动优化
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 异步初始化非关键组件
        lifecycleScope.launch(Dispatchers.IO) {
            initializeSecondaryComponents()
        }
        
        // 同步初始化关键组件
        initializeCriticalComponents()
    }
    
    private suspend fun initializeSecondaryComponents() {
        // 网络、数据库等初始化
    }
    
    private fun initializeCriticalComponents() {
        // 崩溃报告、日志等初始化
    }
}
```

## 🌐 网络和数据问题

### 网络请求失败

#### 1. SSL 证书问题
**问题**: HTTPS 请求失败
```
javax.net.ssl.SSLHandshakeException: Trust anchor for certification path not found
```

**解决方案**:
```kotlin
// 开发环境临时解决方案（生产环境不推荐）
val client = HttpClient(CIO) {
    engine {
        https {
            trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        }
    }
}

// 生产环境正确做法
val client = HttpClient(CIO) {
    engine {
        https {
            // 使用系统默认的信任管理器
        }
    }
}
```

#### 2. 超时问题
**问题**: 网络请求超时
```kotlin
// 配置合适的超时时间
val client = HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 10000
        socketTimeoutMillis = 30000
    }
    
    // 重试机制
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
        exponentialDelay()
    }
}
```

### 数据库问题

#### 1. 数据库锁定
**问题**: SQLite 数据库被锁定
```
database is locked (code 5 SQLITE_BUSY)
```

**解决方案**:
```kotlin
// 使用事务和连接池
class DatabaseManager {
    private val database: SqlDriver by lazy {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "app.db"
        ).also { driver ->
            // 设置 WAL 模式提高并发性能
            driver.execute(null, "PRAGMA journal_mode=WAL", 0)
            driver.execute(null, "PRAGMA synchronous=NORMAL", 0)
        }
    }
    
    suspend fun <T> withTransaction(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            database.transactionWithResult {
                runBlocking { block() }
            }
        }
    }
}
```

#### 2. 数据迁移失败
**问题**: 数据库版本升级失败
```kotlin
// 正确的迁移策略
val driver = AndroidSqliteDriver(
    schema = Database.Schema,
    context = context,
    name = "app.db",
    callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
        override fun onUpgrade(driver: SqlDriver, oldVersion: Int, newVersion: Int) {
            when {
                oldVersion < 2 -> {
                    // 版本 1 到 2 的迁移
                    driver.execute(null, "ALTER TABLE User ADD COLUMN avatar_url TEXT", 0)
                }
                oldVersion < 3 -> {
                    // 版本 2 到 3 的迁移
                    driver.execute(null, "CREATE INDEX user_email_idx ON User(email)", 0)
                }
            }
        }
    }
)
```

## 🎨 UI 和用户体验问题

### Compose UI 问题

#### 1. 重组性能问题
**问题**: 频繁重组导致性能下降
```kotlin
// 问题代码
@Composable
fun SlowComponent(items: List<Item>) {
    // 每次重组都会创建新的列表
    val processedItems = items.map { processItem(it) }
    
    LazyColumn {
        items(processedItems) { item ->
            ItemView(item)
        }
    }
}

// 优化后的代码
@Composable
fun OptimizedComponent(items: List<Item>) {
    // 使用 remember 缓存计算结果
    val processedItems = remember(items) {
        items.map { processItem(it) }
    }
    
    LazyColumn {
        items(
            items = processedItems,
            key = { it.id } // 提供稳定的 key
        ) { item ->
            ItemView(item)
        }
    }
}
```

#### 2. 状态丢失问题
**问题**: 配置变更时状态丢失
```kotlin
// 使用 rememberSaveable 保存状态
@Composable
fun StatefulComponent() {
    var text by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableIntStateOf(0) }
    
    Column {
        TextField(
            value = text,
            onValueChange = { text = it }
        )
        
        Button(onClick = { count++ }) {
            Text("Count: $count")
        }
    }
}
```

### 内存泄漏问题

#### 1. 协程泄漏
**问题**: 协程没有正确取消
```kotlin
// 问题代码
class ViewModel {
    fun loadData() {
        GlobalScope.launch {
            // 可能导致内存泄漏
        }
    }
}

// 正确做法
class ViewModel : androidx.lifecycle.ViewModel() {
    fun loadData() {
        viewModelScope.launch {
            // 自动在 ViewModel 清理时取消
        }
    }
}
```

#### 2. 监听器泄漏
**问题**: 事件监听器没有正确移除
```kotlin
@Composable
fun ComponentWithListener() {
    DisposableEffect(Unit) {
        val listener = { event: Event ->
            // 处理事件
        }
        
        EventBus.register(listener)
        
        onDispose {
            EventBus.unregister(listener)
        }
    }
}
```

## 🔍 调试技巧

### 日志调试
```kotlin
// 统一的日志工具
object Logger {
    private const val TAG = "UnifyKMP"
    
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            println("DEBUG [$tag]: $message")
        }
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        println("ERROR [$tag]: $message")
        throwable?.printStackTrace()
    }
    
    fun i(message: String, tag: String = TAG) {
        println("INFO [$tag]: $message")
    }
}
```

### 性能分析
```kotlin
// 性能监控工具
inline fun <T> measureTimeAndLog(operation: String, block: () -> T): T {
    val startTime = System.currentTimeMillis()
    val result = block()
    val duration = System.currentTimeMillis() - startTime
    
    when {
        duration > 1000 -> Logger.e("Slow operation: $operation took ${duration}ms")
        duration > 500 -> Logger.d("Operation: $operation took ${duration}ms")
    }
    
    return result
}
```

## 📞 获取帮助

### 社区支持
- **GitHub Issues**: 报告 Bug 和功能请求
- **GitHub Discussions**: 技术讨论和经验分享
- **Stack Overflow**: 使用 `unify-kmp` 标签提问
- **官方文档**: 查看最新的文档和示例

### 问题报告模板
```markdown
## 问题描述
简要描述遇到的问题

## 复现步骤
1. 
2. 
3. 

## 预期行为
描述期望的正确行为

## 实际行为
描述实际发生的行为

## 环境信息
- OS: [e.g. macOS 12.0]
- JDK: [e.g. OpenJDK 17]
- Kotlin: [e.g. 2.0.21]
- Gradle: [e.g. 8.14.2]
- 平台: [e.g. Android, iOS, Web]

## 错误日志
```
粘贴相关的错误日志
```

## 其他信息
任何其他相关信息
```

---

遇到问题时，请先查阅本指南。如果问题仍未解决，欢迎通过社区渠道寻求帮助。我们致力于为每个开发者提供最佳的技术支持。
