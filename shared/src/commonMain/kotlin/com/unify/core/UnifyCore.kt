package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.ui.UnifyUIManager
import com.unify.device.UnifyDeviceManager

/**
 * Unify-Core 核心接口
 * 统一管理所有跨平台功能模块
 */
interface UnifyCore {
    val uiManager: UnifyUIManager
    val dataManager: UnifyDataManager
    val networkManager: UnifyNetworkManager
    val deviceManager: UnifyDeviceManager

    suspend fun initialize()

    suspend fun shutdown()

    fun isInitialized(): Boolean

    fun getPlatformInfo(): PlatformInfo
}

/**
 * 平台信息数据类
 */
data class PlatformInfo(
    val platformName: String,
    val version: String,
    val deviceModel: String,
    val osVersion: String,
    val capabilities: List<String>,
)

/**
 * UnifyCore 工厂接口
 */
expect object UnifyCoreFactory {
    fun create(): UnifyCore
}
