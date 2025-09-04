package com.unify.core.network

import com.unify.core.network.NetworkService
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * iOS平台网络服务工厂实现
 * 使用Darwin引擎
 */
actual object NetworkServiceFactory {
    actual fun create(): NetworkService {
        val client = HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            engine {
                configureRequest {
                    setAllowsCellularAccess(true)
                }
            }
        }
        return NetworkService(client)
    }
}
