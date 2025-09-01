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
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

kotlin {
    jvmToolchain(17)

    // Android 目标
    androidTarget()

    // JVM 目标 (桌面端)
    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    // HarmonyOS 目标 (通过JVM实现)
    jvm("harmony") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    // Watch 目标 (通过JVM实现)
    jvm("watch") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }

    // TV 目标 (通过JVM实现)
    jvm("tv") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.configure {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

    // MiniApp 目标 (通过JS实现)
    js("miniApp", IR) {
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

        val androidTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val iosMain by getting {
            dependencies {
                // iOS网络引擎
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jsMain by getting {
            dependencies {
                // Web网络引擎
                implementation(libs.ktor.client.js)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val desktopMain by getting {
            dependencies {
                // Desktop网络引擎
                implementation(libs.ktor.client.cio)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // HarmonyOS平台源集
        val harmonyMain by getting {
            dependencies {
                // HarmonyOS网络引擎
                implementation(libs.ktor.client.cio)
            }
        }

        val harmonyTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // Watch平台源集
        val watchMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        val watchTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // TV平台源集
        val tvMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }

        val tvTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // MiniApp平台源集
        val miniAppMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val miniAppTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
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

// 测试覆盖率配置
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
