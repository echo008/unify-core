package com.unify.core.network

import com.unify.core.network.NetworkService
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Web平台网络服务工厂实现
 * 使用JS引擎
 */
actual object NetworkServiceFactory {
    actual fun create(): NetworkService {
        val client = HttpClient(Js) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
        return NetworkService(client)
    }
}
