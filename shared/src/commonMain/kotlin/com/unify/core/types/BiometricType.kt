package com.unify.core.types

/**
 * 生物识别类型枚举
 * 定义了支持的各种生物识别方式
 */
enum class BiometricType {
    /**
     * 指纹识别
     */
    FINGERPRINT,

    /**
     * 面部识别
     */
    FACE_ID,

    /**
     * 虹膜识别
     */
    IRIS,

    /**
     * 声纹识别
     */
    VOICE,

    /**
     * 手掌识别
     */
    PALM,
}
