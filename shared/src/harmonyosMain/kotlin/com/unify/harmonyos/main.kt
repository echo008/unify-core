package com.unify.harmonyos

import com.unify.core.database.UnifyDatabaseManager
import com.unify.core.network.NetworkConfig
import com.unify.core.network.UnifyNetworkManager
import kotlinx.coroutines.runBlocking

/**
 * HarmonyOS平台主入口
 * 初始化Unify-Core框架并启动HarmonyOS应用
 */
fun main() {
    println("Starting Unify-Core on HarmonyOS...")
    
    runBlocking {
        try {
            // 初始化网络管理器
            val networkManager = UnifyNetworkManager.create()
            networkManager.initialize(NetworkConfig(
                baseUrl = "https://api.unify.com",
                timeoutMs = 30000L,
                connectTimeoutMs = 10000L,
                socketTimeoutMs = 30000L,
                defaultHeaders = mapOf(
                    "User-Agent" to "Unify-Core-HarmonyOS/1.0",
                    "Platform" to "HarmonyOS"
                )
            ))
            
            // 初始化数据库管理器
            val databaseManager = UnifyDatabaseManager.create()
            databaseManager.initialize()
            
            println("Unify-Core initialized successfully on HarmonyOS")
            
            // 启动HarmonyOS应用逻辑
            startHarmonyOSApp(networkManager, databaseManager)
            
        } catch (e: Exception) {
            println("Failed to initialize Unify-Core on HarmonyOS: ${e.message}")
            e.printStackTrace()
        }
    }
}

/**
 * 启动HarmonyOS应用
 */
private suspend fun startHarmonyOSApp(
    networkManager: UnifyNetworkManager,
    databaseManager: UnifyDatabaseManager
) {
    println("HarmonyOS application started with Unify-Core")
    
    // 这里可以添加HarmonyOS特定的应用逻辑
    // 例如：UI初始化、服务启动、事件监听等
    
    // 示例：测试网络连接
    try {
        val response = networkManager.get("https://httpbin.org/get")
        when (response) {
            is com.unify.core.network.NetworkResponse.Success -> {
                println("Network test successful: ${response.statusCode}")
            }
            is com.unify.core.network.NetworkResponse.Error -> {
                println("Network test failed: ${response.message}")
            }
        }
    } catch (e: Exception) {
        println("Network test error: ${e.message}")
    }
    
    // 示例：测试数据库操作
    try {
        val database = databaseManager.getDatabase()
        val stats = databaseManager.getDatabaseStats()
        println("Database initialized with ${stats.totalTables} tables")
    } catch (e: Exception) {
        println("Database test error: ${e.message}")
    }
}
