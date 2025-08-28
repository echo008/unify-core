package com.unify.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.unify.database.UnifyDatabase
import org.w3c.dom.Worker

/**
 * Web平台SQLDelight数据库驱动实现
 */
actual class UnifyDatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker(
                js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)""")
            )
        ).also { driver ->
            UnifyDatabase.Schema.create(driver)
        }
    }
}
