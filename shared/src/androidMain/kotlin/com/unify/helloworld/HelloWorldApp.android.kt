package com.unify.helloworld

import android.os.Build

/**
 * Android平台Hello World实现
 */
actual fun getPlatformName(): String {
    return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}
