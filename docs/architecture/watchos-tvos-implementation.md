# watchOS & tvOS平台实现指南

## 概述

本文档详细说明了Unify-Core项目中watchOS和tvOS平台的具体实现方案，包括技术架构、API桥接、UI适配和部署策略。

## watchOS平台实现

### 1. 核心架构

#### 1.1 技术栈
- **开发语言**: Kotlin Multiplatform + Swift/SwiftUI
- **健康框架**: HealthKit
- **界面框架**: WatchKit + SwiftUI
- **通知框架**: UserNotifications
- **传感器**: CoreMotion
- **存储**: Core Data + UserDefaults

#### 1.2 模块结构
```
watchOS/
├── UnifyWatchManager.ios.kt     # Kotlin实现
├── WatchComposeComponents.kt    # Compose UI组件
├── SwiftUI/                     # SwiftUI桥接
│   ├── WatchContentView.swift
│   ├── HealthDataView.swift
│   └── NotificationHandler.swift
└── Resources/                   # 资源文件
    ├── Assets.xcassets
    └── Info.plist
```

### 2. 健康数据集成

#### 2.1 HealthKit权限管理
```kotlin
// 请求健康数据权限
private fun requestHealthKitPermissions() {
    val readTypes = setOf(
        HKQuantityTypeIdentifierStepCount,
        HKQuantityTypeIdentifierHeartRate,
        HKQuantityTypeIdentifierActiveEnergyBurned,
        HKQuantityTypeIdentifierAppleExerciseTime,
        HKQuantityTypeIdentifierDistanceWalkingRunning
    )
    
    healthStore.requestAuthorizationToShareTypes(
        typesToShare = null,
        typesToRead = readTypes
    ) { success, error ->
        if (success) {
            startHealthDataCollection()
        }
    }
}
```

#### 2.2 实时数据监控
- **步数统计**: 使用HKStatisticsQuery获取当日步数
- **心率监控**: 通过HKAnchoredObjectQuery实时获取心率数据
- **卡路里消耗**: 统计活动能量消耗
- **锻炼时间**: 获取Apple锻炼时间数据

### 3. 用户界面适配

#### 3.1 Compose组件适配
- **圆形布局**: 适配Apple Watch圆形屏幕
- **Digital Crown**: 支持数字表冠滚动交互
- **触觉反馈**: 集成Taptic Engine反馈
- **环境模式**: 支持Always-On Display模式

#### 3.2 SwiftUI桥接
```swift
struct WatchContentView: View {
    @StateObject private var healthManager = HealthManager()
    
    var body: some View {
        NavigationView {
            VStack {
                HealthRingsView(healthData: healthManager.healthData)
                
                ComposeUIView() // Kotlin Compose组件
                
                QuickActionsView()
            }
        }
    }
}
```

## tvOS平台实现

### 1. 核心架构

#### 1.1 技术栈
- **开发语言**: Kotlin Multiplatform + Swift/SwiftUI
- **媒体框架**: AVFoundation + AVKit
- **界面框架**: UIKit + SwiftUI
- **输入处理**: GameController Framework
- **网络**: URLSession + Alamofire

#### 1.2 模块结构
```
tvOS/
├── UnifyTVManager.ios.kt        # Kotlin实现
├── TVComposeComponents.kt       # Compose UI组件
├── SwiftUI/                     # SwiftUI桥接
│   ├── TVContentView.swift
│   ├── MediaPlayerView.swift
│   └── RemoteControlHandler.swift
└── Resources/                   # 资源文件
    ├── Assets.xcassets
    └── Info.plist
```

### 2. 媒体播放集成

#### 2.1 AVPlayer集成
```kotlin
// 媒体播放控制
actual suspend fun loadMedia(mediaUrl: String): Boolean {
    val url = NSURL.URLWithString(mediaUrl)
    avPlayerItem = AVPlayerItem(uRL = url)
    avPlayer?.replaceCurrentItemWithPlayerItem(avPlayerItem)
    
    // 设置播放状态观察者
    setupPlaybackObservers()
    return true
}
```

#### 2.2 远程控制支持
- **Siri Remote**: 支持触摸板、按钮和语音控制
- **媒体控制**: 播放、暂停、快进、快退
- **音量控制**: 系统音量集成
- **焦点管理**: tvOS焦点引擎适配

### 3. 10-foot UI设计

#### 3.1 大屏幕适配原则
- **大字体**: 最小字体16sp，推荐18-24sp
- **高对比度**: 深色主题，高对比度配色
- **大触摸目标**: 最小48dp触摸区域
- **焦点指示**: 清晰的焦点高亮效果

#### 3.2 导航模式
```kotlin
@Composable
fun TVNavigationLayout() {
    Row {
        // 侧边导航栏
        TVNavigationPanel(
            items = navigationItems,
            selectedIndex = selectedIndex,
            onSelectionChanged = { selectedIndex = it },
            modifier = Modifier.width(280.dp)
        )
        
        // 主内容区域
        TVContentPanel(
            selectedIndex = selectedIndex,
            modifier = Modifier.weight(1f)
        )
    }
}
```

## 跨平台共享策略

### 1. 代码共享架构

#### 1.1 共享层级
```
共享代码 (commonMain)
├── 业务逻辑 (100%共享)
├── 数据模型 (100%共享)
├── UI组件 (90%共享)
└── 平台接口 (expect声明)

平台特定 (iosMain)
├── 平台管理器 (actual实现)
├── 原生API桥接
├── 平台UI适配
└── 系统集成
```

#### 1.2 expect/actual模式
```kotlin
// commonMain - 接口定义
expect class UnifyWatchManager() {
    val healthData: StateFlow<WatchHealthData>
    suspend fun requestHealthPermissions(): Boolean
}

// iosMain - iOS实现
actual class UnifyWatchManager {
    private val healthStore = HKHealthStore()
    
    actual suspend fun requestHealthPermissions(): Boolean {
        // HealthKit权限请求实现
    }
}
```

### 2. UI组件复用

#### 2.1 Compose Multiplatform组件
- **基础组件**: 按钮、文本、卡片等100%复用
- **布局组件**: 响应式布局，自动适配屏幕尺寸
- **主题系统**: 统一的Material Design主题
- **动画效果**: 跨平台一致的动画体验

#### 2.2 平台特定适配
```kotlin
@Composable
fun AdaptiveLayout(
    content: @Composable () -> Unit
) {
    when (LocalPlatform.current) {
        Platform.WatchOS -> {
            // 圆形屏幕布局
            CircularLayout { content() }
        }
        Platform.TvOS -> {
            // 大屏幕布局
            TVLayout { content() }
        }
        else -> {
            // 标准布局
            StandardLayout { content() }
        }
    }
}
```

## 开发工具链

### 1. 构建配置

#### 1.1 Gradle配置
```kotlin
kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val iosMain by creating {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("io.ktor:ktor-client-darwin:2.3.12")
            }
        }
    }
}
```

#### 1.2 Xcode项目配置
- **最低版本**: watchOS 9.0+, tvOS 16.0+
- **架构支持**: arm64 (真机), x86_64 (模拟器)
- **权限配置**: HealthKit, UserNotifications等
- **App Store配置**: 分发证书和描述文件

### 2. 调试和测试

#### 2.1 模拟器测试
- **watchOS模拟器**: 支持健康数据模拟
- **tvOS模拟器**: 支持遥控器模拟
- **跨设备测试**: iPhone + Apple Watch配对测试

#### 2.2 真机测试
- **开发者证书**: 配置开发团队和证书
- **设备注册**: 注册测试设备UDID
- **性能测试**: Instruments性能分析

## 部署策略

### 1. App Store发布

#### 1.1 应用配置
- **Bundle ID**: 独立的watchOS和tvOS Bundle ID
- **版本管理**: 与主应用同步版本号
- **元数据**: 应用描述、截图、关键词
- **分级**: 内容分级和隐私政策

#### 1.2 审核准备
- **功能完整性**: 确保所有功能正常工作
- **UI适配**: 符合Apple设计规范
- **性能优化**: 启动时间和响应速度
- **隐私合规**: 数据使用说明和权限申请

### 2. 企业分发

#### 2.1 内部测试
- **TestFlight**: Beta版本测试分发
- **Ad Hoc**: 内部设备直接安装
- **开发者预览**: 早期功能预览版本

#### 2.2 企业部署
- **企业证书**: Apple Developer Enterprise Program
- **MDM集成**: 移动设备管理系统
- **OTA分发**: 无线下载和安装

## 性能优化

### 1. watchOS优化

#### 1.1 电池优化
- **后台处理**: 最小化后台活动
- **传感器使用**: 按需启用传感器
- **网络请求**: 批量处理和缓存
- **UI更新**: 减少不必要的界面刷新

#### 1.2 内存管理
- **数据缓存**: 合理的缓存策略
- **图片优化**: 压缩和懒加载
- **对象池**: 重用常用对象
- **内存监控**: 实时内存使用监控

### 2. tvOS优化

#### 2.1 媒体性能
- **硬件解码**: 利用硬件加速
- **缓冲策略**: 智能预缓冲
- **分辨率适配**: 动态分辨率调整
- **HDR支持**: 高动态范围显示

#### 2.2 用户体验
- **焦点管理**: 流畅的焦点切换
- **响应速度**: 快速的用户交互响应
- **动画优化**: 60fps流畅动画
- **网络优化**: 智能网络请求管理

## 未来扩展

### 1. 新特性支持
- **watchOS 11**: 新的健康功能和UI组件
- **tvOS 18**: 增强的媒体播放和AI功能
- **Vision Pro**: 空间计算平台适配
- **CarPlay**: 车载系统集成

### 2. 技术演进
- **SwiftUI 6.0**: 最新UI框架特性
- **Compose跨平台**: 更好的原生集成
- **AI集成**: 机器学习和智能推荐
- **云服务**: iCloud同步和备份

## 总结

通过完整的watchOS和tvOS平台实现，Unify-Core项目实现了真正的全平台覆盖，为用户提供了一致且优质的跨设备体验。基于Kotlin Multiplatform的架构确保了代码复用和维护效率，同时通过平台特定的优化保证了原生级别的性能表现。
