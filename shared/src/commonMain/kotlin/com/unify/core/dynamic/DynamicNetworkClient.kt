package com.unify.core.dynamic

import com.unify.core.platform.getCurrentTimeMillis
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 网络请求配置
 */
@Serializable
data class NetworkConfig(
    val baseUrl: String = "",
    val timeout: Long = 30000L,
    val retryAttempts: Int = 3,
    val retryDelay: Long = 1000L,
    val enableCache: Boolean = true,
    val cacheTimeout: Long = 300000L, // 5分钟
    val userAgent: String = "UnifyCore/1.0",
    val headers: Map<String, String> = emptyMap(),
)

/**
 * 网络响应结果
 */
@Serializable
data class NetworkResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val statusCode: Int = 0,
    val headers: Map<String, String> = emptyMap(),
    val timestamp: Long = getCurrentTimeMillis(),
)

/**
 * 组件更新信息
 */
@Serializable
data class ComponentUpdateInfo(
    val componentId: String,
    val currentVersion: String,
    val latestVersion: String,
    val updateUrl: String,
    val changelog: String = "",
    val size: Long = 0L,
    val checksum: String = "",
    val signature: String = "",
    val releaseDate: Long = 0L,
    val isRequired: Boolean = false,
)

/**
 * 动态网络客户端接口
 */
interface DynamicNetworkClient {
    // 组件管理
    suspend fun getComponentInfo(componentId: String): DynamicComponent?

    suspend fun downloadComponent(
        componentId: String,
        version: String,
    ): NetworkResponse<DynamicComponent>

    suspend fun uploadComponent(component: DynamicComponent): NetworkResponse<String>

    // 更新检查
    suspend fun checkForUpdates(componentIds: List<String>): NetworkResponse<List<ComponentUpdateInfo>>

    suspend fun getLatestVersion(componentId: String): NetworkResponse<String>

    // 配置同步
    suspend fun syncConfiguration(configId: String): NetworkResponse<DynamicConfiguration>

    suspend fun uploadConfiguration(config: DynamicConfiguration): NetworkResponse<String>

    // 批量操作
    suspend fun batchDownload(requests: List<String>): NetworkResponse<List<DynamicComponent>>

    suspend fun batchUpload(components: List<DynamicComponent>): NetworkResponse<List<String>>

    // 网络配置
    fun updateNetworkConfig(config: NetworkConfig)

    fun getNetworkConfig(): NetworkConfig

    // 缓存管理
    suspend fun clearCache()

    suspend fun getCacheStats(): Map<String, Any>

    // 连接状态
    suspend fun checkConnection(): Boolean

    suspend fun getNetworkStatus(): NetworkStatus
}

@Serializable
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    LIMITED,
    UNKNOWN,
}

/**
 * 动态网络客户端实现
 */
class DynamicNetworkClientImpl(
    private val httpClient: HttpClient,
) : DynamicNetworkClient {
    private var config = NetworkConfig()
    private val responseCache = mutableMapOf<String, Pair<String, Long>>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        private const val API_VERSION = "v1"
        private const val COMPONENTS_ENDPOINT = "components"
        private const val CONFIGURATIONS_ENDPOINT = "configurations"
        private const val UPDATES_ENDPOINT = "updates"
    }

    override suspend fun getComponentInfo(componentId: String): DynamicComponent? {
        return try {
            val response =
                performRequest<DynamicComponent> {
                    get("${config.baseUrl}/$API_VERSION/$COMPONENTS_ENDPOINT/$componentId")
                }
            response.data
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun downloadComponent(
        componentId: String,
        version: String,
    ): NetworkResponse<DynamicComponent> {
        return performRequest {
            get("${config.baseUrl}/$API_VERSION/$COMPONENTS_ENDPOINT/$componentId/$version")
        }
    }

    override suspend fun uploadComponent(component: DynamicComponent): NetworkResponse<String> {
        return performRequest {
            post("${config.baseUrl}/$API_VERSION/$COMPONENTS_ENDPOINT") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                // TODO: Fix setBody implementation for Ktor 2.3.12
            }
        }
    }

    override suspend fun checkForUpdates(componentIds: List<String>): NetworkResponse<List<ComponentUpdateInfo>> {
        return performRequest {
            post("${config.baseUrl}/$API_VERSION/$UPDATES_ENDPOINT/check") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                // TODO: Fix setBody implementation for Ktor 2.3.12
            }
        }
    }

    override suspend fun getLatestVersion(componentId: String): NetworkResponse<String> {
        return performRequest {
            get("${config.baseUrl}/$API_VERSION/$COMPONENTS_ENDPOINT/$componentId/latest")
        }
    }

    override suspend fun syncConfiguration(configId: String): NetworkResponse<DynamicConfiguration> {
        return performRequest {
            get("${config.baseUrl}/$API_VERSION/$CONFIGURATIONS_ENDPOINT/$configId")
        }
    }

    override suspend fun uploadConfiguration(config: DynamicConfiguration): NetworkResponse<String> {
        return performRequest {
            post("${this@DynamicNetworkClientImpl.config.baseUrl}/$API_VERSION/$CONFIGURATIONS_ENDPOINT") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                // TODO: Fix setBody implementation for Ktor 2.3.12
            }
        }
    }

    override suspend fun batchDownload(requests: List<String>): NetworkResponse<List<DynamicComponent>> {
        return performRequest {
            post("${config.baseUrl}/$API_VERSION/$COMPONENTS_ENDPOINT/batch") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                // TODO: Fix setBody implementation for Ktor 2.3.12
            }
        }
    }

    override suspend fun batchUpload(components: List<DynamicComponent>): NetworkResponse<List<String>> {
        return performRequest {
            post("${config.baseUrl}/$API_VERSION/$COMPONENTS_ENDPOINT/batch/upload") {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                // TODO: Fix setBody implementation for Ktor 2.3.12
            }
        }
    }

    override fun updateNetworkConfig(config: NetworkConfig) {
        this.config = config
    }

    override fun getNetworkConfig(): NetworkConfig = config

    // 私有辅助方法 - 执行网络请求
    private suspend inline fun <reified T> performRequest(
        noinline requestBuilder: suspend HttpClient.() -> HttpResponse,
    ): NetworkResponse<T> {
        return executeWithRetry<T>(requestBuilder, config)
    }

    override suspend fun clearCache() {
        responseCache.clear()
    }

    override suspend fun getCacheStats(): Map<String, Any> {
        val now = getCurrentTimeMillis()
        val validEntries =
            responseCache.values.count { (_, timestamp) ->
                now - timestamp < config.cacheTimeout
            }

        return mapOf(
            "totalEntries" to responseCache.size,
            "validEntries" to validEntries,
            "hitRate" to 0.0, // 简化实现
            "cacheTimeout" to config.cacheTimeout,
        )
    }

    override suspend fun checkConnection(): Boolean {
        return try {
            val response = httpClient.get("${config.baseUrl}/health")
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getNetworkStatus(): NetworkStatus {
        return try {
            if (checkConnection()) {
                NetworkStatus.CONNECTED
            } else {
                NetworkStatus.DISCONNECTED
            }
        } catch (e: Exception) {
            NetworkStatus.UNKNOWN
        }
    }

    // 私有辅助方法
    private suspend inline fun <reified T> executeWithRetry(
        noinline requestBuilder: suspend HttpClient.() -> HttpResponse,
        config: NetworkConfig = this.config,
    ): NetworkResponse<T> {
        var lastException: Exception? = null

        repeat(config.retryAttempts) { attempt ->
            try {
                // 检查缓存
                val cacheKey = generateCacheKey(requestBuilder.toString())
                if (config.enableCache) {
                    val cachedResponse = getCachedResponse<T>(cacheKey)
                    if (cachedResponse != null) {
                        return cachedResponse
                    }
                }

                // 执行请求
                val response =
                    withTimeout(config.timeout) {
                        httpClient.requestBuilder()
                    }

                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    val data = Json.decodeFromString<T>(responseText)

                    val networkResponse =
                        NetworkResponse(
                            success = true,
                            data = data,
                            statusCode = response.status.value,
                            headers = response.headers.entries().associate { it.key to it.value.joinToString(",") },
                        )

                    // 缓存响应
                    if (config.enableCache) {
                        cacheResponse(cacheKey, networkResponse)
                    }

                    return networkResponse
                } else {
                    val errorMessage = response.bodyAsText()
                    return NetworkResponse(
                        success = false,
                        error = errorMessage,
                        statusCode = response.status.value,
                        headers = response.headers.entries().associate { it.key to it.value.joinToString(",") },
                    )
                }
            } catch (e: Exception) {
                lastException = e

                // 如果不是最后一次尝试，等待后重试
                if (attempt < config.retryAttempts - 1) {
                    delay(config.retryDelay * (attempt + 1))
                }
            }
        }

        // 所有重试都失败了
        return NetworkResponse(
            success = false,
            error = lastException?.message ?: "网络请求失败",
        )
    }

    private fun generateCacheKey(request: String): String {
        return request.hashCode().toString()
    }

    private suspend fun <T> getCachedResponse(cacheKey: String): NetworkResponse<T>? {
        val cached = responseCache[cacheKey] ?: return null
        val (responseJson, timestamp) = cached

        // 检查缓存是否过期
        if (getCurrentTimeMillis() - timestamp > config.cacheTimeout) {
            responseCache.remove(cacheKey)
            return null
        }

        return try {
            Json.decodeFromString<NetworkResponse<T>>(responseJson)
        } catch (e: Exception) {
            responseCache.remove(cacheKey)
            null
        }
    }

    private fun <T> cacheResponse(
        cacheKey: String,
        response: NetworkResponse<T>,
    ) {
        try {
            val responseJson = Json.encodeToString(response)
            responseCache[cacheKey] = responseJson to getCurrentTimeMillis()

            // 清理过期缓存
            cleanupExpiredCache()
        } catch (e: Exception) {
            // 忽略缓存错误
        }
    }

    private fun cleanupExpiredCache() {
        val now = getCurrentTimeMillis()
        val expiredKeys =
            responseCache.filter { (_, pair) ->
                now - pair.second > config.cacheTimeout
            }.keys

        expiredKeys.forEach { key ->
            responseCache.remove(key)
        }
    }
}

/**
 * 网络客户端工厂
 */
object DynamicNetworkClientFactory {
    fun create(httpClient: HttpClient): DynamicNetworkClient {
        return DynamicNetworkClientImpl(httpClient)
    }

    fun createDefault(): DynamicNetworkClient {
        val httpClient =
            HttpClient {
                // 基础配置
            }
        return DynamicNetworkClientImpl(httpClient)
    }
}
