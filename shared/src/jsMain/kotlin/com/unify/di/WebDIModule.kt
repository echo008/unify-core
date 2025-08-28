package com.unify.di

import com.unify.database.WebDatabaseDriver
import com.unify.database.UnifyDatabaseDriverFactory
import com.unify.network.UnifyNetworkServiceImpl
import com.unify.platform.WebPlatformInfoImpl
import com.unify.platform.WebPlatformCapabilitiesImpl
import com.unify.platform.PlatformInfo
import com.unify.platform.PlatformCapabilities
import com.unify.storage.PreferencesStorageImpl
import com.unify.storage.DatabaseStorageImpl
import com.unify.storage.FileSystemStorageImpl
import com.unify.storage.SecureStorageImpl
import com.unify.storage.UnifyStorage
import com.unify.storage.AESStorageEncryptor
import com.unify.storage.StorageEncryptor
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlinx.browser.window
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage

/**
 * Web平台特定的依赖注入模块
 */
fun webDIModule(): Module = module {
    // 平台信息
    single<PlatformInfo> { WebPlatformInfoImpl() }
    single<PlatformCapabilities> { WebPlatformCapabilitiesImpl() }
    
    // 数据库驱动
    single<UnifyDatabaseDriverFactory> { WebDatabaseDriver() }
    
    // 网络服务
    single { UnifyNetworkServiceImpl(get(), get(), get()) }
    
    // 存储加密器
    single<StorageEncryptor> { AESStorageEncryptor() }
    
    // 存储服务
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("preferences")) { 
        PreferencesStorageImpl()
    }
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("database")) { 
        DatabaseStorageImpl()
    }
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("filesystem")) { 
        FileSystemStorageImpl()
    }
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("secure")) { 
        SecureStorageImpl()
    }
    
    // Web特定服务
    single { window }
    single { document }
    single { localStorage }
    single { sessionStorage }
    
    // Web权限管理
    factory { WebPermissionManager() }
    
    // Web生命周期管理
    factory { WebLifecycleManager() }
    
    // Web通知服务
    factory { WebNotificationService() }
    
    // Web文件管理
    factory { WebFileManager() }
    
    // Web缓存管理
    factory { WebCacheManager() }
}

/**
 * Web权限管理器
 */
class WebPermissionManager {
    suspend fun requestCameraPermission(): Boolean {
        return try {
            js("navigator.mediaDevices.getUserMedia({video: true})")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun requestMicrophonePermission(): Boolean {
        return try {
            js("navigator.mediaDevices.getUserMedia({audio: true})")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun requestNotificationPermission(): Boolean {
        return try {
            val result = js("Notification.requestPermission()") as String
            result == "granted"
        } catch (e: Exception) {
            false
        }
    }
    
    fun hasPermission(permission: String): Boolean {
        return when (permission) {
            "notifications" -> js("Notification.permission === 'granted'") as Boolean
            "camera" -> js("'mediaDevices' in navigator") as Boolean
            "location" -> js("'geolocation' in navigator") as Boolean
            else -> false
        }
    }
}

/**
 * Web生命周期管理器
 */
class WebLifecycleManager {
    fun onPageLoad() {
        window.addEventListener("load", { 
            // 页面加载完成时的初始化逻辑
        })
    }
    
    fun onPageUnload() {
        window.addEventListener("beforeunload", { 
            // 页面即将卸载时的清理逻辑
        })
    }
    
    fun onVisibilityChange() {
        document.addEventListener("visibilitychange", {
            if (document.visibilityState == "visible") {
                // 页面变为可见时的逻辑
            } else {
                // 页面变为隐藏时的逻辑
            }
        })
    }
    
    fun onOnline() {
        window.addEventListener("online", {
            // 网络连接恢复时的逻辑
        })
    }
    
    fun onOffline() {
        window.addEventListener("offline", {
            // 网络连接断开时的逻辑
        })
    }
}

/**
 * Web通知服务
 */
class WebNotificationService {
    fun showNotification(title: String, body: String, icon: String? = null) {
        if (js("Notification.permission === 'granted'") as Boolean) {
            val options = js("{}").apply {
                this.asDynamic().body = body
                icon?.let { this.asDynamic().icon = it }
            }
            js("new Notification(title, options)")
        }
    }
    
    fun requestPermission(): Boolean {
        return js("Notification.requestPermission().then(permission => permission === 'granted')") as Boolean
    }
}

/**
 * Web文件管理器
 */
class WebFileManager {
    fun downloadFile(data: String, filename: String, mimeType: String = "text/plain") {
        val blob = js("new Blob([data], {type: mimeType})")
        val url = js("URL.createObjectURL(blob)")
        val link = document.createElement("a").apply {
            setAttribute("href", url as String)
            setAttribute("download", filename)
        }
        document.body?.appendChild(link)
        (link as org.w3c.dom.HTMLElement).click()
        document.body?.removeChild(link)
        js("URL.revokeObjectURL(url)")
    }
    
    fun readFileAsText(file: org.w3c.files.File, callback: (String) -> Unit) {
        val reader = js("new FileReader()")
        reader.asDynamic().onload = { event ->
            callback(event.target.result as String)
        }
        reader.asDynamic().readAsText(file)
    }
    
    fun supportsFileSystemAccess(): Boolean {
        return js("'showOpenFilePicker' in window") as Boolean
    }
}

/**
 * Web缓存管理器
 */
class WebCacheManager {
    fun clearLocalStorage() {
        localStorage.clear()
    }
    
    fun clearSessionStorage() {
        sessionStorage.clear()
    }
    
    fun clearIndexedDB() {
        // 清理IndexedDB的实现
        js("indexedDB.databases().then(databases => databases.forEach(db => indexedDB.deleteDatabase(db.name)))")
    }
    
    fun clearAllCaches() {
        clearLocalStorage()
        clearSessionStorage()
        clearIndexedDB()
        
        if (js("'caches' in window") as Boolean) {
            js("caches.keys().then(names => names.forEach(name => caches.delete(name)))")
        }
    }
    
    fun getCacheSize(): Long {
        // 估算缓存大小
        var size = 0L
        for (i in 0 until localStorage.length) {
            val key = localStorage.key(i)
            if (key != null) {
                val value = localStorage.getItem(key)
                size += key.length + (value?.length ?: 0)
            }
        }
        return size
    }
}
