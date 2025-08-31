# Watch 平台开发指南

## 🕐 概述

Unify KMP 为智能手表和可穿戴设备提供完整的开发支持，基于 Kotlin Multiplatform + Compose 实现，为小屏幕设备优化的用户界面和功能。

### 🎯 平台特性

#### 核心特性
- **小屏幕优化**: 专门为圆形/方形小屏幕优化的UI组件
- **可穿戴功能**: 健康监测、运动追踪、触觉反馈等原生功能
- **低功耗设计**: 针对可穿戴设备的性能和功耗优化
- **多平台支持**: 支持Wear OS、watchOS、HarmonyOS穿戴等

#### 技术栈
- **Kotlin**: 2.0.21
- **Compose Multiplatform**: 1.7.0
- **Wear Compose**: 专为可穿戴设备优化的Compose组件
- **Health Connect**: Android健康数据集成
- **HealthKit**: iOS健康数据集成

## 🛠️ 环境要求

### 必需工具
- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **Wear OS SDK**: API 33+
- **Xcode**: 15.0+ (iOS手表开发)
- **DevEco Studio**: 4.0+ (HarmonyOS穿戴开发)

### 开发环境配置
```kotlin
// shared/build.gradle.kts
kotlin {
    // 添加Watch目标平台
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
}
```

## 🏗️ 项目结构

### Watch应用模块
```
wearApp/
├── src/
│   └── watchMain/
│       └── kotlin/
│           └── com/unify/wear/
│               ├── MainActivity.kt     # 主活动
│               ├── WearApp.kt          # 应用入口
│               ├── theme/              # 主题配置
│               └── screens/            # 页面组件
├── build.gradle.kts                   # 构建配置
└── AndroidManifest.xml                # 应用清单
```

## 🎨 核心组件

### 智能手表表盘组件

```kotlin
@Composable
fun UnifyWatchFace(
    modifier: Modifier = Modifier,
    time: Long = System.currentTimeMillis(),
    style: WatchFaceStyle = WatchFaceStyle.CLASSIC
) {
    // 实现圆形表盘布局
    Box(
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // 时针、分针、秒针
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time

        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // 绘制表盘刻度
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 绘制刻度线
            for (i in 0..11) {
                val angle = i * 30f
                val startRadius = size.minDimension / 2 - 20
                val endRadius = size.minDimension / 2 - 10

                val startX = center.x + cos(angle.toRadians()) * startRadius
                val startY = center.y + sin(angle.toRadians()) * startRadius
                val endX = center.x + cos(angle.toRadians()) * endRadius
                val endY = center.y + sin(angle.toRadians()) * endRadius

                drawLine(
                    color = Color.White,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )
            }

            // 绘制时针
            val hourAngle = (hour % 12) * 30f + minute * 0.5f
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    x = center.x + cos(hourAngle.toRadians()) * (size.minDimension / 4),
                    y = center.y + sin(hourAngle.toRadians()) * (size.minDimension / 4)
                ),
                strokeWidth = 4f
            )

            // 绘制分针
            val minuteAngle = minute * 6f
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(
                    x = center.x + cos(minuteAngle.toRadians()) * (size.minDimension / 3),
                    y = center.y + sin(minuteAngle.toRadians()) * (size.minDimension / 3)
                ),
                strokeWidth = 3f
            )

            // 绘制秒针
            val secondAngle = second * 6f
            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(
                    x = center.x + cos(secondAngle.toRadians()) * (size.minDimension / 2.5).toFloat(),
                    y = center.y + sin(secondAngle.toRadians()) * (size.minDimension / 2.5).toFloat()
                ),
                strokeWidth = 1f
            )
        }

        // 显示数字时间
        Text(
            text = String.format("%02d:%02d", hour, minute),
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

### 健康数据监控组件

```kotlin
@Composable
fun UnifyHealthMonitor(
    modifier: Modifier = Modifier,
    healthData: List<UnifyHealthData> = emptyList(),
    onDataUpdate: ((UnifyHealthData) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(healthData) { data ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = data.type.displayName,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${data.value} ${data.unit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = data.type.icon,
                        contentDescription = null,
                        tint = data.type.color
                    )
                }
            }
        }
    }
}
```

### 运动追踪组件

```kotlin
@Composable
fun UnifyWorkoutTracker(
    workoutType: UnifyWorkoutType,
    modifier: Modifier = Modifier,
    onWorkoutUpdate: ((UnifyWorkoutData) -> Unit)? = null,
    onComplete: (() -> Unit)? = null
) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0) }
    var currentHeartRate by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 运动类型显示
        Text(
            text = workoutType.displayName,
            style = MaterialTheme.typography.headlineSmall
        )

        // 时间显示
        Text(
            text = formatElapsedTime(elapsedTime),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )

        // 心率显示
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "心率",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$currentHeartRate BPM",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // 控制按钮
        Button(
            onClick = {
                if (isRunning) {
                    // 停止运动
                    onComplete?.invoke()
                } else {
                    // 开始运动
                    // 启动计时器和传感器
                }
                isRunning = !isRunning
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRunning) "停止运动" else "开始运动")
        }
    }
}
```

## 🔧 平台特定配置

### Wear OS 配置
```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-feature android:name="android.hardware.type.watch" />

    <application>
        <uses-library
            android:name="com.google.android.wearable"
            android:required="false" />

        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="true" />
    </application>
</manifest>
```

### watchOS 配置
```swift
// iOS 配置
struct WatchApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## 📊 性能优化建议

### 内存优化
- 使用 `remember` 缓存计算结果
- 避免在Composable中创建大量对象
- 合理使用 `LaunchedEffect` 和 `DisposableEffect`

### 功耗优化
- 减少不必要的重绘
- 使用低频率更新传感器数据
- 优化动画和过渡效果

### UI优化
- 使用圆形布局适应手表屏幕
- 简化视觉元素，突出重要信息
- 优化触摸目标大小

## 🔗 相关链接

- [Wear OS 官方文档](https://developer.android.com/training/wearables)
- [watchOS 开发指南](https://developer.apple.com/watchos/)
- [HarmonyOS 穿戴开发](https://developer.harmonyos.com/)
