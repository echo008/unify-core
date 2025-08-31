# 桌面端平台开发指南

## 🖥️ 概述

Unify KMP 通过 Compose Desktop 为桌面平台提供原生应用开发支持，基于 Kotlin/JVM 编译器生成跨平台桌面应用，支持 Windows、macOS 和 Linux 系统。

### 🎯 平台特性
- **跨平台桌面**: Windows/macOS/Linux原生支持
- **系统集成**: 系统托盘、窗口管理、文件操作原生功能
- **原生体验**: 真正的桌面应用性能和用户体验
- **灵活部署**: JAR包、安装程序等多种打包方式

### 技术栈
- **Kotlin**: 2.0.21
- **Compose Desktop**: 1.6.0
- **Kotlin/JVM**: 2.0.21
- **JDK**: 17+
- **Swing/AWT**: 系统集成

### 必需工具
- **JDK**: 17 或更高版本 (推荐 JDK 21)
- **Kotlin**: 2.0.21+
- **Gradle**: 8.0+
- **操作系统**: Windows 10+, macOS 11+, Ubuntu 20.04+

### 可选工具
- **IntelliJ IDEA**: 2023.3+ (推荐)
- **Git**: 版本控制
- **Docker**: 容器化部署

### 安装验证
```bash
# 检查 JDK 版本
java -version
javac -version

# 检查 Gradle 版本
./gradlew --version

# 检查系统信息
uname -a  # Linux/macOS
systeminfo  # Windows
```

## 🏗️ 项目结构

### 桌面应用模块
```
desktopApp/
├── src/
│   └── jvmMain/
│       └── kotlin/
│           └── com/unify/desktop/
│               ├── Main.kt              # 应用入口
│               ├── DesktopApp.kt        # 主应用组件
│               └── theme/
│                   └── DesktopTheme.kt  # 桌面主题
├── build.gradle.kts                     # 构建配置
└── src/main/resources/                  # 资源文件
    ├── icon.png                         # 应用图标
    └── fonts/                           # 字体文件
```

### Gradle 配置
```kotlin
// desktopApp/build.gradle.kts
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                
                // 可选：如果需要共享代码
                // implementation(project(":shared"))
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.unify.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Unify KMP Desktop"
            packageVersion = "1.0.0"
            description = "Unify KMP Desktop Application"
            copyright = "© 2024 Unify KMP Team"
            vendor = "Unify KMP"
            
            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                menuGroup = "Unify KMP"
                upgradeUuid = "61DAB35E-17CB-43B8-8A71-A7C8D5D4F2E5"
            }
            
            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
                bundleID = "com.unify.desktop"
                appCategory = "public.app-category.developer-tools"
            }
            
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
                debMaintainer = "support@unify-kmp.org"
                menuGroup = "Development"
            }
        }
    }
}
```

## 💻 核心组件实现

### 1. 应用入口 (Main.kt)
```kotlin
// desktopApp/src/jvmMain/kotlin/com/unify/desktop/Main.kt
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.unify.desktop.theme.DesktopTheme

fun main() = application {
    val windowState = rememberWindowState(
        width = 800.dp,
        height = 600.dp
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Unify KMP Desktop",
        state = windowState
    ) {
        DesktopTheme {
            DesktopApp()
        }
    }
}
```

### 2. 主应用组件 (DesktopApp.kt)
```kotlin
// desktopApp/src/jvmMain/kotlin/com/unify/desktop/DesktopApp.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopApp() {
    var count by remember { mutableIntStateOf(0) }
    var isDarkTheme by remember { mutableStateOf(false) }
    
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Unify KMP Desktop",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    actions = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dark Mode", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hello, Desktop!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Platform: ${getPlatformName()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "Runtime: ${getJavaVersion()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "Count: $count",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { count++ },
                                modifier = Modifier.size(width = 140.dp, height = 48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Increment")
                            }
                            
                            OutlinedButton(
                                onClick = { count = 0 },
                                modifier = Modifier.size(width = 120.dp, height = 48.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                SystemInfoCard()
            }
        }
    }
}

@Composable
fun SystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow("OS", System.getProperty("os.name"))
            InfoRow("Architecture", System.getProperty("os.arch"))
            InfoRow("Java Version", System.getProperty("java.version"))
            InfoRow("Available Processors", Runtime.getRuntime().availableProcessors().toString())
            InfoRow("Max Memory", "${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

fun getPlatformName(): String = "Desktop (${System.getProperty("os.name")})"

fun getJavaVersion(): String = "Java ${System.getProperty("java.version")}"
```

### 3. 桌面主题 (DesktopTheme.kt)
```kotlin
// desktopApp/src/jvmMain/kotlin/com/unify/desktop/theme/DesktopTheme.kt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFF018786),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFF018786),
    background = Color(0xFF10131C),
    surface = Color(0xFF10131C),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun DesktopTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
```

## 🚀 构建和运行

### 1. 开发模式运行
```bash
# 直接运行应用
./gradlew :desktopApp:run

# 或者使用 Compose 任务
./gradlew :desktopApp:runDistributable
```

### 2. 构建可执行 JAR
```bash
# 构建 Uber JAR
./gradlew :desktopApp:packageUberJarForCurrentOS

# JAR 文件位置
ls desktopApp/build/compose/jars/
```

### 3. 构建原生安装包
```bash
# 构建所有支持的格式
./gradlew :desktopApp:packageDistributionForCurrentOS

# 构建特定格式
./gradlew :desktopApp:packageDmg     # macOS
./gradlew :desktopApp:packageMsi     # Windows
./gradlew :desktopApp:packageDeb     # Linux

# 安装包位置
ls desktopApp/build/compose/binaries/main/
```

## 🎨 UI 开发指南

### 窗口管理
```kotlin
@Composable
fun MultiWindowApp() {
    var isSecondWindowOpen by remember { mutableStateOf(false) }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Main Window"
    ) {
        Button(
            onClick = { isSecondWindowOpen = true }
        ) {
            Text("Open Second Window")
        }
    }
    
    if (isSecondWindowOpen) {
        Window(
            onCloseRequest = { isSecondWindowOpen = false },
            title = "Second Window"
        ) {
            Text("This is the second window")
        }
    }
}
```

### 菜单栏
```kotlin
@Composable
fun AppWithMenuBar() {
    MenuBar {
        Menu("File") {
            Item("New", onClick = { /* 新建文件 */ })
            Item("Open", onClick = { /* 打开文件 */ })
            Separator()
            Item("Exit", onClick = { exitApplication() })
        }
        
        Menu("Edit") {
            Item("Copy", onClick = { /* 复制 */ })
            Item("Paste", onClick = { /* 粘贴 */ })
        }
        
        Menu("Help") {
            Item("About", onClick = { /* 关于 */ })
        }
    }
}
```

### 系统托盘
```kotlin
@Composable
fun AppWithTray() {
    Tray(
        icon = painterResource("icon.png"),
        tooltip = "Unify KMP Desktop"
    ) {
        Item("Show Window", onClick = { /* 显示窗口 */ })
        Item("Settings", onClick = { /* 设置 */ })
        Separator()
        Item("Exit", onClick = { exitApplication() })
    }
}
```

### 文件对话框
```kotlin
@Composable
fun FileDialogExample() {
    var isFileDialogOpen by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    
    Button(
        onClick = { isFileDialogOpen = true }
    ) {
        Text("Select File")
    }
    
    if (isFileDialogOpen) {
        FileDialog(
            title = "Select a file",
            isLoad = true,
            onResult = { file ->
                selectedFile = file
                isFileDialogOpen = false
            }
        )
    }
    
    selectedFile?.let { file ->
        Text("Selected: ${file.name}")
    }
}
```

## 🔧 平台特定功能

### 系统集成
```kotlin
// 获取系统信息
fun getSystemInfo(): SystemInfo {
    return SystemInfo(
        osName = System.getProperty("os.name"),
        osVersion = System.getProperty("os.version"),
        javaVersion = System.getProperty("java.version"),
        userHome = System.getProperty("user.home"),
        availableProcessors = Runtime.getRuntime().availableProcessors()
    )
}

data class SystemInfo(
    val osName: String,
    val osVersion: String,
    val javaVersion: String,
    val userHome: String,
    val availableProcessors: Int
)
```

### 本地文件操作
```kotlin
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class FileManager {
    fun readTextFile(path: String): String? {
        return try {
            Files.readString(Paths.get(path))
        } catch (e: Exception) {
            null
        }
    }
    
    fun writeTextFile(path: String, content: String): Boolean {
        return try {
            Files.writeString(Paths.get(path), content)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun listFiles(directory: String): List<String> {
        return File(directory).listFiles()?.map { it.name } ?: emptyList()
    }
}
```

## 🔍 调试和测试

### 日志记录
```kotlin
import java.util.logging.Logger
import java.util.logging.Level

class DesktopLogger {
    private val logger = Logger.getLogger("DesktopApp")
    
    fun info(message: String) {
        logger.info(message)
    }
    
    fun error(message: String, throwable: Throwable? = null) {
        logger.log(Level.SEVERE, message, throwable)
    }
    
    fun debug(message: String) {
        logger.fine(message)
    }
}
```

### 单元测试
```kotlin
// desktopApp/src/jvmTest/kotlin/DesktopAppTest.kt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopAppTest {
    @Test
    fun testPlatformName() {
        val platformName = getPlatformName()
        assertTrue(platformName.startsWith("Desktop"))
    }
    
    @Test
    fun testJavaVersion() {
        val javaVersion = getJavaVersion()
        assertTrue(javaVersion.contains("Java"))
    }
    
    @Test
    fun testSystemInfo() {
        val systemInfo = getSystemInfo()
        assertTrue(systemInfo.availableProcessors > 0)
        assertTrue(systemInfo.osName.isNotEmpty())
    }
}
```

### UI 测试
```kotlin
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class DesktopAppUITest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testCounterIncrement() {
        composeTestRule.setContent {
            DesktopApp()
        }
        
        composeTestRule.onNodeWithText("Count: 0").assertExists()
        composeTestRule.onNodeWithText("Increment").performClick()
        composeTestRule.onNodeWithText("Count: 1").assertExists()
    }
}
```

## 📦 打包和分发

### 自定义安装程序
```kotlin
// build.gradle.kts
compose.desktop {
    application {
        nativeDistributions {
            modules("java.compiler", "java.instrument", "java.management", "jdk.unsupported")
            
            windows {
                console = false
                dirChooser = true
                perUserInstall = true
                shortcut = true
                menuGroup = "Unify KMP"
            }
            
            macOS {
                dockName = "Unify KMP"
                setDockNameSameAsPackageName = false
                signing {
                    sign.set(true)
                    identity.set("Developer ID Application: Your Name")
                }
                notarization {
                    appleID.set("your.apple.id@email.com")
                    password.set("app-specific-password")
                }
            }
            
            linux {
                packageName = "unify-kmp-desktop"
                debMaintainer = "support@unify-kmp.org"
                rpmLicenseType = "MIT"
            }
        }
    }
}
```

### 应用签名和公证
```bash
# macOS 签名
codesign --force --deep --sign "Developer ID Application: Your Name" \
    desktopApp/build/compose/binaries/main/dmg/Unify\ KMP\ Desktop-1.0.0.dmg

# Windows 签名
signtool sign /f certificate.p12 /p password \
    desktopApp/build/compose/binaries/main/msi/Unify\ KMP\ Desktop-1.0.0.msi
```

## 🚀 性能优化

### JVM 调优
```bash
# 启动参数优化
java -Xmx2G -Xms512M -XX:+UseG1GC \
     -Dfile.encoding=UTF-8 \
     -jar unify-kmp-desktop.jar
```

### 内存管理
```kotlin
// 内存监控
fun getMemoryUsage(): MemoryUsage {
    val runtime = Runtime.getRuntime()
    return MemoryUsage(
        total = runtime.totalMemory(),
        free = runtime.freeMemory(),
        used = runtime.totalMemory() - runtime.freeMemory(),
        max = runtime.maxMemory()
    )
}

data class MemoryUsage(
    val total: Long,
    val free: Long,
    val used: Long,
    val max: Long
)
```

## ❓ 常见问题

### Q: 应用启动慢
**A**: 检查 JVM 启动参数，使用 `-XX:+UseG1GC` 和适当的堆内存设置。

### Q: 字体渲染问题
**A**: 确保系统字体可用，或在应用中包含自定义字体文件。

### Q: 高 DPI 显示问题
**A**: 设置 JVM 参数 `-Dsun.java2d.uiScale=2.0` 或使用系统自动缩放。

### Q: 打包后文件过大
**A**: 使用 `modules` 配置只包含必需的 JVM 模块，移除未使用的依赖。

## 📚 参考资源

- [Compose Desktop 官方文档](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Desktop_Components)
- [Kotlin/JVM 官方指南](https://kotlinlang.org/docs/jvm-get-started.html)
- [Java 桌面应用开发指南](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Gradle 应用插件文档](https://docs.gradle.org/current/userguide/application_plugin.html)

---

通过本指南，您可以成功构建、测试和分发 Unify KMP 桌面应用，为用户提供原生的桌面体验。
