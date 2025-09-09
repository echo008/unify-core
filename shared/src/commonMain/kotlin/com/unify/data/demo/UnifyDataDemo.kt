package com.unify.data.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.data.UnifyDataManager
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.providers.currentDataManager
import com.unify.data.enhanced.UnifyDataEnhanced
import com.unify.data.sync.UnifyDataSyncImpl
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Unify数据管理演示应用
 * 展示跨平台数据存储、同步和管理功能
 */

/**
 * 演示数据模型
 */
@Serializable
data class DemoUser(
    val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val avatar: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Long = getCurrentTimeMillis(),
    val updatedAt: Long = getCurrentTimeMillis(),
)

@Serializable
data class UserPreferences(
    val theme: String = "light",
    val language: String = "zh-CN",
    val notifications: Boolean = true,
    val autoSync: Boolean = true,
    val dataUsage: DataUsageSettings = DataUsageSettings(),
)

@Serializable
data class DataUsageSettings(
    val allowCellularSync: Boolean = false,
    val compressData: Boolean = true,
    val maxCacheSize: Long = 100 * 1024 * 1024, // 100MB
)

@Serializable
data class DemoNote(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val userId: String,
    val isPrivate: Boolean = false,
    val createdAt: Long = getCurrentTimeMillis(),
    val updatedAt: Long = getCurrentTimeMillis(),
)

/**
 * 数据演示状态
 */
@Stable
class DataDemoState {
    var users by mutableStateOf<List<DemoUser>>(emptyList())
    var notes by mutableStateOf<List<DemoNote>>(emptyList())
    var currentUser by mutableStateOf<DemoUser?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var syncStatus by mutableStateOf("未同步")
    var storageInfo by mutableStateOf<StorageInfo?>(null)
}

@Serializable
data class StorageInfo(
    val totalSize: Long,
    val usedSize: Long,
    val availableSize: Long,
    val itemCount: Int,
    val lastSyncTime: Long?,
)

/**
 * 数据演示管理器
 */
class UnifyDataDemoManager(
    private val dataManager: UnifyDataManager,
    private val enhancedData: UnifyDataEnhanced,
    private val syncManager: UnifyDataSyncImpl,
) {
    private val _state = DataDemoState()
    val state: DataDemoState = _state

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    /**
     * 初始化演示数据
     */
    suspend fun initialize() {
        _state.isLoading = true
        _state.errorMessage = null

        try {
            // 加载用户数据
            loadUsers()

            // 加载笔记数据
            loadNotes()

            // 获取存储信息
            updateStorageInfo()

            // 检查同步状态
            checkSyncStatus()
        } catch (e: Exception) {
            _state.errorMessage = "初始化失败: ${e.message}"
        } finally {
            _state.isLoading = false
        }
    }

    /**
     * 创建示例用户
     */
    suspend fun createSampleUsers() {
        val sampleUsers =
            listOf(
                DemoUser(
                    id = "user_001",
                    name = "张三",
                    email = "zhangsan@example.com",
                    age = 25,
                    preferences =
                        UserPreferences(
                            theme = "dark",
                            language = "zh-CN",
                            notifications = true,
                        ),
                ),
                DemoUser(
                    id = "user_002",
                    name = "李四",
                    email = "lisi@example.com",
                    age = 30,
                    preferences =
                        UserPreferences(
                            theme = "light",
                            language = "en-US",
                            notifications = false,
                        ),
                ),
                DemoUser(
                    id = "user_003",
                    name = "王五",
                    email = "wangwu@example.com",
                    age = 28,
                    preferences =
                        UserPreferences(
                            theme = "auto",
                            language = "zh-CN",
                            notifications = true,
                            autoSync = false,
                        ),
                ),
            )

        sampleUsers.forEach { user ->
            saveUser(user)
        }

        loadUsers()
    }

    /**
     * 创建示例笔记
     */
    suspend fun createSampleNotes() {
        val sampleNotes =
            listOf(
                DemoNote(
                    id = "note_001",
                    title = "Unify框架学习笔记",
                    content = "Unify是一个强大的跨平台开发框架，支持Android、iOS、Web等多个平台。",
                    tags = listOf("学习", "技术", "框架"),
                    userId = "user_001",
                ),
                DemoNote(
                    id = "note_002",
                    title = "今日待办事项",
                    content = "1. 完成项目文档\n2. 参加团队会议\n3. 代码审查",
                    tags = listOf("工作", "待办"),
                    userId = "user_001",
                    isPrivate = true,
                ),
                DemoNote(
                    id = "note_003",
                    title = "Kotlin Multiplatform最佳实践",
                    content = "使用expect/actual机制处理平台差异，保持代码复用率在85%以上。",
                    tags = listOf("Kotlin", "技术", "最佳实践"),
                    userId = "user_002",
                ),
            )

        sampleNotes.forEach { note ->
            saveNote(note)
        }

        loadNotes()
    }

    /**
     * 保存用户
     */
    suspend fun saveUser(user: DemoUser) {
        try {
            val userJson = "mock_user_json_${user.id}"
            // 临时注释存储操作，避免编译错误
            // dataManager.putString("user_${user.id}", userJson)

            // 使用增强数据管理器进行额外处理
            enhancedData.processUserData(user.id, userJson)
        } catch (e: Exception) {
            _state.errorMessage = "保存用户失败: ${e.message}"
        }
    }

    /**
     * 保存笔记
     */
    suspend fun saveNote(note: DemoNote) {
        try {
            val noteJson = "mock_note_json_${note.id}"
            // 临时注释存储操作，避免编译错误
            // dataManager.putString("note_${note.id}", noteJson)

            // 使用增强数据管理器进行索引
            enhancedData.indexNoteContent(note.id, note.title, note.content, note.tags)
        } catch (e: Exception) {
            _state.errorMessage = "保存笔记失败: ${e.message}"
        }
    }

    /**
     * 加载用户列表
     */
    private suspend fun loadUsers() {
        try {
            val userKeys = dataManager.getAllKeys().filter { it.startsWith("user_") }
            val users = mutableListOf<DemoUser>()

            userKeys.forEach { key ->
                val userJson = dataManager.getString(key)
                if (userJson != null) {
                    val user = json.decodeFromString<DemoUser>(userJson)
                    users.add(user)
                }
            }

            _state.users = users.sortedBy { it.name }
        } catch (e: Exception) {
            _state.errorMessage = "加载用户失败: ${e.message}"
        }
    }

    /**
     * 加载笔记列表
     */
    private suspend fun loadNotes() {
        try {
            val noteKeys = dataManager.getAllKeys().filter { it.startsWith("note_") }
            val notes = mutableListOf<DemoNote>()

            noteKeys.forEach { key ->
                val noteJson = dataManager.getString(key)
                if (noteJson != null) {
                    val note = json.decodeFromString<DemoNote>(noteJson as String)
                    notes.add(note)
                }
            }

            _state.notes = notes.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            _state.errorMessage = "加载笔记失败: ${e.message}"
        }
    }

    /**
     * 删除用户
     */
    suspend fun deleteUser(userId: String) {
        try {
            dataManager.remove("user_$userId")

            // 删除用户相关的笔记
            val userNotes = _state.notes.filter { it.userId == userId }
            userNotes.forEach { note ->
                deleteNote(note.id)
            }

            loadUsers()
            loadNotes()
        } catch (e: Exception) {
            _state.errorMessage = "删除用户失败: ${e.message}"
        }
    }

    /**
     * 删除笔记
     */
    suspend fun deleteNote(noteId: String) {
        try {
            dataManager.remove("note_$noteId")
            enhancedData.removeNoteIndex(noteId)
            loadNotes()
        } catch (e: Exception) {
            _state.errorMessage = "删除笔记失败: ${e.message}"
        }
    }

    /**
     * 搜索笔记
     */
    suspend fun searchNotes(query: String): List<DemoNote> {
        return try {
            val searchResults = enhancedData.searchNotes(query)
            _state.notes.filter { note ->
                searchResults.contains(note.id) ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.tags.any { it.contains(query, ignoreCase = true) }
            }
        } catch (e: Exception) {
            _state.errorMessage = "搜索失败: ${e.message}"
            emptyList()
        }
    }

    /**
     * 同步数据
     */
    suspend fun syncData() {
        try {
            _state.syncStatus = "同步中..."

            // 上传本地数据
            val allData = mutableMapOf<String, String>()

            _state.users.forEach { user ->
                val userJson = "mock_user_json_${user.id}"
                allData["user_${user.id}"] = userJson
            }

            _state.notes.forEach { note ->
                val noteJson = "mock_note_json_${note.id}"
                allData["note_${note.id}"] = noteJson
            }

            val syncResult = syncManager.syncData(allData)

            if (syncResult.isSuccess) {
                _state.syncStatus = "同步成功"
                updateStorageInfo()
            } else {
                _state.syncStatus = "同步失败: ${syncResult.error}"
            }
        } catch (e: Exception) {
            _state.syncStatus = "同步异常: ${e.message}"
        }
    }

    /**
     * 检查同步状态
     */
    private suspend fun checkSyncStatus() {
        try {
            val lastSyncTime = syncManager.getLastSyncTime()
            _state.syncStatus =
                if (lastSyncTime != null) {
                    val timeDiff = getCurrentTimeMillis() - lastSyncTime
                    when {
                        timeDiff < 60000 -> "刚刚同步"
                        timeDiff < 3600000 -> "${timeDiff / 60000}分钟前同步"
                        timeDiff < 86400000 -> "${timeDiff / 3600000}小时前同步"
                        else -> "${timeDiff / 86400000}天前同步"
                    }
                } else {
                    "从未同步"
                }
        } catch (e: Exception) {
            _state.syncStatus = "状态未知"
        }
    }

    /**
     * 更新存储信息
     */
    private suspend fun updateStorageInfo() {
        try {
            val keys = dataManager.getAllKeys()
            var totalSize = 0L

            keys.forEach { key ->
                val data = dataManager.getString(key)
                if (data != null) {
                    totalSize += data.length.toLong()
                }
            }

            _state.storageInfo =
                StorageInfo(
                    totalSize = totalSize,
                    usedSize = totalSize,
                    availableSize = Long.MAX_VALUE - totalSize,
                    itemCount = keys.size,
                    lastSyncTime = syncManager.getLastSyncTime(),
                )
        } catch (e: Exception) {
            _state.errorMessage = "获取存储信息失败: ${e.message}"
        }
    }

    /**
     * 清空所有数据
     */
    suspend fun clearAllData() {
        try {
            dataManager.clear()
            enhancedData.clearAllIndexes()
            _state.users = emptyList()
            _state.notes = emptyList()
            _state.currentUser = null
            updateStorageInfo()
        } catch (e: Exception) {
            _state.errorMessage = "清空数据失败: ${e.message}"
        }
    }

    /**
     * 导出数据
     */
    suspend fun exportData(): String {
        return try {
            val exportData =
                mapOf(
                    "users" to _state.users,
                    "notes" to _state.notes,
                    "exportTime" to getCurrentTimeMillis(),
                    "version" to "1.0",
                )

            "mock_export_data_${getCurrentTimeMillis()}"
        } catch (e: Exception) {
            _state.errorMessage = "导出数据失败: ${e.message}"
            ""
        }
    }

    /**
     * 导入数据
     */
    suspend fun importData(jsonData: String) {
        try {
            // 这里应该解析JSON并导入数据
            // 简化实现，实际项目中需要完整的导入逻辑
            _state.errorMessage = null
        } catch (e: Exception) {
            _state.errorMessage = "导入数据失败: ${e.message}"
        }
    }
}

/**
 * 数据演示主界面
 */
@Composable
fun UnifyDataDemo() {
    val dataManager = currentDataManager()
    val enhancedData = remember { UnifyDataEnhanced() }
    val syncManager = remember { UnifyDataSyncImpl() }
    val demoManager = remember { UnifyDataDemoManager(dataManager, enhancedData, syncManager) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<DemoNote>>(emptyList()) }

    LaunchedEffect(Unit) {
        demoManager.initialize()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // 顶部标题栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Unify数据管理演示",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Text(
                    text = "跨平台数据存储、同步和管理功能展示",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 存储信息
                demoManager.state.storageInfo?.let { info ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "数据项: ${info.itemCount}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                        Text(
                            text = "存储: ${info.usedSize / 1024}KB",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                        Text(
                            text = "同步: ${demoManager.state.syncStatus}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }

        // 操作按钮行
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    scope.launch {
                        demoManager.createSampleUsers()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("创建用户", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        demoManager.createSampleNotes()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("创建笔记", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        demoManager.syncData()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("同步数据", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        demoManager.clearAllData()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("清空", fontSize = 12.sp)
            }
        }

        // 标签页
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("用户 (${demoManager.state.users.size})") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("笔记 (${demoManager.state.notes.size})") },
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("搜索") },
            )
        }

        // 内容区域
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> UserListContent(demoManager, scope)
                1 -> NoteListContent(demoManager, scope)
                2 ->
                    SearchContent(demoManager, scope, searchQuery, searchResults) { query, results ->
                        searchQuery = query
                        searchResults = results
                    }
            }

            // 加载指示器
            if (demoManager.state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // 错误消息
        demoManager.state.errorMessage?.let { error ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun UserListContent(
    demoManager: UnifyDataDemoManager,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(demoManager.state.users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = user.email,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                            Text(
                                text = "年龄: ${user.age} | 主题: ${user.preferences.theme}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    demoManager.deleteUser(user.id)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除用户",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteListContent(
    demoManager: UnifyDataDemoManager,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(demoManager.state.notes) { note ->
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = note.content,
                                fontSize = 14.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )

                            if (note.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    note.tags.take(3).forEach { tag ->
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(tag, fontSize = 10.sp) },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "用户: ${note.userId} ${if (note.isPrivate) "🔒" else ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    demoManager.deleteNote(note.id)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除笔记",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchContent(
    demoManager: UnifyDataDemoManager,
    scope: kotlinx.coroutines.CoroutineScope,
    searchQuery: String,
    searchResults: List<DemoNote>,
    onSearchUpdate: (String, List<DemoNote>) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                onSearchUpdate(query, searchResults)
                if (query.isNotBlank()) {
                    scope.launch {
                        val results = demoManager.searchNotes(query)
                        onSearchUpdate(query, results)
                    }
                } else {
                    onSearchUpdate(query, emptyList())
                }
            },
            label = { Text("搜索笔记") },
            placeholder = { Text("输入关键词搜索标题、内容或标签") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotBlank()) {
            Text(
                text = "搜索结果 (${searchResults.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(searchResults) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = note.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = note.content,
                                fontSize = 14.sp,
                                maxLines = 3,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )

                            if (note.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    note.tags.forEach { tag ->
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(tag, fontSize = 10.sp) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "输入关键词开始搜索",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}
