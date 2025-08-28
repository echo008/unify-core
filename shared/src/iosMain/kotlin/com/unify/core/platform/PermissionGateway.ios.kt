package com.unify.core.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// NOTE: Stub implementation. Integrate with iOS APIs (AVFoundation/CLLocation/UserNotifications) via Kotlin/Native interop.
actual object PermissionGateway {
    actual fun isGranted(permission: PlatformPermission): Boolean {
        // TODO: check iOS permission status via native frameworks
        return false
    }

    actual suspend fun request(permission: PlatformPermission): Boolean = suspendCancellableCoroutine { cont ->
        // TODO: request iOS permissions using native frameworks
        cont.resume(false)
    }
}
