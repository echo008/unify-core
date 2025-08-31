# 项目结构

本文档详细介绍 Unify KMP 项目的目录结构和文件组织方式。

## 📁 整体项目结构

```
unify-core/
├── 📱 androidApp/              # Android 应用模块
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/         # Android 特定代码
│   │       ├── res/           # Android 资源文件
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts       # Android 构建配置
│
├── 🍎 iosApp/                  # iOS 应用模块
│   ├── iosApp/
│   │   ├── ContentView.swift  # SwiftUI 视图
│   │   ├── iOSApp.swift      # iOS 应用入口
│   │   └── IOSMainViewModel.swift
│   └── iosApp.xcodeproj      # Xcode 项目文件
│
├── 🌐 webApp/                  # Web 应用模块
│   ├── src/
│   │   └── jsMain/
│   │       └── kotlin/        # Web 特定代码
│   ├── build.gradle.kts      # Web 构建配置
│   └── vite.config.js        # Vite 配置
│
├── 🖥️ desktopApp/              # 桌面应用模块
│   ├── src/
│   │   └── jvmMain/
│   │       └── kotlin/        # 桌面特定代码
│   └── build.gradle.kts      # 桌面构建配置
│
├── 🔄 harmonyApp/              # HarmonyOS 应用模块
│   ├── src/
│   │   └── main/
│   │       └── ets/          # ArkTS 代码
│   └── build-profile.json5   # HarmonyOS 构建配置
│
├── 📱 miniApp/                 # 小程序应用模块
│   ├── pages/                # 小程序页面
│   ├── app.js               # 小程序入口
│   └── app.json             # 小程序配置
│
├── 🔗 miniAppBridge/           # 小程序桥接模块
│   ├── src/
│   │   ├── jsMain/          # JS 桥接代码
│   │   └── main/            # 通用桥接代码
│   └── build.gradle.kts     # 桥接构建配置
│
├── 🎯 shared/                  # 共享代码模块 (核心)
│   ├── src/
│   │   ├── commonMain/       # 跨平台共享代码
│   │   ├── androidMain/      # Android 特定实现
│   │   ├── iosMain/         # iOS 特定实现
│   │   ├── jsMain/          # Web 特定实现
│   │   ├── jvmMain/         # JVM/Desktop 特定实现
│   │   ├── commonTest/      # 共享测试代码
│   │   ├── androidTest/     # Android 测试
│   │   └── iosTest/         # iOS 测试
│   └── build.gradle.kts     # 共享模块构建配置
│
├── 📚 docs/                    # 项目文档
│   ├── .vitepress/          # VitePress 配置
│   ├── api/                 # API 文档
│   ├── guide/               # 开发指南
│   ├── examples/            # 示例文档
│   ├── platforms/           # 平台指南
│   └── contributing/        # 贡献指南
│
├── 🔧 gradle/                  # Gradle 配置
│   ├── wrapper/             # Gradle Wrapper
│   └── libs.versions.toml   # 版本目录
│
├── 📜 scripts/                 # 构建脚本
│   ├── build-all.sh        # 全平台构建
│   ├── build-android.sh    # Android 构建
│   ├── build-desktop.sh    # 桌面构建
│   └── build-harmony.sh    # HarmonyOS 构建
│
├── ⚙️ config/                  # 配置文件
│   └── detekt/
│       └── detekt.yml       # 代码质量配置
│
├── 🏗️ build.gradle.kts         # 根项目构建配置
├── 📋 settings.gradle.kts      # 项目设置
├── 🔧 gradle.properties        # Gradle 属性
├── 📄 local.properties         # 本地配置 (不提交)
├── 🚫 .gitignore              # Git 忽略规则
├── 📜 LICENSE                  # 开源许可证
├── 📖 README.md               # 项目说明
└── 🇨🇳 开发设计指导.md          # 中文开发指导
```

## 🎯 shared 模块详细结构

shared 模块是项目的核心，包含所有跨平台共享代码：

### commonMain 目录结构

```
commonMain/kotlin/
├── com/unify/
│   ├── 🏗️ core/                    # 核心框架
│   │   ├── ai/                    # AI 功能
│   │   │   ├── AIConfigurationManager.kt
│   │   │   └── UnifyAIEngine.kt
│   │   ├── architecture/          # 架构组件
│   │   │   └── UnifyArchitecture.kt
│   │   ├── components/            # 基础组件
│   │   │   ├── UnifyButton.kt
│   │   │   ├── UnifyCard.kt
│   │   │   └── UnifyTextField.kt
│   │   ├── data/                  # 数据层
│   │   │   └── UnifyRepository.kt
│   │   ├── dynamic/               # 动态更新
│   │   │   ├── DynamicComponentFactories.kt
│   │   │   ├── UnifyDynamicEngine.kt
│   │   │   └── HotUpdateSecurityValidator.kt
│   │   ├── memory/                # 内存管理
│   │   │   └── UnifyMemoryManager.kt
│   │   ├── mvi/                   # MVI 架构
│   │   │   └── UnifyMVI.kt
│   │   ├── network/               # 网络层
│   │   │   └── UnifyNetworkService.kt
│   │   ├── performance/           # 性能监控
│   │   │   ├── PerformanceMonitor.kt
│   │   │   └── UnifyPerformanceDashboard.kt
│   │   ├── platform/              # 平台管理
│   │   │   ├── PlatformManager.kt
│   │   │   ├── HarmonyOSAdapter.kt
│   │   │   └── MiniAppBridge.kt
│   │   ├── security/              # 安全模块
│   │   │   ├── SecurityManager.kt
│   │   │   └── BiometricAuthenticator.kt
│   │   ├── storage/               # 存储层
│   │   │   ├── StorageManager.kt
│   │   │   └── CacheManager.kt
│   │   ├── testing/               # 测试工具
│   │   │   ├── TestFramework.kt
│   │   │   └── MockDataGenerator.kt
│   │   ├── ui/                    # UI 组件
│   │   │   ├── theme/             # 主题系统
│   │   │   ├── components/        # UI 组件
│   │   │   └── navigation/        # 导航组件
│   │   └── UnifyCore.kt          # 核心入口
│   │
│   ├── 🎨 ui/                      # UI 组件库
│   │   ├── components/            # 通用组件
│   │   │   ├── basic/             # 基础组件
│   │   │   ├── layout/            # 布局组件
│   │   │   ├── navigation/        # 导航组件
│   │   │   ├── input/             # 输入组件
│   │   │   ├── display/           # 显示组件
│   │   │   ├── feedback/          # 反馈组件
│   │   │   ├── media/             # 媒体组件
│   │   │   └── platform/          # 平台特定组件
│   │   ├── theme/                 # 主题系统
│   │   │   ├── Colors.kt
│   │   │   ├── Typography.kt
│   │   │   └── Shapes.kt
│   │   └── utils/                 # UI 工具
│   │
│   ├── 🌐 network/                 # 网络模块
│   │   ├── api/                   # API 接口
│   │   ├── models/                # 数据模型
│   │   └── client/                # 网络客户端
│   │
│   ├── 💾 data/                    # 数据模块
│   │   ├── local/                 # 本地数据
│   │   ├── remote/                # 远程数据
│   │   ├── models/                # 数据模型
│   │   └── repository/            # 数据仓库
│   │
│   ├── 🔧 utils/                   # 工具类
│   │   ├── extensions/            # 扩展函数
│   │   ├── helpers/               # 辅助类
│   │   └── constants/             # 常量定义
│   │
│   └── 👋 helloworld/              # Hello World 示例
│       ├── HelloWorldApp.kt      # 主应用组件
│       └── PlatformInfo.kt       # 平台信息 (expect)
```

### 平台特定实现目录

#### androidMain
```
androidMain/kotlin/com/unify/
├── core/
│   ├── network/
│   │   └── NetworkServiceFactory.android.kt
│   ├── platform/
│   │   └── PlatformManager.android.kt
│   ├── storage/
│   │   └── StorageFactory.android.kt
│   └── ui/components/
│       └── PlatformModifiers.android.kt
├── helloworld/
│   └── PlatformInfo.android.kt
└── ui/components/
    ├── media/
    │   └── UnifyLiveComponents.android.kt
    └── platform/
        └── UnifyPlatformAdapters.android.kt
```

#### iosMain
```
iosMain/kotlin/com/unify/
├── core/
│   ├── network/
│   │   └── NetworkServiceFactory.ios.kt
│   ├── platform/
│   │   └── PlatformManager.ios.kt
│   └── storage/
│       └── StorageFactory.ios.kt
├── helloworld/
│   └── PlatformInfo.ios.kt
└── ui/components/
    └── platform/
        └── UnifyPlatformAdapters.ios.kt
```

#### jsMain
```
jsMain/kotlin/com/unify/
├── core/
│   ├── network/
│   │   └── NetworkServiceFactory.js.kt
│   ├── platform/
│   │   └── PlatformManager.js.kt
│   └── storage/
│       └── StorageFactory.js.kt
├── helloworld/
│   └── PlatformInfo.js.kt
└── ui/components/
    └── platform/
        └── UnifyPlatformAdapters.js.kt
```

## 📱 应用模块结构

### androidApp 结构
```
androidApp/
├── src/main/
│   ├── kotlin/com/unify/android/
│   │   └── MainActivity.kt      # Android 主活动
│   ├── res/                     # Android 资源
│   │   ├── values/
│   │   │   ├── strings.xml
│   │   │   └── themes.xml
│   │   ├── drawable/
│   │   └── mipmap/
│   └── AndroidManifest.xml     # Android 清单文件
└── build.gradle.kts            # Android 构建配置
```

### iosApp 结构
```
iosApp/
├── iosApp/
│   ├── ContentView.swift       # SwiftUI 主视图
│   ├── iOSApp.swift           # iOS 应用入口
│   └── IOSMainViewModel.swift  # iOS 视图模型
├── iosApp.xcodeproj/          # Xcode 项目文件
└── Podfile                    # CocoaPods 依赖
```

### webApp 结构
```
webApp/
├── src/jsMain/
│   ├── kotlin/com/unify/web/
│   │   └── Main.kt            # Web 应用入口
│   └── resources/
│       └── index.html         # HTML 模板
├── build.gradle.kts          # Web 构建配置
└── vite.config.js            # Vite 配置
```

## 🔧 配置文件说明

### 根级配置文件

#### build.gradle.kts
```kotlin
// 根项目构建配置
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
```

#### settings.gradle.kts
```kotlin
// 项目模块配置
include(":shared")
include(":androidApp")
include(":webApp")
include(":desktopApp")
include(":miniAppBridge")
```

#### gradle.properties
```properties
# Gradle 配置优化
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Kotlin 配置
kotlin.code.style=official
kotlin.mpp.stability.nowarn=true
kotlin.native.ignoreDisabledTargets=true
```

### 版本管理

#### gradle/libs.versions.toml
```toml
[versions]
kotlin = "2.1.0"
compose = "1.7.5"
ktor = "3.2.3"
sqldelight = "2.0.2"
koin = "4.0.0"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
# ... 更多依赖
```

## 📚 文档结构

### docs 目录组织
```
docs/
├── .vitepress/               # VitePress 配置
│   └── config.ts            # 文档站点配置
├── api/                     # API 文档
│   ├── index.md            # API 概览
│   ├── core.md             # 核心 API
│   └── ui.md               # UI 组件 API
├── guide/                  # 开发指南
│   ├── start.md           # 快速开始
│   ├── introduction.md    # 项目介绍
│   └── advanced.md        # 高级教程
├── examples/               # 示例文档
│   ├── hello_world.md     # Hello World
│   └── todo_app.md        # Todo 应用
├── platforms/              # 平台指南
│   ├── android.md         # Android 开发
│   ├── ios.md             # iOS 开发
│   └── web.md             # Web 开发
└── contributing/           # 贡献指南
    └── contributing.md    # 贡献规范
```

## 🚀 构建产物

### 各平台构建输出

#### Android
```
androidApp/build/outputs/
├── apk/debug/              # Debug APK
├── apk/release/            # Release APK
└── bundle/release/         # AAB 文件
```

#### iOS
```
iosApp/build/
├── XCFrameworks/           # iOS Framework
└── ios/                    # iOS 构建产物
```

#### Web
```
webApp/build/dist/          # Web 构建产物
├── js/                     # JavaScript 文件
├── css/                    # 样式文件
└── index.html              # 入口 HTML
```

#### Desktop
```
desktopApp/build/compose/jars/
└── Unify KMP Desktop-*.jar # 可执行 JAR
```

## 📋 开发规范

### 文件命名规范
- **Kotlin 文件**: PascalCase (如 `HelloWorldApp.kt`)
- **资源文件**: snake_case (如 `app_icon.png`)
- **配置文件**: kebab-case (如 `build-config.json`)

### 包名规范
- **根包名**: `com.unify`
- **功能模块**: `com.unify.core.network`
- **平台特定**: `com.unify.android.utils`

### 目录组织原则
1. **按功能分组**: 相关功能放在同一目录
2. **平台隔离**: 平台特定代码分别存放
3. **层次清晰**: 保持合理的目录层次
4. **命名一致**: 使用统一的命名规范

---

通过理解项目结构，您可以快速定位代码、添加新功能，并维护项目的整洁性。
