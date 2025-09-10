plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
    
    // HarmonyOS TV JVM目标
    jvm("harmonyTV") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
        withJava()
    }
    
    sourceSets {
        val harmonyTVMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                
                // HarmonyOS TV特定依赖
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        
        val harmonyTVTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

// HarmonyOS TV应用构建任务
tasks.register("buildHarmonyTVApp") {
    group = "harmony"
    description = "构建HarmonyOS TV应用"
    dependsOn("harmonyTVJar")
    
    doLast {
        logger.info("HarmonyOS TV应用构建完成")
        logger.info("支持设备: HarmonyOS TV, 华为智慧屏等")
    }
}
