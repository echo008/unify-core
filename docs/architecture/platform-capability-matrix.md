# Unify-Core 跨平台能力矩阵表

## 📊 平台支持概览

| 平台类别 | 平台名称 | 技术栈 | 状态 | 实现方式 | 特殊约束 |
|---------|---------|--------|------|----------|----------|
| **移动端** | Android | Kotlin/Compose | ✅ 完整 | Native Compose | API 21+ |
| | iOS | Kotlin/Compose | ✅ 完整 | Compose Multiplatform | iOS 11+ |
| | HarmonyOS | Kotlin/Compose | ✅ 完整 | JVM Target | HarmonyOS 2.0+ |
| **桌面端** | Windows | Kotlin/Compose | ✅ 完整 | Compose Desktop | JVM 17+ |
| | macOS | Kotlin/Compose | ✅ 完整 | Compose Desktop | macOS 10.14+ |
| | Linux | Kotlin/Compose | ✅ 完整 | Compose Desktop | 主流发行版 |
| **小程序** | 微信小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | 支付宝小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | 字节跳动小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | 百度小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | 快手小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | 小米小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | 华为小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| | QQ小程序 | Kotlin/JS + Bridge | ✅ 完整 | JS Target + Bridge | 小程序限制 |
| **穿戴设备** | Wear OS | Kotlin/Compose | ✅ 完整 | Android Target | Wear OS 2.0+ |
| | watchOS | Kotlin/Compose | ✅ 完整 | iOS Target | watchOS 6.0+ |
| | HarmonyOS穿戴 | Kotlin/Compose | ✅ 完整 | JVM Target | HarmonyOS 2.0+ |
| **电视端** | Android TV | Kotlin/Compose | ✅ 完整 | Android Target + TV | API 21+ |
| | tvOS | Kotlin/Compose | ✅ 完整 | iOS Target | tvOS 13.0+ |
| | HarmonyOS TV | Kotlin/Compose | ✅ 完整 | JVM Target | HarmonyOS 2.0+ |
| **Web端** | 现代浏览器 | Kotlin/JS + Compose | ✅ 完整 | JS Target | ES2015+ |

## 🎯 核心能力支持矩阵

### UI组件能力

| 组件类型 | Android | iOS | HarmonyOS | Desktop | 小程序 | 穿戴 | TV | Web |
|---------|---------|-----|-----------|---------|--------|------|----|----|
| 基础布局 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 文本组件 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 按钮组件 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 输入组件 | ✅ | ✅ | ✅ | ✅ | ⚠️ 受限 | ⚠️ 受限 | ✅ | ✅ |
| 图片组件 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 列表组件 | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ 受限 | ✅ | ✅ |
| 导航组件 | ✅ | ✅ | ✅ | ✅ | ⚠️ 受限 | ⚠️ 受限 | ✅ | ✅ |
| 媒体组件 | ✅ | ✅ | ✅ | ✅ | ⚠️ 受限 | ❌ | ✅ | ✅ |
| 图表组件 | ✅ | ✅ | ✅ | ✅ | ⚠️ 受限 | ❌ | ✅ | ✅ |
| 地图组件 | ✅ | ✅ | ✅ | ✅ | ⚠️ 受限 | ❌ | ❌ | ✅ |

### 系统能力支持

| 系统能力 | Android | iOS | HarmonyOS | Desktop | 小程序 | 穿戴 | TV | Web |
|---------|---------|-----|-----------|---------|--------|------|----|----|
| 文件系统 | ✅ | ✅ | ✅ | ✅ | ❌ | ⚠️ 受限 | ✅ | ⚠️ 受限 |
| 网络请求 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 本地存储 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 相机功能 | ✅ | ✅ | ✅ | ⚠️ 受限 | ⚠️ 受限 | ❌ | ❌ | ⚠️ 受限 |
| 位置服务 | ✅ | ✅ | ✅ | ⚠️ 受限 | ⚠️ 受限 | ✅ | ❌ | ⚠️ 受限 |
| 推送通知 | ✅ | ✅ | ✅ | ⚠️ 受限 | ⚠️ 受限 | ✅ | ⚠️ 受限 | ⚠️ 受限 |
| 传感器 | ✅ | ✅ | ✅ | ⚠️ 受限 | ❌ | ✅ | ❌ | ⚠️ 受限 |
| 蓝牙 | ✅ | ✅ | ✅ | ⚠️ 受限 | ❌ | ✅ | ❌ | ⚠️ 受限 |
| NFC | ✅ | ✅ | ✅ | ❌ | ❌ | ⚠️ 受限 | ❌ | ❌ |

### 性能特性

| 性能指标 | Android | iOS | HarmonyOS | Desktop | 小程序 | 穿戴 | TV | Web |
|---------|---------|-----|-----------|---------|--------|------|----|----|
| 启动时间 | < 500ms | < 500ms | < 500ms | < 800ms | < 1000ms | < 800ms | < 1000ms | < 1200ms |
| 内存占用 | < 100MB | < 80MB | < 120MB | < 200MB | < 50MB | < 30MB | < 150MB | < 100MB |
| 包体积 | < 20MB | < 15MB | < 25MB | < 50MB | < 2MB | < 10MB | < 30MB | < 5MB |
| 帧率 | 60fps | 60fps | 60fps | 60fps | 30fps | 30fps | 60fps | 60fps |
| 电池优化 | ✅ | ✅ | ✅ | N/A | ✅ | ✅ | N/A | N/A |

## 🔧 技术架构映射

### Kotlin Multiplatform目标配置

```kotlin
kotlin {
    // 移动端
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework { baseName = "shared" }
    }
    
    // 桌面端
    jvm("desktop")
    
    // HarmonyOS (通过JVM)
    jvm("harmony")
    
    // Web端
    js(IR) { browser() }
    
    // 穿戴设备
    jvm("watch")
    
    // TV端
    jvm("tv")
}
```

### 平台特定实现策略

| 平台 | 实现策略 | 关键技术 | 适配层 |
|------|----------|----------|--------|
| Android | Native Compose | Jetpack Compose | 无需适配 |
| iOS | Compose Multiplatform | Skia渲染 | iOS适配层 |
| HarmonyOS | JVM + Compose | JVM运行时 | HarmonyOS适配层 |
| Desktop | Compose Desktop | Skia渲染 | 桌面适配层 |
| 小程序 | JS + Bridge | Kotlin/JS编译 | 小程序桥接层 |
| 穿戴 | 平台继承 | 继承移动端 | 穿戴UI适配 |
| TV | 平台继承 | 继承移动端 | TV UI适配 |
| Web | Compose Web | DOM渲染 | Web适配层 |

## 📋 约束条件总结

### 技术约束
- **小程序平台**: 受限于各平台API限制，部分系统功能不可用
- **穿戴设备**: 屏幕尺寸和交互方式限制，UI组件需特殊适配
- **TV平台**: 遥控器交互，焦点导航系统
- **Web平台**: 浏览器安全策略限制部分系统API

### 性能约束
- **小程序**: 包体积严格限制（通常2MB以内）
- **穿戴设备**: 内存和电池限制严格
- **TV平台**: 需要优化大屏显示和远距离观看

### 开发约束
- **统一代码库**: 共享业务逻辑，平台特定UI适配
- **API一致性**: 提供统一的跨平台API接口
- **测试覆盖**: 需要在所有目标平台进行测试验证
