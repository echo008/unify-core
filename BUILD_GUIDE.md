# Unify KMP Hello World 构建指南

本指南将帮助您构建和运行 Unify KMP 框架的 Hello World 示例应用。

## 📋 前置要求

### 通用要求
- JDK 17 或更高版本
- Gradle 8.0 或更高版本

### Android 平台
- Android SDK (API 34)
- Android Studio (推荐)

### iOS 平台
- macOS 系统
- Xcode 15.0 或更高版本
- iOS 17.0 SDK

### Web 平台
- 现代浏览器 (Chrome, Firefox, Safari, Edge)

## 🚀 快速开始

### 构建所有平台
```bash
# 赋予执行权限
chmod +x scripts/*.sh

# 构建所有平台
./scripts/build-all.sh
```

### 单独构建平台

#### Android 应用
```bash
./scripts/build-android.sh
```
构建产物: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

#### iOS 应用
```bash
./scripts/build-ios.sh
```
然后在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 运行

#### Web 应用
```bash
./scripts/build-web.sh
```
构建产物: `webApp/build/distributions/`

启动本地服务器:
```bash
cd webApp/build/distributions
./serve.sh
```
访问: http://localhost:8080

## 📱 手动构建步骤

### Android
```bash
# 清理
./gradlew clean

# 构建共享模块
./gradlew :shared:build

# 构建Android应用
./gradlew :androidApp:assembleDebug

# 安装到设备/模拟器
./gradlew :androidApp:installDebug
```

### iOS
```bash
# 构建共享框架
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# 使用Xcode构建
cd iosApp
xcodebuild build -project iosApp.xcodeproj -scheme iosApp
```

### Web
```bash
# 构建Web应用
./gradlew :webApp:jsBrowserDistribution

# 开发模式运行
./gradlew :webApp:jsBrowserDevelopmentRun
```

## 🧪 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行共享模块测试
./gradlew :shared:test

# 运行Android测试
./gradlew :androidApp:testDebugUnitTest
```

## 🔧 开发模式

### Android
在 Android Studio 中打开项目根目录，选择 `androidApp` 配置运行。

### iOS
在 Xcode 中打开 `iosApp/iosApp.xcodeproj`，选择目标设备运行。

### Web
```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```
访问: http://localhost:8080

## 📦 应用功能

Hello World 示例应用包含以下功能:

- **计数器演示**: 展示状态管理和UI更新
- **多语言支持**: 中文、英文、日文切换
- **跨平台一致性**: 相同的业务逻辑和UI体验
- **现代化UI**: 使用 Compose Multiplatform 构建

## 🛠️ 故障排除

### 常见问题

#### Android 构建失败
- 检查 Android SDK 是否正确安装
- 确保 ANDROID_HOME 环境变量设置正确
- 运行 `./gradlew --refresh-dependencies`

#### iOS 构建失败
- 确保在 macOS 系统上运行
- 检查 Xcode 是否正确安装
- 运行 `xcode-select --install` 安装命令行工具

#### Web 构建失败
- 检查 Node.js 是否安装 (某些插件需要)
- 清理构建缓存: `./gradlew clean`

### 清理构建缓存
```bash
# 清理 Gradle 缓存
./gradlew clean
rm -rf .gradle

# 清理 Kotlin/Native 缓存
rm -rf ~/.konan
```

## 📚 更多资源

- [Kotlin Multiplatform 官方文档](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform 文档](https://github.com/JetBrains/compose-multiplatform)
- [Unify KMP 框架文档](./docs/)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个示例项目！
