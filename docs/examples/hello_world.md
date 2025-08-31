# Hello World 示例

这是 Unify KMP 框架的基础示例，展示了如何创建一个简单的跨平台 Hello World 应用。

## 项目概述

Hello World 示例演示了以下核心功能：

- 跨平台 UI 组件开发
- 平台特定功能实现
- 状态管理
- Material3 设计系统
- 响应式交互

## 完整源码

### 共享 UI 组件

```kotlin
// shared/src/commonMain/kotlin/com/unify/helloworld/HelloWorldApp.kt
package com.unify.helloworld

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                    text = "Welcome to Unify KMP",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Text(
                    text = "Platform: ${PlatformInfo.getPlatformName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
                
                Text(
                    text = "Device: ${PlatformInfo.getDeviceInfo()}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Button(
                    onClick = { count++ },
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Count: $count")
                }
                
                if (count > 0) {
                    Button(
                        onClick = { count = 0 },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}
```

### 平台抽象接口

```kotlin
// shared/src/commonMain/kotlin/com/unify/helloworld/PlatformInfo.kt
expect class PlatformInfo {
    companion object {
        fun getPlatformName(): String
        fun getDeviceInfo(): String
    }
}
```

### Android 平台实现

```kotlin
// shared/src/androidMain/kotlin/com/unify/helloworld/PlatformInfo.android.kt
package com.unify.helloworld

import android.os.Build

actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "Android"
        
        actual fun getDeviceInfo(): String = 
            "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
    }
}
```

### iOS 平台实现

```kotlin
// shared/src/iosMain/kotlin/com/unify/helloworld/PlatformInfo.ios.kt
package com.unify.helloworld

import platform.UIKit.UIDevice

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

### Web 平台实现

```kotlin
// shared/src/jsMain/kotlin/com/unify/helloworld/PlatformInfo.js.kt
package com.unify.helloworld

import kotlinx.browser.window

actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "Web"
        
        actual fun getDeviceInfo(): String = window.navigator.userAgent
    }
}
```

## 平台应用入口

### Android MainActivity

```kotlin
// androidApp/src/main/kotlin/com/unify/android/MainActivity.kt
package com.unify.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.unify.helloworld.HelloWorldApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloWorldApp("Android")
                }
            }
        }
    }
}
```

### iOS ContentView

```swift
// iosApp/iosApp/ContentView.swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return IOSMainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

```kotlin
// iosApp/src/iosMain/kotlin/IOSMainViewController.kt
import androidx.compose.ui.window.ComposeUIViewController
import com.unify.helloworld.HelloWorldApp

fun MainViewController() = ComposeUIViewController {
    HelloWorldApp("iOS")
}
```

### Web 入口

```kotlin
// webApp/src/jsMain/kotlin/Main.kt
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.unify.helloworld.HelloWorldApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        HelloWorldApp("Web")
    }
}
```

### 桌面端入口

```kotlin
// desktopApp/src/jvmMain/kotlin/Main.kt
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.unify.helloworld.HelloWorldApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Unify KMP Desktop"
    ) {
        HelloWorldApp("Desktop")
    }
}
```

## 运行示例

### Android

```bash
# 构建并安装到设备
./gradlew :androidApp:installDebug

# 或者构建 APK
./gradlew :androidApp:assembleDebug
```

### iOS

```bash
# 编译 iOS Framework
./gradlew :shared:compileKotlinIosX64

# 在 Xcode 中打开项目
open iosApp/iosApp.xcodeproj
```

### Web

```bash
# 启动开发服务器
./gradlew :webApp:jsBrowserDevelopmentRun

# 访问 http://localhost:8080
```

### 桌面端

```bash
# 构建并运行
./gradlew :desktopApp:run

# 或者打包为 JAR
./gradlew :desktopApp:packageUberJarForCurrentOS
```

## 功能说明

### 1. 响应式计数器

- 点击 "Count" 按钮增加计数
- 计数大于 0 时显示 "Reset" 按钮
- 使用 `remember` 和 `mutableIntStateOf` 管理状态

### 2. 平台信息显示

- 自动检测并显示当前运行平台
- 显示设备特定信息（制造商、型号、系统版本等）
- 使用 `expect/actual` 机制实现平台特定功能

### 3. Material3 设计

- 使用 Material3 主题系统
- 响应式布局适配不同屏幕尺寸
- 现代化的视觉设计

### 4. 多语言准备

- 文本内容支持国际化扩展
- 预留多语言切换功能接口

## 代码解析

### 状态管理

```kotlin
var count by remember { mutableIntStateOf(0) }
```

使用 Compose 的 `remember` 函数保存组件状态，`mutableIntStateOf` 创建可观察的整数状态。

### 条件渲染

```kotlin
if (count > 0) {
    Button(onClick = { count = 0 }) {
        Text("Reset")
    }
}
```

根据计数器值条件性显示重置按钮，展示了 Compose 的声明式 UI 特性。

### 平台特定实现

```kotlin
expect class PlatformInfo {
    companion object {
        fun getPlatformName(): String
        fun getDeviceInfo(): String
    }
}
```

使用 `expect` 声明跨平台接口，各平台使用 `actual` 提供具体实现。

## 扩展示例

### 添加主题切换

```kotlin
@Composable
fun HelloWorldAppWithTheme(platformName: String = "Unknown") {
    var isDarkTheme by remember { mutableStateOf(false) }
    
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                // 原有内容
                HelloWorldContent(platformName)
                
                // 主题切换按钮
                Button(onClick = { isDarkTheme = !isDarkTheme }) {
                    Text(if (isDarkTheme) "Light Theme" else "Dark Theme")
                }
            }
        }
    }
}
```

### 添加动画效果

```kotlin
@Composable
fun AnimatedCounter() {
    var count by remember { mutableIntStateOf(0) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Text(
        text = "Count: $animatedCount",
        style = MaterialTheme.typography.headlineLarge
    )
}
```

### 添加网络请求

```kotlin
@Composable
fun HelloWorldWithNetwork() {
    var networkStatus by remember { mutableStateOf("Checking...") }
    
    LaunchedEffect(Unit) {
        try {
            // 模拟网络请求
            delay(2000)
            networkStatus = "Connected"
        } catch (e: Exception) {
            networkStatus = "Error: ${e.message}"
        }
    }
    
    Column {
        HelloWorldApp()
        Text("Network: $networkStatus")
    }
}
```

## 学习要点

1. **跨平台开发**: 一套 UI 代码在多个平台运行
2. **平台特定功能**: 使用 expect/actual 处理平台差异
3. **状态管理**: Compose 的响应式状态系统
4. **声明式 UI**: 描述 UI 状态而非操作步骤
5. **Material Design**: 现代化的设计系统应用

## 下一步

- 学习 [状态管理](/guide/state_management.md) 进阶技巧
- 探索 [网络请求](/guide/core_concepts.md) 实现方法
- 了解 [导航路由](/guide/advanced.md) 系统
- 查看更复杂的 [Todo 应用示例](/examples/todo_app)
