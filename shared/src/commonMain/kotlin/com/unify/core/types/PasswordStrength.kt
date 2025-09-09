package com.unify.core.types

/**
 * 密码强度等级枚举
 * 定义了密码安全强度的各个级别
 */
enum class PasswordStrength {
    /**
     * 非常弱
     */
    VERY_WEAK,

    /**
     * 弱
     */
    WEAK,

    /**
     * 一般
     */
    FAIR,

    /**
     * 良好
     */
    GOOD,

    /**
     * 强
     */
    STRONG,

    /**
     * 非常强
     */
    VERY_STRONG,
}
