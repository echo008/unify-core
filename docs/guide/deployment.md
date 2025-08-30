# 部署指南

## 🚀 部署概述

Unify KMP 支持多种部署方式，从开发环境的本地部署到生产环境的自动化 CI/CD 流程。本指南将详细介绍各平台的部署策略和最佳实践。

## 📱 Android 部署

### 开发环境部署
```bash
# 构建调试版本
./gradlew :androidApp:assembleDebug

# 安装到设备
./gradlew :androidApp:installDebug

# 生成 APK 文件
ls androidApp/build/outputs/apk/debug/
```

### 生产环境部署
```bash
# 1. 配置签名
# 创建 keystore
keytool -genkey -v -keystore release-key.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000

# 2. 配置 gradle
# android/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            keyAlias = "release"
            keyPassword = "your_key_password"
            storeFile = file("../release-key.keystore")
            storePassword = "your_store_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

# 3. 构建发布版本
./gradlew :androidApp:assembleRelease

# 4. 上传到 Google Play Console
# 使用 Google Play Console 或 fastlane
```

### ProGuard 配置
```proguard
# androidApp/proguard-rules.pro
-keep class com.unify.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class io.ktor.** { *; }

# Compose 相关
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# 保留序列化类
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *;
}
```

## 🍎 iOS 部署

### 开发环境部署
```bash
# 1. 构建 Framework
./gradlew :shared:assembleXCFramework

# 2. 在 Xcode 中运行
open iosApp/iosApp.xcodeproj

# 3. 选择目标设备并运行
# Product -> Run (⌘R)
```

### App Store 部署
```bash
# 1. 配置证书和 Provisioning Profile
# 在 Apple Developer Portal 创建证书
# 在 Xcode 中配置 Signing & Capabilities

# 2. 构建 Archive
# Xcode -> Product -> Archive

# 3. 上传到 App Store Connect
# Xcode -> Window -> Organizer -> Distribute App

# 4. 或使用命令行
xcodebuild -workspace iosApp.xcworkspace \
           -scheme iosApp \
           -configuration Release \
           -archivePath iosApp.xcarchive \
           archive

xcodebuild -exportArchive \
           -archivePath iosApp.xcarchive \
           -exportPath . \
           -exportOptionsPlist ExportOptions.plist
```

### Fastlane 自动化
```ruby
# fastlane/Fastfile
default_platform(:ios)

platform :ios do
  desc "Build and upload to TestFlight"
  lane :beta do
    # 构建 shared framework
    sh("cd .. && ./gradlew :shared:assembleXCFramework")
    
    # 增加构建号
    increment_build_number(xcodeproj: "iosApp.xcodeproj")
    
    # 构建应用
    build_app(
      workspace: "iosApp.xcworkspace",
      scheme: "iosApp",
      configuration: "Release"
    )
    
    # 上传到 TestFlight
    upload_to_testflight(
      skip_waiting_for_build_processing: true
    )
  end
  
  desc "Release to App Store"
  lane :release do
    beta
    upload_to_app_store(
      force: true,
      reject_if_possible: true,
      skip_metadata: false,
      skip_screenshots: false,
      submit_for_review: true
    )
  end
end
```

## 🌐 Web 部署

### 静态网站部署
```bash
# 1. 构建生产版本
./gradlew :webApp:jsBrowserProductionWebpack

# 2. 部署到 Netlify
netlify deploy --prod --dir=webApp/build/distributions

# 3. 部署到 Vercel
vercel --prod webApp/build/distributions

# 4. 部署到 GitHub Pages
# 使用 GitHub Actions (见下文)
```

### Docker 部署
```dockerfile
# Dockerfile
FROM nginx:alpine

# 复制构建产物
COPY webApp/build/distributions /usr/share/nginx/html

# 自定义 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

```nginx
# nginx.conf
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    
    server {
        listen 80;
        server_name localhost;
        
        root /usr/share/nginx/html;
        index index.html;
        
        # SPA 路由支持
        location / {
            try_files $uri $uri/ /index.html;
        }
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # Gzip 压缩
        gzip on;
        gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    }
}
```

### CDN 部署
```bash
# AWS S3 + CloudFront
aws s3 sync webApp/build/distributions s3://your-bucket-name --delete
aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"

# 阿里云 OSS + CDN
ossutil cp -r webApp/build/distributions oss://your-bucket-name/ --update
```

## 🖥️ 桌面端部署

### 本地构建
```bash
# 构建可执行 JAR
./gradlew :desktopApp:packageUberJarForCurrentOS

# JAR 文件位置
ls desktopApp/build/compose/jars/
```

### 原生安装包
```bash
# Windows MSI
./gradlew :desktopApp:packageMsi

# macOS DMG
./gradlew :desktopApp:packageDmg

# Linux DEB
./gradlew :desktopApp:packageDeb

# 所有平台
./gradlew :desktopApp:packageDistributionForCurrentOS
```

### 应用商店分发
```kotlin
// build.gradle.kts
compose.desktop {
    application {
        nativeDistributions {
            // Windows Store
            windows {
                packageName = "Unify KMP Desktop"
                packageVersion = "1.0.0"
                msiPackageVersion = "1.0.0"
                exePackageVersion = "1.0.0"
            }
            
            // Mac App Store
            macOS {
                packageName = "Unify KMP Desktop"
                packageVersion = "1.0.0"
                dmgPackageVersion = "1.0.0"
                pkgPackageVersion = "1.0.0"
                
                signing {
                    sign.set(true)
                    identity.set("Developer ID Application: Your Name")
                }
            }
            
            // Linux 软件仓库
            linux {
                packageName = "unify-kmp-desktop"
                debPackageVersion = "1.0.0"
                rpmPackageVersion = "1.0.0"
            }
        }
    }
}
```

## 🔥 HarmonyOS 部署

### 开发环境部署
```bash
# 在 DevEco Studio 中
# 1. 连接 HarmonyOS 设备
# 2. 点击 Run 按钮
# 3. 选择目标设备

# 命令行构建
hvigor assembleHap --mode debug
```

### 应用市场发布
```bash
# 1. 构建发布版本
hvigor assembleHap --mode release

# 2. 签名应用
# 在 DevEco Studio 中配置签名信息

# 3. 上传到华为应用市场
# 使用华为开发者联盟控制台
```

## 📱 小程序部署

### 开发版本
```bash
# 在微信开发者工具中
# 1. 点击预览按钮
# 2. 扫描二维码在手机上预览
```

### 正式发布
```bash
# 1. 上传代码
# 在微信开发者工具中点击上传

# 2. 提交审核
# 在微信公众平台提交审核

# 3. 发布上线
# 审核通过后点击发布
```

## 🔄 CI/CD 自动化部署

### GitHub Actions 完整流程
```yaml
# .github/workflows/deploy.yml
name: Deploy to All Platforms

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Android SDK
      uses: android-actions/setup-android@v3
    
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
    
    - name: Build Android Release
      run: ./gradlew :androidApp:assembleRelease
    
    - name: Upload Android APK
      uses: actions/upload-artifact@v3
      with:
        name: android-apk
        path: androidApp/build/outputs/apk/release/*.apk

  build-ios:
    runs-on: macos-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Xcode
      uses: maxim-lobanov/setup-xcode@v1
      with:
        xcode-version: latest-stable
    
    - name: Build Shared Framework
      run: ./gradlew :shared:assembleXCFramework
    
    - name: Build iOS App
      run: |
        xcodebuild -project iosApp/iosApp.xcodeproj \
                   -scheme iosApp \
                   -configuration Release \
                   -archivePath iosApp.xcarchive \
                   archive
    
    - name: Export IPA
      run: |
        xcodebuild -exportArchive \
                   -archivePath iosApp.xcarchive \
                   -exportPath . \
                   -exportOptionsPlist iosApp/ExportOptions.plist
    
    - name: Upload iOS IPA
      uses: actions/upload-artifact@v3
      with:
        name: ios-ipa
        path: "*.ipa"

  build-web:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '18'
    
    - name: Build Web App
      run: ./gradlew :webApp:jsBrowserProductionWebpack
    
    - name: Deploy to Netlify
      uses: nwtgck/actions-netlify@v2
      with:
        publish-dir: './webApp/build/distributions'
        production-branch: main
        github-token: ${{ secrets.GITHUB_TOKEN }}
        deploy-message: "Deploy from GitHub Actions"
      env:
        NETLIFY_AUTH_TOKEN: ${{ secrets.NETLIFY_AUTH_TOKEN }}
        NETLIFY_SITE_ID: ${{ secrets.NETLIFY_SITE_ID }}

  build-desktop:
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
    runs-on: ${{ matrix.os }}
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build Desktop App
      run: ./gradlew :desktopApp:packageDistributionForCurrentOS
    
    - name: Upload Desktop Packages
      uses: actions/upload-artifact@v3
      with:
        name: desktop-${{ matrix.os }}
        path: desktopApp/build/compose/binaries/main/**/*

  create-release:
    needs: [build-android, build-ios, build-web, build-desktop]
    runs-on: ubuntu-latest
    steps:
    - name: Download all artifacts
      uses: actions/download-artifact@v3
    
    - name: Create Release
      uses: softprops/action-gh-release@v1
      with:
        files: |
          android-apk/*.apk
          ios-ipa/*.ipa
          desktop-*/**/*
        generate_release_notes: true
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Docker Compose 部署
```yaml
# docker-compose.yml
version: '3.8'

services:
  web-app:
    build:
      context: .
      dockerfile: Dockerfile.web
    ports:
      - "80:80"
    environment:
      - NODE_ENV=production
    restart: unless-stopped
  
  api-server:
    build:
      context: .
      dockerfile: Dockerfile.api
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/unify
    depends_on:
      - db
    restart: unless-stopped
  
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=unify
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

## 🌍 多环境部署策略

### 环境配置
```kotlin
// shared/src/commonMain/kotlin/Config.kt
object Config {
    val apiBaseUrl: String = when (BuildConfig.BUILD_TYPE) {
        "debug" -> "https://dev-api.unify-kmp.com"
        "staging" -> "https://staging-api.unify-kmp.com"
        "release" -> "https://api.unify-kmp.com"
        else -> "https://dev-api.unify-kmp.com"
    }
    
    val enableLogging: Boolean = BuildConfig.BUILD_TYPE != "release"
    
    val crashReportingEnabled: Boolean = BuildConfig.BUILD_TYPE == "release"
}
```

### 部署脚本
```bash
#!/bin/bash
# deploy.sh

set -e

ENVIRONMENT=${1:-staging}
VERSION=${2:-latest}

echo "Deploying to $ENVIRONMENT environment, version $VERSION"

case $ENVIRONMENT in
  "dev")
    echo "Deploying to development environment"
    ./gradlew clean build -Penv=dev
    ;;
  "staging")
    echo "Deploying to staging environment"
    ./gradlew clean build -Penv=staging
    ;;
  "prod")
    echo "Deploying to production environment"
    ./gradlew clean build -Penv=prod
    ;;
  *)
    echo "Unknown environment: $ENVIRONMENT"
    exit 1
    ;;
esac

echo "Deployment completed successfully!"
```

## 📊 部署监控

### 健康检查
```kotlin
// 应用健康检查端点
@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to System.currentTimeMillis(),
            "version" to BuildConfig.VERSION_NAME,
            "environment" to BuildConfig.BUILD_TYPE
        )
    }
    
    @GetMapping("/metrics")
    fun metrics(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        return mapOf(
            "memory" to mapOf(
                "total" to runtime.totalMemory(),
                "free" to runtime.freeMemory(),
                "used" to runtime.totalMemory() - runtime.freeMemory(),
                "max" to runtime.maxMemory()
            ),
            "threads" to Thread.activeCount(),
            "uptime" to ManagementFactory.getRuntimeMXBean().uptime
        )
    }
}
```

### 部署回滚策略
```bash
#!/bin/bash
# rollback.sh

PREVIOUS_VERSION=${1}

if [ -z "$PREVIOUS_VERSION" ]; then
    echo "Usage: ./rollback.sh <previous_version>"
    exit 1
fi

echo "Rolling back to version $PREVIOUS_VERSION"

# 停止当前服务
docker-compose down

# 切换到之前的版本
git checkout $PREVIOUS_VERSION

# 重新构建和部署
docker-compose up -d --build

echo "Rollback to $PREVIOUS_VERSION completed"
```

## 🔐 安全部署考虑

### 密钥管理
```bash
# 使用环境变量管理敏感信息
export API_KEY="your-secret-api-key"
export DATABASE_PASSWORD="your-db-password"

# 或使用密钥管理服务
# AWS Secrets Manager
# Azure Key Vault
# HashiCorp Vault
```

### HTTPS 配置
```nginx
# nginx SSL 配置
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    
    # 安全头
    add_header Strict-Transport-Security "max-age=63072000" always;
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header Referrer-Policy "strict-origin-when-cross-origin";
}
```

---

通过本部署指南，您可以将 Unify KMP 应用成功部署到各个平台，建立完整的 CI/CD 流程，确保应用的稳定性和安全性。
