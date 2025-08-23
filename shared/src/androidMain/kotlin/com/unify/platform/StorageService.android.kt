package com.unify.platform

import android.content.Context
import android.content.SharedPreferences

object AndroidAppContextHolder {
    lateinit var context: Context
}

actual class StorageService {
    private val prefs: SharedPreferences by lazy {
        AndroidAppContextHolder.context.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
    }

    actual suspend fun getString(key: String): String? = prefs.getString(key, null)

    actual suspend fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual suspend fun getInt(key: String): Int? = if (prefs.contains(key)) prefs.getInt(key, 0) else null

    actual suspend fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    actual suspend fun getBoolean(key: String): Boolean? = if (prefs.contains(key)) prefs.getBoolean(key, false) else null

    actual suspend fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual suspend fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual suspend fun clear() {
        prefs.edit().clear().apply()
    }
}
