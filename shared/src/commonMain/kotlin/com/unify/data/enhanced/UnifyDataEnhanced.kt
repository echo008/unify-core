package com.unify.data.enhanced

import kotlinx.coroutines.flow.Flow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.flow.MutableStateFlow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.flow.StateFlow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.serialization.Serializable
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.serialization.json.Json
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

/**
 * Unify增强数据管理器
 * 提供高级数据处理功能，包括索引、搜索、分析和优化
 */
class UnifyDataEnhanced {
    private val _dataMetrics = MutableStateFlow(DataMetrics())
    val dataMetrics: StateFlow<DataMetrics> = _dataMetrics
    
    private val searchIndex = mutableMapOf<String, SearchIndexEntry>()
    private val userDataCache = mutableMapOf<String, UserDataEntry>()
    private val contentAnalyzer = ContentAnalyzer()
    private val dataOptimizer = DataOptimizer()
    
    // 性能常量
    companion object {
        private const val MAX_CACHE_SIZE = 1000
        private const val SEARCH_SCORE_THRESHOLD = 0.3
        private const val CONTENT_ANALYSIS_MIN_LENGTH = 10
        private const val INDEX_UPDATE_BATCH_SIZE = 100
        private const val CACHE_EXPIRY_TIME = 3600000L // 1小时
        private const val MAX_SEARCH_RESULTS = 50
        private const val RELEVANCE_BOOST_FACTOR = 1.5
        private const val TITLE_WEIGHT = 2.0
        private const val CONTENT_WEIGHT = 1.0
        private const val TAG_WEIGHT = 1.5
    }
    
    /**
     * 处理用户数据
     */
    suspend fun processUserData(userId: String, userData: String): DataProcessingResult {
        return try {
            val entry = UserDataEntry(
                userId = userId,
                data = userData,
                processedAt = getCurrentTimeMillis(),
                dataSize = userData.length,
                checksum = calculateChecksum(userData)
            )
            
            userDataCache[userId] = entry
            
            // 分析用户数据
            val analysis = contentAnalyzer.analyzeUserData(userData)
            
            // 更新指标
            updateDataMetrics(DataOperation.USER_PROCESSED, userData.length)
            
            DataProcessingResult.Success(analysis)
            
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            DataProcessingResult.Error("处理用户数据失败: ${e.message}")
        }
    }
    
    /**
     * 索引笔记内容
     */
    suspend fun indexNoteContent(
        noteId: String,
        title: String,
        content: String,
        tags: List<String>
    ): IndexingResult {
        return try {
            val keywords = contentAnalyzer.extractKeywords(content)
            val sentiment = contentAnalyzer.analyzeSentiment(content)
            val categories = contentAnalyzer.categorizeContent(content, tags)
            
            val indexEntry = SearchIndexEntry(
                id = noteId,
                title = title,
                content = content,
                tags = tags,
                keywords = keywords,
                sentiment = sentiment,
                categories = categories,
                indexedAt = getCurrentTimeMillis(),
                searchScore = calculateSearchScore(title, content, tags, keywords)
            )
            
            searchIndex[noteId] = indexEntry
            
            // 更新指标
            updateDataMetrics(DataOperation.CONTENT_INDEXED, content.length)
            
            IndexingResult.Success(indexEntry.searchScore)
            
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            IndexingResult.Error("索引内容失败: ${e.message}")
        }
    }
    
    /**
     * 搜索笔记
     */
    suspend fun searchNotes(query: String): List<String> {
        return try {
            if (query.isBlank()) return emptyList()
            
            val queryTerms = query.lowercase().split("\\s+".toRegex())
            val results = mutableListOf<SearchResult>()
            
            searchIndex.values.forEach { entry ->
                val score = calculateRelevanceScore(entry, queryTerms)
                if (score > SEARCH_SCORE_THRESHOLD) {
                    results.add(SearchResult(entry.id, score))
                }
            }
            
            // 按相关性排序并限制结果数量
            results.sortedByDescending { it.score }
                .take(MAX_SEARCH_RESULTS)
                .map { it.id }
            
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            emptyList()
        }
    }
    
    /**
     * 高级搜索
     */
    suspend fun advancedSearch(searchParams: AdvancedSearchParams): List<SearchResultDetail> {
        return try {
            val results = mutableListOf<SearchResultDetail>()
            
            searchIndex.values.forEach { entry ->
                if (matchesAdvancedCriteria(entry, searchParams)) {
                    val score = calculateAdvancedScore(entry, searchParams)
                    results.add(
                        SearchResultDetail(
                            id = entry.id,
                            title = entry.title,
                            snippet = generateSnippet(entry.content, searchParams.query),
                            score = score,
                            matchedFields = getMatchedFields(entry, searchParams),
                            categories = entry.categories,
                            sentiment = entry.sentiment
                        )
                    )
                }
            }
            
            results.sortedByDescending { it.score }
                .take(searchParams.maxResults ?: MAX_SEARCH_RESULTS)
            
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            emptyList()
        }
    }
    
    /**
     * 分析数据趋势
     */
    suspend fun analyzeDataTrends(timeRange: TimeRange): DataTrendAnalysis {
        return try {
            val startTime = getCurrentTimeMillis() - timeRange.milliseconds
            val relevantEntries = searchIndex.values.filter { it.indexedAt >= startTime }
            
            val categoryDistribution = relevantEntries
                .flatMap { it.categories }
                .groupingBy { it }
                .eachCount()
            
            val sentimentDistribution = relevantEntries
                .groupingBy { it.sentiment }
                .eachCount()
            
            val keywordFrequency = relevantEntries
                .flatMap { it.keywords }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(20)
            
            val contentLengthStats = calculateContentLengthStats(relevantEntries)
            
            DataTrendAnalysis(
                timeRange = timeRange,
                totalItems = relevantEntries.size,
                categoryDistribution = categoryDistribution,
                sentimentDistribution = sentimentDistribution,
                topKeywords = keywordFrequency,
                contentLengthStats = contentLengthStats,
                analysisTime = getCurrentTimeMillis()
            )
            
        } catch (e: Exception) {
            DataTrendAnalysis(
                timeRange = timeRange,
                totalItems = 0,
                categoryDistribution = emptyMap(),
                sentimentDistribution = emptyMap(),
                topKeywords = emptyList(),
                contentLengthStats = ContentLengthStats(),
                analysisTime = getCurrentTimeMillis()
            )
        }
    }
    
    /**
     * 优化数据存储
     */
    suspend fun optimizeDataStorage(): DataOptimizationResult {
        return try {
            val optimizationStats = dataOptimizer.optimize(searchIndex, userDataCache)
            
            // 清理过期缓存
            cleanupExpiredCache()
            
            // 压缩索引
            compressSearchIndex()
            
            // 更新指标
            updateDataMetrics(DataOperation.OPTIMIZATION_COMPLETED, 0)
            
            DataOptimizationResult.Success(optimizationStats)
            
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            DataOptimizationResult.Error("数据优化失败: ${e.message}")
        }
    }
    
    /**
     * 获取数据统计信息
     */
    fun getDataStatistics(): DataStatistics {
        val currentTime = getCurrentTimeMillis()
        val indexSize = searchIndex.size
        val cacheSize = userDataCache.size
        val totalIndexedContent = searchIndex.values.sumOf { it.content.length }
        val totalCachedData = userDataCache.values.sumOf { it.dataSize }
        
        return DataStatistics(
            indexedItems = indexSize,
            cachedUsers = cacheSize,
            totalIndexedContentSize = totalIndexedContent,
            totalCachedDataSize = totalCachedData,
            averageContentLength = if (indexSize > 0) totalIndexedContent / indexSize else 0,
            indexMemoryUsage = estimateIndexMemoryUsage(),
            cacheMemoryUsage = estimateCacheMemoryUsage(),
            lastOptimizationTime = _dataMetrics.value.lastOptimizationTime,
            statisticsGeneratedAt = currentTime
        )
    }
    
    /**
     * 移除笔记索引
     */
    suspend fun removeNoteIndex(noteId: String): Boolean {
        return try {
            val removed = searchIndex.remove(noteId) != null
            if (removed) {
                updateDataMetrics(DataOperation.CONTENT_REMOVED, 0)
            }
            removed
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            false
        }
    }
    
    /**
     * 清空所有索引
     */
    suspend fun clearAllIndexes() {
        try {
            searchIndex.clear()
            userDataCache.clear()
            updateDataMetrics(DataOperation.ALL_CLEARED, 0)
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
        }
    }
    
    /**
     * 导出索引数据
     */
    suspend fun exportIndexData(): String {
        return try {
            val exportData = IndexExportData(
                searchIndex = searchIndex.values.toList(),
                userDataSummary = userDataCache.values.map { 
                    UserDataSummary(it.userId, it.dataSize, it.processedAt, it.checksum)
                },
                exportTime = getCurrentTimeMillis(),
                version = "1.0"
            )
            
            "mock_enhanced_export_data_${getCurrentTimeMillis()}"
        } catch (e: Exception) {
            updateDataMetrics(DataOperation.ERROR, 0)
            ""
        }
    }
    
    // 私有辅助方法
    
    private fun calculateChecksum(data: String): String {
        return data.hashCode().toString(16)
    }
    
    private fun calculateSearchScore(
        title: String,
        content: String,
        tags: List<String>,
        keywords: List<String>
    ): Double {
        val titleScore = title.length * TITLE_WEIGHT
        val contentScore = content.length * CONTENT_WEIGHT
        val tagScore = tags.size * TAG_WEIGHT
        val keywordScore = keywords.size * RELEVANCE_BOOST_FACTOR
        
        return (titleScore + contentScore + tagScore + keywordScore) / 100.0
    }
    
    private fun calculateRelevanceScore(entry: SearchIndexEntry, queryTerms: List<String>): Double {
        var score = 0.0
        
        queryTerms.forEach { term ->
            // 标题匹配
            if (entry.title.lowercase().contains(term)) {
                score += TITLE_WEIGHT
            }
            
            // 内容匹配
            if (entry.content.lowercase().contains(term)) {
                score += CONTENT_WEIGHT
            }
            
            // 标签匹配
            if (entry.tags.any { it.lowercase().contains(term) }) {
                score += TAG_WEIGHT
            }
            
            // 关键词匹配
            if (entry.keywords.any { it.lowercase().contains(term) }) {
                score += RELEVANCE_BOOST_FACTOR
            }
        }
        
        return score / queryTerms.size
    }
    
    private fun matchesAdvancedCriteria(entry: SearchIndexEntry, params: AdvancedSearchParams): Boolean {
        // 查询匹配
        if (params.query.isNotBlank()) {
            val queryMatch = entry.title.contains(params.query, ignoreCase = true) ||
                    entry.content.contains(params.query, ignoreCase = true) ||
                    entry.tags.any { it.contains(params.query, ignoreCase = true) }
            if (!queryMatch) return false
        }
        
        // 类别过滤
        if (params.categories.isNotEmpty()) {
            if (entry.categories.none { it in params.categories }) return false
        }
        
        // 情感过滤
        if (params.sentiment != null) {
            if (entry.sentiment != params.sentiment) return false
        }
        
        // 时间范围过滤
        if (params.timeRange != null) {
            val startTime = getCurrentTimeMillis() - params.timeRange.milliseconds
            if (entry.indexedAt < startTime) return false
        }
        
        return true
    }
    
    private fun calculateAdvancedScore(entry: SearchIndexEntry, params: AdvancedSearchParams): Double {
        var score = entry.searchScore
        
        // 查询相关性加分
        if (params.query.isNotBlank()) {
            val queryTerms = params.query.lowercase().split("\\s+".toRegex())
            score += calculateRelevanceScore(entry, queryTerms)
        }
        
        // 类别匹配加分
        if (params.categories.isNotEmpty()) {
            val matchedCategories = entry.categories.count { it in params.categories }
            score += matchedCategories * 0.5
        }
        
        // 时间新鲜度加分
        val timeDiff = getCurrentTimeMillis() - entry.indexedAt
        val freshnessScore = maxOf(0.0, 1.0 - (timeDiff / (7 * 24 * 3600000.0))) // 7天内的内容有新鲜度加分
        score += freshnessScore * 0.3
        
        return score
    }
    
    private fun generateSnippet(content: String, query: String): String {
        if (query.isBlank()) return content.take(150) + if (content.length > 150) "..." else ""
        
        val queryIndex = content.lowercase().indexOf(query.lowercase())
        if (queryIndex == -1) return content.take(150) + if (content.length > 150) "..." else ""
        
        val start = maxOf(0, queryIndex - 50)
        val end = minOf(content.length, queryIndex + query.length + 50)
        
        val snippet = content.substring(start, end)
        return (if (start > 0) "..." else "") + snippet + (if (end < content.length) "..." else "")
    }
    
    private fun getMatchedFields(entry: SearchIndexEntry, params: AdvancedSearchParams): List<String> {
        val matchedFields = mutableListOf<String>()
        
        if (params.query.isNotBlank()) {
            if (entry.title.contains(params.query, ignoreCase = true)) matchedFields.add("title")
            if (entry.content.contains(params.query, ignoreCase = true)) matchedFields.add("content")
            if (entry.tags.any { it.contains(params.query, ignoreCase = true) }) matchedFields.add("tags")
        }
        
        return matchedFields
    }
    
    private fun calculateContentLengthStats(entries: List<SearchIndexEntry>): ContentLengthStats {
        if (entries.isEmpty()) return ContentLengthStats()
        
        val lengths = entries.map { it.content.length }
        return ContentLengthStats(
            min = lengths.minOrNull() ?: 0,
            max = lengths.maxOrNull() ?: 0,
            average = lengths.average(),
            median = lengths.sorted().let { sorted ->
                if (sorted.size % 2 == 0) {
                    (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
                } else {
                    sorted[sorted.size / 2].toDouble()
                }
            }
        )
    }
    
    private fun cleanupExpiredCache() {
        val currentTime = getCurrentTimeMillis()
        val expiredKeys = userDataCache.filter { (_, entry) ->
            currentTime - entry.processedAt > CACHE_EXPIRY_TIME
        }.keys
        
        expiredKeys.forEach { userDataCache.remove(it) }
    }
    
    private fun compressSearchIndex() {
        // 移除低质量索引项
        val lowQualityKeys = searchIndex.filter { (_, entry) ->
            entry.searchScore < SEARCH_SCORE_THRESHOLD && 
            entry.content.length < CONTENT_ANALYSIS_MIN_LENGTH
        }.keys
        
        lowQualityKeys.forEach { searchIndex.remove(it) }
    }
    
    private fun estimateIndexMemoryUsage(): Long {
        return searchIndex.values.sumOf { entry ->
            entry.title.length + entry.content.length + 
            entry.tags.sumOf { it.length } + 
            entry.keywords.sumOf { it.length } + 
            entry.categories.sumOf { it.length } + 100 // 估算对象开销
        }.toLong()
    }
    
    private fun estimateCacheMemoryUsage(): Long {
        return userDataCache.values.sumOf { it.dataSize + 50 }.toLong() // 估算对象开销
    }
    
    private fun updateDataMetrics(operation: DataOperation, dataSize: Int) {
        _dataMetrics.value = _dataMetrics.value.copy(
            totalOperations = _dataMetrics.value.totalOperations + 1,
            totalDataProcessed = _dataMetrics.value.totalDataProcessed + dataSize,
            lastOperationTime = getCurrentTimeMillis(),
            lastOperation = operation
        )
    }
}

// 数据类定义

@Serializable
data class DataMetrics(
    val totalOperations: Long = 0,
    val totalDataProcessed: Long = 0,
    val lastOperationTime: Long = 0,
    val lastOptimizationTime: Long = 0,
    val lastOperation: DataOperation = DataOperation.NONE
)

enum class DataOperation {
    NONE,
    USER_PROCESSED,
    CONTENT_INDEXED,
    CONTENT_REMOVED,
    OPTIMIZATION_COMPLETED,
    ALL_CLEARED,
    ERROR
}

@Serializable
data class UserDataEntry(
    val userId: String,
    val data: String,
    val processedAt: Long,
    val dataSize: Int,
    val checksum: String
)

@Serializable
data class SearchIndexEntry(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    val keywords: List<String>,
    val sentiment: ContentSentiment,
    val categories: List<String>,
    val indexedAt: Long,
    val searchScore: Double
)

@Serializable
data class SearchResult(
    val id: String,
    val score: Double
)

@Serializable
data class SearchResultDetail(
    val id: String,
    val title: String,
    val snippet: String,
    val score: Double,
    val matchedFields: List<String>,
    val categories: List<String>,
    val sentiment: ContentSentiment
)

@Serializable
data class AdvancedSearchParams(
    val query: String = "",
    val categories: List<String> = emptyList(),
    val sentiment: ContentSentiment? = null,
    val timeRange: TimeRange? = null,
    val maxResults: Int? = null
)

enum class TimeRange(val milliseconds: Long) {
    LAST_HOUR(3600000),
    LAST_DAY(86400000),
    LAST_WEEK(604800000),
    LAST_MONTH(2592000000),
    LAST_YEAR(31536000000)
}

enum class ContentSentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    MIXED
}

@Serializable
data class DataTrendAnalysis(
    val timeRange: TimeRange,
    val totalItems: Int,
    val categoryDistribution: Map<String, Int>,
    val sentimentDistribution: Map<ContentSentiment, Int>,
    val topKeywords: List<Pair<String, Int>>,
    val contentLengthStats: ContentLengthStats,
    val analysisTime: Long
)

@Serializable
data class ContentLengthStats(
    val min: Int = 0,
    val max: Int = 0,
    val average: Double = 0.0,
    val median: Double = 0.0
)

@Serializable
data class DataStatistics(
    val indexedItems: Int,
    val cachedUsers: Int,
    val totalIndexedContentSize: Int,
    val totalCachedDataSize: Int,
    val averageContentLength: Int,
    val indexMemoryUsage: Long,
    val cacheMemoryUsage: Long,
    val lastOptimizationTime: Long,
    val statisticsGeneratedAt: Long
)

@Serializable
data class IndexExportData(
    val searchIndex: List<SearchIndexEntry>,
    val userDataSummary: List<UserDataSummary>,
    val exportTime: Long,
    val version: String
)

@Serializable
data class UserDataSummary(
    val userId: String,
    val dataSize: Int,
    val processedAt: Long,
    val checksum: String
)

sealed class DataProcessingResult {
    data class Success(val analysis: ContentAnalysisResult) : DataProcessingResult()
    data class Error(val message: String) : DataProcessingResult()
}

sealed class IndexingResult {
    data class Success(val searchScore: Double) : IndexingResult()
    data class Error(val message: String) : IndexingResult()
}

sealed class DataOptimizationResult {
    data class Success(val stats: OptimizationStats) : DataOptimizationResult()
    data class Error(val message: String) : DataOptimizationResult()
}

@Serializable
data class ContentAnalysisResult(
    val wordCount: Int,
    val sentiment: ContentSentiment,
    val keywords: List<String>,
    val categories: List<String>,
    val readabilityScore: Double
)

@Serializable
data class OptimizationStats(
    val itemsRemoved: Int,
    val spaceSaved: Long,
    val optimizationTime: Long,
    val compressionRatio: Double
)

// 内容分析器
class ContentAnalyzer {
    fun analyzeUserData(userData: String): ContentAnalysisResult {
        return ContentAnalysisResult(
            wordCount = userData.split("\\s+".toRegex()).size,
            sentiment = ContentSentiment.NEUTRAL,
            keywords = extractKeywords(userData),
            categories = listOf("user_data"),
            readabilityScore = 0.5
        )
    }
    
    fun extractKeywords(content: String): List<String> {
        return content.lowercase()
            .split("\\W+".toRegex())
            .filter { it.length > 3 }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(10)
            .map { it.first }
    }
    
    fun analyzeSentiment(content: String): ContentSentiment {
        val positiveWords = listOf("好", "棒", "优秀", "喜欢", "满意", "成功")
        val negativeWords = listOf("坏", "差", "失败", "讨厌", "不满", "错误")
        
        val words = content.lowercase().split("\\W+".toRegex())
        val positiveCount = words.count { it in positiveWords }
        val negativeCount = words.count { it in negativeWords }
        
        return when {
            positiveCount > negativeCount -> ContentSentiment.POSITIVE
            negativeCount > positiveCount -> ContentSentiment.NEGATIVE
            positiveCount > 0 && negativeCount > 0 -> ContentSentiment.MIXED
            else -> ContentSentiment.NEUTRAL
        }
    }
    
    fun categorizeContent(content: String, tags: List<String>): List<String> {
        val categories = mutableListOf<String>()
        
        // 基于标签的分类
        categories.addAll(tags)
        
        // 基于内容的自动分类
        val contentLower = content.lowercase()
        when {
            contentLower.contains("技术") || contentLower.contains("代码") -> categories.add("技术")
            contentLower.contains("工作") || contentLower.contains("项目") -> categories.add("工作")
            contentLower.contains("学习") || contentLower.contains("教程") -> categories.add("学习")
            contentLower.contains("生活") || contentLower.contains("日常") -> categories.add("生活")
            else -> categories.add("其他")
        }
        
        return categories.distinct()
    }
}

// 数据优化器
class DataOptimizer {
    fun optimize(
        searchIndex: MutableMap<String, SearchIndexEntry>,
        userDataCache: MutableMap<String, UserDataEntry>
    ): OptimizationStats {
        val startTime = getCurrentTimeMillis()
        var itemsRemoved = 0
        var spaceSaved = 0L
        
        // 移除重复内容
        val contentHashes = mutableMapOf<String, String>()
        val duplicateKeys = mutableListOf<String>()
        
        searchIndex.forEach { (key, entry) ->
            val contentHash = entry.content.hashCode().toString()
            if (contentHashes.containsKey(contentHash)) {
                duplicateKeys.add(key)
                spaceSaved += entry.content.length
            } else {
                contentHashes[contentHash] = key
            }
        }
        
        duplicateKeys.forEach { 
            searchIndex.remove(it)
            itemsRemoved++
        }
        
        val optimizationTime = getCurrentTimeMillis() - startTime
        val compressionRatio = if (spaceSaved > 0) spaceSaved.toDouble() / (spaceSaved + searchIndex.values.sumOf { it.content.length }) else 0.0
        
        return OptimizationStats(
            itemsRemoved = itemsRemoved,
            spaceSaved = spaceSaved,
            optimizationTime = optimizationTime,
            compressionRatio = compressionRatio
        )
    }
}
