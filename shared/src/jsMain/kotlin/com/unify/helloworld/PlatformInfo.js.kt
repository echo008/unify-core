package com.unify.helloworld

import kotlinx.browser.window

/**
 * Web 平台信息实现
 */
actual fun getPlatformName(): String = "Web (${window.navigator.userAgent.split(" ").last()})"
