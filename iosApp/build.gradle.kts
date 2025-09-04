plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "iosApp"
            isStatic = true
            
            // 导出共享模块
            export(project(":shared"))
        }
    }
    
    sourceSets {
        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        
        val iosX64Test by getting {
            dependsOn(iosTest)
        }
        
        val iosArm64Test by getting {
            dependsOn(iosTest)
        }
        
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
    }
}

// iOS构建任务
tasks.register("buildIosApp") {
    group = "ios"
    description = "构建iOS应用框架"
    dependsOn("linkDebugFrameworkIosX64", "linkReleaseFrameworkIosX64")
    
    doLast {
        logger.info("iOS应用框架构建完成")
        logger.info("输出路径: build/bin/")
    }
}

tasks.register("packageIosApp") {
    group = "ios"
    description = "打包iOS应用"
    dependsOn("buildIosApp")
    
    doLast {
        logger.info("iOS应用打包完成")
    }
}
