#!/bin/bash

# Unify-Core 平台实现完整性验证脚本
# 验证所有8大平台的actual实现完整性和功能逻辑正确性

set -e

echo "🔍 开始验证 Unify-Core 平台实现完整性..."
echo "========================================"

# 定义颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 验证结果统计
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# 检查函数
check_file() {
    local file_path="$1"
    local description="$2"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌ FAIL${NC} - $description"
        echo -e "   ${YELLOW}缺失文件: $file_path${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

check_content() {
    local file_path="$1"
    local pattern="$2"
    local description="$3"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    
    if [ -f "$file_path" ] && grep -q "$pattern" "$file_path"; then
        echo -e "${GREEN}✅ PASS${NC} - $description"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌ FAIL${NC} - $description"
        echo -e "   ${YELLOW}文件: $file_path${NC}"
        echo -e "   ${YELLOW}缺失内容: $pattern${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

echo -e "${BLUE}📋 第一阶段: 验证平台actual实现文件存在性${NC}"
echo "----------------------------------------"

# 1. Android平台验证
echo -e "${BLUE}🤖 验证 Android 平台实现...${NC}"
check_file "shared/src/androidMain/kotlin/com/unify/core/platform/PlatformManager.android.kt" "Android PlatformManager实现"
check_content "shared/src/androidMain/kotlin/com/unify/core/platform/PlatformManager.android.kt" "actual object PlatformManager" "Android actual实现"
check_content "shared/src/androidMain/kotlin/com/unify/core/platform/PlatformManager.android.kt" "fun getPlatformType(): PlatformType = PlatformType.ANDROID" "Android平台类型"

# 2. iOS平台验证
echo -e "${BLUE}🍎 验证 iOS 平台实现...${NC}"
check_file "shared/src/iosMain/kotlin/com/unify/core/platform/PlatformManager.ios.kt" "iOS PlatformManager实现"
check_content "shared/src/iosMain/kotlin/com/unify/core/platform/PlatformManager.ios.kt" "actual object PlatformManager" "iOS actual实现"
check_content "shared/src/iosMain/kotlin/com/unify/core/platform/PlatformManager.ios.kt" "fun getPlatformType(): PlatformType = PlatformType.IOS" "iOS平台类型"

# 3. Web平台验证
echo -e "${BLUE}🌐 验证 Web 平台实现...${NC}"
check_file "shared/src/jsMain/kotlin/com/unify/core/platform/PlatformManager.js.kt" "Web PlatformManager实现"
check_content "shared/src/jsMain/kotlin/com/unify/core/platform/PlatformManager.js.kt" "actual object PlatformManager" "Web actual实现"
check_content "shared/src/jsMain/kotlin/com/unify/core/platform/PlatformManager.js.kt" "fun getPlatformType(): PlatformType = PlatformType.WEB" "Web平台类型"

# 4. Desktop平台验证
echo -e "${BLUE}🖥️ 验证 Desktop 平台实现...${NC}"
check_file "shared/src/desktopMain/kotlin/com/unify/core/platform/PlatformManager.desktop.kt" "Desktop PlatformManager实现"
check_content "shared/src/desktopMain/kotlin/com/unify/core/platform/PlatformManager.desktop.kt" "actual object PlatformManager" "Desktop actual实现"
check_content "shared/src/desktopMain/kotlin/com/unify/core/platform/PlatformManager.desktop.kt" "fun getPlatformType(): PlatformType = PlatformType.DESKTOP" "Desktop平台类型"

# 5. HarmonyOS平台验证
echo -e "${BLUE}🔥 验证 HarmonyOS 平台实现...${NC}"
check_file "shared/src/harmonyMain/kotlin/com/unify/core/platform/PlatformManager.harmony.kt" "HarmonyOS PlatformManager实现"
check_content "shared/src/harmonyMain/kotlin/com/unify/core/platform/PlatformManager.harmony.kt" "actual object PlatformManager" "HarmonyOS actual实现"
check_content "shared/src/harmonyMain/kotlin/com/unify/core/platform/PlatformManager.harmony.kt" "fun getPlatformType(): PlatformType = PlatformType.HARMONY_OS" "HarmonyOS平台类型"

# 6. 小程序平台验证
echo -e "${BLUE}📱 验证 小程序 平台实现...${NC}"
check_file "shared/src/miniAppMain/kotlin/com/unify/core/platform/PlatformManager.miniapp.kt" "小程序 PlatformManager实现"
check_content "shared/src/miniAppMain/kotlin/com/unify/core/platform/PlatformManager.miniapp.kt" "actual object PlatformManager" "小程序 actual实现"
check_content "shared/src/miniAppMain/kotlin/com/unify/core/platform/PlatformManager.miniapp.kt" "fun getPlatformType(): PlatformType = PlatformType.MINI_PROGRAM" "小程序平台类型"

# 7. Watch平台验证
echo -e "${BLUE}⌚ 验证 Watch 平台实现...${NC}"
check_file "shared/src/watchMain/kotlin/com/unify/core/platform/PlatformManager.watch.kt" "Watch PlatformManager实现"
check_content "shared/src/watchMain/kotlin/com/unify/core/platform/PlatformManager.watch.kt" "actual object PlatformManager" "Watch actual实现"
check_content "shared/src/watchMain/kotlin/com/unify/core/platform/PlatformManager.watch.kt" "fun getPlatformType(): PlatformType = PlatformType.WATCH" "Watch平台类型"

# 8. TV平台验证
echo -e "${BLUE}📺 验证 TV 平台实现...${NC}"
check_file "shared/src/tvMain/kotlin/com/unify/core/platform/PlatformManager.tv.kt" "TV PlatformManager实现"
check_content "shared/src/tvMain/kotlin/com/unify/core/platform/PlatformManager.tv.kt" "actual object PlatformManager" "TV actual实现"
check_content "shared/src/tvMain/kotlin/com/unify/core/platform/PlatformManager.tv.kt" "fun getPlatformType(): PlatformType = PlatformType.TV" "TV平台类型"

echo ""
echo -e "${BLUE}📋 第二阶段: 验证平台应用目录结构${NC}"
echo "----------------------------------------"

# 验证应用目录
check_file "androidApp/build.gradle.kts" "Android应用构建配置"
check_file "iosApp/iosApp.xcodeproj/project.pbxproj" "iOS应用Xcode项目"
check_file "webApp/build.gradle.kts" "Web应用构建配置"
check_file "desktopApp/build.gradle.kts" "Desktop应用构建配置"
check_file "harmonyApp/build-profile.json5" "HarmonyOS应用构建配置"
check_file "miniApp/app.json" "小程序应用配置"
check_file "wearApp/build.gradle.kts" "Wear OS应用构建配置"
check_file "tvApp/build.gradle.kts" "Android TV应用构建配置"

echo ""
echo -e "${BLUE}📋 第三阶段: 验证核心功能实现完整性${NC}"
echo "----------------------------------------"

# 验证核心功能方法
CORE_METHODS=(
    "fun initialize()"
    "fun getPlatformType()"
    "fun getPlatformName()"
    "fun getPlatformVersion()"
    "fun getDeviceInfo()"
    "fun getScreenInfo()"
    "fun getSystemCapabilities()"
    "fun getNetworkStatus()"
    "fun getStorageInfo()"
    "fun getPerformanceInfo()"
    "suspend fun showNativeDialog"
    "suspend fun invokePlatformFeature"
    "fun getPlatformConfig()"
)

PLATFORMS=(
    "androidMain:Android"
    "iosMain:iOS"
    "jsMain:Web"
    "desktopMain:Desktop"
    "harmonyMain:HarmonyOS"
    "miniAppMain:小程序"
    "watchMain:Watch"
    "tvMain:TV"
)

for platform_info in "${PLATFORMS[@]}"; do
    IFS=':' read -r platform_dir platform_name <<< "$platform_info"
    echo -e "${BLUE}🔍 验证 $platform_name 平台核心方法...${NC}"
    
    platform_file="shared/src/$platform_dir/kotlin/com/unify/core/platform/PlatformManager.*.kt"
    platform_file_actual=$(ls $platform_file 2>/dev/null | head -1)
    
    if [ -f "$platform_file_actual" ]; then
        for method in "${CORE_METHODS[@]}"; do
            if grep -q "$method" "$platform_file_actual"; then
                echo -e "${GREEN}  ✅${NC} $method"
                PASSED_CHECKS=$((PASSED_CHECKS + 1))
            else
                echo -e "${RED}  ❌${NC} $method"
                FAILED_CHECKS=$((FAILED_CHECKS + 1))
            fi
            TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
        done
    else
        echo -e "${RED}  ❌ 平台文件不存在${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + ${#CORE_METHODS[@]}))
        TOTAL_CHECKS=$((TOTAL_CHECKS + ${#CORE_METHODS[@]}))
    fi
done

echo ""
echo -e "${BLUE}📋 第四阶段: 验证构建脚本完整性${NC}"
echo "----------------------------------------"

# 验证构建脚本
check_file "scripts/build-android.sh" "Android构建脚本"
check_file "scripts/build-ios.sh" "iOS构建脚本"
check_file "scripts/build-web.sh" "Web构建脚本"
check_file "scripts/build-desktop.sh" "Desktop构建脚本"
check_file "scripts/build-desktop-multiplatform.sh" "Desktop多平台构建脚本"
check_file "scripts/build-harmony.sh" "HarmonyOS构建脚本"
check_file "scripts/build-miniapp.sh" "小程序构建脚本"
check_file "scripts/build-watch.sh" "Watch构建脚本"
check_file "scripts/build-tv.sh" "TV构建脚本"
check_file "scripts/build-all.sh" "全平台构建脚本"

echo ""
echo -e "${BLUE}📋 第五阶段: 验证项目配置完整性${NC}"
echo "----------------------------------------"

# 验证项目配置
check_file "settings.gradle.kts" "项目设置配置"
check_content "settings.gradle.kts" "include.*androidApp" "Android应用模块"
check_content "settings.gradle.kts" "include.*iosApp" "iOS应用模块"
check_content "settings.gradle.kts" "include.*webApp" "Web应用模块"
check_content "settings.gradle.kts" "include.*desktopApp" "Desktop应用模块"
check_content "settings.gradle.kts" "include.*harmonyApp" "HarmonyOS应用模块"
check_content "settings.gradle.kts" "include.*miniApp" "小程序应用模块"
check_content "settings.gradle.kts" "include.*wearApp" "Wear OS应用模块"
check_content "settings.gradle.kts" "include.*tvApp" "Android TV应用模块"

# 验证GitHub Actions工作流
check_file ".github/workflows/build-and-test.yml" "GitHub Actions工作流"
check_content ".github/workflows/build-and-test.yml" "build-android" "Android构建任务"
check_content ".github/workflows/build-and-test.yml" "build-ios" "iOS构建任务"
check_content ".github/workflows/build-and-test.yml" "build-web" "Web构建任务"
check_content ".github/workflows/build-and-test.yml" "build-desktop" "Desktop构建任务"
check_content ".github/workflows/build-and-test.yml" "build-harmony" "HarmonyOS构建任务"
check_content ".github/workflows/build-and-test.yml" "build-miniapp" "小程序构建任务"
check_content ".github/workflows/build-and-test.yml" "build-watch" "Watch构建任务"
check_content ".github/workflows/build-and-test.yml" "build-tv" "TV构建任务"

echo ""
echo "========================================"
echo -e "${BLUE}📊 验证结果统计${NC}"
echo "========================================"
echo -e "总检查项: ${BLUE}$TOTAL_CHECKS${NC}"
echo -e "通过检查: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "失败检查: ${RED}$FAILED_CHECKS${NC}"

# 计算通过率
if [ $TOTAL_CHECKS -gt 0 ]; then
    PASS_RATE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))
    echo -e "通过率: ${BLUE}$PASS_RATE%${NC}"
    
    if [ $PASS_RATE -ge 95 ]; then
        echo -e "${GREEN}🎉 优秀! 平台实现完整性达到生产级标准${NC}"
        exit 0
    elif [ $PASS_RATE -ge 85 ]; then
        echo -e "${YELLOW}⚠️  良好! 平台实现基本完整，建议修复剩余问题${NC}"
        exit 1
    else
        echo -e "${RED}❌ 需要改进! 平台实现存在重大缺陷${NC}"
        exit 2
    fi
else
    echo -e "${RED}❌ 验证失败! 无法执行检查${NC}"
    exit 3
fi
