#!/bin/bash

# Web应用构建脚本
# 构建Unify KMP框架的Web Hello World示例应用

set -e

echo "🚀 开始构建Web应用..."

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

# 构建Web应用
echo "🌐 构建Web应用..."
./gradlew :webApp:jsBrowserDistribution

# 检查构建结果
DIST_PATH="webApp/build/distributions"
if [ -d "$DIST_PATH" ]; then
    echo "✅ Web应用构建成功!"
    echo "🌐 构建产物位置: $DIST_PATH"
    
    # 显示构建信息
    DIST_SIZE=$(du -sh "$DIST_PATH" | cut -f1)
    echo "📊 构建产物大小: $DIST_SIZE"
    
    # 创建简单的HTTP服务器脚本
    cat > "$DIST_PATH/serve.sh" << 'EOF'
#!/bin/bash
echo "🌐 启动Web服务器..."
echo "📱 访问地址: http://localhost:8080"
python3 -m http.server 8080
EOF
    chmod +x "$DIST_PATH/serve.sh"
    
    echo "💡 提示: 运行 '$DIST_PATH/serve.sh' 启动本地服务器"
else
    echo "❌ Web应用构建失败!"
    exit 1
fi

echo "🎉 Web构建完成!"
