plugins {
    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}
sqldelight {
    databases {
        create("UnifyDatabase") {
            packageName.set("com.unify.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
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
                implementation(libs.koin.core)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.content.negotiation)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
            }
        }
        val androidMain by getting {
            dependencies {
                // Android 特定依赖
                implementation(libs.androidx.core)
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.android)
                implementation(libs.koin.android)
                implementation(libs.koin.core)
            }
        }
        
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native)
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                // Web平台暂时不使用SQLDelight，使用localStorage
            }
        }
        
        // 桌面端依赖禁用，使用独立桌面应用
        // val desktopMain by getting {
        //     dependencies {
        //         implementation(libs.ktor.client.cio)
        //         implementation(compose.desktop.currentOs)
        //     }
        // }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}