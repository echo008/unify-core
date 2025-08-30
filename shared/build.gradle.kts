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

    // JVM 目标 (桌面端) - 暂时禁用，使用独立桌面应用
    // 注意：Kotlin 2.0.21 + Compose Desktop存在编译器内部错误
    // 解决方案：创建独立的desktopApp模块，避免shared模块依赖冲突
    // jvm("desktop") {
    //     compilations.all {
    //         compileTaskProvider.configure {
    //             compilerOptions {
    //                 jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    //                 freeCompilerArgs.add("-Xjvm-default=all")
    //             }
    //         }
    //     }
    // }

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
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("io.ktor:ktor-client-logging:2.3.7")
                
                // 序列化
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                
                // 协程
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
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
                implementation("io.ktor:ktor-client-android:2.3.7")
                
                // Android协程
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            }
        }

        val iosMain by getting {
            dependencies {
                // iOS网络引擎
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
        }

        val jsMain by getting {
            dependencies {
                // Web网络引擎
                implementation("io.ktor:ktor-client-js:2.3.7")
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
