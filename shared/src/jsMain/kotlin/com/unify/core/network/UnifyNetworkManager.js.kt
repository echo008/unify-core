package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class UnifyNetworkManager {
    actual companion object {
        actual fun create(): UnifyNetworkManager {
            return UnifyNetworkManager()
        }
    }

    actual fun initialize(config: NetworkConfig) {
        // Initialize network manager in JS environment
    }

    actual suspend fun get(
        url: String,
        headers: Map<String, String>,
        useCache: Boolean,
    ): NetworkResponse<String> {
        return NetworkResponse(
            success = true,
            data = "Mock GET response",
            statusCode = 200,
            headers = emptyMap(),
            responseTime = 100L,
            fromCache = false,
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
            data = "Mock POST response",
            statusCode = 200,
            headers = emptyMap(),
            responseTime = 150L,
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
            data = "Mock PUT response",
            statusCode = 200,
            headers = emptyMap(),
            responseTime = 120L,
            fromCache = false,
        )
    }

    actual suspend fun delete(
        url: String,
        headers: Map<String, String>,
    ): NetworkResponse<String> {
        return NetworkResponse(
            success = true,
            data = "Mock DELETE response",
            statusCode = 200,
            headers = emptyMap(),
            responseTime = 80L,
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
            data = destinationPath,
            statusCode = 200,
            headers = emptyMap(),
            responseTime = 1000L,
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
                    url = url,
                    fileSize = 1024L,
                    uploadTime = com.unify.core.platform.getCurrentTimeMillis(),
                ),
            statusCode = 200,
            headers = emptyMap(),
            responseTime = 2000L,
            fromCache = false,
        )
    }

    actual fun getNetworkStatusFlow(): Flow<NetworkStatus> {
        return flowOf(NetworkStatus.CONNECTED)
    }

    actual suspend fun clearCache() {
        // Clear network cache in JS environment
    }

    actual fun cancelAllRequests() {
        // Cancel all network requests in JS environment
    }
}
