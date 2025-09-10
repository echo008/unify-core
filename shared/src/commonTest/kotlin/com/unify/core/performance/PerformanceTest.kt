package com.unify.core.performance

import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.NetworkConfig
import com.unify.core.database.UnifyDatabaseManager
import com.unify.core.database.UserDao
import com.unify.core.database.CacheDao
import com.unify.core.miniapp.MiniAppRuntime
import com.unify.core.miniapp.MiniAppPlatform
import com.unify.core.miniapp.MiniAppConfig
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.measureTime

/**
 * 性能测试和基准测试
 */
class PerformanceTest {

    @Test
    fun testNetworkManagerPerformance() = runTest {
        val networkManager = UnifyNetworkManager.create()
        val config = NetworkConfig(
            baseUrl = "https://httpbin.org",
            timeout = 10000L
        )
        
        // 测试初始化性能
        val initTime = measureTime {
            networkManager.initialize(config)
        }
        println("Network Manager initialization time: $initTime")
        assertTrue(initTime.inWholeMilliseconds < 1000) // 应该在1秒内完成

        // 测试并发请求性能
        val concurrentRequestTime = measureTime {
            coroutineScope {
                val jobs = (1..10).map { index ->
                    async {
                        networkManager.get("/get?test=$index")
                    }
                }
                jobs.awaitAll()
            }
        }
        println("10 concurrent requests time: $concurrentRequestTime")
        assertTrue(concurrentRequestTime.inWholeMilliseconds < 30000) // 应该在30秒内完成
    }

    @Test
    fun testDatabasePerformance() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        
        // 测试数据库初始化性能
        val initTime = measureTime {
            databaseManager.initialize()
        }
        println("Database initialization time: $initTime")
        assertTrue(initTime.inWholeMilliseconds < 5000) // 应该在5秒内完成

        val database = databaseManager.getDatabase()
        val userDao = UserDao(database)
        val cacheDao = CacheDao(database)

        // 测试批量插入性能
        val batchInsertTime = measureTime {
            repeat(100) { index ->
                userDao.insertUser(
                    username = "user_$index",
                    email = "user$index@test.com",
                    displayName = "User $index"
                )
            }
        }
        println("100 user insertions time: $batchInsertTime")
        assertTrue(batchInsertTime.inWholeMilliseconds < 10000) // 应该在10秒内完成

        // 测试批量查询性能
        val batchQueryTime = measureTime {
            repeat(100) {
                userDao.getAllUsers().executeAsList()
            }
        }
        println("100 user queries time: $batchQueryTime")
        assertTrue(batchQueryTime.inWholeMilliseconds < 5000) // 应该在5秒内完成

        // 测试缓存操作性能
        val cacheOperationTime = measureTime {
            repeat(1000) { index ->
                cacheDao.insertCacheData("cache_key_$index", "cache_value_$index")
                cacheDao.getCacheByKey("cache_key_$index")
            }
        }
        println("1000 cache operations time: $cacheOperationTime")
        assertTrue(cacheOperationTime.inWholeMilliseconds < 5000) // 应该在5秒内完成
    }

    @Test
    fun testMiniAppRuntimePerformance() = runTest {
        val platforms = listOf(
            MiniAppPlatform.WECHAT,
            MiniAppPlatform.ALIPAY,
            MiniAppPlatform.BYTEDANCE
        )

        // 测试多平台初始化性能
        val multiPlatformInitTime = measureTime {
            platforms.forEach { platform ->
                val runtime = MiniAppRuntime.create(platform)
                val config = MiniAppConfig(
                    platform = platform,
                    appId = "perf_test_${platform.name.lowercase()}",
                    version = "1.0.0"
                )
                runtime.initialize(config)
            }
        }
        println("Multi-platform initialization time: $multiPlatformInitTime")
        assertTrue(multiPlatformInitTime.inWholeMilliseconds < 10000) // 应该在10秒内完成

        // 测试API注册和调用性能
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        runtime.initialize(MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "perf_test_app",
            version = "1.0.0"
        ))

        val apiRegistrationTime = measureTime {
            repeat(100) { index ->
                runtime.registerApi("testApi_$index") { params ->
                    com.unify.core.miniapp.MiniAppApiResult(
                        success = true,
                        data = mapOf("index" to index)
                    )
                }
            }
        }
        println("100 API registrations time: $apiRegistrationTime")
        assertTrue(apiRegistrationTime.inWholeMilliseconds < 1000) // 应该在1秒内完成

        val apiCallTime = measureTime {
            repeat(100) { index ->
                runtime.callApi("testApi_$index", emptyMap())
            }
        }
        println("100 API calls time: $apiCallTime")
        assertTrue(apiCallTime.inWholeMilliseconds < 2000) // 应该在2秒内完成
    }

    @Test
    fun testMemoryUsage() = runTest {
        // 获取初始内存使用情况（简化实现）
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // 创建大量对象测试内存使用
        val objects = mutableListOf<Any>()
        
        repeat(1000) { index ->
            val networkManager = UnifyNetworkManager.create()
            val databaseManager = UnifyDatabaseManager.create()
            val miniAppRuntime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
            
            objects.add(networkManager)
            objects.add(databaseManager)
            objects.add(miniAppRuntime)
        }
        
        val peakMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = peakMemory - initialMemory
        
        println("Memory increase: ${memoryIncrease / 1024 / 1024} MB")
        
        // 清理对象
        objects.clear()
        System.gc()
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryAfterCleanup = finalMemory - initialMemory
        
        println("Memory after cleanup: ${memoryAfterCleanup / 1024 / 1024} MB")
        
        // 验证内存泄漏不严重
        assertTrue(memoryAfterCleanup < memoryIncrease / 2) // 清理后内存应该显著减少
    }

    @Test
    fun testConcurrentAccess() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        val cacheDao = CacheDao(database)

        // 测试并发数据库访问
        val concurrentAccessTime = measureTime {
            coroutineScope {
                val jobs = (1..50).map { index ->
                    async {
                        repeat(10) { iteration ->
                            cacheDao.insertCacheData("concurrent_key_${index}_$iteration", "value_$iteration")
                            cacheDao.getCacheByKey("concurrent_key_${index}_$iteration")
                        }
                    }
                }
                jobs.awaitAll()
            }
        }
        
        println("Concurrent database access time: $concurrentAccessTime")
        assertTrue(concurrentAccessTime.inWholeMilliseconds < 15000) // 应该在15秒内完成
    }

    @Test
    fun testLargeDataHandling() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        val cacheDao = CacheDao(database)

        // 测试大数据处理
        val largeData = "x".repeat(10000) // 10KB数据
        
        val largeDataTime = measureTime {
            repeat(100) { index ->
                cacheDao.insertCacheData("large_data_$index", largeData)
            }
        }
        
        println("Large data storage time: $largeDataTime")
        assertTrue(largeDataTime.inWholeMilliseconds < 10000) // 应该在10秒内完成

        val largeDataRetrievalTime = measureTime {
            repeat(100) { index ->
                val retrievedData = cacheDao.getCacheByKey("large_data_$index")
                assertTrue(retrievedData == largeData)
            }
        }
        
        println("Large data retrieval time: $largeDataRetrievalTime")
        assertTrue(largeDataRetrievalTime.inWholeMilliseconds < 5000) // 应该在5秒内完成
    }

    @Test
    fun testStartupPerformance() = runTest {
        // 测试完整系统启动性能
        val startupTime = measureTime {
            // 网络管理器启动
            val networkManager = UnifyNetworkManager.create()
            networkManager.initialize(NetworkConfig(
                baseUrl = "https://api.example.com",
                timeout = 30000L
            ))

            // 数据库管理器启动
            val databaseManager = UnifyDatabaseManager.create()
            databaseManager.initialize()

            // 小程序运行时启动
            val miniAppRuntime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
            miniAppRuntime.initialize(MiniAppConfig(
                platform = MiniAppPlatform.WECHAT,
                appId = "startup_test_app",
                version = "1.0.0"
            ))

            // 注册基础API
            miniAppRuntime.registerApi("getSystemInfo") { params ->
                com.unify.core.miniapp.MiniAppApiResult(
                    success = true,
                    data = mapOf("platform" to "test")
                )
            }
        }
        
        println("Complete system startup time: $startupTime")
        assertTrue(startupTime.inWholeMilliseconds < 10000) // 应该在10秒内完成
    }

    @Test
    fun testThroughput() = runTest {
        val networkManager = UnifyNetworkManager.create()
        networkManager.initialize(NetworkConfig(
            baseUrl = "https://httpbin.org",
            timeout = 5000L
        ))

        // 测试网络请求吞吐量
        val requestCount = 20
        val throughputTime = measureTime {
            coroutineScope {
                val jobs = (1..requestCount).map { index ->
                    async {
                        networkManager.get("/get?id=$index")
                    }
                }
                jobs.awaitAll()
            }
        }
        
        val throughput = requestCount.toDouble() / throughputTime.inWholeSeconds
        println("Network throughput: $throughput requests/second")
        assertTrue(throughput > 0.5) // 至少每秒0.5个请求
    }
}

/**
 * 基准测试工具类
 */
object BenchmarkUtils {
    /**
     * 执行基准测试
     */
    suspend fun benchmark(
        name: String,
        iterations: Int = 100,
        warmupIterations: Int = 10,
        operation: suspend () -> Unit
    ): BenchmarkResult {
        // 预热
        repeat(warmupIterations) {
            operation()
        }

        // 正式测试
        val times = mutableListOf<Long>()
        repeat(iterations) {
            val time = measureTime {
                operation()
            }
            times.add(time.inWholeNanoseconds)
        }

        val avgTime = times.average()
        val minTime = times.minOrNull() ?: 0L
        val maxTime = times.maxOrNull() ?: 0L
        val medianTime = times.sorted()[times.size / 2]

        return BenchmarkResult(
            name = name,
            iterations = iterations,
            averageTimeNs = avgTime.toLong(),
            minTimeNs = minTime,
            maxTimeNs = maxTime,
            medianTimeNs = medianTime
        )
    }
}

/**
 * 基准测试结果
 */
data class BenchmarkResult(
    val name: String,
    val iterations: Int,
    val averageTimeNs: Long,
    val minTimeNs: Long,
    val maxTimeNs: Long,
    val medianTimeNs: Long
) {
    fun printResults() {
        println("=== Benchmark Results: $name ===")
        println("Iterations: $iterations")
        println("Average: ${averageTimeNs / 1_000_000.0} ms")
        println("Min: ${minTimeNs / 1_000_000.0} ms")
        println("Max: ${maxTimeNs / 1_000_000.0} ms")
        println("Median: ${medianTimeNs / 1_000_000.0} ms")
        println("=====================================")
    }
}
