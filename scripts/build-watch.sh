#!/bin/bash

# Watch平台构建脚本
# 构建Unify KMP框架的Watch平台应用 (Wear OS, watchOS, HarmonyOS穿戴)

set -e

echo "⌚ 开始构建Watch平台应用..."

# 构建Wear OS (Android Wear)
echo "🤖 构建Wear OS应用..."
if [ -d "wearApp" ]; then
    cd wearApp
    echo "📦 构建Wear OS APK..."
    ./gradlew assembleDebug assembleRelease --stacktrace --parallel --build-cache
    
    # 检查构建结果
    if [ -f "build/outputs/apk/debug/wearApp-debug.apk" ]; then
        echo "✅ Wear OS Debug APK构建成功!"
        APK_SIZE=$(du -h "build/outputs/apk/debug/wearApp-debug.apk" | cut -f1)
        echo "📊 Debug APK大小: $APK_SIZE"
    fi
    
    if [ -f "build/outputs/apk/release/wearApp-release.apk" ]; then
        echo "✅ Wear OS Release APK构建成功!"
        APK_SIZE=$(du -h "build/outputs/apk/release/wearApp-release.apk" | cut -f1)
        echo "📊 Release APK大小: $APK_SIZE"
    fi
    cd ..
else
    echo "⚠️ wearApp目录未找到，跳过Wear OS构建"
fi

echo ""

# 构建watchOS (仅在macOS上)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 构建watchOS应用..."
    if [ -d "watchApp" ]; then
        cd watchApp
        
        # 构建watchOS Framework
        echo "📦 构建watchOS Framework..."
        cd ..
        ./gradlew shared:linkDebugFrameworkWatchosArm64 --stacktrace --parallel --build-cache
        ./gradlew shared:linkReleaseFrameworkWatchosArm64 --stacktrace --parallel --build-cache
        cd watchApp
        
        # 检查构建结果
        if [ -d "../shared/build/bin/watchosArm64/debugFramework" ]; then
            echo "✅ watchOS Debug Framework构建成功!"
            FRAMEWORK_SIZE=$(du -sh "../shared/build/bin/watchosArm64/debugFramework" | cut -f1)
            echo "📊 Debug Framework大小: $FRAMEWORK_SIZE"
        fi
        
        if [ -d "../shared/build/bin/watchosArm64/releaseFramework" ]; then
            echo "✅ watchOS Release Framework构建成功!"
            FRAMEWORK_SIZE=$(du -sh "../shared/build/bin/watchosArm64/releaseFramework" | cut -f1)
            echo "📊 Release Framework大小: $FRAMEWORK_SIZE"
        fi
        
        cd ..
    else
        echo "⚠️ watchApp目录未找到，跳过watchOS构建"
    fi
else
    echo "⚠️ 当前系统不支持watchOS构建，跳过watchOS构建"
fi

echo ""

# 构建HarmonyOS穿戴应用
echo "🔥 构建HarmonyOS穿戴应用..."
if [ -d "harmonyWearApp" ]; then
    cd harmonyWearApp
    
    if command -v hvigorw &> /dev/null; then
        echo "📦 构建HarmonyOS穿戴HAP..."
        ./hvigorw clean
        ./hvigorw assembleHap
        
        # 检查构建结果
        HAP_PATH="build/default/outputs/default/entry-default-signed.hap"
        if [ -f "$HAP_PATH" ]; then
            echo "✅ HarmonyOS穿戴应用构建成功!"
            HAP_SIZE=$(du -h "$HAP_PATH" | cut -f1)
            echo "📊 穿戴HAP大小: $HAP_SIZE"
        else
            echo "❌ HarmonyOS穿戴应用构建失败!"
        fi
    else
        echo "⚠️ hvigorw未找到，请确保已安装DevEco Studio"
    fi
    
    cd ..
else
    echo "⚠️ harmonyWearApp目录未找到，跳过HarmonyOS穿戴构建"
fi

echo ""
echo "🎉 Watch平台构建完成!"
echo ""
echo "📦 构建产物位置:"
echo "  - Wear OS Debug: wearApp/build/outputs/apk/debug/wearApp-debug.apk"
echo "  - Wear OS Release: wearApp/build/outputs/apk/release/wearApp-release.apk"
echo "  - watchOS Debug: shared/build/bin/watchosArm64/debugFramework/"
echo "  - watchOS Release: shared/build/bin/watchosArm64/releaseFramework/"
echo "  - HarmonyOS穿戴: harmonyWearApp/build/default/outputs/default/entry-default-signed.hap"
echo ""
echo "💡 使用说明:"
echo "  - Wear OS: 使用adb安装到Wear OS设备"
echo "  - watchOS: 在Xcode中打开watchApp项目进行调试"
echo "  - HarmonyOS穿戴: 使用hdc工具安装到HarmonyOS穿戴设备"
