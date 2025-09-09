package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class MiniAppBridge {
    actual fun getPlatformType(): MiniAppPlatformType {
        return MiniAppPlatformType.UNKNOWN
    }

    actual suspend fun getAppInfo(): MiniAppInfo {
        return MiniAppInfo(
            appId = "js_miniapp",
            version = "1.0.0",
            platform = MiniAppPlatformType.UNKNOWN,
            scene = 1001,
            path = "/pages/index",
            query = emptyMap(),
            referrerInfo = null,
        )
    }

    actual suspend fun getUserInfo(): MiniAppUserInfo? {
        return MiniAppUserInfo(
            nickName = "JS User",
            avatarUrl = "",
            gender = 0,
            country = "Unknown",
            province = "Unknown",
            city = "Unknown",
            language = "zh_CN",
        )
    }

    actual suspend fun callAPI(
        apiName: String,
        params: Map<String, Any>,
    ): MiniAppAPIResult {
        return MiniAppAPIResult(
            success = true,
            data = emptyMap(),
            errorMsg = null,
            errorCode = null,
        )
    }

    actual fun observeLifecycle(): Flow<MiniAppLifecycleEvent> {
        return flowOf(MiniAppLifecycleEvent.OnLaunch)
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
    actual suspend fun setItem(
        key: String,
        value: String,
    ): Boolean {
        return true
    }

    actual suspend fun getItem(key: String): String? {
        return null
    }

    actual suspend fun removeItem(key: String): Boolean {
        return true
    }

    actual suspend fun clear(): Boolean {
        return true
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
            data = "Mock response data",
            headers = emptyMap(),
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
            data = "Upload completed",
            tempFilePath = "/tmp/uploaded_file",
        )
    }

    actual suspend fun downloadFile(
        url: String,
        filePath: String?,
    ): MiniAppDownloadResult {
        return MiniAppDownloadResult(
            statusCode = 200,
            tempFilePath = "/tmp/downloaded_file",
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
        // Show toast in JS environment
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
        // Show loading in JS environment
    }

    actual suspend fun hideLoading() {
        // Hide loading in JS environment
    }

    actual suspend fun navigateTo(url: String) {
        // Navigate to URL in JS environment
    }

    actual suspend fun redirectTo(url: String) {
        // Redirect to URL in JS environment
    }

    actual suspend fun navigateBack(delta: Int) {
        // Navigate back in JS environment
    }
}
