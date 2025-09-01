package com.unify.core

import com.unify.core.data.UnifyDataManager
import com.unify.core.data.UnifyDataManagerImpl
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.UnifyNetworkManagerImpl
import com.unify.core.platform.PlatformManager
import com.unify.core.ui.UnifyUIManager
import com.unify.core.ui.UnifyUIManagerImpl

/**
 * TV平台的UnifyCore实现
 */
actual class UnifyCoreImpl : UnifyCore {
    
    override val uiManager: UnifyUIManager = UnifyUIManagerImpl()
    override val dataManager: UnifyDataManager = UnifyDataManagerImpl()
    override val networkManager: UnifyNetworkManager = UnifyNetworkManagerImpl()
    override val platformManager: PlatformManager = PlatformManager
    
    private var initialized = false
    
    override fun initialize() {
        if (initialized) return
        
        // 初始化各个管理器
        platformManager.initialize()
        
        initialized = true
    }
    
    override fun getVersion(): String = UnifyCoreInstance.VERSION
    
    override fun getSupportedPlatforms(): List<String> = UnifyCoreInstance.SUPPORTED_PLATFORMS
    
    override fun isPlatformSupported(platform: String): Boolean {
        return platform in UnifyCoreInstance.SUPPORTED_PLATFORMS
    }
    
    override fun getCurrentPlatformConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "TV",
            "version" to getVersion(),
            "capabilities" to listOf(
                "remote_control", "voice_control", "hdmi", "4k_display", 
                "hdr", "dolby_vision", "surround_sound", "cast", "airplay"
            ),
            "ui_framework" to "Android TV/tvOS + Compose Multiplatform",
            "tv_features" to listOf(
                "lean_back_experience", "d_pad_navigation", "voice_search",
                "content_discovery", "picture_in_picture", "multi_user",
                "parental_controls", "live_tv", "dvr", "streaming"
            ),
            "supported_platforms" to listOf(
                "Android TV", "Google TV", "Apple TV", "Samsung Tizen", 
                "LG webOS", "Roku TV", "Fire TV", "Chromecast"
            ),
            "screen_resolutions" to listOf("1080p", "4K", "8K"),
            "audio_formats" to listOf("Dolby Atmos", "DTS:X", "PCM", "AAC"),
            "video_formats" to listOf("H.264", "H.265", "VP9", "AV1"),
            "input_methods" to listOf("remote", "voice", "mobile_app", "game_controller")
        )
    }
}
