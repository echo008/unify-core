plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.unify.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    testOptions {
        targetSdk = 35
    }
    lint {
        targetSdk = 35
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
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    
    // iOS 目标
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // JVM Desktop 目标
    jvm("desktop")
    
    // JavaScript/Web 目标 (支持Web和小程序)
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        nodejs()
        binaries.executable()
    }
    
    // Native 目标 (用于其他平台)
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    
    // HarmonyOS 目标 (使用 Native 编译)
    linuxArm64("harmony")
    
    // TV 和 Watch 通过 Android 变体支持
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                
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
                implementation(libs.androidx.security.crypto)
                
                // Android网络引擎
                implementation(libs.ktor.client.android)
                
                // Android协程
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.ktor.client.cio)
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(libs.ktor.client.js)
            }
        }
        
        val nativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        
        val linuxX64Main by getting { dependsOn(nativeMain) }
        val macosX64Main by getting { dependsOn(nativeMain) }
        val macosArm64Main by getting { dependsOn(nativeMain) }
        val mingwX64Main by getting { dependsOn(nativeMain) }
        
        // HarmonyOS 平台配置
        val harmonyMain by getting {
            dependsOn(nativeMain)
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        
        // 小程序和TV/Watch平台通过现有目标支持，无需额外配置
        
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        
        val iosTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        
        val iosX64Test by getting { dependsOn(iosTest) }
        val iosArm64Test by getting { dependsOn(iosTest) }
        val iosSimulatorArm64Test by getting { dependsOn(iosTest) }
        
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        
        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}