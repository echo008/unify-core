package com.unify.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

actual class NetworkService {
    private val client = HttpClient(Darwin)

    actual suspend fun get(url: String, headers: Map<String, String>): NetworkResponse {
        val response = client.get(url) { headers { headers.forEach { (k, v) -> append(k, v) } } }
        return NetworkResponse(response.status.value, response.bodyAsText(), response.headers.entries().associate { it.key to it.value.joinToString(",") })
    }

    actual suspend fun post(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        val response = client.post(url) {
            headers { headers.forEach { (k, v) -> append(k, v) } }
            if (headers[HttpHeaders.ContentType] == null) headers { append(HttpHeaders.ContentType, "application/json") }
            setBody(body)
        }
        return NetworkResponse(response.status.value, response.bodyAsText(), response.headers.entries().associate { it.key to it.value.joinToString(",") })
    }

    actual suspend fun put(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        val response = client.put(url) {
            headers { headers.forEach { (k, v) -> append(k, v) } }
            if (headers[HttpHeaders.ContentType] == null) headers { append(HttpHeaders.ContentType, "application/json") }
            setBody(body)
        }
        return NetworkResponse(response.status.value, response.bodyAsText(), response.headers.entries().associate { it.key to it.value.joinToString(",") })
    }

    actual suspend fun delete(url: String, headers: Map<String, String>): NetworkResponse {
        val response = client.delete(url) { headers { headers.forEach { (k, v) -> append(k, v) } } }
        return NetworkResponse(response.status.value, response.bodyAsText(), response.headers.entries().associate { it.key to it.value.joinToString(",") })
    }
}
