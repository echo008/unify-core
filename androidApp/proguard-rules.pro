# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Keep Koin
-keep class org.koin.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }

# Keep SQLDelight
-keep class app.cash.sqldelight.** { *; }

# Keep Unify classes
-keep class com.unify.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }
