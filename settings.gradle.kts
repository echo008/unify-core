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
include(":miniAppBridge")  // 小程序桥接层
include(":wearApp")       // Wear OS应用
include(":watchApp")      // watchOS应用
include(":tvApp")         // Android TV应用
include(":harmonyWearApp") // HarmonyOS穿戴应用
include(":harmonyTVApp")   // HarmonyOS TV应用