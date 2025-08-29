package com.unify.helloworld

import android.os.Build

actual object PlatformInfo {
    actual fun getPlatformName(): String = "Android"
    
    actual fun getDeviceInfo(): String = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
}
