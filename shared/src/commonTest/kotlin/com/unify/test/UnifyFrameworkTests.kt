package com.unify.test

import kotlin.test.*
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.*
import com.unify.ui.state.*
import com.unify.network.*
import com.unify.storage.*
import com.unify.i18n.*
import com.unify.sample.data.User
import com.unify.sample.presentation.*

/**
 * Unify KMP 框架核心功能测试用例
 */

/**
 * MVI状态管理测试
 */
class MVIStateManagerTest : UnifyEnhancedTestBase() {
    
    private lateinit var mviHelper: EnhancedMVITestHelper<UserState, UserIntent, UserEffect>
    private lateinit var stateManager: EnhancedMVIStateManager<UserState, UserIntent, UserEffect>
    
    override fun setupTestEnvironment() {
        mviHelper = EnhancedMVITestHelper()
        stateManager = EnhancedMVIStateManager(
            initialState = UserState(),
            reducer = UserReducer(),
            middleware = listOf(UserMiddleware())
        )
    }
    
    @Test
    fun testInitialState() = runTest {
        val initialState = stateManager.stateFlow.value
        assertEquals(UserState(), initialState)
        assertFalse(initialState.isLoading)
        assertTrue(initialState.users.isEmpty())
        assertNull(initialState.error)
    }
    
    @Test
    fun testLoadUsersIntent() = runTest {
        val stateJob = mviHelper.collectStates(stateManager, this)
        val effectJob = mviHelper.collectEffects(stateManager, this)
        
        mviHelper.sendIntent(stateManager, UserIntent.LoadUsers)
        
        advanceUntilIdle()
        
        mviHelper.assertStateSequence(
            UserState(), // 初始状态
            UserState(isLoading = true) // 加载状态
        )
        
        stateJob.cancel()
        effectJob.cancel()
    }
    
    @Test
    fun testCreateUserIntent() = runTest {
        val testUser = User(id = 1, username = "test", email = "test@example.com")
        val stateJob = mviHelper.collectStates(stateManager, this)
        
        mviHelper.sendIntent(stateManager, UserIntent.CreateUser(testUser))
        
        advanceUntilIdle()
        
        val finalState = mviHelper.getStateHistory().last()
        assertTrue(finalState.users.contains(testUser))
        
        stateJob.cancel()
    }
    
    @Test
    fun testErrorHandling() = runTest {
        val stateJob = mviHelper.collectStates(stateManager, this)
        val testError = "Network error"
        
        mviHelper.sendIntent(stateManager, UserIntent.HandleError(testError))
        
        advanceUntilIdle()
        
        val finalState = mviHelper.getStateHistory().last()
        assertEquals(testError, finalState.error)
        assertFalse(finalState.isLoading)
        
        stateJob.cancel()
    }
}

/**
 * 网络服务测试
 */
class NetworkServiceTest : UnifyEnhancedTestBase() {
    
    private lateinit var networkHelper: NetworkServiceTestHelper
    private lateinit var networkService: UnifyNetworkService
    
    override fun setupTestEnvironment() {
        networkHelper = NetworkServiceTestHelper()
        // 这里应该创建模拟的网络服务实现
        networkService = MockNetworkService(networkHelper)
    }
    
    @Test
    fun testGetRequest() = runTest {
        val expectedResponse = listOf(
            User(1, "user1", "user1@example.com"),
            User(2, "user2", "user2@example.com")
        )
        networkHelper.mockResponse("/api/users", expectedResponse)
        
        val result = networkService.get<List<User>>("/api/users")
        
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        networkHelper.assertRequestMade("/api/users", "GET")
    }
    
    @Test
    fun testPostRequest() = runTest {
        val newUser = User(0, "newuser", "newuser@example.com")
        val createdUser = newUser.copy(id = 3)
        
        networkHelper.mockResponse("/api/users", createdUser)
        
        val result = networkService.post<User, User>("/api/users", newUser)
        
        assertTrue(result.isSuccess)
        assertEquals(createdUser, result.getOrNull())
        networkHelper.assertRequestMade("/api/users", "POST")
    }
    
    @Test
    fun testNetworkError() = runTest {
        // 不设置模拟响应，模拟网络错误
        val result = networkService.get<List<User>>("/api/nonexistent")
        
        assertTrue(result.isFailure)
        networkHelper.assertRequestMade("/api/nonexistent", "GET")
    }
}

/**
 * 存储服务测试
 */
class StorageServiceTest : UnifyEnhancedTestBase() {
    
    private lateinit var storage: UnifyStorage
    
    override fun setupTestEnvironment() {
        storage = MockUnifyStorage()
    }
    
    @Test
    fun testStoreAndRetrieve() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        storage.store(key, value)
        val retrieved = storage.retrieve<String>(key)
        
        assertEquals(value, retrieved)
    }
    
    @Test
    fun testStoreObject() = runTest {
        val key = "user_key"
        val user = User(1, "testuser", "test@example.com")
        
        storage.store(key, user)
        val retrieved = storage.retrieve<User>(key)
        
        assertEquals(user, retrieved)
    }
    
    @Test
    fun testRemove() = runTest {
        val key = "remove_test"
        val value = "to_be_removed"
        
        storage.store(key, value)
        assertTrue(storage.contains(key))
        
        storage.remove(key)
        assertFalse(storage.contains(key))
        assertNull(storage.retrieve<String>(key))
    }
    
    @Test
    fun testClear() = runTest {
        storage.store("key1", "value1")
        storage.store("key2", "value2")
        
        assertTrue(storage.contains("key1"))
        assertTrue(storage.contains("key2"))
        
        storage.clear()
        
        assertFalse(storage.contains("key1"))
        assertFalse(storage.contains("key2"))
    }
}

/**
 * 国际化系统测试
 */
class I18nTest : UnifyEnhancedTestBase() {
    
    private lateinit var i18nHelper: I18nTestHelper
    
    override fun setupTestEnvironment() {
        i18nHelper = I18nTestHelper()
        UnifyI18n.initialize(Locale.ENGLISH)
    }
    
    @Test
    fun testBasicTranslation() {
        val translations = mapOf(
            Locale.ENGLISH to "OK",
            Locale.CHINESE to "确定",
            Locale.JAPANESE to "OK"
        )
        
        i18nHelper.testTranslations("common.ok", translations)
    }
    
    @Test
    fun testParameterizedTranslation() {
        i18nHelper.testParameterizedTranslation(
            key = "user.delete.confirm",
            locale = Locale.CHINESE,
            args = arrayOf("张三"),
            expected = "确定要删除用户 张三 吗？"
        )
        
        i18nHelper.testParameterizedTranslation(
            key = "user.delete.confirm",
            locale = Locale.ENGLISH,
            args = arrayOf("John"),
            expected = "Are you sure you want to delete user John?"
        )
    }
    
    @Test
    fun testLocaleSwitch() {
        i18nHelper.testLocaleSwitch(
            fromLocale = Locale.ENGLISH,
            toLocale = Locale.CHINESE,
            key = "common.cancel"
        )
    }
    
    @Test
    fun testMissingTranslation() {
        UnifyI18n.setLocale(Locale.ENGLISH)
        val result = UnifyI18n.getString("nonexistent.key")
        assertEquals("nonexistent.key", result) // 应该返回key本身
    }
    
    @Test
    fun testDefaultValue() {
        UnifyI18n.setLocale(Locale.ENGLISH)
        val result = UnifyI18n.getString("nonexistent.key", "Default Value")
        assertEquals("Default Value", result)
    }
}

/**
 * 性能测试
 */
class PerformanceTest : UnifyEnhancedTestBase() {
    
    private lateinit var performanceHelper: PerformanceTestHelper
    
    override fun setupTestEnvironment() {
        performanceHelper = PerformanceTestHelper()
    }
    
    @Test
    fun testStateManagerPerformance() = runTest {
        val stateManager = EnhancedMVIStateManager(
            initialState = UserState(),
            reducer = UserReducer(),
            middleware = emptyList()
        )
        
        val metrics = performanceHelper.benchmark(iterations = 1000) {
            stateManager.sendIntent(UserIntent.LoadUsers)
        }
        
        // 断言性能指标在可接受范围内
        assertTrue(metrics.executionTime.inWholeMilliseconds < 100) // 平均执行时间小于100ms
        assertEquals(1000, metrics.operationCount)
    }
    
    @Test
    fun testNetworkServicePerformance() = runTest {
        val networkService = MockNetworkService(NetworkServiceTestHelper())
        
        val metrics = performanceHelper.benchmark(iterations = 100) {
            networkService.get<String>("/api/test")
        }
        
        assertTrue(metrics.executionTime.inWholeMilliseconds < 50) // 平均执行时间小于50ms
    }
}

/**
 * 集成测试
 */
class IntegrationTest : UnifyEnhancedTestBase() {
    
    @Test
    fun testUserManagementFlow() = runTest {
        val integrationHelper = IntegrationTestHelper()
        
        integrationHelper.runEndToEndTest { helper ->
            val networkHelper = helper.getNetworkHelper()
            val databaseHelper = helper.getDatabaseHelper()
            
            // 模拟网络响应
            val users = listOf(
                User(1, "user1", "user1@example.com"),
                User(2, "user2", "user2@example.com")
            )
            networkHelper.mockResponse("/api/users", users)
            
            // 创建ViewModel并测试完整流程
            val viewModel = UserViewModel()
            val mviHelper = EnhancedMVITestHelper<UserState, UserIntent, UserEffect>()
            
            val stateJob = mviHelper.collectStates(viewModel.mviStateManager, this)
            
            // 发送加载用户意图
            mviHelper.sendIntent(viewModel.mviStateManager, UserIntent.LoadUsers)
            
            advanceUntilIdle()
            
            // 验证状态变化
            val finalState = mviHelper.getStateHistory().last()
            assertEquals(users, finalState.users)
            assertFalse(finalState.isLoading)
            assertNull(finalState.error)
            
            // 验证网络请求
            networkHelper.assertRequestMade("/api/users", "GET")
            
            stateJob.cancel()
        }
    }
    
    @Test
    fun testErrorRecoveryFlow() = runTest {
        val integrationHelper = IntegrationTestHelper()
        
        integrationHelper.runEndToEndTest { helper ->
            val networkHelper = helper.getNetworkHelper()
            
            // 不设置模拟响应，模拟网络错误
            val viewModel = UserViewModel()
            val mviHelper = EnhancedMVITestHelper<UserState, UserIntent, UserEffect>()
            
            val stateJob = mviHelper.collectStates(viewModel.mviStateManager, this)
            
            // 发送加载用户意图
            mviHelper.sendIntent(viewModel.mviStateManager, UserIntent.LoadUsers)
            
            advanceUntilIdle()
            
            // 验证错误状态
            val finalState = mviHelper.getStateHistory().last()
            assertNotNull(finalState.error)
            assertFalse(finalState.isLoading)
            assertTrue(finalState.users.isEmpty())
            
            stateJob.cancel()
        }
    }
}

/**
 * 模拟实现用于测试
 */
private class MockNetworkService(
    private val helper: NetworkServiceTestHelper
) : UnifyNetworkService {
    
    override suspend fun <T> get(url: String, headers: Map<String, String>): Result<T> {
        helper.recordRequest(url, "GET", headers)
        val mockResponse = helper.getMockResponse(url)
        return if (mockResponse != null) {
            @Suppress("UNCHECKED_CAST")
            Result.success(mockResponse as T)
        } else {
            Result.failure(Exception("Network error"))
        }
    }
    
    override suspend fun <T, R> post(url: String, body: T, headers: Map<String, String>): Result<R> {
        helper.recordRequest(url, "POST", headers, body.toString())
        val mockResponse = helper.getMockResponse(url)
        return if (mockResponse != null) {
            @Suppress("UNCHECKED_CAST")
            Result.success(mockResponse as R)
        } else {
            Result.failure(Exception("Network error"))
        }
    }
    
    override suspend fun <T, R> put(url: String, body: T, headers: Map<String, String>): Result<R> {
        helper.recordRequest(url, "PUT", headers, body.toString())
        val mockResponse = helper.getMockResponse(url)
        return if (mockResponse != null) {
            @Suppress("UNCHECKED_CAST")
            Result.success(mockResponse as R)
        } else {
            Result.failure(Exception("Network error"))
        }
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): Result<Unit> {
        helper.recordRequest(url, "DELETE", headers)
        val mockResponse = helper.getMockResponse(url)
        return if (mockResponse != null) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Network error"))
        }
    }
    
    override suspend fun <T> uploadFile(url: String, file: ByteArray, fileName: String, headers: Map<String, String>): Result<T> {
        helper.recordRequest(url, "POST", headers, "File upload: $fileName")
        val mockResponse = helper.getMockResponse(url)
        return if (mockResponse != null) {
            @Suppress("UNCHECKED_CAST")
            Result.success(mockResponse as T)
        } else {
            Result.failure(Exception("Upload error"))
        }
    }
    
    override suspend fun downloadFile(url: String, headers: Map<String, String>): Result<ByteArray> {
        helper.recordRequest(url, "GET", headers)
        return Result.success(byteArrayOf(1, 2, 3, 4, 5)) // 模拟文件内容
    }
}

private class MockUnifyStorage : UnifyStorage {
    private val storage = mutableMapOf<String, Any>()
    
    override suspend fun <T> store(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        storage[key] = value as Any
    }
    
    override suspend fun <T> retrieve(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key] as? T
    }
    
    override suspend fun remove(key: String) {
        storage.remove(key)
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    override suspend fun clear() {
        storage.clear()
    }
    
    override suspend fun getAllKeys(): List<String> {
        return storage.keys.toList()
    }
}
