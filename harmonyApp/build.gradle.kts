plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
    
    // HarmonyOS通过JVM目标实现
    jvm("harmony") {
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
        val harmonyMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                
                // HarmonyOS特定依赖
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val harmonyTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

// HarmonyOS应用配置
tasks.register("buildHarmonyApp") {
    group = "harmony"
    description = "构建HarmonyOS应用"
    dependsOn("harmonyJar")
    
    doLast {
        println("HarmonyOS应用构建完成")
        println("输出路径: build/libs/")
    }
}

tasks.register("packageHarmonyApp") {
    group = "harmony"
    description = "打包HarmonyOS应用为HAP"
    dependsOn("buildHarmonyApp")
    
    doLast {
        println("HarmonyOS HAP包构建完成")
    }
}
