package com.unify.platform

import kotlinx.browser.window

actual class StorageService {
    private val storage = window.localStorage

    actual suspend fun getString(key: String): String? = storage.getItem(key)

    actual suspend fun setString(key: String, value: String) {
        storage.setItem(key, value)
    }

    actual suspend fun getInt(key: String): Int? = storage.getItem(key)?.toIntOrNull()

    actual suspend fun setInt(key: String, value: Int) {
        storage.setItem(key, value.toString())
    }

    actual suspend fun getBoolean(key: String): Boolean? = storage.getItem(key)?.let { it == "true" }

    actual suspend fun setBoolean(key: String, value: Boolean) {
        storage.setItem(key, value.toString())
    }

    actual suspend fun remove(key: String) {
        storage.removeItem(key)
    }

    actual suspend fun clear() {
        storage.clear()
    }
}
