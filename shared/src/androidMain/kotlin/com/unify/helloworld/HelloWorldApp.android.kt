package com.unify.helloworld

import android.os.Build

actual fun getPlatformName(): String {
    return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}
