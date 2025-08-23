# 统御 (Unify) - KMP 项目骨架

按照《跨端重构设计方案》初始化的项目骨架：

- 模块：`shared/`（KMP + Compose）
- 版本管理：`gradle/libs.versions.toml`
- 构建：根 `build.gradle.kts`、`settings.gradle.kts`

后续可按文档继续添加 `androidApp/`、`iosApp/`、`webApp/`、`miniAppBridge/`、`electronApp/`、`build-logic/` 等模块。

## 前置条件

- JDK 17（建议 Temurin/OpenJDK 发行版）
- 推荐使用 IntelliJ IDEA 或 Android Studio（最新版）
- 初次克隆后需要生成 Gradle Wrapper（仓库尚未包含 `gradlew`）

## 生成 Gradle Wrapper（一次性）

本仓库尚未提交 Gradle Wrapper。请在 `unify-kmp/` 目录执行以下命令生成 Wrapper：

```bash
gradle wrapper --gradle-version 8.4
```

说明：如本机未安装 `gradle`，可通过包管理器安装，或在 IDE 的 Gradle 工具窗口中执行 `wrapper` 任务生成。

## 本地构建

生成 Wrapper 后，执行：

```bash
./gradlew :shared:assemble --stacktrace
./gradlew :shared:check --stacktrace
```

如需查看更多任务：

```bash
./gradlew tasks
```

## CI

已提供 GitHub Actions 工作流 `/.github/workflows/ci.yml`：

- 使用 Temurin JDK 17
- 依赖仓库中的 Gradle Wrapper
- 运行 `:shared:assemble` 与 `:shared:check`

首次推送 Wrapper 后，CI 将自动生效。
