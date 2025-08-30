# Web 平台开发指南

## 🌐 概述

Unify KMP 通过 Kotlin/JS 编译器为 Web 平台提供完整支持，使用 Compose for Web 技术栈实现跨平台 UI 组件的 Web 渲染，同时支持现代 Web 开发工具链。

## 🛠️ 环境要求

### 必需工具
- **Node.js**: 18.0+ (推荐 LTS 版本)
- **npm**: 9.0+ 或 **yarn**: 1.22+
- **JDK**: 17 或更高版本
- **Kotlin**: 2.0.21+
- **现代浏览器**: Chrome 90+, Firefox 88+, Safari 14+

### 安装验证
```bash
# 检查 Node.js 版本
node --version

# 检查 npm 版本
npm --version

# 检查 JDK 版本
java -version

# 检查浏览器支持
npx browserslist
```

## 🏗️ 项目结构

### Web 应用模块
```
webApp/
├── src/
│   └── jsMain/
│       └── kotlin/
│           └── com/unify/web/
│               └── Main.kt           # Web 应用入口
├── build.gradle.kts                  # 构建配置
├── vite.config.js                    # Vite 配置
├── webpack.config.d/                 # Webpack 配置目录
│   └── webpack.config.js
├── package.json                      # npm 依赖
└── src/main/resources/               # 静态资源
    └── index.html                    # HTML 模板
```

### Gradle 配置
```kotlin
// webApp/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        moduleName = "unify-web-app"
        browser {
            commonWebpackConfig {
                outputFileName = "unify-web-app.js"
                devServer = devServer?.copy(
                    open = false,
                    port = 3000
                )
            }
            binaries.executable()
        }
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.web.core)
                implementation(compose.runtime)
            }
        }
    }
}
```

## 💻 核心组件实现

### 1. Web 应用入口 (Main.kt)
```kotlin
// webApp/src/jsMain/kotlin/com/unify/web/Main.kt
import androidx.compose.runtime.*
import com.unify.helloworld.HelloWorldApp
import com.unify.helloworld.PlatformInfo
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        Style(AppStyleSheet)
        
        Div({
            classes(AppStyleSheet.container)
        }) {
            WebHelloWorldApp()
        }
    }
}

@Composable
fun WebHelloWorldApp() {
    var count by remember { mutableStateOf(0) }
    
    Div({
        classes(AppStyleSheet.appContainer)
    }) {
        H1({
            classes(AppStyleSheet.title)
        }) {
            Text("Hello, Web!")
        }
        
        P({
            classes(AppStyleSheet.subtitle)
        }) {
            Text("Platform: ${PlatformInfo.getPlatformName()}")
        }
        
        P({
            classes(AppStyleSheet.info)
        }) {
            Text("Device: ${PlatformInfo.getDeviceInfo()}")
        }
        
        Div({
            classes(AppStyleSheet.counterContainer)
        }) {
            P({
                classes(AppStyleSheet.counter)
            }) {
                Text("Count: $count")
            }
            
            Button({
                classes(AppStyleSheet.primaryButton)
                onClick { count++ }
            }) {
                Text("Increment")
            }
            
            Button({
                classes(AppStyleSheet.secondaryButton)
                onClick { count = 0 }
            }) {
                Text("Reset")
            }
        }
    }
}
```

### 2. 样式定义 (AppStyleSheet.kt)
```kotlin
// webApp/src/jsMain/kotlin/com/unify/web/AppStyleSheet.kt
import org.jetbrains.compose.web.css.*

object AppStyleSheet : StyleSheet() {
    val container by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        minHeight(100.vh)
        fontFamily("SF Pro Display", "Helvetica", "Arial", "sans-serif")
        backgroundColor(Color("#f8f9fa"))
    }
    
    val appContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        padding(32.px)
        backgroundColor(Color.white)
        borderRadius(16.px)
        boxShadow("0 4px 6px rgba(0, 0, 0, 0.1)")
        maxWidth(400.px)
        width(100.percent)
    }
    
    val title by style {
        fontSize(2.5.em)
        fontWeight("bold")
        color(Color("#1a1a1a"))
        marginBottom(16.px)
        textAlign("center")
    }
    
    val subtitle by style {
        fontSize(1.2.em)
        color(Color("#666"))
        marginBottom(8.px)
    }
    
    val info by style {
        fontSize(0.9.em)
        color(Color("#888"))
        marginBottom(24.px)
    }
    
    val counterContainer by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        gap(16.px)
    }
    
    val counter by style {
        fontSize(1.5.em)
        fontWeight("semibold")
        color(Color("#333"))
        marginBottom(16.px)
    }
    
    val primaryButton by style {
        backgroundColor(Color("#007AFF"))
        color(Color.white)
        border("none")
        padding(12.px, 24.px)
        borderRadius(8.px)
        fontSize(1.1.em)
        fontWeight("500")
        cursor("pointer")
        transition("all 0.2s ease")
        
        hover(self) style {
            backgroundColor(Color("#0056CC"))
            transform { scale(1.05) }
        }
    }
    
    val secondaryButton by style {
        backgroundColor(Color.transparent)
        color(Color("#007AFF"))
        border("2px solid #007AFF")
        padding(10.px, 22.px)
        borderRadius(8.px)
        fontSize(1.em)
        fontWeight("500")
        cursor("pointer")
        transition("all 0.2s ease")
        
        hover(self) style {
            backgroundColor(Color("#007AFF"))
            color(Color.white)
        }
    }
}
```

## 🔧 平台特定实现

### PlatformInfo Web 实现
```kotlin
// shared/src/jsMain/kotlin/com/unify/helloworld/PlatformInfo.js.kt
import kotlinx.browser.window

actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "Web"
        
        actual fun getDeviceInfo(): String {
            val navigator = window.navigator
            return buildString {
                append("Browser: ${navigator.userAgent.substringBefore(" ")}")
                append("\nLanguage: ${navigator.language}")
                append("\nPlatform: ${navigator.platform}")
                append("\nScreen: ${window.screen.width}x${window.screen.height}")
            }
        }
    }
}
```

## 🚀 构建和运行

### 1. 开发模式
```bash
# 启动开发服务器
./gradlew :webApp:jsBrowserDevelopmentRun

# 或使用 Webpack Dev Server
./gradlew :webApp:jsBrowserDevelopmentWebpack

# 访问 http://localhost:8080
```

### 2. 生产构建
```bash
# 构建生产版本
./gradlew :webApp:jsBrowserProductionWebpack

# 构建产物位置
ls webApp/build/distributions/
```

### 3. 预览构建结果
```bash
# 使用简单 HTTP 服务器预览
cd webApp/build/distributions
python -m http.server 8000

# 或使用 Node.js serve
npx serve webApp/build/distributions
```

## 📦 依赖管理

### package.json 配置
```json
{
  "name": "unify-web-app",
  "version": "1.0.0",
  "description": "Unify KMP Web Application",
  "scripts": {
    "dev": "webpack serve --mode development",
    "build": "webpack --mode production",
    "preview": "serve dist"
  },
  "devDependencies": {
    "webpack": "^5.88.0",
    "webpack-cli": "^5.1.0",
    "webpack-dev-server": "^4.15.0",
    "html-webpack-plugin": "^5.5.0",
    "serve": "^14.2.0"
  }
}
```

### Webpack 配置 (webpack.config.js)
```javascript
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    mode: 'development',
    entry: './build/js/packages/unify-web-app/kotlin/unify-web-app.js',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'bundle.js',
        clean: true
    },
    devServer: {
        static: './dist',
        hot: true,
        port: 3000,
        open: true
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: './src/main/resources/index.html',
            title: 'Unify KMP Web App'
        })
    ],
    resolve: {
        modules: [
            path.resolve(__dirname, 'build/js/node_modules'),
            'node_modules'
        ]
    }
};
```

### Vite 配置 (vite.config.js)
```javascript
import { defineConfig } from 'vite'

export default defineConfig({
    root: 'src/main/resources',
    build: {
        outDir: '../../../build/distributions'
    },
    server: {
        port: 3000,
        open: true
    }
})
```

## 🎨 UI 开发指南

### 响应式设计
```kotlin
@Composable
fun ResponsiveLayout() {
    val windowWidth = remember { mutableStateOf(window.innerWidth) }
    
    DisposableEffect(Unit) {
        val listener = { _: Event ->
            windowWidth.value = window.innerWidth
        }
        window.addEventListener("resize", listener)
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }
    
    when {
        windowWidth.value < 768 -> MobileLayout()
        windowWidth.value < 1024 -> TabletLayout()
        else -> DesktopLayout()
    }
}
```

### 动画效果
```kotlin
val animationStyle by style {
    transition("all 0.3s cubic-bezier(0.4, 0, 0.2, 1)")
    
    hover(self) style {
        transform { 
            translateY((-2).px)
            scale(1.02)
        }
    }
}
```

### 主题系统
```kotlin
object WebTheme {
    object Colors {
        val primary = Color("#007AFF")
        val secondary = Color("#5856D6")
        val success = Color("#34C759")
        val warning = Color("#FF9500")
        val error = Color("#FF3B30")
        val background = Color("#F2F2F7")
        val surface = Color("#FFFFFF")
        val onSurface = Color("#1C1C1E")
    }
    
    object Typography {
        val h1 = CSSBuilder().apply {
            fontSize(2.5.em)
            fontWeight("bold")
            lineHeight(1.2)
        }
        
        val body1 = CSSBuilder().apply {
            fontSize(1.em)
            lineHeight(1.5)
        }
    }
}
```

## 🔍 调试和测试

### 浏览器调试
```kotlin
// 控制台输出
console.log("Debug message: $value")

// 性能监控
val startTime = Date.now()
// ... 执行代码
val endTime = Date.now()
console.log("Execution time: ${endTime - startTime}ms")
```

### 单元测试
```kotlin
// webApp/src/jsTest/kotlin/WebAppTest.kt
import kotlin.test.Test
import kotlin.test.assertEquals

class WebAppTest {
    @Test
    fun testPlatformInfo() {
        val platformName = PlatformInfo.getPlatformName()
        assertEquals("Web", platformName)
    }
    
    @Test
    fun testDeviceInfo() {
        val deviceInfo = PlatformInfo.getDeviceInfo()
        assertTrue(deviceInfo.contains("Browser:"))
    }
}
```

### E2E 测试
```javascript
// cypress/integration/app.spec.js
describe('Unify KMP Web App', () => {
    beforeEach(() => {
        cy.visit('http://localhost:3000')
    })
    
    it('should display hello message', () => {
        cy.contains('Hello, Web!')
    })
    
    it('should increment counter', () => {
        cy.contains('Count: 0')
        cy.contains('Increment').click()
        cy.contains('Count: 1')
    })
    
    it('should reset counter', () => {
        cy.contains('Increment').click()
        cy.contains('Reset').click()
        cy.contains('Count: 0')
    })
})
```

## 📱 PWA 支持

### Service Worker
```javascript
// public/sw.js
const CACHE_NAME = 'unify-kmp-v1';
const urlsToCache = [
    '/',
    '/bundle.js',
    '/styles.css'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(urlsToCache))
    );
});

self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => response || fetch(event.request))
    );
});
```

### Web App Manifest
```json
{
    "name": "Unify KMP Web App",
    "short_name": "UnifyKMP",
    "description": "Kotlin Multiplatform Web Application",
    "start_url": "/",
    "display": "standalone",
    "background_color": "#ffffff",
    "theme_color": "#007AFF",
    "icons": [
        {
            "src": "/icon-192.png",
            "sizes": "192x192",
            "type": "image/png"
        },
        {
            "src": "/icon-512.png",
            "sizes": "512x512",
            "type": "image/png"
        }
    ]
}
```

## 🚀 性能优化

### 代码分割
```kotlin
// 动态导入
suspend fun loadModule() {
    val module = js("import('./heavy-module.js')").await()
    // 使用模块
}
```

### 资源优化
```javascript
// webpack.config.js
module.exports = {
    optimization: {
        splitChunks: {
            chunks: 'all',
            cacheGroups: {
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendors',
                    chunks: 'all'
                }
            }
        }
    }
};
```

### 缓存策略
```kotlin
// 本地存储
fun saveToLocalStorage(key: String, value: String) {
    window.localStorage.setItem(key, value)
}

fun loadFromLocalStorage(key: String): String? {
    return window.localStorage.getItem(key)
}
```

## 🌐 部署指南

### 静态部署
```bash
# 构建生产版本
./gradlew :webApp:jsBrowserProductionWebpack

# 部署到 Netlify
netlify deploy --prod --dir=webApp/build/distributions

# 部署到 Vercel
vercel --prod webApp/build/distributions
```

### Docker 部署
```dockerfile
# Dockerfile
FROM nginx:alpine
COPY webApp/build/distributions /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## ❓ 常见问题

### Q: Kotlin/JS 编译失败
**A**: 检查 Node.js 版本，确保使用 18+ 版本，清理构建缓存：`./gradlew clean`。

### Q: 浏览器兼容性问题
**A**: 检查 browserslist 配置，添加必要的 polyfill。

### Q: 热重载不工作
**A**: 检查 webpack-dev-server 配置，确保端口没有被占用。

### Q: 样式不生效
**A**: 确保 StyleSheet 正确注册，检查 CSS 类名是否正确应用。

## 📚 参考资源

- [Compose for Web 官方文档](https://compose-web.ui.pages.jetbrains.team/)
- [Kotlin/JS 官方指南](https://kotlinlang.org/docs/js-overview.html)
- [Webpack 配置指南](https://webpack.js.org/configuration/)
- [MDN Web 开发文档](https://developer.mozilla.org/en-US/docs/Web)

---

通过本指南，您可以成功构建和部署 Unify KMP Web 应用，享受 Kotlin 跨平台开发在 Web 端的强大能力。
