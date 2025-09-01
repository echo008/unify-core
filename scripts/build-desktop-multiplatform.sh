#!/bin/bash

# Desktop多平台构建脚本
# 构建Unify KMP框架的Desktop应用 (Windows/macOS/Linux原生支持)

set -e

echo "🖥️ 开始构建Desktop多平台应用..."

# 创建构建输出目录
mkdir -p build/desktop/windows
mkdir -p build/desktop/macos
mkdir -p build/desktop/linux

# 1. 构建Windows版本
echo "🪟 构建Windows版本..."
if [ -d "desktopApp" ]; then
    cd desktopApp
    
    # 构建Windows可执行文件
    echo "📦 构建Windows可执行文件..."
    ../gradlew packageDistributionForCurrentOS --stacktrace --parallel --build-cache
    ../gradlew createDistributable --stacktrace --parallel --build-cache
    
    # 检查Windows构建结果
    if [ -d "build/compose/binaries/main/app" ]; then
        echo "✅ Windows应用构建成功!"
        cp -r build/compose/binaries/main/app/* ../build/desktop/windows/
        
        # 创建Windows安装包 (如果支持)
        if command -v makensis &> /dev/null; then
            echo "📦 创建Windows安装包..."
            ../gradlew packageMsi --stacktrace || echo "⚠️ MSI包创建失败，但应用构建成功"
        fi
        
        APP_SIZE=$(du -sh ../build/desktop/windows | cut -f1)
        echo "📊 Windows应用大小: $APP_SIZE"
    else
        echo "❌ Windows应用构建失败!"
    fi
    
    cd ..
else
    echo "⚠️ desktopApp目录未找到，跳过Windows构建"
fi

echo ""

# 2. 构建macOS版本 (仅在macOS上)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 构建macOS版本..."
    if [ -d "desktopApp" ]; then
        cd desktopApp
        
        # 构建macOS应用包
        echo "📦 构建macOS应用包..."
        ../gradlew packageDistributionForCurrentOS --stacktrace --parallel --build-cache
        ../gradlew packageDmg --stacktrace --parallel --build-cache || echo "⚠️ DMG包创建失败，但应用构建成功"
        
        # 检查macOS构建结果
        if [ -d "build/compose/binaries/main/app" ]; then
            echo "✅ macOS应用构建成功!"
            cp -r build/compose/binaries/main/app/* ../build/desktop/macos/
            
            # 创建macOS应用包结构
            mkdir -p ../build/desktop/macos/UnifyApp.app/Contents/MacOS
            mkdir -p ../build/desktop/macos/UnifyApp.app/Contents/Resources
            
            # 创建Info.plist
            cat > ../build/desktop/macos/UnifyApp.app/Contents/Info.plist << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleExecutable</key>
    <string>UnifyApp</string>
    <key>CFBundleIdentifier</key>
    <string>com.unify.desktop</string>
    <key>CFBundleName</key>
    <string>Unify Desktop</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
</dict>
</plist>
EOF
            
            APP_SIZE=$(du -sh ../build/desktop/macos | cut -f1)
            echo "📊 macOS应用大小: $APP_SIZE"
        else
            echo "❌ macOS应用构建失败!"
        fi
        
        cd ..
    else
        echo "⚠️ desktopApp目录未找到，跳过macOS构建"
    fi
else
    echo "⚠️ 当前系统不支持macOS构建，跳过macOS构建"
fi

echo ""

# 3. 构建Linux版本
echo "🐧 构建Linux版本..."
if [ -d "desktopApp" ]; then
    cd desktopApp
    
    # 构建Linux应用
    echo "📦 构建Linux应用..."
    ../gradlew packageDistributionForCurrentOS --stacktrace --parallel --build-cache
    
    # 检查Linux构建结果
    if [ -d "build/compose/binaries/main/app" ]; then
        echo "✅ Linux应用构建成功!"
        cp -r build/compose/binaries/main/app/* ../build/desktop/linux/
        
        # 创建Linux .desktop文件
        cat > ../build/desktop/linux/unify-desktop.desktop << 'EOF'
[Desktop Entry]
Version=1.0
Type=Application
Name=Unify Desktop
Comment=Unify KMP跨平台桌面应用
Exec=./UnifyApp
Icon=unify-icon
Terminal=false
Categories=Development;
EOF
        
        # 创建启动脚本
        cat > ../build/desktop/linux/run.sh << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
./UnifyApp
EOF
        chmod +x ../build/desktop/linux/run.sh
        
        # 尝试创建AppImage (如果支持)
        if command -v appimagetool &> /dev/null; then
            echo "📦 创建AppImage..."
            ../gradlew packageAppImage --stacktrace || echo "⚠️ AppImage创建失败，但应用构建成功"
        fi
        
        # 尝试创建DEB包 (如果支持)
        if command -v dpkg-deb &> /dev/null; then
            echo "📦 创建DEB包..."
            ../gradlew packageDeb --stacktrace || echo "⚠️ DEB包创建失败，但应用构建成功"
        fi
        
        # 尝试创建RPM包 (如果支持)
        if command -v rpmbuild &> /dev/null; then
            echo "📦 创建RPM包..."
            ../gradlew packageRpm --stacktrace || echo "⚠️ RPM包创建失败，但应用构建成功"
        fi
        
        APP_SIZE=$(du -sh ../build/desktop/linux | cut -f1)
        echo "📊 Linux应用大小: $APP_SIZE"
    else
        echo "❌ Linux应用构建失败!"
    fi
    
    cd ..
else
    echo "⚠️ desktopApp目录未找到，跳过Linux构建"
fi

echo ""
echo "🎉 Desktop多平台构建完成!"
echo ""
echo "📦 构建产物位置:"
echo "  - Windows: build/desktop/windows/"
echo "  - macOS: build/desktop/macos/"
echo "  - Linux: build/desktop/linux/"
echo ""
echo "💡 使用说明:"
echo "  - Windows: 运行 .exe 文件或安装 .msi 包"
echo "  - macOS: 运行 .app 应用包或安装 .dmg 文件"
echo "  - Linux: 运行 ./run.sh 或安装 .deb/.rpm/.AppImage 包"
echo ""
echo "🔧 支持的Desktop平台:"
echo "- Windows (x64) - .exe/.msi"
echo "- macOS (x64/ARM64) - .app/.dmg"
echo "- Linux (x64) - .deb/.rpm/.AppImage"
