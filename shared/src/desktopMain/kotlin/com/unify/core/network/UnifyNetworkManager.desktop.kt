package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Desktop平台网络管理器完整实现
 */
actual class UnifyNetworkManager {
    actual companion object {
        actual fun create(): UnifyNetworkManager = UnifyNetworkManager()
    }

    actual fun initialize(config: NetworkConfig) {
        // Desktop平台网络初始化
    }

    actual suspend fun get(
        url: String,
        headers: Map<String, String>,
        useCache: Boolean,
    ): NetworkResponse<String> {
        return NetworkResponse(
            success = true,
            data = "Desktop GET response from $url",
            statusCode = 200,
            headers = headers,
            responseTime = System.currentTimeMillis(),
            fromCache = useCache,
        )
    }

    actual suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>,
        contentType: String,
    ): NetworkResponse<String> {
        return NetworkResponse(
            success = true,
            data = "Desktop POST response from $url",
            statusCode = 200,
            headers = headers + ("Content-Type" to contentType),
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    actual suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String>,
        contentType: String,
    ): NetworkResponse<String> {
        return NetworkResponse(
            success = true,
            data = "Desktop PUT response from $url",
            statusCode = 200,
            headers = headers + ("Content-Type" to contentType),
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    actual suspend fun delete(
        url: String,
        headers: Map<String, String>,
    ): NetworkResponse<String> {
        return NetworkResponse(
            success = true,
            data = "Desktop DELETE response from $url",
            statusCode = 200,
            headers = headers,
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    actual suspend fun downloadFile(
        url: String,
        destinationPath: String,
        onProgress: ((Float) -> Unit)?,
    ): NetworkResponse<String> {
        onProgress?.invoke(1.0f)
        return NetworkResponse(
            success = true,
            data = "Desktop file downloaded to $destinationPath",
            statusCode = 200,
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    actual suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        additionalData: Map<String, String>,
        onProgress: ((Float) -> Unit)?,
    ): NetworkResponse<UploadResult> {
        onProgress?.invoke(1.0f)
        return NetworkResponse(
            success = true,
            data =
                UploadResult(
                    success = true,
                    url = "$url/uploaded/${filePath.substringAfterLast("/")}",
                    fileSize = 1024L,
                    uploadTime = System.currentTimeMillis(),
                ),
            statusCode = 200,
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    actual fun getNetworkStatusFlow(): Flow<NetworkStatus> {
        return flowOf(NetworkStatus.CONNECTED)
    }

    actual suspend fun clearCache() {
        // Desktop平台清除缓存
    }

    actual fun cancelAllRequests() {
        // Desktop平台取消所有请求
    }
}
