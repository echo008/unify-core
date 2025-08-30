# HarmonyOS 平台开发指南

## 🔥 概述

Unify KMP 为 HarmonyOS 平台提供原生开发支持，使用 ArkTS 语言和 ArkUI 框架构建符合 HarmonyOS 设计规范的应用，实现与其他平台的代码逻辑共享。

## 🛠️ 环境要求

### 必需工具
- **DevEco Studio**: 4.0+ (HarmonyOS 开发 IDE)
- **HarmonyOS SDK**: API 9+ (推荐 API 11)
- **Node.js**: 16.0+ (用于构建工具)
- **JDK**: 11 或更高版本
- **操作系统**: Windows 10+, macOS 10.15+, Ubuntu 18.04+

### 安装验证
```bash
# 检查 DevEco Studio 版本
# 在 DevEco Studio 中查看 Help -> About

# 检查 HarmonyOS SDK
# 在 DevEco Studio 中查看 File -> Settings -> HarmonyOS SDK

# 检查 Node.js 版本
node --version

# 检查 JDK 版本
java -version
```

## 🏗️ 项目结构

### HarmonyOS 应用模块
```
harmonyApp/
├── src/
│   └── main/
│       ├── ets/                    # ArkTS 源码目录
│       │   ├── entryability/
│       │   │   └── EntryAbility.ets
│       │   ├── pages/
│       │   │   └── Index.ets       # 主页面
│       │   └── common/
│       │       └── constants/
│       ├── resources/              # 资源文件
│       │   ├── base/
│       │   │   ├── element/
│       │   │   ├── media/
│       │   │   └── profile/
│       │   ├── en_US/
│       │   └── zh_CN/
│       └── module.json5            # 模块配置
├── build-profile.json5             # 构建配置
├── hvigorfile.ts                   # 构建脚本
└── oh-package.json5                # 依赖管理
```

### 模块配置
```json5
// harmonyApp/src/main/module.json5
{
  "module": {
    "name": "entry",
    "type": "entry",
    "description": "$string:module_desc",
    "mainElement": "EntryAbility",
    "deviceTypes": [
      "phone",
      "tablet",
      "2in1"
    ],
    "deliveryWithInstall": true,
    "installationFree": false,
    "pages": "$profile:main_pages",
    "abilities": [
      {
        "name": "EntryAbility",
        "srcEntry": "./ets/entryability/EntryAbility.ets",
        "description": "$string:EntryAbility_desc",
        "icon": "$media:icon",
        "label": "$string:EntryAbility_label",
        "startWindowIcon": "$media:icon",
        "startWindowBackground": "$color:start_window_background",
        "exported": true,
        "skills": [
          {
            "entities": [
              "entity.system.home"
            ],
            "actions": [
              "action.system.home"
            ]
          }
        ]
      }
    ]
  }
}
```

## 💻 核心组件实现

### 1. 应用入口 (EntryAbility.ets)
```typescript
// harmonyApp/src/main/ets/entryability/EntryAbility.ets
import UIAbility from '@ohos.app.ability.UIAbility';
import hilog from '@ohos.hilog';
import window from '@ohos.window';

export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onCreate');
  }

  onDestroy(): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onDestroy');
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onWindowStageCreate');

    windowStage.loadContent('pages/Index', (err, data) => {
      if (err.code) {
        hilog.error(0x0000, 'testTag', 'Failed to load the content. Cause: %{public}s', JSON.stringify(err) ?? '');
        return;
      }
      hilog.info(0x0000, 'testTag', 'Succeeded in loading the content. Data: %{public}s', JSON.stringify(data) ?? '');
    });
  }

  onWindowStageDestroy(): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onWindowStageDestroy');
  }

  onForeground(): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onForeground');
  }

  onBackground(): void {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onBackground');
  }
}
```

### 2. 主页面 (Index.ets)
```typescript
// harmonyApp/src/main/ets/pages/Index.ets
import { PlatformInfo } from '../common/PlatformInfo';

@Entry
@Component
struct Index {
  @State count: number = 0;
  @State currentLanguage: string = 'zh';
  private platformInfo: PlatformInfo = new PlatformInfo();

  // 多语言文本
  private getHelloText(): string {
    const texts = {
      'zh': 'Hello, HarmonyOS!',
      'en': 'Hello, HarmonyOS!',
      'ja': 'こんにちは、HarmonyOS！'
    };
    return texts[this.currentLanguage] || texts['zh'];
  }

  private getCountText(): string {
    const texts = {
      'zh': '计数',
      'en': 'Count',
      'ja': 'カウント'
    };
    return texts[this.currentLanguage] || texts['zh'];
  }

  private getIncrementText(): string {
    const texts = {
      'zh': '增加',
      'en': 'Increment',
      'ja': '増加'
    };
    return texts[this.currentLanguage] || texts['zh'];
  }

  private getResetText(): string {
    const texts = {
      'zh': '重置',
      'en': 'Reset',
      'ja': 'リセット'
    };
    return texts[this.currentLanguage] || texts['zh'];
  }

  build() {
    Column() {
      // 标题栏
      Row() {
        Text('Unify KMP')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
        
        Blank()
        
        // 语言切换按钮
        Row() {
          Button('中')
            .fontSize(12)
            .width(30)
            .height(30)
            .backgroundColor(this.currentLanguage === 'zh' ? '#007AFF' : '#f3f4f6')
            .fontColor(this.currentLanguage === 'zh' ? Color.White : '#6b7280')
            .onClick(() => {
              this.currentLanguage = 'zh';
            })
          
          Button('EN')
            .fontSize(12)
            .width(30)
            .height(30)
            .backgroundColor(this.currentLanguage === 'en' ? '#007AFF' : '#f3f4f6')
            .fontColor(this.currentLanguage === 'en' ? Color.White : '#6b7280')
            .margin({ left: 4 })
            .onClick(() => {
              this.currentLanguage = 'en';
            })
          
          Button('日')
            .fontSize(12)
            .width(30)
            .height(30)
            .backgroundColor(this.currentLanguage === 'ja' ? '#007AFF' : '#f3f4f6')
            .fontColor(this.currentLanguage === 'ja' ? Color.White : '#6b7280')
            .margin({ left: 4 })
            .onClick(() => {
              this.currentLanguage = 'ja';
            })
        }
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 10, bottom: 10 })
      .backgroundColor('#ffffff')

      Divider()
        .color('#e5e7eb')
        .strokeWidth(1)

      // 主内容区域
      Column() {
        // 欢迎文本
        Text(this.getHelloText())
          .fontSize(28)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1f2937')
          .margin({ bottom: 16 })

        // 平台信息
        Text(`Platform: ${this.platformInfo.getPlatformName()}`)
          .fontSize(16)
          .fontColor('#6b7280')
          .margin({ bottom: 8 })

        Text(`Device: ${this.platformInfo.getDeviceInfo()}`)
          .fontSize(14)
          .fontColor('#9ca3af')
          .margin({ bottom: 32 })

        // 计数器卡片
        Column() {
          Text(`${this.getCountText()}: ${this.count}`)
            .fontSize(24)
            .fontWeight(FontWeight.Medium)
            .fontColor('#1f2937')
            .margin({ bottom: 24 })

          // 按钮组
          Row() {
            Button(this.getIncrementText())
              .fontSize(16)
              .fontColor(Color.White)
              .backgroundColor('#007AFF')
              .width(100)
              .height(44)
              .borderRadius(8)
              .onClick(() => {
                this.count++;
              })

            Button(this.getResetText())
              .fontSize(16)
              .fontColor('#007AFF')
              .backgroundColor(Color.Transparent)
              .border({
                width: 2,
                color: '#007AFF'
              })
              .width(80)
              .height(44)
              .borderRadius(8)
              .margin({ left: 16 })
              .onClick(() => {
                this.count = 0;
              })
          }
        }
        .width('100%')
        .padding(24)
        .backgroundColor('#ffffff')
        .borderRadius(12)
        .shadow({
          radius: 8,
          color: '#00000010',
          offsetX: 0,
          offsetY: 2
        })

        Blank()

        // 框架信息
        Column() {
          Text('Framework Information')
            .fontSize(18)
            .fontWeight(FontWeight.Medium)
            .fontColor('#1f2937')
            .margin({ bottom: 12 })

          InfoRow('Framework', 'ArkUI (HarmonyOS)')
          InfoRow('Language', 'ArkTS')
          InfoRow('API Level', 'API 11')
          InfoRow('Build Tool', 'DevEco Studio')
        }
        .width('100%')
        .padding(20)
        .backgroundColor('#f8fafc')
        .borderRadius(8)
        .margin({ top: 20 })
      }
      .layoutWeight(1)
      .padding(20)
      .backgroundColor('#f9fafb')
    }
    .height('100%')
    .width('100%')
  }
}

@Component
struct InfoRow {
  private label: string = '';
  private value: string = '';

  build() {
    Row() {
      Text(this.label)
        .fontSize(14)
        .fontColor('#6b7280')
        .layoutWeight(1)

      Text(this.value)
        .fontSize(14)
        .fontColor('#1f2937')
        .fontWeight(FontWeight.Medium)
    }
    .width('100%')
    .margin({ bottom: 8 })
  }
}
```

### 3. 平台信息类 (PlatformInfo.ets)
```typescript
// harmonyApp/src/main/ets/common/PlatformInfo.ets
import deviceInfo from '@ohos.deviceInfo';
import display from '@ohos.display';

export class PlatformInfo {
  getPlatformName(): string {
    return 'HarmonyOS';
  }

  getDeviceInfo(): string {
    try {
      const brand = deviceInfo.brand || 'Unknown';
      const model = deviceInfo.productModel || 'Unknown';
      const osVersion = deviceInfo.displayVersion || 'Unknown';
      
      return `${brand} ${model} (${osVersion})`;
    } catch (error) {
      return 'HarmonyOS Device';
    }
  }

  getScreenInfo(): string {
    try {
      const defaultDisplay = display.getDefaultDisplaySync();
      const width = defaultDisplay.width;
      const height = defaultDisplay.height;
      const density = defaultDisplay.densityDPI;
      
      return `${width}x${height} @${density}dpi`;
    } catch (error) {
      return 'Unknown Screen';
    }
  }

  getSystemInfo(): SystemInfo {
    return {
      platform: this.getPlatformName(),
      device: this.getDeviceInfo(),
      screen: this.getScreenInfo(),
      apiVersion: deviceInfo.sdkApiVersion || 0,
      securityPatchTag: deviceInfo.securityPatchTag || 'Unknown'
    };
  }
}

export interface SystemInfo {
  platform: string;
  device: string;
  screen: string;
  apiVersion: number;
  securityPatchTag: string;
}
```

## 🎨 UI 设计指南

### HarmonyOS 设计原则
```typescript
// 设计令牌
export const DesignTokens = {
  // 颜色
  colors: {
    primary: '#007AFF',
    primaryVariant: '#0056CC',
    secondary: '#5856D6',
    background: '#f9fafb',
    surface: '#ffffff',
    error: '#ff3b30',
    onPrimary: '#ffffff',
    onSecondary: '#ffffff',
    onBackground: '#1f2937',
    onSurface: '#1f2937',
    onError: '#ffffff'
  },
  
  // 字体
  typography: {
    headline1: { fontSize: 32, fontWeight: FontWeight.Bold },
    headline2: { fontSize: 28, fontWeight: FontWeight.Bold },
    headline3: { fontSize: 24, fontWeight: FontWeight.Medium },
    body1: { fontSize: 16, fontWeight: FontWeight.Normal },
    body2: { fontSize: 14, fontWeight: FontWeight.Normal },
    caption: { fontSize: 12, fontWeight: FontWeight.Normal }
  },
  
  // 间距
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32
  },
  
  // 圆角
  borderRadius: {
    sm: 4,
    md: 8,
    lg: 12,
    xl: 16
  }
};
```

### 响应式布局
```typescript
@Component
struct ResponsiveLayout {
  @State screenWidth: number = 0;
  
  aboutToAppear() {
    this.getScreenWidth();
  }
  
  private getScreenWidth() {
    try {
      const defaultDisplay = display.getDefaultDisplaySync();
      this.screenWidth = defaultDisplay.width;
    } catch (error) {
      this.screenWidth = 360; // 默认宽度
    }
  }
  
  build() {
    if (this.screenWidth < 600) {
      // 手机布局
      this.buildMobileLayout();
    } else if (this.screenWidth < 840) {
      // 平板布局
      this.buildTabletLayout();
    } else {
      // 桌面布局
      this.buildDesktopLayout();
    }
  }
  
  @Builder buildMobileLayout() {
    Column() {
      // 手机端单列布局
    }
  }
  
  @Builder buildTabletLayout() {
    Row() {
      // 平板端双列布局
    }
  }
  
  @Builder buildDesktopLayout() {
    Row() {
      // 桌面端多列布局
    }
  }
}
```

### 动画效果
```typescript
@Component
struct AnimatedButton {
  @State isPressed: boolean = false;
  @State scale: number = 1;
  
  build() {
    Button('Animated Button')
      .scale({ x: this.scale, y: this.scale })
      .animation({
        duration: 150,
        curve: Curve.EaseInOut
      })
      .onTouch((event: TouchEvent) => {
        if (event.type === TouchType.Down) {
          this.isPressed = true;
          this.scale = 0.95;
        } else if (event.type === TouchType.Up) {
          this.isPressed = false;
          this.scale = 1;
        }
      })
  }
}
```

## 🚀 构建和运行

### 1. 开发模式运行
```bash
# 在 DevEco Studio 中
# 1. 连接 HarmonyOS 设备或启动模拟器
# 2. 点击 Run 按钮或使用快捷键 Shift+F10
# 3. 选择目标设备运行应用

# 命令行构建（如果支持）
hvigor assembleHap
```

### 2. 构建 HAP 包
```bash
# 构建调试版本
hvigor assembleHap --mode debug

# 构建发布版本
hvigor assembleHap --mode release

# HAP 包位置
ls harmonyApp/build/default/outputs/default/
```

### 3. 安装和测试
```bash
# 安装到设备
hdc install harmonyApp.hap

# 启动应用
hdc shell aa start -a EntryAbility -b com.unify.harmonyapp

# 查看日志
hdc hilog
```

## 🔧 平台特定功能

### 设备能力调用
```typescript
import sensor from '@ohos.sensor';
import geoLocationManager from '@ohos.geoLocationManager';
import camera from '@ohos.multimedia.camera';

export class DeviceCapabilities {
  // 传感器数据
  async getAccelerometerData(): Promise<sensor.AccelerometerResponse> {
    return new Promise((resolve, reject) => {
      sensor.on(sensor.SensorId.ACCELEROMETER, (data: sensor.AccelerometerResponse) => {
        resolve(data);
      });
    });
  }
  
  // 位置信息
  async getCurrentLocation(): Promise<geoLocationManager.Location> {
    const requestInfo: geoLocationManager.CurrentLocationRequest = {
      priority: geoLocationManager.LocationRequestPriority.FIRST_FIX,
      scenario: geoLocationManager.LocationRequestScenario.UNSET,
      maxAccuracy: 0
    };
    
    return geoLocationManager.getCurrentLocation(requestInfo);
  }
  
  // 相机功能
  async initCamera(): Promise<camera.CameraManager> {
    return camera.getCameraManager(getContext());
  }
}
```

### 数据持久化
```typescript
import preferences from '@ohos.data.preferences';
import relationalStore from '@ohos.data.relationalStore';

export class DataManager {
  private preferencesStore: preferences.Preferences | null = null;
  
  async initPreferences(): Promise<void> {
    const options: preferences.Options = { name: 'unify_kmp_prefs' };
    this.preferencesStore = await preferences.getPreferences(getContext(), options);
  }
  
  async saveString(key: string, value: string): Promise<void> {
    if (this.preferencesStore) {
      await this.preferencesStore.put(key, value);
      await this.preferencesStore.flush();
    }
  }
  
  async getString(key: string, defaultValue: string = ''): Promise<string> {
    if (this.preferencesStore) {
      return await this.preferencesStore.get(key, defaultValue) as string;
    }
    return defaultValue;
  }
  
  // 关系型数据库
  async initDatabase(): Promise<relationalStore.RdbStore> {
    const config: relationalStore.StoreConfig = {
      name: 'unify_kmp.db',
      securityLevel: relationalStore.SecurityLevel.S1
    };
    
    return relationalStore.getRdbStore(getContext(), config);
  }
}
```

## 🔍 调试和测试

### 日志记录
```typescript
import hilog from '@ohos.hilog';

export class Logger {
  private static readonly DOMAIN = 0x0001;
  private static readonly TAG = 'UnifyKMP';
  
  static info(message: string, ...args: any[]): void {
    hilog.info(Logger.DOMAIN, Logger.TAG, message, ...args);
  }
  
  static error(message: string, ...args: any[]): void {
    hilog.error(Logger.DOMAIN, Logger.TAG, message, ...args);
  }
  
  static debug(message: string, ...args: any[]): void {
    hilog.debug(Logger.DOMAIN, Logger.TAG, message, ...args);
  }
  
  static warn(message: string, ...args: any[]): void {
    hilog.warn(Logger.DOMAIN, Logger.TAG, message, ...args);
  }
}
```

### 单元测试
```typescript
// harmonyApp/src/test/ets/test/PlatformInfoTest.ets
import { describe, beforeAll, beforeEach, afterEach, afterAll, it, expect } from '@ohos/hypium';
import { PlatformInfo } from '../../../main/ets/common/PlatformInfo';

export default function PlatformInfoTest() {
  describe('PlatformInfoTest', () => {
    let platformInfo: PlatformInfo;
    
    beforeEach(() => {
      platformInfo = new PlatformInfo();
    });
    
    it('should return correct platform name', () => {
      const platformName = platformInfo.getPlatformName();
      expect(platformName).assertEqual('HarmonyOS');
    });
    
    it('should return device info', () => {
      const deviceInfo = platformInfo.getDeviceInfo();
      expect(deviceInfo).not.assertEqual('');
    });
    
    it('should return system info', () => {
      const systemInfo = platformInfo.getSystemInfo();
      expect(systemInfo.platform).assertEqual('HarmonyOS');
      expect(systemInfo.apiVersion).assertLarger(0);
    });
  });
}
```

### UI 测试
```typescript
// harmonyApp/src/test/ets/test/IndexTest.ets
import { describe, beforeAll, beforeEach, afterEach, afterAll, it, expect } from '@ohos/hypium';
import { Driver, ON } from '@ohos.UiTest';

export default function IndexTest() {
  describe('IndexTest', () => {
    let driver: Driver;
    
    beforeAll(async () => {
      driver = Driver.create();
      await driver.delayMs(2000);
    });
    
    it('should display hello text', async () => {
      const helloText = await driver.findComponent(ON.text('Hello, HarmonyOS!'));
      expect(await helloText.isEnabled()).assertTrue();
    });
    
    it('should increment counter', async () => {
      const incrementButton = await driver.findComponent(ON.text('增加'));
      await incrementButton.click();
      
      const countText = await driver.findComponent(ON.textContains('计数: 1'));
      expect(await countText.isEnabled()).assertTrue();
    });
  });
}
```

## 📦 打包和分发

### 应用签名
```json5
// build-profile.json5
{
  "app": {
    "signingConfigs": [
      {
        "name": "default",
        "type": "HarmonyOS",
        "material": {
          "certpath": "path/to/certificate.p12",
          "storePassword": "your_password",
          "keyAlias": "your_alias",
          "keyPassword": "your_key_password",
          "profile": "path/to/profile.p7b",
          "signAlg": "SHA256withECDSA",
          "verify": true,
          "compatibleVersion": 9
        }
      }
    ],
    "products": [
      {
        "name": "default",
        "signingConfig": "default"
      }
    ]
  }
}
```

### 应用市场发布
```typescript
// 应用配置
export const AppConfig = {
  appId: 'com.unify.harmonyapp',
  versionName: '1.0.0',
  versionCode: 1,
  minCompatibleVersionCode: 1,
  targetApiVersion: 11,
  compatibleApiVersion: 9,
  
  // 权限配置
  permissions: [
    'ohos.permission.INTERNET',
    'ohos.permission.GET_NETWORK_INFO',
    'ohos.permission.LOCATION',
    'ohos.permission.CAMERA'
  ]
};
```

## 🚀 性能优化

### 内存管理
```typescript
export class MemoryManager {
  private static instance: MemoryManager;
  private cache: Map<string, any> = new Map();
  
  static getInstance(): MemoryManager {
    if (!MemoryManager.instance) {
      MemoryManager.instance = new MemoryManager();
    }
    return MemoryManager.instance;
  }
  
  setCache(key: string, value: any, ttl: number = 300000): void {
    this.cache.set(key, {
      value,
      expiry: Date.now() + ttl
    });
  }
  
  getCache(key: string): any {
    const item = this.cache.get(key);
    if (item && item.expiry > Date.now()) {
      return item.value;
    }
    this.cache.delete(key);
    return null;
  }
  
  clearExpiredCache(): void {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (item.expiry <= now) {
        this.cache.delete(key);
      }
    }
  }
}
```

### 启动优化
```typescript
@Entry
@Component
struct SplashScreen {
  @State isLoading: boolean = true;
  
  aboutToAppear() {
    this.initializeApp();
  }
  
  private async initializeApp(): Promise<void> {
    try {
      // 预加载关键资源
      await this.preloadResources();
      
      // 初始化核心服务
      await this.initializeServices();
      
      // 延迟显示主界面
      setTimeout(() => {
        this.isLoading = false;
      }, 1000);
    } catch (error) {
      Logger.error('App initialization failed', error);
    }
  }
  
  private async preloadResources(): Promise<void> {
    // 预加载图片、字体等资源
  }
  
  private async initializeServices(): Promise<void> {
    // 初始化数据库、网络等服务
  }
  
  build() {
    if (this.isLoading) {
      // 启动屏
      Column() {
        Image($r('app.media.logo'))
          .width(120)
          .height(120)
        
        Text('Unify KMP')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
          .margin({ top: 20 })
        
        LoadingProgress()
          .width(40)
          .height(40)
          .margin({ top: 40 })
      }
      .width('100%')
      .height('100%')
      .justifyContent(FlexAlign.Center)
      .backgroundColor('#ffffff')
    } else {
      // 主界面
      Index()
    }
  }
}
```

## ❓ 常见问题

### Q: 应用安装失败
**A**: 检查签名配置和设备调试模式，确保证书和 Profile 文件有效。

### Q: 界面显示异常
**A**: 检查 ArkTS 语法和组件使用，确保遵循 HarmonyOS 开发规范。

### Q: 权限申请失败
**A**: 在 `module.json5` 中正确配置权限，并在代码中动态申请敏感权限。

### Q: 性能问题
**A**: 使用 DevEco Studio 的性能分析工具，优化内存使用和渲染性能。

## 📚 参考资源

- [HarmonyOS 开发者官网](https://developer.harmonyos.com/)
- [ArkTS 语言参考](https://developer.harmonyos.com/cn/docs/documentation/doc-guides-V3/arkts-get-started-0000001504769321-V3)
- [ArkUI 开发指南](https://developer.harmonyos.com/cn/docs/documentation/doc-guides-V3/arkui-overview-0000001184610750-V3)
- [DevEco Studio 用户指南](https://developer.harmonyos.com/cn/docs/documentation/doc-guides-V3/deveco-studio-overview-0000001053582387-V3)

---

通过本指南，您可以成功在 HarmonyOS 平台上构建和部署 Unify KMP 应用，为用户提供符合 HarmonyOS 设计规范的原生体验。
