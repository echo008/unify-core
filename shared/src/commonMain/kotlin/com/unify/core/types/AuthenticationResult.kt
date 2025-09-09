package com.unify.core.types

import kotlinx.serialization.Serializable

/**
 * 认证结果数据类
 * 用于表示生物识别或其他认证方式的结果
 */
@Serializable
data class AuthenticationResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val errorCode: Int? = null,
    val biometricType: String? = null,
)
