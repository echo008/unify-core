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
    androidTarget()
    
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
    }
}