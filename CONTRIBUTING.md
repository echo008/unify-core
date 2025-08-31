# 贡献指南

感谢您对 Unify KMP 项目的关注！我们欢迎所有形式的贡献。

## 🚀 快速开始

### 环境准备
- **JDK**: 17+ (推荐 OpenJDK 或 Temurin)
- **IDE**: IntelliJ IDEA 2023.3+ 或 Android Studio Hedgehog+
- **Git**: 2.30+
- **Kotlin**: 2.0.21+
- **Compose Multiplatform**: 1.7.0+

### 本地开发设置
```bash
# 1. Fork 并克隆项目
git clone https://github.com/your-username/unify-core.git
cd unify-core

# 2. 检查环境
./gradlew --version

# 3. 运行测试确保环境正常
./gradlew test

# 4. 构建项目
./gradlew build

# 5. 运行示例应用
./gradlew :shared:run
```

## 📋 贡献类型

### Bug 修复
1. 在 Issues 中搜索是否已有相关问题
2. 如果没有，创建新的 Bug Report
3. Fork 项目并创建修复分支
4. 编写测试用例复现问题
5. 实现修复并确保测试通过
6. 提交 Pull Request

### 新功能开发
1. 先在 Issues 中讨论功能需求
2. 等待维护者确认后开始开发
3. 遵循现有架构模式
4. 编写完整的测试用例
5. 更新相关文档

### 文档改进
1. 修正错误或过时信息
2. 添加缺失的示例代码
3. 改进文档结构和可读性
4. 翻译文档到其他语言

## 🔧 开发规范

### 代码风格
```kotlin
// ✅ 好的示例
@Composable
fun HelloWorldApp(platformName: String = "Unknown") {
    var count by remember { mutableIntStateOf(0) }
    
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌍 Unify KMP",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
```

### 命名约定
- **类名**: PascalCase (`HelloWorldApp`)
- **函数名**: camelCase (`getPlatformName`)
- **变量名**: camelCase (`platformName`)
- **常量**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **包名**: 小写 + 点分隔 (`com.unify.helloworld`)

### 注释规范
```kotlin
/**
 * 跨平台Hello World应用
 * 
 * @param platformName 平台名称，默认为"Unknown"
 */
@Composable
fun HelloWorldApp(platformName: String = "Unknown") {
    // 使用remember保持状态
    var count by remember { mutableIntStateOf(0) }
}
```

## 🧪 测试要求

### 单元测试
```kotlin
class PlatformInfoTest {
    @Test
    fun `getPlatformName should return correct platform name`() {
        val platformName = PlatformInfo.getPlatformName()
        assertTrue(platformName.isNotEmpty())
    }
}
```

### 测试覆盖率
- 新代码必须达到 80% 以上覆盖率
- 核心业务逻辑必须达到 90% 以上覆盖率
- 运行测试: `./gradlew test`
- 生成覆盖率报告: `./gradlew koverHtmlReport`

## 📝 提交规范

### Commit 消息格式
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### 类型说明
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建工具、依赖更新

### 示例
```
feat(android): add platform info display

- Add PlatformInfo.getPlatformName() integration
- Update MainActivity to show platform information
- Add unit tests for platform detection

Closes #123
```

## 🔍 代码审查

### 自检清单
- [ ] 代码遵循项目风格指南
- [ ] 添加了必要的测试用例
- [ ] 测试全部通过
- [ ] 更新了相关文档
- [ ] Commit 消息格式正确
- [ ] 没有引入新的编译警告

### Pull Request 模板
```markdown
## 变更描述
简要描述本次变更的内容和目的

## 变更类型
- [ ] Bug 修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 代码重构

## 测试
- [ ] 添加了新的测试用例
- [ ] 所有测试通过
- [ ] 手动测试验证

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 更新了相关文档
- [ ] 没有破坏性变更
```

## 🏗️ 架构指导

### expect/actual 模式
```kotlin
// commonMain
expect object PlatformInfo {
    fun getPlatformName(): String
}

// androidMain
actual object PlatformInfo {
    actual fun getPlatformName(): String = "Android"
}
```

### Compose 组件设计
- 保持组件纯净和可复用
- 使用 `remember` 管理状态
- 遵循 Material Design 3 规范
- 支持深色模式

### 依赖管理
- 优先使用 Kotlin Multiplatform 官方库
- 避免引入过重的第三方依赖
- 在 `libs.versions.toml` 中统一管理版本

## 🐛 问题报告

### Bug Report 模板
```markdown
**描述问题**
清晰简洁地描述遇到的问题

**复现步骤**
1. 执行 '...'
2. 点击 '....'
3. 滚动到 '....'
4. 看到错误

**期望行为**
描述您期望发生的行为

**实际行为**
描述实际发生的行为

**环境信息**
- OS: [e.g. macOS 14.0]
- Kotlin: [e.g. 2.0.21]
- Gradle: [e.g. 8.5]
- IDE: [e.g. IntelliJ IDEA 2023.3]

**附加信息**
添加任何其他相关信息、截图或日志
```

## 📞 获取帮助

- **GitHub Issues**: 报告问题和功能请求
- **GitHub Discussions**: 技术讨论和问答
- **代码审查**: 通过 Pull Request 获得反馈

## 🙏 致谢

感谢所有为 Unify KMP 项目做出贡献的开发者！

---

**记住**: 好的贡献不仅仅是代码，文档改进、问题报告、功能建议都是宝贵的贡献！
