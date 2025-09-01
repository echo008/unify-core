#!/bin/bash

# HarmonyOS应用构建脚本
# 构建Unify KMP框架的HarmonyOS Hello World示例应用

set -e

echo "🚀 开始构建HarmonyOS应用..."

# 检查是否在CI环境中
if [ "$CI" = "true" ]; then
    echo "🔧 CI环境检测到，使用Gradle构建HarmonyOS模块..."
    
    # 使用Gradle构建HarmonyOS目标
    ./gradlew :harmonyApp:buildHarmonyApp --stacktrace
    
    echo "✅ HarmonyOS模块构建成功（CI模式）"
    echo "💡 提示: 完整的HAP构建需要DevEco Studio环境"
    exit 0
fi

# 检查DevEco Studio环境
if ! command -v hvigorw &> /dev/null; then
    echo "⚠️ 警告: hvigorw 未找到，尝试使用Gradle构建..."
    
    # 回退到Gradle构建
    ./gradlew :harmonyApp:buildHarmonyApp --stacktrace
    
    echo "✅ HarmonyOS模块构建成功（Gradle模式）"
    echo "💡 提示: 完整的HAP构建需要DevEco Studio和HarmonyOS SDK"
    exit 0
fi

# 进入HarmonyOS项目目录
cd harmonyApp

# 清理之前的构建
echo "🧹 清理之前的构建..."
./hvigorw clean

# 构建HarmonyOS应用
echo "📱 构建HarmonyOS应用..."
./hvigorw assembleHap

# 检查构建结果
HAP_PATH="build/default/outputs/default/entry-default-signed.hap"
if [ -f "$HAP_PATH" ]; then
    echo "✅ HarmonyOS应用构建成功!"
    echo "📱 HAP位置: $HAP_PATH"
    
    # 显示HAP信息
    HAP_SIZE=$(du -h "$HAP_PATH" | cut -f1)
    echo "📊 HAP大小: $HAP_SIZE"
    
    echo "💡 提示: 使用hdc工具安装HAP到设备"
    echo "   hdc install $HAP_PATH"
else
    echo "❌ HarmonyOS应用构建失败!"
    echo "💡 请检查DevEco Studio环境配置"
    exit 1
fi

cd ..
echo "🎉 HarmonyOS构建完成!"
