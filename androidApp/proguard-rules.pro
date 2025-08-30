# 高度优化的 ProGuard 规则 - 针对 KuiklyUI 基准包体积优化
# 目标：Android APK ≤280KB

# 基础优化配置
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保留必要的 Kotlin 元数据（最小化）
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# Compose 运行时优化保留
-keep class androidx.compose.runtime.Composer { *; }
-keep class androidx.compose.runtime.ComposerKt { *; }
-keep class androidx.compose.runtime.State { *; }
-keep class androidx.compose.runtime.MutableState { *; }
-keep class androidx.compose.ui.Modifier { *; }

# 移除未使用的 Compose 组件
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    boolean isTraceInProgress();
    void traceEventStart(int, int, int, java.lang.String);
    void traceEventEnd();
}

# Koin 依赖注入优化
-keep class org.koin.core.** { *; }
-keep class org.koin.android.** { *; }

# Unify 核心类保留（性能关键）
-keep class com.unify.core.performance.** { *; }
-keep class com.unify.core.ui.components.** { *; }
-keep class com.unify.core.state.** { *; }

# 序列化优化
-keepattributes *Annotation*, InnerClasses, Signature
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# 协程优化
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 网络层优化
-keep class io.ktor.client.** { *; }
-keep class io.ktor.http.** { *; }

# 移除调试和日志代码
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 移除 Kotlin 内省
-dontwarn kotlin.reflect.**
-dontwarn kotlin.jvm.internal.**

# 字符串优化
-optimizations !code/simplification/string

# 移除未使用的资源
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 性能监控保留
-keep class com.unify.core.performance.PerformanceProfiler { *; }
-keep class com.unify.core.performance.BenchmarkTestSuite { *; }
