package com.unify.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.unify.database.UnifyDatabase

/**
 * iOS平台SQLDelight数据库驱动实现
 */
actual class UnifyDatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = UnifyDatabase.Schema,
            name = "unify_database.db"
        )
    }
}
