package com.unify.i18n

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.darwin.NSObject

/**
 * iOS平台国际化资源管理器实现
 */
actual class I18nResourceManager {
    
    /**
     * 从iOS Bundle加载翻译资源
     */
    actual suspend fun loadTranslationsFromAssets(locale: Locale, fileName: String): String? {
        return withContext(Dispatchers.Default) {
            try {
                val bundle = NSBundle.mainBundle
                val resourcePath = bundle.pathForResource(fileName, "json", "i18n/${locale.code}")
                
                resourcePath?.let { path ->
                    val fileContent = NSString.stringWithContentsOfFile(
                        path = path,
                        encoding = NSUTF8StringEncoding,
                        error = null
                    )
                    fileContent?.toString()
                }
            } catch (e: Exception) {
                println("Failed to load translations from iOS bundle: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 获取iOS系统默认语言
     */
    actual fun getSystemLocale(): Locale {
        val preferredLanguages = NSLocale.preferredLanguages
        val primaryLanguage = preferredLanguages.firstOrNull() as? String ?: "en-US"
        
        return Locale.fromCode(primaryLanguage) ?: when {
            primaryLanguage.startsWith("zh") -> Locale.CHINESE
            primaryLanguage.startsWith("ja") -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }
    }
    
    /**
     * 保存用户选择的语言设置到NSUserDefaults
     */
    actual suspend fun saveLocalePreference(locale: Locale) {
        withContext(Dispatchers.Default) {
            val userDefaults = NSUserDefaults.standardUserDefaults
            userDefaults.setObject(locale.code, "unify_selected_locale")
            userDefaults.synchronize()
        }
    }
    
    /**
     * 从NSUserDefaults加载用户选择的语言设置
     */
    actual suspend fun loadLocalePreference(): Locale? {
        return withContext(Dispatchers.Default) {
            val userDefaults = NSUserDefaults.standardUserDefaults
            val localeCode = userDefaults.stringForKey("unify_selected_locale")
            localeCode?.let { Locale.fromCode(it) }
        }
    }
}

/**
 * iOS平台国际化工具扩展
 */
object IOSi18nUtils {
    
    /**
     * 获取iOS本地化字符串
     */
    fun getLocalizedString(key: String, locale: Locale): String {
        val bundle = NSBundle.mainBundle
        val localeIdentifier = when (locale) {
            Locale.CHINESE -> "zh-Hans"
            Locale.JAPANESE -> "ja"
            Locale.ENGLISH -> "en"
        }
        
        // 尝试获取特定语言的Bundle
        val localizedBundle = bundle.pathForResource(localeIdentifier, "lproj")?.let { path ->
            NSBundle.bundleWithPath(path)
        } ?: bundle
        
        return localizedBundle.localizedStringForKey(key, key, null)
    }
    
    /**
     * 格式化iOS本地化日期
     */
    fun formatLocalizedDate(timestamp: Long, locale: Locale): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble() / 1000.0)
        val formatter = NSDateFormatter()
        
        val localeIdentifier = when (locale) {
            Locale.CHINESE -> "zh_CN"
            Locale.JAPANESE -> "ja_JP"
            Locale.ENGLISH -> "en_US"
        }
        
        formatter.locale = NSLocale.localeWithLocaleIdentifier(localeIdentifier)
        formatter.dateStyle = NSDateFormatterMediumStyle
        formatter.timeStyle = NSDateFormatterShortStyle
        
        return formatter.stringFromDate(date)
    }
    
    /**
     * 格式化iOS本地化数字
     */
    fun formatLocalizedNumber(number: Double, locale: Locale): String {
        val formatter = NSNumberFormatter()
        
        val localeIdentifier = when (locale) {
            Locale.CHINESE -> "zh_CN"
            Locale.JAPANESE -> "ja_JP"
            Locale.ENGLISH -> "en_US"
        }
        
        formatter.locale = NSLocale.localeWithLocaleIdentifier(localeIdentifier)
        formatter.numberStyle = NSNumberFormatterDecimalStyle
        formatter.maximumFractionDigits = 2u
        
        return formatter.stringFromNumber(NSNumber.numberWithDouble(number)) ?: number.toString()
    }
}
