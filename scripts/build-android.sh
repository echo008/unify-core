#!/bin/bash

# Android应用构建脚本
# 构建Unify KMP框架的Android Hello World示例应用

set -e

echo "🚀 开始构建Android应用..."

# 检查环境
if ! command -v ./gradlew &> /dev/null; then
    echo "❌ 错误: gradlew 未找到"
    exit 1
fi

# 清理之前的构建
echo "🧹 清理之前的构建..."
./gradlew clean

# 构建共享模块
echo "📦 构建共享模块..."
./gradlew :shared:build

# 构建Android应用
echo "🤖 构建Android应用..."
./gradlew :androidApp:assembleDebug

# 检查构建结果
APK_PATH="androidApp/build/outputs/apk/debug/androidApp-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "✅ Android应用构建成功!"
    echo "📱 APK位置: $APK_PATH"
    
    # 显示APK信息
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo "📊 APK大小: $APK_SIZE"
else
    echo "❌ Android应用构建失败!"
    exit 1
fi

echo "🎉 Android构建完成!"
