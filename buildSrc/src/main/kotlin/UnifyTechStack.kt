/**
 * Unify-Core 生产级技术栈配置
 * 严格遵循 100% Compose 语法，85%+ 代码复用率
 */
object UnifyTechStack {
    // 核心框架版本 - 使用更稳定的版本避免编译器错误
    const val KOTLIN_VERSION = "1.9.24"
    const val COMPOSE_MULTIPLATFORM_VERSION = "1.6.11"
    const val KMP_VERSION = "1.9.24"
    
    // 基础库
    const val COROUTINES_VERSION = "1.8.1"
    const val SERIALIZATION_VERSION = "1.6.3"
    const val DATETIME_VERSION = "0.6.0"
    
    // 网络和数据
    const val KTOR_VERSION = "2.3.12"
    const val SQLDELIGHT_VERSION = "2.0.2"
    
    // 依赖注入
    const val KOIN_VERSION = "3.5.6"
    
    // 导航
    const val VOYAGER_VERSION = "1.0.0"
    
    // Android 相关
    const val ANDROID_COMPILE_SDK = 34
    const val ANDROID_MIN_SDK = 24
    const val ANDROID_TARGET_SDK = 34
    const val ANDROIDX_ACTIVITY_COMPOSE = "1.9.1"
    const val ANDROIDX_LIFECYCLE = "2.8.4"
    
    // 测试框架
    const val JUNIT_VERSION = "4.13.2"
    const val ANDROIDX_TEST_VERSION = "1.6.1"
    const val COMPOSE_TEST_VERSION = "1.6.8"
    
    // 构建工具
    const val GRADLE_VERSION = "8.5"
    const val AGP_VERSION = "8.5.1"
}

/**
 * 平台支持配置
 */
object UnifyPlatforms {
    // 支持的平台列表
    val SUPPORTED_PLATFORMS = listOf(
        "Android",
        "iOS", 
        "Desktop (JVM)",
        "Web (JS/WASM)",
        "HarmonyOS (适配层)",
        "小程序 (适配层)"
    )
    
    // 目标代码复用率
    const val TARGET_CODE_REUSE_RATE = 85 // 85%+
    
    // 平台特定代码限制
    const val MAX_PLATFORM_SPECIFIC_CODE = 15 // <15%
}

/**
 * 架构设计原则
 */
object UnifyArchitecture {
    // 核心设计约束
    const val PURE_COMPOSE_SYNTAX = true // 100% Jetpack Compose 语法
    const val NO_CUSTOM_DSL = true // 禁止自定义 DSL
    const val PROGRESSIVE_ADAPTATION = true // 渐进式适配
    
    // 模块化设计
    val CORE_MODULES = listOf(
        "shared" to "共享业务逻辑 (85%+)",
        "androidApp" to "Android 宿主应用",
        "iosApp" to "iOS 宿主应用", 
        "desktopApp" to "桌面端宿主应用",
        "webApp" to "Web 宿主应用",
        "harmonyosApp" to "HarmonyOS 适配层",
        "miniProgramAdapter" to "小程序适配层"
    )
    
    // 架构层次
    val ARCHITECTURE_LAYERS = listOf(
        "UI 层" to "纯 Compose 实现",
        "ViewModel 层" to "跨平台业务逻辑",
        "Repository 层" to "数据抽象",
        "Platform 层" to "平台特定实现"
    )
}
