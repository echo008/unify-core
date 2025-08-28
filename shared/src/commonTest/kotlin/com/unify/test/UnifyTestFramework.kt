package com.unify.test

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*
import com.unify.ui.state.*
import com.unify.network.*
import com.unify.storage.*
import com.unify.platform.*

/**
 * Unify KMP 测试框架
 * 基于文档要求的测试体系实现
 */

/**
 * 测试基类
 */
abstract class UnifyTestBase {
    protected lateinit var testScope: TestScope
    protected lateinit var testDispatcher: TestDispatcher
    
    @BeforeTest
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)
    }
    
    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }
    
    protected fun runTest(block: suspend TestScope.() -> Unit) {
        testScope.runTest(block = block)
    }
}

/**
 * MVI状态管理测试工具
 */
class MVITestHelper<S : State, I : Intent, E : Effect> {
    private val stateHistory = mutableListOf<S>()
    private val effectHistory = mutableListOf<E>()
    
    suspend fun collectStates(
        stateManager: EnhancedMVIStateManager<S, I, E>,
        testScope: TestScope
    ) {
        val job = testScope.launch {
            stateManager.stateFlow.collect { state ->
                stateHistory.add(state)
            }
        }
        
        testScope.launch {
            stateManager.effectFlow.collect { effect ->
                effectHistory.add(effect)
            }
        }
        
        testScope.testScheduler.advanceUntilIdle()
    }
    
    fun getStateHistory(): List<S> = stateHistory.toList()
    fun getEffectHistory(): List<E> = effectHistory.toList()
    fun getLatestState(): S? = stateHistory.lastOrNull()
    fun getLatestEffect(): E? = effectHistory.lastOrNull()
    
    fun assertStateCount(expected: Int) {
        assertEquals(expected, stateHistory.size, "Expected $expected states, but got ${stateHistory.size}")
    }
    
    fun assertEffectCount(expected: Int) {
        assertEquals(expected, effectHistory.size, "Expected $expected effects, but got ${effectHistory.size}")
    }
    
    fun assertLatestState(expected: S) {
        assertEquals(expected, getLatestState(), "Latest state doesn't match expected")
    }
    
    fun assertLatestEffect(expected: E) {
        assertEquals(expected, getLatestEffect(), "Latest effect doesn't match expected")
    }
    
    fun clear() {
        stateHistory.clear()
        effectHistory.clear()
    }
}

/**
 * 网络服务测试模拟器
 */
class MockNetworkService : UnifyNetworkService {
    private val responses = mutableMapOf<String, Any>()
    private val errors = mutableMapOf<String, Exception>()
    private val delays = mutableMapOf<String, Long>()
    
    fun mockResponse(url: String, response: Any) {
        responses[url] = response
    }
    
    fun mockError(url: String, error: Exception) {
        errors[url] = error
    }
    
    fun mockDelay(url: String, delayMs: Long) {
        delays[url] = delayMs
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest(url)
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest(url)
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest(url)
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest(url)
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>
    ): NetworkResult<String> {
        return executeRequest(url)
    }
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>
    ): NetworkResult<String> {
        return executeRequest(url)
    }
    
    override fun <T> streamRequest(
        url: String,
        headers: Map<String, String>
    ): kotlinx.coroutines.flow.Flow<NetworkResult<T>> {
        return kotlinx.coroutines.flow.flow {
            emit(executeRequest(url))
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> executeRequest(url: String): NetworkResult<T> {
        delays[url]?.let { delay ->
            kotlinx.coroutines.delay(delay)
        }
        
        errors[url]?.let { error ->
            return NetworkResult.Error(error)
        }
        
        responses[url]?.let { response ->
            return NetworkResult.Success(response as T)
        }
        
        return NetworkResult.Error(Exception("No mock response configured for $url"))
    }
    
    fun clear() {
        responses.clear()
        errors.clear()
        delays.clear()
    }
}

/**
 * 存储服务测试模拟器
 */
class MockStorage : UnifyStorage {
    private val data = mutableMapOf<String, Any>()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return data[key] as? String ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        data[key] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return data[key] as? Int ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        data[key] = value
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return data[key] as? Long ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        data[key] = value
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return data[key] as? Float ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        data[key] = value
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return data[key] as? Boolean ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        data[key] = value
    }
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T? {
        return data[key] as? T
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        data[key] = value as Any
    }
    
    override suspend fun remove(key: String) {
        data.remove(key)
    }
    
    override suspend fun clear() {
        data.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return data.containsKey(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return data.keys.toSet()
    }
    
    override fun <T> observeKey(key: String, clazz: Class<T>): kotlinx.coroutines.flow.Flow<T?> {
        return kotlinx.coroutines.flow.flowOf(data[key] as? T)
    }
    
    fun getData(): Map<String, Any> = data.toMap()
    
    fun setData(newData: Map<String, Any>) {
        data.clear()
        data.putAll(newData)
    }
}

/**
 * 平台信息测试模拟器
 */
class MockPlatformInfo(
    override val platformType: PlatformType = PlatformType.ANDROID,
    override val platformVersion: String = "1.0.0",
    override val deviceModel: String = "Test Device",
    override val isDebug: Boolean = true
) : PlatformInfo {
    
    private var screenSize = Pair(1080, 1920)
    private var deviceId = "test-device-id"
    private var isMobileDevice = true
    private var isTabletDevice = false
    
    override fun getScreenSize(): Pair<Int, Int> = screenSize
    override fun getDeviceId(): String = deviceId
    override fun isMobile(): Boolean = isMobileDevice
    override fun isTablet(): Boolean = isTabletDevice
    
    fun setScreenSize(width: Int, height: Int) {
        screenSize = Pair(width, height)
    }
    
    fun setDeviceId(id: String) {
        deviceId = id
    }
    
    fun setMobile(mobile: Boolean) {
        isMobileDevice = mobile
    }
    
    fun setTablet(tablet: Boolean) {
        isTabletDevice = tablet
    }
}

/**
 * 平台能力测试模拟器
 */
class MockPlatformCapabilities(
    override val supportsFileSystem: Boolean = true,
    override val supportsCamera: Boolean = true,
    override val supportsLocation: Boolean = true,
    override val supportsBiometric: Boolean = true,
    override val supportsNotification: Boolean = true,
    override val supportsVibration: Boolean = true,
    override val supportsClipboard: Boolean = true,
    override val supportsShare: Boolean = true,
    override val supportsDeepLink: Boolean = true,
    override val supportsBackgroundTask: Boolean = true
) : PlatformCapabilities

/**
 * 测试断言工具
 */
object UnifyAssertions {
    
    fun assertNetworkSuccess(result: NetworkResult<*>) {
        assertTrue(result is NetworkResult.Success, "Expected NetworkResult.Success but got ${result::class.simpleName}")
    }
    
    fun assertNetworkError(result: NetworkResult<*>) {
        assertTrue(result is NetworkResult.Error, "Expected NetworkResult.Error but got ${result::class.simpleName}")
    }
    
    fun <T> assertNetworkResult(result: NetworkResult<T>, expected: T) {
        when (result) {
            is NetworkResult.Success -> assertEquals(expected, result.data)
            is NetworkResult.Error -> fail("Expected success but got error: ${result.exception.message}")
            is NetworkResult.Loading -> fail("Expected success but got loading")
        }
    }
    
    fun assertStorageContains(storage: UnifyStorage, key: String) = runBlocking {
        assertTrue(storage.contains(key), "Storage should contain key: $key")
    }
    
    fun assertStorageNotContains(storage: UnifyStorage, key: String) = runBlocking {
        assertFalse(storage.contains(key), "Storage should not contain key: $key")
    }
    
    fun assertStorageValue(storage: UnifyStorage, key: String, expected: String) = runBlocking {
        assertEquals(expected, storage.getString(key), "Storage value for key $key doesn't match expected")
    }
}

/**
 * 性能测试工具
 */
class PerformanceTestHelper {
    
    suspend fun measureTime(operation: suspend () -> Unit): Long {
        val startTime = kotlinx.coroutines.test.currentTime
        operation()
        return kotlinx.coroutines.test.currentTime - startTime
    }
    
    suspend fun assertExecutionTime(
        maxTimeMs: Long,
        operation: suspend () -> Unit
    ) {
        val executionTime = measureTime(operation)
        assertTrue(
            executionTime <= maxTimeMs,
            "Operation took ${executionTime}ms, expected <= ${maxTimeMs}ms"
        )
    }
    
    suspend fun measureMemoryUsage(operation: suspend () -> Unit): Long {
        // 简化的内存测量，实际实现需要平台特定代码
        val beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        operation()
        val afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        return afterMemory - beforeMemory
    }
}

/**
 * UI测试工具
 */
class UITestHelper {
    
    fun createTestComponent(
        componentId: String = "test-component",
        componentType: ComponentType = ComponentType.BUTTON
    ): TestUnifyComponent {
        return TestUnifyComponent(componentId, componentType)
    }
    
    class TestUnifyComponent(
        override val componentId: String,
        override val componentType: ComponentType
    ) : UnifyComponent() {
        
        var renderCount = 0
        var lastState: Any? = null
        var lastEvent: ComponentEvent? = null
        
        @Composable
        override fun renderContentInternal(): @Composable () -> Unit {
            renderCount++
            return { /* Test content */ }
        }
        
        override fun updateState(newState: Any) {
            lastState = newState
        }
        
        override fun handleEvent(event: ComponentEvent) {
            lastEvent = event
        }
        
        override fun renderToArkUI(context: ArkUIContext): ArkUIComponent {
            return object : ArkUIComponent {
                override fun mount() {}
                override fun unmount() {}
                override fun update(props: Map<String, Any>) {}
                override fun getPerformanceMetrics(): ArkUIPerformanceMetrics {
                    return ArkUIPerformanceMetrics(0, 0, 60f, 1.0f)
                }
            }
        }
        
        override fun getPlatformConfig(): PlatformComponentConfig? = null
    }
}

/**
 * 集成测试基类
 */
abstract class UnifyIntegrationTestBase : UnifyTestBase() {
    protected lateinit var mockNetworkService: MockNetworkService
    protected lateinit var mockStorage: MockStorage
    protected lateinit var mockPlatformInfo: MockPlatformInfo
    protected lateinit var mockPlatformCapabilities: MockPlatformCapabilities
    
    @BeforeTest
    override fun setUp() {
        super.setUp()
        mockNetworkService = MockNetworkService()
        mockStorage = MockStorage()
        mockPlatformInfo = MockPlatformInfo()
        mockPlatformCapabilities = MockPlatformCapabilities()
    }
    
    @AfterTest
    override fun tearDown() {
        mockNetworkService.clear()
        mockStorage.clear()
        super.tearDown()
    }
}
