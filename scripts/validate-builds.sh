#!/bin/bash

# 构建验证脚本
# 验证所有平台构建脚本的完整性和可执行性

set -e

echo "🔍 开始验证所有平台构建脚本..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 验证脚本存在性和权限
echo "📋 检查构建脚本文件..."

SCRIPTS=(
    "build-android.sh"
    "build-ios.sh" 
    "build-web.sh"
    "build-desktop.sh"
    "build-desktop-multiplatform.sh"
    "build-harmony.sh"
    "build-miniapp.sh"
    "build-watch.sh"
    "build-tv.sh"
    "build-all.sh"
)

for script in "${SCRIPTS[@]}"; do
    script_path="$SCRIPT_DIR/$script"
    if [ -f "$script_path" ]; then
        echo "✅ $script - 存在"
        
        # 检查执行权限
        if [ -x "$script_path" ]; then
            echo "  ✅ 具有执行权限"
        else
            echo "  ⚠️  缺少执行权限，正在添加..."
            chmod +x "$script_path"
        fi
        
        # 检查脚本语法
        if bash -n "$script_path"; then
            echo "  ✅ 语法检查通过"
        else
            echo "  ❌ 语法检查失败"
        fi
    else
        echo "❌ $script - 不存在"
    fi
    echo ""
done

# 验证项目目录结构
echo "📁 检查项目目录结构..."

DIRECTORIES=(
    "androidApp"
    "iosApp"
    "webApp"
    "desktopApp"
    "harmonyApp"
    "miniApp"
    "shared"
)

for dir in "${DIRECTORIES[@]}"; do
    dir_path="$PROJECT_ROOT/$dir"
    if [ -d "$dir_path" ]; then
        echo "✅ $dir/ - 存在"
    else
        echo "⚠️  $dir/ - 不存在（某些构建可能会跳过）"
    fi
done

echo ""

# 验证构建工具
echo "🛠️  检查构建工具..."

# 检查Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java: $JAVA_VERSION"
else
    echo "❌ Java未安装"
fi

# 检查Gradle
if [ -f "$PROJECT_ROOT/gradlew" ]; then
    echo "✅ Gradle Wrapper: 存在"
    chmod +x "$PROJECT_ROOT/gradlew"
else
    echo "❌ Gradle Wrapper不存在"
fi

# 检查Node.js (用于小程序和Web)
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo "✅ Node.js: $NODE_VERSION"
else
    echo "⚠️  Node.js未安装（小程序和Web构建可能失败）"
fi

# 检查npm
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm --version)
    echo "✅ npm: $NPM_VERSION"
else
    echo "⚠️  npm未安装"
fi

# 检查HarmonyOS工具 (仅在相关目录存在时)
if [ -d "$PROJECT_ROOT/harmonyApp" ]; then
    if command -v hvigorw &> /dev/null; then
        echo "✅ HarmonyOS hvigorw: 可用"
    else
        echo "⚠️  HarmonyOS hvigorw未找到（需要DevEco Studio）"
    fi
fi

# 检查Xcode (仅在macOS上)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if command -v xcodebuild &> /dev/null; then
        XCODE_VERSION=$(xcodebuild -version | head -n 1)
        echo "✅ Xcode: $XCODE_VERSION"
    else
        echo "⚠️  Xcode未安装（iOS/watchOS/tvOS构建将失败）"
    fi
else
    echo "ℹ️  非macOS系统，跳过Xcode检查"
fi

echo ""

# 验证Gradle配置
echo "⚙️  验证Gradle配置..."

cd "$PROJECT_ROOT"

if [ -f "build.gradle.kts" ]; then
    echo "✅ 根build.gradle.kts存在"
else
    echo "❌ 根build.gradle.kts不存在"
fi

if [ -f "settings.gradle.kts" ]; then
    echo "✅ settings.gradle.kts存在"
else
    echo "❌ settings.gradle.kts不存在"
fi

if [ -f "gradle.properties" ]; then
    echo "✅ gradle.properties存在"
else
    echo "❌ gradle.properties不存在"
fi

echo ""

# 运行快速构建测试
echo "🧪 运行快速构建测试..."

echo "测试Gradle任务列表..."
if ./gradlew tasks --quiet > /dev/null 2>&1; then
    echo "✅ Gradle配置有效"
else
    echo "❌ Gradle配置有问题"
fi

echo ""

# 检查GitHub Actions工作流
echo "🔄 检查GitHub Actions工作流..."

WORKFLOW_FILE="$PROJECT_ROOT/.github/workflows/build-and-test.yml"
if [ -f "$WORKFLOW_FILE" ]; then
    echo "✅ GitHub Actions工作流存在"
    
    # 检查是否包含所有新增的构建任务
    if grep -q "build-miniapp" "$WORKFLOW_FILE"; then
        echo "✅ 包含MiniApp构建任务"
    else
        echo "⚠️  缺少MiniApp构建任务"
    fi
    
    if grep -q "build-watch" "$WORKFLOW_FILE"; then
        echo "✅ 包含Watch构建任务"
    else
        echo "⚠️  缺少Watch构建任务"
    fi
    
    if grep -q "build-tv" "$WORKFLOW_FILE"; then
        echo "✅ 包含TV构建任务"
    else
        echo "⚠️  缺少TV构建任务"
    fi
else
    echo "❌ GitHub Actions工作流不存在"
fi

echo ""

# 生成验证报告
echo "📊 生成验证报告..."

REPORT_FILE="$PROJECT_ROOT/build-validation-report.md"

cat > "$REPORT_FILE" << EOF
# Unify-Core 构建验证报告

生成时间: $(date)

## 构建脚本状态

| 脚本 | 状态 | 权限 | 语法 |
|------|------|------|------|
EOF

for script in "${SCRIPTS[@]}"; do
    script_path="$SCRIPT_DIR/$script"
    if [ -f "$script_path" ]; then
        status="✅ 存在"
        if [ -x "$script_path" ]; then
            permission="✅ 可执行"
        else
            permission="❌ 不可执行"
        fi
        if bash -n "$script_path" 2>/dev/null; then
            syntax="✅ 通过"
        else
            syntax="❌ 失败"
        fi
    else
        status="❌ 不存在"
        permission="N/A"
        syntax="N/A"
    fi
    echo "| $script | $status | $permission | $syntax |" >> "$REPORT_FILE"
done

cat >> "$REPORT_FILE" << EOF

## 项目目录结构

| 目录 | 状态 |
|------|------|
EOF

for dir in "${DIRECTORIES[@]}"; do
    dir_path="$PROJECT_ROOT/$dir"
    if [ -d "$dir_path" ]; then
        status="✅ 存在"
    else
        status="⚠️ 不存在"
    fi
    echo "| $dir/ | $status |" >> "$REPORT_FILE"
done

cat >> "$REPORT_FILE" << EOF

## 构建工具检查

- Java: $(if command -v java &> /dev/null; then java -version 2>&1 | head -n 1 | cut -d'"' -f2; else echo "未安装"; fi)
- Gradle: $(if [ -f "$PROJECT_ROOT/gradlew" ]; then echo "✅ Wrapper存在"; else echo "❌ 不存在"; fi)
- Node.js: $(if command -v node &> /dev/null; then node --version; else echo "未安装"; fi)
- npm: $(if command -v npm &> /dev/null; then npm --version; else echo "未安装"; fi)

## 平台特定工具

- HarmonyOS hvigorw: $(if command -v hvigorw &> /dev/null; then echo "✅ 可用"; else echo "⚠️ 未找到"; fi)
- Xcode: $(if [[ "$OSTYPE" == "darwin"* ]] && command -v xcodebuild &> /dev/null; then xcodebuild -version | head -n 1; else echo "不适用或未安装"; fi)

## 建议

1. 确保所有必需的开发工具已安装
2. 在运行构建前检查平台特定的依赖
3. 定期更新构建脚本和工具版本
4. 在CI/CD环境中运行完整的构建验证

EOF

echo "✅ 验证报告已生成: $REPORT_FILE"

echo ""
echo "🎉 构建验证完成!"
echo ""
echo "📋 摘要:"
echo "  - 构建脚本: ${#SCRIPTS[@]}个"
echo "  - 项目目录: ${#DIRECTORIES[@]}个"
echo "  - 详细报告: build-validation-report.md"
echo ""
echo "💡 下一步:"
echo "  1. 查看验证报告了解详细状态"
echo "  2. 安装缺失的构建工具"
echo "  3. 运行 ./scripts/build-all.sh 进行完整构建测试"
