plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.unify.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    testOptions {
        targetSdk = 34
    }
    lint {
        targetSdk = 34
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

kotlin {
    jvmToolchain(17)

    // Android 目标
    androidTarget()

    // JVM 目标 (桌面端) - 启用Desktop支持
    // 使用Compose Multiplatform替代独立的Compose Desktop
    // 避免Kotlin 2.0.21 + Compose Desktop编译器冲突
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                    // 移除可能导致冲突的编译选项
                    freeCompilerArgs.remove("-Xjvm-default=all")
                }
            }
        }
    }

    // iOS 目标
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // Web 目标
    js(IR) {
        browser {
            binaries.executable()
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                
                // 网络请求
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.client.logging)
                
                // 序列化
                implementation(libs.serialization.json)
                
                // 协程
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                
                // Android网络引擎
                implementation(libs.ktor.client.android)
                
                // Android协程
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val iosMain by getting {
            dependencies {
                // iOS网络引擎
                implementation(libs.ktor.client.darwin)
            }
        }

        val jsMain by getting {
            dependencies {
                // Web网络引擎
                implementation(libs.ktor.client.js)
            }
        }
    }
}

// 代码质量检查配置
detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    autoCorrect = true
}

ktlint {
    version.set("0.50.0")
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// 测试覆盖率配置 - 暂时注释，等待Kover插件配置
// kover {
//     reports {
//         total {
//             html {
//                 onCheck = true
//             }
//             xml {
//                 onCheck = true
//             }
//         }
//     }
// }
