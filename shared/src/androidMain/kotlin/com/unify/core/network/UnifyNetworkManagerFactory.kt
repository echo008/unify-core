package com.unify.core.network

/**
 * Android平台网络管理器工厂
 */
object UnifyNetworkManagerFactory {
    fun create(): UnifyNetworkManager {
        return UnifyNetworkManager.create()
    }
}
