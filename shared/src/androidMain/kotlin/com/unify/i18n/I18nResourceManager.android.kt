package com.unify.i18n

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale as AndroidLocale

/**
 * Android平台国际化资源管理器实现
 */
actual class I18nResourceManager(private val context: Context) {
    
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences("unify_i18n_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * 从Android Assets加载翻译资源
     */
    actual suspend fun loadTranslationsFromAssets(locale: Locale, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val assetPath = "i18n/${locale.code}/$fileName"
                context.assets.open(assetPath).bufferedReader().use { reader ->
                    reader.readText()
                }
            } catch (e: Exception) {
                println("Failed to load translations from assets: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 获取Android系统默认语言
     */
    actual fun getSystemLocale(): Locale {
        val systemLocale = AndroidLocale.getDefault()
        val languageTag = "${systemLocale.language}-${systemLocale.country}"
        
        return Locale.fromCode(languageTag) ?: when (systemLocale.language) {
            "zh" -> Locale.CHINESE
            "ja" -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }
    }
    
    /**
     * 保存用户选择的语言设置到SharedPreferences
     */
    actual suspend fun saveLocalePreference(locale: Locale) {
        withContext(Dispatchers.IO) {
            preferences.edit()
                .putString("selected_locale", locale.code)
                .apply()
        }
    }
    
    /**
     * 从SharedPreferences加载用户选择的语言设置
     */
    actual suspend fun loadLocalePreference(): Locale? {
        return withContext(Dispatchers.IO) {
            val localeCode = preferences.getString("selected_locale", null)
            localeCode?.let { Locale.fromCode(it) }
        }
    }
}

/**
 * Android平台国际化工具扩展
 */
object AndroidI18nUtils {
    
    /**
     * 更新应用语言配置
     */
    fun updateAppLocale(context: Context, locale: Locale) {
        val androidLocale = when (locale) {
            Locale.CHINESE -> AndroidLocale.SIMPLIFIED_CHINESE
            Locale.JAPANESE -> AndroidLocale.JAPANESE
            Locale.ENGLISH -> AndroidLocale.ENGLISH
        }
        
        val config = context.resources.configuration
        config.setLocale(androidLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    /**
     * 获取Android资源字符串
     */
    fun getAndroidString(context: Context, resId: Int, locale: Locale): String {
        val androidLocale = when (locale) {
            Locale.CHINESE -> AndroidLocale.SIMPLIFIED_CHINESE
            Locale.JAPANESE -> AndroidLocale.JAPANESE
            Locale.ENGLISH -> AndroidLocale.ENGLISH
        }
        
        val config = context.resources.configuration
        val originalLocale = config.locale
        config.setLocale(androidLocale)
        
        val localizedContext = context.createConfigurationContext(config)
        val result = localizedContext.getString(resId)
        
        // 恢复原始语言设置
        config.setLocale(originalLocale)
        
        return result
    }
}
