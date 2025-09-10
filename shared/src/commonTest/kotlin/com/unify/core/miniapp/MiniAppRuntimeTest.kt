package com.unify.core.miniapp

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

/**
 * 小程序运行时跨平台测试
 */
class MiniAppRuntimeTest {

    @Test
    fun testMiniAppRuntimeCreation() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        assertNotNull(runtime)
    }

    @Test
    fun testMiniAppRuntimeInitialization() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val config = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "test_app_id",
            version = "1.0.0",
            enableDebug = true
        )
        
        runtime.initialize(config)
        assertTrue(true) // 如果没有异常抛出，说明初始化成功
    }

    @Test
    fun testMiniAppLaunch() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val config = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "test_app_id",
            version = "1.0.0"
        )
        
        runtime.initialize(config)
        
        val instance = runtime.launch(
            appId = "test_app_id",
            params = mapOf("scene" to "1001")
        )
        
        assertNotNull(instance)
        assertEquals("test_app_id", instance.appId)
        assertEquals(MiniAppPlatform.WECHAT, instance.platform)
        assertEquals(MiniAppLifecycleState.LAUNCHED, instance.state)
    }

    @Test
    fun testMiniAppInstanceManagement() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val config = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "test_app_id",
            version = "1.0.0"
        )
        
        runtime.initialize(config)
        
        // 启动小程序
        val instance = runtime.launch("test_app_id")
        assertNotNull(runtime.getCurrentInstance())
        assertEquals(instance.instanceId, runtime.getCurrentInstance()?.instanceId)
        
        // 销毁小程序
        runtime.destroyInstance(instance.instanceId)
        assertNull(runtime.getCurrentInstance())
    }

    @Test
    fun testApiRegistrationAndCall() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val config = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "test_app_id",
            version = "1.0.0"
        )
        
        runtime.initialize(config)
        
        // 注册自定义API
        runtime.registerApi("testApi") { params ->
            MiniAppApiResult(
                success = true,
                data = mapOf("result" to "test_success")
            )
        }
        
        // 调用API
        val result = runtime.callApi("testApi", mapOf("param1" to "value1"))
        assertTrue(result.success)
        assertEquals("test_success", result.data?.get("result"))
    }

    @Test
    fun testApiCallWithNonExistentApi() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val config = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "test_app_id",
            version = "1.0.0"
        )
        
        runtime.initialize(config)
        
        // 调用不存在的API
        val result = runtime.callApi("nonExistentApi", emptyMap())
        assertTrue(!result.success)
        assertEquals(404, result.errorCode)
        assertNotNull(result.error)
    }

    @Test
    fun testLifecycleFlow() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val config = MiniAppConfig(
            platform = MiniAppPlatform.WECHAT,
            appId = "test_app_id",
            version = "1.0.0"
        )
        
        runtime.initialize(config)
        
        val lifecycleFlow = runtime.getLifecycleFlow()
        assertNotNull(lifecycleFlow)
    }

    @Test
    fun testMultiplePlatforms() = runTest {
        val platforms = listOf(
            MiniAppPlatform.WECHAT,
            MiniAppPlatform.ALIPAY,
            MiniAppPlatform.BYTEDANCE,
            MiniAppPlatform.BAIDU,
            MiniAppPlatform.KUAISHOU,
            MiniAppPlatform.XIAOMI,
            MiniAppPlatform.HUAWEI,
            MiniAppPlatform.QQ
        )
        
        platforms.forEach { platform ->
            val runtime = MiniAppRuntime.create(platform)
            assertNotNull(runtime)
            
            val config = MiniAppConfig(
                platform = platform,
                appId = "test_app_id",
                version = "1.0.0"
            )
            
            runtime.initialize(config)
            assertTrue(true) // 如果没有异常抛出，说明初始化成功
        }
    }
}

/**
 * 小程序Compose集成测试
 */
class MiniAppComposeIntegrationTest {

    @Test
    fun testComposeIntegrationCreation() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val bridge = TestMiniAppComposeBridge()
        
        val integration = MiniAppComposeIntegration(runtime, bridge)
        assertNotNull(integration)
    }

    @Test
    fun testComponentRegistration() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val bridge = TestMiniAppComposeBridge()
        val integration = MiniAppComposeIntegration(runtime, bridge)
        
        val component = MiniAppComposeComponent(
            id = "test_button",
            name = "TestButton",
            type = MiniAppComponentType.BUTTON,
            props = mapOf(
                "text" to MiniAppPropDefinition("text", MiniAppPropType.STRING, required = true)
            )
        )
        
        integration.registerComponent(component)
        
        val registeredComponents = integration.getRegisteredComponents()
        assertTrue(registeredComponents.contains(component))
    }

    @Test
    fun testComponentRendering() = runTest {
        val runtime = MiniAppRuntime.create(MiniAppPlatform.WECHAT)
        val bridge = TestMiniAppComposeBridge()
        val integration = MiniAppComposeIntegration(runtime, bridge)
        
        val component = MiniAppComposeComponent(
            id = "test_button",
            name = "TestButton",
            type = MiniAppComponentType.BUTTON,
            props = mapOf(
                "text" to MiniAppPropDefinition("text", MiniAppPropType.STRING, required = true)
            )
        )
        
        integration.registerComponent(component)
        
        val viewId = integration.renderComponent(
            componentId = "test_button",
            props = mapOf("text" to "Click Me")
        )
        
        assertNotNull(viewId)
    }

    @Test
    fun testStandardComponents() = runTest {
        val standardComponents = MiniAppUtils.createStandardComponents()
        assertTrue(standardComponents.isNotEmpty())
        
        val viewComponent = standardComponents.find { it.id == "view" }
        assertNotNull(viewComponent)
        assertEquals(MiniAppComponentType.VIEW, viewComponent.type)
        
        val buttonComponent = standardComponents.find { it.id == "button" }
        assertNotNull(buttonComponent)
        assertEquals(MiniAppComponentType.BUTTON, buttonComponent.type)
    }

    @Test
    fun testPropValidation() = runTest {
        val component = MiniAppComposeComponent(
            id = "test_input",
            name = "TestInput",
            type = MiniAppComponentType.INPUT,
            props = mapOf(
                "value" to MiniAppPropDefinition("value", MiniAppPropType.STRING, required = true),
                "maxLength" to MiniAppPropDefinition("maxLength", MiniAppPropType.NUMBER)
            )
        )
        
        // 测试有效属性
        val validProps = mapOf(
            "value" to "test value",
            "maxLength" to 100
        )
        val validationErrors = MiniAppUtils.validateProps(component, validProps)
        assertTrue(validationErrors.isEmpty())
        
        // 测试缺少必需属性
        val invalidProps = mapOf(
            "maxLength" to 100
        )
        val validationErrorsInvalid = MiniAppUtils.validateProps(component, invalidProps)
        assertTrue(validationErrorsInvalid.isNotEmpty())
    }
}

/**
 * 小程序事件管理器测试
 */
class MiniAppEventManagerTest {

    @Test
    fun testEventListenerRegistration() = runTest {
        val eventManager = MiniAppEventManager()
        var eventReceived = false
        
        val listener = MiniAppEventListener { event ->
            eventReceived = true
        }
        
        eventManager.addEventListener("test_event", listener)
        
        val event = MiniAppEvent(
            type = "test_event",
            data = mapOf("message" to "test")
        )
        
        eventManager.emitEvent(event)
        assertTrue(eventReceived)
    }

    @Test
    fun testEventListenerRemoval() = runTest {
        val eventManager = MiniAppEventManager()
        var eventReceived = false
        
        val listener = MiniAppEventListener { event ->
            eventReceived = true
        }
        
        eventManager.addEventListener("test_event", listener)
        eventManager.removeEventListener("test_event", listener)
        
        val event = MiniAppEvent(
            type = "test_event",
            data = mapOf("message" to "test")
        )
        
        eventManager.emitEvent(event)
        assertTrue(!eventReceived)
    }
}

/**
 * 小程序状态管理器测试
 */
class MiniAppStateManagerTest {

    @Test
    fun testStateUpdate() = runTest {
        val stateManager = MiniAppStateManager()
        
        stateManager.updateState("key1", "value1")
        assertEquals("value1", stateManager.getState("key1"))
    }

    @Test
    fun testStateClear() = runTest {
        val stateManager = MiniAppStateManager()
        
        stateManager.updateState("key1", "value1")
        stateManager.updateState("key2", "value2")
        
        stateManager.clearState()
        
        assertNull(stateManager.getState("key1"))
        assertNull(stateManager.getState("key2"))
    }
}

/**
 * 测试用的小程序Compose桥接器
 */
private class TestMiniAppComposeBridge : MiniAppComposeBridge() {
    override suspend fun renderCompose(
        composableId: String,
        props: Map<String, Any>
    ): MiniAppViewResult {
        return MiniAppViewResult(
            viewId = "test_view_${System.currentTimeMillis()}",
            viewType = "test_view",
            properties = props
        )
    }

    override suspend fun updateProps(
        viewId: String,
        props: Map<String, Any>
    ): Boolean {
        return true
    }

    override suspend fun destroyView(viewId: String): Boolean {
        return true
    }
}
