# Unify-Core 版本管理规范

## 版本号规范

采用语义化版本控制 (Semantic Versioning 2.0.0)

### 版本格式
```
MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
```

- **MAJOR**: 不兼容的API修改
- **MINOR**: 向后兼容的功能性新增
- **PATCH**: 向后兼容的问题修正
- **PRERELEASE**: 预发布版本标识 (alpha, beta, rc)
- **BUILD**: 构建元数据

### 版本示例
```
1.0.0          # 正式版本
1.1.0          # 新功能版本
1.1.1          # 修复版本
2.0.0-alpha.1  # 预发布版本
1.0.0+20250910 # 带构建信息
```

## 分支管理策略

### Git Flow 分支模型

```
main (生产分支)
├── develop (开发分支)
├── feature/* (功能分支)
├── release/* (发布分支)
├── hotfix/* (热修复分支)
└── support/* (支持分支)
```

### 分支规范

#### main 分支
- 生产就绪代码
- 只接受来自 release 和 hotfix 分支的合并
- 每次合并都应该打标签

#### develop 分支
- 开发集成分支
- 包含下一个版本的最新开发进度
- 功能分支的合并目标

#### feature 分支
```bash
# 创建功能分支
git checkout -b feature/ai-integration develop

# 完成后合并到 develop
git checkout develop
git merge --no-ff feature/ai-integration
git branch -d feature/ai-integration
```

#### release 分支
```bash
# 创建发布分支
git checkout -b release/1.1.0 develop

# 完成后合并到 main 和 develop
git checkout main
git merge --no-ff release/1.1.0
git tag -a v1.1.0 -m "Release version 1.1.0"

git checkout develop
git merge --no-ff release/1.1.0
git branch -d release/1.1.0
```

#### hotfix 分支
```bash
# 创建热修复分支
git checkout -b hotfix/1.0.1 main

# 完成后合并到 main 和 develop
git checkout main
git merge --no-ff hotfix/1.0.1
git tag -a v1.0.1 -m "Hotfix version 1.0.1"

git checkout develop
git merge --no-ff hotfix/1.0.1
git branch -d hotfix/1.0.1
```

## 发布流程

### 1. 准备发布

```bash
# 1. 从 develop 创建 release 分支
git checkout develop
git pull origin develop
git checkout -b release/1.1.0

# 2. 更新版本号
./scripts/update-version.sh 1.1.0

# 3. 更新 CHANGELOG
./scripts/generate-changelog.sh

# 4. 提交版本更新
git add .
git commit -m "Bump version to 1.1.0"
```

### 2. 测试验证

```bash
# 运行完整测试套件
./gradlew clean test integrationTest

# 运行性能基准测试
./gradlew performanceBenchmark

# 运行安全审计
./gradlew securityAudit

# 构建所有平台
./gradlew buildAllPlatforms
```

### 3. 发布到生产

```bash
# 1. 合并到 main
git checkout main
git merge --no-ff release/1.1.0

# 2. 创建标签
git tag -a v1.1.0 -m "Release version 1.1.0"

# 3. 推送到远程
git push origin main
git push origin v1.1.0

# 4. 合并回 develop
git checkout develop
git merge --no-ff release/1.1.0

# 5. 清理 release 分支
git branch -d release/1.1.0
git push origin --delete release/1.1.0
```

## 自动化脚本

### 版本更新脚本 (scripts/update-version.sh)

```bash
#!/bin/bash
set -e

NEW_VERSION=$1
if [ -z "$NEW_VERSION" ]; then
    echo "Usage: $0 <version>"
    exit 1
fi

# 更新 gradle.properties
sed -i "s/version=.*/version=$NEW_VERSION/" gradle.properties

# 更新 README.md
sed -i "s/version: [0-9]\+\.[0-9]\+\.[0-9]\+/version: $NEW_VERSION/" README.md

# 更新 package.json (如果存在)
if [ -f "package.json" ]; then
    sed -i "s/\"version\": \".*\"/\"version\": \"$NEW_VERSION\"/" package.json
fi

echo "Version updated to $NEW_VERSION"
```

### 变更日志生成脚本 (scripts/generate-changelog.sh)

```bash
#!/bin/bash
set -e

# 获取最新标签
LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")

if [ -z "$LATEST_TAG" ]; then
    echo "No previous tags found, generating full changelog"
    git log --pretty=format:"- %s (%h)" > CHANGELOG_NEW.md
else
    echo "Generating changelog since $LATEST_TAG"
    git log $LATEST_TAG..HEAD --pretty=format:"- %s (%h)" > CHANGELOG_NEW.md
fi

# 合并到现有 CHANGELOG
if [ -f "CHANGELOG.md" ]; then
    echo -e "# Changelog\n\n## [Unreleased]\n" > CHANGELOG_TEMP.md
    cat CHANGELOG_NEW.md >> CHANGELOG_TEMP.md
    echo -e "\n" >> CHANGELOG_TEMP.md
    tail -n +2 CHANGELOG.md >> CHANGELOG_TEMP.md
    mv CHANGELOG_TEMP.md CHANGELOG.md
else
    echo -e "# Changelog\n\n## [Unreleased]\n" > CHANGELOG.md
    cat CHANGELOG_NEW.md >> CHANGELOG.md
fi

rm CHANGELOG_NEW.md
echo "Changelog updated"
```

## 发布检查清单

### 发布前检查

- [ ] 所有测试通过
- [ ] 性能基准测试达标
- [ ] 安全审计通过
- [ ] 文档更新完成
- [ ] 版本号正确更新
- [ ] CHANGELOG 更新
- [ ] 所有平台构建成功

### 发布后检查

- [ ] 标签创建成功
- [ ] 发布说明发布
- [ ] 文档网站更新
- [ ] 依赖项更新通知
- [ ] 社区公告发布

## 版本支持策略

### 长期支持 (LTS) 版本

- 每年发布一个 LTS 版本
- LTS 版本支持 2 年
- 只接受安全修复和严重错误修复

### 常规版本

- 每季度发布一个功能版本
- 支持到下一个功能版本发布
- 接受功能增强和错误修复

### 预发布版本

- Alpha: 功能开发阶段，API 可能变更
- Beta: 功能冻结，仅修复错误
- RC: 发布候选，准备正式发布

## 兼容性保证

### API 兼容性

- MAJOR 版本可以包含破坏性变更
- MINOR 版本必须向后兼容
- PATCH 版本只能包含错误修复

### 平台兼容性

- 新版本必须支持所有当前支持的平台
- 废弃平台支持需要提前一个 MAJOR 版本通知

## 发布渠道

### 正式发布

- GitHub Releases
- Maven Central
- npm Registry (JavaScript 组件)
- CocoaPods (iOS 组件)

### 预发布

- GitHub Packages
- 内部 Maven 仓库
- 测试环境部署

## 回滚策略

### 自动回滚触发条件

- 关键功能失效
- 安全漏洞发现
- 性能严重下降 (>20%)
- 用户报告的严重问题

### 回滚流程

```bash
# 1. 创建 hotfix 分支
git checkout -b hotfix/rollback-1.1.0 v1.0.0

# 2. 应用必要修复
# ... 修复代码 ...

# 3. 发布 hotfix 版本
git tag -a v1.0.1 -m "Hotfix: rollback critical issue"
git push origin v1.0.1

# 4. 部署到生产环境
./scripts/deploy-production.sh v1.0.1
```

## 监控和告警

### 发布监控指标

- 部署成功率
- 应用启动时间
- 错误率变化
- 性能指标对比
- 用户反馈

### 告警设置

- 部署失败立即告警
- 错误率超过阈值告警
- 性能下降超过 10% 告警
- 用户投诉增加告警

---

**文档版本**: 1.0.0  
**最后更新**: 2025-09-10  
**维护者**: Unify-Core 团队
