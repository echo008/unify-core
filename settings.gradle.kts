pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        ivy {
            url = uri("https://nodejs.org/dist/")
            patternLayout {
                artifact("v[revision]/[artifact]-v[revision]-[classifier].[ext]")
            }
            metadataSources {
                artifact()
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "unify-core"
include(":shared")
include(":androidApp")
include(":iosApp")
include(":webApp")
include(":desktopApp")
include(":harmonyApp")     // HarmonyOS应用
include(":miniApp")        // 小程序应用
include(":miniAppBridge")  // 小程序桥接层
// include(":watchApp")   // Watch应用 - 暂时注释，待平台支持完善
// include(":tvApp")      // TV应用 - 暂时注释，待平台支持完善