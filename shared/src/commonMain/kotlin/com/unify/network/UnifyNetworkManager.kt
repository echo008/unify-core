package com.unify.network

import com.unify.core.network.*
import com.unify.network.cache.UnifyNetworkCacheImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * 统一网络管理器 - 高级网络功能封装
 * 提供缓存、重试、批量请求等增强功能
 */
class UnifyNetworkManager {
    private lateinit var coreNetworkManager: com.unify.core.network.UnifyNetworkManager
    private val cache = UnifyNetworkCacheImpl()

    fun initialize(config: NetworkConfig) {
        coreNetworkManager = com.unify.core.network.UnifyNetworkManager.create()
        coreNetworkManager.initialize(config)
    }

    /**
     * 带缓存的GET请求
     */
    suspend fun getCached(
        url: String,
        headers: Map<String, String> = emptyMap(),
        cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
        cacheTimeout: Long = 300000L, // 5分钟
    ): NetworkResponse<String> {
        return when (cacheStrategy) {
            CacheStrategy.CACHE_FIRST -> {
                val cached = cache.get(url)
                if (cached != null && !cache.isExpired(url, cacheTimeout)) {
                    NetworkResponse(
                        success = true,
                        data = cached,
                        fromCache = true,
                    )
                } else {
                    val response = coreNetworkManager.get(url, headers, false)
                    if (response.success && response.data != null) {
                        cache.put(url, response.data)
                    }
                    response
                }
            }
            CacheStrategy.NETWORK_FIRST -> {
                val response = coreNetworkManager.get(url, headers, false)
                if (response.success && response.data != null) {
                    cache.put(url, response.data)
                    response
                } else {
                    val cached = cache.get(url)
                    if (cached != null) {
                        NetworkResponse(
                            success = true,
                            data = cached,
                            fromCache = true,
                        )
                    } else {
                        response
                    }
                }
            }
            CacheStrategy.CACHE_ONLY -> {
                val cached = cache.get(url)
                if (cached != null) {
                    NetworkResponse(
                        success = true,
                        data = cached,
                        fromCache = true,
                    )
                } else {
                    NetworkResponse(
                        success = false,
                        error =
                            NetworkError(
                                code = NetworkErrorCode.UNKNOWN,
                                message = "No cached data available",
                            ),
                    )
                }
            }
            CacheStrategy.NETWORK_ONLY -> {
                coreNetworkManager.get(url, headers, false)
            }
            CacheStrategy.NO_CACHE -> {
                coreNetworkManager.get(url, headers, false)
            }
        }
    }

    /**
     * 批量请求
     */
    suspend fun batchGet(
        urls: List<String>,
        headers: Map<String, String> = emptyMap(),
    ): List<NetworkResponse<String>> {
        return urls.map { url ->
            coreNetworkManager.get(url, headers)
        }
    }

    /**
     * 并行批量请求
     */
    suspend fun parallelBatchGet(
        urls: List<String>,
        headers: Map<String, String> = emptyMap(),
        maxConcurrency: Int = 5,
    ): List<NetworkResponse<String>> =
        coroutineScope {
            urls.chunked(maxConcurrency).flatMap { chunk ->
                async {
                    chunk.map { url ->
                        async {
                            coreNetworkManager.get(url, headers)
                        }
                    }.awaitAll()
                }.await()
            }
        }

    /**
     * 重试请求
     */
    suspend fun getWithRetry(
        url: String,
        headers: Map<String, String> = emptyMap(),
        retryPolicy: RetryPolicy = RetryPolicy(),
    ): NetworkResponse<String> {
        var lastResponse: NetworkResponse<String>? = null
        var attempt = 0
        var delay = retryPolicy.baseDelay

        while (attempt <= retryPolicy.maxRetries) {
            val response = coreNetworkManager.get(url, headers)

            if (response.success) {
                return response
            }

            lastResponse = response
            attempt++

            if (attempt <= retryPolicy.maxRetries) {
                delay(delay)
                delay = (delay * retryPolicy.backoffMultiplier).toLong().coerceAtMost(retryPolicy.maxDelay)
            }
        }

        return lastResponse ?: NetworkResponse(
            success = false,
            error =
                NetworkError(
                    code = NetworkErrorCode.UNKNOWN,
                    message = "All retry attempts failed",
                ),
        )
    }

    /**
     * 获取网络状态流
     */
    fun getNetworkStatusFlow(): Flow<NetworkStatus> {
        return coreNetworkManager.getNetworkStatusFlow()
    }

    /**
     * 清除缓存
     */
    suspend fun clearCache() {
        cache.clear()
        coreNetworkManager.clearCache()
    }

    /**
     * 取消所有请求
     */
    fun cancelAllRequests() {
        coreNetworkManager.cancelAllRequests()
    }
}
