package com.unify.helloworld

import android.os.Build

actual fun getPlatformName(): String = "Android"
actual fun getPlatformNameImpl(): String = "Android"
actual fun getDeviceInfoImpl(): String = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
