@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.unify.core.dynamic

import platform.Foundation.*
import kotlinx.cinterop.*

/**
 * NSData扩展函数
 */
fun convertByteArrayToNSData(byteArray: ByteArray): NSData {
    if (byteArray.isEmpty()) return NSData()
    return byteArray.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = byteArray.size.toULong())
    }
}

fun convertNSDataToByteArray(nsData: NSData): ByteArray {
    if (nsData.length == 0UL) return ByteArray(0)
    return ByteArray(nsData.length.toInt()) { index ->
        nsData.bytes!!.reinterpret<ByteVar>()[index]
    }
}
