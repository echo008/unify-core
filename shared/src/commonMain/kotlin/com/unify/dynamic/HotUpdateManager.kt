package com.unify.dynamic

import androidx.compose.runtime.Composable
import com.unify.cache.LRUCache
import com.unify.platform.NetworkService
import kotlinx.serialization.json.Json

class HotUpdateManager {
    private val configCache = LRUCache<String, ComponentConfig>(100)
    private val componentFactory = ComponentFactory()

    suspend fun checkForUpdates(): UpdateResult {
        return try {
            val response = NetworkService().get("/api/updates/check")
            if (response.status == 200) {
                val updateInfo = Json.decodeFromString<UpdateInfo>(response.body)
                UpdateResult.Success(updateInfo)
            } else UpdateResult.NoUpdate
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun downloadUpdate(updateInfo: UpdateInfo): Boolean {
        return try {
            val response = NetworkService().get(updateInfo.downloadUrl)
            if (response.status == 200) {
                val configData = Json.decodeFromString<Map<String, ComponentConfig>>(response.body)
                configData.forEach { (key, config) -> configCache.put(key, config) }
                true
            } else false
        } catch (_: Exception) { false }
    }

    @Composable
    fun DynamicComponent(
        componentId: String,
        fallbackContent: @Composable () -> Unit = {}
    ) {
        val config = configCache.get(componentId)
        if (config != null) {
            componentFactory.CreateComponent(config)
        } else {
            fallbackContent()
        }
    }
}

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val components: List<String>
)

sealed class UpdateResult {
    data class Success(val updateInfo: UpdateInfo) : UpdateResult()
    object NoUpdate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

@kotlinx.serialization.Serializable
data class ComponentConfig(
    val type: String,
    val props: Map<String, @kotlinx.serialization.Contextual Any>,
    val style: Map<String, String>,
    val children: List<ComponentConfig> = emptyList()
)
