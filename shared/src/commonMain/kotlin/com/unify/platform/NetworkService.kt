package com.unify.platform

expect class NetworkService {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): NetworkResponse
}

data class NetworkResponse(
    val status: Int,
    val body: String,
    val headers: Map<String, String>
)
