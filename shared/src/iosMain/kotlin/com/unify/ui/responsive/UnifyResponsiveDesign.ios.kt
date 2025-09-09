package com.unify.ui.responsive

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceIdiom
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIUserInterfaceIdiomTV

/**
 * iOS平台响应式设计实现
 */

@Composable
actual fun isTablet(): Boolean {
    return UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad
}

@Composable
actual fun isDesktop(): Boolean {
    return false // iOS doesn't have desktop
}

@Composable
actual fun isTV(): Boolean {
    return UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomTV
}

@Composable
actual fun isWatch(): Boolean {
    return false // This would be handled by watchOS target
}

@Composable
actual fun getScreenDensity(): Float {
    return UIScreen.mainScreen.scale.toFloat()
}

@Composable
actual fun getScreenOrientation(): Int {
    val orientation = UIDevice.currentDevice.orientation
    return orientation.value.toInt()
}
