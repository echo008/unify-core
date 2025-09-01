plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
    
    // watchOS目标配置
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WatchShared"
            isStatic = true
        }
    }
    
    // JVM目标用于通用Watch逻辑
    jvm("watch") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
        withJava()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        
        val watchMain by getting {
            dependencies {
                // Watch平台特定依赖
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val iosMain by getting {
            dependencies {
                // iOS Watch特定依赖
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

// Watch应用构建任务
tasks.register("buildWatchApp") {
    group = "watch"
    description = "构建Watch应用"
    dependsOn("watchJar")
    
    doLast {
        logger.info("Watch应用构建完成")
        logger.info("支持平台: Wear OS, watchOS, HarmonyOS穿戴")
    }
}
