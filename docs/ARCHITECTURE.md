# Unify KMP 架构文档

## 整体架构

Unify KMP 采用分层架构设计，确保代码的可维护性、可扩展性和跨平台兼容性。

```
┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Application Layer)                │
├─────────────────┬─────────────────┬─────────────────────────┤
│   Android App   │    iOS App      │      Web App            │
├─────────────────┼─────────────────┼─────────────────────────┤
│                    表示层 (Presentation Layer)               │
├─────────────────────────────────────────────────────────────┤
│                    业务逻辑层 (Business Layer)               │
├─────────────────────────────────────────────────────────────┤
│                    数据访问层 (Data Access Layer)           │
├─────────────────────────────────────────────────────────────┤
│                    平台抽象层 (Platform Abstraction)        │
├─────────────────┬─────────────────┬─────────────────────────┤
│  Android Impl   │   iOS Impl      │     Web Impl            │
└─────────────────┴─────────────────┴─────────────────────────┘
```

## 核心模块

### 1. UI 层 (com.unify.ui)

#### 组件系统
- **UnifyTheme**: 统一主题系统
- **UnifyComponentLibrary**: 跨平台组件库
- **UnifyComponentProtocol**: 组件协议接口

#### 状态管理
- **UnifyViewModel**: 基于 MVI 的 ViewModel 基类
- **EnhancedMVIStateManager**: 增强的状态管理器
- **UnifyStateStore**: Redux 风格的状态存储

#### 性能监控
- **ComposePerformanceMonitor**: Compose 性能监控
- **PlatformPerformanceOptimizer**: 平台性能优化器

### 2. 业务逻辑层 (com.unify.business)

#### 数据流管理
- **UnifyDataFlow**: 响应式数据流
- **PaginatedDataFlow**: 分页数据流
- **RealtimeDataFlow**: 实时数据流

#### 状态管理
- **StateStore**: 全局状态管理
- **StateMiddleware**: 状态中间件系统
- **StateSelector**: 状态选择器

### 3. 数据访问层 (com.unify.data)

#### 数据库
- **UnifyDatabaseRepository**: 数据库访问层
- **SQLDelight Schema**: 数据库表结构定义
- **DatabaseProvider**: 数据库提供者

#### 网络服务
- **UnifyNetworkService**: 网络服务接口
- **UnifyNetworkServiceImpl**: 网络服务实现
- **NetworkInterceptor**: 网络拦截器

#### 存储系统
- **UnifyStorage**: 统一存储接口
- **StorageEncryptor**: 存储加密器
- **CacheManager**: 缓存管理器

### 4. 平台抽象层 (com.unify.platform)

#### 平台管理
- **UnifyPlatformManager**: 平台管理器
- **PlatformCapabilities**: 平台能力检测
- **PlatformType**: 平台类型枚举

#### 平台特定实现
- **AndroidPlatformManager**: Android 平台实现
- **IOSPlatformManager**: iOS 平台实现
- **WebPlatformManager**: Web 平台实现

## 设计原则

### 1. 单一职责原则 (SRP)
每个类和模块都有明确的单一职责，便于维护和测试。

### 2. 开闭原则 (OCP)
系统对扩展开放，对修改封闭。通过接口和抽象类实现。

### 3. 依赖倒置原则 (DIP)
高层模块不依赖低层模块，都依赖于抽象。

### 4. 接口隔离原则 (ISP)
使用多个专门的接口，而不是单一的总接口。

## 跨平台策略

### 1. expect/actual 机制
```kotlin
// 共享代码
expect class PlatformManager {
    fun getPlatformName(): String
}

// Android 实现
actual class PlatformManager {
    actual fun getPlatformName(): String = "Android"
}

// iOS 实现
actual class PlatformManager {
    actual fun getPlatformName(): String = "iOS"
}
```

### 2. 接口抽象
```kotlin
interface NetworkService {
    suspend fun get(url: String): NetworkResult<String>
}

// 各平台实现不同的网络库
class AndroidNetworkService : NetworkService { /* 使用 OkHttp */ }
class IOSNetworkService : NetworkService { /* 使用 URLSession */ }
class WebNetworkService : NetworkService { /* 使用 Fetch API */ }
```

### 3. 依赖注入
```kotlin
// 平台特定的依赖注入模块
object AndroidDI {
    val networkService: NetworkService = AndroidNetworkService()
    val storage: Storage = AndroidStorage()
}

object IOSDI {
    val networkService: NetworkService = IOSNetworkService()
    val storage: Storage = IOSStorage()
}
```

## 数据流架构

### 1. MVI 模式
```
Intent → Reducer → State → View
   ↑                        ↓
   └── User Interaction ←────┘
```

### 2. 状态管理流程
```
Action Dispatch → Middleware → Reducer → New State → UI Update
                     ↓
                 Side Effects → External Services
```

### 3. 数据流向
```
UI Layer → ViewModel → Repository → Network/Database
    ↓         ↓           ↓              ↓
 State    Intent    DataState      Raw Data
```

## 性能优化策略

### 1. 组件级优化
- 组件缓存和复用
- 懒加载和虚拟化
- 性能监控和分析

### 2. 数据层优化
- 数据缓存策略
- 分页加载
- 预加载机制

### 3. 平台特定优化
- Android: ProGuard/R8 代码混淆
- iOS: 编译器优化
- Web: 代码分割和懒加载

## 测试策略

### 1. 单元测试
- 业务逻辑测试
- 数据层测试
- 工具类测试

### 2. 集成测试
- API 集成测试
- 数据库集成测试
- 跨平台兼容性测试

### 3. UI 测试
- Compose 测试
- 端到端测试
- 视觉回归测试

## 扩展指南

### 1. 添加新平台
1. 创建平台特定的源集
2. 实现 expect 声明的 actual 类
3. 创建平台特定的依赖注入模块
4. 实现平台特定的应用入口

### 2. 添加新功能
1. 在 commonMain 中定义接口
2. 实现业务逻辑
3. 添加平台特定实现
4. 编写测试用例
5. 更新文档

### 3. 性能优化
1. 使用性能监控工具
2. 分析瓶颈点
3. 实施优化策略
4. 验证优化效果

## 最佳实践

### 1. 代码组织
- 按功能模块组织代码
- 使用清晰的包结构
- 保持接口简洁明了

### 2. 错误处理
- 统一的错误处理机制
- 详细的错误日志
- 用户友好的错误提示

### 3. 文档维护
- 及时更新 API 文档
- 提供使用示例
- 维护变更日志

### 4. 版本管理
- 语义化版本控制
- 向后兼容性考虑
- 迁移指南提供
