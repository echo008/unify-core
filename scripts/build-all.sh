#!/bin/bash

# 全平台构建脚本
# 构建Unify KMP框架的所有平台Hello World示例应用

set -e

echo "🚀 开始构建所有平台应用..."
echo "📋 支持的平台: Android, iOS, Web, Desktop, HarmonyOS, 小程序"
echo ""

# 获取脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 构建Android
echo "📱 构建Android平台..."
if [ -f "$SCRIPT_DIR/build-android.sh" ]; then
    bash "$SCRIPT_DIR/build-android.sh"
else
    echo "⚠️  Android构建脚本未找到，跳过Android构建"
fi

echo ""

# 构建Web
echo "🌐 构建Web平台..."
if [ -f "$SCRIPT_DIR/build-web.sh" ]; then
    bash "$SCRIPT_DIR/build-web.sh"
else
    echo "⚠️  Web构建脚本未找到，跳过Web构建"
fi

echo ""

# 构建桌面端
echo "🖥️ 构建桌面端平台..."
if [ -f "$SCRIPT_DIR/build-desktop.sh" ]; then
    bash "$SCRIPT_DIR/build-desktop.sh"
else
    echo "⚠️  桌面端构建脚本未找到，跳过桌面端构建"
fi

echo ""

# 构建iOS (仅在macOS上)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "📱 构建iOS平台..."
    if [ -f "$SCRIPT_DIR/build-ios.sh" ]; then
        bash "$SCRIPT_DIR/build-ios.sh"
    else
        echo "⚠️  iOS构建脚本未找到，跳过iOS构建"
    fi
else
    echo "⚠️  当前系统不支持iOS构建，跳过iOS构建"
fi

echo ""

# 构建HarmonyOS
echo "📱 构建HarmonyOS平台..."
if [ -f "$SCRIPT_DIR/build-harmony.sh" ]; then
    bash "$SCRIPT_DIR/build-harmony.sh"
else
    echo "⚠️  HarmonyOS构建脚本未找到，跳过HarmonyOS构建"
fi

echo ""

# 构建小程序
echo "📱 构建小程序平台..."
if [ -f "$SCRIPT_DIR/build-miniapp.sh" ]; then
    bash "$SCRIPT_DIR/build-miniapp.sh"
else
    echo "⚠️  小程序构建脚本未找到，跳过小程序构建"
fi

echo ""
echo "🎉 所有平台构建完成!"
echo ""
echo "📦 构建产物位置:"
echo "  - Android: androidApp/build/outputs/apk/debug/androidApp-debug.apk"
echo "  - iOS: 通过Xcode运行iosApp项目"
echo "  - Web: webApp/build/distributions/"
echo "  - Desktop: desktopApp/build/compose/binaries/main/app/"
echo "  - HarmonyOS: harmonyApp/build/default/outputs/default/entry-default-signed.hap"
echo "  - 小程序: miniApp/ (使用微信开发者工具打开)"
