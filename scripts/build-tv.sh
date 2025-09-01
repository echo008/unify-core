#!/bin/bash

# TV平台构建脚本
# 构建Unify KMP框架的TV平台应用 (Android TV, tvOS, HarmonyOS TV)

set -e

echo "📺 开始构建TV平台应用..."

# 构建Android TV
echo "🤖 构建Android TV应用..."
if [ -d "tvApp" ]; then
    cd tvApp
    echo "📦 构建Android TV APK..."
    ../gradlew assembleDebug assembleRelease --stacktrace --parallel --build-cache
    
    # 检查构建结果
    if [ -f "build/outputs/apk/debug/tvApp-debug.apk" ]; then
        echo "✅ Android TV Debug APK构建成功!"
        APK_SIZE=$(du -h "build/outputs/apk/debug/tvApp-debug.apk" | cut -f1)
        echo "📊 Debug APK大小: $APK_SIZE"
    fi
    
    if [ -f "build/outputs/apk/release/tvApp-release.apk" ]; then
        echo "✅ Android TV Release APK构建成功!"
        APK_SIZE=$(du -h "build/outputs/apk/release/tvApp-release.apk" | cut -f1)
        echo "📊 Release APK大小: $APK_SIZE"
    fi
    cd ..
else
    echo "⚠️ tvApp目录未找到，跳过Android TV构建"
fi

echo ""

# 构建tvOS (仅在macOS上)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 构建tvOS应用..."
    if [ -d "tvosApp" ]; then
        cd tvosApp
        
        # 构建tvOS Framework
        echo "📦 构建tvOS Framework..."
        ../gradlew shared:linkDebugFrameworkTvosArm64 --stacktrace --parallel --build-cache
        ../gradlew shared:linkReleaseFrameworkTvosArm64 --stacktrace --parallel --build-cache
        
        # 检查构建结果
        if [ -d "../shared/build/bin/tvosArm64/debugFramework" ]; then
            echo "✅ tvOS Debug Framework构建成功!"
            FRAMEWORK_SIZE=$(du -sh "../shared/build/bin/tvosArm64/debugFramework" | cut -f1)
            echo "📊 Debug Framework大小: $FRAMEWORK_SIZE"
        fi
        
        if [ -d "../shared/build/bin/tvosArm64/releaseFramework" ]; then
            echo "✅ tvOS Release Framework构建成功!"
            FRAMEWORK_SIZE=$(du -sh "../shared/build/bin/tvosArm64/releaseFramework" | cut -f1)
            echo "📊 Release Framework大小: $FRAMEWORK_SIZE"
        fi
        
        cd ..
    else
        echo "⚠️ tvosApp目录未找到，跳过tvOS构建"
    fi
else
    echo "⚠️ 当前系统不支持tvOS构建，跳过tvOS构建"
fi

echo ""

# 构建HarmonyOS TV应用
echo "🔥 构建HarmonyOS TV应用..."
if [ -d "harmonyTVApp" ]; then
    cd harmonyTVApp
    
    if command -v hvigorw &> /dev/null; then
        echo "📦 构建HarmonyOS TV HAP..."
        ./hvigorw clean
        ./hvigorw assembleHap
        
        # 检查构建结果
        HAP_PATH="build/default/outputs/default/entry-default-signed.hap"
        if [ -f "$HAP_PATH" ]; then
            echo "✅ HarmonyOS TV应用构建成功!"
            HAP_SIZE=$(du -h "$HAP_PATH" | cut -f1)
            echo "📊 TV HAP大小: $HAP_SIZE"
        else
            echo "❌ HarmonyOS TV应用构建失败!"
        fi
    else
        echo "⚠️ hvigorw未找到，请确保已安装DevEco Studio"
    fi
    
    cd ..
else
    echo "⚠️ harmonyTVApp目录未找到，跳过HarmonyOS TV构建"
fi

echo ""
echo "🎉 TV平台构建完成!"
echo ""
echo "📦 构建产物位置:"
echo "  - Android TV Debug: tvApp/build/outputs/apk/debug/tvApp-debug.apk"
echo "  - Android TV Release: tvApp/build/outputs/apk/release/tvApp-release.apk"
echo "  - tvOS Debug: shared/build/bin/tvosArm64/debugFramework/"
echo "  - tvOS Release: shared/build/bin/tvosArm64/releaseFramework/"
echo "  - HarmonyOS TV: harmonyTVApp/build/default/outputs/default/entry-default-signed.hap"
echo ""
echo "💡 使用说明:"
echo "  - Android TV: 使用adb安装到Android TV设备"
echo "  - tvOS: 在Xcode中打开tvosApp项目进行调试"
echo "  - HarmonyOS TV: 使用hdc工具安装到HarmonyOS TV设备"
