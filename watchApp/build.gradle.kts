plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
    
    // 仅支持watchOS Native目标，移除JVM依赖以避免平台冲突
    watchosArm64 {
        binaries.framework {
            baseName = "WatchShared"
            isStatic = true
        }
    }
    
    watchosSimulatorArm64 {
        binaries.framework {
            baseName = "WatchShared"
            isStatic = true
        }
    }
    
    watchosX64 {
        binaries.framework {
            baseName = "WatchShared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // 移除对shared模块的直接依赖，避免JVM平台冲突
            // 仅使用必要的Compose组件
            implementation(compose.runtime)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        
        val watchosMain by creating {
            dependsOn(commonMain.get())
        }
        
        val watchosArm64Main by getting { dependsOn(watchosMain) }
        val watchosSimulatorArm64Main by getting { dependsOn(watchosMain) }
        val watchosX64Main by getting { dependsOn(watchosMain) }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Watch应用构建任务
tasks.register("buildWatchApp") {
    group = "watch"
    description = "构建Watch应用"
    dependsOn("assemble")
    
    doLast {
        logger.info("Watch应用构建完成")
        logger.info("支持平台: watchOS")
    }
}
