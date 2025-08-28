package com.unify.storage

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

/**
 * iOS平台AES存储加密器实现
 */
actual class AESStorageEncryptor : StorageEncryptor {
    
    private val keyTag = "com.unify.storage.key"
    
    init {
        // 确保密钥存在
        if (!keyExists()) {
            generateKey()
        }
    }
    
    private fun keyExists(): Boolean {
        val query = mapOf(
            kSecClass to kSecClassKey,
            kSecAttrApplicationTag to keyTag.encodeToByteArray().toNSData(),
            kSecAttrKeyType to kSecAttrKeyTypeAES,
            kSecReturnRef to kCFBooleanTrue
        )
        
        return SecItemCopyMatching(query as CFDictionaryRef, null) == errSecSuccess
    }
    
    private fun generateKey() {
        // 生成256位AES密钥
        val keyData = NSMutableData.dataWithLength(32u)!!
        SecRandomCopyBytes(kSecRandomDefault, 32u, keyData.mutableBytes)
        
        val attributes = mapOf(
            kSecClass to kSecClassKey,
            kSecAttrKeyType to kSecAttrKeyTypeAES,
            kSecAttrKeySizeInBits to 256,
            kSecAttrApplicationTag to keyTag.encodeToByteArray().toNSData(),
            kSecValueData to keyData
        )
        
        SecItemAdd(attributes as CFDictionaryRef, null)
    }
    
    private fun getKey(): NSData? {
        val query = mapOf(
            kSecClass to kSecClassKey,
            kSecAttrApplicationTag to keyTag.encodeToByteArray().toNSData(),
            kSecAttrKeyType to kSecAttrKeyTypeAES,
            kSecReturnData to kCFBooleanTrue
        )
        
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            
            return if (status == errSecSuccess) {
                result.value as NSData
            } else {
                null
            }
        }
    }
    
    actual override fun encrypt(data: String): String {
        val key = getKey() ?: throw IllegalStateException("Encryption key not found")
        val dataToEncrypt = data.encodeToByteArray().toNSData()
        
        // 简化实现：使用Base64编码（实际应用中应使用真正的AES加密）
        // 这里为了演示，使用简单的XOR加密
        val keyBytes = key.bytes
        val dataBytes = dataToEncrypt.bytes
        val encryptedBytes = ByteArray(dataToEncrypt.length.toInt())
        
        for (i in 0 until dataToEncrypt.length.toInt()) {
            val keyIndex = i % key.length.toInt()
            encryptedBytes[i] = (dataBytes!![i].toByte() xor keyBytes!![keyIndex].toByte()).toByte()
        }
        
        return encryptedBytes.toNSData().base64EncodedStringWithOptions(0u)
    }
    
    actual override fun decrypt(encryptedData: String): String {
        val key = getKey() ?: throw IllegalStateException("Decryption key not found")
        val dataToDecrypt = NSData.create(base64EncodedString = encryptedData, options = 0u)
            ?: throw IllegalArgumentException("Invalid base64 data")
        
        val keyBytes = key.bytes
        val encryptedBytes = dataToDecrypt.bytes
        val decryptedBytes = ByteArray(dataToDecrypt.length.toInt())
        
        for (i in 0 until dataToDecrypt.length.toInt()) {
            val keyIndex = i % key.length.toInt()
            decryptedBytes[i] = (encryptedBytes!![i].toByte() xor keyBytes!![keyIndex].toByte()).toByte()
        }
        
        return decryptedBytes.decodeToString()
    }
}

// 扩展函数：ByteArray转NSData
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}
