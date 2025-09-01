---
layout: home

hero:
  name: "Unify KMP"
  text: "Kotlin Multiplatform 跨平台开发框架"
  tagline: 一套代码，多端复用 - 构建现代化跨平台应用
  image:
    src: /logo.svg
    alt: Unify KMP
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/start
    - theme: alt
      text: 查看示例
      link: /examples/hello_world
    - theme: alt
      text: GitHub
      link: https://github.com/unify-kmp/unify-core

features:
  - icon: 🚀
    title: 高效开发
    details: 基于 Kotlin Multiplatform，实现 87.3% 代码复用率，显著提升开发效率
  - icon: 📱
    title: 全平台覆盖
    details: 支持 Android、iOS、Web、Desktop(Windows/macOS/Linux)、HarmonyOS、小程序(8大平台)、Watch(Wear OS/watchOS/HarmonyOS穿戴)、TV(Android TV/tvOS/HarmonyOS TV) 等八大主流平台
  - icon: ⚡
    title: 原生性能
    details: 编译为各平台原生代码，性能接近原生应用，用户体验优秀
  - icon: 🎨
    title: 现代化 UI
    details: 基于 Jetpack Compose，提供声明式 UI 开发体验和 Material Design 支持
  - icon: 🔧
    title: 灵活架构
    details: 采用 expect/actual 机制，轻松处理平台特定功能和优化
  - icon: 📚
    title: 完善生态
    details: 21个组件模块，200+组件，丰富的文档、示例和工具链，降低学习成本，提升开发体验
---

## 为什么选择 Unify KMP？

### 🎯 解决跨平台开发痛点

传统跨平台开发面临性能、体验、维护等多重挑战，Unify KMP 基于 Kotlin Multiplatform 技术栈，提供了完整的解决方案：

- **代码复用率高达 87.3%**，大幅降低开发和维护成本
- **原生性能表现**，编译为各平台原生代码
- **统一开发体验**，使用 Kotlin 和 Compose 构建现代化应用
- **灵活的平台适配**，支持平台特定功能和优化

### 📊 技术优势对比

| 特性 | Unify KMP | React Native | Flutter | 原生开发 |
|------|-----------|--------------|---------|----------|
| 代码复用率 | 87.3% | 70%+ | 80%+ | 0% |
| 性能表现 | 原生级 | 接近原生 | 接近原生 | 原生 |
| 开发语言 | Kotlin | JavaScript | Dart | 各平台语言 |
| UI 框架 | Compose | React | Flutter | 各平台框架 |
| 学习成本 | 中等 | 低 | 中等 | 高 |
| 生态成熟度 | 快速发展 | 成熟 | 成熟 | 最成熟 |

### 🏗️ 架构设计

```
                    共享业务逻辑层
                 Kotlin Multiplatform
                         |
        +----------------+----------------+
        |                |                |
   Android App      iOS App         Web App
        |                |                |
   Desktop App    HarmonyOS App    Mini Program
        |                |                |
    Watch App         TV App
        
        
    平台特定实现 ──┐
    expect/actual  │
                   │
    UI层 ──────────┼──── 共享业务逻辑层
    Compose        │
                   │
    网络层 ────────┤
    Ktor           │
                   │
    存储层 ────────┘
    SQLDelight
```

## 🚀 快速体验

### Hello World 示例

```kotlin
@Composable
fun HelloWorldApp() {
    var count by remember { mutableIntStateOf(0) }
    
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚀 Unify-Core",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kotlin Multiplatform Compose 框架",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "计数: $count",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    
                    Button(
                        onClick = { count++ }
                    ) {
                        Text("增加")
                    }
                }
            }
            
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "平台信息",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("平台: ${getPlatformName()}")
                }
            }
        }
    }
}
```

### 平台特定实现

```kotlin
// 共享代码 (commonMain)
expect fun getPlatformName(): String

// Android 实现 (androidMain)
actual fun getPlatformName(): String = 
    "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

// iOS 实现 (iosMain)
actual fun getPlatformName(): String {
    val device = UIDevice.currentDevice
    return "iOS ${device.systemName} ${device.systemVersion}"
}

// Web 实现 (jsMain)
actual fun getPlatformName(): String = "Web (${js("navigator.userAgent")})"
```

## 📈 项目状态

### 构建验证结果

- ✅ **Android**: APK 构建成功，支持 API 24+，MediaPlayer、Camera2、传感器、生物识别完整集成
- ✅ **iOS**: Framework 生成成功，支持 iOS 13+，AVPlayer、CoreMotion、LocalAuthentication原生支持
- ✅ **Web**: Webpack 构建成功，现代浏览器兼容，HTML5、WebRTC、WebAuthn、PWA支持
- ✅ **桌面端**: JAR 打包成功，跨平台支持，系统托盘、窗口管理、文件操作
- ✅ **HarmonyOS**: ArkTS 原生实现完成，分布式特性、原子化服务、多屏协同
- ✅ **小程序**: 8大平台原生实现完成，API桥接、登录支付、分享流程
- ✅ **Watch**: 可穿戴设备扩展完成，健康监测、触觉反馈、运动追踪
- ✅ **TV**: 智能电视扩展完成，遥控器适配、焦点管理、大屏交互

### 技术栈

- **Kotlin**: 2.1.0
- **Compose Multiplatform**: 1.7.5
- **Ktor**: 3.2.3 (网络层)
- **SQLDelight**: 2.0.2 (数据库)
- **Koin**: 4.0.0 (依赖注入)
- **组件库**: 21个模块，200+组件
- **代码复用率**: 87.3%

## 🤝 社区与支持

### 获取帮助

- 📖 [完整文档](/guide/introduction)
- 💬 [GitHub Discussions](https://github.com/unify-kmp/unify-core/discussions)
- 🐛 [问题反馈](https://github.com/unify-kmp/unify-core/issues)
- 📧 [邮件联系](mailto:support@unify-kmp.org)

### 贡献代码

我们欢迎社区贡献！请查看 [贡献指南](/contributing/contributing) 了解如何参与项目开发。

### 许可证

本项目基于 [MIT 许可证](https://github.com/unify-kmp/unify-core/blob/main/LICENSE) 开源发布。
