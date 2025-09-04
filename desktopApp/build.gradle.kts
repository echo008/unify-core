plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    jvm {
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
        val jvmMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.foundation)
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.unify.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Unify KMP Desktop"
            packageVersion = "1.0.0"
            description = "Unify KMP跨平台框架桌面端示例应用"
            copyright = "© 2024 Unify KMP Framework"
            vendor = "Unify KMP Team"
            
            windows {
                menuGroup = "Unify KMP"
                // 升级UUID，避免安装冲突
                upgradeUuid = "18159995-d967-4CD2-8885-77BDE65A4911"
            }
            
            macOS {
                bundleID = "com.unify.desktop"
            }
            
            linux {
                packageName = "unify-kmp-desktop"
            }
        }
    }
}
