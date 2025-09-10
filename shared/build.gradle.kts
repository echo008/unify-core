plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
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
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

// SQLDelight配置
sqldelight {
    databases {
        create("UnifyDatabase") {
            packageName.set("com.unify.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}

// Room配置
room {
    schemaDirectory("$projectDir/schemas")
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

    // HarmonyOS 目标 - 暂时通过linuxX64模拟，等待官方支持
    // 实际部署时需要使用HarmonyOS SDK和ArkUI
    // linuxArm64("harmonyos") {
    //     binaries {
    //         executable {
    //             entryPoint = "com.unify.harmonyos.main"
    //         }
    //     }
    // }

    // TV 和 Watch 通过 Android 变体支持

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose UI框架 - 仅在支持的平台使用
                // implementation(compose.runtime)
                // implementation(compose.foundation)
                // implementation(compose.material3)
                // implementation(compose.ui)
                // implementation(compose.materialIconsExtended)

                // 网络请求
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.client.logging)

                // 序列化
                implementation(libs.serialization.json)

                // 协程
                implementation(libs.kotlinx.coroutines.core)
                
                // 日期时间
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                
                // 数据库
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation("org.jetbrains.kotlin:kotlin-test-common:${libs.versions.kotlin.get()}")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:${libs.versions.kotlin.get()}")
            }
        }

        val androidMain by getting {
            dependencies {
                // Compose UI框架 - Android平台
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                
                // Android特定依赖
                implementation(libs.ktor.client.android)
                implementation("androidx.core:core-ktx:1.15.0")
                implementation("androidx.activity:activity-compose:1.9.3")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

                // OkHttp for network operations
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
                
                // Gson for JSON serialization
                implementation("com.google.code.gson:gson:2.10.1")

                // SQLDelight Android驱动
                implementation(libs.sqldelight.android)
                
                // Room数据库依赖
                implementation(libs.room.runtime)
                implementation(libs.room.ktx)

                // DataStore依赖
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("androidx.datastore:datastore-core:1.0.0")
                
                // Android安全存储依赖
                implementation("androidx.security:security-crypto:1.1.0-alpha06")

                // Compose基础依赖
                implementation("androidx.compose.foundation:foundation-layout:1.5.15")
                implementation("androidx.compose.foundation:foundation:1.5.15")
                implementation("androidx.compose.ui:ui-unit:1.5.15")

                // Android测试框架依赖
                implementation("androidx.test.ext:junit:1.1.5")
                implementation("androidx.test.espresso:espresso-core:3.5.1")
                implementation("androidx.test:core:1.5.0")
                implementation("androidx.test:runner:1.5.2")
                implementation("junit:junit:4.13.2")
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // Compose UI框架 - iOS平台
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native)
            }
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val desktopMain by getting {
            dependencies {
                // Compose UI框架 - Desktop平台
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(compose.desktop.common)
                
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.native)
            }
        }

        val jsMain by getting {
            dependencies {
                // Compose UI框架 - Web平台
                implementation(compose.runtime)
                implementation(compose.html.core)
                
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.runtime)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                // Native平台网络和数据库依赖
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.native)
            }
        }

        val linuxX64Main by getting { dependsOn(nativeMain) }
        val macosX64Main by getting { dependsOn(nativeMain) }
        val macosArm64Main by getting { dependsOn(nativeMain) }
        val mingwX64Main by getting { dependsOn(nativeMain) }

        // HarmonyOS 平台配置 - 暂时禁用，等待官方支持
        // val harmonyosMain by getting {
        //     dependsOn(commonMain)
        //     dependencies {
        //         // HarmonyOS 特定依赖 - 仅核心功能，不包含Compose
        //         implementation(libs.ktor.client.cio)
        //         implementation(libs.sqldelight.native)
        //         implementation(libs.kotlinx.coroutines.core)
        //         implementation(libs.serialization.json)
        //     }
        // }

        // 小程序和TV/Watch平台通过现有目标支持，无需额外配置

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${libs.versions.kotlin.get()}")
                implementation("junit:junit:4.13.2")
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
                implementation(libs.kotlinx.coroutines.test)
                implementation("org.jetbrains.kotlin:kotlin-test-junit:${libs.versions.kotlin.get()}")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                // JS平台使用kotlin-test-js，不使用JUnit
            }
        }
        
        val nativeTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        
        val linuxX64Test by getting { dependsOn(nativeTest) }
        val macosX64Test by getting { dependsOn(nativeTest) }
        val macosArm64Test by getting { dependsOn(nativeTest) }
        val mingwX64Test by getting { dependsOn(nativeTest) }
    }
}
