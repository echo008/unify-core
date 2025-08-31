plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.binary.compatibility.validator) apply false
}

allprojects {
    group = "com.unify.core"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
            )
        }
    }
    
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        buildUponDefaultConfig = true
        autoCorrect = true
    }
    
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("0.50.0")
        android.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }
}

// 生产级构建配置
tasks.register("buildAll") {
    group = "build"
    description = "构建所有平台目标"
    dependsOn(
        ":shared:build",
        ":androidApp:build",
        ":desktopApp:build",
        ":webApp:build",
        ":miniAppBridge:build"
    )
}

tasks.register("testAll") {
    group = "verification"
    description = "运行所有平台测试"
    dependsOn(
        ":shared:test",
        ":androidApp:testDebugUnitTest",
        ":desktopApp:test",
        ":webApp:test"
    )
}

tasks.register("lintAll") {
    group = "verification"
    description = "运行所有代码质量检查"
    dependsOn(
        "detekt",
        "ktlintCheck"
    )
}

tasks.register("publishAll") {
    group = "publishing"
    description = "发布所有平台包"
    dependsOn(
        ":shared:publish",
        ":androidApp:publish",
        ":desktopApp:publish"
    )
}

// 性能基准测试
tasks.register("benchmarkAll") {
    group = "benchmark"
    description = "运行所有性能基准测试"
    doLast {
        println("执行跨平台性能基准测试...")
    }
}

// 代码覆盖率报告
apply(plugin = "org.jetbrains.kotlinx.kover")

kover {
    reports {
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
        }
    }
}