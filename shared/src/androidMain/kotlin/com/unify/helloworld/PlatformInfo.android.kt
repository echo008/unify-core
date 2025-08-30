package com.unify.helloworld

import android.os.Build

/**
 * Android 平台信息实现
 */
actual fun getPlatformName(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
