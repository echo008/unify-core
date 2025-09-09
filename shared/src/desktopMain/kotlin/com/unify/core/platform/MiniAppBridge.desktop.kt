package com.unify.core.platform

/**
 * Desktop平台小程序桥接最小化stub实现
 */
actual class MiniAppBridge {
    actual fun getPlatformType(): MiniAppPlatformType {
        TODO("Desktop平台小程序桥接暂未实现")
    }

    actual suspend fun getAppInfo(): MiniAppInfo {
        TODO("Desktop平台小程序信息获取暂未实现")
    }

    actual suspend fun getUserInfo(): MiniAppUserInfo? {
        TODO("Desktop平台用户信息获取暂未实现")
    }

    actual suspend fun callAPI(
        apiName: String,
        params: Map<String, Any>,
    ): MiniAppAPIResult {
        TODO("Desktop平台API调用暂未实现")
    }

    actual fun observeLifecycle(): kotlinx.coroutines.flow.Flow<MiniAppLifecycleEvent> {
        TODO("Desktop平台生命周期监听暂未实现")
    }

    actual fun getStorageManager(): MiniAppStorageManager {
        TODO("Desktop平台存储管理器暂未实现")
    }

    actual fun getNetworkManager(): MiniAppNetworkManager {
        TODO("Desktop平台网络管理器暂未实现")
    }

    actual fun getUIManager(): MiniAppUIManager {
        TODO("Desktop平台UI管理器暂未实现")
    }
}

/**
 * Desktop平台小程序存储管理器stub实现
 */
actual class MiniAppStorageManager {
    actual suspend fun setItem(
        key: String,
        value: String,
    ): Boolean {
        TODO("Desktop平台存储设置暂未实现")
    }

    actual suspend fun getItem(key: String): String? {
        TODO("Desktop平台存储获取暂未实现")
    }

    actual suspend fun removeItem(key: String): Boolean {
        TODO("Desktop平台存储删除暂未实现")
    }

    actual suspend fun clear(): Boolean {
        TODO("Desktop平台存储清理暂未实现")
    }

    actual suspend fun getKeys(): List<String> {
        TODO("Desktop平台存储键列表暂未实现")
    }
}

/**
 * Desktop平台小程序网络管理器stub实现
 */
actual class MiniAppNetworkManager {
    actual suspend fun request(
        url: String,
        method: String,
        data: Map<String, Any>?,
        headers: Map<String, String>?,
    ): MiniAppNetworkResult {
        TODO("Desktop平台网络请求暂未实现")
    }

    actual suspend fun uploadFile(
        url: String,
        filePath: String,
        name: String,
        formData: Map<String, String>?,
    ): MiniAppUploadResult {
        TODO("Desktop平台文件上传暂未实现")
    }

    actual suspend fun downloadFile(
        url: String,
        filePath: String?,
    ): MiniAppDownloadResult {
        TODO("Desktop平台文件下载暂未实现")
    }
}

/**
 * Desktop平台小程序UI管理器stub实现
 */
actual class MiniAppUIManager {
    actual suspend fun showToast(
        title: String,
        icon: String,
        duration: Int,
    ) {
        TODO("Desktop平台Toast显示暂未实现")
    }

    actual suspend fun showModal(
        title: String,
        content: String,
        showCancel: Boolean,
        cancelText: String,
        confirmText: String,
    ): Boolean {
        TODO("Desktop平台模态框显示暂未实现")
    }

    actual suspend fun showActionSheet(itemList: List<String>): Int? {
        TODO("Desktop平台操作表显示暂未实现")
    }

    actual suspend fun showLoading(title: String) {
        TODO("Desktop平台加载显示暂未实现")
    }

    actual suspend fun hideLoading() {
        TODO("Desktop平台加载隐藏暂未实现")
    }

    actual suspend fun navigateTo(url: String) {
        TODO("Desktop平台导航跳转暂未实现")
    }

    actual suspend fun redirectTo(url: String) {
        TODO("Desktop平台重定向暂未实现")
    }

    actual suspend fun navigateBack(delta: Int) {
        TODO("Desktop平台返回暂未实现")
    }
}
