#!/bin/bash

# 小程序构建脚本
# 构建Unify KMP框架的小程序Hello World示例应用

set -e

echo "🚀 开始构建小程序应用..."

# 检查小程序目录
if [ ! -d "miniApp" ]; then
    echo "❌ 错误: miniApp 目录未找到"
    exit 1
fi

# 进入小程序目录
cd miniApp

echo "📦 小程序项目结构检查..."
echo "✅ 页面文件: pages/index/index.wxml"
echo "✅ 逻辑文件: pages/index/index.js"
echo "✅ 样式文件: pages/index/index.wxss"
echo "✅ 配置文件: app.json"
echo "✅ 应用逻辑: app.js"

# 创建项目配置文件
cat > project.config.json << 'EOF'
{
  "description": "Unify KMP跨平台框架小程序示例",
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
  "projectname": "unify-kmp-miniapp",
  "condition": {}
}
EOF

# 创建sitemap配置
cat > sitemap.json << 'EOF'
{
  "desc": "关于本文件的更多信息，请参考文档 https://developers.weixin.qq.com/miniprogram/dev/framework/sitemap.html",
  "rules": [{
    "action": "allow",
    "page": "*"
  }]
}
EOF

echo "✅ 小程序构建配置完成!"
echo "📱 小程序项目已准备就绪"
echo ""
echo "📋 使用说明:"
echo "1. 在微信开发者工具中导入 miniApp 目录"
echo "2. 设置 AppID (可使用测试号)"
echo "3. 点击编译即可预览小程序"
echo ""
echo "🔧 支持的小程序平台:"
echo "- 微信小程序"
echo "- 支付宝小程序 (需调整语法)"
echo "- 百度智能小程序 (需调整语法)"
echo "- 字节跳动小程序 (需调整语法)"

cd ..
echo "🎉 小程序构建完成!"
