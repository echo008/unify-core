package com.unify.platform

expect class StorageService {
    suspend fun getString(key: String): String?
    suspend fun setString(key: String, value: String)
    suspend fun getInt(key: String): Int?
    suspend fun setInt(key: String, value: Int)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun setBoolean(key: String, value: Boolean)
    suspend fun remove(key: String)
    suspend fun clear()
}
