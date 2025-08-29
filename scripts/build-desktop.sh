#!/bin/bash

# 桌面端应用构建脚本
# 构建Unify KMP框架的桌面端Hello World示例应用

set -e

echo "🚀 开始构建桌面端应用..."

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

# 构建桌面应用
echo "🖥️ 构建桌面应用..."
./gradlew :desktopApp:packageDistributionForCurrentOS

# 检查构建结果
DIST_PATH="desktopApp/build/compose/binaries/main/app"
if [ -d "$DIST_PATH" ]; then
    echo "✅ 桌面应用构建成功!"
    echo "🖥️ 应用位置: $DIST_PATH"
    
    # 显示构建信息
    DIST_SIZE=$(du -sh "$DIST_PATH" | cut -f1)
    echo "📊 应用大小: $DIST_SIZE"
    
    # 创建启动脚本
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "💡 提示: 在macOS上运行 '$DIST_PATH/Unify KMP Desktop.app/Contents/MacOS/Unify KMP Desktop'"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "💡 提示: 在Linux上运行 '$DIST_PATH/bin/Unify KMP Desktop'"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
        echo "💡 提示: 在Windows上运行 '$DIST_PATH/bin/Unify KMP Desktop.bat'"
    fi
else
    echo "❌ 桌面应用构建失败!"
    exit 1
fi

echo "🎉 桌面端构建完成!"
