package com.unify.core.tests

import com.unify.core.performance.UnifyPerformanceMonitor
import com.unify.core.performance.PerformanceMetrics
import com.unify.core.performance.MemoryUsage
import com.unify.core.performance.CPUUsage
import com.unify.core.performance.NetworkLatency
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

/**
 * 性能监控系统专项测试
 */
class UnifyPerformanceTestSuite {
    
    @Test
    fun testPerformanceMetricRecording() {
        UnifyPerformanceMonitor.initialize()
        
        // 记录性能指标
        UnifyPerformanceMonitor.recordMetric("test_metric", 100.0, "ms")
        
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("test_metric"), "应该包含记录的指标")
        assertEquals(100.0, metrics["test_metric"]?.value, "指标值应该正确")
    }
    
    @Test
    fun testPerformanceMeasurement() {
        val measurement = UnifyPerformanceMonitor.startMeasurement("test_operation")
        
        // 模拟操作
        Thread.sleep(10)
        
        val duration = measurement.end()
        assertTrue(duration >= 10, "测量时间应该大于等于10ms")
    }
    
    @Test
    fun testFrameTimeRecording() {
        UnifyPerformanceMonitor.initialize()
        
        // 记录多个帧时间
        repeat(10) { frame ->
            UnifyPerformanceMonitor.recordFrameTime(16L + frame)
        }
        
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("frame_rate"), "应该包含帧率指标")
        
        val fps = metrics["frame_rate"]?.value ?: 0.0
        assertTrue(fps > 0, "帧率应该大于0")
    }
    
    @Test
    fun testMemoryUsageRecording() {
        UnifyPerformanceMonitor.initialize()
        
        val memoryBytes = 50 * 1024 * 1024L // 50MB
        UnifyPerformanceMonitor.recordMemoryUsage(memoryBytes)
        
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("memory_usage"), "应该包含内存使用指标")
        
        val memoryMB = metrics["memory_usage"]?.value ?: 0.0
        assertEquals(50.0, memoryMB, 0.1, "内存使用应该为50MB")
    }
    
    @Test
    fun testNetworkRequestRecording() {
        UnifyPerformanceMonitor.initialize()
        
        val url = "https://api.example.com/test"
        val duration = 200L
        val responseSize = 1024L
        
        UnifyPerformanceMonitor.recordNetworkRequest(url, duration, true, responseSize)
        
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("network_request_duration"), "应该包含网络请求时长指标")
        assertTrue(metrics.containsKey("network_response_size"), "应该包含响应大小指标")
    }
    
    @Test
    fun testPerformanceSummary() {
        UnifyPerformanceMonitor.initialize()
        
        // 记录一些指标
        UnifyPerformanceMonitor.recordFrameTime(16L)
        UnifyPerformanceMonitor.recordMemoryUsage(100 * 1024 * 1024L)
        UnifyPerformanceMonitor.recordNetworkRequest("test", 100L, true)
        
        val summary = UnifyPerformanceMonitor.getPerformanceSummary()
        
        assertTrue(summary.averageFrameRate > 0, "平均帧率应该大于0")
        assertTrue(summary.memoryUsage > 0, "内存使用应该大于0")
        assertTrue(summary.networkRequestCount >= 0, "网络请求计数应该大于等于0")
    }
    
    @Test
    fun testPerformanceOptimization() {
        UnifyPerformanceMonitor.initialize()
        
        // 测试启动优化
        UnifyPerformanceMonitor.optimizeStartupPerformance()
        
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("startup_optimization_applied"), "应该记录启动优化")
        
        // 测试运行时优化
        UnifyPerformanceMonitor.optimizeRuntimePerformance()
        assertTrue(metrics.containsKey("runtime_optimization_applied"), "应该记录运行时优化")
    }
    
    @Test
    fun testIntelligentPerformanceTuning() {
        UnifyPerformanceMonitor.initialize()
        
        // 模拟性能问题
        UnifyPerformanceMonitor.recordFrameTime(50L) // 低帧率
        UnifyPerformanceMonitor.recordMemoryUsage(200 * 1024 * 1024L) // 高内存使用
        
        UnifyPerformanceMonitor.intelligentPerformanceTuning()
        
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("intelligent_tuning_applied"), "应该应用智能调优")
    }
    
    @Test
    fun testPerformanceAlerting() {
        // 设置告警阈值
        UnifyPerformanceAlerting.setThreshold(
            "memory_usage", 
            AlertThreshold(80.0, AlertCondition.GREATER_THAN, AlertSeverity.WARNING)
        )
        
        // 触发告警
        val highMemoryMetric = PerformanceMetric(
            name = "memory_usage",
            value = 90.0,
            unit = "MB",
            timestamp = System.currentTimeMillis()
        )
        
        UnifyPerformanceAlerting.checkMetric(highMemoryMetric)
        
        // 验证告警被触发（通过检查相关指标）
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("performance_alert_triggered"), "应该触发性能告警")
    }
    
    @Test
    fun testPerformanceAnalyzer() {
        val testMetrics = mapOf(
            "frame_rate" to PerformanceMetric("frame_rate", 25.0, "fps", System.currentTimeMillis()),
            "memory_usage" to PerformanceMetric("memory_usage", 85.0, "MB", System.currentTimeMillis())
        )
        
        val analysis = UnifyPerformanceAnalyzer.analyzePerformanceTrends(testMetrics)
        
        assertTrue(analysis.trends.isNotEmpty(), "应该有趋势分析")
        assertTrue(analysis.recommendations.isNotEmpty(), "应该有优化建议")
        assertTrue(analysis.overallScore >= 0.0f && analysis.overallScore <= 1.0f, "总分应该在0-1之间")
    }
    
    @Test
    fun testComposeOptimizer() {
        // 测试稳定性标记
        val stableData = UnifyComposeOptimizer.StableData("test")
        assertEquals("test", stableData.value, "稳定数据应该正确")
        
        // 测试不可变集合
        val immutableList = UnifyComposeOptimizer.ImmutableList(listOf(1, 2, 3))
        assertEquals(3, immutableList.items.size, "不可变列表大小应该正确")
    }
    
    @Test
    fun testMemoryOptimizer() {
        // 测试图片内存优化
        val largeImageSize = 6 * 1024 * 1024L // 6MB
        val optimizedSize = UnifyMemoryOptimizer.optimizeImageMemory(largeImageSize)
        assertTrue(optimizedSize < largeImageSize, "优化后图片大小应该减少")
        
        // 测试列表内存优化
        val largeList = (1..2000).toList()
        val optimizedList = UnifyMemoryOptimizer.optimizeListMemory(largeList, 1000)
        assertEquals(1000, optimizedList.size, "优化后列表大小应该为1000")
    }
    
    @Test
    fun testNetworkOptimizer() {
        // 测试请求批处理
        val batcher = UnifyNetworkOptimizer.RequestBatcher<String>()
        
        repeat(5) { i ->
            batcher.addRequest("request_$i")
        }
        
        assertFalse(batcher.shouldFlush(10, 1000), "未达到批处理条件时不应该刷新")
        
        repeat(6) { i ->
            batcher.addRequest("request_${i + 5}")
        }
        
        assertTrue(batcher.shouldFlush(10, 1000), "达到批处理条件时应该刷新")
        
        val batch = batcher.flush()
        assertEquals(11, batch.size, "批处理应该包含所有请求")
    }
    
    @Test
    fun testResponseCache() {
        val cache = UnifyNetworkOptimizer.ResponseCache<String, String>(5)
        
        // 测试缓存存储和获取
        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"), "应该能获取缓存的值")
        
        // 测试缓存过期
        assertNull(cache.get("key1", 0), "过期的缓存应该返回null")
        
        // 测试缓存大小限制
        repeat(10) { i ->
            cache.put("key_$i", "value_$i")
        }
        
        // 由于缓存大小限制为5，早期的缓存应该被清除
        assertNull(cache.get("key_0"), "超出大小限制的缓存应该被清除")
    }
    
    @Test
    fun testStartupOptimizer() = runTest {
        var criticalTaskExecuted = false
        var initTaskExecuted = false
        
        UnifyStartupOptimizer.addCriticalTask {
            criticalTaskExecuted = true
        }
        
        UnifyStartupOptimizer.addInitTask {
            initTaskExecuted = true
        }
        
        UnifyStartupOptimizer.executeStartupTasks()
        
        assertTrue(criticalTaskExecuted, "关键任务应该被执行")
        assertTrue(initTaskExecuted, "初始化任务应该被执行")
        
        // 验证性能指标被记录
        val metrics = UnifyPerformanceMonitor.metrics.value
        assertTrue(metrics.containsKey("startup_total_time"), "应该记录总启动时间")
    }
}
