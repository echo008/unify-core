plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    id("com.github.ben-manes.versions") version "0.51.0" apply false
}

allprojects {
    group = "com.unify.core"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/central")
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Android构建任务
tasks.register("buildAndroid") {
    group = "build"
    description = "构建Android应用"
    dependsOn(":androidApp:assembleDebug")
}

// 依赖更新检查任务
tasks.register("dependencyUpdates") {
    group = "verification"
    description = "检查依赖更新"
}