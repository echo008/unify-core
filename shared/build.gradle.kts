plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
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
        kotlinCompilerExtensionVersion = "1.5.10"
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
        iosSimulatorArm64()
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
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
        
        val iosMain by getting {
            dependencies {
                // iOS 特定依赖
            }
        }
        
        val jsMain by getting {
            dependencies {
                // Web 特定依赖
            }
        }
    }
}