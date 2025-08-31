# 计数器应用示例

## 📱 项目概述

计数器应用是一个简单而实用的跨平台示例，展示了 Unify KMP 框架的基础功能，包括状态管理、用户交互和响应式UI设计。

## 🎯 功能特性

- ✅ **基础计数功能** - 增加、减少、重置计数器
- ✅ **状态持久化** - 应用重启后保持计数状态
- ✅ **响应式UI** - 实时更新界面显示
- ✅ **Material Design** - 现代化的界面设计
- ✅ **跨平台支持** - Android、iOS、Web、桌面端完全一致

## 🏗️ 项目结构

```
counter-app/
├── shared/
│   └── src/
│       └── commonMain/
│           └── kotlin/
│               ├── CounterApp.kt          # 主应用组件
│               ├── CounterViewModel.kt    # 状态管理
│               └── CounterRepository.kt   # 数据持久化
├── androidApp/
├── iosApp/
├── webApp/
└── desktopApp/
```

## 💻 核心实现

### CounterApp.kt - 主应用组件

```kotlin
package com.unify.counter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CounterApp() {
    val viewModel = remember { CounterViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 标题
                Text(
                    text = "计数器应用",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                // 计数器显示卡片
                CounterDisplayCard(
                    count = uiState.count,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                // 控制按钮区域
                CounterControlButtons(
                    onIncrement = { viewModel.increment() },
                    onDecrement = { viewModel.decrement() },
                    onReset = { viewModel.reset() },
                    canDecrement = uiState.count > 0
                )
                
                // 统计信息
                CounterStats(
                    totalClicks = uiState.totalClicks,
                    maxValue = uiState.maxValue,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        }
    }
}

@Composable
fun CounterDisplayCard(
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun CounterControlButtons(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
    canDecrement: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 减少按钮
        FloatingActionButton(
            onClick = onDecrement,
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "减少",
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
        
        // 重置按钮
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.padding(horizontal = 8.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("重置")
        }
        
        // 增加按钮
        FloatingActionButton(
            onClick = onIncrement,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "增加",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun CounterStats(
    totalClicks: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总点击次数",
                    value = totalClicks.toString()
                )
                
                StatItem(
                    label = "历史最大值",
                    value = maxValue.toString()
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### CounterViewModel.kt - 状态管理

```kotlin
package com.unify.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CounterUiState(
    val count: Int = 0,
    val totalClicks: Int = 0,
    val maxValue: Int = 0,
    val isLoading: Boolean = false
)

class CounterViewModel : ViewModel() {
    private val repository = CounterRepository()
    
    private val _uiState = MutableStateFlow(CounterUiState())
    val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()
    
    init {
        loadCounterState()
    }
    
    fun increment() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newCount = currentState.count + 1
            val newMaxValue = maxOf(currentState.maxValue, newCount)
            
            val newState = currentState.copy(
                count = newCount,
                totalClicks = currentState.totalClicks + 1,
                maxValue = newMaxValue
            )
            
            _uiState.value = newState
            saveCounterState(newState)
        }
    }
    
    fun decrement() {
        if (_uiState.value.count > 0) {
            viewModelScope.launch {
                val currentState = _uiState.value
                val newState = currentState.copy(
                    count = currentState.count - 1,
                    totalClicks = currentState.totalClicks + 1
                )
                
                _uiState.value = newState
                saveCounterState(newState)
            }
        }
    }
    
    fun reset() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newState = currentState.copy(
                count = 0,
                totalClicks = currentState.totalClicks + 1
            )
            
            _uiState.value = newState
            saveCounterState(newState)
        }
    }
    
    private fun loadCounterState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val savedState = repository.loadCounterState()
                _uiState.value = savedState.copy(isLoading = false)
            } catch (e: Exception) {
                Logger.e("CounterViewModel", "加载计数器状态失败", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun saveCounterState(state: CounterUiState) {
        viewModelScope.launch {
            try {
                repository.saveCounterState(state)
            } catch (e: Exception) {
                Logger.e("CounterViewModel", "保存计数器状态失败", e)
            }
        }
    }
}
```

### CounterRepository.kt - 数据持久化

```kotlin
package com.unify.counter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class CounterData(
    val count: Int = 0,
    val totalClicks: Int = 0,
    val maxValue: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

class CounterRepository {
    private val storage = KeyValueStorage()
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    companion object {
        private const val COUNTER_DATA_KEY = "counter_data"
    }
    
    suspend fun saveCounterState(state: CounterUiState) = withContext(Dispatchers.IO) {
        val counterData = CounterData(
            count = state.count,
            totalClicks = state.totalClicks,
            maxValue = state.maxValue,
            lastUpdated = System.currentTimeMillis()
        )
        
        val jsonString = json.encodeToString(counterData)
        storage.putString(COUNTER_DATA_KEY, jsonString)
        
        Logger.d("CounterRepository", "计数器状态已保存: $counterData")
    }
    
    suspend fun loadCounterState(): CounterUiState = withContext(Dispatchers.IO) {
        val jsonString = storage.getString(COUNTER_DATA_KEY)
        
        if (jsonString != null) {
            try {
                val counterData = json.decodeFromString<CounterData>(jsonString)
                Logger.d("CounterRepository", "计数器状态已加载: $counterData")
                
                CounterUiState(
                    count = counterData.count,
                    totalClicks = counterData.totalClicks,
                    maxValue = counterData.maxValue
                )
            } catch (e: Exception) {
                Logger.e("CounterRepository", "解析计数器数据失败", e)
                CounterUiState()
            }
        } else {
            Logger.d("CounterRepository", "未找到保存的计数器数据，使用默认状态")
            CounterUiState()
        }
    }
    
    suspend fun clearCounterData() = withContext(Dispatchers.IO) {
        storage.remove(COUNTER_DATA_KEY)
        Logger.d("CounterRepository", "计数器数据已清除")
    }
}
```

## 🎨 主题定制

### 自定义颜色主题

```kotlin
@Composable
fun CounterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC6),
        tertiary = Color(0xFF018786),
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        primaryContainer = Color(0xFFEADDFF),
        secondaryContainer = Color(0xFFCCF8F3)
    )
    
    val darkColors = darkColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        tertiary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF121212),
        primaryContainer = Color(0xFF4F378B),
        secondaryContainer = Color(0xFF005B57)
    )
    
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        content = content
    )
}
```

## 📱 平台特定实现

### Android 实现

```kotlin
// androidApp/src/main/java/MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CounterTheme {
                CounterApp()
            }
        }
    }
}
```

### iOS 实现

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
        MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### Web 实现

```kotlin
// webApp/src/jsMain/kotlin/main.kt
import androidx.compose.ui.window.Window
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        Window("计数器应用") {
            CounterTheme {
                CounterApp()
            }
        }
    }
}
```

### 桌面端实现

```kotlin
// desktopApp/src/jvmMain/kotlin/main.kt
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "计数器应用",
        state = rememberWindowState(width = 400.dp, height = 600.dp)
    ) {
        CounterTheme {
            CounterApp()
        }
    }
}
```

## 🧪 测试实现

### 单元测试

```kotlin
// shared/src/commonTest/kotlin/CounterViewModelTest.kt
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class CounterViewModelTest {
    private lateinit var viewModel: CounterViewModel
    
    @BeforeTest
    fun setup() {
        viewModel = CounterViewModel()
    }
    
    @Test
    fun `初始状态应该为零`() {
        assertEquals(0, viewModel.uiState.value.count)
        assertEquals(0, viewModel.uiState.value.totalClicks)
        assertEquals(0, viewModel.uiState.value.maxValue)
    }
    
    @Test
    fun `增加计数应该正确更新状态`() = runTest {
        viewModel.increment()
        
        val state = viewModel.uiState.value
        assertEquals(1, state.count)
        assertEquals(1, state.totalClicks)
        assertEquals(1, state.maxValue)
    }
    
    @Test
    fun `减少计数不应该低于零`() = runTest {
        viewModel.decrement()
        
        val state = viewModel.uiState.value
        assertEquals(0, state.count)
        assertEquals(0, state.totalClicks)
    }
    
    @Test
    fun `重置应该将计数归零但保持统计`() = runTest {
        // 先增加几次
        repeat(5) { viewModel.increment() }
        
        // 然后重置
        viewModel.reset()
        
        val state = viewModel.uiState.value
        assertEquals(0, state.count)
        assertEquals(6, state.totalClicks) // 5次增加 + 1次重置
        assertEquals(5, state.maxValue) // 历史最大值保持不变
    }
}
```

### UI 测试

```kotlin
// shared/src/commonTest/kotlin/CounterAppTest.kt
import androidx.compose.ui.test.*
import kotlin.test.Test

class CounterAppTest {
    @Test
    fun `点击增加按钮应该增加计数`() {
        composeTestRule.setContent {
            CounterApp()
        }
        
        // 点击增加按钮
        composeTestRule.onNodeWithContentDescription("增加").performClick()
        
        // 验证计数显示为1
        composeTestRule.onNodeWithText("1").assertExists()
    }
    
    @Test
    fun `点击重置按钮应该重置计数`() {
        composeTestRule.setContent {
            CounterApp()
        }
        
        // 先增加几次
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("增加").performClick()
        }
        
        // 点击重置按钮
        composeTestRule.onNodeWithText("重置").performClick()
        
        // 验证计数重置为0
        composeTestRule.onNodeWithText("0").assertExists()
    }
}
```

## 🚀 构建和运行

### Gradle 配置

```kotlin
// shared/build.gradle.kts
kotlin {
    androidTarget()
    
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
    
    js(IR) {
        browser()
    }
    
    jvm("desktop")
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.lifecycle.viewmodel)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
```

### 运行命令

```bash
# Android
./gradlew :androidApp:installDebug

# iOS (需要 Xcode)
./gradlew :iosApp:iosDeployIPhone15Debug

# Web
./gradlew :webApp:jsBrowserDevelopmentRun

# 桌面端
./gradlew :desktopApp:run
```

## 📈 性能优化

### 状态优化

```kotlin
class OptimizedCounterViewModel : ViewModel() {
    // 使用 StateFlow 的 distinctUntilChanged 避免不必要的重组
    val count = _uiState
        .map { it.count }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    // 防抖保存，避免频繁写入存储
    private val saveJob = MutableSharedFlow<CounterUiState>()
    
    init {
        saveJob
            .debounce(500) // 500ms 防抖
            .onEach { state ->
                repository.saveCounterState(state)
            }
            .launchIn(viewModelScope)
    }
}
```

## 🔧 扩展功能

### 添加动画效果

```kotlin
@Composable
fun AnimatedCounterDisplay(
    count: Int,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val scale by animateFloatAsState(
        targetValue = if (count > 0) 1.1f else 1f,
        animationSpec = tween(200)
    )
    
    Card(
        modifier = modifier
            .size(200.dp)
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = animatedCount.toString(),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

## 📚 学习要点

1. **状态管理** - 使用 ViewModel 和 StateFlow 管理应用状态
2. **数据持久化** - 使用 KeyValueStorage 保存应用数据
3. **响应式UI** - Compose 的声明式UI编程
4. **跨平台架构** - 共享业务逻辑，平台特定UI
5. **测试策略** - 单元测试和UI测试的最佳实践

---

这个计数器应用示例展示了 Unify KMP 框架的核心功能，是学习跨平台开发的绝佳起点。通过这个示例，您可以掌握状态管理、数据持久化和跨平台UI开发的基础知识。
