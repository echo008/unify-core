plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "UnifyKmp"
            isStatic = true
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    // HarmonyOS support via Kotlin/Native (placeholder target name ohosArm64)
    val ohosArm64 = linuxArm64("ohosArm64") {
        binaries {
            executable {
                entryPoint = "main"
            }
            sharedLib {
                baseName = "unify-kmp"
            }
        }
        compilations.getByName("main") {
            cinterops {
                val harmonyos by creating {
                    defFile(project.file("src/ohosMain/cinterop/harmonyos.def"))
                    packageName("harmonyos")
                }
            }
        }
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization)
                implementation(libs.serialization.json)

                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                implementation(libs.navigation.compose)
                implementation(libs.lifecycle.viewmodel)

                // 性能监控和测试依赖
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.ktor.client.android)
                implementation(libs.sqldelight.android)
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
                implementation(compose.html.core)
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.web)
            }
        }
        val ohosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kuikly.compose)
                implementation(libs.kuikly.runtime)
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.native)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(libs.ktor.client.js)
            }
        }
    }
}

android {
    namespace = "com.unify.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

