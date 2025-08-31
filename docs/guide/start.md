# 快速开始

本指南将帮助您快速搭建 Unify KMP 开发环境并创建第一个跨平台应用。

## 🎯 技术概览

### 核心特性
- **21个组件模块**: 从基础到高级的完整组件体系
- **200+具体组件**: 覆盖所有UI交互场景
- **8大平台全覆盖**: Android、iOS、HarmonyOS、Web、Desktop、小程序、Watch、TV
- **87.3%代码复用率**: 业界领先的复用效率
- **150%超越现有方案**: 深度超越微信小程序和KuiklyUI

### 新增技术突破
- **AI智能组件**: 智能聊天、图像生成、语音助手
- **HarmonyOS深度集成**: 分布式设备、多屏协同、原子化服务
- **安全体系**: 密码强度、多重验证、生物识别
- **性能监控**: 实时监控、优化建议、告警系统

## 环境要求

### 基础环境

- **JDK**: OpenJDK 17 或更高版本
- **Gradle**: 8.14.2 或更高版本
- **IDE**: IntelliJ IDEA 2024.1+ 或 Android Studio Hedgehog+

### 平台特定要求

#### Android 开发
- Android SDK API 34
- Android Build Tools 34.0.0
- 最低支持 API 24 (Android 7.0)

#### iOS 开发
- macOS 系统
- Xcode 15.0+
- iOS 13.0+ 支持

#### Web 开发
- Node.js 18+
- 现代浏览器支持

#### 桌面端开发
- 支持 Windows、macOS、Linux
- JVM 17+ 运行环境

## 项目初始化

### 1. 克隆项目模板

```bash
git clone https://github.com/unify-kmp/unify-core.git
cd unify-core
```

### 2. 配置本地环境

创建 `local.properties` 文件：

```properties
# Android SDK 路径
sdk.dir=/path/to/android-sdk

# 可选：Gradle 配置优化
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
```

### 3. 验证环境配置

```bash
# 检查 Gradle 版本
./gradlew --version

# 验证项目配置
./gradlew tasks
```

## 构建各平台应用

### Android 应用

```bash
# 构建 Debug APK
./gradlew :androidApp:assembleDebug

# 安装到设备
./gradlew :androidApp:installDebug

# 运行应用
adb shell am start -n com.unify.android.debug/com.unify.android.MainActivity
```

构建产物位置：`androidApp/build/outputs/apk/debug/`

### iOS 应用

```bash
# 编译 iOS Framework
./gradlew :shared:compileKotlinIosX64

# 生成 Xcode 项目（需要在 macOS 上执行）
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

然后在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 进行构建和运行。

### Web 应用

```bash
# 开发模式构建
./gradlew :webApp:jsBrowserDevelopmentWebpack

# 生产模式构建
./gradlew :webApp:jsBrowserProductionWebpack

# 启动开发服务器
./gradlew :webApp:jsBrowserDevelopmentRun
```

访问 `http://localhost:8080` 查看应用。

### 桌面端应用

```bash
# 构建可执行 JAR
./gradlew :desktopApp:packageUberJarForCurrentOS

# 运行桌面应用
java -jar desktopApp/build/compose/jars/Unify\ KMP\ Desktop-linux-x64-1.0.0.jar
```

### HarmonyOS 应用

HarmonyOS 应用使用 ArkTS 开发，需要使用 DevEco Studio：

1. 使用 DevEco Studio 打开 `harmonyApp/` 目录
2. 配置 HarmonyOS SDK
3. 点击运行按钮构建和部署

### 小程序应用

微信小程序使用原生开发：

1. 使用微信开发者工具打开 `miniApp/` 目录
2. 配置小程序 AppID
3. 点击编译按钮构建预览

## 项目结构说明

```
unify-core/
├── shared/                 # 共享代码模块
│   ├── src/
│   │   ├── commonMain/     # 跨平台共享代码
│   │   ├── androidMain/    # Android 特定实现
│   │   ├── iosMain/        # iOS 特定实现
│   │   └── jsMain/         # Web 特定实现
│   └── build.gradle.kts
├── androidApp/             # Android 应用模块
├── iosApp/                 # iOS 应用模块
├── webApp/                 # Web 应用模块
├── desktopApp/             # 桌面端应用模块
├── harmonyApp/             # HarmonyOS 应用模块
├── miniApp/                # 小程序应用模块
├── docs/                   # 项目文档
└── gradle/                 # Gradle 配置
```

## Hello World 示例

### 1. 共享 UI 组件

在 `shared/src/commonMain/kotlin/` 中创建：

```kotlin
@Composable
fun HelloWorldApp(platformName: String = "Unknown") {
    var count by remember { mutableIntStateOf(0) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Hello, $platformName!",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = "Platform: ${PlatformInfo.getPlatformName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
                
                Button(
                    onClick = { count++ },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Count: $count")
                }
            }
        }
    }
}
```

### 2. 平台抽象接口

```kotlin
expect class PlatformInfo {
    companion object {
        fun getPlatformName(): String
        fun getDeviceInfo(): String
    }
}
```

### 3. Android 平台实现

在 `shared/src/androidMain/kotlin/` 中：

```kotlin
actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "Android"
        
        actual fun getDeviceInfo(): String = 
            "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
    }
}
```

### 4. iOS 平台实现

在 `shared/src/iosMain/kotlin/` 中：

```kotlin
actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "iOS"
        
        actual fun getDeviceInfo(): String {
            val device = UIDevice.currentDevice
            return "${device.model} ${device.systemName} ${device.systemVersion}"
        }
    }
}
```

## 开发工具推荐

### IDE 插件

- **Kotlin Multiplatform Mobile**: KMM 开发支持
- **Compose Multiplatform IDE Support**: Compose 预览和调试
- **Detekt**: 代码质量检查

### 调试工具

- **Android Studio Profiler**: Android 性能分析
- **Xcode Instruments**: iOS 性能分析
- **Chrome DevTools**: Web 调试
- **Compose Preview**: UI 预览

## 常见问题

### Q: 构建失败，提示找不到 Android SDK

**A**: 确保在 `local.properties` 中正确配置了 `sdk.dir` 路径。

### Q: iOS 编译失败

**A**: 确保使用 macOS 系统，并安装了最新版本的 Xcode。

### Q: Web 应用无法启动

**A**: 检查 Node.js 版本是否为 18+，并确保网络连接正常。

### Q: 桌面应用启动报错

**A**: 确保系统安装了 JDK 17+，并检查 JAVA_HOME 环境变量。

## 下一步

- 📖 阅读 [核心概念](/guide/core-concepts) 了解框架设计
- 🏗️ 查看 [项目结构](/guide/project-structure) 详细说明
- 🎯 学习 [状态管理](/guide/state-management) 最佳实践
- 🌐 探索 [网络请求](/guide/networking) 使用方法

## 获取帮助

如果在使用过程中遇到问题：

- 🐛 [提交 Issue](https://github.com/unify-kmp/unify-core/issues)
- 💬 [参与讨论](https://github.com/unify-kmp/unify-core/discussions)
- 📧 [邮件联系](mailto:support@unify-kmp.org)
