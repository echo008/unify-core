package com.unify.web.di

import com.unify.database.DatabaseDriverFactory
import com.unify.database.DatabaseProvider
import com.unify.database.UnifyDatabaseRepository
import com.unify.network.NetworkClientFactory
import com.unify.network.UnifyNetworkServiceImpl
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

/**
 * Web平台依赖注入模块
 */
object WebDI {
    
    // 数据库相关
    private val databaseDriverFactory = WebDatabaseDriverFactory()
    private val database = DatabaseProvider.createDatabase(databaseDriverFactory)
    val databaseRepository = UnifyDatabaseRepository(database)
    
    // 网络服务
    private val httpClient = NetworkClientFactory.createHttpClient()
    val networkService = UnifyNetworkServiceImpl(httpClient)
}

/**
 * Web SQLite驱动工厂
 */
class WebDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker(
                js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
            )
        ).also { driver ->
            com.unify.database.UnifyDatabase.Schema.create(driver)
        }
    }
}
