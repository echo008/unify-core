# TV 平台开发指南

## 📺 概述

Unify KMP 为智能电视和大型显示设备提供完整的开发支持，基于 Kotlin Multiplatform + Compose 实现，为大屏幕设备优化的用户界面和交互体验。

### 🎯 平台特性

#### 核心特性
- **大屏幕优化**: 专门为TV大屏幕优化的网格布局和导航
- **遥控器适配**: 完整的遥控器按键映射和焦点管理
- **语音控制**: 内置语音搜索和控制功能
- **多平台支持**: 支持Android TV、tvOS、HarmonyOS TV等

#### 技术栈
- **Kotlin**: 2.0.21
- **Compose Multiplatform**: 1.7.0
- **TV Compose**: 专为电视优化的Compose组件
- **Leanback**: Android TV UI框架
- **TVML**: Apple TV标记语言

## 🛠️ 环境要求

### 必需工具
- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **Android TV SDK**: API 34+
- **Xcode**: 15.0+ (tvOS开发)
- **DevEco Studio**: 4.0+ (HarmonyOS TV开发)

### 开发环境配置
```kotlin
// shared/build.gradle.kts
kotlin {
    // 添加TV目标平台
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
}
```

## 🏗️ 项目结构

### TV应用模块
```
tvApp/
├── src/
│   └── tvMain/
│       └── kotlin/
│           └── com/unify/tv/
│               ├── MainActivity.kt     # 主活动
│               ├── TvApp.kt            # 应用入口
│               ├── theme/              # 主题配置
│               └── screens/            # 页面组件
├── build.gradle.kts                   # 构建配置
└── AndroidManifest.xml                # 应用清单
```

## 🎨 核心组件

### 遥控器按键映射组件

```kotlin
@Composable
fun UnifyTVRemoteControl(
    modifier: Modifier = Modifier,
    onKeyPress: ((TVRemoteKey) -> Unit)? = null,
    onGesture: ((TVGesture) -> Unit)? = null
) {
    // 处理遥控器按键事件
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                when (keyEvent.key) {
                    Key.DirectionUp -> {
                        onKeyPress?.invoke(TVRemoteKey.UP)
                        true
                    }
                    Key.DirectionDown -> {
                        onKeyPress?.invoke(TVRemoteKey.DOWN)
                        true
                    }
                    Key.DirectionLeft -> {
                        onKeyPress?.invoke(TVRemoteKey.LEFT)
                        true
                    }
                    Key.DirectionRight -> {
                        onKeyPress?.invoke(TVRemoteKey.RIGHT)
                        true
                    }
                    Key.Enter -> {
                        onKeyPress?.invoke(TVRemoteKey.ENTER)
                        true
                    }
                    Key.Back -> {
                        onKeyPress?.invoke(TVRemoteKey.BACK)
                        true
                    }
                    else -> false
                }
            }
    ) {
        // TV界面内容
        Text(
            text = "使用遥控器方向键导航",
            modifier = Modifier.align(Alignment.Center)
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
```

### 焦点管理系统组件

```kotlin
@Composable
fun UnifyTVFocusManager(
    modifier: Modifier = Modifier,
    focusableItems: List<TVFocusableItem> = emptyList(),
    onFocusChanged: ((String) -> Unit)? = null
) {
    var focusedIndex by remember { mutableIntStateOf(0) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(focusableItems) { index, item ->
            val isFocused = index == focusedIndex

            Card(
                modifier = Modifier
                    .aspectRatio(16f/9f)
                    .border(
                        width = if (isFocused) 4.dp else 0.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        focusedIndex = index
                        onFocusChanged?.invoke(item.id)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isFocused)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 应用图标
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(48.dp),
                            tint = if (isFocused)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )

                        // 应用标题
                        Text(
                            text = item.title,
                            style = if (isFocused)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
```

### 网格菜单组件

```kotlin
@Composable
fun UnifyTVGridMenu(
    items: List<TVMenuItem>,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    onItemSelected: ((TVMenuItem) -> Unit)? = null
) {
    var selectedItem by remember { mutableStateOf<TVMenuItem?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(items) { item ->
            val isSelected = selectedItem == item

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
                    .clickable {
                        selectedItem = item
                        onItemSelected?.invoke(item)
                    }
                    .border(
                        width = if (isSelected) 4.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 2.dp
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 海报图片
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // 渐变遮罩
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    // 标题和描述
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.description.isNotEmpty()) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // 播放按钮（选中时显示）
                    if (isSelected) {
                        FloatingActionButton(
                            onClick = { onItemSelected?.invoke(item) },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "播放",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### 媒体播放器组件

```kotlin
@Composable
fun UnifyTVMediaPlayer(
    modifier: Modifier = Modifier,
    source: String = "",
    title: String = "",
    onPlay: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0) }
    var totalTime by remember { mutableLongStateOf(100) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 视频播放区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (source.isNotEmpty()) {
                // 这里应该集成实际的视频播放器
                Text(
                    text = "视频播放器\n$title",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "暂无视频源",
                    color = Color.Gray
                )
            }
        }

        // 控制面板
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 标题
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 进度条
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = currentTime.toFloat() / totalTime.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentTime),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = formatTime(totalTime),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // 控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* 上一集 */ }) {
                        Icon(Icons.Default.SkipPrevious, "上一集")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    FloatingActionButton(
                        onClick = {
                            if (isPlaying) {
                                onPause?.invoke()
                            } else {
                                onPlay?.invoke()
                            }
                            isPlaying = !isPlaying
                        }
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (isPlaying) "暂停" else "播放"
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = { /* 下一集 */ }) {
                        Icon(Icons.Default.SkipNext, "下一集")
                    }
                }
            }
        }
    }
}
```

## 🔧 平台特定配置

### Android TV 配置
```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:name=".TvApplication"
        android:banner="@drawable/tv_banner"
        android:icon="@drawable/tv_icon"
        android:label="@string/app_name">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Tv">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### tvOS 配置
```swift
// iOS TV 配置
struct TvApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## 📊 性能优化建议

### 大屏幕优化
- 使用网格布局充分利用屏幕空间
- 优化字体大小和间距
- 使用海报风格的视觉元素

### 导航优化
- 实现焦点管理和键盘导航
- 提供遥控器友好的交互方式
- 支持语音控制和搜索

### 内存优化
- 预加载当前可见的内容
- 实现内容分页加载
- 优化图片和媒体资源

### 响应式设计
- 适应不同分辨率的TV设备
- 支持横屏和竖屏布局
- 优化不同输入设备的交互

## 🔗 相关链接

- [Android TV 开发指南](https://developer.android.com/training/tv)
- [tvOS 开发文档](https://developer.apple.com/tvos/)
- [HarmonyOS TV 开发](https://developer.harmonyos.com/)
