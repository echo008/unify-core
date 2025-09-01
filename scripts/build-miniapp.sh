#!/bin/bash

# 小程序多平台构建脚本
# 构建Unify KMP框架的小程序应用 (微信、支付宝、字节跳动、百度、快手、小米、华为、QQ)

set -e

echo "🚀 开始构建小程序多平台应用..."

# 检查小程序目录
if [ ! -d "miniApp" ]; then
    echo "❌ 错误: miniApp 目录未找到"
    exit 1
fi

# 进入小程序目录
cd miniApp

echo "📦 小程序项目结构检查..."

# 检查必需文件是否存在
if [ ! -f "app.json" ]; then
    echo "⚠️ 警告: app.json 不存在，创建默认配置..."
    cat > app.json << 'EOF'
{
  "pages": [
    "pages/index/index"
  ],
  "window": {
    "backgroundTextStyle": "light",
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTitleText": "Unify KMP",
    "navigationBarTextStyle": "black"
  },
  "style": "v2",
  "sitemapLocation": "sitemap.json"
}
EOF
fi

# 检查页面目录结构
if [ ! -d "pages/index" ]; then
    mkdir -p pages/index
fi

# 创建页面文件（如果不存在）
if [ ! -f "pages/index/index.wxml" ]; then
    echo "⚠️ 创建默认页面文件..."
    cat > pages/index/index.wxml << 'EOF'
<view class="container">
  <text class="title">Unify KMP 跨平台框架</text>
  <text class="subtitle">一套代码，多端复用</text>
  <button bindtap="onTap" class="btn">点击测试</button>
</view>
EOF
fi

if [ ! -f "pages/index/index.js" ]; then
    cat > pages/index/index.js << 'EOF'
Page({
  data: {
    message: 'Hello Unify KMP!'
  },
  onLoad: function() {
    console.log('Unify KMP 小程序加载完成');
  },
  onTap: function() {
    wx.showToast({
      title: 'Unify KMP 运行正常',
      icon: 'success'
    });
  }
});
EOF
fi

if [ ! -f "pages/index/index.wxss" ]; then
    cat > pages/index/index.wxss << 'EOF'
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  padding: 40rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.subtitle {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 60rpx;
}

.btn {
  background-color: #007aff;
  color: white;
  border-radius: 10rpx;
  padding: 20rpx 40rpx;
}
EOF
fi

if [ ! -f "app.js" ]; then
    cat > app.js << 'EOF'
App({
  onLaunch: function() {
    console.log('Unify KMP 小程序启动');
  },
  globalData: {
    version: '1.0.0'
  }
});
EOF
fi

echo "✅ 页面文件: pages/index/index.wxml"
echo "✅ 逻辑文件: pages/index/index.js"
echo "✅ 样式文件: pages/index/index.wxss"
echo "✅ 配置文件: app.json"
echo "✅ 应用逻辑: app.js"

# 创建各平台构建目录
echo "📁 创建各平台构建目录..."
mkdir -p dist/wechat
mkdir -p dist/alipay
mkdir -p dist/bytedance
mkdir -p dist/baidu
mkdir -p dist/kuaishou
mkdir -p dist/xiaomi
mkdir -p dist/huawei
mkdir -p dist/qq

# 1. 构建微信小程序
echo "📱 构建微信小程序..."
cp -r . dist/wechat/
cat > dist/wechat/project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架微信小程序",
  "packOptions": {
    "ignore": [],
    "include": []
  },
  "setting": {
    "bundle": false,
    "userConfirmedBundleSwitch": false,
    "urlCheck": true,
    "scopeDataCheck": false,
    "coverView": true,
    "es6": true,
    "postcss": true,
    "compileHotReLoad": false,
    "lazyloadPlaceholderEnable": false,
    "preloadBackgroundData": false,
    "minified": true,
    "autoAudits": false,
    "newFeature": false,
    "uglifyFileName": false,
    "uploadWithSourceMap": true,
    "useIsolateContext": true,
    "nodeModules": false,
    "enhance": true,
    "useMultiFrameRuntime": true,
    "useApiHook": true,
    "useApiHostProcess": true,
    "showShadowRootInWxmlPanel": true,
    "packNpmManually": false,
    "enableEngineNative": false,
    "packNpmRelationList": [],
    "minifyWXSS": true,
    "showES6CompileOption": false,
    "minifyWXML": true,
    "babelSetting": {
      "ignore": [],
      "disablePlugins": [],
      "outputPath": ""
    }
  },
  "compileType": "miniprogram",
  "libVersion": "2.19.4",
  "appid": "wx1234567890abcdef",
  "projectname": "unify-kmp-wechat",
  "condition": {}
}
EOF

# 2. 构建支付宝小程序
echo "💰 构建支付宝小程序..."
cp -r . dist/alipay/
# 转换文件扩展名 .wxml -> .axml, .wxss -> .acss
find dist/alipay -name "*.wxml" -exec sh -c 'mv "$1" "${1%.wxml}.axml"' _ {} \;
find dist/alipay -name "*.wxss" -exec sh -c 'mv "$1" "${1%.wxss}.acss"' _ {} \;
cat > dist/alipay/mini.project.json << 'EOF'
{
  "enableAppxNg": true,
  "component2": true,
  "enableNodeModuleBabelTransform": false,
  "enableParallelLoader": false,
  "enableDistFileMinify": true,
  "miniprogramRoot": "./",
  "compileType": "miniprogram",
  "projectname": "unify-kmp-alipay"
}
EOF

# 3. 构建字节跳动小程序
echo "🎵 构建字节跳动小程序..."
cp -r . dist/bytedance/
# 转换文件扩展名 .wxml -> .ttml, .wxss -> .ttss
find dist/bytedance -name "*.wxml" -exec sh -c 'mv "$1" "${1%.wxml}.ttml"' _ {} \;
find dist/bytedance -name "*.wxss" -exec sh -c 'mv "$1" "${1%.wxss}.ttss"' _ {} \;
cat > dist/bytedance/project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架字节跳动小程序",
  "setting": {
    "urlCheck": true,
    "es6": true,
    "postcss": true,
    "minified": true
  },
  "compileType": "miniprogram",
  "appid": "tt1234567890abcdef",
  "projectname": "unify-kmp-bytedance"
}
EOF

# 4. 构建百度智能小程序
echo "🔍 构建百度智能小程序..."
cp -r . dist/baidu/
# 转换文件扩展名 .wxml -> .swan, .wxss -> .css
find dist/baidu -name "*.wxml" -exec sh -c 'mv "$1" "${1%.wxml}.swan"' _ {} \;
find dist/baidu -name "*.wxss" -exec sh -c 'mv "$1" "${1%.wxss}.css"' _ {} \;
cat > dist/baidu/project.swan.json << 'EOF'
{
  "description": "Unify KMP跨平台框架百度智能小程序",
  "setting": {
    "urlCheck": true,
    "es6": true,
    "postcss": true,
    "minified": true
  },
  "compileType": "miniprogram",
  "appid": "bd1234567890abcdef",
  "projectname": "unify-kmp-baidu"
}
EOF

# 5. 构建快手小程序
echo "⚡ 构建快手小程序..."
cp -r . dist/kuaishou/
# 转换文件扩展名 .wxml -> .ksml, .wxss -> .css
find dist/kuaishou -name "*.wxml" -exec sh -c 'mv "$1" "${1%.wxml}.ksml"' _ {} \;
find dist/kuaishou -name "*.wxss" -exec sh -c 'mv "$1" "${1%.wxss}.css"' _ {} \;
cat > dist/kuaishou/project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架快手小程序",
  "setting": {
    "urlCheck": true,
    "es6": true,
    "postcss": true,
    "minified": true
  },
  "compileType": "miniprogram",
  "appid": "ks1234567890abcdef",
  "projectname": "unify-kmp-kuaishou"
}
EOF

# 6. 构建小米小程序
echo "📱 构建小米小程序..."
cp -r . dist/xiaomi/
cat > dist/xiaomi/project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架小米小程序",
  "setting": {
    "urlCheck": true,
    "es6": true,
    "postcss": true,
    "minified": true
  },
  "compileType": "miniprogram",
  "appid": "mi1234567890abcdef",
  "projectname": "unify-kmp-xiaomi"
}
EOF

# 7. 构建华为小程序
echo "🔥 构建华为小程序..."
cp -r . dist/huawei/
cat > dist/huawei/project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架华为小程序",
  "setting": {
    "urlCheck": true,
    "es6": true,
    "postcss": true,
    "minified": true
  },
  "compileType": "miniprogram",
  "appid": "hw1234567890abcdef",
  "projectname": "unify-kmp-huawei"
}
EOF

# 8. 构建QQ小程序
echo "🐧 构建QQ小程序..."
cp -r . dist/qq/
# QQ小程序使用与微信相同的语法
cat > dist/qq/project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架QQ小程序",
  "setting": {
    "urlCheck": true,
    "es6": true,
    "postcss": true,
    "minified": true
  },
  "compileType": "miniprogram",
  "appid": "qq1234567890abcdef",
  "projectname": "unify-kmp-qq"
}
EOF

# 创建通用sitemap配置
for platform in wechat alipay bytedance baidu kuaishou xiaomi huawei qq; do
  cat > dist/$platform/sitemap.json << 'EOF'
{
  "desc": "关于本文件的更多信息，请参考各平台开发文档",
  "rules": [{
    "action": "allow",
    "page": "*"
  }]
}
EOF
done

echo "✅ 小程序多平台构建配置完成!"
echo "📱 小程序项目已准备就绪"
echo ""
echo "📦 构建产物位置:"
echo "  - 微信小程序: miniApp/dist/wechat/"
echo "  - 支付宝小程序: miniApp/dist/alipay/"
echo "  - 字节跳动小程序: miniApp/dist/bytedance/"
echo "  - 百度智能小程序: miniApp/dist/baidu/"
echo "  - 快手小程序: miniApp/dist/kuaishou/"
echo "  - 小米小程序: miniApp/dist/xiaomi/"
echo "  - 华为小程序: miniApp/dist/huawei/"
echo "  - QQ小程序: miniApp/dist/qq/"
echo ""
echo "📋 使用说明:"
echo "1. 使用对应平台的开发者工具导入相应目录"
echo "2. 设置对应平台的 AppID"
echo "3. 点击编译即可预览小程序"
echo ""
echo "🔧 支持的小程序平台:"
echo "- 微信小程序 (.wxml/.wxss)"
echo "- 支付宝小程序 (.axml/.acss)"
echo "- 字节跳动小程序 (.ttml/.ttss)"
echo "- 百度智能小程序 (.swan/.css)"
echo "- 快手小程序 (.ksml/.css)"
echo "- 小米小程序 (.wxml/.wxss)"
echo "- 华为小程序 (.wxml/.wxss)"
echo "- QQ小程序 (.wxml/.wxss)"

cd ..
echo "🎉 小程序多平台构建完成!"
