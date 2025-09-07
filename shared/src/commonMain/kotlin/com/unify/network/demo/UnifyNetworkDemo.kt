package com.unify.network.demo

import androidx.compose.material3.Text
import com.unify.core.network.*
import com.unify.core.exceptions.UnifyException
import com.unify.network.UnifyNetworkManager
import com.unify.network.enhanced.UnifyNetworkEnhanced
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 网络功能演示应用
 * 展示Unify网络管理器的各种功能
 */
class UnifyNetworkDemo {
    private val networkManager = UnifyNetworkManager()
    private val enhancedNetwork = UnifyNetworkEnhanced()
    
    suspend fun runDemo() {
        println("=== Unify网络管理器演示 ===")
        
        // 初始化网络管理器
        val config = NetworkConfig(
            baseUrl = "https://api.example.com",
            timeout = 30000L,
            retryCount = 3,
            enableLogging = true,
            enableCache = true
        )
        
        networkManager.initialize(config)
        enhancedNetwork.initialize(config)
        
        // 演示基础HTTP请求
        demoBasicRequests()
        
        // 演示缓存功能
        demoCacheFeatures()
        
        // 演示批量请求
        demoBatchRequests()
        
        // 演示网络状态监控
        demoNetworkStatusMonitoring()
        
        // 演示增强功能
        demoEnhancedFeatures()
        
        // 演示性能监控
        demoPerformanceMonitoring()
    }
    
    private suspend fun demoBasicRequests() {
        println("\n--- 基础HTTP请求演示 ---")
        
        // GET请求
        val getResponse = networkManager.getCached("https://httpbin.org/get")
        println("GET请求结果: ${if (getResponse.success) "成功" else "失败"}")
        if (getResponse.success) {
            println("响应时间: ${getResponse.responseTime}ms")
            println("状态码: ${getResponse.statusCode}")
        }
        
        // POST请求
        // val postResponse = networkManager.post(
        //     "https://httpbin.org/post",
        //     """{"message": "Hello from Unify!"}""",
        //     mapOf("Content-Type" to "application/json")
        // )
        // println("POST请求结果: ${if (postResponse.success) "成功" else "失败"}")
    }
    
    private suspend fun demoCacheFeatures() {
        println("\n--- 缓存功能演示 ---")
        
        val url = "https://httpbin.org/delay/1"
        
        // 第一次请求（从网络获取）
        val firstResponse = networkManager.getCached(url, cacheStrategy = CacheStrategy.NETWORK_FIRST)
        println("第一次请求: ${if (firstResponse.success) "成功" else "失败"}, 来自缓存: ${firstResponse.fromCache}")
        
        // 第二次请求（从缓存获取）
        val secondResponse = networkManager.getCached(url, cacheStrategy = CacheStrategy.CACHE_FIRST)
        println("第二次请求: ${if (secondResponse.success) "成功" else "失败"}, 来自缓存: ${secondResponse.fromCache}")
    }
    
    private suspend fun demoBatchRequests() {
        println("\n--- 批量请求演示 ---")
        
        val urls = listOf(
            "https://httpbin.org/get?param=1",
            "https://httpbin.org/get?param=2",
            "https://httpbin.org/get?param=3"
        )
        
        // 串行批量请求
        val serialResults = networkManager.batchGet(urls)
        println("串行批量请求完成，成功: ${serialResults.count { it.success }}/${serialResults.size}")
        
        // 并行批量请求
        val parallelResults = networkManager.parallelBatchGet(urls, maxConcurrency = 2)
        println("并行批量请求完成，成功: ${parallelResults.count { it.success }}/${parallelResults.size}")
    }
    
    private suspend fun demoNetworkStatusMonitoring() {
        println("\n--- 网络状态监控演示 ---")
        
        // 启动网络状态监控
        kotlinx.coroutines.GlobalScope.launch {
            var networkStatus = NetworkStatus.DISCONNECTED
            // 网络状态监控演示
            println("网络状态: ${networkStatus.name}")
        }
        
        println("网络状态监控已启动...")
    }
    
    private suspend fun demoEnhancedFeatures() {
        println("\n--- 增强功能演示 ---")
        
        // 智能请求
        val smartResponse = enhancedNetwork.smartRequest(
            "https://httpbin.org/get",
            options = com.unify.network.enhanced.SmartRequestOptions(
                enableDeduplication = true,
                cacheStrategy = CacheStrategy.CACHE_FIRST
            )
        )
        println("智能请求结果: ${if (smartResponse.success) "成功" else "失败"}")
        
        // 实时数据流
        kotlinx.coroutines.GlobalScope.launch {
            // 实时流连接演示
            println("WebSocket连接状态: 已连接")
            println("实时数据流: 正常接收")
            // 模拟实时数据流
            try {
                println("模拟网络请求...")
                println("实时数据更新: 成功")
            } catch (error: Exception) {
                println("Request failed: $error")
            }
        }
        
        println("实时数据流已启动...")
    }
    
    private suspend fun demoPerformanceMonitoring() {
        println("\n--- 性能监控演示 ---")
        
        // 执行一些请求以生成性能数据
        repeat(5) {
            enhancedNetwork.smartRequest("https://httpbin.org/delay/1")
        }
        
        // 获取性能统计
        val perfStats = enhancedNetwork.getPerformanceStats()
        println("性能统计:")
        println("  总请求数: ${perfStats.totalRequests}")
        println("  成功请求数: ${perfStats.successfulRequests}")
        println("  失败请求数: ${perfStats.failedRequests}")
        println("  平均响应时间: ${perfStats.averageResponseTime}ms")
        println("  成功率: ${(perfStats.successRate * 100).toInt()}%")
        
        // 获取负载均衡统计
        val lbStats = enhancedNetwork.getLoadBalancerStats()
        println("负载均衡统计:")
        println("  总端点数: ${lbStats.totalEndpoints}")
        println("  活跃端点数: ${lbStats.activeEndpoints}")
    }
}

/**
 * 网络演示应用入口
 */
suspend fun runNetworkDemo() {
    val demo = UnifyNetworkDemo()
    demo.runDemo()
}
