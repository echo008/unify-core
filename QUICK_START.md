# 🚀 Unify KMP 快速开始指南

## 📋 环境要求

### 基础环境
- **JDK**: 17+ (推荐 OpenJDK 17)
- **IDE**: IntelliJ IDEA 或 Android Studio (最新版)
- **Gradle**: 8.5+ (通过 Wrapper 自动管理)

### 平台特定要求
- **Android**: Android SDK API 34+, Android Studio
- **iOS**: macOS + Xcode 15+, CocoaPods
- **Web**: 现代浏览器支持
- **桌面**: JVM 17+

## ⚡ 5分钟快速体验

### 1. 克隆项目
```bash
git clone <repository-url>
cd unify-core
```

### 2. 一键构建所有平台
```bash
# 构建所有平台应用
bash scripts/build-all.sh
```

### 3. 运行示例应用

#### Android
```bash
./gradlew :androidApp:assembleDebug
# APK位置: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

#### Web
```bash
./gradlew :webApp:jsBrowserDevelopmentWebpack
# 访问: http://localhost:8080
```

#### 桌面端
```bash
./gradlew :desktopApp:packageUberJarForCurrentOS
# JAR位置: desktopApp/build/compose/jars/
```

#### iOS (仅macOS)
```bash
# 1. 安装依赖
cd iosApp && pod install

# 2. 在Xcode中打开iosApp.xcworkspace
open iosApp.xcworkspace
```

## 📱 平台支持状态

| 平台 | 支持程度 | 构建命令 | 产物位置 |
|------|----------|----------|----------|
| **Android** | ✅ 100% | `./gradlew :androidApp:assembleDebug` | `androidApp/build/outputs/apk/` |
| **Web** | ✅ 95% | `./gradlew :webApp:jsBrowserDevelopmentWebpack` | `webApp/build/distributions/` |
| **桌面端** | ✅ 95% | `./gradlew :desktopApp:packageUberJarForCurrentOS` | `desktopApp/build/compose/jars/` |
| **iOS** | ✅ 90% | Xcode构建 | Xcode项目 |

## 🛠️ 开发集成

### 添加到现有项目

1. **复制核心模块**
```bash
cp -r shared/ your-project/
```

2. **更新settings.gradle.kts**
```kotlin
include(":shared")
include(":your-app")
```

3. **添加依赖**
```kotlin
// your-app/build.gradle.kts
dependencies {
    implementation(project(":shared"))
}
```

### 创建新功能

1. **在shared模块中定义接口**
```kotlin
// shared/src/commonMain/kotlin/YourFeature.kt
expect class YourFeature {
    fun doSomething(): String
}
```

2. **实现平台特定代码**
```kotlin
// shared/src/androidMain/kotlin/YourFeature.kt
actual class YourFeature {
    actual fun doSomething(): String = "Android实现"
}
```

3. **在UI中使用**
```kotlin
@Composable
fun YourScreen() {
    val feature = YourFeature()
    Text(feature.doSomething())
}
```

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 运行特定平台测试
./gradlew :shared:testDebugUnitTest    # Android
./gradlew :shared:iosSimulatorArm64Test # iOS
./gradlew :shared:jsTest               # Web
```

## 📦 发布

### Android
```bash
./gradlew :androidApp:assembleRelease
```

### Web
```bash
./gradlew :webApp:jsBrowserProductionWebpack
```

### 桌面端
```bash
./gradlew :desktopApp:packageUberJarForCurrentOS
```

## 🔧 常见问题

### Q: 编译失败怎么办？
A: 首先清理项目：
```bash
./gradlew clean
./gradlew --stop
```

### Q: iOS编译报错？
A: 确保：
1. 使用macOS系统
2. 安装了Xcode 15+
3. 运行了`pod install`

### Q: Web应用无法访问？
A: 检查端口是否被占用，或使用：
```bash
./gradlew :webApp:jsBrowserRun --continuous
```

## 📚 更多资源

- [架构文档](docs/ARCHITECTURE.md)
- [API参考](docs/API_REFERENCE.md)
- [贡献指南](CONTRIBUTING.md)
- [问题反馈](https://github.com/your-org/unify-core/issues)

---

**🎉 恭喜！你已经成功运行了Unify KMP跨平台应用！**
