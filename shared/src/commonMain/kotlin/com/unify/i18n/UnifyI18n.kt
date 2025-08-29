package com.unify.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 统一国际化系统
 * 支持多语言切换、动态加载、参数化翻译
 */
object UnifyI18n {
    private val _currentLocale = MutableStateFlow(Locale.CHINESE)
    val currentLocale: StateFlow<Locale> = _currentLocale.asStateFlow()
    
    private val translations = mutableMapOf<Locale, Map<String, String>>()
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * 初始化国际化系统
     */
    fun initialize(defaultLocale: Locale = Locale.CHINESE) {
        _currentLocale.value = defaultLocale
        loadDefaultTranslations()
    }
    
    /**
     * 切换语言
     */
    fun setLocale(locale: Locale) {
        _currentLocale.value = locale
    }
    
    /**
     * 获取翻译文本
     */
    fun getString(key: String, vararg args: Any): String {
        val locale = _currentLocale.value
        val translation = translations[locale]?.get(key) ?: key
        
        return if (args.isNotEmpty()) {
            formatString(translation, *args)
        } else {
            translation
        }
    }
    
    /**
     * 获取翻译文本（带默认值）
     */
    fun getString(key: String, defaultValue: String, vararg args: Any): String {
        val locale = _currentLocale.value
        val translation = translations[locale]?.get(key) ?: defaultValue
        
        return if (args.isNotEmpty()) {
            formatString(translation, *args)
        } else {
            translation
        }
    }
    
    /**
     * 添加翻译资源
     */
    fun addTranslations(locale: Locale, translationMap: Map<String, String>) {
        val existing = translations[locale] ?: emptyMap()
        translations[locale] = existing + translationMap
    }
    
    /**
     * 从JSON加载翻译资源
     */
    fun loadTranslationsFromJson(locale: Locale, jsonString: String) {
        try {
            val translationMap = json.decodeFromString<Map<String, String>>(jsonString)
            addTranslations(locale, translationMap)
        } catch (e: Exception) {
            println("Failed to load translations for $locale: ${e.message}")
        }
    }
    
    /**
     * 检查是否支持指定语言
     */
    fun isLocaleSupported(locale: Locale): Boolean {
        return translations.containsKey(locale)
    }
    
    /**
     * 获取所有支持的语言
     */
    fun getSupportedLocales(): List<Locale> {
        return translations.keys.toList()
    }
    
    private fun formatString(template: String, vararg args: Any): String {
        var result = template
        args.forEachIndexed { index, arg ->
            result = result.replace("{$index}", arg.toString())
        }
        return result
    }
    
    private fun loadDefaultTranslations() {
        // 中文翻译
        addTranslations(Locale.CHINESE, mapOf(
            "app.name" to "Unify KMP",
            "common.ok" to "确定",
            "common.cancel" to "取消",
            "common.save" to "保存",
            "common.delete" to "删除",
            "common.edit" to "编辑",
            "common.add" to "添加",
            "common.refresh" to "刷新",
            "common.loading" to "加载中...",
            "common.error" to "错误",
            "common.success" to "成功",
            "common.retry" to "重试",
            "common.search" to "搜索",
            "common.clear" to "清除",
            "common.back" to "返回",
            "common.next" to "下一步",
            "common.previous" to "上一步",
            "common.close" to "关闭",
            
            "user.list.title" to "用户列表",
            "user.list.empty" to "暂无用户数据",
            "user.list.search.placeholder" to "搜索用户...",
            "user.create.title" to "创建用户",
            "user.edit.title" to "编辑用户",
            "user.delete.confirm" to "确定要删除用户 {0} 吗？",
            "user.form.username" to "用户名",
            "user.form.email" to "邮箱",
            "user.form.displayName" to "显示名称",
            "user.form.username.required" to "用户名不能为空",
            "user.form.email.required" to "邮箱不能为空",
            "user.form.email.invalid" to "邮箱格式不正确",
            
            "network.error.connection" to "网络连接失败",
            "network.error.timeout" to "请求超时",
            "network.error.server" to "服务器错误",
            "network.status.connected" to "网络已连接",
            "network.status.disconnected" to "网络未连接"
        ))
        
        // 英文翻译
        addTranslations(Locale.ENGLISH, mapOf(
            "app.name" to "Unify KMP",
            "common.ok" to "OK",
            "common.cancel" to "Cancel",
            "common.save" to "Save",
            "common.delete" to "Delete",
            "common.edit" to "Edit",
            "common.add" to "Add",
            "common.refresh" to "Refresh",
            "common.loading" to "Loading...",
            "common.error" to "Error",
            "common.success" to "Success",
            "common.retry" to "Retry",
            "common.search" to "Search",
            "common.clear" to "Clear",
            "common.back" to "Back",
            "common.next" to "Next",
            "common.previous" to "Previous",
            "common.close" to "Close",
            
            "user.list.title" to "User List",
            "user.list.empty" to "No users found",
            "user.list.search.placeholder" to "Search users...",
            "user.create.title" to "Create User",
            "user.edit.title" to "Edit User",
            "user.delete.confirm" to "Are you sure you want to delete user {0}?",
            "user.form.username" to "Username",
            "user.form.email" to "Email",
            "user.form.displayName" to "Display Name",
            "user.form.username.required" to "Username is required",
            "user.form.email.required" to "Email is required",
            "user.form.email.invalid" to "Invalid email format",
            
            "network.error.connection" to "Connection failed",
            "network.error.timeout" to "Request timeout",
            "network.error.server" to "Server error",
            "network.status.connected" to "Network connected",
            "network.status.disconnected" to "Network disconnected"
        ))
        
        // 日文翻译
        addTranslations(Locale.JAPANESE, mapOf(
            "app.name" to "Unify KMP",
            "common.ok" to "OK",
            "common.cancel" to "キャンセル",
            "common.save" to "保存",
            "common.delete" to "削除",
            "common.edit" to "編集",
            "common.add" to "追加",
            "common.refresh" to "更新",
            "common.loading" to "読み込み中...",
            "common.error" to "エラー",
            "common.success" to "成功",
            "common.retry" to "再試行",
            "common.search" to "検索",
            "common.clear" to "クリア",
            "common.back" to "戻る",
            "common.next" to "次へ",
            "common.previous" to "前へ",
            "common.close" to "閉じる",
            
            "user.list.title" to "ユーザーリスト",
            "user.list.empty" to "ユーザーが見つかりません",
            "user.list.search.placeholder" to "ユーザーを検索...",
            "user.create.title" to "ユーザー作成",
            "user.edit.title" to "ユーザー編集",
            "user.delete.confirm" to "ユーザー {0} を削除しますか？",
            "user.form.username" to "ユーザー名",
            "user.form.email" to "メール",
            "user.form.displayName" to "表示名",
            "user.form.username.required" to "ユーザー名は必須です",
            "user.form.email.required" to "メールは必須です",
            "user.form.email.invalid" to "メール形式が正しくありません",
            
            "network.error.connection" to "接続に失敗しました",
            "network.error.timeout" to "リクエストタイムアウト",
            "network.error.server" to "サーバーエラー",
            "network.status.connected" to "ネットワーク接続済み",
            "network.status.disconnected" to "ネットワーク未接続"
        ))
    }
}

/**
 * 支持的语言枚举
 */
@Serializable
enum class Locale(val code: String, val displayName: String) {
    CHINESE("zh-CN", "中文"),
    ENGLISH("en-US", "English"),
    JAPANESE("ja-JP", "日本語");
    
    companion object {
        fun fromCode(code: String): Locale? {
            return values().find { it.code == code }
        }
    }
}

/**
 * 国际化资源管理器
 */
class I18nResourceManager {
    
    /**
     * 从平台特定位置加载翻译资源
     */
    expect suspend fun loadTranslationsFromAssets(locale: Locale, fileName: String): String?
    
    /**
     * 获取系统默认语言
     */
    expect fun getSystemLocale(): Locale
    
    /**
     * 保存用户选择的语言设置
     */
    expect suspend fun saveLocalePreference(locale: Locale)
    
    /**
     * 加载用户选择的语言设置
     */
    expect suspend fun loadLocalePreference(): Locale?
}

/**
 * Compose 国际化扩展
 */
@androidx.compose.runtime.Composable
fun rememberI18nString(key: String, vararg args: Any): String {
    val locale by UnifyI18n.currentLocale.collectAsState()
    return remember(key, locale, *args) {
        UnifyI18n.getString(key, *args)
    }
}

@androidx.compose.runtime.Composable
fun rememberI18nString(key: String, defaultValue: String, vararg args: Any): String {
    val locale by UnifyI18n.currentLocale.collectAsState()
    return remember(key, defaultValue, locale, *args) {
        UnifyI18n.getString(key, defaultValue, *args)
    }
}

/**
 * 国际化工具函数
 */
object I18nUtils {
    
    /**
     * 格式化日期
     */
    fun formatDate(timestamp: Long, locale: Locale = UnifyI18n.currentLocale.value): String {
        // 这里应该根据不同语言格式化日期
        // 简化实现
        return when (locale) {
            Locale.CHINESE -> "yyyy年MM月dd日"
            Locale.ENGLISH -> "MM/dd/yyyy"
            Locale.JAPANESE -> "yyyy年MM月dd日"
        }
    }
    
    /**
     * 格式化数字
     */
    fun formatNumber(number: Double, locale: Locale = UnifyI18n.currentLocale.value): String {
        // 这里应该根据不同语言格式化数字
        // 简化实现
        return when (locale) {
            Locale.CHINESE -> String.format("%.2f", number)
            Locale.ENGLISH -> String.format("%.2f", number)
            Locale.JAPANESE -> String.format("%.2f", number)
        }
    }
    
    /**
     * 获取复数形式
     */
    fun getPlural(key: String, count: Int, locale: Locale = UnifyI18n.currentLocale.value): String {
        val pluralKey = when {
            count == 0 -> "${key}.zero"
            count == 1 -> "${key}.one"
            else -> "${key}.other"
        }
        return UnifyI18n.getString(pluralKey, count.toString())
    }
}
