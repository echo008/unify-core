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
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Coroutines & DateTime
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Networking
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.serialization.json)

                // Database
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                // Dependency Injection
                implementation(libs.koin.core)
                implementation(libs.koin.compose)

                // Navigation & Lifecycle
                implementation(libs.navigation.compose)
                implementation(libs.lifecycle.viewmodel)

                // KuiklyUI Framework
                implementation(libs.kuikly.compose)
                implementation(libs.kuikly.runtime)
                implementation(libs.kuikly.performance)
            }
        }
        val androidMain by getting {
            dependencies {
                // Compose
                implementation(compose.preview)
                
                // Android Core
                implementation(libs.androidx.core)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.security.crypto)
                
                // Networking
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp)
                implementation(libs.okhttp.logging)
                
                // Database
                implementation(libs.sqldelight.android)
                implementation(libs.room.runtime)
                implementation(libs.room.ktx)
                
                // Dependency Injection
                implementation(libs.koin.android)
                
                // Lifecycle
                implementation(libs.lifecycle.runtime)
                implementation(libs.lifecycle.viewmodel.ktx)
                
                // Image Loading
                implementation(libs.coil.compose)
                implementation(libs.coil.network)
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
                implementation(libs.kuikly.arkui)
                implementation(libs.kuikly.performance)
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// SQLDelight 配置
sqldelight {
    databases {
        create("UnifyDatabase") {
            packageName.set("com.unify.database")
            srcDirs("src/commonMain/sqldelight")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}

// 构建优化配置
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}

// 内存优化
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xruntime-logs=gc=info",
            "-Xallocator=mimalloc"
        )
    }
}

// JS优化
tasks.withType<org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xir-produce-js",
            "-Xir-minimized-member-names"
        )
    }
}

