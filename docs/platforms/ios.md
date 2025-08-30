# iOS 平台开发指南

## 📱 概述

Unify KMP 为 iOS 平台提供了完整的 Kotlin Multiplatform 支持，通过 Kotlin/Native 编译器生成原生 iOS Framework，实现高性能的跨平台开发体验。

## 🛠️ 环境要求

### 必需工具
- **macOS**: 12.0+ (Monterey 或更高版本)
- **Xcode**: 14.0+ 
- **Xcode Command Line Tools**: 最新版本
- **JDK**: 17 或更高版本
- **Kotlin**: 2.0.21+

### 安装验证
```bash
# 检查 Xcode 版本
xcodebuild -version

# 检查 Command Line Tools
xcode-select --print-path

# 检查 JDK 版本
java -version

# 检查 Kotlin 版本
./gradlew --version
```

## 🏗️ 项目结构

### iOS 应用模块
```
iosApp/
├── iosApp/
│   ├── ContentView.swift          # 主界面视图
│   ├── IOSMainViewModel.swift     # 视图模型
│   ├── iOSApp.swift              # 应用入口
│   └── Info.plist                # 应用配置
├── iosApp.xcodeproj/             # Xcode 项目文件
└── Podfile                       # CocoaPods 依赖
```

### Shared Framework 集成
```kotlin
// shared/build.gradle.kts
kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
}
```

## 💻 核心组件实现

### 1. 应用入口 (iOSApp.swift)
```swift
import SwiftUI
import shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### 2. 主界面视图 (ContentView.swift)
```swift
import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = IOSMainViewModel()
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Text("Hello, iOS!")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                
                Text("Platform: \(PlatformInfo.companion.getPlatformName())")
                    .font(.body)
                    .foregroundColor(.secondary)
                
                Text("Device: \(PlatformInfo.companion.getDeviceInfo())")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text("Count: \(viewModel.count)")
                    .font(.title)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Button(action: {
                    viewModel.increment()
                }) {
                    Text("Increment")
                        .font(.title2)
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(10)
                }
                
                Button(action: {
                    viewModel.reset()
                }) {
                    Text("Reset")
                        .font(.body)
                        .foregroundColor(.blue)
                        .padding()
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.blue, lineWidth: 1)
                        )
                }
            }
            .padding()
            .navigationTitle("Unify KMP")
        }
    }
}

#Preview {
    ContentView()
}
```

### 3. 视图模型 (IOSMainViewModel.swift)
```swift
import Foundation
import shared
import Combine

class IOSMainViewModel: ObservableObject {
    @Published var count: Int32 = 0
    
    private let platformInfo = PlatformInfo.companion
    
    init() {
        // 初始化逻辑
    }
    
    func increment() {
        count += 1
    }
    
    func reset() {
        count = 0
    }
    
    func getPlatformInfo() -> String {
        return platformInfo.getPlatformName()
    }
    
    func getDeviceInfo() -> String {
        return platformInfo.getDeviceInfo()
    }
}
```

## 🔧 平台特定实现

### PlatformInfo iOS 实现
```kotlin
// shared/src/iosMain/kotlin/com/unify/helloworld/PlatformInfo.ios.kt
import platform.UIKit.UIDevice
import platform.Foundation.NSProcessInfo

actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "iOS"
        
        actual fun getDeviceInfo(): String {
            val device = UIDevice.currentDevice
            val processInfo = NSProcessInfo.processInfo
            
            return buildString {
                append("Device: ${device.model}")
                append("\nSystem: ${device.systemName} ${device.systemVersion}")
                append("\nProcessor: ${processInfo.processorCount} cores")
            }
        }
    }
}
```

## 🚀 构建和运行

### 1. 构建 Shared Framework
```bash
# 构建所有 iOS 目标
./gradlew :shared:assembleXCFramework

# 构建特定目标
./gradlew :shared:compileKotlinIosX64
./gradlew :shared:compileKotlinIosArm64
./gradlew :shared:compileKotlinIosSimulatorArm64
```

### 2. 在 Xcode 中运行
```bash
# 打开 Xcode 项目
open iosApp/iosApp.xcodeproj

# 或使用命令行构建
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15' build
```

### 3. 模拟器运行
```bash
# 启动模拟器
xcrun simctl boot "iPhone 15"

# 安装应用
xcrun simctl install booted iosApp/build/ios/Debug-iphonesimulator/iosApp.app
```

## 📦 依赖管理

### CocoaPods 配置 (Podfile)
```ruby
platform :ios, '14.0'

target 'iosApp' do
  use_frameworks!
  
  # Kotlin Multiplatform Framework
  pod 'shared', :path => '../shared'
  
  # 其他依赖
  # pod 'Alamofire', '~> 5.0'
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '14.0'
    end
  end
end
```

### Swift Package Manager (可选)
```swift
// Package.swift
import PackageDescription

let package = Package(
    name: "UnifyKMP",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(name: "UnifyKMP", targets: ["UnifyKMP"])
    ],
    targets: [
        .binaryTarget(
            name: "shared",
            path: "./shared.xcframework"
        ),
        .target(
            name: "UnifyKMP",
            dependencies: ["shared"]
        )
    ]
)
```

## 🎨 UI 设计指南

### SwiftUI 集成最佳实践
```swift
// 1. 状态管理
@StateObject private var viewModel = IOSMainViewModel()
@State private var isLoading = false

// 2. 数据绑定
TextField("输入内容", text: $viewModel.inputText)

// 3. 异步操作
.task {
    await viewModel.loadData()
}

// 4. 错误处理
.alert("错误", isPresented: $viewModel.hasError) {
    Button("确定") { }
} message: {
    Text(viewModel.errorMessage)
}
```

### 主题和样式
```swift
// 自定义颜色
extension Color {
    static let unifyPrimary = Color(red: 0.2, green: 0.6, blue: 1.0)
    static let unifySecondary = Color(red: 0.8, green: 0.8, blue: 0.8)
}

// 自定义字体
extension Font {
    static let unifyTitle = Font.custom("SF Pro Display", size: 24)
    static let unifyBody = Font.custom("SF Pro Text", size: 16)
}
```

## 🔍 调试和测试

### 调试技巧
```swift
// 1. 打印调试
print("Debug: \(viewModel.count)")

// 2. 断点调试
// 在 Xcode 中设置断点

// 3. 日志输出
import os.log
let logger = Logger(subsystem: "com.unify.ios", category: "main")
logger.info("应用启动")
```

### 单元测试
```swift
import XCTest
@testable import iosApp
import shared

class IOSMainViewModelTests: XCTestCase {
    var viewModel: IOSMainViewModel!
    
    override func setUp() {
        super.setUp()
        viewModel = IOSMainViewModel()
    }
    
    func testIncrement() {
        viewModel.increment()
        XCTAssertEqual(viewModel.count, 1)
    }
    
    func testReset() {
        viewModel.count = 5
        viewModel.reset()
        XCTAssertEqual(viewModel.count, 0)
    }
}
```

## 📱 性能优化

### 内存管理
```swift
// 1. 弱引用避免循环引用
weak var delegate: SomeDelegate?

// 2. 合理使用 @StateObject 和 @ObservedObject
@StateObject private var viewModel = IOSMainViewModel() // 创建
@ObservedObject var viewModel: IOSMainViewModel // 传递

// 3. 及时释放资源
deinit {
    // 清理工作
}
```

### 启动优化
```swift
// 1. 延迟初始化
lazy var expensiveResource = createExpensiveResource()

// 2. 异步加载
Task {
    await loadHeavyData()
}

// 3. 预加载关键资源
func preloadCriticalResources() {
    // 预加载逻辑
}
```

## 🚀 发布准备

### App Store 配置
```xml
<!-- Info.plist 关键配置 -->
<key>CFBundleDisplayName</key>
<string>Unify KMP</string>
<key>CFBundleVersion</key>
<string>1.0.0</string>
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <false/>
</dict>
```

### 构建配置
```bash
# Release 构建
./gradlew :shared:assembleXCFramework
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release archive

# 代码签名
codesign --force --sign "iPhone Distribution: Your Company" iosApp.app
```

## ❓ 常见问题

### Q: Framework 找不到符号
**A**: 检查 Kotlin/Native 编译目标配置，确保包含所需的 iOS 架构。

### Q: Xcode 构建失败
**A**: 清理构建缓存：`./gradlew clean` 和 Xcode 中 `Product -> Clean Build Folder`。

### Q: 模拟器运行崩溃
**A**: 检查最低 iOS 版本要求，确保模拟器版本兼容。

### Q: CocoaPods 依赖冲突
**A**: 更新 Podfile.lock：`pod deintegrate && pod install`。

## 📚 参考资源

- [Kotlin Multiplatform Mobile 官方文档](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [SwiftUI 官方教程](https://developer.apple.com/tutorials/swiftui)
- [iOS 开发指南](https://developer.apple.com/ios/)
- [Xcode 用户指南](https://developer.apple.com/documentation/xcode)

---

通过本指南，您可以成功在 iOS 平台上构建和运行 Unify KMP 应用，享受跨平台开发的便利性和原生性能的优势。
