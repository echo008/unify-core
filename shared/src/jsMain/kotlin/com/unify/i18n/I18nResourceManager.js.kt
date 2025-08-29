package com.unify.i18n

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.get
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

/**
 * Web平台国际化资源管理器实现
 */
actual class I18nResourceManager {
    
    /**
     * 从Web静态资源加载翻译资源
     */
    actual suspend fun loadTranslationsFromAssets(locale: Locale, fileName: String): String? {
        return try {
            val resourceUrl = "i18n/${locale.code}/$fileName"
            val response = window.fetch(resourceUrl).await()
            
            if (response.ok) {
                response.text().await()
            } else {
                println("Failed to load translations from $resourceUrl: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Failed to load translations from web assets: ${e.message}")
            null
        }
    }
    
    /**
     * 获取浏览器系统默认语言
     */
    actual fun getSystemLocale(): Locale {
        val browserLanguage = window.navigator.language
        
        return Locale.fromCode(browserLanguage) ?: when {
            browserLanguage.startsWith("zh") -> Locale.CHINESE
            browserLanguage.startsWith("ja") -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }
    }
    
    /**
     * 保存用户选择的语言设置到localStorage
     */
    actual suspend fun saveLocalePreference(locale: Locale) {
        localStorage.setItem("unify_selected_locale", locale.code)
    }
    
    /**
     * 从localStorage加载用户选择的语言设置
     */
    actual suspend fun loadLocalePreference(): Locale? {
        val localeCode = localStorage.getItem("unify_selected_locale")
        return localeCode?.let { Locale.fromCode(it) }
    }
}

/**
 * Web平台国际化工具扩展
 */
object WebI18nUtils {
    
    /**
     * 获取浏览器支持的语言列表
     */
    fun getBrowserLanguages(): List<String> {
        return window.navigator.languages.asList()
    }
    
    /**
     * 设置HTML文档语言属性
     */
    fun setDocumentLanguage(locale: Locale) {
        val htmlElement = window.document.documentElement
        htmlElement?.setAttribute("lang", locale.code)
    }
    
    /**
     * 格式化Web本地化日期
     */
    fun formatLocalizedDate(timestamp: Long, locale: Locale): String {
        val date = js("new Date(timestamp)")
        val options = js("""{
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }""")
        
        return date.toLocaleDateString(locale.code, options) as String
    }
    
    /**
     * 格式化Web本地化数字
     */
    fun formatLocalizedNumber(number: Double, locale: Locale): String {
        val options = js("""{
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }""")
        
        return number.asDynamic().toLocaleString(locale.code, options) as String
    }
    
    /**
     * 检测用户首选语言
     */
    fun detectPreferredLocale(): Locale {
        val browserLanguages = getBrowserLanguages()
        
        for (lang in browserLanguages) {
            Locale.fromCode(lang)?.let { return it }
        }
        
        // 回退到系统语言检测
        return I18nResourceManager().getSystemLocale()
    }
    
    /**
     * 动态加载语言包
     */
    suspend fun loadLanguagePack(locale: Locale): Map<String, String>? {
        return try {
            val resourceManager = I18nResourceManager()
            val jsonContent = resourceManager.loadTranslationsFromAssets(locale, "translations.json")
            
            jsonContent?.let {
                // 简单的JSON解析，实际项目中应使用kotlinx.serialization
                val translations = mutableMapOf<String, String>()
                // 这里应该解析JSON内容
                translations
            }
        } catch (e: Exception) {
            println("Failed to load language pack for $locale: ${e.message}")
            null
        }
    }
}
