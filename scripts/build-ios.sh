#!/bin/bash

# iOS应用构建脚本
# 构建Unify KMP框架的iOS Hello World示例应用

set -e

echo "🚀 开始构建iOS应用..."

# 检查环境
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ 错误: xcodebuild 未找到，请确保已安装Xcode"
    exit 1
fi

# 检查共享框架
echo "📦 构建共享框架..."
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# 进入iOS项目目录
cd iosApp

# 清理之前的构建
echo "🧹 清理之前的构建..."
xcodebuild clean -project iosApp.xcodeproj -scheme iosApp

# 构建iOS应用
echo "📱 构建iOS应用..."
xcodebuild build -project iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15,OS=17.0'

# 检查构建结果
if [ $? -eq 0 ]; then
    echo "✅ iOS应用构建成功!"
    echo "📱 可在Xcode中运行或使用模拟器测试"
else
    echo "❌ iOS应用构建失败!"
    exit 1
fi

cd ..
echo "🎉 iOS构建完成!"
