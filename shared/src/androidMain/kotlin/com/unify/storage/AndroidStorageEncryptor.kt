package com.unify.storage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

/**
 * Android平台AES存储加密器实现
 */
actual class AESStorageEncryptor : StorageEncryptor {
    
    private val keyAlias = "UnifyStorageKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    
    init {
        keyStore.load(null)
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }
    
    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(keyAlias, null) as SecretKey
    }
    
    actual override fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // 将IV和加密数据组合
        val combined = iv + encryptedData
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    actual override fun decrypt(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        
        // 分离IV和加密数据
        val iv = combined.sliceArray(0..11) // GCM IV通常是12字节
        val encrypted = combined.sliceArray(12 until combined.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val decryptedData = cipher.doFinal(encrypted)
        return String(decryptedData, Charsets.UTF_8)
    }
}
