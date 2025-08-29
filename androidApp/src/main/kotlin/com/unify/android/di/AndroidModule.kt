package com.unify.android.di

import android.content.Context
import com.unify.database.DatabaseDriverFactory
import com.unify.database.DatabaseProvider
import com.unify.database.UnifyDatabaseRepository
import com.unify.network.NetworkClientFactory
import com.unify.network.UnifyNetworkServiceImpl
import com.unify.platform.UnifyPlatformManager
import com.unify.storage.UnifyStorage
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Android平台依赖注入模块
 */
class AndroidModule(private val context: Context) {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // 数据库相关
    private val databaseDriverFactory = AndroidDatabaseDriverFactory(context)
    private val database = DatabaseProvider.createDatabase(databaseDriverFactory)
    val databaseRepository = UnifyDatabaseRepository(database)
    
    // 网络服务
    private val httpClient = NetworkClientFactory.createHttpClient()
    val networkService = UnifyNetworkServiceImpl(httpClient)
    
    // 平台管理器
    val platformManager = UnifyPlatformManager()
    
    // 存储服务
    val storage = UnifyStorage()
    
    fun getApplicationScope(): CoroutineScope = applicationScope
}

/**
 * Android SQLite驱动工厂
 */
class AndroidDatabaseDriverFactory(
    private val context: Context
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = com.unify.database.UnifyDatabase.Schema,
            context = context,
            name = "unify.db"
        )
    }
}

/**
 * 依赖注入容器
 */
object AndroidDI {
    private var _module: AndroidModule? = null
    
    fun initialize(context: Context) {
        _module = AndroidModule(context.applicationContext)
    }
    
    val module: AndroidModule
        get() = _module ?: throw IllegalStateException("AndroidDI not initialized")
}
