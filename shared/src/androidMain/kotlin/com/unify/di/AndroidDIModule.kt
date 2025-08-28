package com.unify.di

import android.content.Context
import com.unify.cache.initializeAndroidCache
import com.unify.database.AndroidDatabaseDriver
import com.unify.database.UnifyDatabaseDriverFactory
import com.unify.network.UnifyNetworkServiceImpl
import com.unify.platform.AndroidPlatformInfoImpl
import com.unify.platform.AndroidPlatformCapabilitiesImpl
import com.unify.platform.PlatformInfo
import com.unify.platform.PlatformCapabilities
import com.unify.platform.initializeAndroidPlatform
import com.unify.storage.PreferencesStorageImpl
import com.unify.storage.DatabaseStorageImpl
import com.unify.storage.FileSystemStorageImpl
import com.unify.storage.SecureStorageImpl
import com.unify.storage.UnifyStorage
import com.unify.storage.AESStorageEncryptor
import com.unify.storage.StorageEncryptor
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android平台特定的依赖注入模块
 */
fun androidDIModule(context: Context): Module = module {
    // 初始化Android平台
    single { 
        initializeAndroidPlatform(context)
        initializeAndroidCache(context)
        context
    }
    
    // 平台信息
    single<PlatformInfo> { AndroidPlatformInfoImpl() }
    single<PlatformCapabilities> { AndroidPlatformCapabilitiesImpl() }
    
    // 数据库驱动
    single<UnifyDatabaseDriverFactory> { AndroidDatabaseDriver(context) }
    
    // 网络服务
    single { UnifyNetworkServiceImpl(get(), get(), get()) }
    
    // 存储加密器
    single<StorageEncryptor> { AESStorageEncryptor() }
    
    // 存储服务
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("preferences")) { 
        PreferencesStorageImpl().apply { initialize(context) }
    }
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("database")) { 
        DatabaseStorageImpl()
    }
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("filesystem")) { 
        FileSystemStorageImpl().apply { initialize(context) }
    }
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("secure")) { 
        SecureStorageImpl().apply { initialize(context) }
    }
    
    // Android特定服务
    single { context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager }
    single { context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager }
    single { context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }
    single { context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager }
    single { context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager }
    
    // Android权限管理
    factory { AndroidPermissionManager(context) }
    
    // Android生命周期管理
    factory { AndroidLifecycleManager() }
    
    // Android通知服务
    factory { AndroidNotificationService(get()) }
    
    // Android文件管理
    factory { AndroidFileManager(context) }
}

/**
 * Android权限管理器
 */
class AndroidPermissionManager(private val context: Context) {
    fun hasPermission(permission: String): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context, permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    fun getRequiredPermissions(): List<String> {
        return listOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.WAKE_LOCK
        )
    }
}

/**
 * Android生命周期管理器
 */
class AndroidLifecycleManager {
    fun onAppStart() {
        // 应用启动时的初始化逻辑
    }
    
    fun onAppStop() {
        // 应用停止时的清理逻辑
    }
    
    fun onLowMemory() {
        // 内存不足时的处理逻辑
        System.gc()
    }
}

/**
 * Android通知服务
 */
class AndroidNotificationService(
    private val notificationManager: android.app.NotificationManager
) {
    fun showNotification(title: String, message: String, channelId: String = "default") {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Default Channel",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        // 创建和显示通知的逻辑
    }
    
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

/**
 * Android文件管理器
 */
class AndroidFileManager(private val context: Context) {
    fun getInternalStorageDir(): java.io.File = context.filesDir
    fun getCacheDir(): java.io.File = context.cacheDir
    fun getExternalStorageDir(): java.io.File? = context.getExternalFilesDir(null)
    
    fun createTempFile(prefix: String, suffix: String): java.io.File {
        return java.io.File.createTempFile(prefix, suffix, context.cacheDir)
    }
    
    fun deleteFile(file: java.io.File): Boolean = file.delete()
    
    fun getFileSize(file: java.io.File): Long = if (file.exists()) file.length() else 0L
}

actual fun createPlatformInfo(): PlatformInfo {
    return AndroidPlatformInfoImpl()
}

actual fun createPlatformCapabilities(): PlatformCapabilities {
    return AndroidPlatformCapabilitiesImpl()
}
