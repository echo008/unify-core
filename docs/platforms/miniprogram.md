# 小程序平台开发指南

## 📱 概述

Unify KMP 为微信小程序平台提供原生开发支持，使用小程序原生技术栈实现跨平台业务逻辑共享，同时保持小程序平台的特有优势和用户体验。

## 🛠️ 环境要求

### 必需工具
- **微信开发者工具**: 最新稳定版
- **Node.js**: 16.0+ (用于构建工具)
- **微信开发者账号**: 用于真机调试和发布
- **小程序 AppID**: 在微信公众平台申请

### 安装验证
```bash
# 检查 Node.js 版本
node --version

# 检查微信开发者工具
# 在工具中查看 设置 -> 关于
```

## 🏗️ 项目结构

### 小程序模块
```
miniApp/
├── pages/                    # 页面目录
│   ├── index/               # 首页
│   │   ├── index.js        # 页面逻辑
│   │   ├── index.json      # 页面配置
│   │   ├── index.wxml      # 页面结构
│   │   └── index.wxss      # 页面样式
│   └── logs/               # 日志页面
├── utils/                   # 工具函数
│   └── util.js
├── app.js                  # 小程序逻辑
├── app.json               # 小程序配置
├── app.wxss              # 小程序样式
└── project.config.json   # 项目配置
```

## 💻 核心组件实现

### 1. 小程序配置 (app.json)
```json
{
  "pages": [
    "pages/index/index",
    "pages/logs/logs"
  ],
  "window": {
    "backgroundTextStyle": "light",
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTitleText": "Unify KMP",
    "navigationBarTextStyle": "black"
  },
  "style": "v2",
  "sitemapLocation": "sitemap.json",
  "lazyCodeLoading": "requiredComponents"
}
```

### 2. 应用入口 (app.js)
```javascript
App({
  onLaunch() {
    console.log('Unify KMP 小程序启动');
    
    // 获取用户信息
    if (wx.getUserProfile) {
      this.globalData.canIUseGetUserProfile = true;
    }
    
    // 初始化应用数据
    this.initializeApp();
  },
  
  onShow() {
    console.log('小程序显示');
  },
  
  onHide() {
    console.log('小程序隐藏');
  },
  
  initializeApp() {
    // 初始化全局数据
    this.globalData.systemInfo = wx.getSystemInfoSync();
    this.globalData.version = '1.0.0';
  },
  
  globalData: {
    userInfo: null,
    systemInfo: null,
    version: null,
    canIUseGetUserProfile: false
  }
});
```

### 3. 主页面 (pages/index/index.js)
```javascript
const app = getApp();

Page({
  data: {
    count: 0,
    currentLanguage: 'zh',
    platformInfo: {},
    languages: {
      zh: {
        hello: 'Hello, 小程序!',
        count: '计数',
        increment: '增加',
        reset: '重置',
        share: '分享',
        platform: '平台',
        device: '设备'
      },
      en: {
        hello: 'Hello, MiniProgram!',
        count: 'Count',
        increment: 'Increment',
        reset: 'Reset',
        share: 'Share',
        platform: 'Platform',
        device: 'Device'
      },
      ja: {
        hello: 'こんにちは、ミニプログラム！',
        count: 'カウント',
        increment: '増加',
        reset: 'リセット',
        share: 'シェア',
        platform: 'プラットフォーム',
        device: 'デバイス'
      }
    }
  },

  onLoad() {
    console.log('页面加载');
    this.initializePage();
  },

  onShow() {
    console.log('页面显示');
  },

  initializePage() {
    const systemInfo = wx.getSystemInfoSync();
    this.setData({
      platformInfo: {
        platform: 'WeChat MiniProgram',
        device: `${systemInfo.brand} ${systemInfo.model}`,
        system: `${systemInfo.system}`,
        version: systemInfo.version,
        screenSize: `${systemInfo.screenWidth}x${systemInfo.screenHeight}`
      }
    });
  },

  // 获取当前语言文本
  getText(key) {
    return this.data.languages[this.data.currentLanguage][key] || key;
  },

  // 切换语言
  switchLanguage(e) {
    const language = e.currentTarget.dataset.lang;
    this.setData({
      currentLanguage: language
    });
  },

  // 增加计数
  increment() {
    this.setData({
      count: this.data.count + 1
    });
    
    // 触觉反馈
    wx.vibrateShort({
      type: 'light'
    });
  },

  // 重置计数
  reset() {
    this.setData({
      count: 0
    });
    
    wx.showToast({
      title: '已重置',
      icon: 'success',
      duration: 1000
    });
  },

  // 分享功能
  onShareAppMessage() {
    return {
      title: 'Unify KMP 小程序示例',
      path: '/pages/index/index',
      imageUrl: '/images/share.png'
    };
  },

  // 分享到朋友圈
  onShareTimeline() {
    return {
      title: 'Unify KMP 跨平台开发框架',
      query: 'from=timeline'
    };
  },

  // 显示系统信息
  showSystemInfo() {
    const info = this.data.platformInfo;
    const content = `平台: ${info.platform}\n设备: ${info.device}\n系统: ${info.system}\n屏幕: ${info.screenSize}`;
    
    wx.showModal({
      title: '系统信息',
      content: content,
      showCancel: false
    });
  }
});
```

### 4. 页面结构 (pages/index/index.wxml)
```xml
<view class="container">
  <!-- 标题栏 -->
  <view class="header">
    <text class="title">Unify KMP</text>
    <view class="language-switcher">
      <button 
        class="lang-btn {{currentLanguage === 'zh' ? 'active' : ''}}"
        data-lang="zh"
        bindtap="switchLanguage">中</button>
      <button 
        class="lang-btn {{currentLanguage === 'en' ? 'active' : ''}}"
        data-lang="en"
        bindtap="switchLanguage">EN</button>
      <button 
        class="lang-btn {{currentLanguage === 'ja' ? 'active' : ''}}"
        data-lang="ja"
        bindtap="switchLanguage">日</button>
    </view>
  </view>

  <!-- 主内容 -->
  <view class="main-content">
    <!-- 欢迎文本 -->
    <text class="hello-text">{{languages[currentLanguage].hello}}</text>
    
    <!-- 平台信息 -->
    <view class="info-section">
      <text class="info-text">{{languages[currentLanguage].platform}}: {{platformInfo.platform}}</text>
      <text class="info-text">{{languages[currentLanguage].device}}: {{platformInfo.device}}</text>
    </view>

    <!-- 计数器卡片 -->
    <view class="counter-card">
      <text class="counter-label">{{languages[currentLanguage].count}}: {{count}}</text>
      
      <view class="button-group">
        <button class="primary-btn" bindtap="increment">
          {{languages[currentLanguage].increment}}
        </button>
        <button class="secondary-btn" bindtap="reset">
          {{languages[currentLanguage].reset}}
        </button>
      </view>
    </view>

    <!-- 功能按钮 -->
    <view class="action-buttons">
      <button class="action-btn" open-type="share">
        {{languages[currentLanguage].share}}
      </button>
      <button class="action-btn" bindtap="showSystemInfo">
        系统信息
      </button>
    </view>
  </view>
</view>
```

### 5. 页面样式 (pages/index/index.wxss)
```css
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 40rpx;
  background: #ffffff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2937;
}

.language-switcher {
  display: flex;
  gap: 8rpx;
}

.lang-btn {
  width: 60rpx;
  height: 60rpx;
  font-size: 24rpx;
  border-radius: 30rpx;
  background: #f3f4f6;
  color: #6b7280;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lang-btn.active {
  background: #007AFF;
  color: #ffffff;
}

.main-content {
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hello-text {
  font-size: 56rpx;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 40rpx;
  text-align: center;
}

.info-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.info-text {
  font-size: 28rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
}

.counter-card {
  width: 100%;
  max-width: 600rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}

.counter-label {
  font-size: 48rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 48rpx;
}

.button-group {
  display: flex;
  gap: 32rpx;
}

.primary-btn {
  background: #007AFF;
  color: #ffffff;
  border: none;
  border-radius: 16rpx;
  padding: 24rpx 48rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.secondary-btn {
  background: transparent;
  color: #007AFF;
  border: 4rpx solid #007AFF;
  border-radius: 16rpx;
  padding: 20rpx 44rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 24rpx;
  width: 100%;
  max-width: 600rpx;
}

.action-btn {
  flex: 1;
  background: #f8fafc;
  color: #374151;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
}
```

## 🔧 平台特定功能

### 微信 API 集成
```javascript
// utils/wechat-api.js
class WeChatAPI {
  // 获取用户信息
  static getUserProfile() {
    return new Promise((resolve, reject) => {
      wx.getUserProfile({
        desc: '用于完善用户资料',
        success: resolve,
        fail: reject
      });
    });
  }

  // 获取位置信息
  static getLocation() {
    return new Promise((resolve, reject) => {
      wx.getLocation({
        type: 'gcj02',
        success: resolve,
        fail: reject
      });
    });
  }

  // 扫码功能
  static scanCode() {
    return new Promise((resolve, reject) => {
      wx.scanCode({
        success: resolve,
        fail: reject
      });
    });
  }

  // 支付功能
  static requestPayment(paymentData) {
    return new Promise((resolve, reject) => {
      wx.requestPayment({
        ...paymentData,
        success: resolve,
        fail: reject
      });
    });
  }

  // 本地存储
  static setStorage(key, data) {
    return new Promise((resolve, reject) => {
      wx.setStorage({
        key,
        data,
        success: resolve,
        fail: reject
      });
    });
  }

  static getStorage(key) {
    return new Promise((resolve, reject) => {
      wx.getStorage({
        key,
        success: resolve,
        fail: reject
      });
    });
  }
}

module.exports = WeChatAPI;
```

### 网络请求封装
```javascript
// utils/request.js
class Request {
  static baseURL = 'https://api.unify-kmp.com';
  
  static request(options) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.baseURL}${options.url}`,
        method: options.method || 'GET',
        data: options.data || {},
        header: {
          'Content-Type': 'application/json',
          ...options.header
        },
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(res.data);
          } else {
            reject(new Error(`请求失败: ${res.statusCode}`));
          }
        },
        fail: reject
      });
    });
  }

  static get(url, data = {}) {
    return this.request({ url, method: 'GET', data });
  }

  static post(url, data = {}) {
    return this.request({ url, method: 'POST', data });
  }

  static put(url, data = {}) {
    return this.request({ url, method: 'PUT', data });
  }

  static delete(url, data = {}) {
    return this.request({ url, method: 'DELETE', data });
  }
}

module.exports = Request;
```

## 🚀 构建和发布

### 1. 本地开发
```bash
# 使用微信开发者工具
# 1. 打开微信开发者工具
# 2. 导入项目，选择 miniApp 目录
# 3. 填入 AppID
# 4. 点击编译运行
```

### 2. 真机预览
```bash
# 在微信开发者工具中
# 1. 点击预览按钮
# 2. 使用微信扫描二维码
# 3. 在手机上测试应用
```

### 3. 上传发布
```bash
# 在微信开发者工具中
# 1. 点击上传按钮
# 2. 填写版本号和项目备注
# 3. 上传到微信公众平台
# 4. 在公众平台提交审核
```

## 🎨 UI 设计指南

### 小程序设计规范
```css
/* 设计令牌 */
:root {
  --primary-color: #07c160;
  --secondary-color: #576b95;
  --success-color: #07c160;
  --warning-color: #ff976a;
  --error-color: #fa5151;
  --info-color: #10aeff;
  
  --text-primary: #191f25;
  --text-secondary: #8b929a;
  --text-placeholder: #c5cad1;
  
  --bg-primary: #ffffff;
  --bg-secondary: #f7f8fa;
  --bg-tertiary: #edeff3;
  
  --border-color: #e5e5e5;
  --divider-color: #f0f0f0;
  
  --spacing-xs: 8rpx;
  --spacing-sm: 16rpx;
  --spacing-md: 24rpx;
  --spacing-lg: 32rpx;
  --spacing-xl: 48rpx;
  
  --border-radius-sm: 8rpx;
  --border-radius-md: 12rpx;
  --border-radius-lg: 16rpx;
  --border-radius-xl: 24rpx;
}
```

### 响应式适配
```javascript
// utils/responsive.js
class Responsive {
  static getSystemInfo() {
    return wx.getSystemInfoSync();
  }
  
  static rpxToPx(rpx) {
    const systemInfo = this.getSystemInfo();
    return (rpx / 750) * systemInfo.screenWidth;
  }
  
  static pxToRpx(px) {
    const systemInfo = this.getSystemInfo();
    return (px / systemInfo.screenWidth) * 750;
  }
  
  static isIPhoneX() {
    const systemInfo = this.getSystemInfo();
    return systemInfo.model.includes('iPhone X') || 
           systemInfo.safeArea.bottom < systemInfo.screenHeight;
  }
  
  static getSafeAreaInsets() {
    const systemInfo = this.getSystemInfo();
    return {
      top: systemInfo.safeArea.top,
      bottom: systemInfo.screenHeight - systemInfo.safeArea.bottom
    };
  }
}

module.exports = Responsive;
```

## 🔍 调试和测试

### 调试技巧
```javascript
// 调试工具类
class Debug {
  static log(message, data = null) {
    if (wx.getAccountInfoSync().miniProgram.envVersion !== 'release') {
      console.log(`[Debug] ${message}`, data);
    }
  }
  
  static error(message, error = null) {
    console.error(`[Error] ${message}`, error);
    
    // 在开发环境显示错误提示
    if (wx.getAccountInfoSync().miniProgram.envVersion === 'develop') {
      wx.showModal({
        title: '调试错误',
        content: message,
        showCancel: false
      });
    }
  }
  
  static performance(label, fn) {
    const start = Date.now();
    const result = fn();
    const end = Date.now();
    this.log(`Performance [${label}]: ${end - start}ms`);
    return result;
  }
}

module.exports = Debug;
```

### 性能监控
```javascript
// utils/monitor.js
class Monitor {
  static reportError(error, context = {}) {
    // 上报错误到监控平台
    wx.request({
      url: 'https://monitor.unify-kmp.com/error',
      method: 'POST',
      data: {
        error: error.message,
        stack: error.stack,
        context,
        userAgent: wx.getSystemInfoSync(),
        timestamp: Date.now()
      }
    });
  }
  
  static reportPerformance(metrics) {
    // 上报性能数据
    wx.request({
      url: 'https://monitor.unify-kmp.com/performance',
      method: 'POST',
      data: {
        ...metrics,
        timestamp: Date.now()
      }
    });
  }
  
  static trackPageView(pageName) {
    // 页面访问统计
    wx.request({
      url: 'https://monitor.unify-kmp.com/pageview',
      method: 'POST',
      data: {
        page: pageName,
        timestamp: Date.now()
      }
    });
  }
}

module.exports = Monitor;
```

## ❓ 常见问题

### Q: 小程序包体积过大
**A**: 使用分包加载、压缩图片资源、移除未使用的代码和依赖。

### Q: 网络请求失败
**A**: 检查域名是否在小程序后台配置了合法域名，确保使用 HTTPS 协议。

### Q: 真机和模拟器表现不一致
**A**: 优先以真机表现为准，注意 iOS 和 Android 的差异性。

### Q: 审核被拒
**A**: 仔细阅读审核意见，确保符合微信小程序平台规范和政策要求。

## 📚 参考资源

- [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [小程序设计指南](https://developers.weixin.qq.com/miniprogram/design/)
- [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/devtools.html)
- [小程序云开发](https://developers.weixin.qq.com/miniprogram/dev/wxcloud/basis/getting-started.html)

---

通过本指南，您可以成功构建和发布 Unify KMP 微信小程序，为用户提供便捷的小程序体验。
