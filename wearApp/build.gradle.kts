plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
}

android {
    namespace = "com.unify.wearapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unify.wearapp"
        minSdk = 26 // Wear OS 2.0+
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation(project(":shared"))
    
    // Wear OS Compose
    implementation("androidx.wear.compose:compose-material:1.3.0")
    implementation("androidx.wear.compose:compose-foundation:1.3.0")
    implementation("androidx.wear.compose:compose-navigation:1.3.0")
    
    // Wear OS Activity
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Wear OS Health Services
    implementation("androidx.health:health-services-client:1.0.0-beta03")
    
    // Wear OS Tiles
    implementation("androidx.wear.tiles:tiles:1.2.0")
    implementation("androidx.wear.tiles:tiles-material:1.2.0")
    
    // Core libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
