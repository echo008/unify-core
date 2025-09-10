package com.unify.core

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Unify-Core核心功能测试
 */
class UnifyCoreTest {

    @Test
    fun testUnifyCoreInitialization() = runTest {
        val unifyCore = UnifyCore()
        assertNotNull(unifyCore)
        assertTrue(unifyCore.isInitialized())
    }

    @Test
    fun testUnifyCoreVersion() {
        val unifyCore = UnifyCore()
        val version = unifyCore.getVersion()
        assertNotNull(version)
        assertTrue(version.isNotEmpty())
    }

    @Test
    fun testUnifyCorePlatformInfo() {
        val unifyCore = UnifyCore()
        val platformInfo = unifyCore.getPlatformInfo()
        assertNotNull(platformInfo)
        assertTrue(platformInfo.isNotEmpty())
    }
}
