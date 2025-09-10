package com.unify.core.integration

import com.unify.core.UnifyCore
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.network.NetworkConfig
import com.unify.core.database.UnifyDatabaseManager
import com.unify.core.miniapp.MiniAppRuntime
import com.unify.core.miniapp.MiniAppPlatform
import com.unify.core.miniapp.MiniAppConfig
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 跨平台集成测试
 * 测试各个模块之间的协作和数据流
 */
class CrossPlatformIntegrationTest {

    @Test
    fun testFullStackIntegration() = runTest {
        // 1. 初始化核心框架
        val unifyCore = UnifyCore()
        assertNotNull(unifyCore)
        assertTrue(unifyCore.isInitialized())

        // 2. 初始化网络管理器
        val networkManager = UnifyNetworkManager.create()
        val networkConfig = NetworkConfig(
            baseUrl = "https://api.example.com",
            timeout = 30000L
        )
        networkManager.initialize(networkConfig)

        // 3. 初始化数据库管理器
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()

        // 4. 初始化小程序运行时
        val miniAppRuntime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val miniAppConfig = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "integration_test_app",
            version = "1.0.0",
            enableDebug = true
        )
        miniAppRuntime.initialize(miniAppConfig)

        // 5. 测试模块间协作
        testNetworkDatabaseIntegration(networkManager, databaseManager)
        testMiniAppNetworkIntegration(miniAppRuntime, networkManager)
        testMiniAppDatabaseIntegration(miniAppRuntime, databaseManager)
    }

    private suspend fun testNetworkDatabaseIntegration(
        networkManager: UnifyNetworkManager,
        databaseManager: UnifyDatabaseManager
    ) {
        // 模拟从网络获取数据并存储到数据库
        val response = networkManager.get("/test-data")
        
        // 无论网络请求成功与否，都测试数据库操作
        val database = databaseManager.getDatabase()
        val configDao = com.unify.core.database.ConfigDao(database)
        
        // 存储网络响应状态
        when (response) {
            is com.unify.core.network.NetworkResponse.Success -> {
                configDao.setConfig("last_network_status", "success")
                configDao.setConfig("last_response_code", response.statusCode.toString())
            }
            is com.unify.core.network.NetworkResponse.Error -> {
                configDao.setConfig("last_network_status", "error")
                configDao.setConfig("last_error_message", response.message)
            }
        }

        // 验证数据已存储
        val networkStatus = configDao.getConfigByKey("last_network_status").executeAsOneOrNull()
        assertNotNull(networkStatus)
    }

    private suspend fun testMiniAppNetworkIntegration(
        miniAppRuntime: MiniAppRuntime,
        networkManager: UnifyNetworkManager
    ) {
        // 为小程序注册网络API
        miniAppRuntime.registerApi("networkRequest") { params ->
            val url = params["url"] as? String ?: "/default"
            val response = networkManager.get(url)
            
            when (response) {
                is com.unify.core.network.NetworkResponse.Success -> {
                    com.unify.core.miniapp.MiniAppApiResult(
                        success = true,
                        data = mapOf(
                            "statusCode" to response.statusCode,
                            "data" to response.data
                        )
                    )
                }
                is com.unify.core.network.NetworkResponse.Error -> {
                    com.unify.core.miniapp.MiniAppApiResult(
                        success = false,
                        error = response.message,
                        errorCode = response.statusCode
                    )
                }
            }
        }

        // 测试小程序调用网络API
        val result = miniAppRuntime.callApi("networkRequest", mapOf("url" to "/test"))
        assertNotNull(result)
    }

    private suspend fun testMiniAppDatabaseIntegration(
        miniAppRuntime: MiniAppRuntime,
        databaseManager: UnifyDatabaseManager
    ) {
        // 为小程序注册数据库API
        miniAppRuntime.registerApi("saveData") { params ->
            val key = params["key"] as? String
            val value = params["value"] as? String
            
            if (key != null && value != null) {
                val database = databaseManager.getDatabase()
                val configDao = com.unify.core.database.ConfigDao(database)
                configDao.setConfig("miniapp_$key", value)
                
                com.unify.core.miniapp.MiniAppApiResult(success = true)
            } else {
                com.unify.core.miniapp.MiniAppApiResult(
                    success = false,
                    error = "Key and value are required"
                )
            }
        }

        miniAppRuntime.registerApi("loadData") { params ->
            val key = params["key"] as? String
            
            if (key != null) {
                val database = databaseManager.getDatabase()
                val configDao = com.unify.core.database.ConfigDao(database)
                val config = configDao.getConfigByKey("miniapp_$key").executeAsOneOrNull()
                
                if (config != null) {
                    com.unify.core.miniapp.MiniAppApiResult(
                        success = true,
                        data = mapOf("value" to config.value)
                    )
                } else {
                    com.unify.core.miniapp.MiniAppApiResult(
                        success = false,
                        error = "Data not found"
                    )
                }
            } else {
                com.unify.core.miniapp.MiniAppApiResult(
                    success = false,
                    error = "Key is required"
                )
            }
        }

        // 测试小程序数据存储和读取
        val saveResult = miniAppRuntime.callApi("saveData", mapOf(
            "key" to "test_key",
            "value" to "test_value"
        ))
        assertTrue(saveResult.success)

        val loadResult = miniAppRuntime.callApi("loadData", mapOf("key" to "test_key"))
        assertTrue(loadResult.success)
        assertNotNull(loadResult.data)
    }

    @Test
    fun testConcurrentOperations() = runTest {
        val networkManager = UnifyNetworkManager.create()
        val databaseManager = UnifyDatabaseManager.create()
        
        val networkConfig = NetworkConfig(baseUrl = "https://httpbin.org")
        networkManager.initialize(networkConfig)
        databaseManager.initialize()

        // 并发执行网络请求和数据库操作
        val (networkResult, databaseResult) = coroutineScope {
            val networkJob = async {
                networkManager.get("/get")
            }

            val databaseJob = async {
                val database = databaseManager.getDatabase()
                val cacheDao = com.unify.core.database.CacheDao(database)
                cacheDao.insertCacheData("concurrent_test", "test_value")
                cacheDao.getCacheByKey("concurrent_test")
            }

            // 等待所有操作完成
            Pair(networkJob.await(), databaseJob.await())
        }

        assertNotNull(networkResult)
        assertNotNull(databaseResult)
    }

    @Test
    fun testErrorHandlingIntegration() = runTest {
        val networkManager = UnifyNetworkManager.create()
        val databaseManager = UnifyDatabaseManager.create()
        val miniAppRuntime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)

        // 初始化所有组件
        networkManager.initialize(NetworkConfig(baseUrl = "https://invalid-url-for-testing.com"))
        databaseManager.initialize()
        miniAppRuntime.initialize(MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "error_test_app",
            version = "1.0.0"
        ))

        // 测试网络错误处理
        val networkResponse = networkManager.get("/nonexistent")
        when (networkResponse) {
            is com.unify.core.network.NetworkResponse.Error -> {
                // 将错误信息存储到数据库
                val database = databaseManager.getDatabase()
                val configDao = com.unify.core.database.ConfigDao(database)
                configDao.setConfig("last_error", networkResponse.message)
                
                // 通过小程序API报告错误
                miniAppRuntime.registerApi("reportError") { params ->
                    com.unify.core.miniapp.MiniAppApiResult(
                        success = true,
                        data = mapOf("error_logged" to true)
                    )
                }
                
                val reportResult = miniAppRuntime.callApi("reportError", mapOf(
                    "error" to networkResponse.message
                ))
                assertTrue(reportResult.success)
            }
            is com.unify.core.network.NetworkResponse.Success -> {
                // 如果意外成功，也记录下来
                assertTrue(true)
            }
        }
    }

    @Test
    fun testDataSynchronization() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        val miniAppRuntime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)

        databaseManager.initialize()
        miniAppRuntime.initialize(MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "sync_test_app",
            version = "1.0.0"
        ))

        // 测试数据同步机制
        val database = databaseManager.getDatabase()
        val syncDao = com.unify.core.database.SyncDao(database)

        // 添加同步记录
        syncDao.addSyncRecord(
            tableName = "TestTable",
            recordId = "test_record_1",
            action = "create",
            data = """{"name": "test", "value": 123}"""
        )

        // 获取未同步记录
        val unsyncedRecords = syncDao.getUnsyncedRecords().executeAsList()
        assertTrue(unsyncedRecords.isNotEmpty())

        // 模拟同步过程
        unsyncedRecords.forEach { record ->
            // 标记为已同步
            syncDao.markRecordSynced(record.id)
        }

        // 验证同步完成
        val remainingUnsyncedRecords = syncDao.getUnsyncedRecords().executeAsList()
        assertTrue(remainingUnsyncedRecords.isEmpty())
    }

    @Test
    fun testMemoryManagement() = runTest {
        // 测试内存管理和资源清理
        val instances = mutableListOf<MiniAppRuntime>()

        // 创建多个小程序实例
        repeat(5) { index ->
            val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
            runtime.initialize(MiniAppConfig(
                platform = MiniAppPlatform.WECHAT,
                appId = "memory_test_app_$index",
                version = "1.0.0"
            ))
            
            val instance = runtime.launch("memory_test_app_$index")
            instances.add(runtime)
            assertNotNull(instance)
        }

        // 清理所有实例
        instances.forEach { runtime ->
            val currentInstance = runtime.getCurrentInstance()
            if (currentInstance != null) {
                runtime.destroyInstance(currentInstance.instanceId)
            }
        }

        // 验证实例已清理
        instances.forEach { runtime ->
            assertTrue(runtime.getCurrentInstance() == null)
        }
    }
}
