package com.unify.android

import android.app.Application
import com.unify.di.allModules
import com.unify.database.UnifyDatabaseFactory
import com.unify.database.UnifyDatabaseDriverFactory
import com.unify.error.UnifyErrorHandler
import com.unify.platform.UnifyPlatformManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Unify Android应用程序类
 * 负责初始化框架核心组件
 */
class UnifyAndroidApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Koin依赖注入
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@UnifyAndroidApplication)
            modules(allModules)
        }
        
        // 初始化数据库
        val databaseDriverFactory = UnifyDatabaseDriverFactory(this)
        UnifyDatabaseFactory.createDatabase(databaseDriverFactory)
        
        // 初始化平台管理器
        val platformManager = UnifyPlatformManager()
        
        // 初始化错误处理
        UnifyErrorHandler.initialize(
            database = UnifyDatabaseFactory.database,
            platformManager = platformManager
        )
        
        // 初始化性能监控
        initializePerformanceMonitoring()
        
        println("Unify Android Application initialized successfully!")
    }
    
    private fun initializePerformanceMonitoring() {
        // 这里可以初始化Android特定的性能监控
        // 例如：Firebase Performance、自定义性能监控等
    }
}
