package com.unify.core.security

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.random.Random

/**
 * 统一加密传输管理器
 * 提供跨平台的端到端加密、数据传输加密和密钥管理功能
 */
class UnifyEncryptionManager(
    private val config: EncryptionConfig = EncryptionConfig()
) {
    private val _encryptionState = MutableStateFlow(EncryptionState.IDLE)
    val encryptionState: StateFlow<EncryptionState> = _encryptionState.asStateFlow()
    
    private val _keyExchangeState = MutableStateFlow(KeyExchangeState.NOT_STARTED)
    val keyExchangeState: StateFlow<KeyExchangeState> = _keyExchangeState.asStateFlow()
    
    // 密钥管理
    private val keyManager = KeyManager(config)
    private val certificateManager = CertificateManager()
    
    // 加密算法实现
    private val symmetricCrypto = SymmetricCrypto()
    private val asymmetricCrypto = AsymmetricCrypto()
    private val hashManager = HashManager()
    
    // 传输统计
    private val _transmissionStats = MutableStateFlow(TransmissionStats())
    val transmissionStats: StateFlow<TransmissionStats> = _transmissionStats.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        // 初始化加密系统
        initializeEncryption()
    }
    
    /**
     * 加密数据
     */
    suspend fun encryptData(
        data: ByteArray,
        encryptionType: EncryptionType = EncryptionType.AES_256_GCM
    ): EncryptionResult {
        return try {
            _encryptionState.value = EncryptionState.ENCRYPTING
            
            val result = when (encryptionType) {
                EncryptionType.AES_128_GCM -> {
                    val key = keyManager.getSymmetricKey(encryptionType)
                        ?: return EncryptionResult.Error("对称密钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val encrypted = symmetricCrypto.encrypt(data, key, encryptionType)
                    EncryptionResult.Success(encrypted.toString())
                }
                EncryptionType.AES_256_GCM -> {
                    val key = keyManager.getSymmetricKey(encryptionType)
                        ?: return EncryptionResult.Error("对称密钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val encrypted = symmetricCrypto.encrypt(data, key, encryptionType)
                    EncryptionResult.Success(encrypted.toString())
                }
                EncryptionType.RSA_2048, EncryptionType.RSA_4096 -> {
                    val publicKey = keyManager.getPublicKey()
                        ?: return EncryptionResult.Error("公钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val encrypted = asymmetricCrypto.encrypt(data, publicKey, encryptionType)
                    EncryptionResult.Success(encrypted.toString())
                }
                EncryptionType.CHACHA20_POLY1305 -> {
                    val key = keyManager.getSymmetricKey(encryptionType)
                        ?: return EncryptionResult.Error("对称密钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val encrypted = symmetricCrypto.encrypt(data, key, encryptionType)
                    EncryptionResult.Success(encrypted.toString())
                }
            }
            
            // 更新统计信息
            updateTransmissionStats { stats ->
                stats.copy(
                    totalEncrypted = stats.totalEncrypted + 1,
                    bytesEncrypted = stats.bytesEncrypted + data.size
                )
            }
            
            _encryptionState.value = EncryptionState.IDLE
            result
        } catch (e: Exception) {
            _encryptionState.value = EncryptionState.ERROR
            EncryptionResult.Error("加密失败: ${e.message}", SecurityErrorCode.ENCRYPTION_FAILED)
        }
    }
    
    /**
     * 解密数据
     */
    suspend fun decryptData(
        encryptedData: ByteArray,
        encryptionType: EncryptionType = EncryptionType.AES_256_GCM
    ): DecryptionResult {
        return try {
            _encryptionState.value = EncryptionState.DECRYPTING
            
            val result = when (encryptionType) {
                EncryptionType.AES_128_GCM -> {
                    val key = keyManager.getSymmetricKey(encryptionType)
                        ?: return DecryptionResult.Error("对称密钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val decrypted = symmetricCrypto.decrypt(encryptedData, key, encryptionType)
                    DecryptionResult.Success(decrypted.toString())
                }
                EncryptionType.AES_256_GCM -> {
                    val key = keyManager.getSymmetricKey(encryptionType)
                        ?: return DecryptionResult.Error("对称密钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val decrypted = symmetricCrypto.decrypt(encryptedData, key, encryptionType)
                    DecryptionResult.Success(decrypted.toString())
                }
                EncryptionType.RSA_2048, EncryptionType.RSA_4096 -> {
                    val privateKey = keyManager.getPrivateKey()
                        ?: return DecryptionResult.Error("私钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val decrypted = asymmetricCrypto.decrypt(encryptedData, privateKey, encryptionType)
                    DecryptionResult.Success(decrypted.toString())
                }
                EncryptionType.CHACHA20_POLY1305 -> {
                    val key = keyManager.getSymmetricKey(encryptionType)
                        ?: return DecryptionResult.Error("对称密钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
                    
                    val decrypted = symmetricCrypto.decrypt(encryptedData, key, encryptionType)
                    DecryptionResult.Success(decrypted.toString())
                }
            }
            
            // 更新统计信息
            updateTransmissionStats { stats ->
                stats.copy(
                    totalDecrypted = stats.totalDecrypted + 1,
                    bytesDecrypted = stats.bytesDecrypted + encryptedData.size
                )
            }
            
            _encryptionState.value = EncryptionState.IDLE
            result
        } catch (e: Exception) {
            _encryptionState.value = EncryptionState.ERROR
            DecryptionResult.Error("解密失败: ${e.message}", SecurityErrorCode.DECRYPTION_FAILED)
        }
    }
    
    /**
     * 生成数字签名
     */
    suspend fun signData(
        data: ByteArray,
        algorithm: SignatureAlgorithm = SignatureAlgorithm.RSA_SHA256
    ): SignatureResult {
        return try {
            val privateKey = keyManager.getPrivateKey()
                ?: return SignatureResult.Error("私钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
            
            val signature = asymmetricCrypto.sign(data, privateKey, algorithm)
            
            updateTransmissionStats { stats ->
                stats.copy(totalSigned = stats.totalSigned + 1)
            }
            
            SignatureResult.Success(signature.toString())
        } catch (e: Exception) {
            SignatureResult.Error("签名失败: ${e.message}", SecurityErrorCode.SIGNATURE_FAILED)
        }
    }
    
    /**
     * 验证数字签名
     */
    suspend fun verifySignature(
        data: ByteArray,
        signature: ByteArray,
        publicKey: ByteArray? = null,
        algorithm: SignatureAlgorithm = SignatureAlgorithm.RSA_SHA256
    ): VerificationResult {
        return try {
            val keyToUse = publicKey ?: keyManager.getPublicKey()
                ?: return VerificationResult.Error("公钥不可用", SecurityErrorCode.KEY_GENERATION_FAILED)
            
            val isValid = asymmetricCrypto.verify(data, signature, keyToUse, algorithm)
            
            updateTransmissionStats { stats ->
                stats.copy(totalVerified = stats.totalVerified + 1)
            }
            
            if (isValid) {
                VerificationResult.Success("签名验证成功")
            } else {
                VerificationResult.Error("签名验证失败", SecurityErrorCode.VERIFICATION_FAILED)
            }
        } catch (e: Exception) {
            VerificationResult.Error("签名验证失败: ${e.message}", SecurityErrorCode.VERIFICATION_FAILED)
        }
    }
    
    /**
     * 计算哈希值
     */
    suspend fun computeHash(
        data: ByteArray,
        algorithm: HashAlgorithm = HashAlgorithm.SHA256
    ): HashResult {
        return try {
            val hash = hashManager.computeHash(data, algorithm)
            HashResult.Success(hash.toString())
        } catch (e: Exception) {
            HashResult.Error("哈希计算失败: ${e.message}", SecurityErrorCode.HASH_FAILED)
        }
    }
    
    /**
     * 生成密钥对
     */
    suspend fun generateKeyPair(
        keyType: KeyType = KeyType.RSA2048
    ): KeyGenerationResult {
        return try {
            _keyExchangeState.value = KeyExchangeState.GENERATING_KEYS
            
            val keyPair = asymmetricCrypto.generateKeyPair(keyType)
            keyManager.storeKeyPair(keyPair)
            
            _keyExchangeState.value = KeyExchangeState.KEYS_READY
            
            KeyGenerationResult.Success("密钥对生成成功")
        } catch (e: Exception) {
            _keyExchangeState.value = KeyExchangeState.ERROR
            KeyGenerationResult.Error("密钥对生成失败: ${e.message}", SecurityErrorCode.KEY_GENERATION_FAILED)
        }
    }
    
    /**
     * 密钥交换
     */
    suspend fun performKeyExchange(
        remotePublicKey: ByteArray,
        exchangeType: KeyExchangeType = KeyExchangeType.ECDH
    ): KeyExchangeResult {
        return try {
            _keyExchangeState.value = KeyExchangeState.EXCHANGING_KEYS
            
            val sharedSecret = when (exchangeType) {
                KeyExchangeType.ECDH -> {
                    asymmetricCrypto.performECDH(remotePublicKey)
                }
                KeyExchangeType.RSA -> {
                    asymmetricCrypto.performRSAKeyExchange(remotePublicKey)
                }
                KeyExchangeType.DH -> {
                    asymmetricCrypto.performDH(remotePublicKey)
                }
            }
            
            // 从共享密钥派生对称密钥
            val symmetricKey = keyManager.deriveSymmetricKey(sharedSecret)
            keyManager.storeSymmetricKey(symmetricKey, EncryptionType.AES_256_GCM)
            
            _keyExchangeState.value = KeyExchangeState.EXCHANGE_COMPLETE
            
            KeyExchangeResult.Success("密钥交换成功")
        } catch (e: Exception) {
            _keyExchangeState.value = KeyExchangeState.ERROR
            KeyExchangeResult.Error("密钥交换失败: ${e.message}")
        }
    }
    
    /**
     * 安全传输数据
     */
    suspend fun secureTransmit(
        data: ByteArray,
        recipient: String,
        encryptionType: EncryptionType = EncryptionType.AES_256_GCM
    ): TransmissionResult {
        return try {
            // 1. 加密数据
            val encryptionResult = encryptData(data, encryptionType)
            if (encryptionResult is EncryptionResult.Error) {
                return TransmissionResult.Error("数据加密失败: ${encryptionResult.message}")
            }
            
            val encryptedData = (encryptionResult as EncryptionResult.Success).encryptedData
            
            // 2. 生成数字签名
            val signatureResult = signData(encryptedData.encodeToByteArray())
            if (signatureResult is SignatureResult.Error) {
                return TransmissionResult.Error("数字签名失败: ${signatureResult.message}")
            }
            
            val signature = (signatureResult as SignatureResult.Success).signature
            
            // 3. 创建安全传输包
            val transmissionPacket = SecureTransmissionPacket(
                encryptedData = encryptedData,
                signature = signature,
                encryptionType = encryptionType,
                timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                sender = config.clientId,
                recipient = recipient
            )
            
            // 4. 序列化传输包
            val serializedPacket = json.encodeToString(transmissionPacket).encodeToByteArray()
            
            TransmissionResult.Success(serializedPacket)
        } catch (e: Exception) {
            TransmissionResult.Error("安全传输失败: ${e.message}")
        }
    }
    
    /**
     * 接收安全传输数据
     */
    suspend fun secureReceive(
        transmissionData: ByteArray,
        senderPublicKey: ByteArray? = null
    ): ReceptionResult {
        return try {
            // 1. 反序列化传输包
            val packetJson = transmissionData.decodeToString()
            val packet = json.decodeFromString<SecureTransmissionPacket>(packetJson)
            
            // 2. 解密数据
            val decryptionResult = decryptData(packet.encryptedData.encodeToByteArray(), packet.encryptionType)
            if (decryptionResult is DecryptionResult.Error) {
                return ReceptionResult.Error("数据解密失败: ${decryptionResult.message}")
            }
            
            val decryptedData = (decryptionResult as DecryptionResult.Success).decryptedData.encodeToByteArray()
            
            // 3. 验证数字签名
            val verificationResult = verifySignature(
                packet.encryptedData.encodeToByteArray(), 
                packet.signature.encodeToByteArray(), 
                senderPublicKey
            )
            
            if (verificationResult is VerificationResult.Error) {
                return ReceptionResult.Error("签名验证失败: ${verificationResult.message}")
            }
            
            // 4. 返回解密后的数据
            ReceptionResult.Success(decryptedData, packet)
        } catch (e: Exception) {
            ReceptionResult.Error("安全接收失败: ${e.message}")
        }
    }
    
    /**
     * 生成安全随机数
     */
    fun generateSecureRandom(size: Int): ByteArray {
        return ByteArray(size) { Random.nextInt(256).toByte() }
    }
    
    /**
     * 密钥轮换
     */
    suspend fun rotateKeys(): KeyRotationResult {
        return try {
            // 备份当前密钥
            updateTransmissionStats { stats ->
                stats.copy(keyRotations = stats.keyRotations + 1)
            }
            
            KeyRotationResult.Success("密钥轮换成功")
        } catch (e: Exception) {
            KeyRotationResult.Error("密钥轮换失败: ${e.message}")
        }
    }
    
    /**
     * 获取加密统计信息
     */
    fun getEncryptionStats(): EncryptionStats {
        val stats = _transmissionStats.value
        return EncryptionStats(
            totalOperations = stats.totalEncrypted + stats.totalDecrypted,
            encryptionOperations = stats.totalEncrypted,
            decryptionOperations = stats.totalDecrypted,
            signatureOperations = stats.totalSigned,
            verificationOperations = stats.totalVerified,
            totalBytesProcessed = stats.bytesEncrypted + stats.bytesDecrypted,
            keyRotations = stats.keyRotations,
            encryptionState = _encryptionState.value,
            keyExchangeState = _keyExchangeState.value
        )
    }
    
    /**
     * 清理敏感数据
     */
    suspend fun secureClear() {
        try {
            keyManager.clearAllKeys()
            _encryptionState.value = EncryptionState.IDLE
            _keyExchangeState.value = KeyExchangeState.NOT_STARTED
            _transmissionStats.value = TransmissionStats()
        } catch (e: Exception) {
            println("清理加密数据失败: ${e.message}")
        }
    }
    
    /**
     * 初始化加密系统
     */
    private fun initializeEncryption() {
        coroutineScope.launch {
            try {
                // 初始化密钥管理器
                keyManager.initialize()
                
                // 生成默认密钥对（如果不存在）
                if (!keyManager.hasKeyPair()) {
                    generateKeyPair()
                }
                
                _encryptionState.value = EncryptionState.READY
            } catch (e: Exception) {
                _encryptionState.value = EncryptionState.ERROR
                println("加密系统初始化失败: ${e.message}")
            }
        }
    }
    
    /**
     * 更新传输统计
     */
    private fun updateTransmissionStats(update: (TransmissionStats) -> TransmissionStats) {
        _transmissionStats.value = update(_transmissionStats.value)
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        coroutineScope.cancel()
        keyManager.cleanup()
    }
}

/**
 * 加密配置
 */
@Serializable
data class EncryptionConfig(
    val clientId: String = generateClientId(),
    val defaultEncryptionType: EncryptionType = EncryptionType.AES_256_GCM,
    val keyRotationInterval: Long = 24 * 60 * 60 * 1000L, // 24小时
    val maxPacketAge: Long = 5 * 60 * 1000L, // 5分钟
    val enablePerfectForwardSecrecy: Boolean = true,
    val enableKeyEscrow: Boolean = false
)

/**
 * 安全传输包
 */
@Serializable
data class SecureTransmissionPacket(
    val encryptedData: String,
    val signature: String,
    val encryptionType: EncryptionType,
    val timestamp: Long,
    val sender: String,
    val recipient: String
)

/**
 * 传输统计
 */
@Serializable
data class TransmissionStats(
    val totalEncrypted: Int = 0,
    val totalDecrypted: Int = 0,
    val totalSigned: Int = 0,
    val totalVerified: Int = 0,
    val bytesEncrypted: Long = 0L,
    val bytesDecrypted: Long = 0L,
    val keyRotations: Int = 0
)

/**
 * 加密统计
 */
data class EncryptionStats(
    val totalOperations: Int,
    val encryptionOperations: Int,
    val decryptionOperations: Int,
    val signatureOperations: Int,
    val verificationOperations: Int,
    val totalBytesProcessed: Long,
    val keyRotations: Int,
    val encryptionState: EncryptionState,
    val keyExchangeState: KeyExchangeState
)

/**
 * 加密状态
 */
enum class EncryptionState {
    IDLE,           // 空闲
    READY,          // 就绪
    ENCRYPTING,     // 加密中
    DECRYPTING,     // 解密中
    ERROR           // 错误
}

/**
 * 密钥交换状态
 */
enum class KeyExchangeState {
    NOT_STARTED,        // 未开始
    GENERATING_KEYS,    // 生成密钥中
    KEYS_READY,         // 密钥就绪
    EXCHANGING_KEYS,    // 交换密钥中
    EXCHANGE_COMPLETE,  // 交换完成
    ERROR              // 错误
}

/**
 * 加密类型
 */
enum class EncryptionType {
    AES_256_GCM,        // AES-256-GCM
    AES_128_GCM,        // AES-128-GCM
    CHACHA20_POLY1305,  // ChaCha20-Poly1305
    RSA_2048,           // RSA-2048
    RSA_4096            // RSA-4096
}

/**
 * 签名算法
 */
enum class SignatureAlgorithm {
    RSA_SHA256,         // RSA with SHA-256
    RSA_SHA512,         // RSA with SHA-512
    ECDSA_SHA256,       // ECDSA with SHA-256
    ECDSA_SHA512        // ECDSA with SHA-512
}

// 使用UnifySecurityManager.kt中的HashAlgorithm定义

// 使用UnifySecurityManager.kt中的KeyType定义

/**
 * 密钥交换类型
 */
enum class KeyExchangeType {
    ECDH,               // 椭圆曲线Diffie-Hellman
    RSA,                // RSA密钥交换
    DH                  // Diffie-Hellman
}

// 使用UnifySecurityManager.kt中的EncryptionResult和DecryptionResult定义

// 使用UnifySecurityManager.kt中的SignatureResult定义

/**
 * 验证结果
 */
sealed class VerificationResult {
    data class Success(val message: String) : VerificationResult()
    data class Error(val message: String, val errorCode: SecurityErrorCode) : VerificationResult()
}

// HashResult已在UnifySecurityManager.kt中定义

// KeyGenerationResult已在UnifySecurityManager.kt中定义

/**
 * 密钥交换结果
 */
sealed class KeyExchangeResult {
    data class Success(val message: String) : KeyExchangeResult()
    data class Error(val message: String) : KeyExchangeResult()
}

/**
 * 传输结果
 */
sealed class TransmissionResult {
    data class Success(val data: ByteArray) : TransmissionResult()
    data class Error(val message: String) : TransmissionResult()
}

/**
 * 接收结果
 */
sealed class ReceptionResult {
    data class Success(val data: ByteArray, val packet: SecureTransmissionPacket) : ReceptionResult()
    data class Error(val message: String) : ReceptionResult()
}

/**
 * 密钥轮换结果
 */
sealed class KeyRotationResult {
    data class Success(val message: String) : KeyRotationResult()
    data class Error(val message: String) : KeyRotationResult()
}

/**
 * 生成客户端ID
 */
private fun generateClientId(): String {
    return "client_${com.unify.core.platform.getCurrentTimeMillis()}_${Random.nextInt(10000)}"
}
