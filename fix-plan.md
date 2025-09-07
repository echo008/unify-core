# Unify-Core 项目修复计划

## 项目真实状态 (基于2025-09-06 22:15实际测试)
- **最新检查时间**: 2025-09-06 22:15
- **构建状态**: ✅ **Android平台编译成功** | ❌ **iOS/JS/macOS平台编译失败**
- **主要问题**: expect/actual接口不匹配 + commonMain中使用平台特定API
- **影响范围**: iOS、JS、macOS平台无法编译，Android平台正常
- **紧急程度**: 🔴 **高优先级修复**

## 实际编译错误分析

### ✅ Android平台状态
- **编译状态**: 成功 (BUILD SUCCESSFUL)
- **构建时间**: 17秒
- **错误数量**: 0个

### 🔴 核心编译错误 (基于实际测试)

#### 1. expect/actual接口参数不匹配 (iOS/JS平台)
**错误数量**: 50+个编译错误
**主要文件**: 
- `UnifyPlatformAdapters.ios.kt`
- `UnifyPlatformAdapters.js.kt`

**具体错误类型**:
```
- 'actual fun' has no corresponding expected declaration
- Parameter types are different
- Number of value parameters is different
- Parameter names are different
- Unresolved reference 'PlatformButtonStyle'
- 'when' expression must be exhaustive
```

#### 2. commonMain中使用平台特定API (所有Native平台)
**错误数量**: 30+个编译错误
**主要问题**:
```
- Unresolved reference 'System' (Java特定)
- Unresolved reference 'Runtime' (Java特定)
- Unresolved reference 'Math' (Java特定)
- Unresolved reference 'Thread' (Java特定)
- Unresolved reference 'format' (平台特定)
- Unresolved reference 'javaClass' (Java特定)
```

#### 3. iOS平台特定API引用问题
**错误数量**: 10+个编译错误
**具体问题**:
```
- Unresolved reference 'autoreleasepool'
- Unresolved reference 'NSProcessInfoThermalStateNominal'
- Unresolved reference 'timeIntervalSince1970'
```

#### 4. JS平台Date API引用问题
**错误数量**: 5+个编译错误
**具体问题**:
```
- Unresolved reference 'Date' (需要kotlin.js.Date)
```

## 🔧 基于事实的修复计划

### 阶段1: 修复expect/actual接口不匹配 (紧急)
**预计时间**: 3-4小时
**优先级**: 🔴 最高

**具体修复任务**:
1. **修复平台适配器函数签名不匹配**
   - 统一`UnifyPlatformButton`参数: 移除`style`参数，添加`enabled`参数
   - 统一`UnifyPlatformTextField`参数: 移除`style`参数，添加`enabled`参数
   - 统一`UnifyPlatformSwitch`参数: 添加`enabled`参数
   - 统一`UnifyPlatformSlider`参数: 添加`enabled`参数
   - 统一`UnifyPlatformAlert`参数: 修改为`onConfirm`和`onCancel`回调
   - 统一`UnifyPlatformActionSheet`参数: 修改为简单的字符串列表
   - 修复`UnifyPlatformSegmentedControl`参数名: `onSelectionChange` → `onSelectionChanged`
   - 统一`UnifyPlatformDatePicker`参数: 添加`minDate`和`maxDate`可选参数

2. **添加缺失的样式枚举类**
   - 在commonMain中定义`PlatformButtonStyle`枚举
   - 在commonMain中定义`PlatformTextFieldStyle`枚举
   - 在commonMain中定义`ActionSheetItemStyle`枚举

3. **修复when表达式完整性**
   - 为所有when表达式添加else分支

### 阶段2: 修复commonMain中的平台特定API调用 (紧急)
**预计时间**: 2-3小时
**优先级**: 🔴 最高

**具体修复任务**:
1. **替换Java特定API调用**
   - `System.currentTimeMillis()` → 使用expect/actual机制
   - `Runtime.getRuntime()` → 使用expect/actual机制
   - `Math.sin/cos/PI` → 使用kotlin.math包
   - `Thread.sleep()` → 使用kotlinx.coroutines.delay
   - `String.format()` → 使用字符串模板或expect/actual
   - `javaClass` → 使用expect/actual机制

2. **创建跨平台时间和数学工具**
   - 创建`UnifyTimeUtils`的expect/actual实现
   - 创建`UnifyMathUtils`的expect/actual实现
   - 创建`UnifySystemUtils`的expect/actual实现

### 阶段3: 修复平台特定API引用问题 (高)
**预计时间**: 1-2小时
**优先级**: 🟡 高

**具体修复任务**:
1. **修复iOS平台API引用**
   - 添加正确的Foundation框架导入
   - 修复`autoreleasepool`使用方式
   - 修复热状态常量引用
   - 修复时间戳转换API

2. **修复JS平台API引用**
   - 添加`kotlin.js.Date`导入
   - 修复Performance API使用

### 阶段4: 验证所有平台编译 (高)
**预计时间**: 1小时
**优先级**: 🟡 高

**具体验证任务**:
1. **逐个验证平台编译**
   ```bash
   ./gradlew :shared:compileDebugKotlinAndroid  # ✅ 已通过
   ./gradlew :shared:compileKotlinIosX64        # ❌ 待修复
   ./gradlew :shared:compileKotlinJs            # ❌ 待修复
   ./gradlew :shared:compileKotlinMacosX64      # ❌ 待修复
   ./gradlew :shared:compileKotlinLinuxX64      # 待测试
   ./gradlew :shared:compileKotlinMingwX64      # 待测试
   ```

2. **验证HarmonyOS和其他平台**
   ```bash
   ./gradlew :shared:compileKotlinHarmony       # 待测试
   ```

## ⏰ 实际修复时间表

| 阶段 | 任务 | 预计时间 | 状态 |
|------|------|----------|---------|
| 阶段1 | expect/actual接口不匹配修复 | 3-4小时 | ⏳ 待开始 |
| 阶段2 | commonMain平台特定API修复 | 2-3小时 | ⏳ 待开始 |
| 阶段3 | 平台特定API引用修复 | 1-2小时 | ⏳ 待开始 |
| 阶段4 | 所有平台编译验证 | 1小时 | ⏳ 待开始 |

**总计预估时间**: 7-10小时 (1-2个工作日)

## 🎯 成功标准 (基于实际测试)

### 编译成功标准
- [x] Android平台编译通过 (0个错误) ✅
- [ ] iOS平台编译通过 (iosX64)
- [ ] Web/JS平台编译通过
- [ ] macOS平台编译通过 (macosX64)
- [ ] Linux平台编译通过 (linuxX64)
- [ ] Windows平台编译通过 (mingwX64)
- [ ] HarmonyOS平台编译通过
- [ ] 所有expect/actual接口匹配
- [ ] 构建时间 < 10分钟

### 质量标准
- [ ] 0个编译错误
- [ ] 8大平台构建成功
- [ ] expect/actual接口一致性100%
- [ ] commonMain无平台特定API调用
- [ ] 所有平台特定API正确引用

## ⚠️ 风险评估 (基于实际情况)

### 🔴 高风险
1. **expect/actual接口设计不一致**
   - 多个平台的actual实现与expect声明不匹配
   - 影响: 需要大量接口重构，可能需要4-6小时

2. **commonMain架构设计问题**
   - 大量使用Java特定API，违反跨平台原则
   - 影响: 需要重新设计工具类，可能需要3-4小时

3. **平台特定API依赖过多**
   - iOS、JS等平台有大量特定API引用问题
   - 影响: 需要逐个平台适配，可能需要2-3小时

### 🟡 中风险  
1. **构建配置复杂性**
   - 8大平台的构建配置可能存在其他隐藏问题
   - 影响: 可能需要额外调试时间

2. **依赖版本兼容性**
   - Kotlin Multiplatform版本与某些API可能不兼容
   - 影响: 可能需要降级或升级依赖

## 📋 下一步行动

1. **立即开始**: 修复expect/actual接口不匹配问题，从平台适配器开始
2. **逐步验证**: 修复一个模块立即测试编译，确保进展
3. **系统性修复**: 按优先级修复commonMain中的平台特定API调用
4. **全面验证**: 确保所有8大平台编译通过
5. **记录进度**: 实时更新修复状态和新发现的问题

## 🔍 问题根因分析

### 为什么之前修复进展缓慢？
1. **问题定位完全错误**: 之前认为Android编译失败，实际上Android编译正常
2. **缺乏实际测试验证**: 没有逐个平台进行编译测试，导致分析基于错误假设
3. **架构设计问题**: commonMain中大量使用Java特定API，违反跨平台设计原则
4. **expect/actual机制实现不完整**: 平台适配器的接口定义与实现严重不匹配
5. **平台特定API使用错误**: iOS、JS等平台的API引用方式不正确

### 本次修复的改进
1. **基于实际编译测试**: 逐个平台进行编译测试，获得真实错误信息
2. **系统性问题分析**: 识别出expect/actual不匹配和commonMain设计问题两大核心问题
3. **优先级明确**: 先修复阻塞性的接口问题，再修复架构问题
4. **验证驱动**: 每个修复都立即验证，确保真实进展
5. **全平台覆盖**: 确保8大平台都能编译通过，不遗漏任何平台

### 技术债务识别
1. **commonMain架构问题**: 需要重新设计跨平台工具类
2. **expect/actual接口设计**: 需要统一所有平台的接口实现
3. **平台特定API封装**: 需要正确封装各平台的原生API

---

**当前状态**: ✅ **Android编译成功** | ❌ **iOS/JS/macOS编译失败，expect/actual接口不匹配**  
**下一步**: 立即修复平台适配器的expect/actual接口不匹配问题  
**预期完成**: 1-2个工作日内实现所有8大平台编译通过
