plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
    
    // HarmonyOS穿戴设备JVM目标
    jvm("harmonyWear") {
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
        val harmonyWearMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                
                // HarmonyOS穿戴特定依赖
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        
        val harmonyWearTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

// HarmonyOS穿戴应用构建任务
tasks.register("buildHarmonyWearApp") {
    group = "harmony"
    description = "构建HarmonyOS穿戴应用"
    dependsOn("harmonyWearJar")
    
    doLast {
        logger.info("HarmonyOS穿戴应用构建完成")
        logger.info("支持设备: HarmonyOS Watch, 华为手表等")
    }
}
