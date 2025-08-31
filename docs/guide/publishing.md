# Maven Central 发布指南

## 🎯 发布准备概述

本指南详细介绍如何准备和发布 Unify KMP 到 Maven Central，使其可以被其他开发者作为依赖使用。

## 🛠️ 发布环境准备

### 1. 注册Sonatype账号
```bash
# 访问Sonatype JIRA
# https://issues.sonatype.org/secure/Signup!default.jspa

# 创建新项目申请
# Group ID: com.unify.core
# Project URL: https://github.com/unify-kmp/unify-core
# SCM URL: https://github.com/unify-kmp/unify-core.git
```

### 2. 配置GPG签名
```bash
# 安装GPG
brew install gnupg

# 生成GPG密钥
gpg --gen-key

# 查看生成的密钥
gpg --list-keys

# 导出公钥
gpg --export --armor your-email@example.com > public-key.asc

# 上传公钥到keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. 配置Gradle发布插件
```kotlin
// build.gradle.kts (root)
plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka") version "1.9.0"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
```

## 📦 发布配置

### 1. 共享模块发布配置
```kotlin
// shared/build.gradle.kts
plugins {
    id("maven-publish")
    id("signing")
}

group = "com.unify.core"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("shared") {
            from(components["kotlin"])
            artifactId = "unify-core-shared"

            pom {
                name.set("Unify Core Shared")
                description.set("Shared code for Unify KMP framework")
                url.set("https://github.com/unify-kmp/unify-core")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("unify-team")
                        name.set("Unify Team")
                        email.set("team@unify-core.org")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/unify-kmp/unify-core.git")
                    developerConnection.set("scm:git:ssh://github.com/unify-kmp/unify-core.git")
                    url.set("https://github.com/unify-kmp/unify-core")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["shared"])
}
```

### 2. UI组件库发布配置
```kotlin
// ui/build.gradle.kts
plugins {
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka")
}

publishing {
    publications {
        create<MavenPublication>("ui") {
            from(components["kotlin"])
            artifactId = "unify-core-ui"

            pom {
                name.set("Unify Core UI")
                description.set("Cross-platform UI components for Unify KMP framework")
                url.set("https://github.com/unify-kmp/unify-core")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("unify-team")
                        name.set("Unify Team")
                        email.set("team@unify-core.org")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/unify-kmp/unify-core.git")
                    developerConnection.set("scm:git:ssh://github.com/unify-kmp/unify-core.git")
                    url.set("https://github.com/unify-kmp/unify-core")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["ui"])
}

// 生成KDoc文档
tasks.dokkaHtml.configure {
    outputDirectory.set(file("$rootDir/docs/api"))
}
```

### 3. 平台特定模块发布配置
```kotlin
// androidApp/build.gradle.kts
publishing {
    publications {
        create<MavenPublication>("android") {
            from(components["release"])
            artifactId = "unify-core-android"

            pom {
                name.set("Unify Core Android")
                description.set("Android specific implementation for Unify KMP framework")
            }
        }
    }
}

// iosApp/build.gradle.kts
publishing {
    publications {
        create<MavenPublication>("ios") {
            artifactId = "unify-core-ios"
            // iOS Framework发布配置
        }
    }
}
```

## 🔐 安全配置

### 1. 环境变量配置
```bash
# 配置发布凭据
export SONATYPE_USERNAME=your-sonatype-username
export SONATYPE_PASSWORD=your-sonatype-password
export GPG_KEY_ID=your-gpg-key-id
export GPG_KEY_PASSWORD=your-gpg-key-password

# macOS Keychain (推荐)
security add-generic-password -s "sonatype-username" -a "$USER" -w "your-username"
security add-generic-password -s "sonatype-password" -a "$USER" -w "your-password"
```

### 2. Gradle属性文件
```properties
# gradle.properties
signing.gnupg.keyName=your-gpg-key-id
signing.gnupg.passphrase=your-gpg-passphrase

# Sonatype凭据
sonatype.username=your-sonatype-username
sonatype.password=your-sonatype-password
```

## 📋 发布流程

### 1. 版本管理
```kotlin
// 版本号规则
object Versions {
    const val MAJOR = 1
    const val MINOR = 0
    const val PATCH = 0

    // 快照版本
    const val SNAPSHOT = "$MAJOR.$MINOR.${PATCH + 1}-SNAPSHOT"

    // 发布版本
    const val RELEASE = "$MAJOR.$MINOR.$PATCH"
}

// 使用版本
version = if (project.hasProperty("snapshot")) {
    Versions.SNAPSHOT
} else {
    Versions.RELEASE
}
```

### 2. 发布检查清单
```bash
# 发布前检查
./gradlew clean build
./gradlew test
./gradlew detekt
./gradlew ktlintCheck
./gradlew dokkaHtml

# 检查版本号
grep "version" build.gradle.kts

# 检查签名
./gradlew signMavenPublication
```

### 3. 执行发布
```bash
# 发布到Sonatype Staging
./gradlew publishAllPublicationsToSonatypeRepository

# 关闭和发布Staging仓库
./gradlew findSonatypeStagingRepository closeAndReleaseSonatypeStagingRepository
```

### 4. 验证发布
```kotlin
// 在项目中验证依赖
dependencies {
    implementation("com.unify.core:unify-core-shared:1.0.0")
    implementation("com.unify.core:unify-core-ui:1.0.0")
}
```

## 📚 文档发布

### 1. API文档生成
```kotlin
// build.gradle.kts
plugins {
    id("org.jetbrains.dokka") version "1.9.0"
}

tasks.dokkaHtml.configure {
    outputDirectory.set(file("$rootDir/docs/api"))

    dokkaSourceSets.configureEach {
        includeNonPublic.set(false)
        skipDeprecated.set(true)
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/commonMain/kotlin"))
            remoteUrl.set(uri("https://github.com/unify-kmp/unify-core/blob/main/src/commonMain/kotlin"))
            remoteLineSuffix.set("#L")
        }
    }
}
```

### 2. 发布到GitHub Pages
```yaml
# .github/workflows/docs.yml
name: Deploy Docs
on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: 17
          distribution: 'temurin'

      - name: Generate Docs
        run: ./gradlew dokkaHtml

      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./docs
```

## 🔄 版本更新流程

### 1. 版本号更新
```kotlin
// 更新版本号
object Versions {
    const val MAJOR = 1
    const val MINOR = 0
    const val PATCH = 1  // 从0更新到1

    const val RELEASE = "$MAJOR.$MINOR.$PATCH"
}
```

### 2. 更新日志
```markdown
# CHANGELOG.md

## [1.0.1] - 2024-01-15

### Added
- 新增性能监控组件
- 改进文档生成流程

### Fixed
- 修复iOS平台兼容性问题
- 优化内存使用

### Changed
- 更新依赖版本
- 改进构建配置
```

### 3. 标签和发布
```bash
# 创建Git标签
git tag -a v1.0.1 -m "Release version 1.0.1"

# 推送到远程
git push origin v1.0.1

# 在GitHub上创建Release
# 填写发布说明和更新日志
```

## 🎯 最佳实践

### 1. 发布频率
- **快照版本**: 每天或每次主要更改
- **发布版本**: 每2-4周或功能完成后
- **主要版本**: 重大架构更改时

### 2. 版本兼容性
- 遵循语义化版本控制
- 维护向后兼容性
- 及时更新废弃API

### 3. 社区沟通
- 提前宣布重大更改
- 提供迁移指南
- 收集社区反馈

### 4. 安全考虑
- 定期更新依赖
- 监控安全漏洞
- 及时发布安全补丁

通过遵循这个发布指南，您可以确保Unify KMP库能够安全、可靠地发布到Maven Central，为广大开发者提供高质量的跨平台开发工具。
