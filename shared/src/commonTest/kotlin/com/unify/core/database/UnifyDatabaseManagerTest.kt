package com.unify.core.database

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/**
 * 数据库管理器跨平台测试
 */
class UnifyDatabaseManagerTest {

    @Test
    fun testDatabaseManagerCreation() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        assertNotNull(databaseManager)
    }

    @Test
    fun testDatabaseInitialization() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        assertNotNull(database)
    }

    @Test
    fun testDatabaseVersion() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val version = databaseManager.getDatabaseVersion()
        assertTrue(version > 0)
    }

    @Test
    fun testTransactionExecution() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val result = databaseManager.transaction {
            "transaction_result"
        }
        
        assertEquals("transaction_result", result)
    }

    @Test
    fun testDatabaseStats() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val stats = databaseManager.getDatabaseStats()
        assertNotNull(stats)
        assertTrue(stats.totalTables >= 0)
        assertTrue(stats.totalRecords >= 0)
        assertTrue(stats.databaseSize >= 0)
    }

    @Test
    fun testUserDao() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        val userDao = UserDao(database)
        
        // 测试插入用户
        userDao.insertUser(
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User"
        )
        
        // 测试查询用户
        val users = userDao.getAllUsers().executeAsList()
        assertTrue(users.isNotEmpty())
    }

    @Test
    fun testConfigDao() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        val configDao = ConfigDao(database)
        
        // 测试设置配置
        configDao.setConfig("test_key", "test_value")
        
        // 测试获取配置
        val config = configDao.getConfigByKey("test_key").executeAsOneOrNull()
        assertNotNull(config)
        assertEquals("test_value", config.value)
    }

    @Test
    fun testCacheDao() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        val cacheDao = CacheDao(database)
        
        // 测试设置缓存
        cacheDao.setCache("cache_key", "cache_value")
        
        // 测试获取缓存
        val cachedValue = cacheDao.getCacheByKey("cache_key")
        assertEquals("cache_value", cachedValue)
        
        // 测试清除缓存
        cacheDao.clearAllCache()
        val clearedValue = cacheDao.getCacheByKey("cache_key")
        assertEquals(null, clearedValue)
    }

    @Test
    fun testSyncDao() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        val database = databaseManager.getDatabase()
        val syncDao = SyncDao(database)
        
        // 测试添加同步记录
        syncDao.addSyncRecord(
            tableName = "User",
            recordId = "123",
            action = "create",
            data = """{"username": "testuser"}"""
        )
        
        // 测试获取未同步记录
        val unsyncedRecords = syncDao.getUnsyncedRecords().executeAsList()
        assertTrue(unsyncedRecords.isNotEmpty())
        
        // 测试标记为已同步
        val recordId = unsyncedRecords.first().id
        syncDao.markRecordSynced(recordId)
        
        // 测试清理旧记录
        syncDao.cleanOldSyncRecords()
    }

    @Test
    fun testDatabaseExportImport() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        // 测试导出数据库
        val exportedData = databaseManager.exportDatabase()
        assertNotNull(exportedData)
        
        // 测试导入数据库
        val importResult = databaseManager.importDatabase(exportedData)
        assertTrue(importResult)
    }

    @Test
    fun testClearAllData() = runTest {
        val databaseManager = UnifyDatabaseManager.create()
        databaseManager.initialize()
        
        // 添加一些测试数据
        val database = databaseManager.getDatabase()
        val cacheDao = CacheDao(database)
        cacheDao.setCache("test_key", "test_value")
        
        // 清除所有数据
        databaseManager.clearAllData()
        
        // 验证数据已清除
        val cachedValue = cacheDao.getCacheByKey("test_key")
        assertEquals(null, cachedValue)
    }
}
