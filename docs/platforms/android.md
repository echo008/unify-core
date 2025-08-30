# Android 平台开发指南

本指南详细介绍如何在 Unify KMP 框架中开发 Android 应用。

## 环境配置

### 必需工具

- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **Android SDK**: API 34 (Android 14)
- **Build Tools**: 34.0.0
- **NDK**: 25.1.8937393 (可选，用于原生库)

### Gradle 配置

Android 模块的 `build.gradle.kts` 配置：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.unify.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unify.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation(project(":shared"))
    
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    
    // Android Core
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel.ktx)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
```

## 项目结构

```
androidApp/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/unify/android/
│   │   │       └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── mipmap-*/
│   │   │       └── ic_launcher.png
│   │   └── AndroidManifest.xml
│   └── test/
│       └── kotlin/
└── build.gradle.kts
```

## 主要组件

### MainActivity

```kotlin
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

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.UnifyKMP"
        android:exported="true">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.UnifyKMP">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## 平台特定实现

### PlatformInfo Android 实现

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

### Android 特有功能

#### 权限管理

```kotlin
// AndroidManifest.xml 中添加权限
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

// 运行时权限检查
class PermissionHelper {
    fun checkNetworkPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

#### 生命周期管理

```kotlin
@Composable
fun AndroidLifecycleAware() {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // 应用进入前台
                }
                Lifecycle.Event.ON_STOP -> {
                    // 应用进入后台
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
```

## 构建和部署

### 本地构建

```bash
# 构建 Debug APK
./gradlew :androidApp:assembleDebug

# 构建 Release APK
./gradlew :androidApp:assembleRelease

# 安装到设备
./gradlew :androidApp:installDebug

# 运行测试
./gradlew :androidApp:testDebugUnitTest
```

### 签名配置

在 `androidApp/build.gradle.kts` 中配置签名：

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/release.keystore")
            storePassword = "your_store_password"
            keyAlias = "your_key_alias"
            keyPassword = "your_key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### ProGuard 配置

`proguard-rules.pro` 文件：

```proguard
# Keep Kotlin Multiplatform classes
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep shared module classes
-keep class com.unify.helloworld.** { *; }

# Koin
-keep class org.koin.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
```

## 性能优化

### 启动优化

```kotlin
class UnifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 延迟初始化非关键组件
        GlobalScope.launch(Dispatchers.IO) {
            initializeNonCriticalComponents()
        }
    }
    
    private suspend fun initializeNonCriticalComponents() {
        // 初始化分析工具、崩溃报告等
    }
}
```

### 内存优化

```kotlin
@Composable
fun OptimizedImageLoader(imageUrl: String) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
```

### 网络优化

```kotlin
// 使用 OkHttp 缓存
val cacheSize = 10 * 1024 * 1024L // 10 MB
val cache = Cache(context.cacheDir, cacheSize)

val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .addInterceptor { chain ->
        val request = chain.request()
        val cacheControl = CacheControl.Builder()
            .maxAge(5, TimeUnit.MINUTES)
            .build()
        
        chain.proceed(
            request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        )
    }
    .build()
```

## 调试和测试

### 日志输出

```kotlin
// Android 特定日志实现
actual fun logDebug(message: String) {
    Log.d("UnifyKMP", message)
}

actual fun logError(message: String, throwable: Throwable?) {
    Log.e("UnifyKMP", message, throwable)
}
```

### 单元测试

```kotlin
// androidApp/src/test/kotlin/com/unify/android/MainActivityTest.kt
class MainActivityTest {
    
    @Test
    fun testMainActivityCreation() {
        assertTrue(true, "测试通过")
    }
    
    @Test
    fun testPlatformInfo() {
        val platformName = PlatformInfo.getPlatformName()
        assertEquals("Android", platformName)
    }
}
```

### UI 测试

```kotlin
// androidApp/src/androidTest/kotlin/com/unify/android/HelloWorldTest.kt
@RunWith(AndroidJUnit4::class)
class HelloWorldTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testHelloWorldDisplay() {
        composeTestRule.setContent {
            HelloWorldApp("Android")
        }
        
        composeTestRule
            .onNodeWithText("Hello, Android!")
            .assertIsDisplayed()
    }
}
```

## 发布准备

### 版本管理

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### 应用图标

将应用图标放置在以下目录：
- `res/mipmap-mdpi/ic_launcher.png` (48x48)
- `res/mipmap-hdpi/ic_launcher.png` (72x72)
- `res/mipmap-xhdpi/ic_launcher.png` (96x96)
- `res/mipmap-xxhdpi/ic_launcher.png` (144x144)
- `res/mipmap-xxxhdpi/ic_launcher.png` (192x192)

### Google Play 发布

1. 生成签名的 APK 或 AAB
2. 在 Google Play Console 创建应用
3. 上传构建产物
4. 配置应用信息和截图
5. 提交审核

## 常见问题

### Q: 构建失败，提示找不到 Android SDK

**A**: 确保在 `local.properties` 中正确配置了 `sdk.dir` 路径：

```properties
sdk.dir=/path/to/android-sdk
```

### Q: Compose 预览不显示

**A**: 确保使用了正确的预览注解：

```kotlin
@Preview
@Composable
fun HelloWorldPreview() {
    HelloWorldApp("Android")
}
```

### Q: 应用启动崩溃

**A**: 检查 ProGuard 配置，确保没有混淆关键类：

```proguard
-keep class com.unify.helloworld.** { *; }
```

### Q: 网络请求失败

**A**: 检查网络权限和网络安全配置：

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 允许 HTTP 请求 (仅开发环境) -->
<application android:usesCleartextTraffic="true">
```

## 最佳实践

1. **使用 Compose Preview** 进行 UI 开发和调试
2. **合理配置 ProGuard** 规则，平衡包大小和功能完整性
3. **遵循 Material Design** 设计规范
4. **使用 Android Architecture Components** 管理生命周期
5. **实施适当的错误处理** 和用户反馈机制
6. **定期更新依赖** 保持安全性和兼容性
