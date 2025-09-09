package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.Foundation.NSUserDefaults

actual class MiniAppBridge {
    actual fun getPlatformType(): MiniAppPlatformType {
        return MiniAppPlatformType.WECHAT // Default for iOS
    }

    actual suspend fun getAppInfo(): MiniAppInfo {
        return MiniAppInfo(
            appId = "ios_miniapp",
            version = "1.0.0",
            platform = MiniAppPlatformType.WECHAT,
            scene = 1001,
            path = "/pages/index/index",
        )
    }

    actual suspend fun getUserInfo(): MiniAppUserInfo? {
        return MiniAppUserInfo(
            nickName = "iOS User",
            avatarUrl = "",
            gender = 0,
            country = "China",
            province = "Beijing",
            city = "Beijing",
            language = "zh_CN",
        )
    }

    actual suspend fun callAPI(
        apiName: String,
        params: Map<String, Any>,
    ): MiniAppAPIResult {
        return MiniAppAPIResult(
            success = true,
            data = mapOf("result" to "iOS API call success"),
            errorMsg = null,
            errorCode = null,
        )
    }

    actual fun observeLifecycle(): Flow<MiniAppLifecycleEvent> {
        return flow {
            emit(MiniAppLifecycleEvent.OnLaunch)
        }
    }

    actual fun getStorageManager(): MiniAppStorageManager {
        return MiniAppStorageManager()
    }

    actual fun getNetworkManager(): MiniAppNetworkManager {
        return MiniAppNetworkManager()
    }

    actual fun getUIManager(): MiniAppUIManager {
        return MiniAppUIManager()
    }
}

actual class MiniAppStorageManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual suspend fun setItem(
        key: String,
        value: String,
    ): Boolean {
        return try {
            userDefaults.setObject(value, key)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun getItem(key: String): String? {
        return userDefaults.stringForKey(key)
    }

    actual suspend fun removeItem(key: String): Boolean {
        return try {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun clear(): Boolean {
        return try {
            val domain = "com.unify.miniapp"
            userDefaults.removePersistentDomainForName(domain)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun getKeys(): List<String> {
        return emptyList()
    }
}

actual class MiniAppNetworkManager {
    actual suspend fun request(
        url: String,
        method: String,
        data: Map<String, Any>?,
        headers: Map<String, String>?,
    ): MiniAppNetworkResult {
        return MiniAppNetworkResult(
            statusCode = 200,
            data = "iOS response",
            headers = headers ?: emptyMap(),
        )
    }

    actual suspend fun uploadFile(
        url: String,
        filePath: String,
        name: String,
        formData: Map<String, String>?,
    ): MiniAppUploadResult {
        return MiniAppUploadResult(
            statusCode = 200,
            data = "iOS upload success",
            tempFilePath = filePath,
        )
    }

    actual suspend fun downloadFile(
        url: String,
        filePath: String?,
    ): MiniAppDownloadResult {
        return MiniAppDownloadResult(
            statusCode = 200,
            tempFilePath = "/tmp/download",
            filePath = filePath,
        )
    }
}

actual class MiniAppUIManager {
    actual suspend fun showToast(
        title: String,
        icon: String,
        duration: Int,
    ) {
        // iOS toast implementation using UIAlertController or custom view
    }

    actual suspend fun showModal(
        title: String,
        content: String,
        showCancel: Boolean,
        cancelText: String,
        confirmText: String,
    ): Boolean {
        return true
    }

    actual suspend fun showActionSheet(itemList: List<String>): Int? {
        return 0
    }

    actual suspend fun showLoading(title: String) {
        // iOS loading implementation
    }

    actual suspend fun hideLoading() {
        // iOS hide loading implementation
    }

    actual suspend fun navigateTo(url: String) {
        // iOS navigation implementation
    }

    actual suspend fun redirectTo(url: String) {
        // iOS redirect implementation
    }

    actual suspend fun navigateBack(delta: Int) {
        // iOS navigate back implementation
    }
}
