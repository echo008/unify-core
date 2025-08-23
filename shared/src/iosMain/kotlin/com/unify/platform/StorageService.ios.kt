package com.unify.platform

import platform.Foundation.NSUserDefaults

actual class StorageService {
    private val prefs: NSUserDefaults = NSUserDefaults.standardUserDefaults()

    actual suspend fun getString(key: String): String? = prefs.stringForKey(key)

    actual suspend fun setString(key: String, value: String) {
        prefs.setObject(value, forKey = key)
    }

    actual suspend fun getInt(key: String): Int? = if (prefs.objectForKey(key) != null) prefs.integerForKey(key).toInt() else null

    actual suspend fun setInt(key: String, value: Int) {
        prefs.setInteger(value.toLong(), forKey = key)
    }

    actual suspend fun getBoolean(key: String): Boolean? = if (prefs.objectForKey(key) != null) prefs.boolForKey(key) else null

    actual suspend fun setBoolean(key: String, value: Boolean) {
        prefs.setBool(value, forKey = key)
    }

    actual suspend fun remove(key: String) {
        prefs.removeObjectForKey(key)
    }

    actual suspend fun clear() {
        // NSUserDefaults 无统一 clear，仅用于示例，实际可遍历 keys
    }
}
