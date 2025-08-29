#!/bin/bash

# HarmonyOS应用构建脚本
# 构建Unify KMP框架的HarmonyOS Hello World示例应用

set -e

echo "🚀 开始构建HarmonyOS应用..."

# 检查环境
if ! command -v hvigorw &> /dev/null; then
    echo "⚠️ 警告: hvigorw 未找到，请确保已安装DevEco Studio和HarmonyOS SDK"
    echo "💡 提示: 请在DevEco Studio中打开harmonyApp项目进行构建"
    exit 1
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
